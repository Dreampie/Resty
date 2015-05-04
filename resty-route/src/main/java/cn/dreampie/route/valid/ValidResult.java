package cn.dreampie.route.valid;

import cn.dreampie.common.http.result.HttpStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ice on 15-1-26.
 */
public class ValidResult {
  private Map<String, Object> errors = new HashMap<String, Object>();

  private HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;

  public ValidResult() {
  }

  public ValidResult(Map<String, Object> errors) {
    this.errors = errors;
  }

  public ValidResult(Map<String, Object> errors, HttpStatus status) {
    this.errors = errors;
    this.status = status;
  }

  public void addError(String name, Object error) {
    this.errors.put(name, error);
  }

  public Map<String, Object> getErrors() {
    return errors;
  }

  public void setErrors(Map<String, Object> errors) {
    this.errors = errors;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public void setStatus(HttpStatus status) {
    this.status = status;
  }
}
