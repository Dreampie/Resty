package cn.dreampie.security.sign;

/**
 * User: xavierhanin
 * Date: 1/30/13
 * Time: 6:19 PM
 */
public class SignatureKey {
    public static final SignatureKey DEFAULT = new SignatureKey("this is the default signature key".getBytes());
    private final byte[] key;

    public SignatureKey(byte[] key) {
        this.key = key;
    }

    public byte[] getKey() {
        return key;
    }
}
