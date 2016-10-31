package cn.dreampie.route.valid;

import cn.dreampie.route.core.Params;
import cn.dreampie.route.core.RouteMatch;

/**
 * Created by ice on 15-1-26.
 */
public abstract class Validator {

  public abstract ValidResult validate(Params params, RouteMatch routeMatch);

}
