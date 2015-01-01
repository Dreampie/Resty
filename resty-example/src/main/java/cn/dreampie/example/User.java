package cn.dreampie.example;

import cn.dreampie.orm.Model;
import cn.dreampie.orm.annotation.Table;

/**
 * Created by ice on 14-12-31.
 */
@Table(name = "sec_user")
public class User extends Model<User> {
  public static User dao = new User();


  public static void main(String[] args) {
    String[] s = "s".split(".");
    System.out.println(s.length);
  }
}
