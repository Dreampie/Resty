/*
Copyright 2009-2014 Igor Polevoy

Licensed under the Apache License, Version 2.0 (the "License"); 
you may not use this file except in compliance with the License. 
You may obtain a copy of the License at 

http://www.apache.org/licenses/LICENSE-2.0 

Unless required by applicable law or agreed to in writing, software 
distributed under the License is distributed on an "AS IS" BASIS, 
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
See the License for the specific language governing permissions and 
limitations under the License. 
*/


package cn.dreampie.orm;

import java.io.Serializable;

public class ColumnMetadata implements Serializable {

    private final String columnName;
    private final String typeName;
    private final int columnSize;

    public ColumnMetadata(String columnName, String  typeName, int columnSize) {
        this.columnName = columnName;
        this.typeName = typeName;
        this.columnSize = columnSize;
    }

    /**
     * Column name as reported by DBMS driver.
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

    @Override
    public String toString() {
        return "[ columnName=" + columnName
                + ", typeName=" + typeName
                + ", columnSize=" + columnSize
                + "]";
    }
}
