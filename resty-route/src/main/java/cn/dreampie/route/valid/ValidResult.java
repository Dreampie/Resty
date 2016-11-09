package cn.dreampie.route.valid;

import cn.dreampie.common.http.result.ErrorResult;
import cn.dreampie.common.http.result.HttpStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ice on 15-1-26.
 */
public class ValidResult {
  private List<ErrorResult> errors = new ArrayList<ErrorResult>();

  private HttpStatus status = HttpStatus.UNPROCESSABLE_ENTITY;

  public ValidResult() {
  }

  public ValidResult(List<ErrorResult> errors) {
    this.errors = errors;
  }

  public ValidResult(HttpStatus status, List<ErrorResult> errors) {
    this.errors = errors;
    this.status = status;
  }

  public void addError(String name, String error) {
    this.errors.add(new ErrorResult(name, error));
  }

  public List<ErrorResult> getErrors() {
    return errors;
  }

  public void setErrors(List<ErrorResult> errors) {
    this.errors = errors;
  }

  public HttpStatus getStatus() {
    return status;
  }

  public void setStatus(HttpStatus status) {
    this.status = status;
  }
}
