# --原理探究--

# SQL执行过程详解

## 前言

天天和数据库打交道，一天能写上几十条 SQL 语句，但你知道我们的系统是如何和数据库交互的吗？MySQL 如何帮我们存储数据、又是如何帮我们管理事务？....是不是感觉真的除了写几个 「select * from dual」外基本脑子一片空白？这篇文章就将带你走进 MySQL 的世界，让你彻底了解系统到底是如何和 MySQL 交互的，MySQL 在接受到我们发送的 SQL 语句时又分别做了哪些事情。

## MySQL 驱动

我们的系统在和 MySQL 数据库进行通信的时候，总不可能是平白无故的就能接收和发送请求，就算是你没有做什么操作，那总该是有其他的“人”帮我们做了一些事情，基本上使用过 MySQL 数据库的程序员多多少少都会知道 MySQL 驱动这个概念的。就是这个 MySQL  驱动在底层帮我们做了对数据库的连接，只有建立了连接了，才能够有后面的交互。看下图表示

![图片](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLWokGSuq7zUUt8IprrQgGNf70jpmxe3icUyzccJIqHVsD2SZ6keVcwTpXNFm40sicMvl5XJ9uZw06ng/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这样的话，在系统和 MySQL 进行交互之前，MySQL 驱动会帮我们建立好连接，然后我们只需要发送 SQL 语句就可以执行 CRUD 了。一次 SQL 请求就会建立一个连接，多个请求就会建立多个连接，那么问题来了，我们系统肯定不是一个人在使用的，换句话说肯定是存在多个请求同时去争抢连接的情况。我们的 web 系统一般都是部署在 tomcat 容器中的，而  tomcat  是可以并发处理多个请求的，这就会导致多个请求会去建立多个连接，然后使用完再都去关闭，这样会有什么问题呢？如下图

![图片](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLWokGSuq7zUUt8IprrQgGNfQBLAItzIorBIv1qeNmU6U9KYaNAAuFdzwA7OZWZ836XZeB2MCJLAUQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

java 系统在通过 MySQL 驱动和 MySQL 数据库连接的时候是基于 TCP/IP 协议的，所以如果每个请求都是新建连接和销毁连接，那这样势必会造成不必要的浪费和性能的下降，也就说上面的多线程请求的时候频繁的创建和销毁连接显然是不合理的。必然会大大降低我们系统的性能，但是如果给你提供一些固定的用来连接的线程，这样是不是不需要反复的创建和销毁连接了呢？相信懂行的朋友会会心一笑，没错，说的就是数据库连接池。

> 数据库连接池：维护一定的连接数，方便系统获取连接，使用就去池子中获取，用完放回去就可以了，我们不需要关心连接的创建与销毁，也不需要关心线程池是怎么去维护这些连接的。

![图片](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLWokGSuq7zUUt8IprrQgGNfvXRQnIYlSqEiaI6UyjzpsgHy11dYZKWtS9h4rCg4cezs8pFqHA29F5w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)常见的数据库连接池有 `Druid、C3P0、DBCP`，连接池实现原理在这里就不深入讨论了，采用连接池大大节省了不断创建与销毁线程的开销，这就是有名的「池化」思想，不管是线程池还是 HTTP 连接池，都能看到它的身影。

## 数据库连接池

到这里，我们已经知道的是我们的系统在访问  MySQL  数据库的时候，建立的连接并不是每次请求都会去创建的，而是从数据库连接池中去获取，这样就解决了因为反复的创建和销毁连接而带来的性能损耗问题了。不过这里有个小问题，业务系统是并发的，而 MySQL 接受请求的线程呢，只有一个？

其实 MySQL 的架构体系中也已经提供了这样的一个池子，也是数据库连池。双方都是通过数据库连接池来管理各个连接的，这样一方面线程不需要争抢连接，更重要的是不需要反复的创建的销毁连接。

![图片](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLWokGSuq7zUUt8IprrQgGNflg3qVzZYzz1mbRSFp6ZqURR1ECQtbr4TJpdxL4uwq4DgPDakbcQynA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

至此系统和 MySQL 数据库之间的连接问题已经说明清楚了。那么 MySQL 数据库中的这些连接是怎么来处理的，又是谁来处理呢？

## 网络连接必须由线程来处理

对计算基础稍微有一点了解的的同学都是知道的，网络中的连接都是由线程来处理的，所谓网络连接说白了就是一次请求，每次请求都会有相应的线程去处理的。也就是说对于 SQL 语句的请求在 MySQL  中是由一个个的线程去处理的。

![图片](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLWokGSuq7zUUt8IprrQgGNfJ2IfBfUiaHzGSNMLpZAhzviaicdIX1TYgmfUnVbPqAFY9ukg16gCUnlEA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

那这些线程会怎么去处理这些请求？会做哪些事情？

## SQL 接口

MySQL 中处理请求的线程在获取到请求以后获取 SQL 语句去交给 SQL 接口去处理。

## 查询解析器

假如现在有这样的一个 SQL

```sql
SELECT stuName,age,sex FROM students WHERE id=1
```

但是这个 SQL 是写给我们人看的，机器哪里知道你在说什么？这个时候`解析器`就上场了。他会将  SQL  接口传递过来的 SQL 语句进行解析，翻译成 MySQL 自己能认识的语言，至于怎么解析的就不需要在深究了，无非是自己一套相关的规则。

![图片](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLWokGSuq7zUUt8IprrQgGNf1kuOMEU6ichJzHJ11VcCzRJZnPhaOxCR2mmzmdjz8zHDhaSnXTpSjKg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)现在 SQL 已经被解析成  MySQL  认识的样子的，那下一步是不是就是执行吗？理论上是这样子的，但是 MySQL 的强大远不止于此，他还会帮我们选择最优的查询路径。

> 什么叫最优查询路径？就是 MySQL 会按照自己认为的效率最高的方式去执行查询

具体是怎么做到的呢？这就要说到  MySQL  的查询优化器了

## MySQL 查询优化器

查询优化器内部具体怎么实现的我们不需要关心，我需要知道的是  MySQL  会帮我去使用他自己认为的最好的方式去优化这条  SQL  语句，并生成一条条的执行计划，比如你创建了多个索引，MySQL 会依据**成本最小原则**来选择使用对应的索引，这里的成本主要包括两个方面, IO 成本和 CPU 成本

**IO 成本**: 即从磁盘把数据加载到内存的成本，默认情况下，读取数据页的 IO 成本是 1，MySQL 是以页的形式读取数据的，即当用到某个数据时，并不会只读取这个数据，而会把这个数据相邻的数据也一起读到内存中，这就是有名的`程序局部性原理`，所以 MySQL 每次会读取一整页，一页的成本就是 1。所以 IO 的成本主要和页的大小有关

**CPU 成本**：将数据读入内存后，还要检测数据是否满足条件和排序等 CPU 操作的成本，显然它与行数有关，默认情况下，检测记录的成本是 0.2。

MySQL 优化器 会计算 「IO 成本 + CPU」 成本最小的那个索引来执行

![图片](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLWokGSuq7zUUt8IprrQgGNfT1Xib6icyGFJEqiatXs4qWImPYbic88j7xBJssmP1iazogoAEO0fkic7VwPQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)


优化器执行选出最优索引等步骤后，会去调用存储引擎接口，开始去执行被  MySQL  解析过和优化过的 SQL 语句

## 存储引擎

查询优化器`会调用`存储引擎的接口，去执行  SQL，也就是说真正执行  SQL  的动作是在存储引擎中完成的。数据是被存放在内存或者是磁盘中的（存储引擎是一个非常重要的组件，后面会详细介绍）

> 本篇文章大家先对存储引擎有一个大致的认识就可以了。

## 执行器

执行器是一个非常重要的组件，因为前面那些组件的操作最终必须通过执行器去调用存储引擎接口才能被执行。执行器最终最根据一系列的执行计划去调用存储引擎的接口去完成  SQL  的执行

![图片](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLWokGSuq7zUUt8IprrQgGNfGdcTjTMIeaziaCjqQfMVPOiboHa9ibg4XoDamrPZn7uTRzskl7mULfK8Q/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

## 初识存储引擎

我们以一个更新的SQL语句来说明，SQL 如下

```sql
UPDATE students SET stuName = '小强' WHERE id = 1
```

当我们系统发出这样的查询去交给 MySQL 的时候，MySQL 会按照我们上面介绍的一系列的流程最终通过执行器调用存储引擎去执行，流程图就是上面那个。在执行这个 SQL 的时候 SQL 语句对应的数据要么是在内存中，要么是在磁盘中，如果直接在磁盘中操作，那这样的随机IO读写的速度肯定让人无法接受的，所以每次在执行 SQL 的时候都会将其数据加载到内存中，这块内存就是 InnoDB 中一个非常重要的组件：**缓冲池** Buffer Pool

## Buffer Pool

Buffer Pool （缓冲池）是 **InnoDB** 存储引擎中非常重要的内存结构，顾名思义，缓冲池其实就是类似  Redis  一样的作用，起到一个缓存的作用，因为我们都知道 **MySQL** 的数据最终是存储在磁盘中的，如果没有这个 Buffer Pool  那么我们每次的数据库请求都会磁盘中查找，这样必然会存在 IO 操作，这肯定是无法接受的。但是有了 Buffer Pool 就是我们第一次在查询的时候会将查询的结果存到  Buffer Pool 中，这样后面再有请求的时候就会先从缓冲池中去查询，如果没有再去磁盘中查找，然后在放到  Buffer Pool 中，如下图

![图片](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLWokGSuq7zUUt8IprrQgGNfHP4YricreLnokZAuFcW7g9WtEytnnyhE0quauAn4nvqK4P8WkahUXKA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)


按照上面的那幅图，这条 SQL 语句的执行步骤大致是这样子的

1. Innodb 存储引擎会在缓冲池中查找 id=1 的这条数据是否存在
2. 发现不存在，那么就会去磁盘中加吧载，并将其存放在缓冲池中
3. 该条记录会被加上一个独占锁（总不能你在修改的时候别人也在修改吧，这个机制本篇文章不重点介绍，以后会专门写文章来详细讲解）

## undo 日志文件：记录数据被修改前的样子

undo 顾名思义，就是没有做，没发生的意思。undo log  就是没有发生事情（原本事情是什么）的一些日志

我们刚刚已经说了，在准备更新一条语句的时候，该条语句已经被加载到 Buffer pool 中了，实际上这里还有这样的操作，就是在将该条语句加载到 Buffer Pool 中的时候同时会往 undo 日志文件中插入一条日志，也就是将 id=1 的这条记录的原来的值记录下来。

这样做的目的是什么？

Innodb 存储引擎的最大特点就是支持事务，如果本次更新失败，也就是事务提交失败，那么该事务中的所有的操作都必须回滚到执行前的样子，也就是说当事务失败的时候，也不会对原始数据有影响，看图说话

![图片](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLWokGSuq7zUUt8IprrQgGNfhU6v1dvLgQyoTUPNkr5BnqCPQwfS0S5tE9fW0lKADuTNPZE7ZlYSfQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这里说句额外话，其实 MySQL  也是一个系统，就好比我们平时开发的 java 的功能系统一样，MySQL  使用的是自己相应的语言开发出来的一套系统而已，它根据自己需要的功能去设计对应的功能，它即然能做到哪些事情，那么必然是设计者们当初这么定义或者是根据实际的场景变更演化而来的。所以大家放平心态，把 MySQL 当作一个系统去了解熟悉他。

到这一步，我们的执行的 SQL 语句已经被加载到 Buffer Pool 中了，然后开始更新这条语句，更新的操作实际是在Buffer Pool中执行的，那问题来了，按照我们平时开发的一套理论`缓冲池中的数据和数据库中的数据不一致时候，我们就认为缓存中的数据是脏数据`，那此时 Buffer Pool 中的数据岂不是成了脏数据？没错，目前这条数据就是脏数据，Buffer Pool 中的记录是`小强` 数据库中的记录是`旺财` ，这种情况 MySQL是怎么处理的呢，继续往下看

## redo 日志文件：记录数据被修改后的样子

除了从磁盘中加载文件和将操作前的记录保存到 undo 日志文件中，其他的操作是在内存中完成的，内存中的数据的特点就是：断电丢失。如果此时 MySQL 所在的服务器宕机了，那么 Buffer Pool 中的数据会全部丢失的。这个时候 redo 日志文件就需要来大显神通了

***画外音：redo 日志文件是 InnoDB 特有的，他是存储引擎级别的，不是 MySQL 级别的***

redo 记录的是数据修改之后的值，不管事务是否提交都会记录下来，例如，此时将要做的是`update students set stuName='小强' where id=1;` 那么这条操作就会被记录到 redo log buffer 中，啥？怎么又出来一个 redo log buffer ,很简单，MySQL 为了提高效率，所以将这些操作都先放在内存中去完成，然后会在**某个时机**将其持久化到磁盘中。

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

截至目前，我们应该都熟悉了 MySQL 的执行器调用存储引擎是怎么将一条 SQL 加载到缓冲池和记录哪些日志的，流程如下：

1. 准备更新一条 SQL 语句
2. MySQL（innodb）会先去缓冲池（BufferPool）中去查找这条数据，没找到就会去磁盘中查找，如果查找到就会将这条数据加载到缓冲池（BufferPool）中
3. 在加载到 Buffer Pool 的同时，会将这条数据的原始记录保存到 undo 日志文件中
4. innodb 会在 Buffer Pool 中执行更新操作
5. 更新后的数据会记录在 redo log buffer 中

上面说的步骤都是在正常情况下的操作，但是程序的设计和优化并不仅是为了这些正常情况而去做的，也是为了**那些临界区和极端情况下出现的问题**去优化设计的

这个时候如果服务器宕机了，那么缓存中的数据还是丢失了。真烦，竟然数据总是丢失，那能不能不要放在内存中，直接保存到磁盘呢？很显然不行，因为在上面也已经介绍了，在内存中的操作目的是为了提高效率。

此时，如果 MySQL 真的宕机了，那么没关系的，因为 MySQL 会认为本次事务是失败的，所以数据依旧是更新前的样子，并不会有任何的影响。

好了，语句也更新好了那么需要将更新的值提交啊，也就是需要提交本次的事务了，因为只要事务成功提交了，才会将最后的变更保存到数据库，**在提交事务前**仍然会具有相关的其他操作

将  `redo Log Buffer` 中的数据持久化到磁盘中，就是将 redo log buffer 中的数据写入到 redo log 磁盘文件中，一般情况下，redo log Buffer 数据写入磁盘的策略是立即刷入磁盘（**具体策略情况在下面小总结出会详细介绍**）,上图

![图片](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLWokGSuq7zUUt8IprrQgGNfOk7mmibBDwuicCo8KNiaOBe3gX1zVSK35pZ9Bc08uvaqUkodFp5iala6rA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

如果 redo log Buffer 刷入磁盘后，数据库服务器宕机了，那我们更新的数据怎么办？此时数据是在内存中，数据岂不是丢失了？不，这次数据就不会丢失了，因为 redo log buffer 中的数据已经被写入到磁盘了，已经被持久化了，就算数据库宕机了，在下次重启的时候 MySQL 也会将 redo 日志文件内容恢复到 Buffer Pool 中（这边我的理解是和  Redis  的持久化机制是差不多的，在  Redis  启动的时候会检查 rdb 或者是 aof 或者是两者都检查，根据持久化的文件来将数据恢复到内存中）

到此为止，从执行器开始调用存储引擎接口做了哪些事情呢？

1.准备更新一条 SQL 语句

2.MySQL（innodb）会先去缓冲池（BufferPool）中去查找这条数据，没找到就会去磁盘中查找，如果查找到就会将这条数据加载

到缓冲池（BufferPool）中 3.在加载到 Buffer Pool 的同时，会将这条数据的原始记录保存到 undo 日志文件中

4.innodb 会在 Buffer Pool 中执行更新操作

5.更新后的数据会记录在 redo log buffer 中

`到此是前面已经总结过的`

6.MySQL 提交事务的时候，会将 redo log buffer 中的数据写入到 redo 日志文件中 刷磁盘可以通过 innodb_flush_log_at_trx_commit 参数来设置

值为 0 表示不刷入磁盘

值为 1 表示立即刷入磁盘

值为 2 表示先刷到 os cache

7.myslq 重启的时候会将 redo 日志恢复到缓冲池中

截止到目前为止，MySQL  的执行器调用存储引擎的接口去执行【执行计划】提供的 SQL 的时候 InnoDB 做了哪些事情也就基本差不多了，但是这还没完。下面还需要介绍下 MySQL 级别的日志文件 `bin log`

## bin log 日志文件：记录整个操作过程

上面介绍到的`redo log`是  InnoDB  存储引擎特有的日志文件，而`bin log`属于是  MySQL  级别的日志。`redo log`记录的东西是偏向于物理性质的，如：“对什么数据，做了什么修改”。`bin log`是偏向于逻辑性质的，类似于：“对 students 表中的 id 为 1 的记录做了更新操作” 两者的主要特点总结如下:

| 性质     | redo Log                                                     | bin Log                                                      |
| -------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 文件大小 | redo log 的大小是固定的（配置中也可以设置，一般默认的就足够了） | bin log 可通过配置参数`max_bin log_size`设置每个`bin log`文件的大小（但是一般不建议修改）。 |
| 实现方式 | `redo log`是`InnoDB`引擎层实现的（也就是说是 Innodb  存储引擎过独有的） | `bin log`是  MySQL  层实现的，所有引擎都可以使用 `bin log`日志 |
| 记录方式 | redo log 采用循环写的方式记录，当写到结尾时，会回到开头循环写日志。 | bin log 通过追加的方式记录，当文件大小大于给定值后，后续的日志会记录到新的文件上 |
| 使用场景 | `redo log`适用于崩溃恢复(crash-safe)（这一点其实非常类似与 Redis 的持久化特征） | `bin log `适用于主从复制和数据恢复                           |

bin log文件是如何刷入磁盘的?

bin log 的刷盘是有相关的策略的，策略可以通过`sync_bin log`来修改，默认为 0，表示先写入 os cache，也就是说在提交事务的时候，数据不会直接到磁盘中，这样如果宕机`bin log`数据仍然会丢失。所以建议将`sync_bin log`设置为 1 表示**直接将数据写入到磁盘**文件中。

刷入 bin log 有以下几种模式

**1、 STATMENT**

基于 SQL 语句的复制(statement-based replication, SBR)，每一条会修改数据的 SQL 语句会记录到 bin log 中

【优点】：不需要记录每一行的变化，减少了 bin log 日志量，节约了 IO , 从而提高了性能

【缺点】：在某些情况下会导致主从数据不一致，比如执行sysdate()、slepp()等

**2、ROW**

基于行的复制(row-based replication, RBR)，不记录每条SQL语句的上下文信息，仅需记录哪条数据被修改了

【优点】：不会出现某些特定情况下的存储过程、或 function、或 trigger 的调用和触发无法被正确复制的问题

【缺点】：会产生大量的日志，尤其是 alter table 的时候会让日志暴涨

**3、MIXED**

基于 STATMENT 和 ROW 两种模式的混合复制( mixed-based replication, MBR )，一般的复制使用 STATEMENT 模式保存 bin log ，对于 STATEMENT 模式无法复制的操作使用 ROW 模式保存 bin log

那既然`bin log`也是日志文件，那它是在什么记录数据的呢？

其实 MySQL 在提交事务的时候，不仅仅会将 redo log buffer  中的数据写入到`redo log` 文件中，同时也会将本次修改的数据记录到 bin log文件中，同时会将本次修改的`bin log`文件名和修改的内容在`bin log`中的位置记录到`redo log`中，最后还会在`redo log`最后写入 commit 标记，这样就表示本次事务被成功的提交了。

![图片](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLWokGSuq7zUUt8IprrQgGNfKMKJyG93TvwBp36LT61Js9Bmn8QCLAzxTdhGJ4LmPqMFcPayHyuRibg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

如果在数据被写入到bin log文件的时候，刚写完，数据库宕机了，数据会丢失吗？

首先可以确定的是，只要`redo log`最后没有 commit 标记，说明本次的事务一定是失败的。但是数据是没有丢失了，因为已经被记录到`redo log`的磁盘文件中了。在 MySQL 重启的时候，就会将 `redo log` 中的数据恢复（加载）到`Buffer Pool`中。

好了，到目前为止，一个更新操作我们基本介绍得差不多，但是你有没有感觉少了哪件事情还没有做？是不是你也发现这个时候被更新记录仅仅是在内存中执行的，哪怕是宕机又恢复了也仅仅是将更新后的记录加载到`Buffer Pool`中，这个时候 MySQL 数据库中的这条记录依旧是旧值，也就是说内存中的数据在我们看来依旧是脏数据，那这个时候怎么办呢？

其实 MySQL 会有一个后台线程，它会在某个时机将我们`Buffer Pool`中的脏数据刷到 MySQL 数据库中，这样就将内存和数据库的数据保持统一了。

![图片](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLWokGSuq7zUUt8IprrQgGNf3YX5fs5AlSAtSAll4c02pR8whdEM7g0ibpicjXWg3EibQXzqTiaOd9RKtg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

## 本文总结

到此，关于Buffer Pool、Redo Log Buffer 和undo log、redo log、bin log 概念以及关系就基本差不多了。

我们再回顾下 

1. Buffer Pool 是 MySQL 的一个非常重要的组件，因为针对数据库的增删改操作都是在 Buffer Pool 中完成的
2. Undo log 记录的是数据操作前的样子
3. redo log 记录的是数据被操作后的样子（redo log 是 Innodb 存储引擎特有）
4. bin log 记录的是整个操作记录（这个对于`主从复制`具有非常重要的意义）

从准备更新一条数据到事务的提交的流程描述

1. 首先执行器根据 MySQL 的执行计划来查询数据，先是从缓存池中查询数据，如果没有就会去数据库中查询，如果查询到了就将其放到缓存池中
2. 在数据被缓存到缓存池的同时，会写入 undo log 日志文件
3. 更新的动作是在 BufferPool 中完成的，同时会将更新后的数据添加到 redo log buffer 中
4. 完成以后就可以提交事务，在提交的同时会做以下三件事
5. （第一件事）将redo log buffer中的数据刷入到 redo log 文件中
6. （第二件事）将本次操作记录写入到 bin log文件中
7. （第三件事）将 bin log 文件名字和更新内容在 bin log 中的位置记录到redo log中，同时在 redo log 最后添加 commit 标记

至此表示整个更新事务已经完成

## 结束语

到此为止，系统是如何和 MySQL 数据库打交道，提交一条更新的 SQL 语句到 MySQL，MySQL 执行了哪些流程，做了哪些事情从宏观上都已经讲解完成了。

![图片](https://mmbiz.qpic.cn/mmbiz_gif/b96CibCt70iabwjyojLhA03PtxUnkNPREnt2F48ywfXLpDdDAjicOTPI8Q94tVLbJ58tbRs12iaXDKhUOW9gd4NlFA/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

---

# MySQL的数据存在磁盘上到底长什么样

## 前言

在之前我们聊过了**为什么 MySQL 索引要用 B+tree ，而且还这么快。**里面曾多处提到了找数据要从我们电脑的磁盘上找，今天就来说一说 `MySQL` 中的数据在磁盘上，**它到底是如何进行存储的**？长什么样？

## 存储引擎

百度百科是这样定义存储引擎的：`MySQL` 中的数据用各种不同的技术存储在文件（或者内存）中，这些不同的技术以及配套的相关功能在 `MySQL` 中被称作存储引擎。

简单来说就是**不同的存储引擎，我们的数据存储的格式也会不一样。**就好比图片有不同的格式，比如：`.jpg`, `.png`, `.gif` 等等……



> 扫盲：存储引擎是作用在`表`上的。



现在 `MySQL` 中**常用的**存储引擎有两种：`MyISAM` 和 `InnoDB`。

`MySQL` **5.5之前**，`MyISAM` 是默认的存储引擎。

`MySQL` **5.5开始**，`InnoDB` 是默认的存储引擎。

### 主要区别

|          |  MyISAM  |      InnoDB      |
| :------: | :------: | :--------------: |
|   事务   | 不支持❌  |       支持       |
| 表/行锁  | 只有表锁 |   还引入了行锁   |
|   外键   | 不支持❌  |      支持✔       |
| 全文索引 |  支持✔   | 版本5.6 开始支持 |
| 读写速度 |   更快   |       更慢       |

`MyISAM` 最致命的一点就是不支持事务，而 `InnoDB` 支持。所以现在 `InnoDB` 已经成为我们使用的标配、最主流的存储引擎了。

### 相关命令

查询当前数据库支持的存储引擎

```sql
show engines;
```

查询当前默认的存储引擎

```sql
show variables like '%storage_engine%';
```

查询表的相关信息

```sql
show table status like '表名';
```

## MyISAM

每个 `MyISAM` 表都以3个文件存储在磁盘上。这些文件的名称以表名开头，以扩展名指示文件类型。

`.frm` 文件（frame）存储表**结构**；

`.MYD` 文件（MY Data）存储表**数据**；

`.MYI` 文件（MY Index）存储表**索引**。

`MySQL` 里的数据默认是存放在安装目录下的 data 文件夹中，也可以自己修改。

![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4aLAQ3BhPBxHbSvoOia6GmibSlXbKOysXfaO5Skkia0zzJ1UUvSufa6E9QNgwAicqxb8WH3PrQIdrOEbQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

下面我创建了以 `MyISAM` 作为存储引擎的一张表 t_user_myisam。

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

`.MYI` 文件组织索引的方式就是 `B+tree`。叶子节点的 value 处存放的就是**索引所在行的磁盘文件地址**。

![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4aLAQ3BhPBxHbSvoOia6GmibS6UC3YmIhKw5tAsTCCEmcqlPj6ibJLVITtOw0464c1jaao2ia5GRAzJicg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**底层查找过程**：

首先会判断查找条件 `where` 中的字段是否是索引字段，如果是就会先拿着这字段去 `.MYI` 文件里通过 `B+tree` 快速定位，从根节点开始定位查找；

找到后再把这个索引关键字（就是我们的条件）存放的磁盘文件地址拿到 `.MYD` 文件里面找，从而定位到索引所在行的记录。

> “
>
> 表逻辑上相邻的记录行数据在磁盘上并不一定是物理相邻的。
>
> ”

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

## InnoDB

一张 `InnoDB` 表底层会对应**2个**文件在文件夹中进行数据存储。

`.frm` 文件（frame）存储表**结构**；

`.ibd` 文件（InnoDB Data）存储表**索引+数据**。

下面我创建了以 `InnoDB` 作为存储引擎的一张表 t_user_innodb。

![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4aLAQ3BhPBxHbSvoOia6GmibSgSq6JC0ezfhiabw6fH1y3BzNSOqPzk4wibib35NV6dbVXkOBr8sFrgqJA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

很显然，`InnoDB` 把索引和数据都放在一个文件里存着了。毫无疑问，`InnoDB` 表里面的数据也是用 `B+tree` 数据结构组织起来的。

下面我们来看看它具体是怎么存储的。

![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4aLAQ3BhPBxHbSvoOia6GmibSgocfYO5Heh2wLHovE943qticGdtZkqQHfwY7btjUzZxJMP45zVNZgXQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

`.ibd` 存储数据的特点就是 `B+tree` 的叶子节点上**包括了我们要的索引和该索引所在行的其它列数据**。

**底层查找过程**：

首先会判断查找条件 `where` 中的字段是否是索引字段，如果是就会先拿着这字段去 `.ibd` 文件里通过 `B+tree` 快速定位，从根节点开始定位查找；

找到后直接把这个索引关键字及其记录所在行的其它列数据返回。

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

## 聚集（聚簇）索引

**聚集索引**：叶子节点包含了完整的数据记录。

简单来说就是索引和它所在行的其它列数据全部都在一起了。

很显然，`MyISAM` 没有聚集索引，`InnoDB` 有，而且 `InnoDB` 的主键索引就是天然的聚集索引。

有聚集索引当然就有**非聚集索引（稀疏索引）**。对于 `MyISAM` 来说，它的索引就是非聚集索引。因为它的**索引**和**数据**是**分开**两个文件存的：一个 `.MYI` 存索引，一个 `.MYD` 存数据。

## 为什么 DBA 都建议表中一定要有主键，而且推荐使用整型自增？

> “
>
> 注意：这里是推荐，没说一定。非要用 UUID 也不拦着你😁
>
> ”

### 为什么要有主键？

因为 `InnoDB` 表里面的数据必须要有一个 `B+tree` 的索引结构来组织、维护我们的整张表的所有数据，从而形成 `.idb` 文件。

**那和主键有什么关系？**

如果 `InnoDB` 创建了一张没有主键的表，那这张表就有可能没有任何索引，则 `MySQL`会选择所有具有唯一性并且不为 null 中的第一个字段的创建聚集索引。

如果没有唯一性索引的字段就会有一个隐式字段成为表的聚集索引：而这个隐式字段，就是 `InnoDB` 帮我们创建的一个长度为 6字节 的整数列 `ROW_ID`，它随着新行的插入单调增加，`InnoDB` 就以该列对数据进行聚集。

使用这个 `ROW_ID` 列的表都共享一个相同的**全局**序列计数器（这是数据字典的一部分）。为了避免这个 `ROW_ID` 用完，所以建议表中一定要单独建立一个主键字段。

### 为什么推荐使用整型自增？

首先整型的**占用空间**会比字符串**小**，而且在**查找**上**比大小**也会比字符串更**快**。字符串比大小的时候还要先转换成 ASCII 码再去比较。

如果**使用自增**的话，在**插入**方面的效率也会提高。

不使用自增，可能时不时会往 `B+tree` 的中间某一位置插入元素，当这个节点位置放满了的时候，节点就要进行分裂操作（效率低）再去维护，有可能树还要进行平衡，又是一个耗性能的操作。

都用自增就会永远都往后面插入元素，这样索引节点分裂的概率就会小很多。

## 二级索引

除聚集索引之外的所有索引都叫做二级索引，也称辅助索引。

它的叶子节点则不会存储其它所有列的数据，就**只存储主键值**。

![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4aLAQ3BhPBxHbSvoOia6GmibSfw0sIic3Rv61ibSS7O0iaiavbyeS4upl0lM2IIvdUf95ytwWsh1VibmfAzQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**底层查找过程**：

每次要找数据的时候，会根据它找到对应叶子节点的主键值，再把它拿到聚集索引的 `B+tree` 中查找，从而拿到整条记录。

![图片](https://mmbiz.qpic.cn/mmbiz_gif/wRTybk8SK4aLAQ3BhPBxHbSvoOia6GmibShE5fCt4vVGK1AAAVf5TZNQpUDe07OlhOqUtUMXKbol0wqk4Pp0WKww/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

**优点：保持一致性和节省空间。**

## 参考资料

1. https://blog.jcole.us/innodb/

![图片](https://mmbiz.qpic.cn/mmbiz_gif/b96CibCt70iabwjyojLhA03PtxUnkNPREnt2F48ywfXLpDdDAjicOTPI8Q94tVLbJ58tbRs12iaXDKhUOW9gd4NlFA/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

---



# 数据库索引到底是什么

##  **说在前面** 

数据库超级重要，这个大家应该清楚，学过数据库的朋友一定知道，数据库在使用时，即使没有加索引也可以运行，但是所有学习数据库的资料、教程，一定会有大量的篇幅在介绍数据库索引，各种后端开发工作的**面试**也一定绕不开索引，甚至可以说数据库索引是从后端初级开发跨越到高级开发的**屠龙宝刀**，那么索引到底在服务端程序中起着怎样的作用呢？

------

##  **到底什么是索引？** 

用一句话来描述：数据库索引就是一种**加快海量数据查询的关键技术**。现在还不理解这句话？不要紧，往下看，20分钟以后你就能自己做出这样的总结来了。

首先给大家看一张图片

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzovMibFOicARkDrkgzvogDkm2UmPrYtx960QQTMWIqjGeMiasGxcpzWf4vvHdjwkk7Xb93YfEKggE1EQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这本书大家一定都很熟悉，小学入门第一课一定就是教小朋友们学习如何使用这本书。那这和我们的数据库索引有啥关系呢？别着急，我们翻开第一页看看。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzovMibFOicARkDrkgzvogDkm2XrNxrwpIIowzuJ3Jq3PxHL5ofd6RZscepUEjexTARrArSib6Bd2s04A/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

注意右上角的那一排文字，其实目录就是传说中的索引！从前面的“一句话描述”我们可以知道，索引的目的就是为了加快数据查询。那么我们查字典时翻的第一个地方是哪里呢，我相信大部分人都会先翻到拼音目录，毕竟现在很多人都是提笔忘字了😂。

**数据库索引的作用和拼音目录是一样**的，就是最快速的锁定目标数据所在的位置范围。比如我们在这里要查 `险` 这个字，那么我们找到了Xx部分之后就能按顺序找到xian这个拼音所在的页码，根据前后的页码我们可以知道这个字一定是**在519页到523页之间**的，范围一下子就**缩小**到只有4页了。这相比我们从头翻到尾可是快多了，这时候就出现了第一个专业术语——**全表扫描**，也就是我们说的从头找到尾了。

果然，我们在第521页找到了我们要找的“险”字。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzovMibFOicARkDrkgzvogDkm2ubXaewv7NAekUXnnrYbddzTW7m4aheP1zgwChjMrBftqtOGsXT1jsA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

那么现在我们就知道数据库索引大概是一个什么东西了：数据库索引是一个类似于目录这样的用来加快数据查询的技术。

------

##  什么是`联合索引`？

相信大家都见过一些**包含多个字段的数据库索引**，比如 `INDEX idx_test(col_a,col_b)`。这种包含多个字段的索引就被称为**“联合索引”**。那么在多个字段上建索引能起到什么样的作用呢？下面还是以新华字典为例，来看看到底什么是联合索引。

新华字典里还有一种目录被称为**“部首目录”**，下面可以看到，要使用这个目录我们首先会根据部首的笔画数找到对应该能的部分，然后可以在里面找到我们想找的部首。比如如果我们还是要找 `险`字所在的位置：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzovMibFOicARkDrkgzvogDkm2uazVCNXt6ktyrsPYZyqFtNLE5t86DDTqNcbgAGKhict6xC1aMHgKR4Q/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

找到部首后，右边的页码还不是 `险`字真正的页码，我们还需要根据右边的页码找到对应部首在检字表中的位置。找到第93页的检字表后我们就可以根据 `险`字**余下的笔画数**（7画）在“6-8画”这一部分里找到 `险`字真正的页码了。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzovMibFOicARkDrkgzvogDkm21FuyEgVCuTN4UeC9OJ4jNBvvnHC7gQehSsR1CP8ZbrdBBCABJY1eCw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

在这个过程中，我们按顺序使用了**“两个目录”**，一个叫做**“部首目录”**，一个叫做**“检字表”**。并且我们可以看到上图中检字表的内容都是按部首分门别类组织的。这两个部分合在一起就是我们在本节讨论的主题——联合索引。即通过第一个字段的值（部首）在**第一级索引**中找到对应的**第二级索引**位置（检字表页码），然后在第二级索引中根据第二个字段的值（笔画）找到符合条件的数据所在的位置（险字的真正页码）。

------

###  **最左前缀匹配** 

从前面使用部首目录的例子中可以看出，如果我们不知道一个字的部首是什么的话，那基本是没办法使用这个目录的。这说明仅仅通过笔画数（第二个字段）是没办法使用部首目录的。

这就引申出了**联合索引的一个规则**：联合索引中的字段，只有某个字段（笔画）左边的所有字段（部首）都被使用了，才能使用该字段上的索引。例如，有索引 `INDEX idx_i1(col_a,col_b)`，如果查询条件为 `wherecol_b=1`，则无法使用索引 `idx_i1`。

但是如果我们知道部首但是不知道笔画数，比如不知道“横折竖弯勾”是算一笔还是两笔，那我们仍然可以使用“部首目录”部分的内容，只是要把“检字表”对应部首里的所有字都看一遍就能找到我们要找的字了。

这就引申出了**联合索引的另一个规则**：联合索引中的字段，即使某个字段（部首）右边的其他字段（笔画）没有被使用，该字段之前（含）的所有字段仍然可以正常使用索引。例如，有索引 `INDEX idx_i2(col_a,col_b,col_c)`，则查询条件 `wherecol_a=1andcol_b=2`在字段 `col_a`和 `col_b`上仍然可以走索引。

但是，如果我们在确定部首后，不知道一个字到底是两画还是三画，这种情况下我们只需要在对应部首的两画和三画部分中找就可以了，也就是说我们仍然使用了检字表中的内容。所以，使用范围条件查询时也是可以使用索引的。

最后，我们可以完整地表述一下**最左前缀匹配原则的含义**：对于一个**联合索引**，如果有一个SQL查询语句需要执行，则只有从索引最左边的第一个字段开始到SQL语句查询条件中不包含的字段（不含）或范围条件字段（含）为止的部分才会使用索引进行加速。

------

##  **什么是聚集索引？** 

从上文的部首目录和拼音目录同时存在但是实际的字典内容只有一份这一点上可以看出，在数据库中**一张表上是可以有多个索引**的。那么不同的索引之间有什么区别呢？

我们在新华字典的侧面可以看到一个V字形的一个个黑色小方块，有很多人都会在侧面写上 `A`, `B`, `C`, `D`这样对应的拼音字母。因为字典中所有的字都是按照拼音顺序排列的，有时候直接使用首字母翻开对应的部分查也很快。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzovMibFOicARkDrkgzvogDkm2bNYKdmP5WUavbcmcHvcXmfRWTSAeHKF3x1J6e9AHmLTeLP2Ee62q5g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

像拼音目录这样的索引，数据会根据索引中的顺序进行排列和组织的，这样的索引就被称为**聚集索引**，而非聚集索引就是其他的一般索引。因为数据只能按照一种规则排序，所以`一张表至多有一个聚集索引`，但`可以有多个非聚集索引`。

在 MySQL数据库的 InnoDB存储引擎中，**主键索引就是聚集索引**，所有数据都会按照主键索引进行组织；而在MyISAM存储引擎中，就没有聚集索引了，因为MyISAM存储引擎中的数据不是按索引顺序进行存储的。

---

# 为什么MySQL索引要用B+ tree

## 前言

**当你在遇到了一条慢 `SQL` 需要进行优化时，你第一时间能想到的优化手段是什么？**

大部分人第一反应可能都是**添加索引**，在大多数情况下面，**索引**能够将一条 `SQL` 语句的查询效率提高几个**数量级**。

索引的**本质**：用于快速查找记录的一种**数据结构**。

索引的常用**数据结构**：

1. 二叉树
2. 红黑树
3. Hash 表
4. `B-tree` （B树，并不叫什么B减树😁）
5. `B+tree`

[**数据结构图形化**网址](https://www.cs.usfca.edu/~galles/visualization/Algorithms.html)

## 索引查询

大家知道 `select * from t where col = 88` 这么一条 `SQL` 语句如果不走索引进行查找的话，正常地查就是**全表扫描**：从表的第一行记录开始逐行找，把每一行的 `col` 字段的值和 **88** 进行对比，这明显效率是很低的。

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

而如果走索引的话，查询的流程就完全不一样了（假设现在用一棵**平衡二叉树**数据结构存储我们的索引列）

此时该二叉树的存储结构（Key - Value）：Key 就是索引字段的数据，Value 就是索引所在行的磁盘文件地址。

当最后找到了 **88** 的时候，就可以把它的 Value 对应的磁盘文件地址拿出来，然后就直接去磁盘上去找这一行的数据，这时候的速度就会比全表扫描要快很多。

![图片](https://mmbiz.qpic.cn/mmbiz_gif/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHQchwqnkBfHEOjuZ3kPTr1sDicgJj7icwyEImBYA4Vxv9RiaRAAibpgZ6wg/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

但**实际上** `MySQL` 底层并没有用**二叉树**来存储索引数据，是用的 **B+tree（B+树）**。

## 为什么不采用二叉树

假设此时用普通二叉树记录 `id` 索引列，我们在每插入一行记录的同时还要维护二叉树索引字段。

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

此时当我要找 `id = 7` 的那条数据时，它的查找过程如下：

![图片](https://mmbiz.qpic.cn/mmbiz_gif/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHoe2ftn0NWibs7USDzF4ekdaohic1zY5CqM5uUh4qKlHPJ48gAL8oP9LQ/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

此时找 `id = 7` 这一行记录时找了 **7** 次，和我们全表扫描也没什么很大区别。显而易见，二叉树对于这种**依次递增**的数据列其实是**不适合**作为索引的数据结构。

## 为什么不采用 Hash 表

> “
>
> Hash 表：一个快速搜索的数据结构，搜索的时间复杂度 O(1)
>
> Hash 函数：将一个任意类型的 key，可以转换成一个 int 类型的下标
>
> ”

假设此时用 Hash 表记录 `id` 索引列，我们在每插入一行记录的同时还要维护 Hash 表索引字段。

![图片](https://mmbiz.qpic.cn/mmbiz_gif/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHEnD8IKdwWt60a4jcDvKkAt395UaQRhlqKSoCmwfcgjW36nsXdfefRg/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

这时候开始查找 `id = 7` 的树节点仅找了 **1** 次，效率非常高了。

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

但 `MySQL` 的索引依然**不采用**能够精准定位的**Hash 表**。因为它**不适用**于**范围查询**。

疑问：是否可以同时采用Hash表和B+tree。范围查询的时候走B+tree呢？

## 为什么不采用红黑树

> “
>
> 红黑树是一种特化的 AVL树（平衡二叉树），都是在进行插入和删除操作时通过特定操作保持二叉查找树的平衡；
>
> 若一棵二叉查找树是红黑树，则它的任一子树必为红黑树。
>
> ”

假设此时用红黑树记录 `id` 索引列，我们在每插入一行记录的同时还要维护红黑树索引字段。

![图片](https://mmbiz.qpic.cn/mmbiz_gif/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHuOd6BPqdB0el4uw5IRRiaHf9tAfmHo5oGICheOLVhB1EQPTibCQicM4wg/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

插入过程中会发现它与普通二叉树不同的是当一棵树的左右子树高度差 > 1 时，它会进行**自旋**操作，保持树的平衡。

这时候开始查找 `id = 7` 的树节点只找了 **3** 次，比所谓的普通二叉树还是要更快的。

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

但 `MySQL` 的索引依然**不采用**能够精确定位和范围查询都优秀的**红黑树**。

因为当 `MySQL` 数据量很大的时候，索引的体积也会很大，可能内存放不下，所以需要从磁盘上进行相关读写，如果树的层级太高，则读写磁盘的次数（I/O交互）就会越多，性能就会越差。

## B-tree

> “
>
> 红黑树目前的唯一不足点就是树的高度不可控，所以现在我们的**切入点**就是**树的高度**。
>
> 目前一个节点是只分配了一个存储 1 个元素，如果要控制高度，我们就可以把一个节点分配的空间更大一点，让它**横向存储多个元素**，这个时候高度就可控了。这么个改造过程，就变成了 `B-tree`。
>
> ”

![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHLhqPpt7I99nyq3RRibDF1pF2PfMwenpd81sMHNZPId666txBnMazOHw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

`B-tree` 是一颗绝对平衡的多路树。它的结构中还有两个概念

> “
>
> 度（Degree）：一个节点拥有的子节点（子树）的数量。（有的地方是以**度**来说明 `B-tree` 的，这里解释一下）
>
> 阶（order）：一个节点的子节点的最大个数。（通常用 **m** 表示）
>
> 关键字：数据索引。
>
> ”

一棵 m 阶 `B-tree` 是一棵平衡的 m 路搜索树。它可能是空树，或者满足以下特点：

1. 除根节点和叶子节点外，其它每个节点至少有 [m/2] 个子节点；

    [m/2] 为 m / 2 然后向上取整

2. 每个非根节点所包含的关键字个数 j 满足：[m/2] - 1 ≤ j ≤ m - 1；

3. 节点的关键字从左到右递增排列，有 k 个关键字的非叶子节点正好有 (k + 1) 个子节点；

4. 所有的叶子结点都位于同一层。

### 名字取义（题外话，放松一下）

**以下摘自维基百科**

鲁道夫·拜尔（Rudolf Bayer）和 艾华·M·麦克雷（Ed M. McCreight）于1972年在波音研究实验室（Boeing Research Labs）工作时发明了 `B-tree`，但是他们没有解释 B 代表什么意义（如果有的话）。

道格拉斯·科默尔（Douglas Comer）解释说：两位作者从来都没解释过 `B-tree` 的原始意义。我们可能觉得 balanced, broad 或 bushy 可能适合。其他人建议字母 B 代表 Boeing。源自于他的赞助，不过，看起来把 `B-tree` 当作 Bayer 树更合适些。

高德纳（Donald Knuth）在他1980年5月发表的题为 "CS144C classroom lecture about disk storage and B-trees" 的论文中推测了 `B-tree` 的名字取义，提出 B 可能意味 Boeing 或者 Bayer 的名字。

### 查找

`B-tree` 的查找其实和二叉树很相似：

二叉树是每个节点上有一个关键字和两个分支，`B-tree` 上每个节点有 k 个关键字和 (k + 1) 个分支。

二叉树的查找只考虑向左还是向右走，而 `B-tree` 中需要由多个分支决定。

`B-tree` 的查找分两步：

1. 首先查找节点，由于 `B-tree` 通常是在磁盘上存储的所以这步需要进行**磁盘IO**操作；
2. 查找关键字，当找到某个节点后将该节点**读入内存**中然后通过顺序或者折半查找来查找关键字。若没有找到关键字，则需要判断大小来找到合适的分支继续查找。

#### 操作流程

现在需要查找元素：88

第一次：磁盘IO

![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHVU75fksYLx6icbicoj4DBFE07JqjxcpvQeHqzoZRHY9c6TrnA2bIcZJg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

第二次：磁盘IO

![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHnO4HJ5jTppQicJeZsPKUKDfaHchOpKrRjM7qslqCMRiaia0jOxcKs0qTg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

第三次：磁盘IO

然后这有一次内存比对，分别跟 70 与 88 比对，最后找到 88。

![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHDmVVjQLsUm2OE73KcW8DseFjLbVHUvWAhkf3HBNMwTHaSroQUCUlrA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

从查找过程中发现，`B-tree` 比对次数和磁盘IO的次数其实和二叉树相差不了多少，这么看来并没有什么优势。

但是仔细一看会发现，比对是在内存中完成中，不涉及到磁盘IO，耗时可以忽略不计。

另外 `B-tree` 中一个节点中可以存放很多的**关键字**（个数由阶决定），相同数量的**关键字**在 `B-tree` 中生成的节点要远远少于二叉树中的节点，相差的节点数量就等同于磁盘IO的次数。这样到达一定数量后，性能的差异就显现出来了。

### 插入

当 `B-tree` 要进行插入关键字时，都是直接找到叶子节点进行操作。

1. 根据要插入的**关键字**查找到待插入的叶子节点；

2. 因为一个节点的子节点的最大个数（阶）为 m，所以需要判断当前节点**关键字**的个数是否小于 (m - 1)。

3. - 是：直接插入
   - 否：发生**节点分裂**，以节点的中间的关键字将该节点分为左右两部分，中间的关键字放到父节点中即可。

#### 操作流程

比如我们现在需要在 Max Degree（阶）为 3 的 `B-tree`插入元素：72

1. 查找待插入的叶子节点

   ![图片](https://mmbiz.qpic.cn/mmbiz_gif/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHsHVC6nHn58SpVcyesr9KpQgzM1kWwwnhQ3fsTejGlhQcaKIVHqacAw/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

2. 节点分裂：本来应该和 [70,88] 在同一个磁盘块上，但是当一个节点有 3 个关键字的时候，它就有可能有 4 个子节点，就超过了我们所定义限制的最大度数 3，所以此时必须进行**分裂**：以中间关键字为界将节点一分为二，产生一个新节点，并把中间关键字上移到父节点中。

![图片](https://mmbiz.qpic.cn/mmbiz_gif/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHIhMuGiaDZ8aOnfbLvv77n7grBIQgDvoTmrZRhzakj7vR2gJzjNBKh9w/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

***Tip*** : 当中间关键字有两个时，通常将左关键字进行上移分裂。

### 删除

删除操作就会比查找和插入要麻烦一些，因为要被删除的关键字可能在叶子节点上，也可能不在，而且删除后还可能导致 `B-tree` 的不平衡，又要进行合并、旋转等操作去保持整棵树的平衡。

随便拿棵树（5 阶）举例子👇

![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHT3eTWVa4elTh0aAssVpBspnaMrKpL6p3sngdExibsTwmEVcMnZJG4rA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 情况一：直接删除叶子节点的元素

删除目标：50

1. 查找元素 50 位置

   ![图片](https://mmbiz.qpic.cn/mmbiz_gif/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHdjOvI4O1nZcddmLmekQfwCEVWQeLLZC0Yfa6LKjKX5tY5M0j44ficNw/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

2. 在 [36, 50, 63] 节点移除 50 后，依然符合 `B-tree` 对节点内关键字的要求：

   ```mathematica
   ┌m/2┐ - 1 ≤ 关键字个数 ≤ m - 1
   
   ┌5/2┐ - 1 ≤ 3 - 1 ≤ 5 - 1
   
   2 ≤ 2 ≤ 4 ✔
   ```

   ![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

   删除完成

#### 情况二：删除叶子节点的元素后合并+旋转

删除目标：11

1. 查找元素 11 位置

   ![图片](https://mmbiz.qpic.cn/mmbiz_gif/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHNfYA6iahQCRXicf4IweB9trkiciaXIjanKpRvxNsNbvjeCW3r5eL7WmUGQ/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

2. 在 [10, 11] 节点移除 11 后，违背 `B-tree` 对节点内关键字的要求：

   ```mathematica
   ┌m/2┐ - 1 ≤ 关键字个数 ≤ m - 1
   
   ┌5/2┐ - 1 ≤ 2 - 1 ≤ 5 - 1
   
   2 ≤ 1 ≤ 4 ❌
   ```

3. 在它只剩1个关键字后，需要向兄弟节点借元素，这时候右兄弟有多的，它说：我愿意把14借给你😁

   但不可能让11和14放一起，因为 `14 > 12` ，这时候就要进行**旋转**~

   **首先，将父节点的元素 12 移到该节点，然后 12 就让位给14**

   ![图片](https://mmbiz.qpic.cn/mmbiz_gif/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHKV9psBhTy0Kibl0TPRabE2Uib1YEn9BlA2MEFAiczPLuLTUZzhB4J0anQ/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

   **这整个过程就是删除叶子节点元素后的合并、旋转操作**

   下面再来道菜🍽

接着删除 10

1. 在 [10, 12] 节点移除 10 后，违背 `B-tree` 对节点内关键字的要求

2. 在它只剩1个关键字后，需要向兄弟节点借元素，这时候没有兄弟有多的该怎么办呢🤔

   **首先，将父节点的元素 8 移到该节点，这时候 3、6、8、12 都小于14，就先把它们放一起**

   ![图片](https://mmbiz.qpic.cn/mmbiz_gif/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHqaDOE8lXt2PiclITjk2sCLRV9YV7n0K58OCMic7oaAS53D7rU9wJiabMA/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

   结果又发现父节点只剩个14了，它又违背了 `B-tree` 对节点内关键字的要求，接着造！！！

   **首先，还是将父节点的元素 20 移到该节点，这时候根节点都直接没了，直接合并 14、20、26、72 关键字**

   ![图片](https://mmbiz.qpic.cn/mmbiz_gif/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHmPQx9HibolJX3dibeg2Z2vIx6WKO5dDyxSjrAqfPYOczyQTdjic9VwtGQ/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

   **在这整个过程包括删除叶子节点和非叶子节点的合并、旋转操作**

#### 情况三：删除非叶子节点的元素后合并+旋转

删除目标：12

1. 查找元素 12 位置

   ![图片](https://mmbiz.qpic.cn/mmbiz_gif/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHzw4mUfZibGCWkiad6JrHwkvS0D6hr2n3tJGpsfpntWLdfzoFWq6uNV9A/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

2. 移除 12 后，违背 `B-tree` 对节点内关键字的要求

   **对于非叶子节点元素的删除，我们需要用后继元素覆盖要被删除的元素，然后在后继元素所在的叶子中删除该后继元素。**

   ![图片](https://mmbiz.qpic.cn/mmbiz_gif/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHogVuibk3z0vBfBtKibwa5pqiaWEtPQRjkFGZjLCURkibicPsXw0GhrhUnKg/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

### 小总结

> “
>
> 1. B-tree 主要用于文件系统以及部分数据库索引，例如：MongoDB。
>
> 2. 从查找效率考虑一般要求 B-tree 的阶数 m ≥ 3
>
> 3. B-tree 上算法的执行时间主要由读、写磁盘的次数来决定，故一次I/O操作应读写尽可能多的信息。
>
>    因此 B-tree 的节点规模一般以一个磁盘页为单位。一个结点包含的关键字及其孩子个数取决于磁盘页的大小。
>
> ”

## B+tree

上面这些例子相信大家对 `B-tree` 已经有一定了解了，而 `MySQL` 底层用的索引数据结构正是在 `B-tree` 上面做了一些改造，变成了 `B+tree`。

`B+tree` 和 `B-tree` 区别：

1. 所有的子节点，一定会出现在叶子节点上

2. 相邻的叶子节点之间，会用一个**双向链表**连接起来（关键）

3. 非叶子节点只存储索引，不存储数据，就为放更多索引

4. - 相比 `B-tree` 来说，进行范围查找时只需要查找两个节点，进行遍历就行。而 `B-tree` 需要获取所有节点，相比之下 `B+tree` 效率更高。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHdU3Mfbo2vCeG7vKhgdVeThUCibOoibYD0r4EbvYAm9JDGMOiaeNKn4p0A/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这里其实这个**数据结构可视化网页**画的 `B+tree` 还是不够清晰，只是画了个大概，下面我们就来看看它底层实际具体的数据结构👇👇👇

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

每个节点都被称作一个**磁盘页**

![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHIIoUvGmRSy9PFzWibasxqc8fvrxdsZgtp1sZQhQLkL9OtKJGlAiaxxhA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**B+tree 的叶子节点包含所有索引数据，在非叶子节点会存储不存储数据，只存储索引，从而来组成一颗 B+tree。**

### 查找

`B+tree` 最大的优势就在查找上，主要是范围查询更加明显。

> “
>
> 1. B-tree 节点中的每个关键字都有数据，而 B+tree 中间节点没有数据，只有索引；这就意味着相同大小的磁盘页可以放更多的节点元素，也就是在相同的数据量下，I/O 操作更少
>
> 2. 在范围查询上，B-tree 需要先找到指定范围内的下限，再找到上限，有了这两个过程后再取出它们之间的元素。
>
>    B+tree 因为叶子节点通过双向链表进行连接，找到指定范围内的下限后，直接通过链表顺序遍历就行，这样就方便很多了。
>
> ”

在查询单个关键字上，和 `B-tree` 差不多：先把通过磁盘 I/O 找到节点，再把节点加载到内存中进行内部关键字比对，然后通过大小关系再决定接下来走哪个分支。

但是差别就在于 `B+tree` 的高度更加可控一些。`MySQL` 默认给一个磁盘页数据分配的大小是 **16KB**，也就是 16 × 1024 = 16384 字节

官网说明：https://dev.mysql.com/doc/refman/5.7/en/innodb-physical-structure.html

证明：直接在数据库中通过 `SQL` 语句 `show GLOBAL STATUS LIKE 'INNODB_page_size'`进行验证

![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHb1U1jFoeiajkDTTTUHictuebBpxk2YcgXsFoUczibWcTcVZvdSQG9Nt9g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

当我们的叶子节点全部撑满之后，可以来算一算它树的高度。

我们拿阿里的《Java 开发手册》嵩山版中对表主键的要求进行举例

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

`bigint` 大概占 **8Byte**，索引旁边放指向下一节点的磁盘文件地址那块是**6Byte**，是 `MySQL` 底层写死了的。

![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHQt3UvkaBVyq3byibg7d6bQzcoiclJja7UOJTd2rb4TkiaKQ7iaiaBlXia0dw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

通过计算：16384 Byte / (8+6) Byte ≈ 1170，也就是说一个节点设置 **16KB** 大小的话可以放 1170个索引。

![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icH7oP4sw4wvCticb55hbB2BMsS2q4XQjNl1Ugz3WJnKLYVQOPtfOevDsA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

叶子节点一个关键字占用**1KB**时，那一个节点就可以放16个元素，当整棵树叶子节点全部都被撑满时，通过计算 `1170 × 1170 × 16 = 21902400`

最后结果为**2千多万**，树的高度才为3，也就是我们要的**高度可控**。这也就是为什么 `MySQL` 的表有上千万数据的情况下，查询效率依然快的原因。

### 插入

插入还是挺简单的：**当节点内元素数量大于 (m-1) 时，按中间元素分裂成左右两部分，中间元素分裂到父节点当做索引存储，本身中间元素也还会分裂右边这一部分的。**

下面以5阶(m)举🌰

#### 操作流程

1. 第一次在空树中插入 1

   ![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icH1ibGbjLVibtaic5VGSlnxkhExHQDJPfqJrNVeAhESA9KLenBgia6XSCibXA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

   再依次插入 2,3,4

![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHsccAUgXTA5kibXJJVQXBrqUU4T3QJ1XEbsnhJw5Gxxnu9PTv2Mvlk0w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

1. 插入 5

   当插入关键字 5 时，此时节点内元素数量大于 (5-1) ，即超过了4个，这时候就要进行分裂；

   以中间元素分裂，中间元素分裂到父节点当做索引存储，由于叶子节点包含所有索引数据，所以本身它还会分裂至右边部分。

   ![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHia2kXAvJ0luoQZa2ODppHVsWIyylHR0kc4g1Oh6V5p7y6IpjY4X4Fhg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

   **这个过程是在叶子节点上进行分裂操作**

   **下面再来个插入后的非叶子节点分裂操作**（大差不差）

   在以下的基础上插入关键字：13

   ![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

2. 关键字13 插入到 [9, 10, 11, 12, 13] 后节点内元素数量超过了4个，准备进行分裂；

   以中间元素(11)分裂，中间元素分裂到父节点当做索引存储，本身它也还会分裂右边部分。

   ![图片](https://mmbiz.qpic.cn/mmbiz_gif/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHxB3NsOAMQjQzqtA76hYVNba6IlzTY0lFcPxe1lGhqunFbG8ICC58TQ/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

3. 关键字11 被挪到父节点去之后，节点内元素数量超过了4个，又要准备进行分裂

   以中间元素(7)分裂，中间元素分裂到父节点当做（冗余）索引存储。

   ![图片](https://mmbiz.qpic.cn/mmbiz_gif/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHPxQGXQcDvv62Y80LgiawEVhnCtA6kiaXzaiaBUpJicAgnVzXlRxjHrJKcA/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

   插入完毕

### 删除

在对应节点删除目标关键字后，一样需要看看节点内剩余关键字是否符合：┌m/2┐ - 1 ≤ 关键字个数 ≤ m - 1

符合直接删除就行，不符合就和 `B-tree` 一样需要向兄弟节点**借元素**，不过会比 `B-tree` 稍简单一点点

因为**叶子节点（双向链表）之间有指针关联着，可以不需要再找它们的父节点了，直接通过兄弟节点进行移动，然后再更新父节点；**

**如果兄弟节点内元素没有多余的关键字，那就直接将当前节点和兄弟节点合并，再删除父节点中的关键字。**

#### 操作流程

![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHicyD41Y0pkiagLMksWW4n5bp2tC359j6YQ0edjcn2ORwCjicVzqZgU8IA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

目标删除元素：14

1. 删除 14 关键字后，它所在的节点只剩 13 一个关键字了

   ```mathematica
   ┌m/2┐ - 1 ≤ 关键字个数 ≤ m - 1
   
   ┌5/2┐ - 1 ≤ 2 - 1 ≤ 5 - 1
   
   2 ≤ 1 ≤ 4 ❌
   ```

   准备借元素！

2. **直接通过右兄弟节点（只有它有富余）进行移动，然后再更新父节点的索引**

   ![图片](https://mmbiz.qpic.cn/mmbiz_gif/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHicez6RYX8GFdQrkxawXkWkh8km7soYUmAibDMzm0rwGBU7FUKXnxiaPRw/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

   删除成功后

   ![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icH4ESHWzCBylvAtYIpWZhhWa2d6zNXxKCo79tJtkZiaV27kfpakQSzOBg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

接着删除元素：16

1. 删除 16 关键字后，它所在的节点只剩 17 一个关键字了，又要准备借元素；

2. 这时候兄弟节点都没有多的，就直接把它和兄弟节点合并，再删除父节点中的关键字

   合并关键字 [13, 15, 17] ，在删除父节点中的关键字 16

   ![图片](https://mmbiz.qpic.cn/mmbiz_gif/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHrvQU6gb4CvD543s9toXkvPKboP8ESc1MsgtZtBP4bJv2pKzThMJRsw/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

   删除成功后

   ![图片](https://mmbiz.qpic.cn/mmbiz_png/wRTybk8SK4YOR6EAOF3JkzadWkr3A1icHpKDFe4hA11gnngD1VzTWP3uSkeftMdOezqslzLxc3UMGyV3VzgxZkA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

### 总结

> “
>
> 1. 单个节点存储越多的元素，自然在整个过程中的磁盘💽I/O交互就越少；
> 2. 相对 B-tree 来说，所有的查询最终都会找到叶子节点，这也是 B+tree 性能稳定的一个体现；
> 3. 所有叶子节点通过双向链表相连，范围查询非常方便，这也是 B+tree 最明显的优势。
>
> ”

## 参考资料

- http://m.elecfans.com/article/662237.html
- https://blog.csdn.net/z_ryan/article/details/79685072



---

# 为什么用了索引，SQL查询还是慢

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/xq9PqibkVAzq7YhicveSBAvmIs4pDWZpN0aUdSKicgkhYAFQCOmfic5XBJiazovk7V6q5BBHhSZxv87BVCKcoYibXUVw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**经常有同学疑问**，为什么有时候一个SQL语句使用了索引，为什么还是会进入到慢查询之中呢？今天我们就从这个问题开始来**聊一聊`索引`和`慢查询`**。



另外插入一个题外话，个人认为团队要合理的使用ORM。合理利用的是ORM在面向对象和写操作方面的优势，避免联合查询上可能产生的坑(当然如果你的Linq查询能力很强另当别论)，因为ORM屏蔽了太多的DB底层的知识内容，对程序员不是件好事，对性能有极致追求，但是ORM理解不透彻的团队更加要谨慎。

## **案例剖析**　

言归正传，为了实验，我创建了如下表：

```mysql
CREATE TABLE `T`(
`id` int(11) NOT NULL,
`a` int(11) DEFAUT NULL,
PRIMARY KEY(`id`),
KEY `a`(`a`)
) ENGINE=InnoDB;
```

该表有三个字段，其中用id是主键索引，a是普通索引。

首先SQL判断一个语句是不是慢查询语句，用的是语句的执行时间。他把语句执行时间跟long_query_time这个系统参数作比较，如果语句执行时间比它还大，就会把这个语句记录到慢查询日志里面，这个参数的默认值是10秒。当然在生产上，我们不会设置这么大，一般会设置1秒，对于一些比较敏感的业务，可能会设置一个比1秒还小的值。

语句执行过程中有没有用到表的索引，可以通过explain一个语句的输出结果来看KEY的值不是NULL。

我们看下 `explain select * from t;`的KEY结果是NULL

![图片](https://mmbiz.qpic.cn/mmbiz_png/JfTPiahTHJhorqG6NpyPDibIJxHQlMYuWDXa21FgohINC0yml7zVwhDko97grmGetgrXibHgtdoIZ67clG6nooV3w/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

　　（图一）

`explain select * from t where id=2;`的KEY结果是PRIMARY，就是我们常说的使用了主键索引

![图片](https://mmbiz.qpic.cn/mmbiz_png/JfTPiahTHJhorqG6NpyPDibIJxHQlMYuWD01D1jg4LliaCaU3ZBxzQfokMmflEC0MZgo9KfLYbzrpr8v63UrbQ3Hw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

　（图二）

`explain select a from t;`的KEY结果是a，表示使用了a这个索引。

![图片](https://mmbiz.qpic.cn/mmbiz_png/JfTPiahTHJhorqG6NpyPDibIJxHQlMYuWD0CeJdmTx6ShScjibKVRoc5kdmRIiaBtP1BgKgAP5hMo2Wh3poZGJIXgg/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

　（图三）

虽然后两个查询的KEY都不是NULL，但是最后一个实际上扫描了整个索引树a。

假设这个表的数据量有100万行，图二的语句还是可以执行很快，但是图三就肯定很慢了。如果是更极端的情况，比如，这个数据库上CPU压力非常的高，那么可能第2个语句的执行时间也会超过long_query_time，会进入到慢查询日志里面。

所以我们可以得出一个结论：**是否使用索引和是否进入慢查询之间并没有必然的联系。使用索引只是表示了一个SQL语句的执行过程，而是否进入到慢查询是由它的执行时间决定的，而这个执行时间，可能会受各种外部因素的影响。换句话来说，使用了索引你的语句可能依然会很慢。**

## **全索引扫描的不足**

那如果我们在更深层次的看这个问题，其实他还潜藏了一个问题需要澄清，就是什么叫做使用了索引。

我们都知道，InnoDB是索引组织表，所有的数据都是存储在索引树上面的。比如上面的表t，这个表包含了两个索引，一个主键索引和一个普通索引。在InnoDB里，数据是放在主键索引里的。如图所示：

![图片](https://mmbiz.qpic.cn/mmbiz_png/JfTPiahTHJhorqG6NpyPDibIJxHQlMYuWDRJs46763n8tMEjbxfLk3uCnqO5qW0EOEhprVjd66p2XqA5zJ8Q9Zxg/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

可以看到数据都放在主键索引上，如果从逻辑上说，所有的InnoDB表上的查询，都至少用了一个索引，所以现在我问你一个问题，如果你执行`select from t where id>0`，你觉得这个语句有用上索引吗？

![图片](https://mmbiz.qpic.cn/mmbiz_png/JfTPiahTHJhorqG6NpyPDibIJxHQlMYuWDkicwYqb2J8BMu4IicM5IFk9sAgZg7dzzWM33aYicOxTbBpwicDu6RIxl0Q/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

我们看上面这个语句的explain的输出结果显示的是PRIMARY。其实从数据上你是知道的，这个语句一定是做了全面扫描。但是优化器认为，这个语句的执行过程中，需要根据主键索引，定位到第1个满足ID>0的值，也算用到了索引。

所以即使explain的结果里写的KEY不是NULL，实际上也可能是全表扫描的，因此InnoDB里面只有一种情况叫做没有使用索引，那就是从主键索引的最左边的叶节点开始，向右扫描整个索引树。

也就是说，没有使用索引并不是一个准确的描述。

你可以用全表扫描来表示一个查询遍历了整个主键索引树；



也可以用全索引扫描，来说明像select a from t;这样的查询，他扫描了整个普通索引树；



而select * from t where id=2这样的语句，才是我们平时说的使用了索引。他表示的意思是，我们使用了索引的快速搜索功能，并且有效的减少了扫描行数。

## **索引的过滤性要足够好**

根据以上解剖，我们知道全索引扫描会让查询变慢，接下来就要来谈谈索引的过滤性。

假设你现在维护了一个表，这个表记录了中国14亿人的基本信息，现在要查出所有年龄在10~15岁之间的姓名和基本信息，那么你的语句会这么写，`select * from t_people where age between 10 and 15`。

你一看这个语句一定要在age字段上开始建立索引了，否则就是个全面扫描，但是你会发现，在你建立索引以后，这个语句还是执行慢，因为满足这个条件的数据可能有超过1亿行。

我们来看看建立索引以后，这个表的组织结构图：

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

这个语句的执行流程是这样的:

从索引上用树搜索，取到第1个age等于10的记录，得到它的主键id的值，根据id的值去主键索引取整行的信息，作为结果集的一部分返回；



在索引age上向右扫描，取下一个id的值，到主键索引上取整行信息，作为结果集的一部分返回；



重复上面的步骤，直到碰到第1个age大于15的记录；

你看这个语句，虽然他用了索引，但是他扫描超过了1亿行。所以你现在知道了，当我们在讨论有没有使用索引的时候，其实我们关心的是扫描行数。

**对于一个大表，不止要有索引，`索引的过滤性`还要足够好。**

像刚才这个例子的age，它的过滤性就不够好，在设计表结构的时候，我们要让所有的过滤性足够好，也就是区分度足够高。

## **回表的代价**

那么过滤性好了，是不是表示查询的扫描行数就一定少呢？

**我们再来看一个例子：**

如果你的执行语句是 `select * from t_people where name='张三' and age=8`

t_people表上有一个索引是姓名和年龄的联合索引，那这个联合索引的过滤性应该不错，可以在联合索引上快速找到第1个姓名是张三，并且年龄是8的小朋友，当然这样的小朋友应该不多，因此向右扫描的行数很少，查询效率就很高。

但是查询的过滤性和索引的过滤性可不一定是一样的，如果现在你的需求是查出所有名字的第1个字是张，并且年龄是8岁的所有小朋友，你的语句会怎么写呢？

你的语句要怎么写？很显然你会这么写：`select * from t_people where name like '张%' and age=8;`

在MySQL5.5和之前的版本中，这个语句的执行流程是这样的:
![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

首先从联合索引上找到第1个年龄字段是张开头的记录，取出主键id，然后到主键索引树上，根据id取出整行的值；



判断年龄字段是否等于8，如果是就作为结果集的一行返回，如果不是就丢弃。



在联合索引上向右遍历，并重复做回表和判断的逻辑，直到碰到联合索引树上名字的第1个字不是张的记录为止。

我们把根据id到主键索引上查找整行数据这个动作，称为回表。你可以看到这个执行过程里面，最耗费时间的步骤就是回表，假设全国名字第1个字是张的人有8000万，那么这个过程就要回表8000万次，在定位第一行记录的时候，只能使用索引和联合索引的最左前缀，最称为最左前缀原则。

你可以看到这个执行过程，它的回表次数特别多，性能不够好，有没有优化的方法呢？

在MySQL5.6版本，引入了index condition pushdown的优化。我们来看看这个优化的执行流程：

![图片](https://mmbiz.qpic.cn/mmbiz_png/JfTPiahTHJhorqG6NpyPDibIJxHQlMYuWD0BugyAiaxGSSg95PokVBmkRsJz51iaC6VFfVITZqicRfpKLKAEicyCk6cw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

首先从联合索引树上，找到第1个年龄字段是张开头的记录，判断这个索引记录里面，年龄的值是不是8，如果是就回表，取出整行数据，作为结果集的一部分返回，如果不是就丢弃；



在联合索引树上，向右遍历，并判断年龄字段后，根据需要做回表，直到碰到联合索引树上名字的第1个字不是张的记录为止；

这个过程跟上面的差别，是在遍历联合索引的过程中，将年龄等于8的条件下推到所有遍历的过程中，减少了回表的次数，假设全国名字第1个字是张的人里面，有100万个是8岁的小朋友，那么这个查询过程中在联合索引里要遍历8000万次，而回表只需要100万次。

## **虚拟列**

可以看到这个优化的效果还是很不错的，但是这个优化还是没有绕开最左前缀原则的限制，因此在联合索引你还是要扫描8000万行，那有没有更进一步的优化方法呢？

我们可以考虑把名字的第一个字和age来做一个联合索引。这里可以使用MySQL5.7引入的虚拟列来实现。对应的修改表结构的SQL语句:

```sql
alter table t_people add name_first varchar(2) generated (left(name,1)),add index(name_first,age);
```

我们来看这个SQL语句的执行效果:

```sql
CREATE TABLE `t_people`(
`id` int(11) DEFAULT NULL,
`name` varchar(20) DEFAUT NULL,
`name_first` varchar(2) GENERATED ALWAYS AS (left(`name`,1)) VIRTUAL,KEY `name_first`(`name_first`,'age')
) ENGINE=InnoDB DEFAULT CHARSET=utf8; 
```

首先他在people上创建一个字段叫name_first的虚拟列，然后给name_first和age上创建一个联合索引，并且，让这个虚拟列的值总是等于name字段的前两个字节，虚拟列在插入数据的时候不能指定值，在更新的时候也不能主动修改，它的值会根据定义自动生成，在name字段修改的时候也会自动修改。

有了这个新的联合索引，我们在找名字的第1个字是张，并且年龄为8的小朋友的时候，这个SQL语句就可以这么写：select * from t_people where name_first='张' and age=8。

这样这个语句的执行过程，就只需要扫描联合索引的100万行，并回表100万次，这个优化的本质是我们创建了一个更紧凑的索引，来加速了查询的过程。

## **总结**

本文给你介绍了索引的基本结构和一些查询优化的基本思路，你现在知道了，使用索引的语句也有可能是慢查询，我们的查询优化的过程，往往就是减少扫描行数的过程。

慢查询归纳起来大概有这么几种情况：

- 全表扫描
- 全索引扫描
- 索引过滤性不好
- 频繁回表的开销

------

# 数据库索引的原理和使用准则

## 前 言

生产上为了高效地查询数据库中的数据，我们常常会给表中的字段**添加索引**，大家是否有考虑过**如何添加索引**才能使索引更高效，考虑如下问题

- 添加的索引是越多越好吗
- 为啥有时候明明添加了索引却不生效
- 索引有哪些类型
- 如何评判一个索引设计的好坏

看了本文相信你会对索引的原理有更清晰的认识。本文将会从以下几个方面来讲述索引的相关知识，相信大家耐心看了之后肯定有收获。

- 什么是索引，索引的作用
- 索引的种类
- 高性能索引策略
- 索引设计准则：三星索引

## 什么是索引，索引的作用

当我们要在新华字典里查某个字（如「先」）具体含义的时候，通常都会拿起一本新华字典来查，你可以先从头到尾查询每一页是否有「先」这个字，这样做（对应数据库中的全表扫描）确实能找到，但效率无疑是非常低下的，更高效的方相信大家也都知道，就是在首页的索引里先查找「先」对应的页数，然后直接跳到相应的页面查找，这样查询时候大大减少了，可以为是 O(1)。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/OyweysCSeLVeqReRUs3d1PtxtB9ibNZc053QsqckPrOxzj2kWccVGXcoibkicBvvrWO1SBCicia12LibDhKZHrd1mCRQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

数据库中的索引也是类似的，通过索引定位到要读取的页，大大减少了需要扫描的行数，能极大的提升效率，简而言之，索引主要有以下几个作用

1. 即上述所说，索引能极大地减少扫描行数
2. 索引可以帮助服务器避免排序和临时表
3. 索引可以将随机 IO 变成顺序 IO

第一点上文已经解释了，我们来看下第二点和第三点

先来看第二点，假设我们不用索引，试想运行如下语句

```sql
SELECT * FROM user order by age desc;
```

则 MySQL 的流程是这样的，扫描所有行，把所有行加载到内存后，再按 age 排序生成一张临时表，再把这表排序后将相应行返回给客户端，更糟的，如果这张临时表的大小大于 tmp_table_size 的值（默认为 16 M），内存临时表会转为磁盘临时表，性能会更差，如果加了索引，索引本身是有序的 ，所以从磁盘读的行数本身就是按 age 排序好的，也就不会生成临时表，就不用再额外排序 ，无疑提升了性能。

再来看随机 IO 和顺序 IO。先来解释下这两个概念。

相信不少人应该吃过旋转火锅，服务员把一盘盘的菜放在旋转传输带上，然后等到这些菜转到我们面前，我们就可以拿到菜了，假设装一圈需要 4 分钟，则最短等待时间是 0（即菜就在你跟前），最长等待时间是 4 分钟（菜刚好在你跟前错过），那么平均等待时间即为 2 分钟，假设我们现在要拿四盘菜，这四盘菜**随机分配**在传输带上，则可知拿到这四盘菜的平均等待时间是 8 分钟（随机 IO），如果这四盘菜刚好紧邻着排在一起，则等待时间只需 2 分钟（顺序 IO）。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/OyweysCSeLVeqReRUs3d1PtxtB9ibNZc0yYI1ibAoeJo8WL0Ywm2Xe3IUqgV0amb6IeoFJulibXJ5q7U8jukHibNvQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

上述中传输带就类比磁道，磁道上的菜就类比扇区（sector）中的信息，磁盘块（block）是由多个相邻的扇区组成的，是操作系统读取的最小单元，这样如果信息能以 block 的形式聚集在一起，就能极大减少磁盘 IO 时间,这就是顺序 IO 带来的性能提升，下文中我们将会看到 B+ 树索引就起到这样的作用。

![图片](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLVeqReRUs3d1PtxtB9ibNZc0ydwFBgDjkEA7YUer3roicuaY3qz5q32PUN4eyaq1YZibZzxc99t9TRXw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

*如图示：多个扇区组成了一个 block，如果要读的信息都在这个 block 中，则只需一次 IO 读*

而如果信息在一个磁道中分散地分布在各个扇区中，或者分布在不同磁道的扇区上（寻道时间是随机IO主要瓶颈所在），将会造成随机 IO，影响性能。

我们来看一下一个随机 IO 的时间分布：

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

1. seek Time: 寻道时间，磁头移动到扇区所在的磁道
2. Rotational Latency：完成步骤 1 后，磁头移动到同一磁道扇区对应的位置所需求时间
3. Transfer Time 从磁盘读取信息传入内存时间

这其中寻道时间占据了绝大多数的时间（大概占据随机 IO 时间的占 40%）。

随机 IO 和顺序 IO 大概相差百倍 (随机 IO：10 ms/ page, 顺序  IO 0.1ms / page)，可见顺序 IO 性能之高，索引带来的性能提升显而易见！

## 索引的种类

索引主要分为以下几类

- B+树索引
- 哈希索引

### B+树索引

B+ 树索引之前在[此文](https://mp.weixin.qq.com/s?__biz=MzI5MTU1MzM3MQ==&mid=2247484006&idx=1&sn=3e15abeb5299a3e9b578332dd8565273&scene=21#wechat_redirect)中详细阐述过，强烈建议大家看一遍，对理解 B+ 树有很大的帮助，简单回顾一下吧

![图片](https://mmbiz.qpic.cn/mmbiz_png/OyweysCSeLVeqReRUs3d1PtxtB9ibNZc0FXGrE3BQcwLKNC7Fuqkr9iayyLrhkRB785cWdhckcCTbeEDh8GK4ib9w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

B+ 树是以 N 叉树的形式存在的，这样有效降低了树的高度，查找数据也不需要全表扫描了，顺着根节点层层往下查找能很快地找到我们的目标数据，每个节点的大小即一个磁盘块（页）的大小，一次 IO 会将一个磁盘块的数据都读入（即磁盘预读，程序局部性原理:读到了某个值，很大可能这个值周围的数据也会被用到，干脆一起读入内存），叶子节点通过指针的相互指向连接，能有效减少顺序遍历时的随机 IO，而且我们也可以看到，叶子节点都是按索引的顺序排序好的，这也意味着根据索引查找或排序都是排序好了的，不会再在内存中形成临时表。

### 哈希索引

哈希索引基本散列表实现，散列表（也称哈希表）是根据关键码值(Key value)而直接进行访问的数据结构，它让码值经过哈希函数的转换映射到散列表对应的位置上，查找效率非常高。假设我们对名字建立了哈希索引，则查找过程如下图所示：

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

对于每一行数据，存储引擎都会对所有的索引列（上图中的 name 列）计算一个哈希码（上图散列表的位置），散列表里的每个元素指向数据行的指针，由于索引自身只存储对应的哈希值，所以索引的结构十分紧凑，这让哈希索引查找速度非常快！

当然了哈希表的劣势也是比较明显的，不支持区间查找，不支持排序，所以更多的时候哈希表是与 B Tree等一起使用的，在 InnoDB引擎中就有一种名为`「自适应哈希索引」`的特殊索引，当 InnoDB 注意到某些索引值使用非常频繁时，就会内存中基于 B-Tree 索引之上再创建哈希索引，这样也就让 B+ 树索引也有了哈希索引的快速查找等优点，这是完全自动，内部的行为，用户无法控制或配置，不过如果有必要，可以关闭该功能。

innoDB 引擎本身是不支持显式创建哈希索引的，我们可以在 B+ 树的基础上创建一个伪哈希索引，它与真正的哈希索引不是一回事，它是以哈希值而非键本身来进行索引查找的，这种伪哈希索引的使用场景是怎样的呢，假设我们在 db 某张表中有个 url 字段，我们知道每个 url 的长度都很长，如果以 url 这个字段创建索引，无疑要占用很大的存储空间，如果能通过哈希（比如CRC32）把此 url 映射成 4 个字节，再以此哈希值作索引 ，索引占用无疑大大缩短！不过在查询的时候要记得同时带上 url 和 url_crc,主要是为了避免哈希冲突，导致 url_crc 的值可能一样

```sql
SELECT id FROM url WHERE url = "http://www.baidu.com"  AND url_crc = CRC32("http://www.baidu.com")
```

这样做把基于 url 的字符串索引改成了基于 url_crc 的整型索引，效率更高，同时索引占用的空间也大大减少，一举两得，当然人可能会说需要手动维护索引太麻烦了，那可以改进`触发器实现`。

除了上文说的两个索引 ，还有空间索引（R-Tree），全文索引等，由生产中不是很常用，这里不作过多阐述

## 高性能索引策略

不同的索引设计选择能对性能产生很大的影响，有人可能会发现生产中明明加了索引却不生效，有时候加了虽然生效但对搜索性能并没有提升多少，对于多列联合索引，哪列在前，哪列在后也是有讲究的，我们一起来看看

### 加了索引，为何却不生效

加了索引却不生效可能会有以下几种原因

1、索引列是表示式的一部分，或是函数的一部分

如下 SQL：

```sql
SELECT book_id FROM BOOK WHERE book_id + 1 = 5;
```

或者

```sql
SELECT book_id FROM BOOK WHERE TO_DAYS(CURRENT_DATE) - TO_DAYS(gmt_create) <= 10
```

上述两个 SQL 虽然在列 book_id 和 gmt_create 设置了索引 ，但由于它们是表达式或函数的一部分，导致索引无法生效，最终导致全表扫描。

2、隐式类型转换

以上两种情况相信不少人都知道索引不能生效，但下面这种隐式类型转换估计会让不少人栽跟头，来看下下面这个例子:

假设有以下表:

```sql
CREATE TABLE `tradelog` (
  `id` int(11) NOT NULL,
  `tradeid` varchar(32) DEFAULT NULL,
  `operator` int(11) DEFAULT NULL,
  `t_modified` datetime DEFAULT NULL,
   PRIMARY KEY (`id`),
   KEY `tradeid` (`tradeid`),
   KEY `t_modified` (`t_modified`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

执行 SQL 语句

```sql
SELECT * FROM tradelog WHERE tradeid=110717;
```

交易编号 tradeid 上有索引，但用 EXPLAIN 执行却发现使用了全表扫描，为啥呢，tradeId 的类型是 varchar(32), 而此 SQL 用 tradeid 一个数字类型进行比较，发生了隐形转换，会隐式地将字符串转成整型，如下:

```sql
mysql> SELECT * FROM tradelog WHERE CAST(tradid AS signed int) = 110717;
```

这样也就触发了上文中第一条的规则 ，即：索引列不能是函数的一部分。

3、隐式编码转换

这种情况非常隐蔽，来看下这个例子

```sql
CREATE TABLE `trade_detail` ( 
 `id` int(11) NOT NULL, 
 `tradeid` varchar(32) DEFAULT NULL, 
 `trade_step` int(11) DEFAULT NULL, /*操作步骤*/ 
 `step_info` varchar(32) DEFAULT NULL, /*步骤信息*/ 
   PRIMARY KEY (`id`), KEY `tradeid` (`tradeid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

trade_defail 是交易详情， tradelog 是操作此交易详情的记录，现在要查询 id=2 的交易的所有操作步骤信息，则我们会采用如下方式

```sql
SELECT d.* FROM tradelog l, trade_detail d WHERE d.tradeid=l.tradeid AND l.id=2;
```

由于 tradelog 与 trade_detail 这两个表的字符集不同，且 tradelog 的字符集是 utf8mb4，而 trade_detail 字符集是 utf8, utf8mb4 是 utf8 的超集，所以会自动将 utf8 转成 utf8mb4。即上述语句会发生如下转换:

```sql
SELECT d.* FROM tradelog l, trade_detail d WHERE (CONVERT(d.traideid USING utf8mb4)))=l.tradeid AND l.id=2;
```

自然也就触发了 「索引列不能是函数的一部分」这条规则。怎么解决呢，第一种方案当然是把两个表的字符集改成一样，如果业务量比较大，生产上不方便改的话，还有一种方案是把 utf8mb4 转成 utf8，如下

```sql
mysql> SELECT d.* FROM tradelog l , trade_detail d WHERE d.tradeid=CONVERT(l.tradeid USING utf8) AND l.id=2; 
```

这样索引列就生效了。

4、使用 order by 造成的全表扫描

```sql
SELECT * FROM user ORDER BY age DESC
```

上述语句在 age 上加了索引，但依然造成了全表扫描，这是因为我们使用了 SELECT *,导致回表查询，MySQL 认为回表的代价比全表扫描更大，所以不选择使用索引，如果想使用到 age 的索引，我们可以用覆盖索引来代替:

```sql
SELECT age FROM user ORDER BY age DESC
```

或者加上 limit 的条件（数据比较小）

```sql
SELECT * FROM user ORDER BY age DESC limit 10
```

这样就能利用到索引。

### 无法避免对索引列使用函数，怎么使用索引

有时候我们无法避免对索引列使用函数，但这样做会导致全表索引，是否有更好的方式呢。

比如我现在就是想记录 2016 ~ 2018 所有年份 7月份的交易记录总数

```sql
mysql> SELECT count(*) FROM tradelog WHERE month(t_modified)=7;
```

由于索引列是函数的参数，所以显然无法用到索引，我们可以将它改造成基本字段区间的查找如下

```sql
SELECT count(*) FROM tradelog WHERE
    -> (t_modified >= '2016-7-1' AND t_modified<'2016-8-1') or
    -> (t_modified >= '2017-7-1' AND t_modified<'2017-8-1') or 
    -> (t_modified >= '2018-7-1' AND t_modified<'2018-8-1');
```

### 前缀索引与索引选择性

之前我们说过，如于长字符串的字段（如 url），我们可以用伪哈希索引的形式来创建索引，以避免索引变得既大又慢，除此之外其实还可以用前缀索引（字符串的部分字符）的形式来达到我们的目的，那么这个前缀索引应该如何选取呢，这叫涉及到一个叫索引选择性的概念

> 索引选择性：不重复的索引值（也称为基数，cardinality）和数据表的记录总数的比值，比值越高，代表索引的选择性越好，唯一索引的选择性是最好的，比值是 1。

画外音：我们可以通过 **`SHOW INDEXES FROM table`** 来查看每个索引 cardinality 的值以评估索引设计的合理性

怎么选择这个比例呢，我们可以分别取前 3，4，5，6，7 的前缀索引，然后再比较下选择这几个前缀索引的选择性，执行以下语句

```sql
SELECT 
 COUNT(DISTINCT LEFT(city,3))/COUNT(*) as sel3,
 COUNT(DISTINCT LEFT(city,4))/COUNT(*) as sel4,
 COUNT(DISTINCT LEFT(city,5))/COUNT(*) as sel5,
 COUNT(DISTINCT LEFT(city,6))/COUNT(*) as sel6,
 COUNT(DISTINCT LEFT(city,7))/COUNT(*) as sel7
FROM city_demo
```

得结果如下

| sel3   | sel4   | sel5   | sel6   | sel7   |
| :----- | :----- | :----- | :----- | :----- |
| 0.0239 | 0.0293 | 0.0305 | 0.0309 | 0.0310 |

可以看到当前缀长度为 7 时，索引选择性提升的比例已经很小了，也就是说应该选择 city 的前六个字符作为前缀索引，如下

```sql
ALTER TABLE city_demo ADD KEY(city(6))
```

我们当前是以平均选择性为指标的，有时候这样是不够的，还得考虑最坏情况下的选择性，以这个 demo 为例，可能一些人看到选择 4，5 的前缀索引与选择 6，7 的选择性相差不大，那就得看下选择 4，5 的前缀索引分布是否均匀了

```sql
SELECT 
    COUNT(*) AS  cnt, 
    LEFT(city, 4) AS pref
  FROM city_demo GROUP BY pref ORDER BY cnt DESC LIMIT 5
```

可能会出现以下结果

| cnt  | pref |
| :--- | :--- |
| 305  | Sant |
| 200  | Toul |
| 90   | Chic |
| 20   | Chan |

可以看到分布极不均匀，以 Sant，Toul 为前缀索引的数量极多，这两者的选择性都不是很理想，所以要选择前缀索引时也要考虑最差的选择性的情况。

前缀索引虽然能实现索引占用空间小且快的效果，但它也有明显的弱点，MySQL 无法使用前缀索引做 ORDER BY 和 GROUP BY ，而且也无法使用前缀索引做覆盖扫描，前缀索引也有可能增加扫描行数。

假设有以下表数据及要执行的 SQL

| id   | email              |
| :--- | :----------------- |
| 1    | zhangssxyz@163.com |
| 2    | zhangs1@163.com    |
| 3    | zhangs1@163.com    |
| 4    | zhangs1@163.com    |

```sql
SELECT id,email FROM user WHERE email='zhangssxyz@xxx.com';
```

如果我们针对 email 设置的是整个字段的索引，则上表中根据 「zhangssxyz@163.com」查询到相关记记录后,再查询此记录的下一条记录，发现没有，停止扫描，此时可知**只扫描一行记录**，如果我们以前六个字符（即 email(6)）作为前缀索引，则显然要扫描四行记录，并且获得行记录后不得不回到主键索引再判断 email 字段的值，所以使用前缀索引要评估它带来的这些开销。

另外有一种情况我们可能需要考虑一下，如果前缀基本都是相同的该怎么办，比如现在我们为某市的市民建立一个人口信息表，则这个市人口的身份证虽然不同，但身份证前面的几位数都是相同的，这种情况该怎么建立前缀索引呢。

一种方式就是我们上文说的，针对身份证建立哈希索引，另一种方式比较巧妙，将身份证倒序存储，查的时候可以按如下方式查询:

```sql
SELECT field_list FROM t WHERE id_card = reverse('input_id_card_string');
```

这样就可以用身份证的后六位作前缀索引了，是不是很巧妙 ^_^

实际上上文所述的索引选择性同样适用于联合索引的设计，如果没有特殊情况，我们一般建议在建立联合索引时，把选择性最高的列放在最前面，比如，对于以下语句：

```sql
SELECT * FROM payment WHERE staff_id = xxx AND customer_id = xxx;
```

单就这个语句而言， (staff_id，customer_id) 和  (customer_id, staff_id) 这两个联合索引我们应该建哪一个呢，可以统计下这两者的选择性。

```sql
SELECT 
 COUNT(DISTINCT staff_id)/COUNT(*) as staff_id_selectivity,
 COUNT(DISTINCT customer_id)/COUNT(*) as customer_id_selectivity,
 COUNT(*)
FROM payment
```

结果为: ;

```
staff_id_selectivity: 0.0001
customer_id_selectivity: 0.0373
COUNT(*): 16049
```

从中可以看出 customer_id 的选择性更高，所以应该选择 customer_id 作为第一列。

## 索引设计准则：三星索引

上文我们得出了一个索引列顺序的经验 法则：将选择性最高的列放在索引的最前列，这种建立在某些场景可能有用，但通常不如避免随机 IO 和 排序那么重要，这里引入索引设计中非常著名的一个准则：三星索引。

如果一个查询满足三星索引中三颗星的所有索引条件，**理论上**可以认为我们设计的索引是最好的索引。什么是三星索引

1. 第一颗星：WHERE 后面参与查询的列可以组成了单列索引或联合索引
2. 第二颗星：避免排序，即如果 SQL 语句中出现 order by colulmn，那么取出的结果集就已经是按照 column 排序好的，不需要再生成临时表
3. 第三颗星：SELECT 对应的列应该尽量是索引列，即尽量避免回表查询。

所以对于如下语句:

```sql
SELECT age, name, city where age = xxx and name = xxx order by age
```

设计的索引应该是 (age, name,city) 或者 (name, age,city)

当然 了三星索引是一个比较理想化的标准，实际操作往往只能满足期望中的一颗或两颗星，考虑如下语句:

```sql
SELECT age, name, city where age >= 10 AND age <= 20 and city = xxx order by name desc
```

假设我们分别为这三列建了联合索引，则显然它符合第三颗星（使用了覆盖索引），如果索引是（city, age, name)，则虽然满足了第一颗星，但排序无法用到索引，不满足第二颗星，如果索引是 (city, name, age)，则第二颗星满足了(name有序），但此时 age 在 WHERE 中的搜索条件又无法满足第一星，

另外第三颗星（尽量使用覆盖索引）也无法完全满足，试想我要 SELECT 多列，要把这多列都设置为联合索引吗，这对索引的维护是个问题，因为每一次表的 CURD 都伴随着索引的更新，很可能频繁伴随着页分裂与页合并。

综上所述，三星索引只是给我们构建索引提供了一个参考，索引设计应该尽量靠近三星索引的标准，但实际场景我们一般无法同时满足三星索引，一般我们会优先选择满足第三颗星（因为回表代价较大）至于第一，二颗星就要依赖于实际的成本及实际的业务场景考虑。

## 总结

本文简述了索引的基本原理，索引的几种类型，以及分析了一下设计索引尽量应该遵循的一些准则，相信我们对索引的理解又更深了一步。另外强烈建议大家去学习一下附录中的几本书。文中的挺多例子都是在文末的参考资料中总结出来的，读经典书籍，相信大家会受益匪浅！

巨人的肩膀 

《高性能 MySQL》 

《Relational Database index design and the optimizers》 

《MySQL 实战 45讲》https://time.geekbang.org/column/article/71492

---

# 为什么SQL这么慢

**SQL 语句执行慢的原因是面试中经常会被问到的**，对于服务端开发来说也是必须要关注的问题。总而言之，**出了问题应该要做到心里有数**。



在生产环境中，SQL 执行慢是很严重的事件。那么如何定位慢 SQL、慢的原因及如何防患于未然。接下来带着这些问题来开启本篇之旅！



![图片](https://mmbiz.qpic.cn/mmbiz_png/g6hBZ0jzZb3ERsp8aGUhayQ1Px6RYfaYgkrGfA93bUQJWL09OhWkMYQSLqPQP05AZnbOoabfznE7ZAficiaFyEAQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

\- 思维导图 -





## **写操作**



作为后端开发，日常操作数据库最常用的是写操作和读操作。读操作我们下边会讲，这个分类里我们主要来看看写操作时为什么会导致 SQL 变慢。



### **刷脏页**



脏页的定义是这样的：内存数据页和磁盘数据页不一致时，那么称这个内存数据页为脏页。



那为什么会出现脏页，刷脏页又怎么会导致 SQL 变慢呢？那就需要我们来看看写操作时的流程是什么样的。



对于一条写操作的 SQL 来说，执行的过程中涉及到写日志，内存及同步磁盘这几种情况。



![图片](https://mmbiz.qpic.cn/mmbiz_png/g6hBZ0jzZb2pHALP0GdjH9HCcsdd0ymw1VG9lPUlUp6lKgWxrGEzJJfKYO5OTGNJjqwGcVDSLZYh89NjCYXyTg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

\- Mysql 架构图 -



这里要提到一个日志文件，那就是 redo log，位于存储引擎层，用来存储物理日志。在写操作的时候，存储引擎（这里讨论的是 Innodb）会将记录写入到 redo log 中，并更新缓存，这样更新操作就算完成了。后续操作存储引擎会在适当的时候把操作记录同步到磁盘里。



看到这里你可能会有个疑问，redo log 不是日志文件吗，日志文件就存储在磁盘上，那写的时候岂不很慢吗？



其实，写redo log 的过程是顺序写磁盘的，磁盘顺序写减少了寻道等时间，速度比随机写要快很多（ 类似Kafka存储原理），因此写 redo log 速度是很快的。



好了，让我们回到开始时候的问题，为什么会出现脏页，并且脏页为什么会使 SQL 变慢。你想想，redo log 大小是一定的，且是循环写入的。在高并发场景下，redo log 很快被写满了，但是数据来不及同步到磁盘里，这时候就会产生脏页，并且还会阻塞后续的写入操作。SQL 执行自然会变慢。



### **锁**



写操作时 SQL 慢的另一种情况是可能遇到了锁，这个很容易理解。举个例子，你和别人合租了一间屋子，只有一个卫生间，你们俩同时都想去，但对方比你早了一丢丢。那么此时你只能等对方出来后才能进去。



对应到 Mysql 中，当某一条 SQL 所要更改的行刚好被加了锁，那么此时只有等锁释放了后才能进行后续操作。



但是还有一种极端情况，你的室友一直占用着卫生间，那么此时你该怎么整，总不能尿裤子吧，多丢人。对应到Mysql 里就是遇到了死锁或是锁等待的情况。这时候该如何处理呢？



Mysql 中提供了查看当前锁情况的方式：



![图片](https://mmbiz.qpic.cn/mmbiz_png/g6hBZ0jzZb3ERsp8aGUhayQ1Px6RYfaYdUwYukiawmFoXVy3l2IjbOTPRAqyaB59Tjxh5LWdHEZDR6k6V2AtgHg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



通过在命令行执行图中的语句，可以查看当前运行的事务情况，这里介绍几个查询结果中重要的参数：



![图片](https://mmbiz.qpic.cn/mmbiz_png/g6hBZ0jzZb3ERsp8aGUhayQ1Px6RYfaY25dc0SSUibA9eWic9oOcPwUebGOsplVbaT5znmAc7JXeeW78FsABVRmg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



当前事务如果等待时间过长或出现死锁的情况，可以通过 「**kill 线程ID**」 的方式释放当前的锁。



这里的线程 ID 指表中 **trx_mysql_thread_id** 参数。



## **读操作**



说完了写操作，读操作大家可能相对来说更熟悉一些。SQL 慢导致读操作变慢的问题在工作中是经常会被涉及到的。



### **慢查询**



在讲读操作变慢的原因之前我们先来看看是如何定位慢 SQL 的。Mysql 中有一个叫作**慢查询日志**的东西，它是用来记录超过指定时间的 SQL 语句的。默认情况下是关闭的，通过手动配置才能开启慢查询日志进行定位。


具体的配置方式是这样的：



- 查看当前慢查询日志的开启情况：



![图片](https://mmbiz.qpic.cn/mmbiz_png/g6hBZ0jzZb3ERsp8aGUhayQ1Px6RYfaYz2zGqniaLDMek0I6QcsibZqw9ibCK6In2n4x2IgicfOHPb97R2DGalibuWw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



- 开启慢查询日志（临时）：



![图片](https://mmbiz.qpic.cn/mmbiz_png/g6hBZ0jzZb3ERsp8aGUhayQ1Px6RYfaYI259xicNUxExMkqxuqejXjvKNxoLBI3wcMpHz0e2SGJoqjXnqrZiajEg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

![图片](https://mmbiz.qpic.cn/mmbiz_png/g6hBZ0jzZb3ERsp8aGUhayQ1Px6RYfaYC62icpYGic9jqnRvLzUKXs98tdIfggxTKwsdmePGwPZ8XfWhgqC2sxUw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



注意这里只是临时开启了慢查询日志，如果 mysql 重启后则会失效。可以 my.cnf 中进行配置使其永久生效。



#### **存在原因**



知道了如何查看执行慢的 SQL 了，那么我们接着看读操作时为什么会导致慢查询，这里列两点常见的原因。



**（1）未命中索引**



SQL 查询慢的原因之一是可能未命中索引，关于使用索引为什么能使查询变快以及使用时的注意事项，网上已经很多了，这里就不多赘述了。



**（2）脏页问题**



另一种还是我们上边所提到的刷脏页情况，只不过和写操作不同的是，是在读时候进行刷脏页的。



是不是有点懵逼，别急，听我娓娓道来：



为了避免每次在读写数据时访问磁盘增加 IO 开销，Innodb 存储引擎通过把相应的数据页和索引页加载到内存的缓冲池（buffer pool）中来提高读写速度。然后按照比如最近最少使用原则来保留缓冲池中的缓存数据。



那么当要读入的数据页不在内存中时，就需要到缓冲池中申请一个数据页，但缓冲池中数据页是一定的，当数据页达到上限时此时就需要把最久不使用的数据页从内存中淘汰掉。但如果淘汰的是脏页呢，那么就需要把脏页刷到磁盘里才能进行复用。



你看，又回到了刷脏页的情况，读操作时变慢你也能理解了吧？



#### **防患于未然**



知道了原因，我们如何来避免或缓解这种情况呢？



首先来看未命中索引的情况：



不知道大家有没有使用 Mysql 中 explain 的习惯，反正我是每次都会用它来查看下当前 SQL 命中索引的情况。避免其带来一些未知的隐患。



这里简单介绍下其使用方式，通过在所执行的 SQL 前加上 explain 就可以来分析当前 SQL 的执行计划：



![图片](https://mmbiz.qpic.cn/mmbiz_png/g6hBZ0jzZb04YibLkryLLt3Uj3YSXyHKp6j1Z6sbE80Owsvd3PicVqGx0hffKxTVrNibBuQvU6BGqibbpsoDUiaaBbw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



执行后的结果对应的字段概要描述如下图所示：



![图片](https://mmbiz.qpic.cn/mmbiz_jpg/g6hBZ0jzZb2pHALP0GdjH9HCcsdd0ymw62Tibzg6cr3KsbXuFNbYn5DLaIIhXSibPnbKnRPldvtjIM6akgMsy3SA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



这里需要重点关注以下几个字段：



**1、type**



表示 MySQL 在表中找到所需行的方式。其中常用的类型有：ALL、index、range、 ref、eq_ref、const、system、NULL 这些类型从左到右，性能逐渐变好。



- ALL：Mysql 遍历全表来找到匹配的行；

  

- index：与 ALL 区别为 index 类型只遍历索引树；

  

- range：只检索给定范围的行，使用一个索引来选择行；

  

- ref：表示上述表的连接匹配条件，哪些列或常量被用于查找索引列上的值；

  

- eq_ref：类似ref，区别在于使用的是否为唯一索引。对于每个索引键值，表中只有一条记录匹配，简单来说，就是多表连接中使用 primary key 或者 unique key作为关联条件；

  

- const、system：当 Mysql 对查询某部分进行优化，并转换为一个常量时，使用这些类型访问。如将主键置于 where 列表中，Mysql 就能将该查询转换为一个常量，system 是 const类型的特例，当查询的表只有一行的情况下，使用system；

  

- NULL：Mysql 在优化过程中分解语句，执行时甚至不用访问表或索引，例如从一个索引列里选取最小值可以通过单独索引查找完成。



**2、possible_keys**



查询时可能使用到的索引（但不一定会被使用，没有任何索引时显示为 NULL）。



**3、key**



实际使用到的索引。



**4、rows**



估算查找到对应的记录所需要的行数。



**5、Extra**



比较常见的是下面几种：



- Useing index：表明使用了覆盖索引，无需进行回表；

  

- Using where：不用读取表中所有信息，仅通过索引就可以获取所需数据，这发生在对表的全部的请求列都是同一个索引的部分的时候，表示mysql服务器将在存储引擎检索行后再进行过滤；

  

- Using temporary：表示MySQL需要使用临时表来存储结果集，常见于排序和分组查询，常见 group by，order by；

  

- Using filesort：当Query中包含 order by 操作，而且无法利用索引完成的排序操作称为“文件排序”。



对于刷脏页的情况，我们需要控制脏页的比例，不要让它经常接近 75%。同时还要控制 redo log 的写盘速度，并且通过设置 innodb_io_capacity 参数告诉 InnoDB 你的磁盘能力。









## **小 结**



**写操作**



- 当 redo log 写满时就会进行刷脏页，此时写操作也会终止，那么 SQL 执行自然就会变慢。

  

- 遇到所要修改的数据行或表加了锁时，需要等待锁释放后才能进行后续操作，SQL 执行也会变慢。



**读操作**



- 读操作慢很常见的一个原因是未命中索引从而导致全表扫描，可以通过 explain 方式对 SQL 语句进行分析。

  

- 另一种原因是在读操作时，要读入的数据页不在内存中，需要通过淘汰脏页才能申请新的数据页从而导致执行变慢。

---



# count(1)和count(*)到底那个效率高

 **count(1)和count(\*)对比** 

当表的数据量大些时，对表作分析之后，使用 `count(1)`还要比使用 `count(*)`用时多了！

从执行计划来看， `count(1)` 和 `count(*)`的效果是一样的。但是在表做过分析之后， `count(1)` 会比 `count(*)`的用时少些（1w以内数据量），不过差不了多少。

如果 `count(1)`是聚索引，那肯定是 `count(1)`快，但是差的很小。因为 `count(*)`自动会优化指定到那一个字段，所以没必要去 `count(1)`，用 `count(*)` sql会帮你完成优化的，因此：`count(1)` 和 `count(*)`基本没有差别！

------

 **count(1)和count(列名)对比** 

两者的主要区别是：

- `count(1)` 会统计表中的所有的记录数，包含字段为 `null` 的记录。
- `count(字段)` 会统计该字段在表中出现的次数，忽略字段为 `null` 的情况。即不统计字段为 `null` 的记录。

------

 **count(\*)、count(1)和count(列名)区别** 

**执行效果上：**

- `count(*)`包括了所有的列，相当于行数，在统计结果的时候，不会忽略列值为NULL
- `count(1)`包括了忽略所有列，用1代表代码行，在统计结果的时候，不会忽略列值为NULL
- `count(列名)`只包括列名那一列，在统计结果的时候，会忽略列值为空（这里的空不是只空字符串或者0，而是表示null）的计数，即某个字段值为NULL时，不统计。

**执行效率上：**

- 列名为主键， `count(列名)` 会比 `count(1)`快
- 列名不为主键， `count(1)` 会比 `count(列名)`快
- 如果表多个列并且没有主键，则 `count(1)` 的执行效率优于 `count(*)`
- 如果有主键，则 `selectcount(主键)` 的执行效率是最优的
- 如果表只有一个字段，则 `selectcount（*）`最优。

**实例分析：**

```sql
mysql> create table counttest(name char(1), age char(2));
Query OK, 0 rows affected (0.03 sec)
mysql> insert into counttest values    
-> ('a', '14'),('a', '15'), ('a', '15'),     
-> ('b', NULL), ('b', '16'),     
-> ('c', '17'),    
-> ('d', null),     
-> ('e', '');
Query OK, 8 rows affected (0.01 sec)Records: 8  Duplicates: 0  Warnings: 0
mysql> select * from counttest;
+------+------+
| name | age  |
+------+------+
| a    | 14   |
| a    | 15   |
| a    | 15   |
| b    | NULL |
| b    | 16   |
| c    | 17   |
| d    | NULL |
| e    |      |
+------+------+
8 rows in set (0.00 sec)
mysql> select name, count(name), count(1), count(*), count(age), count(distinct(age))    
-> from counttest    
-> group by name;
+------+-------------+----------+----------+------------+----------------------+
| name | count(name) | count(1) | count(*) | count(age) | count(distinct(age)) |
+------+-------------+----------+----------+------------+----------------------+
| a    |           3 |        3 |        3 |          3 |                    2 |
| b    |           2 |        2 |        2 |          1 |                    1 |
| c    |           1 |        1 |        1 |          1 |                    1 |
| d    |           1 |        1 |        1 |          0 |                    0 |
| e    |           1 |        1 |        1 |          1 |                    1 |
+------+-------------+----------+----------+------------+----------------------+
5 rows in set (0.00 sec)
```

---

# [为什么阿里规定超过三张表禁止join](https://mp.weixin.qq.com/s/7vN9Nf20NGnvLKALHw_O1Q)

 **前  言** 

最近在看《阿里巴巴开发手册》，发现一个很有趣的编程规约，即《阿里巴巴开发手册中》**第五章第二节第二条**中明确规定了：

**超过三个表禁止 join。需要 join 的字段，数据类型必须绝对一致；多表关联查询时，保证被关联的字段需要有索引。**

不信你看：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/xq9PqibkVAzpQ6qWny39lLQm6KYGiazzSIeB50gzLXJSAzamXAhqZCWpUzeTxVVGiaCXic28NTjWhWDNGDt6IiaibAVA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这让人突然想起了这张神图：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzqwhiaSgskicibysSXAV3WMeawwbxBhejbk4MDTSDsGviaD5B26icGxo2uSbMo7HFYbklypXHUtQs3dJtg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

> woc，看来以前多表狂 join的骚操作该停一停了。。。

------

 **为什么做这种限制** 

打个比方，如果我有无限的钱，我想买个豪华别墅，想买个跑车，想买个直升飞机，但现实是我没钱，只能租房住，只能走路上下班。

如果数据库的性能无限强大，多个表的join肯定是需要的，尤其是复杂的分析型(OLAP)查询，甚至可能涉及10几个表的join，但现实是大部分数据库的性能都太弱了，尤其是涉及到多表join的查询。

规范一看就是在使用 MySQL时的限制（这种规范实际上迫不得已的限制），做这个限制有**两个原因：**

一是**优化器很弱**，涉及多个表的查询，往往得不到很好的查询计划，这块比较复杂，需要看一些MySQL深层次的书籍；二是**执行器很弱**，只有 `nested loop join`， `block nested loop join`和 `index nested loop join`。

- **nested loop join** 就是分别从两个表读一行数据进行两两对比，复杂度是 `n^2`
- **block nested loop join** 是分别从两个表读很多行数据，然后进行两两对比，复杂度也是 `n^2`，只是少了些函数调用等overhead
- **index nested loop join** 是从第一个表读一行，然后在第二个表的索引中查找这个数据，索引是B+树索引，复杂度可以近似认为是 `nlogn`，比上面两个好很多，这就是**要保证关联字段有索引的原因**
- 如果有**hash join**，就不用做这种限制了，用第一个表（小表）建hash table，第二个表在hash table中查找匹配的项，复杂度是n。缺点是hash table占的内存可能会比较大，不过也有基于磁盘的hash join，实现起来比较复杂

------

 **改进SQL该如何写** 

但是，可是我确实需要两个表里的数据链接在一起怎么办呢。

一种方案是：我们可以做个冗余，建表的时候，就把这些列放在一个表里，比如一开始有 `student(id,name)`， `class(id,description)`， `student_class(student_id,class_id)`三张表，这样是符合数据库范式的(第一范式，第二范式，第三范式，BC范式等)，没有任何冗余，但是马上就不符合“编程规范“了，那我们可以用一张大表代替它， `student_class_full(student_id,class_id,name,description)`，这样name和description可能要被存储多份，但是由于不需要join了，查询的性能就可以提高很多了。

除此之外，常见的还能思考到的解决方法比如有：

- 可以尝试拆解复杂查询语录成为多条查询语句，将1拆解为n+1，其中每条简单的大表查询尽量走索性，提高查询效率。当然也要参考隔离级别是否会产生数据不一致的情况。
- join顺序适当优化，尽量优化
- 自建应用层缓存，但涉及到增删查改可能额外代价比较大。

**当然多插一句，不同项目的业务不同，能定制化解决的方案也不尽相同，还是得根据实际情况来。**

------

 **多BB两句** 

这种问题没有对错，属于中立性问题，无论从正面或反面都能解释出一堆原因。

**任何规范都有自己合适的应用场景**，很明显 超过三张表禁join是阿里中的场景，而不是你的场景，写代码需要有自己的思维，不能因为阿里说禁join 谷歌说不推荐try catch，然后你就按图索骥全线禁止join禁止try catch。要根据实际情况，该join的join，该catch的catch，其实现代的商业数据库对sql的优化能力比你自己写的可能还是要高不少的。

---

# MySQL索引下推(ICP)



## 什么是索引下推

索引下推（Index Condition Pushdown ICP）

当MySQL使用一个索引来检索表中的行时，可以使用`ICP`作为一种优化方案。不使用`ICP`时，存储引擎通过索引检索基础表中的行并将符合`WHERE`条件中的行返回给客户端。启用`ICP`后，如果只需要通过索引中的列就能够评估行是否符合`WHERE`中的一部分条件，MySQL将这部分`WHERE`条件下推到存储引擎中，然后存储引擎评估使用索引条目来评估下推的索引条件，并只从表中读取符合条件的行。`ICP`可以减少存储引擎访问基础表的次数以及MySQL访问存储引擎的次数。



![图片](https://mmbiz.qpic.cn/mmbiz_png/g6hBZ0jzZb3vFIzSDj4iagHIjcwkV2DRmqHNzxvNKdjBcqfYsbu23pEOaHFL8NqxzaleE6dxm31Asw9dGqeQM1A/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

\- 思维导图 -



### **回表操作**



对于数据库来说，只要涉及到索引，必然绕不过去回表操作。当然这也是我们今天所讲内容的前调基础。



说到回表，我们需要从索引开始说起。别担心，不会长篇大论，这里只是简单讲下主键索引与普通索引，目的是让大家对回表操作有个认识。如果你对回表操作很熟悉了，那么可以跳过这一段。



这里我们只以 Innodb 存储引擎作为讲解对象。



### **主键索引**



主键索引在底层的数据存储是通过 B+ 树来实现的。简单来说，就是除叶子节之外的其他节点都存储的是主键值。而叶子节点上存储的是整行的数据。



大体结构如下图所示。



![图片](https://mmbiz.qpic.cn/mmbiz_png/g6hBZ0jzZb3vFIzSDj4iagHIjcwkV2DRmv8DN1m8WP6C0vYLMHhsThOB4cObP00G8Y7UNO1zTsG9HCr4p8pvEDA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



### **非主键索引**



除了主键索引外，其它的索引都被称为非主键索引。与主键索引不同的是，非主键索引的叶子节点上存储的是主键的值。



![图片](https://mmbiz.qpic.cn/mmbiz_png/g6hBZ0jzZb3vFIzSDj4iagHIjcwkV2DRmn50bPwgOibBNC5FOFe8OxUnQJt9B7W4NjkdcNxsoCSzwKeVicaCtwCTQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



那让我们再回到开始的问题，什么是回表操作？



当我们在非主键索引上查找一行数据的时候，此时的查找方式是先搜索非主键索引树，拿到对应的主键值，再到主键索引树上查找对应的行数据。



这种操作就叫作回表操作。



好了，这里你应该了解了什么是回表操作了。简单来讲，就是在非主键索引树上拿到对应的主键值，然后回到主键索引上找到对应的行数据。



这样做的前提条件是，所要查找的字段不存在于非主键索引树上。



### **低版本操作**



讲完了回表操作，让我们继续回到这篇文章的主题——索引下推。



其实在 Mysql 5.6 版本之前是没有索引下推这个功能的，从 5.6 版本后才加上了这个优化项。所以在引出索引下推前还是先回顾下没有这个功能时是怎样一种处理方式。



我们以一个真实例子来进行讲解。



在这里有张用户表 user，记录着用户的姓名，性别，身高，年龄等信息。表中 id 是自增主键，(name,sex) 是联合索引。在这里用 1 表示男，2 表示女。现在需要查找所有姓王的男性信息。



SQL 实现起来很简单：



![图片](https://mmbiz.qpic.cn/mmbiz_png/g6hBZ0jzZb3fibGnP7olia7Nh0a9MUtVKehTIYhoUiaibAJpsQNdAGTibDZPiagibtM6KoRTiaZzzutaSTUKRMicDYckCPA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



但是它的实现原理是什么呢？



根据联合索引最左前缀原则，我们在非主键索引树上找到第一个满足条件的值时，通过叶子节点记录的主键值再回到主键索引树上查找到对应的行数据，再对比是否为当前所要查找的性别。



整个原理可以用下边的图进行表示。



![图片](https://mmbiz.qpic.cn/mmbiz_png/g6hBZ0jzZb3vFIzSDj4iagHIjcwkV2DRmibnSfeibTHsAGQSEjtm1FPvGZWmtA6wU1ZORreibDPmxvNRPicLltY8BNA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



看到了吧，低版本中需要每条数据都进行回表，增加了树的搜索次数。如果遇到所要查找的数据量很大的话，性能必然有所缺失。



### **高版本操作**



讲完了低版本操作，让我们继续回到这篇文章的主题——索引下推。



知道了痛点，那么怎么解决。很简单，只有符合条件了再进行回表。结合我们的例子来说就是当满足了性别 sex = 1 了，再回表查找。这样原本可能需要进行回表查找 4 次，现在可能只需要 2 次就可以了。



![图片](https://mmbiz.qpic.cn/mmbiz_png/g6hBZ0jzZb3vFIzSDj4iagHIjcwkV2DRmMBlH5LZICQ24ZbEJTtpPpEBxB1Z9Tbejx4HWeCCMCwE0jicqB7dGHxA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



所以本质来说，索引下推就是只有符合条件再进行回表，对索引中包含的字段先进行判断，不符合条件的跳过。减少了不必要的回表操作。









### **总结**



**回表操作**

- 当所要查找的字段不在非主键索引树上时，需要通过叶子节点的主键值去主键索引上获取对应的行数据，这个过程称为回表操作。



**索引下推**

- 索引下推主要是减少了不必要的回表操作。对于查找出来的数据，先过滤掉不符合条件的，其余的再去主键索引树上查找。





## ICP适用情景

- ICP可以用于需要访问整个表中的行的`range,ref,eq_ref,ref_or_null`访问模式。
- ICP可以用于`InnoDB`和`MyISAM`，包括分区表。

### ICP限制

- 对于`InnoDB`表，`ICP`只适用于辅助索引。因为`ICP`的目标是减少全表读的数量从而减少`I/O`操作。对于`InnoDB`聚集索引，完整的记录已经被读取到`InnoDB`缓冲区，在这种情况下使用`ICP`不能降低`I/O`
- `ICP`不支持在虚拟生成列上创建的索引。`InnoDB`存储引擎支持在虚拟生成列上创建索引
- 与子查询相关的条件不能下推
- 与存储函数相关的条件不能下推。
- 触发条件不能下推

## ICP优化过程

要理解ICP优化如何工作，首先考虑一下，在不使用索引下推的情况下，索引扫描是如何进行的：

- 获取下一行，首先读取索引元组，然后通过索引元组检索并读取整行数据。
- 对表中的数据测试是否符合`WHERE`条件，基于测试结果接收或拒绝行
   使用索引下推时，扫描过程如下：
- 获取下一行的索引元组（不需要获取整行数据）
- 测试能否只通过索引中的行确认数据是否符合`WHERE`条件中的一部分。如果不符合条件，继续获取下一个索引元组。
- 如果符合条件，通过索引元组来检索并读取整行数据
- 测试数据是否符合`WHERE`条件中的其他部分。基于测试结果接收或拒绝行。



## ICP的EXPLIAN输出

在`Extra`列中显示`Using index condition`。



## 示例

假设有一个表包括人员和地址数据，在表上具有一个索引`INDEX (zipcode, lastname, firstname)`.如果我们知道一个人的`zipcode`值但是不确定他的名称，我们可以使用以下查询：

```sql
SELECT * FROM people
  WHERE zipcode='95054'
  AND lastname LIKE '%etrunia%'
  AND address LIKE '%Main Street%';
```

MySQL可以使用索引检索`zipcode='95054'`的人。第二部分不能用来限制必须扫描的行数量（`lastname LIKE '%etrunia%'` 因为LIKE条件以`%`开头），当不使用索引下推时，这个查询必须检索所有`zipcode='95054'`的整行数据
 使用索引下推，MySQL在读取整行数据前，先检查`lastname LIKE '%etrunia%'`.这样就不用读取那些符合`zipcode`条件但是不符合`lastname`条件的行数据了。



## 控制ICP是否启用

可以使用`iptimizer_switch`变量的`index_condition_pushdown`标志来控制ICP是否启用

```sql
SET optimizer_switch = 'index_condition_pushdown=off';
SET optimizer_switch = 'index_condition_pushdown=on';
```

---

# [为什么大公司后台数据库都要搞分库分表？](https://mp.weixin.qq.com/s/yflzIQFiNa3tDJm7U9P8ig)

> 本文内容预览：
>
> 1. 库表会在哪天到达瓶颈？
>    `1.1 苏宁拼购百万级库表拆分之前`
>    `1.2 京东配运平台库表拆分之前`
>    `1.3 大众点评订单库拆分之前`
>    `1.4 小结：啥情况需要考虑库表拆分`
> 2. 拆分库表的目的和方案
>    `2.1 业务数据解耦--垂直拆分`
>    `2.2 解决容量和性能压力--水平拆分`
>    `2.3 分多少合适`
>    `2.4 怎么分合适`
> 3. 拆分带来新的问题
>    `分区键/唯一ID/数据迁移/分布式事务等`
> 4. 大厂案例，知识回顾扩展
>    `4.1 蚂蚁金服的库表路由规则`
>    `4.2 大众点评分库分表的数据迁移`
>    `4.3 淘宝万亿级交易订单的存储引擎`

## Part1库表会在哪天到达瓶颈？

### 1.1苏宁拼购百万级库表拆分之前[1]

苏宁拼购，苏宁易购旗下的电商App，18年7月累计用户突破3000万。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xE6oscyT533jiaGhJzEiaSzA8TVGia9dgKtfnO7TyFf3yJ8P17sOVvibAW5MvL2YUzwqhj3kJ8hvzveh4ggCk5CDUA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)图片来源于QCon大会PPT

面对千万级日活 + 千万级日新增SKU + 千万级日均订单，拼购的单库每天增长数据超1亿，峰值10万QPS并发，每个月要搞一次数据迁移。

庞大的数据量，对数据库压力和数据运维成本造成了很大的困扰，并且，一旦有一条未命中缓存的SQL，对于整个应用都是灾难级的。

所以，不得不考虑系统的稳定性和长远的业务支撑。

### 1.2京东配运平台库表拆分之前[2]

![图片](https://mmbiz.qpic.cn/mmbiz_png/xE6oscyT533jiaGhJzEiaSzA8TVGia9dgKte2ELLbr4OvTYHOIpjdic5K7k1PmgOC7ibabbGgwodib2CzRZ4upcm6BmA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)图片来源于QCon大会PPT

起初，用SQL Server存储，⽀支撑每天10万级别业务量。

扛不住后，采购了企业级Oracle/IBM AIX⼩型机，用 RAC + DataGuard方式，支撑配送所有业务，到百万级别单量。但是这种传统的企业架构，对于复杂多变的业务、昂贵的硬件成本、服务的部署和维护成本等痛点，变得越来越突出。

15年开始，京东配运平台开始按业务对数据库做垂直拆分，将存储容器化，实现了方便的水平扩容、更精细的成本控制、更复杂的业务形态支持.

### 1.3大众点评订单库拆分之前[3]

16年前，点评的订单库已经超200G容量，面对的越来越复杂的查询维度，为实现平稳查询，优化了索引并增加两个从库来分散数据库压力，但仍有很多效率不理想的数据库请求出现。

而随后而来的价格战、大量抢购的活动开展，订单数据库很快难以支撑，只能用限流、消息队列削峰填谷对其进行保护，才能勉强维持日常数据读写需求。

而随着业务模式的增加，原订单模型已经不能满足，如果经常用DDL去建表，建索引对于如此庞大的库表是非常吃力的，发生锁库锁表会直接影响线上服务。

所以，点评团队以未来十年不再担心订单容量为目的，开始进行库表切分。

### 1.4小结：啥情况需要考虑库表拆分

实际上，是没有一个非常量化的指标来判定库表瓶颈的，因为每个系统的业务场景，查询复杂度都有不同。

但力有穷尽时，我们虽然可以尽量的从加从库读写分离、优化sql、优化索引、复用连接等等方面进行优化，但总会有到达极限的时候的时候，量变引发质变。甚至，在真实生产环境，要更加未雨绸缪，不能等到崩了才去考虑。那么，应该怎么去判断已经到了库表拆分的时机呢：

- *硬件性能瓶颈*，如果是读操作多，其实可以加多个从库分担主库读压力；但如果是写操作多，会因为主库磁盘IO增大，拖慢处理速度；另外，如果单表数据量过大，导致索引层级增多，扫描行增多，CPU效率降低，影响sql执行效率，拖慢处理速度。而处理速度慢最终会导致连接数增加直至无连接可用。
- *日常运维投入*，就如苏宁拼购的情况，如果一个月就要搞一次数据迁移，这个人力的投入产出比，应该是完全不匹配的，那就不如一次性搞定它。
- *业务发展可支持程度、难度和风险*，当数据增长到一定程度，虽然没有达到极限，还能凑活，但是遇到活动型流量脉冲，无法完全支持业务需求；而业务需要进行迭代增加模式时，修改数据表带来的风险又比较大。就可以考虑重构数据模型，拆分库表了。

## Part2拆分库表的目的和方案

### 2.1业务数据解耦--垂直拆分

把不同的业务数据拆分到各自的数据库中独立维护，那么最底层的原因是什么呢？

是微服务下的上层服务拆分。为了满足快速迭代、安全发布、链路降级、主次业务解耦等问题，去解决代码大量冲突、小功能排队等待大版本发布等等问题，将业务按照一定逻辑进行拆解，形成一个个功能完备，独立运行的服务。[4]

然而，如果数据库层面不配合，就无法解决根本问题。当上层服务实例拆分后可以被大量横向扩展，以应对高并发的流量冲击，会导致底层数据库的承载压力和连接数急剧增加。

所以，通过垂直拆分将业务数据解耦，各管一事，以满足微服务的效能最大化。

### 2.2解决容量和性能压力--水平拆分

对某一业务库，当数据增量达到了库瓶颈，或者表瓶颈，就要进行库表的水平拆分了。

我之前遇到的很多情况，总是先分表，解决单表的容量和读写性能问题，随着业务发展，单库也遇到瓶颈了再考虑分库。

为啥不一步到位？

就像之前在阿里，新应用上来搞个百库百表？一来是因为一些用户规模和一些路由规则的问题；更重要的，不是所有公司其实不是所有的公司都和阿里一样有钱，有限的资源要用在更重要的生存问题上。

如果你作为一个初创公司的架构，给出了一套可能撑10年的存储方案，感觉会被同事在心里怼，公司能活3年么就这么浪费？

但肯定没有人说这话，因为我们还是希望所有公司都能蓬勃发展，蒸蒸日上的☺。

所以，拆分方法就很有讲究了，怎么分能让后续迭代发展的代价最小呢？

### 2.3分多少合适

`表主要看容量`，很多经验表明 上千万后性能会有显著下降，因此，我们可以把表容量定在一半多一点，600w。

`库主要看的是连接数`，我们以阿里对外售卖的云存储来大致估计，单库的连接数定在4000左右。![图片](https://mmbiz.qpic.cn/mmbiz_png/xE6oscyT533jiaGhJzEiaSzA8TVGia9dgKtrnicOekMiaP95jicUZgnbpPqP5SZEB3lQXB2maFC57e87wJIqZJMBFXLw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

抽象一个实际的评估案例来看：假如目前平台每天产生10w订单，峰值并发数8000QPS，然后考虑业务扩展和增长的速率：

比如，业务是和银行合作扩展业务，将大小银行量级平均一下，估计每合作一家可以带来多大的增长量，这里假设是5000单/天/家，如果业务计划是每年度合作10家，那就是5w，5年以后每天的单量，理论上可能会到25w/天。加上现有的10w, 峰值35w。

如果我们计划系统的容量需要支撑3年，或者说，3年之后的该业务扩展会趋于平缓，那么我们可以大致的估计为：

```
表：（3年 * 365天 * 35w=3.8亿 ）/600w = 63 约 64张表.
库：10000并发 / 4000 = 2.5 ,可按4个库来处理
```

当然，如果是BAT这种，不缺用户，不缺钱，又有一些既定路由规则的情况，还是可以一步到位的。比如，我之前做过的项目就是按百库百表来做。关于阿里的玩法后面再详细介绍一下。

*发现上述评估有问题的话，欢迎留言讨论~*

### 2.4怎么分合适

#### Hash取模

![图片](https://mmbiz.qpic.cn/mmbiz_png/xE6oscyT533jiaGhJzEiaSzA8TVGia9dgKt1DuFcb0DJtiald1fs1ydAZucQPugth0nYuPKMm4BEWAjf1pXwVXSfWw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**优点**：经过hash取模之后，分到库和分到表中的数据，都是均衡的，所以，不会出现资源倾斜的问题。

**缺点**：如果后续遇到业务暴增，没有在我们预估范围内，则要涉及到数据迁移，那就需要重新hash , 迁移数据，修改路由等等。

#### range划分

![图片](https://mmbiz.qpic.cn/mmbiz_png/xE6oscyT533jiaGhJzEiaSzA8TVGia9dgKtWkOgcZwP9sK6lBUhWBJIFpJ3ibC0xwadRx7f4WOicpdHgH12ApfV4AMQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)简单说，就是把数据划分范围，挨个存储，存满一个再存另一个。

**优点**：不需要数据迁移，后续数据即时增长很多也没问题。

**缺点**：数据倾斜严重，比如上图，很长一段时间，都会只用到1个库，几个表。

#### 一致性hash

![图片](https://mmbiz.qpic.cn/mmbiz_png/xE6oscyT533jiaGhJzEiaSzA8TVGia9dgKtB5BTIvCu3y6j7tm4kgYdTlzwFGnfaaAL7zvYgPGjCxwSXuzYLiaMNgA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)一致性hash环的节点一般按2^32^-1来算，但是一般如果业务ID足够均衡，则可以降一些节点，如4096等等，4个库的话，则均衡的分布在图上的位置，而数据通过hash计算，对应到外环的虚拟节点，然后归属于真实的库，对于表也可以同样处理。或者，直接把表节点部署在外环上，直接将数据归属于表。

**优点**：更加均匀，并且在需要扩容时，数据迁移的量级更小，只需要迁移1/N的数据即可。

**缺点**：路由算法要复杂，但是对于能得到的好处，这点复杂度就可以忽略了

#### 小结

那么,看起来，一致性hash的方法，是比较靠谱的了。但是只是这样就会对程序员很友好么？

我不知道其他公司，待过的某一家公司，的数据查询后台是纯天然的，不带任何修饰的，想要check下数据，得拿业务ID手动计算库表的位置。没经历过的不知道，真的是要烦死了。

在技术设施方面，还是不得不佩服大公司的投入，阿里给工程师提供的数据查询后台，其实是一个逻辑库，你可以用查询单表的方式去查询分库分表，后台会调用数据库配置平台的配置，自动计算库表路由，人性化的很。就算不去计算路由，直接打包查询多个库也是很好的，毕竟界面查询，能有多大并发呢。

还是那句话，*没有银弹*，其实除了这几种方式，还见过不少变种，但都是结合本公司，本业务的特性进行的改良。

## Part3拆分带来新的问题

#### 分区键选取

分区键要足够的均匀，比如，用户表用UID，订单表可以用UID，也可以用订单ID，商户表用商户ID，问题表用会话ID 等等，总之，一定可以找到业务上的唯一ID。当然还有一些特殊的分区，比如，日表，月表，则要按时间来分，等等。

#### 全局唯一主键ID

实际我理解这个就是分布式ID的生成问题，之前写的一篇[分布式ID生成算法](https://mp.weixin.qq.com/s?__biz=MzA4ODUzMDg5NQ==&mid=2650000700&idx=1&sn=4c6ff37313affd5ef9bb2d4cb863ef70&scene=21#wechat_redirect)，有兴趣可以浏览下。

#### 数据平滑迁移

**停机发布**：好处是简单，风险小；缺点是业务有损。那就看这个损能不能接受了

**平滑迁移**：平滑迁移就像是高速上换轮胎，要非常小心谨慎，也更复杂。思路可以类比快手kafka集群的扩容：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xE6oscyT533jiaGhJzEiaSzA8TVGia9dgKtepM4v85pO40nib4Mia53T6GrxT7eNYgxic3wibkEaUIWmBoHkThtPPdRIw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

![图片](https://mmbiz.qpic.cn/mmbiz_png/xE6oscyT533jiaGhJzEiaSzA8TVGia9dgKtb6tRHQQVw0mgdrNsbX1ljGZKoHz4T7rXUGmryNhf7xKlve725Vk8jg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

虽然场景不一样，但是思路使一致的。从某一点开始设置checkpoint , 然后执行数据双写，最后修改路由，删除旧数据，完成扩容。

#### 事务问题

之前由于数据都在一个库中，所以，只要保证一个本地事务就可以办到。现在数据被分到了多个库，那么事务怎么保证：

**（1）分布式事务**。分布式事务的方式很多，TCC、本地事务表+事务消息、最大努力通知，saga等等，之前有篇写我们[自研的saga长事务引擎](https://mp.weixin.qq.com/s?__biz=MzA4ODUzMDg5NQ==&mid=2650000675&idx=1&sn=f411dcfdf6135409b94963cbb45b4b0c&scene=21#wechat_redirect)的文章，有兴趣的可以看下。

**（2）程序+业务逻辑**。用业务逻辑+程序控制的方式，比如，之前文章中提到的[微信红包的系统设计](https://mp.weixin.qq.com/s?__biz=MzA4ODUzMDg5NQ==&mid=2650000991&idx=1&sn=4cd73cc5aa4ccb97d9823db82737d14b&scene=21#wechat_redirect)，用set化将一个红包的所有操作都落到同一个库上，避免了数据库锁竞争和分布式事务。**而**蚂蚁的支付业务涉及了业务订单库、计收费库、支付库、积分库等等，没有办法从业务逻辑层面进行完全串联，并且由于金融属性的强一致要求，采用了非常重的侵入式TCC来保证全局支付事务的一致。

#### 查询问题

之前一个库就能搞定的join，count等各种联合查询，将不复存在，老老实实调接口在代码层面实现吧。

## Part4大厂案例，知识回顾扩展

### 4.1蚂蚁金服的库表路由规则

上文也提到过，蚂蚁的分库分表其实是独树一帜的。因为，在蚂蚁体系下，需要遵守LDC单元化部署，单元化的路由由用户ID的倒数2，3位来决定。加上蚂蚁的用户规模，基本上大部分的应用都采用了百库百表类的方式进行(遇到定时任务的超大规模数据，还会千库千表的存在)。用户请求发起后的路由规则和数据库的路由执行链路简化如下：![图片](https://mmbiz.qpic.cn/mmbiz_png/xE6oscyT533jiaGhJzEiaSzA8TVGia9dgKtXNLyRia5vwZWCl1YU3t7F0669ew3gxEIia7mohZWa3OLw2zB33QI4hRA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)而一条订单的入库路由规则可以参考下面的示意图：[5]

![图片](https://mmbiz.qpic.cn/mmbiz_png/xE6oscyT533jiaGhJzEiaSzA8TVGia9dgKtcHibXCYTb3ByjlcibRuy5jCPTloQAoPCPZj1OmGgTNDzZwjzIzhAcicFw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)蚂蚁中间件产品介绍

这样的机制保证生成的 ID 支持 10 万亿次获取不重复。

有人可能会问，这个大的订单量，一个库也撑不了多久啊？

是的，比如之前搞的一个应用，其实是百库百表+定时数据迁移来实现的。业务数据每固定时间进行历史表迁移。而查询的时候的库表路由，都由中间件ZDAL从配置平台拉取配置来决定，是走历史库还是走当前库。

### 4.2大众点评分库分表的数据迁移

![图片](https://mmbiz.qpic.cn/mmbiz_png/xE6oscyT533jiaGhJzEiaSzA8TVGia9dgKtFcfN8zRjxkaFxCutqOHF9cld3TqtsUTJfWJG8yFs9rn6H6LPa8wvkg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

![图片](https://mmbiz.qpic.cn/mmbiz_png/xE6oscyT533jiaGhJzEiaSzA8TVGia9dgKtN1vYdh7bcK7va5GnWfMMVcvAia0V0qI2jB0ws9yiaDUmPV1fUI6ZD8fg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

![图片](https://mmbiz.qpic.cn/mmbiz_png/xE6oscyT533jiaGhJzEiaSzA8TVGia9dgKtgVy9Q8XasDd2rdu3ZgUTvK40UVgOBMojTU1r5p3CWUJz32v0uM7sicw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



- 阶段一：数据双写，以老数据为准。通过对账补平差异
- 阶段二：导入历史数据，继续双写，读切到新数据。
- 阶段三：停掉双写，删除老数据完成迁移

### 4.3淘宝万亿级交易订单的存储引擎[6]

淘宝超级量级下的交易单是怎么解决存储性能等问题的：![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

![图片](https://mmbiz.qpic.cn/mmbiz_png/xE6oscyT533jiaGhJzEiaSzA8TVGia9dgKtcicqB2OXG9SyBaK3fyiaC2WrQHUMn3DibbFAERfiarE5hIRHibdp6O9iaufA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)可以看到，该方式和上面说过的历史订单迁移的方式是如初一辙的。

## Part5总结

一篇文章不可能穷尽所有知识点，如有遗漏和错误，欢迎补充和指正，原创不易，欢迎转发，留言讨论~

> 高并发系列历史文章目录
>
> 1. 垂直性能提升
>    1.1. [架构优化：集群部署，负载均衡](https://mp.weixin.qq.com/s?__biz=MzA4ODUzMDg5NQ==&mid=2650000954&idx=1&sn=a9ee98310e583b1712e1e64988d2a796&scene=21#wechat_redirect)
>    1.2. [万亿流量下负载均衡的实现](https://mp.weixin.qq.com/s?__biz=MzA4ODUzMDg5NQ==&mid=2650000991&idx=1&sn=4cd73cc5aa4ccb97d9823db82737d14b&scene=21#wechat_redirect)
>    1.3. [架构优化：消息中间件的妙用](https://mp.weixin.qq.com/s?__biz=MzA4ODUzMDg5NQ==&mid=2650001031&idx=1&sn=75b0eea86788b7b59c61875745b38c4c&scene=21#wechat_redirect)
>    1.4. [存储优化：mysql的索引原理和优化](https://mp.weixin.qq.com/s?__biz=MzA4ODUzMDg5NQ==&mid=2650001071&idx=1&sn=fe00cfd25ae6c8595bcc2aef84ed102f&scene=21#wechat_redirect)
>    1.5. 本文:存储优化：详解分库分表

### 参考资料

[1]日均百万订单下的高可用苏宁拼购系统架构设计.朱羿全: *QCon技术峰会分享*

[2]支撑亿级运单的配运平台架构实践.赵玉开: *QCon技术峰会分享*

[[3]大众点评订单系统分库分表实践](https://tech.meituan.com/2016/11/18/dianping-order-db-sharding.html)

[[4]如何做好服务拆分](http://dockone.io/article/8241)

[[5]蚂蚁金融中间件产品介绍](https://tech.antfin.com/docs/2/46921)

[[6]阿里云数据库RDS产品介绍](https://help.aliyun.com/document_detail/161461.html)

![图片](https://mmbiz.qpic.cn/mmbiz_gif/b96CibCt70iabwjyojLhA03PtxUnkNPREnt2F48ywfXLpDdDAjicOTPI8Q94tVLbJ58tbRs12iaXDKhUOW9gd4NlFA/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

---

# [MySQL不会丢失数据的奥秘](https://mp.weixin.qq.com/s/QBeyJz2gVq1p7wBxcY1Gfw)

进入正题前先简单看看MySQL的逻辑架构，相信我用的着。

![image-20220216111155878](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20220216111155878.png)

MySQL逻辑架构

MySQL的逻辑架构大致可以分为三层：

- 第一层：处理客户端连接、授权认证，安全校验等。
- 第二层：服务器`server`层，负责对SQL解释、分析、优化、执行操作引擎等。
- 第三层：存储引擎，负责MySQL中数据的存储和提取。

> 我们要知道MySQL的服务器层是不管理事务的，事务是由存储引擎实现的，而MySQL中支持事务的存储引擎又属`InnoDB`使用的最为广泛，所以后续文中提到的存储引擎都以`InnoDB`为主。

![image-20220216111246720](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20220216111246720.png)

MySQL数据更新流程

**记住！** **记住！** **记住！** 上边这张图，她是MySQL更新数据的基础流程，其中包括`redo log`、`bin log`、`undo log`三种日志间的大致关系，好了闲话少说直奔主题。

### redo log（重做日志）

`redo log`属于MySQL存储引擎`InnoDB`的事务日志。

MySQL的数据是存放在磁盘中的，每次读写数据都需做磁盘IO操作，如果并发场景下性能就会很差。为此MySQL提供了一个优化手段，引入缓存`Buffer Pool`。这个缓存中包含了磁盘中**部分**数据页（`page`）的映射，以此来缓解数据库的磁盘压力。

当从数据库读数据时，首先从缓存中读取，如果缓存中没有，则从磁盘读取后放入缓存；当向数据库写入数据时，先向缓存写入，此时缓存中的数据页数据变更，这个数据页称为**脏页**，`Buffer Pool`中修改完数据后会按照设定的更新策略，定期刷到磁盘中，这个过程称为**刷脏页**。

#### MySQL宕机

如果刷脏页还未完成，可MySQL由于某些原因宕机重启，此时`Buffer Pool`中修改的数据还没有及时的刷到磁盘中，就会导致数据丢失，无法保证事务的持久性。

为了解决这个问题引入了`redo log`，redo Log如其名侧重于重做！它记录的是数据库中每个页的修改，而不是某一行或某几行修改成怎样，可以用来恢复提交后的物理数据页，且只能恢复到最后一次提交的位置。

`redo log`用到了`WAL`（Write-Ahead Logging）技术，这个技术的核心就在于修改记录前，一定要先写日志，并保证日志先落盘，才能算事务提交完成。(写日志不用修改元数据，这个操作很快)

有了redo log再修改数据时，InnoDB引擎会把更新记录先写在redo log中，在修改`Buffer Pool`中的数据，当提交事务时，调用`fsync`把redo log刷入磁盘。至于缓存中更新的数据文件何时刷入磁盘，则由后台线程异步处理。

> **注意**：此时redo log的事务状态是`prepare`，还未真正提交成功，要等`bin log`日志写入磁盘完成才会变更为`commit`，事务才算真正提交完成。

这样一来即使刷脏页之前MySQL意外宕机也没关系，只要在重启时解析redo log中的更改记录进行重放，重新刷盘即可。

#### 大小固定

redo log采用固定大小，循环写入的格式，当redo log写满之后，重新从头开始如此循环写，形成一个环状。

那为什么要如此设计呢？

因为redo log记录的是数据页上的修改，如果`Buffer Pool`中数据页已经刷磁盘后，那这些记录就失效了，新日志会将这些失效的记录进行覆盖擦除。

![image-20220216113355327](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20220216113355327.png)

上图中的`write pos`表示redo log当前记录的日志序列号`LSN`(log sequence number)，写入还未刷盘，循环往后递增；`check point`表示redo log中的修改记录已刷入磁盘后的LSN，循环往后递增，这个LSN之前的数据已经全落盘。

`write pos`到`check point`之间的部分是redo log空余的部分（绿色），用来记录新的日志；`check point`到`write pos`之间是redo log已经记录的数据页修改数据，此时数据页还未刷回磁盘的部分。当`write pos`追上`check point`时，会先推动`check point`向前移动，空出位置（刷盘）再记录新的日志。

- （write pos -> check point）：绿色部分已经刷入磁盘，用来记录新日志
- （check point -> write pos）：浅色部分记录已经修改但未刷入磁盘的数据
- （write pos 追上 check point）：此时 redo log 全是未刷入磁盘的数据，称脏数据。可以说 redo log 已满。此时无法记录新日志，需要先将脏数据刷盘空出位置才能写新日志
- （check point 追上 write pos）：此时 redo log 中的日志全部已刷盘，可以看做 redo log 为空。

> **注意**：redo log日志满了，在擦除之前，需要确保这些要被擦除记录对应在内存中的数据页都已经刷到磁盘中了。擦除旧记录腾出新空间这段期间，是不能再接收新的更新请求的，此刻MySQL的性能会下降。所以在并发量大的情况下，合理调整redo log的文件大小非常重要。

#### crash-safe

因为redo log的存在使得`Innodb`引擎具有了`crash-safe`的能力，即MySQL宕机重启，系统会自动去检查redo log，将修改还未写入磁盘的数据从redo log恢复到MySQL中。

MySQL启动时，不管上次是正常关闭还是异常关闭，总是会进行恢复操作。会先检查数据页中的`LSN`，如果这个 LSN 小于 redo log 中的LSN，即`write pos`位置，说明在`redo log`上记录着数据页上尚未完成的操作，接着就会从最近的一个`check point`出发，开始同步数据。

简单理解，比如：redo log的`LSN`是500，数据页的`LSN`是300，表明重启前有部分数据未完全刷入到磁盘中，那么系统则将redo log中`LSN`序号300到500的记录进行重放刷盘。

![图片](https://mmbiz.qpic.cn/mmbiz_png/0OzaL5uW2aPNRxiblTumvGnwiaqAA5c7b1uxaibx8j0mFBcfk6eV87blhyGDibj1fPo4htdp0b06BGVwZCGUqha9ew/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

### undo log（回滚日志）

`undo log`也是属于MySQL存储引擎InnoDB的事务日志。

`undo log`属于逻辑日志，如其名主要起到回滚的作用，它是保证事务原子性的关键。记录的是数据修改前的状态，在数据修改的流程中，同时会记录一条与当前操作相反的逻辑日志到`undo log`中。

我们举个栗子：假如更新ID=1记录的name字段，name原始数据为小富，现改name为程序员内点事

事务执行`update X set name = 程序员内点事 where id =1`语句时，先会在`undo log`中记录一条相反逻辑的`update X set name = 小富 where id =1`记录，这样当某些原因导致服务异常事务失败，就可以借助`undo log`将数据回滚到事务执行前的状态，保证事务的完整性。

![image-20220216115702404](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20220216115702404.png)

那可能有人会问：同一个事物内的一条记录被多次修改，那是不是每次都要把数据修改前的状态都写入`undo log`呢？

答案是不会的！

`undo log`只负责记录事务开始前要修改数据的原始版本，当我们再次对这行数据进行修改，所产生的修改记录会写入到`redo log`，`undo log`负责完成回滚，`redo log`负责完成前滚。

#### 回滚

未提交的事务，即事务未执行`commit`。但该事务内修改的脏页中，可能有一部分脏块已经刷盘。如果此时数据库实例宕机重启，就需要用回滚来将先前那部分已经刷盘的脏块从磁盘上撤销。

#### 前滚

未完全提交的事务，即事务已经执行`commit`，但该事务内修改的脏页中只有一部分数据被刷盘，另外一部分还在`buffer pool`缓存上，如果此时数据库实例宕机重启，就需要用前滚来完成未完全提交的事务。将先前那部分由于宕机在内存上的未来得及刷盘数据，从`redo log`中恢复出来并刷入磁盘。

> 数据库实例恢复时，先做前滚，后做回滚。

如果你仔细看过了上边的 `MySQL数据更新流程图` 就会发现，`undo log`、`redo log`、`bin log`三种日志都是在刷脏页之前就已经刷到磁盘了的，相互协作最大限度保证了用户提交的数据不丢失。

### bin log（归档日志）

`bin log`是一种数据库Server层（和什么引擎无关），以二进制形式存储在磁盘中的逻辑日志。`bin log`记录了数据库所有`DDL`和`DML`操作（不包含 `SELECT` 和 `SHOW`等命令，因为这类操作对数据本身并没有修改）。

默认情况下，二进制日志功能是关闭的。可以通过以下命令查看二进制日志是否开启：

```sql
mysql> SHOW VARIABLES LIKE 'log_bin';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| log_bin       | OFF   |
+---------------+-------+
```

`bin log`也被叫做`归档日志`，因为它不会像`redo log`那样循环写擦除之前的记录，而是会一直记录日志。一个`bin log`日志文件默认最大容量`1G`（也可以通过`max_binlog_size`参数修改），单个日志超过最大值，则会新创建一个文件继续写。

```sql
mysql> show binary logs;
+-----------------+-----------+
| Log_name        | File_size |
+-----------------+-----------+
| mysq-bin.000001 |      8687 |
| mysq-bin.000002 |      1445 |
| mysq-bin.000003 |      3966 |
| mysq-bin.000004 |       177 |
| mysq-bin.000005 |      6405 |
| mysq-bin.000006 |       177 |
| mysq-bin.000007 |       154 |
| mysq-bin.000008 |       154 |
```

`bin log`日志的内容格式其实就是执行SQL命令的反向逻辑，这点和`undo log`有点类似。一般来说开启`bin log`都会给日志文件设置过期时间（`expire_logs_days`参数，默认永久保存），要不然日志的体量会非常庞大。

```sql
mysql> show variables like 'expire_logs_days';
+------------------+-------+
| Variable_name    | Value |
+------------------+-------+
| expire_logs_days | 0     |
+------------------+-------+
1 row in set

mysql> SET GLOBAL expire_logs_days=30;
Query OK, 0 rows affected
```

`bin log`主要应用于MySQL主从模式（`master-slave`）中，主从节点间的数据同步；以及基于时间点的数据还原。

#### 主从同步

通过下图MySQL的主从复制过程，来了解下`bin log`在主从模式下的应用。

![图片](https://mmbiz.qpic.cn/mmbiz_png/0OzaL5uW2aPNRxiblTumvGnwiaqAA5c7b16YzUk311gZVVNFr20kia8ic4K1YBMfYly6UY6aykyVHEtTP5uYBqOlOg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- 用户在主库`master`执行`DDL`和`DML`操作，修改记录顺序写入`bin log`;
- 从库`slave`的I/O线程连接上Master，并请求读取指定位置`position`的日志内容;
- `Master`收到从库`slave`请求后，将指定位置`position`之后的日志内容，和主库bin log文件的名称以及在日志中的位置推送给从库;
- slave的I/O线程接收到数据后，将接收到的日志内容依次写入到`relay log`文件最末端，并将读取到的主库bin log文件名和位置`position`记录到`master-info`文件中，以便在下一次读取用;
- slave的SQL线程检测到`relay log`中内容更新后，读取日志并解析成可执行的SQL语句，这样就实现了主从库的数据一致;

#### 基于时间点还原

我们看到`bin log`也可以做数据的恢复，而`redo log`也可以，那它们有什么区别？

- 层次不同：redo log 是InnoDB存储引擎实现的，bin log 是MySQL的服务器层实现的，但MySQL数据库中的任何存储引擎对于数据库的更改都会产生bin log。
- 作用不同：redo log 用于碰撞恢复（`crash recovery`），保证MySQL宕机也不会影响持久性；bin log 用于时间点恢复（`point-in-time recovery`），保证服务器可以基于时间点恢复数据和主从复制。
- 内容不同：redo log 是物理日志，内容基于磁盘的页`Page`；bin log的内容是二进制，可以根据`binlog_format`参数自行设置。
- 写入方式不同：redo log 采用循环写的方式记录；bin log 通过追加的方式记录，当文件大小大于给定值后，后续的日志会记录到新的文件上。
- 刷盘时机不同：bin log在事务提交时写入；redo log 在事务开始时即开始写入。

bin log 与 redo log 功能并不冲突而是起到相辅相成的作用，需要二者同时记录，才能保证当数据库发生宕机重启时，数据不会丢失。

### relay log（中继日志）

`relay log`日志文件具有与`bin log`日志文件相同的格式，从上边MySQL主从复制的流程可以看出，`relay log`起到一个中转的作用，`slave`先从主库`master`读取二进制日志数据，写入从库本地，后续再异步由`SQL线程`读取解析`relay log`为对应的SQL命令执行。

### slow query log（慢查询日志）

慢查询日志（`slow query log`）: 用来记录在 MySQL 中执行时间超过指定时间的查询语句，在 SQL 优化过程中会经常使用到。通过慢查询日志，我们可以查找出哪些查询语句的执行效率低，耗时严重。

出于性能方面的考虑，一般只有在排查慢SQL、调试参数时才会开启，默认情况下，慢查询日志功能是关闭的。可以通过以下命令查看是否开启慢查询日志：

```sql
mysql> SHOW VARIABLES LIKE 'slow_query%';
+---------------------+--------------------------------------------------------+
| Variable_name       | Value                                                  |
+---------------------+--------------------------------------------------------+
| slow_query_log      | OFF                                                    |
| slow_query_log_file | /usr/local/mysql/data/iZ2zebfzaequ90bdlz820sZ-slow.log |
+---------------------+--------------------------------------------------------+
```

通过如下命令开启慢查询日志后，我发现 `iZ2zebfzaequ90bdlz820sZ-slow.log` 日志文件里并没有内容啊，可能因为我执行的 SQL 都比较简单没有超过指定时间。

```sql
mysql>  SET GLOBAL slow_query_log=ON;
Query OK, 0 rows affected
```

上边提到超过 `指定时间` 的查询语句才算是慢查询，那么这个时间阈值又是多少嘞？我们通过 `long_query_time` 参数来查看一下，发现默认是 10 秒。

```sql
mysql> SHOW VARIABLES LIKE 'long_query_time';
+-----------------+-----------+
| Variable_name   | Value     |
+-----------------+-----------+
| long_query_time | 10.000000 |
+-----------------+-----------+
```

这里我们将 `long_query_time` 参数改小为 0.001秒再次执行查询SQL，看看慢查询日志里是否有变化。

```sql
mysql> SET GLOBAL long_query_time=0.001;
Query OK, 0 rows affected
```

果然再执行 SQL 的时，执行时间大于 0.001秒，发现慢查询日志开始记录了。

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)慢查询日志

### general query log

一般查询日志（`general query log`）：用来记录用户的**所有**操作，包括客户端何时连接了服务器、客户端发送的所有`SQL`以及其他事件，比如 `MySQL` 服务启动和关闭等等。`MySQL`服务器会按照它接收到语句的先后顺序写入日志文件。

由于一般查询日志记录的内容过于详细，开启后 Log 文件的体量会非常庞大，所以出于对性能的考虑，默认情况下，该日志功能是关闭的，通常会在排查故障需获得详细日志的时候才会临时开启。

我们可以通过以下命令查看一般查询日志是否开启，命令如下：

```sql
mysql> show variables like 'general_log';
+---------------+-------+
| Variable_name | Value |
+---------------+-------+
| general_log   | OFF   |
+---------------+-------+
```

下边开启一般查询日志并查看日志存放的位置。

```sql
mysql> SET GLOBAL general_log=on;
Query OK, 0 rows affected
mysql> show variables like 'general_log_file';
+------------------+---------------------------------------------------+
| Variable_name    | Value                                             |
+------------------+---------------------------------------------------+
| general_log_file | /usr/local/mysql/data/iZ2zebfzaequ90bdlz820sZ.log |
+------------------+---------------------------------------------------+
```

执行一条查询 SQL 看看日志内容的变化。

```sql
mysql> select * from t_config;
+---------------------+------------+---------------------+---------------------+
| id                  | remark     | create_time         | last_modify_time    |
+---------------------+------------+---------------------+---------------------+
| 1325741604307734530 | 我是广播表 | 2020-11-09 18:06:44 | 2020-11-09 18:06:44 |
+---------------------+------------+---------------------+---------------------+
```

我们看到日志内容详细的记录了所有执行的命令、SQL、SQL的解析过程、数据库设置等等。

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)一般查询日志

### error log

错误日志（`error log`）: 应该是 MySQL 中最好理解的一种日志，主要记录 MySQL 服务器每次启动和停止的时间以及诊断和出错信息。

默认情况下，该日志功能是开启的，通过如下命令查找错误日志文件的存放路径。

```sql
mysql> SHOW VARIABLES LIKE 'log_error';
+---------------+----------------------------------------------------------------+
| Variable_name | Value                                                          |
+---------------+----------------------------------------------------------------+
| log_error     | /usr/local/mysql/data/LAPTOP-UHQ6V8KP.err |
+---------------+----------------------------------------------------------------+
```

**注意**：错误日志中记录的可并非全是错误信息，像 MySQL 如何启动 `InnoDB` 的表空间文件、如何初始化自己的存储引擎，初始化 `buffer pool` 等等，这些也记录在错误日志文件中。

![图片](https://mmbiz.qpic.cn/mmbiz_png/0OzaL5uW2aPNRxiblTumvGnwiaqAA5c7b1YiaiaDuFgrINwOoB1Zia9ZKd0UPnjkxYCDpRLh28x9oNcXfViaqhfqpkcg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

### 总结

MySQL作为我们工作中最常接触的中间件，熟练使用只算是入门，如果要在简历写上一笔精通，还需要深入了解其内部工作原理，而这7种日志也只是深入学习过程中的一个起点，学无止境，兄嘚干就完了！

---

# [MySQL主从复制](https://mp.weixin.qq.com/s/CCLsmKSsodtkz4iX84Cdig)

我们在平时工作中，使用最多的数据库就是 `MySQL` 了，随着业务的增加，如果单单靠一台服务器的话，负载过重，就容易造成**宕机**。

这样我们保存在 MySQL 数据库的数据就会丢失，那么该怎么解决呢？

其实在 MySQL 本身就自带有一个主从复制的功能，可以帮助我们实现**`负载均衡`和`读写分离`**。

对于主服务器（Master）来说，主要负责写，从服务器（Slave）主要负责读，这样的话，就会大大减轻压力，从而提高效率。

接下来一起来看看它都有哪些核心知识点呢：

### 简介

随着业务的增长，一台数据服务器已经满足不了需求了，负载过重。这个时候就需要**减压**了，实现负载均衡读写分离，一主一丛或一主多从。

主服务器只负责写，而从服务器只负责读，从而提高了效率减轻压力。

主从复制可以分为：

- 主从同步：当用户写数据主服务器必须和从服务器**同步**了才告诉用户写入成功，等待时间比较长。
- 主从异步：只要用户**访问**写数据主服务器，立即返回给用户。
- 主从半同步：当用户访问写数据主服务器写入并同步**其中一个从服务器**就返回给用户成功。

### 形式

#### 一主一从

![图片](https://mmbiz.qpic.cn/mmbiz_png/Q5xqCucsiaSBPXUmmsriaE4Rukb8aFHmNqvbUkpRbWGkhyMSibluSGGY5wibCVhWmFUGgC52BpBk3KDgicHYBTrDic1w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



#### 一主多从

![图片](https://mmbiz.qpic.cn/mmbiz_png/Q5xqCucsiaSBPXUmmsriaE4Rukb8aFHmNqtntqZdj659UUOKndDUicSngeJdat69HEelylbBDxtwSribuO1VY582JQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



一主一从和一主多从是我们现在见的最多的主从架构，使用起来简单有效，不仅可以实现 `HA`(Highly Available)，而且还能读写分离，进而提升集群的**并发能力**。

#### 多主一从

![图片](https://mmbiz.qpic.cn/mmbiz_png/Q5xqCucsiaSBPXUmmsriaE4Rukb8aFHmNqzbMLEOd0YxtibkjiaERfPoh4JzvqEBeP1PibQXBiaHXLNmVCz3jUgnKFLw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

多主一从

多主一从可以将多个 MySQL 数据库**备份**到一台存储性能比较好的服务器上。

#### 双主复制

![图片](https://mmbiz.qpic.cn/mmbiz_png/Q5xqCucsiaSBPXUmmsriaE4Rukb8aFHmNqicicibd44xVomflO4Aak4flTLibrdSnObeD64B21XARD1AMsEvYuum1LMw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

双主复制

双主复制，也就是可以互做主从复制，每个 master 既是 master，又是另外一台服务器的 salve。这样任何一方所做的变更，都会通过**复制**应用到另外一方的数据库中。

#### 级联复制

![图片](https://mmbiz.qpic.cn/mmbiz_png/Q5xqCucsiaSBPXUmmsriaE4Rukb8aFHmNqziccrCssAicNE6vm4ZASxnYmeD5h6tTosGWpWT4TR6fxPJiaickOnRkDww/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

级联复制

级联复制模式下，部分 slave 的数据同步不连接主节点，而是连接**从节点**。

因为如果主节点有太多的从节点，就会损耗一部分性能用于 replication ，那么我们可以让 3~5 个从节点连接主节点，其它从节点作为二级或者三级与从节点连接，这样不仅可以**缓解**主节点的压力，并且对**数据一致性**没有负面影响。

可以看做一主多从的优化版

### 原理

MySQL 主从复制是基于主服务器在二进制日志（bin log）跟踪所有对数据库的更改。因此，要进行复制，必须在主服务器上启用二进制日志。

每个从服务器从主服务器接收已经记录到日志的数据。当一个从服务器连接到主服务器时，从服务器将自己日志中读取的最后一个更新成功的 位置 通知给主服务器。

从服务器接收从那时发生起的任何更新，并在主机上执行相同的更新。然后封锁等待主服务器通知的更新。

从服务器执行备份不会干扰主服务器，在备份过程中主服务器可以继续处理更新。

### 过程

#### 工作过程

MySQL 的主从复制工作过程大致如下：

1. 从库生成**两个线程**，一个 I/O 线程，一个 SQL 线程；
2. I/O 线程去请求主库的 bin log，并将得到的 bin log 日志**写到** relay log(中继日志) 文件中；
3. 主库会**生成**一个 log dump 线程，用来给从库 I/O 线程传 bin log；
4. SQL 线程会读取 relay log 文件中的日志，并**解析**成具体操作，来实现主从的操作一致，而最终数据一致；

![图片](https://mmbiz.qpic.cn/mmbiz_png/Q5xqCucsiaSDy7NWoml39jQZB9XN6w01S487iatJqqriaeJlgibcquMjlVTl0LqcpjlVwHM4CKmaVgjWbicNcnD9KMA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

工作过程

#### 请求流程

MySQL 建立请求的主从的详细流程如下：

1. 当从服务器连接主服务器时，主服务器会创建一个 log dump 线程，用于发送 binlog 的内容。在读取 binlog 的内容的操作中，会给对象主节点上的 binlog **加锁**，当读取完成并发送给从服务器后解锁。
2. 当从节点上执行 `start slave` 命令之后，从节点会创建一个 IO 线程用来连接主节点，请求主库中**更新** binlog。IO 线程接收主节点 binlog dump 进程发来的更新之后，保存到 relay-log 中。
3. 从节点 SQL 线程负责读取 relay-log 中的内容，**解析**成具体的操作执行，最终保证主从数据的一致性。

### 类型

#### 异步复制

一个主库，一个或多个从库，数据**异步同步**到从库。

![图片](https://mmbiz.qpic.cn/mmbiz_png/Q5xqCucsiaSBPXUmmsriaE4Rukb8aFHmNq1T1jBNWrNTAkXre6qZfW98dCBTVNhAvZ1jzFe5up7ibdAStD1NibciaJw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

异步复制

这种模式下，主节点**不会主动推送数据**到从节点，主库在执行完客户端提交的事务后会立即将结果返给给客户端，并不关心从库是否已经接收并处理。

这样就会有一个问题，主节点如果崩溃掉了，此时主节点上已经提交的事务可能并没有传到从节点上，如果此时，强行将从提升为主，可能导致新主节点上的数据不完整。

#### 同步复制

在 MySQL cluster 中特有的复制方式。

当主库执行完一个事务，然后所有的从库都复制了该事务并**成功执行完**才返回成功信息给客户端。

因为需要等待所有从库执行完该事务才能返回成功信息，所以全同步复制的性能必然会受到严重的影响。

#### 半同步复制

在异步复制的基础上，确保任何一个主库上的事物在提交之前**至少有一个从库**已经收到该事物并日志记录下来。

![图片](https://mmbiz.qpic.cn/mmbiz_png/Q5xqCucsiaSDy7NWoml39jQZB9XN6w01SoowIlGwz2sY1oTdogt9lbLeCYtIFvjPiamppT59chxdicraYBySymaVA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

半同步复制

介于异步复制和全同步复制之间，主库在执行完客户端提交的事务后不是立刻返回给客户端，而是等待**至少一个从库接收到并写到** relay log 中才返回成功信息给客户端(只能保证主库的 binlog 至少传输到了一个从节点上)，否则需要等待直到**超时时间**然后切换成异步模式再提交。

相对于异步复制，半同步复制提高了数据的**安全性**，一定程度的保证了数据能成功备份到从库，同时它也造成了一定程度的延迟，但是比全同步模式延迟要低，这个延迟最少是一个 TCP/IP 往返的时间。所以，半同步复制最好在**低延时**的网络中使用。

半同步模式不是 MySQL 内置的，从 `MySQL 5.5` 开始集成，需要 master 和 slave 安装插件开启半同步模式。

#### 延迟复制

在异步复制的基础上，人为设定主库和从库的数据**同步延迟时间**，即保证数据延迟至少是这个参数。

### 方式

MySQL 主从复制支持两种不同的日志格式，这两种日志格式也对应了各自的复制方式。当然也有二者相结合的混合类型复制。

#### 语句复制

基于语句的复制相当于**逻辑复制**，即二进制日志中记录了操作的语句，通过这些语句在从数据库中重放来实现复制。

这种方式简单，二进制文件小，传输带宽占用小。但是基于语句更新依赖于其它因素，比如插入数据时利用了时间戳。

因此在开发当中，我们应该尽量将业务逻辑逻辑放在**代码层**，而不应该放在 MySQL 中，不易拓展。

*特点*：

- 传输效率高，减少延迟。
- 在从库更新不存在的记录时，语句赋值不会失败。而行复制会导致失败，从而更早发现主从之间的不一致。
- 设表里有一百万条数据，一条sql更新了所有表，基于语句的复制仅需要发送一条sql，而基于行的复制需要发送一百万条更新记录

#### 行数据复制

基于行的复制相当于**物理复制**，即二进制日志中记录的实际更新数据的每一行。

这样导致复制的压力比较大，日志占用的空间大，传输带宽占用大。但是这种方式比基于语句的复制要更加**精确**。

*特点*：

- 不需要执行查询计划。
- 不知道执行的到底是什么语句。
- 例如一条更新用户总积分的语句，需要统计用户的所有积分再写入用户表。如果是基于语句复制的话，从库需要再一次统计用户的积分，而基于行复制就直接更新记录，无需再统计用户积分。

#### 混合类型的复制

一般情况下，默认采用**基于语句**的复制，一旦发现基于语句无法精确复制时，就会采用基于行的复制。

### 配置

配置主要要点如下：

```properties
# 如果在双主复制结构中没有设置ID的话就会导致循环同步问题
server_id=1

# 即日志中记录的是语句(statement)还是行(row)更新或者是混合(mixed)
binlog_format=mixed

# 在进行n次事务提交以后，Mysql将执行一次fsync的磁盘同步指令。将缓冲区数据刷新到磁盘。
# 为0的话由Mysql自己控制频率。
sync_binlog=n

# 为0的话，log buffer将每秒一次地写入log file中并且刷新到磁盘。
# mysqld进程崩溃会丢失一秒内的所有事务。
# 为1的话，每次事务log buffer会写入log file并刷新到磁盘。（较为安全）
# 在崩溃的时候,仅会丢失一个事务。
# 为2的话，每次事务log buffer会写入log file，但一秒一次刷新到磁盘
innodb_flush_logs_at_trx_commit=0

# 阻止从库崩溃后自动启动复制，给一些时间来修复可能的问题，
# 崩溃后再自动复制可能会导致更多的问题。并且本身就是不一致的
skip_slave_start=1 

# 是否将从库同步的事件也记录到从库自身的bin-log中
# 允许备库将重放的事件也记录到自身的二进制日志中去，可以将备库当做另外一台主库的从库
log_slave_update 

# 日志过期删除时间，延迟严重的话会导致日志文件占用磁盘
expire_logs_days=7
```

### 问题

#### 延迟

当主库的 TPS 并发较高的时候，由于主库上面是多线程写入的，而从库的SQL线程是单线程的，导致从库SQL可能会跟不上主库的**处理速度**。

*解决方法*：

- 网络方面：尽量保证主库和从库之间的**网络稳定**，延迟较小；
- 硬件方面：从库**配置更好**的硬件，提升随机写的性能；
- 配置方面：尽量使 MySQL 的操作在**内存中完成**，减少磁盘操作。或升级 MySQL5.7 版本使用并行复制；
- 建构方面：在事务中尽量对**主库读写**，其它非事务的读在从库。消除一部分**延迟**带来的数据库不一致。**增加缓存**降低一些从库的负载。

#### 数据丢失

当主库宕机后，数据可能丢失。

*解决方法*：

使用**半同步**复制，可以解决数据丢失的问题。

### 注意事项

MySQL 需要注意以下事项：

- MySQL 主从复制是 MySQL **高可用性，高性能**（负载均衡）的基础；
- 简单，灵活，部署**方式多样**，可以根据不同业务场景部署不同复制结构；
- 复制过程中应该时刻**监控**复制状态，复制出错或延时可能给系统造成影响；
- MySQL 主从复制目前也存在一些问题，可以根据需要部署**复制增强**功能。

### 作用

主从复制带来了很多好处，当我们的主服务器出现问题，可以**切换**到从服务器；可以进行数据库层面的**读写分离**；可以在从数据库上进行日常**备份**。还可以保证：

1. 数据更安全：做了**数据冗余**，不会因为单台服务器的宕机而丢失数据；
2. 性能大大提升：一主多从，不同用户从不同数据库读取，**性能提升**；
3. 扩展性更优：流量增大时，可以方便的**增加**从服务器，不影响系统使用；
4. 负载均衡：一主多从相当于分担了主机任务，做了**负载均衡**。

### 应用场景

MySQL 主从复制集群功能使得 MySQL 数据库支持**大规模高并发读写**成为可能，同时有效地保护了物理服务器宕机场景的**数据备份**。

#### 横向扩展

将工作负载**分发**到各 Slave 节点上，从而提高系统性能。

在这个场景下，所有的写(write)和更新(update)操作都在 Master 节点上完成；所有的读( read)操作都在 Slave 节点上完成。通过**增加更多**的 Slave 节点，便能提高系统的读取速度。

#### 数据安全

数据从 Master 节点复制到 Slave 节点上，在 Slave 节点上可以**暂停**复制进程。可以在 Slave 节点上**备份**与 Master 节点对应的数据，而不用影响 Master 节点的运行。

#### 数据分析

实时数据可以在 Master 节点上创建，而分析这些数据可以在 Slave 节点上进行，并且不会对 Master 节点的性能产生影响。

#### 远距离数据分布

可以利用复制在远程主机上创建一份本地数据的**副本**，而不用持久的与Master节点连接。

#### 拆分访问

可以把几个不同的从服务器，根据公司的**业务**进行拆分。通过拆分可以帮助减轻主服务器的压力，还可以使数据库对外部用户浏览、内部用户业务处理及 DBA 人员的备份等**互不影响**。

![图片](https://mmbiz.qpic.cn/mmbiz_gif/b96CibCt70iabwjyojLhA03PtxUnkNPREnt2F48ywfXLpDdDAjicOTPI8Q94tVLbJ58tbRs12iaXDKhUOW9gd4NlFA/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

------



# --实践应用--

# [常见的SQL错误（不当）写法例析](https://mp.weixin.qq.com/s/caBYeVtZvNzbSs4q-6710Q)

今天来分享几个**MySQL**常见的SQL错误（不当）用法。我们在作为一个初学者时，很有可能自己在写SQL时也没有注意到这些问题，导致写出来的SQL语句效率低下，所以我们也可以自省自检一下。

## **1. LIMIT 语句**

分页查询是最常用的场景之一，但也通常也是最容易出问题的地方。比如对于下面简单的语句，一般DBA想到的办法是在type, name, create_time字段上加组合索引。这样条件排序都能有效的利用到索引，性能迅速提升。

```sql
SELECT * 
FROM   operation 
WHERE  type = 'SQLStats' 
       AND name = 'SlowLog' 
ORDER  BY create_time 
LIMIT  1000, 10; 
```

好吧，可能90%以上的DBA解决该问题就到此为止。但当 LIMIT 子句变成 “LIMIT 1000000,10” 时，程序员仍然会抱怨：我只取10条记录为什么还是慢？

要知道数据库也并不知道第1000000条记录从什么地方开始，即使有索引也需要从头计算一次。出现这种性能问题，多数情形下是程序员偷懒了。在前端数据浏览翻页，或者大数据分批导出等场景下，是可以将上一页的最大值当成参数作为查询条件的。SQL重新设计如下：

```sql
SELECT   * 
FROM     operation 
WHERE    type = 'SQLStats' 
AND      name = 'SlowLog' 
AND      create_time > '2017-03-16 14:00:00' 
ORDER BY create_time limit 10;
```

在新设计下查询时间基本固定，不会随着数据量的增长而发生变化。

## **2. 隐式转换**

SQL语句中查询变量和字段定义类型不匹配是另一个常见的错误。比如下面的语句：

```sql
mysql> explain extended SELECT * 
     > FROM   my_balance b 
     > WHERE  b.bpn = 14000000123 
     >       AND b.isverified IS NULL ;
mysql> show warnings;
| Warning | 1739 | Cannot use ref access on index 'bpn' due to type or collation conversion on field 'bpn'
```

其中字段bpn的定义为varchar(20)，MySQL的策略是将字符串转换为数字之后再比较。函数作用于表字段，索引失效。

上述情况可能是应用程序框架自动填入的参数，而不是程序员的原意。现在应用框架很多很繁杂，使用方便的同时也小心它可能给自己挖坑。

## **3. 关联更新、删除**

虽然MySQL5.6引入了物化特性，但需要特别注意它目前仅仅针对查询语句的优化。对于更新或删除需要手工重写成JOIN。

比如下面UPDATE语句，MySQL实际执行的是循环/嵌套子查询（DEPENDENT SUBQUERY)，其执行时间可想而知。

```sql
UPDATE operation o 
SET    status = 'applying' 
WHERE  o.id IN (SELECT id 
                FROM   (SELECT o.id, 
                               o.status 
                        FROM   operation o 
                        WHERE  o.group = 123 
                               AND o.status NOT IN ( 'done' ) 
                        ORDER  BY o.parent, 
                                  o.id 
                        LIMIT  1) t); 
```

执行计划：

```sql
+----+--------------------+-------+-------+---------------+---------+---------+-------+------+-----------------------------------------------------+
| id | select_type        | table | type  | possible_keys | key     | key_len | ref   | rows | Extra                                               |
+----+--------------------+-------+-------+---------------+---------+---------+-------+------+-----------------------------------------------------+
| 1  | PRIMARY            | o     | index |               | PRIMARY | 8       |       | 24   | Using where; Using temporary                        |
| 2  | DEPENDENT SUBQUERY |       |       |               |         |         |       |      | Impossible WHERE noticed after reading const tables |
| 3  | DERIVED            | o     | ref   | idx_2,idx_5   | idx_5   | 8       | const | 1    | Using where; Using filesort                         |
+----+--------------------+-------+-------+---------------+---------+---------+-------+------+-----------------------------------------------------+
```

重写为JOIN之后，子查询的选择模式从DEPENDENT SUBQUERY变成DERIVED,执行速度大大加快，从7秒降低到2毫秒。

```sql
UPDATE operation o 
       JOIN  (SELECT o.id, 
                            o.status 
                     FROM   operation o 
                     WHERE  o.group = 123 
                            AND o.status NOT IN ( 'done' ) 
                     ORDER  BY o.parent, 
                               o.id 
                     LIMIT  1) t
         ON o.id = t.id 
SET    status = 'applying' 
```

执行计划简化为：

```sql
+----+-------------+-------+------+---------------+-------+---------+-------+------+-----------------------------------------------------+
| id | select_type | table | type | possible_keys | key   | key_len | ref   | rows | Extra                                               |
+----+-------------+-------+------+---------------+-------+---------+-------+------+-----------------------------------------------------+
| 1  | PRIMARY     |       |      |               |       |         |       |      | Impossible WHERE noticed after reading const tables |
| 2  | DERIVED     | o     | ref  | idx_2,idx_5   | idx_5 | 8       | const | 1    | Using where; Using filesort                         |
+----+-------------+-------+------+---------------+-------+---------+-------+------+-----------------------------------------------------+
```

## **4. 混合排序**

MySQL不能利用索引进行混合排序。但在某些场景，还是有机会使用特殊方法提升性能的。

```sql
SELECT * 
FROM   my_order o 
       INNER JOIN my_appraise a ON a.orderid = o.id 
ORDER  BY a.is_reply ASC, 
          a.appraise_time DESC 
LIMIT  0, 20 
```

执行计划显示为全表扫描：

```sql
+----+-------------+-------+--------+-------------+---------+---------+---------------+---------+-+
| id | select_type | table | type   | possible_keys     | key     | key_len | ref      | rows    | Extra    
+----+-------------+-------+--------+-------------+---------+---------+---------------+---------+-+
|  1 | SIMPLE      | a     | ALL    | idx_orderid | NULL    | NULL    | NULL    | 1967647 | Using filesort |
|  1 | SIMPLE      | o     | eq_ref | PRIMARY     | PRIMARY | 122     | a.orderid |       1 | NULL           |
+----+-------------+-------+--------+---------+---------+---------+-----------------+---------+-+
```

由于is_reply只有0和1两种状态，我们按照下面的方法重写后，执行时间从1.58秒降低到2毫秒。

```sql
SELECT * 
FROM   ((SELECT *
         FROM   my_order o 
                INNER JOIN my_appraise a 
                        ON a.orderid = o.id 
                           AND is_reply = 0 
         ORDER  BY appraise_time DESC 
         LIMIT  0, 20) 
        UNION ALL 
        (SELECT *
         FROM   my_order o 
                INNER JOIN my_appraise a 
                        ON a.orderid = o.id 
                           AND is_reply = 1 
         ORDER  BY appraise_time DESC 
         LIMIT  0, 20)) t 
ORDER  BY  is_reply ASC, 
          appraisetime DESC 
LIMIT  20; 
```

## **5. EXISTS语句**

MySQL对待EXISTS子句时，仍然采用嵌套子查询的执行方式。如下面的SQL语句：

```sql
SELECT *
FROM   my_neighbor n 
       LEFT JOIN my_neighbor_apply sra 
              ON n.id = sra.neighbor_id 
                 AND sra.user_id = 'xxx' 
WHERE  n.topic_status < 4 
       AND EXISTS(SELECT 1 
                  FROM   message_info m 
                  WHERE  n.id = m.neighbor_id 
                         AND m.inuser = 'xxx') 
       AND n.topic_type <> 5 
```

执行计划为：

```sql
+----+--------------------+-------+------+-----+------------------------------------------+---------+-------+---------+ -----+
| id | select_type        | table | type | possible_keys     | key   | key_len | ref   | rows    | Extra   |
+----+--------------------+-------+------+ -----+------------------------------------------+---------+-------+---------+ -----+
|  1 | PRIMARY            | n     | ALL  |  | NULL     | NULL    | NULL  | 1086041 | Using where                   |
|  1 | PRIMARY            | sra   | ref  |  | idx_user_id | 123     | const |       1 | Using where          |
|  2 | DEPENDENT SUBQUERY | m     | ref  |  | idx_message_info   | 122     | const |       1 | Using index condition; Using where |
+----+--------------------+-------+------+ -----+------------------------------------------+---------+-------+---------+ -----+
```

去掉exists更改为join，能够避免嵌套子查询，将执行时间从1.93秒降低为1毫秒。

```sql
SELECT *
FROM   my_neighbor n 
       INNER JOIN message_info m 
               ON n.id = m.neighbor_id 
                  AND m.inuser = 'xxx' 
       LEFT JOIN my_neighbor_apply sra 
              ON n.id = sra.neighbor_id 
                 AND sra.user_id = 'xxx' 
WHERE  n.topic_status < 4 
       AND n.topic_type <> 5 
```

新的执行计划：

```sql
+----+-------------+-------+--------+ -----+------------------------------------------+---------+ -----+------+ -----+
| id | select_type | table | type   | possible_keys     | key       | key_len | ref   | rows | Extra                 |
+----+-------------+-------+--------+ -----+------------------------------------------+---------+ -----+------+ -----+
|  1 | SIMPLE      | m     | ref    | | idx_message_info   | 122     | const    |    1 | Using index condition |
|  1 | SIMPLE      | n     | eq_ref | | PRIMARY   | 122     | ighbor_id |    1 | Using where      |
|  1 | SIMPLE      | sra   | ref    | | idx_user_id | 123     | const     |    1 | Using where           |
+----+-------------+-------+--------+ -----+------------------------------------------+---------+ -----+------+ -----+
```

## **6. 条件下推**

外部查询条件不能够下推到复杂的视图或子查询的情况有：

1. 聚合子查询；
2. 含有LIMIT的子查询；
3. UNION 或UNION ALL子查询；
4. 输出字段中的子查询；

如下面的语句，从执行计划可以看出其条件作用于聚合子查询之后：

```sql
SELECT * 
FROM   (SELECT target, 
               Count(*) 
        FROM   operation 
        GROUP  BY target) t 
WHERE  target = 'rm-xxxx' 
+----+-------------+------------+-------+---------------+-------------+---------+-------+------+-------------+
| id | select_type | table      | type  | possible_keys | key         | key_len | ref   | rows | Extra       |
+----+-------------+------------+-------+---------------+-------------+---------+-------+------+-------------+
|  1 | PRIMARY     | <derived2> | ref   | <auto_key0>   | <auto_key0> | 514     | const |    2 | Using where |
|  2 | DERIVED     | operation  | index | idx_4         | idx_4       | 519     | NULL  |   20 | Using index |
+----+-------------+------------+-------+---------------+-------------+---------+-------+------+-------------+
```

确定从语义上查询条件可以直接下推后，重写如下：

```sql
SELECT target, 
       Count(*) 
FROM   operation 
WHERE  target = 'rm-xxxx' 
GROUP  BY target
```

执行计划变为：

```sql
+----+-------------+-----------+------+---------------+-------+---------+-------+------+--------------------+
| id | select_type | table | type | possible_keys | key | key_len | ref | rows | Extra |
+----+-------------+-----------+------+---------------+-------+---------+-------+------+--------------------+
| 1 | SIMPLE | operation | ref | idx_4 | idx_4 | 514 | const | 1 | Using where; Using index |
+----+-------------+-----------+------+---------------+-------+---------+-------+------+--------------------+
```



## **7. 提前缩小范围**

先上初始SQL语句：

```sql
SELECT * 
FROM   my_order o 
       LEFT JOIN my_userinfo u 
              ON o.uid = u.uid
       LEFT JOIN my_productinfo p 
              ON o.pid = p.pid 
WHERE  ( o.display = 0 ) 
       AND ( o.ostaus = 1 ) 
ORDER  BY o.selltime DESC 
LIMIT  0, 15 
```

该SQL语句原意是：先做一系列的左连接，然后排序取前15条记录。从执行计划也可以看出，最后一步估算排序记录数为90万，时间消耗为12秒。

```sql
+----+-------------+-------+--------+---------------+---------+---------+-----------------+--------+----------------------------------------------------+
| id | select_type | table | type   | possible_keys | key     | key_len | ref             | rows   | Extra                                              |
+----+-------------+-------+--------+---------------+---------+---------+-----------------+--------+----------------------------------------------------+
|  1 | SIMPLE      | o     | ALL    | NULL          | NULL    | NULL    | NULL            | 909119 | Using where; Using temporary; Using filesort       |
|  1 | SIMPLE      | u     | eq_ref | PRIMARY       | PRIMARY | 4       | o.uid |      1 | NULL                                               |
|  1 | SIMPLE      | p     | ALL    | PRIMARY       | NULL    | NULL    | NULL            |      6 | Using where; Using join buffer (Block Nested Loop) |
+----+-------------+-------+--------+---------------+---------+---------+-----------------+--------+----------------------------------------------------+
```

由于最后WHERE条件以及排序均针对最左主表，因此可以先对my_order排序提前缩小数据量再做左连接。SQL重写后如下，执行时间缩小为1毫秒左右。

```sql
SELECT * 
FROM (
SELECT * 
FROM   my_order o 
WHERE  ( o.display = 0 ) 
       AND ( o.ostaus = 1 ) 
ORDER  BY o.selltime DESC 
LIMIT  0, 15
) o 
     LEFT JOIN my_userinfo u 
              ON o.uid = u.uid 
     LEFT JOIN my_productinfo p 
              ON o.pid = p.pid 
ORDER BY  o.selltime DESC
limit 0, 15
```

再检查执行计划：子查询物化后（select_type=DERIVED)参与JOIN。虽然估算行扫描仍然为90万，但是利用了索引以及LIMIT 子句后，实际执行时间变得很小。

```sql
+----+-------------+------------+--------+---------------+---------+---------+-------+--------+----------------------------------------------------+
| id | select_type | table      | type   | possible_keys | key     | key_len | ref   | rows   | Extra                                              |
+----+-------------+------------+--------+---------------+---------+---------+-------+--------+----------------------------------------------------+
|  1 | PRIMARY     | <derived2> | ALL    | NULL          | NULL    | NULL    | NULL  |     15 | Using temporary; Using filesort                    |
|  1 | PRIMARY     | u          | eq_ref | PRIMARY       | PRIMARY | 4       | o.uid |      1 | NULL                                               |
|  1 | PRIMARY     | p          | ALL    | PRIMARY       | NULL    | NULL    | NULL  |      6 | Using where; Using join buffer (Block Nested Loop) |
|  2 | DERIVED     | o          | index  | NULL          | idx_1   | 5       | NULL  | 909112 | Using where                                        |
+----+-------------+------------+--------+---------------+---------+---------+-------+--------+-------
```

---

# [SQL优化的几个角度](https://mp.weixin.qq.com/s/hl11JYMwl30FsDVZ40CLVQ)

大家都在写SQL，但是不同人写出的SQL执行效率却各有不同，这里面的门道也是所有后端开发者的必修课。



所以，在开始之前（MySQL 优化），咱们先来聊聊性能优化的一些原则。

## 性能优化原则和分类

性能优化一般可以分为：

- 主动优化
- 被动优化

所谓的主动优化是指不需要外力的推动而自发进行的一种行为，比如当服务没有明显的卡顿、宕机或者硬件指标异常的情况下，自我出发去优化的行为，就可以称之为主动优化。



而被动优化刚好与主动优化相反，它是指在发现了服务器卡顿、服务异常或者物理指标异常的情况下，才去优化的这种行为。

**性能优化原则**

无论是主动优化还是被动优化都要符合以下性能优化的原则：

1. 优化不能改变服务运行的逻辑，要保证服务的**正确性**；
2. 优化的过程和结果都要保证服务的**安全性**；
3. 要保证服务的**稳定性**，不能为了追求性能牺牲程序的稳定性。比如不能为了提高 Redis 的运行速度，而关闭持久化的功能，因为这样在 Redis 服务器重启或者掉电之后会丢失存储的数据。

![图片](https://mmbiz.qpic.cn/mmbiz_png/HrWw6ZuXCshrFNZFo2b0P7MnibH9iaic0g1aEKjBLBB8cX3TZoiaoXSmVmfkEOSecVNNJwHyJpZdFSB4dTDtnNu0Uw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

以上原则看似都是些废话，但却给了我们一个启发，那就是我们**性能优化手段应该是：预防性能问题为主+被动优化为辅**。

也就是说，我们应该**以预防性能问题为主**，在开发阶段尽可能的规避性能问题，而**在正常情况下，应尽量避免主动优化，以防止未知的风险**（除非是为了 KPI，或者是闲的没事），尤其对生产环境而言更是如此，最后才是考虑**被动优化**。

> PS：当遇到性能缓慢下降、或硬件指标缓慢增加的情况，如今天内存的占用率是 50%，明天是 70%，后天是 90% ，并且丝毫没有收回的迹象时，我们应该提早发现并处理此类问题（这种情况也属于被动优化的一种）。

## MySQL 被动性能优化

所以我们本文会**重点介绍 MySQL 被动性能优化的知识，根据被动性能优化的知识，你就可以得到预防性能问题发生的一些方法，从而规避 MySQL 的性能问题**。

本文我们会从问题入手，然后考虑这个问题产生的原因以及相应的优化方案。我们在实际开发中，通常会遇到以下 3 个问题：

1. **单条 SQL 运行慢；**
2. **部分 SQL 运行慢；**
3. **整个 SQL 运行慢。**

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

###  问题 1：单条 SQL 运行慢

#### 问题分析

造成单条 SQL 运行比较慢的常见原因有以下两个：

1. 未正常创建或使用索引；
2. 表中数据量太大。

#### 解决方案 1：创建并正确使用索引

**索引**是一种能帮助 MySQL 提高查询效率的主要手段，因此一般情况下我们遇到的单条 SQL 性能问题，通常都是由于未创建或未正确使用索引而导致的，所以在遇到单条 SQL 运行比较慢的情况下，你**首先要做的就是检查此表的索引是否正常创建**。

如果表的索引已经创建了，**接下来就要检查一下此 SQL 语句是否正常触发了索引查询**，如果发生以下情况那么 MySQL 将不能正常的使用索引：

1. 在 where 子句中使用 != 或者 <> 操作符，查询引用会放弃索引而进行全表扫描；
2. 不能使用前导模糊查询，也就是 '%XX' 或 '%XX%'，由于前导模糊不能利用索引的顺序，必须一个个去找，看是否满足条件，这样会导致全索引扫描或者全表扫描；
3. 如果条件中有 or 即使其中有条件带索引也不会正常使用索引，要想使用 or 又想让索引生效，只能将 or 条件中的每个列都加上索引才能正常使用；
4. 在 where 子句中对字段进行表达式操作。

**因此你要尽量避免以上情况**，除了正常使用索引之外，我们也可以**使用以下技巧来优化索引的查询速度**：

1. 尽量使用主键查询，而非其他索引，因为主键查询不会触发回表查询；
2. 查询语句尽可能简单，大语句拆小语句，减少锁时间；
3. 尽量使用数字型字段，若只含数值信息的字段尽量不要设计为字符型；
4. 用 exists 替代 in 查询；
5. 避免在索引列上使用 is null 和 is not null。

> 回表查询：普通索引查询到主键索引后，回到主键索引树搜索的过程，我们称为回表查询。

#### 解决方案 2：数据拆分

当表中数据量太大时 SQL 的查询会比较慢，你可以考虑拆分表，让每张表的数据量变小，从而提高查询效率。

##### 1.垂直拆分

指的是将表进行拆分，把一张列比较多的表拆分为多张表。比如，用户表中一些字段经常被访问，将这些字段放在一张表中，另外一些不常用的字段放在另一张表中，插入数据时，使用事务确保两张表的数据一致性。垂直拆分的原则：

- 把不常用的字段单独放在一张表；
- 把 text，blob 等大字段拆分出来放在附表中；
- 经常组合查询的列放在一张表中。

##### 2.水平拆分

指的是将数据表行进行拆分，表的行数超过200万行时，就会变慢，这时可以把一张的表的数据拆成多张表来存放。通常情况下，我们使用取模的方式来进行表的拆分，比如，一张有 400W 的用户表 users，为提高其查询效率我们把其分成 4 张表 users1，users2，users3，users4，然后通过用户 ID 取模的方法，同时查询、更新、删除也是通过取模的方法来操作。

##### 表的其他优化方案：

1. 使用可以存下数据最小的数据类型；
2. 使用简单的数据类型，int 要比 varchar 类型在 MySQL 处理简单；
3. 尽量使用 tinyint、smallint、mediumint 作为整数类型而非 int；
4. 尽可能使用 not null 定义字段，因为 null 占用 4 字节空间；
5. 尽量少用 text 类型，非用不可时最好考虑分表；
6. 尽量使用 timestamp，而非 datetime；
7. 单表不要有太多字段，建议在 20 个字段以内。

### 问题 2：部分 SQL 运行慢

#### 问题分析

部分 SQL 运行比较慢，我们首先要做的就是先定位出这些 SQL，然后再看这些 SQL 是否正确创建并使用索引。也就是说，我们先要使用慢查询工具定位出具体的 SQL，然后再使用问题 1 的解决方案处理慢 SQL。

#### 解决方案：慢查询分析

MySQL 中自带了慢查询日志的功能，开启它就可以用来记录在 MySQL 中响应时间超过阀值的语句，具体指运行时间超过 long_query_time 值的 SQL，则会被记录到慢查询日志中。long_query_time 的默认值为 10，意思是运行 10S 以上的语句。默认情况下，MySQL 数据库并不启动慢查询日志，需要我们手动来设置这个参数，如果不是调优需要的话，一般不建议启动该参数，因为开启慢查询日志会给 MySQL 服务器带来一定的性能影响。慢查询日志支持将日志记录写入文件，也支持将日志记录写入数据库表。使用 `mysql> show variables like '%slow_query_log%';` 来查询慢查询日志是否开启，执行效果如下图所示：![图片](https://mmbiz.qpic.cn/mmbiz_png/HrWw6ZuXCshrFNZFo2b0P7MnibH9iaic0g1SUeUaMDHyIZufwOtE6SGqzia440vMjapNniav4a8TTzQ7tcanZibjj2hA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)slow_query_log 的值为 OFF 时，表示未开启慢查询日志。

##### 开启慢查询日志

开启慢查询日志，可以使用如下 MySQL 命令：

> mysql> set global slow_query_log=1

不过这种设置方式，只对当前数据库生效，如果 MySQL 重启也会失效，如果要永久生效，就必须修改 MySQL 的配置文件 my.cnf，配置如下：

> slow_query_log =1 slow_query_log_file=/tmp/mysql_slow.log

**当你开启慢查询日志之后，所有的慢查询 SQL 都会被记录在 slow_query_log_file 参数配置的文件内，默认是 /tmp/mysql_slow.log 文件**，此时我们就可以打开日志查询到所有慢 SQL 进行逐个优化。

### 问题 3：整个 SQL 运行慢

#### 问题分析

当出现整个 SQL 都运行比较慢就说明目前数据库的承载能力已经到了峰值，因此我们需要使用一些数据库的扩展手段来缓解 MySQL 服务器了。

#### 解决方案：读写分离

一般情况下对数据库而言都是“读多写少”，换言之，数据库的压力多数是因为大量的读取数据的操作造成的，我们可以采用数据库集群的方案，使用一个库作为主库，负责写入数据；其他库为从库，负责读取数据。这样可以缓解对数据库的访问压力。

MySQL 常见的读写分离方案有以下两种：

##### **1.应用层解决方案**

可以通过应用层对数据源做路由来实现读写分离，比如，使用 SpringMVC + MyBatis，可以将 SQL 路由交给 Spring，通过 AOP 或者 Annotation 由代码显示的控制数据源。优点：路由策略的扩展性和可控性较强。缺点：需要在 Spring 中添加耦合控制代码。

[参考](https://www.freesion.com/article/81071194691/)

##### **2.中间件解决方案**

通过 MySQL 的中间件做主从集群，比如：Mysql Proxy、Amoeba、Atlas 等中间件都能符合需求。优点：与应用层解耦。缺点：增加一个服务维护的风险点，性能及稳定性待测试，需要支持代码强制主从和事务。

## 扩展知识：SQL 语句分析

在 MySQL 中我们可以使用 explain 命令来分析 SQL 的执行情况，比如：

> explain select * from t where id=5;

如下图所示：

![图片](https://mmbiz.qpic.cn/mmbiz_png/HrWw6ZuXCshrFNZFo2b0P7MnibH9iaic0g1RWibJ9dic3ciaMKGACJk6BLIicqMIDd75ibCibdcdtmP0Ze849KhiciaNPuRLw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

其中：

- id — 选择标识符，id 越大优先级越高，越先被执行；
- select_type — 表示查询的类型；
- table — 输出结果集的表；
- partitions — 匹配的分区；
- type — 表示表的连接类型；
- possible_keys — 表示查询时，可能使用的索引；
- key — 表示实际使用的索引；
- key_len — 索引字段的长度；
- ref—  列与索引的比较；
- rows — 大概估算的行数；
- filtered — 按表条件过滤的行百分比；
- Extra — 执行情况的描述和说明。

其中最重要的就是 type 字段，type 值类型如下：

- all — 扫描全表数据；
- index — 遍历索引；
- range — 索引范围查找；
- index_subquery — 在子查询中使用 ref；
- unique_subquery — 在子查询中使用 eq_ref；
- ref_or_null — 对 null 进行索引的优化的 ref；
- fulltext — 使用全文索引；
- ref — 使用非唯一索引查找数据；
- eq_ref — 在 join 查询中使用主键或唯一索引关联；
- const — 将一个主键放置到 where 后面作为条件查询， MySQL 优化器就能把这次查询优化转化为一个常量，如何转化以及何时转化，这个取决于优化器，这个比 eq_ref 效率高一点。

## 总结

本文我们介绍了 MySQL 性能优化的原则和分类，MySQL 的性能优化可分为：主动优化和被动优化，但无论何种优化都要保证服务的正确性、安全性和稳定性。它带给我们的启发是应该采用：预防 + 被动优化的方案来确保 MySQL 服务器的稳定性，而被动优化常见的问题是：

- **单条 SQL 运行慢；**
- **部分 SQL 运行慢；**
- **整个 SQL 运行慢。**

---

# [数据库、数据表设计规范例析](https://mp.weixin.qq.com/s/hE2uKE2ffNCmeHLRn2KSTQ)

## 引擎规范

- 非特殊情况下，默认选择Innodb，支持事务、行级锁，并发性能更好。

------

## 编码规范

- UTF-8

------

## 表设计规范

### **必须有主键**

- 主键递增，可提高写入性能，减少碎片

### **禁止使用外键**

- 降低表之间的耦合，不要涉及更新操作的级联，并发高情况极度影响SQL性能

------

## 字段设计规范

### **必须有注释**

- 不然鬼才知道代表什么意思

### **必须NOT NULL**

- null的列不能使用索引

### **整形**

- 默认 int(11) 0。int(11)代表显示长度，在勾选无符号unsigned并且填充零zerofill后如果长度不够11位会自动补零，如插入1，显示00000000001，选择需要为unsigned。

### **字符串**

- 默认空字符串

### **时 间**

- 非current_timstamp（mysql5版本不支持该语法）默认'1970-01-01 08:00:01'，date类型无时分秒

### **通用字段**

- create_time(created_at)：创建时间，默认current_timestamp
- update_time(updated_at)：更新时间，默认current_timestamp，on update current_timestamp
- is_deleted：逻辑删除标志位，视情况选择

### **禁止使用text\blob**

- 浪费磁盘和内存空间，影响数据库性能

### **金额禁止使用小数**

- 尽量使用分或者更小的单位用整数存储，否则精度的问题会很麻烦

------

## 命名规则

### **表、列**

- 使用业务模块开头，如tb_order，列名以下划线分割

### **索引**

- create_time、update_time必须包含索引

### **主键索引**

- 数据库自动

### **唯一索引、组合唯一索引**

- uk_colName_colName

### **普通索引、组合普通索引**

- idx_colName_colName

------

## 建表示例

```sql
CREATE TABLE `user` (
  `id` int unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(128) NOT NULL DEFAULT '' COMMENT '用户名',
  `cert_no` varchar(64) NOT NULL DEFAULT '' COMMENT '身份证号',
  `gender` tinyint NOT NULL DEFAULT '0' COMMENT '性别0女1男',
  `active_date` date NOT NULL DEFAULT '1970-01-01' COMMENT '激活时间',
  `inactive_time` datetime NOT NULL DEFAULT '1970-01-01 08:00:01' COMMENT '失效时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_cert_no` (`cert_no`) USING BTREE,
  KEY `idx_username_gender` (`username`,`gender`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```

---

# [梳理开发中常用的SQL优化途径](https://mp.weixin.qq.com/s/jl0j-T6XldN6Nq-jYoQ-gA)

## **1、大批量插入数据优化**

**（1）对于MyISAM存储引擎的表，可以使用：DISABLE KEYS 和 ENABLE KEYS 用来打开或者关闭 MyISAM 表非唯一索引的更新。**

```sql
ALTER TABLE tbl_name DISABLE KEYS;
loading the data
ALTER TABLE tbl_name ENABLE KEYS;
```

**（2）对于InnoDB引擎，有以下几种优化措施：**

① 导入的数据按照主键的顺序保存：这是因为InnoDB引擎表是按照主键顺序保存的，如果能将插入的数据提前按照排序好自然能省去很多时间。

比如bulk_insert.txt文件是以表user主键的顺序存储的，导入的时间为15.23秒

```sql
mysql> load data infile 'mysql/bulk_insert.txt' into table user;
Query OK, 126732 rows affected (15.23 sec)
Records: 126732 Deleted: 0 Skipped: 0 Warnings: 0
```

没有按照主键排序的话，时间为：26.54秒

```sql
mysql> load data infile 'mysql/bulk_insert.txt' into table user;
Query OK, 126732 rows affected (26.54 sec)
Records: 126732 Deleted: 0 Skipped: 0 Warnings: 0
```

② 导入数据前执行`SET UNIQUE_CHECKS=0`，关闭唯一性校验，带导入之后再打开设置为1：校验会消耗时间，在数据量大的情况下需要考虑。

③ 导入前设置`SET AUTOCOMMIT=0`，关闭自动提交，导入后结束再设置为1：这是因为自动提交会消耗部分时间与资源，虽然消耗不是很大，但是在数据量大的情况下还是得考虑。

## **2、INSERT的优化**

**（1）尽量使用多个值表的 INSERT 语句，这种方式将大大缩减客户端与数据库之间的连接、关闭等消耗。（同一客户的情况下），即：**

```sql
INSERT INTO tablename values(1,2),(1,3),(1,4)
```

实验：插入8条数据到user表中（使用navicat客户端工具）

```sql
insert into user values(1,'test',replace(uuid(),'-',''));
insert into user values(2,'test',replace(uuid(),'-',''));
insert into user values(3,'test',replace(uuid(),'-',''));
insert into user values(4,'test',replace(uuid(),'-',''));
insert into user values(5,'test',replace(uuid(),'-',''));
insert into user values(6,'test',replace(uuid(),'-',''));
insert into user values(7,'test',replace(uuid(),'-',''));
insert into user values(8,'test',replace(uuid(),'-',''));
```

得到反馈：

```sql
[SQL] insert into user values(1,'test',replace(uuid(),'-',''));
受影响的行: 1
时间: 0.033s
[SQL] 
insert into user values(2,'test',replace(uuid(),'-',''));
受影响的行: 1
时间: 0.034s
[SQL] 
insert into user values(3,'test',replace(uuid(),'-',''));
受影响的行: 1
时间: 0.056s
[SQL] 
insert into user values(4,'test',replace(uuid(),'-',''));
受影响的行: 1
时间: 0.008s
[SQL] 
insert into user values(5,'test',replace(uuid(),'-',''));
受影响的行: 1
时间: 0.008s
[SQL] 
insert into user values(6,'test',replace(uuid(),'-',''));
受影响的行: 1
时间: 0.024s
[SQL] 
insert into user values(7,'test',replace(uuid(),'-',''));
受影响的行: 1
时间: 0.004s
[SQL] 
insert into user values(8,'test',replace(uuid(),'-',''));
受影响的行: 1
时间: 0.004s
```

总共的时间为0.171秒，接下来使用多值表形式：

```sql
insert into user values
(9,'test',replace(uuid(),'-','')),
(10,'test',replace(uuid(),'-','')),
(11,'test',replace(uuid(),'-','')),
(12,'test',replace(uuid(),'-','')),
(13,'test',replace(uuid(),'-','')),
(14,'test',replace(uuid(),'-','')),
(15,'test',replace(uuid(),'-','')),
(16,'test',replace(uuid(),'-',''));
```

得到反馈：

```sql
[SQL] insert into user values
(9,'test',replace(uuid(),'-','')),
(10,'test',replace(uuid(),'-','')),
(11,'test',replace(uuid(),'-','')),
(12,'test',replace(uuid(),'-','')),
(13,'test',replace(uuid(),'-','')),
(14,'test',replace(uuid(),'-','')),
(15,'test',replace(uuid(),'-','')),
(16,'test',replace(uuid(),'-',''));
受影响的行: 8
时间: 0.038s
```

得到时间为0.038，这样一来可以很明显节约时间优化SQL

（2）如果在不同客户端插入很多行，可使用`INSERT DELAYED`语句得到更高的速度，DELLAYED 含义是让 INSERT 语句马上执行，其实数据都被放在内存的队列中。并没有真正写入磁盘。`LOW_PRIORITY`刚好相反。

（3）将索引文件和数据文件分在不同的磁盘上存放（InnoDB引擎是在同一个表空间的）。

**（4）如果批量插入，则可以增加`bluk_insert_buffer_size`变量值提供速度（只对MyISAM有用）**

（5）当从一个文本文件装载一个表时，使用`LOAD DATA INFILE`，通常比INSERT语句快20倍。

## 3、GROUP BY的优化

在默认情况下，MySQL中的GROUP BY语句会对其后出现的字段进行默认排序（非主键情况），就好比我们使用ORDER BY col1,col2,col3…所以我们在后面跟上具有相同列（与GROUP BY后出现的col1,col2,col3…相同）ORDER BY子句并没有影响该SQL的实际执行性能。

那么就会有这样的情况出现，我们对查询到的结果是否已经排序不在乎时，可以使用`ORDER BY NULL`禁止排序达到优化目的。下面使用EXPLAIN命令分析SQL。Java知音公众号内回复“面试题聚合”，送你一份面试题宝典

在user_1中执行 select id, sum(money) form user_1 group by name时，会默认排序（注意group by后的column是非index才会体现group by的排序，如果是primary key，那之前说过了InnoDB默认是按照主键index排好序的）

```sql
mysql> select*from user_1;
+----+----------+-------+
| id | name     | money |
+----+----------+-------+
|  1 | Zhangsan |    32 |
|  2 | Lisi     |    65 |
|  3 | Wangwu   |    44 |
|  4 | Lijian   |   100 |
+----+----------+-------+
4 rows in set
```

不禁止排序，即不使用ORDER BY NULL时：有明显的Using filesort。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzqaRxfFWKLa7TbWPYZFOPS1gh7bg4gFsLpa2aoLWibQ1Ea0S4ZckUlOE0iaWhOjoPF7JauibnnyF42jA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

当使用ORDER BY NULL禁止排序后，Using filesort不存在

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzqaRxfFWKLa7TbWPYZFOPS1OwArr58jaRo1Y1z3rb0UvVeTSTbyYibx3p3belv57zEjN4j9LVV4rtQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

## **4、ORDER BY 的优化**　　

MySQL可以使用一个索引来满足ORDER BY 子句的排序，而不需要额外的排序，但是需要满足以下几个条件：

（1）WHERE 条件和OREDR BY 使用相同的索引：即key_part1与key_part2是复合索引，where中使用复合索引中的key_part1

```sql
SELECT*FROM user WHERE key_part1=1 ORDER BY key_part1 DESC, key_part2 DESC;
```

（2）而且ORDER BY顺序和索引顺序相同：

```sql
SELECT*FROM user ORDER BY key_part1, key_part2;
```

（3）并且要么都是升序要么都是降序：

```sql
SELECT*FROM user ORDER BY key_part1 DESC, key_part2 DESC;
```

但以下几种情况则不使用索引：

（1）ORDER BY中混合ASC和DESC：

```sql
SELECT*FROM user ORDER BY key_part1 DESC, key_part2 ASC;
```

（2）查询行的关键字与ORDER BY所使用的不相同，即WHERE 后的字段与ORDER BY 后的字段是不一样的

```sql
SELECT*FROM user WHERE key2 = ‘xxx’ ORDER BY key1;
```

（3）ORDER BY对不同的关键字使用，即ORDER BY后的关键字不相同

```sql
SELECT*FROM user ORDER BY key1, key2;
```

## **5、OR的优化**

当MySQL使用OR查询时，如果要利用索引的话，必须每个条件列都使独立索引，而不是复合索引（多列索引），才能保证使用到查询的时候使用到索引。

比如我们新建一张用户信息表user_info

```sql
mysql> select*from user_info;
+---------+--------+----------+-----------+
| user_id | idcard | name     | address    |
+---------+--------+----------+-----------+
|       1 | 111111 | Zhangsan | Kunming   |
|       2 | 222222 | Lisi     | Beijing   |
|       3 | 333333 | Wangwu   | Shanghai  |
|       4 | 444444 | Lijian   | Guangzhou |
+---------+--------+----------+-----------+
4 rows in set
```

之后创建ind_name_id(user_id, name)复合索引、id_index(user_id)独立索引，idcard主键索引三个索引。

```sql
mysql> show index from user_info;
+-----------+------------+-------------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+
| Table     | Non_unique | Key_name    | Seq_in_index | Column_name | Collation | Cardinality | Sub_part | Packed | Null | Index_type | Comment | Index_comment |
+-----------+------------+-------------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+
| user_info |          0 | PRIMARY     |            1 | idcard      | A         |           4 | NULL     | NULL   |      | BTREE      |         |               |
| user_info |          1 | ind_name_id |            1 | user_id     | A         |           4 | NULL     | NULL   |      | BTREE      |         |               |
| user_info |          1 | ind_name_id |            2 | name        | A         |           4 | NULL     | NULL   | YES  | BTREE      |         |               |
| user_info |          1 | id_index    |            1 | user_id     | A         |           4 | NULL     | NULL   |      | BTREE      |         |               |
+-----------+------------+-------------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+
4 rows in set
```

测试一：OR连接两个有单独索引的字段，整个SQL查询才会用到索引(index_merge)，并且我们知道OR实际上是把每个结果最后UNION一起的。

```sql
mysql> explain select*from user_info where user_id=1 or idcard='222222';
+----+-------------+-----------+------------+-------------+------------------------------+---------------------+---------+------+------+----------+----------------------------------------------------+
| id | select_type | table     | partitions | type        | possible_keys                | key                 | key_len | ref  | rows | filtered | Extra                                              |
+----+-------------+-----------+------------+-------------+------------------------------+---------------------+---------+------+------+----------+----------------------------------------------------+
|  1 | SIMPLE      | user_info | NULL       | index_merge | PRIMARY,ind_name_id,id_index | ind_name_id,PRIMARY | 4,62    | NULL |    2 |      100 | Using sort_union(ind_name_id,PRIMARY); Using where |
+----+-------------+-----------+------------+-------------+------------------------------+---------------------+---------+------+------+----------+----------------------------------------------------+
1 row in set
```

测试二：OR使用复合索引的字段name，与没有索引的address，整个SQL都是ALL全表扫描的

```sql
mysql> explain select*from user_info where name='Zhangsan' or address='Beijing';
+----+-------------+-----------+------------+------+---------------+------+---------+------+------+----------+-------------+
| id | select_type | table     | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra       |
+----+-------------+-----------+------------+------+---------------+------+---------+------+------+----------+-------------+
|  1 | SIMPLE      | user_info | NULL       | ALL  | NULL          | NULL | NULL    | NULL |    4 |    43.75 | Using where |
+----+-------------+-----------+------------+------+---------------+------+---------+------+------+----------+-------------+
1 row in set
```

交换OR位置并且使用另外的复合索引的列，也是ALL全表扫描：

```sql
mysql> explain select*from user_info where address='Beijing' or user_id=1;
+----+-------------+-----------+------------+------+----------------------+------+---------+------+------+----------+-------------+
| id | select_type | table     | partitions | type | possible_keys        | key  | key_len | ref  | rows | filtered | Extra       |
+----+-------------+-----------+------------+------+----------------------+------+---------+------+------+----------+-------------+
|  1 | SIMPLE      | user_info | NULL       | ALL  | ind_name_id,id_index | NULL | NULL    | NULL |    4 |    43.75 | Using where |
+----+-------------+-----------+------------+------+----------------------+------+---------+------+------+----------+-------------+
1 row in set
```

## **6、优化嵌套查询**

使用嵌套查询有时候可以使用更有效的JOIN连接代替，这是因为MySQL中不需要在内存中创建临时表完成SELECT子查询与主查询两部分查询工作。但是并不是所有的时候都成立，最好是在on关键字后面的列有索引的话，效果会更好！

比如在表major中major_id是有索引的：

```sql
select * from student u left join major m on u.major_id=m.major_id where m.major_id is null;
```

而通过嵌套查询时，在内存中创建临时表完成SELECT子查询与主查询两部分查询工作，会有一定的消耗

```sql
select * from student u where major_id not in (select major_id from major);
```

## 7、使用SQL提示

SQL提示（SQL HINT）是优化数据库的一个重要手段，就是往SQL语句中加入一些人为的提示来达到优化目的。下面是一些常用的SQL提示：

### （1）USE INDEX

**使用USE INDEX是希望MySQL去参考索引列表，就可以让MySQL不需要考虑其他可用索引，其实也就是possible_keys属性下参考的索引值**

```sql
mysql> explain select* from user_info use index(id_index,ind_name_id) where user_id>0;
+----+-------------+-----------+------------+------+----------------------+------+---------+------+------+----------+-------------+
| id | select_type | table     | partitions | type | possible_keys        | key  | key_len | ref  | rows | filtered | Extra       |
+----+-------------+-----------+------------+------+----------------------+------+---------+------+------+----------+-------------+
|  1 | SIMPLE      | user_info | NULL       | ALL  | ind_name_id,id_index | NULL | NULL    | NULL |    4 |      100 | Using where |
+----+-------------+-----------+------------+------+----------------------+------+---------+------+------+----------+-------------+
1 row in set

mysql> explain select* from user_info use index(id_index) where user_id>0;
+----+-------------+-----------+------------+------+---------------+------+---------+------+------+----------+-------------+
| id | select_type | table     | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra       |
+----+-------------+-----------+------------+------+---------------+------+---------+------+------+----------+-------------+
|  1 | SIMPLE      | user_info | NULL       | ALL  | id_index      | NULL | NULL    | NULL |    4 |      100 | Using where |
+----+-------------+-----------+------------+------+---------------+------+---------+------+------+----------+-------------+
1 row in set
```

### （2）IGNORE INDEX忽略索引

我们使用user_id判断，用不到其他索引时，可以忽略索引。即与USE INDEX相反，从possible_keys中减去不需要的索引，但是实际环境中很少使用。

```sql
mysql> explain select* from user_info ignore index(primary,ind_name_id,id_index) where user_id>0;
+----+-------------+-----------+------------+------+---------------+------+---------+------+------+----------+-------------+
| id | select_type | table     | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra       |
+----+-------------+-----------+------------+------+---------------+------+---------+------+------+----------+-------------+
|  1 | SIMPLE      | user_info | NULL       | ALL  | NULL          | NULL | NULL    | NULL |    4 |    33.33 | Using where |
+----+-------------+-----------+------------+------+---------------+------+---------+------+------+----------+-------------+
1 row in set
```

### （3）FORCE INDEX强制索引

比如where user_id > 0，但是user_id在表中都是大于0的，自然就会进行ALL全表搜索，但是使用FORCE INDEX虽然执行效率不是最高（where user_id > 0条件决定的）但MySQL还是使用索引。

```sql
mysql> explain select* from user_info where user_id>0;
+----+-------------+-----------+------------+------+----------------------+------+---------+------+------+----------+-------------+
| id | select_type | table     | partitions | type | possible_keys        | key  | key_len | ref  | rows | filtered | Extra       |
+----+-------------+-----------+------------+------+----------------------+------+---------+------+------+----------+-------------+
|  1 | SIMPLE      | user_info | NULL       | ALL  | ind_name_id,id_index | NULL | NULL    | NULL |    4 |      100 | Using where |
+----+-------------+-----------+------------+------+----------------------+------+---------+------+------+----------+-------------+
1 row in set
```

之后强制使用独立索引id_index(user_id)：

```sql
mysql> explain select* from user_info force index(id_index) where user_id>0;
+----+-------------+-----------+------------+-------+---------------+----------+---------+------+------+----------+-----------------------+
| id | select_type | table     | partitions | type  | possible_keys | key      | key_len | ref  | rows | filtered | Extra                 |
+----+-------------+-----------+------------+-------+---------------+----------+---------+------+------+----------+-----------------------+
|  1 | SIMPLE      | user_info | NULL       | range | id_index      | id_index | 4       | NULL |    4 |      100 | Using index condition |
+----+-------------+-----------+------------+-------+---------------+----------+---------+------+------+----------+-----------------------+
1 row in set
```

## **总 结**

（1）很多时候数据库的性能是由于不合适（是指效率不高，可能会导致锁表等）的SQL语句造成，本篇博文只是介绍简单的SQL优化

（2）其中有些优化在真正开发中是用不到的，但是一旦出问题性能下降的时候需要去一一分析。

---

# [先更新数据库还是先更新缓存？](https://mp.weixin.qq.com/s/SPgtpfgv6bz2AfPa1CYYeQ)

**在大型系统中，为了减少数据库压力**通常会引入缓存机制，一旦引入缓存又很容易造成缓存和数据库数据不一致，导致用户看到的是旧数据。



为了减少数据不一致的情况，更新缓存和数据库的机制显得尤为重要，接下来带领大家踩踩坑。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoXIeA7IYD5r2u1MJ05slAFwFPJJkgmlZeIwaDXBA4Eh6LMqaqw7A9kHRicSMkv98iceicY2ZeKyic3lA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

## Cache aside

`Cache aside`也就是`旁路缓存`，是比较常用的缓存策略。

**（1）`读请求`常见流程**

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoXIeA7IYD5r2u1MJ05slAFPWMwxFSBkghia2ZAuSJc4Qn3OT5lOfMI9pnHD3sBfdSwleWXYeeMsog/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)Cache aside 读请求

应用首先会判断缓存是否有该数据，缓存命中直接返回数据，缓存未命中即缓存穿透到数据库，从数据库查询数据然后回写到缓存中，最后返回数据给客户端。

**（2）`写请求`常见流程**

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoXIeA7IYD5r2u1MJ05slAF2lGLiayjgSbEnCib7syicbBJauHYcJlIQqiaB5uTL20G340tESBnibiaLNWg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

Cache aside 写请求

首先更新数据库，然后从缓存中删除该数据。

看了写请求的图之后，有些同学可能要问了：为什么要删除缓存，直接更新不就行了？这里涉及到几个坑，我们一步一步踩下去。

## Cache aside踩坑

Cache aside策略如果用错就会遇到深坑，下面我们来逐个踩。

### **踩坑一：先更新数据库，再更新缓存**

如果同时有两个`写请求`需要更新数据，每个写请求都先更新数据库再更新缓存，在并发场景可能会出现数据不一致的情况。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoXIeA7IYD5r2u1MJ05slAFrlsyFMmGndYAfhyxhXdOg71Bibib5r2uVzT20qdJEXQticfZzPVcMLdzQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)先更新数据库，再更新缓存

如上图的执行过程：

（1）`写请求1`更新数据库，将 age 字段更新为18；

（2）`写请求2`更新数据库，将 age 字段更新为20；

（3）`写请求2`更新缓存，缓存 age 设置为20；

（4）`写请求1`更新缓存，缓存 age 设置为18；

执行完预期结果是数据库 age 为20，缓存 age 为20，结果缓存 age为18，这就造成了缓存数据不是最新的，出现了脏数据。

### **踩坑二：先删缓存，再更新数据库**

如果`写请求`的处理流程是`先删缓存再更新数据库`，在一个`读请求`和一个`写请求`并发场景下可能会出现数据不一致情况。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoXIeA7IYD5r2u1MJ05slAFFdZtySHvQskJp18BB8kGmvse75Q8c3prmeRhuyZBhCOyqJbdo0aomw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)先删缓存，再更新数据库

如上图的执行过程：

（1）`写请求`删除缓存数据；

（2）`读请求`查询缓存未击中(Hit Miss)，紧接着查询数据库，将返回的数据回写到缓存中；

（3）`写请求`更新数据库。

整个流程下来发现`数据库`中age为20，`缓存`中age为18，缓存和数据库数据不一致，缓存出现了脏数据。

### **踩坑三：先更新数据库，再删除缓存**

在实际的系统中针对`写请求`还是推荐`先更新数据库再删除缓存`，但是在理论上还是存在问题，以下面这个例子说明。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoXIeA7IYD5r2u1MJ05slAFmh4AT070YGbC9w9OstZkvyL5eDddFe2lstDIofPxTmHJNel6wGaH3w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)先更新数据库，再删除缓存

如上图的执行过程：

（1）`读请求`先查询缓存，缓存未击中，查询数据库返回数据；

（2）`写请求`更新数据库，删除缓存；

（3）`读请求`回写缓存；

整个流程操作下来发现`数据库age为20`，`缓存age为18`，即数据库与缓存不一致，导致应用程序从缓存中读到的数据都为旧数据。

但我们仔细想一下，上述问题发生的概率其实非常低，因为通常数据库更新操作比内存操作耗时多出几个数量级，上图中最后一步回写缓存（set age 18）速度非常快，通常会在更新数据库之前完成。

如果这种极端场景出现了怎么办？我们得想一个兜底的办法：`缓存数据设置过期时间`。通常在系统中是可以允许少量的数据短时间不一致的场景出现。

## Read through

在 Cache Aside 更新模式中，应用代码需要维护两个数据源头：一个是缓存，一个是数据库。而在 `Read-Through` 策略下，应用程序无需管理缓存和数据库，只需要将数据库的同步委托给缓存提供程序 `Cache Provider` 即可。所有数据交互都是通过`抽象缓存层`完成的。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoXIeA7IYD5r2u1MJ05slAFlsqbMtiaPsGAd3ZKjQkGuY0tLdwNhsqnlWnuvdNciaBAppsy6N4tQ1jA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)Read-Through流程（图中Cache Hit有问题)



如上图，应用程序只需要与`Cache Provider`交互，不用关心是从缓存取还是数据库。

在进行大量读取时，`Read-Through` 可以减少数据源上的负载，也对缓存服务的故障具备一定的弹性。如果缓存服务挂了，则缓存提供程序仍然可以通过直接转到数据源来进行操作。

`Read-Through 适用于多次请求相同数据的场景`，这与 Cache-Aside 策略非常相似，但是二者还是存在一些差别，这里再次强调一下：

- 在 Cache-Aside 中，应用程序负责从数据源中获取数据并更新到缓存。
- 在 Read-Through 中，此逻辑通常是由独立的缓存提供程序（Cache Provider）支持。

## Write through

`Write-Through` 策略下，当发生数据更新(Write)时，缓存提供程序 `Cache Provider` 负责更新底层数据源和缓存。

缓存与数据源保持一致，并且写入时始终通过`抽象缓存层`到达数据源。

`Cache Provider`类似一个代理的作用。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoXIeA7IYD5r2u1MJ05slAFtA9QgNpctBBam30l7ib9x5f6LD5hL7Z5THbaB0eCR4PX9nVKicVg7cfg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)Write-Through流程

## Write behind

`Write behind`在一些地方也被称为`Write back`， 简单理解就是：应用程序更新数据时只更新缓存， `Cache Provider`每隔一段时间将数据刷新到数据库中。说白了就是`延迟写入`。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoXIeA7IYD5r2u1MJ05slAFjZKUTJyibXRED8Qx1bToJf8401GNxhN3VdrydgHVFaPvQgibribkfic8sA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)Write behind流程

如上图，应用程序更新两个数据，Cache Provider 会立即写入缓存中，但是隔一段时间才会批量写入数据库中。

这种方式有优点也有缺点：

- `优点`是数据写入速度非常快，适用于频繁写的场景。
- `缺点`是缓存和数据库不是强一致性，对一致性要求高的系统慎用。

## 总结一下

学了这么多，相信大家对缓存更新的策略都已经有了清晰的认识。最后稍稍总结一下。

缓存更新的策略主要分为三种：

- Cache aside
- Read/Write through
- Write behind

Cache aside 通常会先更新数据库，然后再删除缓存，为了兜底通常还会将数据设置缓存时间。

Read/Write through 一般是由一个 Cache Provider 对外提供读写操作，应用程序不用感知操作的是缓存还是数据库。

Write behind简单理解就是延迟写入，Cache Provider 每隔一段时间会批量输入数据库，优点是应用程序写入速度非常快。

---

# [百亿级数据分表后如何分页查](https://mp.weixin.qq.com/s/EplL3kBx5vOXGDhDOP8NjQ)

**当业务规模达到一定规模之后**，像淘宝日订单量在5000万单以上，美团3000万单以上。数据库面对海量的数据压力，分库分表就是必须进行的操作了。而分库分表之后一些常规的查询可能都会产生问题，最常见的就是比如分页查询的问题。一般我们把分表的字段称作shardingkey，比如订单表按照用户ID作为shardingkey，那么如果查询条件中不带用户ID查询怎么做分页？又比如更多的多维度的查询都没有shardingkey又怎么查询？

了解：shardingkey按照一定规则进行印射（一般是取模/hash取模），会落入不同的库表中，因此可以据此规则判断出shardingkey归属与哪个库表中。

### 唯一主键

一般我们数据库的主键都是自增的，那么分表之后主键冲突的问题就是一个无法避免的问题，最简单的办法就是以一个唯一的业务字段作为唯一的主键，比如订单表的订单号肯定是全局唯一的。

常见的分布式生成唯一ID的方式很多，最常见的雪花算法Snowflake、滴滴Tinyid、美团Leaf。以雪花算法举例来说，一毫秒可以生成**4194304**多个ID。

**第一位**不使用，默认都是0，**41位时间戳**精确到毫秒，可以容纳69年的时间，**10位工作机器ID**高5位是数据中心ID，低5位是节点ID，**12位序列号**每个节点每毫秒累加，累计可以达到2^12 4096个ID。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUkoxQEjfWWJx3DVBvyz75Mia9eYcWRjyq5QcY4eVgXbq58yziaH0COyAkxP610CdluSA4SCibiaia01wjg/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

### 分表

第一步，分表后要怎么保证订单号的唯一搞定了，现在考虑下分表的问题。首先根据自身的业务量和增量来考虑分表的大小。

举个例子，现在我们日单量是10万单，预估一年后可以达到日100万单，根据业务属性，一般我们就支持查询半年内的订单，超过半年的订单需要做归档处理。

那么以日订单100万半年的数量级来看，不分表的话我们订单量将达到100万X180=1.8亿，以这个数据量级部分表的话肯定单表是扛不住的，就算你能扛RT的时间你也根本无法接受吧。根据经验单表几百万的数量对于数据库是没什么压力的，那么只要分256张表就足够了，1.8亿/256≈70万，如果为了保险起见，也可以分到512张表。那么考虑一下，如果业务量再增长10倍达到1000万单每天，分表1024就是比较合适的选择。

通过分表加上超过半年的数据归档之后，单表70万的数据就足以应对大部分场景了。接下来对订单号hash，然后对256取模的就可以落到具体的哪张表了。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUkoxQEjfWWJx3DVBvyz75MiaYlibtHAa0wW1rCcbMXZVg7sssVBnUficnWvNJC8cEnuvibv6yicGUsZGAA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

那么，因为唯一主键都是以订单号作为依据，以前你写的那些根据主键ID做查询的就不能用了，这就涉及到了历史一些查询功能的修改。不过这都不是事儿对吧，都改成以订单号来查就行了。这都不是问题，问题在我们的标题说的点上。

### C端查询

说了半天，总算到了正题了，那么分表之后查询和分页查询的问题怎么解决？

首先说带shardingkey的查询，比如就通过订单号查询，不管你分页还是怎么样都是能直接定位到具体的表来查询的，显然查询是不会有什么问题的。

如果不是shardingkey的话，上面举例说的以订单号作为shardingkey的话，像APP、小程序这种一般都是通过用户ID查询，那这时候我们通过订单号做的sharding怎么办？很多公司订单表直接用用户ID做shardingkey，那么很简单，直接查就完了。那么订单号怎么办，一个很简单的办法就是在订单号上带上用户ID的属性。举个很简单的例子，原本41位的时间戳你觉得用不完，用户ID是10位的，订单号的生成规则带上用户ID，落具体表的时候根据订单号中10位用户ID hash取模，这样无论根据订单号还是用户ID查询效果都是一样的。

*当然，这种方式只是举例，具体的订单号生成的规则，多少位，包含哪些因素根据自己的业务和实现机制来决定。*

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUkoxQEjfWWJx3DVBvyz75MiajPJhOUMkialLwd3eLicia91wLumjJ3ibxDCEBGySbY9q0swxDznQ40entw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

好，那么无论你是订单号还是用户ID作为shardingkey，按照以上的两种方式都可以解决问题了。那么还有一个问题就是如果既不是订单号又不是用户ID查询怎么办？最直观的例子就是来自商户端或者后台的查询，商户端都是以商户或者说卖家的ID作为查询条件来查的，后台的查询条件可能就更复杂了，像我碰到的有些后台查询条件能有几十个，这怎么查？？？别急，接下来分开说B端和后台的复杂查询。

现实中真正的流量大头都是来自于用户端C端，所以本质上解决了用户端的问题，这个问题就解了大半，剩下来自商户卖家端B端、后台支持运营业务的查询流量并不会很大，这个问题就好解。

### 其他端查询

针对B端的非shardingkey的查询有两个办法解决。

**`双写`**，双写就是下单的数据落两份，C端和B端的各自保存一份，C端用你可以用单号、用户ID做shardingkey都行，B端就用商家卖家的ID作为shardingkey就好了。有些同学会说了，你双写不影响性能吗？因为对于B端来说轻微的延迟是可以接受的，所以可以采取异步的方式去落B端订单。你想想你去淘宝买个东西下单了，卖家稍微延迟个一两秒收到这个订单的消息有什么关系吗？你点个外卖商户晚一两秒收到这个订单有什么太大影响吗？

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUkoxQEjfWWJx3DVBvyz75Mialc7ZTJ9WZUb9iarrRTeibt8DH9qfLuBic3ESLCKDl59gHsLNTXAV0iaZmA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这是一个解决方案，另外一个方案就是走**离线数仓或者ES**查询，订单数据落库之后，不管你通过binlog还是MQ消息的都形式，把数据同步到数仓或者ES，他们支持的数量级对于这种查询条件来说就很简单了。同样这种方式肯定是稍微有延迟的，但是这种可控范围的延迟是可以接受的。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUkoxQEjfWWJx3DVBvyz75MiamRs05kkmVEToY8gzhGia1dicJz5WUTmHRL5crSTRn9DguyzZIEVpyPfA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

而针对管理后台的查询，比如运营、业务、产品需要看数据，他们天然需要复杂的查询条件，同样走ES或者数仓都可以做得到。如果不用这个方案，又要不带shardingkey的分页查询，兄弟，这就只能扫全表查询聚合数据，然后手动做分页了，但是这样查出来的结果是有限制的。

比如你256个片，查询的时候循环扫描所有的分片，每个片取20条数据，最后聚合数据手工分页，那必然是不可能查到全量的数据的。

### 总结

分库分表后的查询问题，对于有经验的同学来说其实这个问题都知道，但是我相信其实大部分同学做的业务可能都没来到这个数量级，分库分表可能都停留在概念阶段，面试被问到后就手足无措了，因为没有经验不知道怎么办。

分库分表首先是基于现有的业务量和未来的增量做出判断，比如拼多多这种日单量5000万的，半年数据得有百亿级别了，那都得分到4096张表了对吧，但是实际的操作是一样的，对于你们的业务分4096那就没有必要了，根据业务做出合理的选择。

对于基于shardingkey的查询我们可以很简单的解决，对于非shardingkey的查询可以通过落双份数据和数仓、ES的方案来解决，当然，如果分表后数据量很小的话，建好索引，扫全表查询其实也不是什么问题。

[其他参考](https://www.cnblogs.com/hanzhong/p/10440286.html)

---

# [什么是SQL注入攻击](https://mp.weixin.qq.com/s/mnZT0Z5L6Hi6gRgEO1C9tg)

**SQL注入攻击是黑客对数据库进行攻击的常用手段之一**，随着B/S模式应用开发的发展，使用这种模式编写应用程序的程序员也越来越多。但是由于程序员的水平及经验参差不齐，相当大一部分程序员在编写代码的时候，没有对用户输入数据的合法性进行判断，使应用程序存在安全隐患。用户可以提交一段数据库查询代码，根据程序返回的结果，获得某些他想获取的数据，这就是所谓的SQL Injection，即SQL注入。

## 一 背景

假如某高校开发了一个网课系统，要求学生选课后完成学习，数据库中有一张表`course`，这张表存放着每个学生的选课信息及完成情况，具体设计如下：

![图片](https://mmbiz.qpic.cn/mmbiz_png/lgiaG5BicLkVca9ZBHbDcmiamqI2q2kgY34zLtJqic6ua4RWjw08UW62gicn0qNOh9oibqSA5gncqmROvQiaQYfCsVtfQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

数据如下：

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

本系统采用mysql做为数据库，使用Jdbc来进行数据库的相关操作。系统提供了一个功能查询该学生的课程完成情况，代码如下。

```sql
@RestController
public class Controller {

    @Autowired
    SqlInject sqlInject;

    @GetMapping("list")
    public List<Course> courseList(@RequestParam("studentId") String studentId){
        List<Course> orders = sqlInject.orderList(studentId);
        return orders;
    }
}
@Service
public class SqlInject {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Course> orderList(String studentId){
        String sql = "select id,course_id,student_id,status from course where student_id = "+ studentId;
        return jdbcTemplate.query(sql,new BeanPropertyRowMapper(Course.class));
    }
}
```

## 二 注入攻击演示

**1**. 正常情况下查询一个学生所选课程及完成情况只需要传入`student_id`，便可以查到相关数据。

![图片](https://mmbiz.qpic.cn/mmbiz_png/lgiaG5BicLkVca9ZBHbDcmiamqI2q2kgY34wP3lL9393Wicd0VY79Oj2NVnSV68pBf2wTygL9nChXQS34icuFUlnZzg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

根据响应结果，我们很快便能写出对应的sql，如下：

```sql
select id,course_id,student_id,status 
from course 
where student_id = 4
```

**2**. 如果我们想要获取这张表的所有数据，只需要保证上面这个sql的where条件恒真就可以了。

```sql
select id,course_id,student_id,status 
from course 
where student_id = 4 or 1 = 1 
```

请求接口的时候将`studendId` 设置为4 or 1 = 1，这样这条sql的where条件就恒真了。sql也就等同于下面这样

```sql
select id,course_id,student_id,status 
from course 
```

请求结果如下，我们拿到了这张表的所有数据

![图片](https://mmbiz.qpic.cn/mmbiz_png/lgiaG5BicLkVca9ZBHbDcmiamqI2q2kgY34ibQ2G8GxOEGQVYYewdqWsl0EA7FlicCWicerYbiaVZ3ECIPOrmzv0Sgucg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**3**. 查询mysql版本号，使用`union`拼接sql

```sql
union select 1,1,version(),1
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/lgiaG5BicLkVca9ZBHbDcmiamqI2q2kgY34N7JahloccMPFC8wJCt3M3CrEuYXYiaonWXl06iclytPiazuEgZmFSl9Kw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**4**. 查询数据库名

```sql
union select 1,1,database(),1
```

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

**5**. 查询mysql当前用户的所有库

```sql
union select 1,1, (SELECT GROUP_CONCAT(schema_name) FROM information_schema.schemata) schemaName,1
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/lgiaG5BicLkVca9ZBHbDcmiamqI2q2kgY34ib52avYZeHwlZHIb9jQfkLBa3fjL2R3yjt6VaxsQbS1Ghqu453YNu8A/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

看完上面这些演示后，你害怕了吗？你所有的数据配置都完全暴露出来了，除此之外，还可以完成很多操作，更新数据、删库、删表等等。

## 三 如何防止sql注入

**1. 代码层防止sql注入攻击的最佳方案就是sql预编译**

```sql
public List<Course> orderList(String studentId){
    String sql = "select id,course_id,student_id,status from course where student_id = ?";
    return jdbcTemplate.query(sql,new Object[]{studentId},new BeanPropertyRowMapper(Course.class));
}
```

这样我们传进来的参数 `4 or 1 = 1`就会被当作是一个`student_id`，所以就不会出现sql注入了。

**2. 确认每种数据的类型，比如是数字，数据库则必须使用int类型来存储**

**3. 规定数据长度，能在一定程度上防止sql注入**

**4. 严格限制数据库权限，能最大程度减少sql注入的危害**

**5. 避免直接响应一些sql异常信息，sql发生异常后，自定义异常进行响应**

**6. 过滤参数中含有的一些数据库关键词**

```java
@Component
public class SqlInjectionFilter implements Filter {
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req=(HttpServletRequest)servletRequest;
        HttpServletRequest res=(HttpServletRequest)servletResponse;
        //获得所有请求参数名
        Enumeration params = req.getParameterNames();
        String sql = "";
        while (params.hasMoreElements()) {
            // 得到参数名
            String name = params.nextElement().toString();
            // 得到参数对应值
            String[] value = req.getParameterValues(name);
            for (int i = 0; i < value.length; i++) {
                sql = sql + value[i];
            }
        }
        if (sqlValidate(sql)) {
            throw new IOException("您发送请求中的参数中含有非法字符");
        } else {
            chain.doFilter(servletRequest,servletResponse);
        }
    }

    /**
     * 关键词校验
     * @param str
     * @return
     */
    protected static boolean sqlValidate(String str) {
        // 统一转为小写
        str = str.toLowerCase();
        // 过滤掉的sql关键字，可以手动添加
        String badStr = "'|and|exec|execute|insert|select|delete|update|count|drop|*|%|chr|mid|master|truncate|" +
                "char|declare|sitename|net user|xp_cmdshell|;|or|-|+|,|like'|and|exec|execute|insert|create|drop|" +
                "table|from|grant|use|group_concat|column_name|" +
                "information_schema.columns|table_schema|union|where|select|delete|update|order|by|count|*|" +
                "chr|mid|master|truncate|char|declare|or|;|-|--|+|,|like|//|/|%|#";
        String[] badStrs = badStr.split("\\|");
        for (int i = 0; i < badStrs.length; i++) {
            if (str.indexOf(badStrs[i]) >= 0) {
                return true;
            }
        }
        return false;
    }
}
```

---

# [一个遗留项目的SQL优化实战录](https://mp.weixin.qq.com/s/MA7FVeJDMg8WDJABiBWpBA)

> 首先说在前面：**数据库很重要**，尤其以后工作做后端开发，数据库当家呀，确实得花精力好好学

------

 **实战背景** 

本次SQL优化是针对Java Web中的表格查询做的。

**部分网络架构图**

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzqR4iapVwSicVP8oqVVsiaPnT6cbNtDDkIm0kgV8G6Hb3YKZjtDjG2uGlLLicazmAPO2260GNSKW2HTmQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

------

 **业务简单说明** 

N个机台将业务数据发送至服务器，服务器程序将数据入库至MySQL数据库。服务器中的Java Web程序将数据展示到网页上供用户查看，怎么样，简单吧

**原数据库设计**

- Windows单机主从分离
- 已分表分库，按年分库，按天分表
- 每张表大概 20w左右的数据

**原查询效率**

3天数据查询 70 - 80s

**目标**

3-5s

**业务缺陷**

无法使用SQL分页，只能用java做分页。

------

 **问题排查** 

**前台慢 or 后台慢**

- 如果你配置了Druid，可在Druid页面中直接查看 sql执行时间和 url请求时间
- 在后台代码中用 `System.currentTimeMillis`计算时间差。

> 结论 ：后台慢，且查询sql慢

**sql有什么问题**

- sql拼接过长，有些竟达到了上千行，我勒个去，大多都是 union all 的操作，且有不必要的嵌套查询和查询了不必要的字段
- 利用 explain查看执行计划，where 条件中除时间外只有一个字段用到了索引

------

 **SQL优化过程** 

**1、去除不必要的字段**

效果没那么明显

**2、去除不必要的嵌套查询**

效果没那么明显

**3、分解sql**

- 将union all的操作分解，例如(一个 union all的 sql也很长)

```sql
select aa from bb_2018_10_01 left join ... on .. left join .. on .. where ..
union all
select aa from bb_2018_10_02 left join ... on .. left join .. on .. where ..
union all
select aa from bb_2018_10_03 left join ... on .. left join .. on .. where ..
union all
select aa from bb_2018_10_04 left join ... on .. left join .. on .. where ..
```

将如上 sql分解成若干个 sql去执行，最终汇总数据，最后快了 20s左右。

```sql
select aa from bb_2018_10_01 left join ... on .. left join .. on .. where ..
select aa from bb_2018_10_02 left join ... on .. left join .. on .. where ..
```

**4、将分解的sql异步执行**

利用 java**异步编程**的操作，将分解的 sql异步执行并最终汇总数据。这里用到了 `CountDownLatch`和 `ExecutorService`，示例代码如下：

```java
        // 获取时间段所有天数        
List<String> days = MyDateUtils.getDays(requestParams.getStartTime(), requestParams.getEndTime());        // 天数长度        
int length = days.size();        
// 初始化合并集合，并指定大小，防止数组越界        
List<你想要的数据类型> list = Lists.newArrayListWithCapacity(length);        
// 初始化线程池        
ExecutorService pool = Executors.newFixedThreadPool(length);        
// 初始化计数器        
CountDownLatch latch = new CountDownLatch(length);        
// 查询每天的时间并合并        
for (String day : days) {            
    Map<String, Object> param = Maps.newHashMap();            
    // param 组装查询条件
            pool.submit(new Runnable() {                
                @Override                
                public void run() {                    
                    try {                        
                        // mybatis查询sql                        
                        // 将结果汇总                        
                        list.addAll(查询结果);                    
                    } catch (Exception e) {                        
                        logger.error("getTime异常", e);                    
                    } finally {                        
                        latch.countDown();                    
                    }                
                }            
            });        
}

        try {            
            // 等待所有查询结束            
            latch.await();        
        } catch (InterruptedException e) {            
            e.printStackTrace();        
        }
        // list为汇总集合        
		// 如果有必要，可以组装下你想要的业务数据，计算什么的，如果没有就没了
```

> 结果又快了 20 - 30s

**5、优化MySQL配置**

以下是我的配置示例。加了 skip-name-resolve, 快了4-5s。其他配置自行断定

```properties
[client]
port=3306
[mysql]
no-beepdefault-character-set=utf8
[mysqld]
server-id=2
relay-log-index=slave-relay-bin.index
relay-log=slave-relay-bin
#跳过所有错误
slave-skip-errors=all 
skip-name-resolve

port=3306
datadir="D:/mysql-slave/data"
character-set-server=utf8
default-storage-engine=INNODB
sql-mode="STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION"

log-output=FILE
general-log=0
general_log_file="WINDOWS-8E8V2OD.log"
slow-query-log=1
slow_query_log_file="WINDOWS-8E8V2OD-slow.log"
long_query_time=10

# Binary Logging.
# log-bin

# Error Logging.
log-error="WINDOWS-8E8V2OD.err"

# 整个数据库最大连接（用户）数
max_connections=1000
# 每个客户端连接最大的错误允许数量
max_connect_errors=100
# 表描述符缓存大小，可减少文件打开/关闭次数
table_open_cache=2000
# 服务所能处理的请求包的最大大小以及服务所能处理的最大的请求大小(当与大的BLOB字段一起工作时相当必要)
# 每个连接独立的大小.大小动态增加
max_allowed_packet=64M
# 在排序发生时由每个线程分配
sort_buffer_size=8M
# 当全联合发生时,在每个线程中分配
join_buffer_size=8M
# cache中保留多少线程用于重用
thread_cache_size=128
# 此允许应用程序给予线程系统一个提示在同一时间给予渴望被运行的线程的数量.
thread_concurrency=64
# 查询缓存
query_cache_size=128M
# 只有小于此设定值的结果才会被缓冲# 此设置用来保护查询缓冲,防止一个极大的结果集将其他所有的查询结果都覆盖
query_cache_limit=2M
# InnoDB使用一个缓冲池来保存索引和原始数据
# 这里你设置越大,你在存取表里面数据时所需要的磁盘I/O越少.
# 在一个独立使用的数据库服务器上,你可以设置这个变量到服务器物理内存大小的80%
# 不要设置过大,否则,由于物理内存的竞争可能导致操作系统的换页颠簸.
innodb_buffer_pool_size=1G
# 用来同步IO操作的IO线程的数量# 此值在Unix下被硬编码为4,但是在Windows磁盘I/O可能在一个大数值下表现的更好.
innodb_read_io_threads=16
innodb_write_io_threads=16
# 在InnoDb核心内的允许线程数量.
# 最优值依赖于应用程序,硬件以及操作系统的调度方式.
# 过高的值可能导致线程的互斥颠簸.
innodb_thread_concurrency=9
# 0代表日志只大约每秒写入日志文件并且日志文件刷新到磁盘.
# 1 ,InnoDB会在每次提交后刷新(fsync)事务日志到磁盘上
# 2代表日志写入日志文件在每次提交后,但是日志文件只有大约每秒才会刷新到磁盘上
innodb_flush_log_at_trx_commit=2
# 用来缓冲日志数据的缓冲区的大小.
innodb_log_buffer_size=16M
# 在日志组中每个日志文件的大小.
innodb_log_file_size=48M
# 在日志组中的文件总数.
innodb_log_files_in_group=3
# 在被回滚前,一个InnoDB的事务应该等待一个锁被批准多久.
# InnoDB在其拥有的锁表中自动检测事务死锁并且回滚事务.
# 如果你使用 LOCK TABLES 指令, 或者在同样事务中使用除了InnoDB以外的其他事务安全的存储引擎
# 那么一个死锁可能发生而InnoDB无法注意到.# 这种情况下这个timeout值对于解决这种问题就非常有帮助.
innodb_lock_wait_timeout=30
# 开启定时
event_scheduler=ON
```

**6、根据业务，再加上筛选条件**

快 4 - 5s

**7、将 where条件中除时间条件外的字段建立联合索引**

效果没那么明显

**8、将where条件中索引条件使用inner join的方式去关联**

针对这条，我自身觉得很诧异。原sql，b为索引

```sql
select aa from bb_2018_10_02 left join ... on .. left join .. on .. where b = 'xxx'
```

应该之前有union all，union all是一个一个的执行，最后汇总的结果。修改为

```sql
select aa from bb_2018_10_02 
left join ... on .. 
left join ... on .. 
inner join(    
    select 'xxx1' as b2    
    union all    
    select 'xxx2' as b2    
    union all    
    select 'xxx3' as b2    
    union all    
    select 'xxx3' as b2) t on b = t.b2
```

> 结果快了 3 - 4s

**性能瓶颈**

根据以上操作，3天查询效率已经达到了8s左右，再也快不了了。查看 mysql的 cpu使用率和 内存使用率都不高，到底为什么查这么慢了，3天最多才60w数据，关联的也都是一些字典表，不至于如此。继续根据网上提供的资料，一系列骚操作，基本没用，没辙。

**环境对比**

因分析过sql优化已经ok了，试想是不是磁盘读写问题。将优化过的程序，分别部署于不同的现场环境。一个有ssd，一个没有ssd。发现查询效率悬殊。用软件检测过发现 ssd读写速度在 700-800M/s,普通机械硬盘读写在 70-80M/s。

**优化结果及结论**

- 优化结果：达到预期。
- 优化结论：sql优化不仅仅是对sql本身的优化，还取决于本身硬件条件，其他应用的影响，外加自身代码的优化。

------

 **小  结** 

优化的过程是自身的一个历练和考验，珍惜这种机会，不做只写业务代码的程序员。希望以上可以有助于大家思考，不足之处望指正。

---

# [误删数据库后该如何恢复](https://mp.weixin.qq.com/s/UYZZkrbAetgnPUjGa71fJA)

**事件起因**

我们的系统中有数据导入的功能，可以把特定的格式的 excel 数据导入到系统中来。

由于客户电脑的文件比较多，很多文件的名字也比较相近，客户在导入 excel 时选错了文件。

这个错误的 excel 文件的格式恰好能被系统解析，客户也没及时发现导错了文件，所以就将 6 万多条没用的数据导入到了系统中。



这 6 万多条数据对系统来说就是无用的数据，不会影响系统的运行，最多也就是占用一点数据库空间而已。

客户只需要把正确的 excel 重新导入，就可以继续完成他的业务了。

但是，客户是一个重度强迫症患者，他觉得在管理平台看到这 6 万多条没用的数据令他抓狂。

客户想要把这些数据删除，我们系统又没有提供批量删除功能，只能单个删除，这无疑是一个巨大的工作量。

客户就通过客服部门找到了研发团队，想让我们研发人员从数据库中直接删除。

## 删库经过

虽然在生产环境直接操作数据库明显是违规操作，但客户的要求又不得不满足，谁让人家是爸爸呢？



> 由于生产环境的数据和表结构属于商业机密，我们讨论的重点也不在于数据和表结构，而是数据恢复的思路。所以我在测试环境新建了用户表，导入了一些测试数据，当作是生产环境进行操作

研发人员登录生产数据库，执行如下 sql，找到了这 6 万多条错误数据。

```sql
select * from t_user where age>18 and deptid=100;
```

在确认这 6 万多条数据确实是错误导入的数据后就准备开始删除。由于表里面没有逻辑删除字段，所以只能进行物理删除。

需要删除的数据已经确定，通常情况下把 sql 中的`select *`替换为`delete`去执行，出错的机率会小一点。

但是，研发人员并没有去改原来的 sql，而是重新写了一个删除语句并且执行。

```sql
delete from t_user where age>18;
```

问题就这样出现了，在新写的删除语句中缺少了`deptid=100`的条件

> 不要问我为什么删除之前没有备份，这都是血泪的教训

重新查表后发现误删了 10 多万条数据。

生产环境中，很多业务都依赖这个表，算是系统的核心表。虽然是只删除了 10 万条数据，但系统的很多功能无法正常使用，其实和删库没啥区别了。



研发人员发现删库后，第一时间报告给了领导（居然没有第一时间跑路）。

领导当机立断，要求系统停止运行，给所有客户发送停服通知，打开所有客服通道，处理客户投诉和答疑。

同时，也安排研发人员进行数据找回，要求尽快搞定。

## 数据找回

我们找到删库的研发人员询问他有没有备份，他的回答是没有。

我们又去咨询运维的同事，看看生产环境有没有开启数据库定期自动备份，运维的回答也是没有。

事情比较难办了，只能把希望寄托于 mysql 的 binlog 了。

binlog 二进制日志文件，数据库的 insert、delete、update、create、alter、drop 等写入操作都会被 binlog 记录（下文对 binlog 有详细介绍）。

binlog 记录日志是需要开启配置的，希望生产环境的 mysql 数据库开启了 binlog 日志，否则只能找专业的磁盘数据恢复的第三方公司了。

登录生产环境数据库，查看 binlog 是否开启。

```sql
SHOW VARIABLES LIKE 'LOG_BIN%';
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/wibgWibeaanUndkyu6MyBgVQWJ7wTwibfiaHYPAbicFmyGgNS17w8od08sOVXRgk93lPN6looWLYyeF3Y88PKWzZKrg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

从图中可以看到`log_bin`是处于`ON`的状态，说明 binlog 是开启的。

悬着的心终于放下了一大半，接下来就是想办法从 binlog 中把数据恢复就行了。

从上图中也可以看到`log_bin_basename`是`/var/lib/mysql/bin-log`，说明 binlog 是存放在 mysql 所在的服务器的`/var/lib/mysql`目录下，文件是以`bin-log`开头，比如：bin-log.000001。

登录 mysql 所在的服务器，进入到 binlog 所在的目录。

```bash
cd /var/lib/mysql
```

查看 binlog 日志文件。
![图片](https://mmbiz.qpic.cn/mmbiz_png/wibgWibeaanUndkyu6MyBgVQWJ7wTwibfiaHUF3HibKrlYAD5Ca86cv3rTWXSgv3CawRWdAE2vLdGJLSAFUZekiceb6g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

binlog 日志文件是滚动生成的，从图中看到现在已经有 4 个文件了。

通常情况下，生产环境的 binlog 会有成百上千个，这时候就需要确认我们需要的数据是在第几个 binlog 中了，下文也会讲怎么确定我们需要的是第几个。

因为我们删库是刚刚发生的事情，所以我们需要的数据大概率是在第 4 个文件中。

直接去查看第 4 个 binlog 文件，看到的全都是乱码，就像下面这样，这是因为 binlog 文件是二进制的。

![图片](https://mmbiz.qpic.cn/mmbiz_png/wibgWibeaanUndkyu6MyBgVQWJ7wTwibfiaHuHNJtO3PYCz79tSAr5gribtI7zkLE8BpgJicDuLDn5sy3nF6BMPziaYpA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

我们需要借助 mysql 官方提供的`mysqlbinlog`命令去才能正确解析 binlog 文件。

用 mysqlbinlog 命令可以打开 binlog 文件，但是一个 binlog 文件的大小可能有几百兆，要从几百兆日志中找到我们需要的日志，还是比较麻烦的。

还好 mysqlbinlog 命令提供一些参数选项可以让我们对 binlog 文件进行筛选，最常用的参数就是时间参数（下文也会对 mysqlbinlog 的详细用法进行说明）。

经过和删库的研发人员确定，删库的时间大概是**「10:40」**，那我们就以这个时间点为参考，找前后 5 分钟的日志。

```sql
mysqlbinlog -v --start-datetime='2021-06-10 10:35:00' --stop-datetime='2021-06-10 10:45:00' bin-log.000004 | grep t_user
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/wibgWibeaanUndkyu6MyBgVQWJ7wTwibfiaHWWRCocQ1QfMbFZRavMllPMnaeEia49jMFHoe7ml8m2EfJ5p43m7glWA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

从图中可以看到，这个时间点的日志确实包含我们删除数据的日志。

接下来我们就需要把这些日志整理一下，然后想办法恢复到数据库就可以了。

首先，把我们需要的日志单独保存到 tmp.log 文件中，方便下载到本地。

```sql
mysqlbinlog -v --start-datetime='2021-06-10 10:35:00' --stop-datetime='2021-06-10 10:45:00' bin-log.000004 > tmp.log
```

把 tmp.log 下载到本地，用文本编辑工具打开看一下，可以看到一堆伪 sql。
![图片](https://mmbiz.qpic.cn/mmbiz_png/wibgWibeaanUndkyu6MyBgVQWJ7wTwibfiaHpxMysARgvqxmibb2IBJzcbUZj10hke588hTiaAEm48T2tzs8rINrYGFA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

> 在上图的伪 sql 中
> @1 表示第一个字段
> @2 表示第二个字段
> 其他的以此类推

日志中包含的 sql 是一些伪 sql，并不能直接在数据库执行，我们需要想办法把这些伪 sql 处理成可在数据库执行的真正的 sql。

我们使用的文本编辑工具的批量替换功能，就像下面这样：
![图片](https://mmbiz.qpic.cn/mmbiz_jpg/wibgWibeaanUndkyu6MyBgVQWJ7wTwibfiaHXGwWLbaOF5Hemyib4DTrVDd9oucVYQiaLiaICgcVsNQ0FicgONwYOmDGtw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

最终处理好的 sql 就像是这样：
![图片](https://mmbiz.qpic.cn/mmbiz_jpg/wibgWibeaanUndkyu6MyBgVQWJ7wTwibfiaHh4Xt3Tic91CSqgw6uBMvRcVuJ3ic3EI3RHqx5L7CdeibgnxCdA9ib9IZuQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

把处理好的 sql 在测试数据库验证一下没问题后直接在生产库执行。

sql 执行完以后，被误删除的数据就恢复回来了。

我们和删库的研发一起，把客户要求删除的 6 万多条数据重新给删除，算是完成了客户的要求。

至此，删库事件就暂时告一段落。不要问删库的研发受到了什么处分，问就是什么处分都没有。

## 几点建议

删库跑路真的不只是一句玩笑话，如果真的不小心删库了而又无法找回数据的话，不仅仅是简单的罚款、扣绩效就完事了，甚至有可能会面临牢狱之灾。

对于公司来说，一个不小心的删库操作，就有可能把公司删没了。毕竟删库造成的数据损失、经济损失不是所有公司都有能力承担的。

所以，生产环境的数据安全一定是重中之重。根据我多年的删库经历，也总结了一些经验分享给你们，希望对你们有所帮助。

### 「1、研发人员不能直连生产库」

生产库一般由 DBA 或者运维来维护，研发人员很少有需要登录生产数据库查看数据的需求，就算数据真的有问题，一般情况下 DBA 或运维人员也能解决。

如果一个系统需要研发人员频繁的登录数据库去维护数据，这时就该考虑在系统中增加一个管理功能来使用，而不是频繁登录数据库。

所以，研发就不应该具有生产库的登录权限。如果偶尔的需要登录生产库查看数据，可以找 DBA 开一个临时账号。

### 「2、登录生产库使用只读账号」

大部分人使用数据库都会使用连接工具，比如 Navicat、SQLyog 等

每个人的电脑上，大概率也只有一个连接工具。开发库、测试库、生产库都在同一个连接工具中打开，有时只是想在开发库中修改一条数据，却不小心修改了生产库。

而 MySql 的事务是自动提交的，在连接工具中，正在修改的当前行失去光标后就会自动提交事务，极其容易操作失误。

所以，如果确实的需要登录生产库，尽量使用具有只读权限的账号登录。

### 「3、关闭 autocomit、多人复核」

如果确实需要在生产库进行数据的增加、修改或删除，在执行 sql 之前最好先关闭事务的自动提交。

在需要登录生产库修改数据的情况下，想必问题也比较复杂，一条 sql 语句应该是完成不了，可能需要写 N 多个 sql 才能完成数据的修改。

这么多的 sql，很有可能在执行的时候会选错。有时你只是想执行一个 select 语句，结果发现执行的是 delete。

更坑爹的是，大部分的数据库连接工具有`执行当前选中内容`的功能。有时候你只想执行当前选中的内容，结果发现执行的是全部内容。

如果关闭了自动提交，就算出现上面的情况，也还有机会挽回。比如下面这样：

```sql
-- 关闭事务自动提交
set @@autocommit=0;


-- 查看需要删除的数据，共65600条
select * from t_user where age>18 and deptid=100;
-- 删除
delete from t_user where age>18;


-- 发现有问题，回滚
select * from t_user where age>18 and deptid=100;
rollback ;

-- 确认没问题，提交
-- commit;
```

另外，在`commit`之前需要至少再找一个同事进行确认。所谓当局者迷，自己有时可能处于一个错误的思路上，就想当然的认为结果没问题，这时就需要一个旁观者来指点迷津。

两个人都确认没问题之后再提交，出错的机率也会小很多。

### 「4、修改数据之前先备份」

备份、备份、备份，重要的事情说三遍。

备份虽然会麻烦一点，但它是保证数据准确性最有效的手段。

况且，掌握一些技巧后，备份也不是很麻烦的事情。

比如，我们删除数据之前可以先这样备份。

```sql
-- 创建一个和原表一样的备份表（包含索引）
create table t_user_bak like t_user;

-- 拷贝数据到备份表
INSERT into t_user_bak select * from t_user;

-- 确认数据拷贝完成
select * from t_user_bak;
```

这样备份的数据，就算原表数据误删了，甚至都不用恢复数据，只需要把备份表的名字改成原表的名字直接使用就可以了。

在生产库修改数据之前，一定要记得备份，一旦数据修改出错，这是成本最低并且最有效的恢复途径。

### **「5、设置数据库定期备份」**

生产环境，运维人员一定要设置数据库定期备份。研发人员也有义务提醒运维同事编写自动备份脚本，因为生产库一旦出现问题需要恢复数据，没有定期备份的话，麻烦的不只是运维人员，研发人员也要跟着麻烦。

备份周期可以根据业务需要来决定。如果业务对数据要求的实时性比较高，备份周期相对短一点，恢复数据时可以最大程度的避免数据丢失；反之，备份周期可以长一点，节省磁盘空间。

如果有必要，可以定期把备份文件拷贝到异地服务器，避免由于一些不可抗力因素导致的当前服务器磁盘损坏，如地震、台风等。

## binlog 日志

binlog 即 Binary Log，它是二进制文件，用来记录数据库写操作的日志。

数据库的 insert、delete、update、create、alter、drop 等写入操作都会被 binlog 记录。

因此，数据库的主从数据同步通常也是基于 binlog 完成的，本文只对 binlog 做一些简单介绍，后期会单独写一篇文章讲基于 binlog 的主从数据同步。

binlog 日志需要配置开启，可以通过脚本查看 binlog 是否开启。

```sql
SHOW VARIABLES LIKE 'LOG_BIN%';
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/wibgWibeaanUndkyu6MyBgVQWJ7wTwibfiaHYPAbicFmyGgNS17w8od08sOVXRgk93lPN6looWLYyeF3Y88PKWzZKrg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

如果`log_bin`参数显示的是`OFF`说明 binlog 是关闭状态，需要手动开启。

开启 binlog 需要修改数据库的`my.cnf`配置文件，my.cnf文件通常在服务器的`/etc`目录下。

打开`/etc/my.cnf`文件，配置 binlog 的相关参数，下文配置 binlog 的常用参数。

```sql
# mysql5.7以上版本需配置service-id
service-id=1
# 启用binlog并设置binlog日志的存储目录
log_bin = /var/lib/mysql/bin-log
# 设置binlog索引存储目录
log_bin_index = /var/lib/mysql/mysql-bin.index
# 30天之前的日志自动删除
expire_logs_days = 30
# 设置binlog日志模式，共有3种模式：STATMENT、ROW、MIXED
 binlog_format = row
```

binlog 的日志有三种格式，分别是 STATEMENT、ROW、MIXED。在 mysql5.7.7 版本之前默认使用的是 STATEMENT，之后的版本默认使用的是 ROW。

### ROW 格式

ROW 格式下，binlog 记录的是每一条数据被修改的详细细节。

比如，执行 delete 语句，删除的数据有多少条，binlog 中就记录有多少条伪 sql。

```sql
delete from t_user where age>18;
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/wibgWibeaanUndkyu6MyBgVQWJ7wTwibfiaHNZNnvREYjCDcn32f8qSnR4icAhibsfAFZh1X7DADMlonDxWDnYKNAr4w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

那么 row 格式的日志的缺点就很明显，在发生批量操作时，日志文件中会记录大量的伪 sql，占用较多的磁盘空间。

尤其是当进行 alter 操作时，每条数据都发生变化，日志文件中就会有每一条的数据的日志。此时，如果表中的数据量很大的话，日志文件也会非常大。

在 mysql5.6 版本之后，针对 ROW 格式的日志，新增了`binlog_row_image`参数。

当`binlog_row_image`设置为`minimal`时，日志中只会记录发生改变的列，而不是全部的列，这在一定程度上能减少 binlog 日志的大小。

虽然记录每行数据的变化会造成日志文件过大，但这也是它的优点所在。

因为它记录了每条数据修改细节，所以在一些极端情况下也不会出现数据错乱的问题。在做数据恢复或主从同步时能很好的保证数据的真实性和一致性

### STATEMENT 格式

STATEMENT 格式下，日志中记录的是真正的 sql 语句，就像是这样。
![图片](https://mmbiz.qpic.cn/mmbiz_png/wibgWibeaanUndkyu6MyBgVQWJ7wTwibfiaHs4qaWw6hdcDBBobnaEaR0FbnJmCfGVEhicfCpspqs4V75gO8W9e8pyQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

日志中的 sql 是直接可以拿到数据库运行的。

STATEMENT 格式的日志的优缺点和 ROW 格式的正好相反，它记录的是 sql 语句和执行语句时的上下文环境，而不是每一条数据。所以它的日志文件会比 ROW 格式的日志文件小一些。

由于记录的只是 sql 语句和上下文的环境，STATEMENT 格式的日志在进行主从数据同步时会有一些不可预估的情况出现，导致数据错乱。比如 sleep()、last_insert_id() 等函数会出现问题。

### MIXED 格式

MIXED 格式是 STATEMENT 和 ROW 的结合，mysql 会根据具体执行的 sql 语句，来选择合适的日志格式进行记录。

MIXED 格式下，在执行普通的 sql 语句时会选 STATEMENT 来记录日志，在遇到复杂的语句或函数操作时会选择 ROW 来记录日志。

## mysqlbinlog 命令

mysql 数据库的 binlog 文件是二进制的，基本看不懂，使用数据库自带的`mysqlbinlog`命令可以把二进制文件转换成能看懂的十进制文件。

由于数据库的 binlog 文件可能会很大，查看起来会很麻烦，所以`mysqlbinlog`命令也提供了一些参数可以用来筛选日志。

**「mysqlbinlog 语法」**

```sql
mysqlbinlog [options] log-files
```

> `options`：可选参数
> `log-files`：文件名称

**「options 的常用值」**

> `-d`: 根据数据库的名称筛选日志
> `-o`：跳过前N行日志
> `-r, --result-fil`: 把日志输出到指定文件
> `--start-datetime`: 读取指定时间之后的日志，时间格式：yyyy-MM-dd HH:mm:ss
> `--stop-datetime`: 读取指定时间之前的日志，时间格式：yyyy-MM-dd HH:mm:ss
> `--start-position`: 从指定位置开始读取日志
> `--stop-position`: 读取到指定位置停止
> `--base64-output`：在 row 格式下，显示伪 sql 语句
> `-v, --verbose`：显示伪 sql 语句，-vv 可以为 sql 语句添加备注

**「常用写法」**
查看 fusion 数据库的日志

```bash
mysqlbinlog -d=fusion bin-log.000001
```

查看某个时间段内的日志

```bash
mysqlbinlog  --start-datetime='2021-06-09 19:30:00' --stop-datetime='2021-06-09 19:50:00' bin-log.000001
```

恢复数据，事件的开始位置是 4300，结束位置是 10345

```bash
mysqlbinlog --start-position 4300 --stop-position 10345 bin-log.000001 | mysql -uroot -p123456 fusion
```



异常：`报错unknown variable 'default-character-set=utf8'解决`

原因：mysqlbinlog这个工具无法识别binlog中的配置中的default-character-set=utf8这个指令。

解决：

一：在MySQL的配置`/etc/my.cnf`中将`default-character-set=utf8` 修改为 `character-set-server = utf8`，但是这需要重启MySQL服务，如果你的MySQL服务正在忙，那这样的代价会比较大。

二：用`mysqlbinlog --no-defaults mysql-bin.000004` 命令打开

---

# [如何科学根治慢SQL？](https://mp.weixin.qq.com/s/eQKphrkPeN_-EcWIxETz9Q)

**今天和大家聊一个常见的问题：慢SQL。
**

**包括以下内容：**

- **慢SQL的危害**
- **SQL语句的执行过程**
- **存储引擎和索引的那些事儿**
- **慢SQL解决之道**

**后续均以MySQL默认存储引擎InnoDB为例进行展开，话不多说，开始！**

## 1.慢SQL的危害

慢SQL，就是跑得很慢的SQL语句，你可能会问慢SQL会有啥问题吗？

试想一个场景：

> 大白和小黑端午出去玩，机票太贵于是买了高铁，火车站的人真是乌央乌央的。

> 马上检票了，大白和小黑准备去厕所清理下库存，坑位不多，排队的人还真不少。

> 小黑发现其中有3个坑的乘客贼慢，其他2个坑位换了好几波人，这3位坑主就是不出来。

> 等在外面的大伙，心里很是不爽，长期占用公共资源，后面的人没法用。

> 小黑苦笑道：这不就是厕所版的慢SQL嘛！

这是实际生活中的例子，换到MySQL服务器也是一样的，毕竟科技源自生活嘛。

MySQL服务器的资源(CPU、IO、内存等)是有限的，尤其在高并发场景下需要快速处理掉请求，否则一旦出现慢SQL就会阻塞掉很多正常的请求，造成大面积的失败/超时等。

![图片](https://mmbiz.qpic.cn/mmbiz_png/wAkAIFs11qaMZss2ia9Bx0yJNIfuKRhiawKIx7Ap2Bfx9G5kxrH9wMxUOAGoJEthtictIjPvwyAUmk9vCQEJVicyxA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

## 2.SQL语句执行过程

![图片](https://mmbiz.qpic.cn/mmbiz_png/wAkAIFs11qaMZss2ia9Bx0yJNIfuKRhiawvAsmfEicYvbEJicic3Zw3Ud8icpIqm5e8xVH5vAEkPUkzEkUbeQ0CUEpZQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

客户端和MySQL服务端的交互过程简介：

1. 客户端发送一条SQL语句给服务端，服务端的连接器先进行账号/密码、权限等环节验证，有异常直接拒绝请求。
2. 服务端查询缓存，如果SQL语句命中了缓存，则返回缓存中的结果，否则继续处理。
3. 服务端对SQL语句进行词法解析、语法解析、预处理来检查SQL语句的合法性。
4. 服务端通过优化器对之前生成的解析树进行优化处理，生成最优的物理执行计划。
5. 将生成的物理执行计划调用存储引擎的相关接口，进行数据查询和处理。
6. 处理完成后将结果返回客户端。

客户端和MySQL服务端的交互过程简图：![图片](https://mmbiz.qpic.cn/mmbiz_png/wAkAIFs11qaMZss2ia9Bx0yJNIfuKRhiawCB9kKgFQd2GrOsuGfXszyoH4VZBEzSEOODJcyc0WUQ6a2kgR7k3Jkw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

俗话说"条条大路通罗马"，优化器的作用就是找到这么多路中最优的那一条。

存储引擎更是决定SQL执行的核心组件，适当了解其中原理十分有益。

## 3. 存储引擎和索引的那些事儿

### 3.1 存储引擎

InnoDB存储引擎(Storage Engine)是MySQL默认之选，所以非常典型。

存储引擎的主要作用是进行数据的存取和检索，也是真正执行SQL语句的组件。

InnoDB的整体架构分为两个部分：内存架构和磁盘架构，如图：

![图片](https://mmbiz.qpic.cn/mmbiz_png/wAkAIFs11qaMZss2ia9Bx0yJNIfuKRhiawfb1drqp3TbIRyP2cCf6mGMDkVq98oaVOXibG7mtT1u02K0NJrupBQAQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

> 存储引擎的内容非常多，并不是一篇文章能说清楚的，本文不过多展开，我们在此只需要了解内存架构和磁盘架构的大致组成即可。

InnoDB 引擎是面向行存储的，数据都是存储在磁盘的数据页中，数据页里面按照固定的行格式存储着每一行数据。

> 行格式主要分为四种类型Compact、Redundant、Dynamic和Compressed，默认为Compact格式。

![图片](https://mmbiz.qpic.cn/mmbiz_png/wAkAIFs11qaMZss2ia9Bx0yJNIfuKRhiawHM0bYBZibKpjUnIBh96avgob6ImMts8IUrUp4meU3kSvZgvQXKIS18A/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 磁盘预读机制和局部性原理

当计算机访问一个数据时，不仅会加载当前数据所在的数据页，还会将当前数据页相邻的数据页一同加载到内存，磁盘预读的长度一般为页的整倍数，从而有效降低磁盘IO的次数。

![图片](https://mmbiz.qpic.cn/mmbiz_png/wAkAIFs11qaMZss2ia9Bx0yJNIfuKRhiawzADXHbHgXLrRvzTo6riacibYCuc6j9iaJVD8UAanYz8SzqUcqD0IS93cA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 磁盘和内存的交互

MySQL中磁盘的数据需要被交换到内存，才能完成一次SQL交互，大致如图：

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

- 扇区是硬盘的读写的基本单位，通常情况下每个扇区的大小是 512B
- 磁盘块文件系统读写数据的最小单位，相邻的扇区组合在一起形成一个块，一般是4KB
- 页是内存的最小存储单位，页的大小通常为磁盘块大小的 2^n 倍
- InnoDB页面的默认大小是16KB，是数倍个操作系统的页

![图片](https://mmbiz.qpic.cn/mmbiz_png/wAkAIFs11qaMZss2ia9Bx0yJNIfuKRhiawQcR5UBxgWJluY8rDsG1UbnabFia04265Pns6AicQticic2WIXJahNE0t0A/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 随机磁盘IO

MySQL的数据是一行行存储在磁盘上的，并且这些数据并非物理连续地存储，这样的话要查找数据就无法避免随机在磁盘上读取和写入数据。

对于MySQL来说，当出现大量磁盘随机IO时，大部分时间都被浪费到寻道上，磁盘呼噜呼噜转，就是传输不了多少数据。

> 一次磁盘访问由三个动作组成：
>
> - 寻道：磁头移动定位到指定磁道
> - 旋转：等待指定扇区从磁头下旋转经过
> - 数据传输：数据在磁盘与内存之间的实际传输

对于存储引擎来说，如何有效降低随机IO是个非常重要的问题。

### 3.2 索引

可以实现增删改查的数据结构非常多，包括：哈希表、二叉搜索树、AVL、红黑树、B树、B+树等，这些都是可以作为索引的候选数据结构。

结合MySQL的实际情况：磁盘和内存交互、随机磁盘IO、排序和范围查找、增删改的复杂度等等，综合考量之下B+树脱颖而出。

B+树作为多叉平衡树，对于范围查找和排序都可以很好地支持，并且更加矮胖，访问数据时的平均磁盘IO次数取决于树的高度，因此B+树可以让磁盘的查找次数更少。

在InnoDB中B+树的高度一般都在2~4层，并且根节点常驻内存中，也就是说查找某值的行记录时最多只需要1~3次磁盘I/O操作。

MyISAM是将数据和索引分开存储的，InnoDB存储引擎的数据和索引没有分开存储，这也就是为什么有人说Innodb索引即数据，数据即索引，如图：

![图片](https://mmbiz.qpic.cn/mmbiz_png/wAkAIFs11qaMZss2ia9Bx0yJNIfuKRhiawXEfGDDbZyicTohgDN9B9QEOOW2VcHRltqJyshFNkPFxgC1via6ksic6JQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

说到InnoDB的数据和索引的存储，就提到一个名词：**聚集索引**。

#### 聚集索引

聚集索引将索引和数据完美地融合在一起，是每个Innodb表都会有的一个特殊索引，一般来说是借助于表的主键来构建的B+树。

假设我们有student表，将id作为主键索引，那么聚集索引的B+树结构，如图：

![图片](https://mmbiz.qpic.cn/mmbiz_png/wAkAIFs11qaMZss2ia9Bx0yJNIfuKRhiawTxntAYMkhaT4mp2Xb7oPJXxMC9lUd7qfqbBcX3PqXiaz2NbsngubJJA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- 非叶子节点不存数据，只有主键和相关指针
- 叶子节点包含主键、行数据、指针
- 叶子节点之间由双向指针串联形成有序双向链表，叶子节点内部也是有序的

聚集索引按照如下规则创建：

- 有主键时InnoDB利用主键来生成
- 没有主键，InnoDB会选择一个非空的唯一索引来创建
- 无主键且无非NULL唯一索引时，InnoDB会隐式创建一个自增的列来创建

假如我们要查找id=10的数据，大致过程如下：

- 索引的根结点在内存中，10>9 因此找到P3指针
- P3指向的数据并没有在内存中，因此产生1次磁盘IO读取磁盘块3到内存
- 在内存中对磁盘块3进行二分查找，找到ID=9的全部值

#### 非聚集索引

非聚集索引的叶子节点中存放的是二级索引值和主键键值，非叶子节点和叶子节点都没有存储整行数据值。

假设我们有student表，将name作为二级索引，那么非聚集索引的B+树结构，如图：

![图片](https://mmbiz.qpic.cn/mmbiz_png/wAkAIFs11qaMZss2ia9Bx0yJNIfuKRhiawcfyMslw9iayvVYUmA00ia3xQgP1NRefw2oIWBRhSbqomtK8icuibOC7eiaQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

由于非聚集索引的叶子节点没有存储行数据，如果通过非聚集索引来查找非二级索引值，需要分为两步：

- 第一：通过非聚集索引的叶子节点来确定数据行对应的主键
- 第二：通过相应的主键值在聚集索引中查询到对应的行记录

我们把通过非聚集索引找到主键值，再根据主键值从聚集索引找对于行数据的过程称为：**回表查询**。

换句话说：select * from student where name = 'Bob' 将产生回表查询，因为在name索引的叶子节点没有其他值，只能从聚集索引获得。

所以如果查找的字段在非聚集索引就可以完成，就可以避免一次回表过程，这种称为：覆盖索引，所以select * 并不是好习惯，需要什么拿什么就好。

假如我们要查找name=Tom的记录的所有值，大致过程如下：

- 从非聚集索引开始，根节点在内存中，按照name的字典序找到P3指针
- P3指针所指向的磁盘块不在内存中，产生1次磁盘IO加载到内存
- 在内存中对磁盘块3的数据进行搜索，获得name=tom的记录的主键值为4
- 根据主键值4从聚集索引的根节点中获得P2指针
- P2指针所指向的磁盘块不在内存中，产生第2次磁盘IO加载到内存
- 将上一步获得的数据，在内存中进行二分查找获得全部行数据

上述查询就包含了一次回表过程，因此性能比主键查询慢了一倍，因此尽量使用主键查询，一次完事。

## 4. 慢SQL解决思路

出现慢SQL的原因很多，我们抛开单表数亿记录和无索引的特殊情况，来讨论一些更有普遍意义的慢SQL原因和解决之道。

我们从两个方面来进行阐述：

- 数据库表索引设置不合理
- SQL语句有问题，需要优化

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

### 4.1 索引设置原则

程序员的角度和存储引擎的角度是不一样的，索引写的好，SQL跑得快。

- **索引区分度低**

假如表中有1000w记录，其中有status字段表示状态，可能90%的数据status=1，可以不将status作为索引，因为其对数据记录区分度很低。

- **切忌过多创建索引**

每个索引都需要占用磁盘空间，修改表数据时会对索引进行更新，索引越多，更新越复杂。

> 因为每添加一个索引，.ibd文件中就需要多维护一个B+Tree索引树，如果某一个table中存在10个索引，那么就需要维护10棵B+Tree，写入效率会降低，并且会浪费磁盘空间。

- **常用查询字段建索引**

如果某个字段经常用来做查询条件，那么该字段的查询速度会影响整个表的查询速度，属于热门字段，为其建立索引非常必要。

- **常排序/分组/去重字段建索引**

对于需要经常使用ORDER BY、GROUP BY、DISTINCT和UNION等操作的字段建立索引，可以有效借助B+树的特性来加速执行。

- **主键和外键建索引**

主键可以用来创建聚集索引，外键也是唯一的且常用于表关联的字段，也需要建索引来提高性能。

### 4.2 SQL的优化

如果数据库表的索引设置比较合理，SQL语句书写不当会造成索引失效，甚至造成全表扫描，迅速拉低性能。

#### 索引失效

我们在写SQL的时候在某些情况下会出现索引失效的情况：

- **对索引使用函数**

> select id from std upper(name) = 'JIM';

- **对索引进行运算**

> select id from std where id+1=10;

- **对索引使用<> 、not in 、not exist、!=**

> select id from std where name != 'jim';

- **对索引进行前导模糊查询**

> select id from std name like '%jim';

- **隐式转换会导致不走索引**

> 比如：字符串类型索引字段不加引号，select id from std name = 100;保持变量类型与字段类型一致

- **非索引字段的or连接**

> 并不是所有的or都会使索引失效，如果or连接的所有字段都设置了索引，是会走索引的，一旦有一个字段没有索引，就会走全表扫描。

- **联合索引仅包含复合索引非前置列**

> 联合索引包含key1，key2，key3三列，但SQL语句没有key1，根据联合索引的最左匹配原则，不会走联合索引。
> select name from table where key2=1 and key3=2;

#### 好的建议

- **使用连接代替子查询**

> 对于数据库来说，在绝大部分情况下，连接会比子查询更快，使用连接的方式，MySQL优化器一般可以生成更佳的执行计划，更高效地处理查询
> 而子查询往往需要运行重复的查询，子查询生成的临时表上也没有索引， 因此效率会更低。

- **LIMIT偏移量过大的优化**

> 禁止分页查询偏移量过大，如limit 100000,10

- **使用覆盖索引**
  减少select * 借助覆盖索引，减少回表查询次数。
- **多表关联查询时，小表在前，大表在后**

> 在MySQL中，执行from后的表关联查询是从左往右执行的，第一张表会涉及到全表扫描，所以将小表放在前面，先扫小表，扫描快效率较高，在扫描后面的大表，或许只扫描大表的前100行就符合返回条件并return了。

- **调整Where字句中的连接顺序**

> MySQL采用从左往右的顺序解析where子句，可以将过滤数据多的条件放在前面，最快速度缩小结果集。

- **使用小范围事务，而非大范围事务**
- **遵循最左匹配原则**
- **使用联合索引，而非建立多个单独索引**

### 4.3 慢SQL的分析

在分析慢SQL之前需要通过MySQL进行相关设置：

- 开启慢SQL日志
- 设置慢SQL的执行时间阈值

```sql
开启：SET GLOBAL slow_query_log = 1;
开启状态：SHOW VARIABLES LIKE '%slow_query_log%';
设置阈值：SET GLOBAL long_query_time=3;
查看阈值：SHOW GLOBAL VARIABLES LIKE 'long_query_time%'; 
```

#### explain分析SQL

explain命令只需要加在select之前即可，例如:

> explain select * from std where id < 100;

该命令会展示sql语句的详细执行过程，帮助我们定位问题，网上关于explain的用法和讲解很多，本文不再展开。

## 5. 小结

本文从慢SQL的危害、Innodb存储引擎、聚集索引、非聚集索引、索引失效、SQL优化、慢SQL分析等角度进行了阐述。如果本文能在某些方面对读者有所启发，足矣。

![图片](https://mmbiz.qpic.cn/mmbiz_gif/b96CibCt70iabwjyojLhA03PtxUnkNPREnt2F48ywfXLpDdDAjicOTPI8Q94tVLbJ58tbRs12iaXDKhUOW9gd4NlFA/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

---

# [面试官最爱的数据库索引连环问](https://mp.weixin.qq.com/s/MLvJsJuFAHHcllqvk1nVRQ)

前段时间一直在面试，问了很多候选人数据库索引相关的知识，能答好的不是很多，令人惋惜啊，我也想留你啊……

> 面试官：了解过数据库索引吗？
>
> 候选人：听过一些，底层数据结构好像是二叉树，不对，好像是 B 树，哦，我想起来了，好像是 B+树……（像极了当年面试的我）
>
> 面试官：听过哈希索引吗？
>
> 候选人：我知道哈希表，哈希索引没听过
>
> 面试官：今天面试先到这里了，回去等消息吧……

*温馨提示：本文是数据库索引的入门篇*

先引入一个简单的示例，通过示例操作解释一下为什么需要数据库索引。

假设我们有一个名为 t_employee 的数据库表，这个数据库表有三列：name，age，address，数据量有上万行。

如果我们想要查找所有名为「leixiaoshuai」员工的详细信息，只需要写一个简单的 SQL 语句就可以搞定，相信大家都会写。

```sql
SELECT * FROM t_employee 
WHERE name = 'leixiaoshuai'
```

## 如果没有索引，会发生什么？

一旦我们运行了这条 SQL 查询语句，在数据库内部是如何工作的呢？数据库会搜索 t_employee 表中的每一行，从而确定员工的名字（name）是否为 ‘leixiaoshuai’。由于我们想要得到每一个名字为 leixiaoshuai 的雇员信息，在查询到第一个符合条件的行记录后，不能停止查询，因为可能还有其他符合条件的行。所以，必须一行一行的查找直到最后一行，这就意味数据库不得不检查上万行数据才能找到所有名字为 leixiaoshuai 的员工。这就是所谓的**全表扫描**。

## 数据库索引如何帮助提高性能？

你可能会想：「这么简单的查询语句居然还需要全表扫描，数据库也太笨了吧？！」

这就类似于用人眼从头到尾逐字逐句读一本书，效率太低了！

那应该怎么办？聪明的你肯定想到解决方案了：「加个索引啊」。

这就是索引派上用场的时候了，使用索引的目的就是**通过减少表中需要检查的记录/行的数量来加速搜索查询。**说的再简单点：「索引就是用来加速查询的」。

## 什么是索引？

那么问题来了，什么是索引呢？索引本质是一种数据结构（最常见的是 B+树），是在表的列上创建的。

## 索引的数据结构是什么样的？

常见MySQL索引一般分为：**Hash索引**和**B+**树索引，InnoDB引擎中默认的是B+树。

**B+树** 是最常用于索引的数据结构，时间复杂度低：查找、删除、插入操作都可以可以在 logn 时间内完成。另外一个重要原因存储在 B+树 中的数据是**有序的**。

在B+树常规检索场景下，从根节点到叶子节点的搜索效率基本相当，不会出现大幅波动，而且基于索引的顺序扫描时，也可以利用双向指针快速左右移动，效率非常高。

**哈希索引就是采用一定的哈希算法**，把键值换算成新的哈希值，检索时不需要类似B+树那样从根节点到叶子节点逐级查找，只需一次哈希算法即可立刻定位到相应的位置，速度非常快。

## 哈希表索引是如何工作的？

如果你在创建索引时指定数据结构为「哈希表」，那这些索引也可称为「哈希索引」。

哈希索引的优点非常明显，在一定场景下，检索指定值时哈希表的效率极高。比如上面我们讨论的一个查询语句：SELECT * FROM t_employee WHERE name = ‘leixiaoshuai’，如果在 name 列上加一个哈希索引，检索速度有可能会成倍提升。

哈系索引的工作方式是将列的值作为索引的键值（key），键值相对应实际的值（value）是指向该表中相应行的指针。因为哈希表基本上可以看作是关联数组，一个典型的数据项就像 「leixiaoshuai => 0x996996」，而 0x996996 是对内存中表中包含 leixiaoshuai 这一行的引用。在哈系索引的中查询一个像 leixiaoshuai 这样的值，并得到对应行的在内存中的引用，明显要比扫描全表获得值为 leixiaoshuai 的行的方式快很多。

## 哈希索引的缺点

上面说了哈希索引的优点，那哈希索引的缺点也是绕不过去的。

哈希表是无顺的数据结构，对于很多类型的查询语句哈希索引都无能为力。举例来说，假如你想要找出所有小于40岁的员工。你怎么使用使用哈希索引进行查询？这不可行，因为哈希表只适合查询键值对，也就是说查询相等的查询（例：like “WHERE name = ‘leixiaoshuai’）。哈希表的键值映射也暗示其键的存储是无序的。这就是为什么哈希索引通常不是数据库索引的默认数据结构，**因为在作为索引的数据结构时，其不像B+Tree那么灵活**。

总结一下缺点：

- （1）不支持范围查询
- （2）不支持索引完成排序
- （3）不支持联合索引的最左前缀匹配规则

## 还有什么其他类型的索引？

常见的还有：R 树和位图索引。

R 树通常用来为空间问题提供帮助。例如，一个查询要求“查询出所有距离我两公里之内的麦当劳”，如果数据库表使用R树索引，这类查询的效率将会提高。

位图索引（bitmap index）， 这类索引适合放在包含布尔值(true 和 false)的列上。

## 索引如何提高性能？

因为索引基本上是用来存储列值的数据结构，这使查找这些列值更加快速。如果索引使用B+树数据结构，那么其中的数据是有序的，有序的列值可以极大的提升性能。

假如我们在 name 这一列上创建一个 B+树 索引，这意味着当我们用之前的SQL查找name=‘leixiaoshuai‘时不需要再扫描全表，而是用索引查找去查找名字为‘leixiaoshuai’的员工，因为索引已经按照按字母顺序排序。索引**已经排序**意味着查询一个名字会快很多，因为名字少字母为‘L’的员工都是排列在一起的。另外重要的一点是，索引同时存储了表中相应行的指针以获取其他列的数据。

## 数据库索引中到底存的是什么？

你现在已经知道数据库索引是创建在表的某列上的，并且存储了这一列的所有值。但是需要理解的重点是**数据库索引并不存储这个表中其他列（字段）的值**。举例来说，如果我们在 name 列创建索引，那么 age 列和 address 列上的值并不会存储在这个索引当中。如果我们确实把其他所有字段也存储在个这个索引中，那这样会占用太大的空间而且会十分低效。

## 索引还存储指向表行的指针

如果我们在索引里找到某一条记录作为索引的列的值，如何才能找到这一条记录的其它值呢？

这很简单，数据库索引同时存储了指向表中的相应行的指针。指针是指一块内存区域， 该内存区域记录的是对硬盘上记录的相应行的数据的引用。因此，索引中除了存储列的值，还存储着一个指向在行数据的索引。也就是说，索引中的name这列的某个值（或者节点）可以描述为 (“leixiaoshuai”, 0x996996)， 0x996996 就是包含 “leixiaoshuai”那行数据在硬盘上的地址。如果没有这个引用，你就只能访问到一个单独的值（“leixiaoshuai”），而这样没有意义，因为你不能获取这一行记录的employee的其他值-例如地址（address）和年龄（age）。

## 数据库如何知道何时使用索引？

当你运行一条查询 SQL 语句时，数据库会检查在查询的列上是否有索引。假设 name 列上确实创建了索引，数据库会接着检查使用这个索引做查询是否合理 ，因为有些场景下，使用索引比起全表扫描会更加低效。

## 可以强制数据库在查询中使用索引吗？

通常来说， 你不会告诉数据库什么时候使用索引，数据库自己决定。

```sql
force index(列)
```

## 如何在SQL中创建索引？

下面是在前面示例中的Employee_Name列上创建索引时实际SQL的外观：

```sql
CREATE INDEX name_index
ON t_employee (name)
```

## 如何在SQL中创建联合（多列）索引？

我们可以在age 和 address 两列上创建联合索引，SQL如下：

```sql
CREATE INDEX age_address_index
ON t_employee (age, address)
```

## 可以把数据库索引类比成什么？

一个非常好的类比是把数据库索引看作是书的索引。

你从头到尾逐字逐行读完就是「全表扫描」；

你翻看目录挑选感兴趣的部分阅读就是走了索引。

## 使用数据库索引有什么代价？

既然索引优点这么多，那给所有列加上索引不就完事了，no no no，加索引是有代价的。

（1）索引会占用空间。你的表越大，索引占用的空间越大。

（2）在更新操作有性能损失。当你在表中添加、删除或者更新行数据的时候， 在索引中也会有相同的操作。

**基本原则是：如果表中某列在查询过程中使用的非常频繁，那就在该列上创建索引**。



*参考：*

1. *How do database indexes work? And, how do indexes help? Provide a tutorial on database indexes.*
2. *数据库索引漫谈*

![图片](https://mmbiz.qpic.cn/mmbiz_gif/b96CibCt70iabwjyojLhA03PtxUnkNPREnt2F48ywfXLpDdDAjicOTPI8Q94tVLbJ58tbRs12iaXDKhUOW9gd4NlFA/640?wx_fmt=gif&wxfrom=5&wx_lazy=1)

---

# [30道保底的MySQL数据库面试题集合](https://mp.weixin.qq.com/s/aBboeqEphejICklAKLqS2Q)

**一个典型的互联网产品**架构包含接入层、逻辑处理层以及存储层，其中存储层承载着数据落地和持久化的任务，同时给逻辑处理层提供数据查询功能支持。说到存储层就要说到数据库，数据库知识掌握程度也是面试考察的知识点。

![图片](https://mmbiz.qpic.cn/mmbiz_png/ceNmtYOhbMSIaAmoSYianlwqUjqymUETl7NTAagjlEnbtzuTlCJm5O3U1jtmJDex1e2UtlDKIQeAEibGzpo0OOzg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

典型服务架构

**数据库分为关系型数据库和非关系型数据库**，也就是我们常说的 SQL 和 NoSQL，这两个方向的数据库代表产品分别是MySQL 和 Redis ，这次我们主要以面试问答的形式，来学习下关系型数据库 MySQL 基础知识。

**面试开始，准备接受面试官灵魂拷问吧！**

## 关系型数据库

### 什么是关系型数据库？

关系型数据库，是指采用了关系模型来组织数据的数据库，其以行和列的形式存储数据，以便于用户理解，关系型数据库这一系列的行和列被称为表，一组表组成了数据库。用户通过查询来检索数据库中的数据，而查询是一个用于限定数据库中某些区域的执行代码。

简单来说，关系模式就是二维表格模型。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ceNmtYOhbMSIaAmoSYianlwqUjqymUETlQWSO9dB8K0p4vmpgcoQkXbQzzkJPzTYpBtlbXib05lPJFG26CKhN3pQ/640?wx_fmt=jpeg&wxfrom=5&wx_lazy=1&wx_co=1)

二维表数据库

### 关系型数据库有什么优势？

关系型数据库的优势：

- 易于理解

  关系型二维表的结构非常贴近现实世界，二维表格，容易理解。

- 支持复杂查询 可以用 SQL 语句方便的在一个表以及多个表之间做非常复杂的数据查询。

- 支持事务 可靠的处理事务并且保持事务的完整性，使得对于安全性能很高的数据访问要求得以实现。

## MySQL数据库

### 什么是SQL

结构化查询语言 (Structured Query Language) 简称SQL，是一种特殊目的的编程语言，是一种数据库查询和程序设计语言，用于存取数据以及查询、更新和管理关系数据库系统。

### 什么是MySQL？

MySQL 是一个关系型数据库管理系统，MySQL 是最流行的关系型数据库管理系统之一，常见的关系型数据库还有 Oracle 、SQL Server、Access 等等。

**MySQL在过去由于性能高、成本低、可靠性好，已经成为最流行的开源数据库，广泛地应用在 Internet 上的中小型网站中**。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ceNmtYOhbMSIaAmoSYianlwqUjqymUETl15QCbJG4K7BWyBialDrshZ6mePPlbaCcMrQ3NFPlxuyd5hKpiauDRUiaA/640?wx_fmt=jpeg&wxfrom=5&wx_lazy=1&wx_co=1)

mysql图标

### MySQL 和 MariaDB 傻傻分不清楚？

MySQL 最初由瑞典 MySQL AB 公司开发，MySQL 的创始人是乌尔夫·米卡埃尔·维德纽斯，常用昵称蒙提（Monty）。

在被甲骨文公司收购后，现在属于甲骨文公司（Oracle） 旗下产品。Oracle 大幅调涨MySQL商业版的售价，因此导致自由软件社区们对于Oracle是否还会持续支持MySQL社区版有所隐忧。

MySQL 的创始人就是之前那个叫 Monty 的大佬以 MySQL为基础成立分支计划 MariaDB。

MariaDB打算保持与MySQL的高度兼容性，确保具有库二进制奇偶校验的直接替换功能，以及与MySQL API 应用程序接口)和命令的精确匹配。而原先一些使用 MySQL 的开源软件逐渐转向 MariaDB 或其它的数据库。

所以如果看到你公司用的是 MariaDB 不用怀疑，其实它骨子里还是 MySQL，学会了MySQL 也就会了 MariaDB。

### 一个彩蛋

MariaDB 是以 Monty 的小女儿Maria命名的，就像MySQL是以他另一个女儿 My 命名的一样，两款鼎鼎大名的数据库分别用两个女儿的名字命名，你大爷还是你大爷，老爷子牛批! 

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ceNmtYOhbMSIaAmoSYianlwqUjqymUETlicUFr4lgOZSugMfnSBwib1K8AXVtosdu1ibibx8p9TgrjvzXhUgnsmhUaA/640?wx_fmt=jpeg&wxfrom=5&wx_lazy=1&wx_co=1)

MySQL之父

### 如何查看MySQL当前版本号？

在系统命令行下：`mysql -V`

连接上MySQL命令行输入:

`> status`;

```sql
Server:   MySQL
Server version:  5.5.45
Protocol version: 10
```

或 `select version();`

```
+------------------------+
| version()              |
+------------------------+
| 5.5.45-xxxxx |
+------------------------+
```

## 基础数据类型

#### MySQL 有哪些数据类型？

MySQL 数据类型非常丰富，常用类型简单介绍如下：

整数类型：`BIT、BOOL、TINY INT、SMALL INT、MEDIUM INT、 INT、 BIG INT`

浮点数类型：`FLOAT、DOUBLE、DECIMAL`

字符串类型：`CHAR、VARCHAR、TINY TEXT、TEXT、MEDIUM TEXT、LONGTEXT、TINY BLOB、BLOB、MEDIUM BLOB、LONG BLOB`

日期类型：`Date、DateTime、TimeStamp、Time、Year`

其他数据类型：`BINARY、VARBINARY、ENUM、SET`...

### CHAR 和 VARCHAR的区别？

**CHAR 是固定长度的字符类型，VARCHAR 则是可变长度的字符类型**，下面讨论基于在 MySQL5.0 以上版本中。

#### 共同点

CHAR(M) 和 VARCHAR(M) 都表示该列能存储 M 个**字符**，**注意不是字节！！**

#### CHAR类型特点

- CHAR 最多可以存储 255 个**字符 (注意不是字节)**，字符有不同的编码集，比如 UTF8 编码 (3字节)、GBK 编码 (2字节) 等。
- 对于 `CHAR(M)` 如果实际存储的数据长度小于M，则 MySQL 会自动会在它的右边用空格字符补足，但是在检索操作中那些填补出来的空格字符会被去掉。

#### VARCHAR类型特点

- VARCHAR 的最大长度为 65535 个**字节**。
- VARCHAR 存储的是实际的字符串加1或2个字节用来记录字符串实际长度，字符串长度小于255字节用1字节记录，超过255就需要2字节记录。[^12 ]

### VARCHAR(50) 能存放几个 UTF8 编码的汉字？

存放的汉字个数与版本相关。

mysql 4.0以下版本，varchar(50) 指的是 50 **字节**，如果存放 UTF8 格式编码的汉字时（每个汉字3字节），只能存放16 个。

mysql 5.0以上版本，varchar(50) 指的是 50 **字符**，无论存放的是数字、字母还是 UTF8 编码的汉字，都可以存放 50 个。

### int(10) 和 bigint(10)能存储的数据大小一样吗？

不一样，具体原因如下：

- int 能存储四字节有符号整数。
- bigint 能存储八字节有符号整数。

所以能存储的数据大小不一样，其中的数字 `10` 代表的只是数据的显示宽度。[^13]

- 显示宽度指明Mysql最大可能显示的数字个数，数值的位数小于指定的宽度时数字左边会用**空格填充**，空格不容易看出。
- 如果插入了大于显示宽度的值，只要该值不超过该类型的取值范围，数值依然可以插入且能够显示出来。
- 建表的时候指定 `zerofill` 选项，则不足显示宽度的部分用 `0` 填充，如果是 1 会显示成 `0000000001`。
- 如果没指定显示宽度， bigint 默认宽度是 20 ，int默认宽度 11。

## 存储引擎相关

### MySQL存储引擎类型有哪些？

常用的存储引擎有 InnoDB 存储引擎和 MyISAM 存储引擎，InnoDB 是 MySQL 的默认事务引擎。

查看数据库表当前支持的引擎，可以用下面查询语句查看 ：

```sql
# 查询结果表中的 Engine 字段指示存储引擎类型。
show table status from 'your_db_name' where name='your_table_name'; 
```

### InnoDB存储引擎应用场景是什么？

InnoDB 是 MySQL的默认「事务引擎」，被设置用来处理大量短期（short-lived）事务，短期事务大部分情况是正常提交的，很少会回滚。

#### InnoDB存储引擎特性有哪些？

采用多版本并发控制（MVCC，MultiVersion Concurrency Control）来支持高并发。并且实现了四个标准的隔离级别，通过间隙锁`next-key locking`策略防止幻读的出现。

引擎的表基于聚簇索引建立，聚簇索引对主键查询有很高的性能。不过它的二级索引`secondary index`非主键索引中必须包含主键列，所以如果主键列很大的话，其他的所有索引都会很大。因此，若表上的索引较多的话，主键应当尽可能的小。另外InnoDB的存储格式是平台独立。

InnoDB做了很多优化，比如：磁盘读取数据方式采用的可预测性预读、自动在内存中创建hash索引以加速读操作的自适应哈希索引（adaptive hash index)，以及能够加速插入操作的插入缓冲区（insert buffer)等。

InnoDB通过一些机制和工具支持真正的热备份，MySQL 的其他存储引擎不支持热备份，要获取一致性视图需要停止对所有表的写入，而在读写混合场景中，停止写入可能也意味着停止读取。

- 冷备(cold backup)：需要关mysql服务，读写请求均不允许状态下进行；

- 温备(warm backup)： 服务在线，但仅支持读请求，不允许写请求；

- 热备(hot backup)：备份的同时，业务不受影响。

### InnoDB  引擎的四大特性是什么？

#### 插入缓冲（Insert buffer)

Insert Buffer 用于非聚集索引的插入和更新操作。先判断插入的非聚集索引是否在缓存池中，如果在则直接插入，否则插入到 Insert Buffer 对象里。再以一定的频率进行 Insert Buffer 和辅助索引叶子节点的 merge 操作，将多次插入合并到一个操作中，提高对非聚集索引的插入性能。

#### 二次写 (Double write)

Double Write由两部分组成，一部分是内存中的double write buffer，大小为2MB，另一部分是物理磁盘上共享表空间连续的128个页，大小也为 2MB。在对缓冲池的脏页进行刷新时，并不直接写磁盘，而是通过 memcpy 函数将脏页先复制到内存中的该区域，之后通过double write buffer再分两次，每次1MB顺序地写入共享表空间的物理磁盘上，然后马上调用fsync函数，同步磁盘，避免操作系统缓冲写带来的问题。

#### 自适应哈希索引 (Adaptive Hash Index)

InnoDB会根据访问的频率和模式，为热点页建立哈希索引，来提高查询效率。索引通过缓存池的 B+ 树页构造而来，因此建立速度很快，InnoDB存储引擎会监控对表上各个索引页的查询，如果观察到建立哈希索引可以带来速度上的提升，则建立哈希索引，所以叫做自适应哈希索引。

#### 缓存池

为了提高数据库的性能，引入缓存池的概念，通过参数 `innodb_buffer_pool_size` 可以设置缓存池的大小，参数 `innodb_buffer_pool_instances` 可以设置缓存池的实例个数。缓存池主要用于存储以下内容：

缓冲池中缓存的数据页类型有：索引页、数据页、undo页、插入缓冲 (insert buffer)、自适应哈希索引(adaptive hash index)、InnoDB存储的锁信息 (lock info)和数据字典信息 (data dictionary)。

### MyISAM存储引擎应用场景有哪些？

MyISAM 是 MySQL 5.1 及之前的版本的默认的存储引擎。MyISAM 提供了大量的特性，包括全文索引、压缩、空间函数（GIS)等，但MyISAM 不「支持事务和行级锁」，对于只读数据，或者表比较小、可以容忍修复操作，依然可以使用它。

### MyISAM存储引擎特性有哪些？

MyISAM「不支持行级锁而是对整张表加锁」。读取时会对需要读到的所有表加共享锁，写入时则对表加排它锁。但在表有读取操作的同时，也可以往表中插入新的记录，这被称为并发插入。

MyISAM 表可以手工或者自动执行检查和修复操作。但是和事务恢复以及崩溃恢复不同，可能导致一些「数据丢失」，而且修复操作是非常慢的。

对于 MyISAM 表，即使是`BLOB`和`TEXT`等长字段，也可以基于其前 500 个字符创建索引，MyISAM 也支持「全文索引」，这是一种基于分词创建的索引，可以支持复杂的查询。

如果指定了`DELAY_KEY_WRITE`选项，在每次修改执行完成时，不会立即将修改的索引数据写入磁盘，而是会写到内存中的键缓冲区，只有在清理键缓冲区或者关闭表的时候才会将对应的索引块写入磁盘。这种方式可以极大的提升写入性能，但是在数据库或者主机崩溃时会造成「索引损坏」，需要执行修复操作。

### MyISAM  与  InnoDB  存储引擎 5 大区别

- InnoDB支持事物，而MyISAM不支持事物
- InnoDB支持行级锁，而MyISAM支持表级锁
- InnoDB支持MVCC, 而MyISAM不支持
- InnoDB支持外键，而MyISAM不支持
- InnoDB不支持全文索引，而MyISAM支持

一张表简单罗列两种引擎的主要区别，如下图：

![图片](https://mmbiz.qpic.cn/mmbiz_png/ceNmtYOhbMSIaAmoSYianlwqUjqymUETljvrMbU5PaMZUOzK2hDwdm9oibJhbslSq5icicsgja5th4S3Y5ZlIuibiajA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)



### SELECT COUNT(*) 在哪个引擎执行更快？

`SELECT COUNT(*)`  常用于统计表的总行数，**在 MyISAM  存储引擎中执行更快，前提是不能加有任何WHERE条件**。

这是因为 MyISAM 对于表的行数做了优化，内部用一个变量存储了表的行数，如果查询条件没有 WHERE 条件则是查询表中一共有多少条数据，MyISAM 可以迅速返回结果，如果加 WHERE 条件就不行。

InnoDB 的表也有一个存储了表行数的变量，但这个值是一个估计值，所以并没有太大实际意义。

## MySQL 基础知识

### 说一下数据库设计三范式是什么？

1范式：1NF是对属性的原子性约束，要求属性具有原子性，不可再分解；(只要是关系型数据库都满足1NF)

2范式：2NF是对记录的惟一性约束，要求记录有惟一标识，即实体的惟一性；（消除部分依赖，针对联合主键）

3范式：3NF是对字段冗余性的约束，即任何字段不能由其他字段派生出来，它要求字段没有冗余。没有冗余的数据库设计可以做到（消除传递依赖）

但是，没有冗余的数据库未必是最好的数据库，有时为了提高运行效率，就必须降低范式标准，适当保留冗余数据，具体做法是：在概念数据模型设计时遵守第三范式，降低范式标准的工作放到物理数据模型设计时考虑，降低范式就是增加字段，允许冗余。

### SQL 语句有哪些分类？

1. DDL：数据定义语言（create alter drop）
2. DML：数据操作语句（insert update delete）
3. DTL：数据事务语句（commit rollback savepoint）
4. DCL：数据控制语句（grant revoke）

### 数据库删除操作中的 delete、drop、truncate 区别在哪？

- 当不再需要该表时可以用 drop 来删除表;
- 当仍要保留该表，但要删除所有记录时， 用 truncate来删除表中记录。
- 当要删除部分记录时（一般来说有 WHERE 子句约束） 用 delete来删除表中部分记录。
- [扩展](https://www.cnblogs.com/weibanggang/p/9590080.html)

### 什么是MySql视图？

视图是虚拟表，并不储存数据，只包含定义时的语句的动态数据。

创建视图语法：

```sql
CREATE
    [OR REPLACE]
    [ALGORITHM = {UNDEFINED | MERGE | TEMPTABLE}]
    [DEFINER = user]
    [SQL SECURITY { DEFINER | INVOKER }]
    VIEW view_name [(column_list)]
    AS select_statement
    [WITH [CASCADED | LOCAL] CHECK OPTION]
```

参数说明：

- OR REPLACE：如果视图存在，则替换已有视图。
- ALGORITHM：视图选择算法，默认算法是 UNDEFINED(未定义的)由 MySQL自动选择要使用的算法。
- DEFINER：指定视图创建者或定义者，如果不指定该选项，则创建视图的用户就是定义者。
- SQL SECURITY：SQL安全性，默认为DEFINER。
- select_statement：创建视图的 SELECT语句，可以从基表或其他视图中选择数据。
- WITH CHECK OPTION：表示视图在更新时保证约束，默认是 CASCADED。

### 使用 MySQL 视图有何优点？

1. 操作简单方便。视图用户完全不需要关心视图对应的表的结构、关联条件和筛选条件，对用户来说已经是过滤好的复合条件的结果集。
2. 数据更加安全。视图用户只能访问视图中的结果集，通过视图可以把对表的访问权限限制在某些行和列上面。
3. 数据隔离。屏蔽了源表结构变化对用户带来的影响，源表结构变化视图结构不变。^1

### MySql服务默认端口号是多少 ？

默认端口是 `3306`

查看端口命令：`> show variables like 'port';`

### 用  DISTINCT  过滤 多列的规则？

DISTINCT 用于对选择的数据去重，单列用法容易理解。比如有如下数据表 `tamb`：

```
   name        number
   Tencent      1
   Alibaba      2
   Bytedance    3
   Meituan      3
```

查询语句：`SELECT DISTINCT name FROM table tamb` 结果如下：

```
   name       
   Tencent   
   Alibaba    
   Bytedance 
   Meituan  
```

如果要求按 `number` 列去重同时显示 `name` ，你可能会写出查询语句：

```sql
SELECT DISTINCT number, name FROM table tamb
```

**多参数 DISTINCT 去重规则是：把 DISTINCT  之后的所有参数当做一个过滤条件，也就是说会对 `(number, name)`整体去重处理，只有当这个组合不同才会去重**，结果如下：

```
       number   name
         1  Tencent
         2  Alibaba
         3  Bytedance
         3  Meituan
```

从结果来看好像并没有达到我们想要的去重的效果，那要怎么实现「按 `number` 列去重同时显示 `name`」呢？可以用 `Group By` 语句：

`SELECT number, name FROM table tamb GROUP BY number` 输出如下，正是我们想要的效果：

```
       number   name
         1  Tencent
         2  Alibaba
         3  Bytedance
```

### 什么是[存储过程](https://www.runoob.com/w3cnote/mysql-stored-procedure.html)？

一条或多条sql语句集合，有以下一些特点：

- 存储过程能实现较快的执行速度。
- 存储过程可以用流程控制语句编写，有很强的灵活性，可以完成复杂的判断和较复杂的运算。
- 存储过程可被作为一种安全机制来充分利用。
- 存储过程能够减少网络流量

```sql
delimiter 分隔符
create procedure|proc proc_name()
begin
    sql语句
end 分隔符
delimiter ;    --还原分隔符，为了不影响后面的语句的使用
--默认的分隔符是 ; 但是为了能在整个存储过程中重用，因此一般需要自定义分隔符（除\外）

show procedure status like ""; --查询存储过程，可以不使用like进行过滤
drop procedure [if exists] <过程名>;--删除存储过程
```

#### 存储过程和函数好像差不多，你说说他们有什么区别?

存储过程和函数是事先经过编译并存储在数据库中的一段 SQL 语句的集合，调用存储过程和函数可以简化应用开发人员的很多工作，减少数据在数据库和应用服务器之间的传输，对于提高数据处理的效率是有好处的。

**相同点**

- 存储过程和函数都是为了可重复的执行操作数据库的 SQL 语句的集合。
- 存储过程和函数都是一次编译后缓存起来，下次使用就直接命中已经编译好的 sql 语句，减少网络交互提高了效率。

**不同点**

- 标识符不同，函数的标识符是 function，存储过程是 procedure。
- 函数返回单个值或者表对象，而存储过程没有返回值，但是可以通过OUT参数返回多个值。
- 函数限制比较多，比如不能用临时表，只能用表变量，一些函数都不可用等，而存储过程的限制相对就比较少。
- 一般来说，存储过程实现的功能要复杂一点，而函数的实现的功能针对性比较强
- 函数的参数只能是 IN 类型，存储过程的参数可以是`IN OUT INOUT`三种类型。
- 存储函数使用 select 调用，存储过程需要使用 call 调用。

## 总结一下

本文以面试问答形式总结了一系列面试常见的基础知识点，都是非常基础的内容，但越是基础越显得重要，可以作为知识点笔记，时常拿出来复习温故而知新。

---

# [数据库自增ID用完了会怎样](https://mp.weixin.qq.com/s/WqM5mhnLOqZhcdzPLeWh5w)

看到这个问题，我想起当初玩魔兽世界的时候，25H难度的脑残吼的血量已经超过了21亿，所以那时候副本的BOSS都设计成了转阶段、回血的模式，因为魔兽的血量是int型，不能超过2^32大小。

估计暴雪的设计师都没想到几个资料片下来血量都超过int上限了，以至于大家猜想才会有后来的属性压缩。

这些都是题外话，只是告诉你数据量大了是有可能达到上限的而已，回到Mysql自增ID上限的问题，可以分为两种情况来说。以后面试再遇到这个题目，可以跟面试官好好吹一吹了。

### 1. 有主键

如果设置了主键，并且一般会把主键设置成自增。

我们知道，Mysql里int类型是4个字节，如果有符号位的话就是[-2^31^,2^31^-1]，无符号位的话最大值就是2^32^-1，也就是4294967295。

创建一张表试试：

```sql
CREATE TABLE `test1` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
 `name` varchar(32) NOT NULL DEFAULT '',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2147483647 DEFAULT CHARSET=utf8mb4;
```

然后执行插入

```sql
insert into test1(name) values('qq');
```

这样表里就有一条达到有符号位的最大值上限的数据。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/ibBMVuDfkZUkPPolNxC0VQkRYTBO8prseoNYEOiag3ibKHtiaiaAWYWzK2SkevA4g5cdn6W5CH29DFDrZIlXPOHPLxA/640?wx_fmt=jpeg&wxfrom=5&wx_lazy=1&wx_co=1)

如果再次执行插入语句：

```sql
insert into test1(name) values('ww');
```

就会看到错误提示：`1062 - Duplicate entry '2147483647' for key 'PRIMARY', Time: 0.000000s`。

也就是说，如果设置了主键并且自增的话，达到自增主键上限就会报错重复的主键key。

解决方案，mysql主键改为bigint，也就是8个字节。

设计的时候要考虑清楚值的上限是多少，如果业务频繁插入的话，21亿的数字其实还是有可能达到的。

### 2. 没有主键

如果没有设置主键的话，InnoDB则会自动帮你创建一个6个字节的row_id，由于row_id是无符号的，所以最大长度是2^48^-1。

同样创建一张表作为测试：

```sql
CREATE TABLE `test2` (
 `name` varchar(32) NOT NULL DEFAULT ''
) ENGINE=InnoDB  DEFAULT CHARSET=utf8mb4;
```

通过`ps -ef|grep mysql`拿到mysql的进程ID，然后执行命令，通过gdb先把row_id修改为1

```bash
sudo gdb -p 2584 -ex 'p dict_sys->row_id=1' -batch
```

然后插入几条数据：

```sql
insert into test2(name) values('1');
insert into test2(name) values('2');
insert into test2(name) values('3');
```

再次修改row_id为2^48^，也就是281474976710656

```bash
sudo gdb -p 2584 -ex 'p dict_sys->row_id=281474976710656' -batch
```

再次插入数据

```sql
insert into test2(name) values('4');
insert into test2(name) values('5');
insert into test2(name) values('6');
```

然后查询数据会发现4条数据，分别是4，5，6，3。

因为我们先设置row_id=1开始，所以1，2，3的row_id也是1，2，3。

修改row_id为上限值之后，row_id会从0重新开始计算，所以4，5，6的row_id就是0，1，2。

由于1，2数据已经存在，`数据则是会被覆盖`。

### 总 结

自增ID达到上限用完了之后，分为两种情况：

1. 如果设置了主键，那么将会报错主键冲突。
2. 如果没有设置主键，数据库则会帮我们自动生成一个全局的row_id，新数据会覆盖老数据

解决方案：

表尽可能都要设置主键，主键尽量使用bigint类型，21亿的上限还是有可能达到的，比如魔兽，虽然说row_id上限高达281万亿，但是覆盖数据显然是不可接受的。

![图片](https://mmbiz.qpic.cn/mmbiz_gif/b96CibCt70iabwjyojLhA03PtxUnkNPREnt2F48ywfXLpDdDAjicOTPI8Q94tVLbJ58tbRs12iaXDKhUOW9gd4NlFA/640?wx_fmt=gif&wxfrom=5&wx_lazy=1)

---

# [如何保证缓存和数据库的一致性问题？](https://mp.weixin.qq.com/s/RDOKLnG7P1j5Ehu3EyrsCQ)

最近面试季节，估计「如何保证缓存和数据库一致性」这个问题经常会被问到，这是一个老生常谈的话题了。

但很多人对这个问题，依旧有很多疑惑：

- 到底是更新缓存还是删缓存？
- 到底选择先更新数据库，再删除缓存，还是先删除缓存，再更新数据库？
- 为什么要引入消息队列保证一致性？
- 延迟双删会有什么问题？到底要不要用？
- ...

这篇文章，我们就来把这些问题讲清楚。

**这篇文章干货很多，希望你可以耐心读完。**

![图片](https://mmbiz.qpic.cn/mmbiz_png/gB9Yvac5K3OezNCibL5S9oyeYqJBQVZCo41CpvfXVh4yAMdkW8giaHjxcLWWicuEn5zejdZoLsr9LjXH5yGsSjSxQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

## 引入缓存提高性能

我们从最简单的场景开始讲起。

如果你的业务处于起步阶段，流量非常小，那无论是读请求还是写请求，直接操作数据库即可，这时你的架构模型是这样的：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/gB9Yvac5K3OezNCibL5S9oyeYqJBQVZCo1NdGKV0ptDl4ZsCXKfwXLl1Kp35aOG1Iku9K2EJ8Y7v5bM91jK5NLQ/640?wx_fmt=jpeg&wxfrom=5&wx_lazy=1&wx_co=1)

但随着业务量的增长，你的项目请求量越来越大，这时如果每次都从数据库中读数据，那肯定会有性能问题。

这个阶段通常的做法是，引入「缓存」来提高读性能，架构模型就变成了这样：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/gB9Yvac5K3OezNCibL5S9oyeYqJBQVZCo3lYxFg1icDgHngialHe8ibUDKCvfib4DmTMo36wJv0FeZ5ex0kId1LTOpw/640?wx_fmt=jpeg&wxfrom=5&wx_lazy=1&wx_co=1)

当下优秀的缓存中间件，当属 Redis 莫属，它不仅性能非常高，还提供了很多友好的数据类型，可以很好地满足我们的业务需求。

但引入缓存之后，你就会面临一个问题：**之前数据只存在数据库中，现在要放到缓存中读取，具体要怎么存呢？**

最简单直接的方案是「全量数据刷到缓存中」：

- 数据库的数据，全量刷入缓存（不设置失效时间）
- 写请求只更新数据库，不更新缓存
- 启动一个定时任务，定时把数据库的数据，更新到缓存中

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

这个方案的优点是，所有读请求都可以直接「命中」缓存，不需要再查数据库，性能非常高。

但缺点也很明显，有 2 个问题：

1. **缓存利用率低**：不经常访问的数据，还一直留在缓存中
2. **数据不一致**：因为是「定时」刷新缓存，缓存和数据库存在不一致（取决于定时任务的执行频率）

所以，这种方案一般更适合业务「体量小」，且对数据一致性要求不高的业务场景。

那如果我们的业务体量很大，怎么解决这 2 个问题呢？

## 缓存利用率和一致性问题

### 缓存利用率

先来看第一个问题，如何提高缓存利用率？

想要缓存利用率「最大化」，我们很容易想到的方案是，缓存中只保留最近访问的「热数据」。但具体要怎么做呢？

我们可以这样优化：

- 写请求依旧只写数据库
- 读请求先读缓存，如果缓存不存在，则从数据库读取，并重建缓存
- 同时，写入缓存中的数据，都设置失效时间

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/gB9Yvac5K3OezNCibL5S9oyeYqJBQVZCo8ic8WbuqDQcJ2bVyia7t9rOu9CGyJnCkWQs16WNibAwdV0GbH3q0K6ZMw/640?wx_fmt=jpeg&wxfrom=5&wx_lazy=1&wx_co=1)

这样一来，缓存中不经常访问的数据，随着时间的推移，都会逐渐「过期」淘汰掉，最终缓存中保留的，都是经常被访问的「热数据」，缓存利用率得以最大化。



### 数据一致性

再来看数据一致性问题。

要想保证缓存和数据库「实时」一致，那就不能再用定时任务刷新缓存了。

所以，当数据发生更新时，我们不仅要操作数据库，还要一并操作缓存。具体操作就是，修改一条数据时，不仅要更新数据库，也要连带缓存一起更新。

但数据库和缓存都更新，又存在先后问题，那对应的方案就有 2 个：

1. 先更新缓存，后更新数据库
2. 先更新数据库，后更新缓存

哪个方案更好呢？

先不考虑并发问题，正常情况下，无论谁先谁后，都可以让两者保持一致，但现在我们需要重点考虑「异常」情况。

因为操作分为两步，那么就很有可能存在「第一步成功、第二步失败」的情况发生。

这 2 种方案我们一个个来分析。

#### **1) 先更新缓存，后更新数据库**

如果缓存更新成功了，但数据库更新失败，那么此时缓存中是最新值，但数据库中是「旧值」。

虽然此时读请求可以命中缓存，拿到正确的值，但是，一旦缓存「失效」，就会从数据库中读取到「旧值」，重建缓存也是这个旧值。

这时用户会发现自己之前修改的数据又「变回去」了，对业务造成影响。

#### **2) 先更新数据库，后更新缓存**

如果数据库更新成功了，但缓存更新失败，那么此时数据库中是最新值，缓存中是「旧值」。

之后的读请求读到的都是旧数据，只有当缓存「失效」后，才能从数据库中得到正确的值。

这时用户会发现，自己刚刚修改了数据，但却看不到变更，一段时间过后，数据才变更过来，对业务也会有影响。

可见，无论谁先谁后，但凡后者发生异常，就会对业务造成影响。那怎么解决这个问题呢？

别急，后面我会详细给出对应的解决方案。

我们继续分析，除了操作失败问题，还有什么场景会影响数据一致性？

这里我们还需要重点关注：**`并发问题`**。

## 并发引发的一致性问题

假设我们采用「先更新数据库，再更新缓存」的方案，并且两步都可以「成功执行」的前提下，如果存在并发，情况会是怎样的呢？

有线程 A 和线程 B 两个线程，需要更新「同一条」数据，会发生这样的场景：

1. 线程 A 更新数据库（X = 1）
2. 线程 B 更新数据库（X = 2）
3. 线程 B 更新缓存（X = 2）
4. 线程 A 更新缓存（X = 1）

最终 X 的值在缓存中是 1，在数据库中是 2，发生不一致。

也就是说，A 虽然先于 B 发生，但 B 操作数据库和缓存的时间，却要比 A 的时间短，执行时序发生「错乱」，最终这条数据结果是不符合预期的。

> 同样地，采用「先更新缓存，再更新数据库」的方案，也会有类似问题，这里不再详述。

除此之外，我们从「缓存利用率」的角度来评估这个方案，也是不太推荐的。

这是因为每次数据发生变更，都「无脑」更新缓存，但是缓存中的数据不一定会被「马上读取」（能否在读取缓存数据时与数据库同步一次呢？），这就会导致缓存中可能存放了很多不常访问的数据，浪费缓存资源。

而且很多情况下，写到缓存中的值，并不是与数据库中的值一一对应的，很有可能是先查询数据库，再经过一系列「计算」得出一个值，才把这个值才写到缓存中。

由此可见，这种「更新数据库 + 更新缓存」的方案，不仅缓存利用率不高，还会造成机器性能的浪费。

所以此时我们需要考虑另外一种方案：**删除缓存**。

## 删除缓存可以保证一致性吗？

删除缓存对应的方案也有 2 种：

1. 先删除缓存，后更新数据库
2. 先更新数据库，后删除缓存

经过前面的分析我们已经得知，但凡「第二步」操作失败，都会导致数据不一致。

这里我不再详述具体场景，你可以按照前面的思路推演一下，就可以看到依旧存在数据不一致的情况。

这里我们重点来看「并发」问题。

### **1) 先删除缓存，后更新数据库**

如果有 2 个线程要并发「读写」数据，可能会发生以下场景：

1. 线程 A 要更新 X = 2（原值 X = 1）
2. 线程 A 先删除缓存
3. 线程 B 读缓存，发现不存在，从数据库中读取到旧值（X = 1）
4. 线程 A 将新值写入数据库（X = 2）
5. 线程 B 将旧值写入缓存（X = 1）

最终 X 的值在缓存中是 1（旧值），在数据库中是 2（新值），发生不一致。

可见，先删除缓存，后更新数据库，当发生「读+写」并发时，还是存在数据不一致的情况。

### **2) 先更新数据库，后删除缓存**

依旧是 2 个线程并发「读写」数据：

1. 缓存中 X 不存在（数据库 X = 1）
2. 线程 A 读取数据库，得到旧值（X = 1）
3. 线程 B 更新数据库（X = 2)
4. 线程 B 删除缓存
5. 线程 A 将旧值写入缓存（X = 1）

最终 X 的值在缓存中是 1（旧值），在数据库中是 2（新值），也发生不一致。

这种情况「理论」来说是可能发生的，但实际真的有可能发生吗？

其实概率「很低」，这是因为它必须满足 3 个条件：

1. 缓存刚好已失效
2. 读请求 + 写请求并发
3. 更新数据库 + 删除缓存的时间（步骤 3-4），要比读数据库 + 写缓存时间短（步骤 2 和 5）

仔细想一下，条件 3 发生的概率其实是非常低的。

因为写数据库一般会先`「加锁」`，所以写数据库，通常是要比读数据库的时间更长的。

这么来看，「先更新数据库 + 再删除缓存」的方案，是可以保证数据一致性的。

所以，我们应该采用这种方案，来操作数据库和缓存。

好，解决了并发问题，我们继续来看前面遗留的，**第二步执行「失败」导致数据不一致的问题**。

## 如何保证两步都执行成功？

前面我们分析到，无论是更新缓存还是删除缓存，只要第二步发生失败，那么就会导致数据库和缓存不一致。

**保证第二步成功执行，就是解决问题的关键。**

想一下，程序在执行过程中发生异常，最简单的解决办法是什么？

答案是：**重试**。

是的，其实这里我们也可以这样做。

无论是先操作缓存，还是先操作数据库，但凡后者执行失败了，我们就可以发起重试，尽可能地去做「补偿」。

那这是不是意味着，只要执行失败，我们「无脑重试」就可以了呢？

答案是否定的。现实情况往往没有想的这么简单，失败后立即重试的问题在于：

- 立即重试很大概率「还会失败」
- 「重试次数」设置多少才合理？
- 重试会一直「占用」这个线程资源，无法服务其它客户端请求

看到了么，虽然我们想通过重试的方式解决问题，但这种「同步」重试的方案依旧不严谨。

那更好的方案应该怎么做？

答案是：**`异步重试`**。什么是异步重试？

### 异步重试

其实就是把重试请求写到「消息队列」中，然后由专门的消费者来重试，直到成功。

或者更直接的做法，为了避免第二步执行失败，我们可以把操作缓存这一步，直接放到消息队列中，由消费者来操作缓存。

到这里你可能会问，写消息队列也有可能会失败啊？而且，引入消息队列，这又增加了更多的维护成本，这样做值得吗？

这个问题很好，但我们思考这样一个问题：如果在执行失败的线程中一直重试，还没等执行成功，此时如果项目「重启」了，那这次重试请求也就「丢失」了，那这条数据就一直不一致了。

所以，这里我们必须把重试或第二步操作放到另一个「服务」中，这个服务用「消息队列」最为合适。这是因为消息队列的特性，正好符合我们的需求：

- **消息队列保证可靠性**：写到队列中的消息，成功消费之前不会丢失（重启项目也不担心）
- **消息队列保证消息成功投递**：下游从队列拉取消息，成功消费后才会删除消息，否则还会继续投递消息给消费者（符合我们重试的场景）

至于写队列失败和消息队列的维护成本问题：

- **写队列失败**：操作缓存和写消息队列，「同时失败」的概率其实是很小的
- **维护成本**：我们项目中一般都会用到消息队列，维护成本并没有新增很多

所以，引入消息队列来解决这个问题，是比较合适的。这时架构模型就变成了这样：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/gB9Yvac5K3OezNCibL5S9oyeYqJBQVZCoNNyZnraolIYC8NntRZu8R0VpQp0iaXsohT5gjf4QpV0biah4iaRiaHOcyw/640?wx_fmt=jpeg&wxfrom=5&wx_lazy=1&wx_co=1)

那如果你确实不想在应用中去写消息队列，是否有更简单的方案，同时又可以保证一致性呢？

方案还是有的，这就是近几年比较流行的解决方案：**`订阅数据库变更日志，再操作缓存`**。

### 订阅变更日志

具体来讲就是，我们的业务应用在修改数据时，「只需」修改数据库，无需操作缓存。

那什么时候操作缓存呢？这就和数据库的「变更日志」有关了。

拿 MySQL 举例，当一条数据发生修改时，MySQL 就会产生一条变更日志（binlog），我们可以订阅这个日志，拿到具体操作的数据，然后再根据这条数据，去删除对应的缓存。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/gB9Yvac5K3OezNCibL5S9oyeYqJBQVZCowjAarwD2g3lIfCPsvhEHGaohPHVa47GR9d1GUgj0Eta1ketClKZjfw/640?wx_fmt=jpeg&wxfrom=5&wx_lazy=1&wx_co=1)

订阅变更日志，目前也有了比较成熟的开源中间件，例如阿里的 [canal](https://github.com/alibaba/canal/)，使用这种方案的优点在于：

- **无需考虑写消息队列失败情况**：只要写 MySQL 成功，binlog 肯定会有
- **自动投递到下游队列**：canal 自动把数据库变更日志「投递」给下游的消息队列

当然，与此同时，我们需要投入精力去维护 canal 的高可用和稳定性。

> 如果你有留意观察很多数据库的特性，就会发现其实很多数据库都逐渐开始提供「订阅变更日志」的功能了，相信不远的将来，我们就不用通过中间件来拉取日志，自己写程序就可以订阅变更日志了，这样可以进一步简化流程。

至此，我们可以得出结论，想要保证数据库和缓存一致性，**推荐采用「先更新数据库，再删除缓存」方案，并配合「消息队列」或「订阅变更日志」的方式来做**。

## 主从库延迟和延迟双删问题

到这里，还有 2 个问题，是我们没有重点分析过的。

**第一个问题**，还记得前面讲到的「先删除缓存，再更新数据库」方案，导致不一致的场景么？

这里我再把例子拿过来让你复习一下：

2 个线程要并发「读写」数据，可能会发生以下场景：

1. 线程 A 要更新 X = 2（原值 X = 1）
2. 线程 A 先删除缓存
3. 线程 B 读缓存，发现不存在，从数据库中读取到旧值（X = 1）
4. 线程 A 将新值写入数据库（X = 2）
5. 线程 B 将旧值写入缓存（X = 1）

最终 X 的值在缓存中是 1（旧值），在数据库中是 2（新值），发生不一致。

**第二个问题**：是关于「读写分离 + 主从复制延迟」情况下，缓存和数据库一致性的问题。

在「先更新数据库，再删除缓存」方案下，「读写分离 + 主从库延迟」其实也会导致不一致：

1. 线程 A 更新主库 X = 2（原值 X = 1）
2. 线程 A 删除缓存
3. 线程 B 查询缓存，没有命中，查询「从库」得到旧值（从库 X = 1）
4. 从库「同步」完成（主从库 X = 2）
5. 线程 B 将「旧值」写入缓存（X = 1）

最终 X 的值在缓存中是 1（旧值），在主从库中是 2（新值），也发生不一致。

看到了么？这 2 个问题的核心在于：**缓存都被回种了「旧值」**。

那怎么解决这类问题呢？

最有效的办法就是，**把缓存删掉**。

但是，不能立即删，而是需要「延迟删」，这就是业界给出的方案：**缓存延迟双删策略**。

按照延时双删策略，这 2 个问题的解决方案是这样的：

**解决第一个问题**：在线程 A 删除缓存、更新完数据库之后，先「休眠一会」，再「删除」一次缓存。

**解决第二个问题**：线程 A 可以生成一条「延时消息」，写到消息队列中，消费者延时「删除」缓存。

这两个方案的目的，都是为了把缓存清掉，这样一来，下次就可以从数据库读取到最新值，写入缓存。

但问题来了，这个「延迟删除」缓存，延迟时间到底设置要多久呢？

- 问题1：延迟时间要大于「主从复制」的延迟时间
- 问题2：延迟时间要大于线程 B 读取数据库 + 写入缓存的时间

但是，**这个时间在分布式和高并发场景下，其实是很难评估的**。

很多时候，我们都是凭借经验大致估算这个延迟时间，例如延迟 1-5s，只能尽可能地降低不一致的概率。

所以你看，采用这种方案，也只是尽可能保证一致性而已，极端情况下，还是有可能发生不一致。

所以实际使用中，我还是建议你采用「先更新数据库，再删除缓存」的方案，同时，要尽可能地保证「主从复制」不要有太大延迟，降低出问题的概率。

## 可以做到强一致吗？

看到这里你可能会想，这些方案还是不够完美，我就想让缓存和数据库「强一致」，到底能不能做到呢？

其实很难。

要想做到强一致，最常见的方案是 2PC、3PC、Paxos、Raft 这类一致性协议，但它们的性能往往比较差，而且这些方案也比较复杂，还要考虑各种容错问题。

相反，这时我们换个角度思考一下，我们引入缓存的目的是什么？

没错，**性能**。

一旦我们决定使用缓存，那必然要面临一致性问题。性能和一致性就像天平的两端，无法做到都满足要求。

而且，就拿我们前面讲到的方案来说，当操作数据库和缓存完成之前，只要有其它请求可以进来，都有可能查到「中间状态」的数据。

所以如果非要追求强一致，那必须要求所有更新操作完成之前期间，不能有「任何请求」进来。

虽然我们可以通过加「分布锁」的方式来实现，但我们要付出的代价，很可能会超过引入缓存带来的性能提升。

所以，既然决定使用缓存，就必须容忍「一致性」问题，我们只能尽可能地去降低问题出现的概率。

同时我们也要知道，缓存都是有「失效时间」的，就算在这期间存在短期不一致，我们依旧有失效时间来兜底，这样也能达到最终一致。

## 总结

好了，总结一下这篇文章的重点。

1、想要提高应用的性能，可以引入「缓存」来解决

2、引入缓存后，需要考虑缓存和数据库一致性问题，可选的方案有：「更新数据库 + 更新缓存」、「更新数据库 + 删除缓存」

3、更新数据库 + 更新缓存方案，在「并发」场景下无法保证缓存和数据一致性，且存在「缓存资源浪费」和「机器性能浪费」的情况发生

4、在更新数据库 + 删除缓存的方案中，「先删除缓存，再更新数据库」在「并发」场景下依旧有数据不一致问题，解决方案是「延迟双删」，但这个延迟时间很难评估，所以推荐用「先更新数据库，再删除缓存」的方案

5、在「先更新数据库，再删除缓存」方案下，为了保证两步都成功执行，需配合「消息队列」或「订阅变更日志」的方案来做，本质是通过「重试」的方式保证数据一致性

6、在「先更新数据库，再删除缓存」方案下，「读写分离 + 主从库延迟」也会导致缓存和数据库不一致，缓解此问题的方案是「延迟双删」，凭借经验发送「延迟消息」到队列中，延迟删除缓存，同时也要控制主从库延迟，尽可能降低不一致发生的概率

## 后记

本以为这个老生常谈的话题，写起来很好写，没想到在写的过程中，还是挖到了很多之前没有深度思考过的细节。

在这里我也分享 4 点心得给你：

1、性能和一致性不能同时满足，为了性能考虑，通常会采用「最终一致性」的方案

2、掌握缓存和数据库一致性问题，核心问题有 3 点：缓存利用率、并发、缓存 + 数据库一起成功问题

3、失败场景下要保证一致性，常见手段就是「重试」，同步重试会影响吞吐量，所以通常会采用异步重试的方案

4、订阅变更日志的思想，本质是把权威数据源（例如 MySQL）当做 leader 副本，让其它异质系统（例如 Redis / Elasticsearch）成为它的 follower 副本，通过同步变更日志的方式，保证 leader 和 follower 之间保持一致

很多一致性问题，都会采用这些方案来解决，希望我的这些心得对你有所启发。

![图片](https://mmbiz.qpic.cn/mmbiz_gif/b96CibCt70iabwjyojLhA03PtxUnkNPREnt2F48ywfXLpDdDAjicOTPI8Q94tVLbJ58tbRs12iaXDKhUOW9gd4NlFA/640?wx_fmt=gif&wxfrom=5&wx_lazy=1)

---

# [面试官最爱的MySQL连环问](https://mp.weixin.qq.com/s/8ddEzG-NzzFD35ehvbER7A)

## 数据库架构部分

> 说说MySQL 的基础架构图

给面试官讲一下 MySQL 的逻辑架构，有白板可以把下面的图画一下，图片来源于网络。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoEjUXOnibfMR6z5w1hgaU4bLRqA6aBLL7or6fCiakH098NBFpVicOAANZ3s72EYyGzy9Noj1Tr81ibjw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

Mysql逻辑架构图主要分三层：

（1）第一层负责连接处理，授权认证，安全等等 

（2）第二层负责编译并优化SQL 

（3）第三层是存储引擎。

> 一条SQL查询语句在MySQL中如何执行的？

- 先检查该语句`是否有权限`，如果没有权限，直接返回错误信息，如果有权限会先查询缓存(MySQL8.0 版本以前)。
- 如果没有缓存，分析器进行`词法分析`，提取 sql 语句中 select 等关键元素，然后判断 sql 语句是否有语法错误，比如关键词是否正确等等。
- 最后优化器确定执行方案进行权限校验，如果没有权限就直接返回错误信息，如果有权限就会`调用数据库引擎接口`，返回执行结果。

## SQL 优化

> 日常工作中你是怎么优化SQL的？

可以从这几个维度回答这个问题：

### **1、优化表结构**

（1）尽量使用数字型字段

若只含数值信息的字段尽量不要设计为字符型，这会降低查询和连接的性能，并会增加存储开销。这是因为引擎在处理查询和连接时会逐个比较字符串中每一个字符，而对于数字型而言只需要比较一次就够了。

（2）尽可能的使用 varchar 代替 char

变长字段存储空间小，可以节省存储空间。

（3）当索引列大量重复数据时，可以把索引删除掉

比如有一列是性别，几乎只有男、女、未知，这样的索引是无效的。

### 2、应尽量避免在 where 子句中使用!=或<>操作符

- 应尽量避免在 where 子句中使用 or 来连接条件
- 任何查询也不要出现select *
- 避免在 where 子句中对字段进行 null 值判断

### **3、索引优化**

- 对作为查询条件和 order by的字段建立索引
- 避免建立过多的索引，多使用组合索引

> 怎么看执行计划（explain），如何理解其中各个字段的含义？

在 select 语句之前增加 explain 关键字，会返回执行计划的信息。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoEjUXOnibfMR6z5w1hgaU4bTSo3ibANJPSGITPxksHrN4lLuviaFrsk69ThloqZz7rs2a2ibicuBRvXTw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)

（1）id 列：是 select 语句的序号，MySQL将 select 查询分为简单查询和复杂查询。

（2）select_type列：表示对应行是是简单还是复杂的查询。

（3）table 列：表示 explain 的一行正在访问哪个表。

（4）type 列：最重要的列之一。表示关联类型或访问类型，即 MySQL 决定如何查找表中的行。从最优到最差分别为：system > const > eq_ref > ref > fulltext > ref_or_null > index_merge > unique_subquery > index_subquery > range > index > ALL

（5）possible_keys 列：显示查询可能使用哪些索引来查找。

（6）key 列：这一列显示 mysql 实际采用哪个索引来优化对该表的访问。

（7）key_len 列：显示了mysql在索引里使用的字节数，通过这个值可以算出具体使用了索引中的哪些列。

（8）ref 列：这一列显示了在key列记录的索引中，表查找值所用到的列或常量，常见的有：const（常量），func，NULL，字段名。

（9）rows 列：这一列是 mysql 估计要读取并检测的行数，注意这个不是结果集里的行数。

（10）Extra 列：显示额外信息。比如有 Using index、Using where、Using temporary等。

> 关心过业务系统里面的sql耗时吗？统计过慢查询吗？对慢查询都怎么优化过？

我们平时写sql时，都要养成用explain分析的习惯。慢查询的统计，运维会定期统计给我们

优化慢查询思路：

- 分析语句，是否加载了不必要的字段/数据
- 分析 SQL 执行句话，是否命中索引等
- 如果 SQL 很复杂，优化 SQL 结构
- 如果表数据量太大，考虑分表

## 索引

> 聚集索引与非聚集索引的区别

可以按以下四个维度回答：

（1）一个表中只能拥有一个聚集索引，而非聚集索引一个表可以存在多个。

（2）聚集索引，索引中键值的逻辑顺序决定了表中相应行的物理顺序；非聚集索引，索引中索引的逻辑顺序与磁盘上行的物理存储顺序不同。

（3）索引是通过二叉树的数据结构来描述的，我们可以这么理解聚簇索引：索引的叶节点就是数据节点。而非聚簇索引的叶节点仍然是索引节点，只不过有一个指针指向对应的数据块。

（4）聚集索引：物理存储按照索引排序；非聚集索引：物理存储不按照索引排序；

> 为什么要用 B+ 树，为什么不用普通二叉树？

可以从几个维度去看这个问题，查询是否够快，效率是否稳定，存储数据多少，以及查找磁盘次数，为什么不是普通二叉树，为什么不是平衡二叉树，为什么不是B树，而偏偏是 B+ 树呢？

（1）为什么不是普通二叉树？

如果二叉树特殊化为一个链表，相当于全表扫描。平衡二叉树相比于二叉查找树来说，查找效率更稳定，总体的查找速度也更快。

（2）为什么不是平衡二叉树呢？

我们知道，在内存比在磁盘的数据，查询效率快得多。如果树这种数据结构作为索引，那我们每查找一次数据就需要从磁盘中读取一个节点，也就是我们说的一个磁盘块，但是平衡二叉树可是每个节点只存储一个键值和数据的，如果是B树，可以存储更多的节点数据，树的高度也会降低，因此读取磁盘的次数就降下来啦，查询效率就快啦。

（3）为什么不是 B 树而是 B+ 树呢？

B+ 树非叶子节点上是不存储数据的，仅存储键值，而B树节点中不仅存储键值，也会存储数据。innodb中页的默认大小是16KB，如果不存储数据，那么就会存储更多的键值，相应的树的阶数（节点的子节点树）就会更大，树就会更矮更胖，如此一来我们查找数据进行磁盘的IO次数有会再次减少，数据查询的效率也会更快。

B+ 树索引的所有数据均存储在叶子节点，而且数据是按照顺序排列的，链表连着的。那么 B+ 树使得范围查找，排序查找，分组查找以及去重查找变得异常简单。

> Hash 索引和 B+ 树索引区别是什么？你在设计索引是怎么抉择的？

- B+ 树可以进行范围查询，Hash 索引不能。
- B+ 树支持联合索引的最左侧原则，Hash 索引不支持。
- B+ 树支持 order by 排序，Hash 索引不支持。
- Hash 索引在等值查询上比 B+ 树效率更高。
- B+ 树使用 like 进行模糊查询的时候，like 后面（比如%结尾）的话可以起到优化的作用，Hash 索引根本无法进行模糊查询。

> 什么是最左前缀原则？什么是最左匹配原则？

最左前缀原则，就是最左优先，在创建多列索引时，要根据业务需求，where 子句中使用最频繁的一列放在最左边。

当我们创建一个组合索引的时候，如 (a1,a2,a3)，相当于创建了（a1）、(a1,a2)和(a1,a2,a3)三个索引，这就是最左匹配原则。

> 索引不适合哪些场景?

- 数据量少的不适合加索引
- 更新比较频繁的也不适合加索引 = 区分度低的字段不适合加索引（如性别）

> 索引有哪些优缺点？

(1) 优点：

- 唯一索引可以保证数据库表中每一行的数据的唯一性
- 索引可以加快数据查询速度，减少查询时间

(2)缺点：

- 创建索引和维护索引要耗费时间
- 索引需要占物理空间，除了数据表占用数据空间之外，每一个索引还要占用一定的物理空间
- 以表中的数据进行增、删、改的时候，索引也要动态的维护。

## 锁

> MySQL 遇到过死锁问题吗，你是如何解决的？

遇到过。我排查死锁的一般步骤是酱紫的：

（1）查看死锁日志 `show engine innodb status;` （2）找出死锁sql （3）分析sql加锁情况 （4）模拟死锁案发 （5）分析死锁日志 （6）分析死锁结果

> 说说数据库的乐观锁和悲观锁是什么以及它们的区别？

（1）悲观锁：

悲观锁她专一且缺乏安全感了，她的心只属于当前事务，每时每刻都担心着它心爱的数据可能被别的事务修改，所以一个事务拥有（获得）悲观锁后，其他任何事务都不能对数据进行修改啦，只能等待锁被释放才可以执行。

（2）乐观锁：

乐观锁的“乐观情绪”体现在，它认为数据的变动不会太频繁。因此，它允许多个事务同时对数据进行变动。

实现方式：乐观锁一般会使用版本号机制或CAS算法实现。

> MVCC 熟悉吗，知道它的底层原理？

[MVCC](https://blog.csdn.net/waves___/article/details/105295060)(Multiversion Concurrency Control)，即多版本并发控制技术。

MVCC在MySQL InnoDB中的实现主要是为了提高数据库并发性能，用更好的方式去处理读-写冲突，做到即使有读写冲突时，也能做到不加锁，非阻塞并发读。

## 事务

> MySQL事务得四大特性以及实现原理

- 原子性：事务作为一个整体被执行，包含在其中的对数据库的操作要么全部被执行，要么都不执行。
- 一致性：指在事务开始之前和事务结束以后，数据不会被破坏，假如A账户给B账户转10块钱，不管成功与否，A和B的总金额是不变的。
- 隔离性：多个事务并发访问时，事务之间是相互隔离的，即一个事务不影响其它事务运行效果。简言之，就是事务之间是进水不犯河水的。
- 持久性：表示事务完成以后，该事务对数据库所作的操作更改，将持久地保存在数据库之中。

> 事务的隔离级别有哪些？MySQL的默认隔离级别是什么？

- 读未提交（Read Uncommitted）
- 读已提交（Read Committed）
- 可重复读（Repeatable Read）
- 串行化（Serializable）

Mysql默认的事务隔离级别是可重复读(Repeatable Read)

> 什么是脏读，不可重复读，幻读呢？

`脏读`：事务A、B交替执行，事务A被事务B干扰到了，因为事务A读取到事务B未提交的数据。

`不可重复读`：在一个事务范围内，两个相同的查询，读取同一条记录，却返回了不同的数据。

`幻读`：事务A查询一个范围的结果集，另一个并发事务B往这个范围中插入/删除了数据，并静悄悄地提交，然后事务A再次查询相同的范围，两次读取得到的结果集不一样了。

## 实战

### MySQL数据库cpu飙升的话，要怎么处理呢？

排查过程：

（1）使用top 命令观察，确定是mysqld导致还是其他原因。

（2）如果是mysqld导致的，`show processlist`，查看session情况，确定是不是有消耗资源的sql在运行。

（3）找出消耗高的 sql，看看执行计划是否准确， 索引是否缺失，数据量是否太大。

处理：

（1）kill 掉这些线程(同时观察 cpu 使用率是否下降)

（2）进行相应的调整(比如说加索引、改 sql、改内存参数) 

（3）重新跑这些 SQL。

其他情况：

也有可能是每个 sql 消耗资源并不多，但是突然之间，有大量的 session 连进来导致 cpu 飙升，这种情况就需要跟应用一起来分析为何连接数会激增，再做出相应的调整，比如说限制连接数等



### MYSQL的主从延迟，你怎么解决？

**主从复制分了五个步骤进行：（图片来源于网络）**

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoEjUXOnibfMR6z5w1hgaU4b1jekcvxDML40m1qWeNYjZsKpnEszBL3ob65pWOWQcXCpeC9uQBShtw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

- 步骤一：主库的更新事件(update、insert、delete)被写到binlog
- 步骤二：从库发起连接，连接到主库。
- 步骤三：此时主库创建一个binlog dump thread，把binlog的内容发送到从库。
- 步骤四：从库启动之后，创建一个I/O线程，读取主库传过来的binlog内容并写入到relay log
- 步骤五：还会创建一个SQL线程，从relay log里面读取内容，从Exec_Master_Log_Pos位置开始执行读取到的更新事件，将更新内容写入到slave的db

#### **主从同步延迟的原因**

一个服务器开放Ｎ个链接给客户端来连接的，这样有会有大并发的更新操作, 但是从服务器的里面读取binlog的线程仅有一个，当某个SQL在从服务器上执行的时间稍长 或者由于某个SQL要进行锁表就会导致，主服务器的SQL大量积压，未被同步到从服务器里。这就导致了主从不一致， 也就是主从延迟。

#### **主从同步延迟的解决办法**

- 主服务器要负责更新操作，对安全性的要求比从服务器要高，所以有些设置参数可以修改，比如sync_binlog=1，innodb_flush_log_at_trx_commit = 1 之类的设置等。
- 选择更好的硬件设备作为slave。
- 把一台从服务器当度作为备份使用， 而不提供查询， 那边他的负载下来了， 执行relay log 里面的SQL效率自然就高了。
- 增加从服务器喽，这个目的还是分散读的压力，从而降低服务器负载。



### 分库分表

如果让你做分库与分表的设计，简单说说你会怎么做？

#### **分库分表方案**

- 水平分库：以字段为依据，按照一定策略（hash、range等），将一个库中的数据拆分到多个库中。
- 水平分表：以字段为依据，按照一定策略（hash、range等），将一个表中的数据拆分到多个表中。
- 垂直分库：以表为依据，按照业务归属不同，将不同的表拆分到不同的库中。
- 垂直分表：以字段为依据，按照字段的活跃性，将表中字段拆到不同的表（主表和扩展表）中。

#### **常用的分库分表中间件**

- sharding-jdbc
- Mycat

#### **分库分表可能遇到的问题**

- 事务问题：需要用分布式事务啦
- 跨节点Join的问题：解决这一问题可以分两次查询实现
- 跨节点的count,order by,group by以及聚合函数问题：分别在各个节点上得到结果后在应用程序端进行合并。
- 数据迁移，容量规划，扩容等问题
- ID问题：数据库被切分后，不能再依赖数据库自身的主键生成机制啦，最简单可以考虑UUID，分布式唯一ID生成
- 跨分片的排序分页问题

![图片](https://mmbiz.qpic.cn/mmbiz_gif/b96CibCt70iabwjyojLhA03PtxUnkNPREnt2F48ywfXLpDdDAjicOTPI8Q94tVLbJ58tbRs12iaXDKhUOW9gd4NlFA/640?wx_fmt=gif&wxfrom=5&wx_lazy=1)

---

# [MVCC](https://blog.csdn.net/waves___/article/details/105295060)

序
        最近在学习MySQL中的MVCC，看了网上的各种版本，什么创建版本号、删除版本号，一开始看的时候，好像很对的样子，但实际上很多都是错误的。经过好几天的查阅对比，在几篇博客的帮助下，才算是觉得正确理解了MySQL中的MVCC。
        本文是对MVCC的一些总结，并找到相关源码佐证（talk is cheap，show me the code！网上错误的解释实在是太多了）。如果你刚接触MVCC，或者是被网上的各种解释弄得快要晕了，请坚持看下去，一定会对你有收获。



## 1、MVCC概念

​        多版本并发控制（Multiversion Concurrency Control）: 指的是一种提高并发的技术。最早的数据库系统，只有读读之间可以并发，读写，写读，写写都要阻塞。引入多版本之后，只有写写之间相互阻塞，其他三种操作都可以并行，这样大幅度提高了InnoDB的并发度。在内部实现中，InnoDB通过undo log保存每条数据的多个版本，并且能够找回数据历史版本提供给用户读，每个事务读到的数据版本可能是不一样的。在同一个事务中，用户只能看到该事务创建快照之前已经提交的修改和该事务本身做的修改。

MVCC在 `Read Committed` 和 `Repeatable Read`两个隔离级别下工作。

MySQL的InnoDB存储引擎默认事务隔离级别是RR(可重复读)，是通过 "`行级锁+MVCC`"一起实现的，正常读的时候不加锁，写的时候加锁。而 MVCC 的实现依赖：`隐藏字段`、`Read View`、`Undo log`。



### 1.1、隐藏字段

​        InnoDB存储引擎在每行数据的后面添加了三个隐藏字段：

  1. DB_TRX_ID(6字节)：表示最近一次对本记录行作修改（insert | update）的事务ID。至于delete操作，InnoDB认为是一个update操作，不过会更新一个另外的删除位，将行表示为deleted。并非真正删除。
  2. DB_ROLL_PTR(7字节)：回滚指针，指向当前记录行的undo log信息
 3. DB_ROW_ID(6字节)：随着新行插入而单调递增的行ID。理解：当表没有主键或唯一非空索引时，innodb就会使用这个行ID自动产生聚簇索引。如果表有主键或唯一非空索引，聚簇索引就不会包含这个行ID了。这个DB_ROW_ID跟MVCC关系不大。

   

隐藏字段并不是什么创建版本、删除版本。官方文档：[14.3 InnoDB Multi-Versioning](https://dev.mysql.com/doc/refman/5.7/en/innodb-multi-versioning.html)

![img](https://img-blog.csdnimg.cn/20200409105342893.png)

### 1.2、Read View 结构（重点）

其实Read View（读视图），跟快照、snapshot是一个概念。

Read View主要是用来做可见性判断的, 里面保存了“对本事务不可见的其他活跃事务”。

[Read View 结构源码](https://github.com/facebook/mysql-5.6/blob/42a5444d52f264682c7805bf8117dd884095c476/storage/innobase/include/read0read.h#L125)，其中包括几个变量，在网上这些变量的解释各种各样，下面我结合源码给出它们正确的解释。
                     ![img](https://img-blog.csdnimg.cn/20200404112320346.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1dhdmVzX19f,size_16,color_FFFFFF,t_70)

① low_limit_id：目前出现过的最大的事务ID+1，即下一个将被分配的事务ID。[源码 350行](https://github.com/facebook/mysql-5.6/blob/42a5444d52f264682c7805bf8117dd884095c476/storage/innobase/read/read0read.cc#L350)：

![img](https://img-blog.csdnimg.cn/20200404111850711.png)

max_trx_id的定义如下，[源码 628行](https://github.com/facebook/mysql-5.6/blob/42a5444d52f264682c7805bf8117dd884095c476/storage/innobase/include/trx0sys.h#L628)，翻译过来就是“还未分配的最小事务ID”，也就是下一个将被分配的事务ID。（low_limit_id 并不是活跃事务列表中最大的事务ID）

![img](https://img-blog.csdnimg.cn/20200406155316987.png)

② up_limit_id：活跃事务列表trx_ids中最小的事务ID，如果trx_ids为空，则up_limit_id 为 low_limit_id。[源码 358行](https://github.com/facebook/mysql-5.6/blob/42a5444d52f264682c7805bf8117dd884095c476/storage/innobase/read/read0read.cc#L358)：

![img](https://img-blog.csdnimg.cn/20200404111329981.png)

因为trx_ids中的活跃事务号是逆序的，所以最后一个为最小活跃事务ID。（up_limit_id 并不是已提交的最大事务ID+1，后面的 例子2 会证明这是错误的）

③ trx_ids：Read View创建时其他未提交的活跃事务ID列表。意思就是创建Read View时，将当前未提交事务ID记录下来，后续即使它们修改了记录行的值，对于当前事务也是不可见的。

   注意：Read View中trx_ids的活跃事务，不包括当前事务自己和已提交的事务（正在内存中），[源码 295行](https://github.com/facebook/mysql-5.6/blob/42a5444d52f264682c7805bf8117dd884095c476/storage/innobase/read/read0read.cc#L295)：

![img](https://img-blog.csdnimg.cn/20200408231417644.png)

 ④ creator_trx_id：当前创建事务的ID，是一个递增的编号，[源码 345行](https://github.com/facebook/mysql-5.6/blob/42a5444d52f264682c7805bf8117dd884095c476/storage/innobase/read/read0read.cc#L345) 。（这个编号并不是DB_ROW_ID）

![img](https://img-blog.csdnimg.cn/20200404113210575.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1dhdmVzX19f,size_16,color_FFFFFF,t_70)

### 1.3、Undo log       

​        Undo log中存储的是老版本数据，当一个事务需要读取记录行时，如果当前记录行不可见，可以顺着undo log链找到满足其可见性条件的记录行版本。

​        大多数对数据的变更操作包括 insert/update/delete，在InnoDB里，undo log分为如下两类：

​        ①insert undo log : 事务对insert新记录时产生的undo log, 只在事务回滚时需要, 并且在事务提交后就可以立即丢弃。

​       ②update undo log : 事务对记录进行delete和update操作时产生的undo log，不仅在事务回滚时需要，快照读也需要，只有当数据库所使用的快照中不涉及该日志记录，对应的回滚日志才会被purge线程删除。

> ​     Purge线程：为了实现InnoDB的MVCC机制，更新或者删除操作都只是设置一下旧记录的deleted_bit，并不真正将旧记录删除。
> ​    为了节省磁盘空间，InnoDB有专门的purge线程来清理deleted_bit为true的记录。purge线程自己也维护了一个read view，如果某个记录的deleted_bit为true，并且DB_TRX_ID相对于purge线程的read view可见，那么这条记录一定是可以被安全清除的。

## 2、记录行修改的具体流程

​        假设有一条记录行如下，字段有Name和Honor，值分别为"curry"和"mvp"，最新修改这条记录的事务ID为1。

![img](https://img-blog.csdnimg.cn/20200701205716343.png)

（1）现在事务A（事务ID为2）对该记录的Honor做出了修改，将Honor改为"fmvp"：

​		①事务A先对该行加排它锁

​        ②然后把该行数据拷贝到undo log中，作为旧版本

​        ③拷贝完毕后，修改该行的Honor为"fmvp"，并且修改DB_TRX_ID为2（事务A的ID）, 回滚指针指向拷贝到undo log的旧版本。（然后还会将修改后的最新数据写入redo log）

​        ④事务提交，释放排他锁

![img](https://img-blog.csdnimg.cn/20200701210046670.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1dhdmVzX19f,size_16,color_FFFFFF,t_70)

（2） 接着事务B（事务ID为3）修改同一个记录行，将Name修改为"iguodala"：

​		①事务B先对该行加排它锁

​		②然后把该行数据拷贝到undo log中，作为旧版本

​		③拷贝完毕后，修改该行Name为"iguodala"，并且修改DB_TRX_ID为3（事务B的ID）, 回滚指针指向拷贝到undo log最新的旧版本。

​		④事务提交，释放排他锁

![img](https://img-blog.csdnimg.cn/2020070121022442.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1dhdmVzX19f,size_16,color_FFFFFF,t_70)

从上面可以看出，不同事务或者相同事务的对同一记录行的修改，会使该记录行的undo log成为一条链表，undo log的链首就是最新的旧记录，链尾就是最早的旧记录。

## 3、可见性比较算法

​        在innodb中，创建一个新事务后，执行第一个select语句的时候，innodb会创建一个快照（read view），快照中会保存系统当前不应该被本事务看到的其他活跃事务id列表（即trx_ids）。当用户在这个事务中要读取某个记录行的时候，innodb会将该记录行的DB_TRX_ID与该Read View中的一些变量进行比较，判断是否满足可见性条件。

假设当前事务要读取某一个记录行，该记录行的DB_TRX_ID（即最新修改该行的事务ID）为trx_id，Read View的活跃事务列表trx_ids中最早的事务ID为up_limit_id，将在生成这个Read Vew时系统出现过的最大的事务ID+1记为low_limit_id（即还未分配的事务ID）。

具体的比较算法如下（可以照着后面的 例子 ，看这段）:

1. 如果 trx_id < up_limit_id, 那么表明“最新修改该行的事务”在“当前事务”创建快照之前就提交了，所以该记录行的值对当前事务是可见的。跳到步骤5。

2. 如果 trx_id >= low_limit_id, 那么表明“最新修改该行的事务”在“当前事务”创建快照之后才修改该行，所以该记录行的值对当前事务不可见。跳到步骤4。

3. 如果 up_limit_id <= trx_id < low_limit_id, 表明“最新修改该行的事务”在“当前事务”创建快照的时候可能处于“活动状态”或者“已提交状态”；所以就要对活跃事务列表trx_ids进行查找（源码中是用的二分查找，因为是有序的）：

    (1) 如果在活跃事务列表trx_ids中能找到 id 为 trx_id 的事务，表明①在“当前事务”创建快照前，“该记录行的值”被“id为trx_id的事务”修改了，但没有提交；或者②在“当前事务”创建快照后，“该记录行的值”被“id为trx_id的事务”修改了（不管有无提交）；这些情况下，这个记录行的值对当前事务都是不可见的，跳到步骤4；

    (2)在活跃事务列表中找不到，则表明“id为trx_id的事务”在修改“该记录行的值”后，在“当前事务”创建快照前就已经提交了，所以记录行对当前事务可见，跳到步骤5。

4. 在该记录行的 DB_ROLL_PTR 指针所指向的undo log回滚段中，取出最新的的旧事务号DB_TRX_ID, 将它赋给trx_id，然后跳到步骤1重新开始判断。

5. 将该可见行的值返回。

比较算法[源码 84行](https://github.com/facebook/mysql-5.6/blob/42a5444d52f264682c7805bf8117dd884095c476/storage/innobase/include/read0read.ic#L84)，也可看下图，有注释，图代码来自 [link](https://www.leviathan.vip/2019/03/20/InnoDB%E7%9A%84%E4%BA%8B%E5%8A%A1%E5%88%86%E6%9E%90-MVCC/)：
         ![img](https://img-blog.csdnimg.cn/20200403172924433.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1dhdmVzX19f,size_16,color_FFFFFF,t_70)

## 4、当前读和快照读

​	快照读(snapshot read)：普通的 select 语句(不包括 select ... lock in share mode, select ... for update)

​	当前读(current read) ：select ... lock in share mode，select ... for update，insert，update，delete 语句（这些语句获取的是数据库中的最新数据，官方文档：[14.7.2.4 Locking Reads](https://dev.mysql.com/doc/refman/5.7/en/innodb-locking-reads.html) ）

​	只靠 MVCC 实现RR隔离级别，可以保证可重复读，还能防止部分幻读，但并不是完全防止。

​	比如事务A开始后，执行普通select语句，创建了快照；之后事务B执行insert语句；然后事务A再执行普通select语句，得到的还是之前B没有insert过的数据，因为这时候A读的数据是符合快照可见性条件的数据。这就防止了部分幻读，此时事务A是快照读。

​	但是，如果事务A执行的不是普通select语句，而是select ... for update等语句，这时候，事务A是当前读，每次语句执行的时候都是获取的最新数据。也就是说，在只有MVCC时，A先执行 `select ... where nid between 1 and 10 … for update；`然后事务B再执行 ` insert … nid = 5 …；`然后 A 再执行` select ... where nid between 1 and 10 … for update`，就会发现，多了一条B insert进去的记录。这就产生幻读了，所以单独靠MVCC并不能完全防止幻读。

​	因此，InnoDB在实现RR隔离级别时，不仅使用了MVCC，还会对“当前读语句”读取的记录行加记录锁（record lock）和间隙锁（gap lock），禁止其他事务在间隙间插入记录行，来防止幻读。也就是前文说的"行级锁+MVCC"。

​	如果你对这些锁不是很熟悉，[这是一篇将MySQL 中锁机制讲的很详细的博客](https://tonydong.blog.csdn.net/article/details/103324323) 。

​	RR和RC的Read View产生区别：
​        ①在innodb中的Repeatable Read级别, 只有事务在begin之后，执行第一条select（读操作）时, 才会创建一个快照(read view)，将当前系统中活跃的其他事务记录起来；并且事务之后都是使用的这个快照，不会重新创建，直到事务结束。

​		②在innodb中的Read Committed级别, 事务在begin之后，执行每条select（读操作）语句时，快照会被重置，即会重新创建一个快照(read view)。

官方文档：[consistent read](https://dev.mysql.com/doc/refman/5.7/en/glossary.html#glos_consistent_read)，里面所说的consistent read 一致性读，我的理解就是 快照读，也就是普通select语句，它们不会对访问的数据加锁。     只有普通select语句才会创建快照，select ... lock in share mode，select ... for update不会，update、delete、insert语句也不会，因为它们都是 当前读，会对访问的数据加锁。

![img](https://img-blog.csdnimg.cn/20200409105504228.png)

## 5、例子（帮助理解）

假设原始数据行：

| 假设原始数据行： |           |           |             |
| ---------------- | --------- | --------- | ----------- |
| Field            | DB_ROW_ID | DB_TRX_ID | DB_ROLL_PTR |
| 0                | 10        | 10000     | 0x13525342  |

例子1

![img](https://img-blog.csdnimg.cn/20200413204733606.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1dhdmVzX19f,size_16,color_FFFFFF,t_70)

例子2
（证明“up_limit_id为已提交的最大事务ID + 1”是错误的）

![img](https://img-blog.csdnimg.cn/20200404191129740.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1dhdmVzX19f,size_16,color_FFFFFF,t_70)

例子3
（跟例子2一样的情况，不过up_limit_id变为trx_ids中最小的事务ID）：

![img](https://img-blog.csdnimg.cn/20200404213058729.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L1dhdmVzX19f,size_16,color_FFFFFF,t_70)

参考:
[MySQL-InnoDB-MVCC多版本并发控制](https://segmentfault.com/a/1190000012650596)
[MySQL数据库事务各隔离级别加锁情况--read committed && MVCC](https://www.imooc.com/article/17290)
[InnoDB存储引擎MVCC的工作原理](https://my.oschina.net/xinxingegeya/blog/505675)
[【MySQL笔记】正确的理解MySQL的MVCC及实现原理](https://blog.csdn.net/SnailMann/article/details/94724197)
[Mysql Innodb中undo-log和MVCC多版本一致性读 的实现](http://blog.sina.com.cn/s/blog_4673e603010111ty.html)
[InnoDB事务分析-MVCC](https://www.leviathan.vip/2019/03/20/InnoDB%E7%9A%84%E4%BA%8B%E5%8A%A1%E5%88%86%E6%9E%90-MVCC/)

---

# [如何实现丝滑的数据库扩容](https://mp.weixin.qq.com/s/1VCC3i6ZCk7sb9kVRZ1czQ)

## 初版 

一般来说，如果我们的线上服务体量还没有那么大，通常单体的数据库DB来存储数据即可。

![图片](https://mmbiz.qpic.cn/mmbiz_png/wJvXicD0z2dWfYvduOEcibXkVPdx4yGM1A7WFwvd6ticYoEkYtnicKYpb76ft4ialHxxB0EJyZcPbOpfhiciazpgLhaNg/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)单体应用


**优点**：简单，省事，方便。
**缺点**：数据并发性，稳定性都有问题。

## 进阶

随着数据量的不断增大，一般我们要对数据进行水平切分，水平切分的规则你可以简单根据用户ID或者用户IP对数据进行取模，实现路由功能。当然也可以增加Slave跟KeepAlived来实现高可用。

![图片](https://mmbiz.qpic.cn/mmbiz_png/wJvXicD0z2dWfYvduOEcibXkVPdx4yGM1AgKfquVUyh0WtSicfQEnh6x8bibohNe2J69lTA8xeHR1dun2uMqJzQqvw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)主从+路由


但问题是，如果随着业务发展，目前我们2个库的性能扛不住了，还要继续水平拆分，造出更多库咋办？你一般是如何实现丝滑扩容的呢？

## 扩容

### 第一版：停机扩容

![图片](https://mmbiz.qpic.cn/mmbiz_png/wJvXicD0z2dWfYvduOEcibXkVPdx4yGM1AIhLnME4VwiaH9ibF1ibnCbM7d5W8PnKiaOXO6eNvY5S9b7yicjWJMVuoPFA/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)停机扩容


简单直接暴力的方法。

1. APP通知用户在某个时间段停机维护升级。
2. 新建若干个具有高可用的库。
3. 停止当前服务，然后写个数据迁移程序，实现把老库数据全部迁移到新库中。
4. 修改代码路由规则后重新对外提供服务。

**优点**：简单
**缺点**：中间停服务了，无法保证高可用。数据切换前跟切换过程中需确保无任何出错。

### 第二版：在线双写

![图片](https://mmbiz.qpic.cn/mmbiz_png/wJvXicD0z2dWfYvduOEcibXkVPdx4yGM1A80sicr5hIuplot91hQib74osHnYMcwCljpyH0Y7fawkia2ib8CnlSCxcMQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)在线双写

1. 建立好新的数据库，然后接下来用户在写原有数据库到同时也写一份数据到我们的新库中。
2. 写个数据迁移程序，实现旧库中的历史数据迁移到新库中。
3. 迁移过程中，每次插入数据时，需检测数据的更新情况。比如，如果新的表中没有当前的数据，则直接新增；如果新表有数据并没有我们要迁移的数据新的话，我们就更新为当前数据，只能允许新的数据覆盖旧的数据，推荐使用[Canal](https://github.com/alibaba/canal/)这样的中间件。
4. 经过一段时间后需要校验新库跟旧库两边数据是否一样。如果检查到一样了，则直接切换即可。

**优点：**高可用了。
**缺点：**不够丝滑，来回挪动数据较大。

### 第三版：丝滑般扩容

目标：打算将原来到两个数据库扩容到4个。

##### 第一步：修改配置

![图片](https://mmbiz.qpic.cn/mmbiz_png/wJvXicD0z2dWfYvduOEcibXkVPdx4yGM1Aq81WPxBibK501arvC5GG0k5Zwko5ciaYLGC63YKD9JMhJzKCezQYibFCw/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)修改配置

1. 修改配置信息，注意旧库跟新库之间到映射关系。确保扩容后数据可以正确路由到服务器。

- Id % 2 = 0 的库变为了  id % 4 = 0 或  id % 4 = 2
- Id % 2 = 1 的库变为了  id % 4 = 1 或  id % 4 = 3

##### 第二步：reload配置

服务层reload配置，可以重启服务，也可以Cloud那样配置中心发送信号来实现重读配置文件。

至此，数据库的2 --> 4 扩容完成，原来是2个数据库实例提供服务，现在变为4个数据库实例提供服务。

##### 第三步：收缩数据

![图片](https://mmbiz.qpic.cn/mmbiz_png/wJvXicD0z2dWfYvduOEcibXkVPdx4yGM1ASx1SwcTkC1DZYm8icRn0RylhHmuiajppt4Z8hP70BmBqqzypWiaDxJ5uQ/640?wx_fmt=png&wxfrom=5&wx_lazy=1&wx_co=1)丝滑扩容


此时  id % 4 = 0 跟  id % 4 = 2 的两个DB 还在同步数据。id % 4 = 1 跟 id % 4 = 3的两个DB还在同步数据。需做一些收尾操作。

1. 接触上面的两个同步操作。
2. 对新库新建高可用。
3. 删除冗余数据，比如id % 4 = 0的机器中删除id % 4 = 2的冗余数据，只为id % 4 = 0的数据提供服务，其余三个类似操作。
4. 至此实现成倍扩容，还避免来数据迁移。

扩展

[如何实现数据库和表扩展的顺利数据迁移？](https://developpaper.com/256-to-4096-how-to-realize-smooth-data-migration-for-database-and-table-expansion/)

![图片](https://mmbiz.qpic.cn/mmbiz_gif/b96CibCt70iabwjyojLhA03PtxUnkNPREnt2F48ywfXLpDdDAjicOTPI8Q94tVLbJ58tbRs12iaXDKhUOW9gd4NlFA/640?wx_fmt=gif&wxfrom=5&wx_lazy=1)
