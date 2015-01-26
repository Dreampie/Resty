package cn.dreampie.resource;

import cn.dreampie.common.util.Validator;
import cn.dreampie.route.core.Params;
import cn.dreampie.route.valid.Valid;

/**
 * Created by ice on 15-1-26.
 */
public class SigninValid extends Valid {

  public Valid newInstance() {
    return new SigninValid();
  }

  public void valid(Params params) {

    String username = params.get("username");
    if (!Validator.isGeneral(username, 5, 16))
      errors.put("username", "用户名错误!");
  }
}
