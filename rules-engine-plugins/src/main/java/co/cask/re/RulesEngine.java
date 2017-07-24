package co.cask.re;

import co.cask.cdap.api.annotation.Description;
import co.cask.cdap.api.annotation.Macro;
import co.cask.cdap.api.annotation.Name;
import co.cask.cdap.api.annotation.Plugin;
import co.cask.cdap.api.data.format.StructuredRecord;
import co.cask.cdap.api.data.schema.Schema;
import co.cask.cdap.api.plugin.PluginConfig;
import co.cask.cdap.etl.api.Emitter;
import co.cask.cdap.etl.api.InvalidEntry;
import co.cask.cdap.etl.api.PipelineConfigurer;
import co.cask.cdap.etl.api.Transform;
import co.cask.cdap.etl.api.TransformContext;
import co.cask.directives.aggregates.DefaultTransientStore;
import co.cask.wrangler.api.ExecutorContext;
import co.cask.wrangler.api.Row;
import co.cask.wrangler.api.TransientStore;
import co.cask.wrangler.utils.RecordConvertor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class description here.
 */
@Plugin(type = "transform")
@Name("RulesEngine")
@Description("A Rule Engine that uses Inference to determines the fields to process in a record")
public final class RulesEngine extends Transform<StructuredRecord, StructuredRecord> {
  private static final Logger LOG = LoggerFactory.getLogger(RulesEngine.class);

  // Plugin configuration.
  private final Config config;

  // Inference Engine
  private InferenceEngine ie;

  // Transient Store
  private TransientStore store;

  // Output Schema associated with transform output.
  private Schema oSchema = null;

  // Rule book being processed.
  private Rulebook rulebook;

  // Converts record from row to StructuredRecord.
  private final RecordConvertor convertor = new RecordConvertor();

  // Output rows
  private final List<Row> rows = new ArrayList<>();

  public RulesEngine(Config config) {
    this.config = config;
  }

  @Override
  public void configurePipeline(PipelineConfigurer configurer)
    throws IllegalArgumentException {
    super.configurePipeline(configurer);

    try {
      if(!config.containsMacro("rulebook")) {
        Reader reader = new StringReader(config.rulebook);
        Compiler compiler = new RulebookCompiler();
        rulebook = compiler.compile(reader);
        InferenceEngine ie = new RuleInferenceEngine(rulebook, null);
        ie.initialize();
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(e.getMessage());
    }

    // Based on the configuration create output schema.
    try {
      if (!config.containsMacro("schema")) {
        oSchema = Schema.parseJson(config.schema);
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Format of output schema specified is invalid. Please check the format.");
    }

    // Set the output schema.
    if (oSchema != null) {
      configurer.getStageConfigurer().setOutputSchema(oSchema);
    }
  }

  @Override
  public void initialize(TransformContext context) throws Exception {
    super.initialize(context);

    store = new DefaultTransientStore();

    // Based on the configuration create output schema.
    try {
      oSchema = Schema.parseJson(config.schema);
    } catch (IOException e) {
      throw new IllegalArgumentException(
        String.format("Stage:%s - Format of output schema specified is invalid. Please check the format.",
                      context.getStageName())
      );
    }

    Reader reader = new StringReader(config.rulebook);
    Compiler compiler = new RulebookCompiler();
    rulebook = compiler.compile(reader);

    ExecutorContext ctx = new RulesEngineContext(ExecutorContext.Environment.TRANSFORM,
                                                 context, store);
    ie = new RuleInferenceEngine(rulebook, ctx);
    ie.initialize();
  }

  @Override
  public void transform(StructuredRecord input, Emitter<StructuredRecord> emitter)
    throws Exception {

    Row row = new Row();
    for (Schema.Field field : input.getSchema().getFields()) {
      row.add(field.getName(), input.get(field.getName()));
    }

    try {
      rows.clear();
      row = ie.infer(row);
      rows.add(row);
      List<StructuredRecord> records = convertor.toStructureRecord(rows, oSchema);
      for (StructuredRecord record : records) {
        StructuredRecord.Builder builder = StructuredRecord.builder(oSchema);
        // Iterate through output schema, if the 'record' doesn't have it, then
        // attempt to take if from 'input'.
        for (Schema.Field field : oSchema.getFields()) {
          Object wObject = record.get(field.getName()); // wrangled records
          if (wObject == null) {
            builder.set(field.getName(), null);
          } else {
            if (wObject instanceof String) {
              builder.convertAndSet(field.getName(), (String) wObject);
            } else {
              builder.set(field.getName(), wObject);
            }
          }
        }
        emitter.emit(builder.build());
      }
    } catch (SkipRowException e) {
      String message = String.format("Fired rulebook '%s', version '%s', rule name '%s', description '%s', condition {%s}.",
                                     rulebook.getName(), rulebook.getVersion(), e.getRule().getName(),
                                     e.getRule().getDescription(), e.getRule().getWhen());
      emitter.emitError(new InvalidEntry<>(100, message, input));
    }
  }

  public static class Config extends PluginConfig {
    @Name("rulebook")
    @Description("Specify the rule book.")
    @Macro
    private String rulebook;

    @Name("schema")
    @Description("Specifies the schema that has to be output.")
    @Macro
    private final String schema;

    public Config(String rulebook, String schema) {
      this.rulebook = rulebook;
      this.schema = schema;
    }
  }
}
