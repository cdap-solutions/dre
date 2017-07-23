package co.cask.re;

/**
 * Class description here.
 */
public class SkipRowException extends Exception {
  private Rule rule;

  public SkipRowException(Rule rule) {
    this.rule = rule;
  }

  public Rule getRule() {
    return rule;
  }
}
