package cn.dreampie.route.config;

import cn.dreampie.common.util.ClassScaner;
import cn.dreampie.log.Logger;
import cn.dreampie.route.core.Resource;

import java.util.ArrayList;
import java.util.List;

/**
 * Routes.
 */
final public class ResourceLoader {
  private static final Logger logger = Logger.getLogger(ResourceLoader.class);

  private final List<Class<? extends Resource>> resources = new ArrayList<Class<? extends Resource>>();
  private List<Class<? extends Resource>> excludeResources = new ArrayList<Class<? extends Resource>>();
  private List<Class<? extends Resource>> includeResources = new ArrayList<Class<? extends Resource>>();
  private List<String> includeResourcePaths = new ArrayList<String>();
  private List<String> excludeResourcePaths = new ArrayList<String>();

  public ResourceLoader add(ResourceLoader resourceLoader) {
    if (resourceLoader != null) {
      resourceLoader.build();
      this.resources.addAll(resourceLoader.resources);
    }
    return this;
  }


  /**
   * Add url mapping to resource. The view path is resourceKey
   *
   * @param resourceClass Controller Class
   */
  public ResourceLoader add(Class<? extends Resource> resourceClass) {
    resources.add(resourceClass);
    return this;
  }

  public ResourceLoader addExcludeClasses(Class<? extends Resource>... clazzes) {
    for (Class<? extends Resource> clazz : clazzes) {
      excludeResources.add(clazz);
    }
    return this;
  }

  public ResourceLoader addExcludeClasses(List<Class<? extends Resource>> clazzes) {
    if (clazzes != null) {
      excludeResources.addAll(clazzes);
    }
    return this;
  }

  public ResourceLoader addExcludePaths(String... paths) {
    for (String path : paths) {
      excludeResourcePaths.add(path);
    }
    return this;
  }

  public ResourceLoader addIncludeClasses(Class<? extends Resource>... clazzes) {
    for (Class<? extends Resource> clazz : clazzes) {
      includeResources.add(clazz);
    }
    return this;
  }

  public ResourceLoader addIncludeClasses(List<Class<? extends Resource>> clazzes) {
    if (clazzes != null) {
      includeResources.addAll(clazzes);
    }
    return this;
  }

  public ResourceLoader addIncludePaths(String... paths) {
    for (String path : paths) {
      includeResourcePaths.add(path);
    }
    return this;
  }

  public void build() {
    if (includeResourcePaths.size() > 0) {
      if (includeResources.size() <= 0) {
        includeResources = ClassScaner.of(Resource.class).includepaths(includeResourcePaths).search();
      } else {
        includeResources.addAll(ClassScaner.of(Resource.class).includepaths(includeResourcePaths).<Resource>search());
      }
    }
    boolean isexclude = false;
    if (includeResources.size() > 0) {
      for (Class resource : includeResources) {
        isexclude = false;
        if (excludeResourcePaths.size() > 0) {
          for (String excludepath : excludeResourcePaths) {
            if (resource.getName().startsWith(excludepath)) {
              logger.debug("Exclude resource:" + resource.getName());
              isexclude = true;
              break;
            }
          }
        }
        if (isexclude || excludeResources.contains(resource)) {
          continue;
        }
        this.add(resource);
        logger.info("Resources.add(" + resource.getName() + ")");
      }
    } else {
      logger.warn("Could not load any resources.");
    }
  }

  public List<Class<? extends Resource>> getResources() {
    return resources;
  }
}






