package cn.dreampie.oauth.entity;

import cn.dreampie.common.Constant;
import cn.dreampie.common.util.Joiner;
import cn.dreampie.orm.Model;
import cn.dreampie.orm.annotation.Table;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Created by Dreampie on 16/7/7.
 * 授权token
 */
public class Token implements Serializable {
  private String token;
  private Integer clientId;
  private Integer userId;
  private int expires;
  private Date expiredAt;
  private Set<Scope> scopes;
  private String scope;

  public Token(Code code) {
    this(UUID.randomUUID().toString().replaceAll("-", ""), code.getClientId(), code.getUserId(), code.getExpires(), code.getScopes(), code.getScope());
  }

  public Token(String token, Integer clientId, Integer userId, int expires, Set<Scope> scopes, String scope) {
    this.token = token;
    this.clientId = clientId;
    this.userId = userId;
    this.expires = expires;
    this.expiredAt = new Date(new Date().getTime() + expires);
    this.scopes = scopes;
    this.scope = scope;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Integer getClientId() {
    return clientId;
  }

  public void setClientId(Integer clientId) {
    this.clientId = clientId;
  }

  public Integer getUserId() {
    return userId;
  }

  public void setUserId(Integer userId) {
    this.userId = userId;
  }

  public int getExpires() {
    return expires;
  }

  public void setExpires(int expires) {
    this.expires = expires;
  }

  public Date getExpiredAt() {
    return expiredAt;
  }

  public void setExpiredAt(Date expiredAt) {
    this.expiredAt = expiredAt;
  }

  public Set<Scope> getScopes() {
    return scopes;
  }

  public void setScopes(Set<Scope> scopes) {
    this.scopes = scopes;
  }

  public String getScope() {
    return scope;
  }

  public void setScope(String scope) {
    this.scope = scope;
  }
}
