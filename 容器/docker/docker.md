# docker学习笔记

## docker常用命令

[参考一](https://cloud.tencent.com/developer/article/1848185)

### 一、常规命令

#### 1、启动 Docker

```bash
# 微信公众号：ITester软件测试小栈
sudo systemctl start docker
```



#### 2、停止 Docker

```bash
# 微信公众号：ITester软件测试小栈
sudo systemctl stop docker
```



#### 3、重启 Docker

```bash
# 微信公众号：ITester软件测试小栈
sudo systemctl restart docker
```



#### 4、修改配置后重启 Docker

```bash
# 微信公众号：ITester软件测试小栈
sudo systemctl daemon-reload
sudo systemctl restart docker
```



#### 5、查看版本

```bash
# 微信公众号：ITester软件测试小栈
docker version
```



#### 6、查看Docker 信息

```bash
# 微信公众号：ITester软件测试小栈
docker info
```



#### 7、Docker 帮助

```bash
# 微信公众号：ITester软件测试小栈
docker --help
```





### 二、镜像命令

#### 1、查看Docker上已经安装的镜像

```bash
# 微信公众号：ITester软件测试小栈
docker images
```



#### 2、搜索Docker hub上面的镜像

```bash
# 微信公众号：ITester软件测试小栈
# 以tomcat为例
docker search tomcat
```



#### 3、下载镜像

```bash
# 微信公众号：ITester软件测试小栈
# 以下载tomcat为例
docker pull tomcat[:version]
```



#### 4、删除镜像

```bash
# 微信公众号：ITester软件测试小栈
# 以删除tomcat为例
docker rmi tomcat[:version]
# 通过镜像ID删除
docker rmi -f 镜像ID
# 通过镜像ID删除多个
docker rmi -f 镜像名1:TAG 镜像名2:TAG 
# 删除全部
# docker images -qa : 获取所有镜像ID
docker rmi -f $(docker images -qa)
```



### 三、容器命令

#### 1、启动容器

```bash
# 微信公众号：ITester软件测试小栈
docker run [options] image [command] [arg...]
```

##### 常用参数：

```bash
# 微信公众号：ITester软件测试小栈
-d: 后台运行容器,并返回容器ID

-i: 以交互式运行容器,通常与-t同时使用

-p: 端口映射,格式为 主机(宿主)端口:容器端口

-t: 为容器重新分配一个伪输入终端,通常与-i同时使用

--name="name": 为容器指定一个名称

--dns 8.8.8.8: 为容器指定一个dns服务器,默认与宿主一致

--dns-search domain:为容器指定一个DNS域名,默认与宿主一致

-h "hostname": 指定容器的hostname

-e arg="value": 设置环境变量

-env-file=[]:从指定文件读入环境变量

--cpuset="0-2" or --cpuset="0,1,2": 绑定容器到指定的cpu运行

-m: 设置容器使用内存最大值

--net="bridge": 指定容器的网络连接类型,支持bridge/host/none/container四种类型

--link=[]:添加链接到另外一个容器

--expose=[]:开放一个端口或一组端口,宿主机使用随机端口映射到开放的端口
```



##### 实例：

```bash
# 微信公众号：ITester软件测试小栈
docker run --name mynginx -d nginx:latest
# 映射多个端口
docker run -p 80:80/tcp -p 90:90 -v /data:/data -d nginx:latest
```



#### 2、查看正在运行的Docker 容器

```bash
# 微信公众号：ITester软件测试小栈
docker ps
```



##### 常用参数：

```bash
# 微信公众号：ITester软件测试小栈
# 显示所有容器，包括当前没有运行的容器
-a
# 显示最近创建的容器
-l
# 显示最近创建的N个容器
-n
# 静默模式,只显示容器ID
-q
# 不截断输出
--no-trunc
```



#### 3、退出容器

```bash
# 微信公众号：ITester软件测试小栈
# 退出并停止
exit
# 容器不停止退出
ctrl+P+Q
```



#### 4、启动容器

```bash
# 微信公众号：ITester软件测试小栈
docker start 容器ID或容器name
```



#### 5、重启容器

```bash
# 微信公众号：ITester软件测试小栈
docker restart 容器ID或容器name
```



#### 6、停止容器

```bash
# 微信公众号：ITester软件测试小栈
docker stop 容器ID或容器name
```



#### 7、强制停止容器

```bash
# 微信公众号：ITester软件测试小栈
docker kill 容器ID或容器name
```



#### 8、删除容器

```bash
# 微信公众号：ITester软件测试小栈
# 删除已经停止的容器
docker rm 容器ID或容器name 
# 强制删除已经停止或正在运行的容器
docker rm -f  容器ID或容器name 
#一次性删除所有正在运行的容器
docker rm -f $(docker ps -qa)
```



#### 9、从容器拷贝文件到宿主机

```bash
# 微信公众号：ITester软件测试小栈
docker cp 容器ID或容器名称:/文件路径与文件名 宿主机地址
```



##### 实例:

```bash
# 微信公众号：ITester软件测试小栈
拷贝容器coco的tmp文件夹下的info.txt到宿主机的当前位置
docker cp coco:/tmp/info.txt .
```





### 四、日志命令

```bash
# 微信公众号：ITester软件测试小栈
docker logs -f -t --tail 10 容器ID或容器名称
```



#### 参数说明：

```bash
# 微信公众号：ITester软件测试小栈
# 加入时间戳
-t
# 跟随最新的日志打印
-f
# 输出最后几行的日志
--tail 行数
```



#### 实例：

```bash
# 微信公众号：ITester软件测试小栈
docker logs -f -t --tail 10 5b66c8ab957e
```



结果如下：

![img](https://ask.qcloudimg.com/http-save/yehe-6478133/d5007896992190ec5b4637546160ba35.png?imageView2/2/w/1620)



## docker安装软件

### docker安装golang

[参考](https://blog.csdn.net/kevin_tech/article/details/104116592)

下载`dockerHub`上的镜像

```bash
docker pull golang
```

下载完镜像后用镜像运行一个容器：

```bash
#docker run --rm -it --name go golang bash
#以后台方式运行容器
docker run --name go -d golang
#启动容器go
docker start go
#以交互方式进入容器 go
docker exec -it go bash
```



上面这个命令用镜像 golang创建了一个名为 go的容器，在容器中创建了一个 Bash会话。--rm选项指定容器退出后自动移除容器。

查看 go的版本：

```bash
go version 
```


你可以根据自己的需要在https://hub.docker.com/_/golang 中查找自己需要的版本的 golang镜像运行容器。



