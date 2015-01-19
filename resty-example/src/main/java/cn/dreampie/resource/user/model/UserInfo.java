package cn.dreampie.resource.user.model;

import cn.dreampie.orm.Model;
import cn.dreampie.orm.annotation.Table;

/**
 * Created by wangrenhui on 15/1/2.
 */
@Table(name = "sec_user_info")
public class UserInfo extends Model<UserInfo> {
  public static UserInfo dao = new UserInfo();
}
