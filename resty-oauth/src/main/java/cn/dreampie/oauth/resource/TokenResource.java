package cn.dreampie.oauth.resource;

import cn.dreampie.common.Constant;
import cn.dreampie.common.entity.Entity;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.common.http.result.WebResult;
import cn.dreampie.common.util.Joiner;
import cn.dreampie.common.util.Maper;
import cn.dreampie.log.Logger;
import cn.dreampie.oauth.Authorizes;
import cn.dreampie.oauth.entity.*;
import cn.dreampie.route.annotation.API;
import cn.dreampie.route.annotation.GET;
import cn.dreampie.route.annotation.POST;
import cn.dreampie.route.annotation.PUT;
import cn.dreampie.route.core.Resource;
import cn.dreampie.security.Subject;

import java.util.*;

/**
 * Created by Dreampie on 16/7/7.
 */
@API("/tokens")
public class TokenResource extends Resource {
  private static final Logger logger = Logger.getLogger(TokenResource.class);

  /**
   * @param client={"key":"xx","secret":"xx"}&code=xx&grant_type
   * @param code
   * @param grant_type
   * @return
   */
  @POST
  public WebResult getToken(Client client, String code, String grant_type) {
    String redirectParam = "?key=" + client.get("key") + "&code=" + code + "&grant_type=" + grant_type;
    if (!grant_type.equals("authorization_code")) {
      return new WebResult(HttpStatus.FOUND, Maper.<String, String>of("location", Constant.oauthErrorUrl + redirectParam + "&error=grant_type_not_match"));
    }
    Client oldClient = Client.DAO.findFirstBy("key=? AND secret=?", client.get("key"), client.get("secret"));

    Code c = Authorizes.getCode(code);
    if (oldClient == null || c == null) {
      return new WebResult(HttpStatus.FOUND, Maper.<String, String>of("location", Constant.oauthErrorUrl + redirectParam + "&error=client_or_code_not_found"));
    } else {
      //判断账号是否支持该授权方式
      if (!Arrays.asList(oldClient.getGrant().split(",")).contains(grant_type)) {
        return new WebResult(HttpStatus.FOUND, Maper.<String, String>of("location", Constant.oauthErrorUrl + redirectParam + "&error=grant_type_not_support"));
      }

      Token token = new Token(c);
      Authorizes.addToken(token);
      OpenID openID = OpenID.DAO.findFirstBy("user_id=? AND client_id=?", token.getUserId(), token.getClientId());
      if (openID == null) {
        OpenID newOpenID = new OpenID();
        newOpenID.set("user_id", token.getUserId()).set("client_id", token.getClientId()).set("open_id", UUID.randomUUID().toString().replaceAll("-", ""))
            .set("created_at", new Date()).set("creater_id", token.getUserId()).save();
      }
      String refreshToken = UUID.randomUUID().toString().replaceAll("-", "");
      Authorizes.addRefreshToken(refreshToken, token);
      AccessToken accessToken = new AccessToken(token.getToken(), token.getExpires(), refreshToken, openID.<String>get("open_id"), token.getScope());
      return new WebResult(HttpStatus.OK, accessToken);
    }
  }

  @PUT
  public WebResult refreshToken(String refresh_token, String grant_type) {
    String redirectParam = "?refresh_token=" + refresh_token + "&grant_type=" + grant_type;
    if (!grant_type.equals("refresh_token")) {
      return new WebResult(HttpStatus.FOUND, Maper.<String, String>of("location", Constant.oauthErrorUrl + redirectParam + "&error=grant_type_not_match"));
    }

    Token token = Authorizes.getTokenByRefreshToken(refresh_token);

    if (token == null) {
      return new WebResult(HttpStatus.FOUND, Maper.<String, String>of("location", Constant.oauthErrorUrl + redirectParam + "&error=token_not_found"));
    } else {
      Client oldClient = Client.DAO.findById(token.getClientId());
      //判断账号是否支持该授权方式
      if (!Arrays.asList(oldClient.getGrant().split(",")).contains(grant_type)) {
        return new WebResult(HttpStatus.FOUND, Maper.<String, String>of("location", Constant.oauthErrorUrl + redirectParam + "&error=grant_type_not_support"));
      }
      OpenID openID = OpenID.DAO.findFirstBy("user_id=? AND client_id=?", token.getUserId(), token.getClientId());

      AccessToken accessToken = new AccessToken(token.getToken(), token.getExpires(), null, openID.<String>get("open_id"), token.getScope());
      return new WebResult(HttpStatus.OK, accessToken);
    }
  }
}
