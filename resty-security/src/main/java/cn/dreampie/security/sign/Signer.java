package cn.dreampie.security.sign;

/**
 * Permits to sign and verify messages.
 */
public interface Signer {

  /**
   * Sign the specified message.
   *
   * @param message The message to sign.
   * @return The signed message.
   */
  String sign(String message);

  /**
   * Verify if the specified message correspond to the signed one.
   *
   * @param message       The message to verify.
   * @param signedMessage The signed message.
   * @return True if the message is corresponding to the signed message, false otherwise.
   */
  boolean verify(String message, String signedMessage);
}
