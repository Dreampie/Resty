package cn.dreampie.orm.provider.c3p0;

import cn.dreampie.common.util.properties.Prop;
import cn.dreampie.common.util.properties.Proper;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.dialect.DialectFactory;
import cn.dreampie.orm.exception.DBException;
import cn.dreampie.orm.provider.DataSourceProvider;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.impl.C3P0Defaults;

import javax.sql.DataSource;
import java.beans.PropertyVetoException;

import static cn.dreampie.common.util.Checker.checkNotNull;


/**
 * Created by ice on 14-12-30.
 */
public class C3p0DataSourceProvider implements DataSourceProvider {

  private String dsName;
  // 基本属性 url、user、password
  private String url;
  private String user;
  private String password;
  private String driverClass;  // 由 "com.mysql.jdbc.Driver" 改为 null 让 druid 自动探测 driverClass 值
  private boolean showSql = false;

  // 初始连接池大小、最小空闲连接数、最大活跃连接数
  private int maxStatements = 0;
  private int maxStatementsPerConnection = 0;
  private int initialPoolSize = 3;
  private int minPoolSize = 3;
  private int maxPoolSize = 15;
  private int idleConnectionTestPeriod = 0;
  private int maxIdleTime = 0;
  private int propertyCycle = 0;
  private int acquireIncrement = 3;
  private int acquireRetryAttempts = 30;
  private int acquireRetryDelay = 1000;
  private int checkoutTimeout = 0;
  private int maxAdministrativeTaskTime = 0;
  private int maxIdleTimeExcessConnections = 0;
  private int maxConnectionAge = 0;
  private int unreturnedConnectionTimeout = 0;
  private boolean breakAfterAcquireFailure = false;
  private boolean testConnectionOnCheckout = false;
  private boolean testConnectionOnCheckin = false;
  private boolean autoCommitOnClose = false;
  private String automaticTestTable = null;

  private String preferredTestQuery = "select 1";
  private ComboPooledDataSource ds;
  private Dialect dialect;

  public C3p0DataSourceProvider() {
    this("default");
  }

  public C3p0DataSourceProvider(String dsName) {
    this.dsName = dsName;
    Prop prop = Proper.use("application.properties");
    this.url = prop.get("db." + dsName + ".url");
    checkNotNull(this.url, "Could not found database url for " + "db." + dsName + ".url");
    this.user = prop.get("db." + dsName + ".user");
    checkNotNull(this.user, "Could not found database user for " + "db." + dsName + ".user");
    this.password = prop.get("db." + dsName + ".password");
    checkNotNull(this.password, "Could not found database password for " + "db." + dsName + ".password");
    this.dialect = DialectFactory.get(prop.get("db." + dsName + ".dialect", "mysql"));
    this.driverClass = prop.get("db." + dsName + ".driver", dialect.driverClass());
    this.showSql = prop.getBoolean("db." + dsName + ".showSql", false);

    this.maxStatements = prop.getInt("c3p0." + dsName + ".maxStatements", C3P0Defaults.maxStatements());
    this.maxStatementsPerConnection = prop.getInt("c3p0." + dsName + ".maxStatementsPerConnection", C3P0Defaults.maxStatementsPerConnection());
    this.initialPoolSize = prop.getInt("c3p0." + dsName + ".initialPoolSize", C3P0Defaults.initialPoolSize());
    this.minPoolSize = prop.getInt("c3p0." + dsName + ".minPoolSize", C3P0Defaults.minPoolSize());
    this.maxPoolSize = prop.getInt("c3p0." + dsName + ".maxPoolSize", C3P0Defaults.maxPoolSize());
    this.idleConnectionTestPeriod = prop.getInt("c3p0." + dsName + ".idleConnectionTestPeriod", C3P0Defaults.idleConnectionTestPeriod());
    this.maxIdleTime = prop.getInt("c3p0." + dsName + ".minEvictableIdleTimeMillis", C3P0Defaults.maxIdleTime());
    this.propertyCycle = prop.getInt("c3p0." + dsName + ".propertyCycle", C3P0Defaults.propertyCycle());
    this.acquireIncrement = prop.getInt("c3p0." + dsName + ".acquireIncrement", C3P0Defaults.acquireIncrement());
    this.acquireRetryAttempts = prop.getInt("c3p0." + dsName + ".acquireRetryAttempts", C3P0Defaults.acquireRetryAttempts());
    this.acquireRetryDelay = prop.getInt("c3p0." + dsName + ".acquireRetryDelay", C3P0Defaults.acquireRetryDelay());
    this.checkoutTimeout = prop.getInt("c3p0." + dsName + ".checkoutTimeout", C3P0Defaults.checkoutTimeout());
    this.maxAdministrativeTaskTime = prop.getInt("c3p0." + dsName + ".maxAdministrativeTaskTime", C3P0Defaults.maxAdministrativeTaskTime());
    this.maxIdleTimeExcessConnections = prop.getInt("c3p0." + dsName + ".maxIdleTimeExcessConnections", C3P0Defaults.maxIdleTimeExcessConnections());
    this.maxConnectionAge = prop.getInt("c3p0." + dsName + ".maxConnectionAge", C3P0Defaults.maxConnectionAge());
    this.unreturnedConnectionTimeout = prop.getInt("c3p0." + dsName + ".unreturnedConnectionTimeout", C3P0Defaults.unreturnedConnectionTimeout());
    this.breakAfterAcquireFailure = prop.getBoolean("c3p0." + dsName + ".breakAfterAcquireFailure", C3P0Defaults.breakAfterAcquireFailure());
    this.testConnectionOnCheckout = prop.getBoolean("c3p0." + dsName + ".testConnectionOnCheckout", C3P0Defaults.testConnectionOnCheckout());
    this.testConnectionOnCheckin = prop.getBoolean("c3p0." + dsName + ".breakAfterAcquireFailure", C3P0Defaults.testConnectionOnCheckin());
    this.autoCommitOnClose = prop.getBoolean("c3p0." + dsName + ".breakAfterAcquireFailure", C3P0Defaults.autoCommitOnClose());
    this.automaticTestTable = prop.get("c3p0." + dsName + ".testOnBorrow", C3P0Defaults.automaticTestTable());

    this.preferredTestQuery = prop.get("c3p0." + dsName + ".preferredTestQuery", this.dialect.validQuery());
    buidDataSource();
  }

  public C3p0DataSourceProvider(String url, String user, String password) {
    this(url, user, password, null);
  }

  public C3p0DataSourceProvider(String url, String user, String password, boolean showSql) {
    this(url, user, password, null, showSql);
  }

  public C3p0DataSourceProvider(String url, String user, String password, String dbType) {
    this(url, user, password, dbType, null);
  }

  public C3p0DataSourceProvider(String url, String user, String password, String dbType, boolean showSql) {
    this(url, user, password, dbType, null, showSql);
  }

  public C3p0DataSourceProvider(String url, String user, String password, String dbType, String driverClass) {
    this(url, user, password, dbType, driverClass, false);
  }

  public C3p0DataSourceProvider(String url, String user, String password, String dbType, String driverClass, boolean showSql) {
    this.url = url;
    checkNotNull(this.url, "Could not found database url for custom.");
    this.user = user;
    checkNotNull(this.user, "Could not found database user for custom.");
    this.password = password;
    checkNotNull(this.password, "Could not found database password for custom.");
    this.dialect = DialectFactory.get(dbType);
    this.driverClass = driverClass == null ? dialect.driverClass() : driverClass;
    this.showSql = showSql;
    buidDataSource();
  }

  private void buidDataSource() {
    //init druid
    ds = new ComboPooledDataSource();
    ds.setJdbcUrl(url);
    ds.setUser(user);
    ds.setPassword(password);

    try {
      ds.setDriverClass(driverClass);
    } catch (PropertyVetoException e) {
      ds = null;
      throw new DBException("ComboPooledDataSource set driverClass error.");
    }
    ds.setMaxStatements(maxStatements);
    ds.setMaxStatementsPerConnection(maxStatementsPerConnection);
    ds.setInitialPoolSize(initialPoolSize);
    ds.setMinPoolSize(minPoolSize);
    ds.setMaxPoolSize(maxPoolSize);
    ds.setIdleConnectionTestPeriod(idleConnectionTestPeriod);
    ds.setMaxIdleTime(maxIdleTime);
    ds.setPropertyCycle(propertyCycle);
    ds.setAcquireIncrement(acquireIncrement);
    ds.setAcquireRetryAttempts(acquireRetryAttempts);
    ds.setAcquireRetryDelay(acquireRetryDelay);
    ds.setCheckoutTimeout(checkoutTimeout);
    ds.setMaxAdministrativeTaskTime(maxAdministrativeTaskTime);
    ds.setMaxIdleTimeExcessConnections(maxIdleTimeExcessConnections);
    ds.setMaxConnectionAge(maxConnectionAge);
    ds.setUnreturnedConnectionTimeout(unreturnedConnectionTimeout);
    ds.setBreakAfterAcquireFailure(breakAfterAcquireFailure);
    ds.setTestConnectionOnCheckout(testConnectionOnCheckout);
    ds.setTestConnectionOnCheckin(testConnectionOnCheckin);
    ds.setAutoCommitOnClose(autoCommitOnClose);
    ds.setAutomaticTestTable(automaticTestTable);
    ds.setPreferredTestQuery(preferredTestQuery);
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

  public C3p0DataSourceProvider setShowSql(boolean showSql) {
    this.showSql = showSql;
    return this;
  }

  public void close() {
    ds.close();
  }

  public C3p0DataSourceProvider setDriverClass(String driverClass) {
    this.driverClass = driverClass;
    try {
      ds.setDriverClass(driverClass);
    } catch (PropertyVetoException e) {
      ds = null;
      throw new DBException("ComboPooledDataSource set driverClass error.");
    }
    return this;
  }

  public C3p0DataSourceProvider setMaxStatements(int maxStatements) {
    this.maxStatements = maxStatements;
    ds.setMaxStatements(maxStatements);
    return this;
  }

  public C3p0DataSourceProvider setMaxStatementsPerConnection(int maxStatementsPerConnection) {
    this.maxStatementsPerConnection = maxStatementsPerConnection;
    ds.setMaxStatementsPerConnection(maxStatementsPerConnection);
    return this;
  }

  public C3p0DataSourceProvider setInitialPoolSize(int initialPoolSize) {
    this.initialPoolSize = initialPoolSize;
    ds.setInitialPoolSize(initialPoolSize);
    return this;
  }

  public C3p0DataSourceProvider setMinPoolSize(int minPoolSize) {
    this.minPoolSize = minPoolSize;
    ds.setMinPoolSize(minPoolSize);
    return this;
  }

  public C3p0DataSourceProvider setMaxPoolSize(int maxPoolSize) {
    this.maxPoolSize = maxPoolSize;
    ds.setMaxPoolSize(maxPoolSize);
    return this;
  }

  public C3p0DataSourceProvider setIdleConnectionTestPeriod(int idleConnectionTestPeriod) {
    this.idleConnectionTestPeriod = idleConnectionTestPeriod;
    ds.setIdleConnectionTestPeriod(idleConnectionTestPeriod);
    return this;
  }

  public C3p0DataSourceProvider setMaxIdleTime(int maxIdleTime) {
    this.maxIdleTime = maxIdleTime;
    ds.setMaxIdleTime(maxIdleTime);
    return this;
  }

  public C3p0DataSourceProvider setPropertyCycle(int propertyCycle) {
    this.propertyCycle = propertyCycle;
    ds.setPropertyCycle(propertyCycle);
    return this;
  }

  public C3p0DataSourceProvider setAcquireIncrement(int acquireIncrement) {
    this.acquireIncrement = acquireIncrement;
    ds.setAcquireIncrement(acquireIncrement);
    return this;
  }

  public C3p0DataSourceProvider setAcquireRetryAttempts(int acquireRetryAttempts) {
    this.acquireRetryAttempts = acquireRetryAttempts;
    ds.setAcquireRetryAttempts(acquireRetryAttempts);
    return this;
  }

  public C3p0DataSourceProvider setAcquireRetryDelay(int acquireRetryDelay) {
    this.acquireRetryDelay = acquireRetryDelay;
    ds.setAcquireRetryDelay(acquireRetryDelay);
    return this;
  }

  public C3p0DataSourceProvider setCheckoutTimeout(int checkoutTimeout) {
    this.checkoutTimeout = checkoutTimeout;
    ds.setCheckoutTimeout(checkoutTimeout);
    return this;
  }

  public C3p0DataSourceProvider setMaxAdministrativeTaskTime(int maxAdministrativeTaskTime) {
    this.maxAdministrativeTaskTime = maxAdministrativeTaskTime;
    ds.setMaxAdministrativeTaskTime(maxAdministrativeTaskTime);
    return this;
  }

  public C3p0DataSourceProvider setMaxIdleTimeExcessConnections(int maxIdleTimeExcessConnections) {
    this.maxIdleTimeExcessConnections = maxIdleTimeExcessConnections;
    ds.setMaxIdleTimeExcessConnections(maxIdleTimeExcessConnections);
    return this;
  }

  public C3p0DataSourceProvider setMaxConnectionAge(int maxConnectionAge) {
    this.maxConnectionAge = maxConnectionAge;
    ds.setMaxConnectionAge(maxConnectionAge);
    return this;
  }

  public C3p0DataSourceProvider setUnreturnedConnectionTimeout(int unreturnedConnectionTimeout) {
    this.unreturnedConnectionTimeout = unreturnedConnectionTimeout;
    ds.setUnreturnedConnectionTimeout(unreturnedConnectionTimeout);
    return this;
  }

  public C3p0DataSourceProvider setBreakAfterAcquireFailure(boolean breakAfterAcquireFailure) {
    this.breakAfterAcquireFailure = breakAfterAcquireFailure;
    ds.setBreakAfterAcquireFailure(breakAfterAcquireFailure);
    return this;
  }

  public C3p0DataSourceProvider setTestConnectionOnCheckout(boolean testConnectionOnCheckout) {
    this.testConnectionOnCheckout = testConnectionOnCheckout;
    ds.setTestConnectionOnCheckout(testConnectionOnCheckout);
    return this;
  }

  public C3p0DataSourceProvider setTestConnectionOnCheckin(boolean testConnectionOnCheckin) {
    this.testConnectionOnCheckin = testConnectionOnCheckin;
    ds.setTestConnectionOnCheckin(testConnectionOnCheckin);
    return this;
  }

  public C3p0DataSourceProvider setAutoCommitOnClose(boolean autoCommitOnClose) {
    this.autoCommitOnClose = autoCommitOnClose;
    ds.setAutoCommitOnClose(autoCommitOnClose);
    return this;
  }

  public C3p0DataSourceProvider setAutomaticTestTable(String automaticTestTable) {
    this.automaticTestTable = automaticTestTable;
    ds.setAutomaticTestTable(automaticTestTable);
    return this;
  }
}
