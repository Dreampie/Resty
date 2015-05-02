package cn.dreampie.route.config;

import cn.dreampie.common.util.scan.ClassScaner;
import cn.dreampie.log.Logger;
import cn.dreampie.route.core.Service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * ServiceLoader.
 */
final public class ServiceLoader {
  private static final Logger logger = Logger.getLogger(ServiceLoader.class);

  private final Set<Class<? extends Service>> services = new HashSet<Class<? extends Service>>();
  private Set<Class<? extends Service>> excludeServices = new HashSet<Class<? extends Service>>();
  private Set<Class<? extends Service>> includeServices = new HashSet<Class<? extends Service>>();
  private Set<String> includeServicePackages = new HashSet<String>();
  private Set<String> excludeServicePackages = new HashSet<String>();

  public ServiceLoader add(ServiceLoader serviceLoader) {
    if (serviceLoader != null) {
      serviceLoader.build();
      this.services.addAll(serviceLoader.services);
    }
    return this;
  }


  /**
   * Add url mapping to service. The view p is serviceKey
   *
   * @param serviceClass Controller Class
   */
  public ServiceLoader add(Class<? extends Service> serviceClass) {
    services.add(serviceClass);
    return this;
  }

  public ServiceLoader addExcludeClasses(Class<? extends Service>... clazzes) {
    Collections.addAll(excludeServices, clazzes);
    return this;
  }

  public ServiceLoader addExcludeClasses(Set<Class<? extends Service>> clazzes) {
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

  public ServiceLoader addIncludeClasses(Class<? extends Service>... clazzes) {
    Collections.addAll(includeServices, clazzes);
    return this;
  }

  public ServiceLoader addIncludeClasses(Set<Class<? extends Service>> clazzes) {
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
        includeServices = ClassScaner.of(Service.class).includePackages(includeServicePackages).scan();
      } else {
        includeServices.addAll(ClassScaner.of(Service.class).includePackages(includeServicePackages).<Service>scan());
      }
    }
    boolean isExclude = false;
    if (includeServices.size() > 0) {
      for (Class service : includeServices) {
        isExclude = false;
        if (excludeServicePackages.size() > 0) {
          for (String excludepath : excludeServicePackages) {
            if (service.getName().startsWith(excludepath)) {
              logger.debug("Exclude service:" + service.getName());
              isExclude = true;
              break;
            }
          }
        }
        if (isExclude || excludeServices.contains(service)) {
          continue;
        }
        this.add(service);
        logger.info("Services.add(" + service.getName() + ")");
      }
    } else {
      logger.warn("Could not load any services.");
    }
  }

  public Set<Class<? extends Service>> getServices() {
    return services;
  }
}






