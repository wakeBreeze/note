### DataX

#### 库资源

ETL服务器地址：10.72.65.196

源库（mysql）：

- driver-class-name: com.mysql.jdbc.Driver
- url: jdbc:mysql://172.16.13.11:15022/setlcent_db
- username: dip
- password: 123456

管理员：

- admin/QWE123qwe



目标库（oracle）：

- driver-class-name: oracle.jdbc.driver.OracleDriver
- url: jdbc:oracle:thin:@ 10.72.65.247:1521/pdbcrl
- username: data_sync
- password: sjtb2021_05

sync_job...表放此库



数据集成平台（ETL库）：

- driver-class-name: oracle.jdbc.driver.OracleDriver
- url: jdbc:oracle:thin:@10.72.65.247:1521/pdbcrl
- username: yhetl
- password: yhetl

xxl_job...表放此库



fenjian库：

```yaml
url: jdbc:mysql://10.72.65.207:3306/feijian_suqian
username: root
password: ybdsj2021
```





宿迁库：

```yaml
url:  jdbc:oracle:thin:@172.20.22.231:1521/orcl
username: drgsplatform_sq
password: drgsplatform_sq
```



#### 配置

- 替换掉datax/lib/datax-core-0.0.1-SNAPSHOT.jar
- data-sync-back-0.0.1-SNAPSHOT-20211009.jar放在datax同级目录。如图：![image-20211021173142319](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20211021173142319.png)

- 



data-sync-back-0.0.1-SNAPSHOT-20211009.jar包的==application.yml==中的数据源配置为两个sync_job...表所在的库。

xxl-job-admin-2.0.1.jar包的==application.properties==中的数据源配置为16个xxl_job_qrtz...表所在的库。



**application.yml位置：**data-sync-back-0.0.1-SNAPSHOT-20211009.jar/BOOT-INF/classes/application.yml

![image-20211021164803066](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20211021164803066.png)



**application.properties位置：**`xxl_job_admin-2.0.1.jar/BOOT-INF/classes/application.properties`

![image-20211021165538826](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20211021165538826.png)



新增执行器

![image-20211021171238335](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20211021171238335.png)



平台新增任务

![image-20211021172044991](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20211021172044991.png)



参数配置：

```
。差不多参照这个建就行。哈哈~~~说明哈参数。begnUpdtTime，endUpdtTime 源数据的数据更新时间字段，如果有填写就按照这个填写的抽，如果只填写了begnUpdtTime那就是从这个开始到昨天，如果都为null，就是从上次抽取数据的最后的时间开始抽到昨天。然后如果上次抽取数据的最后的时间为null，就是前一天到昨天。
```





xxl_job的日志配置（日志处于关闭状态）

![image-20211021172441037](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20211021172441037.png)





同步数据的表，按这种写进去

![image-20211021175136884](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20211021175136884.png)



每次同步的数据量的日志

![image-20211021175200835](C:\Users\Administrator\AppData\Roaming\Typora\typora-user-images\image-20211021175200835.png)



#### DataX使用指南

https://blog.csdn.net/qq_37552993/article/details/80235010





#### DataX结合Airflow

https://zhuanlan.zhihu.com/p/400369164



***mysqlToOracle.json***

```json
{
    "job": {
        "setting": {
            "speed": {
                 "channel": 3
            },
            "errorLimit": {
                "record": 0,
                "percentage": 0.02
            }
        },
        "content": [
            {
                "reader": {
                    "name": "mysqlreader",
                    "parameter": {
                        "username": "root",
                        "password": "root",
                        "column": [
                            "deptno",
                            "dname",
                            "loc"
                        ],
                        "splitPk": "",
                        "connection": [
                            {
                                "table": [
                                    "dept"
                                ],
                                "jdbcUrl": [
     "jdbc:mysql://192.168.56.10:3306/test"
                                ]
                            }
                        ]
                    }
                },
               "writer": {
                    "name": "oraclewriter",
                    "parameter": {
                        "username": "system",
                        "password": "system",
                        "column": [
                            "deptno",
                            "dname",
                            "loc"
                        ],
                        "preSql": [
                            "delete from dept"
                        ],
                        "connection": [
                            {
                                "jdbcUrl": "jdbc:oracle:thin:@192.168.56.10:1521:orcl",
                                "table": [
                                    "dept"
                                ]
                            }
                        ]
                    }
                }
            }
        ]
    }
}
```



***oracle2mysql.json***

```json
{
    "job": {
        "setting": {
            "speed": {
            	"channel": 5
            }
        },
        "content": [
            {
                "reader": {
                    "name": "oraclereader",
                    "parameter": {
                        "username": "system",
                        "password": "system",
                        "where": "",
                        "connection": [
                            {
                                "querySql": [
                                    "select deptno,dname,loc from dept where deptno < 30"
                                ],
                                "jdbcUrl": [
                                    "jdbc:oracle:thin:@192.168.56.10:1521:orcl"
                                ]
                            }
                        ]
                    }
                },
                "writer": {
                    "name": "mysqlwriter",
                    "parameter": {
                        "writeMode": "insert",
                        "username": "root",
                        "password": "root",
                        "column": [
                            "deptno",
                            "dname",
                            "loc"
                        ],
                        "session": [
                        	"set session sql_mode='ANSI'"
                        ],
                        "preSql": [
                            "delete from test"
                        ],
                        "connection": [
                            {
                                "jdbcUrl": "jdbc:mysql://192.168.56.10:3306/test?useUnicode=true&characterEncoding=gbk",
                                "table": [
                                    "dept"
                                ]
                            }
                        ]
                    }
                }
            }
        ]
    }
}

```



template.json

```json
{
    "job": {
        "content": [
            {
                "reader": {
                    "name": "streamreader",
                    "parameter": {
                        "column": [],
                        "sliceRecordCount": ""
                    }
                },
                "writer": {
                    "name": "streamwriter",
                    "parameter": {
                        "encoding": "",
                        "print": true
                    }
                }
            }
        ],
        "setting": {
            "speed": {
                "channel": ""
            }
        }
    }
}
```



#### kettle

[【数据迁移工具】使用 kettle数据迁移从oracle到mysql的图文教程 - 云+社区 - 腾讯云 (tencent.com)](https://cloud.tencent.com/developer/article/1452447)



```sql
-- Create table
create table SYNC_JOB_D
(
  job_id                  VARCHAR2(40),
  sync_table_name         VARCHAR2(100),
  job_json                CLOB,
  job_json_file           VARCHAR2(200),
  vali_flag               VARCHAR2(6),
  sys_codg                VARCHAR2(10),
  last_sync_max_updt_time TIMESTAMP(6)
)
tablespace SYSTEM
  pctfree 10
  pctused 40
  initrans 1
  maxtrans 255
  storage
  (
    initial 64K
    next 1M
    minextents 1
    maxextents unlimited
  );
```



#### 其他

##### **oracle时间条件**

```sql
where UPDT_TIME >= to_date('${begnUpdtTime}','yyyy-MM-dd hh24:mi:ss') and UPDT_TIME < to_date('${endUpdtTime}','yyyy-MM-dd hh24:mi:ss')
```

##### **mysql时间条件**

```sql
UPDT_TIME >= '${begnUpdtTime}' and UPDT_TIME < '${endUpdtTime}'
```



##### **jar包处理**

```linux
-- 解压
jar -xvf .\**.jar
-- 打包
jar -cfM0 **.jar ./
```



##### sync_job_d表插入 

```mysql
INSERT INTO sync_job_d(sync_table_name,job_json,job_json_file,vali_flag,sys_codg,last_sync_max_updt_time) 
VALUES('setl_d',NULL,'/usr/local/DataX/datax/job/setl_d.json',1,'sd',NOW());
```



（序列：SE_AAZ851）

```sql
INSERT INTO sync_job_d VALUES(SE_AAZ851.NEXTVAL,'setl_d_hosp',NULL,'/usr/local/DataX/datax/job/setl_d_hosp_st.json',1,'sdhs',SYSDATE);

INSERT INTO sync_job_d VALUES(SE_AAZ851.NEXTVAL,'kc',NULL,'/usr/local/DataX/datax/job/kc24_table.json',1,'kt',SYSDATE);

INSERT INTO sync_job_d VALUES(SE_AAZ851.NEXTVAL,'mdtrt_d',NULL,'/usr/local/DataX/datax/job/mdtrt_d_perday.json',1,'m',SYSDATE);

{"admdvs":"440000","begnUpdtTime":"2021-11-30 00:00:00","endUpdtTime":"2021-12-01 00:00:00","syncTableName":"setl_d_hosp","sysCodg":"sdhs"}

INSERT INTO sync_job_d VALUES(2,'outmed_setl_d',NULL,'/usr/local/DataX/datax/job/outmed_setl_d.json',1,'osd',SYSDATE);

INSERT INTO sync_job_d VALUES(5,'setl_d_hosp',NULL,'/usr/local/DataX/datax/job/setl_d_hosp.json',1,'sdh',SYSDATE);

INSERT INTO sync_job_d VALUES(6,'setl_d_outpatient',NULL,'/usr/local/DataX/datax/job/setl_d_outpatient.json',1,'sdo',SYSDATE);

INSERT INTO sync_job_d VALUES(7,'fee_list_d_hosp',NULL,'/usr/local/DataX/datax/job/fee_list_d_hosp.json',1,'fldh',SYSDATE);

INSERT INTO sync_job_d VALUES(8,'fee_list_d_outpatient',NULL,'/usr/local/DataX/datax/job/fee_list_d_outpatient.json',1,'fldo',SYSDATE);

select count(*) from outmed_setl_d where date_format(updt_time,'%Y/%d') like '2021/09'

select count(*) from outmed_setl_d where to_char(updt_time,'yyyy/MM') like '2021/09'


{"admdvs":"440000","begnUpdtTime":"2021-09-01 00:00:00","endUpdtTime":"2021-10-01 00:00:00","syncTableName":"outmed_setl_d","sysCodg":"osd"}

{"admdvs":"440000","syncTableName":"doc_suspt_detl_final","sysCodg":"dsdf"}

```

```sql
INSERT INTO sync_job_d VALUES(SE_AAZ851.NEXTVAL,'fee_list_d_hosp_34',NULL,'/usr/local/DataX/datax/job/fee_list_d_hosp_34.json',1,'fldh34',SYSDATE);
INSERT INTO sync_job_d VALUES(SE_AAZ851.NEXTVAL,'fee_list_d_hosp_56',NULL,'/usr/local/DataX/datax/job/fee_list_d_hosp_56.json',1,'fldh56',SYSDATE);
INSERT INTO sync_job_d VALUES(SE_AAZ851.NEXTVAL,'fee_list_d_hosp_78',NULL,'/usr/local/DataX/datax/job/fee_list_d_hosp_78.json',1,'fldh78',SYSDATE);
INSERT INTO sync_job_d VALUES(SE_AAZ851.NEXTVAL,'fee_list_d_hosp_910',NULL,'/usr/local/DataX/datax/job/fee_list_d_hosp_910.json',1,'fldh910',SYSDATE);
INSERT INTO sync_job_d VALUES(SE_AAZ851.NEXTVAL,'fee_list_d_hosp_1112',NULL,'/usr/local/DataX/datax/job/fee_list_d_hosp_1112.json',1,'fldh1112',SYSDATE);
```

```sql
{"admdvs":"440000","begnUpdtTime":"2021-03-01 00:00:00","endUpdtTime":"2021-05-01 00:00:00","syncTableName":"fee_list_d_hosp_34","sysCodg":"fldh34"}

{"admdvs":"440000","begnUpdtTime":"2021-05-01 00:00:00","endUpdtTime":"2021-07-01 00:00:00","syncTableName":"fee_list_d_hosp_56","sysCodg":"fldh56"}

{"admdvs":"440000","begnUpdtTime":"2021-10-01 00:00:00","endUpdtTime":"2021-10-02 00:00:00","syncTableName":"setl_d","sysCodg":"sd"}
```



###### **表抽取对照sql**

```sql
-- fee_list_d (十月数据)
	-- 源库
	select count(1) from fee_list_d whereUPDT_TIME >= '2021-10-01 00:00:00' and UPDT_TIME < '2021-11-01 00:00:00' and med_type IN ('21', '22', '52', '5302', '28')
	-- 目的库
	select count(1) from fee_list_d where UPDT_TIME >= to_date('2021-10-01 00:00:00','yyyy-MM-dd hh24:mi:ss') and UPDT_TIME < to_date('2021-11-01 00:00:00','yyyy-MM-dd hh24:mi:ss')
	
-- setl_d (七月~十月数据)
	-- 源库
	select count(1) from setl_d whereUPDT_TIME >= '2021-07-01 00:00:00' and UPDT_TIME < '2021-11-01 00:00:00' and and med_type IN ('21', '22', '52', '5302', '28') AND pay_loc = '2'
	-- 目的库
	select count(1) from setl_d where UPDT_TIME >= to_date('2021-0-01 00:00:00','yyyy-MM-dd hh24:mi:ss') and UPDT_TIME < to_date('2021-11-01 00:00:00','yyyy-MM-dd hh24:mi:ss')
```



##### **删除重复数据**

```sql
-- fee_list_d
delete from fee_list_d t where t.rowid in(
select a.rowid from(
select rowid,row_number()over(partition by BKKP_SN order by UPDT_TIME desc) rn from(
select BKKP_SN,UPDT_TIME from fee_list_d where UPDT_TIME >= to_date('${begnUpdtTime}','yyyy-MM-dd hh24:mi:ss') and UPDT_TIME < to_date('${endUpdtTime}','yyyy-MM-dd hh24:mi:ss'))
)a where a.rn>1
)

delete from fee_list_d t where t.rowid in(select a.rowid from(select rowid,row_number()over(partition by BKKP_SN order by UPDT_TIME desc) rn from(select BKKP_SN,UPDT_TIME from fee_list_d where UPDT_TIME >= to_date('${begnUpdtTime}','yyyy-MM-dd hh24:mi:ss') and UPDT_TIME < to_date('${endUpdtTime}','yyyy-MM-dd hh24:mi:ss')))a where a.rn>1)

-- outmed_setl_d
delete from outmed_setl_d t where t.rowid in(select a.rowid from(select rowid,row_number()over(partition by setl_id order by UPDT_TIME desc) rn from(select setl_id,UPDT_TIME from outmed_setl_d where UPDT_TIME >= to_date('${begnUpdtTime}','yyyy-MM-dd hh24:mi:ss') and UPDT_TIME < to_date('${endUpdtTime}','yyyy-MM-dd hh24:mi:ss')))a where a.rn>1)

-- setl_fee_stt_d
delete from setl_fee_stt_d t where t.rowid in(select a.rowid from(select rowid,row_number()over(partition by setl_id order by UPDT_TIME desc) rn from(select setl_id,UPDT_TIME from setl_fee_stt_d where UPDT_TIME >= to_date('${begnUpdtTime}','yyyy-MM-dd hh24:mi:ss') and UPDT_TIME < to_date('${endUpdtTime}','yyyy-MM-dd hh24:mi:ss')))a where a.rn>1)

-- setl_d
delete from setl_d t where t.rowid in(select a.rowid from(select rowid,row_number()over(partition by setl_id order by UPDT_TIME desc) rn from(select setl_id,UPDT_TIME from setl_d where UPDT_TIME >= to_date('${begnUpdtTime}','yyyy-MM-dd hh24:mi:ss') and UPDT_TIME < to_date('${endUpdtTime}','yyyy-MM-dd hh24:mi:ss')))a where a.rn>1)
```



删除setl_d_hosp重复数据

```sql
-- 2021年11月
-- setl_time 结算时间 
-- 保留一条当月结算未退费的最新数据）
delete from setl_d_hosp t
where t.rowid in 
(select a.rowid from 
   (select rowid,row_number() over(partition by mdtrt_id, fixmedins_code order by UPDT_TIME desc) rn from 
       (select mdtrt_id, fixmedins_code, UPDT_TIME from setl_d_hosp
        where setl_time >= to_date('2021-11-01 00:00:00', 'yyyy-MM-dd hh24:mi:ss') 
        and setl_time < to_date('2021-12-01 00:00:00', 'yyyy-MM-dd hh24:mi:ss')
        and refd_setl_flag = '0'
        AND pay_loc = 2 --支付地点类别(1:中心,2:医院,3:省内异地,4:省外异地)
       AND setl_type = 2 --结算类别(1:中心报销,2:联网结算,3:补充报销)
       AND clr_type = 21 --清算类别(11:门诊,21:住院,41:药店,51:暂缓)
       AND vali_flag = '1' --有效标志
        )
    ) a
where a.rn > 1
)


--
"delete from outmed_setl_d t where t.rowid in(select a.rowid from(select rowid,row_number()over(partition by mdtrt_id, fixmedins_code order by UPDT_TIME desc) rn from(select mdtrt_id, fixmedins_code,UPDT_TIME from outmed_setl_d where setl_time >= to_date('${begnUpdtTime}','yyyy-MM-dd hh24:mi:ss') and setl_time < to_date('${endUpdtTime}','yyyy-MM-dd hh24:mi:ss')))a where a.rn>1)"
```





删除kea5重复数据

```sql
-- 2021年11月
-- setl_time 结算时间
delete from kea5 t
where t.rowid in 
(select a.rowid from 
 	(select rowid,row_number() over(partition by akc190, akb020 order by aae036 desc) rn from 
     	(select akc190, akb020, aae036 from kea5
        where ake100 >= to_date('2021-11-01 00:00:00', 'yyyy-MM-dd hh24:mi:ss') and ake100 < to_date('2021-12-01 00:00:00', 'yyyy-MM-dd hh24:mi:ss')
        )
    ) a
	where a.rn > 1
)
```









#### 抽取目的表

#### 

```sql
-- 住院结算数据表(SETL_D_HOSP)
med_type IN ('21', '22', '52', '5302', '28')
-- 门诊结算数据表(SETL_D_OUTPATIENT)
med_type NOT IN ('21', '22', '52', '5302', '28')
-- 住院费用明细表(FEE_LIST_D_HOSP)
med_type IN ('21', '22', '52', '5302', '28')
-- 门诊费用明细表(FEE_LIST_D_OUTPATIENT)
med_type NOT IN ('21', '22', '52', '5302', '28')
-- 医保结算清单费用分类表(SETL_FEE_STT_D)
```

##### 住院结算数据表(SETL_D_HOSP)





select count(*) from fee_list_d where to_char(updt_time,'yyyy/MM/dd') = '2021/11/23'



#### 问题处理

```java
2021-12-15 17:28:56.786 - [] - [Thread-15] INFO  c.x.r.r.invoker.reference.XxlRpcReferenceBean.invoke(165) - >>>>>>>>>>> xxl-job, invoke error, address:http://10.72.65.196:8901/xxl-job-admin//api, XxlRpcRequestXxlRpcRequest{requestId='d1025b42-18f2-4a0b-85cf-fabdfbb25e50', createMillisTime=1639560536644, accessToken='', className='com.xxl.job.core.biz.AdminBiz', methodName='registry', parameterTypes=[class com.xxl.job.core.biz.model.RegistryParam], parameters=[RegistryParam{registGroup='EXECUTOR', registryKey='data-sync', registryValue='172.17.0.1:9998'}], version='null'}
2021-12-15 17:28:56.793 - [] - [Thread-15] INFO  com.xxl.job.core.thread.ExecutorRegistryThread.run(57) - >>>>>>>>>>> xxl-job registry error, registryParam:RegistryParam{registGroup='EXECUTOR', registryKey='data-sync', registryValue='172.17.0.1:9998'}
com.xxl.rpc.util.XxlRpcException: java.net.ConnectException: Connection refused
	at sun.nio.ch.SocketChannelImpl.checkConnect(Native Method)
	at sun.nio.ch.SocketChannelImpl.finishConnect(SocketChannelImpl.java:717)
	at org.eclipse.jetty.io.SelectorManager.doFinishConnect(SelectorManager.java:355)
	at org.eclipse.jetty.io.ManagedSelector.processConnect(ManagedSelector.java:347)
	at org.eclipse.jetty.io.ManagedSelector.access$1700(ManagedSelector.java:65)
	at org.eclipse.jetty.io.ManagedSelector$SelectorProducer.processSelected(ManagedSelector.java:676)
	at org.eclipse.jetty.io.ManagedSelector$SelectorProducer.produce(ManagedSelector.java:535)
	at org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.produceTask(EatWhatYouKill.java:362)
	at org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.doProduce(EatWhatYouKill.java:186)
	at org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.tryProduce(EatWhatYouKill.java:173)
	at org.eclipse.jetty.util.thread.strategy.EatWhatYouKill.produce(EatWhatYouKill.java:137)
	at org.eclipse.jetty.io.ManagedSelector$$Lambda$605/1691415884.run(Unknown Source)
	at org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:883)
	at org.eclipse.jetty.util.thread.QueuedThreadPool$Runner.run(QueuedThreadPool.java:1034)
	at java.lang.Thread.run(Thread.java:745)

	at com.xxl.rpc.remoting.invoker.reference.XxlRpcReferenceBean$1.invoke(XxlRpcReferenceBean.java:161)
	at com.sun.proxy.$Proxy59.registry(Unknown Source)
	at com.xxl.job.core.thread.ExecutorRegistryThread$1.run(ExecutorRegistryThread.java:48)
	at java.lang.Thread.run(Thread.java:745)
```

注意：

data-sync-back-0.0.1-SNAPSHOT-20211009.jar中application.yml

![image-20211215182235771](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20211215182235771.png)

此处addresses:即xxl-job-admin-2.0.1.jar包运行的地址



xxl-job-admin-2.0.1.jar中application.properties

![image-20211215182500566](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20211215182500566.png)

这两处端口是否一致
