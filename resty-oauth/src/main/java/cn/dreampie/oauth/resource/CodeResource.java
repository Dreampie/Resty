package cn.dreampie.oauth.resource;

import cn.dreampie.cache.CacheProvider;
import cn.dreampie.cache.SimpleCache;
import cn.dreampie.common.Constant;
import cn.dreampie.common.entity.Entity;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.common.http.result.WebResult;
import cn.dreampie.common.util.Maper;
import cn.dreampie.log.Logger;
import cn.dreampie.oauth.Authorizes;
import cn.dreampie.oauth.entity.Client;
import cn.dreampie.oauth.entity.Code;
import cn.dreampie.oauth.entity.Scope;
import cn.dreampie.oauth.exception.OAuthException;
import cn.dreampie.orm.generate.UUIDGenerator;
import cn.dreampie.route.annotation.API;
import cn.dreampie.route.annotation.GET;
import cn.dreampie.route.annotation.POST;
import cn.dreampie.route.core.Resource;
import cn.dreampie.security.Sessions;
import cn.dreampie.security.Subject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Dreampie on 16/7/7.
 */
@API("/codes")
public class CodeResource extends Resource {
  private static final Logger logger = Logger.getLogger(CodeResource.class);

  @GET
  public WebResult toSignIn(String key, String response_type, String state) {
    String redirectParam = "?key=" + key + "&response_type=" + response_type + "&state=" + state;
    if (!response_type.equals("code")) {
      return new WebResult(HttpStatus.FOUND, Maper.<String, String>of("location", Constant.oauthErrorUrl + redirectParam + "&error=response_type_not_match"));
    }
    Client client = Client.DAO.findFirstBy("key=?", key);
    if (client == null) {
      return new WebResult(HttpStatus.FOUND, Maper.<String, String>of("location", Constant.oauthErrorUrl + redirectParam + "&error=client_not_found"));
    } else {
      return new WebResult(HttpStatus.FOUND, Maper.<String, String>of("location", Constant.oauthSigninUrl + redirectParam));
    }
  }

  /**
   * @param client={"key":"xx","scopeIds":[xx,xx]}
   * @param response_type
   * @param state
   * @return
   */
  @POST
  public WebResult getCode(Client client, String response_type, String state) {
    String redirectParam = "?key=" + client.get("key") + "&response_type=" + response_type + "&state=" + state;
    if (!response_type.equals("code")) {
      return new WebResult(HttpStatus.FOUND, Maper.<String, String>of("location", Constant.oauthErrorUrl + redirectParam + "&error=response_type_not_match"));
    }
    Client oldClient = Client.DAO.findFirstBy("key=?", client.get("key"));
    if (oldClient == null) {
      return new WebResult(HttpStatus.FOUND, Maper.<String, String>of("location", Constant.oauthErrorUrl + redirectParam + "&error=client_not_found"));
    } else {
      Set<Scope> scopes = new HashSet<Scope>();
      for (Scope scope : oldClient.getScopes()) {
        if (scope.<Integer>get("selected") == 1 || Arrays.asList(client.getScopeIds()).contains(scope.<Integer>get("id"))) {
          scopes.add(scope);
        }
      }
      Entity<?> user = Subject.getPrincipal().getModel();
      int userId = user.<Integer>get("id");
      Code code = new Code(UUID.randomUUID().toString().replaceAll("-", ""), oldClient.<Integer>get("id"), userId, Constant.oauthExpires, scopes);
      Authorizes.addCode(code);//cache code
      return new WebResult(HttpStatus.FOUND, Maper.<String, String>of("location", oldClient.get("redirect_uri") + "?code=" + code.getCode() + "&state=" + state));
    }
  }


}
