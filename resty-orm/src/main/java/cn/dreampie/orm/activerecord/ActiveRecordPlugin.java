package cn.dreampie.orm.activerecord;

import cn.dreampie.common.Plugin;
import cn.dreampie.common.generate.Generator;
import cn.dreampie.common.util.json.EntitySerializer;
import cn.dreampie.common.util.json.Jsoner;
import cn.dreampie.common.util.json.EntityDeserializer;
import cn.dreampie.common.util.scan.ClassScaner;
import cn.dreampie.log.Logger;
import cn.dreampie.orm.DataSourceMeta;
import cn.dreampie.orm.Metadata;
import cn.dreampie.orm.TableMeta;
import cn.dreampie.orm.TableMetaBuilder;
import cn.dreampie.orm.activerecord.annotation.ActiveRecord;
import cn.dreampie.orm.activerecord.exception.ActiveRecordException;
import cn.dreampie.orm.provider.DataSourceProvider;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * ActiveRecord plugin.
 */
public class ActiveRecordPlugin implements Plugin {
  private static final Logger logger = Logger.getLogger(ActiveRecordPlugin.class);

  private Set<Class<? extends Model>> excludeClasses = new HashSet<Class<? extends Model>>();
  private Set<Class<? extends Model>> includeClasses = new HashSet<Class<? extends Model>>();
  private Set<String> includeClassPackages = new HashSet<String>();
  private Set<String> excludeClassPackages = new HashSet<String>();

  private DataSourceProvider dataSourceProvider;

  public ActiveRecordPlugin(DataSourceProvider dataSourceProvider) {
    this.dataSourceProvider = dataSourceProvider;
  }

  public ActiveRecordPlugin addExcludeClasses(Class<? extends Model>... classes) {
    Collections.addAll(excludeClasses, classes);
    return this;
  }

  public ActiveRecordPlugin addExcludeClasses(Set<Class<? extends Model>> classes) {
    if (classes != null) {
      excludeClasses.addAll(classes);
    }
    return this;
  }

  /**
   * exclude scan packages  eg. cn.dreampie.resource
   *
   * @param packages packages
   * @return
   */
  public ActiveRecordPlugin addExcludePackages(String... packages) {
    Collections.addAll(excludeClassPackages, packages);
    return this;
  }

  public ActiveRecordPlugin addIncludeClasses(Class<? extends Model>... classes) {
    Collections.addAll(includeClasses, classes);
    return this;
  }

  public ActiveRecordPlugin addIncludeClasses(Set<Class<? extends Model>> classes) {
    if (classes != null) {
      includeClasses.addAll(classes);
    }
    return this;
  }

  /**
   * scan packages  eg. cn.dreampie.resource
   *
   * @param packages packages
   * @return
   */
  public ActiveRecordPlugin addIncludePackages(String... packages) {
    Collections.addAll(includeClassPackages, packages);
    return this;
  }

  public boolean start() {
    if (includeClassPackages.size() > 0) {
      if (includeClasses.size() <= 0) {
        includeClasses = ClassScaner.of(Model.class).includePackages(includeClassPackages).scan();
      } else {
        includeClasses.addAll(ClassScaner.of(Model.class).includePackages(includeClassPackages).<Model>scan());
      }
    }

    DataSourceMeta dsm = new DataSourceMeta(dataSourceProvider);
    if (includeClasses.size() > 0) {
      Set<TableMeta> tableMetas = new HashSet<TableMeta>();
      TableMeta tableMeta = null;
      boolean isExclude = false;
      for (Class<? extends Model> modelClass : includeClasses) {
        if (excludeClasses.contains(modelClass) || Modifier.isAbstract(modelClass.getModifiers())) {
          continue;
        }
        isExclude = false;
        if (excludeClassPackages.size() > 0) {
          for (String excludepath : excludeClassPackages) {
            if (modelClass.getName().startsWith(excludepath)) {
              logger.debug("Exclude model:" + modelClass.getName());
              isExclude = true;
              break;
            }
          }
        }
        if (isExclude) {
          continue;
        }
        //add modelMeta
        ActiveRecord modelAnn = modelClass.getAnnotation(ActiveRecord.class);
        Generator generator = null;
        if(modelAnn.generator()!=null) {
          try {
            generator = modelAnn.generator().newInstance();
          } catch (InstantiationException e) {
            throw new ActiveRecordException(e.getMessage(), e);
          } catch (IllegalAccessException e) {
            throw new ActiveRecordException(e.getMessage(), e);
          }
        }

        tableMeta = new TableMeta(dataSourceProvider.getDsName(),modelAnn.tableName(), modelAnn.generatedKey(),generator,modelAnn.primaryKey(),modelAnn.cached());
        tableMetas.add(tableMeta);
        logger.info("Models.add(" + tableMeta.getTableName() + ", " + modelClass.getName() + ")");

        //json  config
        Jsoner.addConfig(modelClass, EntitySerializer.instance(), EntityDeserializer.instance());
      }
      //model 元数据
      TableMetaBuilder.buildTableMeta(tableMetas, dsm);
    } else {
      logger.warn("Could not load any model for   " + dsm.getDsName() + ".");
    }
    //Record 解析支持
    Jsoner.addConfig(Record.class, EntitySerializer.instance(), EntityDeserializer.instance());
    //数据源  元数据
    Metadata.addDataSourceMeta(dsm);
    return true;
  }

  public boolean stop() {
    Metadata.close();
    return true;
  }

}
