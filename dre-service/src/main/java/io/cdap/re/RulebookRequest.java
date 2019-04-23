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

package io.cdap.re;

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
