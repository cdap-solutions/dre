package co.cask.re;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * Class description here.
 */
public class TestingRig {

  public static Rulebook compile(String filename) throws RulebookCompileException, RuleCompileException {
    InputStream resourceAsStream = RulebookGrammarCompilerTest.class
      .getClassLoader().getResourceAsStream(filename);
    Reader reader = new InputStreamReader(resourceAsStream);

    // Compile Rulebook.
    Compiler compiler = new RulebookCompiler();
    Rulebook rulebook = compiler.compile(reader);
    return rulebook;
  }
}
