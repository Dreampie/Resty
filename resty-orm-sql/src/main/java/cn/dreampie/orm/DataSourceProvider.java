package cn.dreampie.orm;

import cn.dreampie.orm.annotation.Table;

import javax.sql.DataSource;

/**
 * Created by ice on 14-12-30.
 */
@Table(name = "",primaryKey = "",cached = true)
public interface DataSourceProvider {
  DataSource getDataSource();
}
