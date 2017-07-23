package co.cask.re;

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
