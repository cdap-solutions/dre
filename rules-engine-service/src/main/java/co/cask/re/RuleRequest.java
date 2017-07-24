package co.cask.re;

import java.util.List;

/**
 * Class description here.
 */
public final class RuleRequest {
  private final String id;
  private final String description;
  private final String when;
  private final List<String> then;

  public RuleRequest(String id, String description, String when, List<String> then) {
    this.id = id;
    this.description = description;
    this.when = when;
    this.then = then;
  }

  public String getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public String getWhen() {
    return when;
  }

  public String getThen() {
    StringBuilder sb = new StringBuilder();
    for (String t : then) {
      sb.append(t);
      if(!t.endsWith(";")) {
        sb.append(";");
      }
    }
    return sb.toString();
  }
}
