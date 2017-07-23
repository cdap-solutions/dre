package co.cask.re;

import co.cask.wrangler.api.Row;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Class description here.
 */
public class RuleInferenceEngineTest {

  @Test
  public void testWorkingInferenceEngine() throws Exception {
    InputStream resourceAsStream = RulebookGrammarCompilerTest.class
      .getClassLoader().getResourceAsStream("send-to-error-rulebook.rb");

    Reader reader = new InputStreamReader(resourceAsStream);
    Compiler compiler = new RulebookCompiler();
    Rulebook rulebook = compiler.compile(reader);

    InferenceEngine ie = new RuleInferenceEngine(rulebook, null);
    ie.initialize();

    List<Row> rows = new ArrayList<>();
    rows.add(new Row("a", 10).add("id", 123).
      add("ssn", "123-45-6789").add("address", "Mars Ave"));
    rows.add(new Row("a", 10).add("id", 123).
      add("ssn", "123-45-6789").add("address", "Mars Ave").add("fare", 11.0));

    for (Row row : rows) {
      try {
        Row result = ie.infer(row);
        Assert.assertNotNull(result);
      } catch (SkipRowException e) {
        Rule rule = e.getRule();
        System.out.println(rule.toJson().toString());
      }
    }

    ie.destroy();
  }

  @Test
  public void testTitanicParsing() throws Exception {
    InputStream resourceAsStream = RulebookGrammarCompilerTest.class
      .getClassLoader().getResourceAsStream("titanic-rulebook.rb");

    Reader reader = new InputStreamReader(resourceAsStream);
    Compiler compiler = new RulebookCompiler();
    Rulebook rulebook = compiler.compile(reader);

    InferenceEngine ie = new RuleInferenceEngine(rulebook, null);
    ie.initialize();

    List<Row> rows = new ArrayList<>();
    //rows.add(new Row("body", "PassengerId,Survived,Pclass,Name,Sex,Age,SibSp,Parch,Ticket,Fare,Cabin,Embarked"));
    rows.add(new Row("body", "1,0,3,\"Braund, Mr. Owen Harris\",male,22,1,0,A/5 21171,7.25,,S"));
    rows.add(new Row("body", "2,1,1,\"Cumings, Mrs. John Bradley (Florence Briggs Thayer)\",female,38,1,0,PC 17599,71.2833,C85,C"));
    rows.add(new Row("body", "3,1,3,\"Heikkinen, Miss. Laina\",female,26,0,0,STON/O2. 3101282,7.925,,S"));

    for (Row row : rows) {
      try {
        Row result = ie.infer(row);
        Assert.assertNotNull(result);
      } catch (SkipRowException e) {
        Rule rule = e.getRule();
        System.out.println(rule.toJson().toString());
      }
    }

    ie.destroy();
  }

}