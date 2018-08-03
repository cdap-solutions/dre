/*
 * Copyright © 2017-2018 Cask Data, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License. You may obtain a copy of
 *  the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 */

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
