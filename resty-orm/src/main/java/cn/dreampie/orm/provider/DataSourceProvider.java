package cn.dreampie.orm.provider;

import javax.sql.DataSource;

/**
 * Created by ice on 14-12-30.
 */
public interface DataSourceProvider {
  public DataSource getDataSource();

  public String getDsName();

  public boolean isShowSql();

  public void close();
}
