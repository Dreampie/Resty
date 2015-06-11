package cn.dreampie.common.http;

/**
 * Created by wangrenhui on 15/1/10.
 */
public class HttpMethod {
  public static final String GET = "GET";
  public static final String POST = "POST";
  public static final String PUT = "PUT";
  public static final String DELETE = "DELETE";
  //jdk7- not support
  public static final String PATCH = "PATCH";
  //cors
  public static final String OPTIONS = "OPTIONS";
  //not filter
  public static final String HEAD = "HEAD";


  public static boolean support(String httpMethod) {
    return GET.equals(httpMethod) || POST.equals(httpMethod) || PUT.equals(httpMethod)
        || DELETE.equals(httpMethod) || PATCH.equals(httpMethod);
  }
}
