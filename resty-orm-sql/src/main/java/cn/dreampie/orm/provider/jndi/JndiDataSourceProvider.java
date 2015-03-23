package cn.dreampie.orm.provider.jndi;

import cn.dreampie.orm.DataSourceProvider;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.dialect.DialectFactory;
import cn.dreampie.orm.exception.DBException;

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

  public JndiDataSourceProvider(String jndiName) {
    this("default", jndiName, null);
  }

  public JndiDataSourceProvider(String dsName, String jndiName, String dialect) {
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
}
