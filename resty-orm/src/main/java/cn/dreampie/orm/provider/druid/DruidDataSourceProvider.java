package cn.dreampie.orm.provider.druid;

import cn.dreampie.common.util.properties.Prop;
import cn.dreampie.common.util.properties.Proper;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.dialect.DialectFactory;
import cn.dreampie.orm.provider.DataSourceProvider;
import com.alibaba.druid.DruidRuntimeException;
import com.alibaba.druid.filter.Filter;
import com.alibaba.druid.pool.DruidDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.List;

import static cn.dreampie.common.util.Checker.checkNotNull;


/**
 * Created by ice on 14-12-30.
 */
public class DruidDataSourceProvider implements DataSourceProvider {

  private String dsName;
  // 基本属性 url、user、password
  private String url;
  private String user;
  private String password;
  private String driverClass;  // 由 "com.mysql.jdbc.Driver" 改为 null 让 druid 自动探测 driverClass 值

  // 初始连接池大小、最小空闲连接数、最大活跃连接数
  private int initialSize = 10;
  private int minIdle = 10;
  private int maxActive = 100;

  // 配置获取连接等待超时的时间
  private long maxWait = DruidDataSource.DEFAULT_MAX_WAIT;

  // 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
  private long timeBetweenEvictionRunsMillis = DruidDataSource.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS;
  // 配置连接在池中最小生存的时间
  private long minEvictableIdleTimeMillis = DruidDataSource.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS;
  // 配置发生错误时多久重连
  private long timeBetweenConnectErrorMillis = DruidDataSource.DEFAULT_TIME_BETWEEN_CONNECT_ERROR_MILLIS;

  private boolean testWhileIdle = true;
  private boolean testOnBorrow = false;
  private boolean testOnReturn = false;
  // 是否打开连接泄露自动检测
  private boolean removeAbandoned = false;

  // 连接长时间没有使用，被认为发生泄露时长
  private long removeAbandonedTimeoutMillis = 300 * 1000;
  // 发生泄露时是否需要输出 log，建议在开启连接泄露检测时开启，方便排错
  private boolean logAbandoned = false;
  // 是否缓存preparedStatement，即PSCache，对支持游标的数据库性能提升巨大，如 oracle、mysql 5.5 及以上版本

  // private boolean poolPreparedStatements = false;	// oracle、mysql 5.5 及以上版本建议为 true;
  // 只要maxPoolPreparedStatementPerConnectionSize>0,poolPreparedStatements就会被自动设定为true，使用oracle时可以设定此值。
  private int maxPoolPreparedStatementPerConnectionSize = 10;

  // 配置监控统计拦截的filters
  private String filters;  // 监控统计："stat"    防SQL注入："wall"     组合使用： "stat,wall"

  /**
   * hsqldb - "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS"
   * Oracle - "select 1 from dual"
   * DB2 - "select 1 from sysibm.sysdummy1"
   * mysql - "select 1"
   */
  private String validationQuery = "select 1";
  private DruidDataSource ds;
  private Dialect dialect;
  private boolean showSql = false;

  public DruidDataSourceProvider() {
    this("default");
  }

  public DruidDataSourceProvider(String dsName) {
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

    this.filters = prop.get("druid." + dsName + ".filters");
    this.initialSize = prop.getInt("druid." + dsName + ".initialSize", 10);
    this.minIdle = prop.getInt("druid." + dsName + ".minIdle", 10);
    this.maxActive = prop.getInt("druid." + dsName + ".maxActive", 100);
    this.maxWait = prop.getInt("druid." + dsName + ".maxWait", DruidDataSource.DEFAULT_MAX_WAIT);
    this.timeBetweenEvictionRunsMillis = prop.getLong("druid." + dsName + ".timeBetweenEvictionRunsMillis", DruidDataSource.DEFAULT_TIME_BETWEEN_EVICTION_RUNS_MILLIS);
    this.minEvictableIdleTimeMillis = prop.getLong("druid." + dsName + ".minEvictableIdleTimeMillis", DruidDataSource.DEFAULT_MIN_EVICTABLE_IDLE_TIME_MILLIS);
    this.timeBetweenConnectErrorMillis = prop.getLong("druid." + dsName + ".timeBetweenConnectErrorMillis", DruidDataSource.DEFAULT_TIME_BETWEEN_CONNECT_ERROR_MILLIS);
    this.testWhileIdle = prop.getBoolean("druid." + dsName + ".testWhileIdle", true);
    this.testOnBorrow = prop.getBoolean("druid." + dsName + ".testOnBorrow", false);
    this.testOnReturn = prop.getBoolean("druid." + dsName + ".testOnReturn", false);
    this.removeAbandoned = prop.getBoolean("druid." + dsName + ".removeAbandoned", false);
    this.removeAbandonedTimeoutMillis = prop.getInt("druid." + dsName + ".removeAbandonedTimeoutMillis", 300 * 1000);
    this.logAbandoned = prop.getBoolean("druid." + dsName + ".logAbandoned", false);
    this.maxPoolPreparedStatementPerConnectionSize = prop.getInt("druid." + dsName + ".maxPoolPreparedStatementPerConnectionSize", 10);

    this.validationQuery = prop.get("druid." + dsName + ".validationQuery", this.dialect.validQuery());
    buidDataSource();
  }

  public DruidDataSourceProvider(String url, String user, String password) {
    this(url, user, password, false);
  }


  public DruidDataSourceProvider(String url, String user, String password, boolean showSql) {
    this(url, user, password, null, showSql);
  }

  public DruidDataSourceProvider(String url, String user, String password, String dbType, boolean showSql) {
    this(url, user, password, dbType, null, showSql);
  }

  public DruidDataSourceProvider(String url, String user, String password, String dbType, String driverClass, boolean showSql) {
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
    ds = new DruidDataSource();
    ds.setUrl(url);
    ds.setUsername(user);
    ds.setPassword(password);
    if (driverClass != null)
      ds.setDriverClassName(driverClass);
    ds.setInitialSize(initialSize);
    ds.setMinIdle(minIdle);
    ds.setMaxActive(maxActive);
    ds.setMaxWait(maxWait);
    ds.setTimeBetweenConnectErrorMillis(timeBetweenConnectErrorMillis);
    ds.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
    ds.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);

    ds.setValidationQuery(validationQuery);
    ds.setTestWhileIdle(testWhileIdle);
    ds.setTestOnBorrow(testOnBorrow);
    ds.setTestOnReturn(testOnReturn);

    ds.setRemoveAbandoned(removeAbandoned);
    ds.setRemoveAbandonedTimeoutMillis(removeAbandonedTimeoutMillis);
    ds.setLogAbandoned(logAbandoned);

    //只要maxPoolPreparedStatementPerConnectionSize>0,poolPreparedStatements就会被自动设定为true，参照druid的源码
    ds.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);

    if (filters != null)
      try {
        ds.setFilters(filters);
      } catch (SQLException e) {
        throw new DruidRuntimeException(e.getMessage(), e);
      }
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

  public void setShowSql(boolean showSql) {
    this.showSql = showSql;
  }

  public void close() {
    ds.close();
  }

  public DruidDataSourceProvider setDriverClass(String driverClass) {
    this.driverClass = driverClass;
    ds.setDriverClassName(driverClass);
    return this;
  }

  public DruidDataSourceProvider setInitialSize(int initialSize) {
    this.initialSize = initialSize;
    ds.setInitialSize(initialSize);
    return this;
  }

  public DruidDataSourceProvider setMinIdle(int minIdle) {
    this.minIdle = minIdle;
    ds.setMinIdle(minIdle);
    return this;
  }

  public DruidDataSourceProvider setMaxActive(int maxActive) {
    this.maxActive = maxActive;
    ds.setMaxActive(maxActive);
    return this;
  }

  public DruidDataSourceProvider setMaxWait(long maxWait) {
    this.maxWait = maxWait;
    ds.setMaxWait(maxWait);
    return this;
  }

  public DruidDataSourceProvider setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
    this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    ds.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
    return this;
  }

  public DruidDataSourceProvider setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
    this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    ds.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
    return this;
  }

  public DruidDataSourceProvider setTimeBetweenConnectErrorMillis(long timeBetweenConnectErrorMillis) {
    this.timeBetweenConnectErrorMillis = timeBetweenConnectErrorMillis;
    ds.setTimeBetweenConnectErrorMillis(timeBetweenConnectErrorMillis);
    return this;
  }

  public DruidDataSourceProvider setValidationQuery(String validationQuery) {
    this.validationQuery = validationQuery;
    ds.setValidationQuery(validationQuery);
    return this;
  }

  public DruidDataSourceProvider setTestWhileIdle(boolean testWhileIdle) {
    this.testWhileIdle = testWhileIdle;
    ds.setTestWhileIdle(testWhileIdle);
    return this;
  }

  public DruidDataSourceProvider setTestOnBorrow(boolean testOnBorrow) {
    this.testOnBorrow = testOnBorrow;
    ds.setTestOnBorrow(testOnBorrow);
    return this;
  }

  public DruidDataSourceProvider setTestOnReturn(boolean testOnReturn) {
    this.testOnReturn = testOnReturn;
    ds.setTestOnReturn(testOnReturn);
    return this;
  }

  public DruidDataSourceProvider setRemoveAbandoned(boolean removeAbandoned) {
    this.removeAbandoned = removeAbandoned;
    ds.setRemoveAbandoned(removeAbandoned);
    return this;
  }

  public DruidDataSourceProvider setRemoveAbandonedTimeoutMillis(long removeAbandonedTimeoutMillis) {
    this.removeAbandonedTimeoutMillis = removeAbandonedTimeoutMillis;
    ds.setRemoveAbandonedTimeoutMillis(removeAbandonedTimeoutMillis);
    return this;
  }

  public DruidDataSourceProvider setLogAbandoned(boolean logAbandoned) {
    this.logAbandoned = logAbandoned;
    ds.setLogAbandoned(logAbandoned);
    return this;
  }

  public DruidDataSourceProvider setMaxPoolPreparedStatementPerConnectionSize(int maxPoolPreparedStatementPerConnectionSize) {
    this.maxPoolPreparedStatementPerConnectionSize = maxPoolPreparedStatementPerConnectionSize;
    ds.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
    return this;
  }

  public DruidDataSourceProvider setFilters(String filters) {
    this.filters = filters;
    try {
      ds.setFilters(filters);
    } catch (SQLException e) {
      throw new DruidRuntimeException(e.getMessage(), e);
    }
    return this;
  }

  public DruidDataSourceProvider addFilter(Filter... filters) {
    List<Filter> targetList = ds.getProxyFilters();
    for (Filter add : filters) {
      boolean found = false;
      for (Filter target : targetList) {
        if (add.getClass().equals(target.getClass())) {
          found = true;
          break;
        }
      }
      if (!found)
        targetList.add(add);
    }
    return this;
  }

}
