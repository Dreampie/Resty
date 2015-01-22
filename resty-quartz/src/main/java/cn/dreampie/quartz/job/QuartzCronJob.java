package cn.dreampie.quartz.job;

import cn.dreampie.quartz.QuartzKey;
import cn.dreampie.quartz.QuartzKit;
import org.quartz.*;

import java.util.Map;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by wangrenhui on 14/11/29.
 */
public class QuartzCronJob extends QuartzJob {

  private String cron;

  public QuartzCronJob(String name, String cron, Class<? extends Job> jobClass) {
    this(QuartzKit.nextKey(name), cron, jobClass);
  }

  public QuartzCronJob(String group, String name, String cron, Class<? extends Job> jobClass) {
    this(QuartzKit.nextKey(group, name), cron, jobClass);
  }

  public QuartzCronJob(QuartzKey quartzKey, String cron, Class<? extends Job> jobClass) {
    this.quartzKey = quartzKey;
    this.state = JobState.INITED;
    this.cron = cron;
    this.jobClass = jobClass;
  }

  /**
   *
   * @param force 是否强制启动，true 表示  如果存在相同的key值的任务时，停止任务，强制执行新任务
   */
  public void start(boolean force) {

    QuartzJob quartzJob = QuartzKit.getJob(quartzKey);
    if (quartzJob != null) {
      if (force) {
        quartzJob.stop();
      } else {
        return;
      }
    }

    long id = quartzKey.getId();
    String name = quartzKey.getName();
    String group = quartzKey.getGroup();
    SchedulerFactory factory = QuartzKit.getSchedulerFactory();
    try {
      if (factory != null) {
        Scheduler sched = factory.getScheduler();
        // define the job and tie it to our HelloJob class
        JobDetail job = newJob(jobClass)
            .withIdentity(JOB_MARK + SEPARATOR + name + SEPARATOR + id, GROUP_MARK + SEPARATOR + group + SEPARATOR + id)
            .requestRecovery()
            .build();

        Map jobMap = job.getJobDataMap();
        jobMap.put(group + SEPARATOR + name, id);
        //添加参数
        if (params != null && params.size() > 0)
          jobMap.putAll(params);

        // 执行表达式
        CronTrigger trigger = newTrigger()
            .withIdentity(TRIGGER_MARK + SEPARATOR + name + SEPARATOR + id, GROUP_MARK + SEPARATOR + group + SEPARATOR + id)
            .withSchedule(cronSchedule(this.cron)).build();

        this.scheduleTime = sched.scheduleJob(job, trigger);
        sched.start();
        this.state = JobState.STARTED;
        QuartzKit.addQuartzJob(this);
      }
    } catch (Exception e) {
      throw new RuntimeException("Can't start cron job.", e);
    }

  }

  public String getCron() {
    return cron;
  }

  public void setCron(String cron) {
    this.cron = cron;
  }
}
