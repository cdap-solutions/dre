package co.cask.re;

/**
 * Class description here.
 */
public class RuleExecutionException extends Exception {
  public RuleExecutionException(String message) {
    super(message);
  }

  public RuleExecutionException(String message, Throwable e) {
    super(message, e);
  }
}
