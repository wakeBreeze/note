Java相关知识点收录

## 序列化/反序列化

[链接](https://mp.weixin.qq.com/s?__biz=MzU4ODI1MjA3NQ==&mid=2247486849&idx=1&sn=c0a2555ba22921187a28547b66877af8&scene=21#wechat_redirect)

### 序列化是干啥用的？

序列化的原本意图是希望对一个Java对象作一下“变换”，变成字节序列，这样一来方便持久化存储到磁盘，避免程序运行结束后对象就从内存里消失，另外变换成字节序列也更便于网络运输和传播，所以概念上很好理解：

- **序列化**：把Java对象转换为字节序列。
- **反序列化**：把字节序列恢复为原先的Java对象。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrIJpQyicWIiagQBV75jzuSkicaYIeibUVRdLOEYcW1ZtxZS5zfIlkqX78MZ0GImNB4QbPbprN2jW4ic3A/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

而且序列化机制从某种意义上来说也弥补了平台化的一些差异，毕竟转换后的字节流可以在其他平台上进行反序列化来恢复对象。

事情就是那么个事情，看起来很简单，不过后面的东西还不少，请往下看。

------

### 对象如何序列化？

然而Java目前并没有一个关键字可以直接去定义一个所谓的“可持久化”对象。

对象的持久化和反持久化需要靠程序员在代码里手动**显式地**进行序列化和反序列化还原的动作。

举个例子，假如我们要对`Student`类对象序列化到一个名为`student.txt`的文本文件中，然后再通过文本文件反序列化成`Student`类对象：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrIJpQyicWIiagQBV75jzuSkic0q7iakkFm0WIiacoyicO02ibJib1tgY3lnB1Dz1FpPXTDt5vLnJicsjGCY3g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

1、Student类定义

```java
public class Student implements Serializable {

    private String name;
    private Integer age;
    private Integer score;
    
    @Override
    public String toString() {
        return "Student:" + '\n' +
        "name = " + this.name + '\n' +
        "age = " + this.age + '\n' +
        "score = " + this.score + '\n'
        ;
    }
    
    // ... 其他省略 ...
}
```

2、序列化

```java
public static void serialize(  ) throws IOException {

    Student student = new Student();
    student.setName("CodeSheep");
    student.setAge( 18 );
    student.setScore( 1000 );

    ObjectOutputStream objectOutputStream = 
        new ObjectOutputStream( new FileOutputStream( new File("student.txt") ) );
    objectOutputStream.writeObject( student );
    objectOutputStream.close();
    
    System.out.println("序列化成功！已经生成student.txt文件");
    System.out.println("==============================================");
}
```

3、反序列化

```java
public static void deserialize(  ) throws IOException, ClassNotFoundException {
    ObjectInputStream objectInputStream = 
        new ObjectInputStream( new FileInputStream( new File("student.txt") ) );
    Student student = (Student) objectInputStream.readObject();
    objectInputStream.close();
    
    System.out.println("反序列化结果为：");
    System.out.println( student );
}
```

4、运行结果

控制台打印：

```java
序列化成功！已经生成student.txt文件
==============================================
反序列化结果为：
Student:
name = CodeSheep
age = 18
score = 1000
```

------

### Serializable接口有何用？

上面在定义`Student`类时，实现了一个`Serializable`接口，然而当我们点进`Serializable`接口内部查看，发现它**竟然是一个空接口**，并没有包含任何方法！

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrIJpQyicWIiagQBV75jzuSkicttzt9DsOcQgUcovoPYngBIhA9LbGnwYqKCHiastcIguGCApgicYtETCg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

试想，如果上面在定义`Student`类时忘了加`implements Serializable`时会发生什么呢？

实验结果是：此时的程序运行**会报错**，并抛出`NotSerializableException`异常：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrIJpQyicWIiagQBV75jzuSkicD8cHzLepjkd6eAkNgA1TNQHX3523zC5HmFjT7T34we96n6BDQcD2JA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

我们按照错误提示，由源码一直跟到`ObjectOutputStream`的`writeObject0()`方法底层一看，才恍然大悟：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrIJpQyicWIiagQBV75jzuSkicTxvcicQjC22DpAOXqgE0NmoU9QaXibhrQnoR3gBGvrOQ3F1WgribBSJZQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

如果一个对象既不是**字符串**、**数组**、**枚举**，而且也没有实现`Serializable`接口的话，在序列化时就会抛出`NotSerializableException`异常！

哦，我明白了！

原来`Serializable`接口也仅仅只是做一个标记用！！！

它告诉代码只要是实现了`Serializable`接口的类都是可以被序列化的！然而真正的序列化动作不需要靠它完成。

------

### `serialVersionUID`号有何用？

相信你一定经常看到有些类中定义了如下代码行，即定义了一个名为`serialVersionUID`的字段：

```java
private static final long serialVersionUID = -4392658638228508589L;
```

**你知道这句声明的含义吗？为什么要搞一个名为`serialVersionUID`的序列号？**

继续来做一个简单实验，还拿上面的`Student`类为例，我们并没有人为在里面显式地声明一个`serialVersionUID`字段。

我们首先还是调用上面的`serialize()`方法，将一个`Student`对象序列化到本地磁盘上的`student.txt`文件：

```java
public static void serialize() throws IOException {

    Student student = new Student();
    student.setName("CodeSheep");
    student.setAge( 18 );
    student.setScore( 100 );

    ObjectOutputStream objectOutputStream = 
        new ObjectOutputStream( new FileOutputStream( new File("student.txt") ) );
    objectOutputStream.writeObject( student );
    objectOutputStream.close();
}
```

接下来我们在`Student`类里面动点手脚，比如在里面再增加一个名为`studentID`的字段，表示学生学号：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrIJpQyicWIiagQBV75jzuSkicqcaQj6YSG9ISwqzwQQKOxDEE2Y30eGzBALk0RnKJZV71DxoEtWZRHQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这时候，我们拿刚才已经序列化到本地的`student.txt`文件，还用如下代码进行反序列化，试图还原出刚才那个`Student`对象：

```java
public static void deserialize(  ) throws IOException, ClassNotFoundException {
    ObjectInputStream objectInputStream = 
        new ObjectInputStream( new FileInputStream( new File("student.txt") ) );
    Student student = (Student) objectInputStream.readObject();
    objectInputStream.close();
    
    System.out.println("反序列化结果为：");
    System.out.println( student );
}
```

运行发现**报错了**，并且抛出了`InvalidClassException`异常：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrIJpQyicWIiagQBV75jzuSkicntTwPEibqSTicsicJBDicxgx7SVcNx8oGoY2aBO4uMxgL3X5VAwu2DyxHg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这地方提示的信息非常明确了：序列化前后的`serialVersionUID`号码不兼容！

从这地方最起码可以得出**两个**重要信息：

- **1、serialVersionUID是序列化前后的唯一标识符**
- **2、默认如果没有人为显式定义过`serialVersionUID`，那编译器会为它自动声明一个！**

**第1个问题：** `serialVersionUID`序列化ID，可以看成是序列化和反序列化过程中的“暗号”，在反序列化时，JVM会把字节流中的序列号ID和被序列化类中的序列号ID做比对，只有两者一致，才能重新反序列化，否则就会报异常来终止反序列化的过程。

**第2个问题：** 如果在定义一个可序列化的类时，没有人为显式地给它定义一个`serialVersionUID`的话，则Java运行时环境会根据该类的各方面信息自动地为它生成一个默认的`serialVersionUID`，一旦像上面一样更改了类的结构或者信息，则类的`serialVersionUID`也会跟着变化！

所以，为了`serialVersionUID`的确定性，写代码时还是建议，凡是`implements Serializable`的类，都最好人为显式地为它声明一个`serialVersionUID`明确值！

当然，如果不想手动赋值，你也可以借助IDE的自动添加功能，比如我使用的`IntelliJ IDEA`，按`alt + enter`就可以为类自动生成和添加`serialVersionUID`字段，十分方便：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrIJpQyicWIiagQBV75jzuSkicbib8sMHBaC7h7iax1EMVTYicCWCss6B6NoXagvejB85TMvz8OFA0zFI3g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

------



### 两种特殊情况

- 1、凡是被`static`修饰的字段是不会被序列化的
- 2、凡是被`transient`修饰符修饰的字段也是不会被序列化的

**对于第一点**，因为序列化保存的是**对象的状态**而非类的状态，所以会忽略`static`静态域也是理所应当的。

**对于第二点**，就需要了解一下`transient`修饰符的作用了。

如果在序列化某个类的对象时，就是不希望某个字段被序列化（比如这个字段存放的是隐私值，如：`密码`等），那这时就可以用`transient`修饰符来修饰该字段。

比如在之前定义的`Student`类中，加入一个**密码字段**，但是不希望序列化到`txt`文本，则可以：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrIJpQyicWIiagQBV75jzuSkico3ynBOQE7ibZG1Ej7pLt0yrhicuIqx2wYIt3UofwdN6KhAK9hDarr3Xg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这样在序列化`Student`类对象时，`password`字段会设置为默认值`null`，这一点可以从反序列化所得到的结果来看出：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrIJpQyicWIiagQBV75jzuSkicL4Ow6QdrRrScmIDSWib5ic9o6SdM8f4V3tPvJAWjHLgiaiaNbiaKqpOvwxw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

------

### 序列化的受控和加强

#### **约束性加持**

从上面的过程可以看出，序列化和反序列化的过程其实是**有漏洞的**，因为从序列化到反序列化是有中间过程的，如果被别人拿到了中间字节流，然后加以伪造或者篡改，那反序列化出来的对象就会有一定风险了。

毕竟反序列化也相当于一种 **“隐式的”对象构造** ，因此我们希望在反序列化时，进行**受控的**对象反序列化动作。

那怎么个受控法呢？

**答案就是：** 自行编写`readObject()`函数，用于对象的反序列化构造，从而提供约束性。

既然自行编写`readObject()`函数，那就可以做很多可控的事情：比如各种判断工作。

还以上面的`Student`类为例，一般来说学生的成绩应该在`0 ~ 100`之间，我们为了防止学生的考试成绩在反序列化时被别人篡改成一个奇葩值，我们可以自行编写`readObject()`函数用于反序列化的控制：

```java
private void readObject( ObjectInputStream objectInputStream ) throws IOException, ClassNotFoundException {

    // 调用默认的反序列化函数
    objectInputStream.defaultReadObject();

    // 手工检查反序列化后学生成绩的有效性，若发现有问题，即终止操作！
    if( 0 > score || 100 < score ) {
        throw new IllegalArgumentException("学生分数只能在0到100之间！");
    }
}
```

比如我故意将学生的分数改为`101`，此时反序列化立马终止并且报错：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrIJpQyicWIiagQBV75jzuSkic2xoLlv280vF0cslHiaO5WvFxeW5ua64hRqlogFUMWq9PPa6QJYNHQ7g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

对于上面的代码，有些小伙伴可能会好奇，为什么自定义的`private`的`readObject()`方法可以被自动调用，这就需要你跟一下底层源码来一探究竟了，我帮你跟到了`ObjectStreamClass`类的最底层，看到这里我相信你一定恍然大悟：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrIJpQyicWIiagQBV75jzuSkicecNR0ib95QhaGyb1bdWOiaZuYzohRl2T1H0ZzCPYd1ok3q0BZCsPQ3uQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

又是反射机制在起作用！是的，在Java里，果然万物皆可“反射”（滑稽），即使是类中定义的`private`私有方法，也能被抠出来执行了，简直引起舒适了。

#### **单例模式增强**

一个容易被忽略的问题是：**可序列化的单例类有可能并不单例**！

举个代码小例子就清楚了。

比如这里我们先用`java`写一个常见的「静态内部类」方式的单例模式实现：

```java
public class Singleton implements Serializable {

    private static final long serialVersionUID = -1576643344804979563L;

    private Singleton() {
    }

    private static class SingletonHolder {
        private static final Singleton singleton = new Singleton();
    }

    public static synchronized Singleton getSingleton() {
        return SingletonHolder.singleton;
    }
}
```

然后写一个验证主函数：

```java
public class Test2 {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        ObjectOutputStream objectOutputStream =
                new ObjectOutputStream(
                    new FileOutputStream( new File("singleton.txt") )
                );
        // 将单例对象先序列化到文本文件singleton.txt中
        objectOutputStream.writeObject( Singleton.getSingleton() );
        objectOutputStream.close();

        ObjectInputStream objectInputStream =
                new ObjectInputStream(
                    new FileInputStream( new File("singleton.txt") )
                );
        // 将文本文件singleton.txt中的对象反序列化为singleton1
        Singleton singleton1 = (Singleton) objectInputStream.readObject();
        objectInputStream.close();

        Singleton singleton2 = Singleton.getSingleton();

        // 运行结果竟打印 false ！
        System.out.println( singleton1 == singleton2 );
    }

}
```

运行后我们发现：**反序列化后的单例对象和原单例对象并不相等**了，这无疑没有达到我们的目标。

**解决办法是**：在单例类中手写`readResolve()`函数，直接返回单例对象，来规避之：

```java
private Object readResolve() {
    return SingletonHolder.singleton;
}
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrIJpQyicWIiagQBV75jzuSkic5GMKhvo0AI3o81fEobQJJURsu0V0oa21X0I77z9C5yfkjTYYcMlibqQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这样一来，当反序列化从流中读取对象时，`readResolve()`会被调用，用其中返回的对象替代反序列化新建的对象。





## 枚举 enum

### 为什么需要枚举

**常量定义它不香吗？为啥非得用枚举？**

举个栗子，就以B站上传视频为例，视频一般有三个状态：**草稿**、**审核**和**发布**，我们可以将其定义为**静态常量**：

```java
publicclass VideoStatus {
    
    publicstaticfinalint Draft = 1; //草稿
    
    publicstaticfinalint Review = 2; //审核
    
    publicstaticfinalint Published = 3; //发布
}
```

对于这种**单值类型**的静态常量定义，本身也没错，主要是在使用的地方没有一个明确性的约束而已，比如：

```java
void judgeVideoStatus( int status ) {
    
    ...
    
}
```

比如这里的 `judgeVideoStatus` 函数的本意是传入 `VideoStatus` 的三种静态常量之一，但由于没有类型上的约束，因此传入任意一个`int`值都是可以的，编译器也不会提出任何警告。

但是在枚举类型出现之后，上面这种情况就可以用枚举严谨地去约束，比如用枚举去定义视频状态就非常简洁了：

```java
publicenum VideoStatus {
    Draft, Review, Published
}
```

而且主要是在用枚举的地方会有更强的**类型约束**：

```java
// 入参就有明确类型约束
void judgeVideoStatus( VideoStatus status ) {
    
    ...
    
}
```

这样在使用 `judgeVideoStatus` 函数时，入参类型就会受到明确的类型约束，一旦传入无效值，编译器就会帮我们检查，从而规避潜在问题。

除此之外，枚举在扩展性方面比普常量更方便、也更优雅。

------

### 重新系统认识一下枚举

还是拿前文《[**答应我，别再if/else走天下了可以吗**](http://mp.weixin.qq.com/s?__biz=MzU4ODI1MjA3NQ==&mid=2247484807&idx=1&sn=27de517d6b992fb03a0a6ab637189125&chksm=fdded343caa95a550ab3b3da530c11762eaea1ab95dfc9e826643e1da21f16c28d3541287214&scene=21#wechat_redirect)》中的那个例子来说：比如，在后台管理系统中，肯定有用户角色一说，而且角色一般都是固定的，适合定义成一个枚举：

```java
publicenum UserRole {

    ROLE_ROOT_ADMIN,  // 系统管理员

    ROLE_ORDER_ADMIN, // 订单管理员

    ROLE_NORMAL       // 普通用户
}
```

接下来我们就用这个`UserRole`为例来说明**枚举的所有基本用法**：

```java
UserRole role1 = UserRole.ROLE_ROOT_ADMIN;
UserRole role2 = UserRole.ROLE_ORDER_ADMIN;
UserRole role3 = UserRole.ROLE_NORMAL;

// values()方法：返回所有枚举常量的数组集合
for ( UserRole role : UserRole.values() ) {
    System.out.println(role);
}
// 打印：
// ROLE_ROOT_ADMIN
// ROLE_ORDER_ADMIN
// ROLE_NORMAL

// ordinal()方法：返回枚举常量的序数，注意从0开始
System.out.println( role1.ordinal() ); // 打印0
System.out.println( role2.ordinal() ); // 打印1
System.out.println( role3.ordinal() ); // 打印2

// compareTo()方法：枚举常量间的比较
System.out.println( role1.compareTo(role2) ); //打印-1
System.out.println( role2.compareTo(role3) ); //打印-1
System.out.println( role1.compareTo(role3) ); //打印-2

// name()方法：获得枚举常量的名称
System.out.println( role1.name() ); // 打印ROLE_ROOT_ADMIN
System.out.println( role2.name() ); // 打印ROLE_ORDER_ADMIN
System.out.println( role3.name() ); // 打印ROLE_NORMAL

// valueOf()方法：返回指定名称的枚举常量
System.out.println( UserRole.valueOf( "ROLE_ROOT_ADMIN" ) );
System.out.println( UserRole.valueOf( "ROLE_ORDER_ADMIN" ) );
System.out.println( UserRole.valueOf( "ROLE_NORMAL" ) );
```

除此之外，枚举还可以用于`switch`语句中，而且意义更加明确：

```java
UserRole userRole = UserRole.ROLE_ORDER_ADMIN;
switch (userRole) {
    case ROLE_ROOT_ADMIN:  // 比如此处的意义就非常清晰了，比1，2，3这种数字好！
        System.out.println("这是系统管理员角色");
        break;
    case ROLE_ORDER_ADMIN:
        System.out.println("这是订单管理员角色");
        break;
    case ROLE_NORMAL:
        System.out.println("这是普通用户角色");
        break;
}
```

------

### 自定义扩充枚举

上面展示的枚举例子非常简单，仅仅是**单值的情形**，而实际项目中用枚举往往是**多值**用法。

比如，我想扩充一下上面的`UserRole`枚举，在里面加入 **角色名 -- 角色编码** 的对应关系，这也是实际项目中常用的用法。

这时候我们可以在枚举里自定义各种属性、构造函数、甚至各种方法：

```java
publicenum UserRole {

    ROLE_ROOT_ADMIN( "系统管理员", 000000 ),
    ROLE_ORDER_ADMIN( "订单管理员", 100000 ),
    ROLE_NORMAL( "普通用户", 200000 ),
    ;

    // 以下为自定义属性
    
    privatefinal String roleName;  //角色名称

    privatefinal Integer roleCode; //角色编码

    // 以下为自定义构造函数
    
    UserRole( String roleName, Integer roleCode ) {
        this.roleName = roleName;
        this.roleCode = roleCode;
    }

    // 以下为自定义方法
    
    public String getRoleName() {
        returnthis.roleName;
    }

    public Integer getRoleCode() {
        returnthis.roleCode;
    }

    public static Integer getRoleCodeByRoleName( String roleName ) {
        for( UserRole enums : UserRole.values() ) {
            if( enums.getRoleName().equals( roleName ) ) {
                return enums.getRoleCode();
            }
        }
        returnnull;
    }

}
```

从上述代码可知，在`enum`枚举类中完全可以像在普通`Class`里一样声明属性、构造函数以及成员方法。

------

### 枚举 + 接口 = ?

比如在我的前文《[**答应我，别再if/else走天下了可以吗**](http://mp.weixin.qq.com/s?__biz=MzU4ODI1MjA3NQ==&mid=2247484807&idx=1&sn=27de517d6b992fb03a0a6ab637189125&chksm=fdded343caa95a550ab3b3da530c11762eaea1ab95dfc9e826643e1da21f16c28d3541287214&scene=21#wechat_redirect)》中讲烦人的`if/else`消除时，就讲过如何通过**让枚举去实现接口**来方便的完成。

这地方不妨再回顾一遍：

什么角色能干什么事，这很明显有一个对应关系，所以我们首先定义一个公用的接口`RoleOperation`，表示不同角色所能做的操作：

```java
publicinterface RoleOperation {
    String op();  // 表示某个角色可以做哪些op操作
}
```

接下来我们将不同角色的情况全部交由枚举类来做，定义一个枚举类`RoleEnum`，并让它去实现`RoleOperation`接口：

```java
publicenum RoleEnum implements RoleOperation {

    // 系统管理员(有A操作权限)
    ROLE_ROOT_ADMIN {
        @Override
        public String op() {
            return"ROLE_ROOT_ADMIN:" + " has AAA permission";
        }
    },

    // 订单管理员(有B操作权限)
    ROLE_ORDER_ADMIN {
        @Override
        public String op() {
            return"ROLE_ORDER_ADMIN:" + " has BBB permission";
        }
    },

    // 普通用户(有C操作权限)
    ROLE_NORMAL {
        @Override
        public String op() {
            return"ROLE_NORMAL:" + " has CCC permission";
        }
    };
}
```

这样，在调用处就变得异常简单了，一行代码就行了，根本不需要什么`if/else`：

```java
publicclass JudgeRole {
    public String judge( String roleName ) {
        // 一行代码搞定！之前的if/else灰飞烟灭
        return RoleEnum.valueOf(roleName).op();
    }
}
```

而且这样一来，以后假如我想扩充条件，只需要去枚举类中**加代码**即可，而不用改任何老代码，非常符合**开闭原则**！

------

### 枚举与设计模式

什么？枚举还能实现设计模式？

是的！不仅能而且还能实现好几种！

**1、单例模式**

```java
publicclass Singleton {

    // 构造函数私有化，避免外部创建实例
    private Singleton() {

    }

    //定义一个内部枚举
    publicenum SingletonEnum{

        SEED;  // 唯一一个枚举对象，我们称它为“种子选手”！

        private Singleton singleton;

        SingletonEnum(){
            singleton = new Singleton(); //真正的对象创建隐蔽在此！
        }

        public Singleton getInstnce(){
            return singleton;
        }
    }

    // 故意外露的对象获取方法，也是外面获取实例的唯一入口
    public static Singleton getInstance(){
        return SingletonEnum.SEED.getInstnce(); // 通过枚举的种子选手来完成
    }
}
```

**2、策略模式**

这个也比较好举例，比如用枚举就可以写出一个基于策略模式的加减乘除计算器

```java
publicclass Test {

    publicenum Calculator {

        ADDITION {
            public Double execute( Double x, Double y ) {
                return x + y; // 加法
            }
        },

        SUBTRACTION {
            public Double execute( Double x, Double y ) {
                return x - y; // 减法
            }
        },

        MULTIPLICATION {
            public Double execute( Double x, Double y ) {
                return x * y; // 乘法
            }
        },


        DIVISION {
            public Double execute( Double x, Double y ) {
                return x/y;  // 除法
            }
        };

        public abstract Double execute(Double x, Double y);
    }
    
    public static void main(String[] args) {
        System.out.println( Calculator.ADDITION.execute( 4.0, 2.0 ) );
        // 打印 6.0
        System.out.println( Calculator.SUBTRACTION.execute( 4.0, 2.0 ) );
        // 打印 2.0
        System.out.println( Calculator.MULTIPLICATION.execute( 4.0, 2.0 ) );
        // 打印 8.0
        System.out.println( Calculator.DIVISION.execute( 4.0, 2.0 ) );
        // 打印 2.0
    }
}
```

------

### 专门用于枚举的集合类

我们平常一般习惯于使用诸如：`HashMap` 和 `HashSet`等集合来盛放元素，而对于枚举，有它专门的集合类：`EnumSet`和`EnumMap`

**1、EnumSet**

`EnumSet` 是专门为盛放枚举类型所设计的 `Set` 类型。

还是举例来说，就以文中开头定义的角色枚举为例：

```java
publicenum UserRole {

    ROLE_ROOT_ADMIN,  // 系统管理员

    ROLE_ORDER_ADMIN, // 订单管理员

    ROLE_NORMAL       // 普通用户
}
```

比如系统里来了一批人，我们需要查看他是不是某个角色中的一个：

```java
// 定义一个管理员角色的专属集合
EnumSet<UserRole> userRolesForAdmin
    = EnumSet.of(
        UserRole.ROLE_ROOT_ADMIN,
        UserRole.ROLE_ORDER_ADMIN
    );

// 判断某个进来的用户是不是管理员
Boolean isAdmin( User user ) {
    
    if( userRoles.contains( user.getUserRole() ) )
        returntrue;
    
    returnfalse;
}
```

**2、EnumMap**

同样，`EnumMap` 则是用来专门盛放枚举类型为`key`的 `Map` 类型。

比如，系统里来了一批人，我们需要统计不同的角色到底有多少人这种的话：

```java
Map<UserRole,Integer> userStatisticMap = new EnumMap<>(UserRole.class);

for ( User user : userList ) {
    Integer num = userStatisticMap.get( user.getUserRole() );
    if( null != num ) {
        userStatisticMap.put( user.getUserRole(), num+1 );
    } else {
        userStatisticMap.put( user.getUserRole(), 1 );
    }
}
```

用`EnumMap`可以说非常方便了。





## hashCode和hash算法

本文会围绕以下几个点来讲：

`什么是hashCode？
hashCode和equals的关系
剖析hashMap的hash算法（重点）`

为什么会有hashCode
先抛一个结论

`hashCode的设计初衷是提高哈希容器的性能`

抛开hashCode，现在让你对比两个对象是否相等，你会怎么做？

`thisObj == thatObj
thisObj.equals(thatObj)`

我想不出第三种了，而且这两种其实没啥大的区别，object的equals()方法底层也是==，jdk1.8 Object类的第148行；

```java
    public boolean equals(Object obj) {
        return (this == obj);
    }

```

为什么有了equals还要有hashCode？上面说了，hashCode的设计初衷是提高哈希容器的性能，equals的效率是没有hashCode高的，不信的可以自己去试一下；

像我们常用的HashMap、HashTable等，某些场景理论上讲是可以不要hashCode的，但是会牺牲很多性能，这肯定不是我们想看到的；

### 什么是hashCode

知道hashCode存在的意义后，我们来研究下hashCode，看下长什么样

对象调用hashCode方法后，会返回一串int类型的数字码

```java
Car car = new Car();
log.info("对象的hashcode：{}", car.hashCode());
log.info("1433223的hashcode：{}", "1433223".hashCode());
log.info("郭德纲的hashcode：{}", "郭德纲".hashCode());
log.info("小郭德纲的hashcode：{}", "小郭德纲".hashCode());
log.info("彭于晏的hashcode：{}", "彭于晏".hashCode());
log.info("唱跳rap篮球的hashcode：{}", "唱跳rap篮球".hashCode());

```

运行结果

`对象的hashcode：357642
1433223的hashcode：2075391824
郭德纲的hashcode：36446088
小郭德纲的hashcode：738530585
彭于晏的hashcode：24125870
唱跳rap篮球的hashcode：-767899628      ##因为返回值是int类型，有负数很正常`

可以看出，对象的hashcode值跟对象本身的值没啥联系，比如郭德纲和小郭德纲，虽然只差一个字，它们的hashCode值没半毛钱关系~

hashCode和equals的关系

java规定：

`如果两个对象的hashCode()相等，那么他们的equals()不一定相等。
如果两个对象的equals()相等，那么他们的hashCode()必定相等。`

还有一点，重写equals()方法时候一定要重写hashCode()方法，不要问为什么，无脑写就行了，会省很多事

### hash算法

前面都是铺垫，这才是今天的主题

我们以HashMap的hash算法来看，个人认为这是很值得搞懂的hash算法，设计超级超级巧妙

```java
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

```

这是hashMap的hash算法，我们一步一步来看

```java
(h = key.hashCode()) ^ (h >>> 16)
```

hashCode就hashCode嘛，为啥还要>>>16，这个 ^ 又是啥，不着急一个一个来说

hashMap我们知道默认初始容量是16，也就是有16个桶，那hashmap是通过什么来计算出put对象的时候该放到哪个桶呢

```java
    final Node<K,V> getNode(int hash, Object key) {
        Node<K,V>[] tab; Node<K,V> first, e; int n; K k;
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (first = tab[(n - 1) & hash]) != null) {
            if (first.hash == hash && // always check first node
                ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            if ((e = first.next) != null) {
                if (first instanceof TreeNode)
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        return null;
    }

```

上面是hashmap的getNode方法，对hashmap源码有兴趣的同学自行研究，我们今天主要看这一句：(n - 1) & hash

也就是说hashmap是通过数组长度-1&key的hash值来计算出数组下标的，这里的hash值就是上面(h = key.hashCode()) ^ (h >>> 16)计算出来的值

不要慌不要慌不要慌，看不懂没关系，我们现在总结下目前的疑问

`为什么数组长度要 - 1，直接数组长度&key.hashCode不行吗
为什么要length-1 & key.hashCode计算下标，而不是用key.hashCode % length
为什么要^运算
为什么要>>>16`

先说结论

`数组长度-1、^运算、>>>16，这三个操作都是为了让key在hashmap的桶中尽可能分散;
用&而不用%是为了提高计算性能`

我们先看下如果数组长度不-1和不进行>>>16运算造成的结果，知道了结果我们后面才来说为什么，这样子更好理解

```java
log.info("数组长度不-1：{}", 16 & "郭德纲".hashCode());
log.info("数组长度不-1：{}", 16 & "彭于晏".hashCode());
log.info("数组长度不-1：{}", 16 & "李小龙".hashCode());
log.info("数组长度不-1：{}", 16 & "蔡徐鸡".hashCode());
log.info("数组长度不-1：{}", 16 & "唱跳rap篮球鸡叫".hashCode());

log.info("数组长度-1但是不进行异或和>>>16运算：{}", 15 & "郭德纲".hashCode());
log.info("数组长度-1但是不进行异或和>>>16运算：{}", 15 & "彭于晏".hashCode());
log.info("数组长度-1但是不进行异或和>>>16运算：{}", 15 & "李小龙".hashCode());
log.info("数组长度-1但是不进行异或和>>>16运算：{}", 15 & "蔡徐鸡".hashCode());
log.info("数组长度-1但是不进行异或和>>>16运算：{}", 15 & "唱跳rap篮球鸡叫".hashCode());

log.info("数组长度-1并且进行异或和>>>16运算：{}", 15 & ("郭德纲".hashCode()^("郭德纲".hashCode()>>>16)));
log.info("数组长度-1并且进行异或和>>>16运算：{}", 15 & ("彭于晏".hashCode()^("彭于晏".hashCode()>>>16)));
log.info("数组长度-1并且进行异或和>>>16运算：{}", 15 & ("李小龙".hashCode()^("李小龙".hashCode()>>>16)));
log.info("数组长度-1并且进行异或和>>>16运算：{}", 15 & ("蔡徐鸡".hashCode()^("蔡徐鸡".hashCode()>>>16)));
log.info("数组长度-1并且进行异或和>>>16运算：{}", 15 & ("唱跳rap篮球鸡叫".hashCode()^("唱跳rap篮球鸡叫".hashCode()>>>16)));

```

`数组长度不-1：0
数组长度不-1：0
数组长度不-1：16
数组长度不-1：16
数组长度不-1：16
数组长度-1但是不进行异或和>>>16运算：8
数组长度-1但是不进行异或和>>>16运算：14
数组长度-1但是不进行异或和>>>16运算：8
数组长度-1但是不进行异或和>>>16运算：2
数组长度-1但是不进行异或和>>>16运算：14
数组长度-1并且进行异或和>>>16运算：4
数组长度-1并且进行异或和>>>16运算：14
数组长度-1并且进行异或和>>>16运算：7
数组长度-1并且进行异或和>>>16运算：13
数组长度-1并且进行异或和>>>16运算：2`

一下就看出区别了哇，第一组返回的下标就只有0和16，第二组也只有2、8、14，第三组的下标就很分散，这才是我们想要的

这结合hashMap来看，前两组造成的影响就是key几乎全部怼到同一个桶里，及其不分散，用行话讲就是有太多的hash冲突，这对hashMap的性能有很大影响，hash冲突造成的链表红黑树转换那些具体的原因这里就不展开说了
而且！！
而且！！
而且！！
如果数组长度不 - 1，刚上面也看到了，会返回16这个下标，数组总共长度才16，下标最大才15，16越界了呀

### 原理

知道了结果，现在说说其中的玄学

#### **1、为什么数组长度要 - 1，直接数组长度&key.hashCode不行吗?**

**我们先不考虑数组下标越界的问题**，hashMap默认长度是16，看看16的二进制码是多少

```java
log.info("16的二进制码：{}",Integer.toBinaryString(16));  
//16的二进制码：10000，

```

再看看key.hashCode()的二进制码是多少，以郭德纲为例

```java
log.info("key的二进制码：{}",Integer.toBinaryString("郭德纲".hashCode()));
//key的二进制码：10001011000001111110001000

length & key.hashCode()  => 10000 & 10001011000001111110001000
位数不够，高位补0，即

0000 0000 0000 0000 0000 0001 0000 
                & 
0010 0010 1100 0001 1111 1000 1000

&运算规则是第一个操作数的的第n位于第二个操作数的第n位都为1才为1，否则为0
所以结果为0000 0000 0000 0000 0000 0000 0000，即 0

```

冷静分析，问题就出在16的二进制码上，它码是10000，只有遇到hash值二进制码倒数第五位为1的key他们&运算的结果才不等于0，这句话好好理解下，看不懂就别强制看，去摸会儿鱼再回来看

再来看16-1的二进制码，它码是1111，同样用郭德纲这个key来举例

```java
(length-1) & key.hashCode()  => 1111 & 10001011000001111110001000
位数不够，高位补0，即

0000 0000 0000 0000 0000 0000 1111 
                & 
0010 0010 1100 0001 1111 1000 1000

&运算规则是第一个操作数的的第n位于第二个操作数的第n位都为1才为1，否则为0
所以结果为0000 0000 0000 0000 0000 0000 1000，即 8
```

如果还看不出这其中的玄机，你就多搞几个key来试试，总之记住，限制它们&运算的结果就会有很多种可能性了，不再受到hash值二进制码倒数第五位为1才能为1的限制

#### 2、为什么要length-1&key.hashCode计算下标，而不是用key.hashCode%length?

这个其实衍生出三个知识点

1、其实(length-1)&key.hashCode计算出来的值和key.hashCode%length是一样的

```java
log.info("(length-1)&key.hashCode：{}",15&"郭德纲".hashCode());
log.info("key.hashCode%length：{}","郭德纲".hashCode()%16);

//  (length-1)&key.hashCode：8
//  key.hashCode%length：8
```

那你可能更蒙逼了，都一样的为啥不用%，这就要说到第二个知识点

2、只有当length为2的n次方时，(length-1)&key.hashCode才等于key.hashCode%length，比如当length为15时

```java
log.info("(length-1)&key的hash值：{}",14&"郭德纲".hashCode());
log.info("key的hash值%length：{}","郭德纲".hashCode()%15);

//  (length-1)&key.hashCode：8
//  key.hashCode%length：3
```

可能又有小朋友会思考，我不管那我就想用%运算，要用魔法打败魔法，请看第三点

3、用&而不用%是为了提高计算性能，对于处理器来讲，&运算的效率是高于%运算的，就这么简单，除此之外，除法的效率也没&高

#### 3、为什么要进行^运算，|运算、&运算不行吗?

这是异或运算符，第一个操作数的的第n位于第二个操作数的第n位相反才为1，否则为0
我们多算几个key的值出来对比

```java
//不进行异或运算返回的数组下标
log.info("郭德纲：{}", Integer.toBinaryString("郭德纲".hashCode()));            
log.info("彭于晏：{}", Integer.toBinaryString("彭于晏".hashCode()));            
log.info("李小龙：{}", Integer.toBinaryString("李小龙".hashCode()));            
log.info("蔡徐鸡：{}", Integer.toBinaryString("蔡徐鸡".hashCode()));            
log.info("唱跳rap篮球鸡叫：{}", Integer.toBinaryString("唱跳rap篮球鸡叫".hashCode()));

00001000101100000111111000 1000
00000101110000001000011010 1110
00000110001111100100010011 1000
00000111111111111100010111 0010
10111010111100100011001111 1110

进行&运算，看下它们返回的数组下标，length为16的话，只看后四位即可
8
14
8
2
14
    
//进行异或运算返回的数组下标
log.info("郭德纲：{}", Integer.toBinaryString("郭德纲".hashCode()^("郭德纲".hashCode()>>>16)));                  
log.info("彭于晏：{}", Integer.toBinaryString("彭于晏".hashCode()^("彭于晏".hashCode()>>>16)));                  
log.info("李小龙：{}", Integer.toBinaryString("李小龙".hashCode()^("李小龙".hashCode()>>>16)));                  
log.info("蔡徐鸡：{}", Integer.toBinaryString("蔡徐鸡".hashCode()^("蔡徐鸡".hashCode()>>>16)));                  
log.info("唱跳rap篮球鸡叫：{}", Integer.toBinaryString("唱跳rap篮球鸡叫".hashCode()^("唱跳rap篮球鸡叫".hashCode()>>>16)));

0000001000101100000111011010 0100
0000000101110000001000001101 1110
0000000110001111100100001011 0111
0000000111111111111100001000 1101
0010111010111100101000100100 0010

进行&运算，看下它们返回的数组下标，length为16的话，只看后四位即可
4
14
7
13
2
```

很明显，做了^运算的数组下标更分散

如果还不死心，再来看几个例子

看下 ^、|、&这三个位运算的结果就知道了

```java
log.info("^ 运算：{}", 15 & ("郭德纲".hashCode() ^ ("郭德纲".hashCode() >>> 16)));  
log.info("^ 运算：{}", 15 & ("彭于晏".hashCode() ^ ("彭于晏".hashCode() >>> 16)));  
log.info("^ 运算：{}", 15 & ("李小龙".hashCode() ^ ("李小龙".hashCode() >>> 16)));  
log.info("^ 运算：{}", 15 & ("蔡徐鸡".hashCode() ^ ("蔡徐鸡".hashCode() >>> 16)));  
//^ 运算：4      
//^ 运算：14     
//^ 运算：7      
//^ 运算：13      
                                                                               
log.info("| 运算：{}", 15 & ("郭德纲".hashCode() | ("郭德纲".hashCode() >>> 16)));  
log.info("| 运算：{}", 15 & ("彭于晏".hashCode() | ("彭于晏".hashCode() >>> 16)));  
log.info("| 运算：{}", 15 & ("李小龙".hashCode() | ("李小龙".hashCode() >>> 16)));  
log.info("| 运算：{}", 15 & ("蔡徐鸡".hashCode() | ("蔡徐鸡".hashCode() >>> 16)));  
//| 运算：12     
//| 运算：14     
//| 运算：15     
//| 运算：15  
                                                                                           
log.info("& 运算：{}", 15 & ("郭德纲".hashCode() & ("郭德纲".hashCode() >>> 16)));  
log.info("& 运算：{}", 15 & ("彭于晏".hashCode() & ("彭于晏".hashCode() >>> 16)));  
log.info("& 运算：{}", 15 & ("李小龙".hashCode() & ("李小龙".hashCode() >>> 16)));  
log.info("& 运算：{}", 15 & ("蔡徐鸡".hashCode() & ("蔡徐鸡".hashCode() >>> 16))); 
//& 运算：8      
//& 运算：0      
//& 运算：8      
//& 运算：2   
```

现在看出来了吧，^ 运算的下标分散，具体原理在下文会说

#### 4、为什么要>>>16，>>>15不行吗？

这是无符号右移16位，位数不够，高位补0

现在来说进行 ^ 运算中的玄学，其实>>>16和 ^ 运算是相辅相成的关系，这一套操作是为了保留hash值高16位和低16位的特征，因为数组长度(按默认的16来算)减1后的二进制码低16位永远是1111，我们肯定要尽可能的让1111和hash值产生联系，但是很显然，如果只是1111&hash值的话，1111只会与hash值的低四位产生联系，也就是说这种算法出来的值只保留了hash值低四位的特征，前面还有28位的特征全部丢失了；

因为&运算是都为1才为1，1111我们肯定是改变不了的，只有从hash值入手，所以hashMap作者采用了 key.hashCode() ^ (key.hashCode() >>> 16) 这个巧妙的扰动算法，key的hash值经过无符号右移16位，再与key原来的hash值进行 ^ 运算，就能很好的保留hash值的所有特征，这种离散效果才是我们最想要的。

上面这两段话就是理解>>>16和 ^ 运算的精髓所在，如果没看懂，建议你休息一会儿再回来看，总之记住，目的都是为了让数组下标更分散

再补充一点点，其实并不是非得右移16位，如下面得测试，右移8位右移12位都能起到很好的扰动效果，但是hash值的二进制码是32位，所以最理想的肯定是折半咯，雨露均沾

```java
log.info(">>>16运算：{}", 15 & ("郭德纲".hashCode() ^ ("郭德纲".hashCode() >>> 16)));
log.info(">>>16运算：{}", 15 & ("彭于晏".hashCode() ^ ("彭于晏".hashCode() >>> 16)));
log.info(">>>16运算：{}", 15 & ("李小龙".hashCode() ^ ("李小龙".hashCode() >>> 16)));
log.info(">>>16运算：{}", 15 & ("蔡徐鸡".hashCode() ^ ("蔡徐鸡".hashCode() >>> 16)));
//>>>16运算：4  
//>>>16运算：14 
//>>>16运算：7  
//>>>16运算：13
   
log.info(">>>16运算：{}", 15 & ("郭德纲".hashCode() ^ ("郭德纲".hashCode() >>> 8))); 
log.info(">>>16运算：{}", 15 & ("彭于晏".hashCode() ^ ("彭于晏".hashCode() >>> 8))); 
log.info(">>>16运算：{}", 15 & ("李小龙".hashCode() ^ ("李小龙".hashCode() >>> 8))); 
log.info(">>>16运算：{}", 15 & ("蔡徐鸡".hashCode() ^ ("蔡徐鸡".hashCode() >>> 8))); 
//>>>8运算：7
//>>>8运算：1
//>>>8运算：9
//>>>8运算：3 

log.info(">>>16运算：{}", 15 & ("郭德纲".hashCode() ^ ("郭德纲".hashCode() >>> 12)));
log.info(">>>16运算：{}", 15 & ("彭于晏".hashCode() ^ ("彭于晏".hashCode() >>> 12)));
log.info(">>>16运算：{}", 15 & ("李小龙".hashCode() ^ ("李小龙".hashCode() >>> 12)));
log.info(">>>16运算：{}", 15 & ("蔡徐鸡".hashCode() ^ ("蔡徐鸡".hashCode() >>> 12)));
//>>>12运算：9 
//>>>12运算：12
//>>>12运算：1 
//>>>12运算：13
```



## 函数式编程

### Stream 流式计算

> 什么是Stream流式计算

大数据：存储+计算

集合、MySQL 本质就是用来存储数据的；

计算都应该交给流来操作！

![image-20210120093442133](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210120093442133.png)

#### 示例1

```java
package juc.stream;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @Description: juc.stream
 * @version: 1.0
 */
/*
题目要求：一分钟内完成此题，只能用一行代码实现！
现在有5个用户！筛选：
1、ID必须是偶数
2、年龄必须大于23岁
3、用户名字转为大写字母
4、用户名 字母倒着排序
5、只输出一个用户
 */
public class Test {
    public static void main(String[] args) {
        User u1 = new User(1, "a", 21);
        User u2 = new User(2, "b", 22);
        User u3 = new User(3, "c", 23);
        User u4 = new User(4, "d", 24);
        User u5 = new User(6, "e", 25);
        
        //集合负责存储
        List<User> list = Arrays.asList(u1, u2, u3, u4, u5);
        //Stream流负责计算
        list.stream()
                .filter(user->user.getId()%2==0)
                .filter(user->user.getAge()>23)
                .map(user->user.getName().toUpperCase())
                .sorted(Comparator.reverseOrder())//.sorted((user1,user2)->user2.compareTo(user1))
                .limit(1)
                .forEach(System.out::println);
    }
}
```



#### 示例2

给定一个字符串元素列表，如下所示：

```java
["1", "2", "bilibili", "of", "codesheep", "5", "at", "BILIBILI", "codesheep", "23", "CHEERS", "6"]
```

里面有数字型字符串，有字母型字符串；字符串里有大写，也有小写；字符串长度也有长有短

现在要写代码完成**一个小功能**：

> 我想找出所有 **长度>=5**的字符串，并且**忽略大小写**、**去除重复字符串**，然后**按字母排序**，最后**用“爱心❤”连接成一个字符串**输出！



首先我写一个函数，判断输入字符串到底是字母还是数字

```java
public static Boolean isNum( String str ) {    
    for( int i=0; i<str.length(); i++ ) {        
        if (!Character.isDigit(str.charAt(i))) {            
            return false;        
        }    
    }    
    return true;
}
```

接下来我一顿SAO操作：

```java
// 先定义一个具备按字母排序功能的Set容器，Set本身即可去重
Set<String> stringSet = new TreeSet<String>(new Comparator<String>() {
    @Override
    public int compare(String o1, String o2) {
        return o1.compareTo(o2);  // 按字母顺序排列
    }
});
// 以下for循环完成元素去重、大小写转换、长度判断等操作
for( int i=0; i<list.size(); i++ ) {    
    String s = list.get(i);    
    if( !isNum(s) && s.length()>=5 ) {        
        String sLower = s.toLowerCase(); // 统一转小写 
		stringSet.add( sLower );
    }
}
// 以下for循环完成连词成句
StringBuilder result = new StringBuilder();
for( String s : stringSet ) {    
    result.append( s );    
    result.append("❤"); // 用“爱心”连接符连接
}
String finalResult = result.substring(0,result.length()-1).toString();  // 去掉最后一个多余连接符
System.out.println( finalResult );
```

最后输出结果为：

```
bilibili❤cheers❤codesheep
```

啪啪啪，打脸

我原以为这个功能我只需要3分钟即可写完并运行出结果，而实际对时我发现我居然花了5分钟。。。 

而且我现在是一看到**for循环**遍历，我头就痛，上面代码倒还好，假如列表层级变复杂，**俄罗斯套娃式的for循环** 谁扛得住。

------

 **函数式编程，爽！** 

没错，自Java 8开始，引入了函数式编程范式，这对于咱这种**底层劳动密集型码畜**来说，简直解放了双手，代码几乎少写一半，从此真正实现**编码5分钟，划水2小时**！



针对上面的作业，用Java 8的 `Stream`流式操作，仅需**一行代码**就可以搞定，for循环啥的统统灰飞烟灭。

```java
// 首先将列表转化为Stream流        
// 首先筛选出字母型字符串   
// 其次筛选出长度>=5的字符串   
// 字符串统一转小写   
// 去重操作来一下  
// 字符串排序来一下      
// 连词成句来一下，完美！
String result = list.stream()
    .filter( i -> !isNum(i) )
    .filter( i -> i.length() >= 5 )
    .map( i -> i.toLowerCase() )
    .distinct()
    .sorted( Comparator.naturalOrder() )
    .collect( Collectors.joining("❤") );
System.out.println(result);
```

------

 **言归正传** 

上面其实已经通过举栗的方式阐述了Java 8函数式编程范式：**Stream流** 的优雅和强大，尤其在处理集合时，几本一步到位，嘎嘣脆。

当然**Stream**也仅仅只是Java 8函数式编程接口的一个而已，除了Stream接口，还有其他非常强大的函数式编程接口，比如：

- **`Consumer`接口**
- **`Optional`接口**
- **`Function`接口**





### 函数式接口（必须掌握）

---

需掌握：lambda表达式、链式编程、函数式接口、Stream流式计算

> **函数式接口：**只有一个方法的接口

```java
@FunctionalInterface
public interface Runnable {
    public abstract void run();
}
//泛型、枚举、反射
//lambda表达式、链式编程、函数式接口、Stream流式计算
//超级多的FunctionalInterface
//简化编程模型，在新版本的框架底层大量应用！
//foreach(消费者类型的函数式接口)
```



![image-20210119213252222](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210119213252222.png)



####  一、Function 函数式接口

![image-20210119230143338](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210119230143338.png)

```java
package juc.function;

import java.util.function.Function;

/*
Function 函数型接口，有一个输入参数，有一个输出参数
只要是 函数式接口，就可以用 lambda 表达式进行简化
 */
public class FunctionDemo {
    public static void main(String[] args) {
        //
        /*Function<String, String> function = new Function<String, String>() {
            @Override
            public String apply(String str) {
                return str;
            }
        };*/

        //Function<String,Integer> function = str->str.length();
        Function<String, Integer> function = String::length;
        System.out.println(function.apply("abc"));
        
        //比如我想对一个整数先乘以 `2`，再计算平方值
        Function<Integer,Integer> f1 = i -> i+i;  // 乘以2功能
		Function<Integer,Integer> f2 = i -> i*i;  // 平方功能
		Consumer c = System.out::println;         // 打印功能
		c.accept( f1.andThen(f2).apply(2) );      // 三种功能组合：打印结果 16
    }
}
```



#### 二、Predicate 断定型接口

![image-20210119235354405](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210119235354405.png)

```java
package juc.function;

import java.util.function.Predicate;

/*
Predicate 断定型接口：有一个参数，返回值只能是boolean类型
 */
public class PredicateDemo {
    public static void main(String[] args) {
        //判断是否是空字符串
        /*Predicate<String> predicate = new Predicate<String>() {
            @Override
            public boolean test(String str) {
                return str.isEmpty();
            }
        };*/
        Predicate<String> predicate = str->str.isEmpty();
        //Predicate<String> predicate = String::isEmpty;
        System.out.println(predicate.test("Predicate"));
    }
}
```



####  三、Consumer 消费型接口

![image-20210119233434066](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210119233434066.png)

```java
package juc.function;

import java.util.function.Consumer;

/*
Consumer 消费型接口：只有参数，没有返回值
 */
public class ConsumerDemo {
    public static void main(String[] args) {
        /*Consumer<String> consumer = new Consumer<String>() {
            @Override
            public void accept(String str) {
                System.out.println(str);
            }
        };*/
        Consumer<String> consumer = str -> {
            System.out.println(str);
        };
        consumer.accept("Consumer");
        
        Consumer c = System.out::println;  
		c.accept("hello world");      // 打印 hello world
		c.accept("hello codesheep");  // 打印 hello codesheep
		c.accept("bilibili cheers");  // 打印 bilibili cheers
        c.andThen(c).andThen(c).accept("hello world"); // 会连续打印 3次：hello world
    }
}
```



#### 四、Supplier 供给型接口

![image-20210119234016021](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210119234016021.png)

```java
package juc.function;

import java.util.function.Supplier;

/*
Supplier 供给型接口：没有参数，只有返回值
 */
public class SupplierDemo {
    public static void main(String[] args) {
        /*Supplier<String> supplier = new Supplier<String>() {
            @Override
            public String get() {
                return "Supplier";
            }
        };*/
        Supplier<String> supplier = ()->{return "Supplier";};
        System.out.println(supplier.get());
    }
}
```



#### **五、Optional接口**

`Optional`本质是个容器，你可以将你的变量交由它进行封装，这样我们就不用显式对原变量进行 `null`值检测，防止出现各种空指针异常。举例：

我们想写一个获取学生某个课程考试分数的函数：`getScore()`

```java
public Integer getScore( Student student ) {    
    if( student != null ) {      // 第一层 null判空        
        Subject subject = student.getSubject();        
        if( subject != null ) {  // 第二层 null判空           
            return subject.score;        
        }    
    }    
    return null;
}
```

这样写倒不是不可以，但我们作为一个**“严谨且良心的”**后端工程师，这么多嵌套的 if 判空多少有点扎眼！

为此我们必须引入 `Optional`：

```java
public Integer getScore( Student student ) {    
    return Optional.ofNullable(student)            
        .map( Student::getSubject )            
        .map( Subject::getScore )            
        .orElse(null);
}
```





## 注解Annotation

### 前言

自Java EE框架步入Spring Boot时代之后，注解简直是Java程序员的**命根子**啊，**面向注解编程**成了日常操作！



所以本文来唠一唠关于注解的相关操作，并自己**动手来写一个注解**感受一下原理。原理性的东西掌握了，心里自然就不慌了。

------

### **注解的基本原理**

首先必须要说的是，注解它也不是什么高深的玩意儿，没必要畏惧它！

意如其名，其本来的意思就是用来做**标注**用：可以在**类**、**字段变量**、**方法**、**接口**等位置进行一个**特殊的标记**，为后续做一些诸如：**代码生成**、**数据校验**、**资源整合**等工作做铺垫。

对嘛，就做标记用的嘛！

注解一旦对代码标注完成，后续我们就可以结合Java强大的**反射机制**，在运行时动态地获取到注解的标注信息，从而可以执行很多其他逻辑，完成我们想要的自动化工作。

所以，反射必须要学好！

------

### 手动实现注解

在我的前文《[**听说你还在手写复杂的参数校验？**](http://mp.weixin.qq.com/s?__biz=MzU4ODI1MjA3NQ==&mid=2247484955&idx=1&sn=29b0f228415abd8542d07c898e6398ba&chksm=fdded0dfcaa959c9d9fc9d04130409c392460561bdce53996261fe8973b81f4811a8ebe5c357&scene=21#wechat_redirect)》里曾经讲过， `Spring`自身提供了非常多好用的注解可以用来方便地帮我们做数据校验的工作。

比如，在没有注解加持时，我们想要校验 `Student`类：

```java
public class Student {    
    private Long id;        // 学号    
    private String name;    // 姓名    
    private String mobile;  // 手机号码(11位)
}
```

我们只能通过手写 `if`判断来进行校验：

```java
@PostMapping("/add")
public String addStudent(@RequestBody Student student ) {
    if( student == null )return "传入的Student对象为null，请传值";    
    if( student.getName()==null || "".equals(student.getName()) )        
        return "传入的学生姓名为空，请传值";    
    if( student.getScore()==null )        
        return "传入的学生成绩为null，请传值";    
    if( (student.getScore()<0) || (student.getScore()>100) )        
        return "传入的学生成绩有误，分数应该在0~100之间";    
    if( student.getMobile()==null || "".equals(student.getMobile()) )        
        return "传入的学生电话号码为空，请传值";    
    if( student.getMobile().length()!=11 )        
        return "传入的学生电话号码长度有误，应为11位";
    studentService.addStudent( student );// 将student对象存入MySQL数据库    
    return "SUCCESS";
}
```

这样非常繁琐！

但是借助于 `Spring`提供的注解，数据校验工作可以变得非常优雅，就像这样：

```java
public class Student {
    @NotNull(message = "传入的姓名为null，请传值")    
    @NotEmpty(message = "传入的姓名为空字符串，请传值")    
    private String name;    // 姓名
    @NotNull(message = "传入的分数为null，请传值")    
    @Min(value = 0,message = "传入的学生成绩有误，分数应该在0~100之间")    
    @Max(value = 100,message = "传入的学生成绩有误，分数应该在0~100之间")    
    private Integer score;  // 分数
    @NotNull(message = "传入的电话为null，请传值")    
    @NotEmpty(message = "传入的电话为空字符串，请传值")    
    @Length(min = 11, max = 11, message = "传入的电话号码长度有误，必须为11位")    
    private String mobile;  // 电话号码
}
```

于是很多人就表示疑问，**这些注解到底如何实现功能的呢**？

今天本文则以上文的 `@Length`注解为例，自己动手实现一遍，这个学会了，其他注解实现原理也是类似。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzpmcmIf4VcteY0DT69fvJibEuoLrM6a0p1IP9rkRMMKiaY1Kln3N8kXka5fcd394QoTbf5kdNre6KLg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

总共分三大步实现。



#### 第一步：首先定义注解：@Length

```java
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Length {
    int min();          // 允许字符串长度的最小值
    int max();          // 允许字符串长度的最大值
    String errorMsg();  // 自定义的错误提示信息
}
```

下面做几点说明：

1、注解的定义有点像定义接口 `interface`，但唯一不同的是前面需要加一个 `@`符号

2、注解的成员变量只能使用基本类型、 `String`或者 `enum`枚举，比如 `int`可以，但 `Integer`这种包装类型就不行，需注意

3、像上面 `@Target`、 `@Retention`这种加在注解定义上面的注解，我们称为 **“元注解”**，元注解就是专门用于给注解添加注解的注解，哈哈，很拗口，简单理解，元注解就是天生就有的注解，可直接用于注解的定义上

4、 `@Target(xxx)` 用来说明该自定义注解可以用在什么位置，比如：

- `ElementType.FIELD`：说明自定义的注解可以用于类的变量
- `ElementType.METHOD`：说明自定义的注解可以用于类的方法
- `ElementType.TYPE`：说明自定义的注解可以用于类本身、接口或 `enum`类型
- 等等... 还有很多，如果记不住，建议现用现查

5、 `@Retention(xxx)` 用来说明你自定义注解的生命周期，比如：

- `@Retention(RetentionPolicy.RUNTIME)`：表示注解可以一直保留到运行时，因此可以通过反射获取注解信息
- `@Retention(RetentionPolicy.CLASS)`：表示注解被编译器编译进 `class`文件，但运行时会忽略
- `@Retention(RetentionPolicy.SOURCE)`：表示注解仅在源文件中有效，编译时就会被忽略

所以声明周期从长到短分别为：**RUNTIME** > **CLASS** > **SOURCE** ，一般来说，如果需要在运行时去动态获取注解的信息，还是得用RUNTIME，就像本文所用。



#### 第二步：获取注解并对其进行验证

在运行时想获取注解所代包含的信息，该怎么办？那当然得用 **Java的反射相关知识**！

下面写了一个验证函数 `validate()`，代码中会**逐行用注释去解释**想要达到的目的，认真看一下每一行的注释：

```java
import java.lang.reflect.Field;

//反射校验Length注解
public class ReflectionVali {
	public static String validate( Object object ) throws IllegalAccessException {
    	// 首先通过反射获取object对象的类有哪些字段    
    	// 对本文来说就可以获取到Student类的id、name、mobile三个字段    
    	Field[] fields = object.getClass().getDeclaredFields();
    	// for循环逐个字段校验，看哪个字段上标了注解    
    	for( Field field : fields ) {        
        	// if判断：检查该字段上有没有标注了@Length注解        
        	if( field.isAnnotationPresent(Length.class) ) {            
            	// 通过反射获取到该字段上标注的@Length注解的详细信息            
            	Length length = field.getAnnotation( Length.class );            
            	field.setAccessible( true ); // 让我们在反射时能访问到私有变量            
            	// 用过反射获取字段的实际值            
            	int value =( (String)field.get(object) ).length();            
            	// 将字段的实际值和注解上做标示的值进行比对            
            	if( value<length.min() || value>length.max() ) {                
                	return length.errorMsg();            
            	}        
        	}    
    	}    
    	return null;
	}
}
```

可见，学好Java的反射知识是多么的重要！



#### 第三步：使用注解

这一步比较轻松，使用注解的过程往往都是很愉悦的

```java
package zx.normal.注解.自定义注解.demo01;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
//@AllArgsConstructor
public class Student {
    private String name;// 学号

    private int age;    // 姓名

    @Length(min = 11,max = 11,errorMsg = "电话号码长度应为11位")
    private String mobile;// 手机号码(11位)

    //set时校验注解(也可通过反射实现)
    public void setMobile(String mobile) throws IllegalAccessException {
        this.mobile = mobile;
        // 反射校验注解
        System.out.println(ReflectionVali.validate(this));
    }

    //构造时校验注解(也可通过反射实现)
    public Student(String name, int age, String mobile) throws IllegalAccessException {
        this.name = name;
        this.age = age;
        this.mobile = mobile;
        // 反射校验注解
        System.out.println(ReflectionVali.validate(this));
    }

    public static void main(String[] args) throws IllegalAccessException {
        Student s = new Student("小明", 22, "1234567890");
//        String validate = ReflectionVali.validate(s);
//        System.out.println(validate);
        s.setMobile("12345678901");
    }
}

```

主要就是反射相关的知识！

------



### spring注解扩展

#### **简单业务场景模拟：**

假如你现在在做一个成绩录入系统，你愉快地用**Spring Boot框架**写了一个后台接口，用于接收前台浏览器传过来的 `Student`对象，并插入后台数据库。

我们将传入的 `Student`对象定义为：

```java
public class Student {    
    private String name;    // 姓名    
    private Integer score;  // 考试分数（满分100分）    
    private String mobile;  // 电话号码（11位）
}
```

然后写一个**Post请求**的后台接口，来接收网页端传过来的 `Student`对象：

```java
@RestControllerpublic class TestController {
    @Autowired    
    private StudentService studentService;
    @PostMapping("/add")    
    public String addStudent( @RequestBody Student student ) {        
        studentService.addStudent( student ); // 将student对象存入数据库        
        return "SUCCESS";    
    }
}
```

此时我想你一定看出来了上面这段**代码的漏洞**，因为我们并没有对传入的 `Student`对象做任何**数据校验**，比如：

`Student`对象里三个字段的某一个忘传了，为 `null`怎么办？`Student`的 `score`分数，假如写错了，写成 `101`分怎么办？`Student`的 `mobile`11位手机号码，假如填错了，多写了一位怎么办？...等等

这些数据虽然在前端页面一般会做校验，但我们作为一个**严谨且良心**的后端开发工程师，我们肯定要对传入的每一项数据做**严格的校验**，所以我们应该怎么写？

```java
@PostMapping("/add")
public String addStudent( @RequestBody Student student ) {
    if( student == null )        
        return "传入的Student对象为null，请传值";    
    if( student.getName()==null || "".equals(student.getName()) )        
        return "传入的学生姓名为空，请传值";    
    if( student.getScore()==null )        
        return "传入的学生成绩为null，请传值";    
    if( (student.getScore()<0) || (student.getScore()>100) )        
        return "传入的学生成绩有误，分数应该在0~100之间";    
    if( student.getMobile()==null || "".equals(student.getMobile()) )        
        return "传入的学生电话号码为空，请传值";    
    if( student.getMobile().length()!=11 )        
        return "传入的学生电话号码长度有误，应为11位";
    studentService.addStudent( student ); // 将student对象存入MySQL数据库    
    return "SUCCESS";
}
```

写是写完了，就是感觉**手有点酸**，并且**心有点累**，这个 `Student`对象倒还好，毕竟内部仅3个字段，假如一个复杂的对象有30个字段怎么办？简直不敢想象！

------

 **神注解加持！** 

其实Spring框架很早版本开始，就通过**注解的方式**，来方便地为我们提供了各项交互**数据的校验**工作，比如上面的例子，我们只需要在传入的 `Student`实体类的字段中加入对应注解即可方便的解决问题：

```java
public class Student {
    @NotNull(message = "传入的姓名为null，请传值")    
    @NotEmpty(message = "传入的姓名为空字符串，请传值")    
    private String name;    // 姓名
    @NotNull(message = "传入的分数为null，请传值")    
    @Min(value = 0,message = "传入的学生成绩有误，分数应该在0~100之间")    
    @Max(value = 100,message = "传入的学生成绩有误，分数应该在0~100之间")    
    private Integer score;  // 分数
    @NotNull(message = "传入的电话为null，请传值")    
    @NotEmpty(message = "传入的电话为空字符串，请传值")    
    @Length(min = 11, max = 11, message = "传入的电话号码长度有误，必须为11位")    
    private String mobile;  // 电话号码
}
```

当然，于此同时，我们还需要在对象入口处，加上注解 `@Valid`来开启对传入 `Student`对象的验证工作：

```java
@PostMapping("/add")
public String addStudent( @RequestBody  @Valid Student student ) {
    // 棒棒哒！原先各种繁杂的参数校验工作统统都省了！一行代码不用写
    studentService.addStudent( student ); // 将student对象存入MySQL数据库    
    return "SUCCESS";
}
```

这时候，如果某个字段传入错误，比如我传数据的时候，将学生的成绩误传为 `101`分，则接口返回结果便会提示出错误详情：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzr1wjoePznKKsVVsfolIIFQZ7pEZzt59Ock28dORwTKyWQoK80Iw1XcW6mJicWA1RCpk24Lz88JjGw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



当然，关于这个事情的原理，既然用到了注解，无非用的也就是Java里的各种反射等知识来实现的，感兴趣的小伙伴可以借此机会研究一下！

------

####  **数据异常统一拦截** 

上面利用注解的方式做统一数据校验**感觉十分美好**，但唯一美中不足的就是返回的**结果太过繁杂**，不一定使我们需要的格

为此，我们为项目配置**全局统一异常拦截器**来格式化所有数据校验的返回结果。

```java
@ControllerAdvice@ResponseBodypublic 
class GlobalExceptionInterceptor {
  	@ExceptionHandler(value = Exception.class)  
    public String exceptionHandler(HttpServletRequest request, Exception e) {    
        String failMsg = null;    
        if (e instanceof MethodArgumentNotValidException) {      
            // 拿到参数校验具体异常信息提示      
            failMsg = ((MethodArgumentNotValidException) e).getBindingResult().getFieldError().getDefaultMessage();    
        }    
        return failMsg; // 直接吐回给前端  
    }
}
```

如上面代码所示，我们**全局统一拦截了**参数校验异常 `MethodArgumentNotValidException`，并仅仅只拿到对应异常的详细 `Message`信息吐给前端，此时返回给前端的数据就清楚得多：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzr1wjoePznKKsVVsfolIIFQa38LMfjbZ7oibRduV6jsTYFbib0wBDzvf5JwpCkJibp7O3ekyy90pfv3w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



---

## 时间处理

### LocalDateTime

***重点：***`方法好用，线程安全`



####  **为啥Date遭嫌弃了** 

别的先不说，我们先来看几个关于 `Date`用法的例子，这玩意真的好用吗？

**一、我想新建一个表示"此刻"的日期，打印出来：**

```java
Date rightNow = new Date();
System.out.println( "当前时刻：" + rightNow );
System.out.println( "当前年份：" + rightNow.getYear() );
System.out.println( "当前月份：" + rightNow.getMonth() );
输出结果为：
    // 当前时刻：Fri Dec 13 21:46:34 CST 2019
    // 当前年份：119// 当前月份：11
```

- 第一行：这打印结果你第一眼能看明白？可读性忒差了
- 第二行：今天是2019年，你给我返回个119，没法读
- 第三行：现在是12月份，你给我返回个11，这也没法读

**二、假如我再想构造一个*指定年、月、日*的时间，我尝试这么去做：**

```
Date beforeDate = new Date(2019,12,12);
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzqnuiaMzfNIG9n52WUoMoiagpmVSPTs0qGUVlicCIzxRot4So6Zvk2jZL6vU579jMsicmy1BvYbKrlicVw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

你看到啥了，连构造函数都**被弃用**了！

你可以再仔细瞅瞅，其实 `Date`里的很多方法现在都**已经弃用**了！

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzqnuiaMzfNIG9n52WUoMoiagplTlL3icpVMDAIXKgy4GRxaib92Rq6xWKElCJAlUeibbrcJCTSibia8Iia14g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

都这样了，你项目还敢用这个吗？你醒醒吧！

------

####  **LocalDateTime不香吗？** 

自 `Java8`开始， `JDK`中其实就增加了一系列表示日期和时间的新类，最典型的就是 `LocalDateTime`。直言不讳，这玩意的出现就是为了干掉之前 `JDK`版本中的 `Date`老哥！

同样，我们也先来感受一下用法！

##### **一、获取当前此刻的时间**

```java
LocalDateTime rightNow = LocalDateTime.now();
System.out.println( "当前时刻：" + rightNow );
System.out.println( "当前年份：" + rightNow.getYear() );
System.out.println( "当前月份：" + rightNow.getMonth() );
System.out.println( "当前日份：" + rightNow.getDayOfMonth() );
System.out.println( "当前时：" + rightNow.getHour() );
System.out.println( "当前分：" + rightNow.getMinute() );
System.out.println( "当前秒：" + rightNow.getSecond() );
// 输出结果：
	当前时刻：2019-12-13T22:05:26.779
    当前年份：2019
    当前月份：DECEMBER
    当前日份：13
    当前时：22
    当前分：5
    当前秒：26
```



##### **二、构造一个**指定年、月、日**的时间：**

比如，想构造：`2019年10月12月12日9时21分32秒`

```java
LocalDateTime beforeDate = LocalDateTime.of(2019, Month.DECEMBER, 12, 9, 21, 32);
```



##### **三、修改日期**

```java
LocalDateTime rightNow = LocalDateTime.now(); 
rightNow = rightNow.minusYears( 2 );  // 减少 2 年
rightNow = rightNow.plusMonths( 3 );  // 增加 3 个月
rightNow = rightNow.withYear( 2008 ); // 直接修改年份到2008年
rightNow = rightNow.withHour( 13 );   // 直接修改小时到13时
```



##### **四、格式化日期**

```java
LocalDateTime rightNow = LocalDateTime.now();
String result1 = rightNow.format( DateTimeFormatter.ISO_DATE );
String result2 = rightNow.format( DateTimeFormatter.BASIC_ISO_DATE );
String result3 = rightNow.format( DateTimeFormatter.ofPattern("yyyy/MM/dd") );
System.out.println("格式化后的日期(基本样式一举例)：" + result1);
System.out.println("格式化后的日期(基本样式二举例)：" + result2);
System.out.println("格式化后的日期(自定义样式举例)：" + result3);
// 输出结果：
	格式化后的日期(基本样式一举例)：2019-12-13
    格式化后的日期(基本样式二举例)：20191213
    格式化后的日期(自定义样式举例)：2019/12/13
```

我无话可说，漂亮



##### **五、时间反解析**

给你一个陌生的字符串，你可以按照你需要的格式把时间给反解出来

```java
LocalDateTime time = LocalDateTime.parse("2002--01--02 11:21",DateTimeFormatter.ofPattern("yyyy--MM--dd HH:mm"));
System.out.println("字符串反解析后的时间为：" + time);
// 输出：字符串反解析后的时间为：2002-01-02T11:21
```

tql！

零零散散举了这么些例子，我想 `LocalDateTime`怎么地也不输 `Date`吧！

------

#####  **线程安全性问题！** 

其实上面讲来讲去只讲了两者在用法上的差别，这其实倒还好，并不致命，可是接下来要讨论的**线程安全性问题**才是致命的！

其实以前我们惯用的 `Date`时间类是可变类，这就意味着在多线程环境下对共享 `Date`变量进行操作时，必须**由程序员自己来保证线程安全**！否则极有可能翻车。

而自 `Java8`开始推出的 `LocalDateTime`却是线程安全的，开发人员不用再考虑并发问题，这点我们从 `LocalDateTime`的官方源码中即可看出：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzqnuiaMzfNIG9n52WUoMoiagpbN0g7mjLWP63mbPDibjfzpQO47aLfp2uuibz55viaVpo7PvZXIP4ialuAQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

不说别的，就光一句：

```java
This class is immutable and thread-safe.  （不可变、线程安全！）
```

你就没有任何理由不用 `LocalDateTime`！

------

#####  **日期格式化的选择** 

大家除了惯用 `Date`来表示时间之外，还有一个用于和 `Date`连用的 `SimpleDateFormat` 时间格式化类大家可能也戒不掉了!

`SimpleDateFormat`最主要的致命问题也是在于它本身**并不线程安全**，这在它的源码注释里已然告知过了：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzqnuiaMzfNIG9n52WUoMoiagpPFmj3icSfNRZO93pCOuzicqCartyHEKc0Lo2jn1rbROGuDYX7wWLN2pw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

那取而代之，我们现在改用什么呢？其实在前文已经用到啦，那就是了 `DateTimeFormatter`了，他也是线程安全的：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzqnuiaMzfNIG9n52WUoMoiagpZwT0bKt65rQjjcwdXgATFNJosXSsrHp2ucP30lmC9SERy2Gg97Y1gA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)
