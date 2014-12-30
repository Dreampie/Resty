package cn.dreampie.orm;

import cn.dreampie.core.base.Plugin;
import cn.dreampie.log.Logger;
import cn.dreampie.log.LoggerFactory;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.dialect.DialectFactory;
import cn.dreampie.orm.name.INameStyle;
import cn.dreampie.orm.name.SimpleNameStyles;
import cn.dreampie.util.ClassScaner;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * ActiveRecord plugin.
 */
public class ActiveRecordPlugin implements Plugin {
  private static final Logger logger = LoggerFactory.getLogger(ActiveRecordPlugin.class);

  private List<Class<? extends Model>> excludeClasses = new ArrayList();
  private List<Class<? extends Model>> includeClasses = new ArrayList();
  private List<String> includeClassPaths = new ArrayList();
  private List<String> excludeClassPaths = new ArrayList();
  private INameStyle nameStyle;

  private DataSourceProvider dataSourceProvider;
  private String dsName;
  private List<ModelMeta> modelMetas;
  private boolean showSql = false;

  public ActiveRecordPlugin(DataSource dataSource) {
    this(dataSource, SimpleNameStyles.DEFAULT);
  }

  public ActiveRecordPlugin(DataSource dataSource, INameStyle nameStyle) {
    this.nameStyle = nameStyle;
  }

  public ActiveRecordPlugin(DataSourceProvider dataSourceProvider) {
    this(dataSourceProvider, SimpleNameStyles.DEFAULT);
  }

  public ActiveRecordPlugin(DataSourceProvider dataSourceProvider, INameStyle nameStyle) {
    this(DS.DEFAULT_DS_NAME, dataSourceProvider, nameStyle);
  }

  public ActiveRecordPlugin(String dsName, DataSourceProvider dataSourceProvider, INameStyle nameStyle) {
    this.dsName = dsName;
    this.dataSourceProvider = dataSourceProvider;
    this.nameStyle = nameStyle;
  }


  public ActiveRecordPlugin addExcludeClasses(Class<? extends Model>... clazzes) {
    for (Class<? extends Model> clazz : clazzes) {
      excludeClasses.add(clazz);
    }
    return this;
  }

  public ActiveRecordPlugin addExcludeClasses(List<Class<? extends Model>> clazzes) {
    if (clazzes != null) {
      excludeClasses.addAll(clazzes);
    }
    return this;
  }

  public ActiveRecordPlugin addExcludePaths(String... paths) {
    for (String path : paths) {
      excludeClassPaths.add(path);
    }
    return this;
  }

  public ActiveRecordPlugin addIncludeClasses(Class<? extends Model>... clazzes) {
    for (Class<? extends Model> clazz : clazzes) {
      includeClasses.add(clazz);
    }
    return this;
  }

  public ActiveRecordPlugin addIncludeClasses(List<Class<? extends Model>> clazzes) {
    if (clazzes != null) {
      includeClasses.addAll(clazzes);
    }
    return this;
  }

  public ActiveRecordPlugin addIncludePaths(String... paths) {
    for (String path : paths) {
      includeClassPaths.add(path);
    }
    return this;
  }

  public boolean start() {
    if (includeClasses.size() <= 0) {
      includeClasses = ClassScaner.of(Model.class).includepaths(includeClassPaths).search();
    }
    for (Class<? extends Model> modelClass : includeClasses) {
      boolean isexclude = false;
      if (excludeClassPaths.size() > 0) {
        for (String excludepath : excludeClassPaths) {
          if (modelClass.getName().startsWith(excludepath)) {
            logger.debug("exclude model:" + modelClass.getName());
            isexclude = true;
            break;
          }
        }
      }
      if (isexclude || excludeClasses.contains(modelClass)) {
        continue;
      }
      modelMetas = new ArrayList<ModelMeta>();
      ModelMeta modelMeta = new ModelMeta(modelClass, dsName);
      modelMetas.add(modelMeta);
      logger.debug("addMapping(" + modelMeta.getTableName() + ", " + modelClass.getName() + ")");
    }
    DataSourceMeta dsm = new DataSourceMeta(dsName, dataSourceProvider, showSql);
    //数据源  元数据
    Metadatas.addDataSourceMetadata(dsName, dsm);
    //model 元数据
    ModelMetaBuilder.build(modelMetas, dsm);
    return true;
  }

  public boolean stop() {
    return true;
  }

  public void setShowSql(boolean showSql) {
    this.showSql = showSql;
  }

  public void setDsName(String dsName) {
    this.dsName = dsName;
  }

  public void addDialect(String dialectName, Dialect dialect) {
    DialectFactory.addDialect(dialectName, dialect);
  }
}
