package cn.dreampie.client;

import cn.dreampie.common.util.Maper;
import cn.dreampie.common.util.json.Jsoner;
import org.junit.Test;

import static org.junit.Assert.*;

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
    postRequest.addParameter("test", Jsoner.toJSONString(Maper.of("a", "1")));
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

  }
}