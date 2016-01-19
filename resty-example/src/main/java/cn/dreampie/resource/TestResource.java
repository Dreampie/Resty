package cn.dreampie.resource;

import cn.dreampie.captcha.CaptchaFactory;
import cn.dreampie.captcha.service.Captcha;
import cn.dreampie.common.http.HttpResponse;
import cn.dreampie.common.http.UploadedFile;
import cn.dreampie.common.http.exception.WebException;
import cn.dreampie.common.http.result.HttpStatus;
import cn.dreampie.common.http.result.WebResult;
import cn.dreampie.common.util.Maper;
import cn.dreampie.orm.Record;
import cn.dreampie.orm.TableSetting;
import cn.dreampie.orm.transaction.Transaction;
import cn.dreampie.resource.user.model.User;
import cn.dreampie.route.annotation.*;
import cn.dreampie.route.core.Headers;
import cn.dreampie.route.core.multipart.FILE;
import cn.dreampie.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by wangrenhui on 15/1/10.
 */
@API("/tests")
public class TestResource extends ApiResource {

  @Autowired
  private HelloService helloService;

  @GET(value = "/:get", headers = {"Accept: application/vnd.resty.v1+json"})
  public WebResult<List<User>> get(String get, Date x, HttpResponse response) {
    throw new WebException(HttpStatus.NO_CONTENT, "xxx");
//    response.addCookie( "xxxx", "hh");
//    System.out.println("xxxxx" + x);
//    System.out.println(helloService.hello());
    //如果需要返回请求状态  new WebResult
//    return new WebResult<List<User>>(HttpStatus.OK, User.dao.findAll());
  }

  @POST("/:post")
  public Map post(String post, Map<String, String> params) {
    params.put("post", post);
    return params;
  }

  @PUT("/:put")
  public Map put(String put, Map<String, String> map) {
    map.put("put", put);
    return map;
  }

  @PATCH("/:patch")
  public Map patch(String patch) {
    return Maper.of("patch", patch);
  }

  @DELETE("/:delete")
  public Map delete(String delete) {
    Map<String, String> map = Maper.of("a", "1", "b", "2");
    map.remove(delete);
    return map;
  }

  @POST("/transaction")
  @Transaction(name = {"default", "demo"})
  public void transaction() {
    User u = new User().set("sid", 1).set("username", "a").set("providername", "test").set("password", "123456").set("created_at", new Date());
    u.save();
    Record record = new Record("demo", new TableSetting("sec_user"));
    Record user = record.reNew().set("sid", 1).set("username", "a").set("providername", "test").set("password", "123456").set("created_at", new Date());
    user.save();
//    throw new RuntimeException("xx");
  }

  //上传文件
  @POST("/file")
  @FILE(renamer = MyFileRenamer.class)
  public UploadedFile upload(UploadedFile testfile, String des) {
    //如果上传文件的同时 有参数  注意UploadedFile  参数的名字 需要和input的name对应
    System.out.println(des);
    return testfile;
  }

  //下载文件
  @GET("/file")
  public File download() {
    return new File(getRequest().getRealPath("/") + "upload/resty000.jar");
  }

  @GET("/headers")
  public Headers headers(Headers headers) {
    return headers;
  }

  @GET("/boom")
  public boolean test() {
    return new User().set("username", "test" + Thread.currentThread().getName()).set("providername", "test").set("created_at", new Date())
        .set("password", "123456").set("sid", "1").save();
  }

  @GET
  public String get() {
    return "ok";
  }

  private static CaptchaFactory captchaFactory = new CaptchaFactory();

  @GET(value = "/captcha", cached = false)
  public BufferedImage captcha() {
//    captchaFactory.setWordFactory(new RandomWordFactory("ABCDEFGHIJKLMNPQRSTUVWXYZ123456789",4));
//    captchaFactory.setColorFactory(new SingleColorFactory(new Color(25, 60, 170)));
//    captchaFactory.setBackgroundFactory(new RandomColorBackgroundFactory(new Color(25, 60, 170)));

    Captcha captcha = captchaFactory.getCaptcha();
    //把值存好
    System.out.println(captcha.getWord());

    return captcha.getImage();
  }
}
