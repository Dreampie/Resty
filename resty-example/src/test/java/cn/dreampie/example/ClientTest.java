package cn.dreampie.example;

import cn.dreampie.client.Client;
import cn.dreampie.client.ClientRequest;
import cn.dreampie.client.HttpMethod;
import cn.dreampie.common.util.json.Jsoner;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

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
    client = new Client("http://127.0.0.1:8081/api/v1.0", "/sessions", "awesa", "asda", false);
  }

  @Test
  public void testLogin() {
    ClientRequest request = new ClientRequest("/sessions", HttpMethod.POST);
    request.addParameter("username", "asdasda").addParameter("password", "1232").addParameter("rememberMe", "true");
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
    request.addParameter("user", "{\"id\":\"1\",\"username\":\"k\"}");
    System.out.println(client.build(request).ask());
  }

  @Test
  public void testSave() {
    ClientRequest request = new ClientRequest("/users/1", HttpMethod.POST);
    request.addHeader("Content-Type", "application/json;charset=utf-8");
    request.setJsonParameter(Jsoner.toJSONString(
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
}
