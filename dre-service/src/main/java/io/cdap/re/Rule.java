/*
 *
 * Copyright © 2019 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.re;

import java.util.ArrayList;
import java.util.List;

public final class Rule {

  private final String id;
  private final String namespace;
  private final String description;
  private final String when;
  private final List<String> then;
  private final long created;
  private final long updated;

  private Rule(Builder builder) {
    this.id = builder.id;
    this.namespace = builder.namespace;
    this.description = builder.description;
    this.when = builder.when;
    this.then = builder.then;
    this.created = builder.created;
    this.updated = builder.updated;
  }

  String getId() {
    return id;
  }

  String getNamespace() {
    return namespace;
  }

  String getDescription() {
    return description;
  }

  String getWhen() {
    return when;
  }

  List<String> getThen() {
    return then;
  }

  long getUpdated() {
    return updated;
  }

  long getCreated() {
    return created;
  }

  /**
   * Validates the state of this {@link Rule} excluding the id.
   */
  void validate() {
    if (description == null || description.trim().isEmpty()) {
      throw new IllegalArgumentException(
        String.format("Rule '%s' requires the mandatory field 'description'", id)
      );
    }

    if (when == null || when.trim().isEmpty()) {
      throw new IllegalArgumentException(
        String.format("Rule '%s' requires mandatory field 'when'", id)
      );
    }

    if (then == null || then.isEmpty()) {
      throw new IllegalArgumentException(
        String.format("Rule '%s' requires the mandatory field 'then'", id)
      );
    }
  }

  static final class Builder {

    private final String id;
    private final String namespace;

    private String description;
    private String when;
    private List<String> then = new ArrayList<>();
    private long created = -1;
    private long updated = -1;

    Builder(String id, String namespace) {
      this.id = id;
      this.namespace = namespace;
    }

    Builder description(String description) {
      this.description = description;

      return this;
    }

    Builder when(String when) {
      this.when = when;

      return this;
    }

    Builder then(List<String> then) {
      this.then = then;

      return this;
    }

    Builder created(long created) {
      this.created = created;

      return this;
    }

    Builder updated(long updated) {
      this.updated = updated;

      return this;
    }

    Rule build() {
      if (description == null || description.isEmpty()) {
        description = "No rule description";
      }

      if (when == null || when.isEmpty()) {
        throw new IllegalArgumentException(
          String.format("Rule '%s' has no 'when' clause specified.", id)
        );
      }

      if (then.size() == 0) {
        throw new IllegalArgumentException(
          String.format(
            "Rule '%s' has not specified any 'then' clauses", id)
        );
      }

      if (created < 0) {
        throw new IllegalArgumentException(
          String.format(
            "Rule '%s' has a created time ('%s') less than 0", id, created)
        );
      }

      if (updated < 0) {
        throw new IllegalArgumentException(
          String.format(
            "Rule '%s' has an updated time ('%s') less than 0", id, updated)
        );
      }

      return new Rule(this);
    }

  }
}

