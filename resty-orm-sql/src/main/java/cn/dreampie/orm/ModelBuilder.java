package cn.dreampie.orm;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ice on 14-12-30.
 */
public class ModelBuilder {

  public static final <T> List<T> build(ResultSet rs, Class<? extends Model> modelClass) throws SQLException, InstantiationException, IllegalAccessException {
    List<T> result = new ArrayList<T>();
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    String[] labelNames = new String[columnCount + 1];
    int[] types = new int[columnCount + 1];
    buildLabelNamesAndTypes(rsmd, labelNames, types);
    while (rs.next()) {
      Model ar = modelClass.newInstance();
      Map<String, Object> attrs = ar.getAttrs();
      for (int i = 1; i <= columnCount; i++) {
        Object value;
        if (types[i] < Types.BLOB)
          value = rs.getObject(i);
        else if (types[i] == Types.CLOB)
          value = handleClob(rs.getClob(i));
        else if (types[i] == Types.NCLOB)
          value = handleClob(rs.getNClob(i));
        else if (types[i] == Types.BLOB)
          value = handleBlob(rs.getBlob(i));
        else
          value = rs.getObject(i);

        attrs.put(labelNames[i], value);
      }
      result.add((T) ar);
    }
    return result;
  }

  private static final void buildLabelNamesAndTypes(ResultSetMetaData rsmd, String[] labelNames, int[] types) throws SQLException {
    for (int i = 1; i < labelNames.length; i++) {
      labelNames[i] = rsmd.getColumnLabel(i);
      types[i] = rsmd.getColumnType(i);
    }
  }

  public static byte[] handleBlob(Blob blob) throws SQLException {
    if (blob == null)
      return null;

    InputStream is = null;
    try {
      is = blob.getBinaryStream();
      byte[] data = new byte[(int) blob.length()];    // byte[] data = new byte[is.available()];
      is.read(data);
      is.close();
      return data;
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        is.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public static String handleClob(Clob clob) throws SQLException {
    if (clob == null)
      return null;

    Reader reader = null;
    try {
      reader = clob.getCharacterStream();
      char[] buffer = new char[(int) clob.length()];
      reader.read(buffer);
      return new String(buffer);
    } catch (IOException e) {
      throw new RuntimeException(e);
    } finally {
      try {
        reader.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
