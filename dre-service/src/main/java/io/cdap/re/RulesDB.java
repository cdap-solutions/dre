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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;
import io.cdap.cdap.api.dataset.lib.CloseableIterator;
import io.cdap.cdap.spi.data.StructuredRow;
import io.cdap.cdap.spi.data.StructuredTable;
import io.cdap.cdap.spi.data.StructuredTableContext;
import io.cdap.cdap.spi.data.TableNotFoundException;
import io.cdap.cdap.spi.data.table.StructuredTableId;
import io.cdap.cdap.spi.data.table.StructuredTableSpecification;
import io.cdap.cdap.spi.data.table.field.Field;
import io.cdap.cdap.spi.data.table.field.FieldType;
import io.cdap.cdap.spi.data.table.field.Fields;
import io.cdap.cdap.spi.data.table.field.Range;
import io.cdap.re.exception.conflict.RuleAlreadyExistsException;
import io.cdap.re.exception.conflict.RulebookAlreadyExistsException;
import io.cdap.re.exception.notfound.RuleNotFoundException;
import io.cdap.re.exception.notfound.RulebookNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;

/**
 * This class {@link RulesDB} manages all the connections defined. It manages the lifecycle of Rules and Rulebooks
 * including all CRUD operations.
 */
final class RulesDB {

  private static final Logger LOG = LoggerFactory.getLogger(RulesDB.class);

  private static final Type LIST_STRING_TYPE = new TypeToken<List<String>>() {}.getType();
  private static final Gson GSON = new Gson();
  private static final String ID_COL = "id";
  private static final String NAMESPACE_COL = "namespace";
  private static final String GENERATION_COL = "generation";
  private static final String DESCRIPTION_COL = "description";
  private static final String CONDITION_COL = "condition";
  private static final String ACTION_COL = "action";
  private static final String CREATED_COL = "created";
  private static final String UPDATED_COL = "updated";
  private static final String USER_COL = "user";
  private static final String SOURCE_COL = "source";
  private static final String VERSION_COL = "version";
  private static final String RULES_COL = "rules";
  private static final StructuredTableId RULES_TABLE_ID = new StructuredTableId("rules");
  static final StructuredTableSpecification RULES_TABLE_SPEC = new StructuredTableSpecification.Builder()
    .withId(RULES_TABLE_ID)
    .withFields(new FieldType(NAMESPACE_COL, FieldType.Type.STRING),
                new FieldType(GENERATION_COL, FieldType.Type.LONG),
                new FieldType(ID_COL, FieldType.Type.STRING),
                new FieldType(DESCRIPTION_COL, FieldType.Type.STRING),
                new FieldType(CONDITION_COL, FieldType.Type.STRING),
                new FieldType(ACTION_COL, FieldType.Type.STRING),
                new FieldType(CREATED_COL, FieldType.Type.LONG),
                new FieldType(UPDATED_COL, FieldType.Type.LONG))
    .withPrimaryKeys(NAMESPACE_COL, GENERATION_COL, ID_COL)
    .withIndexes()
    .build();
  private static final StructuredTableId RULEBOOK_TABLE_ID = new StructuredTableId("rulebook");
  static final StructuredTableSpecification RULEBOOK_TABLE_SPEC = new StructuredTableSpecification.Builder()
    .withId(RULEBOOK_TABLE_ID)
    .withFields(new FieldType(NAMESPACE_COL, FieldType.Type.STRING),
                new FieldType(GENERATION_COL, FieldType.Type.LONG),
                new FieldType(ID_COL, FieldType.Type.STRING),
                new FieldType(DESCRIPTION_COL, FieldType.Type.STRING),
                new FieldType(VERSION_COL, FieldType.Type.LONG),
                new FieldType(SOURCE_COL, FieldType.Type.STRING),
                new FieldType(USER_COL, FieldType.Type.STRING),
                new FieldType(CREATED_COL, FieldType.Type.LONG),
                new FieldType(UPDATED_COL, FieldType.Type.LONG),
                new FieldType(RULES_COL, FieldType.Type.STRING))
    .withPrimaryKeys(NAMESPACE_COL, GENERATION_COL, ID_COL)
    .build();
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
  private final StructuredTable rulesTable;
  private final StructuredTable rulebookTable;

  private RulesDB(StructuredTable rulesTable, StructuredTable rulebookTable) {
    this.rulesTable = rulesTable;
    this.rulebookTable = rulebookTable;
  }

  static RulesDB get(StructuredTableContext context) {
    try {
      StructuredTable rulesTable = context.getTable(RULES_TABLE_ID);
      StructuredTable rulebookTable = context.getTable(RULEBOOK_TABLE_ID);

      return new RulesDB(rulesTable, rulebookTable);
    } catch (TableNotFoundException tnfe) {
      throw new IllegalStateException(tnfe);
    }
  }

  void createRule(NamespacedId namespacedId, RuleRequest rule) throws RuleAlreadyExistsException, IOException {
    if (namespacedId.getId() == null || namespacedId.getId().trim().isEmpty()) {
      throw new IllegalArgumentException("Rule requires a mandatory field 'id'.");
    }

    Collection<Field<?>> keyFields = getKeyFields(namespacedId);

    if (rulesTable.read(keyFields).isPresent()) {
      throw new RuleAlreadyExistsException(
        String.format("Rule '%s' in namespace '%s' already exists in the rules database. Delete it first or use PUT.",
                      namespacedId.getId(), namespacedId.getNamespace().getName())
      );
    }

    rule.validate();

    Collection<Field<?>> ruleFields = new ArrayList<>(keyFields);
    ruleFields.add(Fields.stringField(DESCRIPTION_COL, rule.getDescription()));
    ruleFields.add(Fields.stringField(CONDITION_COL, rule.getWhen()));
    ruleFields.add(Fields.stringField(ACTION_COL, GSON.toJson(rule.getThen())));

    long currentTime = getCurrentTime();
    ruleFields.add(Fields.longField(CREATED_COL, currentTime));
    ruleFields.add(Fields.longField(UPDATED_COL, currentTime));

    rulesTable.upsert(ruleFields);
  }

  void updateRule(NamespacedId namespacedId, RuleRequest rule) throws RuleNotFoundException, IOException {
    Collection<Field<?>> keyFields = getKeyFields(namespacedId);

    if (!rulesTable.read(keyFields).isPresent()) {
      throw new RuleNotFoundException(
        String.format("Rule '%s' in namespace '%s' does not exist. Create it first with a POST request.",
                      namespacedId.getId(),
                      namespacedId.getNamespace().getName()
        )
      );
    }

    rule.validate();

    Collection<Field<?>> updatedRuleFields = new ArrayList<>(keyFields);
    updatedRuleFields.add(Fields.stringField(DESCRIPTION_COL, rule.getDescription()));
    updatedRuleFields.add(Fields.stringField(CONDITION_COL, rule.getWhen()));
    updatedRuleFields.add(Fields.stringField(ACTION_COL, GSON.toJson(rule.getThen())));
    updatedRuleFields.add(Fields.longField(UPDATED_COL, getCurrentTime()));

    rulesTable.upsert(updatedRuleFields);
  }

  Map<String, Object> retrieveRule(NamespacedId namespacedId) throws RuleNotFoundException, IOException {
    Collection<Field<?>> keyFields = getKeyFields(namespacedId);
    Optional<StructuredRow> optionalStructuredRow = rulesTable.read(keyFields);

    if (!optionalStructuredRow.isPresent()) {
      throw new RuleNotFoundException(
        String.format("Rule '%s' in namespace '%s' does not exist. Create it first with a POST request.",
                      namespacedId.getId(),
                      namespacedId.getNamespace().getName()
        )
      );
    }

    StructuredRow row = optionalStructuredRow.get();
    Map<String, Object> rule = new TreeMap<>();
    rule.put(ID_COL, row.getString(ID_COL));
    rule.put(DESCRIPTION_COL, row.getString(DESCRIPTION_COL));
    rule.put(CONDITION_COL, row.getString(CONDITION_COL));
    rule.put(CREATED_COL, row.getLong(CREATED_COL));
    rule.put(UPDATED_COL, row.getLong(UPDATED_COL));

    String then = row.getString(ACTION_COL);
    List<String> thens = new ArrayList<>();
    Collections.addAll(thens, Objects.requireNonNull(then).split(";"));
    rule.put(ACTION_COL, thens);

    return rule;
  }

  void deleteRule(NamespacedId namespacedId) throws RuleNotFoundException, IOException {
    Collection<Field<?>> keyFields = getKeyFields(namespacedId);
    Optional<StructuredRow> optionalStructuredRow = rulesTable.read(keyFields);

    if (!optionalStructuredRow.isPresent()) {
      throw new RuleNotFoundException(
        String.format("Rule '%s' in namespace '%s' does not exist. Create it first with a POST request.",
                      namespacedId.getId(),
                      namespacedId.getNamespace().getName()
        )
      );
    }

    rulesTable.delete(keyFields);
  }

  String retrieveUsingRuleTemplate(String id) throws RuleNotFoundException, IOException {
    Collection<Field<?>> keyFields = new ArrayList<>();
    keyFields.add(Fields.stringField(ID_COL, id));
    Optional<StructuredRow> optionalStructuredRow = rulesTable.read(keyFields);

    if (!optionalStructuredRow.isPresent()) {
      throw new RuleNotFoundException(
        String.format("Rule '%s' does not exist. Create it first with a POST request.", id)
      );
    }

    StructuredRow row = optionalStructuredRow.get();

    return String.format(RULE_TEMPLATE, row.getString(ID_COL), row.getString(DESCRIPTION_COL),
                         row.getString(CONDITION_COL), row.getString(ACTION_COL));
  }

  /**
   * Creates a rulebook with the rules specified in {@link RulebookRequest}. All rules must have been created.
   * Otherwise, a {@link RuleNotFoundException} is thrown.
   *
   * @param namespacedId the namespace in which to create the rulebook
   * @param rulebookRequest the request to create a rulebook
   * @throws RulebookAlreadyExistsException if the rulebook already exists
   * @throws RuleNotFoundException if one of the rules in the request does not exist
   * @throws IOException if there is an IO error
   */
  void createRulebook(NamespacedId namespacedId, RulebookRequest rulebookRequest)
    throws RulebookAlreadyExistsException, RuleNotFoundException, IOException {
    Collection<Field<?>> rulebookKeyFields = getKeyFields(namespacedId);
    Optional<StructuredRow> optionalRulebookStructuredRow = rulebookTable.read(rulebookKeyFields);

    if (optionalRulebookStructuredRow.isPresent()) {
      throw new RulebookAlreadyExistsException(
        String.format("Rulebook '%s' in namespace '%s' already exists. Either update it or delete it and create it.",
                      namespacedId.getId(), namespacedId.getNamespace().getName())
      );
    }

    Namespace namespace = namespacedId.getNamespace();

    for (String rule : rulebookRequest.getRules()) {
      Collection<Field<?>> ruleKeyFields = new ArrayList<>();
      ruleKeyFields.add(Fields.stringField(NAMESPACE_COL, namespace.getName()));
      ruleKeyFields.add(Fields.longField(GENERATION_COL, namespace.getGeneration()));
      ruleKeyFields.add(Fields.stringField(ID_COL, rule));

      Optional<StructuredRow> optionalRuleStructuredRow = rulesTable.read(ruleKeyFields);

      if (!optionalRuleStructuredRow.isPresent()) {
        throw new RuleNotFoundException(
          String.format(
            "Rulebook '%s' in namespace '%s' includes a rule '%s' that does not exist. Please add rule first",
            namespacedId.getId(), namespace.getName(), rule)
        );
      }
    }

    Collection<Field<?>> rulebookFields = new ArrayList<>(rulebookKeyFields);
    rulebookFields.add(Fields.stringField(DESCRIPTION_COL, rulebookRequest.getDescription()));
    rulebookFields.add(Fields.stringField(USER_COL, rulebookRequest.getUser()));
    rulebookFields.add(Fields.longField(VERSION_COL, 1L));
    rulebookFields.add(Fields.stringField(RULES_COL, GSON.toJson(rulebookRequest.getRules())));

    long currentTime = getCurrentTime();
    rulebookFields.add(Fields.longField(CREATED_COL, currentTime));
    rulebookFields.add(Fields.longField(UPDATED_COL, currentTime));

    rulebookTable.upsert(rulebookFields);
  }

  /**
   * Creates a rulebook with the rules specified in {@link RulebookRequest}. The method attempts to create the rules
   * specified in {@link Rulebook} object. However, a {@link RuleNotFoundException} might be thrown when creating the
   * rulebook if one of the rules is not found. This case may occur if there was an exception thrown when attempting to
   * create a rule.
   *
   * @param rulebook the rulebook object to be created
   * @throws RulebookAlreadyExistsException if the rulebook already exists
   * @throws IOException if there is an IO error
   * @throws RuleNotFoundException if one of the rules in the rulebook does not exist. This exception maybe thrown if
   * there was an exception thrown during the creation of the rule
   */
  void createRulebook(NamespacedId namespacedId, Rulebook rulebook)
    throws RulebookAlreadyExistsException, IOException, RuleNotFoundException {
    List<String> ruleIds = new ArrayList<>();

    for (Rule rule : rulebook.getRules()) {
      RuleRequest request = new RuleRequest(rule.getName(), rule.getDescription(), rule.getWhen(), rule.getThen());
      ruleIds.add(rule.getName());

      try {
        createRule(namespacedId, request);
      } catch (RuleAlreadyExistsException e) {
        // Nothing to be done here.
      }
    }

    RulebookRequest rbreq = new RulebookRequest(rulebook.getName(), rulebook.getMeta().getDescription(),
                                                rulebook.getMeta().getSource(), rulebook.getMeta().getUser(),
                                                ruleIds);
    createRulebook(namespacedId, rbreq);
  }

  void updateRulebook(NamespacedId namespacedId, RulebookRequest rulebookRequest)
    throws RulebookAlreadyExistsException, IOException {
    Collection<Field<?>> keyFields = getKeyFields(namespacedId);
    Optional<StructuredRow> optionalStructuredRow = rulebookTable.read(keyFields);

    if (!optionalStructuredRow.isPresent()) {
      throw new RulebookAlreadyExistsException(
        String.format("Rulebook '%s' in namespace '%s' does not exist. Create it first with a POST request.",
                      namespacedId.getId(),
                      namespacedId.getNamespace().getName())
      );
    }

    Collection<Field<?>> updatedRulebookFields = new ArrayList<>(keyFields);
    updatedRulebookFields.add(Fields.stringField(DESCRIPTION_COL, rulebookRequest.getDescription()));
    updatedRulebookFields.add(Fields.stringField(USER_COL, rulebookRequest.getUser()));
    updatedRulebookFields.add(Fields.stringField(SOURCE_COL, rulebookRequest.getSource()));
    updatedRulebookFields.add(Fields.longField(VERSION_COL, optionalStructuredRow.get().getLong(VERSION_COL)));
    updatedRulebookFields.add(Fields.stringField(RULES_COL, GSON.toJson(rulebookRequest.getRules())));

    rulebookTable.upsert(updatedRulebookFields);
  }

  void addRuleToRulebook(NamespacedId rulebookNamespacedId, NamespacedId ruleNamespacedId)
    throws RuleAlreadyExistsException, RulebookNotFoundException, RuleNotFoundException, IOException {
    Collection<Field<?>> rulebookKeyFields = getKeyFields(rulebookNamespacedId);
    Optional<StructuredRow> optionalRulebookStructuredRow = rulebookTable.read(rulebookKeyFields);

    if (!optionalRulebookStructuredRow.isPresent()) {
      throw new RulebookNotFoundException(
        String.format("Rulebook '%s' in namespace '%s' not found.",
                      rulebookNamespacedId.getId(),
                      rulebookNamespacedId.getNamespace().getName())
      );
    }

    Collection<Field<?>> ruleKeyFields = getKeyFields(ruleNamespacedId);
    Optional<StructuredRow> optionalRuleStructuredRow = rulesTable.read(ruleKeyFields);

    if (!optionalRuleStructuredRow.isPresent()) {
      throw new RuleNotFoundException(
        String.format(
          "Attempt to add rule '%s' to rulebook '%s' in namespace '%s' failed as rule doesn't exist in rules database.",
          rulebookNamespacedId.getId(), ruleNamespacedId.getId(), rulebookNamespacedId.getNamespace().getName())
      );
    }

    StructuredRow rulebookRow = optionalRulebookStructuredRow.get();
    List<String> rules = GSON.fromJson(rulebookRow.getString(RULES_COL), LIST_STRING_TYPE);

    if (rules.contains(ruleNamespacedId.getId())) {
      throw new RuleAlreadyExistsException(
        String.format("Rule '%s' already exists in the rulebook '%s' in namespace '%s'.",
                      ruleNamespacedId.getId(), rulebookNamespacedId.getId(),
                      rulebookNamespacedId.getNamespace().getName())
      );
    }

    rules.add(ruleNamespacedId.getId());

    Collection<Field<?>> rulebookFields = new ArrayList<>(rulebookKeyFields);
    rulebookFields.add(Fields.longField(UPDATED_COL, getCurrentTime()));
    rulebookFields.add(Fields.longField(VERSION_COL, rulebookRow.getLong(VERSION_COL)));
    rulebookFields.add(Fields.stringField(RULES_COL, GSON.toJson(rules)));

    rulebookTable.upsert(rulebookFields);
  }

  void removeRuleFromRulebook(NamespacedId rulebookNamespacedId, NamespacedId ruleNamespacedId)
    throws RulebookNotFoundException, RuleNotFoundException, IOException {
    Collection<Field<?>> rulebookKeyFields = getKeyFields(rulebookNamespacedId);
    Optional<StructuredRow> optionalRulebookStructuredRow = rulebookTable.read(rulebookKeyFields);

    if (!optionalRulebookStructuredRow.isPresent()) {
      throw new RulebookNotFoundException(
        String.format("Rulebook '%s' in namespace '%s' not found.",
                      rulebookNamespacedId.getId(),
                      rulebookNamespacedId.getNamespace().getName())
      );
    }

    Collection<Field<?>> ruleKeyFields = getKeyFields(ruleNamespacedId);
    Optional<StructuredRow> optionalRuleStructuredRow = rulesTable.read(ruleKeyFields);

    if (!optionalRuleStructuredRow.isPresent()) {
      throw new RuleNotFoundException(
        String.format(
          "Attempt to remove rule '%s' from rulebook '%s' in namespace '%s' failed as rule doesn't exist in rules database.",
          rulebookNamespacedId.getId(), ruleNamespacedId.getId(), rulebookNamespacedId.getNamespace().getName())
      );
    }

    StructuredRow rulebookRow = optionalRulebookStructuredRow.get();
    List<String> rules = GSON.fromJson(rulebookRow.getString(RULES_COL), LIST_STRING_TYPE);
    rules.remove(ruleNamespacedId.getId());

    Collection<Field<?>> rulebookFields = new ArrayList<>(rulebookKeyFields);
    rulebookFields.add(Fields.longField(UPDATED_COL, getCurrentTime()));
    rulebookFields.add(Fields.longField(VERSION_COL, rulebookRow.getLong(VERSION_COL) + 1));
    rulebookFields.add(Fields.stringField(RULES_COL, GSON.toJson(rules)));

    rulebookTable.upsert(rulebookFields);
  }

  void deleteRulebook(NamespacedId namespacedId) throws RulebookNotFoundException, IOException {
    Collection<Field<?>> keyFields = getKeyFields(namespacedId);
    Optional<StructuredRow> optionalStructuredRow = rulebookTable.read(keyFields);

    if (!optionalStructuredRow.isPresent()) {
      throw new RulebookNotFoundException(
        String.format("Rulebook '%s' in namespace '%s' not found.", namespacedId.getId(),
                      namespacedId.getNamespace().getName()));
    }

    rulebookTable.delete(keyFields);
  }

  JsonArray getRulebookRules(NamespacedId namespacedId) throws RulebookNotFoundException, IOException {
    Collection<Field<?>> rulebookKeyFields = getKeyFields(namespacedId);
    Optional<StructuredRow> optionalRulebookStructuredRow = rulebookTable.read(rulebookKeyFields);

    if (!optionalRulebookStructuredRow.isPresent()) {
      throw new RulebookNotFoundException(
        String.format("Rulebook '%s' in namespace '%s' not found.", namespacedId.getId(),
                      namespacedId.getNamespace().getName()));
    }

    StructuredRow rulebookRow = optionalRulebookStructuredRow.get();
    List<String> rules = GSON.fromJson(rulebookRow.getString(RULES_COL), LIST_STRING_TYPE);
    Namespace namespace = namespacedId.getNamespace();
    String namespaceName = namespace.getName();
    long namespaceGeneration = namespace.getGeneration();
    JsonArray array = new JsonArray();

    for (String rule : rules) {
      Collection<Field<?>> ruleKeyFields = new ArrayList<>();
      ruleKeyFields.add(Fields.stringField(NAMESPACE_COL, namespaceName));
      ruleKeyFields.add(Fields.longField(GENERATION_COL, namespaceGeneration));
      ruleKeyFields.add(Fields.stringField(ID_COL, rule));

      Optional<StructuredRow> optionalRuleStructuredRow = rulesTable.read(ruleKeyFields);

      if (!optionalRuleStructuredRow.isPresent()) {
        continue;
      }

      StructuredRow ruleRow = optionalRuleStructuredRow.get();
      JsonObject object = new JsonObject();
      object.addProperty(NAMESPACE_COL, ruleRow.getString(NAMESPACE_COL));
      object.addProperty(GENERATION_COL, ruleRow.getLong(GENERATION_COL));
      object.addProperty(ID_COL, ruleRow.getString(ID_COL));
      object.addProperty(DESCRIPTION_COL, ruleRow.getString(DESCRIPTION_COL));
      object.addProperty(CONDITION_COL, ruleRow.getString(CONDITION_COL));
      object.addProperty(CREATED_COL, ruleRow.getLong(CREATED_COL));
      object.addProperty(UPDATED_COL, ruleRow.getLong(UPDATED_COL));

      JsonArray actions = new JsonArray();
      String then = ruleRow.getString(ACTION_COL);

      if (then != null) {
        for (String t : then.split(";")) {
          actions.add(new JsonPrimitive(t));
        }

        object.add(ACTION_COL, actions);
      }

      array.add(object);
    }

    return array;
  }

  String generateRulebook(NamespacedId namespacedId)
    throws RulebookNotFoundException, RuleNotFoundException, IOException {
    Collection<Field<?>> rulebookKeyFields = getKeyFields(namespacedId);
    Optional<StructuredRow> optionalRulebookStructuredRow = rulebookTable.read(rulebookKeyFields);

    if (!optionalRulebookStructuredRow.isPresent()) {
      throw new RulebookNotFoundException(
        String.format("Rulebook '%s' in namespace '%s' not found.", namespacedId.getId(),
                      namespacedId.getNamespace().getName()));
    }

    StructuredRow rulebookRow = optionalRulebookStructuredRow.get();
    List<String> rules = GSON.fromJson(rulebookRow.getString(RULES_COL), LIST_STRING_TYPE);
    Namespace namespace = namespacedId.getNamespace();
    String namespaceName = namespace.getName();
    long namespaceGeneration = namespace.getGeneration();
    List<String> ruleOutput = new ArrayList<>();

    for (String rule : rules) {
      Collection<Field<?>> ruleKeyFields = new ArrayList<>();
      ruleKeyFields.add(Fields.stringField(NAMESPACE_COL, namespaceName));
      ruleKeyFields.add(Fields.longField(GENERATION_COL, namespaceGeneration));
      ruleKeyFields.add(Fields.stringField(ID_COL, rule));

      Optional<StructuredRow> optionalRuleStructuredRow = rulesTable.read(ruleKeyFields);

      if (!optionalRuleStructuredRow.isPresent()) {
        throw new RuleNotFoundException(
          String.format(
            "Rulebook '%s' in namespace '%s' contains rule '%s', but rule '%s' is not present in rules database.",
            namespacedId.getId(), namespace.getName(), rule, rule)
        );
      }

      StructuredRow ruleRow = optionalRuleStructuredRow.get();
      ruleOutput.add(String.format(RULE_TEMPLATE, ruleRow.getString(ID_COL), ruleRow.getString(DESCRIPTION_COL),
                                   ruleRow.getString(CONDITION_COL), ruleRow.getString(ACTION_COL)));
    }

    return String.format(RULEBOOK_TEMPLATE, rulebookRow.getString(ID_COL), rulebookRow.getLong(VERSION_COL),
                         rulebookRow.getString(DESCRIPTION_COL), rulebookRow.getLong(CREATED_COL),
                         rulebookRow.getLong(UPDATED_COL), rulebookRow.getString(SOURCE_COL),
                         rulebookRow.getString(USER_COL), ruleOutput);
  }

  /**
   * Lists the rules and rulebooks present in the system.
   *
   * @return List of rules in the system.
   */
  List<Map<String, Object>> rules(Namespace namespace) throws IOException {
    List<Map<String, Object>> rules = new ArrayList<>();
    List<Field<?>> keyFields = new ArrayList<>(2);
    keyFields.add(Fields.stringField(NAMESPACE_COL, namespace.getName()));
    keyFields.add(Fields.longField(GENERATION_COL, namespace.getGeneration()));
    Range range = Range.singleton(keyFields);

    try (CloseableIterator<StructuredRow> rowIter = rulesTable.scan(range, Integer.MAX_VALUE)) {
      while (rowIter.hasNext()) {
        StructuredRow row = rowIter.next();
        Map<String, Object> object = new HashMap<>();
        object.put(NAMESPACE_COL, row.getString(NAMESPACE_COL));
        object.put(GENERATION_COL, row.getLong(GENERATION_COL));
        object.put(ID_COL, row.getString(ID_COL));
        object.put(DESCRIPTION_COL, row.getString(DESCRIPTION_COL));
        object.put(CONDITION_COL, row.getString(CONDITION_COL));
        object.put(ACTION_COL, row.getString(ACTION_COL));
        object.put(CREATED_COL, row.getLong(CREATED_COL));
        object.put(UPDATED_COL, row.getLong(UPDATED_COL));
        rules.add(object);
      }
    }

    return rules;
  }

  List<Map<String, Object>> rulebooks(Namespace namespace) throws IOException {
    List<Map<String, Object>> rulebooks = new ArrayList<>();
    List<Field<?>> keyFields = new ArrayList<>(2);
    keyFields.add(Fields.stringField(NAMESPACE_COL, namespace.getName()));
    keyFields.add(Fields.longField(GENERATION_COL, namespace.getGeneration()));
    Range range = Range.singleton(keyFields);

    try (CloseableIterator<StructuredRow> rowIter = rulebookTable.scan(range, Integer.MAX_VALUE)) {
      while (rowIter.hasNext()) {
        StructuredRow row = rowIter.next();
        Map<String, Object> object = new HashMap<>();
        object.put(NAMESPACE_COL, row.getString(NAMESPACE_COL));
        object.put(GENERATION_COL, row.getLong(GENERATION_COL));
        object.put(ID_COL, row.getString(ID_COL));
        object.put(DESCRIPTION_COL, row.getString(DESCRIPTION_COL));
        object.put(USER_COL, row.getString(USER_COL));
        object.put(SOURCE_COL, row.getString(SOURCE_COL));
        object.put(VERSION_COL, row.getLong(VERSION_COL));
        object.put(RULES_COL, row.getString(RULES_COL));
        object.put(CREATED_COL, row.getLong(CREATED_COL));
        object.put(UPDATED_COL, row.getLong(UPDATED_COL));
        rulebooks.add(object);
      }
    }
    return rulebooks;
  }

  private long getCurrentTime() {
    return (System.currentTimeMillis() / 1000);
  }

  private Collection<Field<?>> getKeyFields(NamespacedId namespacedId) {
    Namespace namespace = namespacedId.getNamespace();
    List<Field<?>> keyFields = new ArrayList<>(3);
    keyFields.add(Fields.stringField(NAMESPACE_COL, namespace.getName()));
    keyFields.add(Fields.longField(GENERATION_COL, namespace.getGeneration()));
    keyFields.add(Fields.stringField(ID_COL, namespacedId.getId()));

    return keyFields;
  }
}
