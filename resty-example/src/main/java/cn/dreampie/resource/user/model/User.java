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
@Table(name = "sec_user", primaryKey = "id,sid", cached = true)
public class User extends Model<User> {
  public static User dao = new User();

  // 默认 getXxx 的形式的方法 会被认为是属性  方法会被执行一次
  // json反转时  如果 getXxx的存在  会按 getXxx的返回值类型 就行转换
  //  @JSONField(serialize = false) 如果不转为json  使用该注解
  // 注意属性名和GetXxx一致   如  userInfos的get方法为 getUserInfos
  public List<UserInfo> getUserInfos() {
    if (this.get("userInfos") == null) {
      this.put("userInfos", UserInfo.dao.findBy("user_id=?", this.get("id")));
    }
    return this.get("userInfos");
  }

  public Long getRoleId() {
    if (this.get("roleId") == null) {
      String sql = "SELECT userRole.role_id FROM sec_user_role userRole WHERE userRole.user_id=?";
      this.put("roleId", DS.use().queryLong(sql, this.get("id")));
    }
    return this.get("roleId");
  }

  public Set<String> getPermissionsSet() {
    if (this.get("permissionsSet") == null) {
      this.put("permissionsSet", new HashSet<String>(getPermissions()));
    }
    return this.get("permissionsSet");
  }

  public List<String> getPermissions() {
    if (this.get("permissions") == null) {
      String sql = "SELECT permission.value FROM sec_permission permission WHERE permission.id in(SELECT rolePermission.permission_id FROM sec_role_permission rolePermission WHERE rolePermission.role_id=?)";
      this.put("permissions", DS.use().query(sql, getRoleId()));
    }
    return this.get("permissions");
  }

  public List<Long> getPermissionIds() {
    if (this.get("permissionIds") == null) {
      String sql = "SELECT permission.id FROM sec_permission permission WHERE permission.id in(SELECT rolePermission.permission_id FROM sec_role_permission rolePermission WHERE rolePermission.role_id=?)";
      this.put("permissionIds", DS.use().query(sql, getRoleId()));
    }
    return this.get("permissionIds");
  }
}
