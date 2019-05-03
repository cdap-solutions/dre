/*
 * Copyright © 2017-2019 Cask Data, Inc.
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

import com.google.common.base.Joiner;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.cdap.cdap.api.common.Bytes;
import io.cdap.cdap.api.dataset.table.Row;
import io.cdap.cdap.api.dataset.table.Scanner;
import io.cdap.cdap.api.dataset.table.Table;
import io.cdap.cdap.api.messaging.MessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Class description here.
 */
public final class RulesDB {
  private static final Logger LOG = LoggerFactory.getLogger(RulesDB.class);
  private final Table rulebook;
  private final Table rules;
  private final MessagePublisher publisher;

  private static final String RULE_TEMPLATE = "\n" +
    "  rule %s {\n" +
    "    description '%s'\n" +
    "    when(%s) then {\n" +
    "      %s\n" +
    "    }\n" +
    "  }" +
    "\n";

  private static final String RULEBOOK_TEMPLATE = "rulebook %s {\n" +
    "  version %d\n" +
    "\n" +
    "  meta {\n" +
    "    description '%s'\n" +
    "    created-date %d\n" +
    "    updated-date %d\n" +
    "    source '%s'\n" +
    "    user '%s'\n" +
    "  }\n" +
    "  %s\n" +
    "}";

  private static final byte[] ID = Bytes.toBytes("id");
  private static final byte[] DESCRIPTION = Bytes.toBytes("description");
  private static final byte[] CONDITION = Bytes.toBytes("condition");
  private static final byte[] ACTION = Bytes.toBytes("action");
  private static final byte[] CREATED =  Bytes.toBytes("created");
  private static final byte[] UPDATED = Bytes.toBytes("updated");
  private static final byte[] USER = Bytes.toBytes("user");
  private static final byte[] SOURCE = Bytes.toBytes("source");
  private static final byte[] VERSION = Bytes.toBytes("version");
  private static final byte[] RULES = Bytes.toBytes("rules");

  public RulesDB(Table rulebook, Table rules, MessagePublisher publisher) {
    this.rulebook = rulebook;
    this.rules = rules;
    this.publisher = publisher;
  }

  public void createRule(RuleRequest rule) throws RuleAlreadyExistsException {
    Row row = rules.get(toKey(rule.getId()));
    if (!row.isEmpty()) {
      throw new RuleAlreadyExistsException(
        String.format("Rule '%s' already exists in the rules database. Delete it first or use PUT.", rule.getId())
      );
    }

    if (rule.getId() == null || rule.getId().trim().isEmpty()) {
      throw new IllegalArgumentException("Rule requires a mandatory field 'id'.");
    }

    if (rule.getDescription() == null || rule.getDescription().trim().isEmpty()) {
      throw new IllegalArgumentException(
        String.format("Rule '%s' requires the mandatory field 'description'", rule.getId())
      );
    }

    if (rule.getWhen() == null || rule.getWhen().trim().isEmpty()) {
      throw new IllegalArgumentException(
        String.format("Rule '%s' requires mandatory field 'when'", rule.getId())
      );
    }

    if (rule.getThen() == null || rule.getWhen().trim().isEmpty()) {
      throw new IllegalArgumentException(
        String.format("Rule '%s' requires the mandatory field 'then'", rule.getId())
      );
    }

    byte[][] columns = new byte[][] {
      ID, DESCRIPTION, CONDITION, ACTION, CREATED, UPDATED
    };

    byte[][] values = new byte[][] {
      toKey(rule.getId()),
      Bytes.toBytes(rule.getDescription()),
      Bytes.toBytes(rule.getWhen()),
      Bytes.toBytes(rule.getThen()),
      Bytes.toBytes(getCurrentTime()),
      Bytes.toBytes(getCurrentTime())
    };
    rules.put(toKey(rule.getId()), columns, values);
  }

  public void updateRule(String id, RuleRequest rule) throws RuleNotFoundException {
    Row row = rules.get(toKey(id));
    if (row.isEmpty()) {
      throw new RuleNotFoundException(
        String.format("Rule '%s' does not exist. Create it first with a POST request.", rule.getId())
      );
    }

    if (rule.getDescription() == null || rule.getDescription().trim().isEmpty()) {
      throw new IllegalArgumentException(
        String.format("Rule '%s' requires the mandatory field 'description'", rule.getId())
      );
    }

    if (rule.getWhen() == null || rule.getWhen().trim().isEmpty()) {
      throw new IllegalArgumentException(
        String.format("Rule '%s' requires mandatory field 'when'", rule.getId())
      );
    }

    if (rule.getThen() == null || rule.getWhen().trim().isEmpty()) {
      throw new IllegalArgumentException(
        String.format("Rule '%s' requires the mandatory field 'then'", rule.getId())
      );
    }

    byte[][] columns = new byte[][] {
      DESCRIPTION, CONDITION, ACTION, UPDATED
    };

    byte[][] values = new byte[][] {
      Bytes.toBytes(rule.getDescription()),
      Bytes.toBytes(rule.getWhen()),
      Bytes.toBytes(rule.getThen()),
      Bytes.toBytes(getCurrentTime())
    };

    rules.put(toKey(id), columns, values);
  }

  public Map<String, Object> retrieveRule(String id) throws RuleNotFoundException {
    Map<String, Object> result = new TreeMap<>();
    Row row = rules.get(toKey(id));
    if (row.isEmpty()) {
      throw new RuleNotFoundException(
        String.format("Rule '%s' does not exist. Create it first with a POST request.", id)
      );
    }

    result.put(Bytes.toString(ID), row.getString(ID));
    result.put(Bytes.toString(DESCRIPTION), row.getString(DESCRIPTION));
    result.put(Bytes.toString(CONDITION), row.getString(CONDITION));
    result.put(Bytes.toString(CREATED), row.getLong(CREATED));
    result.put(Bytes.toString(UPDATED), row.getLong(UPDATED));
    String then = row.getString(ACTION);
    List<String> thens = new ArrayList<>();
    for (String t : then.split(";")) {
      thens.add(t);
    }
    result.put(Bytes.toString(ACTION), thens);

    return result;
  }

  public void deleteRule(String id) throws RuleNotFoundException {
    Row row = rules.get(toKey(id));
    if (row.isEmpty()) {
      throw new RuleNotFoundException(
        String.format("Rule '%s' does not exist. Create it first with a POST request.", id)
      );
    }
    rules.delete(toKey(id));
  }

  public String retrieveUsingRuleTemplate(String id) throws RuleNotFoundException {
    Row row = rules.get(toKey(id));
    if (row.isEmpty()) {
      throw new RuleNotFoundException(
        String.format("Rule '%s' does not exist. Create it first with a POST request.", id)
      );
    }

    return String.format(RULE_TEMPLATE, row.getString(ID), row.getString(DESCRIPTION),
                                row.getString(CONDITION), row.getString(ACTION));
  }

  public void createRulebook(RulebookRequest rb) throws RulebookAlreadyExistsException, RuleNotFoundException {
    Row row = rulebook.get(toKey(rb.getId()));
    if (!row.isEmpty()) {
      throw new RulebookAlreadyExistsException(
        String.format("Rulebook '%s' already exists. Either update it or delete it and create it.", rb.getId())
      );
    }

    for (String rule : rb.getRules()) {
      if(rules.get(toKey(rule)).isEmpty()) {
        throw new RuleNotFoundException(
          String.format("Rulebook '%s' includes a rule '%s' that does not exist. Please add rule first",
                        rb.getId(), rule)
        );
      }
    }

    byte[][] columns = new byte[][] {
      ID, DESCRIPTION, CREATED, UPDATED, USER, SOURCE, VERSION, RULES
    };

    byte[][] values = new byte[][] {
      Bytes.toBytes(rb.getId()),
      Bytes.toBytes(rb.getDescription()),
      Bytes.toBytes(getCurrentTime()),
      Bytes.toBytes(getCurrentTime()),
      Bytes.toBytes(rb.getUser()),
      Bytes.toBytes(rb.getSource()),
      Bytes.toBytes(1L),
      Bytes.toBytes(rb.getRulesString())
    };

    rulebook.put(toKey(rb.getId()), columns, values);
  }

  public void createRulebook(Rulebook rulebook) throws RulebookAlreadyExistsException {
    List<String> ruleIds = new ArrayList<>();
    for (Rule rule : rulebook.getRules()) {
      RuleRequest request = new RuleRequest(rule.getName(), rule.getDescription(), rule.getWhen(), rule.getThen());
      ruleIds.add(rule.getName());
      try {
        createRule(request);
      } catch (RuleAlreadyExistsException e) {
        // Nothing to be done here.
      }
    }
    RulebookRequest rbreq = new RulebookRequest(rulebook.getName(), rulebook.getMeta().getDescription(),
                                                rulebook.getMeta().getSource(), rulebook.getMeta().getUser(),
                                                ruleIds);
    try {
      createRulebook(rbreq);
    } catch (RuleNotFoundException e) {
      // no-op should not happen.
    }
  }

  public void updateRulebook(String id, RulebookRequest rb) throws RulebookAlreadyExistsException, RuleNotFoundException {
    Row row = rulebook.get(toKey(id));
    if (row.isEmpty()) {
      throw new RulebookAlreadyExistsException(
        String.format("Rulebook '%s' does not exist. Create it first with a POST request.", id)
      );
    }

    byte[][] columns = new byte[][] {
      DESCRIPTION, USER, SOURCE, VERSION, RULES
    };

    LOG.info("Ordering of rules {}.", rb.getRulesString());

    long version = row.getLong(VERSION);
    version = version + 1;
    byte[][] values = new byte[][] {
      Bytes.toBytes(rb.getDescription()),
      Bytes.toBytes(rb.getUser()),
      Bytes.toBytes(rb.getSource()),
      Bytes.toBytes(version),
      Bytes.toBytes(rb.getRulesString())
    };

    rulebook.put(toKey(rb.getId()), columns, values);
  }

  public void addRuleToRulebook(String rulebookId, String ruleId) throws RuleAlreadyExistsException,
    RulebookNotFoundException, RuleNotFoundException {
    Row row = rulebook.get(toKey(rulebookId));
    if (row.isEmpty()) {
      throw new RulebookNotFoundException(
        String.format("Rulebook '%s' not found.", rulebookId)
      );
    }

    if (rules.get(toKey(ruleId)).isEmpty()) {
      throw new RuleNotFoundException(
        String.format("Attempt to add rule '%s' to rulebook '%s' failed as rule doesn't exist in rules database.",
                      rulebookId, ruleId)
      );
    }

    long version = row.getLong(VERSION);
    List<String> rules = convertRulesToSet(row.getString(RULES));
    if (rules.contains(ruleId)) {
      throw new RuleAlreadyExistsException(
        String.format("Rule '%s' already exists in the rulebook '%s'.", ruleId, rulebookId)
      );
    }
    rules.add(ruleId);

    byte[][] columns = new byte[][] {
      UPDATED, VERSION, RULES
    };

    version = version + 1;
    byte[][] values = new byte[][] {
      Bytes.toBytes(getCurrentTime()),
      Bytes.toBytes(version),
      Bytes.toBytes(Joiner.on(",").join(rules))
    };
    rulebook.put(toKey(rulebookId), columns, values);
  }

  public void removeRuleFromRulebook(String rulebookId, String ruleId) throws RuleAlreadyExistsException,
    RulebookNotFoundException, RuleNotFoundException {
    Row row = rulebook.get(toKey(rulebookId));
    if (row.isEmpty()) {
      throw new RulebookNotFoundException(
        String.format("Rulebook '%s' not found.", rulebookId)
      );
    }

    if (rules.get(toKey(ruleId)).isEmpty()) {
      throw new RuleNotFoundException(
        String.format("Attempt to remove rule '%s' from rulebook '%s' failed as rule doesn't exist in rules database.",
                      rulebookId, ruleId)
      );
    }

    long version = row.getLong(VERSION);
    List<String> rules = convertRulesToSet(row.getString(RULES));
    rules.remove(ruleId);

    byte[][] columns = new byte[][] {
      UPDATED, VERSION, RULES
    };

    version = version + 1;
    byte[][] values = new byte[][] {
      Bytes.toBytes(getCurrentTime()),
      Bytes.toBytes(version),
      Bytes.toBytes(Joiner.on(",").join(rules))
    };
    rulebook.put(toKey(rulebookId), columns, values);
  }

  public void cloneRulebook(String rulebookId) throws RuleAlreadyExistsException,
    RulebookNotFoundException, RuleNotFoundException {
    Row row = rulebook.get(toKey(rulebookId));
    if (row.isEmpty()) {
      throw new RulebookNotFoundException(
        String.format("Rulebook '%s' not found.", rulebookId)
      );
    }

    int count = 1;
    String clonedRulebookId = String.format("%s_%d", rulebookId, count);
    while(!rulebook.get(toKey(clonedRulebookId)).isEmpty()) {
      count++;
      clonedRulebookId = String.format("%s_%d", rulebookId, count);
    }

    byte[][] columns = new byte[][] {
      ID, DESCRIPTION, CREATED, UPDATED, USER, SOURCE, VERSION, RULES
    };

    byte[][] values = new byte[][] {
      Bytes.toBytes(clonedRulebookId),
      row.get(DESCRIPTION),
      Bytes.toBytes(getCurrentTime()),
      Bytes.toBytes(getCurrentTime()),
      row.get(USER),
      row.get(SOURCE),
      Bytes.toBytes(1L),
      row.get(RULES)
    };

    rulebook.put(toKey(clonedRulebookId), columns, values);
  }

  public void deleteRulebook(String rulebookId) throws RulebookNotFoundException {
    Row row = rulebook.get(toKey(rulebookId));
    if (row.isEmpty()) {
      throw new RulebookNotFoundException(
        String.format("Rulebook '%s' not found.", rulebookId)
      );
    }
    rulebook.delete(toKey(rulebookId));
  }

  public JsonArray getRulebookRules(String rulebookId) throws RulebookNotFoundException, RuleNotFoundException {
    Row row = rulebook.get(toKey(rulebookId));
    if (row.isEmpty()) {
      throw new RulebookNotFoundException(
        String.format("Rulebook '%s' not found.", rulebookId)
      );
    }

    List<String> ruleSet = convertRulesToSet(row.getString(RULES));
    JsonArray array = new JsonArray();
    for (String rule : ruleSet) {
      JsonObject object = new JsonObject();
      Row ruleRow = rules.get(toKey(rule));
      if (ruleRow.isEmpty()) {
        continue;
      }
      object.addProperty(Bytes.toString(ID), ruleRow.getString(ID));
      object.addProperty(Bytes.toString(DESCRIPTION), ruleRow.getString(DESCRIPTION));
      object.addProperty(Bytes.toString(CONDITION), ruleRow.getString(CONDITION));
      object.addProperty(Bytes.toString(CREATED), ruleRow.getLong(CREATED));
      object.addProperty(Bytes.toString(UPDATED), ruleRow.getLong(UPDATED));

      JsonArray actions = new JsonArray();
      String then = ruleRow.getString(ACTION);
      if (then != null) {
        for (String t : then.split(";")) {
          actions.add(new JsonPrimitive(t));
        }
        object.add(Bytes.toString(ACTION), actions);
      }
      array.add(object);
    }
    return array;
  }

  public String generateRulebook(String rulebookId) throws RulebookNotFoundException, RuleNotFoundException {
    Row row = rulebook.get(toKey(rulebookId));
    if (row.isEmpty()) {
      throw new RulebookNotFoundException(
        String.format("Rulebook '%s' not found.", rulebookId)
      );
    }

    List<String> ruleSet = convertRulesToSet(row.getString(RULES));
    List<String> ruleOutput = new ArrayList<>();
    for (String rule : ruleSet) {
      Row ruleRow = rules.get(toKey(rule));
      if (ruleRow.isEmpty()) {
        throw new RuleNotFoundException(
          String.format("Rulebook '%s' contains rule '%s', but rule '%s' is not present in rules database.",
                        rulebookId, rule, rule)
        );
      }
      ruleOutput.add(String.format(RULE_TEMPLATE, ruleRow.getString(ID), ruleRow.getString(DESCRIPTION),
                                   ruleRow.getString(CONDITION), ruleRow.getString(ACTION)));
    }

    String format = String.format(RULEBOOK_TEMPLATE, row.getString(ID), row.getLong(VERSION),
                                  row.getString(DESCRIPTION), row.getLong(CREATED),
                                  row.getLong(UPDATED), row.getString(SOURCE),
                                  row.getString(USER), Joiner.on("").join(ruleOutput));
    return format;
  }

  /**
   * Lists the rules and rulebooks present in the system.
   *
   * @return List of rules in the system.
   */
  public List<Map<String, Object>> rules() {
    List<Map<String, Object>> result = new ArrayList<>();
    try (Scanner scan = rules.scan(null, null)) {
      Row next;
      while ((next = scan.next()) != null) {
        Map<String, Object> object = new HashMap<>();
        object.put(Bytes.toString(ID), next.getString(ID));
        object.put(Bytes.toString(DESCRIPTION), next.getString(DESCRIPTION));
        object.put(Bytes.toString(CONDITION), next.getString(CONDITION));
        object.put(Bytes.toString(ACTION), next.getString(ACTION));
        object.put(Bytes.toString(CREATED), next.getLong(CREATED));
        object.put(Bytes.toString(UPDATED), next.getLong(UPDATED));
        result.add(object);
      }
    }
    return result;
  }

  public List<Map<String, Object>> rulebooks() {
    List<Map<String, Object>> result = new ArrayList<>();
    try (Scanner scan = rulebook.scan(null, null)) {
      //ID, DESCRIPTION, CREATED, UPDATED, USER, SOURCE, VERSION, RULES
      Row next;
      while ((next = scan.next()) != null) {
        Map<String, Object> object = new HashMap<>();
        object.put(Bytes.toString(ID), next.getString(ID));
        object.put(Bytes.toString(DESCRIPTION), next.getString(DESCRIPTION));
        object.put(Bytes.toString(USER), next.getString(USER));
        object.put(Bytes.toString(SOURCE), next.getString(SOURCE));
        object.put(Bytes.toString(VERSION), next.getLong(VERSION));
        object.put(Bytes.toString(RULES), next.getString(RULES));
        object.put(Bytes.toString(CREATED), next.getLong(CREATED));
        object.put(Bytes.toString(UPDATED), next.getLong(UPDATED));
        result.add(object);
      }
    }
    return result;
  }

  private long getCurrentTime() {
    return (System.currentTimeMillis() / 1000);
  }

  private List<String> convertRulesToSet(String rules) {
    List<String> ruleSet = new ArrayList<>();
    if (rules == null || rules.trim().isEmpty()) {
      return ruleSet;
    }
    String[] rulesArray = rules.split(",");
    for (String rule : rulesArray) {
      ruleSet.add(rule);
    }
    return ruleSet;
  }

  private byte[] toKey(String value) {
    return Bytes.toBytes(value);
  }

}
