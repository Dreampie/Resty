package cn.dreampie.quartz;

import cn.dreampie.common.util.Lister;
import cn.dreampie.quartz.job.QuartzJob;
import org.quartz.SchedulerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by wangrenhui on 14-4-21.
 */
public class Quartzer {

  private static SchedulerFactory schedulerFactory;

  private static List<QuartzJob> quartzJobs = Lister.of();

  private static Map<String, Long> quartzKeys = new ConcurrentHashMap<String, Long>();

  private Quartzer() {
  }

  public static QuartzKey nextKey(String name) {
    return nextKey(QuartzKey.DEFAULT_GROUP, name);
  }

  public static QuartzKey nextKey(String group, String name) {
    Long id = quartzKeys.get(group + "." + name);
    if (id == null) {
      id = 1L;
    } else {
      id++;
    }
    quartzKeys.put(group + "." + name, id);
    return new QuartzKey(id, group, name);
  }

  public static QuartzJob getJob(QuartzKey quartzKey) {
    for (QuartzJob quartzJob : quartzJobs) {
      if (quartzJob.getQuartzKey().equals(quartzKey)) {
        return quartzJob;
      }
    }
    return null;
  }

  public static void stopJob(QuartzKey quartzKey) {
    for (QuartzJob quartzJob : quartzJobs) {
      if (quartzJob.getQuartzKey().equals(quartzKey)) {
        quartzJob.stop();
      }
    }
  }

  public static void pauseJob(QuartzKey quartzKey) {
    for (QuartzJob quartzJob : quartzJobs) {
      if (quartzJob.getQuartzKey().equals(quartzKey)) {
        quartzJob.pause();
      }
    }
  }

  public static void resumeJob(QuartzKey quartzKey) {
    for (QuartzJob quartzJob : quartzJobs) {
      if (quartzJob.getQuartzKey().equals(quartzKey)) {
        quartzJob.resume();
      }
    }
  }


  public static SchedulerFactory getSchedulerFactory() {
    return schedulerFactory;
  }

  public static void setSchedulerFactory(SchedulerFactory schedulerFactory) {
    Quartzer.schedulerFactory = schedulerFactory;
  }

  public static List<QuartzJob> getQuartzJobs() {
    return quartzJobs;
  }

  public static void setQuartzJobs(List<QuartzJob> quartzJobs) {
    Quartzer.quartzJobs = quartzJobs;
  }

  public static void addQuartzJob(QuartzJob startedJob) {
    Quartzer.quartzJobs.add(startedJob);
  }

  public static void removeQuartzJob(QuartzJob startedJob) {
    Quartzer.quartzJobs.remove(startedJob);
  }
}