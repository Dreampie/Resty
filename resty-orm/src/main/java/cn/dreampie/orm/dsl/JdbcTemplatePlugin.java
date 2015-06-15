package cn.dreampie.orm.dsl;

import cn.dreampie.common.Plugin;
import cn.dreampie.common.annotation.Repository;
import cn.dreampie.common.util.json.EntityDeserializer;
import cn.dreampie.common.util.json.EntitySerializer;
import cn.dreampie.common.util.json.Jsoner;
import cn.dreampie.common.util.scan.AnnotationScaner;
import cn.dreampie.log.Logger;
import cn.dreampie.orm.DataSourceMeta;
import cn.dreampie.orm.Metadata;
import cn.dreampie.orm.provider.DataSourceProvider;

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * ActiveRecord plugin.
 */
public class JdbcTemplatePlugin implements Plugin {
  private static final Logger logger = Logger.getLogger(JdbcTemplatePlugin.class);

  private Set<Class<?>> excludeClasses = new HashSet<Class<?>>();
  private Set<Class<?>> includeClasses = new HashSet<Class<?>>();
  private Set<String> includeClassPackages = new HashSet<String>();
  private Set<String> excludeClassPackages = new HashSet<String>();

  private DataSourceProvider dataSourceProvider;

  public JdbcTemplatePlugin(DataSourceProvider dataSourceProvider) {
    this.dataSourceProvider = dataSourceProvider;
  }

  public JdbcTemplatePlugin addExcludeClasses(Class<?>... classes) {
    Collections.addAll(excludeClasses, classes);
    return this;
  }

  public JdbcTemplatePlugin addExcludeClasses(Set<Class<?>> classes) {
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
  public JdbcTemplatePlugin addExcludePackages(String... packages) {
    Collections.addAll(excludeClassPackages, packages);
    return this;
  }

  public JdbcTemplatePlugin addIncludeClasses(Class<?>... classes) {
    Collections.addAll(includeClasses, classes);
    return this;
  }

  public JdbcTemplatePlugin addIncludeClasses(Set<Class<?>> classes) {
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
  public JdbcTemplatePlugin addIncludePackages(String... packages) {
    Collections.addAll(includeClassPackages, packages);
    return this;
  }

  public boolean start() {
    if (includeClassPackages.size() > 0) {
      if (includeClasses.size() <= 0) {
        includeClasses = AnnotationScaner.of(Repository.class).includePackages(includeClassPackages).scan();
      } else {
        includeClasses.addAll(AnnotationScaner.of(Repository.class).includePackages(includeClassPackages).scan());
      }
    }

    DataSourceMeta dsm = new DataSourceMeta(dataSourceProvider);
    if (includeClasses.size() > 0) {
      boolean isExclude = false;
      for (Class<?> clazz : includeClasses) {
        if (excludeClasses.contains(clazz) || Modifier.isAbstract(clazz.getModifiers())) {
          continue;
        }
        isExclude = false;
        if (excludeClassPackages.size() > 0) {
          for (String excludepath : excludeClassPackages) {
            if (clazz.getName().startsWith(excludepath)) {
              logger.debug("Exclude Repository:" + clazz.getName());
              isExclude = true;
              break;
            }
          }
        }
        if (isExclude) {
          continue;
        }
        logger.info("Repositories.add(" + clazz.getName() + ")");

        //json  config
        Jsoner.addConfig(clazz, EntitySerializer.instance(), EntityDeserializer.instance());
      }
      //model 元数据
    } else {
      logger.warn("Could not load any model for   " + dsm.getDsName() + ".");
    }
    //数据源  元数据
    Metadata.addDataSourceMeta(dsm);
    return true;
  }

  public boolean stop() {
    Metadata.close();
    return true;
  }

}
