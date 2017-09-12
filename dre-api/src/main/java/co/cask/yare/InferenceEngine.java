package co.cask.yare;

import co.cask.wrangler.api.RecipeException;
import co.cask.wrangler.api.Row;

/**
 * Class description here.
 */
public interface InferenceEngine {
  void initialize() throws RuleCompileException;
  Row infer(Row row) throws RecipeException, SkipRowException;
  void destroy();
}
