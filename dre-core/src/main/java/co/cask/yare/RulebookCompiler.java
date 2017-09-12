package co.cask.yare;

import co.cask.yare.parser.RulebookLexer;
import co.cask.yare.parser.RulebookParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlException;

import java.io.IOException;
import java.io.Reader;
import java.util.List;

/**
 *
 */
public class RulebookCompiler implements Compiler {
  private JexlEngine engine = new JexlBuilder()
    .silent(false)
    .cache(1024*1024)
    .cacheThreshold(1024*1024)
    .strict(true)
    .create();

  @Override
  public Rulebook compile(Reader reader) throws RulebookCompileException {
    try {
      SyntaxErrorListener error = new SyntaxErrorListener();
      CodePointCharStream stream = CharStreams.fromReader(reader);

      RulebookLexer lexer = new RulebookLexer(stream);
      lexer.removeErrorListeners();

      RulebookParser parser = new RulebookParser(new CommonTokenStream(lexer));
      parser.removeErrorListeners();
      parser.addErrorListener(error);

      parser.setBuildParseTree(true);
      ParseTree ast = parser.rulebook();

      if (error.hasErrors()) {
        throw new RulebookCompileException(
          "Error parsing rulebook, please check the rulebook syntax.",
          error.iterator()
        );
      }

      RulebookASTVisitor visitor = new RulebookASTVisitor();
      visitor.visit(ast);
      Rulebook rulebook = visitor.get();
      List<Rule> rules = rulebook.getRules();
      for (Rule rule : rules) {
        try {
          engine.createScript(rule.getWhen());
        } catch (JexlException.Variable e) {
          throw new RulebookCompileException(
            String.format(
              "%s:%s - Variable '%s' is not defined.",
              rulebook.getName(), rule.getName(), e.getVariable()
            )
          );
        } catch (JexlException.Parsing e) {
          throw new RulebookCompileException(
            String.format(
              "%s:%s - %s",
              rulebook.getName(), rule.getName(), e.getMessage()
            )
          );
        }
      }
      return rulebook;
    } catch (IOException e) {
      throw new RulebookCompileException(e.getMessage());
    }
  }

  public static Throwable getRootCause(Throwable throwable) {
    if (throwable.getCause() != null)
      return getRootCause(throwable.getCause());

    return throwable;
  }
}
