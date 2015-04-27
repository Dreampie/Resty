package cn.dreampie.example;

import cn.dreampie.client.Client;
import cn.dreampie.client.ClientRequest;
import cn.dreampie.client.HttpMethod;
import cn.dreampie.client.ResponseData;
import cn.dreampie.common.util.json.Jsoner;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ice on 15-1-4.
 */
@FixMethodOrder(MethodSorters.JVM)
public class ClientTest {

  private Client client;

  @Before
  public void setUp() throws Exception {
    client = new Client("http://127.0.0.1:9090/api/v1.0", "/sessions", "awesa", "123", false);
  }

  @Test
  public void testLogin() {
    ClientRequest request = new ClientRequest("/sessions", HttpMethod.POST);
    request.addParam("username", "asdasda").addParam("password", "123").addParam("rememberMe", "true");
    System.out.println(client.build(request).ask());
  }

  @Test
  public void testLogut() {
    ClientRequest request = new ClientRequest("/sessions", HttpMethod.DELETE);
    System.out.println(client.build(request).ask());
  }

  @Test
  public void testDelete() {
    ClientRequest request = new ClientRequest("/users/1", HttpMethod.DELETE);
    System.out.println(client.build(request).ask());
  }

  @Test
  public void testUpdate() {
    ClientRequest request = new ClientRequest("/users", HttpMethod.PUT);
    request.addParam("user", "{\"id\":\"1\",\"username\":\"k\"}");
    System.out.println(client.build(request).ask());
  }

  @Test
  public void testTransaction() {
    ClientRequest request = new ClientRequest("/users/transactions", HttpMethod.GET);
    System.out.println(client.build(request).ask());
  }

  @Test
  public void testUpload() {
    //upload
    ClientRequest uploadRequest = new ClientRequest("/tests/测试", HttpMethod.POST);
    uploadRequest.addUploadFile("testfile", ClientTest.class.getResource("/resty.jar").getFile());
    uploadRequest.addParam("des", "test file  paras  测试笔");
    ResponseData uploadResult = client.build(uploadRequest).ask();
    System.out.println(uploadResult.getData());
  }

  @Test
  public void testDownload() {
    //download  支持断点续传
    ClientRequest downloadRequest = new ClientRequest("/tests/file", HttpMethod.GET);
    downloadRequest.setDownloadFile(ClientTest.class.getResource("/").getFile(), false);
    ResponseData downloadResult = client.build(downloadRequest).ask();
    System.out.println(downloadResult.getData());
  }

  @Test
  public void testSave() {
    ClientRequest request = new ClientRequest("/users/1", HttpMethod.POST);
    request.setJsonParam(Jsoner.toJSON(
        new HashMap<String, Object>() {
          {
            put("users", new ArrayList<Map>() {
              {
                add(new HashMap<String, String>() {{
                  put("sid", "1");
                  put("username", "test1");
                  put("providername", "test1");
                  put("password", "123456");
                  put("created_at", "2014-10-11 10:09:12");
                }});

                add(new HashMap<String, String>() {{
                  put("sid", "2");
                  put("username", "test2");
                  put("providername", "tes2");
                  put("password", "123456");
                  put("created_at", "2014-10-12 10:09:12");
                }});
              }
            });
          }
        }
    ));
    System.out.println(client.build(request).ask());
  }


  @Test
  public void testConnection() throws Exception {
    URL url = new URL("https://www.baidu.com/");

    URLConnection rulConnection = url.openConnection();
    // 此处的urlConnection对象实际上是根据URL的
    // 请求协议(此处是http)生成的URLConnection类
    // 的子类HttpURLConnection,故此处最好将其转化
    // 为HttpURLConnection类型的对象,以便用到
    // HttpURLConnection更多的API.如下:

    HttpURLConnection httpUrlConnection = (HttpURLConnection) rulConnection;

    // 设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在
    // http正文内，因此需要设为true, 默认情况下是false;
    httpUrlConnection.setDoOutput(true);

    // 设置是否从httpUrlConnection读入，默认情况下是true;
//    httpUrlConnection.setDoInput(true);

    // Post 请求不能使用缓存
    httpUrlConnection.setUseCaches(false);

    // 设定传送的内容类型是可序列化的java对象
    // (如果不设此项,在传送序列化对象时,当WEB服务默认的不是这种类型时可能抛java.io.EOFException)
//    httpUrlConnection.setRequestProperty("Content-type", "application/x-java-serialized-object");

    // 设定请求的方法为"POST"，默认是GET
//    httpUrlConnection.setRequestMethod("POST");

    // 连接，从上述第2条中url.openConnection()至此的配置必须要在connect之前完成，
    httpUrlConnection.connect();

    // 此处getOutputStream会隐含的进行connect(即：如同调用上面的connect()方法，
    // 所以在开发中不调用上述的connect()也可以)。
    OutputStream outStrm = httpUrlConnection.getOutputStream();
    System.out.println(outStrm);
  }
}
