package co.cask.re;


import co.cask.wrangler.api.parser.SyntaxError;

import java.util.Iterator;

/**
 * Class description here.
 */
public class RulebookCompileException extends Exception {
  private Iterator<SyntaxError> it = new Iterator<SyntaxError>() {
    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public SyntaxError next() {
      return null;
    }

    @Override
    public void remove() {
      // no-op
    }
  };

  public RulebookCompileException(String message) {
    super(message);
  }

  public RulebookCompileException(String message, Iterator<SyntaxError> it) {
    super(message);
    this.it = it;
  }

  public Iterator<SyntaxError> iterator() {
    return it;
  }
}
