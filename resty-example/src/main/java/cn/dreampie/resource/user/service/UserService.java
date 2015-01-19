package cn.dreampie.resource.user.service;

import cn.dreampie.orm.DS;
import cn.dreampie.orm.transaction.Transaction;
import cn.dreampie.resource.user.model.User;

/**
 * Created by wangrenhui on 15/1/2.
 */
public interface UserService {
  @Transaction(name = {DS.DEFAULT_DS_NAME, "demo"})
  public User save(User u);
}
