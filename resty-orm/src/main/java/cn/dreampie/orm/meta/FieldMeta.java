package cn.dreampie.orm.meta;

import java.io.Serializable;
import java.lang.reflect.Field;

public class FieldMeta implements Serializable {

  private final String column;
  private final Field field;

  public FieldMeta(String column, Field field) {
    this.field = field;
    this.column = column;
  }

  /**
   * Column table as reported by DBMS driver.
   *
   * @return column table as reported by DBMS driver.
   */
  public String getColumn() {
    return column;
  }

  public Field getField() {
    return field;
  }
}
