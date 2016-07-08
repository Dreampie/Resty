package cn.dreampie.oauth.entity;

import cn.dreampie.orm.Model;
import cn.dreampie.orm.annotation.Table;

/**
 * Created by Dreampie on 16/7/8.
 */
@Table(name = "oau_openid", cached = true)
public class Openid extends Model<Openid> {
  public static final Openid DAO = new Openid();
}
