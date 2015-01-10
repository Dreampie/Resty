package cn.dreampie.orm;

import cn.dreampie.common.Entity;
import cn.dreampie.common.util.json.Jsoner;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Record
 */
public class Record extends Entity<Record> implements Serializable {


  private Map<String, Object> attrs;

  /**
   * Return attrs map.
   */
  public Map<String, Object> getAttrs() {
    if (attrs == null) {
      attrs = new CaseInsensitiveMap<Object>();
    }
    return attrs;
  }

  /**
   * Set attrs value with map.
   *
   * @param attrs the attrs map
   */
  public Record putAttrs(Map<String, Object> attrs) {
    this.getAttrs().putAll(attrs);
    return this;
  }

  /**
   * Set attrs value with record.
   *
   * @param record the record
   */
  public Record putAttrs(Record record) {
    getAttrs().putAll(record.getAttrs());
    return this;
  }

  /**
   * Remove attribute of this record.
   *
   * @param column the column name of the record
   */
  public Record remove(String column) {
    getAttrs().remove(column);
    return this;
  }

  /**
   * Remove attrs of this record.
   *
   * @param columns the column name of the record
   */
  public Record remove(String... columns) {
    if (columns != null)
      for (String c : columns)
        this.getAttrs().remove(c);
    return this;
  }

  /**
   * Remove attrs if it is null.
   */
  public Record removeNullValueAttrs() {
    for (java.util.Iterator<Entry<String, Object>> it = getAttrs().entrySet().iterator(); it.hasNext(); ) {
      Entry<String, Object> e = it.next();
      if (e.getValue() == null) {
        it.remove();
      }
    }
    return this;
  }

  /**
   * Keep attrs of this record and remove other attrs.
   *
   * @param columns the column name of the record
   */
  public Record keep(String... columns) {
    if (columns != null && columns.length > 0) {
      Map<String, Object> newAttrs = new HashMap<String, Object>(columns.length);  // getConfig().containerFactory.getAttrsMap();
      for (String c : columns)
        if (this.getAttrs().containsKey(c))  // prevent put null value to the newAttrs
          newAttrs.put(c, this.getAttrs().get(c));

      this.getAttrs().clear();
      this.getAttrs().putAll(newAttrs);
    } else
      this.getAttrs().clear();
    return this;
  }

  /**
   * Keep column of this record and remove other attrs.
   *
   * @param column the column name of the record
   */
  public Record keep(String column) {
    if (getAttrs().containsKey(column)) {  // prevent put null value to the newAttrs
      Object keepIt = getAttrs().get(column);
      getAttrs().clear();
      getAttrs().put(column, keepIt);
    } else
      getAttrs().clear();
    return this;
  }

  /**
   * Remove all attrs of this record.
   */
  public Record clear() {
    getAttrs().clear();
    return this;
  }

  /**
   * Set column to record.
   *
   * @param column the column name
   * @param value  the value of the column
   */
  public Record set(String column, Object value) {
    getAttrs().put(column, value);
    return this;
  }

  /**
   * Get column of any mysql type
   */
  public <T> T get(String column) {
    return (T) getAttrs().get(column);
  }

  /**
   * Get column of any mysql type. Returns defaultValue if null.
   */
  public <T> T get(String column, Object defaultValue) {
    Object result = getAttrs().get(column);
    return (T) (result != null ? result : defaultValue);
  }

  /**
   * Get column of mysql type: varchar, char, enum, set, text, tinytext, mediumtext, longtext
   */
  public String getStr(String column) {
    return (String) getAttrs().get(column);
  }

  /**
   * Get column of mysql type: int, integer, tinyint(n) n > 1, smallint, mediumint
   */
  public Integer getInt(String column) {
    return (Integer) getAttrs().get(column);
  }

  /**
   * Get column of mysql type: bigint
   */
  public Long getLong(String column) {
    return (Long) getAttrs().get(column);
  }

  /**
   * Get column of mysql type: unsigned bigint
   */
  public java.math.BigInteger getBigInteger(String column) {
    return (java.math.BigInteger) getAttrs().get(column);
  }

  /**
   * Get column of mysql type: date, year
   */
  public java.util.Date getDate(String column) {
    return (java.util.Date) getAttrs().get(column);
  }

  /**
   * Get column of mysql type: time
   */
  public java.sql.Time getTime(String column) {
    return (java.sql.Time) getAttrs().get(column);
  }

  /**
   * Get column of mysql type: timestamp, datetime
   */
  public java.sql.Timestamp getTimestamp(String column) {
    return (java.sql.Timestamp) getAttrs().get(column);
  }

  /**
   * Get column of mysql type: real, double
   */
  public Double getDouble(String column) {
    return (Double) getAttrs().get(column);
  }

  /**
   * Get column of mysql type: float
   */
  public Float getFloat(String column) {
    return (Float) getAttrs().get(column);
  }

  /**
   * Get column of mysql type: bit, tinyint(1)
   */
  public Boolean getBoolean(String column) {
    return (Boolean) getAttrs().get(column);
  }

  /**
   * Get column of mysql type: decimal, numeric
   */
  public java.math.BigDecimal getBigDecimal(String column) {
    return (java.math.BigDecimal) getAttrs().get(column);
  }

  /**
   * Get column of mysql type: binary, varbinary, tinyblob, blob, mediumblob, longblob
   * I have not finished the test.
   */
  public byte[] getBytes(String column) {
    return (byte[]) getAttrs().get(column);
  }

  /**
   * Get column of any type that extends from Number
   */
  public Number getNumber(String column) {
    return (Number) getAttrs().get(column);
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(super.toString()).append(" {");
    boolean first = true;
    for (Entry<String, Object> e : getAttrs().entrySet()) {
      if (first)
        first = false;
      else
        sb.append(", ");

      Object value = e.getValue();
      if (value != null)
        value = value.toString();
      sb.append(e.getKey()).append(":").append(value);
    }
    sb.append("}");
    return sb.toString();
  }

  public boolean equals(Object o) {
    if (!(o instanceof Record))
      return false;
    if (o == this)
      return true;
    return this.getAttrs().equals(((Record) o).getAttrs());
  }

  public int hashCode() {
    return getAttrs() == null ? 0 : getAttrs().hashCode();
  }

  /**
   * Return column name of this record.
   */
  public String[] getColumnNames() {
    Set<String> attrNameSet = getAttrs().keySet();
    return attrNameSet.toArray(new String[attrNameSet.size()]);
  }

  /**
   * Return column values of this record.
   */
  public Object[] getColumnValues() {
    java.util.Collection<Object> attrValueCollection = getAttrs().values();
    return attrValueCollection.toArray(new Object[attrValueCollection.size()]);
  }

  /**
   * Return json string of this record.
   */
  public String toJson() {
    return Jsoner.toJSONString(getAttrs());
  }
}




