package cn.dreampie.service;

import org.springframework.stereotype.Component;

/**
 * @author Dreampie
 * @date 2015-10-08
 * @what
 */
@Component
public class HelloServiceImpl implements HelloService {
  public String hello() {
    return "hello";
  }
}
