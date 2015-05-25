package cn.dreampie.security;

import cn.dreampie.security.credential.Credential;

import java.io.Serializable;
import java.util.Set;

/**
 * Created by ice on 14-12-23.
 */
public abstract class AuthenticateService implements Serializable {

  public abstract Principal getPrincipal(String username);

  public PasswordService getPasswordService() {
    return DefaultPasswordService.instance();
  }

  public abstract Set<Credential> getAllCredentials();
}
