package cn.dreampie.oauth.entity;

import cn.dreampie.common.util.Joiner;
import cn.dreampie.orm.Model;
import cn.dreampie.orm.annotation.Table;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Dreampie on 16/7/7.
 * 第三方客户端
 */
@Table(name = "oau_client", cached = true)
public class Client extends Model<Client> {
  public static final Client DAO = new Client();

  public Set<Scope> getScopes() {
    if (this.get("scopes") == null) {
      List<ClientScope> clientScopes = ClientScope.DAO.findBy("client_id=?", this.get("id"));
      if (clientScopes != null && clientScopes.size() > 0) {
        int[] scopeIds = new int[clientScopes.size()];
        int i = 0;
        for (ClientScope clientScope : clientScopes) {
          scopeIds[i++] = clientScope.<Integer>get("scope_id");
        }
        Set<Scope> scopes = new HashSet<Scope>(Scope.DAO.findInIds(scopeIds));

        this.put("scopeIds", scopeIds);
        this.put("scopes", scopes);
        String[] scopesArr = new String[scopes.size()];
        i = 0;
        for (Scope scope : scopes) {
          scopesArr[i++] = scope.get("key");
        }
        this.put("scope", Joiner.on(",").join(scopesArr));
      }
    }
    return this.get("scopes");
  }

  public String getScope() {
    if (this.get("scope") == null) {
      getScopes();
    }
    return this.get("scope");
  }

  public int[] getScopeIds() {
    if (this.get("scopeIds") == null) {
      getScopes();
    }
    return this.get("scopeIds");
  }

  public Set<Grant> getGrants() {
    if (this.get("grants") == null) {
      List<ClientGrant> clientGrants = ClientGrant.DAO.findBy("client_id=?", this.get("id"));
      if (clientGrants != null && clientGrants.size() > 0) {
        int[] grantIds = new int[clientGrants.size()];
        int i = 0;
        for (ClientGrant clientGrant : clientGrants) {
          grantIds[i++] = clientGrant.<Integer>get("grant_id");
        }
        Set<Grant> grants = new HashSet<Grant>(Grant.DAO.findInIds(grantIds));

        this.put("grantIds", grantIds);
        this.put("grants", grants);
        String[] grantsArr = new String[grants.size()];
        i = 0;
        for (Grant grant : grants) {
          grantsArr[i++] = grant.get("key");
        }
        this.put("grant", Joiner.on(",").join(grantsArr));
      }
    }
    return this.get("grants");
  }

  public String getGrant() {
    if (this.get("grant") == null) {
      getGrants();
    }
    return this.get("scope");
  }

  public int[] getGrantIds() {
    if (this.get("grantIds") == null) {
      getGrants();
    }
    return this.get("grantIds");
  }
}
