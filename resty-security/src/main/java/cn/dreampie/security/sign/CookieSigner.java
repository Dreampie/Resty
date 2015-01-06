package cn.dreampie.security.sign;

/**
 * Default cookie signer, using HMAC-SHA1 algorithm to sign the cookie.
 *
 * @author apeyrard
 */
public class CookieSigner implements Signer {
  private final SignatureKey signatureKey;

  public CookieSigner() {
    this.signatureKey = SignatureKey.DEFAULT;
  }

  public CookieSigner(SignatureKey signatureKey) {
    this.signatureKey = signatureKey;
  }

  @Override
  public String sign(String cookie) {
    return Cryptoer.sign(cookie, signatureKey.getKey());
  }

  @Override
  public boolean verify(String cookie, String signedCookie) {
    return sign(cookie).equals(signedCookie);
  }
}
