package co.cask.yare;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.List;

/**
 * Class description here.
 */
public final class Rule {
  private String name;
  private String description;
  private List<String> given;
  private String when;
  private List<String> then;


  private Rule(String name, String description, List<String> given, String when, List<String> then) {
    this.name = name;
    this.description = description;
    this.given = given;
    this.when = when;
    this.then = then;
  }

  public String getName() {
    return name;
  }

  public List<String> getGiven() {
    return given;
  }


  public String getWhen() {
    return when;
  }

  public List<String> getThen() {
    return then;
  }

  public String getDescription() {
    return description;
  }

  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("name", name);
    object.addProperty("description", description);
    object.addProperty("when", when);
    JsonArray thens = new JsonArray();
    for (String t : then) {
      thens.add(new JsonPrimitive(t));
    }
    object.add("then", thens);
    JsonArray givens = new JsonArray();
    for (String g : given) {
      givens.add(new JsonPrimitive(g));
    }
    object.add("given", givens);
    return object;
  }

  public static final class Builder {
    private String name;
    private String description;
    private String when;
    private List<String> then;
    private List<String> given;

    public Builder(String name) {
      this.name = name;
      this.then = new ArrayList<>();
      this.given = new ArrayList<>();
    }

    public Builder description(String description) {
      this.description = description;
      return this;
    }

    public Builder when(String when) {
      this.when = when;
      return this;
    }

    public Builder then(String then) {
      this.then.add(then);
      return this;
    }

    public Builder given(String given) {
      this.given.add(given);
      return this;
    }

    public Rule build() {
      if (description == null || description.isEmpty()) {
        description = "No rule description";
      }
      if (when == null || when.isEmpty()) {
        throw new IllegalArgumentException(
          String.format("Rule '%s' has no 'when' clause specified.", name)
        );
      }
      if (then.size() == 0) {
        throw new IllegalArgumentException(
          String.format(
            "Rule '%s' has not specified any 'then' clauses", name
          )
        );
      }
      return new Rule(name, description, given, when, then);
    }

  }
}
