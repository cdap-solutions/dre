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

import io.cdap.re.parser.RulebookBaseVisitor;
import io.cdap.re.parser.RulebookParser;
import org.antlr.v4.runtime.tree.ParseTree;

import static org.apache.commons.lang3.StringUtils.trim;

/**
 * Class description here.
 */
public class RulebookASTVisitor extends RulebookBaseVisitor<Rulebook.Builder> {
  private Rulebook.Builder rbBuilder;
  private Rule.Builder ruleBuilder;

  public Rulebook get() {
    return rbBuilder.build();
  }

  private String trimQuote(String value) {
    return value.substring(1, value.length() - 1);
  }

  @Override
  public Rulebook.Builder visitRulebook(RulebookParser.RulebookContext ctx) {
    rbBuilder = new Rulebook.Builder(ctx.Identifier().toString());
    return super.visitRulebook(ctx);
  }

  @Override
  public Rulebook.Builder visitRbVersion(RulebookParser.RbVersionContext ctx) {
    rbBuilder.version(Long.parseLong(ctx.Number().getText()));
    return super.visitRbVersion(ctx);
  }

  @Override
  public Rulebook.Builder visitRbMetaDescription(RulebookParser.RbMetaDescriptionContext ctx) {
    rbBuilder.description(trimQuote(ctx.String().getText()));
    return super.visitRbMetaDescription(ctx);
  }

  @Override
  public Rulebook.Builder visitRbMetaCreatedDate(RulebookParser.RbMetaCreatedDateContext ctx) {
    rbBuilder.created(Long.parseLong(ctx.Number().getText()));
    return super.visitRbMetaCreatedDate(ctx);
  }

  @Override
  public Rulebook.Builder visitRbMetaUpdatedDate(RulebookParser.RbMetaUpdatedDateContext ctx) {
    rbBuilder.updated(Long.parseLong(ctx.Number().getText()));
    return super.visitRbMetaUpdatedDate(ctx);
  }

  @Override
  public Rulebook.Builder visitRbMetaUserName(RulebookParser.RbMetaUserNameContext ctx) {
    rbBuilder.user(trimQuote(ctx.String().getText()));
    return super.visitRbMetaUserName(ctx);
  }

  @Override
  public Rulebook.Builder visitRbMetaSourceName(RulebookParser.RbMetaSourceNameContext ctx) {
    rbBuilder.source(trimQuote(ctx.String().getText()));
    return super.visitRbMetaSourceName(ctx);
  }


  @Override
  public Rulebook.Builder visitRbRule(RulebookParser.RbRuleContext ctx) {
    ruleBuilder = new Rule.Builder(ctx.Identifier().toString());
    super.visitRbRule(ctx);
    rbBuilder.addRule(ruleBuilder.build());
    return rbBuilder;
  }

  @Override
  public Rulebook.Builder visitDescriptionClause(RulebookParser.DescriptionClauseContext ctx) {
    ruleBuilder.description(trimQuote(ctx.String().getText()));
    return super.visitDescriptionClause(ctx);
  }

  @Override
  public Rulebook.Builder visitAssignments(RulebookParser.AssignmentsContext ctx) {
    return super.visitAssignments(ctx);
  }

  @Override
  public Rulebook.Builder visitAssignment(RulebookParser.AssignmentContext ctx) {
    int childCount = ctx.getChildCount();
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i < childCount - 1; ++i) {
      ParseTree child = ctx.getChild(i);
      sb.append(trim(child.getText())).append(" ");
    }
    ruleBuilder.given(sb.toString());
    return super.visitAssignment(ctx);
  }

  @Override
  public Rulebook.Builder visitAction(RulebookParser.ActionContext ctx) {
    int childCount = ctx.getChildCount();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < childCount; ++i) {
      ParseTree child = ctx.getChild(i);
      sb.append(trim(child.getText())).append(" ");
    }
    ruleBuilder.then(sb.toString());
    return super.visitAction(ctx);
  }

  @Override
  public Rulebook.Builder visitCondition(RulebookParser.ConditionContext ctx) {
    int childCount = ctx.getChildCount();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < childCount; ++i) {
      ParseTree child = ctx.getChild(i);
      sb.append(trim(child.getText()));
    }
    ruleBuilder.when(sb.toString());
    return super.visitCondition(ctx);
  }

}