package cn.dreampie.quartz;

import cn.dreampie.common.Plugin;
import cn.dreampie.common.util.properties.Proper;
import cn.dreampie.log.Logger;
import cn.dreampie.quartz.job.QuartzCronJob;
import cn.dreampie.quartz.job.QuartzOnceJob;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Created by wangrenhui on 14-4-21.
 */
public class QuartzPlugin implements Plugin {

  private static final Logger logger = Logger.getLogger(QuartzPlugin.class);
  /**
   * 默认配置文件*
   */
  private String config = "quartz/quartz.properties";

  private String jobs = "quartz/jobs.properties";


  public QuartzPlugin() {

  }

  public QuartzPlugin(String config) {
    this.config = config;
  }

  public QuartzPlugin(String config, String jobs) {
    this.config = config;
    this.jobs = jobs;
  }

  @Override
  public boolean start() {
    try {
      //加载配置文件
      Properties configProp = Proper.use(config).getProperties();
      //实例化
      QuartzKit.setSchedulerFactory(new StdSchedulerFactory(configProp));
      //获取Scheduler
      Scheduler sched = QuartzKit.getSchedulerFactory().getScheduler();
      //内存,数据库的任务
      sched.start();
      //属性文件中的任务
      startPropertiesJobs();
      return true;
    } catch (Exception e) {
      throw new RuntimeException("Can't start quartz plugin.", e);
    }
  }

  @Override
  public boolean stop() {
    try {
      QuartzKit.getSchedulerFactory().getScheduler().shutdown();
      QuartzKit.setSchedulerFactory(null);
      return true;
    } catch (Exception e) {
      throw new RuntimeException("Can't stop quartz plugin.", e);
    }
  }


  public void startPropertiesJobs() {
    if (new File(jobs).exists()) {
      Properties jobsProp = Proper.use(jobs).getProperties();
      Enumeration enums = jobsProp.keys();

      while (enums.hasMoreElements()) {
        String key = enums.nextElement() + "";
        if (!key.startsWith("job")) {
          continue;
        }

        String[] keyArr = key.split("\\.");


        String jobClassKey = key.replace(keyArr[2], "class");
        String groupKey = key.replace(keyArr[2], "group");
        String cronKey = key.replace(keyArr[2], "cron");
        String onceKey = key.replace(keyArr[2], "once");
        String enable = key.replace(keyArr[2], "enable");

        //判断任务是否启用
        if (!Boolean.valueOf(jobsProp.getProperty(enable))) {
          continue;
        }

        String group = jobsProp.getProperty(groupKey);
        String jobCron = jobsProp.getProperty(cronKey);
        String jobOnce = jobsProp.getProperty(onceKey);
        String jobClassName = jobsProp.getProperty(jobClassKey);
        Class clazz;
        try {
          clazz = Class.forName(jobClassName);
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
        //启动任务
        if (jobCron != null) {
          if (group != null) {
            new QuartzCronJob(group, keyArr[1], jobCron, clazz).start();
          } else {
            new QuartzCronJob(keyArr[1], jobCron, clazz).start();
          }
        } else if (jobOnce != null) {
          SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

          try {
            if (group != null) {
              new QuartzOnceJob(group, keyArr[1], sdf.parse(jobOnce), clazz).start();
            } else {
              new QuartzOnceJob(keyArr[1], sdf.parse(jobOnce), clazz).start();
            }
          } catch (ParseException e) {
            throw new RuntimeException(e);
          }
        } else {
          throw new RuntimeException("This job must has cron or once attribute " + keyArr[1]);
        }
      }
    }
  }

  public String getConfig() {
    return config;
  }

  public void setConfig(String config) {
    this.config = config;
  }

  public String getJobs() {
    return jobs;
  }

  public void setJobs(String jobs) {
    this.jobs = jobs;
  }

}
