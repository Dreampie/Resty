package cn.dreampie.quartz;

import cn.dreampie.quartz.job.QuartzCronJob;
import cn.dreampie.quartz.job.QuartzOnceJob;
import org.junit.Test;

import java.util.Date;

public class QuartzTest {

  @org.junit.Before
  public void setUp() throws Exception {
    QuartzPlugin quartzPlugin = new QuartzPlugin();
    quartzPlugin.start();
  }

  @Test
  public void testQuartz() throws Exception {


    new QuartzCronJob("test", "*/5 * * * * ?", DemoJob.class).addParam("name", "quartz1").start();

    new QuartzOnceJob("test", new Date(), DemoJob.class).addParam("name", "quartz2").start();
    new QuartzOnceJob("test", new Date(), DemoJob.class).addParam("name", "quartz3").start();
//    Thread.sleep(10000);
//    QuartzKit.stopJob(quartzKey);

    Thread.sleep(5000);
  }

}