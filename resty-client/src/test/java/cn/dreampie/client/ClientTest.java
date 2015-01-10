package cn.dreampie.client;

import cn.dreampie.common.util.Maper;
import cn.dreampie.common.util.json.Jsoner;
import org.junit.Test;

public class ClientTest {

  @Test
  public void ask() {
    String basUrl = "http://localhost:8081/api/v1.0";

    //get
    ClientRequest getRequest = new ClientRequest(basUrl + "/tests", HttpMethod.GET);
    Client getClient = Client.newInstance(getRequest);
    String getResult = getClient.ask();
    System.out.println(getResult);

    //post
    ClientRequest postRequest = new ClientRequest(basUrl + "/tests", HttpMethod.POST);
    postRequest.addParameter("test", Jsoner.toJSONString(Maper.of("a", "谔谔")));
    Client postClient = Client.newInstance(postRequest);
    String postResult = postClient.ask();
    System.out.println(postResult);

    //put
    ClientRequest putRequest = new ClientRequest(basUrl + "/tests/x", HttpMethod.PUT);
//    putRequest.addParameter("b", "x");
    Client putClient = Client.newInstance(putRequest);
    String putResult = putClient.ask();
    System.out.println(putResult);


    //delete
    ClientRequest deleteRequest = new ClientRequest(basUrl + "/tests/a", HttpMethod.DELETE);
    Client deleteClient = Client.newInstance(deleteRequest);
    String deleteResult = deleteClient.ask();
    System.out.println(deleteResult);


    //upload
    ClientRequest uploadRequest = new ClientRequest(basUrl + "/tests/resty", HttpMethod.POST);
    uploadRequest.addUploadFiles("resty", ClientTest.class.getResource("/resty.jar").getFile());
    Client uploadClient = Client.newInstance(uploadRequest);
    String uploadResult = uploadClient.ask();
    System.out.println(uploadResult);


    //download  支持断点续传
    ClientRequest downloadRequest = new ClientRequest(basUrl + "/tests/file", HttpMethod.GET);
    downloadRequest.setDownloadFile(ClientTest.class.getResource("/resty.jar").getFile().replace(".jar", "x.jar"));
    Client downloadClient = Client.newInstance(downloadRequest);
    String downloadResult = downloadClient.ask();
    System.out.println(downloadResult);

  }
}