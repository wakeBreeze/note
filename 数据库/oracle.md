## Oracle笔记



#### sysdate用法



##### ORACLE 日期加减操作

https://blog.csdn.net/qq_31391571/article/details/83058114

```sql
对当前日期增加一个小时：
SQL> select sysdate, sysdate+numtodsinterval(1,’hour’) from dual ;

SYSDATE             SYSDATE+NUMTODSINTE
——————- ——————-
2010-10-14 21:38:19 2010-10-14 22:38:19
对当前日期增加50分z
SQL> select sysdate, sysdate+numtodsinterval(50,’minute’) from dual ;

SYSDATE             SYSDATE+NUMTODSINTE
——————- ——————-
2010-10-14 21:39:12 2010-10-14 22:29:12
对当前日期增加45秒
SQL> select sysdate, sysdate+numtodsinterval(45,’second’) from dual ;

SYSDATE             SYSDATE+NUMTODSINTE
——————- ——————-
2010-10-14 21:40:06 2010-10-14 21:40:51
对当前日期增加3天
SQL> select sysdate, sysdate+3 from dual ;

SYSDATE             SYSDATE+3
——————- ——————-
2010-10-14 21:40:46 2010-10-17 21:40:46
对当前日期增加4个月
SQL> select sysdate, add_months(sysdate,4) from dual ;

SYSDATE             ADD_MONTHS(SYSDATE,
——————- ——————-
2010-10-14 21:41:43 2011-02-14 21:41:43

当前日期增加2年
SQL> select sysdate, add_months(sysdate,12*2) from dual ;

SYSDATE             ADD_MONTHS(SYSDATE,
——————- ——————-
2010-10-14 21:42:17 2012-10-14 21:42:17

timestamp的操作方法与上面类似；
求两个日期之差：
例：求2007-5-23 21：23：34与当前时间之间的差值。
SQL> select sysdate-to_date(’20070523 21:23:34′,’yyyy-mm-dd hh24:mi:ss’) dt from
dual ;

DT
———-
1240.01623
```



**加法** 

```sql
select sysdate,add_months(sysdate,12) from dual; --加1年 
select sysdate,add_months(sysdate,1) from dual; --加1月 
select sysdate,to_char(sysdate+7,'yyyy-mm-dd HH24:MI:SS') from dual; --加1星期 
select sysdate,to_char(sysdate+1,'yyyy-mm-dd HH24:MI:SS') from dual; --加1天 
select sysdate,to_char(sysdate+1/24,'yyyy-mm-dd HH24:MI:SS') from dual; --加1小时 
select sysdate,to_char(sysdate+1/24/60,'yyyy-mm-dd HH24:MI:SS') from dual; --加1分钟 
select sysdate,to_char(sysdate+1/24/60/60,'yyyy-mm-dd HH24:MI:SS') from dual; --加1秒 

-- 补充
-- 当前时间减去7分钟的时间 
select sysdate,sysdate - interval '7' MINUTE from dual; 
--当前时间减去7小时的时间 
select sysdate - interval '7' hour from dual; 
--当前时间减去7天的时间 
select sysdate - interval '7' day from dual; 
--当前时间减去7月的时间 
select sysdate,sysdate - interval '7' month from dual; 
--当前时间减去7年的时间 
select sysdate,sysdate - interval '7' year from dual; 
--时间间隔乘以一个数字 
select sysdate,sysdate - 8*interval '7' hour from dual;
```

**减法** 

```sql
select sysdate,add_months(sysdate,-12) from dual; --减1年 
select sysdate,add_months(sysdate,-1) from dual; --减1月 
select sysdate,to_char(sysdate-7,'yyyy-mm-dd HH24:MI:SS') from dual; --减1星期 
select sysdate,to_char(sysdate-1,'yyyy-mm-dd HH24:MI:SS') from dual; --减1天 
select sysdate,to_char(sysdate-1/24,'yyyy-mm-dd HH24:MI:SS') from dual; --减1小时 
select sysdate,to_char(sysdate-1/24/60,'yyyy-mm-dd HH24:MI:SS') from dual; --减1分钟 
select sysdate,to_char(sysdate-1/24/60/60,'yyyy-mm-dd HH24:MI:SS') from dual; --减1秒 
```

**ORACLE时间函数(SYSDATE)简析** 

*1:取得当前日期是本月的第几周* 

SQL> select to_char(sysdate,'YYYYMMDD W HH24:MI:SS') from dual; 
TO_CHAR(SYSDATE,'YY 
\------------------- 
20030327 4 18:16:09 
SQL> select to_char(sysdate,'W') from dual; 
T 
\- 
4 

*2:取得当前日期是一个星期中的第几天,注意星期日是第一天* 

SQL> select sysdate,to_char(sysdate,'D') from dual; 
SYSDATE T 
--------- -
27-MAR-03 5 
　　类似: 
select to_char(sysdate,'yyyy') from dual; --年 
select to_char(sysdate,'Q' from dual; --季 
select to_char(sysdate,'mm') from dual; --月 
select to_char(sysdate,'dd') from dual; --日 
ddd 年中的第几天 
WW 年中的第几个星期 
W 该月中第几个星期 
D 周中的星期几 
hh 小时(12) 
hh24 小时(24) 
Mi 分 
ss 秒 

*3:取当前日期是星期几中文显示:* 

SQL> select to_char(sysdate,'day') from dual; 
TO_CHAR(SYSDATE,'DAY') 
\---------------------- 

星期四 

*4:如果一个表在一个date类型的字段上面建立了索引，如何使用* 

`alter session set NLS_DATE_FORMAT='YYYY-MM-DD HH24:MI:SS' `

*5: 得到当前的日期* 
`select sysdate from dual; `

*6: 得到当天凌晨0点0分0秒的日期* 
`select trunc(sysdate) from dual; `
-- 得到这天的最后一秒 
`select trunc(sysdate) + 0.99999 from dual;` 
-- 得到小时的具体数值 
`select trunc(sysdate) + 1/24 from dual; `
`select trunc(sysdate) + 7/24 from dual; `

*7.得到明天凌晨0点0分0秒的日期* 
`select trunc(sysdate+1) from dual; `
`select trunc(sysdate)+1 from dual; `

*8: 本月一日的日期* 
`select trunc(sysdate,'mm') from dual; `

*9:得到下月一日的日期* 
`select trunc(add_months(sysdate,1),'mm') from dual; `

*10:返回当前月的最后一天?* 
`select last_day(sysdate) from dual; `
`select last_day(trunc(sysdate)) from dual; `
`select trunc(last_day(sysdate)) from dual; `
`select trunc(add_months(sysdate,1),'mm') - 1 from dual; `

*11: 得到一年的每一天* 
`select trunc(sysdate,'yyyy')+ rn -1 date0 
from 
(select rownum rn from all_objects 
where rownum<366); `

*12:今天是今年的第N天* 
`SELECT TO_CHAR(SYSDATE,'DDD') FROM DUAL; `

*13:如何在给现有的日期加上2年* 
`select add_months(sysdate,24) from dual; `

*14:判断某一日子所在年分是否为润年* 
`select decode(to_char(last_day(trunc(sysdate,'y')+31),'dd'),'29','闰年','平年') from dual; `

*15:判断两年后是否为润年* 
`select decode(to_char(last_day(trunc(add_months(sysdate,24),'y')+31),'dd'),'29','闰年','平年') from dual; `

*16:得到日期的季度* 
`select ceil(to_number(to_char(sysdate,'mm'))/3) from dual; `
`select to_char(sysdate, 'Q') from dual;`