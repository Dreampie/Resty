package cn.dreampie.oauth.entity;

import cn.dreampie.common.util.Joiner;
import cn.dreampie.orm.Model;
import cn.dreampie.orm.annotation.Table;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * Created by Dreampie on 16/7/7.
 * 授权码
 */
public class Code implements Serializable {
  private String code;
  private Integer clientId;
  private Integer userId;
  private int expires;
  private Date expiredAt;
  private Set<Scope> scopes;
  private String scope;

  public Code(String code, Integer clientId, Integer userId, int expires, Set<Scope> scopes) {
    this.code = code;
    this.clientId = clientId;
    this.userId = userId;
    this.expires = expires;
    this.expiredAt = new Date(new Date().getTime() + expires);
    this.scopes = scopes;
    String[] scopesArr = new String[scopes.size()];
    int i = 0;
    for (Scope scope : scopes) {
      scopesArr[i++] = scope.get("key");
    }
    this.scope = Joiner.on(",").join(scopesArr);
  }

  public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
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
