# Maven

我为什么要学习这个技术？ 

1. 在Javaweb开发中，需要使用大量的jar包，我们手动去导入； 
2. 如何能够让一个东西自动帮我导入和配置这个jar包。 由此，Maven诞生了！



### 1、 Maven项目架构管理工具 

我们目前用来就是方便导入jar包的！ 

Maven的核心思想：约定大于配置

- 有约束，不要去违反。

Maven会规定好你该如何去编写我们的Java代码，必须要按照这个规范来；



### 2、 下载安装Maven

官网;https://maven.apache.org/

![image-20210222180353974](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210222180353974.png)

下载完成后，解压即可；
小狂神友情建议：电脑上的所有环境都放在一个文件夹下，方便管理；



### 3 配置环境变量

在我们的系统环境变量中

配置如下配置：

- M2_HOME maven目录下的bin目录
- MAVEN_HOME maven的目录
- 在系统的path中配置 %MAVEN_HOME%\bin



测试Maven是否安装成功（mvn -version)，保证必须配置完毕！



### 4、 阿里云镜像

镜像：mirrors
作用：加速我们的下载 国内建议使用阿里云的镜像

```xml
<mirror> 
    <id>nexus-aliyun</id> 
    <mirrorOf>*,!jeecg,!jeecg-snapshots</mirrorOf> 
    <name>Nexus aliyun</name>               <url>http://maven.aliyun.com/nexus/content/groups/public</url>
</mirror>
```



### 5.5 本地仓库 

在本地的仓库，远程仓库； 

建立一个本地仓库：localRepository

```xml
<localRepository>D:\Environment\apache-maven-3.6.2\mavenrepo</localRepository>
```



### 5.6、在IDEA中使用Maven 

1. 启动IDEA 
2. 2. 创建一个MavenWeb项目

![image-20210224130704018](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224130704018.png)

![image-20210224130734167](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224130734167.png)

![image-20210224130803185](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224130803185.png)

3. 等待项目初始化完毕

![image-20210224130853215](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224130853215.png)

![image-20210224130921303](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224130921303.png)

4. 观察maven仓库中多了什么东西？
5. IDEA中的Maven设置 
	注意：IDEA项目创建成功后，看一眼Maven的配置

![image-20210224131238988](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224131238988.png)

![image-20210224131304885](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224131304885.png)

6. 到这里，Maven在IDEA中的配置和使用就OK了! 



### 5.7、创建一个普通的Maven项目

![image-20210224131501754](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224131501754.png)

![image-20210224131550379](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224131550379.png)



这个只有在Web应用下才会有！

![image-20210224131631454](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224131631454.png)



### 5.8 标记文件夹功能

![image-20210224131827715](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224131827715.png)

![image-20210224131847006](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224131847006.png)

![image-20210224131910870](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224131910870.png)

![image-20210224132029255](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224132029255.png)



### 5.9 在 IDEA中配置Tomcat

![image-20210224132116629](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224132116629.png)

![image-20210224132148475](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224132148475.png)

![image-20210224132212672](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224132212672.png)

![image-20210224132241003](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224132241003.png)

解决警告问题 必须要的配置：

为什么会有这个问题：我们访问一个网站，需要指定一个文件夹名字；

![image-20210224132352435](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224132352435.png)

![image-20210224132430123](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224132430123.png)

![image-20210224132503391](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224132503391.png)



### 5.10 pom文件 

pom.xml 是Maven的核心配置文件

![image-20210224132725055](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224132725055.png)

```xml
<?xml version="1.0" encoding="UTF-8"?>

<!-- Maven版本和头文件-->
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <!--这里就是我们刚才配置的GAV-->
  <groupId>com.zx</groupId>
  <artifactId>javaweb-01-maven</artifactId>
  <version>1.0-SNAPSHOT</version>
  <!--Package：项目的打包方式 
  jar：java应用 
  war：JavaWeb应用 -->
  <packaging>war</packaging>

  <name>javaweb-01-maven Maven Webapp</name>
  <!-- FIXME change it to the project's website -->
  <url>http://www.example.com</url>

  <!--配置-->
  <properties>
    <!--项目的默认构建编码-->
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <!--编码版本-->
    <maven.compiler.source>1.7</maven.compiler.source>
    <maven.compiler.target>1.7</maven.compiler.target>
  </properties>

  <!--项目依赖-->
  <dependencies>
    <!--具体依赖的jar包配置文件-->
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.11</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>javax.servlet</groupId>
      <artifactId>javax.servlet-api</artifactId>
      <version>4.0.0</version>
    </dependency>

  </dependencies>

  <!--项目构建用的东西-->
  <build>
    <finalName>javaweb-01-maven</finalName>
    <pluginManagement><!-- lock down plugins versions to avoid using Maven defaults (may be moved to parent pom) -->
      <plugins>
        <plugin>
          <artifactId>maven-clean-plugin</artifactId>
          <version>3.1.0</version>
        </plugin>
        <!-- see http://maven.apache.org/ref/current/maven-core/default-bindings.html#Plugin_bindings_for_war_packaging -->
        <plugin>
          <artifactId>maven-resources-plugin</artifactId>
          <version>3.0.2</version>
        </plugin>
        <plugin>
          <artifactId>maven-compiler-plugin</artifactId>
          <version>3.8.0</version>
        </plugin>
        <plugin>
          <artifactId>maven-surefire-plugin</artifactId>
          <version>2.22.1</version>
        </plugin>
        <plugin>
          <artifactId>maven-war-plugin</artifactId>
          <version>3.2.2</version>
        </plugin>
        <plugin>
          <artifactId>maven-install-plugin</artifactId>
          <version>2.5.2</version>
        </plugin>
        <plugin>
          <artifactId>maven-deploy-plugin</artifactId>
          <version>2.8.2</version>
        </plugin>
      </plugins>
    </pluginManagement>
  </build>
</project>

```

![image-20210224133439067](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224133439067.png)



maven由于他的约定大于配置，我们之后可以能遇到我们写的配置文件，无法被导出或者生效的问题， 

解决方案：

```xml
<!--在build中配置resources，来防止我们资源导出失败的问题--> 
<build> 
    <resources> 
        <resource> 
            <directory>src/main/resources</directory> 
            <includes> 
                <include>**/*.properties</include> 
                <include>**/*.xml</include>
            </includes> 
            <filtering>true</filtering>
        </resource>
        <resource> 
            <directory>src/main/java</directory> 
            <includes> 
                <include>**/*.properties</include> 
                <include>**/*.xml</include>
            </includes> 
            <filtering>true</filtering>
        </resource> 
    </resources>
</build>
```



### 5.12 IDEA操作

![image-20210224133818035](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224133818035.png)

![image-20210224133848278](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224133848278.png)



5.13 解决遇到的问题 

1. Maven 3.6.2 
	解决方法：降级为3.6.1
	
	​	![image-20210224134041444](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224134041444.png)
	
2. Tomcat闪退



3. IDEA中每次都要重复配置Maven 
	在IDEA中的全局默认配置中去配置

![image-20210224134215477](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224134215477.png)

![image-20210224134244978](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224134244978.png)

4. Maven项目中Tomcat无法配置 
5. maven默认web项目中的web.xml版本问题

![image-20210224134349552](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224134349552.png)

6. 替换为webapp4.0版本和tomcat一致

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee
         http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0"
         metadata-complete="true">
    
</web-app>
```

7. Maven仓库的使用 
	地址：[https://mvnrepository.com/](https://mvnrepository.com/)

![image-20210224134655991](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224134655991.png)

![image-20210224134711144](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224134711144.png)

![image-20210224134746555](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224134746555.png)

![image-20210224134804110](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210224134804110.png)