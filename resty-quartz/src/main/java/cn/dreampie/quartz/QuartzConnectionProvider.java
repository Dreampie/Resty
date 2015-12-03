package cn.dreampie.quartz;

import cn.dreampie.orm.DataSourceMeta;
import cn.dreampie.orm.Metadata;
import org.quartz.utils.ConnectionProvider;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Dreampie
 * @date 2015-05-04
 * @what 为quartz提供数据源
 */
public class QuartzConnectionProvider implements ConnectionProvider {
  private DataSourceMeta dataSourceMeta;

  public Connection getConnection() throws SQLException {
    return dataSourceMeta.getWriteConnection();
  }

  public void shutdown() throws SQLException {
    if (QuartzPlugin.isDsmAlone()) {
      dataSourceMeta.close();
    }
  }

  public void initialize() throws SQLException {
    dataSourceMeta = Metadata.getDataSourceMeta(QuartzPlugin.getDsmName());
  }
}
