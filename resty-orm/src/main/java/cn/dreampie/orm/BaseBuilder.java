package cn.dreampie.orm;

import cn.dreampie.common.entity.exception.EntityException;
import cn.dreampie.log.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ice on 14-12-30.
 */
public class BaseBuilder {
  private static final Logger logger = Logger.getLogger(BaseBuilder.class);

  public static <T> List<T> build(ResultSet rs, Class<? extends Base> modelClass, DataSourceMeta dataSourceMeta, TableMeta tableMeta) throws SQLException, InstantiationException, IllegalAccessException {
    List<T> result = new ArrayList<T>();
    ResultSetMetaData rsmd = rs.getMetaData();
    int columnCount = rsmd.getColumnCount();
    String[] labelNames = new String[columnCount + 1];
    int[] types = new int[columnCount + 1];
    buildLabelNamesAndTypes(rsmd, labelNames, types);

    Base entity;
    Object value;

    while (rs.next()) {
      if (Record.class.isAssignableFrom(modelClass)) {
        entity = new Record(tableMeta);
      } else {
        entity = modelClass.newInstance();
      }
      for (int i = 1; i <= columnCount; i++) {

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

        entity.init(labelNames[i], value);
      }
      result.add((T) entity);
    }
    return result;
  }

  private static void buildLabelNamesAndTypes(ResultSetMetaData rsmd, String[] labelNames, int[] types) throws SQLException {
    for (int i = 1; i < labelNames.length; i++) {
      labelNames[i] = rsmd.getColumnLabel(i);
      types[i] = rsmd.getColumnType(i);
    }
  }

  public static byte[] handleBlob(Blob blob) {
    if (blob == null)
      return null;

    InputStream is = null;
    try {
      is = blob.getBinaryStream();
      byte[] data = new byte[(int) blob.length()];    // byte[] data = new byte[is.available()];
      is.read(data);
      return data;
    } catch (SQLException e) {
      throw new EntityException(e.getMessage(), e);
    } catch (IOException e) {
      throw new EntityException(e.getMessage(), e);
    } finally {
      try {
        if (is != null)
          is.close();
      } catch (IOException e) {
        logger.warn(e.getMessage(), e);
      }
    }
  }

  public static String handleClob(Clob clob) {
    if (clob == null)
      return null;

    Reader reader = null;
    try {
      reader = clob.getCharacterStream();
      char[] buffer = new char[(int) clob.length()];
      reader.read(buffer);
      return new String(buffer);
    } catch (SQLException e) {
      throw new EntityException(e.getMessage(), e);
    } catch (IOException e) {
      throw new EntityException(e.getMessage(), e);
    } finally {
      try {
        if (reader != null)
          reader.close();
      } catch (IOException e) {
        logger.warn(e.getMessage(), e);
      }
    }
  }
}
