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
   * @return model 集合
   */
  public List<M> findAll() {
    return find(getDialect().select(getTableMeta().getTableName()));
  }

  /**
   * 查询全部的model数据
   *
   * @param columns 列 用逗号分割
   * @return model 集合
   */
  public List<M> findColsAll(String columns) {
    return find(getDialect().select(getTableMeta().getTableName(), columns.split(",")));
  }

  /**
   * 根据where条件查询model集合
   *
   * @param where 条件
   * @param paras 参数
   * @return list
   */
  public List<M> findBy(String where, Object... paras) {
    return find(getDialect().select(getTableMeta().getTableName(), getAlias(), where), paras);
  }

  /**
   * 根据where条件查询model集合
   *
   * @param colums 列 用逗号分割
   * @param where  条件
   * @param paras  参数
   * @return model集合
   */
  public List<M> findColsBy(String colums, String where, Object... paras) {
    return find(getDialect().select(getTableMeta().getTableName(), getAlias(), where, colums.split(",")), paras);
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
    return paginate(1, topNumber, getDialect().select(getTableMeta().getTableName(), getAlias(), where), paras).getList();
  }

  /**
   * 根据条件查询 前几位
   *
   * @param topNumber 前几位
   * @param columns   列 用逗号分割
   * @param where     条件
   * @param paras     参数
   * @return list
   */
  public List<M> findColsTopBy(int topNumber, String columns, String where, Object... paras) {
    return paginate(1, topNumber, getDialect().select(getTableMeta().getTableName(), getAlias(), where, columns.split(",")), paras).getList();
  }

  /**
   * 根据条件查询第一个对象
   *
   * @param where 条件
   * @param paras 参数
   * @return model对象
   */
  public M findFirstBy(String where, Object... paras) {
    return findFirst(getDialect().select(getTableMeta().getTableName(), getAlias(), where), paras);
  }

  /**
   * 根据条件查询第一个对象
   *
   * @param columns 列 用逗号分割
   * @param where   条件
   * @param paras   参数
   * @return model对象
   */
  public M findColsFirstBy(String columns, String where, Object... paras) {
    return findFirst(getDialect().select(getTableMeta().getTableName(), getAlias(), where, columns.split(",")), paras);
  }

  /**
   * 分页查询
   *
   * @param pageNumber 页码
   * @param pageSize   每页大小
   * @return 分页对象
   */
  public Page<M> paginateAll(int pageNumber, int pageSize) {
    return paginate(pageNumber, pageSize, getDialect().select(getTableMeta().getTableName()));
  }

  /**
   * 分页查询
   *
   * @param pageNumber 页码
   * @param pageSize   每页大小
   * @param columns    列 用逗号分割
   * @return 分页对象
   */
  public Page<M> paginateColsAll(int pageNumber, int pageSize, String columns) {
    return paginate(pageNumber, pageSize, getDialect().select(getTableMeta().getTableName(), columns.split(",")));
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
    return paginate(pageNumber, pageSize, getDialect().select(getTableMeta().getTableName(), getAlias(), where), paras);
  }

  /**
   * 分页查询
   *
   * @param pageNumber 页码
   * @param pageSize   每页大小
   * @param columns    列  用逗号分割
   * @param where      条件
   * @param paras      参数
   * @return 分页对象
   */
  public Page<M> paginateColsBy(int pageNumber, int pageSize, String columns, String where, Object... paras) {
    return paginate(pageNumber, pageSize, getDialect().select(getTableMeta().getTableName(), getAlias(), where, columns.split(",")), paras);
  }

  /**
   * 更新全部传入的列  UPDATE table SET name=?,age=? 参数 "abc",20
   *
   * @param columns 通过逗号分隔的列名 "name,age"
   * @param paras   按列名顺序排列参数   "abc",20
   * @return boolean
   */
  public boolean updateColsAll(String columns, Object... paras) {
    logger.warn("You must ensure that \"updateAll()\" method of safety.");
    return update(getDialect().update(getTableMeta().getTableName(), columns.split(",")), paras) > 0;
  }

  /**
   * 根据条件和传入的列更新  UPDATE table SET name=?,age=? WHERE x=?  参数 "abc",20,12
   *
   * @param columns 通过逗号分隔的列   "name,age"
   * @param where   条件 x=?
   * @param paras   按列名顺序排列参数   "abc",20,12
   * @return boolean
   */
  public boolean updateColsBy(String columns, String where, Object... paras) {
    return update(getDialect().update(getTableMeta().getTableName(), getAlias(), where, columns.split(",")), paras) > 0;
  }

  /**
   * 删除全部数据
   *
   * @return boolean
   */
  public boolean deleteAll() {
    logger.warn("You must ensure that \"deleteAll()\" method of safety.");
    return update(getDialect().delete(getTableMeta().getTableName())) > 0;
  }

  /**
   * 根据条件删除数据
   *
   * @param where 条件
   * @param paras 参数
   * @return
   */
  public boolean deleteBy(String where, Object... paras) {
    return update(getDialect().delete(getTableMeta().getTableName(), where), paras) > 0;
  }

  /**
   * COUNT 函数求和
   *
   * @return Long
   */
  public Long countAll() {
    return DS.useDS(getTableMeta().getDsName()).queryFirst(getDialect().count(getTableMeta().getTableName()));
  }

  /**
   * COUNT 根据条件函数求和
   *
   * @return Long
   */
  public Long countBy(String where, Object... paras) {
    return DS.useDS(getTableMeta().getDsName()).queryFirst(getDialect().count(getTableMeta().getTableName(), getAlias(), where), paras);
  }


  public String getAlias() {
    return alias;
  }

  /**
   * 表的别名
   *
   * @param alias 别名
   * @return model
   */
  public M setAlias(String alias) {
    this.alias = alias;
    return (M) this;
  }
}
