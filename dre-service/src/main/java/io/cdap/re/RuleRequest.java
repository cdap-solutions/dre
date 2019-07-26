/*
 * Copyright © 2017-2019 Cask Data, Inc.
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

import java.util.Collections;
import java.util.List;

/**
 * Class description here.
 */
final class RuleRequest {

  private final String id;
  private final String description;
  private final String when;
  private final List<String> then;

  RuleRequest(String id, String description, String when, List<String> then) {
    this.id = id;
    this.description = description;
    this.when = when;
    this.then = then;
  }

  /**
   * Validates the state of this {@link RuleRequest} excluding the id.
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

  String getId() {
    return id;
  }

  String getDescription() {
    return description;
  }

  String getWhen() {
    return when;
  }

  List<String> getThen() {
    return then == null ? Collections.emptyList() : then;
  }

}
