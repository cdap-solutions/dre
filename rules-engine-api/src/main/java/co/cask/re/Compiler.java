package co.cask.re;

import java.io.Reader;

/**
 * Class description here.
 */
public interface Compiler {
  Rulebook compile(Reader reader) throws RulebookCompileException;
}
