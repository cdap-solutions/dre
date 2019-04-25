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

import co.cask.wrangler.api.Row;
import edu.emory.mathcs.backport.java.util.TreeMap;
import org.apache.commons.jexl3.JexlContext;

import java.util.Map;

/**
 * Class description here.
 */
public class RowActiveSet implements JexlContext, ActiveSet<Row> {
  private Map<String, Object> map = new TreeMap();

  public RowActiveSet(Row row) {
    set(row);
  }

  @Override
  public Row get() {
    Row row = new Row();
    for (Map.Entry<String, Object> entry : map.entrySet()) {
      row.add(entry.getKey(), entry.getValue());
    }
    return row;
  }

  @Override
  public void set(Row value) {
    map.clear();
    for(int i = 0; i < value.length(); ++i) {
      map.put(value.getColumn(i), value.getValue(i));
    }
  }


  @Override
  public Object get(String name) {
    return map.get(name);
  }

  @Override
  public void set(String name, Object value) {
    map.put(name, value);
  }

  @Override
  public boolean has(String name) {
    return map.containsKey(name);
  }
}
