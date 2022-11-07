## tomcat安装

步骤：

### 1、使用解压命令解压 

```bash
tar -zxvf apache-tomcat-9.0.45.tar.gz 
```

### 2、查询系统防火墙情况

```bash
##查询防火墙状态 
systemctl status firewalld  
```

### 3、开启对应端口

以下状态则表示防火墙已开启，需要开放对应端口,未开启防火墙可跳过此步

![img](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\clip_image002.jpg)

```bash
#查看防火墙开启的端口 
firewall-cmd --zone=public --list-ports 

#开放8080端口 
firewall-cmd --zone=public --add-port=8080/tcp --permanent  

#重新加载防火墙 
firewall-cmd --reload  
```



### 4、设置开机自启

#### 4.1 rc.local

```bash
#打开启动文件 
vi /etc/rc.d/rc.local 

#在文件中添加启动命令 
source /etc/profile 
/opt/apache-tomcat-9.0.45/bin/startup.sh 
```



![img](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\clip_image004.jpg)



#### 4.2 [配置为系统服务](https://blog.csdn.net/qq_43080036/article/details/90064320)

一些服务器命令
查看全部服务命令：systemctl list-unit-files --type service      ctrl+c可以回到命令输入

```bash
查看全部服务命令：systemctl list-unit-files --type service      ctrl+c可以回到命令输入
 
查看服务：systemctl status 服务名.service
 
启动服务：systemctl start 服务名.service
 
停止服务：systemctl stop 服务名.service
 
重启服务：systemctl restart 服务名.service
 
增加开机启动：systemctl enable 服务名.service
 
删除开机启动：systemctl disable 服务名.service
 
注:.service 可以省略。
```

1.为Tomcat添加启动参数pid
   在tomcat/bin 目录下面，创建setenv.sh ,tomcat启动的时候会调用

在setenv.sh增加以下内容:

```txt
#add tomcat pid  
CATALINA_PID="$CATALINA_BASE/tomcat.pid"
#add java opts  
JAVA_OPTS="-server -XX:PermSize=256M -XX:MaxPermSize=1024m -Xms512M -Xmx1024M -XX:MaxNewSize=256m" 
```


2.在/usr/lib/systemd/system目录下增加tomcat.service,内容如下:

```bash
[Unit]
Description=Tomcat
After=syslog.target network.target remote-fs.target nss-lookup.target
[Service]
Type=forking
Environment="JAVA_HOME=/usr/java/jdk1.8.0_144"
PIDFile=/usr/apache/apache-tomcat-8.0.53/tomcat.pid
ExecStart=/usr/apache/apache-tomcat-8.0.53/bin/startup.sh
ExecReload=/bin/kill -s HUP $MAINPID
ExecStop=/bin/kill -s QUIT $MAINPID
PrivateTmp=true
[Install]
WantedBy=multi-user.target

注:/usr/java/jdk1.8.0_144改为自己的jdk路径
   /usr/apache/apache-tomcat-8.0.53改为自己的tomcat路径
```

3.配置开机启动

```bash
systemctl enable tomcat.service
```

4.其它

```bash
启动tomcat:  systemctl start tomcat

停止tomcat:  systemctl stop tomcat

重启tomcat:  systemctl restart tomcat

修改tomcat.service文件后使其生效：systemctl daemon-reload
```

5.出现的错误

```txt
Job for tomcat.service failed because a configured resource limit was exceeded. See "systemctl status tomcat.service" and "journalctl -xe" for details
```

确保setnev.sh，tomcat.service书写正确,符号，空格要多加注意
