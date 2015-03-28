package cn.dreampie.resource;

import cn.dreampie.resource.user.model.User;

import javax.servlet.*;
import java.io.IOException;
import java.util.Date;

/**
 * Created by wangrenhui on 15/3/27.
 */

public class TestServlet implements Servlet {

  public void init(ServletConfig config) throws ServletException {

  }

  public ServletConfig getServletConfig() {
    return null;
  }

  public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
    new User().set("username", "test" + Thread.currentThread().getName()).set("providername", "test").set("created_at", new Date())
        .set("password", "123456").set("sid", "1").save();
  }

  public String getServletInfo() {
    return null;
  }

  public void destroy() {

  }
}
