package cn.dreampie.quartz.job;

import cn.dreampie.common.util.Maper;
import cn.dreampie.quartz.QuartzKey;
import cn.dreampie.quartz.QuartzKit;
import org.quartz.*;

import java.util.Date;
import java.util.Map;

/**
 * Created by wangrenhui on 14/11/29.
 */
public abstract class QuartzJob {
  protected QuartzKey quartzKey;
  protected JobState state;//started,stoped,paused
  protected Class<? extends Job> jobClass;
  protected Date scheduleTime;
  protected Map<String, Object> params = Maper.of();

  protected static final String TRIGGER_MARK = "trigger";
  protected static final String GROUP_MARK = "group";
  protected static final String JOB_MARK = "job";
  protected static final String SEPARATOR = "_";

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
    SchedulerFactory factory = QuartzKit.getSchedulerFactory();

    try {
      if (factory != null) {
        Scheduler scheduler = factory.getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(TRIGGER_MARK + SEPARATOR + name + SEPARATOR + id, GROUP_MARK + SEPARATOR + group + SEPARATOR + id);
        Trigger trigger = scheduler.getTrigger(triggerKey);
        if (trigger != null) {
          scheduler.pauseTrigger(triggerKey);
          scheduler.unscheduleJob(triggerKey);
          scheduler.deleteJob(trigger.getJobKey());
          this.state = JobState.STOPED;
          QuartzKit.removeQuartzJob(this);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Can't stop job.", e);
    }
  }

  /**
   * 暂停任务
   */
  public void pause() {
    long id = quartzKey.getId();
    String name = quartzKey.getName();
    String group = quartzKey.getGroup();
    SchedulerFactory factory = QuartzKit.getSchedulerFactory();
    try {
      if (factory != null) {
        Scheduler scheduler = factory.getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(TRIGGER_MARK + SEPARATOR + name + SEPARATOR + id, GROUP_MARK + SEPARATOR + group + SEPARATOR + id);
        Trigger trigger = scheduler.getTrigger(triggerKey);
        if (trigger != null) {
          scheduler.pauseTrigger(triggerKey);
          this.state = JobState.PAUSED;
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Can't pause job.", e);
    }
  }

  /**
   * 恢复任务
   */
  public void resume() {
    long id = quartzKey.getId();
    String name = quartzKey.getName();
    String group = quartzKey.getGroup();
    SchedulerFactory factory = QuartzKit.getSchedulerFactory();
    try {
      if (factory != null) {
        Scheduler scheduler = factory.getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(TRIGGER_MARK + SEPARATOR + name + SEPARATOR + id, GROUP_MARK + SEPARATOR + group + SEPARATOR + id);
        Trigger trigger = scheduler.getTrigger(triggerKey);
        if (trigger != null) {
          scheduler.resumeJob(trigger.getJobKey());
          this.state = JobState.RESUMED;
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Can't resume job.", e);
    }
  }

  public QuartzKey getQuartzKey() {
    return quartzKey;
  }

  public void setQuartzKey(QuartzKey quartzKey) {
    this.quartzKey = quartzKey;
  }

  public JobState getState() {
    return state;
  }

  public void setState(JobState state) {
    this.state = state;
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

  public Date getScheduleTime() {
    return scheduleTime;
  }

  public void setScheduleTime(Date scheduleTime) {
    this.scheduleTime = scheduleTime;
  }

  public void setParams(Map<String, Object> params) {
    this.params = params;
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
