package cn.dreampie.quartz;

import cn.dreampie.common.Plugin;
import cn.dreampie.common.util.properties.Proper;
import cn.dreampie.common.util.stream.Filer;
import cn.dreampie.log.Logger;
import cn.dreampie.quartz.job.QuartzCronJob;
import cn.dreampie.quartz.job.QuartzOnceJob;
import org.quartz.Scheduler;
import org.quartz.impl.StdSchedulerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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

  public boolean start() {
    try {
      //加载配置文件
      Properties configProp = Proper.use(config).getProperties();
      //实例化
      Quartzer.setSchedulerFactory(new StdSchedulerFactory(configProp));
      //获取Scheduler
      Scheduler sched = Quartzer.getSchedulerFactory().getScheduler();
      //内存,数据库的任务
      sched.start();
      //属性文件中的任务
      startPropertiesJobs();
      return true;
    } catch (Exception e) {
      throw new RuntimeException("Can't start quartz plugin.", e);
    }
  }

  public boolean stop() {
    try {
      Quartzer.getSchedulerFactory().getScheduler().shutdown();
      Quartzer.setSchedulerFactory(null);
      return true;
    } catch (Exception e) {
      throw new RuntimeException("Can't stop quartz plugin.", e);
    }
  }


  public void startPropertiesJobs() {
    if (Filer.exist(jobs)) {
      Properties jobsProp = Proper.use(jobs).getProperties();
      Enumeration enums = jobsProp.keys();

      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      List<String> startedJobs = new ArrayList<String>();
      String[] keyArr;
      String key, jobName, jobClassKey, groupKey, cronKey, onceKey, enable, group, jobCron, jobOnce, jobClassName;
      Class clazz;
      Date onceTime;
      while (enums.hasMoreElements()) {
        key = enums.nextElement() + "";
        if (!key.startsWith("job")) {
          continue;
        }

        keyArr = key.split("\\.");
        jobName = keyArr[1];
        //已经启动过的任务
        if (startedJobs.contains(jobName))
          continue;
        startedJobs.add(jobName);

        jobClassKey = key.replace(keyArr[2], "class");
        groupKey = key.replace(keyArr[2], "group");
        cronKey = key.replace(keyArr[2], "cron");
        onceKey = key.replace(keyArr[2], "once");
        enable = key.replace(keyArr[2], "enable");

        //判断任务是否启用
        if (!Boolean.valueOf(jobsProp.getProperty(enable))) {
          continue;
        }

        group = jobsProp.getProperty(groupKey);
        jobCron = jobsProp.getProperty(cronKey);
        jobOnce = jobsProp.getProperty(onceKey);
        jobClassName = jobsProp.getProperty(jobClassKey);

        try {
          clazz = Class.forName(jobClassName);
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
        //启动任务
        if (jobCron != null) {
          if (group != null) {
            new QuartzCronJob(group, keyArr[1], jobCron, clazz).start();
          } else {
            new QuartzCronJob(keyArr[1], jobCron, clazz).start();
          }
        } else if (jobOnce != null) {
          try {
            onceTime = sdf.parse(jobOnce);
          } catch (ParseException e) {
            throw new RuntimeException(e.getMessage(), e);
          }
          if (System.currentTimeMillis() <= onceTime.getTime()) {
            if (group != null) {
              new QuartzOnceJob(group, keyArr[1], onceTime, clazz).start();
            } else {
              new QuartzOnceJob(keyArr[1], onceTime, clazz).start();
            }
          }
        } else {
          new QuartzOnceJob(group, keyArr[1], new Date(), clazz).start();
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
