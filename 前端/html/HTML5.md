了解：

**W3C：**World Wide Web Consortium(万维网联盟)

![image-20210203173119490](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210203173119490.png)

## 基本标签

注释标签：<!-- -->

标题标签：<h1></h1>	<h2></h2>……

段落标签：<p></p>

换行标签：<br/>

水平线标签：<hr/>

粗体标签：<strong></strong>

斜体标签：<em></em>



**特殊符号：**

空格：`&nbsp;`

大于：`&gt;`

小于：`&lt;`

版权：`&copy;`



## 图像标签

**常见的图像格式**

- JPG
- GIF
- PNG
- BMP：位图

![image-20210203182227703](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210203182227703.png)



## 链接标签

![image-20210203182555658](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210203182555658.png)

![image-20210203183711388](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210203183711388.png)

**锚链接：**

```html
<!--锚链接-->

<!--使用name作为锚标记-->
<a name="top">顶部</a>

<!--跳转到标记-->
<a href="#top">跳转到顶部</a>
</body>
</html>
```

**功能性链接：**

```html
<!--功能性链接
邮件链接：mailto
QQ链接
-->
<a href="mailto:386859692@qq.com">点击联系我</a>

<a target="_blank" href="http://wpa.qq.com/msgrd?v=3&uin=&site=qq&menu=yes">
    <img border="0" src="http://wpa.qq.com/pa?p=2::53" alt="点击联系我" title="点击联系我"/>

```



## 行内元素和块元素

![image-20210203200801280](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210203200801280.png)



## 列表

![image-20210203201108880](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210203201108880.png)

```html
<!--有序列表order list
应用范围：试卷，问答...
-->
<ol>
    <li>Java</li>
    <li>Python</li>
    <li>运维</li>
    <li>前端</li>
    <li>C/C++</li>
</ol>
<hr/>
<!--无序列表unordered list
应用范围：导航，侧边栏...
-->
<ul>
    <li>Java</li>
    <li>Python</li>
    <li>运维</li>
    <li>前端</li>
    <li>C/C++</li>
</ul>

<!--自定义列表
dl:标签
dt：列表名称
dd：列表内容

应用：公司网站底部
-->
<dl>
    <dt>学科</dt>

    <dd>Java</dd>
    <dd>Python</dd>
    <dd>Linux</dd>
    <dd>C</dd>

    <dt>位置</dt>

    <dd>西安</dd>
    <dd>重庆</dd>
    <dd>成都</dd>

</dl>
```



## 表格标签

![image-20210203202902590](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210203202902590.png)

```html
<!--表格table
行：tr
列：td
-->
<table border="1" cellspacing="0">
    <tr>
        <!--colspan:跨列-->
        <td colspan="3" align="center" valign="bottom">学生成绩</td>
    </tr>
    <tr>
        <!--rowspan：跨行-->
        <td rowspan="2">狂神</td>
        <td>语文</td>
        <td>100</td>
    </tr>
    <tr>
        <td>数学</td>
        <td>100</td>
    </tr>
    <tr>
        <td rowspan="2">秦疆</td>
        <td>语文</td>
        <td>100</td>
    </tr>
    <tr>
        <td>数学</td>
        <td>100</td>
    </tr>
```

（1）.cellpadding：

从其名称看，此属性与padding类似。

cellpadding用来设置单元格中文本内容和边框之间的距离，类似于内边距。

（2）.cellspacing:

cell是单元格的意思，从字面意思是设置单元格之间或者单元格与外围边框的距离。

此属性类似于margin外边距。





## 媒体元素

```html
<!--音频audio
controls：控制条
autoplay：自动播放
-->
<audio src="../resources/audio/约束.flac" controls autoplay></audio>

<br/>

<!--视频video-->
<video src="../resources/video/ThrowAFit.mp4" controls ></video>
```



## 页面结构分析

![image-20210203215509102](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210203215509102.png)



## iframe内联框架

![image-20210203220319817](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210203220319817.png)

```html
<a href="https://www.cnblogs.com/code-xu/" target="mine">点击跳转到我的博客</a>

<br/>

<!--iframe内联框架
src：地址
width：宽度
height：高度
name:内联框架名，链接标签中的target可以设置为此属性。链接的内容将在此框架上展示。
-->
<iframe src="https://www.baidu.com" name="mine" frameborder="0" width="1000px" height="600px"></iframe>

<!-- 
<iframe src="//player.bilibili.com/player.html?aid=246389838&bvid=BV11v411s7sH&cid=291771539&page=1" 
scrolling="no" border="0" frameborder="no" framespacing="0" allowfullscreen="true"> 
</iframe> 
-->

```



## 表单

- get 和 post 提交
- 文本框和单选框  input type="text"、input type="radio"
- 按钮和多选框  input type="button"、input type="checkbox"
- 列表框、文本域和文件域  select-option、textarea、input type="file"
- 搜索框、滑块和简单验证  input type="search"、input type="range"

![image-20210203224944773](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210203224944773.png)

![image-20210203231309913](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210203231309913.png)



```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>表单学习</title>
</head>
<body>

<h1>注册</h1>

<!--表单form
action：表单提交的位置，可以是网站，也可以是一个请求处理地址
method：get，post 提交方式
    get：我们可以在url中看到我们提交的信息，不安全，高效
    post：比较安全，可以传输大文件。
-->

<form action="7.媒体元素.html" method="get">

    <!--文本输入框：input type="text"
     value="cool"   默认初始值
     maxlength="8"  最长能输入几个字符
     size="30"      文本框的长度
     -->
    <p>名字：<input type="text" name="username" placeholder="请输入用户名" required/></p>

    <!--密码框：input type="password"-->
    <p>密码：<input type="password" name="pwd" placeholder="请输入密码" required/></p>

    <!--单选框
    input type="radio"
    value:单选框的值
    name:表示组。同一组的才能单选
    checked:默认选中
    -->
    <p>性别：
        <input type="radio" value="boy" name="sex"  disabled/>男
        <input type="radio" value="girl" name="sex" checked/>女
    </p>

    <!--多选框
    input type="checkbox"
    -->
    <p>爱好：
        <input type="checkbox" value="code" name="hobby" checked/>敲代码
        <input type="checkbox" value="sleep" name="hobby"/>睡觉
        <input type="checkbox" value="game" name="hobby"/>玩游戏
        <input type="checkbox" value="swim" name="hobby"/>游泳
    </p>

    <!--按钮
    input type="button" 普通按钮
    input type="image"  图像按钮    会提交表单
    input type="submit" 提交按钮
    input type="reset"  重置按钮
    -->
    <p>按钮：
        <input type="button" name="btn1" value="点击变大">
        <input type="image" src="../resources/images/1.png" width="300px" height="200px">
    </p>

    <!--下拉框，列表框-->
    <p>国家：
        <select name="counties">
            <option value="China">中国</option>
            <option value="US">美国</option>
            <option value="India">印度</option>
            <option value="Switzerland" selected>瑞士</option>
        </select>
    </p>

    <!--文本域 textarea
    name="textarea"
    -->
    <p>反馈：
        <textarea name="textarea" id="" cols="30" rows="10" placeholder="请输入反馈信息"></textarea>
    </p>

    <!--文件域
    input type="file"
    name="files"
    -->
    <p>
        <input type="file" name="files"/>
        <input type="button" value="上传" name="upload">
    </p>

<!-- 验证 -->
    <!--邮箱验证
    input type="email"
    -->
    <p>邮箱：
        <input type="email" name="email">
    </p>

    <!--URL
    input type="url"
    -->
    <p>URL:
        <input type="url" name="url">
    </p>

    <!--数字
    input type="number"
    step:步值
    -->
    <p>商品数量：
        <input type="number" name="number" min="0" max="10" step="5">
    </p>

    <!--滑块
    input type="range"
    step：步值
    value：默认值
    -->
    <p>
        <input type="range" name="voice" min="0" max="100" step="1" value="80">
    </p>

    <!--搜索框
    input type="search"
    -->
    <p>搜索：
        <input type="search" name="search">
    </p>

    <!--增强鼠标可用性-->
    <p>
        <label for="mark">点我试试</label>
        <input type="text" id="mark">
    </p>

    <!--自定义邮箱
    常用正则表达式：
        https://www.jb51.net/article/76901.htm
        https://www.jb51.net/tools/regexsc.htm
        ^\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*$
    -->
    <p>
        <label for="diy">自定义邮箱：</label>
        <input type="text" id="diy" name="diyEmail" pattern="\w+([-+.]\w+)*@\w+([-.]\w+)*\.\w+([-.]\w+)*">
    </p>

    <p>
        <input type="submit"/>
        <input type="reset"/>
    </p>
</form>

</body>
</html>
```



**表单的应用**

![image-20210204102746112](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210204102746112.png)

![image-20210204104857209](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210204104857209.png)