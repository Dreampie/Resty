package cn.dreampie.route.config;

import cn.dreampie.common.annotation.Service;
import cn.dreampie.common.ioc.ApplicationContainer;
import cn.dreampie.common.util.scan.AnnotationScaner;
import cn.dreampie.log.Logger;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * ServiceLoader.
 */
final public class ServiceLoader {
  private static final Logger logger = Logger.getLogger(ServiceLoader.class);

  private final Set<Class<?>> service = new HashSet<Class<?>>();
  private Set<Class<?>> excludeServices = new HashSet<Class<?>>();
  private Set<Class<?>> includeServices = new HashSet<Class<?>>();
  private Set<String> includeServicePackages = new HashSet<String>();
  private Set<String> excludeServicePackages = new HashSet<String>();

  public ServiceLoader add(ServiceLoader serviceLoader) {
    if (serviceLoader != null) {
      serviceLoader.build();
      this.service.addAll(serviceLoader.service);
    }
    return this;
  }


  /**
   * Add url mapping to service. The view p is serviceKey
   *
   * @param serviceClass Service Class
   */
  public ServiceLoader add(Class<?> serviceClass) {
    service.add(serviceClass);
    return this;
  }

  public ServiceLoader addExcludeClasses(Class<?>... clazzes) {
    Collections.addAll(excludeServices, clazzes);
    return this;
  }

  public ServiceLoader addExcludeClasses(Set<Class<?>> clazzes) {
    if (clazzes != null) {
      excludeServices.addAll(clazzes);
    }
    return this;
  }

  /**
   * exclude scan packages  eg. cn.dreampie.service
   *
   * @param packages packages
   * @return
   */
  public ServiceLoader addExcludePackages(String... packages) {
    Collections.addAll(excludeServicePackages, packages);
    return this;
  }

  public ServiceLoader addIncludeClasses(Class<?>... clazzes) {
    Collections.addAll(includeServices, clazzes);
    return this;
  }

  public ServiceLoader addIncludeClasses(Set<Class<?>> clazzes) {
    if (clazzes != null) {
      includeServices.addAll(clazzes);
    }
    return this;
  }

  /**
   * scan packages  eg. cn.dreampie.service
   *
   * @param packages packages
   * @return
   */
  public ServiceLoader addIncludePackages(String... packages) {
    Collections.addAll(includeServicePackages, packages);
    return this;
  }

  public void build() {
    if (includeServicePackages.size() > 0) {
      if (includeServices.size() <= 0) {
        includeServices = AnnotationScaner.of(Service.class).includePackages(includeServicePackages).scan();
      } else {
        includeServices.addAll(AnnotationScaner.of(Service.class).includePackages(includeServicePackages).scan());
      }
    }
    boolean isExclude = false;
    if (includeServices.size() > 0) {
      for (Class serviceClazz : includeServices) {
        isExclude = false;
        if (excludeServicePackages.size() > 0) {
          for (String excludepath : excludeServicePackages) {
            if (serviceClazz.getName().startsWith(excludepath)) {
              logger.debug("Exclude service:" + serviceClazz.getName());
              isExclude = true;
              break;
            }
          }
        }
        if (isExclude || excludeServices.contains(serviceClazz)) {
          continue;
        }
        this.add(serviceClazz);
        //加入容器
        ApplicationContainer.set(serviceClazz);
        logger.info("Services.add(" + serviceClazz.getName() + ")");
      }
    } else {
      logger.warn("Could not load any service.");
    }
  }

  public Set<Class<?>> getService() {
    return service;
  }
}






