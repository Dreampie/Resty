package cn.dreampie.orm;

import cn.dreampie.common.Plugin;
import cn.dreampie.common.util.ClassScaner;
import cn.dreampie.common.util.json.Jsoner;
import cn.dreampie.common.util.json.ModelDeserializer;
import cn.dreampie.common.util.json.ModelSerializer;
import cn.dreampie.log.Logger;
import cn.dreampie.orm.provider.DataSourceProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * ActiveRecord plugin.
 */
public class ActiveRecordPlugin implements Plugin {
  private static final Logger logger = Logger.getLogger(ActiveRecordPlugin.class);

  private List<Class<? extends Model>> excludeClasses = new ArrayList<Class<? extends Model>>();
  private List<Class<? extends Model>> includeClasses = new ArrayList<Class<? extends Model>>();
  private List<String> includeClassPaths = new ArrayList<String>();
  private List<String> excludeClassPaths = new ArrayList<String>();

  private DataSourceProvider dataSourceProvider;
  private String dsName;
  private List<TableMeta> tableMetas;
  private boolean showSql = false;

  public ActiveRecordPlugin(DataSourceProvider dataSourceProvider) {
    this(dataSourceProvider, false);
  }

  public ActiveRecordPlugin(DataSourceProvider dataSourceProvider, boolean showSql) {
    this.dsName = dataSourceProvider.getDsName();
    this.dataSourceProvider = dataSourceProvider;
    this.showSql = showSql;
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
    if (includeClassPaths.size() > 0) {
      if (includeClasses.size() <= 0) {
        includeClasses = ClassScaner.of(Model.class).includepaths(includeClassPaths).search();
      } else {
        includeClasses.addAll(ClassScaner.of(Model.class).includepaths(includeClassPaths).<Model>search());
      }
    }

    DataSourceMeta dsm = new DataSourceMeta(dsName, dataSourceProvider, showSql);
    if (includeClasses.size() > 0) {
      tableMetas = new ArrayList<TableMeta>();
      TableMeta tableMeta = null;
      for (Class<? extends Model> modelClass : includeClasses) {
        boolean isexclude = false;
        if (excludeClassPaths.size() > 0) {
          for (String excludepath : excludeClassPaths) {
            if (modelClass.getName().startsWith(excludepath)) {
              logger.debug("Exclude model:" + modelClass.getName());
              isexclude = true;
              break;
            }
          }
        }
        if (isexclude || excludeClasses.contains(modelClass)) {
          continue;
        }
        //add modelMeta
        tableMeta = new TableMeta(dsName, modelClass);
        tableMetas.add(tableMeta);
        logger.info("AddMapping(" + tableMeta.getTableName() + ", " + modelClass.getName() + ")");

        //json  config
        Jsoner.addConfig(modelClass, ModelSerializer.instance(), ModelDeserializer.instance());
      }
      //model 元数据
      TableMetaBuilder.buildModel(tableMetas, dsm);
    }
    //Record 解析支持
    Jsoner.addConfig(Record.class, ModelSerializer.instance(), ModelDeserializer.instance());
    //数据源  元数据
    Metadata.addDataSourceMeta(dsName, dsm);
    return true;
  }

  public boolean stop() {
    //关闭数据源  元数据
    Metadata.closeDataSourceMeta();
    return true;
  }

}
