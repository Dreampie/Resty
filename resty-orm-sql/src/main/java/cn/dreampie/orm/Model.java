package cn.dreampie.orm;

import cn.dreampie.log.Logger;

import java.util.List;

/**
 * Created by wangrenhui on 2014/7/1.
 */
public abstract class Model<M extends Model> extends Base<M> {
  private static final Logger logger = Logger.getLogger(Model.class);

  protected String alias;

  /**
   * 查询全部的model数据
   *
   * @return
   */
  public List<M> findAll() {
    return find(getDialect().select(getModelMeta().getTableName()));
  }

  /**
   * 根据where条件查询model集合
   *
   * @param where 条件
   * @param paras 参数
   * @return list
   */
  public List<M> findBy(String where, Object... paras) {
    return find(getDialect().select(getModelMeta().getTableName(), getAlias(), where), paras);
  }

  /**
   * 根据条件查询 前几位
   *
   * @param topNumber 前几位
   * @param where     条件
   * @param paras     参数
   * @return list
   */
  public List<M> findTopBy(int topNumber, String where, Object... paras) {
    return paginate(1, topNumber, getDialect().select(getModelMeta().getTableName(), getAlias(), where), paras).getList();
  }

  /**
   * 根据条件查询第一个对象
   *
   * @param where 条件
   * @param paras 参数
   * @return model
   */
  public M findFirstBy(String where, Object... paras) {
    return findFirst(getDialect().select(getModelMeta().getTableName(), getAlias(), where), paras);
  }

  /**
   * 分页查询
   *
   * @param pageNumber 页码
   * @param pageSize   每页大小
   * @return 分页对象
   */
  public Page<M> paginateAll(int pageNumber, int pageSize) {
    return paginate(pageNumber, pageSize, getDialect().select(getModelMeta().getTableName()));
  }

  /**
   * 分页查询
   *
   * @param pageNumber 页码
   * @param pageSize   每页大小
   * @param where      条件
   * @param paras      参数
   * @return 分页对象
   */
  public Page<M> paginateBy(int pageNumber, int pageSize, String where, Object... paras) {
    return paginate(pageNumber, pageSize, getDialect().select(getModelMeta().getTableName(), getAlias(), where), paras);
  }

  /**
   * 更新全部传入的列  UPDATE table SET name=?,age=? 参数 "abc",20
   *
   * @param columns 通过逗号分隔的列名 "name,age"
   * @param paras   按列名顺序排列参数   "abc",20
   * @return boolean
   */
  public boolean updateAll(String columns, Object... paras) {
    logger.warn("You must ensure that \"updateAll()\" method of safety.");
    return update(getDialect().update(getModelMeta().getTableName(), columns.split(",")), paras) > 0;
  }

  /**
   * 根据条件和传入的列更新  UPDATE table SET name=?,age=? WHERE x=?  参数 "abc",20,12
   *
   * @param columns 通过逗号分隔的列   "name,age"
   * @param where   条件 x=?
   * @param paras   按列名顺序排列参数   "abc",20,12
   * @return boolean
   */
  public boolean updateBy(String columns, String where, Object... paras) {
    return update(getDialect().update(getModelMeta().getTableName(), getAlias(), where, columns.split(",")), paras) > 0;
  }

  /**
   * 删除全部数据
   *
   * @return boolean
   */
  public boolean deleteAll() {
    logger.warn("You must ensure that \"deleteAll()\" method of safety.");
    return update(getDialect().delete(getModelMeta().getTableName())) > 0;
  }

  /**
   * 根据条件删除数据
   *
   * @param where 条件
   * @param paras 参数
   * @return
   */
  public boolean deleteBy(String where, Object... paras) {
    return update(getDialect().delete(getModelMeta().getTableName(), where), paras) > 0;
  }

  /**
   * COUNT 函数求和
   *
   * @return Long
   */
  public Long countAll() {
    return count(getDialect().count(getModelMeta().getTableName()));
  }

  /**
   * COUNT 根据条件函数求和
   *
   * @return Long
   */
  public Long countBy(String where, Object... paras) {
    return count(getDialect().count(getModelMeta().getTableName(), getAlias(), where), paras);
  }


  public String getAlias() {
    if (alias == null) {
      Class clazz = getClass();
      byte[] items = clazz.getSimpleName().getBytes();
      items[0] = (byte) ((char) items[0] + ('a' - 'A'));
      alias = new String(items);
    }
    return alias;
  }

  public M setAlias(String alias) {
    this.alias = alias;
    return (M) this;
  }
}
