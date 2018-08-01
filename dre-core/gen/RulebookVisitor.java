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

import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link RulebookParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface RulebookVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link RulebookParser#rulebook}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRulebook(RulebookParser.RulebookContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#statements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatements(RulebookParser.StatementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitStatement(RulebookParser.StatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#meta}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMeta(RulebookParser.MetaContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#metaStatements}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitMetaStatements(RulebookParser.MetaStatementsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#ruleDescription}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRuleDescription(RulebookParser.RuleDescriptionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#createdDate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCreatedDate(RulebookParser.CreatedDateContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#updatedDate}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdatedDate(RulebookParser.UpdatedDateContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#userName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUserName(RulebookParser.UserNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#sourceName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSourceName(RulebookParser.SourceNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#versionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitVersionName(RulebookParser.VersionNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#ruleStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitRuleStatement(RulebookParser.RuleStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#descriptionClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDescriptionClause(RulebookParser.DescriptionClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#givenClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitGivenClause(RulebookParser.GivenClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#whenClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitWhenClause(RulebookParser.WhenClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#thenClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitThenClause(RulebookParser.ThenClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#actions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitActions(RulebookParser.ActionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#action}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitAction(RulebookParser.ActionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCondition(RulebookParser.ConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link RulebookParser#expression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitExpression(RulebookParser.ExpressionContext ctx);
}