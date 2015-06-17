package cn.dreampie.resource.user.service;

import cn.dreampie.orm.transaction.Transactional;
import cn.dreampie.resource.user.model.User;

/**
 * Created by wangrenhui on 15/1/2.
 */
public interface UserService {
  @Transactional
  public User save(User u);

  @Transactional
  public User update(User u);
}
