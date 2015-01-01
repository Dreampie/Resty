resty 一款restful的轻量级的web框架
===========


运行example示例：

1.运行根目录下的pom.xml->install （把相关的插件安装到本地，稳定版之后发布到maven就不需要这样了）

2.在本地mysql数据库里创建demo数据库，对应application.properties的数据库配置

3.运行resty-example下的pom.xml->flyway-maven-plugin:migration，生成resources下得数据库表创建文件

4.运行resty-example下的pom.xml->tomcat7-maven-plugin:run,启动example程序

注意:推荐idea作为开发ide，使用分模块的多module开发

