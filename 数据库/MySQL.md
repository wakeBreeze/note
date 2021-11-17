## 1、初始MySQL

JavaEE：企业级Java开发 web

前端（页面：展示，数据！）

后台（连接点：链接数据库JDBC，连接前端（控制，控制视图跳转，给前端传递数据））

数据库（存数据，Txt，Excel，Word）

> 只会写代码，学好数据库，基本混饭吃！
>
> 操作系统，数据结构预算法！当一个不错的程序员！
>
> 离散数学，数字电路，体系结构，编译原理。+实战经验，高级程序员~优秀的程序员



### 1.1、为什么学习数据库

1、岗位要求

2、现在的世界，大数据时代~，得数据者得天下。

3、被迫需求：存数据

4、==数据库是所有软件体系中最核心的存在==	DBA



### 1.2、什么是数据库

数据库（DB，DataBase）

概念：数据仓库，软件，安装在操作系统（windows，linux，mac……）之上！SQL，可以存储大量的数据。500万以上，需要优化。

作用：存储数据，管理数据



### 1.3、数据库分类

**关系型数据库**（SQL)

- Mysql，Oracle，SQL server，DB2，SQL Lite
- 通过表和表之间，行和列之间的关系进行数据的存储。学员信息表，考勤表……



**非关系型数据库：**（NoSQL) Not Only

- Redis，MongoDB
- 对象存储，通过对象自身的属性来决定。



==**DBMS（数据库管理系统）**==

- 数据库的管理软件，科学有效的管理我们的数据。维护和获取数据；
- MySQL，数据库管理系统！



### 1.4、MySQL简介

MySQL是一个**关系型数据库管理系统**

前世：瑞典MySQL AB 公司

今生：属于Oracle 旗下产品

MySQL是最好的 RDBMS（Relational Database Management System，关系型数据库管理系统）应用软件之一。

开源的数据库软件~

体积小、速度快、总体拥有成本低，招人比较低，所有人必须会~

中小型网站、或者大型网站，集群！

官网：https://www.mysql.com

官网下载地址：https://dev.mysql.com/downloads/mysql/



安装建议：

1、尽量不要使用exe，有注册表

2、尽可能使用压缩包安装



### 1.5、安装MySQL

[教程](https://www.cnblogs.com/hellokuangshen/p/10242958.html)



1、解压

2、把这个包放到自己的电脑环境目录下

3、环境变量配置

4、新建mysql配置文件 ini

```mysql
[mysqld]
# 目录一定要换成自己的
basedir=D:\Environment\mysql-5.7.19\
datadir=D:\Environment\mysql-5.7.19\data\
port=3306
# 跳过密码验证
skip-grant-tables
```

5、启动管理员模式下的CMD，运行所有的命令

6、安装mysql服务

7、初始化数据库文件

8、启动mysql，进去修改密码~

​	启动mysql服务：net start mysql

​	关闭mysql服务：net stop mysql

9、进入mysql 通过命令行 mysql -u root -p

​	注意：-p后面不要加空格，修改密码-sql语句后面一定要加分号

10、注掉 ini 中的跳过密码

11、重启mysql。连接测试，如果连接成功就行了



### 1.6、安装SQLyog



### 1.7、连接数据库

命令行连接！

```mysql
mysql -uroot -proot	-- 连接数据库

update mysql.user set authentication_string=password('root') where user='root' and Host = 'localhost';	-- 修改用户密码
flush privileges;	-- 刷新权限

-- 所有语句都使用 ; 结尾
show databases;	-- 查看所有数据库

use school	-- 	切换数据库：use 数据库名
Database changed

show tables;	-- 查看数据库中的所有表；
describe 表名;	-- 显示数据库中所有的表的信息

create database 数据库名;	-- 创建一个数据库

exit;	-- 退出连接

-- 单行注释（SQL 的原生注释）
/*SQL的多行注释*/
```



数据库 XXX 语言	CRUD!	增删改查

DDL	定义

DML	操作

DQL	查询

DCL	控制



## 2、操作数据库

操作数据库 > 操作数据库中的表 > 操作数据库中表的数据

==mysql关键字不区分大小写==



### 2.1、操作数据库

1、创建数据库

```mysql
CREATE DATABASE [IF NOT EXISTS] westos;	-- []表示可以省略
```



2、删除数据库

```mysql
DROP DATABASE [IF EXISTS] westos;
```



3、使用数据库

```mysql
USE westos;
```



4、查看数据库

```mysql
SHOW DATABASES;	-- 查看所有的数据库
```



对比SQLyog 的可视化操作

![image-20210210084341951](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210210084341951.png)

学习思路：

- 对照 sqlyog 可视化历史记录查看sql

- 固定的语法和关键字必须要强行记住！



### 2.2、数据库的列类型

> 数值

- tinyint	十分小的数据	1个字节

- smallint	较小的数据	2个字节

- mediumint	中等大小的数据	3个字节

- **int	标准的整数	4个字节**	常用的	int

- bigint	较大的数据	8个字节



- float	浮点数	4个字节
- double	浮点数	8个字节	（精度问题！）
- decimal	字符串形式的浮点数	金融计算的时候，一般使用decimal

DECIMAL从MySQL 5.1引入，列的声明语法是DECIMAL(M,D)。

- M是数字的最大位数（精度）。其范围为1～65（在较旧的MySQL版本中，允许的范围是1～254），M 的默认值是10。
- D是小数点右侧数字的位数（标度）。其范围是0～30，但不得超过M。

- M-D：整数位数。

decimal(M,D)占 M/9*4 个字节

例：数据库的商品售价字段类型被定义为Decimal(18,9)，18这个数代表着商品售价最长可以到18位，而9这个数字表示小数点后面有9位数字，那么18-9=9，也就得出了整数位可以有9位。

对于decimal类型来说，每4个字节存9个数字，那么以上数据一共有18位，所以会有18/9x4=8,再加上小数点会占一个字节，所以8+1=9，因此decimal(18,9)占用9个字节。



> 字符串

- char	字符串固定大小	0~255	单位：字节
- **varchar	可变字符串	0~65535**	常用的变量	String	单位：字节

- tinytext	微型文本	2^8-1	单位：bit

- text	文本串	2^16-1	保存大文本	单位：bit



> 时间日期

- date	yyyy-MM-dd,日期格式
- time	HH:mm:ss	时间格式
- **datetime	yyyy-MM-dd HH:mm:ss	最常用的时间格式**	MM大写是区分 “月” 与 “分” ，HH是24小时制，hh是12小时制
- **timestamp	时间戳	1970.1.1 到现在的毫秒数！**	也较为常用！
- year	年份



> null

没有值，未知

==注意，不要使用null进行运算，结果会为null==



### 2.3、数据库的字段属性（重点）

==Unsigned：==

- 无符号整数
- 声明了该列不能声明为负数



==zerofill：==

- 0填充
- 不足的位数，使用0来填充。int(3)    5--005



==自增：==

- 通常理解为自增，自动在上一条记录的基础上 + 1 （默认）
- 通常用来设计唯一的主键~index，必须是整数类型
- 可以自定义设计主键自增的起始值和步长



==非空== NULL  not null

- 假设设置为 not null，如果不给它赋值，就会报错！
- null，如果不填写值，默认就是null！



==默认：==

- 设置默认的值！



**拓展：**

```mysql
/*每一个表，都必须存在以下五个字段！未来做项目用的，表示一个记录存在的意义！

id	主键
`version`	乐观锁
is_delete	伪删除
gmt_create	创建时间
gmt_update	修改时间
*/
```



### 2.4、创建数据库表

```mysql
-- 目标：创建一个school数据库
-- 创建学生表（列，字段）	使用SQL创建
-- 学号int 姓名varchar 登陆密码varchar 性别varchar 生日datetime 地址varchar

-- 注意点，使用英文 ()，表的名称和字段尽量使用 `` 括起来
-- AUTO_INCREMENT 自增
-- 字符串使用 单引号括起来！
-- 所有的语句后面加 , （英文的），最后一个不用加
-- PRIMARY KEY 主键，一般一个表只有一个唯一的主键！
CREATE TABLE IF NOT EXISTS `student`(
`sno` INT(20) NOT NULL AUTO_INCREMENT COMMENT '学号',
`name` VARCHAR(30) NOT NULL DEFAULT '匿名' COMMENT '姓名',
`pwd` VARCHAR(30) NOT NULL DEFAULT 'root' COMMENT '密码',
`sex` VARCHAR(6) NOT NULL DEFAULT 'female' COMMENT '性别',
`birthday` DATETIME DEFAULT NULL COMMENT '生日',
`address` VARCHAR(50) DEFAULT NULL COMMENT '地址',
`email` VARCHAR(50) DEFAULT NULL COMMENT '邮件',
PRIMARY KEY(`sno`)
)ENGINE=INNODB DEFAULT CHARSET=utf8
```

格式

```mysql
CREATE TABLE [IF NOT EXISTS] `表名`(
	`字段名` 列类型 [属性] [索引] [注释],
    `字段名` 列类型 [属性] [索引] [注释],
    ……
    `字段名` 列类型 [属性] [索引] [注释]
)[表类型] [字符集设置] [注释]
```



常用命令

```mysql
SHOW CREATE DATABASE 数据库名; -- 查看创建数据库的语句
SHOW CREATE TABLE 表名; -- 查看创建表的语句
DESC student; -- 显示表结构
```



### 2.5、数据表的类型

```mysql
-- 关于数据库引擎
/*
INNODB 默认使用
MYISAM 早些年使用的
*/
```



|            | MYISAM |   INNODB   |
| :--------: | :----: | :--------: |
|  事务支持  | 不支持 |    支持    |
| 数据行锁定 | 不支持 |    支持    |
|  外键约束  | 不支持 |    支持    |
|  全文索引  |  支持  |   不支持   |
| 表空间大小 |  较小  | 2倍 MYISAM |

常规使用操作：

- MYISAM	节约空间，速度较快
- INNODB    安全性高，事务的处理，多表多用户操作



> 在物理空间的存储位置

所有的数据文件都存在 data 目录下，一个文件夹就对应一个数据库

本质还是文件的存储！



MySQL 引擎在物理文件上的区别

- InnoDB 在数据库表中只有一个 *.frm文件，以及上级目录下的 ibtata1 文件

- MYISAM 对应文件

  *.frm	表结构的定义文件

  *.MYD  数据文件（data）

  *.MYI	索引文件（index）



> 设置数据库表的字符集编码

```mysql
CHARSET=utf8
```

不设置的话，会是mysql默认的字符集编码（不支持中文）

MySQL的默认字符集编码是Latin1，不支持中文

在my.ini 中配置默认的编码

```mysql
character-set-server=utf8
```



### 2.6、修改删除表

> 修改

```mysql
-- 修改表名：ALTER TABLE 旧表名 RENAME AS 新表名
ALTER TABLE teacher RENAME AS teacher1;

-- 增加表字段：ALTER TABLE ADD 字段名 列属性
ALTER TABLE ADD age INT(11);


-- 修改表字段（重命名，修改约束！）
-- 修改约束：ALTER TABLE 表名 MODIFY 字段名 列属性[]
ALTER TABLE teacher1 MODIFY age VARCHAR(10);
-- 字段重命名：ALTER TABLE 表名 change 旧字段名 新字段名 列属性[]
ALTER TABLE teache1 CHANGE age age1 INT(3);


-- 删除表的字段：ALTER TABLE 表名 DROP 字段名

```



> 删除

```mysql
-- 删除表（如果表存在再删除）
DROP TABLE IF EXISTS teacher1;
```

==所有的创建和删除操作尽量加上判断，以免报错==



注意点：

- `` 字段名使用这个包裹！
- 注释 -- /**/
- sql 关键字大小写不敏感，建议大家写小写
- 所有的符号全部用英文



## 3、MySQL数据管理

### 3.1、外键（了解即可）

> 方式一、在创建表的时候，增加约束（比较复杂）

```mysql
CREATE TABLE `grade`(
	`gradeid` INT(10) NOT NULL AUTO_INCREMENT COMMENT '成绩号',
	`name` VARCHAR(20) NOT NULL COMMENT '姓名',
	`grade` INT(3) NOT NULL COMMENT '成绩',
	PRIMARY KEY (`gradeid`)
	)ENGINE=INNODB DEFAULT CHARSET=utf8

-- 学生表的 gradeid 字段，要去应用年纪表的 gradeid 字段
-- 定义外键 key
-- 给这个外键添加约束（执行引用） references 引用

CREATE TABLE IF NOT EXISTS `student` (
  `sno` INT(20) NOT NULL AUTO_INCREMENT COMMENT '学号',
  `name` VARCHAR(30) NOT NULL DEFAULT '匿名' COMMENT '姓名',
  `pwd` VARCHAR(30) NOT NULL DEFAULT 'root' COMMENT '密码',
  `sex` VARCHAR(6) NOT NULL DEFAULT 'female' COMMENT '性别',
  `birthday` DATETIME DEFAULT NULL COMMENT '生日',
  `address` VARCHAR(50) DEFAULT NULL COMMENT '地址',
  `email` VARCHAR(50) DEFAULT NULL COMMENT '邮件',
  `gradeid` INT(10) NOT NULL COMMENT '成绩号',
  PRIMARY KEY (`sno`),
  KEY `fk_gradeid`(`gradeid`),
  CONSTRAINT `fk_gradeid` FOREIGN KEY (`gradeid`) REFERENCES `grade`(`gradeid`)
) ENGINE=INNODB DEFAULT CHARSET=utf8
```

删除有外键关系的表的时候，必须要先删除引用别人的表（从表），再删除被引用的表（主表）



> 方式二：创建表成功后，添加外键约束

```mysql
-- ALTER TABLE 表名 ADD CONSTRAINT 约束名 FOREIGN KEY (作为外键的列) REFERENCES 关联的表名(关联表的字段);
ALTER TABLE `student` ADD CONSTRAINT `fk_gradeid` FOREIGN KEY (`gradeid`) REFERENCES `grade`(`gradeid`);
```

> 删除外键

```mysql
-- ALTER TABLE 表名 DROP FOREIGN KEY 外键名
ALTER TABLE `student` DROP FOREIGN KEY `fk_gradeid`;
```



以上操作都是物理外键，数据库级别的外键，我们不建议使用！（避免数据库过多造成困扰）

==最佳实践==

- 数据库就是单纯的表，只用来存数据，只有行（数据）和列（字段）
- 我们想要使用多张表的数据，想要使用外键（程序去实现）







## 4、使用DQL查询数据

### 4.1、DQL语言 

**DQL( Data Query Language 数据查询语言 )**

- 查询数据库数据 , 如SELECT语句 

- 简单的单表查询或多表的复杂查询和嵌套查询 

- 是数据库语言中最核心,最重要的语句 

- 使用频率最高的语句



> SELECT语法

```mysql
SELECT [ALL | DISTINCT] 
{* | table.* | [table.field1[as alias1][,table.field2[as alias2]][,...]]} 
FROM table_name [as table_alias] 
[left | right | inner join table_name2] -- 联合查询
[WHERE ...] -- 指定结果需满足的条件 
[GROUP BY ...] -- 指定结果按照哪几个字段来分组 
[HAVING] -- 过滤分组的记录必须满足的次要条件 
[ORDER BY ...] -- 指定查询记录按一个或多个条件排序 
[LIMIT {[offset,]row_count | row_countOFFSET offset}]; 
-- 指定查询的记录从哪条至哪条
```

**注意 : [ ] 括号代表可选的 , { }括号代表必选得** 

导入素材提供的SQL

![image-20210217162040652](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210217162040652.png)



### 4.2、指定查询字段

```mysql
-- 查询表中所有的数据列结果 , 采用 **" \* "** 符号; 但是效率低，不推荐 .
-- 查询所有学生信息 
SELECT * FROM student;
-- 查询指定列(学号 , 姓名) 
SELECT studentno,studentname FROM student;
```



> AS 子句作为别名

作用：

- 可给数据列取一个新别名 
- 可给表取一个新别名
- 可把经计算或总结的结果用另一个新名称来代替

```mysql
-- 这里是为列取别名(当然as关键词可以省略) 
SELECT studentno AS 学号,studentname AS 姓名 FROM student;

-- 使用as也可以为表取别名 
SELECT studentno AS 学号,studentname AS 姓名 FROM student AS s;

-- 使用as,为查询结果取一个新名字 
-- CONCAT()函数拼接字符串 
SELECT CONCAT('姓名:',studentname) AS 新姓名 FROM student;
```



> DISTINCT关键字的使用

作用 : 去掉SELECT查询返回的记录结果中重复的记录 ( 返回所有列的值都相同 ) , 只返回一条

```mysql
-- # 查看哪些同学参加了考试(学号) 去除重复项 
SELECT * FROM result; -- 查看考试成绩 
SELECT studentno FROM result; -- 查看哪些同学参加了考试 
SELECT DISTINCT studentno FROM result; -- 了解:DISTINCT 去除重复项 , (默认是ALL)
```



> 使用表达式的列

==数据库中的表达式 : 一般由文本值 , 列值 , NULL , 函数和操作符等组成== 

应用场景 :

- SELECT语句返回结果列中使用 
- SELECT语句中的ORDER BY , HAVING等子句中使用 
- DML语句中的 where 条件语句中使用表达式

```mysql
-- selcet查询中可以使用表达式 
SELECT @@auto_increment_increment; -- 查询自增步长 
SELECT VERSION(); -- 查询版本号 
SELECT 100*3-1 AS 计算结果; -- 表达式

-- 学员考试成绩集体提分一分查看 
SELECT studentno,StudentResult+1 AS '提分后' FROM result;
```



### 4.3、where条件语句 

作用：用于检索数据表中 符合条件 的记录 

搜索条件可由一个或多个逻辑表达式组成 , 结果一般为真或假

> 逻辑操作符

| 操作符名称 | 语法              | 描述                               |
| ---------- | ----------------- | ---------------------------------- |
| AND 或 &&  | a AND b 或 a && b | 逻辑与，同时为真结果才为真         |
| OR 或 \|\| | a OR b 或 a\|\|b  | 逻辑或，只要一个为真，则结果为真   |
| NOT 或 ！  | NOT a 或 ！a      | 逻辑非，若操作数为假，则结果为真！ |

测试

```mysql
-- 满足条件的查询(where) 
SELECT Studentno,StudentResult FROM result;

-- 查询考试成绩在95-100之间的 
SELECT Studentno,StudentResult 
FROM result 
WHERE StudentResult>=95 AND StudentResult<=100;

-- AND也可以写成 && 
SELECT Studentno,StudentResult 
FROM result 
WHERE StudentResult>=95 && StudentResult<=100;

-- 模糊查询(对应的词:精确查询)
SELECT Studentno,StudentResult 
FROM result 
WHERE StudentResult BETWEEN 95 AND 100;

-- 除了1000号同学,要其他同学的成绩 
SELECT studentno,studentresult 
FROM result 
WHERE studentno!=1000;

-- 使用NOT 
SELECT studentno,studentresult 
FROM result 
WHERE NOT studentno=1000;
```

> 模糊查询 ： 比较操作符

| 操作符名称  | 语法                      | 描述                                        |
| ----------- | ------------------------- | ------------------------------------------- |
| IS NULL     | a IS NULL                 | 若操作符为NULL，则结果为真                  |
| IS NOT NULL | a IS NOT NULL             | 若操作符不为NULL，则结果为真                |
| BETWEEN     | a BETWEEN b AND c         | 若 a 范围在 b 与 c 之间，则结果为真         |
| LIKE        | a LIKE b                  | SQL 模式匹配，若a匹配b，则结果为真          |
| IN          | a IN (a1，a2，a3，......) | 若 a 等于 a1,a2..... 中的某一个，则结果为真 |

注意：

- 数值数据类型的记录之间才能进行算术运算 ; 
- 相同数据类型的数据之间才能进行比较 ;



测试：

```mysql
-- 模糊查询 between and \ like \ in \ null

-- ============================================= 
-- LIKE 
-- ============================================= 
-- 查询姓刘的同学的学号及姓名 
-- like结合使用的通配符 : % (代表0到任意个字符) _ (一个字符) 
SELECT studentno,studentname 
FROM student 
WHERE studentname LIKE '刘%';

-- 查询姓刘的同学,后面只有一个字的 
SELECT studentno,studentname 
FROM student 
WHERE studentname LIKE '刘_';
-- 查询姓刘的同学,后面只有两个字的 
SELECT studentno,studentname 
FROM student 
WHERE studentname LIKE '刘__';

-- 查询姓名中含有 嘉 字的
SELECT studentno,studentname 
FROM student 
WHERE studentname LIKE '%嘉%';
-- 查询姓名中含有特殊字符的需要使用转义符号 '\' 
-- 自定义转义符关键字: ESCAPE ':'

-- ============================================= 
-- IN 
-- ============================================= 
-- 查询学号为1000,1001,1002的学生姓名 
SELECT studentno,studentname 
FROM student 
WHERE studentno IN (1000,1001,1002);

-- 查询地址在北京,南京,河南洛阳的学生 
SELECT studentno,studentname,address 
FROM student 
WHERE address IN ('北京','南京','河南洛阳');

-- ============================================= 
-- NULL 空 
-- ============================================= 
-- 查询出生日期没有填写的同学 
-- 不能直接写=NULL , 这是代表错误的 , 用 is null 
SELECT studentname 
FROM student 
WHERE BornDate IS NULL;

-- 查询出生日期填写的同学 
SELECT studentname 
FROM student 
WHERE BornDate IS NOT NULL;

-- 查询没有写家庭住址的同学(空字符串不等于null) 
SELECT studentname 
FROM student 
WHERE Address='' OR Address IS NULL;
```





### 4.4、连接查询

> JOIN 对比

| 操作符名称 | 描述                                       |
| ---------- | ------------------------------------------ |
| INNER JOIN | 如果表中有至少一个匹配，则返回行           |
| LEFT JOIN  | 即使右表中没有匹配，也从左表中返回所有的行 |
| RIGHT JOIN | 即使左表中没有匹配，也从右表中返回所有的行 |

![image-20210218193542587](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210218193542587.png)

![image-20210218193557567](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210218193557567.png)

测试

```mysql
/* 
连接查询 
	如需要多张数据表的数据进行查询,则可通过连接运算符实现多个查询
内连接 inner join 
	查询两个表中的结果集中的交集
外连接 outer join 
	左外连接 left join
    	(以左表作为基准,右边表来一一匹配,匹配不上的,返回左表的记录,右表以NULL填充)
	右外连接 right join 
		(以右表作为基准,左边表来一一匹配,匹配不上的,返回右表的记录,左表以NULL填充)

等值连接和非等值连接

自连接 
*/

-- 查询参加了考试的同学信息(学号,学生姓名,科目编号,分数) 
SELECT * FROM student; 
SELECT * FROM result;
/*思路: 
(1):分析需求,确定查询的列来源于两个类,student result,连接查询 (2):确定使用哪种连接查询?(内连接) 
*/ 
SELECT s.studentno,studentname,subjectno,StudentResult FROM student s 
INNER JOIN result r 
ON r.studentno = s.studentno

-- 右连接(也可实现) 
SELECT s.studentno,studentname,subjectno,StudentResult FROM student s 
RIGHT JOIN result r 
ON r.studentno = s.studentno

-- 等值连接 
SELECT s.studentno,studentname,subjectno,StudentResult FROM student s , result r 
WHERE r.studentno = s.studentno

-- 左连接 (查询了所有同学,不考试的也会查出来) 
SELECT s.studentno,studentname,subjectno,StudentResult FROM student s 
LEFT JOIN result r 
ON r.studentno = s.studentno

-- 查一下缺考的同学(左连接应用场景) 
SELECT s.studentno,studentname,subjectno,StudentResult FROM student s 
LEFT JOIN result r 
ON r.studentno = s.studentno 
WHERE StudentResult IS NULL

-- 思考题:查询参加了考试的同学信息(学号,学生姓名,科目名,分数) 
SELECT s.studentno,studentname,subjectname,StudentResult FROM student s 
INNER JOIN result r
ON r.studentno = s.studentno 
INNER JOIN `subject` sub 
ON sub.subjectno = r.subjectno
```

> 自连接

```mysql
/* 
自连接
	数据表与自身进行连接

需求:从一个包含栏目ID , 栏目名称和父栏目ID的表中 
查询父栏目名称和其他子栏目名称
*/

-- 创建一个表 
CREATE TABLE `category` ( 
    `categoryid` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主题id', 
    `pid` INT(10) NOT NULL COMMENT '父id', 			 	 `categoryName` VARCHAR(50) NOT NULL COMMENT '主题名字', 
    PRIMARY KEY (`categoryid`)
) ENGINE=INNODB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8

-- 插入数据 
INSERT INTO `category` (`categoryid`, `pid`, `categoryName`) 
VALUES('2','1','信息技术'), ('3','1','软件开发'),
('4','3','数据库'), ('5','1','美术设计'), 
('6','3','web开发'), ('7','5','ps技术'), 
('8','2','办公信息');

-- 编写SQL语句,将栏目的父子关系呈现出来 (父栏目名称,子栏目名称) 
-- 核心思想:把一张表看成两张一模一样的表,然后将这两张表连接查询(自连接) 
SELECT a.categoryName AS '父栏目',b.categoryName AS '子栏目' 
FROM category AS a,category AS b 
WHERE a.`categoryid`=b.`pid`

-- 思考题:查询参加了考试的同学信息(学号,学生姓名,科目名,分数)
SELECT s.studentno,studentname,subjectname,StudentResult FROM student s 
INNER JOIN result r 
ON r.studentno = s.studentno 
INNER JOIN `subject` sub
ON sub.subjectno = r.subjectno

-- 查询学员及所属的年级(学号,学生姓名,年级名) 
SELECT studentno AS 学号,studentname AS 学生姓名,gradename AS 年级名称 
FROM student s 
INNER JOIN grade g 
ON s.`GradeId` = g.`GradeID`

-- 查询科目及所属的年级(科目名称,年级名称) 
SELECT subjectname AS 科目名称,gradename AS 年级名称 
FROM SUBJECT sub 
INNER JOIN grade g 
ON sub.gradeid = g.gradeid

-- 查询 数据库结构-1 的所有考试结果(学号 学生姓名 科目名称 成绩)
SELECT s.studentno,studentname,subjectname,StudentResult FROM student s 
INNER JOIN result r 
ON r.studentno = s.studentno 
INNER JOIN `subject` sub 
ON r.subjectno = sub.subjectno 
WHERE subjectname='数据库结构-1'
```





表test：

```mysql
CREATE TABLE `test` (
  `id` int(3) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(20) NOT NULL COMMENT '名字',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8
```

![image-20210217084134696](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210217084134696.png)



表test1：

```mysql
CREATE TABLE test1(
id INT(10) NOT NULL AUTO_INCREMENT,
studentno INT(12) NOT NULL,
`name` VARCHAR(20) ,
`pwd` VARCHAR(20) ,
PRIMARY KEY (`id`)
)ENGINE=INNODB DEFAULT CHARSET=utf8
```

![image-20210217084331133](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210217084331133.png)



左连接（LEFT JOIN）：

```MYSQL
SELECT `test`.`name`,`pwd`,`studentno` 
FROM test
LEFT JOIN test1
ON test.`name`=test1.`name`;
```

![image-20210217084459995](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210217084459995.png)



右连接（RIGHT）：

```mysql
SELECT `test`.`name`,`pwd`,`studentno` 
FROM test
RIGHT JOIN test1
ON test.`name`=test1.`name`;
```

![image-20210217084740969](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210217084740969.png)



内连接（INNER JOIN）：

```mysql
SELECT `test`.`name`,`pwd`,`studentno` 
FROM test
INNER JOIN test1
ON test.`name`=test1.`name`;
```

![image-20210217084906143](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210217084906143.png)



> **自连接**

自己的表和自己表连接，**核心：一张表拆为两张一样的表。**

表category：

![image-20210217091840837](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210217091840837.png)

拆分为两个表：

父类：

| categoryid | categoryName |
| ---------- | ------------ |
| 2          | 信息技术     |
| 3          | 软件开发     |
| 5          | 美术设计     |

子类：

| pid  | categoryid | categoryName |
| ---- | ---------- | ------------ |
| 2    | 8          | 办公信息     |
| 3    | 4          | 数据库       |
| 3    | 6          | web开发      |
| 5    | 7          | ps技术       |

查询父类对应子类的关系：

|   父类   |   子类   |
| :------: | :------: |
| 信息技术 | 办公信息 |
| 软件开发 |  数据库  |
| 软件开发 | web开发  |
| 美术设计 |  ps技术  |



自连接查询：

```mysql
SELECT c1.categoryName,c2.categoryName 
FROM category c1
INNER JOIN category c2
ON c1.`categoryid`=c2.`pid`
```

![image-20210217140400563](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210217140400563.png)



### 4.5、排序和分页

> 排序

```mysql
/*============== 排序 ================ 
语法 : ORDER BY 
	ORDER BY 语句用于根据指定的列对结果集进行排序。 
	ORDER BY 语句默认按照ASC升序对记录进行排序。 
	如果您希望按照降序对记录进行排序，可以使用 DESC 关键字。
	
	ORDER BY 排序字段 排序方式
*/
```

```mysql
SELECT title AS '手机',price AS '价格'
FROM tb_book
ORDER BY price DESC
```

![image-20210217144024084](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210217144024084.png)



> 分页

```mysql
/*============== 分页 ================ 
语法 : SELECT * FROM table LIMIT [offset,] rows | rows OFFSET offset 
好处 : (用户体验,网络传输,查询压力)
推导:
第一页 : limit 0,5 
第二页 : limit 5,5 
第三页 : limit 10,5 
...... 
第N页 : limit (pageNo-1)*pageSzie,pageSzie 
[pageNo:页码,pageSize:单页面显示条数]

总条数：（页面总数-1）*每页的条数 + 最后一页的条数
*/
```



```mysql
-- 从第0条数据开始查，一共查5条数据
SELECT title AS '手机',price AS '价格'
FROM tb_book
ORDER BY price DESC
LIMIT 0,5
```

![image-20210217145347537](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210217145347537.png)



```mysql
-- 从第6条数据开始查，查询5条数据
SELECT title AS '手机',price AS '价格'
FROM tb_book
ORDER BY price DESC
LIMIT 5,5
```

![image-20210217150206074](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210217150206074.png)





### 4.6、子查询



> 执行顺序：由里到外



```mysql
/*============== 子查询 ================ 
什么是子查询? 
	在查询语句中的WHERE条件子句中,又嵌套了另一个查询语句 
	嵌套查询可由多个子查询组成,求解的方式是由里及外; 
	子查询返回的结果一般都是集合,故而建议使用IN关键字;
*/
```



表：tb_address

![image-20210217154515533](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210217154515533.png)

表：tb_user

![image-20210217154557902](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210217154557902.png)

```mysql
-- 子查询
SELECT receiver,address
FROM tb_address ta
WHERE user_id IN (SELECT phone FROM tb_user WHERE uname='user01')
```

查询结果：

![image-20210217154653776](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210217154653776.png)



**题目：查询 高等数学-2 成绩大于80 的学生（学号，姓名）**

![image-20210217162040652](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210217162040652.png)

```mysql
-- 答案1：（全部使用连接查询）
SELECT DISTINCT s.StudentNo,StudentName
FROM student s
INNER JOIN result r
ON s.StudentNo=r.StudentNo
Inner JOIN subject sub
ON s.GradeID=sub.GradeID
WHERE SubjectName='高等数学-2'
AND StudentResult >= 80;
 
 -- 答案2：（连接查询 + 子查询）
 SELECT DISTINCT s.`StudentNo`,`StudentName`
 FROM student s
 INNER JOIN result r
 ON s.StudentNo=r.StudentNo
 WHERE `StudentResult` >= 80
 AND `SubjectNo` IN (
     SELECT `SubjectNo` FROM subject 
 	 WHERE `SubjectName`='高等数学-2');
 	 
-- 答案3：（全部使用子查询）
SELECT DISTINCT StudentNo,StudentName
FROM student
WHERE StudentNo IN (
    SELECT StudentNo FROM result 
 	WHERE StudentResult >= 80
 	AND SubjectNo = (
        SELECT SubjectNo FROM subject
 		WHERE SubjectName='高等数学-2'
    )
);
```



```mysql
/* 
练习题目1:查 C语言-1 的前5名学生的成绩信息(学号,姓名,分数) 

练习题目2:查询郭靖同学所在的年级名称
*/
-- 使用子查询

```

```mysql
-- 题目1解答：
SELECT s.StudentNo,StudentName,StudentResult
FROM student s
INNER JOIN result r
WHERE s.StudentNo=r.StudentNo
AND SubjectNo = (
SELECT SubjectNo from subject 
where SubjectName='C语言-1') 
ORDER BY StudentResult DESC 
LIMIT 0,5;
```

```mysql
-- 题目2解答：
SELECT GradeName FROM grade
WHERE GradeID = (
SELECT GradeID FROM student WHERE StudentName='郭靖')
```



### 4.7、分组和过滤

```mysql
-- group by 字段名	 -- 通过此字段分组
-- having 条件  --根据条件过滤

-- 查询不同课程的平均分,最高分,最低分 
-- 前提:根据不同的课程进行分组
SELECT subjectname,AVG(studentresult) AS 平均分,MAX(StudentResult) AS 最高分,
MIN(StudentResult) AS 最低分 
FROM result AS r 
INNER JOIN `subject` AS s 
ON r.subjectno = s.subjectno 
GROUP BY r.subjectno 
HAVING 平均分>80;

/*
where写在group by前面. 
要是放在分组后面的筛选 
要使用HAVING.. 
因为having是从前面筛选的字段再筛选，
而where是从数据表中的>字段直接进行的筛选的
*/
```



### 4.8、SELECT小结

```mysql
SELECT  [去重] 要查询的字段 FROM 表 （字段和表可以取别名)
XXX JOIN 要连接的表 
ON 等值判断
WHERE （具体的值/子查询语句）
GROUP BY （通过哪个字段分组）
HAVING （过滤的条件）
ORDER BY （按哪个字段排序） [升序/降序]
LIMIT startIndex pageSize

/*
业务层面：
查询：跨表，跨数据库……
*/
```



## 5、MySQL函数 

[官方文档](https://dev.mysql.com/doc/refman/5.7/en/func-op-summary-ref.html) 

### 5.1、常用函数 

数据函数

```mysql
SELECT ABS(-8); /*绝对值*/ 
SELECT CEILING(9.4); /*向上取整*/ 
SELECT FLOOR(9.4);/*向下取整*/
SELECT RAND(); /*随机数,返回一个0-1之间的随机数*/ 
SELECT SIGN(0); /*符号函数: 负数返回-1,正数返回1,0返回0*/
```

字符串函数

```mysql
SELECT CHAR_LENGTH('狂神说坚持就能成功'); /*返回字符串包含的字符数*/ 
SELECT CONCAT('我','爱','程序'); /*合并字符串,参数可以有多个*/ 
SELECT INSERT('我爱编程helloworld',1,2,'超级热爱'); /*替换字符串,从某个位置开始替 换某个长度*/ 
SELECT LOWER('KuangShen'); /*小写*/ 
SELECT UPPER('KuangShen'); /*大写*/ 
SELECT LEFT('hello,world',5);/*从左边截取*/
SELECT RIGHT('hello,world',5); /*从右边截取*/ 
SELECT REPLACE('狂神说坚持就能成功','坚持','努力'); /*替换字符串*/ 
SELECT SUBSTR('狂神说坚持就能成功',4,6); /*截取字符串,开始和长度*/ 
SELECT REVERSE('狂神说坚持就能成功'); /*反转
-- 查询姓周的同学,改成邹 
SELECT REPLACE(studentname,'周','邹') AS 新名字 
FROM student WHERE studentname LIKE '周%';
```

日期和时间函数

```mysql
/*获取当前日期*/
SELECT CURRENT_DATE(); 
SELECT CURDATE(); 

/*获取当前日期和时间*/
SELECT NOW();
SELECT LOCALTIME(); 
SELECT SYSDATE();

-- 获取年月日,时分秒 
SELECT YEAR(NOW()); 
SELECT MONTH(NOW()); 
SELECT DAY(NOW()); 
SELECT HOUR(NOW()); 
SELECT MINUTE(NOW()); 
SELECT SECOND(NOW());
```

系统信息函数

```mysql
SELECT VERSION(); /*版本*/ 
SELECT USER();	/*用户*/
SELECT SYSTEM_USER();	/*用户*/
```



### 5.2、聚合函数

| 函数名称 | 描述                                                     |
| -------- | -------------------------------------------------------- |
| COUNT()  | 返回满足Select条件的记录总数，如 select count(*)         |
| SUM()    | 返回数字字段或表达式列作统计，返回一列的总和。           |
| AVG()    | 通常为数值字段或表达列作统计，返回一列的平均值           |
| MAX()    | 可以为数值字段，字符字段或表达式列作统计，返回最大的值。 |
| MIN()    | 可以为数值字段，字符字段或表达式列作统计，返回最小的值。 |

```mysql
-- 聚合函数 /*COUNT:非空的*/ 
SELECT COUNT(studentname) FROM student; 
SELECT COUNT(*) FROM student; /*不建议使用，效率低*/
SELECT COUNT(1) FROM student; /*推荐*/

-- 从含义上讲，count(1) 与 count(*) 都表示对全部数据行的查询。 
-- count(字段) 会统计该字段在表中出现的次数，忽略字段为null 的情况。即不统计字段为null 的记录。 
-- count(*) 包括了所有的列，相当于行数，在统计结果的时候，包含字段为null 的记录； 
-- count(1) 用1代表代码行，在统计结果的时候，包含字段为null 的记录 。 
/* 很多人认为count(1)执行的效率会比count(*)高，原因是count(*)会存在全表扫描，而count(1) 可以针对一个字段进行查询。其实不然，count(1)和count(*)都会对全表进行扫描，统计所有记录的 条数，包括那些为null的记录，因此，它们的效率可以说是相差无几。而count(字段)则与前两者不 同，它会统计该字段不为null的记录条数。

下面它们之间的一些对比：
1）在表没有主键时，count(1)比count(*)快 
2）有主键时，主键作为计算条件，count(主键)效率最高； 
3）若表格只有一个字段，则count(*)效率较高。 
*/
SELECT SUM(StudentResult) AS 总和 FROM result; 
SELECT AVG(StudentResult) AS 平均分 FROM result; 
SELECT MAX(StudentResult) AS 最高分 FROM result; 
SELECT MIN(StudentResult) AS 最低分 FROM result;
```



### 5.3、MD5加密

**MD5简介：**MD5即Message-Digest Algorithm 5（信息-摘要算法5），用于确保信息传输完整一致。是计算机广泛 使用的杂凑算法之一（又译摘要算法、哈希算法），主流编程语言普遍已有MD5实现。将数据（如汉 字）运算为另一固定长度值，是杂凑算法的基础原理，MD5的前身有MD2、MD3和MD4。
百度搜索md5介绍

二、实现数据加密

```mysql
-- MD5 加密
-- 相同的值经过MD5加密后的值也相同

CREATE TABLE `md5test`(
`id` INT(4) NOT NULL AUTO_INCREMENT,
`name` VARCHAR(20) NOT NULL,
`pwd` VARCHAR(50) NOT NULL,
PRIMARY KEY (`id`)
)ENGINE=INNODB DEFAULT CHARSET=utf8

-- 明文密码
INSERT INTO md5test(`name`,`pwd`) 
VALUES('张三','123456'),('李四','123456'),('王五','123456');

-- 加密
-- update md5test set pwd=md5(pwd) where id=1;
UPDATE md5test SET pwd=MD5(pwd);  -- 将pwd字段的所有值用MD5进行加密

-- 插入时加密
INSERT INTO md5test(`name`,pwd)
VALUES('赵六',MD5('123456'));

-- 校验：将用户传入的密码进行MD5加密，然后比对加密后的值
SELECT * FROM md5test WHERE `name`='张三' AND pwd=MD5('123456');
```



### 5.4、小结

```mysql
-- ================ 内置函数 ================

-- 数值函数 
abs(x)	-- 绝对值 abs(-10.9) = 10
format(x, d) -- 格式化千分位数值 format(1234567.456, 2) = 1,234,567.46
ceil(x)  -- 向上取整 ceil(10.1) = 11
floor(x)  -- 向下取整 floor (10.1) = 10
round(x)  -- 四舍五入去整
mod(m, n)  -- m%n m mod n 求余 10%3=1
pi()	 -- 获得圆周率
pow(m, n)  -- m^n
sqrt(x) -- 算术平方根
rand()	 -- 随机数
truncate(x, d)	-- 截取d位小数



-- 时间日期函数 
now(), current_timestamp(); -- 当前日期时间
current_date();	-- 当前日期
current_time(); -- 当前时间
date('yyyy-mm-dd hh:ii:ss'); -- 获取日期部分
time('yyyy-mm-dd hh:ii:ss'); -- 获取时间部分
date_format('yyyy-mm-dd hh:ii:ss', '%d %y %a %d %m %b %j'); -- 格式化时间
unix_timestamp(); -- 获得unix时间戳
from_unixtime();  -- 从时间戳获得时间

-- 字符串函数 
length(string)	-- string长度，字节
char_length(string) -- string的字符个数
substring(str, position [,length]) -- 从str的position开始,取length个字符
replace(str ,search_str ,replace_str) -- 在str中用replace_str替换search_str 
instr(string ,substring) -- 返回substring首次在string中出现的位置
concat(string [,...]) -- 连接字串
charset(str) lcase(string)	-- 返回字串字符集
lcase(string) -- 转换成小写
left(string, length) -- 从string2中的左边起取length个字符 -- 从文件读取内容
load_file(file_name) -- 从文件读取内容
locate(substring, string [,start_position]) -- 同instr,但可指定开始位置
lpad(string, length, pad) -- 重复用pad加在string开头,直到字串长度为length
ltrim(string) -- 去除前端空格 
repeat(string, count) -- 重复count次
strcmp(string1 ,string2)
rpad(string, length, pad) -- 在str后用pad补充,直到长度为length
rtrim(string)-- 去除后端空格
strcmp(string1,string2) -- 逐字符比较两字串大小  

-- 聚合函数 
count() 
sum(); 
max(); 
min(); 
avg(); 
group_concat()

-- 其他常用函数 
md5(); 
default();
```



## 6、事务

 ### 6.1、概述 

> 什么是事务

- 事务就是将一组SQL语句放在同一批次内去执行 
- 如果一个SQL语句出错,则该批次内的所有SQL都将被取消执行 
- MySQL事务处理只支持InnoDB和BDB数据表类型

> 事务的ACID原则 百度 ACID 

[参考博客链接](https://blog.csdn.net/dengjili/article/details/82468576)

**原子性(Atomicity)**

- 整个事务中的所有操作，要么全部完成，要么全部不完成，不可能停滞在中间某个环节。事务在执 行过程中发生错误，会被回滚（ROLLBACK）到事务开始前的状态，就像这个事务从来没有执行过 一样。

**一致性(Consistency)**

- 一个事务可以封装状态改变（除非它是一个只读的）。事务必须始终保持系统处于一致的状态，不管在任何给定的时间并发事务有多少。也就是说：如果事务是并发多个，系统也必须如同串行事务 一样操作。其主要特征是保护性和不变性(Preserving an Invariant)，以转账案例为例，假设有五 个账户，每个账户余额是100元，那么五个账户总额是500元，如果在这个5个账户之间同时发生多 个转账，无论并发多少个，比如在A与B账户之间转账5元，在C与D账户之间转账10元，在B与E之 间转账15元，五个账户总额也应该还是500元，这就是保护性和不变性。
- 过程一致性
- 最终一致性

**隔离性(Isolation)**

- 隔离状态执行事务，使它们好像是系统在给定时间内执行的唯一操作。如果有两个事务，运行在相同的时间内，执行相同的功能，事务的隔离性将确保每一事务在系统中认为只有该事务在使用系统。这种属性有时称为串行化，为了防止事务操作间的混淆，必须串行化或序列化请求，使得在同一时间仅有一个请求用于同一数据。

**持久性(Durability)**

-  在事务完成以后，该事务对数据库所作的更改便持久的保存在数据库之中，并不会被回滚。



> 隔离性所导致的一些问题

​	**脏读：**

​			指一个事务读取了另外一个事务*未提交*的数据

​	**不可重复读：**

​			在一个事务内读取表中的某一行数据，多次读取结果不同。（这个不一定是错误的，只是某些场合不对）

​	**虚读（幻读）：**

​			指在一个事务内读取到了别的事务插入的数据，导致前后读取不一致。（一般是行影响，多了一行）



> 事物的隔离级别

set transaction isolation level 设置事务隔离级别
select @@tx_isolation 查询当前事务隔离级别

| 设置             | 描述                                               |
| ---------------- | -------------------------------------------------- |
| Serializable     | 可避免脏读、不可重复读、虚读情况的发生。（串行化） |
| Repeatable read  | 可避免脏读、不可重复读情况的发生。（可重复读）     |
| Read committed   | 可避免脏读情况发生（读已提交）。                   |
| Read uncommitted | 最低级别，以上情况均无法保证。(读未提交)           |



### 6.2、事务实现 

基本语法：

```mysql
-- 使用set语句来改变自动提交模式 
SET autocommit = 0; /*关闭*/
SET autocommit = 1; /*开启*/

-- 注意: 
	-- 1.MySQL中默认是自动提交 
	-- 2.使用事务时应先关闭自动提交
	
-- 开始一个事务,标记事务的起始点 
START TRANSACTION;

-- 提交一个事务给数据库 
COMMIT;

-- 将事务回滚,数据回到本次事务的初始状态 
ROLLBACK;

-- 还原MySQL数据库的自动提交 
SET autocommit =1;

-- 保存点 
SAVEPOINT 保存点名称; -- 设置一个事务保存点 
ROLLBACK TO SAVEPOINT 保存点名称; -- 回滚到保存点
RELEASE SAVEPOINT 保存点名称; -- 删除保存点
```

事务处理步骤：

![image-20210218223707244](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210218223707244.png)



### 6.3、测试题目

```mysql
/* 课堂测试题目
张三在线买一款价格为2000元商品,网上银行转账. 
张三的银行卡余额为5000,然后给商家老板李四支付2000. 
老板李四一开始的银行卡余额为10000

创建数据库shop和创建表account并插入2条数据 
*/

-- 转账
-- 创建数据库
CREATE DATABASE shop CHARACTER SET utf8 COLLATE utf8_general_ci;

-- 创建表
CREATE TABLE IF NOT EXISTS `account`(
`id` INT(3) UNSIGNED NOT NULL AUTO_INCREMENT,
`name` VARCHAR(20) NOT NULL,
`money` DECIMAL(8,2) NOT NULL,
PRIMARY KEY (`id`)
)ENGINE=INNODB DEFAULT CHARSET=utf8

-- 插入数据
INSERT INTO `account` (`name`,`money`)
VALUES('张三',5000),('李四',10000);


-- 模拟转账：事务
SET autocommit=0; -- 关闭自动提交
START TRANSACTION; -- 开启事务

-- 处理数据
UPDATE `account` SET `money`=`money`+2000 
WHERE `name`='张三'; -- 张三减2000

UPDATE `account` SET `money`=`money`-2000
WHERE `name`='李四'; -- 李四加2000

COMMIT; -- 提交

ROLLBACK; -- 回滚

SET autocommit=1; -- 开启自动提交
```

![image-20210219155840319](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210219155840319.png)

## 7、索引

> MySQL官方对索引的定义为：**索引（Index）是帮助MySQL高效获取数据的数据结构。**
>
> 提取句子主干，就可以得到索引的本质：索引是数据结构

### 7.1、索引分类 

> 索引的作用

- 提高查询速度 
- 确保数据的唯一性
- 可以加速表和表之间的连接 , 实现表与表之间的参照完整性 
- 使用分组和排序子句进行数据检索时 , 可以显著减少分组和排序的时间 
- 全文检索字段进行搜索优化.

> 分类

- 主键索引 (Primary Key) 
- 唯一索引 (Unique) 
- 常规索引 (Index) 
- 全文索引 (FullText)



### 7.2、主键索引 

主键 : 某一个属性组能唯一标识一条记录

 特点 :

- 最常见的索引类型 
- 确保数据记录的唯一性
- 确定特定数据记录在数据库中的位置 



### 7.3、唯一索引

作用 : 避免同一个表中某数据列中的值重复 

与主键索引的区别

- 主键索引只能有一个 
- 唯一索引可能有多个

```mysql
CREATE TABLE `Grade`( 
    `GradeID` INT(11) AUTO_INCREMENT PRIMARYKEY, 
    `GradeName` VARCHAR(32) NOT NULL UNIQUE 
    -- 或 UNIQUE KEY `GradeID` (`GradeID`)
)
```



### 7.4、常规索引 

作用 : 快速定位特定数据 

注意 :

- index 和 key 关键字都可以设置常规索引 
- 应加在查询条件的字段 
- 不宜添加太多常规索引,影响数据的插入,删除和修改操作

```mysql
CREATE TABLE `result`( 
    -- 省略一些代码 
    INDEX/KEY `ind` (`studentNo`,`subjectNo`) 
    -- 创建表时添加
)

-- 创建后添加 
ALTER TABLE `result` 
ADD INDEX `ind`(`studentNo`,`subjectNo`);
```



### 7.5、全文索引 

百度搜索：全文索引 

作用 : 快速定位特定数据 

注意 :

- 只能用于MyISAM类型的数据表 
- 只能用于CHAR , VARCHAR , TEXT数据列类型 
- 适合大型数据集

```mysql
/* 
#方法一：创建表时 
	CREATE TABLE 表名 ( 
		字段名1 数据类型 [完整性约束条件…], 
		字段名2 数据类型 [完整性约束条件…], 
		[UNIQUE | FULLTEXT | SPATIAL ]
INDEX | KEY
		[索引名] (字段名[(长度)] [ASC |DESC]) 
	);
	
#方法二：CREATE在已存在的表上创建索引 
CREATE [UNIQUE | FULLTEXT | SPATIAL ] INDEX 索引名 
ON 表名 (字段名[(长度)] [ASC |DESC]) ;

#方法三：ALTER TABLE在已存在的表上创建索引 
ALTER TABLE 表名 ADD [UNIQUE | FULLTEXT | SPATIAL ] INDEX 索引名 (字段名[(长度)] [ASC |DESC]) ;

#删除索引：DROP INDEX 索引名 ON 表名字; 
#删除主键索引: ALTER TABLE 表名 DROP PRIMARY KEY;
#显示索引信息: SHOW INDEX FROM student; 
*/

/*增加全文索引*/ 
ALTER TABLE `school`.`student` 
ADD FULLTEXT INDEX `studentname` (`StudentName`);

/*EXPLAIN : 分析SQL语句执行性能*/ 
EXPLAIN SELECT * FROM student 
WHERE studentno='1000';

/*使用全文索引*/ 
-- 全文搜索通过 MATCH() 函数完成。 
-- 搜索字符串做为 against() 的参数被给定。搜索以忽略字母大小写的方式执行。对于表中的每个记录行，MATCH() 返回一个相关性值。即，在搜索字符串与记录行在 MATCH() 列表中指定的列的文本之间的相似性尺度。 
EXPLAIN SELECT *FROM student WHERE MATCH(studentname) AGAINST('love');

/* 
开始之前，先说一下全文索引的版本、存储引擎、数据类型的支持情况
MySQL 5.6 以前的版本，只有 MyISAM 存储引擎支持全文索引；
MySQL 5.6 及以后的版本，MyISAM 和 InnoDB 存储引擎均支持全文索引; 
只有字段的数据类型为 char、varchar、text 及其系列才可以建全文索引。 
测试或使用全文索引时，要先看一下自己的 MySQL 版本、存储引擎和数据类型是否支持全文索引。
```

[关于 EXPLAIN](https://blog.csdn.net/jiadajing267/article/details/81269067)



### 7.6、测试索引 

建表app_user：

```mysql
CREATE TABLE `app_user` ( 
`id` BIGINT(20) UNSIGNED NOT NULL AUTO_INCREMENT, 
`name` VARCHAR(50) DEFAULT '' COMMENT '用户昵称', 
`email` VARCHAR(50) NOT NULL COMMENT '用户邮箱', 
`phone` VARCHAR(20) DEFAULT '' COMMENT '手机号', 
`gender` TINYINT(4) UNSIGNED DEFAULT '0' COMMENT '性别（0:男；1：女）', 
`password` VARCHAR(100) NOT NULL COMMENT '密码', 
`age` TINYINT(4) DEFAULT '0' COMMENT '年龄', 
`create_time` DATETIME DEFAULT CURRENT_TIMESTAMP, 
`update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE
CURRENT_TIMESTAMP, PRIMARY KEY (`id`)
) ENGINE=INNODB DEFAULT CHARSET=utf8mb4 COMMENT='app用户表'
```

批量插入数据：100w

```mysql
-- 批量插入数据：100w条

DROP FUNCTION IF EXISTS mock_data;
DELIMITER $$ -- 写函数之前必须要写，标志
CREATE FUNCTION mock_data()
RETURNS INT
BEGIN
	DECLARE num INT DEFAULT 1000000; -- 循环次数
	DECLARE i INT DEFAULT 0;
	
	WHILE i<num DO
		-- 插入语句
		INSERT INTO school.app_user(`name`,`email`,`phone`,`gender`,`password`,`age`)
		VALUES(CONCAT('用户',i),
		'386859692@qq.com',
		CONCAT('1',FLOOR(RAND()*(9999999999-1000000000)+1000000000)),
		FLOOR(RAND()*2),
		UUID(),
		CEILING(RAND()*100)
		);
		
		SET i=i+1;
	END WHILE;
	RETURN i;
END;

SELECT mock_data(); -- 执行函数
```



```mysql
-- 不加索引
-- 测试效率
SELECT * FROM app_user WHERE `name`='用户9999'; -- Total Time: 0.583 sec
SELECT * FROM app_user WHERE `name`='用户9999'; -- Total Time: 0.713 sec
EXPLAIN SELECT * FROM app_user WHERE `name`='用户9999'; -- rows:999239

-- 创建索引
-- create index 索引名 on 表(字段);
-- 索引名：id_表名_字段名
CREATE INDEX id_app_user_name ON app_user(`name`);

SELECT * FROM app_user WHERE `name`='用户9999'; -- Total Time: 0.001 sec
EXPLAIN SELECT * FROM app_user WHERE `name`='用户9999'; -- rows:1
```

索引在小数据量的时候，用处不大，但是在大数据量的时候，优势明显！



### 7.7、索引准则 

- 索引不是越多越好
- 不要对经常变动的数据加索引 
- 小数据量的表建议不要加索引 
- 索引一般应加在查找条件的字段

### 7.8、索引的数据结构

```mysql
-- 我们可以在创建上述索引的时候，为其指定索引类型，分两类 
hash类型的索引：查询单条快，范围查询慢 
btree类型的索引：b+树，层数越多，数据量指数级增长（我们就用它，因为innodb默认支持它）

-- 不同的存储引擎支持的索引类型也不一样 
InnoDB 支持事务，支持行级别锁定，支持 B-tree、Full-text 等索引，不支持 Hash 索引； 
MyISAM 不支持事务，支持表级别锁定，支持 B-tree、Full-text 等索引，不支持 Hash 索引； 
Memory 不支持事务，支持表级别锁定，支持 B-tree、Hash 等索引，不支持 Full-text 索引； 
NDB 支持事务，支持行级别锁定，支持 Hash 索引，不支持 B-tree、Full-text 等索引； 
Archive 不支持事务，支持表级别锁定，不支持 B-tree、Hash、Full-text 等索引；
```

[关于索引的本质：](http://blog.codinglabs.org/articles/theory-of-mysql-index.html)



## 8、权限管理 

### 8.1、用户管理 

> 1、使用SQLyog 创建用户，并授予权限演示

![image-20210219174855088](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210219174855088.png)



> 2、基本命令

```mysql
/* 用户和权限管理 */ 
用户信息表：mysql.user

-- 刷新权限 
FLUSH PRIVILEGES

-- 增加用户 CREATE USER kuangshen IDENTIFIED BY '123456' 
CREATE USER 用户名 IDENTIFIED BY [PASSWORD] 密码(字符串) 
	- 必须拥有mysql数据库的全局CREATE USER权限，或拥有INSERT权限。 
	- 只能创建用户，不能赋予权限。 
	- 用户名，注意引号：如 'user_name'@'192.168.1.1' 
	- 密码也需引号，纯数字密码也要加引号 
	- 要在纯文本中指定密码，需忽略PASSWORD关键词。要把密码指定为由PASSWORD()函数返回的
混编值，需包含关键字PASSWORD

-- 重命名用户 RENAME USER kuangshen TO kuangshen2 
RENAME USER old_user TO new_user

-- 设置密码 
SET PASSWORD = '密码' -- 为当前用户设置密码 
SET PASSWORD FOR 用户名 = '密码' -- 为指定用户设置密码

-- 删除用户 DROP USER kuangshen2 
DROP USER 用户名

-- 分配权限/添加用户 
GRANT 权限列表 ON 表名 TO 用户名 [IDENTIFIED BY [PASSWORD] 'password'] [WITH GRANT OPTION;]
	- all privileges 表示所有权限 
	- *.* 表示所有库的所有表 
	- 库名.表名 表示某库下面的某表
	- WITH GRANT OPTION 用户可以将权限传递给第三方
	- 用GRANT创建新表将被弃用，建议仅用GRANT进行授权

-- 查看用户权限
	-- 查看指定用户权限 SHOW GRANTS FOR root@localhost; 
	SHOW GRANTS FOR 用户名 
	
	-- 查看当前用户权限 
	SHOW GRANTS; 或 
	SHOW GRANTS FOR CURRENT_USER; 或
	SHOW GRANTS FOR CURRENT_USER();

-- 撤消权限 REVOKE ALL PRIVILEGES ON *.* FROM zx;
REVOKE 权限列表 ON 表名 FROM 用户名

-- 撤销所有权限 REVOKE ALL PRIVILEGES,GRANT OPTION FROM zx;
REVOKE ALL PRIVILEGES, GRANT OPTION FROM 用户名 
```

> 权限解释

```mysql
-- 权限列表 
ALL [PRIVILEGES] -- 设置除GRANT OPTION之外的所有简单权限
ALTER -- 允许使用ALTER TABLE
ALTER ROUTINE  -- 更改或取消已存储的子程序

CREATE -- 允许使用CREATE TABLE 
CREATE ROUTINE -- 创建已存储的子程序
CREATE TEMPORARY TABLES -- 允许使用CREATE TEMPORARY TABLE
CREATE USER -- 允许使用CREATE USER, DROP USER, RENAME USER和REVOKE ALLPRIVILEGES。 
CREATE VIEW  -- 允许使用CREATE VIEW
DELETE  -- 允许使用DELETE
DROP -- 允许使用DROP TABLE
EXECUTE  -- 允许用户运行已存储的子程序
FILE -- 允许使用SELECT...INTO OUTFILE和LOAD DATA INFILE
INDEX  -- 允许使用CREATE INDEX和DROP INDEX
INSERT -- 允许使用INSERT

LOCK TABLES -- 允许对您拥有SELECT权限的表使用LOCK TABLES
PROCESS -- 允许使用SHOW FULL PROCESSLIST
REFERENCES  -- 未被实施
RELOAD -- 允许使用FLUSH

REPLICATION CLIENT  -- 允许用户询问从属服务器或主服务器的地址
REPLICATION SLAVE  -- 用于复制型从属服务器（从主服务器中读取二进制日志事件）
SELECT -- 允许使用SELECT
SHOW DATABASES  -- 显示所有数据库
SHOW VIEW -- 允许使用SHOW CREATE VIEW
SHUTDOWN -- 允许使用mysqladmin shutdown
SUPER -- 允许使用CHANGE MASTER, KILL, PURGE MASTER LOGS和SET GLOBAL语句，
mysqladmin debug命令；允许您连接（一次），即使已达到max_connections。 
UPDATE -- 允许使用UPDATE
USAGE -- “无权限”的同义词
GRANT OPTION -- 允许授予权限


/* 表维护 */ 
-- 分析和存储表的关键字分布 
ANALYZE [LOCAL | NO_WRITE_TO_BINLOG] TABLE 表名 ... 
-- 检查一个或多个表是否有错误 
CHECK TABLE tbl_name [, tbl_name] ... [option] ... 
option = {QUICK | FAST | MEDIUM | EXTENDED | CHANGED} 
-- 整理数据文件的碎片 
OPTIMIZE [LOCAL | NO_WRITE_TO_BINLOG] TABLE tbl_name [, tbl_name] ...
```



### 8.2、MySQL备份 

数据库备份必要性

- 保证重要数据不丢失 
- 数据转移

MySQL数据库备份方法

- mysqldump备份工具 
- 数据库管理工具,如SQLyog 
- 直接拷贝数据库文件和相关配置文件

**mysqldump客户端** 

作用 :

- 转储数据库
- 搜集数据库进行备份 
- 将数据转移到另一个SQL服务器,不一定是MySQL服务器

![image-20210219191521662](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210219191521662.png)



```mysql
-- 导出 
-- 默认主机为localhost即: -hlocalhost
1. 导出一张表 -- mysqldump -uroot -p123456 school student >D:/a.sql 
	mysqldump [-h主机] -u用户名 -p密码 库名 表名 > 文件名(D:/a.sql)

2. 导出多张表 -- mysqldump -uroot -p123456 school student result >D:/a.sql 
	mysqldump [-h主机] -u用户名 -p密码 库名 表1 表2 表3 > 文件名(D:/a.sql)

3. 导出所有表 -- mysqldump -uroot -p123456 school >D:/a.sql 
	mysqldump [-h主机] -u用户名 -p密码 库名 > 文件名(D:/a.sql)

4. 导出一个库 -- mysqldump -uroot -p123456 -B school >D:/a.sql 
	mysqldump [-h主机] -u用户名 -p密码 -B 库名 > 文件名(D:/a.sql)

可以-w携带备份条件

-- 导入 
1. 在登录mysql的情况下： -- source D:/a.sql 
	source 备份文件
	注意：在导入一张表时需要先 use 数据库

2. 在不登录的情况下 
	导入一个库 -- mysql -uroot -proot <e:/mysql/db_school.sql
	导入一张表 -- mysql -uroot -proot shop <e:/mysql/tb_account.sql
	mysql -u用户名 -p密码 库名 < 备份文件
```



## 9、规范化数据库设计 

### 9.1、为什么需要数据库设计 

==当数据库比较复杂时我们需要设计数据库==

*糟糕的数据库设计 :*

- 数据冗余,存储空间浪费 
- 数据更新和插入的异常 
- 程序性能差



*良好的数据库设计 :*

- 节省数据的存储空间 
- 能够保证数据的完整性 
- 方便进行数据库应用系统的开发



*软件项目开发周期中数据库设计 :*

- 需求分析阶段: 分析客户的业务和数据处理需求 
- 概要设计阶段:设计数据库的E-R模型图 , 确认需求信息的正确和完整.



*设计数据库步骤* 

- 收集信息
  - 与该系统有关人员进行交流 , 座谈 , 充分了解用户需求 , 理解数据库需要完成的任务. 
- 标识实体[Entity]
  - 标识数据库要管理的关键对象或实体,实体一般是名词 
  - 标识每个实体需要存储的详细信息[Attribute] 
  - 标识实体之间的关系[Relationship]



***设计数据库的步骤：(个人博客)***

- 收集信息，分析需求
  - 用户表（用户登录注销，用户的个人信息，写博客，创建分类）
  - 分类表（文章分类，谁创建的）
  - 文章表（文章的信息）
  - 评论表
  - 友链表（友链信息）
  - 自定义表（系统信息，某个关键的字，或者一些主字段）  key：value
  - 说说表（发表心情.. id... content... create_time)
- 标识实体（把需求落地到每个字段）
- 标识实体之间的关系
  - 写博客：user-blog
  - 创建分类：user-category
  - 关注：user-user
  - 友链：links
  - 评论：user-user-blog



### 9.2、三大范式 

*问题 : 为什么需要数据规范化?* 

不合规范的表设计会导致的问题： 

- 信息重复 
- 更新异常 
- 插入异常
  - 无法正确表示信息
- 删除异常 
  - 丢失有效信息



> 百度搜索：三大范式

[参考](https://www.cnblogs.com/wsg25/p/9615100.html)

 ***第一范式 (1st NF)***

​		目标：确保每列的原子性。如果每列都是不可再分的最小数据单元,则满足第一范式 



***第二范式（2NF）***

​		前提：先满足第一 范式（1NF）。

​		目标：消除部分依赖。第二范式要求每个表只描述一件事情，即确保数据库表中的每一列都和主键相关，而不能和主键的一部分相关 。（主要针对联合主键而言）



***第三范式(3rd NF)***

​		前提：满足第二范式

​		目标：消除传递依赖。除了主键以外的其他列都不传递依赖于主键列，第三范式需要确保数据表中的每一列数据都和主键直接相关，而不能间接相关。



***规范化和性能的关系*** 

- 为满足某种商业目标 , 数据库性能比规范化数据库更重要 
- 在数据规范化的同时 , 要综合考虑数据库的性能 
- 通过在给定的表中添加额外的字段,以大量减少需要从中搜索信息所需的时间 
- 通过在给定的表中插入计算列,以方便查询



## 10、JDBC 

### 10.1、数据库驱动

这里的驱动的概念和平时听到的那种驱动的概念是一样的，比如平时购买的声卡，网卡直接插到计算机 上面是不能用的，必须要安装相应的驱动程序之后才能够使用声卡和网卡，同样道理，我们安装好数据 库之后，我们的应用程序也是不能直接使用数据库的，必须要通过相应的数据库驱动程序，通过驱动程 序去和数据库打交道，如下所示：

![image-20210220205138161](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210220205138161.png)



### 10.2、JDBC介绍

SUN公司为了简化、统一对数据库的操作，定义了一套Java操作数据库的规范（接口），称之为JDBC。 这套接口由数据库厂商去实现，这样，开发人员只需要学习jdbc接口，并通过jdbc加载具体的驱动，就 可以操作数据库。
如下图所示：

![image-20210220205221961](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210220205221961.png)

​		JDBC全称为：Java Data Base Connectivity（java数据库连接），它主要由接口组成。 组成JDBC的２个包：java.sql、javax.sql 

​		开发JDBC应用需要以上2个包的支持外，还需要导入相应JDBC的数据库实现(即数据库驱动)。



### 10.3、编写JDBC程序 

> 搭建实验环境

```mysql
CREATE DATABASE jdbcStudy CHARACTER SET utf8 COLLATE utf8_general_ci; 

USE jdbcStudy;

CREATE TABLE `users`( 
`id` INT PRIMARY KEY, 
`name` VARCHAR(40), 
`password` VARCHAR(40), 
`email` VARCHAR(60), birthday DATE
);


INSERT INTO users(id,`name`,`password`,email,birthday) 
VALUES(1,'zhansan','123456','zs@sina.com','1980-12-04'), 
(2,'lisi','123456','lisi@sina.com','1981-12-04'), 
(3,'wangwu','123456','wangwu@sina.com','1979-12-04');
```

> 新建一个Java工程，并导入数据驱动

![image-20210220205529613](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210220205529613.png)

> 编写程序从user表中读取数据，并打印在命令行窗口中。

```java
package com.zx.lesson01;

import java.sql.*;

/**
 * @Description: com.zx.lesson01
 * @version: 1.0
 */
public class JdbcDemo01 {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        //1、加载驱动
        Class.forName("com.mysql.jdbc.Driver");
        //2、建立链接
        String url="jdbc:mysql://localhost:3306/jdbcStudy?useUnicode=true&characterEncoding=utf8";//&useSSL=true 报错可以不加这个，或者更换connector版本。
        String username="root";
        String password="root";
        Connection conn = DriverManager.getConnection(url, username, password);
        //3、执行sql并获得结果集
        String sql="select * from users";
        Statement stat = conn.createStatement();
        ResultSet rs = stat.executeQuery(sql);
        //4、处理结果集
        while (rs.next()){
            System.out.println("id="+rs.getInt("id"));
            System.out.println("name="+rs.getString("name"));
            System.out.println("password="+rs.getString("password"));
            System.out.println("email="+rs.getString("email"));
            System.out.println("birthday="+rs.getDate("birthday"));
            System.out.println("--------------------------------------------");
        }
        //5、关闭连接
        rs.close();
        stat.close();
        conn.close();
    }
}

```



### 10.4、对象说明 

> DriverManager类讲解

Jdbc程序中的DriverManager用于加载驱动，并创建与数据库的链接，这个API的常用方法：

```java
DriverManager.registerDriver(new Driver()) DriverManager.getConnection(url, user,password)
```



注意：在实际开发中并不推荐采用registerDriver方法注册驱动。原因有二： 

1. 查看Driver的源代码可以看到，如果采用此种方式，会导致驱动程序注册两次，也就是在内存中会 有两个Driver对象。

2. 程序依赖mysql的api，脱离mysql的jar包，程序将无法编译，将来程序切换底层数据库将会非常麻烦。

推荐方式：Class.forName("com.mysql.jdbc.Driver");

采用此种方式不会导致驱动对象在内存中重复出现，并且采用此种方式，程序仅仅只需要一个字符串， 不需要依赖具体的驱动，使程序的灵活性更高。

> 数据库URL讲解 

URL用于标识数据库的位置，通过URL地址告诉JDBC程序连接哪个数据库，URL的写法为：

![image-20210220210326799](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210220210326799.png)



常用数据库URL地址的写法： 

- Oracle写法：jdbc:oracle:thin:@localhost:1521:sid 
- SqlServer写法：jdbc:microsoft:sqlserver://localhost:1433;DatabaseName=sid 
- MySql写法：jdbc:mysql://localhost:3306/sid 

如果连接的是本地的Mysql数据库，并且连接使用的端口是3306，那么的url地址可以简写为 

`jdbc:mysql:///数据库`



> Connection类讲解

​		Jdbc程序中的Connection，它用于代表数据库的链接，Collection是数据库编程中最重要的一个对象， 客户端与数据库所有交互都是通过connection对象完成的，这个对象的常用方法：

- createStatement()：创建向数据库发送sql的statement对象。 
- prepareStatement(sql) ：创建向数据库发送预编译sql的PrepareSatement对象。 
- setAutoCommit(boolean autoCommit)：设置事务是否自动提交。 
- commit() ：在链接上提交事务。 
- rollback() ：在此链接上回滚事务。



> Statement类讲解

 Jdbc程序中的Statement对象用于向数据库发送SQL语句， Statement对象常用方法：

- executeQuery(String sql) ：用于向数据发送查询语句。 
- executeUpdate(String sql)：用于向数据库发送insert、update或delete语句 
- execute(String sql)：用于向数据库发送任意sql语句 
- addBatch(String sql) ：把多条sql语句放到一个批处理中。 
- executeBatch()：向数据库发送一批sql语句执行。



> ResultSet类讲解

Jdbc程序中的ResultSet用于代表Sql语句的执行结果。Resultset封装执行结果时，采用的类似于表格的 方式。ResultSet 对象维护了一个指向表格数据行的游标，初始的时候，游标在第一行之前，调用 ResultSet.next() 方法，可以使游标指向具体的数据行，进行调用方法获取该行的数据。

ResultSet既然用于封装执行结果的，所以该对象提供的都是用于获取数据的get方法： 

- 获取任意类型的数据 
  - getObject(int index)
  - getObject(string columnName) 
- 获取指定类型的数据，例如：
  - getString(int index) 
  - getString(String columnName) 



ResultSet还提供了对结果集进行滚动的方法：
- next()：移动到下一行 
- Previous()：移动到前一行 
- absolute(int row)：移动到指定行 
- beforeFirst()：移动resultSet的最前面。 
- afterLast() ：移动到resultSet的最后面。



> 释放资源

​		Jdbc程序运行完后，切记要释放程序在运行过程中，创建的那些与数据库进行交互的对象，这些对象通 常是ResultSet, Statement和Connection对象，特别是Connection对象，它是非常稀有的资源，用完后 必须马上释放，如果Connection不能及时、正确的关闭，极易导致系统宕机。Connection的使用原则 是尽量晚创建，尽量早的释放。

​		为确保资源释放代码能运行，资源释放代码也一定要放在finally语句中。



### 10.5、statement对象

​		Jdbc中的statement对象用于向数据库发送SQL语句，想完成对数据库的增删改查，只需要通过这个对象 向数据库发送增删改查语句即可。

​		Statement对象的executeUpdate方法，用于向数据库发送增、删、改的sql语句，executeUpdate执行 完后，将会返回一个整数（即增删改语句导致了数据库几行数据发生了变化）。

​		Statement.executeQuery方法用于向数据库发送查询语句，executeQuery方法返回代表查询结果的 ResultSet对象。



> CRUD操作-create 

使用executeUpdate(String sql)方法完成数据添加操作，示例操作：

```java
Statement st = conn.createStatement(); 
String sql = "insert into user(….) values(…..) "; 
int num = st.executeUpdate(sql); 
if(num>0){ 
    System.out.println("插入成功！！！");
}
```



> CRUD操作-read 

使用executeQuery(String sql)方法完成数据查询操作，示例操作：

```java
Statement st = conn.createStatement(); 
String sql = "select * from user where id=1"; 
ResultSet rs = st.executeUpdate(sql); 
while(rs.next()){ 
    //根据获取列的数据类型，分别调用rs的相应方法映射到java对象中
}
```



> CRUD操作-update 

使用executeUpdate(String sql)方法完成数据修改操作，示例操作：

```java
Statement st = conn.createStatement(); 
String sql = "update user set name='' where name=''"; 
int num = st.executeUpdate(sql); 
if(num>0){ 
    System.out.println(“修改成功！！！");
}
```



> CRUD操作-delete 

使用executeUpdate(String sql)方法完成数据删除操作，示例操作：

```java
Statement st = conn.createStatement(); 
String sql = "delete from user where id=1"; 
int num = st.executeUpdate(sql); 
if(num>0){ 
    System.out.println(“删除成功！！！");
}
```





***使用jdbc对数据库增删改查*** 

1、新建一个 lesson02 的包 

2、在src目录下创建一个db.properties文件，如下图所示：

```properties
driver=com.mysql.jdbc.Driver
url=jdbc:mysql://localhost:3306/jdbcStudy?useUnicode=true&characterEncoding=utf8
username=root
password=root
```

3、在lesson02 下新建一个 utils 包，新建一个类 JdbcUtils

```java
package com.zx.lesson02.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * @Description: com.zx.lesson02.utils
 * @version: 1.0
 */
public class JdbcUtils {
    private static String driver=null;
    private static String url=null;
    private static String username=null;
    private static String password=null;

    static{
        try {
            //读取db.properties文件中的数据库连接信息
            InputStream in = JdbcUtils.class.getClassLoader().getResourceAsStream("db.properties");
            Properties prop = new Properties();
            prop.load(in);

            //获取数据库连接驱动
            driver=prop.getProperty("driver");
            //获取数据库连接URL地址
            url=prop.getProperty("url");
            //获取数据库连接用户名
            username=prop.getProperty("username");
            //获取数据库连接密码
            password=prop.getProperty("password");

            //加载数据库驱动
            Class.forName(driver);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取数据库连接
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url,username,password);
    }

    //释放资源，要释放的资源包括Connection数据库连接对象，负责执行SQL命令的Statement对象，存储查询结果的ResultSet对象
    public static void release(Connection conn, Statement st, ResultSet rs){
        if(rs!=null){
            try {
                //关闭存储查询结果的ResultSet对象
                rs.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            rs=null;
        }
        if (st!=null){
            try {
                //关闭负责执行SQL命令的Statement对象
                st.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        if (conn!=null){
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}

```



> 使用statement对象完成对数据库的CRUD操作 

1、插入一条数据

```java
package com.zx.lesson02.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @Description: com.zx.lesson02.utils
 * @version: 1.0
 */
public class TestInsert {
    public static void main(String[] args) {
        Connection conn=null;
        Statement st=null;
        ResultSet rs=null;
        try {
            //获得数据库连接
            conn=JdbcUtils.getConnection();
            //通过conn对象获取负责执行SQL命令的Statement对象
            st = conn.createStatement();
            //要执行的SQL命令
            String sql="insert into users(id,name,password,email,birthday) " +
                    "values(4,'zhouxu','123456','386859692@qq.com','2021-2-20')";
            //执行插入操作，executeUpdate方法
            int num = st.executeUpdate(sql);
            if (num>0){
                System.out.println("插入成功！");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            JdbcUtils.release(conn,st,rs);
        }
    }
}

```



2、删除一条数据

```java
package com.zx.lesson02.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @Description: com.zx.lesson02.utils
 * @version: 1.0
 */
public class TestDelete {
    public static void main(String[] args) {
        Connection conn=null;
        Statement st=null;
        ResultSet rs=null;
        try {
            //建立连接
            conn=JdbcUtils.getConnection();
            //获取执行sql的Statement对象
            st=conn.createStatement();
            //创建sql
            String sql="delete from users where id=4";
            int num = st.executeUpdate(sql);
            if (num>0){
                System.out.println("删除成功！");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            //关闭连接
            JdbcUtils.release(conn,st,rs);
        }
    }
}

```



3、更新一条数据

```java
package com.zx.lesson02.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @Description: com.zx.lesson02.utils
 * @version: 1.0
 */
public class TestUpdate {
    public static void main(String[] args) {
        Connection conn=null;
        Statement st=null;
        ResultSet rs=null;

        try {
            //建立连接
            conn=JdbcUtils.getConnection();
            st = conn.createStatement();
            //创建sql语句
            String sql="update users set password='root' where name='zhouxu'";
            int num = st.executeUpdate(sql);
            if (num>0){
                System.out.println("更新成功！");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            JdbcUtils.release(conn,st,rs);
        }
    }
}

```



4、查询数据

```java
package com.zx.lesson02.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @Description: com.zx.lesson02.utils
 * @version: 1.0
 */
public class TestSelect {
    public static void main(String[] args) {
        Connection conn=null;
        Statement st=null;
        ResultSet rs=null;
        try {
            //建立连接
            conn=JdbcUtils.getConnection();
            //获取执行sql语句的Statement对象
            st=conn.createStatement();
            //创建sql语句
            String sql="select * from users";
            rs=st.executeQuery(sql);
            //处理结果集
            while (rs.next()){
                System.out.print("| "+rs.getString("id")+" | ");
                System.out.print(rs.getString("name")+" | ");
                System.out.print(rs.getString("password")+" | ");
                System.out.print(rs.getString("email")+" | ");
                System.out.print(rs.getString("birthday")+" |");
                System.out.println();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            JdbcUtils.release(conn,st,rs);
        }
    }
}

```



> SQL 注入问题

==通过巧妙的技巧来拼接字符串，造成SQL短路，从而获取数据库数据==

```java
package com.zx.lesson02.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @Description: com.zx.lesson02.utils
 * @version: 1.0
 */
//sql注入
public class SqlInjection {
    public static void main(String[] args) {
        //login("lisi","123456");//正常sql
        login("lisi","' or '1=1");
    }
    public static void login(String username,String password){
        Connection conn=null;
        Statement st=null;
        ResultSet rs=null;
        try {
            conn=JdbcUtils.getConnection();
            st=conn.createStatement();
            String sql="select * from users where name='"+username+"' and password='"+password+"';";
            rs=st.executeQuery(sql);
            while(rs.next()){
                System.out.println("用户名："+rs.getString("name"));
                System.out.println("密码："+rs.getString("password"));
                System.out.println("-----------------------------------");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally{
            JdbcUtils.release(conn,st,rs);
        }
    }
}

```



### 10.6、PreparedStatement对象

PreperedStatement是Statement的子类，它的实例对象可以通过调用 `Connection.preparedStatement()`方法获得，

相对于Statement对象而言：PreperedStatement可以避 免SQL注入的问题。

Statement会使数据库频繁编译SQL，可能造成数据库缓冲区溢出。

PreparedStatement可对SQL进行预编译，从而提高数据库的执行效率。并且PreperedStatement对于 sql中的参数，允许使用占位符的形式进行替换，简化sql语句的编写。

> 使用PreparedStatement对象完成对数据库的CRUD操作 

1、插入数据

```java
package com.zx.lesson02.utils;

import java.sql.*;
import java.text.SimpleDateFormat;

/**
 * @Description: com.zx.lesson02.utils
 * @version: 1.0
 */
public class TestInsert {
    public static void main(String[] args) {
        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;
        try {
            //获得数据库连接
            conn=JdbcUtils.getConnection();
            //通过conn对象获取负责执行SQL命令的Statement对象
            //要执行的SQL命令
            String sql="insert into users(id,name,password,email,birthday) " +
                    "values(?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setInt(1,5);//4,'zhouxu','123456','386859692@qq.com','2021-2-20'
            ps.setString(2,"erha");
            ps.setString(3,"250250");
            ps.setString(4,"3838438@gmail.com");
            ps.setDate(5,new Date(System.currentTimeMillis()));
            //执行插入操作，executeUpdate方法
            int num = ps.executeUpdate();
            if (num>0){
                System.out.println("插入成功！");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            JdbcUtils.release(conn,ps,rs);
        }
    }
}

```

2、删除数据

```java
package com.zx.lesson02.utils;

import java.sql.*;

/**
 * @Description: com.zx.lesson02.utils
 * @version: 1.0
 */
public class TestDelete {
    public static void main(String[] args) {
        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;
        try {
            //建立连接
            conn=JdbcUtils.getConnection();
            //创建sql
            String sql="delete from users where id=?";
            //获取执行sql的Statement对象
            ps=conn.prepareStatement(sql);
            ps.setInt(1,5);
            int num = ps.executeUpdate();
            if (num>0){
                System.out.println("删除成功！");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            //关闭连接
            JdbcUtils.release(conn,ps,rs);
        }
    }
}

```

3、更新数据

```java
package com.zx.lesson02.utils;

import java.sql.*;

/**
 * @Description: com.zx.lesson02.utils
 * @version: 1.0
 */
public class TestUpdate {
    public static void main(String[] args) {
        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;

        try {
            //建立连接
            conn=JdbcUtils.getConnection();
            //创建sql语句
            String sql="update users set password=? where name=?";
            //创建负责执行sql的PreparedStatement对象
            ps = conn.prepareStatement(sql);
            //为sql语句中的参数赋值
            ps.setString(1,"haha");
            ps.setString(2,"zhouxu");
            int num = ps.executeUpdate();
            if (num>0){
                System.out.println("更新成功！");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            JdbcUtils.release(conn,ps,rs);
        }
    }
}

```

4、查询数据

```java
package com.zx.lesson02.utils;

import java.sql.*;

/**
 * @Description: com.zx.lesson02.utils
 * @version: 1.0
 */
public class TestSelect {
    public static void main(String[] args) {
        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;
        try {
            //建立连接
            conn=JdbcUtils.getConnection();
            //创建sql语句
            String sql="select * from users;";
            //获取执行sql语句的PreparedStatement对象
            ps=conn.prepareStatement(sql);
            //为sql语句中的参数赋值

            rs=ps.executeQuery();
            //处理结果集
            while (rs.next()){
                System.out.print("| "+rs.getString("id")+" | ");
                System.out.print(rs.getString(2)+" | ");
                System.out.print(rs.getString("password")+" | ");
                System.out.print(rs.getString("email")+" | ");
                System.out.print(rs.getString("birthday")+" |");
                System.out.println();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally {
            JdbcUtils.release(conn,ps,rs);
        }
    }
}

```



> 避免SQL 注入

```java
package com.zx.lesson02.utils;

import java.sql.*;

/**
 * @Description: com.zx.lesson02.utils
 * @version: 1.0
 */
//避免sql注入
public class SqlInjection {
    public static void main(String[] args) {
        //login("lisi","123456");//正常sql
        login("lisi","' or '1=1");
    }
    public static void login(String username,String password){
        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;
        try {
            conn=JdbcUtils.getConnection();
            //String sql="select * from users where name='"+username+"' and password='"+password+"';";
            String sql="select * from users where name=? and password=?";
            ps=conn.prepareStatement(sql);
            ps.setString(1,username);
            ps.setString(2,password);
            rs=ps.executeQuery();
            while(rs.next()){
                System.out.println("用户名："+rs.getString("name"));
                System.out.println("密码："+rs.getString("password"));
                System.out.println("-----------------------------------");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }finally{
            JdbcUtils.release(conn,ps,rs);
        }
    }
}

```

原理：执行的时候参数会用引号包起来，并把参数中的引号作为转义字符，从而避免了参数也作为条件 的一部分



### 10.7、事务 

> 概念

事务指逻辑上的一组操作，组成这组操作的各个单元，要不全部成功，要不全部不成功。 

> ACID 原则

原子性(Atomic)

- 整个事务中的所有操作，要么全部完成，要么全部不完成，不可能停滞在中间某个环节。事务在执 行过程中发生错误，会被回滚（ROLLBACK）到事务开始前的状态，就像这个事务从来没有执行过一样。 

一致性(Consist)

- 一个事务可以封装状态改变（除非它是一个只读的）。事务必须始终保持系统处于一致的状态，不管在任何给定的时间并发事务有多少。也就是说：如果事务是并发多个，系统也必须如同串行事务 一样操作。其主要特征是保护性和不变性(Preserving an Invariant)，以转账案例为例，假设有五 个账户，每个账户余额是100元，那么五个账户总额是500元，如果在这个5个账户之间同时发生多 个转账，无论并发多少个，比如在A与B账户之间转账5元，在C与D账户之间转账10元，在B与E之 间转账15元，五个账户总额也应该还是500元，这就是保护性和不变性。

隔离性(Isolated)

- 隔离状态执行事务，使它们好像是系统在给定时间内执行的唯一操作。如果有两个事务，运行在相 同的时间内，执行相同的功能，事务的隔离性将确保每一事务在系统中认为只有该事务在使用系 统。这种属性有时称为串行化，为了防止事务操作间的混淆，必须串行化或序列化请求，使得在同 一时间仅有一个请求用于同一数据。

持久性(Durable) 

- 在事务完成以后，该事务对数据库所作的更改便持久的保存在数据库之中，并不会被回滚。 



> 隔离性问题

1、脏读：脏读指一个事务读取了另外一个事务未提交的数据。 

2、不可重复读：不可重复读指在一个事务内读取表中的某一行数据，多次读取结果不同。 

3、虚读(幻读) : 虚读(幻读)是指在一个事务内读取到了别的事务插入的数据，导致前后读取不一致。



> 代码测试

```java
/*创建账户表*/
create table account(
    id int auto_increment,
    name varchar(40),
    money float,
    primary key (`id`)
    );

/*插入测试数据*/
insert into account (name,money) values('A',1000),('B',1000),('C',1000);
```

当Jdbc程序向数据库获得一个Connection对象时，默认情况下这个Connection对象会自动向数据库提交 在它上面发送的SQL语句。若想关闭这种默认提交方式，让多条SQL在一个事务中执行，可使用下列的 JDBC控制事务语句

- Connection.setAutoCommit(false);//开启事务(start transaction) 
- Connection.rollback();//回滚事务(rollback) 
- Connection.commit();//提交事务(commit)

> 程序编写 

1、模拟转账成功时的业务场景

```java
package com.zx.lesson03;

import com.zx.lesson02.utils.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Description: com.zx.lesson03
 * @version: 1.0
 */
public class TestTransaction01 {
    public static void main(String[] args) {
        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;
        try {
            conn=JdbcUtils.getConnection();
            //关闭自动提交，会默认开启事务
            conn.setAutoCommit(false);

            String sql1="update account set money=money-? where name=?;";
            ps=conn.prepareStatement(sql1);
            ps.setInt(1,100);
            ps.setString(2,"A");
            ps.executeUpdate();

            String sql2="update account set money=money+? where name=?;";
            ps=conn.prepareStatement(sql2);
            ps.setInt(1,100);
            ps.setString(2,"B");
            ps.executeUpdate();

            conn.commit();//提交事务
            conn.setAutoCommit(true);//开启自动提交
        } catch (SQLException throwables) {
            try {
                conn.rollback();//发生异常就回滚
            } catch (SQLException e) {
                e.printStackTrace();
            }
            throwables.printStackTrace();
        }finally{
            JdbcUtils.release(conn,ps,rs);
        }
    }
}

```



2、模拟转账过程中出现异常导致有一部分SQL执行失败后让数据库自动回滚事务

```java
package com.zx.lesson03;

import com.zx.lesson02.utils.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Description: com.zx.lesson03
 * @version: 1.0
 */
public class TestTransaction02 {
    public static void main(String[] args) {
        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;
        try {
            conn=JdbcUtils.getConnection();
            //关闭自动提交，会默认开启事务
            conn.setAutoCommit(false);

            String sql1="update account set money=money-? where name=?;";
            ps=conn.prepareStatement(sql1);
            ps.setInt(1,100);
            ps.setString(2,"A");
            ps.executeUpdate();

            int i=1/0;//创建一个异常语句

            String sql2="update account set money=money+? where name=?;";
            ps=conn.prepareStatement(sql2);
            ps.setInt(1,100);
            ps.setString(2,"B");
            ps.executeUpdate();

            conn.commit();//提交事务
            conn.setAutoCommit(true);//开启自动提交
        }catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        finally{
            JdbcUtils.release(conn,ps,rs);
        }
    }
}

```



3、模拟转账过程中出现异常导致有一部分SQL执行失败时手动通知数据库回滚事务

```java
package com.zx.lesson03;

import com.zx.lesson02.utils.JdbcUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Description: com.zx.lesson03
 * @version: 1.0
 */
public class TestTransaction03 {
    public static void main(String[] args) {
        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;
        try {
            conn=JdbcUtils.getConnection();
            //关闭自动提交，会默认开启事务
            conn.setAutoCommit(false);

            String sql1="update account set money=money-? where name=?;";
            ps=conn.prepareStatement(sql1);
            ps.setInt(1,100);
            ps.setString(2,"A");
            ps.executeUpdate();

            int i=1/0;//创建一个异常语句

            String sql2="update account set money=money+? where name=?;";
            ps=conn.prepareStatement(sql2);
            ps.setInt(1,100);
            ps.setString(2,"B");
            ps.executeUpdate();

            conn.commit();//提交事务
            conn.setAutoCommit(true);//开启自动提交
        }catch (SQLException throwables) {

            throwables.printStackTrace();
        }catch (Exception exception){
            try {
                conn.rollback();//发生异常就回滚
                System.out.println("发生异常，已回滚！");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        finally{
            JdbcUtils.release(conn,ps,rs);
        }
    }
}



```



### 10.8、数据库连接池

用户每次请求都需要向数据库获得链接，而数据库创建连接通常需要消耗相对较大的资源，创建时间也 较长。假设网站一天10万访问量，数据库服务器就需要创建10万次连接，极大的浪费数据库的资源，并 且极易造成数据库服务器内存溢出、拓机。

> 数据库连接池的基本概念

数据库连接是一种关键的有限的昂贵的资源,这一点在多用户的网页应用程序中体现的尤为突出.对数据库 连接的管理能显著影响到整个应用程序的伸缩性和健壮性,影响到程序的性能指标.数据库连接池正是针对这个问题提出来的**.数据库连接池负责分配,管理和释放数据库连接,它允许应用程序重复使用一个现有的 数据库连接,而不是重新建立一个。**

数据库连接池在初始化时将创建一定数量的数据库连接放到连接池中, 这些数据库连接的数量是由 *最小数据库连接数* 来设定的.无论这些数据库连接是否被使用,连接池都将一直保证至少拥有这么多的连接数量. 连接池的 *最大数据库连接数量* 限定了这个连接池能占有的最大连接数,当应用程序向连接池请求的连接数 超过最大连接数量时,这些请求将被加入到等待队列中.

数据库连接池的最小连接数和最大连接数的设置要考虑到以下几个因素:

1. 最小连接数:是连接池一直保持的数据库连接,所以如果应用程序对数据库连接的使用量不大,将会有 大量的数据库连接资源被浪费.
2. 最大连接数:是连接池能申请的最大连接数,如果数据库连接请求超过次数,后面的数据库连接请求将 被加入到等待队列中,这会影响以后的数据库操作
3. 如果最小连接数与最大连接数相差很大:那么最先连接请求将会获利,之后超过最小连接数量的连接 请求等价于建立一个新的数据库连接.不过,这些大于最小连接数的数据库连接在使用完不会马上被 释放,他将被放到连接池中等待重复使用或是空间超时后被释放.



==编写连接池需实现java.sql.DataSource接口。==

> 开源数据库连接池

现在很多WEB服务器(Weblogic, WebSphere, Tomcat)都提供了DataSoruce的实现，即连接池的实现。 

**通常我们把DataSource的实现，按其英文含义称之为数据源，数据源中都包含了数据库连接池的实现。**

 也有一些开源组织提供了数据源的独立实现：

- DBCP 数据库连接池 
- C3P0 数据库连接池

在使用了数据库连接池之后，在项目的实际开发中就不需要编写连接数据库的代码了，直接从数据源获 得数据库的连接。

> DBCP数据源

  DBCP 是 Apache 软件基金组织下的开源连接池实现，要使用DBCP数据源，需要应用程序应在系统中增 加如下两个 jar 文件：

- Commons-dbcp.jar：连接池的实现 
- Commons-pool.jar：连接池实现的依赖库

Tomcat 的连接池正是采用该连接池来实现的。该数据库连接池既可以与应用服务器整合使用，也可由 应用程序独立使用。

测试： 

1、导入相关jar包 

2、在类目录下加入dbcp的配置文件：dbcpconfig.properties

```properties
#连接设置
driverClassName=com.mysql.jdbc.Driver
# mysql-connector-java-5.1.46 不支持&useSSL=true
url=jdbc:mysql://localhost:3306/jdbcStudy? useUnicode=true&characterEncoding=utf8
username=root
password=root

#<!-- 初始化连接 -->
initialSize=10

#最大连接数量
maxActive=50

#<!-- 最大空闲连接 -->
maxIdle=20

#<!-- 最小空闲连接 -->
minIdle=5

#<!-- 超时等待时间以毫秒为单位 6000毫秒/1000等于60秒 -->
maxWait=60000

#JDBC驱动建立连接时附带的连接属性属性的格式必须为这样：[属性名=property;] #注意："user" 与 "password" 两个属性会被明确地传递，因此这里不需要包含他们。
connectionProperties=useUnicode=true;characterEncoding=UTF8

#指定由连接池所创建的连接的自动提交（auto-commit）状态。
defaultAutoCommit=true

#driver default 指定由连接池所创建的连接的只读（read-only）状态。 #如果没有设置该值，则“setReadOnly”方法将不被调用。（某些驱动并不支持只读模式，如： Informix）
defaultReadOnly=

#driver default 指定由连接池所创建的连接的事务级别（TransactionIsolation）。 #可用值为下列之一：（详情可见javadoc。）NONE,READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE
defaultTransactionIsolation=READ_UNCOMMITTED
```

3、编写工具类 JdbcUtils_dbcp

```java
package com.zx.lesson04;

import com.zx.lesson02.utils.JdbcUtils;
import org.apache.commons.dbcp2.BasicDataSourceFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * @Description: com.zx.lesson04
 * @version: 1.0
 */
//数据库连接工具类
public class JdbcUtils_dbcp {
    /**
     * 在java中，编写数据库连接池需实现java.sql.DataSource接口，每一种数据库连接池都是DataSource接口的实现
     * DBCP连接池就是java.sql.DataSource接口的一个具体实现
     */
    private static DataSource ds=null;

    static{
        try {
            //读取db.properties文件中的数据库连接信息
            InputStream in = JdbcUtils.class.getClassLoader().getResourceAsStream("dbcpConfig.properties");
            Properties prop = new Properties();
            prop.load(in);
            ds=BasicDataSourceFactory.createDataSource(prop);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取数据库连接
    public static Connection getConnection() throws Exception {
        return ds.getConnection();
    }

    //释放资源，要释放的资源包括Connection数据库连接对象，负责执行SQL命令的Statement对象，存储查询结果的ResultSet对象
    public static void release(Connection conn, Statement st, ResultSet rs){
        if(rs!=null){
            try {
                //关闭存储查询结果的ResultSet对象
                rs.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            rs=null;
        }
        if (st!=null){
            try {
                //关闭负责执行SQL命令的Statement对象
                st.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        if (conn!=null){
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}

```

测试类

```java
package com.zx.lesson04;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Description: com.zx.lesson04
 * @version: 1.0
 */
public class TestDBCP {
    public static void main(String[] args) {
        Connection conn =null;
        PreparedStatement ps=null;
        ResultSet rs=null;
        try {
            conn = JdbcUtils_dbcp.getConnection();
            String sql="select * from users";
            ps=conn.prepareStatement(sql);
            rs=ps.executeQuery();
            while (rs.next()){
                System.out.println(rs.getString("name"));
                System.out.println(rs.getString("password"));
                System.out.println("------------------");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            JdbcUtils_dbcp.release(conn,ps,rs);
        }
    }
}

```



> C3P0

C3P0是一个开源的JDBC连接池，它实现了数据源和JNDI绑定，支持JDBC3规范和JDBC2的标准扩展。目 前使用它的开源项目有Hibernate， Spring等。C3P0数据源在项目开发中使用得比较多。

**c3p0与dbcp区别**

- dbcp没有自动回收空闲连接的功能 
- c3p0有自动回收空闲连接功能

测试 

1、导入相关jar包 

- c3p0-0.9.1.2.jar
- mchange-commons-java-0.2.9.jar

2、在类目录下加入C3P0的配置文件：c3p0-config.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>

<c3p0-config>
    <!--
    C3P0的缺省(默认)配置， 如果在代码中“ComboPooledDataSource ds = new ComboPooledDataSource();”这样写
    就表示使用的是C3P0的缺省(默认)配置信息来创建数据源
    -->
        <default-config>
            <property name="driverClass">com.mysql.jdbc.Driver</property>
            <property name="jdbcUrl">jdbc:mysql://localhost:3306/jdbcStudy? useUnicode=true&amp;characterEncoding=utf8</property>
            <property name="user">root</property>
            <property name="password">root</property>

            <property name="acquireIncrement">5</property>
            <property name="initialPoolSize">10</property>
            <property name="minPoolSize">5</property>
            <property name="maxPoolSize">20</property>
        </default-config>

        <!--C3P0的命名配置，如果在代码中“ComboPooledDataSource ds = new ComboPooledDataSource("MySQL");
        ”这样写就表示使用的是name是MySQL的配置信息来创建数据源
        -->
    <named-config name="MySQL">
        <property name="driverClass">com.mysql.jdbc.Driver</property>
        <property name="jdbcUrl">jdbc:mysql://localhost:3306/jdbcStudy? useUnicode=true&amp;characterEncoding=utf8</property>
        <property name="user">root</property>
        <property name="password">root</property>

        <property name="acquireIncrement">5</property>
        <property name="initialPoolSize">10</property>
        <property name="minPoolSize">5</property>
        <property name="maxPoolSize">20</property>
    </named-config>
</c3p0-config>
```



3、创建工具类

```java
package com.zx.lesson04;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/**
 * @Description: com.zx.lesson04
 * @version: 1.0
 */
public class JdbcUtils_c3p0 {
    /**
     * 在java中，编写数据库连接池需实现java.sql.DataSource接口，每一种数据库连接池都是DataSource接口的实现
     * C3P0连接池就是java.sql.DataSource接口的一个具体实现
     */
    private static ComboPooledDataSource ds=null;

    static{
        try {
            //通过代码创建c3p0数据库连接池
            /*ds=new ComboPooledDataSource();
            ds.setDriverClass("com.mysql.jdbc.Driver");
            ds.setJdbcUrl("jdbc:mysql://localhost:3306/jdbcstudy");
            ds.setUser("root");
            ds.setPassword("root");
            ds.setInitialPoolSize(10);
            ds.setMinPoolSize(5);
            ds.setMaxPoolSize(20);*/

            //通过c3p0的xml配置文件创建数据源，c3p0的xml配置文件c3p0-config.xml必须放在src目录下
            //ds=new ComboPooledDataSource();//使用c3p0的默认配置来创建数据源
            //ds= new ComboPooledDataSource("MySQL");//使用C3P0的命名配置来创建数据源
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //获取数据库连接
    public static Connection getConnection() throws Exception {
        return ds.getConnection();
    }

    //释放资源，要释放的资源包括Connection数据库连接对象，负责执行SQL命令的Statement对象，存储查询结果的ResultSet对象
    public static void release(Connection conn, Statement st, ResultSet rs){
        if(rs!=null){
            try {
                //关闭存储查询结果的ResultSet对象
                rs.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            rs=null;
        }
        if (st!=null){
            try {
                //关闭负责执行SQL命令的Statement对象
                st.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
        if (conn!=null){
            try {
                conn.close();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }
}

```

测试

```java
package com.zx.lesson04;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @Description: com.zx.lesson04
 * @version: 1.0
 */
public class TestC3P0 {
    public static void main(String[] args) {
        Connection conn=null;
        PreparedStatement ps=null;
        ResultSet rs=null;
        try {
            conn=JdbcUtils_c3p0.getConnection();
            String sql="select * from users";
            ps=conn.prepareStatement(sql);
            rs=ps.executeQuery();
            while (rs.next()){
                System.out.println(rs.getString("name"));
                System.out.println(rs.getString("password"));
                System.out.println("------------------");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally{
            JdbcUtils_c3p0.release(conn,ps,rs);
        }
    }
}

```



> 结论

无论使用什么数据源，本质都是一样的DateSource接口不变，方法就不会变



> 扩展:Druid



Apache

![image-20210222170304730](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210222170304730.png)