package cn.dreampie.example.service;

import cn.dreampie.example.model.User;
import cn.dreampie.orm.DS;
import cn.dreampie.orm.transaction.Transaction;

/**
 * Created by wangrenhui on 15/1/2.
 */
public interface UserService {
  @Transaction(name = {DS.DEFAULT_DS_NAME, "demo"})
  public User save(User u);
}
