package co.cask.re;

import com.google.common.base.Joiner;

import java.util.List;

/**
 * Class description here.
 */
public class RulebookRequest {
  private final String id;
  private final String description;
  private final String source;
  private final String user;
  private final List<String> rules;

  public RulebookRequest(String id, String description, String source, String user, List<String> rules) {
    this.id = id;
    this.description = description;
    this.source = source;
    this.user = user;
    this.rules = rules;
  }

  public String getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public String getSource() {
    return source;
  }

  public String getUser() {
    return user;
  }

  public List<String> getRules() {
    return rules;
  }

  public String getRulesString() {
    return Joiner.on(",").join(rules);
  }
}
