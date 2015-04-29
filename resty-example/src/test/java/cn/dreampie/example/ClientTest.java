package cn.dreampie.example;

import cn.dreampie.client.Client;
import cn.dreampie.client.ClientRequest;
import cn.dreampie.client.ResponseData;
import cn.dreampie.common.http.HttpMethods;
import cn.dreampie.common.util.json.Jsoner;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by ice on 15-1-4.
 */
@FixMethodOrder(MethodSorters.JVM)
public class ClientTest {

  private Client client;

  @Before
  public void setUp() throws Exception {
    client = new Client("http://localhost:9090/api/v1.0", "/sessions", "awesa", "123", false);
  }

  @Test
  public void testLogin() {
    ClientRequest request = new ClientRequest("/sessions", HttpMethods.POST);
    request.addParam("username", "asdasda").addParam("password", "123").addParam("rememberMe", "true");
    System.out.println(client.build(request).ask());
  }

  @Test
  public void testLogut() {
    ClientRequest request = new ClientRequest("/sessions", HttpMethods.DELETE);
    System.out.println(client.build(request).ask());
  }

  @Test
  public void testDelete() {
    ClientRequest request = new ClientRequest("/users/1", HttpMethods.DELETE);
    System.out.println(client.build(request).ask());
  }

  @Test
  public void testUpdate() {
    ClientRequest request = new ClientRequest("/users", HttpMethods.PUT);
    request.addParam("user", "{\"id\":\"1\",\"username\":\"k\"}");
    System.out.println(client.build(request).ask());
  }

  @Test
  public void testTransaction() {
    ClientRequest request = new ClientRequest("/users/transactions", HttpMethods.GET);
    System.out.println(client.build(request).ask());
  }

  @Test
  public void testUpload() {
    //upload
    ClientRequest uploadRequest = new ClientRequest("/tests/测试", HttpMethods.POST);
    uploadRequest.addUploadFile("testfile", ClientTest.class.getResource("/resty.jar").getFile());
    uploadRequest.addParam("des", "test file  paras  测试笔");
    ResponseData uploadResult = client.build(uploadRequest).ask();
    System.out.println(uploadResult.getData());
  }

  @Test
  public void testDownload() {
    //download  支持断点续传
    ClientRequest downloadRequest = new ClientRequest("/tests/file", HttpMethods.GET);
    downloadRequest.setDownloadFile(ClientTest.class.getResource("/").getFile(), false);
    ResponseData downloadResult = client.build(downloadRequest).ask();
    System.out.println(downloadResult.getData());
  }

  @Test
  public void testSave() {
    ClientRequest request = new ClientRequest("/users/1", HttpMethods.POST);
    String json = Jsoner.toJSON(
//        new HashMap<String, Object>() {
//          {
//            put("users",
        new HashSet() {
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
        }
    );
    request.setJsonParam(json);
    System.out.println(client.build(request).ask());
  }

}
