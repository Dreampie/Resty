package cn.dreampie.security;


import cn.dreampie.common.entity.Entity;

import java.io.Serializable;
import java.util.Set;

/**
 * Date: 1/30/13
 * Time: 6:30 PM
 */
public class Principal<M extends Entity> implements Serializable {
  public static final String PRINCIPAL_DEF_KEY = "_principal";
  private String username;
  private String password;
  private String salt;
  private Set<String> credentials;
  private M model;

  public Principal(String username, String password, Set<String> credentials, M model) {
    this(username, password, null, credentials, model);
  }

  public Principal(String username, String password, String salt, Set<String> credentials, M model) {
    this.username = username;
    this.password = password;
    this.salt = salt;
    this.credentials = credentials;
    this.model = model;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public String getSalt() {
    return salt;
  }

  public Set<String> getCredentials() {
    return credentials;
  }

  public boolean hasCredential(String permission) {
    return credentials.contains(permission);
  }

  public M getModel() {
    return model;
  }
}
