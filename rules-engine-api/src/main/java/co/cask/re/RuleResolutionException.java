package co.cask.re;

import java.util.List;

/**
 * Class description here.
 */
public class RuleResolutionException extends Exception {
  private List<Rule> unresolved;
  public RuleResolutionException(String message, List<Rule> unresolved) {
    super(message);
    this.unresolved = unresolved;
  }

  public List<Rule> getUnresolved() {
    return unresolved;
  }
}
