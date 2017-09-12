package co.cask.re;

import co.cask.yare.RulebookCompiler;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests {@link RulebookCompiler}
 */
public class RulebookGrammarCompilerTest {
  @Test
  public void testCompilationWithDirectives() throws Exception {
    TestingRig.compile("simple-rulebook-2.rb");
    Assert.assertTrue(true);
  }

}