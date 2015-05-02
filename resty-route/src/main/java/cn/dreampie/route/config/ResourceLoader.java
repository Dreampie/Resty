package cn.dreampie.route.config;

import cn.dreampie.common.util.scan.ClassScaner;
import cn.dreampie.log.Logger;
import cn.dreampie.route.core.Resource;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * ResourceLoader.
 */
final public class ResourceLoader {
  private static final Logger logger = Logger.getLogger(ResourceLoader.class);

  private final Set<Class<? extends Resource>> resources = new HashSet<Class<? extends Resource>>();
  private Set<Class<? extends Resource>> excludeResources = new HashSet<Class<? extends Resource>>();
  private Set<Class<? extends Resource>> includeResources = new HashSet<Class<? extends Resource>>();
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
  public ResourceLoader add(Class<? extends Resource> resourceClass) {
    resources.add(resourceClass);
    return this;
  }

  public ResourceLoader addExcludeClasses(Class<? extends Resource>... clazzes) {
    Collections.addAll(excludeResources, clazzes);
    return this;
  }

  public ResourceLoader addExcludeClasses(Set<Class<? extends Resource>> clazzes) {
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

  public ResourceLoader addIncludeClasses(Class<? extends Resource>... clazzes) {
    Collections.addAll(includeResources, clazzes);
    return this;
  }

  public ResourceLoader addIncludeClasses(Set<Class<? extends Resource>> clazzes) {
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
        includeResources = ClassScaner.of(Resource.class).includePackages(includeResourcePackages).scan();
      } else {
        includeResources.addAll(ClassScaner.of(Resource.class).includePackages(includeResourcePackages).<Resource>scan());
      }
    }
    boolean isExclude = false;
    if (includeResources.size() > 0) {
      for (Class resource : includeResources) {
        isExclude = false;
        if (excludeResourcePackages.size() > 0) {
          for (String excludepath : excludeResourcePackages) {
            if (resource.getName().startsWith(excludepath)) {
              logger.debug("Exclude resource:" + resource.getName());
              isExclude = true;
              break;
            }
          }
        }
        if (isExclude || excludeResources.contains(resource)) {
          continue;
        }
        this.add(resource);
        logger.info("Resources.add(" + resource.getName() + ")");
      }
    } else {
      logger.warn("Could not load any resources.");
    }
  }

  public Set<Class<? extends Resource>> getResources() {
    return resources;
  }
}






