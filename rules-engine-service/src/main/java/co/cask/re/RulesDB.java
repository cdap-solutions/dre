package co.cask.re;

import co.cask.cdap.api.common.Bytes;
import co.cask.cdap.api.dataset.table.Row;
import co.cask.cdap.api.dataset.table.Table;
import com.google.common.base.Joiner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Class description here.
 */
public final class RulesDB {
  private final Table rulebook;
  private final Table rules;

  private static final String RULE_TEMPLATE = "rule %s { description '%s' when(%s) then { %s } }";
  private static final String RULEBOOK_TEMPLATE = "rulebook %s { version %d meta { description '%s' " +
    "created-date %d updated-date %d source '%s' user '%s' } %s }";

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

  public RulesDB(Table rulebook, Table rules) {
    this.rulebook = rulebook;
    this.rules = rules;
  }

  public void createRule(RuleRequest rule) throws RuleAlreadyExistsException {
    Row row = rules.get(toKey(rule.getId()));
    if (!row.isEmpty()) {
      throw new RuleAlreadyExistsException(
        String.format("Rule '%s' already exists in the rules database. Delete it first or use PUT.", rule.getId())
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
      Bytes.toBytes(System.currentTimeMillis() / 1000),
      Bytes.toBytes(System.currentTimeMillis() / 1000)
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

    byte[][] columns = new byte[][] {
      DESCRIPTION, CONDITION, ACTION, UPDATED
    };

    byte[][] values = new byte[][] {
      Bytes.toBytes(rule.getDescription()),
      Bytes.toBytes(rule.getWhen()),
      Bytes.toBytes(rule.getThen()),
      Bytes.toBytes(System.currentTimeMillis() / 1000)
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
      Bytes.toBytes(System.currentTimeMillis() / 1000),
      Bytes.toBytes(System.currentTimeMillis() / 1000),
      Bytes.toBytes(rb.getUser()),
      Bytes.toBytes(rb.getSource()),
      Bytes.toBytes(1L),
      Bytes.toBytes(rb.getRulesString())
    };

    rulebook.put(toKey(rb.getId()), columns, values);
  }

  public void updateRulebook(String id, RulebookRequest rb) throws RulebookAlreadyExistsException, RuleNotFoundException {
    Row row = rulebook.get(toKey(id));
    if (row.isEmpty()) {
      throw new RulebookAlreadyExistsException(
        String.format("Rulebook '%s' does not exist. Create it first with a POST request.", id)
      );
    }

    byte[][] columns = new byte[][] {
      DESCRIPTION, USER, SOURCE
    };

    long version = row.getLong(VERSION);
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
    Set<String> rules = convertRulesToSet(row.getString(RULES));
    if (rules.contains(ruleId)) {
      throw new RuleAlreadyExistsException(
        String.format("Rule '%s' already exists in the rulebook '%s'.", ruleId, rulebookId)
      );
    }
    rules.add(ruleId);

    byte[][] columns = new byte[][] {
      UPDATED, VERSION, RULES
    };

    byte[][] values = new byte[][] {
      Bytes.toBytes(System.currentTimeMillis() / 1000),
      Bytes.toBytes(version++),
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
        String.format("Attempt to remove rule '%s' to rulebook '%s' failed as rule doesn't exist in rules database.",
                      rulebookId, ruleId)
      );
    }

    long version = row.getLong(VERSION);
    Set<String> rules = convertRulesToSet(row.getString(RULES));
    if (rules.contains(ruleId)) {
      throw new RuleAlreadyExistsException(
        String.format("Rule '%s' already exists in the rulebook '%s'.", ruleId, rulebookId)
      );
    }
    rules.remove(ruleId);

    byte[][] columns = new byte[][] {
      UPDATED, VERSION, RULES
    };

    byte[][] values = new byte[][] {
      Bytes.toBytes(System.currentTimeMillis() / 1000),
      Bytes.toBytes(version++),
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
      Bytes.toBytes(System.currentTimeMillis() / 1000),
      Bytes.toBytes(System.currentTimeMillis() / 1000),
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

  public String generateRulebook(String rulebookId) throws RulebookNotFoundException, RuleNotFoundException {
    Row row = rulebook.get(toKey(rulebookId));
    if (row.isEmpty()) {
      throw new RulebookNotFoundException(
        String.format("Rulebook '%s' not found.", rulebookId)
      );
    }

    Set<String> ruleSet = convertRulesToSet(row.getString(RULES));
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

  private Set<String> convertRulesToSet(String rules) {
    Set<String> ruleSet = new HashSet<>();
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
