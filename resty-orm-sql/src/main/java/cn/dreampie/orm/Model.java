package cn.dreampie.orm;

import cn.dreampie.log.Logger;

import java.util.Date;
import java.util.List;

/**
 * Created by wangrenhui on 2014/7/1.
 */
public abstract class Model<M extends Model> extends Base<M> {
  private static final Logger logger = Logger.getLogger(Model.class);

  protected String modelName;
  protected String deleteKey = "deleted_at";

  public List<M> findAll() {
    return find(getDialect().select(getModelMeta().getTableName()));
  }

  public List<M> findBy(String where, Object... paras) {
    return find(getDialect().select(getModelMeta().getTableName(), getModelName(), where), paras);
  }

  public List<M> findTopBy(int topNumber, String where, Object... paras) {
    return paginate(1, topNumber, getDialect().select(getModelMeta().getTableName(), getModelName(), where), paras).getList();
  }

  public M findFirstBy(String where, Object... paras) {
    return findFirst(getDialect().select(getModelMeta().getTableName(), getModelName(), where), paras);
  }

  public Page<M> paginateAll(int pageNumber, int pageSize) {
    return paginate(pageNumber, pageSize, getDialect().select(getModelMeta().getTableName()));
  }

  public Page<M> paginateBy(int pageNumber, int pageSize, String where, Object... paras) {
    return paginate(pageNumber, pageSize, getDialect().select(getModelMeta().getTableName(), getModelName(), where), paras);
  }

  public boolean updateAll(String columns) {
    logger.warn("You must ensure that \"updateAll()\" method of safety.");
    return update(getDialect().update(getModelMeta().getTableName(), columns.split(","))) > 0;
  }

  public boolean updateBy(String columns, String where, Object... paras) {
    return update(getDialect().update(getModelMeta().getTableName(), getModelName(), where, columns.split(",")), paras) > 0;
  }

  public boolean deleteAll() {
    logger.warn("You must ensure that \"deleteAll()\" method of safety.");
    this.set(deleteKey, new Date());
    return updateAll(deleteKey);
  }

  public boolean deleteBy(String where, Object... paras) {
    this.set(deleteKey, new Date());
    return updateBy(deleteKey, where, paras);
  }

  public boolean dropAll() {
    logger.warn("You must ensure that \"dropAll()\" method of safety.");
    return update(getDialect().delete(getModelMeta().getTableName())) > 0;
  }

  public boolean dropBy(String where, Object... paras) {
    return update(getDialect().delete(getModelMeta().getTableName(), where), paras) > 0;
  }

  public Long countAll() {
    return count(getDialect().count(getModelMeta().getTableName()));
  }

  public Long countBy(String where, Object... paras) {
    return count(getDialect().count(getModelMeta().getTableName(), getModelName(), where), paras);
  }


  public String getModelName() {
    if (modelName == null) {
      Class clazz = getClass();
      byte[] items = clazz.getSimpleName().getBytes();
      items[0] = (byte) ((char) items[0] + ('a' - 'A'));
      modelName = new String(items);
    }
    return modelName;
  }

}
