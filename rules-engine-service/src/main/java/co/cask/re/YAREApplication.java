package co.cask.re;

import co.cask.cdap.api.app.AbstractApplication;
import co.cask.cdap.api.data.schema.Schema;
import co.cask.cdap.api.dataset.DatasetProperties;
import co.cask.cdap.api.dataset.table.Table;

/**
 * Class description here.
 */
public class YAREApplication extends AbstractApplication {
  /**
   * Override this method to declare and configure the application.
   */
  @Override
  public void configure() {
    setName("yare");
    setDescription("Yet Another Rules Engine - Just for Big Data.");

    Schema rulebookSchema = Schema.recordOf(
      "rulebook",
      Schema.Field.of("id", Schema.of(Schema.Type.STRING)),
      Schema.Field.of("description", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
      Schema.Field.of("version", Schema.of(Schema.Type.LONG)),
      Schema.Field.of("source", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
      Schema.Field.of("user", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
      Schema.Field.of("created", Schema.of(Schema.Type.LONG)),
      Schema.Field.of("updated", Schema.of(Schema.Type.LONG)),
      Schema.Field.of("rules", Schema.of(Schema.Type.STRING))
    );

    Schema ruleSchema = Schema.recordOf(
      "rules",
      Schema.Field.of("id", Schema.of(Schema.Type.STRING)),
      Schema.Field.of("description", Schema.of(Schema.Type.STRING)),
      Schema.Field.of("condition", Schema.of(Schema.Type.STRING)),
      Schema.Field.of("action", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
      Schema.Field.of("created", Schema.of(Schema.Type.LONG)),
      Schema.Field.of("updated", Schema.of(Schema.Type.LONG))
    );


    createDataset("rulebook", Table.class.getName(), DatasetProperties.builder()
      .add(Table.PROPERTY_SCHEMA, rulebookSchema.toString())
      .add(Table.PROPERTY_SCHEMA_ROW_FIELD, "id")
      .build());

    createDataset("rules", Table.class.getName(), DatasetProperties.builder()
      .add(Table.PROPERTY_SCHEMA, ruleSchema.toString())
      .add(Table.PROPERTY_SCHEMA_ROW_FIELD, "id")
      .build());

    addService("service",
               new YARERulebookService());
  }
}
