package cn.dreampie;

import cn.dreampie.common.http.HttpStatus;
import cn.dreampie.common.http.WebResult;
import cn.dreampie.common.util.Maper;
import cn.dreampie.route.core.annotation.DELETE;
import cn.dreampie.route.core.annotation.GET;
import cn.dreampie.route.core.annotation.POST;
import cn.dreampie.route.core.annotation.PUT;
import cn.dreampie.security.Principal;
import cn.dreampie.security.Subject;
import cn.dreampie.upload.UploadedFile;

import java.io.File;
import java.util.Map;

/**
 * Created by wangrenhui on 15/1/10.
 */
public class TestResource extends ApiResource {


  @POST("/tests/login")
  public Principal login(String username, String password) {
    Subject.login(username, password);
    return Subject.getPrincipal();
  }

  @GET("/tests")
  public WebResult get() {
    //如果需要返回请求状态  new WebResult
    return new WebResult(HttpStatus.OK, Maper.of("a", "1", "b", "2"));
  }

  @POST("/tests")
  public Map post(Map<String, String> test) {
    return test;
  }

  @PUT("/tests/:b")
  public Map put(String b) {
    Map<String, String> map = Maper.of("a", "1", "b", "2");
    map.put("b", b);
    return map;
  }

  @DELETE("/tests/:key")
  public Map delete(String key) {
    Map<String, String> map = Maper.of("a", "1", "b", "2");
    map.remove(key);
    return map;
  }

  //上传文件
  @POST("/tests/:filename")
  public UploadedFile upload(String filename) {
    //如果上传文件的同时 有参数
    UploadedFile uploadedFile = getFile(filename);
    System.out.println(getParameters("des"));
    return uploadedFile;
  }

  //下载文件
  @GET("/tests/file")
  public File download() {
    return new File(getRequest().getRealPath("/") + "upload/resty.jar");
  }


}
