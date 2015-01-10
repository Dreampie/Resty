package cn.dreampie.orm;

import java.io.Serializable;

public class ColumnMeta implements Serializable {

  private final String columnName;
  private final String typeName;
  private final int columnSize;

  public ColumnMeta(String columnName, String typeName, int columnSize) {
    this.columnName = columnName;
    this.typeName = typeName;
    this.columnSize = columnSize;
  }

  /**
   * Column name as reported by DBMS driver.
   *
   * @return column name as reported by DBMS driver.
   */
  public String getColumnName() {
    return columnName;
  }

  /**
   * Column size as reported by DBMS driver.
   *
   * @return column size as reported by DBMS driver.
   */
  public int getColumnSize() {
    return columnSize;
  }

  /**
   * Column type name as reported by DBMS driver.
   *
   * @return column type name as reported by DBMS driver.
   */
  public String getTypeName() {
    return typeName;
  }


  public String toString() {
    return "[ columnName=" + columnName
        + ", typeName=" + typeName
        + ", columnSize=" + columnSize
        + "]";
  }
}
