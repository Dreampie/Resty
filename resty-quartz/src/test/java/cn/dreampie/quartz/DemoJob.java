package cn.dreampie.quartz;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;
import java.util.Map;

/**
 * Created by ice on 14-11-28.
 */
public class DemoJob implements Job {
  public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
    Map data = jobExecutionContext.getJobDetail().getJobDataMap();
    System.out.println("hi," + data.get("name") + "," + new Date().getTime());
  }
}

