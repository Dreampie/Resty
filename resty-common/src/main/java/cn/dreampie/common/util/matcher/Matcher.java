package cn.dreampie.common.util.matcher;

/**
 * Created by ice on 14-9-10.
 */
public interface Matcher {

  /**
   * Returns <code>true</code> if the given <code>source</code> matches the specified <code>pattern</code>,
   * <code>false</code> otherwise.
   *
   * @param pattern the pattern to match against
   * @param source  the source to match
   * @return <code>true</code> if the given <code>source</code> matches the specified <code>pattern</code>,
   * <code>false</code> otherwise.
   */
  boolean matches(String pattern, String source);
}