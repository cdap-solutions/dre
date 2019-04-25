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

import co.cask.cdap.api.app.AbstractApplication;
import co.cask.cdap.api.data.schema.Schema;
import co.cask.cdap.api.dataset.DatasetProperties;
import co.cask.cdap.api.dataset.table.Table;

/**
 * Class description here.
 */
public class YAREApplication extends AbstractApplication {
  /**
   * Override this method to declare and configure the application.
   */
  @Override
  public void configure() {
    setName("yare");
    setDescription("Yet Another Rules Engine - Just for Big Data.");

    Schema rulebookSchema = Schema.recordOf(
      "rulebook",
      Schema.Field.of("id", Schema.of(Schema.Type.STRING)),
      Schema.Field.of("description", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
      Schema.Field.of("version", Schema.of(Schema.Type.LONG)),
      Schema.Field.of("source", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
      Schema.Field.of("user", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
      Schema.Field.of("created", Schema.of(Schema.Type.LONG)),
      Schema.Field.of("updated", Schema.of(Schema.Type.LONG)),
      Schema.Field.of("rules", Schema.of(Schema.Type.STRING))
    );

    Schema ruleSchema = Schema.recordOf(
      "rules",
      Schema.Field.of("id", Schema.of(Schema.Type.STRING)),
      Schema.Field.of("description", Schema.of(Schema.Type.STRING)),
      Schema.Field.of("condition", Schema.of(Schema.Type.STRING)),
      Schema.Field.of("action", Schema.nullableOf(Schema.of(Schema.Type.STRING))),
      Schema.Field.of("created", Schema.of(Schema.Type.LONG)),
      Schema.Field.of("updated", Schema.of(Schema.Type.LONG))
    );


    createDataset("rulebook", Table.class.getName(), DatasetProperties.builder()
      .add(Table.PROPERTY_SCHEMA, rulebookSchema.toString())
      .add(Table.PROPERTY_SCHEMA_ROW_FIELD, "id")
      .build());

    createDataset("rules", Table.class.getName(), DatasetProperties.builder()
      .add(Table.PROPERTY_SCHEMA, ruleSchema.toString())
      .add(Table.PROPERTY_SCHEMA_ROW_FIELD, "id")
      .build());

    addService("service",
               new YARERulebookService());
  }
}
