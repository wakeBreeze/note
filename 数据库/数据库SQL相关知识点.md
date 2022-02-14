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

# 为什么你写的SQL这么慢

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



**锁**



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



**慢查询**



在讲读操作变慢的原因之前我们先来看看是如何定位慢 SQL 的。Mysql 中有一个叫作**慢查询日志**的东西，它是用来记录超过指定时间的 SQL 语句的。默认情况下是关闭的，通过手动配置才能开启慢查询日志进行定位。


具体的配置方式是这样的：



- 查看当前慢查询日志的开启情况：



![图片](https://mmbiz.qpic.cn/mmbiz_png/g6hBZ0jzZb3ERsp8aGUhayQ1Px6RYfaYz2zGqniaLDMek0I6QcsibZqw9ibCK6In2n4x2IgicfOHPb97R2DGalibuWw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



- 开启慢查询日志（临时）：



![图片](https://mmbiz.qpic.cn/mmbiz_png/g6hBZ0jzZb3ERsp8aGUhayQ1Px6RYfaYI259xicNUxExMkqxuqejXjvKNxoLBI3wcMpHz0e2SGJoqjXnqrZiajEg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

![图片](https://mmbiz.qpic.cn/mmbiz_png/g6hBZ0jzZb3ERsp8aGUhayQ1Px6RYfaYC62icpYGic9jqnRvLzUKXs98tdIfggxTKwsdmePGwPZ8XfWhgqC2sxUw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



注意这里只是临时开启了慢查询日志，如果 mysql 重启后则会失效。可以 my.cnf 中进行配置使其永久生效。



**存在原因**



知道了如何查看执行慢的 SQL 了，那么我们接着看读操作时为什么会导致慢查询，这里列两点常见的原因。



**（1）未命中索引**



SQL 查询慢的原因之一是可能未命中索引，关于使用索引为什么能使查询变快以及使用时的注意事项，网上已经很多了，这里就不多赘述了。



**（2）脏页问题**



另一种还是我们上边所提到的刷脏页情况，只不过和写操作不同的是，是在读时候进行刷脏页的。



是不是有点懵逼，别急，听我娓娓道来：



为了避免每次在读写数据时访问磁盘增加 IO 开销，Innodb 存储引擎通过把相应的数据页和索引页加载到内存的缓冲池（buffer pool）中来提高读写速度。然后按照比如最近最少使用原则来保留缓冲池中的缓存数据。



那么当要读入的数据页不在内存中时，就需要到缓冲池中申请一个数据页，但缓冲池中数据页是一定的，当数据页达到上限时此时就需要把最久不使用的数据页从内存中淘汰掉。但如果淘汰的是脏页呢，那么就需要把脏页刷到磁盘里才能进行复用。



你看，又回到了刷脏页的情况，读操作时变慢你也能理解了吧？



**防患于未然**



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
