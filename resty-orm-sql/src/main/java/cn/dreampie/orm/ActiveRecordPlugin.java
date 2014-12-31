package cn.dreampie.orm;

import cn.dreampie.common.Plugin;
import cn.dreampie.log.Logger;
import cn.dreampie.log.LoggerFactory;
import cn.dreampie.orm.dialect.Dialect;
import cn.dreampie.orm.dialect.DialectFactory;
import cn.dreampie.util.ClassScaner;

import java.util.ArrayList;
import java.util.List;

/**
 * ActiveRecord plugin.
 */
public class ActiveRecordPlugin implements Plugin {
  private static final Logger logger = LoggerFactory.getLogger(ActiveRecordPlugin.class);

  private List<Class<? extends Base>> excludeClasses = new ArrayList<Class<? extends Base>>();
  private List<Class<? extends Base>> includeClasses = new ArrayList<Class<? extends Base>>();
  private List<String> includeClassPaths = new ArrayList<String>();
  private List<String> excludeClassPaths = new ArrayList<String>();

  private DataSourceProvider dataSourceProvider;
  private String dsName;
  private List<ModelMeta> modelMetas;
  private boolean showSql = false;


  public ActiveRecordPlugin(DataSourceProvider dataSourceProvider) {
    this(DS.DEFAULT_DS_NAME, dataSourceProvider);
  }

  public ActiveRecordPlugin(String dsName, DataSourceProvider dataSourceProvider) {
    this.dsName = dsName;
    this.dataSourceProvider = dataSourceProvider;
  }


  public ActiveRecordPlugin addExcludeClasses(Class<? extends Base>... clazzes) {
    for (Class<? extends Base> clazz : clazzes) {
      excludeClasses.add(clazz);
    }
    return this;
  }

  public ActiveRecordPlugin addExcludeClasses(List<Class<? extends Base>> clazzes) {
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

  public ActiveRecordPlugin addIncludeClasses(Class<? extends Base>... clazzes) {
    for (Class<? extends Base> clazz : clazzes) {
      includeClasses.add(clazz);
    }
    return this;
  }

  public ActiveRecordPlugin addIncludeClasses(List<Class<? extends Base>> clazzes) {
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
      includeClasses = ClassScaner.of(Base.class).includepaths(includeClassPaths).search();
    }
    for (Class<? extends Base> modelClass : includeClasses) {
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
    Metadatas.addDataSourceMeta(dsName, dsm);
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
