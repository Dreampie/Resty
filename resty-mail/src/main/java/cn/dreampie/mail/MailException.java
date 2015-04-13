package cn.dreampie.mail;

/**
 * DBException
 */
public class MailException extends RuntimeException {

  public MailException() {
  }

  public MailException(String message) {
    super(message);
  }

  public MailException(Throwable cause) {
    super(cause);
  }

  public MailException(String message, Throwable cause) {
    super(message, cause);
  }
}










