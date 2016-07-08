package cn.dreampie.oauth.entity;

import cn.dreampie.orm.Model;
import cn.dreampie.orm.annotation.Table;

/**
 * Created by Dreampie on 16/7/7.
 * 授权范围
 */
@Table(name = "oau_client_grant",  cached = true)
public class ClientGrant extends Model<ClientGrant> {
  public static final ClientGrant DAO = new ClientGrant();
}
