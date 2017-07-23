package co.cask.re;

/**
 * Class description here.
 */
public class RuleFunctions {
  public static boolean present(Object value) {
    if (value == null) {
      return false;
    }
    return true;
  }
}
