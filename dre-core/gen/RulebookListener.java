// Generated from /Users/nitin/Work/Devel/hydrator/rules-engine/rules-engine-core/src/main/antlr4/co/cask/re/parser/Rulebook.g4 by ANTLR 4.7

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

import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link RulebookParser}.
 */
public interface RulebookListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link RulebookParser#rulebook}.
	 * @param ctx the parse tree
	 */
	void enterRulebook(RulebookParser.RulebookContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#rulebook}.
	 * @param ctx the parse tree
	 */
	void exitRulebook(RulebookParser.RulebookContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#statements}.
	 * @param ctx the parse tree
	 */
	void enterStatements(RulebookParser.StatementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#statements}.
	 * @param ctx the parse tree
	 */
	void exitStatements(RulebookParser.StatementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(RulebookParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(RulebookParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#meta}.
	 * @param ctx the parse tree
	 */
	void enterMeta(RulebookParser.MetaContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#meta}.
	 * @param ctx the parse tree
	 */
	void exitMeta(RulebookParser.MetaContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#metaStatements}.
	 * @param ctx the parse tree
	 */
	void enterMetaStatements(RulebookParser.MetaStatementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#metaStatements}.
	 * @param ctx the parse tree
	 */
	void exitMetaStatements(RulebookParser.MetaStatementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#ruleDescription}.
	 * @param ctx the parse tree
	 */
	void enterRuleDescription(RulebookParser.RuleDescriptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#ruleDescription}.
	 * @param ctx the parse tree
	 */
	void exitRuleDescription(RulebookParser.RuleDescriptionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#createdDate}.
	 * @param ctx the parse tree
	 */
	void enterCreatedDate(RulebookParser.CreatedDateContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#createdDate}.
	 * @param ctx the parse tree
	 */
	void exitCreatedDate(RulebookParser.CreatedDateContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#updatedDate}.
	 * @param ctx the parse tree
	 */
	void enterUpdatedDate(RulebookParser.UpdatedDateContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#updatedDate}.
	 * @param ctx the parse tree
	 */
	void exitUpdatedDate(RulebookParser.UpdatedDateContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#userName}.
	 * @param ctx the parse tree
	 */
	void enterUserName(RulebookParser.UserNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#userName}.
	 * @param ctx the parse tree
	 */
	void exitUserName(RulebookParser.UserNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#sourceName}.
	 * @param ctx the parse tree
	 */
	void enterSourceName(RulebookParser.SourceNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#sourceName}.
	 * @param ctx the parse tree
	 */
	void exitSourceName(RulebookParser.SourceNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#versionName}.
	 * @param ctx the parse tree
	 */
	void enterVersionName(RulebookParser.VersionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#versionName}.
	 * @param ctx the parse tree
	 */
	void exitVersionName(RulebookParser.VersionNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#ruleStatement}.
	 * @param ctx the parse tree
	 */
	void enterRuleStatement(RulebookParser.RuleStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#ruleStatement}.
	 * @param ctx the parse tree
	 */
	void exitRuleStatement(RulebookParser.RuleStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#descriptionClause}.
	 * @param ctx the parse tree
	 */
	void enterDescriptionClause(RulebookParser.DescriptionClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#descriptionClause}.
	 * @param ctx the parse tree
	 */
	void exitDescriptionClause(RulebookParser.DescriptionClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#givenClause}.
	 * @param ctx the parse tree
	 */
	void enterGivenClause(RulebookParser.GivenClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#givenClause}.
	 * @param ctx the parse tree
	 */
	void exitGivenClause(RulebookParser.GivenClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#whenClause}.
	 * @param ctx the parse tree
	 */
	void enterWhenClause(RulebookParser.WhenClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#whenClause}.
	 * @param ctx the parse tree
	 */
	void exitWhenClause(RulebookParser.WhenClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#thenClause}.
	 * @param ctx the parse tree
	 */
	void enterThenClause(RulebookParser.ThenClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#thenClause}.
	 * @param ctx the parse tree
	 */
	void exitThenClause(RulebookParser.ThenClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#actions}.
	 * @param ctx the parse tree
	 */
	void enterActions(RulebookParser.ActionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#actions}.
	 * @param ctx the parse tree
	 */
	void exitActions(RulebookParser.ActionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#action}.
	 * @param ctx the parse tree
	 */
	void enterAction(RulebookParser.ActionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#action}.
	 * @param ctx the parse tree
	 */
	void exitAction(RulebookParser.ActionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterCondition(RulebookParser.ConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitCondition(RulebookParser.ConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link RulebookParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpression(RulebookParser.ExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link RulebookParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpression(RulebookParser.ExpressionContext ctx);
}