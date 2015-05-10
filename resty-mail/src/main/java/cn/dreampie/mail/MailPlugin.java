package cn.dreampie.mail;

import cn.dreampie.common.Plugin;
import cn.dreampie.common.util.properties.Prop;
import cn.dreampie.common.util.properties.Proper;

/**
 * Created by wangrenhui on 14-5-6.
 */
public class MailPlugin implements Plugin {

  private static Mail mail;
  private String config;

  public MailPlugin() {
    this("application.properties");
  }

  public MailPlugin(String config) {
    this.config = config;
  }

  public static Mail getMail() {
    return mail;
  }

  public boolean start() {
    Prop prop = Proper.use(config);
    String charset = prop.get("smtp.charset", "utf-8");
    String host = prop.get("smtp.host", "");
    if (host == null || host.isEmpty()) {
      throw new MailException("email host has not found!");
    }
    String port = prop.get("smtp.port", "");

    boolean ssl = prop.getBoolean("smtp.ssl", false);
    String sslport = prop.get("smtp.sslport", "");
    int timeout = prop.getInt("smtp.timeout", 60000);
    int connectout = prop.getInt("smtp.connectout", 60000);
    boolean tls = prop.getBoolean("smtp.tls", false);
    boolean debug = prop.getBoolean("smtp.debug", false);
    String user = prop.get("smtp.user");

    if (user == null || user.isEmpty()) {
      throw new MailException("email user has not found!");
    }
    String password = prop.get("smtp.password");
    if (password == null || password.isEmpty()) {
      throw new MailException("email password has not found!");
    }

    String name = prop.get("smtp.name");

    String from = prop.get("smtp.from", user);
    if (from == null || from.isEmpty()) {
      throw new MailException("email from has not found!");
    }
    mail = new Mail(charset, host, sslport, timeout, connectout, port, ssl, tls, debug, user, password, name, from);
    return true;
  }

  public boolean stop() {
    mail = null;
    return true;
  }
}
