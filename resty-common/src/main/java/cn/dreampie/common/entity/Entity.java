package cn.dreampie.common.entity;

import cn.dreampie.common.entity.exception.EntityException;
import cn.dreampie.common.util.Maper;
import cn.dreampie.common.util.json.Jsoner;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ice on 14-12-31.
 */
public abstract class Entity<M extends Entity> {

  private Map<String, Object> attrs = new CaseInsensitiveMap<Object>();
  /**
   * Flag of attr has been modified. update need this flag
   */
  private Map<String, Object> modifyAttrs = new CaseInsensitiveMap<Object>();

  /**
   * getMessage Conversion to convert attr
   *
   * @param attr
   * @return
   */
  public abstract Conversion getConversion(String attr);

  /**
   * Return attribute Map.
   * Danger! The update method will ignore the attribute if you change it directly.
   * You must use set method to change attribute that update method can handle it.
   */
  public Map<String, Object> getAttrs() {
    return attrs;
  }

  /**
   * Set attributes with other entity.
   *
   * @param entity the Model
   * @return this Model
   */
  public M setAttrs(M entity) {
    return (M) setAttrs(entity.getAttrs());
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
   * 获取更新的属性列表
   *
   * @return Map<String, Object>
   */
  public Map<String, Object> getModifyAttrs() {
    return modifyAttrs;
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
   * 判断数据库是否拥有该列
   *
   * @param attr 属性名
   * @return boolean
   */
  public abstract boolean hasColumn(String attr);

  /**
   * 获取改数据库列对应的java类型
   *
   * @param attr 属性名
   * @return class
   */
  public abstract Class getColumnType(String attr);

  /**
   * Set attribute to entity.
   *
   * @param attr  the attribute name of the entity
   * @param value the value of the attribute
   * @return this entity
   * @throws cn.dreampie.common.entity.exception.EntityException if the attribute is not exists of the entity
   */
  public M set(String attr, Object value) {
    if (hasColumn(attr)) {
      attrs.put(attr, value);
      modifyAttrs.put(attr, value);  // Add modify flag, update() need this flag.
      return (M) this;
    }
    throw new EntityException("The attribute name is not exists: " + attr);
  }

  /**
   * 初始化属性 不会添加到modify
   *
   * @param attr
   * @param value
   * @return
   */
  public M init(String attr, Object value) {
    attrs.put(attr, value);
    return (M) this;
  }

  public M initAttrs(Map<String, Object> attrs) {
    for (Map.Entry<String, Object> e : attrs.entrySet()) {
      init(e.getKey(), e.getValue());
    }
    return (M) this;
  }

  public M initAttrs(M entity) {
    return (M) initAttrs(entity.getAttrs());
  }

  /**
   * Put key value pair to the entity when the key is not attribute of the entity.
   *
   * @param attr  属性名称
   * @param value 属性值
   * @return 当前entity对象
   */
  public M put(String attr, Object value) {
    if (hasColumn(attr)) {
      modifyAttrs.put(attr, value);
    }
    attrs.put(attr, value);
    return (M) this;
  }

  public M putAttrs(Map<String, Object> attrs) {
    for (Map.Entry<String, Object> e : attrs.entrySet()) {
      put(e.getKey(), e.getValue());
    }
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

  /**
   * Get attr of any sql type
   */
  public <T> T get(String attr) {
    return (T) attrs.get(attr);
  }

  /**
   * Parse attr to any type
   */
  public <T> T get(String attr, Class<T> clazz) {
    if (attrs.containsKey(attr)) {
      Object value = attrs.get(attr);
      T result = null;

      if (value != null) {
        if (clazz.isAssignableFrom(value.getClass())) {
          result = (T) value;
        } else {
          result = Jsoner.toObject(Jsoner.toJSON(value), clazz);
        }
      }
      return result;
    } else {
      throw new EntityException("Attribute '" + attr + "' could not found.");
    }
  }

  /**
   * Get attr of any sql type. Returns defaultValue if null.
   */
  public <T> T get(String attr, Object defaultValue) {
    Object result = get(attr);
    return (T) (result != null ? result : defaultValue);
  }

  /**
   * Get attr for clazz. Returns defaultValue if null.
   */
  public <T> T get(String attr, Class<T> clazz, Object defaultValue) {
    Object result = get(attr, clazz);
    return (T) (result != null ? result : defaultValue);
  }


  /**
   * Remove attribute of this entity.
   *
   * @param attr the attr name of the entity
   */
  public M remove(String attr) {
    this.attrs.remove(attr);
    this.modifyAttrs.remove(attr);
    return (M) this;
  }

  /**
   * Remove attrs of this entity.
   *
   * @param attrs the attr name of the entity
   */
  public M remove(String... attrs) {
    if (attrs != null)
      for (String attr : attrs) {
        remove(attr);
      }
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
   * @param attrs the attr name of the entity
   */
  public M keep(String... attrs) {
    if (attrs != null && attrs.length > 0) {
      Map<String, Object> newAttrs = new HashMap<String, Object>(attrs.length);
      Map<String, Object> newModifyAttrs = new HashMap<String, Object>();
      for (String c : attrs) {
        if (this.attrs.containsKey(c)) { // prevent put null value to the newAttrs
          newAttrs.put(c, this.attrs.get(c));
          if (this.modifyAttrs.containsKey(c)) {
            newModifyAttrs.put(c, this.attrs.get(c));
          }
        }
      }
      this.attrs.clear();
      this.attrs.putAll(newAttrs);
      this.modifyAttrs.clear();
      this.modifyAttrs.putAll(newModifyAttrs);
    } else {
      this.attrs.clear();
      this.modifyAttrs.clear();
    }
    return (M) this;
  }

  /**
   * Keep attr of this entity and remove other attrs.
   *
   * @param attr the attr name of the entity
   */
  public M keep(String attr) {
    if (this.attrs.containsKey(attr)) {  // prevent put null value to the newAttrs

      Object keepIt = this.attrs.get(attr);
      this.attrs.clear();
      this.attrs.put(attr, keepIt);
      if (this.modifyAttrs.containsKey(attr)) {
        this.modifyAttrs.clear();
        this.modifyAttrs.put(attr, keepIt);
      }
    } else {
      this.attrs.clear();
      this.modifyAttrs.clear();
    }
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

  public M reSetAttrs(Map<String, Object> attrs) {
    this.attrs = attrs;
    return (M) this;
  }

  public M reSetModifyAttrs(Map<String, Object> modifyAttrs) {
    this.modifyAttrs = modifyAttrs;
    return (M) this;
  }

  /**
   * Return attr name of this record.
   */
  public String[] getAttrNames() {
    Set<String> attrNameSet = attrs.keySet();
    return attrNameSet.toArray(new String[attrNameSet.size()]);
  }

  /**
   * Return attr values of this record.
   */
  public Object[] getAttrValues() {
    Collection<Object> attrValueCollection = attrs.values();
    return attrValueCollection.toArray(new Object[attrValueCollection.size()]);
  }

  public String[] getModifyAttrNames() {
    return getModifyAttrNames(null);
  }

  /**
   * Return attribute name of this entity.
   */
  public String[] getModifyAttrNames(String generatedKey) {
    Set<String> attrNameSet = modifyAttrs.keySet();
    if (generatedKey != null && !generatedKey.isEmpty()) {
      attrNameSet.remove(generatedKey);
    }
    return attrNameSet.toArray(new String[attrNameSet.size()]);
  }

  /**
   * Return attribute values of this entity.
   */
  public Object[] getModifyAttrValues() {
    return getModifyAttrValues(null);
  }

  public Object[] getModifyAttrValues(String generatedKey) {
    Map<String, Object> attrValueMap = Maper.copyOf(new CaseInsensitiveMap<Object>(), modifyAttrs);
    if (generatedKey != null && !generatedKey.isEmpty()) {
      attrValueMap.remove(generatedKey);
    }

    Conversion conversion;
    for (Map.Entry<String, Object> attrValueEntry : attrValueMap.entrySet()) {
      conversion = getConversion(attrValueEntry.getKey());
      if (conversion != null) {
        attrValueEntry.setValue(conversion.write(attrValueEntry.getValue()));
      }
    }

    Collection<Object> attrValueCollection = attrValueMap.values();
    return attrValueCollection.toArray(new Object[attrValueCollection.size()]);
  }

  public String toString() {
    return toJson();
  }

  /**
   * Return json string of this record.
   */
  public String toJson() {
    return Jsoner.toJSON(attrs);
  }

}
