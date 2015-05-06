package cn.dreampie.orm.provider;

import cn.dreampie.orm.dialect.Dialect;

import javax.sql.DataSource;

/**
 * Created by ice on 14-12-30.
 */
public interface DataSourceProvider {
  public DataSource getDataSource();

  public Dialect getDialect();

  public String getDsName();

  public boolean isShowSql();

  public void close();
}
