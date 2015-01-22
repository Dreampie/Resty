package cn.dreampie.common.util.analysis;

import java.util.List;

/**
 * Created by ice on 15-1-22.
 */
public class ParamAttribute {
  private List<String> names;
  private int[] lines;

  public ParamAttribute(List<String> names, int[] lines) {
    this.names = names;
    this.lines = lines;
  }

  public List<String> getNames() {
    return names;
  }

  public void setNames(List<String> names) {
    this.names = names;
  }

  public int[] getLines() {
    return lines;
  }

  public void setLines(int[] lines) {
    this.lines = lines;
  }
}
