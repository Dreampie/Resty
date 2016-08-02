package cn.dreampie.oauth.entity;

import cn.dreampie.orm.Model;
import cn.dreampie.orm.annotation.Table;

/**
 * Created by Dreampie on 16/7/8.
 */
@Table(name = "oau_openid", cached = true)
public class OpenID extends Model<OpenID> {
  public static final OpenID DAO = new OpenID();
}
