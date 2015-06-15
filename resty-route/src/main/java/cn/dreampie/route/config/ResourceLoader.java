package cn.dreampie.route.config;

import cn.dreampie.common.ioc.ApplicationContainer;
import cn.dreampie.common.util.scan.AnnotationScaner;
import cn.dreampie.log.Logger;
import cn.dreampie.common.annotation.Resource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * ResourceLoader.
 */
final public class ResourceLoader {
  private static final Logger logger = Logger.getLogger(ResourceLoader.class);

  private final Set<Class<?>> resources = new HashSet<Class<?>>();
  private Set<Class<?>> excludeResources = new HashSet<Class<?>>();
  private Set<Class<?>> includeResources = new HashSet<Class<?>>();
  private Set<String> includeResourcePackages = new HashSet<String>();
  private Set<String> excludeResourcePackages = new HashSet<String>();

  public ResourceLoader add(ResourceLoader resourceLoader) {
    if (resourceLoader != null) {
      resourceLoader.build();
      this.resources.addAll(resourceLoader.resources);
    }
    return this;
  }


  /**
   * Add url mapping to resource. The view p is resourceKey
   *
   * @param resourceClass Controller Class
   */
  public ResourceLoader add(Class<?> resourceClass) {
    resources.add(resourceClass);
    return this;
  }

  public ResourceLoader addExcludeClasses(Class<?>... clazzes) {
    Collections.addAll(excludeResources, clazzes);
    return this;
  }

  public ResourceLoader addExcludeClasses(Set<Class<?>> clazzes) {
    if (clazzes != null) {
      excludeResources.addAll(clazzes);
    }
    return this;
  }

  /**
   * exclude scan packages  eg. cn.dreampie.resource
   *
   * @param packages packages
   * @return
   */
  public ResourceLoader addExcludePackages(String... packages) {
    Collections.addAll(excludeResourcePackages, packages);
    return this;
  }

  public ResourceLoader addIncludeClasses(Class<?>... clazzes) {
    Collections.addAll(includeResources, clazzes);
    return this;
  }

  public ResourceLoader addIncludeClasses(Set<Class<?>> clazzes) {
    if (clazzes != null) {
      includeResources.addAll(clazzes);
    }
    return this;
  }

  /**
   * scan packages  eg. cn.dreampie.resource
   *
   * @param packages packages
   * @return
   */
  public ResourceLoader addIncludePackages(String... packages) {
    Collections.addAll(includeResourcePackages, packages);
    return this;
  }

  public void build() {
    if (includeResourcePackages.size() > 0) {
      if (includeResources.size() <= 0) {
        includeResources = AnnotationScaner.of(Resource.class).includePackages(includeResourcePackages).scan();
      } else {
        includeResources.addAll(AnnotationScaner.of(Resource.class).includePackages(includeResourcePackages).scan());
      }
    }
    boolean isExclude = false;
    if (includeResources.size() > 0) {
      for (Class resourceClazz : includeResources) {
        isExclude = false;
        if (excludeResourcePackages.size() > 0) {
          for (String excludepath : excludeResourcePackages) {
            if (resourceClazz.getName().startsWith(excludepath)) {
              logger.debug("Exclude resource:" + resourceClazz.getName());
              isExclude = true;
              break;
            }
          }
        }
        if (isExclude || excludeResources.contains(resourceClazz)) {
          continue;
        }
        this.add(resourceClazz);
        //加入容器
        ApplicationContainer.set(resourceClazz);
        logger.info("Resources.add(" + resourceClazz.getName() + ")");
      }
    } else {
      logger.warn("Could not load any resources.");
    }
  }

  public Set<Class<?>> getResources() {
    return resources;
  }
}






