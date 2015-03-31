package cn.dreampie.common.entity;

import cn.dreampie.common.util.json.Jsoner;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.*;

/**
 * Created by ice on 14-12-31.
 */
public abstract class Entity<M extends Entity> {

  /**
   * Attributes of this model
   */
  public abstract Map<String, Object> getAttrs();


  /**
   * 获取更新的属性列表
   *
   * @return Map<String, Object>
   */
  public abstract Map<String, Object> getModifyAttrs();

  /**
   * Set attribute to model.
   *
   * @param attr  the attribute name of the model
   * @param value the value of the attribute
   * @return this model
   */
  public abstract M set(String attr, Object value);

  /**
   * Put key value pair to the model when the key is not attribute of the model.
   *
   * @param attr  属性名称
   * @param value 属性值
   * @return 当前model对象
   */
  public abstract M put(String attr, Object value);

  /**
   * check method for to json
   *
   * @return boolean
   */
  public boolean checkMethod() {
    return false;
  }

  /**
   * Set attributes with Map.
   *
   * @param attrs attributes of this model
   * @return this Model
   */
  public M setAttrs(Map<String, Object> attrs) {
    for (Map.Entry<String, Object> e : attrs.entrySet())
      set(e.getKey(), e.getValue());
    return (M) this;
  }

  /**
   * Set attributes with other model.
   *
   * @param model the Model
   * @return this Model
   */
  public M setAttrs(M model) {
    return setAttrs(model.getAttrs());
  }

  public M putAttrs(Map<String, Object> attrs) {
    for (Map.Entry<String, Object> e : attrs.entrySet())
      put(e.getKey(), e.getValue());
    return (M) this;
  }

  /**
   * Set attrs value with entity.
   *
   * @param entity the entity
   */
  public M putAttrs(M entity) {
    return putAttrs(entity.getAttrs());
  }


  /**
   * Get column of any sql type
   */
  public <T> T get(String column) {
    return (T) getAttrs().get(column);
  }

  /**
   * Parse column to any type
   */
  public <T> T parse(String column, Class<T> clazz) {
    Object value = getAttrs().get(column);
    if (clazz.isAssignableFrom(value.getClass())) {
      return (T) value;
    } else {
      return Jsoner.parseObject(Jsoner.toJSONString(value), clazz);
    }
  }


  /**
   * Get column of any sql type. Returns defaultValue if null.
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
    Map<String, Object> attrs = getAttrs();
    if (columns != null && columns.length > 0) {
      Map<String, Object> newAttrs = new HashMap<String, Object>(columns.length);
      for (String c : columns)
        if (attrs.containsKey(c))  // prevent put null value to the newAttrs
          newAttrs.put(c, attrs.get(c));

      attrs.clear();
      attrs.putAll(newAttrs);
    } else
      attrs.clear();
    return (M) this;
  }

  /**
   * Keep column of this entity and remove other attrs.
   *
   * @param column the column name of the entity
   */
  public M keep(String column) {
    Map<String, Object> attrs = getAttrs();
    if (attrs.containsKey(column)) {  // prevent put null value to the newAttrs
      Object keepIt = attrs.get(column);
      attrs.clear();
      attrs.put(column, keepIt);
    } else
      attrs.clear();
    return (M) this;
  }

  /**
   * Remove all attrs of this entity.
   */
  public M clear() {
    getAttrs().clear();
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
    Collection<Object> attrValueCollection = getAttrs().values();
    return attrValueCollection.toArray(new Object[attrValueCollection.size()]);
  }


  /**
   * Return attribute name of this model.
   */
  public String[] getModifyAttrNames() {
    Set<String> attrNameSet = getModifyAttrs().keySet();
    return attrNameSet.toArray(new String[attrNameSet.size()]);
  }

  /**
   * Return attribute values of this model.
   */
  public Object[] getModifyAttrValues() {
    Collection<Object> attrValueCollection = getModifyAttrs().values();
    return attrValueCollection.toArray(new Object[attrValueCollection.size()]);
  }


  public String toString() {
    return toJson();
  }

  /**
   * Return json string of this record.
   */
  public String toJson() {
    return Jsoner.toJSONString(getAttrs());
  }

  /**
   * Get column of sql type: varchar, char, enum, set, text, tinytext, mediumtext, longtext
   */
  public String getStr(String column) {
    return (String) getAttrs().get(column);
  }

  /**
   * Get column of sql type: int, integer, tinyint(n) n > 1, smallint, mediumint
   */
  public Integer getInt(String column) {
    return (Integer) getAttrs().get(column);
  }

  /**
   * Get column of sql type: bigint
   */
  public Long getLong(String column) {
    return (Long) getAttrs().get(column);
  }

  /**
   * Get column of sql type: unsigned bigint
   */
  public BigInteger getBigInteger(String column) {
    return (BigInteger) getAttrs().get(column);
  }

  /**
   * Get column of sql type: date, year
   */
  public Date getDate(String column) {
    return (Date) getAttrs().get(column);
  }

  /**
   * Get column of sql type: time
   */
  public Time getTime(String column) {
    return (Time) getAttrs().get(column);
  }

  /**
   * Get column of sql type: timestamp, datetime
   */
  public Timestamp getTimestamp(String column) {
    return (Timestamp) getAttrs().get(column);
  }

  /**
   * Get column of sql type: real, double
   */
  public Double getDouble(String column) {
    return (Double) getAttrs().get(column);
  }

  /**
   * Get column of sql type: float
   */
  public Float getFloat(String column) {
    return (Float) getAttrs().get(column);
  }

  /**
   * Get column of sql type: bit, tinyint(1)
   */
  public Boolean getBoolean(String column) {
    return (Boolean) getAttrs().get(column);
  }

  /**
   * Get column of sql type: decimal, numeric
   */
  public BigDecimal getBigDecimal(String column) {
    return (BigDecimal) getAttrs().get(column);
  }

  /**
   * Get column of sql type: binary, varbinary, tinyblob, blob, mediumblob, longblob
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
