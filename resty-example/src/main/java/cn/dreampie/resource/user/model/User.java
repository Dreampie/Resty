package cn.dreampie.resource.user.model;

import cn.dreampie.orm.DS;
import cn.dreampie.orm.Model;
import cn.dreampie.orm.annotation.Table;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by ice on 14-12-31.
 */
@Table(name = "sec_user", cached = true)
public class User extends Model<User> {
  public static User dao = new User();

  public UserInfo getUserInfo() {
    if (this.get("userInfo") == null) {
      this.put("userInfo", UserInfo.dao.findFirstBy("user_id=?", this.get("id")));
    }
    return this.get("userInfo");
  }

  public Long getRoleId() {
    if (this.get("role_id") == null) {
      String sql = "SELECT userRole.role_id FROM sec_user_role userRole WHERE userRole.user_id=?";
      this.put("role_id", DS.use().queryLong(sql, this.get("id")));
    }
    return this.get("role_id");
  }

  public Set<String> getPermissions() {
    if (this.get("permissions") == null) {
      String sql = "SELECT permission.value FROM sec_permission permission WHERE permission.id in(SELECT rolePermission.permission_id FROM sec_role_permission rolePermission WHERE rolePermission.role_id=?)";
      this.put("permissions", DS.use().query(sql, getRoleId()));
    }
    return new HashSet<String>((List<String>) this.get("permissions"));
  }
}
