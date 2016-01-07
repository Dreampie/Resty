package cn.dreampie.example;

import cn.dreampie.orm.Record;
import cn.dreampie.orm.TableSetting;
import cn.dreampie.orm.callable.ObjectCall;
import cn.dreampie.orm.callable.ResultSetCall;
import cn.dreampie.orm.page.FullPage;
import cn.dreampie.resource.user.model.User;
import cn.dreampie.resource.user.model.UserInfo;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

/**
 * Created by ice on 15-1-4.
 */
@FixMethodOrder(MethodSorters.JVM)
public class SqlTest {

  private TableSetting tableSetting = new TableSetting("sec_user", true);

  @Before
  public void setUp() throws Exception {
    ActiveRecord.init();
  }

  @Test
  public void testSql() {
    Record recordDAO = new Record();

    List<Record> records = recordDAO.find("select * from sec_user");
    System.out.println(records.size());

    recordDAO.execute("create table tb1 select * from sec_user");
    List<Record> records1 = recordDAO.find("select * from tb1");
    System.out.println(records1.size());

    recordDAO.execute("insert into tb1(sid,username,password,providername,created_at)values(1000,'xx','11','12','2014-10-00 10:00:00');");
  }


  @Test
  public void testSave() {
    User u = new User().set("sid", 1).set("username", "a").set("providername", "test").set("password", "123456").set("created_at", new Date());
    u.save();
    User u1 = new User().set("sid", 1).set("username", "a").set("providername", "test").set("password", "123456").set("created_at", new Date());
    User u2 = new User().set("sid", 1).set("username", "a").set("providername", "test").set("password", "123456").set("created_at", new Date());
    UserInfo userInfo = null;
    if (u.get("user_info") == null) {
      userInfo = new UserInfo().set("gender", 0);
    } else {
      userInfo = u.get("user_info");
    }
    if (User.dao.save(u1, u2)) {
      System.out.println(u.get("id") + "/" + u1.get("id"));
      userInfo.set("user_id", u.get("id"));
      userInfo.save();
    }

    Record recordDAO = new Record(tableSetting);
    recordDAO.reNew().set("sid", 2).set("username", "test").set("providername", "test").set("password", "123456").set("created_at", new Date()).save();
    Record r1 = recordDAO.reNew().set("sid", 2).set("username", "test").set("providername", "test").set("password", "123456").set("created_at", new Date());
    Record r2 = recordDAO.reNew().set("sid", 2).set("username", "test").set("providername", "test").set("password", "123456").set("created_at", new Date());


    recordDAO.save(r1, r2);
  }

  @Test
  public void testFind() {
    List<User> users = User.dao.findAll();
    Long id = 1L, sid = 1L;
    for (User user : users) {
      id = user.<Long>get("id");
      sid = user.<Long>get("sid");
      System.out.println(user.<String>get("username"));
    }

    Record recordDAO = new Record(tableSetting);
    List<Record> records = recordDAO.findAll();
    for (Record r : records) {
      System.out.println(r.<String>get("username"));
    }

    User us = User.dao.findByIds(id, sid);
    System.out.println("findByIds," + us.get("id") + "," + us.get("sid"));
  }

  @Test
  public void testPaginate() {
    FullPage<User> users = User.dao.unCache().fullPaginateAll(1, 10);
    for (User user : users.getList()) {
      System.out.println(user.<String>get("username"));
    }
    Record recordDAO = new Record(tableSetting);
    FullPage<Record> records = recordDAO.fullPaginate(1, 10, "SELECT * FROM sec_user");
    for (Record record : records.getList()) {
      System.out.println(record.<String>get("username"));
    }

    records = recordDAO.unCache().fullPaginate(1, 10, "SELECT * FROM sec_user");
  }

  @Test
  public void testUpdate() {
    List<User> users = User.dao.findAll();
    for (User user : users) {
      user.set("username", "testupdate").update();
    }
    User.dao.update("UPDATE sec_user SET username='c' WHERE username='a'");
    Record recordDAO = new Record(tableSetting);
    List<Record> records = recordDAO.findAll();
    int i = 0;
    for (Record record : records) {
      if (i % 2 == 0)
        record.set("username", "testupdxx").update();
      i++;
    }
  }

  @Test
  public void testExcute() {
    //批量执行sql语句
    User.dao.execute("UPDATE sec_user SET username='b' WHERE username='c'", "UPDATE sec_user SET username='x' WHERE username='test'");
  }


  @Test
  public void testDelete() {
    List<User> users = User.dao.findAll();
    Long id = 1L, sid = 1L;
    int i = 0;
    for (User user : users) {
      id = user.<Long>get("id");
      sid = user.<Long>get("sid");
      if (i == 0) {
        User.dao.deleteByIds(id, sid);
      }
      user.delete();
      i++;
    }
    Record recordDAO = new Record(tableSetting);
    ;
    recordDAO.deleteById("1");
  }

  //@Test
  public void testProcess() {
    //返回一个指定类型的值
    Integer r = User.dao.queryCall("{CALL PROCESS(?,?)}", new ObjectCall() {
      public Object call(CallableStatement cstmt) throws SQLException {
        cstmt.setInt(1, 1);
        cstmt.registerOutParameter(2, Types.BIGINT);
        cstmt.execute();
        cstmt.executeQuery();
        return cstmt.getInt(2);
      }
    });

    //返回ResultSet结果集 查询的某个类型的值
    String str = User.dao.queryCallFirst("{CALL PROCESS(?,?)}", new ResultSetCall() {
          public ResultSet call(CallableStatement cstmt) throws SQLException {
            return null;
          }
        }
    );
    //返回ResultSet结果集 查询的某个类型的值
    List<String> rs = User.dao.queryCall("{CALL PROCESS(?,?)}", new ResultSetCall() {
          public ResultSet call(CallableStatement cstmt) throws SQLException {
            return null;
          }
        }
    );
    //返回ResultSet结果集 封装成User的model对象
    User user = User.dao.findCallFirst("{CALL PROCESS(?,?)}", new ResultSetCall() {
      public ResultSet call(CallableStatement cstmt) throws SQLException {
        //操作
        return null;
      }
    });

    //返回ResultSet结果集 封装成user的集合类型
    List<User> userList = User.dao.findCall("{CALL PROCESS(?,?)}", new ResultSetCall() {
      public ResultSet call(CallableStatement cstmt) throws SQLException {
        //操作
        return null;
      }
    });
  }
}
