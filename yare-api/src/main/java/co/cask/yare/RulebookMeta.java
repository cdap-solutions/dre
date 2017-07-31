package co.cask.yare;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Class description here.
 */
public class RulebookMeta {
  private String description;
  private long createDate;
  private long updateDate;
  private String source;
  private String user;

  public RulebookMeta(String description, long createDate, long updateDate, String source, String user) {
    this.description = description;
    this.createDate = createDate;
    this.updateDate = updateDate;
    this.source = source;
    this.user = user;
  }

  public RulebookMeta() {
    this("No description", System.currentTimeMillis()/1000, System.currentTimeMillis()/1000, "", "default");
  }

  public String getDescription() {
    return description;
  }

  public long getCreateDate() {
    return createDate;
  }

  public long getUpdateDate() {
    return updateDate;
  }

  public String getSource() {
    return source;
  }

  public String getUser() {
    return user;
  }

  public JsonElement toJson() {
    JsonObject object = new JsonObject();
    object.addProperty("description", description);
    object.addProperty("create-date", createDate);
    object.addProperty("update-date", updateDate);
    object.addProperty("source", source);
    object.addProperty("user", user);
    return object;
  }
}
