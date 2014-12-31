package cn.dreampie.orm;

import cn.dreampie.orm.dialect.Dialect;

import javax.sql.DataSource;

/**
 * Created by ice on 14-12-30.
 */
public interface DataSourceProvider {
  DataSource getDataSource();

  Dialect getDialect();
}
