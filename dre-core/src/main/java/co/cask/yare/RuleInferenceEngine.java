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

package co.cask.yare;

import co.cask.wrangler.api.ExecutorContext;
import co.cask.wrangler.api.RecipeException;
import co.cask.wrangler.api.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Class description here.
 */
public class RuleInferenceEngine implements InferenceEngine {
  private static final Logger LOG = LoggerFactory.getLogger(RuleInferenceEngine.class);
  private final Rulebook rulebook;
  private final List<RuleExecutor> ruleset;
  private final Set<String> executed = new HashSet<>();
  private final ExecutorContext executorContext;

  public RuleInferenceEngine(Rulebook rulebook, ExecutorContext executorContext) {
    this.rulebook = rulebook;
    this.executorContext = executorContext;
    this.ruleset = new ArrayList<>();
  }

  @Override
  public void initialize() throws RuleCompileException {
    LOG.info("Applying rulebook '{}', version '{}' to incoming records.", rulebook.getName(),
             rulebook.getVersion());
    for(Rule rule : rulebook.getRules()) {
      RuleExecutor ruleExecutor = new RuleExecutor(rule, executorContext);
      ruleset.add(ruleExecutor);
    }
  }

  @Override
  public Row infer(Row row) throws RecipeException, SkipRowException {
    List<RuleExecutor> workingSet = ruleset;
    RowActiveSet rowSet = new RowActiveSet(row);

    executed.clear();
    while(workingSet.size() > 0) {
      List<RuleExecutor> selected = new ArrayList<>();
      List<RuleExecutor> unselected = new ArrayList<>();
      for(RuleExecutor executable : workingSet) {
        if ((executable.shouldExecute(rowSet) && executable.when(rowSet)) && !executed.contains(executable.getRule().getName())) {
          registerMetric(executable);
          selected.add(executable);
        } else {
          unselected.add(executable);
        }
      }

      if (workingSet.size() == unselected.size()) {
        return row;
      }

      // Execute selected.
      for(RuleExecutor rule : selected) {
        try {
          row = rule.then(rowSet.get());
          if (row != null) {
            rowSet.set(row);
          }
        } finally {
          executed.add(rule.getRule().getName());
        }
      }

      // Work on unselected.
      workingSet = unselected;
    }
    return row;
  }

  private void registerMetric(RuleExecutor executor) {
    String metricName = String.format("%s.%s.%s.fired", rulebook.getName(),
                                      rulebook.getVersion(), executor.getRule().getName());
    if (executorContext != null) {
      executorContext.getMetrics().pipelineCount(metricName, 1);
      executorContext.getMetrics().pipelineCount(executor.getRule().getName() + ".fired", 1);
      executorContext.getMetrics().pipelineCount(rulebook.getName() + "." + executor.getRule().getName() + ".fired", 1);
    }
  }


  @Override
  public void destroy() {
    // no-op
  }
}
