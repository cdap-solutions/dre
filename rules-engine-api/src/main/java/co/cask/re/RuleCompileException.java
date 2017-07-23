package co.cask.re;

/**
 * Class description here.
 */
public class RuleCompileException extends Exception {
  private Rule rule;
  public RuleCompileException(String message, Rule rule) {
    super(message);
    this.rule = rule;
  }

  public Rule getRule() {
    return rule;
  }
}
