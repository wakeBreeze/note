## 安装

### 1、使用解压命令解压

```bash
tar -zxvf apache-zookeeper-3.6.2-bin.tar.gz 

```

### 2、修改配置文件

进入zookeeper配置文件目录 

```bash
cd apache-zookeeper-3.6.2-bin.tar.gz /conf 
```

将zoo_sample.cfg这个文件复制为zoo.cfg (必须是这个文件名) 

```bash
cp zoo_sample.cfg zoo.cfg 
```

进入zoo.cfg文件进行编辑 

```bash
vi zoo.cfg 
```

在文件中修改以下内容   

```tex
dataDir=/tmp/zookeeper/data 

dataLogDir=/tmp/zookeeper/log 
```

### 3、配置环境变量

打开配置文件    

```bash
vi /etc/profile 
```

在配置文件末尾添加以下配置 (/usr/local/zookeeper/更换为zookeeper的安装目录)  

```tex
export ZOOKEEPER_INSTALL=/usr/local/zookeeper/ 

export PATH=$PATH:$ZOOKEEPER_INSTALL/bin 
```

### 4、启动zookeeper，验证是否安装成功

```bash
#进入到zookeeper下的bin目录启动 
cd /usr/local/zookeeper/bin 

#启动 
./zkServer.sh start 

#停止 
./zkServer.sh stop  
```

### 5、设置开机启动

#### rc.local

进入到文件 

```bash
vi /etc/rc.d/rc.local
```

在文件中添加 

```tex
/usr/local/zookeeper-3.4.6/bin/zkServer.sh start
```

#### [配置为系统服务](https://blog.csdn.net/qq_30486109/article/details/113818696)

1、创建文件zookeeper

vim /etc/rc.d/init.d/zookeeper

```tex
#!/bin/bash
#chkconfig:2345 20 90
#description:zookeeper
#processname:zookeeper
export JAVA_HOME=/usr/java/jdk-11.0.10
case $1 in
        start) su root /usr/local/zookeeper/bin/zkServer.sh start;;
        stop) su root /usr/local/zookeeper/bin/zkServer.sh stop;;
        status) su root /usr/local/zookeeper/bin/zkServer.sh status;;
        restart) su root /usr/local/zookeeper/bin/zkServer.sh restart;;
        *) echo "require start|stop|status|restart" ;;
esac
```

2、加可执行权限

```bash
chmod +x /etc/rc.d/init.d/zookeeper
```

3、注册为系统服务

```bash
chkconfig --add zookeeper
```

删除系统服务

```bash
chkconfig --del zookeeper
```

4、添加开机自启动

```bash
chkconfig zookeeper on
```

关闭开机自启动

```bash
chkconfig zookeeper off
```

5、启动、关闭zookeeper服务

```bash
service zookeeper start
service zookeeper stop
```

6、查看服务列表

```bash
chkconfig --list
```

zookeeper             0:关    1:关    2:开    3:开    4:开    5:开    6:关

二、使用systemctl管理zookeeper服务(未验证)
1、创建文件zookeeper.service

```bash
vim /usr/lib/systemd/system/zookeeper.service
```

```tex
[Unit]
Description=zookeeper.service
After=network.target
ConditionPathExists=/usr/local/zookeeper/conf/zoo.cfg
[Service]
Type=forking
User=root
Group=root
ExecStart=/usr/local/zookeeper/bin/zkServer.sh start
ExecStop=/usr/local/zookeeper/bin/zkServer.sh stop
[Install]
WantedBy=multi-user.target
```

2.启动服务
重新加载服务

```bash
systemctl daemon-reload

```

启动，停止，查看，自启动服务

```bash
systemctl start zookeeper.service
systemctl stop zookeeper.service
systemctl status zookeeper.service
systemctl enable zookeeper.service
```

