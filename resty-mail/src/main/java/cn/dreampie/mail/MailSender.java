package cn.dreampie.mail;

import cn.dreampie.common.util.Joiner;
import cn.dreampie.log.Logger;
import org.apache.commons.mail.*;

/**
 * Mailer.sendHtml("测试","173956022@qq.com","<a href='www.dreampie.cn'>Dreampie</a>");
 * Created by wangrenhui on 14-5-6.
 */
public class MailSender {
  private static Logger logger = Logger.getLogger(MailSender.class);

  /**
   * @param subject    主题
   * @param body       内容
   * @param recipients 收件人
   */
  public static void sendText(String subject, String body, String... recipients) {
    try {
      SimpleEmail simpleEmail = getSimpleEmail(subject, body, recipients);
      simpleEmail.send();
      logger.info("send email to {}", Joiner.on(",").useForNull("null").join(recipients));
    } catch (EmailException e) {
      throw new MailException("Unabled to send email", e);
    }
  }

  /**
   * @param subject    主题
   * @param body       内容
   * @param recipients 收件人
   */
  public static SimpleEmail getSimpleEmail(String subject, String body, String... recipients) throws EmailException {
    SimpleEmail simpleEmail = new SimpleEmail();
    configEmail(subject, simpleEmail, recipients);
    if (body != null)
      simpleEmail.setMsg(body);
    return simpleEmail;
  }


  /**
   * @param subject    主题
   * @param body       内容
   * @param recipients 收件人
   */
  public static void sendHtml(String subject, String body, String... recipients) {
    sendHtml(subject, body, null, recipients);
  }

  /**
   * @param subject    主题
   * @param body       内容
   * @param attachment 附件
   * @param recipients 收件人
   */
  public static void sendHtml(String subject, String body, EmailAttachment attachment, String... recipients) {
    try {
      HtmlEmail htmlEmail = getHtmlEmail(subject, body, attachment, recipients);
      htmlEmail.send();
      logger.info("send email to {}", Joiner.on(",").useForNull("null").join(recipients));
    } catch (EmailException e) {
      throw new MailException("Unabled to send email", e);
    }
  }

  /**
   * @param subject    主题
   * @param recipients 收件人
   */
  public static HtmlEmail getHtmlEmail(String subject, String... recipients) {
    return getHtmlEmail(subject, null, null, recipients);
  }

  /**
   * @param subject    主题
   * @param body       内容
   * @param attachment 附件
   * @param recipients 收件人
   */
  public static HtmlEmail getHtmlEmail(String subject, String body, EmailAttachment attachment, String... recipients) {
    try {
      HtmlEmail htmlEmail = new HtmlEmail();
      configEmail(subject, htmlEmail, recipients);
      if (body != null)
        htmlEmail.setHtmlMsg(body);
      // set the alter native message
      htmlEmail.setTextMsg("Your email client does not support HTML messages");
      if (attachment != null)
        htmlEmail.attach(attachment);
      return htmlEmail;
    } catch (EmailException e) {
      throw new MailException("Unabled to send email", e);
    }
  }

  /**
   * @param subject    主题
   * @param body       内容
   * @param attachment 附件
   * @param recipients 收件人
   */
  public static void sendAttachment(String subject, String body, EmailAttachment attachment, String... recipients) {
    try {
      MultiPartEmail multiPartEmail = getMultiPartEmail(subject, body, attachment, recipients);
      multiPartEmail.send();
      logger.info("send email to {}", Joiner.on(",").useForNull("null").join(recipients));
    } catch (EmailException e) {
      throw new MailException("Unabled to send email", e);
    }
  }

  /**
   * @param subject    主题
   * @param recipients 收件人
   */
  public static MultiPartEmail getMultiPartEmail(String subject, String body, String... recipients) {
    return getMultiPartEmail(subject, body, null, recipients);
  }

  /**
   * @param subject    主题
   * @param body       内容
   * @param attachment 附件
   * @param recipients 收件人
   */
  public static MultiPartEmail getMultiPartEmail(String subject, String body, EmailAttachment attachment, String... recipients) {
    try {
      MultiPartEmail multiPartEmail = new MultiPartEmail();
      configEmail(subject, multiPartEmail, recipients);
      multiPartEmail.setMsg(body);
      // add the attachment
      if (attachment != null)
        multiPartEmail.attach(attachment);
      return multiPartEmail;
    } catch (EmailException e) {
      throw new MailException("Unabled to send email", e);
    }
  }

  private static void configEmail(String subject, Email email, String... recipients) throws EmailException {

    if (recipients == null)
      throw new EmailException("Recipients not found.");
    Mail mail = MailPlugin.getMail();
    email.setCharset(mail.getCharset());
    email.setSocketTimeout(mail.getTimeout());
    email.setSocketConnectionTimeout(mail.getConnectout());
    email.setHostName(mail.getHost());
    if (!mail.getSslport().isEmpty())
      email.setSslSmtpPort(mail.getSslport());
    if (!mail.getPort().isEmpty())
      email.setSmtpPort(Integer.parseInt(mail.getPort()));
    email.setSSLOnConnect(mail.isSsl());
    email.setStartTLSEnabled(mail.isTls());
    email.setDebug(mail.isDebug());
    email.setAuthentication(mail.getUser(), mail.getPassword());
    email.setFrom(mail.getFrom(), mail.getName());
    email.setSubject(subject);
    email.addTo(recipients);
  }

}
