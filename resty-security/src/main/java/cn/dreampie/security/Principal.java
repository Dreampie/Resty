package cn.dreampie.security;


import java.util.Set;

/**
 * Date: 1/30/13
 * Time: 6:30 PM
 */
public class Principal {
  public static final String PRINCIPAL_DEF_KEY = "_principal";
  private String username;
  private String passwordHash;
  private Set<String> permissions;

  public Principal(String username, String passwordHash, Set<String> permissions) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.permissions = permissions;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public Set<String> getPermissions() {
    return permissions;
  }

  public boolean hasPermission(String permission) {
    return permissions.contains(permission);
  }
}
