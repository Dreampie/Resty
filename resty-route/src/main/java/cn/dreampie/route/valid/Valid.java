package cn.dreampie.route.valid;

import cn.dreampie.common.http.HttpStatus;
import cn.dreampie.route.core.Params;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ice on 15-1-26.
 */
public abstract class Valid {
  protected Map<String, Object> errors = new HashMap<String, Object>();

  protected HttpStatus status = HttpStatus.BAD_REQUEST;

  /**
   * 解决线程安全问题和自主初始化参数 主动性
   *
   * @return Valid
   */
  public abstract Valid newInstance();

  public abstract void valid(Params params);

  public Map<String, Object> getErrors() {
    return errors;
  }

  public HttpStatus getStatus() {
    return status;
  }
}
