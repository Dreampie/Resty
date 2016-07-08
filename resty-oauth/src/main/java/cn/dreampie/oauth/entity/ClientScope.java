package cn.dreampie.oauth.entity;

import cn.dreampie.orm.Model;
import cn.dreampie.orm.annotation.Table;

/**
 * Created by Dreampie on 16/7/7.
 * 授权范围
 */
@Table(name = "oau_client_scope",  cached = true)
public class ClientScope extends Model<ClientScope> {
  public static final ClientScope DAO = new ClientScope();
}
