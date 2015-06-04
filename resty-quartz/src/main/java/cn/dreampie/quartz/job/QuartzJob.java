package cn.dreampie.quartz.job;

import cn.dreampie.common.util.Maper;
import cn.dreampie.quartz.QuartzKey;
import cn.dreampie.quartz.Quartzer;
import cn.dreampie.quartz.exception.QuartzException;
import org.quartz.*;

import java.util.Date;
import java.util.Map;

import static org.quartz.JobBuilder.newJob;

/**
 * Created by wangrenhui on 14/11/29.
 */
public abstract class QuartzJob {
  protected static final String TRIGGER_MARK = "trigger";
  protected static final String GROUP_MARK = "group";
  protected static final String JOB_MARK = "job";
  protected static final String SEPARATOR = "_";
  protected QuartzKey quartzKey;
  protected Class<? extends Job> jobClass;
  protected Date scheduleTime;
  protected Map<String, Object> params = Maper.of();

  /**
   * 启动任务
   */
  public void start() {
    start(false);
  }

  /**
   * 强制启动任务
   *
   * @param force 是否强制启动，true 表示  如果存在相同的key值的任务时，停止任务，强制执行新任务
   */
  public abstract void start(boolean force);

  /**
   * 停止任务
   */
  public void stop() {
    long id = quartzKey.getId();
    String name = quartzKey.getName();
    String group = quartzKey.getGroup();
    SchedulerFactory factory = Quartzer.getSchedulerFactory();

    try {
      if (factory != null) {
        Scheduler scheduler = factory.getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(TRIGGER_MARK + SEPARATOR + name + SEPARATOR + id, GROUP_MARK + SEPARATOR + group + SEPARATOR + id);
        Trigger trigger = scheduler.getTrigger(triggerKey);
        if (trigger != null) {
          scheduler.pauseTrigger(triggerKey);
          scheduler.unscheduleJob(triggerKey);
          scheduler.deleteJob(trigger.getJobKey());
          Quartzer.removeQuartzJob(this);
        }
      }
    } catch (Exception e) {
      throw new QuartzException("Can't stop job.", e);
    }
  }

  /**
   * 暂停任务
   */
  public void pause() {
    long id = quartzKey.getId();
    String name = quartzKey.getName();
    String group = quartzKey.getGroup();
    SchedulerFactory factory = Quartzer.getSchedulerFactory();
    try {
      if (factory != null) {
        Scheduler scheduler = factory.getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(TRIGGER_MARK + SEPARATOR + name + SEPARATOR + id, GROUP_MARK + SEPARATOR + group + SEPARATOR + id);
        Trigger trigger = scheduler.getTrigger(triggerKey);
        if (trigger != null) {
          scheduler.pauseTrigger(triggerKey);
        }
      }
    } catch (Exception e) {
      throw new QuartzException("Can't pause job.", e);
    }
  }

  /**
   * 恢复任务
   */
  public void resume() {
    long id = quartzKey.getId();
    String name = quartzKey.getName();
    String group = quartzKey.getGroup();
    SchedulerFactory factory = Quartzer.getSchedulerFactory();
    try {
      if (factory != null) {
        Scheduler scheduler = factory.getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(TRIGGER_MARK + SEPARATOR + name + SEPARATOR + id, GROUP_MARK + SEPARATOR + group + SEPARATOR + id);
        Trigger trigger = scheduler.getTrigger(triggerKey);
        if (trigger != null) {
          scheduler.resumeJob(trigger.getJobKey());
        }
      }
    } catch (Exception e) {
      throw new QuartzException("Can't resume job.", e);
    }
  }


  public Trigger.TriggerState getState() {
    long id = quartzKey.getId();
    String name = quartzKey.getName();
    String group = quartzKey.getGroup();
    SchedulerFactory factory = Quartzer.getSchedulerFactory();
    Trigger.TriggerState triggerState = null;
    try {
      if (factory != null) {
        Scheduler scheduler = factory.getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(TRIGGER_MARK + SEPARATOR + name + SEPARATOR + id, GROUP_MARK + SEPARATOR + group + SEPARATOR + id);
        triggerState = scheduler.getTriggerState(triggerKey);
      }
    } catch (Exception e) {
      throw new QuartzException("Can't get job state.", e);
    }
    return triggerState;
  }


  protected JobDetail getJobDetail(long id, String name, String group) {
    // define the job and tie it to our HelloJob class
    JobDetail job = newJob(jobClass)
        .withIdentity(JOB_MARK + SEPARATOR + name + SEPARATOR + id, GROUP_MARK + SEPARATOR + group + SEPARATOR + id)
        .requestRecovery()
        .build();

    JobDataMap jobMap = job.getJobDataMap();
    jobMap.put("job_group", group);
    jobMap.put("job_name", name);
    jobMap.put("job_id", id);
    //添加参数
    if (params != null && params.size() > 0) {
      jobMap.putAll(params);
    }
    return job;
  }

  public QuartzKey getQuartzKey() {
    return quartzKey;
  }

  public void setQuartzKey(QuartzKey quartzKey) {
    this.quartzKey = quartzKey;
  }

  public Class<? extends Job> getJobClass() {
    return jobClass;
  }

  public void setJobClass(Class<? extends Job> jobClass) {
    this.jobClass = jobClass;
  }

  public Map<String, Object> getParams() {
    return params;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
  }

  public Date getScheduleTime() {
    return scheduleTime;
  }

  public void setScheduleTime(Date scheduleTime) {
    this.scheduleTime = scheduleTime;
  }

  public QuartzJob addParam(String key, Object value) {
    this.params.put(key, value);
    return this;
  }

  public QuartzJob addParams(Map<String, Object> values) {
    this.params.putAll(values);
    return this;
  }
}
