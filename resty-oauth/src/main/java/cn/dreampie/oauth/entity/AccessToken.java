package cn.dreampie.oauth.entity;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * Created by Dreampie on 16/7/7.
 */
public class AccessToken {
  private String accessToken;
  private int expiresIn;
  private String refreshToken;
  private String openID;
  private String scope;

  public AccessToken(String accessToken, int expiresIn, String refreshToken, String openID, String scope) {
    this.accessToken = accessToken;
    this.expiresIn = expiresIn;
    this.refreshToken = refreshToken;
    this.openID = openID;
    this.scope = scope;
  }


  @JSONField(name = "access_token")
  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  @JSONField(name = "expires_in")
  public int getExpiresIn() {
    return expiresIn;
  }

  public void setExpiresIn(int expiresIn) {
    this.expiresIn = expiresIn;
  }

  @JSONField(name = "refresh_token")
  public String getRefreshToken() {
    return refreshToken;
  }

  public void setRefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  @JSONField(name = "open_id")
  public String getOpenID() {
    return openID;
  }

  public void setOpenID(String openID) {
    this.openID = openID;
  }

  @JSONField(name = "scope")
  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }
}
