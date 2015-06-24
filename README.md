
Resty 一款极简的restful轻量级的web框架
===========

<a href="http://dreampie.gitbooks.io/resty-chs/content/index.html" target="_blank">开发文档</a>

如果你还不是很了解restful，或者认为restful只是一种规范不具有实际意义，推荐一篇osc两年前的文章：[RESTful API 设计最佳实践](http://www.oschina.net/translate/best-practices-for-a-pragmatic-restful-api)  和 Infoq的一篇极其理论的文章  [理解本真的REST架构风格](http://www.infoq.com/cn/articles/understanding-restful-style) 虽然有点老，介绍的也很简单，大家权当了解，restful的更多好处，还请google

拥有jfinal/activejdbc一样的activerecord的简洁设计，使用更简单的restful框架

restful的api设计，是作为restful的服务端最佳选择（使用场景：客户端和服务端解藕，用于对静态的html客户端（mvvm等），ios，andriod等提供服务端的api接口）

Java开发指南:[Java style guide](https://github.com/Dreampie/java-style-guide)

Api设计指南:[Http api design](https://github.com/Dreampie/http-api-design-ZH_CN)

Resty例子：[resty-demo](https://github.com/Dreampie/resty-demo)

开发群: <a target="_blank" href="http://shang.qq.com/wpa/qunwpa?idkey=8fc9498714ebbc3675cc5a5035858004154ef4645ebc9c128dfd76688d32179b"><img border="0" src="http://pub.idqqimg.com/wpa/images/group.png" alt="极简Restful框架 - Resty" title="极简Restful框架 - Resty"></a>

其他开发者贡献的插件:[Beetl扩展(大鹏)](https://github.com/zhaopengme/Resty-ext)

maven使用方式：

1. 添加maven snapshots仓库
```xml
<repositories>
    <repository>
      <id>oss-snapshots</id>
      <url>https://oss.sonatype.org/content/repositories/snapshots</url>
      <releases>
        <enabled>false</enabled>
      </releases>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
    </repository>
  </repositories>
```
2. 添加依赖包
```xml
<dependency>
    <groupId>cn.dreampie</groupId>
    <artifactId>resty-route</artifactId>
    <version>2.0-SNAPSHOT</version>
</dependency>
```

提醒:推荐idea作为开发ide，使用分模块的多module开发

License <a href="https://www.apache.org/licenses/LICENSE-2.0" target="_blank">Apache License V2</a>


