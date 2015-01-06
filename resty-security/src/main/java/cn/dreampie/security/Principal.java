package cn.dreampie.security;


import java.util.Set;

/**
 * User: xavierhanin
 * Date: 1/30/13
 * Time: 6:30 PM
 */
public class Principal {
  public static final String PRINCIPAL_DEF_KEY = "principalKey";
  private String username;
  private Set<String> roles;
  private Set<String> permissions;

  public Principal(String username, Set<String> roles, Set<String> permissions) {
    this.username = username;
    this.roles = roles;
    this.permissions = permissions;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(Set<String> roles) {
    this.roles = roles;
  }

  public Set<String> getPermissions() {
    return permissions;
  }

  public void setPermissions(Set<String> permissions) {
    this.permissions = permissions;
  }
}
