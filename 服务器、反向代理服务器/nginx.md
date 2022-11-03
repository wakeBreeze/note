![image-20220802113244147](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20220802113244147.png)

![image-20220802113739192](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20220802113739192.png)

![image-20220802114232131](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20220802114232131.png)

docker run -itd --name nginx -p 8080:80 nginx



docker安装nginx

```bash
# 查看是否已经安装了nginx
docker images
# 拉取官方最新镜像
docker pull nginx
# 运行nginx容器
docker run -itd --name nginx-test -p 8080:80 nginx 

# 生成容器id
4356c883b25f70e5ffe677085efd018985b6661596f1fa5d62f9e65e94b6f2c1

# 参数说明：
	--name nginx-test：容器名称。
	-p 8080:80：端口进行映射，将本地8080端口映射到容器内部的80端口。
	-d nginx：设置容器在在后台一直运行。
	5、浏览器访问
	最后我们可以通过浏览器可以直接访问服务器的8080端口的nginx服务
```



首先创建挂载目录

```bash
mkdir -p /usr/dokcer_nginx_data/{conf,conf.d,html,log}
```

自定义nginx.conf文件

```bash
docker run --name nginx -d -p 80:80 -v /usr/local/docker_nginx_data/conf/nginx.conf:/etc/nginx/nginx.conf -v /usr/local/docker_nginx_data/log/:/var/log/nginx -v /usr/local/docker_nginx_data/html:/usr/share/nginx/html nginx
```





### docker nginx容器升级

[参考](https://www.hyluz.cn/post/67875.html)

背景：nginx存在漏洞需要升级

由于nginx是docker版本，也不知道容器内是否做过其他配置修改，所以不敢直接删容器更新镜像启动

#### 常规方法更新

下载源码--docker没有wget

安装wget--apt下载失败

更新apt源--还是莫名其妙失败

主机上wget下载完成使用docker cp拷进去--是拷进去了

解压，编译--没有make

安装make--陷入apt无法使用的死循环

#### 解决

- 尝试直接替换二进制文件 /usr/sbin/nginx

- 在自己服务器内起一个新版的nginx容器 提取其中的/usr/sbin/nginx 文件放置到现场容器的对应目录中

- 一开始复制的时候不要起nginx这个名字，否则容器会起不来，起一个nginx2啥的

- 中间报错缺少了几个库，或者库中没有相关函数

- 再次从自己的nginx容器中提取相关文件放置到容器（做好原库备份！！！）

![image.png](https://www.hyluz.cn/zb_users/upload/2022/07/202207051656988236110740.png)

- 最终nginx2可以执行了，此时可以尝试 cp nginx2 nginx

![image.png](https://www.hyluz.cn/zb_users/upload/2022/07/202207051656988279694451.png)

- 报错nginx正在使用，可以把nginx进程停止后覆盖，或者停止容器后搜索nginx2文件的位置

![image.png](https://www.hyluz.cn/zb_users/upload/2022/07/202207051656988349584284.png)

- cd进入该目录 将nginx2改成nginx

![image.png](https://www.hyluz.cn/zb_users/upload/2022/07/202207051656988387347066.png)

容器正常运行

测试业务是否正常



升级完毕

可以再进容器验证nginx版本

![image.png](https://www.hyluz.cn/zb_users/upload/2022/07/202207051656988463537819.png)

## 总结 

优点:非常安全，不涉及任何配置文件的修改，理论上一定可以成功(做好中间过程记录和文件备份)

缺点:如果原先没有做集群，业务会中断一段时间