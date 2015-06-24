package cn.dreampie.orm.meta;

import java.io.Serializable;

public class ColumnMeta implements Serializable {

  private final String columnName;
  private final String typeName;
  private final int dataType;
  private final int columnSize;

  public ColumnMeta(String columnName, String typeName, int dataType, int columnSize) {
    this.columnName = columnName;
    this.typeName = typeName;
    this.dataType = dataType;
    this.columnSize = columnSize;
  }

  /**
   * Column table as reported by DBMS driver.
   *
   * @return column table as reported by DBMS driver.
   */
  public String getColumnName() {
    return columnName;
  }

  /**
   * Column type
   *
   * @return column type
   */
  public int getDataType() {
    return dataType;
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
   * Column type table as reported by DBMS driver.
   *
   * @return column type table as reported by DBMS driver.
   */
  public String getTypeName() {
    return typeName;
  }


  public String toString() {
    return "ColumnMeta{" +
        "columnName='" + columnName + '\'' +
        ", typeName='" + typeName + '\'' +
        ", dataType=" + dataType +
        ", columnSize=" + columnSize +
        '}';
  }
}
