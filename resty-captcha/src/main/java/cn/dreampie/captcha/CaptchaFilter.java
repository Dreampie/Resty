package cn.dreampie.captcha;

/**
 * Created by Dreampie on 16/1/6.
 */
public enum CaptchaFilter {
  //曲面
  Curves(0),
  //大理石纹
  Marble(1),
  //对折
  Double(2),
  //颤动
  Wobble(3),
  //扩散
  Diffuse(4);
  private int value;

  private CaptchaFilter(int value) {
    this.value = value;
  }

  public static CaptchaFilter valueOf(int value) {
    for (CaptchaFilter filter : values()) {
      if (filter.value() == value) {
        return filter;
      }
    }
    throw new IllegalArgumentException("Invalid filter value : " + value);
  }

  public int value() {
    return this.value;
  }
}
