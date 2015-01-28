package cn.dreampie.orm.jndi;

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

  public JndiDataSourceProvider(String name) {
    this(name, null);
  }

  public JndiDataSourceProvider(String name, String dialect) {
    Context ctx;
    try {
      ctx = new InitialContext();
      ds = (DataSource) ctx.lookup(name);
      if (ds == null) {
        throw new DBException("Jndi could not found error for " + name);
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
}
