package cn.dreampie.client;

import cn.dreampie.common.util.Maper;
import cn.dreampie.common.util.json.Jsoner;
import org.junit.Before;
import org.junit.Test;

public class ClientTest {

  private Client client;

  @Before
  public void setUp() throws Exception {
    //启动resty-example项目，即可测试客户端
    String apiUrl = "http://localhost:8081/api/v1.0";
    //如果不需要 使用账号登陆
    //client = new Client(apiUrl);
    //如果有账号权限限制  需要登陆
    client = new Client(apiUrl, "/tests/login", "u", "123");
  }

  @Test
  public void testAsk() {
    //login  auth
    ClientRequest authRequest = new ClientRequest("/users", HttpMethod.GET);
    ResponseData authResult = client.build(authRequest).ask();
    System.out.println(authResult.getData());

    //get
    ClientRequest getRequest = new ClientRequest("/tests", HttpMethod.GET);
    ResponseData getResult = client.build(getRequest).ask();
    System.out.println(getResult.getData());

    //post
    ClientRequest postRequest = new ClientRequest("/tests", HttpMethod.POST);
    postRequest.addParameter("test", Jsoner.toJSONString(Maper.of("a", "谔谔")));
    ResponseData postResult = client.build(postRequest).ask();
    System.out.println(postResult.getData());

    //put
    ClientRequest putRequest = new ClientRequest("/tests/x", HttpMethod.PUT);
    ResponseData putResult = client.build(putRequest).ask();
    System.out.println(putResult.getData());


    //delete
    ClientRequest deleteRequest = new ClientRequest("/tests/a", HttpMethod.DELETE);
    ResponseData deleteResult = client.build(deleteRequest).ask();
    System.out.println(deleteResult.getData());


    //upload
    ClientRequest uploadRequest = new ClientRequest("/tests/resty", HttpMethod.POST);
    uploadRequest.addUploadFiles("resty", ClientTest.class.getResource("/resty.jar").getFile());
    uploadRequest.addParameter("des", "test file  paras  测试笔");
    ResponseData uploadResult = client.build(uploadRequest).ask();
    System.out.println(uploadResult.getData());


    //download  支持断点续传
    ClientRequest downloadRequest = new ClientRequest("/tests/file", HttpMethod.GET);
    downloadRequest.setDownloadFile(ClientTest.class.getResource("/resty.jar").getFile().replace(".jar", "x.jar"));
    ResponseData downloadResult = client.build(downloadRequest).ask();
    System.out.println(downloadResult.getData());

  }
}