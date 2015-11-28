package cn.dreampie.common.tree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Dreampie
 * @date 2015-11-17
 * @what
 */
public class Treer {

  private static Comparator<TreeNode> comparator = new Comparator<TreeNode>() {
    public int compare(TreeNode rp1, TreeNode rp2) {
      return new Long(rp1.getPid()).compareTo(rp2.getPid());
    }
  };

  public static List toTree(List params) {
    List nodes = null;
    if (params.size() > 0) {
      //优先级排序
      Collections.sort(params, comparator);
      nodes = toTree(params, ((TreeNode) params.get(0)).getPid());
    }
    return nodes;
  }

  public static List toTreeLevel(List params, int level) {
    List nodes = null;
    if (params.size() > 0) {
      //优先级排序
      Collections.sort(params, comparator);
      nodes = toTreeLevel(params, ((TreeNode) params.get(0)).getPid(), level);
    }
    return nodes;
  }

  /**
   * 无限级树形结构
   *
   * @param params params
   * @param pid    pid
   * @return list
   */
  public static List toTree(List params, long pid) {
    List nodes = new ArrayList();

    if (params != null && params.size() > 0) {

      TreeNode node = null;
      for (int i = 0; i < params.size(); i++) {
        node = (TreeNode) params.get(i);
        if (node.getPid() == pid) {
          nodes.add(node);
          params.remove(i);
          node.setChildren(toTree(params, node.getId()));
          i--;
        }
      }
    }
    return nodes;
  }

  /**
   * 两级树形数据
   *
   * @param params params
   * @param pid    pid
   * @param level  level
   * @return list
   */
  public static List toTreeLevel(List params, long pid, int level) {
    List nodes = new ArrayList();

    if (params != null && params.size() > 0) {
      TreeNode node = null;//当前节点
      for (int i = 0; i < params.size(); i++) {
        node = (TreeNode) params.get(i);
        if (node.getPid() == pid) {
          nodes.add(node);
          params.remove(i);
          if (level > 1) {
            node.setChildren(toTreeLevel(params, node.getId(), --level));
          } else {
            nodes.addAll(toTreeLevel(params, node.getId(), --level));
          }
          level++;
          i--;
        }
      }
    }
    return nodes;
  }


}
