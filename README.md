resty 一款restful的轻量级的web框架
===========

拥有jfinal，activejdbc一样的activerecord的简洁设计，使用极其简单


独有优点：
restful的api设计，是作为restful的服务端最佳选择（使用场景：客户端和服务端解藕，用于对静态的html客户端（mvvm等），ios，andriod等提供服务端的api接口）

极简的route设计:
```java
  @GET("/users/:name")  在路径中自定义解析的参数 如果有其他符合 也可以用 /users/{name}
  // 参数名就是方法变量名  除路径参数之外的参数也可以放在方法参数里  传递方式 user={json字符串}
  public Map find(String name,User user) {
    // return Lister.of(name);
    return Maper.of("k1", "v1,name:" + name, "k2", "v2");//返回什么数据直接return，完全融入普通方法的方式
  }
```

支持多数据源和嵌套事务（使用场景：需要访问多个数据库的应用，或者作为公司内部的数据中间件向客户端提供数据访问api等）

```java
  // 在resource里使用事务,也就是controller里，rest的世界认为所以的请求都表示资源，所以这儿叫resource
  @GET("/users")
  @Transaction(name = {"default", "demo"}) //多数据源的事务，如果你只有一个数据库  直接@Transaction 不需要参数
  public User transaction() {
  //TODO 用model执行数据库的操作  只要有操作抛出异常  两个数据源 都会回滚  虽然不是分布式事务  也能保证代码块的数据执行安全
  }

  // 如果你需要在service里实现事务，通过java动态代理（必须使用接口，jdk设计就是这样）
  public interface UserService {
    @Transaction(name = {"default", "demo"})//service里添加多数据源的事务，如果你只有一个数据库  直接@Transaction 不需要参数
    public User save(User u);
  }
  // 在resource里使用service层的 事务
  // @Transaction(name = {"default", "demo"})的注解需要写在service的接口上
  // 注意java的自动代理必须存在接口
  // TransactionAspect 是事务切面 ，你也可以实现自己的切面比如日志的Aspect，实现Aspect接口
  // 再private UserService userService = AspectFactory.newInstance(new UserServiceImpl(), new TransactionAspect(),new LogAspect());
  private UserService userService = AspectFactory.newInstance(new UserServiceImpl(), new TransactionAspect());

```

极简的缓存设计，可扩展，非常简单即可启用model的自动缓存功能

```java
  public void configConstant(ConstantLoader constantLoader) {
    //启用缓存并在要自动使用缓存的model上  开启缓存@Table(name = "sec_user", cached = true)
    constantLoader.setCacheEnable(true);
  }
  
  @Table(name = "sec_user", cached = true)
  public class User extends Model<User> {
    public static User dao = new User();

  }
```

当然也是支持传统的web开发，你可以自己实现数据解析，在config里添加自定义的解析模板

```java
  public void configConstant(ConstantLoader constantLoader) {
    // 通过后缀来返回不同的数据类型  你可以自定义自己的  render  如：FreemarkerRender
    // constantLoader.addRender("json", new JsonRender());//默认已添加json和text的支持，只需要把自定义的Render add即可
  }
```


运行example示例：

1.运行根目录下的pom.xml->install （把相关的插件安装到本地，稳定版之后发布到maven就不需要这样了）

2.在本地mysql数据库里创建demo,example数据库，对应application.properties的数据库配置

3.运行resty-example下的pom.xml->flyway-maven-plugin:migration，生成resources下得数据库表创建文件

4.运行resty-example下的pom.xml->tomcat7-maven-plugin:run,启动example程序

注意:推荐idea作为开发ide，使用分模块的多module开发


