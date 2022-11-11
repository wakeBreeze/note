# HEXO 博客搭建

## #安装[nodejs](https://nodejs.org/en/)

![image-20220123023804164](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20220123023804164.png)



查看nodejs版本

```bash
node -v
```

查看npm版本

```bash
npm -v
```

由于npm比较慢,安装cnpm(淘宝镜像)

```bash
npm install -g cnpm --registry=https://registry.npm.taobao.org
```



## #安装HEXO

全局卸载`HEXO`

```bash
npm uninstall hexo-cli -g
```



全局安装`HEXO`

```bash
cnpm install -g hexo-cli
```

查看HEXO版本

```bash
hexo -v
```

创建一个博客目录并初始化

```bash
-- 在目录中初始化
hexo init
```

启动博客

```bash
hexo s
```

通过链接访问博客

![image-20220123025858255](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20220123025858255.png)







## #HEXO相关操作

> Quick Start

### Create a new post

```bash
$ hexo new "My New Post"
```

More info: [Writing](https://hexo.io/docs/writing.html)

### Clean

```bash
$ hexo clean
```

### Generate static files

```bash
$ hexo generate
```

More info: [Generating](https://hexo.io/docs/generating.html)

### Run server

```bash
$ hexo server
```

More info: [Server](https://hexo.io/docs/server.html)

### Deploy to remote sites

```bash
$ hexo deploy
```

More info: [Deployment](https://hexo.io/docs/one-command-deployment.html)



## #将HEXO部署到GitHub

登陆github新建仓库

![image-20220123032729382](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20220123032729382.png)



在HEXO目录下安装git部署插件

```bash
cnpm install --save hexo-deployer-git
```

设置`_config.yml`文件 `deploy`属性

```yml
deploy:
  type: git
  repo: https://github.com/Windy-Rain/Wind-Rain.github.io.git
  branch: master
```

发布到github仓库

```bash
hexo d
```

