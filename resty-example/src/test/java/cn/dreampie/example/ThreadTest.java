package cn.dreampie.example;

import cn.dreampie.client.HttpClientRequest;
import cn.dreampie.client.HttpClient;

import java.util.concurrent.CountDownLatch;

/**
 * Created by wangrenhui on 15/2/2.
 */
public class ThreadTest {
  //  @Test
  public void testThread() {
    final int num = 300;
    final CountDownLatch begin = new CountDownLatch(1);
    final CountDownLatch end = new CountDownLatch(num);

    final HttpClient httpClient = new HttpClient("http://127.0.0.1:9090/api/v1.0");

    for (int i = 0; i < num; i++) {
      final int finalI = i;
      new Thread(new Runnable() {
        public void run() {
          try {
            System.out.println(finalI + " ready !");
            begin.await();
            // execute your logic
            HttpClientRequest request = new HttpClientRequest("/sessions");
            request.addParam("username", "testuser" + finalI).addParam("password", "123").addParam("rememberMe", "true");
            System.out.println(httpClient.build(request).post());

            Thread.sleep((long) (Math.random() * 10000));
          } catch (Throwable e) {
            e.printStackTrace();
          } finally {
            System.out.println(finalI + " 完成测试 !");
            end.countDown();
          }
        }
      }).start();
    }

    // 睡眠十秒
    try {
      Thread.sleep(10000);
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    }

    System.out.println("开始进行并发测试");
    begin.countDown();
    long startTime = System.currentTimeMillis();

    try {
      end.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    } finally {
      long endTime = System.currentTimeMillis();
      System.out.println("结束并发测试 !");
      System.out.println("花费时间: " + (endTime - startTime));
    }

  }
}
