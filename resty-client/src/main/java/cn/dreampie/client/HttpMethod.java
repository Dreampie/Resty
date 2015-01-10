package cn.dreampie.client;

import java.util.*;

import cn.dreampie.common.util.Lister;


/**
 * Created by wangrenhui on 15/1/10.
 */
public class HttpMethod {
  public static final String GET = "GET";
  public static final String POST = "POST";
  public static final String PUT = "PUT";
  public static final String DELETE = "DELETE";
  public static final String PATCH = "PATCH";
  public static final String HEAD = "HEAD";

  public static final List<String> OUT_METHODS = Lister.of(POST, PUT, PATCH);
}
