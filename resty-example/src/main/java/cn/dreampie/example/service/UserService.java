package cn.dreampie.example.service;

import cn.dreampie.example.model.User;
import cn.dreampie.orm.transaction.Transaction;

/**
 * Created by wangrenhui on 15/1/2.
 */
public interface UserService {
  @Transaction(name = {"default", "demo"})
  public User save(User u);
}
