package cn.dreampie.route.core;

import cn.dreampie.common.entity.CaseInsensitiveMap;

import java.util.Map;

/**
 * @author Dreampie
 * @date 2015-08-21
 * @what
 */
public class Cookies extends Params {
  public Cookies(final Map<String, String> cookies) {
    super(new CaseInsensitiveMap<Object>() {{
      putAll(cookies);
    }});
  }
}
