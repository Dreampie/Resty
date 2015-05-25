package cn.dreampie.orm.page;

import java.util.List;

/**
 * FullPage is the result of Model.fullPaginate(......) or Record.fullPaginate(......)
 */
public class FullPage<T> extends Page<T> {

  private int totalPage;        // total page
  private int totalRow;        // total row

  /**
   * Constructor.
   *
   * @param list       the list of fullPaginate result
   * @param pageNumber the page number
   * @param pageSize   the page size
   * @param totalPage  the total page of fullPaginate
   * @param totalRow   the total row of fullPaginate
   */
  public FullPage(List<T> list, int pageNumber, int pageSize, int totalPage, int totalRow) {
    super(list, pageNumber, pageSize);
    this.totalPage = totalPage;
    this.totalRow = totalRow;
  }

  /**
   * Return total page.
   */
  public int getTotalPage() {
    return totalPage;
  }

  /**
   * Return total row.
   */
  public int getTotalRow() {
    return totalRow;
  }
}


