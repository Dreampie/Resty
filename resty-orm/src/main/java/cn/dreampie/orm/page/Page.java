package cn.dreampie.orm.page;

import java.io.Serializable;
import java.util.List;

/**
 * FullPage is the result of Model.fullPaginate(......) or Record.fullPaginate(......)
 */
public class Page<T> implements Serializable {

  private List<T> list;        // list result of this page
  private int pageNumber;        // page number
  private int pageSize;        // result amount of this page

  /**
   * Constructor.
   *
   * @param list       the list of fullPaginate result
   * @param pageNumber the page number
   * @param pageSize   the page size
   */
  public Page(List<T> list, int pageNumber, int pageSize) {
    this.list = list;
    this.pageNumber = pageNumber;
    this.pageSize = pageSize;
  }

  /**
   * Return list of this page.
   */
  public List<T> getList() {
    return list;
  }

  /**
   * Return page number.
   */
  public int getPageNumber() {
    return pageNumber;
  }

  /**
   * Return page size.
   */
  public int getPageSize() {
    return pageSize;
  }

}


