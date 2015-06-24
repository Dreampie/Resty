package cn.dreampie.orm.repository;

import cn.dreampie.common.CaseStyle;
import cn.dreampie.common.Plugin;
import cn.dreampie.common.annotation.Repository;
import cn.dreampie.common.annotation.Service;
import cn.dreampie.common.entity.exception.EntityException;
import cn.dreampie.common.ioc.ApplicationContainer;
import cn.dreampie.common.util.scan.AnnotationScaner;
import cn.dreampie.log.Logger;
import cn.dreampie.orm.Metadata;
import cn.dreampie.orm.exception.DBException;
import cn.dreampie.orm.meta.*;
import cn.dreampie.orm.provider.DataSourceProvider;
import cn.dreampie.orm.repository.annotation.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author Dreampie
 * @date 2015-06-18
 * @what
 */
public class RepositoryPlugin implements Plugin {
  private static final Logger logger = Logger.getLogger(RepositoryPlugin.class);

  private Set<Class<?>> excludeClasses = new HashSet<Class<?>>();
  private Set<Class<?>> includeClasses = new HashSet<Class<?>>();
  private Set<String> includeClassPackages = new HashSet<String>();
  private Set<String> excludeClassPackages = new HashSet<String>();

  private DataSourceProvider dataSourceProvider;

  public RepositoryPlugin(DataSourceProvider dataSourceProvider) {
    this.dataSourceProvider = dataSourceProvider;
  }

  public RepositoryPlugin addExcludeClasses(Class<?>... classes) {
    Collections.addAll(excludeClasses, classes);
    return this;
  }

  public RepositoryPlugin addExcludeClasses(Set<Class<?>> classes) {
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
  public RepositoryPlugin addExcludePackages(String... packages) {
    Collections.addAll(excludeClassPackages, packages);
    return this;
  }

  public RepositoryPlugin addIncludeClasses(Class<?>... classes) {
    Collections.addAll(includeClasses, classes);
    return this;
  }

  public RepositoryPlugin addIncludeClasses(Set<Class<?>> classes) {
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
  public RepositoryPlugin addIncludePackages(String... packages) {
    Collections.addAll(includeClassPackages, packages);
    return this;
  }

  public boolean start() {
    if (includeClassPackages.size() > 0) {
      if (includeClasses.size() <= 0) {
        includeClasses = AnnotationScaner.or(Service.class, Repository.class).includePackages(includeClassPackages).scan();
      } else {
        includeClasses.addAll(AnnotationScaner.or(Service.class, Repository.class).includePackages(includeClassPackages).scan());
      }
    }

    DataSourceMeta dataSourceMeta = new DataSourceMeta(dataSourceProvider);
    if (includeClasses.size() > 0) {
      Set<EntityMeta> entityMetas = new HashSet<EntityMeta>();
      EntityMeta entityMeta = null;
      boolean isExclude = false;
      for (Class<?> componentClass : includeClasses) {
        if (excludeClasses.contains(componentClass) || Modifier.isAbstract(componentClass.getModifiers())) {
          continue;
        }
        isExclude = false;
        if (excludeClassPackages.size() > 0) {
          for (String excludepath : excludeClassPackages) {
            if (componentClass.getName().startsWith(excludepath)) {
              logger.debug("Exclude model:" + componentClass.getName());
              isExclude = true;
              break;
            }
          }
        }
        if (isExclude) {
          continue;
        }
        //获取repository的entity
        if (CentralRepository.class.isAssignableFrom(componentClass)) {
          Class<?> entityClass;
          Type[] actualTypeArguments = ((ParameterizedType) componentClass.getGenericSuperclass()).getActualTypeArguments();
          if (actualTypeArguments.length > 0) {
            entityClass = (Class<?>) actualTypeArguments[0];
          } else {
            throw new DBException("Could not found entity.");
          }
          //add entityMeta
          Entity entityAnn = entityClass.getAnnotation(Entity.class);

          String table = entityAnn.table();
          CaseStyle style = entityAnn.style();
          boolean cached = entityAnn.cached();

          Field[] filds = entityClass.getFields();
          String id = null;

          Map<Field, FieldMeta> fieldMetas = new HashMap<Field, FieldMeta>();
          IdMeta idMeta = null;
          FieldMeta fieldMeta = null;
          for (Field field : filds) {
            //id
            if (id == null) {
              Id idAnn = field.getAnnotation(Id.class);
              if (idAnn != null) {
                id = idAnn.name();
                try {
                  idMeta = new IdMeta(idAnn.name(), field, idAnn.generate(), idAnn.generator().newInstance());
                  fieldMetas.put(field, idMeta);
                } catch (InstantiationException e) {
                  throw new EntityException(e.getMessage(), e);
                } catch (IllegalAccessException e) {
                  throw new EntityException(e.getMessage(), e);
                }
                continue;
              }
            }
            //普通列
            Column columnAnn = field.getAnnotation(Column.class);
            if (columnAnn != null) {
              fieldMeta = new FieldMeta(columnAnn.name(), field);
              fieldMetas.put(field, fieldMeta);
              continue;
            }
            //一对多列
            OneToMany manyAnn = field.getAnnotation(OneToMany.class);
            if (manyAnn != null) {
              fieldMeta = new SubMeta(manyAnn.name(), field, field.getDeclaringClass(), manyAnn.lazy(), manyAnn.foreign(), manyAnn.cascade(), manyAnn.join());
              fieldMetas.put(field, fieldMeta);
              continue;
            }
            //一对一列
            OneToOne oneAnn = field.getAnnotation(OneToOne.class);
            if (oneAnn != null) {
              fieldMeta = new SubMeta(oneAnn.name(), field, field.getDeclaringClass(), oneAnn.lazy(), oneAnn.foreign(), oneAnn.cascade(), oneAnn.join());
              fieldMetas.put(field, fieldMeta);
              continue;
            }
          }
          entityMeta = new EntityMeta(dataSourceMeta, table, style, cached, idMeta, fieldMetas);
          //添加到entity元数据集合
          Metadata.addEntityMeta(entityClass, entityMeta);
          entityMetas.add(entityMeta);
          logger.info("@Repository.add(" + entityMeta.getTable() + ", " + componentClass.getName() + ")");
        } else {
          logger.info("@Service.add(" + componentClass.getName() + ")");
        }
        //添加到ioc
        ApplicationContainer.set(componentClass);
      }
      //model 元数据
      EntityMetaBuilder.buildColumnMeta(entityMetas, dataSourceMeta);
    } else {
      logger.warn("Could not load any model for   " + dataSourceMeta.getDsName() + ".");
    }
    //数据源  元数据
    Metadata.addDataSourceMeta(dataSourceMeta);
    return true;
  }

  public boolean stop() {
    Metadata.close();
    return true;
  }
}
