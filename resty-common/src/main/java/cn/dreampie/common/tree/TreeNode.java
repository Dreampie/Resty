package cn.dreampie.common.tree;

import java.util.Collection;

/**
 * @author Dreampie
 * @date 2015-11-17
 * @what
 */
public interface TreeNode<T> {

  public long getId();

  public long getPid();

  public Collection<T> getChildren();

  public void setChildren(Collection<T> children);
}
