package co.cask.re;

import co.cask.wrangler.api.Row;
import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlContext;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;
import org.junit.Assert;
import org.junit.Test;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Tests {@link RulebookCompiler}
 */
public class RulebookGrammarCompilerTest {

  private static final String SIMPLE_RULEBOOK = "rulebook 'test1' {\n" +
    "  version 1\n" +
    "  meta {\n" +
    "    description \"Test\"\n" +
    "    created-date 123233\n" +
    "    updated-date 323132\n" +
    "    source \"mdm\"\n" +
    "    user \"nmotgi\"\n" +
    "  }\n" +
    "  \n" +
    "  rule 'window1' {\n" +
    "    description \"test\"\n" +
    "    when (\n" +
    "      a > 10 && d == 1 && test == \"test\"\n" +
    "    ) then {\n" +
    "      d = 1; \n" +
    "      f = 1;\n" +
    "    }\n" +
    "  }\n" +
    "\n" +
    "  rule 'window2' {\n" +
    "    when (\n" +
    "      name == 'test' && count > 10\n" +
    "    ) then {\n" +
    "      a = 1;\n" +
    "      c = \"test\";\n" +
    "    }\n" +
    "  }\n" +
    "}";

  @Test
  public void testBasicFunctionality() throws Exception {
    Compiler compiler = new RulebookCompiler();
    Rulebook rulebook = compiler.compile(new StringReader(SIMPLE_RULEBOOK));
    List<Rule> rules = rulebook.getRules();

    Assert.assertNotNull(rulebook);
  }

  public static class IterableMapContext implements JexlContext {
    private Map<String, Object> map;

    public IterableMapContext() {
      map = new TreeMap<>();
    }

    @Override
    public Object get(String name) {
      return map.get(name);
    }

    @Override
    public void set(String name, Object value) {
      map.put(name, value);
    }

    @Override
    public boolean has(String name) {
      return map.containsKey(name);
    }

    public Map<String, Object> get() {
      return map;
    }
  }

  @Test
  public void testJexl() throws Exception {
    long t1 = System.currentTimeMillis()/1000;
    JexlEngine engine = new JexlBuilder()
      .silent(false)
      .cache(10)
      .strict(true)
      .create();

    Row row = new Row();
    row.add("a", 11);
    row.add("b", 2);

    IterableMapContext ctx = new IterableMapContext();
    for (int i = 0; i < row.length(); ++i) {
      ctx.set(row.getColumn(i), row.getValue(i));
    }

    JexlScript script = engine.createScript("if(a > 10 && b == 2 ) {c=10;f=1}");
    Object execute = script.execute(ctx);

    for(Map.Entry<String, Object> entry : ctx.get().entrySet()) {
      row.addOrSet(entry.getKey(), entry.getValue());
    }
    Assert.assertEquals(10, row.getValue("c"));
  }

  @Test
  public void testCompilationWithDirectives() throws Exception {
    TestingRig.compile("simple-rulebook-2.rb");
    Assert.assertTrue(true);
  }

}