package cn.dreampie.resource;

import cn.dreampie.common.util.pattern.PatternValidator;
import cn.dreampie.route.core.Params;
import cn.dreampie.route.valid.ValidResult;
import cn.dreampie.route.valid.Validator;

/**
 * Created by ice on 15-1-26.
 */
public class SigninValidator extends Validator {

  public ValidResult validate(Params params) {

    ValidResult result = new ValidResult();

    String username = params.get("username");
    if (!PatternValidator.isGeneral(username, 5, 16))
      result.addError("username", "用户名错误!");
    return result;
  }
}
