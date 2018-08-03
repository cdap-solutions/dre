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
