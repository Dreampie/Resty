package cn.dreampie.orm.provider.ds;

import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.provider.DataSourceProvider;

import javax.sql.DataSource;

/**
 * Created by zhaopeng on 16/1/21.
 */
public class DsDataSourceProvider implements DataSourceProvider {
    private DataSource dataSource;
    private Dialect dialect;
    private boolean showSql = false;
    private String dsName = "default";

    public DsDataSourceProvider(DataSource dataSource, Dialect dialect) {
        this.dataSource = dataSource;
        this.dialect = dialect;
    }

    public DsDataSourceProvider(DataSource dataSource, Dialect dialect, boolean showSql, String dsName) {
        this.dataSource = dataSource;
        this.dialect = dialect;
        this.showSql = showSql;
        this.dsName = dsName;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public Dialect getDialect() {
        return this.dialect;
    }

    public String getDsName() {
        return dsName;
    }

    public boolean isShowSql() {
        return this.showSql;
    }

    public void close() {
        //do nothing  becouse  it have no close method
    }

}
