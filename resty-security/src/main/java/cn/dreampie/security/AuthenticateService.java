package cn.dreampie.security;

import java.util.Set;

/**
 * Created by ice on 14-12-23.
 */
public interface AuthenticateService {

  public Principal findByUsername(String username);

  public Set<Credential> loadAllCredentials();
}
