package cn.dreampie.orm;

import cn.dreampie.log.Logger;

import java.util.Date;
import java.util.List;

/**
 * Created by wangrenhui on 2014/7/1.
 */
public abstract class Model<M extends Model> extends Base<M> {
  private static final Logger logger = Logger.getLogger(Model.class);

  protected static String deleteKey = "deleted_at";

  public List<M> findAll() {
    return find(getDialect().select(getModelMeta().getTableName()));
  }

  public List<M> findBy(String where, Object... paras) {
    return find(getDialect().select(getModelMeta().getTableName(), where), paras);
  }

  public List<M> findTopBy(int topNumber, String where, Object... paras) {
    return paginate(1, topNumber, getDialect().select(getModelMeta().getTableName(), where), paras).getList();
  }

  public M findFirstBy(String where, Object... paras) {
    return findFirst(getDialect().select(getModelMeta().getTableName(), where), paras);
  }

  public Page<M> paginateAll(int pageNumber, int pageSize) {
    return paginate(pageNumber, pageSize, getDialect().select(getModelMeta().getTableName()));
  }

  public Page<M> paginateBy(int pageNumber, int pageSize, String where, Object... paras) {
    return paginate(pageNumber, pageSize, getDialect().select(getModelMeta().getTableName(), where), paras);
  }

  public boolean updateAll(String columns) {
    logger.warn("You must ensure that \"updateAll\" method of safety.");
    return update(columns) > 0;
  }

  public boolean updateBy(String columns, String where, Object... paras) {
    return update(columns, where, paras) > 0;
  }

  public boolean deleteAll() {
    logger.warn("You must ensure that \"deleteAll\" method of safety.");
    this.set(deleteKey, new Date());
    return update(deleteKey) > 0;
  }

  public boolean deleteBy(String where, Object... paras) {
    this.set(deleteKey, new Date());
    return update(deleteKey, where, paras) > 0;
  }

  public boolean dropAll() {
    logger.warn("You must ensure that \"dropAll\" method of safety.");
    return update(getDialect().delete(getModelMeta().getTableName())) > 0;
  }

  public boolean dropBy(String where, Object... paras) {
    return update(getDialect().delete(getModelMeta().getTableName(), where), paras) > 0;
  }

  public Long countAll() {
    return count(getDialect().count(getModelMeta().getTableName()));
  }

  public Long countBy(String where, Object... paras) {
    return count(getDialect().count(getModelMeta().getTableName(), where), paras);
  }

}
