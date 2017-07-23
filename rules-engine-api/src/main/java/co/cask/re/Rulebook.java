package co.cask.re;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Class description here.
 */
public final class Rulebook {
  private String name;
  private long version;
  private RulebookMeta meta;
  private List<Rule> rules;

  private Rulebook(String name, RulebookMeta meta, long version, List<Rule> rules) {
    this.name = name;
    this.meta = meta;
    this.version = version;
    this.rules = rules;
  }

  public String getName() {
    return name;
  }

  public RulebookMeta getMeta() {
    return meta;
  }

  public long getVersion() {
    return version;
  }

  public List<Rule> getRules() {
    return rules;
  }

  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("name", name);
    object.addProperty("version", version);
    object.add("meta", meta.toJson());
    JsonArray array = new JsonArray();
    for (Rule rule : rules) {
      array.add(rule.toJson());
    }
    object.add("rules", array);
    return object;
  }

  public Builder builder(String name) {
    return new Builder(name);
  }

  public static final class Builder {
    private String name;
    private long version;
    private String rbDescription;
    private long created;
    private long updated;
    private String source;
    private String user;
    private List<Rule> rules;

    public Builder(String name) {
      this.name = name;
      this.rules = new ArrayList<>();
    }

    public Builder version(long version) {
      this.version = version;
      return this;
    }

    public Builder description(String description) {
      this.rbDescription = description;
      return this;
    }

    public Builder created(long created) {
      this.created = created;
      return this;
    }

    public Builder updated(long updated) {
      this.updated = updated;
      return this;
    }

    public Builder source(String source) {
      this.source = source;
      return this;
    }

    public Builder user(String user) {
      this.user = user;
      return this;
    }

    public Builder addRule(Rule rule) {
      this.rules.add(rule);
      return this;
    }

    public Rulebook build() {
      if (created == 0) {
        created = System.currentTimeMillis() / 1000;
      }

      if (updated == 0) {
        updated = created;
      }

      if (source == null || source.isEmpty()) {
        source = "not defined";
      }

      if (user == null || user.isEmpty()) {
        user = "";
      }
      RulebookMeta meta = new RulebookMeta(rbDescription, created, updated, source, user);
      return new Rulebook(name, meta, version, rules);
    }
  }
}
