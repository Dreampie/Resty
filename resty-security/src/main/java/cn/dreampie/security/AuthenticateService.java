package cn.dreampie.security;

import cn.dreampie.security.credential.Credential;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by ice on 14-12-23.
 */
public interface AuthenticateService extends Serializable {

  public Principal findByUsername(String username);

  public Set<Credential> loadAllCredentials();
}
