package cn.dreampie.orm.provider.jndi;

import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.dialect.DialectFactory;
import cn.dreampie.orm.exception.DBException;
import cn.dreampie.orm.provider.DataSourceProvider;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Created by ice on 15-1-16.
 */
public class JndiDataSourceProvider implements DataSourceProvider {

  private DataSource ds;
  private Dialect dialect;
  private String dsName;
  private boolean showSql;

  public JndiDataSourceProvider(String jndiName) {
    this(jndiName, false);
  }

  public JndiDataSourceProvider(String jndiName, boolean showSql) {
    this("default", jndiName, showSql);
  }

  public JndiDataSourceProvider(String dsName, String jndiName) {
    this(dsName, jndiName, false);
  }

  public JndiDataSourceProvider(String dsName, String jndiName, boolean showSql) {
    this(dsName, jndiName, null, showSql);
  }

  public JndiDataSourceProvider(String dsName, String jndiName, String dialect) {
    this(dsName, jndiName, dialect, false);
  }

  public JndiDataSourceProvider(String dsName, String jndiName, String dialect, boolean showSql) {
    this.dsName = dsName;
    Context ctx;
    try {
      ctx = new InitialContext();
      ds = (DataSource) ctx.lookup(jndiName);
      if (ds == null) {
        throw new DBException("Jndi could not found error for " + jndiName);
      }
    } catch (NamingException e) {
      throw new DBException(e.getMessage(), e);
    }
    this.dialect = DialectFactory.get(dialect == null ? "mysql" : dialect);
    this.showSql = showSql;
  }

  public DataSource getDataSource() {
    return ds;
  }

  public Dialect getDialect() {
    return dialect;
  }

  public String getDsName() {
    return dsName;
  }

  public boolean isShowSql() {
    return showSql;
  }

  public JndiDataSourceProvider setShowSql(boolean showSql) {
    this.showSql = showSql;
    return this;
  }

  public void close() {
    //do nothing  becouse  it have no close method
  }
}
