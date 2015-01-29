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
  private Set<String> credentials;

  public Principal(String username, String passwordHash, Set<String> credentials) {
    this.username = username;
    this.passwordHash = passwordHash;
    this.credentials = credentials;
  }

  public String getUsername() {
    return username;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public Set<String> getCredentials() {
    return credentials;
  }

  public boolean hasCredential(String permission) {
    return credentials.contains(permission);
  }
}
