# ![img](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAeCAYAAAA7MK6iAAAAAXNSR0IArs4c6QAABH1JREFUSA3tVl1oHFUUPmdmd2ltklqbpJDiNnXFmgbFktho7YMPNiJSSZM0+CAYSkUELVhM6YuwIPpgoOKDqOBDC0XE2CQoNtQXBUFTTcCi+Wlh1V2TQExsUzcltd3M9Tt3ZjZzZ2fT+OJTL8yeM+eee757fmeJbq//KQL8X3DUSFOcfr7cRsRtxNQMWueeVzOkaITIGqQHNg5y8+jNW9ldM7A6nTpAjuolUikAwq7CE3WcM2RRDz+XGVgN3FptU/aUSlvq9Pa3iZ1+sgAqJyyAFqkipd9dqiwHF3P65YycLWc/6sqGrvoEoIp6DOFaX5h6+dnfjkWprwqsPk0dUGq5vySwDImC10KxFHgGL1SWoc92O3eVht09qdXNH11I2SsTsJYqMWzihqGMi+A+Garf3BAuuLI5oGlULyNfyB/HYNujwktOfRrMr5t77NmevqaUopx0grnKAyvVpmwUDB4x6FPXuGvYLTDwWsejwgtgkYKPqRJg8SV6xaiZ3ZTppGneS4yfH5/66fZSDHv+QZci/+h5c5UHtpy67JUqGppM0sh0Nc1dW6/N1W5Yoqat8/TU/VnadmdeW2PLLSyh0cvxBs3KbqTmwYPpxN4do/mzE8nEpvX/UMu2Wbp74zUAK5q6WkHns7V0eWkdPbPzd3rxkTGybadYySumVzhcaJFbs5UrEkQ/+CK8gF5dnh/6ciIZ73gwQ927L1IitoxKLXYP3SjYdOrHHfTZhRRlFyrorafPk20B3HPD1y2G3qKZME5Jcf3t/HUC13/8tSd++vqFveMUTwAUxSUFI1QekR1+bIze3D9MF2aq6cPvG72CgnldWCFqyRw3lwH8ZMerjTD9ElRO7Gv44wNpC90aASqGfVlz/Rx17srQ57/UU26hkhQqUB7dBR71WmzQhHUnblGmVOEw0jhbV1n9OlXUDCIRGaNV5Jp43N516fN7JmnTHdfp7Hgy0luO4aMhtkLL8Bi3bUWYvzh5Mn1dTxrL6QmGuRhGL/TiTTxRoEdTszSaq9GR0NGA3KdkOz3hqSV3MIDhQ5IVX/Ivx3umBti2es2h4eZby7x8br1rkf7Mo90AqC8aQ3sJeNzqFRu+vSANAQe3PL7l0HGOAdwDCeZYvNKeoZp1Qfs6Aipndh86HmFRi0LAnEO47wsqM6cdfjh3jBPUzhZy7nvlUfFsamED1VQt6aISHVymXZ/B2aCtIG8AI8xfobj2d3en1wWVhOeHELKmLQ1s211s88comkv4UCwWyF787mJdYXtNfhKAXVqnKTq8QZvGAGGOfaTo5pGZ/PwbUCr5+DPr/1J92JNHr9aOl/F3iI5+O1nfybsGxoimvZ3ViWSluDITw3P37mypheDIPY0tw7+O/5ApbkYw+zpfaUVu32Pi98+defdUhEpZkRFq0aqyNh9FuL9hpYbEm6iwi0z2REd09ZmyENEbuhjDWzKvZXTqKYaBIr3tt5kuPtQBZFvEUwHt60vfCNu41XsksH9Ij1BMMz1Y0OOunHNShFIP5868g5zeXmuLwL9T4b6Q2+KejgAAAABJRU5ErkJggg==) 简介

[MyBatis-Plus (opens new window)](https://github.com/baomidou/mybatis-plus)（简称 MP）是一个 [MyBatis (opens new window)](https://www.mybatis.org/mybatis-3/)的增强工具，在 MyBatis 的基础上只做增强不做改变，为简化开发、提高效率而生。



> JPA、tk-mapper、MyBatisPlus三者类似



愿景

我们的愿景是成为 MyBatis 最好的搭档，就像 [魂斗罗](https://baomidou.com/img/contra.jpg) 中的 1P、2P，基友搭配，效率翻倍。

![img](https://baomidou.com/img/relationship-with-mybatis.png)

## [#](https://baomidou.com/pages/24112f/#特性)特性

- **无侵入**：只做增强不做改变，引入它不会对现有工程产生影响，如丝般顺滑
- **损耗小**：启动即会自动注入基本 CURD，性能基本无损耗，直接面向对象操作
- **强大的 CRUD 操作**：内置通用 Mapper、通用 Service，仅仅通过少量配置即可实现单表大部分 CRUD 操作，更有强大的条件构造器，满足各类使用需求
- **支持 Lambda 形式调用**：通过 Lambda 表达式，方便的编写各类查询条件，无需再担心字段写错
- **支持主键自动生成**：支持多达 4 种主键策略（内含分布式唯一 ID 生成器 - Sequence），可自由配置，完美解决主键问题
- **支持 ActiveRecord 模式**：支持 ActiveRecord 形式调用，实体类只需继承 Model 类即可进行强大的 CRUD 操作
- **支持自定义全局通用操作**：支持全局通用方法注入（ Write once, use anywhere ）
- **内置代码生成器**：采用代码或者 Maven 插件可快速生成 Mapper 、 Model 、 Service 、 Controller 层代码，支持模板引擎，更有超多自定义配置等您来使用
- **内置分页插件**：基于 MyBatis 物理分页，开发者无需关心具体操作，配置好插件之后，写分页等同于普通 List 查询
- **分页插件支持多种数据库**：支持 MySQL、MariaDB、Oracle、DB2、H2、HSQL、SQLite、Postgre、SQLServer 等多种数据库
- **内置性能分析插件**：可输出 SQL 语句以及其执行时间，建议开发测试时启用该功能，能快速揪出慢查询
- **内置全局拦截插件**：提供全表 delete 、 update 操作智能分析阻断，也可自定义拦截规则，预防误操作

## [#](https://baomidou.com/pages/24112f/#支持数据库)支持数据库

> 任何能使用 `MyBatis` 进行 CRUD, 并且支持标准 SQL 的数据库，具体支持情况如下，如果不在下列表查看分页部分教程 PR 您的支持。

- MySQL，Oracle，DB2，H2，HSQL，SQLite，PostgreSQL，SQLServer，Phoenix，Gauss ，ClickHouse，Sybase，OceanBase，Firebird，Cubrid，Goldilocks，csiidb
- 达梦数据库，虚谷数据库，人大金仓数据库，南大通用(华库)数据库，南大通用数据库，神通数据库，瀚高数据库

## [#](https://baomidou.com/pages/24112f/#框架结构)框架结构

![framework](https://baomidou.com/img/mybatis-plus-framework.jpg)



# ![img](data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAB4AAAAeCAYAAAA7MK6iAAAAAXNSR0IArs4c6QAABKFJREFUSA3tVl1oFVcQnrMbrak3QUgkya1akpJYcrUtIqW1JvFBE9LiQ5v6JmJpolbMg32rVrhgoYK0QiMY6i9Y6EMaW5D+xFJaTYItIuK2Kr3+BJNwkxBj05sQY3b3nM6cs2dv9t7NT/vQJw/sndk5M/PNzJkzewGerP+pAmy+ON8lLzUJgA8ZYxYIYZmGYRnctDaWvJJAmTtfP1pvXsBCCPP8QFcCaRkZYACgDZFO4stNIcBCajEOlmmC9XpJ9bAGCaPaPmzPl32dvLSVu3BWCTQs0XQQ6g0DYgwLIoAZbBCdW/i+781o1VVlm/410mw4h06Y7bIPHNyWDyL4FHkX03Q8SrzNhZTZriieckWt7cL6MM85YcLpsi/7O9/iXFT6MswI0DmmpkSaJ0qLxFIm3+i1THHB3zmBH3PYx9CcykcLOeQVVa7QtdxTgQgEleX2AjHYfwA+2ddV77ruGoJUbhGDI09YSNXyMpUt5ylOzxgbUmtOp7NmbNt8v3arjTBfYELmLUV+M+nSawNNAUqpT3ClJWg5I3BLT+cGW/DXNGCa6tx1aakCGEigArTn4TDIPdrXXYKCZNrHLMCOEPvHBlLQ99s9eHB7EB6NTki73CVPQ2F5MSx/uRQixfmq7rK0wYD8w8E905bnPDfwoWs/rfv93NWN/ZfvwsLIU7A09gxECyISeGJkHAau98L97tuw7NXnoPyNF8FcYGLGKsOs0mN3OEyec9esGW/ZEl945dTP34wlR2FZVQWU1q0Cw8Tr7p+hgLLNL0FPxx/Q35mA8aEUrH6nCgwEl0tn7wUiZYJnNRh6DK4UH/k0lfyrsBKdPVv/AriGIQcEDQZ65LBAGe2Rzui9Ybjz7XUppz1/uKBbyVPGkN3ZAeC6hr0x7Nr38N5+EqkoOm17xpoqR9ohQF55ERSvr4Dkr3chNfC3DMzGJlNBElW8w9nsGQvhNGIzDkXzCg8cLK951xHsFBlTJspJNi3ZFIMF2AeDV3q8DNOB+YHi6QTrChDIWDBRi5U5f+ZMfJLu3ccrqxtdxk4SKH336LFxSmkqefwU5T8fhdSdQf9IVKD6aNiwI/hnmcAZ91isYMJIaCUCx9W098+LgruikeTqzqqxKPUwqJyCPJiyemVVZBOijDGjD38Os0jOiSPL1z3SPjXNANbiNPXAdzTfukjjuknNBbyz3nwgTd3AVFqUJ5hpHlq9MveLnWwttUfoygBmvVjuikxND3znrhsELnZk7k+OjIGxeNEkomyLVta0xxn+HZhjBc4YZ/AFjHjz9u3xRZl2BN4aq9nFwWh16IrQ1aHHEd3j1+4/dB9OtH4e29A2H1DyHQRmOSfQZ1Fy7MHBTGB6J/Djq6p3OxyO2cB+4Car7v/o3GXgfAkj23+x9ID1Teoamo/SXcbvSf2PX7Vc8DdCmE1vN9di+32P9/5YR3vLnhCVGUWBjEkr3yh4H8v9CzmsbdhzOKzsJKM90iFdaTMjRPhGVsakRvOaRidljo6H6G7j+ctrJpsP+4COhDIl0La2+FS4+5mlocBaXY5QnGZysIBYoeSsl5qQzrSj/cgNrfuEzlWBfwA+EjrZyWUvpAAAAABJRU5ErkJggg==) 快速开始

我们将通过一个简单的 Demo 来阐述 MyBatis-Plus 的强大功能，在此之前，我们假设您已经：

- 拥有 Java 开发环境以及相应 IDE
- 熟悉 Spring Boot
- 熟悉 Maven

------

现有一张 `User` 表，其表结构如下：

| id   | name   | age  | email              |
| ---- | ------ | ---- | ------------------ |
| 1    | Jone   | 18   | test1@baomidou.com |
| 2    | Jack   | 20   | test2@baomidou.com |
| 3    | Tom    | 28   | test3@baomidou.com |
| 4    | Sandy  | 21   | test4@baomidou.com |
| 5    | Billie | 24   | test5@baomidou.com |

其对应的数据库 Schema 脚本如下：

```sql
DROP TABLE IF EXISTS user;

CREATE TABLE user
(
    id BIGINT(20) NOT NULL COMMENT '主键ID',
    name VARCHAR(30) NULL DEFAULT NULL COMMENT '姓名',
    age INT(11) NULL DEFAULT NULL COMMENT '年龄',
    email VARCHAR(50) NULL DEFAULT NULL COMMENT '邮箱',
    PRIMARY KEY (id)
);

-- 真实开发中，version(乐观锁)、deleted(逻辑删除)、gmt_create(创建时间)、gmt_modified(修改时间)
```



其对应的数据库 Data 脚本如下：

```sql
DELETE FROM user;

INSERT INTO user (id, name, age, email) VALUES
(1, 'Jone', 18, 'test1@baomidou.com'),
(2, 'Jack', 20, 'test2@baomidou.com'),
(3, 'Tom', 28, 'test3@baomidou.com'),
(4, 'Sandy', 21, 'test4@baomidou.com'),
(5, 'Billie', 24, 'test5@baomidou.com');
```

------

Question

如果从零开始用 MyBatis-Plus 来实现该表的增删改查我们需要做什么呢？

## [#](https://baomidou.com/pages/226c21/#初始化工程)初始化工程

创建一个空的 Spring Boot 工程（工程将以 H2 作为默认数据库进行演示）

提示

可以使用 [Spring Initializer (opens new window)](https://start.spring.io/)快速初始化一个 Spring Boot 工程

## [#](https://baomidou.com/pages/226c21/#添加依赖)添加依赖

引入 Spring Boot Starter 父工程：

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.6.3</version>
    <relativePath/>
</parent>
```

引入 `spring-boot-starter`、`spring-boot-starter-test`、`mybatis-plus-boot-starter`、`h2` 依赖：

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
   <!--数据库驱动-->
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <!--lombok-->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.10</version>
        </dependency>
        <!--mybatis-plus-->
        <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.0.5</version>
        </dependency>
</dependencies>
```

说明：我们使用mybatis-plus可以简化大量的代码，尽量不要同时导入mybatis和mybatis-plus！版本差异！



## [#](https://baomidou.com/pages/226c21/#配置)配置

在 `application.yml` 配置文件中添加 MySQL 数据库的相关配置：

```yaml
# DataSource Config
# mysql 5 驱动不同 com.mysql.jdbc.Driver
spring:
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/mybatis_plus?useSSL=false&useUnicode=true&characterEncoding=utf-8
    username: root
    password: root

# mysql 8 驱动不同 com.mysql.cj.jdbc.Driver、需要增加时区配置 serverTimezone=UTC
```

`传统方式：`pojo - dao(连接mybatis，配置mapper.xml文件) - service - controller



>  Mybatis-plus方式：

在 Spring Boot 启动类中添加 `@MapperScan` 注解，扫描 Mapper 文件夹：

```java
// 扫描mapper文件夹
@MapperScan("com.zx.mapper.*")
@SpringBootApplication
public class MybatisPlusLearnApplication {

    public static void main(String[] args) {
        SpringApplication.run(MybatisPlusLearnApplication.class, args);
    }

}
```



## [#](https://baomidou.com/pages/226c21/#编码)编码

编写实体类 `User.java`（此处使用了 [Lombok (opens new window)](https://www.projectlombok.org/)简化代码）

```java
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;
    private String name;
    private Integer age;
    private String email;
}
```



编写 Mapper 类 `UserMapper.java`

```java
// 在对应的mapper上继承对应的类 baseMapper
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 所有CRUD操作已经编写完成
    // 不需要像之前那样配一大堆文件了！
}

```



## [#](https://baomidou.com/pages/226c21/#开始使用)开始使用

添加测试类，进行功能测试：

```java
@SpringBootTest
public class SampleTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testSelect() {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper
                .select("id","name","age")
                .ge("id",3);

        // 参数是一个wrapper，条件构造器
        List<User> users = userMapper.selectList(queryWrapper);
//        Assert.assertEquals(5, userList.size());
        users.forEach(System.out::println);
    }

}
```



提示

UserMapper 中的 `selectList()` 方法的参数为 MP 内置的条件封装器 [Wrapper](https://www.jianshu.com/p/c5537559ae3a)，所以不填写就是无任何条件

控制台输出：

```log
User(id=1, name=Jone, age=18, email=test1@baomidou.com)
User(id=2, name=Jack, age=20, email=test2@baomidou.com)
User(id=3, name=Tom, age=28, email=test3@baomidou.com)
User(id=4, name=Sandy, age=21, email=test4@baomidou.com)
User(id=5, name=Billie, age=24, email=test5@baomidou.com)
```



## #日志

我们并不能看到详细的执行过程，但可以通过配置日志来输出日志。

在application1.yml中配置：

```yml
# 配置日志
mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl # 控制台打印
```

控制台输出：

![image-20220121125758881](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20220121125758881.png)



## #CRUD扩展

### 插入操作

测试类中加入：

```java
// 测试插入
    @Test
    void insertTest(){
        User user = new User();
        user.setName("张三");
        user.setAge(23);
        user.setEmail("386859692@qq.com");

        int i = userMapper.insert(user);
        Consumer c = System.out::println;
        c.accept(i);
    }
```

控制台输出：

![image-20220121131237662](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20220121131237662.png)

> 数据库插入的id的默认值为：全局的唯一id



### 批量插入

最近 Review 小伙伴代码的时候，发现了一个小小的问题，小伙伴竟然在 for 循环中进行了 insert （插入）[数据库](https://cloud.tencent.com/solution/database?from=10680)的操作，这就会导致每次循环时都会进行连接、插入、断开连接的操作，从而导致一定的性能问题，简化后代码如下：

```javascript
/**
 * 插入操作
 */
@RequestMapping("/save")
public Object save() {
    boolean flag = false; // 返回结果
    // 待添加（用户）数据
    for (int i = 0; i < 1000; i++) {
        User user = new User();
        user.setName("test:"+i);
        user.setPassword("123456");
        // 插入数据
        flag = userService.save(user);
        if(!flag) break;
    }
    return flag;
}
```

**这样做并不会改变程序最终的执行结果，但会对程序的执行效率带来很大的影响**，就好比你现在要从 A 地点送 10 件货到 B 地点，你可以选择 1 次送 1 件，送 10 次的方案；也可以选择 1 次送 10 件，送 1 次的方案，请问你会选择哪种？这就是多次循环插入和批量一次插入的问题。 

>  PS：要插入的数据量越大，批量插入的时间（相比于循环多次插入来说）也越短、其优势也越大。 

#### MP的saveBatch

本文我们使用 MyBatis-Plus（下文简称 MP）自带的 saveBatch 方法，来实现数据的批量插入功能，因为 MP 不是本文讨论的重点，所以这里咱们就不介绍了，如果有不熟悉的朋友可以去他的官方自行恶补：https://baomidou.com/guide/，咱们本文重点介绍一下 MP 实现批量插入的具体步骤。 

##### 1.引入 MP 框架

首先，打开您的 pom.xml 文件，在文件中添加以下内容：

```javascript
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>mybatis-plus-latest-version</version>
</dependency>
```

>  注意：mybatis-plus-latest-version 表示 MP 框架的最新版本号，可访问 https://mvnrepository.com/artifact/com.baomidou/mybatis-plus-boot-starter 查询最新版本号，但在使用的时候记得一定要将上面的 “mybatis-plus-latest-version”替换成换成具体的版本号，如 3.4.3 才能正常的引入框架。 

##### 2.创建数据库和表

此步骤可省略，主要用于本文功能的实现，创建数据库和数据表的脚本如下：

```javascript
-- ----------------------------
-- 创建数据库
-- ----------------------------
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
DROP DATABASE IF EXISTS `testdb`;
CREATE DATABASE `testdb`;
USE `testdb`;

-- ----------------------------
-- 创建 user 表
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `createtime` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- 添加测试数据
-- ----------------------------
INSERT INTO `user` VALUES (1, '赵云', '123456', '2021-09-10 18:11:16');
INSERT INTO `user` VALUES (2, '张飞', '123456', '2021-09-10 18:11:28');
INSERT INTO `user` VALUES (3, '关羽', '123456', '2021-09-10 18:11:34');
INSERT INTO `user` VALUES (4, '刘备', '123456', '2021-09-10 18:11:41');
INSERT INTO `user` VALUES (5, '曹操', '123456', '2021-09-10 18:12:02');

SET FOREIGN_KEY_CHECKS = 1;
```

##### 3.具体代码实现（重点）

###### ① 实体类

先来创建数据库所对应的 User 实体类：

```javascript
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class User {
    private int id;
    private String name;
    private String password;
    private Date createtime;
}
```

###### ② Controller 层代码

本文的核心是使用 MP 框架中，IService 类提供的 saveBatch 方法，来实现批量数据的插入功能，对应在 Controller 中的实现代码如下：

```javascript
import com.example.demo.model.User;
import com.example.demo.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/u")
public class UserController {

    @Autowired
    private UserServiceImpl userService;

    /**
     * MP 批量插入
     */
    @RequestMapping("/savebatch")
    public boolean saveBatch() {
        List<User> list = new ArrayList<>();
        // 待添加（用户）数据
        for (int i = 0; i < 1000; i++) {
            User user = new User();
            user.setName("test:"+i);
            user.setPassword("123456");
            list.add(user);
        }
        // 批量插入
        return userService.saveBatch(list);
    }
}
```

###### ③ Service 层代码（重点）

接下来，我们要创建一个 UserService 接口，继承 MP 框架中的 IService 接口，实现代码如下：

```javascript
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.demo.model.User;

public interface UserService extends IService<User> {

}
```

然后再创建一个 UserService 的实现类：

```javascript
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.demo.mapper.UserMapper;
import com.example.demo.model.User;
import com.example.demo.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper,User>
        implements UserService {

}
```

>  PS：注意 UserServiceImpl 必须要继承 MP 框架中的 ServiceImpl，不然要重写很多方法。 

###### ④ Mapper 层代码

Mapper 层的实现相对来说就比较简单了，只需要创建一个 Mapper 类继承 MP 框架中的 BaseMapper 类即可，实现代码如下：

```javascript
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.demo.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User>{

}
```

>  PS：BaseMapper 提供了对某个对象（类）最基础的 CRUD 操作。 

##### 总结

本文我们介绍了 MP（MyBatis Plus）中实现批量插入的具体实现步骤，它的核心是通过调用 MP 中 IService 提供的 saveBatch 方法来完成的



#### [多线程批量插入](https://blog.csdn.net/qq_33709582/article/details/121745749)

很多同学都有这样的困扰：

工作中项目的数据量不大，遇不到sql优化的场景：单表就几万，我优化个der啊；
业务对性能要求不高，远远没达到性能瓶颈：咱这项目又不是不能跑，优化个der啊；
确实，如果你的项目体量不大，不管是数据层还是应用层，都很难接触到性能优化

但是

我们可以自己造数据啊

今天我带来了一个demo，不仅让你能把多线程运用到实际项目中，还能用它往数据库造测试数据，让你体验下大数据量的表优化

定个小目标，今天造它一亿条数据

首先搞清楚，不要为了用技术而用技术，技术一定是为了实现需求：

插入一亿条数据，这是需求；
为了提高效率，运用多线程异步插入，这是方案；
1、为了尽可能模拟真实场景，我们new个对象

靠phone和createTime俩字段，能大大降低数据重复度，抛开别的字段不说，这俩字段基本能保证没有重复数据，所以我们最终的数据很真实，没有一条是重复的，而且，最后还能通过createTime来统计每秒插入条数，nice~

```java
public class Person {
    private Long id;
    private String name;//姓名
    private Long phone;//电话
    private BigDecimal salary;//薪水
    private String company;//公司
    private Integer ifSingle;//是否单身
    private Integer sex;//性别
    private String address;//住址
    private LocalDateTime createTime;
    private String createUser;
}
```

2、想要插的更快，我们得使用MyISAM引擎，并且要主键自增（不知道为什么的兄弟私聊我或者评论区留言，咱们今天主题不是讲数据库本身）

ddl：

```sql
CREATE TABLE `person` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `phone` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `salary` decimal(10,2) NOT NULL,
  `company` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `if_single` tinyint NOT NULL,
  `sex` tinyint NOT NULL,
  `address` varchar(225) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `create_time` datetime NOT NULL,
  `create_user` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=30170001 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;
```

3、为了模拟真实数据，我们得用到一些枚举值和随机算法

部分属性枚举值：

```java
private String[] names = {"黄某人", "负债程序猿", "谭sir", "郭德纲", "蔡徐鸡", "蔡徐老母鸡", "李狗蛋", "铁蛋", "赵铁柱"};
private String[] addrs = {"二仙桥", "成华大道", "春熙路", "锦里", "宽窄巷子", "双子塔", "天府大道", "软件园", "熊猫大道", "交子大道"};
private String[] companys = {"京东", "腾讯", "百度", "小米", "米哈游", "网易", "字节跳动", "美团", "蚂蚁", "完美世界"};

```

随机获取person

```java
private Person getPerson() {
    Person person = Person.builder()
            .name(names[random.nextInt(names.length)])
            .phone(18800000000L + random.nextInt(88888888))
            .salary(new BigDecimal(random.nextInt(99999)))
            .company(companys[random.nextInt(companys.length)])
            .ifSingle(random.nextInt(2))
            .sex(random.nextInt(2))
            .address("四川省成都市" + addrs[random.nextInt(addrs.length)])
            .createUser(names[random.nextInt(names.length)]).build();
    return person;
}
```

5、orm层用的mybatis

```xml
<insert id="insertList" parameterType="com.example.demos.entity.Person">
    insert into person (name, phone, salary, company, if_single, sex, address, create_time, create_user)
    values
    <foreach collection="list" item="item" separator=",">
        (#{item.name}, #{item.phone}, #{item.salary}, #{item.company}, #{item.ifSingle}, #{item.sex},
        #{item.address}, now(), #{item.createUser})
    </foreach>
</insert>
```

准备工作完成，开始写核心逻辑

思路：
1、想要拉高插入效率，肯定不能够一条一条插了，必须得foreach批量插入，经测试，单次批量3w条以下时性价比最高，并且不用修改mysql配置
2、文章开头说了，得开多个线程异步插入，我们先把应用层效率拉满，mysql顶不顶得住
3、我们不可能单次提交一亿次insert，这谁顶得住，而且大量插入操作会很耗时，短时间内完不成，我们不可能一直守着，我的方案是用定时任务
。。。

算了屁话不多说，直接上demo

```java
@Component
public class PersonService {
    private static final int THREAD_COUNT = 10;
    @Autowired
    private PersonMapper personMapper;
    @Autowired
    private ThreadPoolExecutor executor;
    private AtomicInteger integer = new AtomicInteger();
    private Random random = new Random();
    private String[] names = {"黄某人", "负债程序猿", "谭sir", "郭德纲", "蔡徐鸡", "蔡徐母鸡", "李狗蛋", "铁蛋", "赵铁柱"};
    private String[] addrs = {"二仙桥", "成华大道", "春熙路", "锦里", "宽窄巷子", "双子塔", "天府大道", "软件园", "熊猫大道", "交子大道"};
    private String[] companys = {"京东", "腾讯", "百度", "小米", "米哈游", "网易", "字节跳动", "美团", "蚂蚁", "完美世界"};

    @Scheduled(cron = "0/15 * * * * ?")
    public void insertList() {
        System.out.println("本轮任务开始，总任务数：" + THREAD_COUNT);
        long start = System.currentTimeMillis();
        AtomicLong end = new AtomicLong();
        for (int i = 0; i < THREAD_COUNT; i++) {
            Thread thread = new Thread(() -> {
                try {
                    for (int j = 0; j < 20; j++) {
                        personMapper.insertList(getPersonList(5000));
                    }
                    end.set(System.currentTimeMillis());
                    System.out.println("本轮任务耗时：" + (end.get() - start) + "____已执行" + integer.addAndGet(1) + "个任务" + "____当前队列任务数" + executor.getQueue().size());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            try {
                executor.execute(thread);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private ArrayList<Person> getPersonList(int count) {
        ArrayList<Person> persons = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            persons.add(getPerson());
        }
        return persons;
    }

    private Person getPerson() {
        Person person = Person.builder()
                .name(names[random.nextInt(names.length)])
                .phone(18800000000L + random.nextInt(88888888))
                .salary(new BigDecimal(random.nextInt(99999)))
                .company(companys[random.nextInt(companys.length)])
                .ifSingle(random.nextInt(2))
                .sex(random.nextInt(2))
                .address("四川省成都市" + addrs[random.nextInt(addrs.length)])
                .createUser(names[random.nextInt(names.length)]).build();
        return person;
    }
}

```

我的线程池配置，我电脑配置比较拉跨，只有12个线程…

```java
@Configuration
public class ThreadPoolExecutorConfig {
    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 12, 5, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
        executor.allowCoreThreadTimeOut(true);
        return executor;
    }
}
```

测试

![在这里插入图片描述](https://img-blog.csdnimg.cn/196a89f1ac2443df8ac29aa14ed964f8.png)

现在表是空的

项目跑起来

![在这里插入图片描述](https://img-blog.csdnimg.cn/39b9fd39cb8e4dcda299caf9ccba1f27.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA6LSf5YC656iL5bqP54y_,size_20,color_FFFFFF,t_70,g_se,x_16)

已经在开始插了，挂在后台让它自己跑吧。。。
25 minutes later ~
看下数据库

![在这里插入图片描述](https://img-blog.csdnimg.cn/f3238036efe740daba4614bc97a7e389.png?x-oss-process=image/watermark,type_d3F5LXplbmhlaQ,shadow_50,text_Q1NETiBA6LSf5YC656iL5bqP54y_,size_20,color_FFFFFF,t_70,g_se,x_16)

已经插入了1.04亿条数据，需求完成

第一条数据是15:54:15开始的，耗时大概25min

![在这里插入图片描述](https://img-blog.csdnimg.cn/c4d7c37596f74050ab832ff9dc877f87.png)

再来从数据库中看下一秒插入多少条，直接count某秒即可

![在这里插入图片描述](https://img-blog.csdnimg.cn/697b5106b8ab49248566abb160814e11.png)


一秒8.5w，嘎嘎快

来说下demo中核心的几个点：

- 关于线程：我的cpu只有十二个线程，所以核心线程设置的10，留两个线程打杂；
- 关于线程中的逻辑：每个线程有20次循环，每次循环插入5000条；
- 关于获取随机对象：我没有统计创建对象的耗时，因为即使是创建100w个对象，但是这都是内存操作，跟insert这种io操作比起来，耗时几乎可以忽略，我就不测试了，你可以自己试试；
- 关于效率：你们看到的版本（10 * 20 * 5000）是我只优化过几次的半成品，这种搭配最终的效率是100w条耗时12.5s，效率肯定不是最高的，但基本够用了；
  

可以看看之前的测试效率记录

```tex
10 * 100 * 1000：22-23s
10 * 50 * 2000：19-20s
10 * 10 * 10000 ：18-20s
```



可以参考记录进行深度调优

哦对了，想效率更快的话，表不要建索引，insert时维护索引也是一笔不小的开销

---

### 主键生成策略

> 默认 IdType.ID_WORKER 全局唯一ID

[分布式系统唯一ID生成](https://www.cnblogs.com/haoxinyue/p/5208136.html)

雪花算法：

`snowflake`是Twitter开源的分布式ID生成算法，结果是一个long型的ID。其核心思想是：使用41bit作为毫秒数，10bit作为机器的ID（5个bit是数据中心，5个bit的机器ID），12bit作为毫秒内的流水号（意味着每个节点在每毫秒可以产生 4096 个 ID），最后还有一个符号位，永远是0。

snowflake算法可以根据自身项目的需要进行一定的修改。比如估算未来的数据中心个数，每个数据中心的机器数以及统一毫秒可以能的并发数来调整在算法中所需要的bit数。

优点：

1）不依赖于数据库，灵活方便，且性能优于数据库。

2）ID按照时间在单机上是递增的。

缺点：

1）在单机上是递增的，但是由于涉及到分布式环境，每台机器上的时钟不可能完全同步，在算法上要解决时间回拨的问题。



> 主键自增

1. 实体类字段上`@TableId(type = IdType.AUTO)`
2. 数据库字段一定要是自增！不然会抛异常

![image-20220121194945228](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20220121194945228.png)

![image-20220121195114881](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20220121195114881-16427658751261.png)



> 其他的源码解释

```java
public enum IdType {
    AUTO(0),	// 数据库ID自增
    NONE(1),	// 未设置主键
    INPUT(2),	// 手动输入
    ID_WORKER(3),	// 默认的全局唯一ID
    UUID(4),	// 全局唯一ID uuid
    ID_WORKER_STR(5);	// ID_WORKER 的字符串表示
}
```



### 更新操作

测试类中加入：

```java
// 测试更新
    @Test
    void updateTest(){
        User user = new User();
        user.setId(1L);
        user.setName("王二");
        user.setAge(999);
        userMapper.updateById(user);
    }
```

日志

![image-20220124123654044](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20220124123654044.png)

sql会自动配置



### 自动填充

创建时间，修改时间！这些操作一般都是自动化完成的，我们不希望手动更新！

阿里巴巴开发手册：所有的数据库表：gmt_create、gmt_modified 几乎所有的表都要配置上！并且需要自动化！

> 方式一：数据库级别（工作中不允许修改数据库）

1、在表中新增字段 create_time、update_time

![image-20220124135235370](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20220124135235370.png)

注意：datetime类型的自动更新MySQL5.6以后才支持

MySQL5.6以前的版本可以用timestamp（但是只支持一个字段默认值是CURRENT_TIMESTAMP）





> 方式二：代码级别



提示

完整的代码示例请移步：[Spring Boot 快速启动示例 (opens new window)](https://github.com/baomidou/mybatis-plus-samples/tree/master/mybatis-plus-sample-quickstart)| [Spring MVC 快速启动示例(opens new window)](https://github.com/baomidou/mybatis-plus-samples/tree/master/mybatis-plus-sample-quickstart-springmvc)

## [#](https://baomidou.com/pages/226c21/#小结)小结

通过以上几个简单的步骤，我们就实现了 User 表的 CRUD 功能，甚至连 XML 文件都不用编写！

从以上步骤中，我们可以看到集成`MyBatis-Plus`非常的简单，只需要引入 starter 工程，并配置 mapper 扫描路径即可。

但 MyBatis-Plus 的强大远不止这些功能，想要详细了解 MyBatis-Plus 的强大功能？那就继续往下看吧！



# 其他

## #设置resource进行资源文件忽略

```xml
<resources>
    <resource>
        <directory>src/main/java</directory>
        <includes>
            <include>**/*.xml</include>
        </includes>
    </resource>
    <resource>
        <directory>src/main/resources</directory>
        <includes>
            <include>**/*</include>
        </includes>
    </resource>
</resources>
```

