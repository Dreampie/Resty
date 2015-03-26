package cn.dreampie.common.entity;

import cn.dreampie.common.util.json.Jsoner;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ice on 14-12-31.
 */
public abstract class Entity<M extends Entity> {
  /**
   * Attributes of this model
   */
  public abstract Map<String, Object> getAttrs();


  public M putAttrs(Map<String, Object> attrs) {
    getAttrs().putAll(attrs);
    return (M) this;
  }

  /**
   * Set attrs value with entity.
   *
   * @param entity the entity
   */
  public M putAttrs(M entity) {
    return (M) putAttrs(entity.getAttrs());
  }

  public M put(String key, Object value) {
    getAttrs().put(key, value);
    return (M) this;
  }


  /**
   * Return column name of this record.
   */
  public String[] getAttrNames() {
    Set<String> attrNameSet = getAttrs().keySet();
    return attrNameSet.toArray(new String[attrNameSet.size()]);
  }

  /**
   * Return column values of this record.
   */
  public Object[] getAttrValues() {
    java.util.Collection<Object> attrValueCollection = getAttrs().values();
    return attrValueCollection.toArray(new Object[attrValueCollection.size()]);
  }

  /**
   * Set column to entity.
   *
   * @param column the column name
   * @param value  the value of the column
   */
  public M set(String column, Object value) {
    getAttrs().put(column, value);
    return (M) this;
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
   * Remove attribute of this entity.
   *
   * @param column the column name of the entity
   */
  public M remove(String column) {
    getAttrs().remove(column);
    return (M) this;
  }

  /**
   * Remove attrs of this entity.
   *
   * @param columns the column name of the entity
   */
  public M remove(String... columns) {
    if (columns != null)
      for (String c : columns)
        this.getAttrs().remove(c);
    return (M) this;
  }

  /**
   * Remove attrs if it is null.
   */
  public M removeNull() {
    for (java.util.Iterator<Map.Entry<String, Object>> it = getAttrs().entrySet().iterator(); it.hasNext(); ) {
      Map.Entry<String, Object> e = it.next();
      if (e.getValue() == null) {
        it.remove();
      }
    }
    return (M) this;
  }

  /**
   * Keep attrs of this entity and remove other attrs.
   *
   * @param columns the column name of the entity
   */
  public M keep(String... columns) {
    if (columns != null && columns.length > 0) {
      Map<String, Object> newAttrs = new HashMap<String, Object>(columns.length);
      for (String c : columns)
        if (this.getAttrs().containsKey(c))  // prevent put null value to the newAttrs
          newAttrs.put(c, this.getAttrs().get(c));

      this.getAttrs().clear();
      this.getAttrs().putAll(newAttrs);
    } else
      this.getAttrs().clear();
    return (M) this;
  }

  /**
   * Keep column of this entity and remove other attrs.
   *
   * @param column the column name of the entity
   */
  public M keep(String column) {
    if (getAttrs().containsKey(column)) {  // prevent put null value to the newAttrs
      Object keepIt = getAttrs().get(column);
      getAttrs().clear();
      getAttrs().put(column, keepIt);
    } else
      getAttrs().clear();
    return (M) this;
  }

  /**
   * Remove all attrs of this entity.
   */
  public M clear() {
    getAttrs().clear();
    return (M) this;
  }

  public String toString() {
    return Jsoner.toJSONString(getAttrs());
  }

  /**
   * Return json string of this record.
   */
  public String toJson() {
    return Jsoner.toJSONString(getAttrs());
  }

  public boolean equals(Object o) {
    if (!(o instanceof Entity))
      return false;
    return o == this || this.getAttrs().equals(((Entity) o).getAttrs());
  }

  public int hashCode() {
    return getAttrs() == null ? 0 : getAttrs().hashCode();
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

}
