package cn.dreampie.core.config;

import cn.dreampie.core.route.base.Resource;
import cn.dreampie.kit.ClassScaner;
import cn.dreampie.log.Logger;
import cn.dreampie.log.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Routes.
 */
final public class ResourceLoader {

  private final List<Class<? extends Resource>> resources = new ArrayList();
  private List<Class<? extends Resource>> excludeResources = new ArrayList();
  private List<Class<? extends Resource>> includeResources = new ArrayList();
  private List<String> includeResourcePaths = new ArrayList();
  private List<String> excludeResourcePaths = new ArrayList();
  private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoader.class);

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
    List<Class<? extends Resource>> resourceClasses = ClassScaner.of(Resource.class).includepaths(includeResourcePaths).search();
    for (Class resource : resourceClasses) {
      if (excludeResources.contains(resource)) {
        continue;
      }
      this.add(resource);
      LOGGER.info("resources.add(" + resource.getName() + ")");
    }
  }

  public List<Class<? extends Resource>> getResources() {
    return resources;
  }
}






