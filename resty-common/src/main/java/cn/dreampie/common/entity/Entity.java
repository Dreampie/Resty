package cn.dreampie.common.entity;

import cn.dreampie.common.entity.exception.EntityException;
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

  private Map<String, Object> attrs = new CaseInsensitiveMap<Object>();

  /**
   * Return attribute Map.
   * Danger! The update method will ignore the attribute if you change it directly.
   * You must use set method to change attribute that update method can handle it.
   */
  public Map<String, Object> getAttrs() {
    return Collections.unmodifiableMap(attrs);
  }

  /**
   * Flag of column has been modified. update need this flag
   */
  private Map<String, Object> modifyAttrs = new CaseInsensitiveMap<Object>();

  /**
   * 获取更新的属性列表
   *
   * @return Map<String, Object>
   */
  public Map<String, Object> getModifyAttrs() {
    return Collections.unmodifiableMap(modifyAttrs);
  }

  /**
   * check method for to json
   *
   * @return boolean
   */
  public boolean checkMethod() {
    return false;
  }

  /**
   * 判断数据库是否拥有该属性
   *
   * @param attr 属性名
   * @return boolean
   */
  public abstract boolean hasAttr(String attr);

  /**
   * Set attribute to entity.
   *
   * @param attr  the attribute name of the entity
   * @param value the value of the attribute
   * @return this entity
   * @throws cn.dreampie.common.entity.exception.EntityException if the attribute is not exists of the entity
   */
  public M set(String attr, Object value) {
    if (hasAttr(attr)) {
      attrs.put(attr, value);
      modifyAttrs.put(attr, value);  // Add modify flag, update() need this flag.
      return (M) this;
    }
    throw new EntityException("The attribute name is not exists: " + attr);
  }

  /**
   * Put key value pair to the entity when the key is not attribute of the entity.
   *
   * @param attr  属性名称
   * @param value 属性值
   * @return 当前entity对象
   */
  public M put(String attr, Object value) {
    if (hasAttr(attr))
      modifyAttrs.put(attr, value);
    attrs.put(attr, value);
    return (M) this;
  }

  /**
   * Set attributes with Map.
   *
   * @param attrs attributes of this entity
   * @return this Model
   */
  public M setAttrs(Map<String, Object> attrs) {
    for (Map.Entry<String, Object> e : attrs.entrySet())
      set(e.getKey(), e.getValue());
    return (M) this;
  }

  /**
   * Set attributes with other entity.
   *
   * @param entity the Model
   * @return this Model
   */
  public M setAttrs(M entity) {
    return setAttrs(entity.attrs);
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
    return putAttrs(entity.attrs);
  }


  /**
   * Get column of any sql type
   */
  public <T> T get(String column) {
    return (T) attrs.get(column);
  }

  /**
   * Parse column to any type
   */
  public <T> T parse(String column, Class<T> clazz) {
    Object value = attrs.get(column);
    if (clazz.isAssignableFrom(value.getClass())) {
      return (T) value;
    } else {
      if (clazz.isAssignableFrom(String.class)) {
        return (T) value.toString();
      } else {
        return Jsoner.parseObject(Jsoner.toJSONString(value), clazz);
      }
    }
  }


  /**
   * Get column of any sql type. Returns defaultValue if null.
   */
  public <T> T get(String column, Object defaultValue) {
    Object result = attrs.get(column);
    return (T) (result != null ? result : defaultValue);
  }


  /**
   * Remove attribute of this entity.
   *
   * @param column the column name of the entity
   */
  public M remove(String column) {
    attrs.remove(column);
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
        this.attrs.remove(c);
    return (M) this;
  }

  /**
   * Remove attrs if it is null.
   */
  public M removeNull() {
    for (java.util.Iterator<Map.Entry<String, Object>> it = attrs.entrySet().iterator(); it.hasNext(); ) {
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
  public M clearAttrs() {
    attrs.clear();
    return (M) this;
  }

  public M clearModifyAttrs() {
    modifyAttrs.clear();
    return (M) this;
  }


  /**
   * Return column name of this record.
   */
  public String[] getAttrNames() {
    Set<String> attrNameSet = attrs.keySet();
    return attrNameSet.toArray(new String[attrNameSet.size()]);
  }

  /**
   * Return column values of this record.
   */
  public Object[] getAttrValues() {
    Collection<Object> attrValueCollection = attrs.values();
    return attrValueCollection.toArray(new Object[attrValueCollection.size()]);
  }


  /**
   * Return attribute name of this entity.
   */
  public String[] getModifyAttrNames() {
    Set<String> attrNameSet = modifyAttrs.keySet();
    return attrNameSet.toArray(new String[attrNameSet.size()]);
  }

  /**
   * Return attribute values of this entity.
   */
  public Object[] getModifyAttrValues() {
    Collection<Object> attrValueCollection = modifyAttrs.values();
    return attrValueCollection.toArray(new Object[attrValueCollection.size()]);
  }


  public String toString() {
    return toJson();
  }

  /**
   * Return json string of this record.
   */
  public String toJson() {
    return Jsoner.toJSONString(attrs);
  }

  /**
   * Get column of sql type: varchar, char, enum, set, text, tinytext, mediumtext, longtext
   */
  public String getStr(String column) {
    return (String) attrs.get(column);
  }

  /**
   * Get column of sql type: int, integer, tinyint(n) n > 1, smallint, mediumint
   */
  public Integer getInt(String column) {
    return (Integer) attrs.get(column);
  }

  /**
   * Get column of sql type: bigint
   */
  public Long getLong(String column) {
    return (Long) attrs.get(column);
  }

  /**
   * Get column of sql type: unsigned bigint
   */
  public BigInteger getBigInteger(String column) {
    return (BigInteger) attrs.get(column);
  }

  /**
   * Get column of sql type: date, year
   */
  public Date getDate(String column) {
    return (Date) attrs.get(column);
  }

  /**
   * Get column of sql type: time
   */
  public Time getTime(String column) {
    return (Time) attrs.get(column);
  }

  /**
   * Get column of sql type: timestamp, datetime
   */
  public Timestamp getTimestamp(String column) {
    return (Timestamp) attrs.get(column);
  }

  /**
   * Get column of sql type: real, double
   */
  public Double getDouble(String column) {
    return (Double) attrs.get(column);
  }

  /**
   * Get column of sql type: float
   */
  public Float getFloat(String column) {
    return (Float) attrs.get(column);
  }

  /**
   * Get column of sql type: bit, tinyint(1)
   */
  public Boolean getBoolean(String column) {
    return (Boolean) attrs.get(column);
  }

  /**
   * Get column of sql type: decimal, numeric
   */
  public BigDecimal getBigDecimal(String column) {
    return (BigDecimal) attrs.get(column);
  }

  /**
   * Get column of sql type: binary, varbinary, tinyblob, blob, mediumblob, longblob
   * I have not finished the test.
   */
  public byte[] getBytes(String column) {
    return (byte[]) attrs.get(column);
  }

  /**
   * Get column of any type that extends from Number
   */
  public Number getNumber(String column) {
    return (Number) attrs.get(column);
  }

}
