/*
 * Copyright © 2017-2018 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

package io.cdap.re;

import co.cask.cdap.api.common.Bytes;
import co.cask.wrangler.api.DirectiveLoadException;
import co.cask.wrangler.api.DirectiveNotFoundException;
import co.cask.wrangler.api.DirectiveParseException;
import co.cask.wrangler.api.ExecutorContext;
import co.cask.wrangler.api.RecipeException;
import co.cask.wrangler.api.RecipeParser;
import co.cask.wrangler.api.RecipePipeline;
import co.cask.wrangler.api.Row;
import co.cask.wrangler.executor.RecipePipelineExecutor;
import co.cask.wrangler.parser.GrammarBasedParser;
import co.cask.wrangler.parser.MigrateToV2;
import co.cask.wrangler.registry.CompositeDirectiveRegistry;
import co.cask.wrangler.registry.SystemDirectiveRegistry;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class description here.
 */
public class RuleExecutor {
  private static final Logger LOG = LoggerFactory.getLogger(RuleExecutor.class);
  
  private static final JexlEngine engine = new JexlBuilder()
    .namespaces(getRegisteredFunctions())
    .silent(false)
    .cache(1024*1024)
    .cacheThreshold(1024*1024)
    .silent(true)
    .strict(false)
    .debug(false)
    .create();

  private Rule rule;
  private JexlScript script;
  private List<String> variables = new ArrayList<>();
  private RecipePipeline pipeline;
  private ExecutorContext context;


  public RuleExecutor(Rule rule, ExecutorContext context) throws RuleCompileException {
    try {
      this.script = engine.createScript(rule.getWhen());
      for(List<String> vars : script.getVariables()) {
        for(String var : vars) {
          this.variables.add(var);
        }
      }
      this.rule = rule;
      this.context = context;
      this.pipeline = compile(rule.getThen());
    } catch (Exception e) {
      throw new RuleCompileException(e.getMessage(), rule);
    }
  }

  public boolean validateWhen(String when) throws JexlException {
    engine.createScript(when);
    return true;
  }

  public boolean validateThen(String then) throws Exception {
    compile(Arrays.asList(then.split(";")));
    return true;
  }

  public boolean shouldExecute(RowActiveSet set) {
    if (variables.size() == 0) {
      return true;
    }
    for(String variable : variables) {
      if (!set.has(variable)) {
        return false;
      }
    }
    return true;
  }

  public boolean when(RowActiveSet set) {
    Object object = script.execute(set);
    if (object == null) {
      return false;
    } else if (object instanceof Boolean) {
      return (Boolean) object;
    }
    return true;
  }

  public Row then(Row row) throws RecipeException, SkipRowException {
    List<Row> result = pipeline.execute(Arrays.asList(row));
    List<Row> errors = pipeline.errors();
    if (errors.size() > 0) {
      throw new SkipRowException(rule);
    }
    if(result.size() > 0) {
      return result.get(0);
    }
    return null;
  }

  public Rule getRule() {
    return rule;
  }

  public static Map<String, Object> getRegisteredFunctions() {
    Map<String, Object> functions = new HashMap<>();
    functions.put(null, RuleFunctions.class);
    functions.put("math", Math.class);
    functions.put("string", StringUtils.class);
    functions.put("bytes", Bytes.class);
    functions.put("arrays", Arrays.class);
    return functions;
  }


  public RecipePipeline compile(List<String> recipe)
    throws RecipeException, DirectiveParseException, DirectiveLoadException, DirectiveNotFoundException {
    // Support only in-built directives.
    CompositeDirectiveRegistry registry = new CompositeDirectiveRegistry(
      new SystemDirectiveRegistry()
    );

    String migrate = new MigrateToV2(recipe).migrate();
    RecipeParser parser = new GrammarBasedParser(context.getNamespace(), migrate, registry);
    parser.initialize(null); // No Directive Context.
    RecipePipeline pipeline = new RecipePipelineExecutor();
    pipeline.initialize(parser, context);
    return pipeline;
  }



}
