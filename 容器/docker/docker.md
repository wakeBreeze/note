# docker学习笔记

## docker常用命令

[参考一](https://cloud.tencent.com/developer/article/1848185)



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