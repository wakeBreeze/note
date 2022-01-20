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

---

## Object类中的方法

### **概  览**

**Object是java所有类的基类，是整个类继承结构的顶端，也是最抽象的一个类**。大家天天都在使用 `toString()、equals()、hashCode()、wait()、notify()、getClass()`等方法，或许都没有意识到是 `Object`的方法，也没有去看 `Object`还有哪些方法以及思考为什么这些方法要放到 `Object`中。本篇就每个方法具体功能、重写规则以及自己的一些理解。

------

### **Object类所有方法详解**

`Object`中含有： `registerNatives()、getClass()、hashCode()、equals()、clone()、toString()、notify()、notifyAll()、wait(long)、wait(long,int)、wait()、finalize()` 共**十二个方法**。这个顺序是按照 `Object`类中定义方法的顺序列举的，下面我也会按照这个顺序依次进行讲解。



#### **registerNatives()**

```java
public class Object {    
    private static native void registerNatives();    
    static {        
        registerNatives();    
    }
}
```

**从名字上理解，这个方法是注册 `native`方法**（本地方法，由 `JVM`实现，底层是 `C/C++`实现的）**向谁注册呢？当然是向 `JVM`**，当有程序调用到 `native`方法时， `JVM`才好去找到这些底层的方法进行调用。

`Object`中的 `native`方法，并使用 `registerNatives()`向 `JVM`进行注册。（这属于 `JNI`的范畴，有兴趣的可自行查阅。）

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzp1opdyycWF8FqUsnz1pdR07HZn67GzuK8MwwAyF5VBXbmOKfqRfQX1TtiaryNQCSpFgWVdvjibpewA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

> 为什么要使用静态方法，还要放到静态块中呢？

我们知道了在类初始化的时候，会依次从父类到本类的类变量及类初始化块中的类变量及方法按照定义顺序放到 `< clinit>`方法中，这样可以保证父类的类变量及方法的初始化一定先于子类。所以当子类调用相应 `native`方法，比如计算 `hashCode`时，一定可以保证能够调用到 `JVM`的 `native`方法。



#### **getClass()**

```java
public final native Class getClass();
```

这是一个 `public`的方法，我们可以直接通过对象调用。

类加载的第一阶段类的加载就是将 `.class`文件加载到内存，并生成一个 `java.lang.Class`对象的过程。 `getClass()`方法就是获取这个对象，这是当前类的对象在运行时类的所有信息的集合。这个方法是反射三种方式之一。

另外两种：

- 类.class
- Class.forName("类完整路径<包名+类名>")



#### **hashCode()**

```java
public native int hashCode(); 
```

这是一个 `public`的方法，所以子类可以重写它。这个方法返回当前对象的 `hashCode`值，这个值是一个整数范围内的 ==（-2^31^~2^31^-1）==数字。

对于 `hashCode`有以下几点约束:

1. 在 Java应用程序执行期间，在对同一对象多次调用 `hashCode` 方法时，必须一致地返回相同的整数，前提是将对象进行 `equals` 比较时所用的信息没有被修改；
2. 如果两个对象 `x.equals(y)` 方法返回 `true`，则 `x`、 `y`这两个对象的 `hashCode`必须相等。
3. 如果两个对象 `x.equals(y)` 方法返回 `false`，则 `x`、 `y`这两个对象的 `hashCode`可以相等也可以不等。但是，为不相等的对象生成不同整数结果可以提高哈希表的性能。
4. 默认的 `hashCode`是将内存地址转换为的 `hash`值，重写过后就是自定义的计算方式；也可以通过 `System.identityHashCode(Object)`来返回原本的 `hashCode`。

```java
public class HashCodeTest {        
    private int age;        
    private String name;    
    @Override        
    public int hashCode() {                
        Object[] a = Stream.of(age, name).toArray();                
        int result = 1;                
        for (Object element : a) {                        
            result = 31 * result + (element == null ? 0 : element.hashCode());                
        }                
        return result;        
    }
}
```

推荐使用 `Objects.hash(Object…values)`方法。相信看源码的时候，都看到计算 `hashCode`都使用了 `31`作为基础乘数，为什么使用 `31`呢？我比较赞同与理解 `result*31=(result<<5)-result`。 `JVM`底层可以自动做优化为位运算，效率很高；还有因为 `31`计算的 `hashCode`冲突较少，利于 `hash`桶位的分布。



#### **equals()**

```java
public boolean equals(Object obj);
```

用于比较当前对象与目标对象是否相等，默认是比较引用是否指向同一对象。为 `public`方法，子类可重写。

```java
public class Object{    
    public boolean equals(Object obj) {        
        return (this == obj);    
    }
}
```

> 为什么需要重写 `equals`方法？

**因为如果不重写equals方法，当将自定义对象放到 `map`或者 `set`中时**；如果这时两个对象的 `hashCode`相同，就会调用 `equals`方法进行比较，这个时候会调用 `Object`中默认的 `equals`方法，而默认的 `equals`方法只是比较了两个对象的引用是否指向了同一个对象，显然大多数时候都不会指向，这样就会将重复对象存入 `map`或者 `set`中。这就**破坏了 `map`与 `set`不能存储重复对象的特性，会造成内存溢出**。

**重写 `equals`方法的几条约定：**

1. **自反性**：即 `x.equals(x)`返回 `true`， `x`不为 `null`；
2. **对称性**：即 `x.equals(y)`与 `y.equals(x）`的结果相同， `x`与 `y`不为 `null`；
3. **传递性**：即 `x.equals(y)`结果为 `true`, `y.equals(z)`结果为 `true`，则 `x.equals(z)`结果也必须为 `true`；
4. **一致性**：即 `x.equals(y)`返回 `true`或 `false`，在未更改 `equals`方法使用的参数条件下，多次调用返回的结果也必须一致。 `x`与 `y`不为 `null`。
5. 如果 `x`不为 `null`, `x.equals(null)`返回 `false`。



#### **clone()**

```java
protected native Object clone() throws CloneNotSupportedException;
```

此方法返回当前对象的一个副本。

这是一个 `protected`方法，提供给子类重写。但需要实现 `Cloneable`接口，这是一个标记接口，如果没有实现，当调用 `object.clone()`方法，会抛出 `CloneNotSupportedException`。

```java
public class CloneTest implements Cloneable {        
    private int age;        
    private String name;        
    //省略get、set、构造函数等    
    @Override        
    protected CloneTest clone() throws CloneNotSupportedException {                
        return (CloneTest) super.clone();        
    }    
    public static void main(String[] args) throws CloneNotSupportedException {                
        CloneTest cloneTest = new CloneTest(23, "XX");                
        CloneTest clone = cloneTest.clone();                
        System.out.println(clone == cloneTest);                
        System.out.println(cloneTest.getAge()==clone.getAge());                
        System.out.println(cloneTest.getName()==clone.getName());        
    }
}
//输出结果
//false
//true
//true
```

从输出我们看见， `clone`的对象是一个新的对象；但原对象与 `clone`对象的 `String`类型的 `name`却是同一个引用，这表明， `super.clone`方法对成员变量如果是引用类型，进行是浅拷贝。

> 那如果我们要进行深拷贝怎么办呢？ 
>
> **答案是**：如果成员变量是引用类型，想实现深拷贝，则成员变量也要实现 `Cloneable`接口，重写 `clone`方法。或者实现序列化接口，进行序列化操作。



#### **toString()**

```java
public String toString()；
```

这是一个 `public`方法，子类可重写，建议所有子类都重写 `toString`方法，默认的 `toString`方法，只是将当前类的全限定性类名 `+@+`十六进制的 `hashCode`值。

**我们思考一下为什么需要toString方法？**

可以这么理解：返回当前对象的字符串表示，可以将其打印方便查看对象的信息，方便记录日志信息提供调试。我们可以选择需要表示的重要信息重写到 `toString`方法中。



#### **wait()/ wait(long)/ wait(long,int)**

这三个方法是用来线程间通信用的，作用是阻塞当前线程，等待其他线程调用 `notify()/notifyAll()`方法将其唤醒。这些方法都是 `publicfinal`的，不可被重写。

**注意：**

1. 此方法只能在当前线程获取到对象的锁监视器之后才能调用，否则会抛出 `IllegalMonitorStateException`异常。
2. 调用 `wait`方法，线程会将锁监视器进行释放；而 `Thread.sleep，Thread.yield()`并不会释放锁。
3. `wait`方法会一直阻塞，直到其他线程调用当前对象的 `notify()/notifyAll()`方法将其唤醒；而 `wait(long)`是等待给定超时时间内（单位毫秒），如果还没有调用 `notify()/nofiyAll()`会自动唤醒； `wait(long,int)`如果第二个参数大于 `0`并且小于 `999999`，则第一个参数 `+1`作为超时时间；



#### **notify()/notifyAll()**

前面说了，如果当前线程获得了当前对象锁，调用 `wait`方法，将锁释放并阻塞；这时另一个线程获取到了此对象锁，并调用此对象的 `notify()/notifyAll()`方法将之前的线程唤醒。这些方法都是 `publicfinal`的，不可被重写。

1. `publicfinalnativevoidnotify();` 随机唤醒之前在当前对象上调用 `wait`方法的一个线程
2. `publicfinalnativevoidnotifyAll()`; 唤醒所有之前在当前对象上调用 `wait`方法的线程

**注意**：调用 `notify()`后，阻塞线程被唤醒，可以参与锁的竞争，但可能调用 `notify()`方法的线程还要继续做其他事，锁并未释放，所以我们看到的结果是，无论 `notify()`是在方法一开始调用，还是最后调用，阻塞线程都要等待当前线程结束才能开始。

> 为什么 `wait()/notify()`方法要放到 `Object`中呢？ 因为每个对象都可以成为锁监视器对象，所以放到 `Object`中，可以直接使用。



#### **finalize()**

```JAVA
protected void finalize() throws Throwable ;
```

此方法是在垃圾回收之前，JVM会调用此方法来清理资源。此方法可能会将对象重新置为可达状态，导致JVM无法进行垃圾回收。

我们知道java相对于C++很大的优势是程序员不用手动管理内存，内存由jvm管理；如果我们的引用对象在堆中没有引用指向他们时，当内存不足时，JVM会自动将这些对象进行回收释放内存，这就是我们常说的垃圾回收。但垃圾回收没有讲述的这么简单。

**`finalize()`方法具有如下4个特点：**

1. 永远不要主动调用某个对象的 `finalize()`方法，该方法由垃圾回收机制自己调用；
2. `finalize()`何时被调用，是否被调用具有不确定性；
3. 当 `JVM`执行可恢复对象的 `finalize()`可能会将此对象重新变为可达状态；
4. 当 `JVM`执行 `finalize()`方法时出现异常，垃圾回收机制不会报告异常，程序继续执行。

### **总  结**

本篇举例讲解了 `Objec`中的所有方法的作用、意义及使用，从 `java`最基础的类出发，感受 `java`设计之美吧。。

---

## IO流

**IO就是输入/输出**。Java IO类库基于抽象基础类InputStream和OutputStream构建了一套**I/O体系**，主要解决从数据源读入数据和将数据写入到目的地问题。`我们把数据源和目的地可以理解为IO流的两端。`当然，通常情况下，这两端可能是文件或者网络连接。

我们用下面的图描述下，加深理解：

> 从一种数据源中通过InputStream流对象读入数据到程序内存中

![图片](https://mmbiz.qpic.cn/mmbiz_png/zG26PepPiafY9Se4sibrElyafaicX7k9RbuaPDHJwybiaxkH7bGwy2IGCicnwej13icRiaibU6KAsOVa28Q4qZPJamib8Jg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)在这里插入图片描述

> 当然我们把上面的图再反向流程，就是OutputStream的示意了。

![图片](https://mmbiz.qpic.cn/mmbiz_png/zG26PepPiafY9Se4sibrElyafaicX7k9RbusVBEiciboOkLMlITMZUYZ4kS5JdbCyicia7ksBL3Ym6CDFueapqleeEcJA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)在这里插入图片描述

其实除了面向字节流的InputStream/OutputStream体系外，Java IO类库还提供了面向字符流的Reader/Writer体系。Reader/Writer继承结构主要是为了国际化，因为它能更好地处理16位的Unicode字符。

在学习是这两套IO流处理体系可以对比参照着学习，因为有好多相似之处。

### 要理解总体设计

刚开始写IO代码，总被各种IO流类搞得晕头转向。这么多IO相关的类，各种方法，啥时候能记住。

其实只要我们掌握了IO类库的总体设计思路，理解了它的层次脉络之后，就很清晰。知道啥时候用哪些流对象去组合想要的功能就好了，API的话，可以查手册的嘛。

首先从流的流向上可以分为输入流InputStream或Reader，输出流OutputStream或Writer。`任何从InputStream或Reader派生而来的类都有read()基本方法，读取单个字节或字节数组；任何从OutputSteam或Writer派生的类都含有write()的基本方法，用于写单个字节或字节数组。`

从操作字节还是操作字符的角度，有面向字节流的类，基本都以XxxStream结尾，面向字符流的类都以XxxReader或XxxWriter结尾。当然这两种类型的流是可以转化的，有两个转化流的类，这个后面会说到。

一般在使用IO流的时候会有下面类似代码：

```java
1 FileInputStream inputStream = new FileInputStream(new File("a.txt"));
2 BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
```

这里其实是一种装饰器模式的使用，`IO流体系中使用了装饰器模式包装了各种功能流类。`

在Java IO流体系中`FilterInputStream/FilterOutStream`和`FilterReader/FilterWriter`就是装饰器模式的接口类，从该类向下包装了一些功能流类。有`DataInputStream、BufferedInputStream、LineNumberInputStream、PushbackInputStream`等，当然还有输出的功能流类；面向字符的功能流类等。

下面几张图描述了整个IO流的继承体系结构

> InputStream流体系

![图片](https://mmbiz.qpic.cn/mmbiz_png/zG26PepPiafY9Se4sibrElyafaicX7k9RbuRyic9LXDaibVeib0PH1LRTMBCuaN7kAgDV7Dzm3M2jxs7lZKo4zn8945g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)在这里插入图片描述

> OutputStream流体系

![图片](https://mmbiz.qpic.cn/mmbiz_png/zG26PepPiafY9Se4sibrElyafaicX7k9Rbu65shQvaePqT2FgIniaUU2mGPHicarV3QptJJkZEKoBeicu6rtiamFJS5Qg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)在这里插入图片描述

> Reader体系

![图片](https://mmbiz.qpic.cn/mmbiz_png/zG26PepPiafY9Se4sibrElyafaicX7k9RbumG0YNSG8YWh8VDd5ljHLgRUM24bO0h3J4NBXZpXrxs8ibibaERuMxIoA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)在这里插入图片描述

> Writer体系

![图片](https://mmbiz.qpic.cn/mmbiz_png/zG26PepPiafY9Se4sibrElyafaicX7k9RbuY9liaZR2Duj28ppY1Vb9NjWrxxodSNX3w3l9KicOrRjibOPsnwicAgKYibg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)在这里插入图片描述



在这里插入图片描述

### File其实是个工具类

File类其实不止是代表一个文件，它也能代表一个目录下的一组文件（代表一个文件路径）。下面我们盘点一下File类中最常用到的一些方法

```java
File.delete() 删除文件或文件夹目录。
File.createNewFile() 创建一个新的空文件。
File.mkdir() 创建一个新的空文件夹。
File.list() 获取指定目录下的文件和文件夹名称。
File.listFiles() 获取指定目录下的文件和文件夹对象。
File.exists() 文件或者文件夹是否存在

String   getAbsolutePath()   // 获取绝对路径
long   getFreeSpace()       // 返回分区中未分配的字节数。
String   getName()         // 返回文件或文件夹的名称。
String   getParent()         // 返回父目录的路径名字符串；如果没有指定父目录，则返回 null。
File   getParentFile()      // 返回父目录File对象
String   getPath()         // 返回路径名字符串。
long   getTotalSpace()      // 返回此文件分区大小。
long   getUsableSpace()    //返回占用字节数。
int   hashCode()             //文件哈希码。
long   lastModified()       // 返回文件最后一次被修改的时间。
long   length()          // 获取长度,字节数。
boolean canRead()  //判断是否可读
boolean canWrite()  //判断是否可写
boolean isHidden()  //判断是否隐藏


// 成员函数
static File[]    listRoots()    // 列出可用的文件系统根。
boolean    renameTo(File dest)    // 重命名
boolean    setExecutable(boolean executable)    // 设置执行权限。
boolean    setExecutable(boolean executable, boolean ownerOnly)    // 设置其他所有用户的执行权限。
boolean    setLastModified(long time)       // 设置最后一次修改时间。
boolean    setReadable(boolean readable)    // 设置读权限。
boolean    setReadable(boolean readable, boolean ownerOnly)    // 设置其他所有用户的读权限。
boolean    setWritable(boolean writable)    // 设置写权限。
boolean    setWritable(boolean writable, boolean ownerOnly)    // 设置所有用户的写权限。
```

需要注意的是，不同系统对文件路径的分割符表是不一样的，比如Windows中是“\”，Linux是“/”。`而File类给我们提供了抽象的表示File.separator，屏蔽了系统层的差异`。因此平时在代码中不要使用诸如“\”这种代表路径，可能造成Linux平台下代码执行错误。

下面是一些示例：

**根据传入的规则，遍历得到目录中所有的文件构成的File对象数组**

```java
 1public class Directory {
 2    public static File[] getLocalFiles(File dir, final String regex){
 3        return dir.listFiles(new FilenameFilter() {
 4            private Pattern pattern = Pattern.compile(regex);
 5            public boolean accept(File dir, String name) {
 6                return pattern.matcher(new File(name).getName()).matches();
 7            }
 8        });
 9    }
10
11    // 重载方法
12    public static File[] getLocalFiles(String path, final String regex){
13        return getLocalFiles(new File(path),regex);
14    }
15
16    public static void main(String[] args) {
17        String dir = "d:";
18        File[] files = Directory.getLocalFiles(dir,".*\\.txt");
19        for(File file : files){
20            System.out.println(file.getAbsolutePath());
21        }
22    }
23}
```

输出结果：

```java
d:\\1.txt
d:\\新建文本文档.txt
```

上面的代码中**dir.listFiles(FilenameFilter )** 是策略模式的一种实现，而且使用了匿名内部类的方式。

> 上面的例子是《Java 编程思想》中的示例，这本书中的每个代码示例都很经典，Bruce Eckel大神把面向对象的思想应用的炉火纯青，非常值得细品。

### InputStream和OutputStream

`InputStream是输入流，前面已经说到，它是从数据源对象将数据读入程序内容时，使用的流对象。`通过看InputStream的源码知道，它是一个抽象类，

```java
public abstract class InputStream  extends Object  implements Closeable
```

提供了一些基础的输入流方法：

```java
 1//从数据中读入一个字节，并返回该字节，遇到流的结尾时返回-1
 2abstract int read() 
 3
 4//读入一个字节数组，并返回实际读入的字节数，最多读入b.length个字节，遇到流结尾时返回-1
 5int read(byte[] b)
 6
 7// 读入一个字节数组，返回实际读入的字节数或者在碰到结尾时返回-1.
 8//b:代表数据读入的数组， off：代表第一个读入的字节应该被放置的位置在b中的偏移量，len：读入字节的最大数量
 9int read(byte[],int off,int len)
10
11// 返回当前可以读入的字节数量，如果是从网络连接中读入，这个方法要慎用，
12int available() 
13
14//在输入流中跳过n个字节，返回实际跳过的字节数
15long skip(long n)
16
17//标记输入流中当前的位置
18void mark(int readlimit) 
19
20//判断流是否支持打标记，支持返回true
21boolean markSupported() 
22
23// 返回最后一个标记，随后对read的调用将重新读入这些字节。
24void reset() 
25
26//关闭输入流，这个很重要，流使用完一定要关闭
27void close()
28
```

直接从InputStream继承的流，可以发现，基本上对应了每种数据源类型。

| 类                      | 功能                                                 |
| :---------------------- | :--------------------------------------------------- |
| ByteArrayInputStream    | 将字节数组作为InputStream                            |
| StringBufferInputStream | 将String转成InputStream                              |
| FileInputStream         | 从文件中读取内容                                     |
| PipedInputStream        | 产生用于写入相关PipedOutputStream的数据。实现管道化  |
| SequenceInputStream     | 将两个或多个InputStream对象转换成单一的InputStream   |
| FilterInputStream       | 抽象类，主要是作为“装饰器”的接口类，实现其他的功能流 |

`OutputStream`是输出流的抽象，它是将程序内存中的数据写入到目的地（也就是接收数据的一端）。看下类的签名：

```
1public abstract class OutputStream implements Closeable, Flushable {}
```

提供了基础方法相比输入流来说简单多了，主要就是write写方法（几种重载的方法）、flush冲刷和close关闭。

```java
 1// 写出一个字节的数据
 2abstract void write(int n)
 3
 4// 写出字节到数据b
 5void write(byte[] b)
 6
 7// 写出字节到数组b，off：代表第一个写出字节在b中的偏移量，len：写出字节的最大数量
 8void write(byte[] b, int off, int len)
 9
10//冲刷输出流，也就是将所有缓冲的数据发送到目的地
11void flush()
12
13// 关闭输出流
14void close()
15
```

同样地，`OutputStream`也提供了一些基础流的实现，这些实现也可以和特定的目的地（接收端）对应起来，比如输出到字节数组或者是输出到文件/管道等。

| 类                    | 功能                                                       |
| :-------------------- | :--------------------------------------------------------- |
| ByteArrayOutputStream | 在内存中创建一个缓冲区，所有送往“流”的数据都要放在此缓冲区 |
| FileOutputStream      | 将数据写入文件                                             |
| PipedOutputStream     | 和PipedInputStream配合使用。实现管道化                     |
| FilterOutputStream    | 抽象类，主要是作为“装饰器”的接口类，实现其他的功能流       |

### 使用装饰器包装有用的流

Java IO 流体系使用了装饰器模式来给哪些基础的输入/输出流添加额外的功能。这写额外的功能可能是：可以将流缓冲起来提高性能、是流能够读写基本数据类型等。

`这些通过装饰器模式添加功能的流类型都是从FilterInputStream和FilterOutputStream抽象类扩展而来的。`可以再返回文章最开始说到IO流体系的层次时，那几种图加深下印象。

> FilterInputStream类型

| 类                    | 功能                                                         |
| :-------------------- | :----------------------------------------------------------- |
| DataInputStream       | 和DataOutputStream搭配使用，使得流可以读取int char long等基本数据类型 |
| BufferedInputStream   | 使用缓冲区，主要是提高性能                                   |
| LineNumberInputStream | 跟踪输入流中的行号，可以使用getLineNumber、setLineNumber(int) |
| PushbackInputStream   | 使得流能弹出“一个字节的缓冲区”，可以将读到的最后一个字符回退 |

> FilterOutStream类型

| 类                   | 功能                                                         |
| :------------------- | :----------------------------------------------------------- |
| DataOutputStream     | 和DataInputStream搭配使用，使得流可以写入int char long等基本数据类型 |
| PrintStream          | 用于产生格式化的输出                                         |
| BufferedOutputStream | 使用缓冲区，可以调用flush()清空缓冲区                        |

大多数情况下，其实我们在使用流的时候都是输入流和输出流搭配使用的。目的就是为了转移和存储数据，单独的read()对我们而言有啥用呢，读出来一个字节能干啥？对吧。`因此要理解流的使用就是搭配起来或者使用功能流组合起来去转移或者存储数据。`

### Reader和Writer

`Reader`是Java IO中所有Reader的基类。`Reader`与`InputStream`类似，不同点在于，Reader基于字符而非基于字节。

`Writer`是Java IO中所有Writer的基类。与`Reader`和`InputStream`的关系类似，Writer基于字符而非基于字节，Writer用于写入文本，`OutputStream`用于写入字节。

`Reader`和`Writer`的基础功能类，可以对比`InputStream`、`OutputStream`来学习。

| 面向字节                          | 面向字符        |
| :-------------------------------- | :-------------- |
| InputStream                       | Reader          |
| OutputStream                      | Writer          |
| FileInputStream                   | FileReader      |
| FileOutputStream                  | FileWriter      |
| ByteArrayInputStream              | CharArrayReader |
| ByteArrayOutputStream             | CharArrayWriter |
| PipedInputStream                  | PipedReader     |
| PipedOutputStream                 | PipedWriter     |
| StringBufferInputStream（已弃用） | StringReader    |
| 无对应类                          | StringWriter    |

有两个“适配器” 流类型，它们可以将字节流转化成字符流。`这就是InputStreamReader 可以将InputStream转成为Reader，OutputStreamWriter可以将OutputStream转成为Writer。`

> 适配器类，字节流转字符流

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)在这里插入图片描述

当然也有类似字节流的装饰器实现方式，给字符流添加额外的功能或者说是行为。这些功能字符流类主要有：

- BufferedReader
- BufferedWriter
- PrintWriter
- LineNumberReader
- PushbackReader

### System类中的I/O流

想想你的第一个Java程序是啥？我没猜错的话，应该是 hello world。

```java
System.out.println("hello world")
```

简单到令人发指，今天就说说标准的输入/输出流。

在标准IO模型中，Java提供了`System.in、System.out和System.error。`

**先说`System.in`，看下源码**

```java
public final static InputStream in
```

是一个静态域，未被包装过的`InputStream`。通常我们会使用`BufferedReader`进行包装然后一行一行地读取输入，这里就要用到前面说的适配器流`InputStreamReader`。

```java
1public class SystemInReader {
2    public static void main(String[] args) throws IOException {
3        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
4        String s;
5        while ((s = reader.readLine()) != null && s.length() != 0){
6            System.out.println(s);
7        }
8    }
9}
```

该程序等待会一直等待我们输入，输入啥，后面会接着输出。输入空字符串可以结束。

```java
11
21
3123
4123
```

`System.out是一个PrintStream流。`System.out一般会把你写到其中的数据输出到控制台上。System.out通常仅用在类似命令行工具的控制台程序上。System.out也经常用于打印程序的调试信息(尽管它可能并不是获取程序调试信息的最佳方式)。

`System.err是一个PrintStream流。`System.err与System.out的运行方式类似，但它更多的是用于打印错误文本。

**可以将这些系统流重定向**

尽管System.in, System.out, System.err这3个流是java.lang.System类中的静态成员，并且已经预先在JVM启动的时候初始化完成，你依然可以更改它们。

`可以使用setIn(InputStream)、setOut(PrintStream)、setErr(PrintStream)进行重定向。`比如可以将控制台的输出重定向到文件中。

```java
1OutputStream output = new FileOutputStream("d:/system.out.txt");
2PrintStream printOut = new PrintStream(output);
3System.setOut(printOut);
```

### 压缩（ZIP文档）

Java IO类库是支持读写压缩格式的数据流的。我们可以把一个或一批文件压缩成一个zip文档。`这些压缩相关的流类是按字节处理的。`先看下设计压缩解压缩的相关流类。

| 压缩类               | 功能                                                         |
| :------------------- | :----------------------------------------------------------- |
| CheckedInputStream   | getCheckSum()可以为任何InputStream产生校验和（不仅是解压缩） |
| CheckedOutputStream  | getCheckSum()可以为任何OutputStream产生校验和（不仅是压缩）  |
| DeflaterOutputStream | 压缩类的基类                                                 |
| ZipOutputStream      | 继承自DeflaterOutputStream，将数据压缩成Zip文件格式          |
| GZIPOutputStream     | 继承自DeflaterOutputStream，将数据压缩成GZIP文件格式         |
| InflaterInputStream  | 解压缩类的基类                                               |
| ZipInputStream       | 继承自InflaterInputStream，解压缩Zip文件格式的数据           |
| GZIPInputStream      | 继承自InflaterInputStream，解压缩GZIP文件格式的数据          |

表格中CheckedInputStream  和 CheckedOutputStream  一般会和Zip压缩解压过程配合使用，主要是为了保证我们压缩和解压过程数据包的正确性，得到的是中间没有被篡改过的数据。

我们以CheckedInputStream  为例，它的构造器需要传入一个Checksum类型：

```java
public CheckedInputStream(InputStream in, Checksum cksum) {
        super(in);
        this.cksum = cksum;
}
```

而Checksum 是一个接口，`可以看到这里又用到了策略模式`，具体的校验算法是可以选择的。`Java类库给我提供了两种校验和算法：Adler32 和 CRC32`，性能方面可能Adler32 会更好一些，不过CRC32可能更准确。各有优劣吧。

好了，接下来看下压缩/解压缩流的具体使用。

**将多个文件压缩成zip包**

```java
 1public class ZipFileUtils {
 2    public static void compressFiles(File[] files, String zipPath) throws IOException {
 3
 4        // 定义文件输出流，表明是要压缩成zip文件的
 5        FileOutputStream f = new FileOutputStream(zipPath);
 6
 7        // 给输出流增加校验功能
 8        CheckedOutputStream checkedOs = new CheckedOutputStream(f,new Adler32());
 9
10        // 定义zip格式的输出流，这里要明白一直在使用装饰器模式在给流添加功能
11        // ZipOutputStream 也是从FilterOutputStream 继承下来的
12        ZipOutputStream zipOut = new ZipOutputStream(checkedOs);
13
14        // 增加缓冲功能，提高性能
15        BufferedOutputStream buffOut = new BufferedOutputStream(zipOut);
16
17        //对于压缩输出流我们可以设置个注释
18        zipOut.setComment("zip test");
19
20        // 下面就是从Files[] 数组中读入一批文件，然后写入zip包的过程
21        for (File file : files){
22
23            // 建立读取文件的缓冲流，同样是装饰器模式使用BufferedReader
24            // 包装了FileReader
25            BufferedReader bfReadr = new BufferedReader(new FileReader(file));
26
27            // 一个文件对象在zip流中用一个ZipEntry表示，使用putNextEntry添加到zip流中
28            zipOut.putNextEntry(new ZipEntry(file.getName()));
29
30            int c;
31            while ((c = bfReadr.read()) != -1){
32                buffOut.write(c);
33            }
34
35            // 注意这里要关闭
36            bfReadr.close();
37            buffOut.flush();
38        }
39        buffOut.close();
40    }
41
42    public static void main(String[] args) throws IOException {
43        String dir = "d:";
44        String zipPath = "d:/test.zip";
45        File[] files = Directory.getLocalFiles(dir,".*\\.txt");
46        ZipFileUtils.compressFiles(files, zipPath);
47    }
48}
```

在main函数中我们使用了本文中 **File其实是个工具类** 章节里的Directory工具类。

**解压缩zip包到目标文件夹**

```java
 1    public static void unConpressZip(String zipPath, String destPath) throws IOException {
 2        if(!destPath.endsWith(File.separator)){
 3            destPath = destPath + File.separator;
 4            File file = new File(destPath);
 5            if(!file.exists()){
 6                file.mkdirs();
 7            }
 8        }
 9        // 新建文件输入流类，
10        FileInputStream fis = new FileInputStream(zipPath);
11
12        // 给输入流增加检验功能
13        CheckedInputStream checkedIns = new CheckedInputStream(fis,new Adler32());
14
15        // 新建zip输出流，因为读取的zip格式的文件嘛
16        ZipInputStream zipIn = new ZipInputStream(checkedIns);
17
18        // 增加缓冲流功能，提高性能
19        BufferedInputStream buffIn = new BufferedInputStream(zipIn);
20
21        // 从zip输入流中读入每个ZipEntry对象
22        ZipEntry zipEntry;
23        while ((zipEntry = zipIn.getNextEntry()) != null){
24            System.out.println("解压中" + zipEntry);
25
26            // 将解压的文件写入到目标文件夹下
27            int size;
28            byte[] buffer = new byte[1024];
29            FileOutputStream fos = new FileOutputStream(destPath + zipEntry.getName());
30            BufferedOutputStream bos = new BufferedOutputStream(fos, buffer.length);
31            while ((size = buffIn.read(buffer, 0, buffer.length)) != -1) {
32                bos.write(buffer, 0, size);
33            }
34            bos.flush();
35            bos.close();
36        }
37        buffIn.close();
38
39        // 输出校验和
40        System.out.println("校验和：" + checkedIns.getChecksum().getValue());
41    }
42
43    // 在main函数中直接调用
44    public static void main(String[] args) throws IOException {
45        String dir = "d:";
46        String zipPath = "d:/test.zip";
47//        File[] files = Directory.getLocalFiles(dir,".*\\.txt");
48//        ZipFileUtils.compressFiles(files, zipPath);
49
50        ZipFileUtils.unConpressZip(zipPath,"F:/ziptest");
51    }
```

这里解压zip包还有一种更加简便的方法，`使用ZipFile对象`。该对象的entries()方法直接返回ZipEntry类型的枚举。看下代码片段：

```java
1        ZipFile zipFile = new ZipFile("test.zip");
2        Enumeration e = zipFile.entries();
3        while (e.hasMoreElements()){
4            ZipEntry zipEntry = (ZipEntry) e.nextElement();
5            System.out.println("file:" + zipEntry);
6        }
```

### 对象序列化

**什么是序列化和反序列化呢？**

序列化就是将对象转成字节序列的过程，反序列化就是将字节序列重组成对象的过程。

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)在这里插入图片描述

**为什么要有对象序列化机制**

程序中的对象，其实是存在有内存中，当我们JVM关闭时，无论如何它都不会继续存在了。那有没有一种机制能让对象具有“持久性”呢？序列化机制提供了一种方法，你可以将对象序列化的字节流输入到文件保存在磁盘上。

序列化机制的另外一种意义便是我们可以通过网络传输对象了，Java中的 `远程方法调用（RMI）`，底层就需要序列化机制的保证。

**在Java中怎么实现序列化和反序列化**

首先要序列化的对象必须实现一个`Serializable接口（这是一个标识接口，不包括任何方法）`

```java
1public interface Serializable {
2}
```

其次需要是用两个对象流类：`ObjectInputStream 和ObjectOutputStream`。`主要使用ObjectInputStream对象的readObject方法读入对象、ObjectOutputStream的writeObject方法写入对象到流中`

下面我们通过序列化机制将一个简单的pojo对象写入到文件，并再次读入到程序内存。

```java
 1public class User implements Serializable {
 2    private String name;
 3    private int age;
 4
 5    public User(String name, int age) {
 6        this.name = name;
 7        this.age = age;
 8    }
 9
10    @Override
11    public String toString() {
12        return "User{" +
13                "name='" + name + '\'' +
14                ", age='" + age + '\'' +
15                '}';
16    }
17
18    public static void main(String[] args) throws IOException, ClassNotFoundException {
19        User user = new User("二营长",18);
20        ObjectOutputStream objectOps = new ObjectOutputStream(new FileOutputStream("f:/user.out"));
21        objectOps.writeObject(user);
22        objectOps.close();
23
24        // 再从文件中取出对象
25        ObjectInputStream objectIns = new ObjectInputStream(new FileInputStream("f:/user.out"));
26
27        // 这里要做一次强转
28        User user1 = (User) objectIns.readObject();
29        System.out.println(user1);
30        objectIns.close();
31    }
32}
33
```

程序运行结果：

```java
User{name='二营长', age='18'}
```

**不想序列化的数据使用transient（瞬时）关键字屏蔽**

如果我们上面的user对象有一个password字段，属于敏感信息，这种是不能走序列化的方式的，但是实现了Serializable 接口的对象会自动序列化所有的数据域，怎么办呢？在password字段上加上关键字transient就好了。

```java
1 private transient String password;
```

序列化机制就简单介绍到这里吧。这是Java原生的序列化，现在市面上有好多序列化协议可以选择，比如`Json、FastJson、Thrift、Hessian 、protobuf等`。

### I/O流的典型使用方式

IO流种类繁多，可以通过不同的方式组合I/O流类，但平时我们常用的也就几种组合。下盘通过示例的方式盘点几种I/O流的典型用法。

**缓冲输入文件**

```java
 1public class BufferedInutFile {
 2    public static String readFile(String fileName) throws IOException {
 3        BufferedReader bf = new BufferedReader(new FileReader(fileName));
 4        String s;
 5
 6        // 这里读取的内容存在了StringBuilder，当然也可以做其他处理
 7        StringBuilder sb = new StringBuilder();
 8        while ((s = bf.readLine()) != null){
 9            sb.append(s + "\n");
10        }
11        bf.close();
12        return sb.toString();
13    }
14
15    public static void main(String[] args) throws IOException {
16        System.out.println(BufferedInutFile.readFile("d:/1.txt"));
17    }
18}
```

**格式化内存输入**

要读取格式化的数据，可以使用DataInputStream。

```java
 1public class FormattedMemoryInput {
 2    public static void main(String[] args) throws IOException {
 3        try {
 4            DataInputStream dataIns = new DataInputStream(
 5                    new ByteArrayInputStream(BufferedInutFile.readFile("f:/FormattedMemoryInput.java").getBytes()));
 6            while (true){
 7                System.out.print((char) dataIns.readByte());
 8            }
 9        } catch (EOFException e) {
10            System.err.println("End of stream");
11        }
12    }
13}
```

上面程序会在控制台输出当前类本身的所有代码，并且会抛出一个`EOFException异常`。抛出异常的原因是已经到留的结尾了还在读数据。这里可以使用available()做判断还有多少可以的字符。

```java
 1package com.herp.pattern.strategy;
 2
 3import java.io.ByteArrayInputStream;
 4import java.io.DataInputStream;
 5import java.io.IOException;
 6
 7public class FormattedMemoryInput {
 8    public static void main(String[] args) throws IOException {
 9        DataInputStream dataIns = new DataInputStream(
10                new ByteArrayInputStream(BufferedInutFile.readFile("FormattedMemoryInput.java").getBytes()));
11        while (true){
    		  if (dis.available() == 0){
                    break;
                }
12            System.out.println((char) dataIns.readByte());
13        }
14    }
15}
```

**基本的文件输出**

`FileWriter`对象可以向文件写入数据。首先创建一个FileWriter和指定的文件关联，然后使用`BufferedWriter`将其包装提供缓冲功能，为了提供格式化机制，它又被装饰成为`PrintWriter`。

```java
 1public class BasicFileOutput {
 2    static String file = "BasicFileOutput.out";
 3
 4    public static void main(String[] args) throws IOException {
 5        BufferedReader in = new BufferedReader(new StringReader(BufferedInutFile.readFile("f:/BasicFileOutput.java")));
 6        PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
 7
 8        int lineCount = 1;
 9        String s;
10        while ((s = in.readLine()) != null){
11            out.println(lineCount ++ + ": " + s);
12        }
13        out.close();
14        in.close();
15    }
16}
```

下面是我们写出的`BasicFileOutput.out文件`，可以看到我们通过代码字节加上了行号

```java
 package com.herp.pattern.strategy;
 
 import java.io.*;
 
 public class BasicFileOutput {
     static String file = "BasicFileOutput.out";
 
 public static void main(String[] args) throws IOException {
         BufferedReader in = new BufferedReader(new StringReader(BufferedInutFile.readFile("f:/BasicFileOutput")));
         PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
 
         int lineCount = 1;
         String s;
         while ((s = in.readLine()) != null){
             out.println(lineCount ++ + ": " + s);
         }
         out.close();
         in.close();
     }
 }
```

**数据的存储和恢复**

为了输出可供另一个“流”恢复的数据，我们需要使用`DataOutputStream`写入数据，然后使用DataInputStream恢复数据。当然这些流可以是任何形式（这里的形式其实就是我们前面说过的流的两端的类型），比如文件。

```java
 1public class StoringAndRecoveringData {
 2    public static void main(String[] args) throws IOException {
 3        DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("data.txt")));
 4        out.writeDouble(3.1415926);
 5        out.writeUTF("三连走起");
 6        out.writeInt(125);
 7        out.writeUTF("点赞加关注");
 8        out.close();
 9
10        DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream("data.txt")));
11        System.out.println(in.readDouble());
12        System.out.println(in.readUTF());
13        System.out.println(in.readInt());
14        System.out.println(in.readUTF());
15        in.close();
16    }
17}
```

输出结果：

```java
3.1415926
三连走起
125
点赞加关注
```

需要注意的是我们使用`writeUTF()和readUTF()来写入和读取字符串。`



---

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



---

## String

### intern()

[链接](https://www.cnblogs.com/wa1l-E/p/14216386.html)

首先先说一下结论，后面会实际操作，验证一下结论。intern方法在不同的Java版本中的实现是不一样的。Java6之前是一种实现，Java6之后也就是Java7和Java8是另外一种实现。

先说一下intern方法的定义 在Java的String类中是这样定义的，是一个本地方法，其中源码由C实现

```java
public native String intern();
```

再来看一下源码的注释描述：

```java
* <p>
* When the intern method is invoked, if the pool already contains a
* string equal to this {@code String} object as determined by
* the {@link #equals(Object)} method, then the string from the pool is
* returned. Otherwise, this {@code String} object is added to the
* pool and a reference to this {@code String} object is returned.
* <p>
```

翻译过来的意思就是：如果常量池中已经有了此字符串，那么将常量池中该字符串的引用返回，如果没有，那么将该字符串对象添加到常量池中，并且将引用返回。

首先要明白，这里注释的该字符串是调用此方法的字符串，返回的是引用。

#### **Java6版本：**

intern方法作用：确实如上述注释上所描述，如果常量池中没有字符串，则将该字符串对象加入常量池，并返回引用。

** 这里需要注意：Java6中常量池是在方法区中，而Java1.6版本hotspot采用永久带实现了方法区，永久代是和Java堆区分的，即就是常量池中没有字符串，那么将该字符串对象放入永久带的常量池中，并返回其引用。

 

#### **Java7和Java8版本：**

intern方法作用：和注释描述的并不同，

- 如果常量池有，那么返回该字符串的引用。
- 如果常量池没有，那么如果是”a“.intern调用，那么就会把”a“放入常量池，并返回”a“在常量池中的引用。
- 如果是new String("a").internal ，其中在 new String的时候上文已经说到过，会在堆和常量池各创建一个对象，那么这里返回的就是常量池的字符串a的引用。
- **如果是new StringBuilder("a").internal，其中new StringBuilder会在堆中创建一个对象，常量池没有，这里调用intern方法后，会将堆中字串a的引用放到常量池，==注意这里始终只是创建了一个对象==，**返回的引用虽然是常量池的，但是常量池的引用是指向堆中字串a的引用的。

上述粗体字描述部分也就是不同之处，也是intern方法在Java6 之后的使用变化如何体现。



再简单总结一下Java7和Java8的intern方法作用：

​	如果常量池没有，那么会将堆中的字符串的引用放到常量池，注意是引用，然后返回该引用。



为什么Java7和Java8会不一样呢:

​	原因就是 Java7之后（部分虚拟机，Hotspot,JRockit）已经将永久代的常量池、静态变量移出，放入了Java堆中，而永久代也在Java8中完全废弃，方法区改名为元空间。既然常量池已经在Java6之后放入了堆中，那么如果堆中已经创建过此字符串的对象了，那么就没有必要在常量池中再创建一个一模一样的对象了，直接将其引用拷贝返回就好了，因为都是处于同一个区域Java堆中。

#### 示例：

1、下图 在new的时候已经创建了两个对象，第二行，只是获取的第一行创建的常量池的对象的引用，实际的对象已经创建过了。这里是两个不同的对象，返回false。

![img](https://img2020.cnblogs.com/blog/1920418/202012/1920418-20201231144734538-1228256307.png)

2、和上述一样，只不过这一次第一行，现在常量池创建了对象，第二行发现常量池已经有了，只在堆上创建了一次对象，但仍然是两个对象，引用不同，返回false。

![img](https://img2020.cnblogs.com/blog/1920418/202012/1920418-20201231144749064-1459046057.png)

3、第一行，Strignbuilder只会在堆中创建一个对象，第二行调用intern方法后，会将堆中的引用放到到常量池中。第三行发现常量池中已经有这个字符串的引用了，直接返回。因此是同一个引用，返回的都是第一次创建的堆中字串的引用

![img](https://img2020.cnblogs.com/blog/1920418/202012/1920418-20201231144754831-455147509.png)

4、和上述3的不同之处在于没有调用intern方法，因此结果输出不一样。

![img](https://img2020.cnblogs.com/blog/1920418/202012/1920418-20201231145625426-1045553162.png)

 

5、new String之后使用 + 在Java中会进行编译优化，编译成字节码指令后，会将+ 优化成 先new Stringbuilder对象，然后调用append方法进行拼接。

如下图：

![img](https://img2020.cnblogs.com/blog/1920418/202012/1920418-20201231150303494-1281777904.png)

反编译生成字节码：

![img](https://img2020.cnblogs.com/blog/1920418/202012/1920418-20201231150458160-1595987966.png)

因此这里s1最终创建的时候，xyzz字符串并没有在常量池创建，只是在堆中创建了，因为就如同上面的3一样，是new Stringbuilder操作。

所以在调用intern操作后，将其堆中的引用放入常量池并返回。所以后面的结果都是true，因为至始至终都是堆中的一个对象。

![img](https://img2020.cnblogs.com/blog/1920418/202012/1920418-20201231144801824-1808103622.png)

6、和上述5是相反的，结果输出也不同。

![img](https://img2020.cnblogs.com/blog/1920418/202012/1920418-20201231144805739-849823258.png)

#### 总结：

 在Java6之后，使用intern可以起到优化的作用，但也要看具体的情况，比如在使用加号做字符拼接的时候，如果不想在因为其他的操作在常量池中重新创建相同的对象，那么调用intern方法，在常量池中只会放入一个引用，这时候只创建了一个对象。

---

### String使用技巧

String 类型是我们使用最频繁的数据类型，没有之一。那么提高 String 的运行效率，无疑是提升程序性能的最佳手段。

我们本文将从 String 的源码入手，一步步带你实现字符串优化的小目标。**不但教你如何有效的使用字符串，还为你揭晓这背后的深层次原因**。

本文涉及的知识点，如下图所示：

![图片](https://mmbiz.qpic.cn/mmbiz_png/HrWw6ZuXCsg72dUzgh2ibsMUxjLzhfoDSSsFq97qNFNG7duRicKdBibVTL0IfoP5HloMq5Ze5YEF6EaqIw5JGhfkQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

在看如何优化 String 之前，我们先来了解一下 String 的特性，毕竟知己知彼，才能百战不殆。

#### 字符串的特性

想要了解 String 的特性就必须从它的源码入手，如下所示：

```java
// 源码基于 JDK 1.8
public final class String
    implements java.io.Serializable, Comparable<String>, CharSequence {
    // String 值的实际存储容器
    private final char value[];
    public String() {
        this.value = "".value;
    }
    public String(String original) {
        this.value = original.value;
        this.hash = original.hash;
    }
    // 忽略其他信息
}
```

从他的源码我们可以看出，String 类以及它的 `value[]` 属性都被 `final` 修饰了，其中 `value[]` 是实现字符串存储的最终结构，而 `final` 则表示“最后的、最终的”。

我们知道，被 `final` 修饰的类是不能被继承的，也就是说此类将不能拥有子类，而被 `final` 修饰的变量即为常量，它的值是不能被改变的。**这也就说当 String 一旦被创建之后，就不能被修改了**。

**String 为什么不能被修改？**

String 的类和属性 `value[]` 都被定义为 `final` 了，这样做的好处有以下三点：

1. 安全性：当你在调用其他方法时，比如调用一些系统级操作指令之前，可能会有一系列校验，如果是可变类的话，可能在你校验过后，它的内部的值又被改变了，这样有可能会引起严重的系统崩溃问题，所以迫使 String 设计为 final 类的一个重要原因就是出于安全考虑；
2. 高性能：String 不可变之后就保证的 hash 值的唯一性，这样它就更加高效，并且更适合做 HashMap 的 key- value 缓存；
3. 节约内存：String 的不可变性是它实现字符串常量池的基础，字符串常量池指的是字符串在创建时，先去“常量池”查找是否有此“字符串”，如果有，则不会开辟新空间创作字符串，而是直接把常量池中的引用返回给此对象，这样就能更加节省空间。例如，通常情况下 String 创建有两种方式，直接赋值的方式，如 String str="Java"；另一种是 new 形式的创建，如 String str = new String("Java")。
   1. 当代码中使用第一种方式创建字符串对象时，JVM 首先会检查该对象是否在字符串常量池中，如果在，就返回该对象引用，否则新的字符串将在常量池中被创建。这种方式可以**减少同一个值的字符串对象的重复创建，节约内存**。
   2. String str = new String("Java") 这种方式，首先在编译类文件时，“Java”常量字符串将会放入到常量结构中，在类加载时，“Java”将会在常量池中创建；其次，在调用 new 时，JVM 命令将会调用 String 的构造函数，同时引用常量池中的“Java”字符串，在堆内存中创建一个 String 对象，最后 str 将引用 String 对象。==即：当常量池中存在该字符串时，String对象只在堆中创建一次；不存在时，会去常量池中先创建一个此字符串对象，然后在堆中再创建一个常量池中该对象的拷贝对象，也就是会创建两次。==

#### 1.不要直接+=字符串

通过上面的内容，我们知道了 String 类是不可变的，那么在使用 String 时就不能频繁的 += 字符串了。

**优化前代码**：

```java
public static String doAdd() {
    String result = "";
    for (int i = 0; i < 10000; i++) {
        result += (" i:" + i);
    }
    return result;
}
```

有人可能会问，我的业务需求是这样的，那我该如何实现？

官方为我们提供了两种字符串拼加的方案：`StringBuffer` 和 `StringBuilder`，其中 `StringBuilder` 为非线程安全的，而 `StringBuffer` 则是线程安全的，`StringBuffer` 的拼加方法使用了关键字 `synchronized` 来保证线程的安全，源码如下：

```java
@Override
public synchronized StringBuffer append(CharSequence s) {
    toStringCache = null;
    super.append(s);
    return this;
}
```

也因为使用 `synchronized` 修饰，所以 `StringBuffer` 的拼加性能会比 `StringBuilder` 低。

那我们就用 `StringBuilder` 来实现字符串的拼加，**优化后代码**：

```java
public static String doAppend() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 10000; i++) {
        sb.append(" i:" + i);
    }
    return sb.toString();
}
```

我们通过代码测试一下，两个方法之间的性能差别：

```java
public class StringTest {
    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            // String
            long st1 = System.currentTimeMillis(); // 开始时间
            doAdd();
            long et1 = System.currentTimeMillis(); // 开始时间
            System.out.println("String 拼加，执行时间：" + (et1 - st1));
            // StringBuilder
            long st2 = System.currentTimeMillis(); // 开始时间
            doAppend();
            long et2 = System.currentTimeMillis(); // 开始时间
            System.out.println("StringBuilder 拼加，执行时间：" + (et2 - st2));
            System.out.println();
        }
    }
    public static String doAdd() {
        String result = "";
        for (int i = 0; i < 10000; i++) {
            result += ("Java中文社群:" + i);
        }
        return result;
    }
    public static String doAppend() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("Java中文社群:" + i);
        }
        return sb.toString();
    }
}
```

以上程序的执行结果如下：

> String 拼加，执行时间：429
> StringBuilder 拼加，执行时间：1
>
> String 拼加，执行时间：325
> StringBuilder 拼加，执行时间：1
>
> String 拼加，执行时间：287
> StringBuilder 拼加，执行时间：1
>
> String 拼加，执行时间：265
> StringBuilder 拼加，执行时间：1
>
> String 拼加，执行时间：249
> StringBuilder 拼加，执行时间：1

从结果可以看出，优化前后的性能相差很大。

> 注意：此性能测试的结果与循环的次数有关，也就是说循环的次数越多，他们性能相除的结果也越大。

接下来，我们要思考一个问题：**为什么 StringBuilder.append() 方法比 += 的性能高？而且拼接的次数越多性能的差距也越大？**

当我们打开 StringBuilder 的源码，就可以发现其中的“小秘密”了，StringBuilder 父类 AbstractStringBuilder 的实现源码如下：

```java
abstract class AbstractStringBuilder implements Appendable, CharSequence {
    char[] value;
    int count;
    @Override
    public AbstractStringBuilder append(CharSequence s, int start, int end) {
        if (s == null)
            s = "null";
        if ((start < 0) || (start > end) || (end > s.length()))
            throw new IndexOutOfBoundsException(
                "start " + start + ", end " + end + ", s.length() "
                + s.length());
        int len = end - start;
        ensureCapacityInternal(count + len);
        for (int i = start, j = count; i < end; i++, j++)
            value[j] = s.charAt(i);
        count += len;
        return this;
    }
    // 忽略其他信息...
}
```

而 StringBuilder 使用了父类提供的 `char[]` 作为自己值的实际存储单元，每次在拼加时会修改 `char[]` 数组，StringBuilder `toString()` 源码如下：

```java
@Override
public String toString() {
    // Create a copy, don't share the array
    return new String(value, 0, count);
}
```

综合以上源码可以看出：**StringBuilder 使用了 `char[]` 作为实际存储单元，每次在拼加时只需要修改 `char[]` 数组即可，只是在 `toString()` 时创建了一个字符串；而 String 一旦创建之后就不能被修改，因此在每次拼加时，都需要重新创建新的字符串，所以 StringBuilder.append() 的性能就会比字符串的 += 性能高很多**。

#### 2.善用 intern 方法

善用 String.intern() 方法可以有效的节约内存并提升字符串的运行效率，先来看 `intern()` 方法的定义与源码：

```java
/**
* Returns a canonical representation for the string object.
* <p>
* A pool of strings, initially empty, is maintained privately by the
* class {@code String}.
* <p>
* When the intern method is invoked, if the pool already contains a
* string equal to this {@code String} object as determined by
* the {@link #equals(Object)} method, then the string from the pool is
* returned. Otherwise, this {@code String} object is added to the
* pool and a reference to this {@code String} object is returned.
* <p>
* It follows that for any two strings {@code s} and {@code t},
* {@code s.intern() == t.intern()} is {@code true}
* if and only if {@code s.equals(t)} is {@code true}.
* <p>
* All literal strings and string-valued constant expressions are
* interned. String literals are defined in section 3.10.5 of the
* <cite>The Java&trade; Language Specification</cite>.
*
* @return  a string that has the same contents as this string, but is
*          guaranteed to be from a pool of unique strings.
*/
public native String intern();
```

可以看出 `intern()` 是一个高效的本地方法，它的定义中说的是，当调用 `intern` 方法时，如果字符串常量池中已经包含此字符串，则直接返回此字符串的引用，如果不包含此字符串，先将字符串添加到常量池中，再返回此对象的引用。

那什么情况下适合使用 `intern()` 方法？

Twitter 工程师曾分享过一个 `String.intern()` 的使用示例，Twitter 每次发布消息状态的时候，都会产生一个地址信息，以当时 Twitter 用户的规模预估，服务器需要 32G 的内存来存储地址信息。

```java
public class Location {
    private String city;
    private String region;
    private String countryCode;
    private double longitude;
    private double latitude;
}
```

考虑到其中有很多用户在地址信息上是有重合的，比如，国家、省份、城市等，这时就可以将这部分信息单独列出一个类，以减少重复，代码如下：

```java
public class SharedLocation {

  private String city;
  private String region;
  private String countryCode;
}

public class Location {

  private SharedLocation sharedLocation;
  double longitude;
  double latitude;
}
```

通过优化，数据存储大小减到了 20G 左右。但对于内存存储这个数据来说，依然很大，怎么办呢？

Twitter 工程师使用 `String.intern()` 使重复性非常高的地址信息存储大小从 20G 降到几百兆，从而优化了 String 对象的存储。

实现的核心代码如下：

```java
SharedLocation sharedLocation = new SharedLocation();
sharedLocation.setCity(messageInfo.getCity().intern());    
sharedLocation.setCountryCode(messageInfo.getRegion().intern());
sharedLocation.setRegion(messageInfo.getCountryCode().intern());
```

从 JDK1.7 版本以后，常量池已经合并到了堆中，所以不会复制字符串副本，只是会把首次遇到的字符串的引用添加到常量池中。此时只会判断常量池中是否已经有此字符串，如果有就返回常量池中的字符串引用。

这就相当于以下代码：

```java
String s1 = new String("Java中文社群").intern();
String s2 = new String("Java中文社群").intern();
System.out.println(s1 == s2);
```

执行的结果为：true

此处如果有人问为什么不直接赋值（使用 String s1 = "Java中文社群"），是因为这段代码是简化了上面 Twitter 业务代码的语义而创建的，他使用的是对象的方式，而非直接赋值的方式。更多关于 `intern()` 的内容可以查看[《别再问我new字符串创建了几个对象了！我来证明给你看！》](http://mp.weixin.qq.com/s?__biz=MzU1NTkwODE4Mw==&mid=2247485083&idx=1&sn=a35df68f98a5abaa3460033d54fb2b90&chksm=fbcc6ba3ccbbe2b5bca89762cfac44f1a9c62604c405a4b713474c39880f16257c2982bf4fde&scene=21#wechat_redirect)这篇文章。

#### 3.慎重使用 Split 方法

之所以要劝各位慎用 `Split` 方法，是因为 `Split` 方法大多数情况下使用的是正则表达式，这种分割方式本身没有什么问题，但是由于正则表达式的性能是非常不稳定的，使用不恰当会引起回溯问题，很可能导致 CPU 居高不下。

例如以下正则表达式：

```java
String badRegex = "^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~\\\\/])+$";
String bugUrl = "http://www.apigo.com/dddp-web/pdf/download?request=6e7JGxxxxx4ILd-kExxxxxxxqJ4-CHLmqVnenXC692m74H38sdfdsazxcUmfcOH2fAfY1Vw__%5EDadIfJgiEf";
if (bugUrl.matches(badRegex)) {
    System.out.println("match!!");
} else {
    System.out.println("no match!!");
}
```

执行效果如下图所示：

![图片](https://mmbiz.qpic.cn/mmbiz_png/HrWw6ZuXCsg72dUzgh2ibsMUxjLzhfoDSNWwsg1AFVEOeg0S0j6YibxrTqpgf3psr93v7tQrevL1YJtfXBlkZ4OQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)可以看出，此代码导致了 CPU 使用过高。

**Java 正则表达式使用的引擎实现是 NFA（Non deterministic Finite Automaton，不确定型有穷自动机）自动机，这种正则表达式引擎在进行字符匹配时会发生回溯（backtracking），而一旦发生回溯，那其消耗的时间就会变得很长，有可能是几分钟，也有可能是几个小时，时间长短取决于回溯的次数和复杂度。**

为了更好地解释什么是回溯，我们使用以下面例子进行解释：

```java
text = "abbc";
regex = "ab{1,3}c";
```

上面的这个例子的目的比较简单，匹配以 a 开头，以 c 结尾，中间有 1-3 个 b 字符的字符串。

NFA 引擎对其解析的过程是这样子的：

- 首先，读取正则表达式第一个匹配符 `a` 和 字符串第一个字符 `a` 比较，匹配上了，于是读取正则表达式第二个字符；
- 读取正则表达式第二个匹配符 `b{1,3}` 和字符串的第二个字符 b 比较，匹配上了。但因为 `b{1,3}` 表示 1-3 个 `b` 字符串，以及 NFA 自动机的贪婪特性（也就是说要尽可能多地匹配），所以此时并不会再去读取下一个正则表达式的匹配符，而是依旧使用 `b{1,3}` 和字符串的第三个字符 `b` 比较，发现还是匹配上了，于是继续使用 `b{1,3}` 和字符串的第四个字符 `c` 比较，发现不匹配了，此时就会发生回溯；
- 发生回溯后，我们已经读取的字符串第四个字符 `c` 将被吐出去，指针回到第三个字符串的位置，之后程序读取正则表达式的下一个操作符 `c`，然后再读取当前指针的下一个字符 `c` 进行对比，发现匹配上了，于是读取下一个操作符，然后发现已经结束了。

这就是正则匹配执行的流程和简单的回溯执行流程，而上面的示例在匹配到“com/dzfp-web/pdf/download?request=6e7JGm38jf.....”时因为贪婪匹配的原因，所以程序会一直读后面的字符串进行匹配，最后发现没有点号，于是就一个个字符回溯回去了，于是就会导致了 CPU 运行过高。

所以我们应该慎重使用 Split() 方法，我们可以用 String.indexOf() 方法代替 Split() 方法完成字符串的分割。如果实在无法满足需求，你就在使用 Split() 方法时，对回溯问题加以重视就可以了。

#### 总结

本文通过 String 源码分析，发现了 String 的不可变特性，以及不可变特性的 3 大优点讲解；然后讲了**字符串优化的三个手段：不要直接 += 字符串、善用 intern() 方法和慎重使用 Split() 方法**。并且通过 StringBuilder 的源码分析，了解了 append() 性能高的主要原因，以及正则表达式不稳定性导致回溯问题，进入导致 CPU 使用过高的案例分析，希望可以切实的帮助到你。



---

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



### 为什么要重写hashCode和equals方法

 **一个几乎必问的面试题** 

在面试 Java初级开发的时候，经常会问的一个问题是：**你有没有重写过 `hashcode`方法**？不少候选人直接说没写过。或许真的是没写过，于是还可以再通过一个问题确认：**你在用HashMap的时候，键（ `Key`）部分，有没有放过自定义对象**？而这个时候，候选人说放过，于是两个问题的回答就自相矛盾了。

其实很多人这个问题普遍回答得都不大好，于是在本文里，就干脆 **从 `hash`表讲起**，讲述HashMap的存数据规则，由此大家就自然清楚上述问题的答案了。

------

 **再过一遍Hash算法** 

先复习一下数据结构里的一个知识点：在一个长度为 `n`（假设是 `10000`）的线性表（假设是ArrayList）里，存放着无序的数字；如果我们要找一个指定的数字，就不得不通过从头到尾依次遍历来查找。

我们再来观察Hash表（这里的Hash表纯粹是数据结构上的概念，和Java无关）。它的平均查找次数接近于 `1`，代价相当小，关键是在Hash表里，存放在其中的数据和它的存储位置是用Hash函数关联的。

我们假设一个Hash函数是 `x*x%5`。当然实际情况里不可能用这么简单的Hash函数，这里纯粹为了说明方便，而Hash表是一个长度是 `11`的线性表。如果我们要把 `6`放入其中，那么我们首先会对 `6`用Hash函数计算一下，结果是 `1`，所以我们就把 `6`放入到索引号是 `1`这个位置。同样如果我们要放数字 `7`，经过Hash函数计算， `7`的结果是 `4`，那么它将被放入索引是 `4`的这个位置。这个效果如下图所示。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzriaxY3UB0Kcg50owNgoMtHmqT1vWJ0iaNJ9PRBT7nfgrZ5dUhWQE0NJAJbnribpAx9xaNFNxooR8YLQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这样做的好处非常明显。比如我们要从中找 `6`这个元素，我们可以先通过Hash函数计算 `6`的索引位置，然后直接从 `1`号索引里找到它了。

不过我们会遇到“Hash值冲突”这个问题。比如经过Hash函数计算后， `7`和 `8`会有相同的Hash值，对此Java的HashMap对象采用的是**"链地址法"**的解决方案。效果如下图所示

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzriaxY3UB0Kcg50owNgoMtHmwTKNiaVhsJJkwibrbzCfV9rJ43Sv8kjRelMRs49QqQ0AhhxDUEDoH45A/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

具体的做法是，为所有Hash值是 `i`的对象建立一个同义词链表。假设我们在放入 `8`的时候，发现 `4`号位置已经被占，那么就会新建一个链表结点放入 `8`。同样，如果我们要找 `8`，那么发现 `4`号索引里不是 `8`，那会沿着链表依次查找。

虽然我们还是无法彻底避免Hash值冲突的问题，但是Hash函数设计合理，仍能保证同义词链表的长度被控制在一个合理的范围里。这里讲的理论知识并非无的放矢，大家能在后文里清晰地了解到**重写hashCode方法的重要性**。

------

 **为啥要重写equals和hashCode方法** 

当我们用 HashMap存入自定义的类时，如果不重写这个自定义类的equals和hashCode方法，得到的结果会和我们预期的不一样。我们来看 `WithoutHashCode.java`这个例子。

我们定义了一个 `Key`类；在类中定义了唯一的一个属性 `id`。当前我们先注释掉 `equals`方法和第`hashCode`方法。

```java
import java.util.HashMap;

public class WithoutHashCode {
    public static void main(String[] args) {
        Key k1 = new Key(1);
        Key k2 = new Key(1);
        HashMap<Key, String> hm = new HashMap<>();
        hm.put(k1,"Key with id is 1");
        System.out.println(hm.get(k2));
    }
}
class Key {
    private Integer id;

    public Key(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    /**
     * 没重写hashCode方法时
     * 默认调用的是Object.hashCode()方法，返回的是对象的内存地址
     * 所以不同的对象由于内存地址不同，所以hashCode也不一样
     *
     *重写之后相同id值的对象hashCode也相同
     * @return
     */
    public int hashCode(){
        return id.hashCode();
    }

    /**
     * 没重写equals方法时
     * 默认调用的是Object.equals()方法，根据两个对象的内存地址来判断是否相等
     * 所以不同的对象由于内存地址不同，所以equals返回false
     *
     * 重写之后只要对象都是Key类型并且id值都相等就返回true
     * @param o
     * @return
     */
    public boolean equals(Object o){
        if (o == null || !(o instanceof Key)){
            return false;
        }else {
            return this.getId().equals(((Key)o).getId());
        }
    }
}

```

在 `WithoutHashCode.main`函数里我们定义了两个 `Key`对象，它们的 `id`都是 `1`，就好比它们是两把相同的都能打开同一扇门的钥匙。

我们通过泛型创建了一个HashMap对象。它的键部分可以存放 `Key`类型的对象，值部分可以存储String类型的对象。

我们通过 `put`方法把 `k1`和一串字符放入到 `hm`里；我们想用 `k2`去从HashMap里得到值；这就好比我们想用 `k1`这把钥匙来锁门，用 `k2`来开门。这是符合逻辑的，但返回结果不是我们想象中的那个字符串，而是 `null`。

原因有两个：**一是没有重写hashCode方法**，**二是没有重写equals方法**。

==当我们往HashMap里放 `k1`时，首先会调用 `Key`这个类的 `hashCode`方法计算它的 `hash`值，随后把 `k1`放入hash值所指引的内存位置。==

关键是我们没有在 `Key`里定义 `hashCode`方法。这里调用的仍是 `Object`类的 `hashCode`方法（所有的类都是Object的子类），而 ==`Object`类的 `hashCode`方法返回的 `hash`值其实是 `k1`对象的 `内存地址`==（假设是1000）。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzriaxY3UB0Kcg50owNgoMtHmGRnFCbqPOE8qiaibabOL3Zibod8jKGAUVxln8w1NcTOewMggNUGg6YOTg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

如果我们随后是调用 `hm.get(k1)`，那么我们会再次调用 `hashCode`方法（还是返回 `k1`的地址 `1000`），随后根据得到的 `hash`值，能很快地找到 `k1`。

但我们这里的代码是 `hm.get(k2)`，当我们调用 `Object`类的 `hashCode`方法（因为 `Key`里没定义）计算 `k2`的 `hash`值时，其实得到的是 `k2`的内存地址（假设是 `2000`）。由于 `k1`和 `k2`是两个不同的对象，所以它们的内存地址一定不会相同，也就是说它们的 `hash`值一定不同，这就是我们无法用 `k2`的 `hash`值去拿 `k1`的原因。

当我们把`hashCode`方法的注释去掉后，会发现它是返回 `id`属性的 `hashCode`值，这里 `k1`和 `k2`的 `id`都是1,所以它们的 `hash`值是相等的。

我们再来更正一下存 `k1`和取 `k2`的动作。存 `k1`时，是根据它 `id`的 `hash`值，假设这里是 `100`，把 `k1`对象放入到对应的位置。而取 `k2`时，是先计算它的 `hash`值（由于 `k2`的 `id`也是 `1`，这个值也是 `100`），随后到这个位置去找。

但结果会出乎我们意料：明明 `100`号位置已经有 `k1`，但第 `26`行的输出结果依然是 `null`。其原因就是没有重写 `Key`对象的 `equals`方法。

==HashMap是用***链地址法***来处理冲突==，也就是说，在 `100`号位置上，有可能存在着多个用链表形式存储的对象。它们通过 `hashCode`方法返回的 `hash`值都是100。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzriaxY3UB0Kcg50owNgoMtHmKYfxOmylrYqYLN7aXJEbG5f0UunHBNlL37YGQec9t8nCfcQp6I5qqg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

当我们通过 `k2`的 `hashCode`到 `100`号位置查找时，确实会得到 `k1`。但 `k1`有可能仅仅是和 `k2`具有相同的 `hash`值，但未必和 `k2`相等（ `k1`和 `k2`两把钥匙未必能开同一扇门），这个时候，就需要调用 `Key`对象的 `equals`方法来判断两者是否相等了。

由于我们在 `Key`对象里没有定义 `equals`方法，系统就不得不调用 `Object`类的 `equals`方法。==由于 `Object`的`equals`方法是根据两个对象的**内存地址**来判断==，所以 `k1`和 `k2`一定不会相等，这就是为什么通过 `hm.get(k2)`依然得到 `null`的原因。

为了解决这个问题，我们需要打开`equals`方法的注释。在这个方法里，只要两个对象都是 `Key`类型，而且它们的 `id`相等，它们就相等。

------

 **再次强调** 

由于在项目里经常会用到HashMap，所以在**面试的时候几乎一定会问这个问题**：你有没有重写过 `hashCode`方法？你在使用HashMap时有没有重写 `hashCode`和 `equals`方法？你是怎么写的？

**最后再强调一下：**如果大家要在HashMap的 “键” 部分存放自定义的对象，一定要在这个对象里用自己的 `equals`和 `hashCode`方法来覆盖 `Object`里的同名方法。



#### 对于 `hashCode`有以下几点约束:

1. 在 Java应用程序执行期间，在对同一对象多次调用 `hashCode` 方法时，必须一致地返回相同的整数，前提是将对象进行 `equals` 比较时所用的信息没有被修改；
2. 如果两个对象 `x.equals(y)` 方法返回 `true`，则 `x`、 `y`这两个对象的 `hashCode`必须相等。
3. 如果两个对象 `x.equals(y)` 方法返回 `false`，则 `x`、 `y`这两个对象的 `hashCode`可以相等也可以不等。但是，为不相等的对象生成不同整数结果可以提高哈希表的性能。
4. 默认的 `hashCode`是将内存地址转换为的 `hash`值，重写过后就是自定义的计算方式；也可以通过 `System.identityHashCode(Object)`来返回原本的 `hashCode`。

#### **重写 `equals`方法的几条约定：**

1. **自反性**：即 `x.equals(x)`返回 `true`， `x`不为 `null`；
2. **对称性**：即 `x.equals(y)`与 `y.equals(x）`的结果相同， `x`与 `y`不为 `null`；
3. **传递性**：即 `x.equals(y)`结果为 `true`, `y.equals(z)`结果为 `true`，则 `x.equals(z)`结果也必须为 `true`；
4. **一致性**：即 `x.equals(y)`返回 `true`或 `false`，在未更改 `equals`方法使用的参数条件下，多次调用返回的结果也必须一致。 `x`与 `y`不为 `null`。
5. 如果 `x`不为 `null`, `x.equals(null)`返回 `false`。

---

## HashMap

### **前 言**

在一场面试中最能打动面试官的其实是**细节**，候选人对细节的了解程度决定了留给面试官的印象到底是“基础扎实”还是“基础薄弱”，如果候选人能够举一反三主动阐述自己对一些技术细节的理解和总结，那无疑是面试过程中的一大亮点。`HashMap`是一个看着简单，但其实里面有很多技术细节的数据结构，在一场高端的面试中即使不问任何红黑树(`Java 8`中`HashMap`引入了红黑树来处理极端情况下的哈希碰撞)相关的问题，也会有很多的技术细节值得挖掘。

#### 把书读薄

在`Java 7`中`HashMap`实现有1000多行，到了`Java 8`中增长为2000多行，虽然代码行数不多，但代码中有比较多的位运算，以及其他的一些细枝末节，导致这部分代码看起来很复杂，理解起来比较困难。但是如果我们跳出来看，`HashMap`这个数据结构是非常基础的，我们大脑中首先要有这样一幅图：

![图片](https://mmbiz.qpic.cn/mmbiz_png/R7PtjL3tdAib0uwiarfrxiaEt9lmHOAhYdibMJVazadOLIHm8dB5Us2Nq4WlibbqZL4NMBNIMsRP3NibcOYT3uU7wNrw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



图片来源：https://www.cnblogs.com/tianzhihensu/p/11972780.html

这张图囊括了HashMap中最基础的几个点：

1. `Java`中`HashMap`的实现的基础数据结构是数组，每一对`key`->`value`的键值对组成`Entity`类以双向链表的形式存放到这个数组中
2. 元素在数组中的位置由`key.hashCode()`的值决定，如果两个`key`的哈希值相等，即发生了哈希碰撞，则这两个`key`对应的`Entity`将以链表的形式存放在数组中
3. 调用`HashMap.get()`的时候会首先计算`key`的值，继而在数组中找到`key`对应的位置，然后遍历该位置上的链表找相应的值。

当然这张图中没有体现出来的有两点：

1. 为了提升整个`HashMap`的读取效率，当`HashMap`中存储的元素大小等于桶数组大小乘以负载因子的时候整个`HashMap`就要扩容，以减小哈希碰撞，具体细节我们在后文中讲代码会说到
2. 在`Java 8`中如果桶数组的同一个位置上的链表数量超过一个定值，则整个链表有一定概率会转为一棵红黑树。

整体来看，整个`HashMap`中最重要的点有四个：**初始化**，**数据寻址-`hash`方法**，**数据存储-`put`方法**,**扩容-`resize`方法**，只要理解了这四个点的原理和调用时机，也就理解了整个`HashMap`的设计。

#### 把书读厚

在理解了`HashMap`的整体架构的基础上，我们可以试着回答一下下面的几个问题，如果对其中的某几个问题还有疑惑，那就说明我们还需要深入代码，把书读厚。

1. `HashMap`内部的`bucket`数组长度为什么一直都是2的整数次幂
2. `HashMap`默认的`bucket`数组是多大
3. `HashMap`什么时候开辟`bucket`数组占用内存
4. `HashMap`何时扩容？
5. 桶中的元素链表何时转换为红黑树，什么时候转回链表，为什么要这么设计？
6. `Java 8`中为什么要引进红黑树，是为了解决什么场景的问题？
7. `HashMap`如何处理`key`为`null`的键值对？

### `new HashMap()`

在`JDK 8`中，在调用`new HashMap()`的时候并没有分配数组堆内存，只是做了一些参数校验，初始化了一些常量

```java
public HashMap(int initialCapacity, float loadFactor) {
    if (initialCapacity < 0)
        throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
    if (initialCapacity > MAXIMUM_CAPACITY)
        initialCapacity = MAXIMUM_CAPACITY;
    if (loadFactor <= 0 || Float.isNaN(loadFactor))
        throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
    this.loadFactor = loadFactor;
    this.threshold = tableSizeFor(initialCapacity);
}

static final int tableSizeFor(int cap) {
    int n = cap - 1;
    n |= n >>> 1;
    n |= n >>> 2;
    n |= n >>> 4;
    n |= n >>> 8;
    n |= n >>> 16;
    return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
}
```

`tableSizeFor`的作用是找到大于`cap`的最小的2的整数幂，我们假设n(注意是n，不是cap哈)对应的二进制为000001xxxxxx，其中x代表的二进制位是0是1我们不关心，

`n |= n >>> 1;`执行后`n`的值为：

![图片](https://mmbiz.qpic.cn/mmbiz_png/R7PtjL3tdAib0uwiarfrxiaEt9lmHOAhYdibnWhteLvazicGAkd7go3CeiabRjYN0ib1Wb5h1B8TuPOHBT1cr1K0GCaSA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)image-20210403000630081

可以看到此时`n`的二进制最高两位已经变成了1（1和0或1异或都是1），再接着执行第二行代码：

![图片](https://mmbiz.qpic.cn/mmbiz_png/R7PtjL3tdAib0uwiarfrxiaEt9lmHOAhYdibibEwy9YFEA0Gy21LJYNColicAxpW11teDQpRZvE0HqcTC1QYJ6Z7fWBQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

可见`n`的二进制最高四位已经变成了1，等到执行完代码`n |= n >>> 16;`之后，`n`的二进制最低位全都变成了1，也就是`n = 2^x - 1`其中x和n的值有关，如果没有超过`MAXIMUM_CAPACITY`，最后会返回一个2的正整数次幂(即 2^x^ )，因此`tableSizeFor`的作用就是保证==返回一个比入参大的最小的2的正整数次幂==。

在`JDK 7`中初始化的代码大体一致，在`HashMap`第一次`put`的时候会调用`inflateTable`计算桶数组的长度，但其算法并没有变：

```java
// 第一次put时，初始化table
private void inflateTable(int toSize) {
    // Find an power of 2 >= toSize
    int capacity = roundUpToPowerOf2(toSize);
    threshold = (int)Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
    table = new Entry(capacity);
    initHashSeedAsNeeded(capacity);
}
```

这里我们也回答了开头提出来的问题：

`HashMap`什么时候开辟`bucket`数组占用内存？答案是在`HashMap`第一次`put`的时候，无论`Java 8`还是`Java 7`都是这样实现的。这里我们可以看到两个版本的实现中，桶数组的大小都是2的正整数幂，至于为什么这么设计，看完后文你就明白了。

### `hash`

在`HashMap`这个特殊的数据结构中，`hash`函数承担着寻址定址的作用，其性能对整个`HashMap`的性能影响巨大，那什么才是一个好的`hash`函数呢？

- 计算出来的哈希值足够散列，能够有效减少哈希碰撞
- 本身能够快速计算得出，因为`HashMap`每次调用`get`和`put`的时候都会调用`hash`方法

下面是`Java 8`中的实现：

```java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```

这里比较重要的是`(h = key.hashCode()) ^ (h >>> 16)`，这个位运算其实是将`key.hashCode()`计算出来的`hash`值的高16位与低16位继续异或，为什么要这么做呢？

我们知道`hash`函数的作用是用来确定`key`在桶数组中的位置的，在`JDK`中为了更好的性能，通常会这样写：

```java
index =(table.length - 1) & key.hash();
```

回忆前文中的内容，`table.length`是一个2的正整数次幂，类似于`000100000`，这样的值减一就成了`000011111`，通过位运算可以高效寻址，这也回答了前文中提到的一个问题，`HashMap`内部的`bucket`数组长度为什么一直都是2的整数次幂？好处之一就是可以通过构造位运算快速寻址定址。

回到本小节的议题，既然计算出来的哈希值都要与`table.length - 1`做与运算，那就意味着计算出来的`hash`值只有低位有效，这样会加大碰撞几率，因此让高16位与低16位做异或，让低位保留部分高位信息，减少哈希碰撞。

我们再看`Java 7`中对hash的实现：

```java
final int hash(Object k) {
    int h = hashSeed;
    if (0 != h && k instanceof String) {
        return sun.misc.Hashing.stringHash32((String) k);
    }

    h ^= k.hashCode();

    // This function ensures that hashCodes that differ only by 
    // constant multiples at each bit position have a bounded 
    // number of collisions (approximately 8 at default load factor). 
    h ^= (h >>> 20) ^ (h >>> 12);
    return h ^ (h >>> 7) ^ (h >>> 4);
}
```

`Java 7`中为了避免`hash`值的高位信息丢失，做了更加复杂的异或运算，但是基本出发点都是一样的，都是让哈希值的低位保留部分高位信息，减少哈希碰撞。

### `put`

在`Java 8`中`put`这个方法的思路分为以下几步：

1. 调用`key`的`hashCode`方法计算哈希值，并据此计算出数组下标index
2. 如果发现当前的桶数组为`null`，则调用`resize()`方法进行初始化
3. 如果没有发生哈希碰撞，则直接放到对应的桶中
4. 如果发生哈希碰撞，且节点已经存在，就替换掉相应的`value`
5. 如果发生哈希碰撞，且桶中存放的是树状结构，则挂载到树上
6. 如果碰撞后为链表，添加到链表尾，如果链表长度超过`TREEIFY_THRESHOLD`默认是8，则将链表转换为树结构
7. 数据`put`完成后，如果`HashMap`的总数超过`threshold`就要`resize`

具体代码以及注释如下：

```java
public V put(K key, V value) {
    // 调用上文我们已经分析过的hash方法
    return putVal(hash(key), key, value, false, true);
}

final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
                boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    if ((tab = table) == null || (n = tab.length) == 0)
        // 第一次put时，会调用resize进行桶数组初始化
        n = (tab = resize()).length;
    // 根据数组长度和哈希值相与来寻址，原理上文也分析过
    if ((p = tab[i = (n - 1) & hash]) == null)
        // 如果没有哈希碰撞，直接放到桶中
        tab[i] = newNode(hash, key, value, null);
    else {
        Node<K,V> e; K k;
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            // 哈希碰撞，且节点已存在，直接替换
            e = p;
        else if (p instanceof TreeNode)
            // 哈希碰撞，树结构
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            // 哈希碰撞，链表结构
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    // 链表过长，转换为树结构
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    // 如果节点已存在，则跳出循环
                    break;
                // 否则，指针后移，继续后循环
                p = e;
            }
        }
        if (e != null) { // existing mapping for key
            // 对应着上文中节点已存在，跳出循环的分支
            // 直接替换
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    ++modCount;
    if (++size > threshold)
        // 如果超过阈值，还需要扩容
        resize();
    afterNodeInsertion(evict);
    return null;
}
```

相比之下`Java 7`中的`put`方法就简单不少

```java
public V put(K key, V value) {
    // 如果 key 为 null，调用 putForNullKey 方法进行处理  
    if (key == null)
        return putForNullKey(value);
    int hash = hash(key.hashCode());
    int i = indexFor(hash, table.length);
    for (Entry<K, V> e = table[i]; e != null; e = e.next) {
        Object k;  
        if (e.hash == hash && ((k = e.key) == key
                || key.equals(k))) {
            V oldValue = e.value;
            e.value = value;
            e.recordAccess(this);
            return oldValue;
        }
    }
    modCount++;
    addEntry(hash, key, value, i);
    return null;
}

void addEntry(int hash, K key, V value, int bucketIndex) {
    Entry<K, V> e = table[bucketIndex];     // ①  
    table[bucketIndex] = new Entry<K, V>(hash, key, value, e);
    if (size++ >= threshold)
        resize(2 * table.length);    // ②  
}
```

这里有一个小细节，`HashMap`允许`put`key为`null`的键值对，但是这样的键值对都放到了桶数组的第0个桶中。

### `resize()`

`resize`是整个`HashMap`中最复杂的一个模块，如果在`put`数据之后超过了`threshold`的值，则需要扩容，扩容意味着桶数组大小变化，我们在前文中分析过，`HashMap`寻址是通过`index =(table.length - 1) & key.hash();`来计算的，现在`table.length`发生了变化，势必会导致部分`key`的位置也发生了变化，`HashMap`是如何设计的呢？

这里就涉及到桶数组长度为2的正整数幂的第二个优势了：当桶数组长度为2的正整数幂时，如果桶发生扩容（长度翻倍），则桶中的元素大概只有一半需要切换到新的桶中，另一半留在原先的桶中就可以，并且这个概率可以看做是均等的。

![图片](https://mmbiz.qpic.cn/mmbiz_png/R7PtjL3tdAib0uwiarfrxiaEt9lmHOAhYdiblzYia6ic0unz6yDBBUz9zaYTfnYCdtazFW4ibtEf8bs5F6K2zdNPK7n9w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)image-20210403103507368

通过这个分析可以看到如果在即将扩容的那个位上`key.hash()`的二进制值为0，则扩容后在桶中的地址不变，否则，扩容后的最高位变为了1，新的地址也可以快速计算出来`newIndex = oldCap + oldIndex;`

下面是`Java 8`中的实现：

```java
final Node<K,V>[] resize() {
    Node<K,V>[] oldTab = table;
    int oldCap = (oldTab == null) ? 0 : oldTab.length;
    int oldThr = threshold;
    int newCap, newThr = 0;
    if (oldCap > 0) {
        // 如果oldCap > 0则对应的是扩容而不是初始化
        if (oldCap >= MAXIMUM_CAPACITY) {
            threshold = Integer.MAX_VALUE;
            return oldTab;
        }
        // 没有超过最大值，就扩大为原先的2倍
        else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY &&
                    oldCap >= DEFAULT_INITIAL_CAPACITY)
            newThr = oldThr << 1; // double threshold
    }
    else if (oldThr > 0) // initial capacity was placed in threshold
        // 如果oldCap为0， 但是oldThr不为0，则代表的是table还未进行过初始化
        newCap = oldThr;
    else {               // zero initial threshold signifies using defaults
        newCap = DEFAULT_INITIAL_CAPACITY;
        newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
    }
    if (newThr == 0) {
        // 如果到这里newThr还未计算，比如初始化时，则根据容量计算出新的阈值
        float ft = (float)newCap * loadFactor;
        newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                    (int)ft : Integer.MAX_VALUE);
    }
    threshold = newThr;
    @SuppressWarnings({"rawtypes","unchecked"})
    Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
    table = newTab;
    if (oldTab != null) {
        for (int j = 0; j < oldCap; ++j) {
            // 遍历之前的桶数组，对其值重新散列
            Node<K,V> e;
            if ((e = oldTab[j]) != null) {
                oldTab[j] = null;
                if (e.next == null)
                    // 如果原先的桶中只有一个元素，则直接放置到新的桶中
                    newTab[e.hash & (newCap - 1)] = e;
                else if (e instanceof TreeNode)
                    ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                else { // preserve order
                    // 如果原先的桶中是链表
                    Node<K,V> loHead = null, loTail = null;
                    // hiHead和hiTail代表元素在新的桶中和旧的桶中的位置不一致
                    Node<K,V> hiHead = null, hiTail = null;
                    Node<K,V> next;
                    do {
                        next = e.next;
                        if ((e.hash & oldCap) == 0) {
                            if (loTail == null)
                                loHead = e;
                            else
                                loTail.next = e;
                            loTail = e;
                        }
                        else {
                            if (hiTail == null)
                                hiHead = e;
                            else
                                hiTail.next = e;
                            hiTail = e;
                        }
                    } while ((e = next) != null);
                    if (loTail != null) {
                        loTail.next = null;
                        // loHead和loTail代表元素在新的桶中和旧的桶中的位置一致
                        newTab[j] = loHead;
                    }
                    if (hiTail != null) {
                        hiTail.next = null;
                        // 新的桶中的位置 = 旧的桶中的位置 + oldCap， 详细分析见前文
                        newTab[j + oldCap] = hiHead;
                    }
                }
            }
        }
    }
    return newTab;
}
```

`Java 7`中的`resize`方法相对简单许多：

1. 基本的校验之后`new`一个新的桶数组，大小为指定入参
2. 桶内的元素根据新的桶数组长度确定新的位置，放置到新的桶数组中

```java
void resize(int newCapacity) {
    Entry[] oldTable = table;
    int oldCapacity = oldTable.length;
    if (oldCapacity == MAXIMUM_CAPACITY) {
        threshold = Integer.MAX_VALUE;
        return;
    }

    Entry[] newTable = new Entry[newCapacity];
    boolean oldAltHashing = useAltHashing;
    useAltHashing |= sun.misc.VM.isBooted() &&
            (newCapacity >= Holder.ALTERNATIVE_HASHING_THRESHOLD);
    boolean rehash = oldAltHashing ^ useAltHashing;
    transfer(newTable, rehash);
    table = newTable;
    threshold = (int) Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);
}

void transfer(Entry[] newTable, boolean rehash) {
    int newCapacity = newTable.length;
    for (Entry<K, V> e : table) {
        //链表跟table[i]断裂遍历，头部往后遍历插入到newTable中
        while (null != e) {
            Entry<K, V> next = e.next;
            if (rehash) {
                e.hash = null == e.key ? 0 : hash(e.key);
            }
            int i = indexFor(e.hash, newCapacity);
            e.next = newTable[i];
            newTable[i] = e;
            e = next;
        }
    }
}
```

### 总结

在看完了`HashMap`在`Java 8`和`Java 7`的实现之后我们回答一下前文中提出来的那几个问题：

1. `HashMap`内部的`bucket`数组长度为什么一直都是2的整数次幂

   答：这样做有两个好处，第一，可以通过`(table.length - 1) & key.hash()`这样的位运算快速寻址，第二，在`HashMap`扩容的时候可以保证同一个桶中的元素均匀的散列到新的桶中，具体一点就是同一个桶中的元素在扩容后一半留在原先的桶中，一半放到了新的桶中。

2. `HashMap`默认的`bucket`数组是多大

   答：默认是16，即使指定的大小不是2的整数次幂，`HashMap`也会找到一个最近的2的整数次幂来初始化桶数组。

3. `HashMap`什么时候开辟`bucket`数组占用内存

   答：在第一次`put`的时候调用`resize`方法

4. `HashMap`何时扩容？

   答：当`HashMap`中的元素数量超过阈值时，阈值计算方式是`capacity * loadFactor`，在`HashMap`中`loadFactor`是0.75

5. 桶中的元素链表何时转换为红黑树，什么时候转回链表，为什么要这么设计？

   答：当同一个桶中的元素数量大于等于8的时候元素中的链表转换为红黑树，反之，当桶中的元素数量小于等于6的时候又会转为链表，这样做的原因是避免红黑树和链表之间频繁转换，引起性能损耗

   [树化链化](https://www.cnblogs.com/aaabbbcccddd/p/14849064.html)

   > 树化

   hashMap并不是在链表元素个数大于8就一定会转换为红黑树，而是先考虑扩容（table长度 < 64 采用扩容方式），扩容达到默认限制后才转换。

   ```java
   // 源码
   // static final int TREEIFY_THRESHOLD = 8;
   // binCount 从0开始
   if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
   	treeifyBin(tab, hash);
   ```

   > 链化

   `当红黑树中的元素减少并小于一定数量时，会切换回链表。`

   hashMap的红黑树不一定小于6的时候才会转换为链表，而是只有在resize的时候才会根据 UNTREEIFY_THRESHOLD 进行转换。

   

   1、调用map的remove方法删除元素

   ​		通过红黑树根节点及其子节点是否为空来判断。

   2、resize的时候，对红黑树进行了拆分

   ​		此处用到了 UNTREEIFY_THRESHOLD 的判断，当红黑树节点元素小于等于6时，才调用`untreeify`方法转换回链表

   ```java
   // 源码
   // static final int UNTREEIFY_THRESHOLD = 6;
    if (lc <= UNTREEIFY_THRESHOLD)
   	tab[index] = loHead.untreeify(map);
   ```

   

6. `Java 8`中为什么要引进红黑树，是为了解决什么场景的问题？

   答：引入红黑树是为了避免`hash`性能急剧下降，引起`HashMap`的读写性能急剧下降的场景，正常情况下，一般是不会用到红黑树的，在一些极端场景下，假如客户端实现了一个性能拙劣的`hashCode`方法，可以保证`HashMap`的读写复杂度不会低于O(lgN)

   ```java
   public int hashCode() {
       return 1;
   }
   ```

7. `HashMap`如何处理`key`为`null`的键值对？

   答：放置在桶数组中下标为0的桶中

   ![图片](https://mmbiz.qpic.cn/mmbiz_gif/b96CibCt70iabwjyojLhA03PtxUnkNPREnt2F48ywfXLpDdDAjicOTPI8Q94tVLbJ58tbRs12iaXDKhUOW9gd4NlFA/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

### HashMap遍历

本文**先从 HashMap 的**遍历方法讲起，然后再从**性能**、**原理**以及**安全性**等方面，来分析 HashMap 各种遍历方式的优势与不足，本文主要内容如下图所示：

![图片](https://mmbiz.qpic.cn/mmbiz_png/HrWw6ZuXCsgg2akYxAAibK8UY8kKvNDSibHvX8IrHWA7ibPKib7zN3ZU0ibFuqT9icbNTibl8wB9VdBKLAt10vYYa0TVA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### HashMap 遍历

HashMap **遍历从大的方向来说，可分为以下 4 类**：

1. 迭代器（Iterator）方式遍历；
2. For Each 方式遍历；
3. Lambda 表达式遍历（JDK 1.8+）;
4. Streams API 遍历（JDK 1.8+）。

但每种类型下又有不同的实现方式，因此具体的遍历方式又可以分为以下 7 种：

1. 使用迭代器（Iterator）EntrySet 的方式进行遍历；
2. 使用迭代器（Iterator）KeySet 的方式进行遍历；
3. 使用 For Each EntrySet 的方式进行遍历；
4. 使用 For Each KeySet 的方式进行遍历；
5. 使用 Lambda 表达式的方式进行遍历；
6. 使用 Streams API 单线程的方式进行遍历；
7. 使用 Streams API 多线程的方式进行遍历。

接下来我们来看每种遍历方式的具体实现代码。

##### 1.迭代器 EntrySet

```java
public class HashMapTest {
    public static void main(String[] args) {
        // 创建并赋值 HashMap
        Map<Integer, String> map = new HashMap();
        map.put(1, "Java");
        map.put(2, "JDK");
        map.put(3, "Spring Framework");
        map.put(4, "MyBatis framework");
        map.put(5, "Java中文社群");
        // 遍历
        Iterator<Map.Entry<Integer, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, String> entry = iterator.next();
            System.out.print(entry.getKey());
            System.out.print(entry.getValue());
        }
    }
}
```

以上程序的执行结果为：

> 1 Java 2 JDK 3 Spring Framework 4 MyBatis framework 5 Java中文社群

##### 2.迭代器 KeySet

```java
public class HashMapTest {
    public static void main(String[] args) {
        // 创建并赋值 HashMap
        Map<Integer, String> map = new HashMap();
        map.put(1, "Java");
        map.put(2, "JDK");
        map.put(3, "Spring Framework");
        map.put(4, "MyBatis framework");
        map.put(5, "Java中文社群");
        // 遍历
        Iterator<Integer> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            Integer key = iterator.next();
            System.out.print(key);
            System.out.print(map.get(key));
        }
    }
}
```

以上程序的执行结果为：

> 1 Java 2 JDK 3 Spring Framework 4 MyBatis framework 5 Java中文社群

##### 3.ForEach EntrySet

```java
public class HashMapTest {
    public static void main(String[] args) {
        // 创建并赋值 HashMap
        Map<Integer, String> map = new HashMap();
        map.put(1, "Java");
        map.put(2, "JDK");
        map.put(3, "Spring Framework");
        map.put(4, "MyBatis framework");
        map.put(5, "Java中文社群");
        // 遍历
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            System.out.print(entry.getKey());
            System.out.print(entry.getValue());
        }
    }
}
```

以上程序的执行结果为：

> 1 Java 2 JDK 3 Spring Framework 4 MyBatis framework 5 Java中文社群

##### 4.ForEach KeySet

```java
public class HashMapTest {
    public static void main(String[] args) {
        // 创建并赋值 HashMap
        Map<Integer, String> map = new HashMap();
        map.put(1, "Java");
        map.put(2, "JDK");
        map.put(3, "Spring Framework");
        map.put(4, "MyBatis framework");
        map.put(5, "Java中文社群");
        // 遍历
        for (Integer key : map.keySet()) {
            System.out.print(key);
            System.out.print(map.get(key));
        }
    }
}
```

以上程序的执行结果为：

> 1 Java 2 JDK 3 Spring Framework 4 MyBatis framework 5 Java中文社群

##### 5.Lambda

```java
public class HashMapTest {
    public static void main(String[] args) {
        // 创建并赋值 HashMap
        Map<Integer, String> map = new HashMap();
        map.put(1, "Java");
        map.put(2, "JDK");
        map.put(3, "Spring Framework");
        map.put(4, "MyBatis framework");
        map.put(5, "Java中文社群");
        // 遍历
        map.forEach((key, value) -> {
            System.out.print(key);
            System.out.print(value);
        });
    }
}
```

以上程序的执行结果为：

> 1 Java 2 JDK 3 Spring Framework 4 MyBatis framework 5 Java中文社群

##### 6.Streams API 单线程

```java
public class HashMapTest {
    public static void main(String[] args) {
        // 创建并赋值 HashMap
        Map<Integer, String> map = new HashMap();
        map.put(1, "Java");
        map.put(2, "JDK");
        map.put(3, "Spring Framework");
        map.put(4, "MyBatis framework");
        map.put(5, "Java中文社群");
        // 遍历
        map.entrySet().stream().forEach((entry) -> {
            System.out.print(entry.getKey());
            System.out.print(entry.getValue());
        });
    }
}
```

以上程序的执行结果为：

> 1 Java 2 JDK 3 Spring Framework 4 MyBatis framework 5 Java中文社群

##### 7.Streams API 多线程

```java
public class HashMapTest {
    public static void main(String[] args) {
        // 创建并赋值 HashMap
        Map<Integer, String> map = new HashMap();
        map.put(1, "Java");
        map.put(2, "JDK");
        map.put(3, "Spring Framework");
        map.put(4, "MyBatis framework");
        map.put(5, "Java中文社群");
        // 遍历
        map.entrySet().parallelStream().forEach((entry) -> {
            System.out.print(entry.getKey());
            System.out.print(entry.getValue());
        });
    }
}
```

以上程序的执行结果为：

> 4 MyBatis framework 5 Java中文社群 1 Java 2 JDK 3 Spring Framework

#### 性能测试

接下来我们使用 Oracle 官方提供的性能测试工具 JMH（Java Microbenchmark Harness，JAVA 微基准测试套件）来测试一下这 7 种循环的性能。

首先，我们先要引入 JMH 框架，在 `pom.xml` 文件中添加如下配置：

```xml
<!-- https://mvnrepository.com/artifact/org.openjdk.jmh/jmh-core -->
<dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-core</artifactId>
    <version>1.23</version>
</dependency>
<dependency>
    <groupId>org.openjdk.jmh</groupId>
    <artifactId>jmh-generator-annprocess</artifactId>
    <version>1.20</version>
    <scope>provided</scope>
</dependency>
```

然后编写测试代码，如下所示：

```java
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput) // 测试类型：吞吐量
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS) // 预热 2 轮，每次 1s
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS) // 测试 5 轮，每次 3s
@Fork(1) // fork 1 个线程
@State(Scope.Thread) // 每个测试线程一个实例
public class HashMapCycle {
    static Map<Integer, String> map = new HashMap() {{
        // 添加数据
        for (int i = 0; i < 10; i++) {
            put(i, "val:" + i);
        }
    }};

    public static void main(String[] args) throws RunnerException {
        // 启动基准测试
        Options opt = new OptionsBuilder()
                .include(HashMapCycle.class.getSimpleName()) // 要导入的测试类
                .output("/Users/admin/Desktop/jmh-map.log") // 输出测试结果的文件
                .build();
        new Runner(opt).run(); // 执行测试
    }

    @Benchmark
    public void entrySet() {
        // 遍历
        Iterator<Map.Entry<Integer, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, String> entry = iterator.next();
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
    }

    @Benchmark
    public void keySet() {
        // 遍历
        Iterator<Integer> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            Integer key = iterator.next();
            System.out.println(key);
            System.out.println(map.get(key));
        }
    }

    @Benchmark
    public void forEachEntrySet() {
        // 遍历
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
    }

    @Benchmark
    public void forEachKeySet() {
        // 遍历
        for (Integer key : map.keySet()) {
            System.out.println(key);
            System.out.println(map.get(key));
        }
    }

    @Benchmark
    public void lambda() {
        // 遍历
        map.forEach((key, value) -> {
            System.out.println(key);
            System.out.println(value);
        });
    }

    @Benchmark
    public void streamApi() {
        // 单线程遍历
        map.entrySet().stream().forEach((entry) -> {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        });
    }

    @Benchmark
    public void parallelStreamApi() {
        // 多线程遍历
        map.entrySet().parallelStream().forEach((entry) -> {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        });
    }
}
```

所有被添加了 `@Benchmark` 注解的方法都会被测试，测试结果如下：

![图片](https://mmbiz.qpic.cn/mmbiz_png/HrWw6ZuXCsgg2akYxAAibK8UY8kKvNDSibWRYHaoaFCcYmHxwk9C29D7e2U10cj0cSNqSa9BglG68og8Wr6NicCYg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

其中 Score 列表示平均执行时间， `±` 符号表示误差。从以上结果可以看出，**如果加上后面的误差值的话，可以得出的结论是，除了并行循环的 `parallelStream` 性能比极高之外（多线程方式性能肯定比较高），其他方式的遍历方法在性能方面几乎没有任何差别。**

> 注：以上结果基于测试环境：JDK 1.8 / Mac mini (2018) / Idea 2020.1

#### 性能原理分析

要理解性能测试的结果，我们需要把所有遍历代码通过 `javac`，编译成字节码来看具体的原因，编译之后我们使用 Idea 打开字节码信息，内容如下：

```java
//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package com.example;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class HashMapTest {
    static Map<Integer, String> map = new HashMap() {
        {
            for(int var1 = 0; var1 < 2; ++var1) {
                this.put(var1, "val:" + var1);
            }

        }
    };

    public HashMapTest() {
    }

    public static void main(String[] var0) {
        entrySet();
        keySet();
        forEachEntrySet();
        forEachKeySet();
        lambda();
        streamApi();
        parallelStreamApi();
    }

    public static void entrySet() {
        Iterator var0 = map.entrySet().iterator();

        while(var0.hasNext()) {
            Entry var1 = (Entry)var0.next();
            System.out.println(var1.getKey());
            System.out.println((String)var1.getValue());
        }

    }

    public static void keySet() {
        Iterator var0 = map.keySet().iterator();

        while(var0.hasNext()) {
            Integer var1 = (Integer)var0.next();
            System.out.println(var1);
            System.out.println((String)map.get(var1));
        }

    }

    public static void forEachEntrySet() {
        Iterator var0 = map.entrySet().iterator();

        while(var0.hasNext()) {
            Entry var1 = (Entry)var0.next();
            System.out.println(var1.getKey());
            System.out.println((String)var1.getValue());
        }

    }

    public static void forEachKeySet() {
        Iterator var0 = map.keySet().iterator();

        while(var0.hasNext()) {
            Integer var1 = (Integer)var0.next();
            System.out.println(var1);
            System.out.println((String)map.get(var1));
        }

    }

    public static void lambda() {
        map.forEach((var0, var1) -> {
            System.out.println(var0);
            System.out.println(var1);
        });
    }

    public static void streamApi() {
        map.entrySet().stream().forEach((var0) -> {
            System.out.println(var0.getKey());
            System.out.println((String)var0.getValue());
        });
    }

    public static void parallelStreamApi() {
        map.entrySet().parallelStream().forEach((var0) -> {
            System.out.println(var0.getKey());
            System.out.println((String)var0.getValue());
        });
    }
}
```

从结果可以看出，除了 Lambda 和 Streams API 之外，通过迭代器循环和 `for` 循环的遍历的 `EntrySet` 最终生成的代码是一样的，他们都是在循环中创建了一个遍历对象 `Entry` ，如下所示：

```java
public static void entrySet() {
    Iterator var0 = map.entrySet().iterator();
    while(var0.hasNext()) {
        Entry var1 = (Entry)var0.next();
        System.out.println(var1.getKey());
        System.out.println((String)var1.getValue());
    }
}
public static void forEachEntrySet() {
    Iterator var0 = map.entrySet().iterator();
    while(var0.hasNext()) {
        Entry var1 = (Entry)var0.next();
        System.out.println(var1.getKey());
        System.out.println((String)var1.getValue());
    }
}
```

而通过迭代器和 `for` 循环遍历的 `KeySet` 代码也是一样的，如下所示：

```java
public static void keySet() {
    Iterator var0 = map.keySet().iterator();
    while(var0.hasNext()) {
        Integer var1 = (Integer)var0.next();
        System.out.println(var1);
        System.out.println((String)map.get(var1));
    }
} 
public static void forEachKeySet() {
    Iterator var0 = map.keySet().iterator();
    while(var0.hasNext()) {
        Integer var1 = (Integer)var0.next();
        System.out.println(var1);
        System.out.println((String)map.get(var1));
    }
}
```

可以看出 `KeySet` 在循环中创建了一个 `Integer` 的局部变量，并且值是从 `map` 对象中直接获取的。

**所以通过字节码来看，使用 `EntrySet` 和 `KeySet` 代码差别不是很大，并不像网上说的那样 `KeySet` 的性能远不如 `EntrySet`，因此从性能的角度来说 `EntrySet` 和 `KeySet` 几乎是相近的，但从代码的优雅型和可读性来说，还是推荐使用 `EntrySet`。**

##### 安全性测试

从上面的性能测试结果和原理分析，我想大家应该选用那种遍历方式，已经心中有数的，而接下来我们就从「**安全**」的角度入手，来分析那种遍历方式更安全。

我们把以上遍历划分为四类进行测试：迭代器方式、For 循环方式、Lambda 方式和 Stream 方式，测试代码如下。

###### 1.迭代器方式

```java
Iterator<Map.Entry<Integer, String>> iterator = map.entrySet().iterator();
while (iterator.hasNext()) {
    Map.Entry<Integer, String> entry = iterator.next();
    if (entry.getKey() == 1) {
        // 删除
        System.out.println("del:" + entry.getKey());
        iterator.remove();
    } else {
        System.out.println("show:" + entry.getKey());
    }
}
```

以上程序的执行结果：

> show:0
>
> del:1
>
> show:2

测试结果：**迭代器中循环删除数据安全**。

###### 2.For 循环方式

```java
for (Map.Entry<Integer, String> entry : map.entrySet()) {
    if (entry.getKey() == 1) {
        // 删除
        System.out.println("del:" + entry.getKey());
        map.remove(entry.getKey());
    } else {
        System.out.println("show:" + entry.getKey());
    }
}
```

以上程序的执行结果：

![图片](https://mmbiz.qpic.cn/mmbiz_png/HrWw6ZuXCsgg2akYxAAibK8UY8kKvNDSibw83V0x5s2XtHOTbLmxVWxu47U3nvbZ3O3TIOBm6DNyrg9hTPEFhMOg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

测试结果：**For 循环中删除数据非安全**。

###### 3.Lambda 方式

```java
map.forEach((key, value) -> {
    if (key == 1) {
        System.out.println("del:" + key);
        map.remove(key);
    } else {
        System.out.println("show:" + key);
    }
});
```

以上程序的执行结果：

![图片](https://mmbiz.qpic.cn/mmbiz_png/HrWw6ZuXCsgg2akYxAAibK8UY8kKvNDSibI2gMTvdR68ibQnI4LCvr1E66g4jvv4AfG2dQjricwbXUIibiaONICvia4Fw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)测试结果：**Lambda 循环中删除数据非安全**。

**Lambda 删除的正确方式**：

```java
// 根据 map 中的 key 去判断删除
map.keySet().removeIf(key -> key == 1);
map.forEach((key, value) -> {
    System.out.println("show:" + key);
});
```

以上程序的执行结果：

> show:0
>
> show:2

从上面的代码可以看出，可以先使用 `Lambda` 的 `removeIf` 删除多余的数据，再进行循环是一种正确操作集合的方式。

###### 4.Stream 方式

```java
map.entrySet().stream().forEach((entry) -> {
    if (entry.getKey() == 1) {
        System.out.println("del:" + entry.getKey());
        map.remove(entry.getKey());
    } else {
        System.out.println("show:" + entry.getKey());
    }
});
```

以上程序的执行结果：

![图片](https://mmbiz.qpic.cn/mmbiz_png/HrWw6ZuXCsgg2akYxAAibK8UY8kKvNDSibroZEllaKwrFolYsDabsDMrOLnibXDWGmdhLCZQKlicYYMvEJvQe0RE9g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

测试结果：**Stream 循环中删除数据非安全**。

**Stream 循环的正确方式**：

```java
map.entrySet().stream().filter(m -> 1 != m.getKey()).forEach((entry) -> {
    if (entry.getKey() == 1) {
        System.out.println("del:" + entry.getKey());
    } else {
        System.out.println("show:" + entry.getKey());
    }
});
```

以上程序的执行结果：

> show:0
>
> show:2

从上面的代码可以看出，可以使用 `Stream` 中的 `filter` 过滤掉无用的数据，再进行遍历也是一种安全的操作集合的方式。

###### 小结

我们不能在遍历中使用集合 `map.remove()` 来删除数据，这是非安全的操作方式，但我们可以使用迭代器的 `iterator.remove()` 的方法来删除数据，这是安全的删除集合的方式。同样的我们也可以使用 Lambda 中的 `removeIf` 来提前删除数据，或者是使用 Stream 中的 `filter` 过滤掉要删除的数据进行循环，这样都是安全的，当然我们也可以在 `for` 循环前删除数据在遍历也是线程安全的。

#### 总结

本文我们讲了 HashMap 4 大类（迭代器、for、lambda、stream）遍历方式，以及具体的 7 种遍历方法，**除了 Stream 的并行循环，其他几种遍历方法的性能差别不大，但从简洁性和优雅性上来看，Lambda 和 Stream 无疑是最适合的遍历方式**。除此之外我们还从「安全性」方面测试了 4 大类遍历结果，**从安全性来讲，我们应该使用迭代器提供的 `iterator.remove()` 方法来进行删除，这种方式是安全的在遍历中删除集合的方式，或者使用 Stream 中的 `filter` 过滤掉要删除的数据再进行循环，也是安全的操作方式**。

总体来说，本文提供了 7 种方式肯定也不是最全的，只是想给读者在使用 HashMap 时多一种选择，**然而选择那一种形式的写法，要综合：性能、安全性、使用环境的 JDK 版本以及优雅性和可读性等方面来综合考虑**。

---

## ConcurrentHashMap

![图片](https://mmbiz.qpic.cn/mmbiz_png/wJvXicD0z2dUfft3IveZlPs9s7gibBiaqK4RdJQrUCMxSEBS7Bc88zpyexBZlO7toXypZtLNM1OSClCoJciag0yh2Q/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)之前的文章关于HashMap已经讲解的很详细了，因此此篇文章会简单介绍思路，再学习**并发HashMap**就简单很多了。我们最终知道`HashMap`是线程不安全的，因此在老版本JDK中提供了`HashTable`来实现多线程级别的，改变之处重要有以下几点。

> ❝
>
> 1. `HashTable`的`put`, `get`,`remove`等方法是通过`synchronized`来修饰保证其线程安全性的。
> 2. `HashTable`是 不允许key跟value为null的。
> 3. 问题是`synchronized`是个关键字级别的重量锁，在get数据的时候任何写入操作都不允许。相对来说性能不好。因此目前主要用的`ConcurrentHashMap`来保证线程安全性。
>
> ❞

`ConcurrentHashMap`主要分为JDK<=7跟JDK>=8的两个版本，`ConcurrentHashMap`的空间利用率更低一般只有10%～20%，接下来分别介绍。

### JDK7

先宏观说下JDK7中的大致组成，ConcurrentHashMap由`Segment`数组结构和`HashEntry`数组组成。Segment是一种可重入锁，是一种数组和链表的结构，一个Segment中包含一个HashEntry数组，每个HashEntry又是一个链表结构。正是通过Segment分段锁，ConcurrentHashMap实现了高效率的并发。缺点是并发程度是有segment数组来决定的，并发度一旦初始化无法扩容。先绘制个`ConcurrentHashMap`的形象直观图。![图片](https://mmbiz.qpic.cn/mmbiz_png/wJvXicD0z2dUfft3IveZlPs9s7gibBiaqK4JxI0N6LicFadficuq9VCE6nTqz0Ud7uDtWRleFk3udAIeSHGfFibOHvOQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)要想理解`currentHashMap`,可以简单的理解为将数据**「分表分库」**。`ConcurrentHashMap`是由 `Segment` 数组 结构和`HashEntry` 数组 结构组成。

> ❝
>
> - Segment 是一种可重入锁`ReentrantLock`的子类 ，在 `ConcurrentHashMap` 里扮演锁的角色，`HashEntry`则用于存储键值对数据。
> - `ConcurrentHashMap` 里包含一个 `Segment` 数组来实现锁分离，`Segment`的结构和 `HashMap` 类似，一个 `Segment`里包含一个 `HashEntry` 数组，每个 `HashEntry` 是一个链表结构的元素， 每个 `Segment`守护者一个 `HashEntry` 数组里的元素，当对 `HashEntry`数组的数据进行修改时，必须首先获得它对应的 `Segment` 锁。
>
> ❞

1. 我们先看下segment类：

```
static final class Segment<K,V> extends ReentrantLock implements Serializable {
     transient volatile HashEntry<K,V>[] table; //包含一个HashMap 可以理解为
}
```

可以理解为我们的每个`segment`都是实现了`Lock`功能的`HashMap`。如果我们同时有多个`segment`形成了`segment`数组那我们就可以实现并发咯。

我们看下`currentHashMap`的构造函数，先总结几点。

1. 1. 每一个segment里面包含的table(HashEntry数组)初始化大小也一定是2的次幂
   2. 这里设置了若干个用于位计算的参数。
   3. initialCapacity：初始容量大小 ，默认16。
   4. loadFactor: 扩容因子，默认0.75，当一个Segment存储的元素数量大于initialCapacity* loadFactor时，该Segment会进行一次扩容。
   5. concurrencyLevel:并发度，默认16。并发度可以理解为程序运行时能够**「同时更新」**ConccurentHashMap且不产生锁竞争的最大线程数，实际上就是ConcurrentHashMap中的**分段锁**个数，即Segment[]的数组长度。如果并发度设置的过小，会带来严重的锁竞争问题；如果并发度设置的过大，原本位于同一个Segment内的访问会扩散到不同的Segment中，CPU cache命中率会下降，从而引起程序性能下降。
   6. segment的数组大小最终一定是2的次幂

构造函数详解：

```java
   //initialCapacity 是我们保存所以KV数据的初始值
   //loadFactor这个就是HashMap的负载因子
   // 我们segment数组的初始化大小
      @SuppressWarnings("unchecked")
       public ConcurrentHashMap(int initialCapacity,
                                float loadFactor, int concurrencyLevel) {
           if (!(loadFactor > 0) || initialCapacity < 0 || concurrencyLevel <= 0)
               throw new IllegalArgumentException();
           if (concurrencyLevel > MAX_SEGMENTS) // 最大允许segment的个数，不能超过 1< 24
               concurrencyLevel = MAX_SEGMENTS;
           int sshift = 0; // 类似扰动函数
           int ssize = 1; 
           while (ssize < concurrencyLevel) {
               ++sshift;
               ssize <<= 1; // 确保segment一定是2次幂
           }
           this.segmentShift = 32 - sshift;  
           //有点类似与扰动函数，跟下面的参数配合使用实现 当前元素落到那个segment上面。
           this.segmentMask = ssize - 1; // 为了 取模 专用
           if (initialCapacity > MAXIMUM_CAPACITY) //不能大于 1< 30
               initialCapacity = MAXIMUM_CAPACITY;
   
           //总的数组大小 被 segment 分散后 需要多少个table
           //即：要保存数据量 / 并发数（segment数量）
           int c = initialCapacity / ssize; 
           if (c * ssize < initialCapacity)
               ++c; //确保向上取值
           int cap = MIN_SEGMENT_TABLE_CAPACITY; 
           // 每个table初始化大小为2
           while (cap < c) // 单独的一个segment[i] 对应的table 容量大小。
               cap <<= 1;	// 将table的容量初始化为2的次幂
           Segment<K,V> s0 =
               new Segment<K,V>(loadFactor, (int)(cap * loadFactor), (HashEntry<K,V>[])new HashEntry[cap]);// 负载因子，阈值，每个segment的初始化大小。跟hashmap 初始值类似。
           
               // 并且segment的初始化是懒加载模式，刚开始只有一个s0，其余的在需要的时候才会增加。
           Segment<K,V>[] ss = (Segment<K,V>[])new Segment[ssize];
           UNSAFE.putOrderedObject(ss, SBASE, s0); // ordered write of segments[0]
           this.segments = ss;
       }
```

1. hash 不管是我们的get操作还是put操作要需要通过hash来对数据进行定位。

```java
   //  整体思想就是通过多次不同方式的位运算来努力将数据均匀的分布到目标table中，都是些扰动函数
   private int hash(Object k) {
       int h = hashSeed;
       if ((0 != h) && (k instanceof String)) {
           return sun.misc.Hashing.stringHash32((String) k);
       }
       h ^= k.hashCode();
       // single-word Wang/Jenkins hash.
       h += (h <<  15) ^ 0xffffcd7d;
       h ^= (h >>> 10);
       h += (h <<   3);
       h ^= (h >>>  6);
       h += (h <<   2) + (h << 14);
       return h ^ (h >>> 16);
   }
```

1. get 相对来说比较简单，无非就是通过`hash`找到对应的`segment`，继续通过`hash`找到对应的`table`,然后就是遍历这个链表看是否可以找到，并且要注意 `get`的时候是没有加锁的。

```java
   public V get(Object key) {
       Segment<K,V> s;
       HashEntry<K,V>[] tab;
       int h = hash(key); // JDK7中标准的hash值获取算法
       long u = (((h >>> segmentShift) & segmentMask) << SSHIFT) + SBASE; // hash值如何映射到对应的segment上
       if ((s = (Segment<K,V>)UNSAFE.getObjectVolatile(segments, u)) != null && (tab = s.table) != null) {
           //  无非就是获得hash值对应的segment 是否存在，
           for (HashEntry<K,V> e = (HashEntry<K,V>) UNSAFE.getObjectVolatile
                    (tab, ((long)(((tab.length - 1) & h)) << TSHIFT) + TBASE);
                e != null; e = e.next) {
               // 看下这个hash值对应的是segment(HashEntry)中的具体位置。然后遍历查询该链表
               K k;
               if ((k = e.key)  key || (e.hash  h && key.equals(k)))
                   return e.value;
           }
       }
       return null;
   }
```

1. put 相同的思路，先找到`hash`值对应的`segment`位置，然后看该`segment`位置是否初始化了(因为segment是懒加载模式)。选择性初始化，最终执行put操作。

```java
   @SuppressWarnings("unchecked")
   public V put(K key, V value) {
       Segment<K,V> s;
       if (value  null)
           throw new NullPointerException();
       int hash = hash(key);// 还是获得最终hash值
       int j = (hash >>> segmentShift) & segmentMask; // hash值位操作对应的segment数组位置
       if ((s = (Segment<K,V>)UNSAFE.getObject          
            (segments, (j << SSHIFT) + SBASE))  null)
           s = ensureSegment(j); 
       // 初始化时候因为只有第一个segment，如果落在了其余的segment中 则需要现初始化。
       return s.put(key, hash, value, false);
       // 直接在数据中执行put操作。
   }
```

其中`put`操作基本思路跟`HashMap`几乎一样，只是在开始跟结束进行了加锁的操作`tryLock and unlock`，然后JDK7中都是先扩容再添加数据的，并且获得不到锁也会进行自旋的tryLock或者lock阻塞排队进行等待(同时获得锁前提前new出新数据)。

```java
final V put(K key, int hash, V value, boolean onlyIfAbsent) {
    // 在往该 segment 写入前，需要先获取该 segment 的独占锁，获取失败尝试获取自旋锁
    HashEntry<K,V> node = tryLock() ? null :
        scanAndLockForPut(key, hash, value);
    V oldValue;
    try {
        // segment 内部的数组
        HashEntry<K,V>[] tab = table;
        // 利用 hash 值，求应该放置的数组下标
        int index = (tab.length - 1) & hash;
        // first 是数组该位置处的链表的表头
        HashEntry<K,V> first = entryAt(tab, index);
 
        for (HashEntry<K,V> e = first;;) {
            if (e != null) {
                K k;
                if ((k = e.key)  key ||
                    (e.hash  hash && key.equals(k))) {
                    oldValue = e.value;
                    if (!onlyIfAbsent) {
                        // 覆盖旧值
                        e.value = value;
                        ++modCount;
                    }
                    break;
                }
                // 继续顺着链表走
                e = e.next;
            }
            else {
                // node 是不是 null，这个要看获取锁的过程。没获得锁的线程帮我们创建好了节点，直接头插法
                // 如果不为 null，那就直接将它设置为链表表头；如果是 null，初始化并设置为链表表头。
                if (node != null)
                    node.setNext(first);
                else
                    node = new HashEntry<K,V>(hash, key, value, first);
 
                int c = count + 1;
                // 如果超过了该 segment 的阈值，这个 segment 需要扩容
                if (c > threshold && tab.length < MAXIMUM_CAPACITY)
                    rehash(node); // 扩容
                else
                    // 没有达到阈值，将 node 放到数组 tab 的 index 位置，
                    // 将新的结点设置成原链表的表头
                    setEntryAt(tab, index, node);
                ++modCount;
                count = c;
                oldValue = null;
                break;
            }
        }
    } finally {
        // 解锁
        unlock();
    }
    return oldValue;
}
```

如果加锁失败了调用`scanAndLockForPut`，完成查找或新建节点的工作。当获取到锁后直接将该节点加入链表即可，**「提升」**了put操作的性能，这里涉及到自旋。大致过程：

> ❝
>
> 1. 在我获取不到锁的时候我进行tryLock,准备好new的数据，同时还有一定的次数限制，还要考虑别的已经获得线程的节点修改该头节点。
>
> ❞

```java
private HashEntry<K,V> scanAndLockForPut(K key, int hash, V value) {
    HashEntry<K,V> first = entryForHash(this, hash);
    HashEntry<K,V> e = first;
    HashEntry<K,V> node = null;
    int retries = -1; // negative while locating node
 
    // 循环获取锁
    while (!tryLock()) {
        HashEntry<K,V> f; // to recheck first below
        if (retries < 0) {
            if (e  null) {
                if (node  null) // speculatively create node
              // 进到这里说明数组该位置的链表是空的，没有任何元素
             // 当然，进到这里的另一个原因是 tryLock() 失败，所以该槽存在并发，不一定是该位置
                    node = new HashEntry<K,V>(hash, key, value, null);
                retries = 0;
            }
            else if (key.equals(e.key))
                retries = 0;
            else
                // 顺着链表往下走
                e = e.next;
        }
    // 重试次数如果超过 MAX_SCAN_RETRIES（单核 1 次多核 64 次），那么不抢了，进入到阻塞队列等待锁
    //    lock() 是阻塞方法，直到获取锁后返回
        else if (++retries > MAX_SCAN_RETRIES) {
            lock();
            break;
        }
        else if ((retries & 1)  0 &&
                 // 进入这里，说明有新的元素进到了链表，并且成为了新的表头
                 // 这边的策略是，重新执行 scanAndLockForPut 方法
                 (f = entryForHash(this, hash)) != first) {
            e = first = f; // re-traverse if entry changed
            retries = -1;
        }
    }
    return node;
}
```

1. Size

   这个size方法比较有趣，他是先无锁的统计下所有的数据量看下前后两次是否数据一样，如果一样则返回数据，如果不一样则要把全部的segment进行加锁，统计，解锁。并且size方法只是返回一个统计性的数字，因此size谨慎使用哦。

```java
public int size() {
       // Try a few times to get accurate count. On failure due to
       // continuous async changes in table, resort to locking.
       final Segment<K,V>[] segments = this.segments;
       int size;
       boolean overflow; // true if size overflows 32 bits
       long sum;         // sum of modCounts
       long last = 0L;   // previous sum
       int retries = -1; // first iteration isn't retry
       try {
           for (;;) {
               if (retries++  RETRIES_BEFORE_LOCK) {  //  超过2次则全部加锁
                   for (int j = 0; j < segments.length; ++j)
                       ensureSegment(j).lock(); // 直接对全部segment加锁消耗性太大
               }
               sum = 0L;
               size = 0;
               overflow = false;
               for (int j = 0; j < segments.length; ++j) {
                   Segment<K,V> seg = segmentAt(segments, j);
                   if (seg != null) {
                       sum += seg.modCount; // 统计的是modCount,涉及到增删该都会加1
                       int c = seg.count;
                       if (c < 0 || (size += c) < 0)
                           overflow = true;
                   }
               }
               if (sum  last) // 每一个前后的修改次数一样 则认为一样，但凡有一个不一样则直接break。
                   break;
               last = sum;
           }
       } finally {
           if (retries > RETRIES_BEFORE_LOCK) {
               for (int j = 0; j < segments.length; ++j)
                   segmentAt(segments, j).unlock();
           }
       }
       return overflow ? Integer.MAX_VALUE : size;
   }
```

1. rehash`segment` 数组初始化后就不可变了，也就是说**「并发性不可变」**，不过`segment`里的`table`可以扩容为2倍，该方法没有考虑并发，因为执行该方法之前已经获取了锁。其中JDK7中的`rehash`思路跟JDK8 中扩容后处理链表的思路一样，个人不过感觉没有8写的精髓好看。

```java
// 方法参数上的 node 是这次扩容后，需要添加到新的数组中的数据。
private void rehash(HashEntry<K,V> node) {
    HashEntry<K,V>[] oldTable = table;
    int oldCapacity = oldTable.length;
    // 2 倍
    int newCapacity = oldCapacity << 1;
    threshold = (int)(newCapacity * loadFactor);
    // 创建新数组
    HashEntry<K,V>[] newTable =
        (HashEntry<K,V>[]) new HashEntry[newCapacity];
    // 新的掩码，如从 16 扩容到 32，那么 sizeMask 为 31，对应二进制 ‘000...00011111’
    int sizeMask = newCapacity - 1;
    // 遍历原数组，将原数组位置 i 处的链表拆分到 新数组位置 i 和 i+oldCap 两个位置
    for (int i = 0; i < oldCapacity ; i++) {
        // e 是链表的第一个元素
        HashEntry<K,V> e = oldTable[i];
        if (e != null) {
            HashEntry<K,V> next = e.next;
            // 计算应该放置在新数组中的位置，
            // 假设原数组长度为 16，e 在 oldTable[3] 处，那么 idx 只可能是 3 或者是 3 + 16 = 19
            int idx = e.hash & sizeMask; // 新位置
            if (next  null)   // 该位置处只有一个元素
                newTable[idx] = e;
            else { // Reuse consecutive sequence at same slot
                // e 是链表表头
                HashEntry<K,V> lastRun = e;
                // idx 是当前链表的头结点 e 的新位置
                int lastIdx = idx;
                // for 循环找到一个 lastRun 结点，这个结点之后的所有元素是将要放到一起的
                for (HashEntry<K,V> last = next;
                     last != null;
                     last = last.next) {
                    int k = last.hash & sizeMask;
                    if (k != lastIdx) {
                        lastIdx = k;
                        lastRun = last;
                    }
                }
                // 将 lastRun 及其之后的所有结点组成的这个链表放到 lastIdx 这个位置
                newTable[lastIdx] = lastRun;
                // 下面的操作是处理 lastRun 之前的结点，
                //这些结点可能分配在另一个链表中，也可能分配到上面的那个链表中
                for (HashEntry<K,V> p = e; p != lastRun; p = p.next) {
                    V v = p.value;
                    int h = p.hash;
                    int k = h & sizeMask;
                    HashEntry<K,V> n = newTable[k];
                    newTable[k] = new HashEntry<K,V>(h, p.key, v, n);
                }
            }
        }
    }
    // 将新来的 node 放到新数组中刚刚的 两个链表之一 的 头部
    int nodeIndex = node.hash & sizeMask; // add the new node
    node.setNext(newTable[nodeIndex]);
    newTable[nodeIndex] = node;
    table = newTable;
}
```

1. CAS操作 在JDK7里在`ConcurrentHashMap`中通过原子操作`sun.misc.Unsafe`查找元素、替换元素和设置元素。通过这样的硬件级别获得数据可以保证即使是多线程我也每次获得的数据是最新的。这些原子操作起着非常关键的作用，你可以在所有`ConcurrentHashMap`的基本功能中看到，随机距离如下：

```java
     final void setNext(HashEntry<K,V> n) {
            UNSAFE.putOrderedObject(this, nextOffset, n);
        }
    static final <K,V> HashEntry<K,V> entryAt(HashEntry<K,V>[] tab, int i) {
        return (tab  null) ? null :
            (HashEntry<K,V>) UNSAFE.getObjectVolatile
            (tab, ((long)i << TSHIFT) + TBASE);
    }
   static final <K,V> void setEntryAt(HashEntry<K,V>[] tab, int i,
                                       HashEntry<K,V> e) {
        UNSAFE.putOrderedObject(tab, ((long)i << TSHIFT) + TBASE, e);
    }
```

#### 常见问题

1. ConcurrentHashMap实现原理是怎么样的或者ConcurrentHashMap如何在保证高并发下线程安全的同时实现了性能提升？

> ❝
>
> `ConcurrentHashMap`允许多个修改操作并发进行，其关键在于使用了锁分离技术。它使用了多个锁来控制对hash表的不同部分进行的修改。内部使用段(`Segment`)来表示这些不同的部分，每个段其实就是一个小的`HashTable`，只要多个修改操作发生在不同的段上，它们就可以并发进行。
>
> ❞

1. 在高并发下的情况下如何保证取得的元素是最新的？

> ❝
>
> 用于存储键值对数据的`HashEntry`，在设计上它的成员变量value跟`next`都是`volatile`类型的，这样就保证别的线程对value值的修改，get方法可以马上看到。
>
> ❞

ConcurrentHashMap的弱一致性体现在迭代器,clear和get方法，原因在于没有加锁。

1. 1. 比如迭代器在遍历数据的时候是一个Segment一个Segment去遍历的，如果在遍历完一个Segment时正好有一个线程在刚遍历完的Segment上插入数据，就会体现出不一致性。clear也是一样。
   2. get方法和containsKey方法都是遍历对应索引位上所有节点，都是不加锁来判断的，如果是修改性质的因为可见性的存在可以直接获得最新值，不过如果是新添加值则无法保持一致性。

### JDK8

JDK8相比与JDK7主要区别如下：

> ❝
>
> 1. 取消了segment数组，直接用table保存数据，锁的粒度更小，减少并发冲突的概率。采用table数组元素作为锁，从而实现了对每一行数据进行加锁，进一步减少并发冲突的概率，并发控制使用Synchronized和CAS来操作。
> 2. 存储数据时采用了数组+ 链表+红黑树的形式。
>
> ❞

1. CurrentHashMap重要参数：

> ❝
>
> private static final int MAXIMUM_CAPACITY = 1 << 30; // 数组的最大值 
>
> private static final int DEFAULT_CAPACITY = 16; // 默认数组长度 
>
> static final int TREEIFY_THRESHOLD = 8; // 链表转红黑树的一个条件 
>
> static final int UNTREEIFY_THRESHOLD = 6; // 红黑树转链表的一个条件 
>
> static final int MIN_TREEIFY_CAPACITY = 64; // 链表转红黑树的另一个条件
>
> static final int MOVED   = -1;  // 表示正在扩容转移 
>
> static final int TREEBIN  = -2; // 表示已经转换成树 
>
> static final int RESERVED  = -3; // hash for transient reservations 
>
> static final int HASH_BITS = 0x7fffffff; // 获得hash值的辅助参数
>
> transient volatile Node<K,V>[] table;// 默认没初始化的数组，用来保存元素 
>
> private transient volatile Node<K,V>[] nextTable; // 转移的时候用的数组 
>
> static final int NCPU = Runtime.getRuntime().availableProcessors();// 获取可用的CPU个数 
>
> private transient volatile Node<K,V>[] nextTable; // 连接表，用于哈希表扩容，扩容完成后会被重置为 null 
>
> private transient volatile long baseCount;保存着整个哈希表中存储的所有的结点的个数总和，有点类似于 HashMap 的 size 属性。
>
> private transient volatile int `sizeCtl`; 
>
> 负数：表示进行初始化或者扩容，-1：表示正在初始化，-N：表示有 N-1 个线程正在进行扩容 正数：0 表示还没有被初始化，> 0的数：初始化或者是下一次进行扩容的阈值，有点类似HashMap中的`threshold`，不过功能**「更强大」**。
>
> ❞

1. 若干重要类

- 构成每个元素的基本类 `Node`

```java
      static class Node<K,V> implements Map.Entry<K,V> {
              final int hash;    // key的hash值
              final K key;       // key
              volatile V val;    // value
              volatile Node<K,V> next; 
               //表示链表中的下一个节点
      }
```

- TreeNode继承于Node，用来存储红黑树节点

```java
      static final class TreeNode<K,V> extends Node<K,V> {
              TreeNode<K,V> parent;  
              // 红黑树的父亲节点
              TreeNode<K,V> left;
              // 左节点
              TreeNode<K,V> right;
             // 右节点
              TreeNode<K,V> prev;    
             // 前节点
              boolean red;
             // 是否为红点
      }
```

- ForwardingNode 在 Node 的子类 `ForwardingNode` 的构造方法中，可以看到此变量的hash = **「-1」** ，类中还存储`nextTable`的引用。该初始化方法只在 `transfer`方法被调用，如果一个类被设置成此种情况并且hash = -1 则说明该节点不需要resize了。

```java
static final class ForwardingNode<K,V> extends Node<K,V> {
        final Node<K,V>[] nextTable;
        ForwardingNode(Node<K,V>[] tab) {
            //注意这里
            super(MOVED, null, null, null);
            this.nextTable = tab;
        }
 //.....
}
```

- TreeBin TreeBin从字面含义中可以理解为存储树形结构的容器，而树形结构就是指TreeNode，所以TreeBin就是封装TreeNode的容器，它提供转换黑红树的一些条件和锁的控制.

```java
static final class TreeBin<K,V> extends Node<K,V> {
        TreeNode<K,V> root;
        volatile TreeNode<K,V> first;
        volatile Thread waiter;
        volatile int lockState;
        // values for lockState
        static final int WRITER = 1; // set while holding write lock
        static final int WAITER = 2; // set when waiting for write lock
        static final int READER = 4; // increment value for setting read lock
}
```

#### 构造函数

整体的构造情况基本跟HashMap类似，并且为了跟原来的JDK7中的兼容性还可以传入并发度。不过JDK8中并发度已经有table的具体长度来控制了。

> ❝
>
> 1. ConcurrentHashMap()：创建一个带有默认初始容量 (16)、加载因子 (0.75) 和 concurrencyLevel (16) 的新的空映射
> 2. ConcurrentHashMap(int)：创建一个带有指定初始容量`tableSizeFor`、默认加载因子 (0.75) 和 concurrencyLevel (16) 的新的空映射
> 3. ConcurrentHashMap(Map<? extends K, ? extends V> m)：构造一个与给定映射具有相同映射关系的新映射
> 4. ConcurrentHashMap(int initialCapacity, float loadFactor)：创建一个带有指定初始容量、加载因子和默认 concurrencyLevel (1) 的新的空映射
> 5. ConcurrentHashMap(int, float, int)：创建一个带有指定初始容量、加载因子和并发级别的新的空映射。
>
> ❞

#### put

假设table已经初始化完成，put操作采用 CAS + synchronized 实现并发插入或更新操作，具体实现如下。

> ❝
>
> 1. 做一些边界处理，然后获得hash值。
> 2. 没初始化就初始化，初始化后看下对应的桶是否为空，为空就原子性的尝试插入。
> 3. 如果当前节点正在扩容还要去帮忙扩容，骚操作。
> 4. 用`syn`来加锁当前节点，然后操作几乎跟就跟hashmap一样了。
>
> ❞

```java
// Node 节点的 hash值在HashMap中存储的就是hash值，在currenthashmap中可能有多种情况哦！
final V putVal(K key, V value, boolean onlyIfAbsent) {
    if (key  null || value  null) throw new NullPointerException(); //边界处理
    int hash = spread(key.hashCode());// 最终hash值计算
    int binCount = 0;
    for (Node<K,V>[] tab = table;;) { //循环表
        Node<K,V> f; int n, i, fh;
        if (tab  null || (n = tab.length)  0)
            tab = initTable(); // 初始化表 如果为空,懒汉式
        else if ((f = tabAt(tab, i = (n - 1) & hash))  null) {
        // 如果对应桶位置为空
            if (casTabAt(tab, i, null, new Node<K,V>(hash, key, value, null))) 
                         // CAS 原子性的尝试插入
                break;
        } 
        else if ((fh = f.hash)  MOVED) 
        // 如果当前节点正在扩容。还要帮着去扩容。
            tab = helpTransfer(tab, f);
        else {
            V oldVal = null;
            synchronized (f) { //  桶存在数据 加锁操作进行处理
                if (tabAt(tab, i)  f) {
                    if (fh >= 0) { // 如果存储的是链表 存储的是节点的hash值
                        binCount = 1;
                        for (Node<K,V> e = f;; ++binCount) {
                            K ek;
                            // 遍历链表去查找，如果找到key一样则选择性
                            if (e.hash  hash &&
                                ((ek = e.key)  key ||
                                 (ek != null && key.equals(ek)))) {
                                oldVal = e.val;
                                if (!onlyIfAbsent)
                                    e.val = value;
                                break;
                            }
                            Node<K,V> pred = e;
                            if ((e = e.next)  null) {// 找到尾部插入
                                pred.next = new Node<K,V>(hash, key,
                                                          value, null);
                                break;
                            }
                        }
                    }
                    else if (f instanceof TreeBin) {// 如果桶节点类型为TreeBin
                        Node<K,V> p;
                        binCount = 2;
                        if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                       value)) != null) { 
                             // 尝试红黑树插入，同时也要防止节点本来就有，选择性覆盖
                            oldVal = p.val;
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                }
            }
            if (binCount != 0) { // 如果链表数量
                if (binCount >= TREEIFY_THRESHOLD)
                    treeifyBin(tab, i); //  链表转红黑树哦！
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    addCount(1L, binCount); // 统计大小 并且检查是否要扩容。
    return null;
}
```

涉及到重要函数`initTable`、`tabAt`、`casTabAt`、`helpTransfer`、`putTreeVal`、`treeifyBin`、`addCount`函数。

#### initTable

**「只允许一个线程」**对表进行初始化，如果不巧有其他线程进来了，那么会让其他线程交出 CPU 等待下次系统调度`Thread.yield`。这样，保证了表同时只会被一个线程初始化，对于table的大小，会根据`sizeCtl`的值进行设置，如果没有设置szieCtl的值，那么默认生成的table大小为16，否则，会根据`sizeCtl`的大小设置table大小。

```java
// 容器初始化 操作
private final Node<K,V>[] initTable() {
    Node<K,V>[] tab; int sc;
    while ((tab = table)  null || tab.length  0) {
        if ((sc = sizeCtl) < 0) // 如果正在初始化-1，-N 正在扩容。
            Thread.yield(); // 进行线程让步等待
     // 让掉当前线程 CPU 的时间片，使正在运行中的线程重新变成就绪状态，并重新竞争 CPU 的调度权。
     // 它可能会获取到，也有可能被其他线程获取到。
        else if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) { 
          //  比较sizeCtl的值与sc是否相等，相等则用 -1 替换,这表明我这个线程在进行初始化了！
            try {
                if ((tab = table)  null || tab.length  0) {
                    int n = (sc > 0) ? sc : DEFAULT_CAPACITY; // 默认为16
                    @SuppressWarnings("unchecked")
                    Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                    table = tab = nt;
                    sc = n - (n >>> 2); // sc = 0.75n
                }
            } finally {
                sizeCtl = sc; //设置sizeCtl 类似threshold
            }
            break;
        }
    }
    return tab;
}
```

#### unsafe

在`ConcurrentHashMap`中使用了`unSafe`方法，通过直接操作内存的方式来保证并发处理的安全性，使用的是硬件的安全机制。

```java
 // 用来返回节点数组的指定位置的节点的原子操作
@SuppressWarnings("unchecked")
static final <K,V> Node<K,V> tabAt(Node<K,V>[] tab, int i) {
    return (Node<K,V>)U.getObjectVolatile(tab, ((long)i << ASHIFT) + ABASE);
}

// cas原子操作，在指定位置设定值
static final <K,V> boolean casTabAt(Node<K,V>[] tab, int i,
                                    Node<K,V> c, Node<K,V> v) {
    return U.compareAndSwapObject(tab, ((long)i << ASHIFT) + ABASE, c, v);
}

// 原子操作，在指定位置设定值
static final <K,V> void setTabAt(Node<K,V>[] tab, int i, Node<K,V> v) {
    U.putObjectVolatile(tab, ((long)i << ASHIFT) + ABASE, v);
}

// 比较table数组下标为i的结点是否为c，若为c，则用v交换操作。否则，不进行交换操作。
static final <K,V> boolean casTabAt(Node<K,V>[] tab, int i,
                                    Node<K,V> c, Node<K,V> v) {
    return U.compareAndSwapObject(tab, ((long)i << ASHIFT) + ABASE, c, v);
}
```

可以看到获得table[i]数据是通过`Unsafe`对象通过反射获取的，取数据直接table[index]不可以么，为什么要这么复杂？在java内存模型中，我们已经知道每个线程都有一个工作内存，里面存储着table的**「副本」**，虽然table是`volatile`修饰的，但不能保证线程每次都拿到table中的最新元素，Unsafe.getObjectVolatile可以`直接获取指定内存的数据`，**「保证了每次拿到数据都是最新的」**。

#### helpTransfer

```java
// 可能有多个线程在同时帮忙运行helpTransfer
final Node<K,V>[] helpTransfer(Node<K,V>[] tab, Node<K,V> f) {
    Node<K,V>[] nextTab; int sc;
    if (tab != null && (f instanceof ForwardingNode) && (nextTab = ((ForwardingNode<K,V>)f).nextTable) != null) {
        // table不是空  且 node节点是转移类型，并且转移类型的nextTable 不是空 说明还在扩容ing
        int rs = resizeStamp(tab.length); 
        // 根据 length 得到一个前16位的标识符，数组容量大小。
        // 确定新table指向没有变，老table数据也没变，并且此时 sizeCtl小于0 还在扩容ing
        while (nextTab  nextTable && table  tab && (sc = sizeCtl) < 0) {
            if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc  rs + 1 || sc  rs + MAX_RESIZERS || transferIndex <= 0)
            // 1. sizeCtl 无符号右移16位获得高16位如果不等 rs 标识符变了
            // 2. 如果扩容结束了 这里可以看 trePresize 函数第一次扩容操作：
            // 默认第一个线程设置 sc = rs 左移 16 位 + 2，当第一个线程结束扩容了，
            // 就会将 sc 减一。这个时候，sc 就等于 rs + 1。
            // 3. 如果达到了最大帮助线程个数 65535个
            // 4. 如果转移下标调整ing 扩容已经结束了
                break;
            if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1)) {
            // 如果以上都不是, 将 sizeCtl + 1,增加一个线程来扩容
                transfer(tab, nextTab); // 进行转移
                break;// 结束循环
            }
        }
        return nextTab;
    }
    return table;
}
```

- Integer.numberOfLeadingZeros(n)

> ❝
>
> 该方法的作用是**「返回无符号整型i的最高非零位前面的0的个数」**，包括符号位在内；如果i为负数，这个方法将会返回0，符号位为1. 比如说，10的二进制表示为 0000 0000 0000 0000 0000 0000 0000 1010 java的整型长度为32位。那么这个方法返回的就是28
>
> ❞

- resizeStamp 主要用来获得标识符，可以简单理解是对当前系统容量大小的一种监控。

```java
static final int resizeStamp(int n) {
   return Integer.numberOfLeadingZeros(n) | (1 << (RESIZE_STAMP_BITS - 1)); 
   //RESIZE_STAMP_BITS = 16
}
```

#### addCount

主要就2件事：一是更新 baseCount，二是判断是否需要扩容。

```java
private final void addCount(long x, int check) {
 CounterCell[] as; long b, s;
 // 首先如果没有并发 此时countCells is null, 此时尝试CAS设置数据值。
 if ((as = counterCells) != null || !U.compareAndSwapLong(this, BASECOUNT, b = baseCount, s = b + x)) {
     // 如果 counterCells不为空以为此时有并发的设置 或者 CAS设置 baseCount 失败了
     CounterCell a; long v; int m;
     boolean uncontended = true;
     if (as  null || (m = as.length - 1) < 0 || (a = as[ThreadLocalRandom.getProbe() & m])  null ||
         !(uncontended = U.compareAndSwapLong(a, CELLVALUE, v = a.value, v + x))) {
         // 1. 如果没出现并发 此时计数盒子为 null
         // 2. 随机取出一个数组位置发现为空
         // 3. 出现并发后修改这个cellvalue 失败了
         // 执行funAddCount
         fullAddCount(x, uncontended);// 死循环操作
         return;
     }
     if (check <= 1)
         return;
     s = sumCount(); // 吧counterCells数组中的每一个数据进行累加给baseCount。
 }
 // 如果需要扩容
 if (check >= 0) {
  Node<K,V>[] tab, nt; int n, sc;
  while (s >= (long)(sc = sizeCtl) && (tab = table) != null && (n = tab.length) < MAXIMUM_CAPACITY) {
   int rs = resizeStamp(n);// 获得高位标识符
   if (sc < 0) { // 是否需要帮忙去扩容
    if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc  rs + 1 ||
     sc  rs + MAX_RESIZERS || (nt = nextTable)  null || transferIndex <= 0)
     break;
    if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
     transfer(tab, nt);
   } // 第一次扩容
   else if (U.compareAndSwapInt(this, SIZECTL, sc, (rs << RESIZE_STAMP_SHIFT) + 2))
    transfer(tab, null);
   s = sumCount();
  }
 }
}
```

1. baseCount添加`ConcurrentHashMap`提供了`baseCount`、`counterCells` 两个辅助变量和一个 `CounterCell`辅助内部类。sumCount() 就是迭代 `counterCells`来统计 sum 的过程。put 操作时，肯定会影响 `size()`，在 `put()` 方法最后会调用 `addCount()`方法。整体的思维方法跟LongAdder类似，用的思维就是借鉴的`ConcurrentHashMap`。每一个`Cell`都用Contended修饰来避免伪共享。

> ❝
>
> 1. JDK1.7 和 JDK1.8 对 size 的计算是不一样的。1.7 中是先不加锁计算三次，如果三次结果不一样在加锁。
> 2. JDK1.8 size 是通过对 baseCount 和 counterCell 进行 CAS 计算，最终通过 baseCount 和 遍历 CounterCell 数组得出 size。
> 3. JDK 8 推荐使用mappingCount 方法，因为这个方法的返回值是 long 类型，不会因为 size 方法是 int 类型限制最大值。
>
> ❞

1. 关于扩容![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)在`addCount`第一次扩容时候会有骚操作`sc=rs << RESIZE_STAMP_SHIFT) + 2)`其中`rs = resizeStamp(n)`。这里需要核心说一点，

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)如果不是第一次扩容则直接将低16位的数字 +1 即可。

#### putTreeVal

这个操作几乎跟`HashMap`的操作完全一样，核心思想就是一定要决定向左还是向右然后最终尝试放置新数据，然后balance。不同点就是有锁的考虑。

#### treeifyBin

这里的基本思路跟`HashMap`几乎一样，不同点就是先变成TreeNode，然后是**「单向链表」**串联。

```java
private final void treeifyBin(Node<K,V>[] tab, int index) {
    Node<K,V> b; int n, sc;
    if (tab != null) {
        //如果整个table的数量小于64，就扩容至原来的一倍，不转红黑树了
        //因为这个阈值扩容可以减少hash冲突，不必要去转红黑树
        if ((n = tab.length) < MIN_TREEIFY_CAPACITY)
            tryPresize(n << 1);
        else if ((b = tabAt(tab, index)) != null && b.hash >= 0) {
            synchronized (b) { //锁定当前桶
                if (tabAt(tab, index)  b) {
                    TreeNode<K,V> hd = null, tl = null;
                    for (Node<K,V> e = b; e != null; e = e.next) {
                        //遍历这个链表然后将每个节点封装成TreeNode，最终单链表串联起来，
                        // 最终 调用setTabAt 放置红黑树
                        TreeNode<K,V> p =
                            new TreeNode<K,V>(e.hash, e.key, e.val,
                                              null, null);
                        if ((p.prev = tl)  null)
                            hd = p;
                        else
                            tl.next = p;
                        tl = p;
                    }
                    //通过TreeBin对象对TreeNode转换成红黑树
                    setTabAt(tab, index, new TreeBin<K,V>(hd));
                }
            }
        }
    }
}
```

#### TreeBin

主要功能就是链表变化为红黑树，这个红黑树用`TreeBin`来包装。并且要注意 转成红黑树以后以前链表的结构信息还是有的，最终信息如下：

1. TreeBin.first = 链表中第一个节点。
2. TreeBin.root = 红黑树中的root节点。

```java
TreeBin(TreeNode<K,V> b) {
            super(TREEBIN, null, null, null);   
            //创建空节点 hash = -2 
            this.first = b;
            TreeNode<K,V> r = null; // root 节点
            for (TreeNode<K,V> x = b, next; x != null; x = next) {
                next = (TreeNode<K,V>)x.next;
                x.left = x.right = null;
                if (r  null) {
                    x.parent = null;
                    x.red = false;
                    r = x; // root 节点设置为x 
                }
                else {
                    K k = x.key;
                    int h = x.hash;
                    Class<?> kc = null;
                    for (TreeNode<K,V> p = r;;) {
                   // x代表的是转换为树之前的顺序遍历到链表的位置的节点，r代表的是根节点
                        int dir, ph;
                        K pk = p.key;
                        if ((ph = p.hash) > h)
                            dir = -1;
                        else if (ph < h)
                            dir = 1;
                        else if ((kc  null &&
                                  (kc = comparableClassFor(k))  null) ||
                                 (dir = compareComparables(kc, k, pk))  0)
                            dir = tieBreakOrder(k, pk);    
                            // 当key不可以比较，或者相等的时候采取的一种排序措施
                            TreeNode<K,V> xp = p;
                        // 放一定是放在叶子节点上，如果还没找到叶子节点则进行循环往下找。
                        // 找到了目前叶子节点才会进入 再放置数据
                        if ((p = (dir <= 0) ? p.left : p.right)  null) {
                            x.parent = xp;
                            if (dir <= 0)
                                xp.left = x;
                            else
                                xp.right = x;
                            r = balanceInsertion(r, x); 
                     // 每次插入一个元素的时候都调用 balanceInsertion 来保持红黑树的平衡
                            break;
                        }
                    }
                }
            }
            this.root = r;
            assert checkInvariants(root);
        }
```

#### tryPresize

当数组长度小于64的时候，扩张数组长度一倍，调用此函数。扩容后容量大小的核对，可能涉及到初始化容器大小。并且扩容的时候又跟2的次幂联系上了！，其中初始化时候传入map会调用putAll方法直接put一个map的话，在**「putAll」**方法中没有调用initTable方法去初始化table，而是直接调用了tryPresize方法，所以这里需要做一个是不是需要初始化table的判断。

PS：默认第一个线程设置 sc = rs 左移 16 位 + 2，当第一个线程结束扩容了，就会将 sc 减一。这个时候，sc 就等于 rs + 1，这个时候说明扩容完毕了。

```java
     /**
     * 扩容表为指可以容纳指定个数的大小（总是2的N次方）
     * 假设原来的数组长度为16，则在调用tryPresize的时候，size参数的值为16<<1(32)，此时sizeCtl的值为12
     * 计算出来c的值为64, 则要扩容到 sizeCtl ≥ c
     *  第一次扩容之后 数组长：32 sizeCtl：24
     *  第三次扩容之后 数组长：128  sizeCtl：96 退出
     */
    private final void tryPresize(int size) {
        int c = (size >= (MAXIMUM_CAPACITY >>> 1)) ? MAXIMUM_CAPACITY :
            tableSizeFor(size + (size >>> 1) + 1); // 合理范围
        int sc;
        while ((sc = sizeCtl) >= 0) {
            Node<K,V>[] tab = table; int n;
                if (tab  null || (n = tab.length)  0) {
                // 初始化传入map，今天putAll会直接调用这个。
                n = (sc > c) ? sc : c;
                if (U.compareAndSwapInt(this, SIZECTL, sc, -1)) {    
                //初始化tab的时候，把 sizeCtl 设为 -1
                    try {
                        if (table  tab) {
                            @SuppressWarnings("unchecked")
                            Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n];
                            table = nt;
                            sc = n - (n >>> 2); // sc=sizeCtl = 0.75n
                        }
                    } finally {
                        sizeCtl = sc;
                    }
                }
            }
             // 初始化时候如果  数组容量<=sizeCtl 或 容量已经最大化了则退出
            else if (c <= sc || n >= MAXIMUM_CAPACITY) {
                    break;//退出扩张
            }
            else if (tab  table) {
                int rs = resizeStamp(n);

                if (sc < 0) { // sc = siztCtl 如果正在扩容Table的话，则帮助扩容
                    Node<K,V>[] nt;
                    if ((sc >>> RESIZE_STAMP_SHIFT) != rs || sc  rs + 1 ||
                        sc  rs + MAX_RESIZERS || (nt = nextTable)  null ||
                        transferIndex <= 0)
                        break; // 各种条件判断是否需要加入扩容工作。
                     // 帮助转移数据的线程数 + 1
                    if (U.compareAndSwapInt(this, SIZECTL, sc, sc + 1))
                        transfer(tab, nt);
                }
                 // 没有在初始化或扩容，则开始扩容
                 // 此处切记第一次扩容 直接 +2 
                else if (U.compareAndSwapInt(this, SIZECTL, sc,
                      (rs << RESIZE_STAMP_SHIFT) + 2)) {
                        transfer(tab, null);
                }
            }
        }
    }
```

#### transfer

这里代码量比较大主要分文三部分，并且感觉思路很精髓，尤其**「是其他线程帮着去扩容的骚操作」**。

1. 主要是 单个线程能处理的最少桶结点个数的计算和一些属性的初始化操作。
2. 每个线程进来会先领取自己的任务区间`[bound,i]`，然后开始 --i 来遍历自己的任务区间，对每个桶进行处理。如果遇到桶的头结点是空的，那么使用 `ForwardingNode`标识旧table中该桶已经被处理完成了。如果遇到已经处理完成的桶，直接跳过进行下一个桶的处理。如果是正常的桶，对桶首节点加锁，正常的迁移即可(跟HashMap第三部分一样思路)，迁移结束后依然会将原表的该位置标识位已经处理。

该函数中的`finish = true` 则说明整张表的迁移操作已经**「全部」**完成了，我们只需要重置 `table`的引用并将 `nextTable` 赋为空即可。否则，`CAS` 式的将 `sizeCtl`减一，表示当前线程已经完成了任务，退出扩容操作。如果退出成功，那么需要进一步判断当前线程是否就是最后一个在执行扩容的。

```java
f ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT)
   return;
```

第一次扩容时在`addCount`中有写到`(resizeStamp(n) << RESIZE_STAMP_SHIFT) + 2` 表示当前只有一个线程正在工作，**「相对应的」**，如果 `(sc - 2) resizeStamp(n) << RESIZE_STAMP_SHIFT`，说明当前线程就是最后一个还在扩容的线程，那么会将 finishing 标识为 true，并在下一次循环中退出扩容方法。

1. 几乎跟`HashMap`大致思路类似的遍历链表/红黑树然后扩容操作。

```java
private final void transfer(Node<K,V>[] tab, Node<K,V>[] nextTab) {
    int n = tab.length, stride;
    if ((stride = (NCPU > 1) ? (n >>> 3) / NCPU : n) < MIN_TRANSFER_STRIDE)    //MIN_TRANSFER_STRIDE 用来控制不要占用太多CPU
        stride = MIN_TRANSFER_STRIDE; // subdivide range    //MIN_TRANSFER_STRIDE=16 每个CPU处理最小长度个数

    if (nextTab  null) { // 新表格为空则直接新建二倍，别的辅助线程来帮忙扩容则不会进入此if条件
        try {
            @SuppressWarnings("unchecked")
            Node<K,V>[] nt = (Node<K,V>[])new Node<?,?>[n << 1];
            nextTab = nt;
        } catch (Throwable ex) {      // try to cope with OOME
            sizeCtl = Integer.MAX_VALUE;
            return;
        }
        nextTable = nextTab;
        transferIndex = n; // transferIndex 指向最后一个桶，方便从后向前遍历
    }
    int nextn = nextTab.length; // 新表长度
    ForwardingNode<K,V> fwd = new ForwardingNode<K,V>(nextTab); // 创建一个fwd节点，这个是用来控制并发的，当一个节点为空或已经被转移之后，就设置为fwd节点
    boolean advance = true;    //是否继续向前查找的标志位
    boolean finishing = false; // to ensure sweep(清扫) before committing nextTab,在完成之前重新在扫描一遍数组，看看有没完成的没
     // 第一部分
    // i 指向当前桶， bound 指向当前线程需要处理的桶结点的区间下限【bound，i】 这样来跟线程划分任务。
    for (int i = 0, bound = 0;;) {
        Node<K,V> f; int fh;
       // 这个 while 循环的目的就是通过 --i 遍历当前线程所分配到的桶结点
       // 一个桶一个桶的处理
        while (advance) {//  每一次成功处理操作都会将advance设置为true，然里来处理区间的上一个数据
            int nextIndex, nextBound;
            if (--i >= bound || finishing) { //通过此处进行任务区间的遍历
                advance = false;
            }
            else if ((nextIndex = transferIndex) <= 0) {
                i = -1;// 任务分配完了
                advance = false;
            }
            // 更新 transferIndex
           // 为当前线程分配任务，处理的桶结点区间为（nextBound,nextIndex）
            else if (U.compareAndSwapInt(this, TRANSFERINDEX, nextIndex,nextBound = (nextIndex > stride ? nextIndex - stride : 0))) {
               // nextIndex本来等于末尾数字，
                bound = nextBound;
                i = nextIndex - 1;
                advance = false;
            }
        }
        // 当前线程所有任务完成 
        if (i < 0 || i >= n || i + n >= nextn) {
            int sc;
            if (finishing) {  // 已经完成转移 则直接赋值操作
                nextTable = null;
                table = nextTab;
                sizeCtl = (n << 1) - (n >>> 1);    //设置sizeCtl为扩容后的0.75
                return;
            }
            if (U.compareAndSwapInt(this, SIZECTL, sc = sizeCtl, sc - 1)) { // sizeCtl-1 表示当前线程任务完成。
                if ((sc - 2) != resizeStamp(n) << RESIZE_STAMP_SHIFT) { 
                // 判断当前线程完成的线程是不是最后一个在扩容的，思路精髓
                        return;
                }
                finishing = advance = true;// 如果是则相应的设置参数
                i = n; 
            }
        }
        else if ((f = tabAt(tab, i))  null) // 数组中把null的元素设置为ForwardingNode节点(hash值为MOVED[-1])
            advance = casTabAt(tab, i, null, fwd); // 如果老节点数据是空的则直接进行CAS设置为fwd
        else if ((fh = f.hash)  MOVED) //已经是个fwd了，因为是多线程操作 可能别人已经给你弄好了，
            advance = true; // already processed
        else {
            synchronized (f) { //加锁操作
                if (tabAt(tab, i)  f) {
                    Node<K,V> ln, hn;
                    if (fh >= 0) { //该节点的hash值大于等于0，说明是一个Node节点
                    // 关于链表的操作整体跟HashMap类似不过 感觉好像更扰一些。
                        int runBit = fh & n; // fh= f.hash first hash的意思，看第一个点 放老位置还是新位置
                        Node<K,V> lastRun = f;

                        for (Node<K,V> p = f.next; p != null; p = p.next) {
                            int b = p.hash & n;    //n的值为扩张前的数组的长度
                            if (b != runBit) {
                                runBit = b;
                                lastRun = p;//最后导致发生变化的节点
                            }
                        }
                        if (runBit  0) { //看最后一个变化点是新还是旧 旧
                            ln = lastRun;
                            hn = null;
                        }
                        else {
                            hn = lastRun; //看最后一个变化点是新还是旧 旧
                            ln = null;
                        }
                        /*
                         * 构造两个链表，顺序大部分和原来是反的,不过顺序也有差异
                         * 分别放到原来的位置和新增加的长度的相同位置(i/n+i)
                         */
                        for (Node<K,V> p = f; p != lastRun; p = p.next) {
                            int ph = p.hash; K pk = p.key; V pv = p.val;
                            if ((ph & n)  0)
                                    /*
                                     * 假设runBit的值为0，
                                     * 则第一次进入这个设置的时候相当于把旧的序列的最后一次发生hash变化的节点(该节点后面可能还有hash计算后同为0的节点)设置到旧的table的第一个hash计算后为0的节点下一个节点
                                     * 并且把自己返回，然后在下次进来的时候把它自己设置为后面节点的下一个节点
                                     */
                                ln = new Node<K,V>(ph, pk, pv, ln);
                            else
                                    /*
                                     * 假设runBit的值不为0，
                                     * 则第一次进入这个设置的时候相当于把旧的序列的最后一次发生hash变化的节点(该节点后面可能还有hash计算后同不为0的节点)设置到旧的table的第一个hash计算后不为0的节点下一个节点
                                     * 并且把自己返回，然后在下次进来的时候把它自己设置为后面节点的下一个节点
                                     */
                                hn = new Node<K,V>(ph, pk, pv, hn);    
                        }
                        setTabAt(nextTab, i, ln);    
                        setTabAt(nextTab, i + n, hn);
                        setTabAt(tab, i, fwd);
                        advance = true;
                    }
                    else if (f instanceof TreeBin) { // 该节点hash值是个负数否则的话是一个树节点
                        TreeBin<K,V> t = (TreeBin<K,V>)f;
                        TreeNode<K,V> lo = null, loTail = null; // 旧 头尾
                        TreeNode<K,V> hi = null, hiTail = null; //新头围
                        int lc = 0, hc = 0;
                        for (Node<K,V> e = t.first; e != null; e = e.next) {
                            int h = e.hash;
                            TreeNode<K,V> p = new TreeNode<K,V>
                                (h, e.key, e.val, null, null);
                            if ((h & n)  0) {
                                if ((p.prev = loTail)  null)
                                    lo = p;
                                else
                                    loTail.next = p; //旧头尾设置
                                loTail = p;
                                ++lc;
                            }
                            else { // 新头围设置
                                if ((p.prev = hiTail)  null)
                                    hi = p;
                                else
                                    hiTail.next = p;
                                hiTail = p;
                                ++hc;
                            }
                        }
                         //ln  如果老位置数字<=6 则要对老位置链表进行红黑树降级到链表，否则就看是否还需要对老位置数据进行新建红黑树
                        ln = (lc <= UNTREEIFY_THRESHOLD) ? untreeify(lo) :
                            (hc != 0) ? new TreeBin<K,V>(lo) : t;
                        hn = (hc <= UNTREEIFY_THRESHOLD) ? untreeify(hi) :
                            (lc != 0) ? new TreeBin<K,V>(hi) : t;
                        setTabAt(nextTab, i, ln);
                        setTabAt(nextTab, i + n, hn);
                        setTabAt(tab, i, fwd); //老表中i位置节点设置下
                        advance = true;
                    }
                }
            }
        }
    }
}
```

#### get

这个就很简单了，获得hash值，然后判断存在与否，遍历链表即可，注意get没有任何锁操作！

```java
    public V get(Object key) {
        Node<K,V>[] tab; Node<K,V> e, p; int n, eh; K ek;
        // 计算key的hash值
        int h = spread(key.hashCode()); 
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (e = tabAt(tab, (n - 1) & h)) != null) { // 表不为空并且表的长度大于0并且key所在的桶不为空
            if ((eh = e.hash)  h) { // 表中的元素的hash值与key的hash值相等
                if ((ek = e.key)  key || (ek != null && key.equals(ek))) // 键相等
                    // 返回值
                    return e.val;
            }
            else if (eh < 0) // 是个TreeBin hash = -2 
                // 在红黑树中查找,因为红黑树中也保存这一个链表顺序
                return (p = e.find(h, key)) != null ? p.val : null;
            while ((e = e.next) != null) { // 对于结点hash值大于0的情况链表
                if (e.hash  h &&
                    ((ek = e.key)  key || (ek != null && key.equals(ek))))
                    return e.val;
            }
        }
        return null;
    }
```

#### clear

关于清空也相对简单 ，无非就是遍历桶数组，然后通过CAS来置空。

```java
public void clear() {
    long delta = 0L;
    int i = 0;
    Node<K,V>[] tab = table;
    while (tab != null && i < tab.length) {
        int fh;
        Node<K,V> f = tabAt(tab, i);
        if (f  null)
            ++i; //这个桶是空的直接跳过
        else if ((fh = f.hash)  MOVED) { // 这个桶的数据还在扩容中，要去扩容同时等待。
            tab = helpTransfer(tab, f);
            i = 0; // restart
        }
        else {
            synchronized (f) { // 真正的删除
                if (tabAt(tab, i)  f) {
                    Node<K,V> p = (fh >= 0 ? f :(f instanceof TreeBin) ?((TreeBin<K,V>)f).first : null);
                        //循环到链表/红黑树的尾部
                        while (p != null) {
                            --delta; // 记录删除了多少个
                            p = p.next;
                        } 
                        //利用CAS无锁置null  
                        setTabAt(tab, i++, null);
                    }
                }
            }
        }
        if (delta != 0L)
            addCount(delta, -1); //调整count
    }
```

### end

ConcurrentHashMap是如果来做到**「并发安全」**，又是如何做到**「高效」**的并发的呢？

1. 首先是读操作，读源码发现get方法中根本没有使用同步机制，也没有使用`unsafe`方法(因此不能保证读一致性，主要体现在新插入数据），所以读操作是支持并发操作的。
2. 写操作

- . 数据扩容函数是`transfer`，该方法的只有`addCount`，`helpTransfer`和`tryPresize`这三个方法来调用。

> > 1. addCount是在当对数组进行操作，使得数组中存储的元素个数发生了变化的时候会调用的方法。
> > 2. `helpTransfer`是在当一个线程要对table中元素进行操作的时候，如果检测到节点的·hash·= MOVED 的时候，就会调用`helpTransfer`方法，在`helpTransfer`中再调用`transfer`方法来帮助完成数组的扩容
> >
> > > ❝
> > >
> > > 1. `tryPresize`是在`treeIfybin`和`putAll`方法中调用，`treeIfybin`主要是在`put`添加元素完之后，判断该数组节点相关元素是不是已经超过8个的时候，如果超过则会调用这个方法来扩容数组或者把链表转为树。注意`putAll`在初始化传入一个大map的时候会调用。·
> > >
> > > ❞

总结扩容情况发生：

> ❝
>
> 1. 在往map中添加元素的时候，在某一个节点的数目已经超过了8个，同时数组的长度又小于64的时候，才会触发数组的扩容。
> 2. 当数组中元素达到了sizeCtl的数量的时候，则会调用transfer方法来进行扩容
>
> ❞

\3. 扩容时候是否可以进行读写。

> ❝
>
> 对于读操作，因为是没有加锁的所以可以的. 对于写操作，JDK8中已经将锁的范围细腻到`table[i]`l了，当在进行数组扩容的时候，如果当前节点还没有被处理（也就是说还没有设置为fwd节点)，那就可以进行设置操作。如果该节点已经被处理了，则当前线程也会加入到扩容的操作中去。
>
> ❞

1. 多个线程又是如何同步处理的 在`ConcurrentHashMap`中，同步处理主要是通过`Synchronized`和`unsafe`的硬件级别原子性 这两种方式来完成的。

> ❝
>
> 1. 在取得sizeCtl跟某个位置的Node的时候，使用的都是`unsafe`的方法，来达到并发安全的目的
> 2. 当需要在某个位置设置节点的时候，则会通过`Synchronized`的同步机制来锁定该位置的节点。
> 3. 在数组扩容的时候，则通过处理的`步长`和`fwd`节点来达到并发安全的目的，通过设置hash值为MOVED=-1。
> 4. 当把某个位置的节点复制到扩张后的table的时候，也通过`Synchronized`的同步机制来保证线程安全
>
> ❞

### 套路

> ❝
>
> 1. 谈谈你理解的 HashMap，讲讲其中的 get put 过程。
> 2. 1.8 做了什么优化？
> 3. 是线程安全的嘛？
> 4. 不安全会导致哪些问题？
> 5. 如何解决？有没有线程安全的并发容器？
> 6. ConcurrentHashMap 是如何实现的？1.7、1.8 实现有何不同，为什么这么做。
> 7. 1.8中ConcurrentHashMap的sizeCtl作用，大致说下协助扩容跟标志位。
> 8. HashMap 为什么不用跳表替换红黑树呢？



---

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



---

## 判空处理

 **代码炸了！** 

前一段时间，项目紧急迭代，临时加入了一个新功能，具体功能就不描述了，反正就是业务功能：用户通过浏览器在系统界面上操作，然后Java后台代码做一些数据的查询、计算和整合的工作，并对第三方提供了操作接口。

当晚凌晨上线，本系统内测试，完美通过！

第二天将接口对外提供，供第三方系统调用，**duang**！工单立马来了

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzoXwSibGTJU4hicvbCmVxiclAhcuSxsuIzGMkfSAE09jERoGa4OrXI0ibpMOicJs8VEoxQfL9hf0EMwGmQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

很明显，后台代码炸了！拉了一下后台日志，原来又是烦人的空指针异常 `NullPointerException` ！

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzoXwSibGTJU4hicvbCmVxiclAhSgzUjwW4L5qODGFbqXKoVeV0Ny9jkbvWdoksXAOdy3xLPtm6cp1gjQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)



为此，本文痛定思痛，关于 `null`空指针异常问题的预防和解决，详细整理成文，并严格反思：**我们到底在代码中应该如何防止空指针异常所导致的Bug？**

------

 **最常见的输入判空** 

对输入判空非常有必要，并且常见，举个栗子：

```java
public String addStudent( Student student ) {   
    // ...
}
```

无论如何，你在进行函数内部业务代码编写之前一定会对传入的 `student`对象本身以及每个字段进行判空或校验：

```java
public String addStudent( Student student ) {
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

为了避免**人肉手写**这种繁杂的输入判空，我们最起码可以用两种方式来进行**优雅的规避**：

**方法一：**借助Spring框架本身的注解  `@NotNull`，可参考我的前文《[啥？听说你还在手写复杂的参数校验？](http://mp.weixin.qq.com/s?__biz=MzU4ODI1MjA3NQ==&mid=2247484955&idx=1&sn=29b0f228415abd8542d07c898e6398ba&chksm=fdded0dfcaa959c9d9fc9d04130409c392460561bdce53996261fe8973b81f4811a8ebe5c357&scene=21#wechat_redirect)》【点击可跳转】

**方法二：**借助Lombok工具的注解 `@NonNull`，可参考我的前文《[Lombok，嗯，真香香香香香香！](http://mp.weixin.qq.com/s?__biz=MzU4ODI1MjA3NQ==&mid=2247485020&idx=1&sn=3c3405807f96a597398f513c48bb094e&chksm=fdded098caa9598e6159bd7315180e149e6a789a3ee75d42b4acaaae96669504e08378eeddae&scene=21#wechat_redirect)》【点击可跳转】

------

 **手动空指针保护** 

手动进行 `if(obj!=null)`的判空自然是最全能的，也是最可靠的，但是怕就怕**俄罗斯套娃式**的 `if`判空。

举例一种情况：

为了获取：`省(Province)→市(Ctiy)→区(District)→街道(Street)→道路名(Name)`

作为一个**“严谨且良心”**的后端开发工程师，如果手动地进行空指针保护，我们难免会这样写：

```java
public String getStreetName( Province province ) {    
    if( province != null ) {        
        City city = province.getCity();        
        if( city != null ) {            
            District district = city.getDistrict();            
            if( district != null ) {                
                Street street = district.getStreet();                
                if( street != null ) {                    
                    return street.getName();               
                }            
            }        
        }    
    }    
    return "未找到该道路名";
}
```

为了获取到链条最终端的目的值，直接**链式取值**必定有问题，因为中间只要某一个环节的对象为 `null`，则代码一定会炸，并且抛出 `NullPointerException`异常，然而俄罗斯套娃式的 `if`判空实在有点心累。

------

 **消除俄罗斯套娃式判空** 

在我的前文《[以后要是再写for循环，我就捶自己](http://mp.weixin.qq.com/s?__biz=MzU4ODI1MjA3NQ==&mid=2247484985&idx=1&sn=29209d6f17297c27ef84a2ecb2efcdda&chksm=fdded0fdcaa959ebda04393482f9302a368dec578040bede7ad03e23c86c94b09224df6f7fa6&scene=21#wechat_redirect)》(点击可跳转) 里已经提及过，我们也可以利用Java的函数式编程接口 `Optional`来进行优雅的判空！

`Optional`接口本质是个容器，你可以将你可能为 `null`的变量交由它进行托管，这样我们就不用显式对原变量进行 `null`值检测，防止出现各种空指针异常。

Optional语法专治上面的**俄罗斯套娃式 `if` 判空**，因此上面的代码可以重构如下：

```java
public String getStreetName( Province province ) {    
    return Optional.ofNullable( province )            
        .map( i -> i.getCity() )            
        .map( i -> i.getDistrict() )            
        .map( i -> i.getStreet() )            
        .map( i -> i.getName() )            
        .orElse( "未找到该道路名" );
}
```

漂亮！嵌套的 `if/else`判空灰飞烟灭！

**解释一下执行过程：**

- `ofNullable(province )` ：它以一种智能包装的方式来构造一个 `Optional`实例， `province`是否为 `null`均可以。如果为 `null`，返回一个单例空 `Optional`对象；如果非 `null`，则返回一个 `Optional`包装对象
- `map(xxx )`：该函数主要做值的转换，如果上一步的值非 `null`，则调用括号里的具体方法进行值的转化；反之则直接返回上一步中的单例 `Optional`包装对象
- `orElse(xxx )`：很好理解，在上面某一个步骤的值转换终止时进行调用，给出一个最终的默认值

当然实际代码中倒很少有这种极端情况，不过普通的 `if(obj!=null)`判空也可以用 `Optional`语法进行改写，比如很常见的一种代码：

```java
List<User> userList = userMapper.queryUserList( userType );
if( userList != null ) {
    //此处免不了对userList进行判空  
    for( User user : userList ) {    
    // ...    
    // 对user对象进行操作    
    // ...  
	}
}
```

如果用 `Optional`接口进行改造，可以写为：

```java
List<User> userList = userMapper.queryUserList( userType );
Optional.ofNullable( userList ).ifPresent(  list -> {    
    for( User user : list ) {      
        // ...      
        // 对user对象进行操作      
        // ...    
    }  
})
```

这里的 `ifPresent()`的含义很明显：仅在前面的 `userList`值不为 `null`时，才做下面其余的操作。

------

 **只是一颗语法糖** 

没有用过 `Optional`语法的小伙伴们肯定感觉上面的写法**非常甜蜜**！然而褪去华丽的外衣，甜蜜的 `Optional`语法底层依然是朴素的语言级写法，比如我们看一下 `Optional`的 `ifPresent()`函数源码，就是普通的 `if`判断而已：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzoXwSibGTJU4hicvbCmVxiclAhKibibCiceTXYcymiaibIakwlDdukRMqhKPxwTuFKJEIjDAOULvK4jl9wuibQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

那就有人问：**我们何必多此一举**，做这样一件无聊的事情呢？

其实不然！

用 `Optional`来包装一个可能为 `null`值的变量，其最大意义其实仅仅在于给了调用者一个**明确的警示**！

怎么理解呢？

比如你写了一个函数，输入学生学号 `studentId`，给出学生的得分 ：

- 
- 
- 

```java
Score getScore( Long studentId ) {  
    // ...
}
```

调用者在调用你的方法时，一旦忘记 `if(score!=null)`判空，那么他的代码肯定是有一定 `bug`几率的。

但如果你用 `Optional`接口对函数的返回值进行了包裹：

- 
- 
- 

```java
Optional<Score> getScore( Long studentId ) {  
    // ...
}
```

这样当调用者调用这个函数时，他可以清清楚楚地看到 `getScore()`这个函数的返回值的特殊性（有可能为 `null`），这样一个警示一定会很大几率上帮助调用者规避 `null`指针异常。

------

 **老项目该怎么办？** 

上面所述的 `Optional`语法只是在 `JDK1.8`版本后才开始引入，那还在用 `JDK1.8`版本之前的老项目怎么办呢？

没关系！

`Google`大名鼎鼎的 `Guava`库中早就提供了 `Optional`接口来帮助优雅地处理 `null`对象问题，其本质也是在可能为 `null`的对象上做了一层封装，使用起来和JDK本身提供的 `Optional`接口没有太大区别。

你只需要在你的项目里引入 `Google`的 `Guava`库：

```xml
<dependency>    
    <groupId>com.google.guava</groupId>    
    <artifactId>guava</artifactId>
</dependency>
```

即可享受到和 `Java8`版本开始提供的 `Optional`一样的待遇！





---

## lombok

### **Lombok到底是什么？** 

先看一下它的官网

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzp6uHcdh3OGoN5lib8qiahVv1BEukCAUrrmjp1iay5HDcRCMlTJCwyWhnpehDYY15Dicfw4EPhR4VSbcA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

英语懒得看？没关系。

**它大致意思是：**Lombok是一个很牛批的**插件**（本质是个**Java库**），项目里一旦引入了Lombok神器之后，你项目中所有诸如：对象的构造函数、 `equals()`方法，属性的 `get()/set()`方法等等，这些没有技术含量的代码统统都不用写了，Lombok帮你搞定一切，全部帮你自动生成！

------

 **项目中引入Lombok** 

首先在项目的 `pom.xml`中引入 `Lombok`依赖：

```xml
<dependency>    
    <groupId>org.projectlombok</groupId>    
    <artifactId>lombok</artifactId>
</dependency>
```

除此之外，还要在IDE中安装Lombok插件，并配置：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzp6uHcdh3OGoN5lib8qiahVv1rgb30E3H5wCrNwtCA2pGkfjryVLFsOx7eJHJs3ogaFXU8nybM4Fiacw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

接下来进行代码实验。

------

 **爽！get/set不用写了** 

以前我们写一个 `POJO`对象时，比如定义一个课程 `Course`，需要自己手动写上每个字段的 `get()`和 `set()`方法，就像这样：

```java
public class Course {
    private Long id;        // 课程ID
    private String name;    // 课程名称
    private Integer score;  // 课程成绩
    // 自己手写下面的 get/set 方法！    
    public Long getId() {        return id;    }
    public void setId(Long id) {        this.id = id;    }
    public String getName() {        return name;    }
    public void setName(String name) {        this.name = name;    }
    public Integer getScore() {        return score;    }
    public void setScore(Integer score) {        this.score = score;    }}
```

但是借助于Lombok，一切都变得优雅起来，代码篇幅省了一半

```java
public class Course {
    @Getter    
    @Setter    
    private Long id;        // 课程ID
    @Getter    
    @Setter    
    private String name;    // 课程名称
    @Getter    
    @Setter    
    private Integer score;  // 课程成绩
}
```

两个注解：`@Getter`和 `@Setter`即可方便搞定。

------

 **爽！new对象变得优雅了** 

在没有用Lombok之前，我们假如想new一个对象，我们往往会这么做（以上面的 `Course`类为例）：

```java
Course course = new Course();  // 首先new一个对象
// 然后逐步去装填对象的各个字段
course.setId( 123l );     
course.setName( "高等数学" );
course.setScore( 100 );
```

引进Lombok之后，我们只需要在 `Course`类上用上 `@Builder`注解：

```java
@Builder
public class Course {    
    private Long id;        // 课程ID    
    private String name;    // 课程名称    
    private Integer score;  // 课程成绩
}
```

则 `Course`类对象的创建即可使用 **链式表达** 的方式**一行代码**完成：

```java
// 对象可以链式构造，一句话完成 ！
Course course = Course.builder().id(123l).name("高等数学").score(100).build();
```

看到这里，如果你学过**设计模式**中的 **“建造者模式”** 的话，一定能猜到 `@Builder`注解就是一个典型的“建造者模式”的实现案例！

建造者模式的链式调用用起来实在是很爽！

------

 **爽！构造函数不用写了** 

### **一、全参构造器不用写了**

当你在你的类上使用了Lombok的注解 `AllArgsConstructor`时：

```java
@AllArgsConstructorpublic 
class Course {
    private Long id;        // 课程ID
    private String name;    // 课程名称
    private Integer score;  // 课程成绩
}
```

这时候你的类在编译后会自动生成一个无参构造函数，就像这样：

```java
public class Course {
    private Long id;        // 课程ID
    private String name;    // 课程名称
    private Integer score;  // 课程成绩
    // Lombok自动会帮你生成一个全参构造器！！    
    public Course( Long id, String name, Integer score ) {        
        this.id = id;        
        this.name = name;        
        this.score = score;    
    }
}
```

### **二、无参数构造器也不用写了**

当你在你的类上使用了Lombok的注解 `NoArgsConstructor`时：

```java
@NoArgsConstructorpublic 
class Course {
    private Long id;        // 课程ID
    private String name;    // 课程名称
    private Integer score;  // 课程成绩}
```

这时候你的类在编译后会自动生成一个无参构造函数，就像这样：

```java
public class Course {
    private Long id;        // 课程ID
    private String name;    // 课程名称
    private Integer score;  // 课程成绩
    // Lombok自动会帮你生成一个无参构造器！！    
    public Course() {    }
}
```

### **三、部分参数构造器也不用写了**

当你在你的类上使用了Lombok的注解 `RequiredArgsConstructor`时：

```java
@RequiredArgsConstructorpublic 
class Course {
    private Long id;        // 课程ID
    private final String name;    // 课程名称
    private Integer score;  // 课程成绩
}
```

这时候你的类在编译后会自动生成一个具备部分参数的构造函数，就像这样：

```java
public class Course {
    private Long id;        // 课程ID
    private final String name;    // 课程名称
    private Integer score;  // 课程成绩
    // 因为name字段定义成final，所以Lombok自动会帮你生成一个部分参数的构造器！！    public Course(String name) {        this.name = name;    }}
```

因为 `name`字段定义成 `final`，所以 `Lombok`自动会帮你生成一个部分参数的构造器！！

------

###  **null判空不用写了** 

Lombok的 `@NonNull`注解可以自动帮我们**避免空指针判断**。该注解作用在方法参数上，用于自动生成空值参数检查，比如：

```java
public static void output( String input ) {    
    // 作为一个严谨且良心的Java开发工程师    
    // 一般我们要手动对入参进行校验，就像下面这样 ！    
    if( input == null ) {        
        System.out.println("该函数输入参数为空");    
    }    
    System.out.println( input );
}
```

但是有了Lombok之后，事情就变得简单了，一个注解搞定：

```java
public static void output( @NonNull String input ) {    
    // 原先这里对 input 的判空不用手动做了    
    System.out.println( input );
}
```

------

###  **收尾工作处理** 

什么意思呢？

假如我们要读取一个 `txt`文本文件，一般会这样写代码：

```java
BufferedReader br = null;try {    
    FileReader fileReader = new FileReader("呵呵.tet"); // 定义文件    
    br = new BufferedReader( fileReader ); // 读取文件    
    System.out.println( br.readLine() ); // 读取文件一行内容
} catch (Exception e) {    
    e.printStackTrace();
} finally {    
    try {        
        br.close(); // 无论如何文件句柄在使用结束后得手动关闭！！   
    } catch (IOException e) {        
        e.printStackTrace();    
    }
}
```

注意，这个文件句柄在使用完成之后是一定要手动 `close`的，否则就有可能**资源泄漏**。

有了Lombok之后，这种擦屁股活儿统统不用干了，一个 `@Cleanup`注解即可搞定

```java
@Cleanup BufferedReader br = null;  // 一个 @Cleanup注解搞定！
try {    
    FileReader fileReader = new FileReader("呵呵.tet");    
    br = new BufferedReader( fileReader );    
    System.out.println( br.readLine() );
} catch (Exception e) {    
    e.printStackTrace();
}
```

------

###  **异常不用捕捉了（慎用）** 

比如我们打开一个 `txt`文本文件：

```java
public void openTxt( String filename ) {    
    try {        
        FileReader fileReader = new FileReader( filename );    
    } catch (FileNotFoundException e) {        
        e.printStackTrace();    
    }
}
```

这地方的 `FileNotFoundException`异常，要么显式地在函数级抛出，要么就像上面一样 `try/catch`内部消灭。

如果在编码时，不想处理这种繁杂的异常处理，你可以使用Lombok的 `@SneakyThrows`注解进行简化，比如上面的代码就可以简化为：

```java
@SneakyThrowspublic 
void openTxt( String filename ) {    
    FileReader fileReader = new FileReader( filename );
}
```

这样你编码时就无需处理异常了。

必须**友情提示**的是：这样写你是爽了，但你很有可能会被队友**一顿暴打**，因为别人调用你编写的函数，并不能显式的获知需要处理哪些异常，这样容易留坑！

---

上面列举了几个平时项目开发中使用非常频繁的Lombok注解，除此之外，还有诸如像：

- `@ToString`：为类自动生成toString()方法
- `@EqualsAndHashCode`：为类自动生成hashCode和equals实现
- `@Log`：为类自动生成log日志记录
- `@Synchronized`：为类方法或实例方法自动生成synchronized保护

---



## List使用踩坑

List 可谓是我们经常使用的集合类之一，几乎所有业务代码都离不开 List。既然天天在用，那就没准就会踩中这几个 List 常见坑。

今天我们就来总结这些常见的坑在哪里，捞自己一手，防止后续同学再继续踩坑。

本文设计知识点如下：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGnmEXwq4Y7r7iavrsmKUqQ9LpzOX21e9ZavIwiaeic66ZZZTv89AxwCwDDQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)List 踩坑大全

### ArrayList 这是李逵，还是李鬼？

以前实习的时候，写过这样一段简单代码，通过 `Arrays#asList` 将数组转化为 List 集合。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGnuGCnsKBJWo7FE6VFrw9MOqpkgSSJo5CE0miaX9giaHzT71tMk4prXVnw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这段代码表面看起来没有任何问题，编译也能通过，但是真正测试运行的时候将会在第 4 行抛出  `UnsupportedOperationException`。

刚开始很不解，`Arrays#asList` 返回明明也是一个 `ArrayList`，为什么添加一个元素就会报错？这以后还能好好新增元素吗？

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGnwEWXU5528TtJaFyQAJPANXeWNOjicPshcqVBZBCibQjfqvib9fTU3BrCA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

最后通过 Debug 才发现这个`Arrays#asList` 返回的 `ArrayList` 其实是个**李鬼**，仅仅只是 Arrays 一个内部类，并非真正的 `java.util.ArrayList`。



通过 IDEA，生成这两个的类图，如下：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGnX3FHLYjUUDpj1D4eRqBA3GZquGEtnlG6EzH2wrKyuxyyoohfZoLU8A/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

从上图我们发现，`add/remove` 等方法实际都来自 `AbstractList`，而 `java.util.Arrays$ArrayList` 并没有重写父类的方法。而父类方法恰恰都会抛出 `UnsupportedOperationException`。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGn1xniabvwRAaqIia136vY0hxCNNQNoN00p6BLwKdXicgwgc2WticZKcPubg/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这就是为什么这个李鬼  `ArrayList` 不支持的增删的实际原因。

### 你用你的新 List，为什么却还互相影响

李鬼 `ArrayList` 除了不支持增删操作这个坑以外，还存在另外一个大坑，改动内部元素将会同步影响原数组。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGnzSZdIDLblFflbUCtVkTibiaciaDfVXiad62hMRF7WfPhHz65fmq7aUGLIw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

输出结果：

```java
arrays:[modify_1, modify_2, 3]
list:[modify_1, modify_2, 3]
```

从日志输出可以看到，不管我们是修改原数组，还是新 List 集合，两者都会互相影响。

查看 `java.util.Arrays$ArrayList` 实现，我们可以发现底层实际使用了原始数组。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGnxR5JuWyyg4NfMmsH9CibJa0QCdR3kRnaKZxwMBonHzicOYjln6gV2u8g/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

知道了实际原因，修复的办法也很简单，套娃一层 `ArrayList` 呗！

```java
List<String> list = new ArrayList<>(Arrays.asList(arrays));
```

不过这么写感觉十分繁琐，推荐使用 **Guava Lists** 提供的方法。

```java
List<String> list = Lists.newArrayList(arrays);
```

通过上面两种方式，我们将新的 List 集合与原始数组解耦，不再互相影响，同时由于此时还是真正的 `ArrayList`，不用担心 `add/remove`报错了。

除了 `Arrays#asList`产生新集合与原始数组互相影响之外，JDK 另一个方法 `List#subList` 生成新集合也会与原始 `List` 互相影响。

我们来看一个例子：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGnOtUSfSibqnFXR7Ml2XB3zhGauKDR7FIlE9vkO9YUPAdWNT1fEYQib5ew/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

日志输出结果：

```
integerList:[10, 20, 3]
subList:[10, 20]
```

查看  `List#subList` 实现方式，可以发现这个 SubList 内部有一个 `parent` 字段保存保存最原始 List 。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGnf7Ro2CWjecr5rACAIdTunbAShbdd7YJQxUiborvosz0Gg4UHXDLv3rA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

所有外部读写动作看起来是在操作 `SubList` ，实际上底层动作却都发生在原始 List 中，比如 `add` 方法：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGnzLYE0tww78RCibtJmc2j6xOelg8mTrNE3sXLRoYWCz2Onz9f9hUS75g/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

另外由于 `SubList` 实际上还在引用原始 List，业务开发中，如果不注意，很可能产生 **OOM** 问题。

> 以下例子来自于极客时间：**Java业务开发常见错误100例**

```java
private static List<List<Integer>> data = new ArrayList<>();

private static void oom() {
    for (int i = 0; i < 1000; i++) {
        List<Integer> rawList = IntStream.rangeClosed(1, 100000).boxed().collect(Collectors.toList());
        data.add(rawList.subList(0, 1));
    }
}
```

`data` 看起来最终保存的只是 1000 个具有 1 个元素的 List，不会占用很大空间。但是程序很快就会 **OOM**。

**OOM** 的原因正是因为每个 SubList 都强引用个一个 10 万个元素的原始 List，导致 GC 无法回收。

这里修复的办法也很简单，跟上面一样，也来个套娃呗，加一层 `ArrayList` 。



### 不可变集合，说好不变，你怎么就变了

为了防止 List 集合被误操作，我们可以使用 `Collections#unmodifiableList` 生成一个不可变（**immutable**）集合，进行防御性编程。

这个不可变集合只能被读取，不能做任何修改，包括增加，删除，修改，从而保护不可变集合的安全。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGnLc4ibbRpickF8Wy0YboO69WGNS05zQMKOq8krNV5Ubfw3dWfPbmMTaIQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

上面最后三行写操作都将会抛出 `UnsupportedOperationException` 异常

但是你以为这样就安全了吗？

如果有谁不小心改动原始 List，你就会发现这个不可变集合，竟然就变了。。。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGnkI7nwDiaKlHpX8EIQGjqzdXvKCnlpLiaf2WMus3M7urRZ6icP8NIzpqVw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

上面单元测试结果将会全部通过，这就代表 `Collections#unmodifiableList` 产生不可变集合将会被原始 List 所影响。

查看  `Collections#unmodifiableList` 底层实现方法：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGnvAL1QBDMsPcC4zicIcyhewGy8rKQeZhV83U5y879h4FHF2ic7K1yLRYg/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

可以看到这跟上面 `SubList` 其实是同一个问题，新集合底层实际使用了**原始 List**。

由于不可变集合所有修改操作都会报错，所以不可变集合不会产生任何改动，所以并不影响的原始集合。但是防过来，却不行，原始 List 随时都有可能被改动，从而影响不可变集合。

可以使用如下两种方式防止上卖弄的情况。

**使用 JDK9 List#of 方法。**

```java
List<String> list = new ArrayList<>(Arrays.asList("one", "two", "three"));
List<String> unmodifiableList = List.of(list.toArray(new String[]{}));
```

**使用 Guava immutable list**

```java
List<String> list = new ArrayList<>(Arrays.asList("one", "two", "three"));
List<String> unmodifiableList = ImmutableList.copyOf(list);
```

​		**maven引入guava ：**

```xml
<dependency>
	<groupId>com.google.guava</groupId>
    <artifactId>guava</artifactId>
    <version>16.0.1</version>
</dependency>
```

相比而言 Guava 方式比较清爽，使用也比较简单，推荐使用 Guava 这种方式生成不可变集合。



### foreach 增加/删除元素大坑

先来看一段代码：

```java
String[] arrays = {"1", "2", "3"};
List<String> list = new ArrayList<>(Arrays.asList(arrays));
for (String str : list) {
    if (str.equals("1")) {
        list.remove(str);
    }
}
```

上面的代码我们使用 `foreach` 方式遍历 List 集合，如果符合条件，将会从集合中删除改元素。

这个程序编译正常，但是运行时，程序将会发生异常，日志如下：

```java
java.util.ConcurrentModificationException
	at java.base/java.util.ArrayList$Itr.checkForComodification(ArrayList.java:939)
	at java.base/java.util.ArrayList$Itr.next(ArrayList.java:893)
```

可以看到程序最终错误是由 `ArrayList$Itr.next` 处的代码抛出，但是代码中我们并没有调用该方法，为什么会这样?

实际是因为 `foreach` 这种方式实际上 Java 给我们提供的一种语法糖，编译之后将会变为另一种方式。

我们将上面的代码产生 class 文件反编来看下最后代码长的啥样。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGnGYXWAkJhhFmibPKDNEES04KjkUrdjaibiaEBcyE7ibO0uo57zAgdpNhlrA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

可以看到 `foreach` 这种方式实际就是 `Iterator` 迭代器实现方式，这就是为什么 `foreach` 被遍历的类需要实现 `Iterator`接口的原因。

接着我们来看下抛出异常方法：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGnGO9WgbeuLKrYnyesPdNlql0VTohiajE8t3jxGbPby9OluiboVHQOcO3A/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

`expectedModCount` 来源于 `list#iterator` 方法：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGnNAll2UvibGNIyy33oWJvYhrgEDMLD6qC0j9e7ExopcVicCHjL6YouNVQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

也就是说刚开始遍历循环的时候 `expectedModCount==modCount`，下面我们来看下 `modCount`。

`modCount` 来源于 `ArrayList` 的父类  `AbstractList`，可以用来记录 List 集合被修改的次数。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGnAEyEgaDqKCnz1Vka7QepPlDNyvCkiccMCic5nYr41NBL8LedQY3Wv54w/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

`ArrayList#remove` 之后将会使 `modCount` 加一，`expectedModCount`与 `modCount`将会不相等，这就导致迭代器遍历时将会抛错。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGnA0cHia4VswMYFN4xRSUE06TLtHictQOne7ZPE12ZzKwgR9lHLNPcpj5w/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

> `modCount` 计数操作将会交子类自己操作，`ArrayList` 每次修改操作（增、删）都会使 `modCount` 加 1。但是如 `CopyOnWriteArrayList` 并不会使用 `modCount` 计数。
>
> 所以 `CopyOnWriteArrayList` 使用 `foreach` 删除是安全的，但是还是建议使用如下两种删除元素，统一操作。

修复的办法有两种：

**使用 Iterator#remove 删除元素**

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGntCT6iarelKKpaymalOao7ggcpVfsKYJYevXGgFYVQnQTFBFEGSOib4UA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)iterator

**JDK1.8 List#removeIf**

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGndklciaJq8n16x4tzauVCtuZQtJicmJuiawVRLjYFibwziaDqNY0gibadu5Ew/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

推荐使用 JDK1.8 这种方式，简洁明了。

**思考**

如果我将上面 `foreach` 代码判断条件简单修改一下：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq7lsCNncLAFicib819rONCZGnMEXJa3NohrRviaxb1HAXIH5Jm4x9YibwRFejW4Sq4GkNV2EvaUWxzJJQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

运行这段代码，可以发现这段代码又不会报错了，有没有很意外？

> 疑问：移除list倒数第二个数时不会抛出并发修改异常，这是为啥？



### 总结

第一，我们不要先入为主，想当然就认为 `Arrays.asList` 和 `List.subList` 就是一个普通，独立的 `ArrayList`。

如果没办法，使用了 `Arrays.asList` 和 `List.subList` ，返回给其他方法的时候，一定要记得再套娃一层真正的 `java.util.ArrayList`。

第二 JDK 的提供的不可变集合实际非常笨重，并且低效，还不安全，所以推荐使用 Guava 不可变集合代替。

最后，切记，不要随便在 `foreach`增加/删除元素。



## Map使用踩坑

[链接](https://mp.weixin.qq.com/s/D21mcfI4cxNf4D9ukjESog)

本文设计知识点如下：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq48iaHicnSmkn6ibeFENhECwUnE9bTlcpn2SVLicFNylXL8UB26JjM0E1kJoWZZcJuIwowkFl9rLaoA6Q/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

### 不是所有的 Map 都能包含  null

这个踩坑经历还是发生在实习的时候，那时候有这样一段业务代码，功能很简单，从 XML 中读取相关配置，存入 Map 中。

代码示例如下：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq48iaHicnSmkn6ibeFENhECwUnRa2icdozcHpgFALFDfoP45lPQGyW1mP8O82GYX79vUZRgeVMCH3UBVA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

> SAXReader 所需jar包导入

```xml
	  <dependency>
          <groupId>dom4j</groupId>
          <artifactId>dom4j</artifactId>
          <version>1.1</version>
      </dependency>
      <dependency>
          <groupId>jaxen</groupId>
          <artifactId>jaxen</artifactId>
          <version>1.2.0</version>
      </dependency>
```



那时候正好有个小需求，需要改动一下这段业务代码。改动的过程中，突然想到 `HashMap` 并发过程可能导致死锁的问题。

于是改动了一下这段代码，将 `HashMap` 修改成了 `ConcurrentHashMap`。

美滋滋提交了代码，然后当天上线的时候，就发现炸了。。。

应用启动过程发生 **NPE** 问题，导致应用启动失败。



根据异常日志，很快就定位到了问题原因。由于 XML 某一项配置问题，导致读取元素为 null，然后元素置入到 `ConcurrentHashMap` 中，抛出了空指针异常。

这不科学啊！之前 `HashMap` 都没问题，都可以存在 **null**，为什么它老弟 `ConcurrentHashMap` 就不可以？

翻阅了一下 `ConcurrentHashMap#put` 方法的源码，开头就看到了对 KV 的判空校验。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq48iaHicnSmkn6ibeFENhECwUnpic6jhguF1LEviamSBiaTHz0h2LkZSrKzlQgTU9LiaV2dcrMSlpVQLKX4Q/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

看到这里，不知道你有没有疑惑，为什么 `ConcurrentHashMap` 与 `HashMap` 设计的判断逻辑不一样？

求助了下万能的 Google，找到 **Doug Lea** 老爷子的回答：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq48iaHicnSmkn6ibeFENhECwUnVORXrv4CYQrQMx00UDLhwm26WskA65vFh1VvBALrxlYFbRQr7QiaeWQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)来源:http://cs.oswego.edu/pipermail/concurrency-interest/2006-May/002485.html

总结一下：

- null 会引起歧义，如果 value 为 null，我们无法得知是值为 null，还是 key 未映射具体值？
- **Doug Lea** 并不喜欢 null，认为 null 就是个隐藏的炸弹。

上面提到 **Josh Bloch** 正是 `HashMap` 作者，他与 **Doug Lea** 在 null 问题意见并不一致。

也许正是因为这些原因，从而导致 `ConcurrentHashMap` 与 `HashMap` 对于 null 处理并不一样。

最后贴一下常用 Map 子类集合对于 null 存储情况：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq48iaHicnSmkn6ibeFENhECwUnzMKpicXddHzxFrC4XGWK6sb4ltrhCRn5nKhZSMTu9fAmhJoupqJeZ2Q/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

`解决：`上面的实现类约束，都太不一样，有点不好记忆。其实只要我们在加入元素之前，主动去做空指针判断，不要在 Map 中存入 null，就可以从容避免上面问题。



### 自定义对象为 key

先来看个简单的例子，我们自定义一个 `Goods` 商品类，将其作为 Key 存在 Map 中。

示例代码如下：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq48iaHicnSmkn6ibeFENhECwUnlBDcAg1dQTnDI9ibg2wTUaPVTzk7wUW8ibMUmhledicXOhuoA6FA3KrqQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

上面代码中，第二次我们加入一个相同的商品，原本我们期望新加入的值将会替换原来旧值。但是实际上这里并没有替换成功，反而又加入一对键值。

翻看一下 `HashMap#put` 的源码：

> 以下代码基于 JDK1.7

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq48iaHicnSmkn6ibeFENhECwUnj2yjzVVoN1iacXW4QdbTpDj1vcDKlianCjEic4hcJWdpVia99noElMsyMw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这里首先判断 `hashCode` 计算产生的 hash，如果相等，再判断 `equals` 的结果。但是由于 `Goods`对象未重写的`hashCode` 与 `equals` 方法，默认情况下 `hashCode` 将会使用父类对象 Object 方法逻辑。

而 `Object#hashCode` 是一个 **native** 方法，默认将会为每一个对象生成不同 **hashcode**（**与内存地址有关**），这就导致上面的情况。

所以如果需要使用自定义对象做为 Map 集合的 key，那么一定记得**重写**`hashCode` 与 `equals` 方法。

然后当你为自定义对象重写上面两个方法，接下去又可能踩坑另外一个坑。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq48iaHicnSmkn6ibeFENhECwUnt9iaJCO9OF4OSFGVzJXiabCQVDnPXobQ4WX3ZuMPCh7DawnshL6Gp7icg/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

> 使用 lombok 的 `EqualsAndHashCode` 自动重写 `hashCode` 与 `equals` 方法。

上面的代码中，当 Map 中置入自定义对象后，接着修改了商品金额。然后当我们想根据同一个对象取出 Map 中存的值时，却发现取不出来了。

上面的问题主要是因为 `get` 方法是根据对象 的 hashcode 计算产生的 hash 值取定位内部存储位置。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq48iaHicnSmkn6ibeFENhECwUn8aEsvlibtiapBHibzv11h1icpXGahSvbTmdRORNhuwXAOyTKOWuMOdSNLg/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

当我们修改了金额字段后，导致 `Goods` 对象 hashcode 产生的了变化，从而导致 get 方法无法获取到值。

通过上面两种情况，可以看到使用自定义对象作为 Map 集合 key，还是挺容易踩坑的。

所以尽量避免使用自定义对象作为 Map 集合 key，如果一定要使用，记得重写 `hashCode` 与 `equals` 方法。另外还要保证这是一个`不可变对象`，即对象创建之后，无法再修改里面字段值。

>  不可变类优缺点

- 构造、测试和使用都很简单
- 线程安全且没有同步问题，不需要担心数据会被其它线程修改
- 当用作类的属性时不需要保护性拷贝
- 可以很好的用作Map键值和Set元素
- 不可变对象最大的缺点就是创建对象的开销，因为每一步操作都会产生一个新的对象。



>  编写不可变类

- 确保类不能被继承 - 将类声明为final, 或者使用静态工厂并声明构造器为private
- 声明属性为private 和 final
- 不要提供任何可以修改对象状态的方法 - 不仅仅是set方法, 还有任何其它可以改变状态的方法
- 如果类有任何可变对象属性, 那么当它们在类和类的调用者间传递的时候必须被保护性拷贝



### 错用 ConcurrentHashMap 导致线程不安全

我们都知道 `HashMap` 其实是一个**线程不安全**的容器，多线程环境为了线程安全，我们需要使用 `ConcurrentHashMap`代替。

但是不要认为使用了 `ConcurrentHashMap` 一定就能保证线程安全，在某些错误的使用场景下，依然会造成线程不安全。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq48iaHicnSmkn6ibeFENhECwUnvSjKNjH4kpW5caJRrtibHqn3tS7TQG72f2UR2vibKYsKszO4Q5CAFic7g/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

上面示例代码，我们原本期望输出 **1001**，但是运行几次，得到结果都是小于 **1001**。

深入分析这个问题原因，实际上是因为第一步与第二步是一个组合逻辑，不是一个原子操作。

`ConcurrentHashMap` 只能保证这两步单的操作是个原子操作，线程安全。但是并不能保证两个组合逻辑线程安全，很有可能 A 线程刚通过 get 方法取到值，还未来得及加 1，线程发生了切换，B 线程也进来取到同样的值。

这个问题同样也发生在其他线程安全的容器，比如 `Vector`等。

上面的问题解决办法也很简单，加锁就可以解决，不过这样就会使性能大打折扣，所以不太推荐。

我们可以使用 `AtomicInteger` 解决以上的问题。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq48iaHicnSmkn6ibeFENhECwUnsltXV6IYbnIxS3W1pbwARvvI6iarLVYLmSg3NOHMIicYsjaHekta4eibw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

> 代码

```java
/*ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<>();
        map.put("key",0);
        ExecutorService pool = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 1000; i++) {
            pool.execute(()->{
                int value = map.get("key") + 1; // 第一步，值+1
                map.put("key",value);   // 第二部，重新设值
            });
        }
        Thread.sleep(1000);
        System.out.println(map.get("key"));
        pool.shutdown();*/
        /**
         * 期望输出 1000，但是运行几次，得到结果都是小于 1000。
         * 原因：因为第一步与第二步是一个组合逻辑，不是一个原子操作。
         *
         * 解决：
         *      1、第一二步代码加锁
         *      2、使用AtomicInteger
         */
        ConcurrentHashMap<String, AtomicInteger> map = new ConcurrentHashMap<>();
//        map.put("key",new AtomicInteger(0));
        ExecutorService pool = Executors.newFixedThreadPool(100);
        for (int i = 0; i < 1000; i++) {
            pool.execute(()->{
//                AtomicInteger atomicInteger = map.get("key");// 第一步，获取值
//                atomicInteger.incrementAndGet();    // 第二部，值+1
//                map.put("key",atomicInteger);   // 第二部，重新设值

                // 如果key不存在，新建 AtomicInteger 对象，否则内部 AtomicInteger 递增；
                map.computeIfAbsent("key",s -> new AtomicInteger(0)).incrementAndGet();
            });
        }
        Thread.sleep(1000);
        System.out.println(map.get("key"));
        pool.shutdown();
```



### List 集合这些坑，Map 中也有

[List踩坑那篇文章](http://mp.weixin.qq.com/s?__biz=MzU4ODI1MjA3NQ==&mid=2247485709&idx=2&sn=58bdfb8fb73d95f75041215de10450ba&chksm=fddedfc9caa956dfd727d57e01454465c1d3c091fdff193a98b31770f2151a74869ede05e263&scene=21#wechat_redirect)中我们提过，`Arrays#asList` 与 `List#subList` 返回 List 将会与原集合互相影响，且可能并不支持 `add` 等方法。同样的，这些坑爹的特性在 Map 中也存在，一不小心，将会再次掉坑。



Map 接口除了支持增删改查功能以外，还有三个特有的方法，能返回所有 key，返回所有的 value，返回所有 kv 键值对。

```java
// 返回 key 的 set 视图
Set<K> keySet()；
// 返回所有 value   Collection 视图
Collection<V> values();
// 返回 key-value 的 set 视图
Set<Map.Entry<K, V>> entrySet();
```

这三个方法创建返回新集合，底层其实都依赖的原有 Map 中数据，所以一旦 Map 中元素变动，就会同步影响返回的集合。

另外这三个方法返回新集合，是不支持的新增以及修改操作的，但是却支持 `clear、remove` 等操作。

示例代码如下：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/LEFcpfxrbq48iaHicnSmkn6ibeFENhECwUnBC9Iv9iauteVibxWtVJsdp9e6Xx4TJnGkKOuIY2pPibLjkiaicO34ANc4bA/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

所以如果需要对外返回 Map 这三个方法产生的集合，建议再来个套娃。

```java
new ArrayList<>(map.values());
```

最后再简单提一下，使用 `foreach` 方式遍历新增/删除 Map 中元素，也将会和 List 集合一样，抛出 `ConcurrentModificationException`。



### 总结

一、从上面文章可以看到不管是 List 提供的方法返回集合，还是 Map 中方法返回集合，底层实际还是使用原有集合的元素，这就导致两者将会被互相影响。所以如果需要对外返回，请使用套娃大法，这样让别人用的也安心。

二、 Map 各个实现类对于 null 的约束都不太一样，这里建议在 Map 中加入元素之前，主动进行空指针判断，提前发现问题。

三、慎用自定义对象作为 Map 中的 key，如果需要使用，一定要重写 `hashCode` 与 `equals` 方法，并且还要保证这是个`不可变对象`。

四、`ConcurrentHashMap` 是线程安全的容器，但是不要思维定势，不要片面认为使用 `ConcurrentHashMap` 就会线程安全。

---

# 多线程和并发

## ThreadLocal

 **说在前面** 

`ThreadLocal`用来提供线程级别变量，变量只对当前线程可见。相比与“使用锁控制共享变量访问顺序”的解决方案。`ThreadLocal`通过**空间换时间**的方案，规避了竞争问题，因为每个线程都有属于自己的变量。

此时就产生了第一个问题：**线程如何维护属于自己的变量副本**，搞懂了这个也就搞懂了其原理。



`使用场景`：就是当我们只想在本身的线程内使用的变量，可以用 ThreadLocal 来实现，并且这些变量是和线程的生命周期密切相关的，线程结束，变量也就销毁了。

所以说 ThreadLocal 不是为了解决线程间的共享变量问题的，如果是多线程都需要访问的数据，那需要用全局变量加同步机制。

------

###  **源码分析** 

#### **一、首先看下Thread类**

```java
public class Thread implements Runnable {
    /* ThreadLocal values pertaining to this thread. This map is maintained     
    * by the ThreadLocal class. 
    */  
    ThreadLocal.ThreadLocalMap threadLocals = null;
}
```

Thread`中有一个 `threadLocals` 属性表示线程的本地变量。这个属性的类型是 `ThreadLocal.ThreadLocalMap



#### **二、ThreadLocalMap是啥？**

`ThreadLocalMap`是 `ThreadLocal`的内部类，他是类 `Map`结构，也是存储 `K-V`结构数据，并用 `Entry`封装 `K-V`。不同的是 `ThreadLocalMap`的 `Entry`的 `Key`只能是 `ThreadLocal`类型对象，并且是一个弱引用。

```java
       static class Entry extends WeakReference<ThreadLocal<?>> {            
           /** The value associated with this ThreadLocal. */            
           Object value;
            
           Entry(ThreadLocal<?> k, Object v) {                
               super(k);                
               value = v;            
           }        
       }
```

也就是说线程通过一个类Map数据结构 `ThreadLocal.ThreadLocalMap` 来存储属于自己的线程变量。

```java
ThreadLocal.ThreadLocalMap 何时初始化？
ThreadLocal.ThreadLocalMap 如何存取值？
```



#### **三、ThreadLocal本尊**

`ThreadLocalMap`赋值、取值操作的入口在其外部类 `ThreadLocal`中。

`set(v)`方法内调用 `ThreadLocalMap` 的 `set(this,v)`方法存值。（类似 `Map`的 `put(k,v)`方法）

```java
public void set(T value) {    
    //1. 获取当前线程实例对象    
    Thread t = Thread.currentThread();    
    //2. 通过当前线程实例获取到ThreadLocalMap对象    
    ThreadLocalMap map = getMap(t);    
    if (map != null)        
        //3. 如果Map不为null,则以当前threadLocl实例为key,值为value进行存入        
        map.set(this, value);    
    else        
        //4.map为null,则新建ThreadLocalMap并存入value        
        createMap(t, value);
}

ThreadLocalMap getMap(Thread t) {    
    return t.threadLocals;
}

void createMap(Thread t, T firstValue) {    
    t.threadLocals = new ThreadLocalMap(this, firstValue);
}
```

`get()`方法内调用 `ThreadLocalMap`的 `getEntry(this)`方法取值（类似 `Map`的 `get(k)`方法）

```java
public T get() {    
    Thread t = Thread.currentThread();    
    ThreadLocalMap map = getMap(t);    
    if (map != null) {        
        ThreadLocalMap.Entry e = map.getEntry(this);        
        if (e != null) {            
            @SuppressWarnings("unchecked")            
            T result = (T)e.value;            
            return result;        
        }    
    }    
    return setInitialValue();
}

private T setInitialValue() {    
    T value = initialValue();    
    Thread t = Thread.currentThread();    
    ThreadLocalMap map = getMap(t);    
    if (map != null)        
        map.set(this, value);    
    else        
        createMap(t, value);    
    return value;
}

protected T initialValue() {    
    return null;
}
```

通过代码可以看出:

- 第一次操作线程的 `ThreadLocalMap`属性时，会初始化一个 `ThreadLocal.ThreadLocalMap`， `set(v)`会存入以参数为 `Value`的 `K/V`数据， `get()`会存入以 `null`为 `value`的 `K/V`数据。
- `ThreadLocal.ThreadLocalMap` 存值操作入口是 `ThreadLocal.set(v)`方法，并以当前 `ThreadLocal`变量为 `key`，参数为 `value`。
- `ThreadLocal.ThreadLocalMap` 取值操作入口是 `ThreadLocal.get(v)`方法， `key`为当前ThreadLocal变量。

我们在从代码层面直观的体会这个操作：

```java
ThreadLocal threadLocal1 = new ThreadLocal();
//如果第一次给线程赋值，此处类似
// Map map = new HashMap(); map.put(threadLocal1,"变量第一次赋值")
threadLocal1.set("变量第一次赋值");//类似map.put(threadLocal1,"变量第一次赋值")
threadLocal1.set("变量第二次赋值");//类似map.put(threadLocal1,"变量第一次赋值")
System.out.println(threadLocal1.get());//类似map.get(threadLocal1)
threadLocal1.remove();
输出：变量第二次赋值
```

至此线程的本地变量的本质就清晰了。就是 `Thread`用类似 `Map`的 `ThreadLocal.ThreadLocalMap`数据结构来存储以 `ThreadLocal`类型的变量为 `Key`的数值，并用 `ThreadLocal`来存取删，操作 `ThreadLocalMap`。

- 当我们定义一个 `ThreadLocal`变量时，其实就是在定义一个 `Key`
- 当我们调用 `set(v)`方法时，就是以当前 `ThreadLocal`变量为 `key`，传入参数为 `value`，向 `ThreadLocal.ThreadLocalMap`存数据
- 当我们调用 `get()`方法时，就是以当前 `ThreadLocal`变量为 `key`，从 `ThreadLocal.ThreadLocalMap`取对应的数据

- 使用 ThreadLocal 的时候，最好不要声明为静态的；
- 使用完 ThreadLocal ，最好手动调用 remove() 方法，不然可能出现内存泄漏问题，而且会影响业务逻辑；

------

###  **扩  展** 

#### **一、ThreadLocalMap的Hash冲突解决办法**

采用**线性探测**的方式，根据 `key`计算 `hash`值，如果出现冲突，则向后探测，当到哈希表末尾的时候再从0开始，直到找到一个合适的位置。

这种算法也决定了 `ThreadLocalMap`不适合存储大量数据。

#### **二、ThreadLocalMap的扩容问题**

`ThreadLocalMap`初始大小为 `16`，加载因子为 `2/3`，当 `size`大于 `threshold`时，就会进行扩容。

扩容时，新建一个大小为原来数组长度的**两倍**的数组，然后遍历旧数组中的 `entry`并将其插入到新的hash数组中，在扩容的时候，会把 `key`为 `null`的 `Entry`的 `value`值设置为 `null`，以便内存回收，减少内存泄漏问题。

---

## JMM

### 为什么要有内存模型？

要想回答这个问题，我们需要先弄懂传统计算机硬件内存架构。

#### 硬件内存架构

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoeBothhI5TKLic6t7l9o8jG75e8rgJ4RCK8cpCa3VCicCVmibgjiaiaDszibBRzb4UGZgK9Zfdc4y1EdoA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**（1）CPU**

去过机房的同学都知道，一般在大型服务器上会配置多个CPU，每个CPU还会有多个`核`，这就意味着多个CPU或者多个核可以同时（并发）工作。如果使用Java 起了一个多线程的任务，很有可能每个 CPU 都会跑一个线程，那么你的任务在某一刻就是真正并发执行了。

**（2）CPU Register**

CPU Register也就是 CPU 寄存器。CPU 寄存器是 CPU 内部集成的，在寄存器上执行操作的效率要比在主存上高出几个数量级。

**（3）CPU Cache Memory**

CPU Cache Memory也就是 CPU 高速缓存，相对于寄存器来说，通常也可以称为 L2 二级缓存。相对于硬盘读取速度来说内存读取的效率非常高，但是与 CPU 还是相差数量级，所以在 CPU 和主存间引入了多级缓存，目的是为了做一下缓冲。

**（4）Main Memory**

Main Memory 就是主存，主存比 L1、L2 缓存要大很多。

注意：部分高端机器还有 L3 三级缓存。

#### 缓存一致性问题

由于主存与 CPU 处理器的运算能力之间有数量级的差距，所以在传统计算机内存架构中会引入高速缓存来作为主存和处理器之间的缓冲，CPU 将常用的数据放在高速缓存中，运算结束后 CPU 再讲运算结果同步到主存中。

使用高速缓存解决了 CPU 和主存速率不匹配的问题，但同时又引入另外一个新问题：缓存一致性问题。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoeBothhI5TKLic6t7l9o8jGYib0OVtNySicwoicw15kPCnJPhAhI5gib6pibmrf08ENnvpWstvGXrru8tw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

在多CPU的系统中(或者单CPU多核的系统)，每个CPU内核都有自己的高速缓存，它们共享同一主内存(Main Memory)。当多个CPU的运算任务都涉及同一块主内存区域时，CPU 会将数据读取到缓存中进行运算，这可能会导致各自的缓存数据不一致。

因此需要每个 CPU 访问缓存时遵循一定的协议，在读写数据时根据协议进行操作，共同来维护缓存的一致性。这类协议有 MSI、MESI、MOSI、和 Dragon Protocol 等。

#### 处理器优化和指令重排序

为了提升性能在 CPU 和主内存之间增加了高速缓存，但在多线程并发场景可能会遇到`缓存一致性问题`。那还有没有办法进一步提升 CPU 的执行效率呢？答案是：处理器优化。

> 为了使处理器内部的运算单元能够最大化被充分利用，处理器会对输入代码进行乱序执行处理，这就是处理器优化。

除了处理器会对代码进行优化处理，很多现代编程语言的编译器也会做类似的优化，比如像 Java 的即时编译器（JIT）会做指令重排序。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoeBothhI5TKLic6t7l9o8jGxH3tXq3XSCeC2J6giaKE8kicmeq75YqSAj7VpuRhp2H9bVy9YzCIOQzQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

> 处理器优化其实也是重排序的一种类型，这里总结一下，重排序可以分为三种类型：
>
> - 编译器优化的重排序。编译器在不改变单线程程序语义放入前提下，可以重新安排语句的执行顺序。
> - 指令级并行的重排序。现代处理器采用了指令级并行技术来将多条指令重叠执行。如果不存在数据依赖性，处理器可以改变语句对应机器指令的执行顺序。
> - 内存系统的重排序。由于处理器使用缓存和读写缓冲区，这使得加载和存储操作看上去可能是在乱序执行。



疑问：`volatile 禁止指令重排，同时也会禁止处理器优化中的重排序吗？`

答：不会，处理器优化是原子性问题，处理器优化本质上是将不存在数据依赖性的指令重叠执行（并发），加锁可以禁止处理器优化。如：synchronized。 volatile 禁止的是编译器中的重排序（通过加内存屏障）



### 并发编程的问题

上面讲了一堆硬件相关的东西，有些同学可能会有点懵，绕了这么大圈，这些东西跟 Java 内存模型有啥关系吗？不要急咱们慢慢往下看。

熟悉 Java 并发的同学肯定对这三个问题很熟悉：『**可见性问题**』、『**原子性问题**』、『**有序性问题**』。如果从更深层次看这三个问题，其实就是上面讲的『缓存一致性』、『处理器优化』、『指令重排序』造成的。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoeBothhI5TKLic6t7l9o8jGCPjWA9V5WWEvQD71s4l7aiaAeJ7nU2ic2TJJ2HfGvuUuhXNZNuwWIvrA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

缓存一致性问题其实就是可见性问题，处理器优化可能会造成原子性问题，指令重排序会造成有序性问题，看看这样是不是都联系上了。

出了问题总是要解决的，那有什么办法呢？首先想到简单粗暴的办法，干掉缓存让 CPU 直接与主内存交互就解决了可见性问题，禁止处理器优化和指令重排序就解决了原子性和有序性问题，但这样一夜回到解放前了，显然不可取。

所以技术前辈们想到了在物理机器上定义出一套内存模型， 规范内存的读写操作。内存模型解决并发问题主要采用两种方式：`限制处理器优化`和`使用内存屏障`。

### Java 内存模型

同一套内存模型规范，不同语言在实现上可能会有些差别。接下来着重讲一下 Java 内存模型实现原理。

#### Java 运行时内存区域与硬件内存的关系

了解过 JVM 的同学都知道，JVM 运行时内存区域是分片的，分为栈、堆等，其实这些都是 JVM 定义的逻辑概念。在传统的硬件内存架构中是没有栈和堆这种概念。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoeBothhI5TKLic6t7l9o8jG2lCdPdkQOpFypCjCXfaqnChvsJLV9Ww6FJB3Vkia15csQYClcVMDajQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

从图中可以看出栈和堆既存在于高速缓存中又存在于主内存中，所以两者并没有很直接的关系。

#### Java 线程与主内存的关系

Java 内存模型是一种规范，定义了很多东西：

- 所有的变量都存储在主内存（Main Memory）中。
- 每个线程都有一个私有的本地内存（Local Memory），本地内存中存储了该线程以读/写共享变量的拷贝副本。
- 线程对变量的所有操作都必须在本地内存中进行，而不能直接读写主内存。
- 不同的线程之间无法直接访问对方本地内存中的变量。

看文字太枯燥了，来张图：

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoeBothhI5TKLic6t7l9o8jGfiaKGmHML4iaAMgP8OGrEIs2VfIBJk8aQ6dwpQzXqf3zcibQWxEibDo76g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 线程间通信

如果两个线程都对一个共享变量进行操作，共享变量初始值为 1，每个线程都变量进行加 1，预期共享变量的值为 3。在 JMM 规范下会有一系列的操作。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoeBothhI5TKLic6t7l9o8jGoaFDDFDVJCib20GPP6BwHBibkgvF1RCbiaeTEiawwND3mvufe7MbqEWa0A/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

为了更好的控制主内存和本地内存的交互，Java 内存模型定义了八种操作来实现：

- lock：锁定。作用于主内存的变量，把一个变量标识为一条线程独占状态。
- unlock：解锁。作用于主内存变量，把一个处于锁定状态的变量释放出来，释放后的变量才可以被其他线程锁定。
- read：读取。作用于主内存变量，把一个变量值从主内存传输到线程的工作内存中，以便随后的load动作使用
- load：载入。作用于工作内存的变量，它把read操作从主内存中得到的变量值放入工作内存的变量副本中。
- use：使用。作用于工作内存的变量，把工作内存中的一个变量值传递给执行引擎，每当虚拟机遇到一个需要使用变量的值的字节码指令时将会执行这个操作。
- assign：赋值。作用于工作内存的变量，它把一个从执行引擎接收到的值赋值给工作内存的变量，每当虚拟机遇到一个给变量赋值的字节码指令时执行这个操作。
- store：存储。作用于工作内存的变量，把工作内存中的一个变量的值传送到主内存中，以便随后的write的操作。
- write：写入。作用于主内存的变量，它把store操作从工作内存中一个变量的值传送到主内存的变量中。

> 注意：工作内存也就是本地内存的意思。

### 小 结

由于CPU 和主内存间存在数量级的速率差，想到了引入了多级高速缓存的传统硬件内存架构来解决，多级高速缓存作为 CPU 和主内间的缓冲提升了整体性能。解决了速率差的问题，却又带来了缓存一致性问题。

数据同时存在于高速缓存和主内存中，如果不加以规范势必造成灾难，因此在传统机器上又抽象出了内存模型。

Java 语言在遵循内存模型的基础上推出了 JMM 规范，目的是解决由于多线程通过共享内存进行通信时，存在的本地内存数据不一致、编译器会对代码指令重排序、处理器会对代码乱序执行等带来的问题。

为了更精准控制工作内存和主内存间的交互，JMM 还定义了八种操作：`lock`, `unlock`, `read`, `load`,`use`,`assign`, `store`, `write`。

---

 JMM 内存模型是围绕并发编程中原子性、可见性、有序性三个特征来建立的

- 原子性：就是说一个操作不能被打断，要么执行完要么不执行，类似事务操作，Java 基本类型数据的访问大都是原子操作，long 和 double 类型是 64 位，在 32 位 JVM 中会将 64 位数据的读写操作分成两次 32 位来处理，所以 long 和 double 在 32 位 JVM 中是非原子操作，也就是说在并发访问时是线程非安全的，要想保证原子性就得对访问该数据的地方进行同步操作，譬如 synchronized 等。
- 可见性：就是说当一个线程对共享变量做了修改后其他线程可以立即感知到该共享变量的改变，从 Java 内存模型我们就能看出来多线程访问共享变量都要经过线程工作内存到主存的复制和主存到线程工作内存的复制操作，所以普通共享变量就无法保证可见性了；Java 提供了 volatile 修饰符来保证变量的可见性，每次使用 volatile 变量都会主动从主存中刷新，除此之外 synchronized、Lock、final 都可以保证变量的可见性。
- 有序性：就是说 Java 内存模型中的指令重排不会影响单线程的执行顺序，但是会影响多线程并发执行的正确性，所以在并发中我们必须要想办法保证并发代码的有序性；在 Java 里可以通过 volatile 关键字保证一定的有序性，还可以通过 synchronized、Lock 来保证有序性，因为 synchronized、Lock 保证了每一时刻只有一个线程执行同步代码相当于单线程执行，所以自然不会有有序性的问题；除此之外 Java 内存模型通过 **happens-before** 原则如果能推导出来两个操作的执行顺序就能先天保证有序性，否则无法保证

**Happen-Before**（先行发生规则）：意思是当A操作先行发生于B操作，则在发生B操作的时候，A操作产生的 影响 能被B观察到，“影响” 包括修改了内存中的共享变量的值、发送了消息、调用了方法等。

Happen-Before的规则：

- 程序次序规则（Program Order Rule）：在一个线程内，程序的执行规则跟程序的书写规则是一致的，从上往下执行。
- 管程锁定规则（Monitor Lock Rule）：一个Unlock的操作肯定先于下一次Lock的操作。这里必须是同一个锁。同理我们可以认为在synchronized同步同一个锁的时候，锁内先行执行的代码，对后续同步该锁的线程来说是完全可见的。
- volatile变量规则（volatile Variable Rule）：对同一个volatile的变量，先行发生的写操作，肯定早于后续发生的读操作
- 线程启动规则（Thread Start Rule）：Thread对象的start()方法先行发生于此线程的每一个动作
- 线程中止规则（Thread Termination Rule）：Thread对象的中止检测（如：Thread.join()，Thread.isAlive()等）操作，必行晚于线程中所有操作
- 线程中断规则（Thread Interruption Rule）：对线程的interruption（）调用，先于被调用的线程检测中断事件(Thread.interrupted())的发生
- 对象中止规则（Finalizer Rule）：一个对象的初始化方法先于一个方法执行Finalizer()方法
- 传递性（Transitivity）：如果操作A先于操作B、操作B先于操作C,则操作A先于操作C



- **缓存一致性** 问题其实就是 **可见性问题**。 
- **处理器优化 **是可以导致 **原子性问题** 
- **指令重排 **即会导致 **有序性问题**



> 请你谈谈你对 Volatile 的理解

Volatile 是 Java 虚拟机提供的 **轻量级的同步机制**

1、保证可见性

2、不保证原子性

3、禁止指令重排



> 什么是JMM

JMM：Java内存模型，不存在的东西，概念！约定！



关于JMM的一些同步的约定：

1、线程解锁前，必须把共享变量==**立刻**==刷回主存。

2、线程加锁前，必须读取主存中的最新值到工作内存中！

3、加锁与解锁是同一把锁



线程私有： **工作内存**、**执行引擎**

线程共有：**主内存**



8种内存交互操作：

![image-20210122095705576](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210122095705576.png)

**内存交互操作:**

 　内存交互操作有8种，虚拟机实现必须保证每一个操作都是原子的，不可在分的（对于double和long类型的变量来说，load、store、read和write操作在某些平台上允许例外）

- - lock   （锁定）：作用于主内存的变量，把一个变量标识为线程独占状态
  - unlock （解锁）：作用于主内存的变量，它把一个处于锁定状态的变量释放出来，释放后的变量才可以被其他线程锁定
  - read  （读取）：作用于主内存变量，它把一个变量的值从主内存传输到线程的工作内存中，以便随后的load动作使用
  - load   （载入）：作用于工作内存的变量，它把read操作从主存中变量放入工作内存中
  - use   （使用）：作用于工作内存中的变量，它把工作内存中的变量传输给执行引擎，每当虚拟机遇到一个需要使用到变量的值，就会使用到这个指令
  - assign （赋值）：作用于工作内存中的变量，它把一个从执行引擎中接受到的值放入工作内存的变量副本中
  - store  （存储）：作用于工作内存中的变量，它把一个从工作内存中一个变量的值传送到主内存中，以便后续的write使用
  - write 　（写入）：作用于主内存中的变量，它把store操作从工作内存中得到的变量的值放入主内存的变量中

　　JMM对这八种指令的使用，制定了如下规则：

- - 不允许read和load、store和write操作之一单独出现。即使用了read必须load，使用了store必须write
  - 不允许线程丢弃他最近的assign操作，即工作变量的数据改变了之后，必须告知主存
  - 不允许一个线程将没有assign的数据从工作内存同步回主内存
  - 一个新的变量必须在主内存中诞生，不允许工作内存直接使用一个未被初始化的变量。就是怼变量实施use、store操作之前，必须经过assign和load操作
  - 一个变量同一时间只有一个线程能对其进行lock。多次lock后，必须执行相同次数的unlock才能解锁
  - 如果对一个变量进行lock操作，会清空所有工作内存中此变量的值，在执行引擎使用这个变量前，必须重新load或assign操作初始化变量的值
  - 如果一个变量没有被lock，就不能对其进行unlock操作。也不能unlock一个被其他线程锁住的变量
  - 对一个变量进行unlock操作之前，必须把此变量同步回主内存



**问题：**程序不知道主内存中的值已经被修改过了

![image-20210122101151311](F:\编程学习\笔记\Typora\typoraNeed\Typora\typora-user-images\image-20210122101151311.png)

**扩展：**为什么synchronized关键字**不能禁止 指令重排**，却能保证**有序性**？

答：处理器优化 和 指令重排 是为了提高计算机各方面能力而在硬件层面所作的优化，当这些技术的引入会导致 有序性 问题。解决有序性问题的最好方法就是 禁止处理器优化 和 禁止指令重排，就像volatile中使用 **内存屏障 **一样。重排必须遵守 **as-if-serial** 语义（即重排不能影响单线程程序的执行结果）。synchronized是Java提供的锁，可以通过他对Java中的对象加锁，并且他是一种 **排他的、可重入的锁**，当某个线程执行到一段被 synchronized 修饰的代码之前，会进行加锁，执行完之后再解锁。在*加锁之后，解锁之前*（前提），其他线程无法再次获得锁（排他），只有这条加锁线程可以重复获得该锁（可重入）。synchronized通过 **排他锁** 的方式保证了***同一时间内，被 synchronized 修饰的代码是单线程执行的***。这就满足了 **as-if-serial** 语义的一个关键前提，即 **单线程** ，因为有 **as-if-serial** 语义保证，单线程的有序性就天然存在了。



---

## volatile

`volatile` 这个关键字可能很多朋友都听说过，或许也都用过。在 Java 5 之前，它是一个备受争议的关键字，因为在程序中使用它往往会导致出人意料的结果。在 Java 5之后，volatile 关键字才得以重获生机。

volatile 关键字虽然从字面上理解起来比较简单，但是要用好不是一件容易的事情。由于 volatile 关键字是与 Java 的内存模型有关的，因此在讲述 volatile 关键之前，我们先来了解一下与内存模型相关的概念和知识，然后分析了 volatile 关键字的实现原理，最后给出了几个使用 volatile 关键字的场景。

> ❝
>
> 为防止不提供原网址的转载，特加上原文链接：
>
> www.cnblogs.com/dolphin0520/p/3920373.html

### 内存模型的相关概念

大家都知道，计算机在执行程序时，每条指令都是在 `CPU` 中执行的，而执行指令过程中，势必涉及到数据的读取和写入。由于程序运行过程中的临时数据是存放在`主存（物理内存）`当中的，这时就存在一个问题，由于CPU执行速度很快，而从内存读取数据和向内存写入数据的过程跟 CPU 执行指令的速度比起来要慢的多，因此如果任何时候对数据的操作都要通过和内存的交互来进行，会大大降低指令执行的速度。因此在 CPU 里面就有了高速缓存。

也就是，**当程序在运行过程中，会将运算需要的数据从主存复制一份到 CPU 的高速缓存当中**，那么CPU进行计算时就可以直接从它的高速缓存读取数据和向其中写入数据，当运算结束之后，再将高速缓存中的数据刷新到主存当中。举个简单的例子，比如下面的这段代码：

```java
i = i + 1;
```

当线程执行这个语句时，会先从主存当中读取i的值，然后复制一份到高速缓存当中，然后 CPU 执行指令对 i 进行加 1 操作，然后将数据写入高速缓存，最后将高速缓存中i最新的值刷新到主存当中。

这个代码在单线程中运行是没有任何问题的，但是在多线程中运行就会有问题了。在多核 CPU 中，每条线程可能运行于不同的 CPU 中，因此每个线程运行时有自己的高速缓存（对单核CPU来说，其实也会出现这种问题，只不过是以线程调度的形式来分别执行的）。本文我们以多核 CPU 为例。

比如同时有 2 个线程执行这段代码，假如初始时 i 的值为 0，那么我们希望两个线程执行完之后 i 的值变为 2。但是事实会是这样吗？

可能存在下面一种情况：初始时，两个线程分别读取i的值存入各自所在的 CPU 的高速缓存当中，然后线程 1 进行加 1 操作，然后把i的最新值 1 写入到内存。此时线程 2 的高速缓存当中 i 的值还是 0，进行加 1 操作之后，i 的值为1，然后线程 2 把 i 的值写入内存。

最终结果 i 的值是 1，而不是 2。这就是著名的`缓存一致性`问题。通常称这种被多个线程访问的变量为`共享变量`。

**也就是说，如果一个变量在多个CPU中都存在缓存（一般在多线程编程时才会出现），那么就可能存在缓存不一致的问题**。

为了解决缓存不一致性问题，通常来说有以下 2 种解决方法：

- 通过在总线加 LOCK# 锁的方式
- 通过缓存一致性协议

这 2 种方式都是`硬件层面`上提供的方式。

在早期的 CPU 中，是通过在总线上加 LOCK# 锁的形式来解决缓存不一致的问题。因为 CPU 和其他部件进行通信都是通过总线来进行的，如果对总线加 LOCK# 锁的话，也就是说阻塞了其他 CPU 对其他部件访问（如内存），从而使得只能有一个 CPU 能使用这个变量的内存。比如上面例子中 如果一个线程在执行 i = i +1，如果在执行这段代码的过程中，在总线上发出了 LCOK# 锁的信号，那么只有等待这段代码完全执行完毕之后，其他CPU 才能从变量 i 所在的内存读取变量，然后进行相应的操作。这样就解决了缓存不一致的问题。

但是上面的方式会有一个问题，由于在锁住总线期间，其他 CPU 无法访问内存，导致效率低下。

所以就出现了缓存一致性协议。最出名的就是 Intel 的 MESI 协议，MESI 协议保证了每个缓存中使用的共享变量的副本是一致的。它核心的思想是：当 CPU 写数据时，如果发现操作的变量是共享变量，即在其他 CPU 中也存在该变量的副本，会发出信号通知其他 CPU 将该变量的缓存行置为无效状态，因此当其他 CPU 需要读取这个变量时，发现自己缓存中缓存该变量的缓存行是无效的，那么它就会从内存重新读取。

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/libYRuvULTdXfguz2lU2cpURRORvhAA9j9XYFQsLyKBkZ3aGcg4H5ibI3r47DSt8ick4bWDBFrAVUOw2DAHKlcuNQ/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

### 并发编程中的三个概念

在并发编程中，我们通常会遇到以下三个问题：**原子性问题，可见性问题，有序性问题**。我们先看具体看一下这三个概念：

#### 原子性

原子性：即一个操作或者多个操作 要么全部执行并且执行的过程不会被任何因素打断，要么就都不执行。

一个很经典的例子就是银行账户转账问题：

比如从账户 A 向账户 B 转 1000 元，那么必然包括 2 个操作：从账户 A 减去 1000 元，往账户 B 加上 1000 元。

试想一下，如果这 2 个操作不具备原子性，会造成什么样的后果。假如从账户 A 减去 1000 元之后，操作突然中止。然后又从 B 取出了 500 元，取出 500 元之后，再执行往账户 B 加上 1000 元 的操作。这样就会导致账户 A虽然减去了 1000 元，但是账户 B 没有收到这个转过来的 1000 元。

所以这 2 个操作必须要具备原子性才能保证不出现一些意外的问题。

同样地反映到并发编程中会出现什么结果呢？

举个最简单的例子，大家想一下假如为一个 32 位的变量赋值过程不具备原子性的话，会发生什么后果？

```java
i = 9;
```

假若一个线程执行到这个语句时，我暂且假设为一个 32 位的变量赋值包括两个过程：为低 16 位赋值，为高 16 位赋值。

那么就可能发生一种情况：当将低 16 位数值写入之后，突然被中断，而此时又有一个线程去读取i的值，那么读取到的就是错误的数据。

#### 可见性

可见性是指当多个线程访问同一个变量时，一个线程修改了这个变量的值，其他线程能够立即看得到修改的值。

举个简单的例子，看下面这段代码：

```java
//线程1执行的代码
int i = 0;
i = 10;
 
//线程2执行的代码
j = i;
```

假若执行线程1的是CPU1，执行线程2的是CPU2。由上面的分析可知，当线程1执行 i =10这句时，会先把i的初始值加载到CPU1的高速缓存中，然后赋值为10，那么在CPU1的高速缓存当中i的值变为10了，却没有立即写入到主存当中。

此时线程 2 执行 j = i，它会先去主存读取i的值并加载到 CPU2 的缓存当中，注意此时内存当中i的值还是 0，那么就会使得 j 的值为 0，而不是 10。

这就是可见性问题，线程 1 对变量 i 修改了之后，线程 2 没有立即看到线程 1 修改的值。

#### 有序性

有序性：即程序执行的顺序按照代码的先后顺序执行。举个简单的例子，看下面这段代码：

```java
int i = 0;              
boolean flag = false;
i = 1;                //语句1  
flag = true;          //语句2
```

上面代码定义了一个 int 型变量，定义了一个 boolean 类型变量，然后分别对两个变量进行赋值操作。从代码顺序上看，语句 1 是在语句 2 前面的，那么 JVM 在真正执行这段代码的时候会保证语句 1 一定会在语句 2 前面执行吗？不一定，为什么呢？这里可能会发生`指令重排序（Instruction Reorder）`。

下面解释一下什么是指令重排序，一般来说，处理器为了提高程序运行效率，可能会对输入代码进行优化，它不保证程序中各个语句的执行先后顺序同代码中的顺序一致，但是它会保证程序最终执行结果和代码顺序执行的结果是一致的。

比如上面的代码中，语句 1 和语句 2 谁先执行对最终的程序结果并没有影响，那么就有可能在执行过程中，语句 2 先执行而语句 1 后执行。

但是要注意，虽然处理器会对指令进行重排序，但是它会保证程序最终结果会和代码顺序执行结果相同，那么它靠什么保证的呢？再看下面一个例子：

```java
int a = 10;    //语句1
int r = 2;    //语句2
a = a + 3;    //语句3
r = a*a;     //语句4
```

这段代码有4个语句，那么可能的一个执行顺序是：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/libYRuvULTdXfguz2lU2cpURRORvhAA9jDu1pjsOgMhz8ILiclh4pdgW6wT9Q2NuZJnyyhKz2VzfFURraY1thk5g/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

那么可不可能是这个执行顺序呢：语句2  语句1  语句4  语句3

不可能，因为处理器在进行重排序时是会考虑指令之间的数据依赖性，如果一个指令 Instruction 2  必须用到Instruction 1的结果，那么处理器会保证 Instruction 1会在 Instruction 2 之前执行。

虽然重排序不会影响单个线程内程序执行的结果，但是多线程呢？下面看一个例子：

```java
//线程1:
context = loadContext();   //语句1
inited = true;             //语句2
 
//线程2:
while(!inited ){
  sleep()
}
doSomethingwithconfig(context);
```

上面代码中，由于语句 1 和语句 2 没有数据依赖性，因此可能会被重排序。假如发生了重排序，在线程 1 执行过程中先执行语句 2，而此时线程 2 会以为初始化工作已经完成，那么就会跳出 while 循环，去执行`doSomethingwithconfig(context)`方法，而此时 context 并没有被初始化，就会导致程序出错。

从上面可以看出，指令重排序不会影响单个线程的执行，但是会影响到线程并发执行的正确性。

也就是说，要想并发程序正确地执行，**必须要保证原子性、可见性以及有序性**。只要有一个没有被保证，就有可能会导致程序运行不正确。

### Java内存模型

在前面谈到了一些关于内存模型以及并发编程中可能会出现的一些问题。下面我们来看一下 Java 内存模型，研究一下 Java 内存模型为我们提供了哪些保证以及在 java 中提供了哪些方法和机制来让我们在进行多线程编程时能够保证程序执行的正确性。

在 Java 虚拟机规范中试图定义一种 `Java 内存模型（Java Memory Model，JMM）`来屏蔽各个硬件平台和操作系统的内存访问差异，以实现让 Java 程序在各种平台下都能达到一致的内存访问效果。那么 Java 内存模型规定了哪些东西？它定义了程序中变量的访问规则，往大一点说是定义了程序执行的次序。注意，为了获得较好的执行性能，Java 内存模型并没有限制执行引擎使用处理器的寄存器或者高速缓存来提升指令执行速度，也没有限制编译器对指令进行重排序。也就是说，在 java 内存模型中，也会存在缓存一致性问题和指令重排序的问题。

Java 内存模型规定所有的变量都是存在主存当中（类似于前面说的物理内存），每个线程都有自己的工作内存（类似于前面的高速缓存）。线程对变量的所有操作都必须在工作内存中进行，而不能直接对主存进行操作。并且每个线程不能访问其他线程的工作内存。

举个简单的例子：在 java中，执行下面这个语句：

```java
i  = 10;
```

执行线程必须先在自己的工作线程中对变量 i 所在的缓存行进行赋值操作，然后再写入主存当中。而不是直接将数值 10 写入主存当中。

那么Java 语言 本身对原子性、可见性以及有序性提供了哪些保证呢？

#### 原子性

在Java中，对基本数据类型的变量的读取和赋值操作是原子性操作，即这些操作是不可被中断的，要么执行，要么不执行。

上面一句话虽然看起来简单，但是理解起来并不是那么容易。看下面一个例子i：

请分析以下哪些操作是原子性操作：

```java
x = 10;         //语句1
y = x;         //语句2
x++;           //语句3
x = x + 1;     //语句4
```

咋一看，有些朋友可能会说上面的 4 个语句中的操作都是原子性操作。其实只有语句 1 是原子性操作，其他三个语句都不是原子性操作。

语句 1 是直接将数值 10 赋值给 x，也就是说线程执行这个语句的会直接将数值 10 写入到工作内存中。

语句 2 实际上包含 2 个操作，它先要去读取 x 的值，再将 x 的值写入工作内存，虽然读取 x 的值以及 将 x 的值写入工作内存 这 2 个操作都是原子性操作，但是合起来就不是原子性操作了。

同样的，x++ 和 x = x+1 包括 3 个操作：读取 x 的值，进行加 1 操作，写入新的值。

所以上面 4 个语句只有语句 1 的操作具备原子性。

也就是说，**只有简单的读取、赋值（而且必须是将数字赋值给某个变量，变量之间的相互赋值不是原子操作）才是原子操作**。

不过这里有一点需要注意：在 32 位平台下，对 64 位数据的读取和赋值是需要通过两个操作来完成的，不能保证其原子性。但是好像在最新的 JDK 中，JVM 已经保证对 64 位数据的读取和赋值也是原子性操作了。

从上面可以看出，Java 内存模型只保证了基本读取和赋值是原子性操作，如果要实现更大范围操作的原子性，可以通过 synchronized 和 Lock 来实现。由于 synchronized 和 Lock 能够保证任一时刻只有一个线程执行该代码块，那么自然就不存在原子性问题了，从而保证了原子性。

#### 可见性

对于可见性，Java提供了 volatile 关键字来保证可见性。

当一个共享变量被 volatile 修饰时，它会保证修改的值会立即被更新到主存，当有其他线程需要读取时，它会去内存中读取新值。

而普通的共享变量不能保证可见性，因为普通共享变量被修改之后，什么时候被写入主存是不确定的，当其他线程去读取时，此时内存中可能还是原来的旧值，因此无法保证可见性。

另外，通过 synchronized 和 Lock 也能够保证可见性，synchronized 和 Lock 能保证同一时刻只有一个线程获取锁然后执行同步代码，并且在释放锁之前会将对变量的修改刷新到主存当中。因此可以保证可见性。

#### 有序性

在 Java 内存模型中，允许编译器和处理器对指令进行重排序，但是重排序过程不会影响到单线程程序的执行，却会影响到多线程并发执行的正确性。

在 Java 里面，可以通过 volatile 关键字来保证一定的`有序性`（具体原理在下一节讲述）。另外可以通过 synchronized 和 Lock 来保证有序性(他们保证的有序性是相对的，即线程间有序，并不会禁止指令重排），很显然，synchronized 和 Lock 保证每个时刻是有一个线程执行同步代码，相当于是让线程顺序执行同步代码，自然就保证了有序性。

另外，Java 内存模型具备一些先天的`有序性`，即不需要通过任何手段就能够得到保证的有序性，这个通常也称为 `happens-before 原则`。如果两个操作的执行次序无法从 happens-before 原则推导出来，那么它们就不能保证它们的有序性，虚拟机可以随意地对它们进行重排序。

下面就来具体介绍下 happens-before原则（先行发生原则）：

- 程序次序规则：一个线程内，按照代码顺序，书写在前面的操作先行发生于书写在后面的操作
- 锁定规则：一个 unLock 操作先行发生于后面对同一个锁的 lock 操作
- volatile 变量规则：对一个变量的写操作先行发生于后面对这个变量的读操作
- 传递规则：如果操作 A 先行发生于操作 B，而操作 B 又先行发生于操作 C，则可以得出操作 A 先行发生于操作C
- 线程启动规则：Thread 对象的 `start()` 方法先行发生于此线程的每个一个动作
- 线程中断规则：对线程`interrupt()`方法的调用先行发生于被中断线程的代码检测到中断事件的发生
- 线程终结规则：线程中所有的操作都先行发生于线程的终止检测，我们可以通过 `Thread.join()` 方法结束、`Thread.isAlive()`的返回值手段检测到线程已经终止执行
- 对象终结规则：一个对象的初始化完成先行发生于他的`finalize()`方法的开始

这 8 条原则摘自《深入理解Java虚拟机》。

这 8 条规则中，前 4 条规则是比较重要的，后 4 条规则都是显而易见的。

下面我们来解释一下前 4 条规则：

对于程序次序规则来说，我的理解就是一段程序代码的执行在单个线程中看起来是有序的。注意，虽然这条规则中提到“书写在前面的操作先行发生于书写在后面的操作”，这个应该是程序看起来执行的顺序是按照代码顺序执行的，因为虚拟机可能会对程序代码进行指令重排序。虽然进行重排序，但是最终执行的结果是与程序顺序执行的结果一致的，它只会对不存在数据依赖性的指令进行重排序。因此，在单个线程中，程序执行看起来是有序执行的，这一点要注意理解。事实上，这个规则是用来保证程序在单线程中执行结果的正确性，但无法保证程序在多线程中执行的正确性。

第二条规则也比较容易理解，也就是说无论在单线程中还是多线程中，同一个锁如果处于被锁定的状态，那么必须先对锁进行了释放操作，后面才能继续进行 lock 操作。

第三条规则是一条比较重要的规则，也是后文将要重点讲述的内容。直观地解释就是，如果一个线程先去写一个变量，然后一个线程去进行读取，那么写入操作肯定会先行发生于读操作。

第四条规则实际上就是体现 happens-before 原则具备传递性。

### 深入剖析volatile关键字

在前面讲述了很多东西，其实都是为讲述 volatile 关键字作铺垫，那么接下来我们就进入主题。

#### volatile关键字的两层语义

一旦一个共享变量（类的成员变量、类的静态成员变量）被 volatile 修饰之后，那么就具备了两层语义：

- 保证了不同线程对这个变量进行操作时的可见性，即一个线程修改了某个变量的值，这新值对其他线程来说是立即可见的。
- 禁止进行指令重排序。

先看一段代码，假如线程 1 先执行，线程 2 后执行：

```java
//线程1
boolean stop = false;
while(!stop){
    doSomething();
}
 
//线程2
stop = true;
```

这段代码是很典型的一段代码，很多人在中断线程时可能都会采用这种标记办法。但是事实上，这段代码会完全运行正确么？即一定会将线程中断么？不一定，也许在大多数时候，这个代码能够把线程中断，但是也有可能会导致无法中断线程（虽然这个可能性很小，但是只要一旦发生这种情况就会造成死循环了）。

下面解释一下这段代码为何有可能导致无法中断线程。在前面已经解释过，每个线程在运行过程中都有自己的工作内存，那么线程1在运行的时候，会将 stop 变量的值拷贝一份放在自己的工作内存当中。

那么当线程 2 更改了 stop 变量的值之后，但是还没来得及写入主存当中，线程 2 转去做其他事情了，那么线程 1由于不知道线程 2 对 stop 变量的更改，因此还会一直循环下去。

但是用 volatile 修饰之后就变得不一样了：

- 使用 volatile 关键字会强制将修改的值立即写入主存；
- 使用 volatile 关键字的话，当线程 2 进行修改时，会导致线程 1 的工作内存中缓存变量 stop 的缓存行无效（反映到硬件层的话，就是CPU 的 L1 或者 L2 缓存中对应的缓存行无效）；
- 由于线程1的工作内存中缓存变量 stop 的缓存行无效，所以线程 1 再次读取变量 stop 的值时会去主存读取。

那么在线程 2 修改 stop 值时（当然这里包括 2 个操作，修改线程 2 工作内存中的值，然后将修改后的值写入内存），会使得线程 1 的工作内存中缓存变量 stop 的缓存行无效，然后线程 1 读取时，发现自己的缓存行无效，它会等待缓存行对应的主存地址被更新之后，然后去对应的主存读取最新的值。

那么线程 1 读取到的就是最新的正确的值。

#### volatile 保证原子性吗

从上面知道 volatile 关键字保证了操作的可见性，但是 volatile 能保证对变量的操作是原子性吗？

下面看一个例子：

```java
public class Test {
    public volatile int inc = 0;
     
    public void increase() {
        inc++;
    }
     
    public static void main(String[] args) {
        final Test test = new Test();
        for(int i=0;i<10;i++){
            new Thread(){
                public void run() {
                    for(int j=0;j<1000;j++)
                        test.increase();
                };
            }.start();
        }
         
        while(Thread.activeCount()>1)  //保证前面的线程都执行完
            Thread.yield();
        System.out.println(test.inc);
    }
}
```

大家想一下这段程序的输出结果是多少？也许有些朋友认为是 10000。但是事实上运行它会发现每次运行结果都不一致，都是一个小于 10000 的数字。

可能有的朋友就会有疑问，不对啊，上面是对变量 inc 进行自增操作，由于 volatile 保证了可见性，那么在每个线程中对 inc 自增完之后，在其他线程中都能看到修改后的值啊，所以有 10 个线程分别进行了 1000 次操作，那么最终 inc 的值应该是 1000*10=10000。

这里面就有一个误区了，volatile 关键字能保证可见性没有错，但是上面的程序错在没能保证原子性。可见性只能保证每次读取的是最新的值，但是 volatile 没办法保证对变量的操作的原子性。

在前面已经提到过，自增操作是不具备原子性的，它包括读取变量的原始值、进行加 1 操作、写入工作内存。那么就是说自增操作的三个子操作可能会分割开执行，就有可能导致下面这种情况出现：

假如某个时刻变量 inc 的值为10，

线程 1 对变量进行自增操作，线程 1 先读取了变量inc的原始值，然后线程1被阻塞了；

然后线程 2 对变量进行自增操作，线程 2 也去读取变量 inc 的原始值，由于线程 1 只是对变量 inc 进行读取操作，而没有对变量进行修改操作，所以不会导致线程 2 的工作内存中缓存变量 inc 的缓存行无效，所以线程 2 会直接去主存读取 inc 的值，发现 inc 的值是 10，然后进行加 1 操作，并把 11 写入工作内存，最后写入主存。

然后线程 1 接着进行加 1 操作，由于已经读取了 inc 的值，注意此时在线程 1 的工作内存中 inc 的值仍然为 10，所以线程 1 对 inc 进行加 1 操作后 inc 的值为 11，然后将 11 写入工作内存，最后写入主存。

那么两个线程分别进行了一次自增操作后，inc 只增加了 1。

解释到这里，可能有朋友会有疑问，不对啊，前面不是保证一个变量在修改 volatile 变量时，会让缓存行无效吗？然后其他线程去读就会读到新的值，对，这个没错。这个就是上面的 happens-before 规则中的 volatile 变量规则，但是要注意，线程 1 对变量进行读取操作之后，被阻塞了的话，并没有对 inc 值进行修改。然后虽然 volatile能保证线程 2 对变量 inc 的值读取是从内存中读取的，但是线程 1 没有进行修改，所以线程 2 根本就不会看到修改的值。

根源就在这里，自增操作不是原子性操作，而且 volatile 也无法保证对变量的任何操作都是原子性的。

助解：

```java
第二：使用volatile关键字的话，当线程2进行修改时，会导致线程1的工作内存中缓存变量stop的缓存行无效
然后我们再重新整理下.....
A线程：①读 ②+1 ③写
B线程：①读 ②+1 ③写
文章中例子的流程：A① → B① B② B③ → A② A③
按照上面这段话来看，到了第二个箭头处的时候A应该会重新读取主内存中已经被B修改过的值，可例子的结果是A依然使用了原先缓存的值。
@sirding给出的流程：A① A② → B① B② B③ → A③
不同点在于：A要先执行+1操作。接着根据上面的这段话，A会在第二个箭头处重新读取主内存中的值。
    
即A线程的 +1 操作做了无用功。
```





把上面的代码改成以下任何一种都可以达到效果：

采用synchronized：

```java
public class Test {
    public  int inc = 0;
    
    public synchronized void increase() {
        inc++;
    }
    
    public static void main(String[] args) {
        final Test test = new Test();
        for(int i=0;i<10;i++){
            new Thread(){
                public void run() {
                    for(int j=0;j<1000;j++)
                        test.increase();
                };
            }.start();
        }
        
        while(Thread.activeCount()>1)  //保证前面的线程都执行完
            Thread.yield();
        System.out.println(test.inc);
    }
}
```

采用 Lock：

```java
public class Test {
    public  int inc = 0;
    Lock lock = new ReentrantLock();
    
    public  void increase() {
        lock.lock();
        try {
            inc++;
        } finally{
            lock.unlock();
        }
    }
    
    public static void main(String[] args) {
        final Test test = new Test();
        for(int i=0;i<10;i++){
            new Thread(){
                public void run() {
                    for(int j=0;j<1000;j++)
                        test.increase();
                };
            }.start();
        }
        
        while(Thread.activeCount()>1)  //保证前面的线程都执行完
            Thread.yield();
        System.out.println(test.inc);
    }
}
```

采用 AtomicInteger

```java
public class Test {
    public  AtomicInteger inc = new AtomicInteger();
     
    public  void increase() {
        inc.getAndIncrement();
    }
    
    public static void main(String[] args) {
        final Test test = new Test();
        for(int i=0;i<10;i++){
            new Thread(){
                public void run() {
                    for(int j=0;j<1000;j++)
                        test.increase();
                };
            }.start();
        }
        
        while(Thread.activeCount()>1)  //保证前面的线程都执行完
            Thread.yield();
        System.out.println(test.inc);
    }
}
```

在 java 1.5的 `java.util.concurrent.atomic` 包下提供了一些原子操作类，即对基本数据类型的 自增（加 1操作），自减（减 1 操作）、以及加法操作（加一个数），减法操作（减一个数）进行了封装，保证这些操作是原子性操作。atomic 是利用 CAS 来实现原子性操作的（Compare And Swap），CAS 实际上是利用处理器提供的 CMPXCHG 指令实现的，而处理器执行 CMPXCHG 指令是一个原子性操作。

#### volatile能保证有序性吗

在前面提到 volatile 关键字能禁止指令重排序，所以 volatile 能在一定程度上保证有序性。

volatile 关键字禁止指令重排序有两层意思：

- 当程序执行到 volatile 变量的读操作或者写操作时，在其前面的操作的更改肯定全部已经进行，且结果已经对后面的操作可见；在其后面的操作肯定还没有进行；
- 在进行指令优化时，不能将在对 volatile 变量访问的语句放在其后面执行，也不能把 volatile 变量后面的语句放到其前面执行。

可能上面说的比较绕，举个简单的例子：

```java
//x、y为非volatile变量
//flag为volatile变量
 
x = 2;        //语句1
y = 0;        //语句2
flag = true;  //语句3
x = 4;         //语句4
y = -1;       //语句5
```

由于 flag 变量为 volatile 变量，那么在进行指令重排序的过程的时候，不会将语句 3 放到语句 1、语句 2 前面，也不会讲语句 3 放到语句 4、语句 5 后面。但是要注意语句 1 和语句 2 的顺序、语句 4 和语句 5 的顺序是不作任何保证的。

并且 volatile 关键字能保证，执行到语句 3 时，语句 1 和语句 2 必定是执行完毕了的，且语句 1 和语句 2 的执行结果对语句 3、语句 4、语句 5 是可见的。

那么我们回到前面举的一个例子：

```java
//线程1:
context = loadContext();   //语句1
inited = true;             //语句2
 
//线程2:
while(!inited ){
  sleep()
}
doSomethingwithconfig(context);
```

前面举这个例子的时候，提到有可能语句 2 会在语句 1 之前执行，那么就可能导致 context 还没被初始化，而线程 2 中就使用未初始化的 context 去进行操作，导致程序出错。

这里如果用 volatile 关键字对 inited 变量进行修饰，就不会出现这种问题了，因为当执行到语句 2 时，必定能保证 context 已经初始化完毕。

#### volatile 的原理和实现机制

前面讲述了源于 volatile 关键字的一些使用，下面我们来探讨一下 volatile 到底如何保证可见性和禁止指令重排序的。

下面这段话摘自《深入理解Java虚拟机》

**观察加入 volatile 关键字和没有加入 volatile 关键字时所生成的汇编代码发现，加入 volatile 关键字时，会多出一个 lock 前缀指令**

lock 前缀指令实际上相当于一个内存屏障（也称内存栅栏），内存屏障会提供 3 个功能：

- 它确保指令重排序时不会把其后面的指令排到内存屏障之前的位置，也不会把前面的指令排到内存屏障的后面；即在执行到内存屏障这句指令时，在它前面的操作已经全部完成；
- 它会强制将对缓存的修改操作立即写入主存；
- 如果是写操作，它会导致其他 CPU 中对应的缓存行无效。

### 使用 volatile 关键字的场景

synchronized 关键字是防止多个线程同时执行一段代码，那么就会很影响程序执行效率，而 volatile 关键字在某些情况下性能要优于 synchronized，但是要注意 volatile 关键字是无法替代 synchronized 关键字的，因为 volatile 关键字无法保证操作的原子性。通常来说，使用 volatile 必须具备以下 2 个条件：

- 对变量的写操作不依赖于当前值
- 该变量没有包含在具有其他变量的不变式中

实际上，这些条件表明，可以被写入 volatile 变量的这些有效值独立于任何程序的状态，包括变量的当前状态。

事实上，我的理解就是上面的 2 个条件需要保证操作是原子性操作，才能保证使用 volatile 关键字的程序在并发时能够正确执行。

下面列举几个 Java 中使用 volatile 的几个场景。

#### 状态标记量

```java
volatile boolean flag = false;
 
while(!flag){
    doSomething();
}
 
public void setFlag() {
    flag = true;
}


volatile boolean inited = false;
//线程1:
context = loadContext();  
inited = true;            
 
//线程2:
while(!inited ){
sleep()
}
doSomethingwithconfig(context);
```

#### double check

```java
class Singleton{
    private volatile static Singleton instance = null;
     
    private Singleton() {
         
    }
     
    public static Singleton getInstance() {
        if(instance==null) {
            synchronized (Singleton.class) {
                if(instance==null)
                    instance = new Singleton();
            }
        }
        return instance;
    }
}
```

---

## 并发和锁

### 乐观锁和悲观锁

`悲观锁和乐观锁并非是一种实际的锁，而是指一种加锁的概念`

> **悲观锁**

`悲观锁`对应于生活中悲观的人，悲观的人总是想着事情往坏的方向发展。

举个生活中的例子，假设厕所只有一个坑位了，悲观锁上厕所会第一时间把门反锁上，这样其他人上厕所只能在门外等候，这种状态就是「阻塞」了。

回到代码世界中，一个共享数据加了悲观锁，那线程每次想操作这个数据前都会假设其他线程也可能会操作这个数据，所以每次操作前都会上锁，这样其他线程想操作这个数据拿不到锁只能阻塞了。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoOs44EkUiceqsT2yyRBYuPP7RkU21gibQo4icojIVfI6YsEHnib3eVrkSySr2FXKibSxS3f2aBFWKBDfg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

在 Java 语言中 `synchronized` 和 `ReentrantLock`等就是典型的悲观锁，还有一些使用了 synchronized 关键字的容器类如 `HashTable` 等也是悲观锁的应用。

> **乐观锁**

`乐观锁` 对应于生活中乐观的人，乐观的人总是想着事情往好的方向发展。

举个生活中的例子，假设厕所只有一个坑位了，乐观锁认为：这荒郊野外的，又没有什么人，不会有人抢我坑位的，每次关门上锁多浪费时间，还是不加锁好了。你看乐观锁就是天生乐观！

回到代码世界中，乐观锁操作数据时不会上锁，在更新的时候会判断一下在此期间是否有其他线程去更新这个数据。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoOs44EkUiceqsT2yyRBYuPP67neroExfjzmQI2xnDAQsXHBpOBX6HSSPOOC6salPMJmt9mibtia6ZWg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

乐观锁可以使用`版本号机制`和`CAS算法`实现。在 Java 语言中 `java.util.concurrent.atomic`包下的原子类就是使用CAS 乐观锁实现的。

**两种锁的使用场景**

悲观锁和乐观锁没有孰优孰劣，有其各自适应的场景。

乐观锁适用于写比较少（冲突比较小）的场景，因为不用上锁、释放锁，省去了锁的开销，从而提升了吞吐量。

如果是写多读少的场景，即冲突比较严重，线程间竞争激励，使用乐观锁就是导致线程不断进行重试，这样可能还降低了性能，这种场景下使用悲观锁就比较合适。

### 独占锁和共享锁

> **独占锁**

`独占锁`是指锁一次只能被一个线程所持有。如果一个线程对数据加上排他锁后，那么其他线程不能再对该数据加任何类型的锁。获得独占锁的线程即能读数据又能修改数据。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoOs44EkUiceqsT2yyRBYuPPicRVviaGia4gTTiblCdxRfvCWskplyz6mddicm8BcFql96MjRI6Hv5nnsmw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

JDK中的`synchronized`和`java.util.concurrent(JUC)`包中Lock的实现类就是独占锁。

> **共享锁**

`共享锁`是指锁可被多个线程所持有。如果一个线程对数据加上共享锁后，那么其他线程只能对数据再加共享锁，不能加独占锁。获得共享锁的线程只能读数据，不能修改数据。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoOs44EkUiceqsT2yyRBYuPPGaglkYgzL8ROKSbDQLiacocF1W4amTTJORQ2RekMBhibLRlmAsz1YE9w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

在 JDK 中 `ReentrantReadWriteLock` 就是一种共享锁。

### 互斥锁和读写锁

> **互斥锁**

`互斥锁`是独占锁的一种常规实现，是指某一资源同时只允许一个访问者对其进行访问，具有唯一性和排它性。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoOs44EkUiceqsT2yyRBYuPPt1icljXsibs48NTaQKzFWYXOFyoibbnkdqP1IlbWVMoVVrmAQcfI8EhGQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

互斥锁一次只能一个线程拥有互斥锁，其他线程只有等待。

> **读写锁**

`读写锁`是共享锁的一种具体实现。读写锁管理一组锁，一个是只读的锁，一个是写锁。

读锁可以在没有写锁的时候被多个线程同时持有，而写锁是独占的。写锁的优先级要高于读锁，一个获得了读锁的线程必须能看到前一个释放的写锁所更新的内容。

读写锁相比于互斥锁并发程度更高，每次只有一个写线程，但是同时可以有多个线程并发读。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoOs44EkUiceqsT2yyRBYuPP140Knfgg6fts0hcn5CN4xV60QY7fADkY8Pyk9Vqyp0AM60q8Lhr4Nw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

在 JDK 中定义了一个读写锁的接口：`ReadWriteLock`

```java
public interface ReadWriteLock {
    /**
     * 获取读锁
     */
    Lock readLock();

    /**
     * 获取写锁
     */
    Lock writeLock();
}
```

`ReentrantReadWriteLock` 实现了`ReadWriteLock`接口，具体实现这里不展开，后续会深入源码解析。

### 公平锁和非公平锁

> **公平锁**

`公平锁`是指多个线程按照申请锁的顺序来获取锁，这里类似排队买票，先来的人先买，后来的人在队尾排着，这是公平的。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoOs44EkUiceqsT2yyRBYuPPmibwzYdV6yNUPQloGlRNrw8qaEuEUk03oeib7y0m9OpUw65nxtSLrKfQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

在 java 中可以通过构造函数初始化公平锁

```java
/**
* 创建一个可重入锁，true 表示公平锁，false 表示非公平锁。默认非公平锁
*/
Lock lock = new ReentrantLock(true);
```

> **非公平锁**

`非公平锁`是指多个线程获取锁的顺序并不是按照申请锁的顺序，有可能后申请的线程比先申请的线程优先获取锁，在高并发环境下，有可能造成优先级翻转，或者饥饿的状态（某个线程一直得不到锁）。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoOs44EkUiceqsT2yyRBYuPPEucqSBecFa5ww4N8ppVcgDIU68IugIKq2sumxEP6Z0UOERgCPb331w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

在 java 中 synchronized 关键字是非公平锁，ReentrantLock默认也是非公平锁。

```java
/**
* 创建一个可重入锁，true 表示公平锁，false 表示非公平锁。默认非公平锁
*/
Lock lock = new ReentrantLock(false);
```

### 可重入锁

`可重入锁`又称之为`递归锁`，是指同一个线程在外层方法获取了锁，在进入内层方法会自动获取锁。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoOs44EkUiceqsT2yyRBYuPP8nYwRjZ3RQOm44ENYBhOpKJicylbA7md7fRkSxibicibZ8tQmpBGT8Vs7Q/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

对于Java ReentrantLock而言, 他的名字就可以看出是一个可重入锁。对于Synchronized而言，也是一个可重入锁。

敲黑板：可重入锁的一个好处是可一定程度避免死锁。

以 synchronized 为例，看一下下面的代码：

```java
public synchronized void mehtodA() throws Exception{
 // Do some magic tings
 mehtodB();
}

public synchronized void mehtodB() throws Exception{
 // Do some magic tings
}
```

上面的代码中 methodA 调用 methodB，如果一个线程调用methodA 已经获取了锁再去调用 methodB 就不需要再次获取锁了，这就是可重入锁的特性。如果不是可重入锁的话，mehtodB 可能不会被当前线程执行，可能造成死锁。

### 自旋锁

`自旋锁`是指线程在没有获得锁时不是被直接挂起，而是执行一个忙循环，这个忙循环就是所谓的自旋。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoOs44EkUiceqsT2yyRBYuPPMFdsHctX22NteTRiaHnnIj1YR53UzBmTI9CshP0OEppSy31LRMr5icsA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

自旋锁的目的是为了减少线程被挂起的几率，因为线程的挂起和唤醒也都是耗资源的操作。

如果锁被另一个线程占用的时间比较长，即使自旋了之后当前线程还是会被挂起，忙循环就会变成浪费系统资源的操作，反而降低了整体性能。因此自旋锁是不适应锁占用时间长的并发情况的。

在 Java 中，`AtomicInteger` 类有自旋的操作，我们看一下代码：

```java
public final int getAndAddInt(Object o, long offset, int delta) {
    int v;
    do {
        v = getIntVolatile(o, offset);
    } while (!compareAndSwapInt(o, offset, v, v + delta));
    return v;
}
```

CAS 操作如果失败就会一直循环获取当前 value 值然后重试。

另外`自适应自旋锁`也需要了解一下。

在JDK1.6又引入了自适应自旋，这个就比较智能了，自旋时间不再固定，由前一次在同一个锁上的自旋时间以及锁的拥有者的状态来决定。如果虚拟机认为这次自旋也很有可能再次成功那就会次序较多的时间，如果自旋很少成功，那以后可能就直接省略掉自旋过程，避免浪费处理器资源。

### 分段锁

`分段锁` 是一种锁的设计，并不是具体的一种锁。

分段锁设计目的是将锁的粒度进一步细化，当操作不需要更新整个数组的时候，就仅仅针对数组中的一项进行加锁操作。

![图片](https://mmbiz.qpic.cn/mmbiz_png/RXvHpViaz3EoOs44EkUiceqsT2yyRBYuPP8Q7cxicbbFn8TyHxtOD93VY1M07AEpBbEBTFI2xxic8vKbibf5NrjzokA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

在 Java 语言中 CurrentHashMap 底层就用了分段锁，使用Segment，就可以进行并发使用了。

### 锁升级（无锁|偏向锁|轻量级锁|重量级锁）

JDK1.6 为了提升性能减少获得锁和释放锁所带来的消耗，引入了4种锁的状态：`无锁`、`偏向锁`、`轻量级锁`和`重量级锁`，它会随着多线程的竞争情况逐渐升级，但不能降级。

> **无锁**

`无锁`状态其实就是上面讲的乐观锁，这里不再赘述。

> **偏向锁**

Java偏向锁(Biased Locking)是指它会偏向于第一个访问锁的线程，如果在运行过程中，只有一个线程访问加锁的资源，不存在多线程竞争的情况，那么线程是不需要重复获取锁的，这种情况下，就会给线程加一个偏向锁。

偏向锁的实现是通过控制对象`Mark Word`的标志位来实现的，如果当前是`可偏向状态`，需要进一步判断对象头存储的线程 ID 是否与当前线程 ID 一致，如果一致直接进入。

> **轻量级锁**

当线程竞争变得比较激烈时，偏向锁就会升级为`轻量级锁`，轻量级锁认为虽然竞争是存在的，但是理想情况下竞争的程度很低，通过`自旋方式`等待上一个线程释放锁。

> **重量级锁**

如果线程并发进一步加剧，线程的自旋超过了一定次数，或者一个线程持有锁，一个线程在自旋，又来了第三个线程访问时（反正就是竞争继续加大了），轻量级锁就会膨胀为`重量级锁`，重量级锁会使除了此时拥有锁的线程以外的线程都阻塞。

升级到重量级锁其实就是互斥锁了，一个线程拿到锁，其余线程都会处于阻塞等待状态。

在 Java 中，synchronized 关键字内部实现原理就是锁升级的过程：无锁 --> 偏向锁 --> 轻量级锁 --> 重量级锁。这一过程在后续讲解 synchronized 关键字的原理时会详细介绍。

#### [扩展](https://www.cnblogs.com/mingyueyy/p/13054296.html)

##### 一、前言

锁的状态总共有四种，级别由低到高依次为：**无锁、偏向锁、轻量级锁、重量级锁**，这四种锁状态分别代表什么，为什么会有锁升级？其实在 JDK 1.6之前，**synchronized 还是一个重量级锁**，是一个效率比较低下的锁，但是在JDK 1.6后，JVM为了提高锁的获取与释放效率对（**synchronized** ）进行了优化，引入了 **偏向锁 和 轻量级锁** ，从此以后锁的状态就有了四种（无锁、偏向锁、轻量级锁、重量级锁），**并且四种状态会随着竞争的情况逐渐升级，而且是不可逆的过程，即不可降级，也就是说只能进行锁升级（从低级别到高级别），不能锁降级（高级别到低级别）**，意味着偏向锁升级成轻量级锁后不能降级成偏向锁。这种锁升级却不能降级的策略，目的是为了提高获得锁和释放锁的效率。

##### 二、锁的四种状态

在 `synchronized` 最初的实现方式是 “**阻塞或唤醒一个Java线程需要操作系统切换CPU状态来完成，这种状态切换需要耗费处理器时间，如果同步代码块中内容过于简单，这种切换的时间可能比用户代码执行的时间还长”**，这种方式就是 `synchronized`实现同步最初的方式，这也是当初开发者诟病的地方，这也是在JDK6以前 `synchronized`效率低下的原因，JDK6中为了减少获得锁和释放锁带来的性能消耗，引入了“偏向锁”和“轻量级锁”。

所以目前锁状态一种有四种，从级别由低到高依次是：**无锁、偏向锁，轻量级锁，重量级锁**，锁状态只能升级，不能降级

**如图所示：**
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200603161323889.png)

##### 三、锁状态的思路以及特点

| 锁状态   | 存储内容                                              | 标志位 |
| -------- | ----------------------------------------------------- | ------ |
| 无锁     | 对象的hashCode、对象分代年龄、是否是偏向锁(0)         | 01     |
| 偏向锁   | 偏向线程ID、偏向时间戳、对象分代年龄、是否是偏向锁(1) | 01     |
| 轻量级锁 | 指向栈中锁记录的指针                                  | 00     |
| 重量级锁 | 指向互斥量的指针                                      | 11     |

##### 四、锁对比

| 锁       | 优点                                                         | 缺点                                           | 适用场景                           |
| -------- | ------------------------------------------------------------ | ---------------------------------------------- | ---------------------------------- |
| 偏向锁   | 加锁和解锁不需要额外的消耗，和执行非同步方法相比仅存在纳秒级的差距 | 如果线程间存在锁竞争，会带来额外的锁撤销的消耗 | 适用于只有一个线程访问同步块场景   |
| 轻量级锁 | 竞争的线程不会阻塞，提高了程序的响应速度                     | 如果始终得不到索竞争的线程，使用自旋会消耗CPU  | 追求响应速度，同步块执行速度非常快 |
| 重量级锁 | 线程竞争不使用自旋，不会消耗CPU                              | 线程阻塞，响应时间缓慢                         | 追求吞吐量，同步块执行速度较慢     |

##### 五、Synchronized锁

`synchronized` 用的锁是存在Java对象头里的，那么什么是对象头呢？

###### 5.1 Java 对象头

我们以 Hotspot 虚拟机为例，Hopspot 对象头主要包括两部分数据：`Mark Word（标记字段） 和 Klass Pointer（类型指针）`

**Mark Word**：默认存储对象的HashCode，分代年龄和锁标志位信息。这些信息都是与对象自身定义无关的数据，所以Mark Word被设计成一个非固定的数据结构以便在极小的空间内存存储尽量多的数据。它会根据对象的状态复用自己的存储空间，也就是说在运行期间Mark Word里存储的数据会随着锁标志位的变化而变化。

**Klass Point**：对象指向它的类元数据的指针，虚拟机通过这个指针来确定这个对象是哪个类的实例。

在上面中我们知道了，`synchronized` 用的锁是存在Java对象头里的，那么具体是存在对象头哪里呢？答案是：**存在锁对象的对象头的Mark Word中**，那么MarkWord在对象头中到底长什么样，它到底存储了什么呢？

**在64位的虚拟机中：**
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200606113746579.png)
**在32位的虚拟机中：**
![在这里插入图片描述](https://img-blog.csdnimg.cn/20200606113736103.png)

下面我们以 32位虚拟机为例，来看一下其 Mark Word 的字节具体是如何分配的

**无锁**：对象头开辟 25bit 的空间用来存储对象的 hashcode ，4bit 用于存放对象分代年龄，1bit 用来存放是否偏向锁的标识位，2bit 用来存放锁标识位为01

**偏向锁：** 在偏向锁中划分更细，还是开辟 25bit 的空间，其中23bit 用来存放线程ID，2bit 用来存放 Epoch，4bit 存放对象分代年龄，1bit 存放是否偏向锁标识， 0表示无锁，1表示偏向锁，锁的标识位还是01

**轻量级锁**：在轻量级锁中直接开辟 30bit 的空间存放指向栈中锁记录的指针，2bit 存放锁的标志位，其标志位为00

**重量级锁：** 在重量级锁中和轻量级锁一样，30bit 的空间用来存放指向重量级锁的指针，2bit 存放锁的标识位，为11

**GC标记：** 开辟30bit 的内存空间却没有占用，2bit 空间存放锁标志位为11。

其中无锁和偏向锁的锁标志位都是01，只是在前面的1bit区分了这是无锁状态还是偏向锁状态

关于内存的分配，我们可以在git中openJDK中 [markOop.hpp](https://github.com/openjdk-mirror/jdk7u-hotspot/blob/50bdefc3afe944ca74c3093e7448d6b889cd20d1/src/share/vm/oops/markOop.hpp) 可以看出：

```assembly
public:
  // Constants
  enum { age_bits                 = 4,
         lock_bits                = 2,
         biased_lock_bits         = 1,
         max_hash_bits            = BitsPerWord - age_bits - lock_bits - biased_lock_bits,
         hash_bits                = max_hash_bits > 31 ? 31 : max_hash_bits,
         cms_bits                 = LP64_ONLY(1) NOT_LP64(0),
         epoch_bits               = 2
  };
```

- **age_bits：** 就是我们说的分代回收的标识，占用4字节
- **lock_bits：** 是锁的标志位，占用2个字节
- **biased_lock_bits：** 是是否偏向锁的标识，占用1个字节
- **max_hash_bits：** 是针对无锁计算的hashcode 占用字节数量，如果是32位虚拟机，就是 32 - 4 - 2 -1 = 25 byte，如果是64 位虚拟机，64 - 4 - 2 - 1 = 57 byte，但是会有 25 字节未使用，所以64位的 hashcode 占用 31 byte
- **hash_bits：** 是针对 64 位虚拟机来说，如果最大字节数大于 31，则取31，否则取真实的字节数
- **cms_bits：** 不是64位虚拟机就占用 0 byte，是64位就占用 1byte
- **epoch_bits：** 就是 epoch 所占用的字节大小，2字节。

###### 5.2 Monitor

Monitor 可以理解为一个同步工具或一种同步机制，通常被描述为一个对象。每一个 Java 对象就有一把看不见的锁，称为内部锁或者 Monitor 锁。

Monitor 是线程私有的数据结构，每一个线程都有一个可用 monitor record 列表，同时还有一个全局的可用列表。每一个被锁住的对象都会和一个 monitor 关联，同时 monitor 中有一个 Owner 字段存放拥有该锁的线程的唯一标识，表示该锁被这个线程占用。

`Synchronized`是通过对象内部的一个叫做监视器锁（monitor）来实现的，监视器锁本质又是依赖于底层的操作系统的 Mutex Lock（互斥锁）来实现的。而操作系统实现线程之间的切换需要从用户态转换到核心态，这个成本非常高，状态之间的转换需要相对比较长的时间，这就是为什么 Synchronized 效率低的原因。因此，这种依赖于操作系统 Mutex Lock 所实现的锁我们称之为重量级锁。

随着锁的竞争，锁可以从偏向锁升级到轻量级锁，再升级的重量级锁（但是锁的升级是单向的，也就是说只能从低到高升级，不会出现锁的降级）。JDK 1.6中默认是开启偏向锁和轻量级锁的，我们也可以通过-XX:-UseBiasedLocking=false来禁用偏向锁。

##### 六、锁的分类

###### 6.1 无锁

无锁是指没有对资源进行锁定，所有的线程都能访问并修改同一个资源，但同时只有一个线程能修改成功。

无锁的特点是修改操作会在循环内进行，线程会不断的尝试修改共享资源。如果没有冲突就修改成功并退出，否则就会继续循环尝试。如果有多个线程修改同一个值，必定会有一个线程能修改成功，而其他修改失败的线程会不断重试直到修改成功。

###### 6.2 偏向锁

初次执行到synchronized代码块的时候，锁对象变成偏向锁（通过CAS修改对象头里的锁标志位），字面意思是“偏向于第一个获得它的线程”的锁。执行完同步代码块后，线程并不会主动释放偏向锁。当第二次到达同步代码块时，线程会判断此时持有锁的线程是否就是自己（持有锁的线程ID也在对象头里），如果是则正常往下执行。由于之前没有释放锁，这里也就不需要重新加锁。如果自始至终使用锁的线程只有一个，很明显偏向锁几乎没有额外开销，性能极高。

偏向锁是指当一段同步代码一直被同一个线程所访问时，即不存在多个线程的竞争时，那么该线程在后续访问时便会自动获得锁，从而降低获取锁带来的消耗，即提高性能。

当一个线程访问同步代码块并获取锁时，会在 Mark Word 里存储锁偏向的线程 ID。在线程进入和退出同步块时不再通过 CAS 操作来加锁和解锁，而是检测 Mark Word 里是否存储着指向当前线程的偏向锁。轻量级锁的获取及释放依赖多次 CAS 原子指令，而偏向锁只需要在置换 ThreadID 的时候依赖一次 CAS 原子指令即可。

偏向锁只有遇到其他线程尝试竞争偏向锁时，持有偏向锁的线程才会释放锁，线程是不会主动释放偏向锁的。

关于偏向锁的撤销，需要等待全局安全点，即在某个时间点上没有字节码正在执行时，它会先暂停拥有偏向锁的线程，然后判断锁对象是否处于被锁定状态。如果线程不处于活动状态，则将对象头设置成无锁状态，并撤销偏向锁，恢复到无锁（标志位为01）或轻量级锁（标志位为00）的状态。

###### 6.3 轻量级锁（自旋锁）

![在这里插入图片描述](https://img-blog.csdnimg.cn/20200606123648335.png)

轻量级锁是指当锁是偏向锁的时候，却被另外的线程所访问，此时偏向锁就会升级为轻量级锁，其他线程会通过自旋（关于自旋的介绍见文末）的形式尝试获取锁，线程不会阻塞，从而提高性能。

轻量级锁的获取主要由两种情况：
① 当关闭偏向锁功能时；
② 由于多个线程竞争偏向锁导致偏向锁升级为轻量级锁。

一旦有第二个线程加入锁竞争，偏向锁就升级为轻量级锁（自旋锁）。这里要明确一下什么是锁竞争：如果多个线程轮流获取一个锁，但是每次获取锁的时候都很顺利，没有发生阻塞，那么就不存在锁竞争。只有当某线程尝试获取锁的时候，发现该锁已经被占用，只能等待其释放，这才发生了锁竞争。

在轻量级锁状态下继续锁竞争，没有抢到锁的线程将自旋，即不停地循环判断锁是否能够被成功获取。获取锁的操作，其实就是通过CAS修改对象头里的锁标志位。先比较当前锁标志位是否为“释放”，如果是则将其设置为“锁定”，比较并设置是原子性发生的。这就算抢到锁了，然后线程将当前锁的持有者信息修改为自己。

长时间的自旋操作是非常消耗资源的，一个线程持有锁，其他线程就只能在原地空耗CPU，执行不了任何有效的任务，这种现象叫做忙等（busy-waiting）。如果多个线程用一个锁，但是没有发生锁竞争，或者发生了很轻微的锁竞争，那么synchronized就用轻量级锁，允许短时间的忙等现象。这是一种折衷的想法，短时间的忙等，换取线程在用户态和内核态之间切换的开销。

###### 6.4 重量级锁

重量级锁显然，此忙等是有限度的（有个计数器记录自旋次数，默认允许循环10次，可以通过虚拟机参数更改）。如果锁竞争情况严重，某个达到最大自旋次数的线程，会将轻量级锁升级为重量级锁（依然是CAS修改锁标志位，但不修改持有锁的线程ID）。当后续线程尝试获取锁时，发现被占用的锁是重量级锁，则直接将自己挂起（而不是忙等），等待将来被唤醒。

重量级锁是指当有一个线程获取锁之后，其余所有等待获取该锁的线程都会处于阻塞状态。

简言之，就是所有的控制权都交给了操作系统，由操作系统来负责线程间的调度和线程的状态变更。而这样会出现频繁地对线程运行状态的切换，线程的挂起和唤醒，从而消耗大量的系统资



### 锁优化技术（锁粗化、锁消除）

> **锁粗化**

`锁粗化`就是将多个同步块的数量减少，并将单个同步块的作用范围扩大，本质上就是将多次上锁、解锁的请求合并为一次同步请求。

举个例子，一个循环体中有一个代码同步块，每次循环都会执行加锁解锁操作。

```java
private static final Object LOCK = new Object();

for(int i = 0;i < 100; i++) {
    synchronized(LOCK){
        // do some magic things
    }
}
```

经过`锁粗化`后就变成下面这个样子了：

```java
 synchronized(LOCK){
     for(int i = 0;i < 100; i++) {
        // do some magic things
    }
}
```

> **锁消除**

`锁消除`是指虚拟机编译器在运行时检测到了共享数据没有竞争的锁，从而将这些锁进行消除。

举个例子让大家更好理解。

```java
public String test(String s1, String s2){
    StringBuffer stringBuffer = new StringBuffer();
    stringBuffer.append(s1);
    stringBuffer.append(s2);
    return stringBuffer.toString();
}
```

上面代码中有一个 test 方法，主要作用是将字符串 s1 和字符串 s2 串联起来。

test 方法中三个变量s1, s2, stringBuffer， 它们都是局部变量，局部变量是在栈上的，栈是线程私有的，所以就算有多个线程访问 test 方法也是线程安全的。

我们都知道 StringBuffer 是线程安全的类，append 方法是同步方法，但是 test 方法本来就是线程安全的，为了提升效率，虚拟机帮我们消除了这些同步锁，这个过程就被称为`锁消除`。

```java
StringBuffer.class

// append 是同步方法
public synchronized StringBuffer append(String str) {
    toStringCache = null;
    super.append(str);
    return this;
}
```

---

## 并发多线程面试题

### ***1、多线程有什么用？***

一个可能在很多人看来很扯淡的一个问题：我会用多线程就好了，还管它有什么用？在我看来，这个回答更扯淡。所谓"知其然知其所以然"，"会用"只是"知其然"，"为什么用"才是"知其所以然"，只有达到"知其然知其所以然"的程度才可以说是把一个知识点运用自如。OK，下面说说我对这个问题的看法：

（1）发挥多核CPU的优势

随着工业的进步，现在的笔记本、台式机乃至商用的应用服务器至少也都是双核的，4核、8核甚至16核的也都不少见，如果是单线程的程序，那么在双核CPU上就浪费了50%，在4核CPU上就浪费了75%。**单核CPU上所谓的"多线程"那是假的多线程，同一时间处理器只会处理一段逻辑，只不过线程之间切换得比较快，看着像多个线程"同时"运行罢了**。多核CPU上的多线程才是真正的多线程，它能让你的多段逻辑同时工作，多线程，可以真正发挥出多核CPU的优势来，达到充分利用CPU的目的。

（2）防止阻塞

从程序运行效率的角度来看，单核CPU不但不会发挥出多线程的优势，反而会因为在单核CPU上运行多线程导致线程上下文的切换，而降低程序整体的效率。但是单核CPU我们还是要应用多线程，就是为了防止阻塞。试想，如果单核CPU使用单线程，那么只要这个线程阻塞了，比方说远程读取某个数据吧，对端迟迟未返回又没有设置超时时间，那么你的整个程序在数据返回回来之前就停止运行了。多线程可以防止这个问题，多条线程同时运行，哪怕一条线程的代码执行读取数据阻塞，也不会影响其它任务的执行。

（3）便于建模

这是另外一个没有这么明显的优点了。假设有一个大的任务A，单线程编程，那么就要考虑很多，建立整个程序模型比较麻烦。但是如果把这个大的任务A分解成几个小任务，任务B、任务C、任务D，分别建立程序模型，并通过多线程分别运行这几个任务，那就简单很多了。

### ***2、创建线程的方式***

比较常见的一个问题了，一般就是两种：

（1）继承Thread类

（2）实现Runnable接口

至于哪个好，不用说肯定是后者好，因为实现接口的方式比继承类的方式更灵活，也能减少程序之间的耦合度，**面向接口编程**也是设计模式6大原则的核心。

### ***3、start()方法和run()方法的区别***

只有调用了start()方法，才会表现出多线程的特性，不同线程的run()方法里面的代码交替执行。如果只是调用run()方法，那么代码还是同步执行的，必须等待一个线程的run()方法里面的代码全部执行完毕之后，另外一个线程才可以执行其run()方法里面的代码。

### ***4、Runnable接口和Callable接口的区别***

有点深的问题了，也看出一个Java程序员学习知识的广度。

Runnable接口中的run()方法的返回值是void，它做的事情只是纯粹地去执行run()方法中的代码而已；Callable接口中的call()方法是有返回值的，是一个泛型，和Future、FutureTask配合可以用来获取异步执行的结果。

这其实是很有用的一个特性，因为**多线程相比单线程更难、更复杂的一个重要原因就是因为多线程充满着未知性**，某条线程是否执行了？某条线程执行了多久？某条线程执行的时候我们期望的数据是否已经赋值完毕？无法得知，我们能做的只是等待这条多线程的任务执行完毕而已。而Callable+Future/FutureTask却可以获取多线程运行的结果，可以在等待时间太长没获取到需要的数据的情况下取消该线程的任务，真的是非常有用。

### ***5、CyclicBarrier和CountDownLatch的区别***

两个看上去有点像的类，都在java.util.concurrent下，都可以用来表示代码运行到某个点上，二者的区别在于：

（1）CyclicBarrier的某个线程运行到某个点上之后，该线程即停止运行，直到所有的线程都到达了这个点，所有线程才重新运行；CountDownLatch则不是，某线程运行到某个点上之后，只是给某个数值-1而已，该线程继续运行

（2）CyclicBarrier只能唤起一个任务，CountDownLatch可以唤起多个任务

（3）CyclicBarrier可重用，CountDownLatch不可重用，计数值为0该CountDownLatch就不可再用了

### ***6、volatile关键字的作用***

一个非常重要的问题，是每个学习、应用多线程的Java程序员都必须掌握的。理解volatile关键字的作用的前提是要理解Java内存模型，这里就不讲Java内存模型了，可以参见第31点，volatile关键字的作用主要有两个：

（1）多线程主要围绕可见性和原子性两个特性而展开，使用volatile关键字修饰的变量，保证了其在多线程之间的可见性，即每次读取到volatile变量，一定是最新的数据

（2）代码底层执行不像我们看到的高级语言----Java程序这么简单，它的执行是**Java代码-->字节码-->根据字节码执行对应的C/C++代码-->C/C++代码被编译成汇编语言-->和硬件电路交互**，现实中，为了获取更好的性能JVM可能会对指令进行重排序，多线程下可能会出现一些意想不到的问题。使用volatile则会对禁止语义重排序，当然这也一定程度上降低了代码执行效率

从实践角度而言，volatile的一个重要作用就是和CAS结合，保证了原子性，详细的可以参见java.util.concurrent.atomic包下的类，比如AtomicInteger。

### ***7、什么是线程安全***

又是一个理论的问题，各式各样的答案有很多，我给出一个个人认为解释地最好的：**如果你的代码在多线程下执行和在单线程下执行永远都能获得一样的结果，那么你的代码就是线程安全的**。

这个问题有值得一提的地方，就是线程安全也是有几个级别的：

（1）不可变

像String、Integer、Long这些，都是final类型的类，任何一个线程都改变不了它们的值，要改变除非新创建一个，因此这些不可变对象不需要任何同步手段就可以直接在多线程环境下使用

（2）绝对线程安全

不管运行时环境如何，调用者都不需要额外的同步措施。要做到这一点通常需要付出许多额外的代价，Java中标注自己是线程安全的类，实际上绝大多数都不是线程安全的，不过绝对线程安全的类，Java中也有，比方说CopyOnWriteArrayList、CopyOnWriteArraySet

（3）相对线程安全

相对线程安全也就是我们通常意义上所说的线程安全，像Vector这种，add、remove方法都是原子操作，不会被打断，但也仅限于此，如果有个线程在遍历某个Vector、有个线程同时在add这个Vector，99%的情况下都会出现ConcurrentModificationException，也就是**fail-fast机制**。

（4）线程非安全

这个就没什么好说的了，ArrayList、LinkedList、HashMap等都是线程非安全的类

### ***8、Java中如何获取到线程dump文件***

死循环、死锁、阻塞、页面打开慢等问题，打线程dump是最好的解决问题的途径。所谓线程dump也就是线程堆栈，获取到线程堆栈有两步：

（1）获取到线程的pid，可以通过使用jps命令，在Linux环境下还可以使用ps -ef | grep java

（2）打印线程堆栈，可以通过使用jstack pid命令，在Linux环境下还可以使用kill -3 pid

另外提一点，Thread类提供了一个getStackTrace()方法也可以用于获取线程堆栈。这是一个实例方法，因此此方法是和具体线程实例绑定的，每次获取获取到的是具体某个线程当前运行的堆栈，

### ***9、一个线程如果出现了运行时异常会怎么样***

如果这个异常没有被捕获的话，这个线程就停止执行了。另外重要的一点是：**如果这个线程持有某个某个对象的监视器，那么这个对象监视器会被立即释放**

### ***10、如何在两个线程之间共享数据***

通过在线程之间共享对象就可以了，然后通过wait/notify/notifyAll、await/signal/signalAll进行唤起和等待，比方说阻塞队列BlockingQueue就是为线程之间共享数据而设计的

### ***11、sleep方法和wait方法有什么区别***

这个问题常问，sleep方法和wait方法都可以用来放弃CPU一定的时间，不同点在于如果线程持有某个对象的监视器，sleep方法不会放弃这个对象的监视器，wait方法会放弃这个对象的监视器

### ***12、生产者消费者模型的作用是什么***

这个问题很理论，但是很重要：

（1）**通过平衡生产者的生产能力和消费者的消费能力来提升整个系统的运行效率**，这是生产者消费者模型最重要的作用

（2）解耦，这是生产者消费者模型附带的作用，解耦意味着生产者和消费者之间的联系少，联系越少越可以独自发展而不需要收到相互的制约

### ***13、ThreadLocal有什么用***

简单说ThreadLocal就是一种以**空间换时间**的做法，在每个Thread里面维护了一个以开地址法实现的ThreadLocal.ThreadLocalMap，把数据进行隔离，数据不共享，自然就没有线程安全方面的问题了

### ***14、为什么wait()方法和notify()/notifyAll()方法要在同步块中被调用***

这是JDK强制的，wait()方法和notify()/notifyAll()方法在调用前都必须先获得对象的锁

### ***15、wait()方法和notify()/notifyAll()方法在放弃对象监视器时有什么区别***

wait()方法和notify()/notifyAll()方法在放弃对象监视器的时候的区别在于：**wait()方法立即释放对象监视器，notify()/notifyAll()方法则会等待线程剩余代码执行完毕才会放弃对象监视器**。

### ***16、为什么要使用线程池***

避免频繁地创建和销毁线程，达到线程对象的重用。另外，使用线程池还可以根据项目灵活地控制并发的数目。

### ***17、怎么检测一个线程是否持有对象监视器***

我也是在网上看到一道多线程面试题才知道有方法可以判断某个线程是否持有对象监视器：Thread类提供了一个holdsLock(Object obj)方法，当且仅当对象obj的监视器被某条线程持有的时候才会返回true，注意这是一个static方法，这意味着**"某条线程"指的是当前线程**。

### ***18、synchronized和ReentrantLock的区别***

synchronized是和if、else、for、while一样的关键字，ReentrantLock是类，这是二者的本质区别。既然ReentrantLock是类，那么它就提供了比synchronized更多更灵活的特性，可以被继承、可以有方法、可以有各种各样的类变量，ReentrantLock比synchronized的扩展性体现在几点上：

（1）ReentrantLock可以对获取锁的等待时间进行设置，这样就避免了死锁

（2）ReentrantLock可以获取各种锁的信息

（3）ReentrantLock可以灵活地实现多路通知

另外，二者的锁机制其实也是不一样的。ReentrantLock底层调用的是Unsafe的park方法加锁，synchronized操作的应该是对象头中mark word，这点我不能确定。

### ***19、ConcurrentHashMap的并发度是什么***

ConcurrentHashMap的并发度就是segment的大小，默认为16，这意味着最多同时可以有16条线程操作ConcurrentHashMap，这也是ConcurrentHashMap对Hashtable的最大优势，任何情况下，Hashtable能同时有两条线程获取Hashtable中的数据吗？

### ***20、ReadWriteLock是什么***

首先明确一下，不是说ReentrantLock不好，只是ReentrantLock某些时候有局限。如果使用ReentrantLock，可能本身是为了防止线程A在写数据、线程B在读数据造成的数据不一致，但这样，如果线程C在读数据、线程D也在读数据，读数据是不会改变数据的，没有必要加锁，但是还是加锁了，降低了程序的性能。

因为这个，才诞生了读写锁ReadWriteLock。ReadWriteLock是一个读写锁接口，ReentrantReadWriteLock是ReadWriteLock接口的一个具体实现，实现了读写的分离，**读锁是共享的，写锁是独占的**，读和读之间不会互斥，读和写、写和读、写和写之间才会互斥，提升了读写的性能。

### ***21、FutureTask是什么***

这个其实前面有提到过，FutureTask表示一个异步运算的任务。FutureTask里面可以传入一个Callable的具体实现类，可以对这个异步运算的任务的结果进行等待获取、判断是否已经完成、取消任务等操作。当然，由于FutureTask也是Runnable接口的实现类，所以FutureTask也可以放入线程池中。

### ***22、Linux环境下如何查找哪个线程使用CPU最长***

这是一个比较偏实践的问题，这种问题我觉得挺有意义的。可以这么做：

（1）获取项目的pid，jps或者ps -ef | grep java，这个前面有讲过

（2）top -H -p pid，顺序不能改变

这样就可以打印出当前的项目，每条线程占用CPU时间的百分比。注意这里打出的是LWP，也就是操作系统原生线程的线程号，我笔记本山没有部署Linux环境下的Java工程，因此没有办法截图演示，网友朋友们如果公司是使用Linux环境部署项目的话，可以尝试一下。

使用"top -H -p pid"+"jps pid"可以很容易地找到某条占用CPU高的线程的线程堆栈，从而定位占用CPU高的原因，一般是因为不当的代码操作导致了死循环。

最后提一点，"top -H -p pid"打出来的LWP是十进制的，"jps pid"打出来的本地线程号是十六进制的，转换一下，就能定位到占用CPU高的线程的当前线程堆栈了。

### ***23、Java编程写一个会导致死锁的程序***

第一次看到这个题目，觉得这是一个非常好的问题。很多人都知道死锁是怎么一回事儿：线程A和线程B相互等待对方持有的锁导致程序无限死循环下去。当然也仅限于此了，问一下怎么写一个死锁的程序就不知道了，这种情况说白了就是不懂什么是死锁，懂一个理论就完事儿了，实践中碰到死锁的问题基本上是看不出来的。

真正理解什么是死锁，这个问题其实不难，几个步骤：

（1）两个线程里面分别持有两个Object对象：lock1和lock2。这两个lock作为同步代码块的锁；

（2）线程1的run()方法中同步代码块先获取lock1的对象锁，Thread.sleep(xxx)，时间不需要太多，50毫秒差不多了，然后接着获取lock2的对象锁。这么做主要是为了防止线程1启动一下子就连续获得了lock1和lock2两个对象的对象锁

（3）线程2的run)(方法中同步代码块先获取lock2的对象锁，接着获取lock1的对象锁，当然这时lock1的对象锁已经被线程1锁持有，线程2肯定是要等待线程1释放lock1的对象锁的

这样，线程1"睡觉"睡完，线程2已经获取了lock2的对象锁了，线程1此时尝试获取lock2的对象锁，便被阻塞，此时一个死锁就形成了。代码就不写了，占的篇幅有点多，Java多线程7：死锁这篇文章里面有，就是上面步骤的代码实现。

### ***24、怎么唤醒一个阻塞的线程***

如果线程是因为调用了wait()、sleep()或者join()方法而导致的阻塞，可以中断线程，并且通过抛出InterruptedException来唤醒它；如果线程遇到了IO阻塞，无能为力，因为IO是操作系统实现的，Java代码并没有办法直接接触到操作系统。

### ***25、不可变对象对多线程有什么帮助***

前面有提到过的一个问题，不可变对象保证了对象的内存可见性，对不可变对象的读取不需要进行额外的同步手段，提升了代码执行效率。

### ***26、什么是多线程的上下文切换***

多线程的上下文切换是指CPU控制权由一个已经正在运行的线程切换到另外一个就绪并等待获取CPU执行权的线程的过程。

### ***27、如果你提交任务时，线程池队列已满，这时会发生什么***

这里区分一下：

1. 如果使用的是无界队列LinkedBlockingQueue，也就是无界队列的话，没关系，继续添加任务到阻塞队列中等待执行，因为LinkedBlockingQueue可以近乎认为是一个无穷大的队列，可以无限存放任务
2. 如果使用的是有界队列比如ArrayBlockingQueue，任务首先会被添加到ArrayBlockingQueue中，ArrayBlockingQueue满了，会根据maximumPoolSize的值增加线程数量，如果增加了线程数量还是处理不过来，ArrayBlockingQueue继续满，那么则会使用拒绝策略RejectedExecutionHandler处理满了的任务，默认是AbortPolicy

### ***28、Java中用到的线程调度算法是什么***

抢占式。一个线程用完CPU之后，操作系统会根据线程优先级、线程饥饿情况等数据算出一个总的优先级并分配下一个时间片给某个线程执行。

### ***29、Thread.sleep(0)的作用是什么***

这个问题和上面那个问题是相关的，我就连在一起了。由于Java采用抢占式的线程调度算法，因此可能会出现某条线程常常获取到CPU控制权的情况，为了让某些优先级比较低的线程也能获取到CPU控制权，可以使用Thread.sleep(0)手动触发一次操作系统分配时间片的操作，这也是平衡CPU控制权的一种操作。

### ***30、什么是自旋***

很多synchronized里面的代码只是一些很简单的代码，执行时间非常快，此时等待的线程都加锁可能是一种不太值得的操作，因为线程阻塞涉及到用户态和内核态切换的问题。既然synchronized里面的代码执行得非常快，不妨让等待锁的线程不要被阻塞，而是在synchronized的边界做忙循环，这就是自旋。如果做了多次忙循环发现还没有获得锁，再阻塞，这样可能是一种更好的策略。

### ***31、什么是Java内存模型***

Java内存模型定义了一种多线程访问Java内存的规范。Java内存模型要完整讲不是这里几句话能说清楚的，我简单总结一下Java内存模型的几部分内容：

（1）Java内存模型将内存分为了**主内存和工作内存**。类的状态，也就是类之间共享的变量，是存储在主内存中的，每次Java线程用到这些主内存中的变量的时候，会读一次主内存中的变量，并让这些内存在自己的工作内存中有一份拷贝，运行自己线程代码的时候，用到这些变量，操作的都是自己工作内存中的那一份。在线程代码执行完毕之后，会将最新的值更新到主内存中去

（2）定义了几个原子操作，用于操作主内存和工作内存中的变量

（3）定义了volatile变量的使用规则

（4）happens-before，即先行发生原则，定义了操作A必然先行发生于操作B的一些规则，比如在同一个线程内控制流前面的代码一定先行发生于控制流后面的代码、一个释放锁unlock的动作一定先行发生于后面对于同一个锁进行锁定lock的动作等等，只要符合这些规则，则不需要额外做同步措施，如果某段代码不符合所有的happens-before规则，则这段代码一定是线程非安全的

### ***32、什么是CAS***

CAS，全称为Compare and Swap，即比较-替换。假设有三个操作数：**内存值V、旧的预期值A、要修改的值B，当且仅当预期值A和内存值V相同时，才会将内存值修改为B并返回true，否则什么都不做并返回false**。当然CAS一定要volatile变量配合，这样才能保证每次拿到的变量是主内存中最新的那个值，否则旧的预期值A对某条线程来说，永远是一个不会变的值A，只要某次CAS操作失败，永远都不可能成功。

### ***33、什么是乐观锁和悲观锁***

（1）乐观锁：就像它的名字一样，对于并发间操作产生的线程安全问题持乐观状态，乐观锁认为竞争不总是会发生，因此它不需要持有锁，将**比较-替换**这两个动作作为一个原子操作尝试去修改内存中的变量，如果失败则表示发生冲突，那么就应该有相应的重试逻辑。

（2）悲观锁：还是像它的名字一样，对于并发间操作产生的线程安全问题持悲观状态，悲观锁认为竞争总是会发生，因此每次对某资源进行操作时，都会持有一个独占的锁，就像synchronized，不管三七二十一，直接上了锁就操作资源了。

### ***34、什么是AQS***

简单说一下AQS，AQS全称为AbstractQueuedSychronizer，翻译过来应该是抽象队列同步器。

如果说java.util.concurrent的基础是CAS的话，那么AQS就是整个Java并发包的核心了，ReentrantLock、CountDownLatch、Semaphore等等都用到了它。AQS实际上以双向队列的形式连接所有的Entry，比方说ReentrantLock，所有等待的线程都被放在一个Entry中并连成双向队列，前面一个线程使用ReentrantLock好了，则双向队列实际上的第一个Entry开始运行。

AQS定义了对双向队列所有的操作，而只开放了tryLock和tryRelease方法给开发者使用，开发者可以根据自己的实现重写tryLock和tryRelease方法，以实现自己的并发功能。

### ***35、单例模式的线程安全性***

老生常谈的问题了，首先要说的是单例模式的线程安全意味着：**某个类的实例在多线程环境下只会被创建一次出来**。单例模式有很多种的写法，我总结一下：

（1）饿汉式单例模式的写法：线程安全

（2）懒汉式单例模式的写法：非线程安全

（3）双检锁单例模式的写法：线程安全

### ***36、Semaphore有什么作用***

Semaphore就是一个信号量，它的作用是**限制某段代码块的并发数**。Semaphore有一个构造函数，可以传入一个int型整数n，表示某段代码最多只有n个线程可以访问，如果超出了n，那么请等待，等到某个线程执行完毕这段代码块，下一个线程再进入。由此可以看出如果Semaphore构造函数中传入的int型整数n=1，相当于变成了一个synchronized了。

### ***37、Hashtable的size()方法中明明只有一条语句"return count"，为什么还要做同步？***

这是我之前的一个困惑，不知道大家有没有想过这个问题。某个方法中如果有多条语句，并且都在操作同一个类变量，那么在多线程环境下不加锁，势必会引发线程安全问题，这很好理解，但是size()方法明明只有一条语句，为什么还要加锁？

关于这个问题，在慢慢地工作、学习中，有了理解，主要原因有两点：

（1）**同一时间只能有一条线程执行固定类的同步方法，但是对于类的非同步方法，可以多条线程同时访问**。所以，这样就有问题了，可能线程A在执行Hashtable的put方法添加数据，线程B则可以正常调用size()方法读取Hashtable中当前元素的个数，那读取到的值可能不是最新的，可能线程A添加了完了数据，但是没有对size++，线程B就已经读取size了，那么对于线程B来说读取到的size一定是不准确的。而给size()方法加了同步之后，意味着线程B调用size()方法只有在线程A调用put方法完毕之后才可以调用，这样就保证了线程安全性

（2）**CPU执行代码，执行的不是Java代码，这点很关键，一定得记住**。Java代码最终是被翻译成机器码执行的，机器码才是真正可以和硬件电路交互的代码。**即使你看到Java代码只有一行，甚至你看到Java代码编译之后生成的字节码也只有一行，也不意味着对于底层来说这句语句的操作只有一个**。一句"return count"假设被翻译成了三句汇编语句执行，一句汇编语句和其机器码做对应，完全可能执行完第一句，线程就切换了。

### ***38、线程类的构造方法、静态块是被哪个线程调用的***

这是一个非常刁钻和狡猾的问题。请记住：线程类的构造方法、静态块是被new这个线程类所在的线程所调用的，而run方法里面的代码才是被线程自身所调用的。

如果说上面的说法让你感到困惑，那么我举个例子，假设Thread2中new了Thread1，main函数中new了Thread2，那么：

（1）Thread2的构造方法、静态块是main线程调用的，Thread2的run()方法是Thread2自己调用的

（2）Thread1的构造方法、静态块是Thread2调用的，Thread1的run()方法是Thread1自己调用的

### ***39、同步方法和同步块，哪个是更好的选择***

同步块，这意味着同步块之外的代码是异步执行的，这比同步整个方法更提升代码的效率。请知道一条原则：**同步的范围越小越好**。

借着这一条，我额外提一点，虽说同步的范围越少越好，但是在Java虚拟机中还是存在着一种叫做**锁粗化**的优化方法，这种方法就是把同步范围变大。这是有用的，比方说StringBuffer，它是一个线程安全的类，自然最常用的append()方法是一个同步方法，我们写代码的时候会反复append字符串，这意味着要进行反复的加锁->解锁，这对性能不利，因为这意味着Java虚拟机在这条线程上要反复地在内核态和用户态之间进行切换，因此Java虚拟机会将多次append方法调用的代码进行一个锁粗化的操作，将多次的append的操作扩展到append方法的头尾，变成一个大的同步块，这样就减少了加锁-->解锁的次数，有效地提升了代码执行的效率。

### ***40、高并发、任务执行时间短的业务怎样使用线程池？并发不高、任务执行时间长的业务怎样使用线程池？并发高、业务执行时间长的业务怎样使用线程池？***

这是我在并发编程网上看到的一个问题，把这个问题放在最后一个，希望每个人都能看到并且思考一下，因为这个问题非常好、非常实际、非常专业。关于这个问题，个人看法是：

（1）高并发、任务执行时间短的业务，线程池线程数可以设置为CPU核数+1，减少线程上下文的切换

（2）并发不高、任务执行时间长的业务要区分开看：

a）假如是业务时间长集中在IO操作上，也就是IO密集型的任务，因为IO操作并不占用CPU，所以不要让所有的CPU闲下来，可以加大线程池中的线程数目，让CPU处理更多的业务

b）假如是业务时间长集中在计算操作上，也就是计算密集型任务，这个就没办法了，和（1）一样吧，线程池中的线程数设置得少一些，减少线程上下文的切换

（3）并发高、业务执行时间长，解决这种类型任务的关键不在于线程池而在于整体架构的设计，看看这些业务里面某些数据是否能做缓存是第一步，增加服务器是第二步，至于线程池的设置，设置参考（2）。最后，业务执行时间长的问题，也可能需要分析一下，看看能不能使用中间件对任务进行拆分和解耦。

---

# JVM虚拟机

## JVM GC机制

使用Java快一年时间了，从最早大学时候对Java的憎恶，到逐渐接受，到工作中体会到了Java开发的各种便捷与福利，这确实是一门不错的开发语言。不仅是 `Intellij`开发Java程序的爽快，还有无需手动管理内存的便捷、 `Maven`管理依赖的整洁、 `SpringCloud(SpringBoot)` 大礼包的规整等等。

所以，作为一个有追求的Java程序员，深入底层掌握 `GC`（垃圾回收）的机制，应该算是必备的技能了。本文即我在学习过程中的一些个人观点以及心得，不正之处敬请指正。

------

**JVM的运行数据区**

首先我简单来画一张 `JVM`的结构原理图：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrARrKmlZyPx5e1cBmeylsU9WrPMiaK6uKtsrwEcjrVtRUqNvRkXIT44OeSZLqT8a3ic9fx0CAa6SSQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

我们重点关注 `JVM`在运行时的数据区，你可以看到在程序运行时，大致有5个部分：

- **方法区**

不止是存“方法”，而是存储整个 `class`文件的信息，JVM运行时，类加载器子系统将会提取 `class`文件里面的类信息，并将其存放在方法区中。例如类的名称、类的类型（枚举、类、接口）、字段、方法等等。

- **堆（ `Heap`）**

熟悉 `c/c++`编程的同学们应该相当熟悉 `Heap`了，而对于Java而言，每个应用都唯一对应一个JVM实例，而每一个JVM实例唯一对应一个堆。堆主要包括关键字 `new`的对象实例、 `this`指针，或者数组都放在堆中，并由应用所有的线程共享。堆由JVM的自动内存管理机制所管理，名为垃圾回收—— `GC（garbage collection）`。

- **栈（ `Stack`）**

操作系统内核为某个进程或者线程建立的存储区域,它保存着一个线程中的方法的调用状态，它具有先进后出的特性。在栈中的数据大小与生命周期严格来说都是确定的，例如在一个函数中声明的int变量便是存储在 `stack`中，它的大小是固定的，在函数退出后它的生命周期也从此结束。在栈中，每一个方法对应一个栈帧，JVM会对Java栈执行两种操作：压栈和出栈。这两种操作在执行时都是以栈帧为单位的。还有一些即时编译器编译后的代码等数据。

- **PC寄存器**

pc寄存器用于存放一条指令的地址，每一个线程都有一个PC寄存器。

- **本地方法栈**

用来调用其他语言的本地方法，例如 `C/C++`写的本地代码， 这些方法在本地方法栈中执行，而不会在Java栈中执行。

------

**初识GC**

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrARrKmlZyPx5e1cBmeylsUiaOKxJHbFyoWTPFPHUuhk1xtlcdiaVfqWtsU7c9s6ybKuwwMPBmy8t7Q/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

自动垃圾回收机制，简单来说就是寻找 Java堆中的无用对象。打个比方：你的房间是JVM的内存，你在房间里生活会制造垃圾和脏乱，而你妈就是 `GC`（听起来有点像骂人）。你妈每时每刻都觉得你房间很脏乱，不时要把你赶出门打扫房间，如果你妈一直在房间打扫，那么这个过程你无法继续在房间打游戏吃泡面。但如果你一直在房间，你的房间早晚要变成一个无法居住的猪窝。

那么，怎么样回收垃圾比较好呢？我们大致可以想出下面的思路：

- **Marking**

首先，所有堆中的对象都会被扫描一遍：我们总得知道哪些是垃圾，哪些是有用的物品吧。因为垃圾实在太多了，所以，你妈会把所有的要扔掉的东西都找出来并打上一个标签，到了时机成熟时回头来一起处理，这样她就能处理你不需要的废物、旧家具，而不是把你喜欢的衣服或者身份证之类的东西扔掉。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrARrKmlZyPx5e1cBmeylsUhE8XibSX46g3sGvhOb9rE2u4fwGFoqVzRIc7PFIvtnjZFDG8nibickS1Q/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- **Normal Deletion**

垃圾收集器将清除掉标记的对象：你妈已经整理了一部分杂物（或者已全部整理完），然后会将他们直接拎出去倒掉。你很开心房间又可以继续接受蹂躏了。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrARrKmlZyPx5e1cBmeylsURI9KhZQN8VwbNziaZgAncWXCQeFvUTVYOY4Sa1aUthcqSBdHAB9CKBA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- **Deletion with Compacting**

压缩清除的方法：我们知道，内存有空闲，并不代表着我们就能使用它，例如我们要分配数组这种一段连续空间，假如内存中碎片较多，肯定是行不通的。正如房间可能需要再放一个新的床，但是扔掉旧衣柜后，原来的位置并不能放得下新床，所以需要进行空间压缩，把剩下的家具和物品位置并到一起，这样就能腾出更多的空间啦。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrARrKmlZyPx5e1cBmeylsUu3nkhxqK2ibgg9G3ib6K7nlFOBTR2gQATBtiay4NaB7ETvfaRmOLzJQYQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

有趣的是，JVM并不是使用类似于 `objective-c`的 `ARC（AutomaticReferenceCounting）`的方式来引用计数对象，而是使用了叫根搜索算法( `GC Root`)的方法，基本思想就是选定一些对象作为 `GC Roots`，并组成根对象集合，然后从这些作为 `GC Roots`的对象作为起始点，搜索所走过的引用链（ `ReferenceChain`）。如果目标对象到 `GC Roots`是连接着的，我们则称该目标对象是可达的，如果目标对象不可达，则说明目标对象是可以被回收的对象。



`GC Root`使用的算法是相当复杂的，你不必记住里面的所有细节。但是你要知道的一点就是，可以作为 `GC Root`的对象可以主要分为四种：

```
1、JVM栈中引用的对象； 
2、方法区中，静态属性引用的对象； 
3、方法区中，常量引用的对象； 
4、本地方法栈中，JNI（即Native方法）引用的对象；
```

在 `JDK1.2`之后，Java将引用分为强引用、软引用、弱引用、虚引用4种，这4种引用强度依次减弱。

![img](https://pic1.zhimg.com/v2-070257fbe3358484b0ef2282bd8a732c_b.jpg)

------

**分代与GC机制**

嗯，听起来这样就可以了？但是实际情况下，很不幸，在JVM中绝大部分对象都是英年早逝的，在编码时大部分堆中的内存都是短暂临时分配的，所以无论是效率还是开销方面，按上面那样进行 `GC`往往是无法满足我们需求的。而且，实际上随着分配的对象增多， `GC`的时间与开销将会放大。所以，JVM的内存被分为了三个主要部分：新生代，老年代和永久代。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrARrKmlZyPx5e1cBmeylsUjW1FfDlPXNTtib3sGSZ7H7gF6fY8Poz1dTwXnXjAhgj1ZEJ0lmjGzog/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- **新生代**

所有新产生的对象全部都在新生代中， `Eden`区保存最新的对象，有两个 `SurvivorSpace`—— `S1`和 `S0`，三个区域的比例大致为 `8:1:1`。当新生代的 `Eden`区满了，将触发一次 `GC`，我们把新生代中的 `GC`称为 `minor garbage collections`。 `minor garbage collections`是一种 `Stop the world`事件，比如你妈在打扫时，会把你赶出去，而不是你一边扔垃圾她一边打扫。

我们来看下对象在堆中的分配过程，首先有新的对象进入时，默认放入新生代的 `Eden`区， `S`区都是默认为空的。下面对象的数字代表经历了多少次 `GC`，也就是对象的年龄。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrARrKmlZyPx5e1cBmeylsU22OYbaefvkkUUia65JN2a9BjtTf00gn46U0KO0AobtkydhAyEvT14ibg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

当 `eden`区满了，触发 `minor garbage collections`，这时还有被引用的对象，就会被分配到 `S0`区域，剩下没有被引用的对象就都会被清除。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrARrKmlZyPx5e1cBmeylsU9hAn1nDavCn3ictaQ2iaUETG4xWv8LzJqPGFQYChqictOOX2sj26vm7ww/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

再一次 `GC`时， `S0`区的部分对象很可能会出现没有引用的，被引用的对象以及 `S0`中的存活对象，会被一起移动到 `S1`中。 `eden`和 `S0`中的未引用对象会被全部清除。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrARrKmlZyPx5e1cBmeylsUI0DnEmMiaD6s1EslvlXJ9dicImoNEplvIwjw6Dia4VO7nrGSvMHXxNAlw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

接下来就是无限循环上面的步骤了，当新生代中存活的对象超过了一定的【年龄】，会被分配至老年代的 `Tenured`区中。这个年龄可以通过参数 `MaxTenuringThreshold`设定，默认值为 `15`，图中的例子为 `8`次。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrARrKmlZyPx5e1cBmeylsUPLmVRyAtkxoO5NiauUskicYsh2xIQQMBpCapzECBW7ia0JiacQ4AGVOV4g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

新生代管理内存采用的算法为 `GC`复制算法( `CopyingGC`)，也叫标记-复制法，原理是把内存分为两个空间:一个 `From`空间，一个 `To`空间，对象一开始只在 `From`空间分配， `To`空间是空闲的。 `GC`时把存活的对象从 `From`空间复制粘贴到 `To`空间，之后把 `To`空间变成新的 `From`空间，原来的 `From`空间变成 `To`空间。

首先标记不可达对象：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrARrKmlZyPx5e1cBmeylsUPnXZMrERJ0TOBsqv9q28ovsWJibxW1ZVJwJsYJkJ7PCiaKkMtKy5QwOA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

然后移动存活的对象到 `to`区，并保证他们在内存中连续：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrARrKmlZyPx5e1cBmeylsUhl6yjibDibxGlzpq7wazGIXbUVX3xZibicqeGAs6XtXN2Te4f3wI0iaFxjA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

清扫垃圾：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrARrKmlZyPx5e1cBmeylsUzGTicqWW8hmiahibibIE8SdFllssA4z3PqJ78NXljPu4K5TY8KLMpXuueQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

可以看到上图操作后内存几乎都是连续的，所以它的效率是非常高的，但是相对的吞吐量会较大。并且，把内存一分为二，占用了将近一半的可用内存。用一段伪代码来实现大致为下：

```java
void copying(){        
    $free = $to_start // $free表示To区占用偏移量，每复制成功一个对象obj,                          
        // $free向前移动size(obj)        
        for(r : $roots)            
            *r = copy(*r) // 复制成功后返回新的引用
        swap($from_start, $to_start) // GC完成后交互From区与To区的指针    
}
```

- **老年代**

老年代用来存储活时间较长的对象，老年代区域的 `GC`是 `major garbage collection`，老年代中的内存不够时，就会触发一次。这也是一个 `Stop the world`事件，但是看名字就知道，这个回收过程会相当慢，因为这包括了对新生代和老年代所有对象的回收，也叫 `FullGC`。

老年代管理内存最早采用的算法为标记-清理算法，这个算法很好理解，结合 `GC Root`的定义，我们会把所有不可达的对象全部标记进行清除。

在清除前，黄色的为不可达对象：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrARrKmlZyPx5e1cBmeylsUkkBueKr362vJqqlNqpdKnKl8wuiavItkyh73rDC6gDzEfTz9mLLCiclw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

在清除后，全部都变成可达对象：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrARrKmlZyPx5e1cBmeylsUfZDmAhnWuARwTxM9hxUAJJ4ELpvdibo15AT9ibYPHHxdteRdL3kDXpjA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

那么，这个算法的劣势很好理解：对，会在标记清除的过程中产生大量的内存碎片，Java在分配内存时通常是按连续内存分配，这样我们会浪费很多内存。所以，现在的 `JVM GC`在老年代都是使用标记-压缩清除方法，将上图在清除后的内存进行整理和压缩，以保证内存连续，虽然这个算法的效率是三种算法里最低的。



- **永久代**

永久代位于方法区，主要存放元数据，例如 `Class`、 `Method`的元信息，与 `GC`要回收的对象其实关系并不是很大，我们可以几乎忽略其对 `GC`的影响。除了 `JavaHotSpot`这种较新的虚拟机技术，会回收无用的常量和的类，以免大量运用反射这类频繁自定义 `ClassLoader`的操作时方法区溢出。

------

**GC收集器与优化**

一般而言， `GC`不应该成为影响系统性能的瓶颈，我们在评估 `GC`收集器的优劣时一般考虑以下几点：

```java
（1）吞吐量（2）GC开销（3）暂停时间（4）GC频率（5）堆空间（6）对象生命周期
```

所以针对不同的 `GC`收集器，我们要对应我们的应用场景来进行选择和调优，回顾 `GC`的历史，主要有 `4`种 `GC`收集器: `Serial`、 `Parallel`、 `CMS`和 `G1`。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrARrKmlZyPx5e1cBmeylsUPJjic2js9XxQ805UI6Dx5p8VqB2Bt4Ud9o8vg3JiascicmiaOxlWyfKUBQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- **Serial**

`Serial`收集器使用了标记-复制的算法，可以用 `-XX:+UseSerialGC`使用单线程的串行收集器。但是在 `GC`进行时，程序会进入长时间的暂停时间，一般不太建议使用。

- **Parallel**

`-XX:+UseParallelGC-XX:+UseParallelOldGCParallel`也使用了标记-复制的算法，但是我们称之为吞吐量优先的收集器，因为 `Parallel`最主要的优势在于并行使用多线程去完成垃圾清理工作，这样可以充分利用多核的特性，大幅降低 `gc`时间。当你的程序场景吞吐量较大，例如消息队列这种应用，需要保证有效利用 `CPU`资源，可以忍受一定的停顿时间，可以优先考虑这种方式。

- **CMS ( `ConcurrentMarkSweep`)**

`-XX:+UseParNewGC-XX:+UseConcMarkSweepGCCMS`使用了标记-清除的算法，当应用尤其重视服务器的响应速度（比如 `Apiserver`），希望系统停顿时间最短，以给用户带来较好的体验，那么可以选择 `CMS`。 `CMS`收集器在 `MinorGC`时会暂停所有的应用线程，并以多线程的方式进行垃圾回收。在 `FullGC`时不暂停应用线程，而是使用若干个后台线程定期的对老年代空间进行扫描，及时回收其中不再使用的对象。

- **G1（ `GarbageFirst`）**

`-XX:+UseG1GC` 在堆比较大的时候，如果 `full gc`频繁，会导致停顿，并且调用方阻塞、超时、甚至雪崩的情况出现，所以降低 `full gc`的发生频率和需要时间，非常有必要。 `G1`的诞生正是为了降低 `FullGC`的次数，而相较于 `CMS`， `G1`使用了标记-压缩清除算法，这可以大大降低较大内存（ `4GB`以上） `GC`时产生的内存碎片。

`G1`提供了两种 `GC`模式， `YoungGC`和 `MixedGC`，两种都是 `StopTheWorld(STW)`的。 `YoungGC`主要是对 `Eden`区进行 `GC`， `MixGC`不仅进行正常的新生代垃圾收集，同时也回收部分后台扫描线程标记的老年代分区。

另外有趣的一点， `G1`将新生代、老年代的物理空间划分取消了，而是将堆划分为若干个区域（ `region`），每个大小都为 `2`的倍数且大小全部一致，最多有 `2000`个。除此之外， `G1`专门划分了一个 `Humongous`区，它用来专门存放超过一个 `region 50%`大小的巨型对象。在正常的处理过程中，对象从一个区域复制到另外一个区域，同时也完成了堆的压缩。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzrARrKmlZyPx5e1cBmeylsUic2hXEymC7Ks7ic30xZPm9APu0Oh3AMPU4jhE9Xqmvjzr3N4xy6uVHjQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

- **常用参数**

```
-XX:+UseSerialGC：在新生代和老年代使用串行收集器
-XX:+UseParNewGC：在新生代使用并行收集器
-XX:+UseParallelGC ：新生代使用并行回收收集器，更加关注吞吐量
-XX:+UseParallelOldGC：老年代使用并行回收收集器
-XX:ParallelGCThreads：设置用于垃圾回收的线程数
-XX:+UseConcMarkSweepGC：新生代使用并行收集器，老年代使用CMS+串行收集器
-XX:ParallelCMSThreads：设定CMS的线程数量
-XX:+UseG1GC：启用G1垃圾回收器
```

- **调优**

针对调优这块，推荐可以看下美团点评的这个GC实战调优案例：《从实际案例聊聊Java应用的GC优化》。里面详细讲述了 `MinorGC`频繁、 `CMS`高峰期性能下降、 `Stop-The-World`三个应用场景的问题分析过程，以及在应用程序编码优化空间不大的情况下，如何通过 `GC`优化令系统性能达到一个质的提升。



## JVM垃圾回收18问

GC 对于Java 来说重要性不言而喻，不论是平日里对 JVM 的调优还是面试中的无情轰炸。

这篇文章会以一问一答的方式来展开有关 GC 的内容。

本文章所说的 GC 实现没有特殊说明的话，默认指的是 HotSpot 的。

我先将十八个问题都列出来，大家可以先思考下能答出几道。

![图片](https://mmbiz.qpic.cn/mmbiz_png/eSdk75TK4nE655lRBLg0LibQPnOTe3hRQTMZzUmfI0Y6mADqH3AA41Cnw4TTPtUjVofCCJWy7tFFXpUiaHeFOD2Q/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

好了，开始表演。

### young gc、old gc、full gc、mixed gc 傻傻分不清？

这个问题的前置条件是你得知道 GC 分代，为什么分代。这个在之前文章提了，不清楚的可以去看看。

现在我们来回答一下这个问题。

其实 GC 分为两大类，分别是 Partial GC 和 Full GC。

Partial GC 即部分收集，分为 young gc、old gc、mixed gc。

- young gc：指的是单单收集年轻代的 GC。
- old gc：指的是单单收集老年代的 GC。
- mixed gc：这个是 G1 收集器特有的，指的是收集整个年轻代和部分老年代的 GC。

Full GC 即整堆回收，指的是收取整个堆，包括年轻代、老年代，如果有永久代的话还包括永久代。

其实还有 Major GC 这个名词，在《深入理解Java虚拟机》中这个名词指代的是单单老年代的 GC，也就是和 old gc 等价的，不过也有很多资料认为其是和 full gc 等价的。

还有 Minor GC，其指的就是年轻代的 gc。

### young gc 触发条件是什么？

大致上可以认为在年轻代的 eden 快要被占满的时候会触发 young gc。

为什么要说大致上呢？因为有一些收集器的回收实现是在 full gc 前会让先执行以下 young gc。

比如 Parallel Scavenge，不过有参数可以调整让其不进行 young gc。

可能还有别的实现也有这种操作，不过正常情况下就当做 eden 区快满了即可。

eden 快满的触发因素有两个，一个是为对象分配内存不够，一个是为` TLAB `分配内存不够。

### full gc 触发条件有哪些？

这个触发条件稍微有点多，我们来看下。

- 在要进行 young gc 的时候，根据之前统计数据发现年轻代平均晋升大小比现在老年代剩余空间要大，那就会触发 full gc。
- 有永久代的话如果永久代满了也会触发 full gc。
- 老年代空间不足，大对象直接在老年代申请分配，如果此时老年代空间不足则会触发 full gc。
- 担保失败即 promotion failure，新生代的 to 区放不下从 eden 和 from 拷贝过来对象，或者新生代对象 gc 年龄到达阈值需要晋升这两种情况，老年代如果放不下的话都会触发 full gc。
- 执行 System.gc()、jmap -dump 等命令会触发 full gc。

### 知道 TLAB 吗？来说说看

这个得从内存申请说起。

一般而言生成对象需要向堆中的新生代申请内存空间，而堆又是全局共享的，像新生代内存又是规整的，是通过一个指针来划分的。

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

内存是紧凑的，新对象创建指针就右移对象大小 size 即可，这叫指针加法（bump [up] the pointer）。

可想而知如果多个线程都在分配对象，那么这个指针就会成为热点资源，需要互斥那分配的效率就低了。

于是搞了个 TLAB（Thread Local Allocation Buffer），为一个线程分配的内存申请区域。

这个区域只允许这一个线程申请分配对象，允许所有线程访问这块内存区域。

TLAB 的思想其实很简单，就是划一块区域给一个线程，这样每个线程只需要在自己的那亩地申请对象内存，不需要争抢热点指针。

当这块内存用完了之后再去申请即可。

这种思想其实很常见，比如分布式发号器，每次不会一个一个号的取，会取一批号，用完之后再去申请一批。

![图片](https://mmbiz.qpic.cn/mmbiz_png/eSdk75TK4nE655lRBLg0LibQPnOTe3hRQXZD4mBH91MBZCxiaibLUz9h3fMnH5cjhmgPzwwia3qYfos1RrzubCuw9A/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

可以看到每个线程有自己的一块内存分配区域，短一点的箭头代表 TLAB 内部的分配指针。

如果这块区域用完了再去申请即可。

不过每次申请的大小不固定，会根据该线程启动到现在的历史信息来调整，比如这个线程一直在分配内存那么 TLAB 就大一些，如果这个线程基本上不会申请分配内存那 TLAB 就小一些。

还有 TLAB 会浪费空间，我们来看下这个图。

![图片](https://mmbiz.qpic.cn/mmbiz_png/eSdk75TK4nE655lRBLg0LibQPnOTe3hRQHNibia5nic50R95mR19xUh5ZJzMYS7XhT4uvhHQU3SV4sahjllAfAw5cQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

可以看到 TLAB 内部只剩一格大小，申请的对象需要两格，这时候需要再申请一块 TLAB ，之前的那一格就浪费了。

在 HotSpot 中会生成一个填充对象来填满这一块，因为堆需要线性遍历，遍历的流程是通过对象头得知对象的大小，然后跳过这个大小就能找到下一个对象，所以不能有空洞。

当然也可以通过空闲链表等外部记录方式来实现遍历。

还有 TLAB 只能分配小对象，大的对象还是需要在共享的 eden 区分配。

所以总的来说 TLAB 是为了避免对象分配时的竞争而设计的。

### 那 PLAB 知道吗？

可以看到和 TLAB 很像，PLAB 即 Promotion Local Allocation Buffers。

用在年轻代对象晋升到老年代时。

在多线程并行执行 YGC 时，可能有很多对象需要晋升到老年代，此时老年代的指针就“热”起来了，于是搞了个 PLAB。

先从老年代 freelist（空闲链表） 申请一块空间，然后在这一块空间中就可以通过指针加法（bump the pointer）来分配内存，这样对 freelist 竞争也少了，分配空间也快了。

![图片](https://mmbiz.qpic.cn/mmbiz_png/eSdk75TK4nE655lRBLg0LibQPnOTe3hRQDeNCic3qO6IuTrPVrYjlrEm0d6FR77rtNyYA4HVlghDlPS09HA8b2mQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

大致就是上图这么个思想，每个线程先申请一块作为 PLAB ，然后在这一块内存里面分配晋升的对象。

这和 TLAB 的思想相似。

### 产生 concurrent mode failure 真正的原因

> 《深入理解Java虚拟机》：由于CMS收集器无法处理“浮动垃圾”（FloatingGarbage），有可能出现“Con-current Mode Failure”失败进而导致另一次完全“Stop The World”的Full GC的产生。

这段话的意思是因为抛这个错而导致一次 Full GC。

而实际上是 Full GC 导致抛这个错，我们来看一下源码，版本是 openjdk-8。

首先搜一下这个错。

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

再找找看  `report_concurrent_mode_interruption` 被谁调用。

查到是在 `void CMSCollector::acquire_control_and_collect(...)` 这个方法中被调用的。

![图片](https://mmbiz.qpic.cn/mmbiz_png/eSdk75TK4nE655lRBLg0LibQPnOTe3hRQicTSGk0dkop0icbpVnqa0t1kbvGITzEvYcPFcH7yshiaY5QW8UK118plQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

再来看看 first_state ：`CollectorState first_state = _collectorState;`

![图片](https://mmbiz.qpic.cn/mmbiz_png/eSdk75TK4nE655lRBLg0LibQPnOTe3hRQUmhG2Q2wujagtqVtlKhUNpKn6WeAgQTu9GXRlH7oqtZ16nsYGVPVmA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

看枚举已经很清楚了，就是在 cms gc 还没结束的时候。

而 `acquire_control_and_collect` 这个方法是 cms 执行 foreground gc 的。

cms 分为  foreground gc 和 background gc。

foreground 其实就是 Full gc。

因此是 full gc 的时候 cms gc 还在进行中导致抛这个错。

究其原因是因为分配速率太快导致堆不够用，回收不过来因此产生 full gc。

也有可能是发起 cms gc 设置的堆的阈值太高。

### CMS GC 发生 concurrent mode failure 时的 full GC 为什么是单线程的?

以下的回答来自 R 大。

因为没足够开发资源，偷懒了。就这么简单。没有任何技术上的问题。大公司都自己内部做了优化。

所以最初怎么会偷这个懒的呢？多灾多难的CMS GC经历了多次动荡。它最初是作为Sun Labs的Exact VM的低延迟GC而设计实现的。

但 Exact VM在与 HotSpot VM争抢 Sun 的正牌 JVM 的内部斗争中失利，CMS GC 后来就作为 Exact VM 的技术遗产被移植到了 HotSpot VM上。

就在这个移植还在进行中的时候，Sun 已经开始略显疲态；到 CMS GC 完全移植到 HotSpot VM 的时候，Sun 已经处于快要不行的阶段了。

开发资源减少，开发人员流失，当时的 HotSpot VM 开发组能够做的事情并不多，只能挑重要的来做。而这个时候 Sun Labs 的另一个 GC 实现，Garbage-First GC（G1 GC）已经面世。

相比可能在长时间运行后受碎片化影响的 CMS，G1 会增量式的整理/压缩堆里的数据，避免受碎片化影响，因而被认为更具潜力。

于是当时本来就不多的开发资源，一部分还投给了把G1 GC产品化的项目上——结果也是进展缓慢。

毕竟只有一两个人在做。所以当时就没能有足够开发资源去打磨 CMS GC 的各种配套设施的细节，配套的备份 full GC 的并行化也就耽搁了下来。

但肯定会有同学抱有疑问：HotSpot VM不是已经有并行GC了么？而且还有好几个？

让我们来看看：

- ParNew：并行的young gen GC，不负责收集old gen。
- Parallel GC（ParallelScavenge）：并行的young gen GC，与ParNew相似但不兼容；同样不负责收集old gen。
- ParallelOld GC（PSCompact）：并行的full GC，但与ParNew / CMS不兼容。

所以…就是这么一回事。

HotSpot VM 确实是已经有并行 GC 了，但两个是只负责在 young GC 时收集 young gen 的，这俩之中还只有 ParNew 能跟 CMS 搭配使用；

而并行 full GC 虽然有一个 ParallelOld，但却与 CMS GC 不兼容所以无法作为它的备份 full GC使用。

### 为什么有些新老年代的收集器不能组合使用比如 ParNew 和 Parallel Old？

![图片](https://mmbiz.qpic.cn/mmbiz_png/eSdk75TK4nE655lRBLg0LibQPnOTe3hRQrelqw9LEuuYibQGeq1yQ7S7AicEHjta1iazjxHT8NZeTgficKd2TI0SWhQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这张图是 2008 年 HotSpot 一位 GC 组成员画的，那时候 G1 还没问世，在研发中，所以画了个问号在上面。

里面的回答是 :

> "ParNew" is written in a style... "Parallel Old" is not written in the "ParNew" style

HotSpot VM 自身的分代收集器实现有一套框架，只有在框架内的实现才能互相搭配使用。

而有个开发他不想按照这个框架实现，自己写了个，测试的成绩还不错后来被  HotSpot VM 给吸收了，这就导致了不兼容。

我之前看到一个回答解释的很形象：就像动车组车头带不了绿皮车厢一样，电气，挂钩啥的都不匹配。

### 新生代的 GC 如何避免全堆扫描？

在常见的分代 GC 中就是利用记忆集来实现的，记录可能存在的老年代中有新生代的引用的对象地址，来避免全堆扫描。

![图片](https://mmbiz.qpic.cn/mmbiz_png/eSdk75TK4nE655lRBLg0LibQPnOTe3hRQTq8afrGCKpfAibqVvx2tgNCK2OQUZJ5vP62twY0JpJOGGFsTDZeicvicw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

上图有个对象精度的，一个是卡精度的，卡精度的叫卡表。

把堆中分为很多块，每块 512 字节（卡页），用字节数组来中的一个元素来表示某一块，1表示脏块，里面存在跨代引用。

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

在 Hotspot 中的实现是卡表，是通过写后屏障维护的，伪代码如下。

![图片](https://mmbiz.qpic.cn/mmbiz_png/eSdk75TK4nE655lRBLg0LibQPnOTe3hRQkbXGZuOrYfy8IeVZQe0cgtnxXRNiaN6rADjr7z99qHKWfEHhqPeUalw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

cms 中需要记录老年代指向年轻代的引用，但是写屏障的实现并没有做任何条件的过滤。

即不判断当前对象是老年代对象且引用的是新生代对象才会标记对应的卡表为脏。

只要是引用赋值都会把对象的卡标记为脏，当然YGC扫描的时候只会扫老年代的卡表。

这样做是减少写屏障带来的消耗，毕竟引用的赋值非常的频繁。

### 那 cms 的记忆集和 G1 的记忆集有什么不一样？

cms 的记忆集的实现是卡表即 card table。

通常实现的记忆集是 points-out 的，我们知道记忆集是用来记录非收集区域指向收集区域的跨代引用，它的主语其实是非收集区域，所以是 points-out 的。

在 cms 中只有老年代指向年轻代的卡表，用于年轻代 gc。

而 G1 是基于 region 的，所以在 points-out 的卡表之上还加了个 points-into 的结构。

因为一个 region 需要知道有哪些别的 region 有指向自己的指针，然后还需要知道这些指针在哪些 card 中。

其实 G1 的记忆集就是个 hash table，key 就是别的 region 的起始地址，然后 value 是一个集合，里面存储这 card table 的 index。

我们来看下这个图就很清晰了。

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

像每次引用字段的赋值都需要维护记忆集开销很大，所以 G1 的实现利用了 logging write barrier（下文会介绍）。

也是异步思想，会先将修改记录到队列中，当队列超过一定阈值由后台线程取出遍历来更新记忆集。

### 为什么 G1 不维护年轻代到老年代的记忆集？

G1 分了 young GC 和 mixed gc。

young gc 会选取所有年轻代的 region 进行收集。

midex gc 会选取所有年轻代的 region 和一些收集收益高的老年代 region 进行收集。

所以年轻代的 region 都在收集范围内，所以不需要额外记录年轻代到老年代的跨代引用。

### cms 和 G1 为了维持并发的正确性分别用了什么手段？

之前文章分析到了并发执行漏标的两个充分必要条件是：

1. 将新对象插入已扫描完毕的对象中，即插入黑色对象到白色对象的引用。
2. 删除了灰色对象到白色对象的引用。

cms 和 g1 分别通过增量更新和 SATB 来打破这两个充分必要条件，维持了 GC 线程与应用线程并发的正确性。

cms 用了增量更新（Incremental update），打破了第一个条件，通过写屏障将插入的白色对象标记成灰色，即加入到标记栈中，在 remark 阶段再扫描，防止漏标情况。

G1 用了 SATB（snapshot-at-the-beginning），打破了第二个条件，会通过写屏障把旧的引用关系记下来，之后再把旧引用关系再扫描过。

这个从英文名词来看就已经很清晰了。讲白了就是在 GC 开始时候如果对象是存活的就认为其存活，等于拍了个快照。

而且 gc 过程中新分配的对象也都认为是活的。每个 region 会维持 TAMS （top at mark start）指针，分别是 prevTAMS 和 nextTAMS 分别标记两次并发标记开始时候 Top 指针的位置。

Top 指针就是 region 中最新分配对象的位置，所以 nextTAMS 和 Top 之间区域的对象都是新分配的对象都认为其是存活的即可。

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

而利用增量更新的 cms 在 remark 阶段需要重新所有线程栈和整个年轻代，因为等于之前的根有新增，所以需要重新扫描过，如果年轻代的对象很多的话会比较耗时。

要注意这阶段是 STW 的，很关键，所以 CMS 也提供了一个 CMSScavengeBeforeRemark 参数，来强制 remark 阶段之前来一次 YGC。

而 g1 通过 SATB 的话在最终标记阶段只需要扫描 SATB 记录的旧引用即可，从这方面来说会比 cms 快，但是也因为这样浮动垃圾会比 cms 多。

### 什么是 logging write barrier ？

写屏障其实耗的是应用程序的性能，是在引用赋值的时候执行的逻辑，这个操作非常的频繁，因此就搞了个 logging write barrier。

把写屏障要执行的一些逻辑搬运到后台线程执行，来减轻对应用程序的影响。

在写屏障里只需要记录一个 log 信息到一个队列中，然后别的后台线程会从队列中取出信息来完成后续的操作，其实就是异步思想。

像 SATB write barrier ，每个 Java 线程有一个独立的、定长的 SATBMarkQueue，在写屏障里只把旧引用压入该队列中。满了之后会加到全局 SATBMarkQueueSet。

![图片](https://mmbiz.qpic.cn/mmbiz_png/eSdk75TK4nE655lRBLg0LibQPnOTe3hRQ1BemQJHhZSiaG10q6exZoJOvx8psLt2ialTne70PFEichhOrYMicKVL8pQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

后台线程会扫描，如果超过一定阈值就会处理，开始 tracing。

在维护记忆集的写屏障也用了 logging write barrier 。

### 简单说下 G1 回收流程

G1 从大局上看分为两大阶段，分别是并发标记和对象拷贝。

并发标记是基于 STAB 的，可以分为四大阶段：

1、初始标记（initial marking），这个阶段是 STW 的，扫描根集合，标记根直接可达的对象即可。在G1中标记对象是利用外部的bitmap来记录，而不是对象头。

2、并发阶段（concurrent marking）,这个阶段和应用线程并发，从上一步标记的根直接可达对象开始进行 tracing，递归扫描所有可达对象。STAB 也会在这个阶段记录着变更的引用。

3、最终标记（final marking）, 这个阶段是 STW 的，处理 STAB 中的引用。

4、清理阶段（clenaup），这个阶段是 STW 的，根据标记的 bitmap 统计每个 region 存活对象的多少，如果有完全没存活的 region 则整体回收。

对象拷贝阶段（evacuation），这个阶段是 STW 的。

根据标记结果选择合适的 region 组成收集集合（collection set 即 CSet），然后将 CSet 存活对象拷贝到新 region 中。

G1 的瓶颈在于对象拷贝阶段，需要花较多的瓶颈来转移对象。

### 简单说下 cms 回收流程

其实从之前问题的 CollectorState 枚举可以得知几个流程了。

1、初始标记(initial mark)，这个阶段是 STW 的，扫描根集合，标记根直接可达的对象即可。

2、并发标记(Concurrent marking)，这个阶段和应用线程并发，从上一步标记的根直接可达对象开始进行 tracing，递归扫描所有可达对象。

3、并发预清理(Concurrent precleaning)，这个阶段和应用线程并发，就是想帮重新标记阶段先做点工作，扫描一下卡表脏的区域和新晋升到老年代的对象等，因为重新标记是 STW 的，所以分担一点。

4、可中断的预清理阶段（AbortablePreclean），这个和上一个阶段基本上一致，就是为了分担重新标记的工作。

5、重新标记(remark)，这个阶段是 STW 的，因为并发阶段引用关系会发生变化，所以要重新遍历一遍新生代对象、GCRoots、卡表等，来修正标记。

6、并发清理(Concurrent sweeping)，这个阶段和应用线程并发，用于清理垃圾。

7、并发重置(Concurrent reset)，这个阶段和应用线程并发，重置 cms 内部状态。

cms 的瓶颈就在于重新标记阶段，需要较长花费时间来进行重新扫描。

### cms 写屏障又是维护卡表，又得维护增量更新？

卡表其实只有一份，又得用来支持 YGC 又得支持 CMS 并发时的增量更新肯定是不够的。

每次 YGC 都会扫描重置卡表，这样增量更新的记录就被清理了。

所以还搞了个 mod-union table，在并发标记时，如果发生 YGC 需要重置卡表的记录时，就会更新  mod-union table 对应的位置。

这样 cms 重新标记阶段就能结合当时的卡表和  mod-union table 来处理增量更新，防止漏标对象了。

### GC 调优的两大目标是啥？

分别是最短暂停时间和吞吐量。

最短暂停时间：因为 GC 会 STW 暂停所有应用线程，这时候对于用户而言就等于卡顿了，因此对于时延敏感的应用来说减少 STW 的时间是关键。

吞吐量：对于一些对时延不敏感的应用比如一些后台计算应用来说，吞吐量是关注的重点，它们不关注每次 GC 停顿的时间，只关注总的停顿时间少，吞吐量高。

举个例子：

方案一：每次 GC 停顿 100 ms，每秒停顿 5 次。

方案二：每次 GC 停顿 200 ms，每秒停顿 2 次。

两个方案相对而言第一个时延低，第二个吞吐高，基本上两者不可兼得。

所以调优时候需要明确应用的目标。

### GC 如何调优

这个问题在面试中很容易问到，抓住核心回答。

现在都是分代 GC，调优的思路就是尽量让对象在新生代就被回收，防止过多的对象晋升到老年代，减少大对象的分配。

需要平衡分代的大小、垃圾回收的次数和停顿时间。

需要对 GC 进行完整的监控，监控各年代占用大小、YGC 触发频率、Full GC 触发频率，对象分配速率等等。

然后根据实际情况进行调优。

比如进行了莫名其妙的 Full GC，有可能是某个第三方库调了 System.gc。

Full GC 频繁可能是 CMS GC 触发内存阈值过低，导致对象分配不过来。

还有对象年龄晋升的阈值、survivor 过小等等，具体情况还是得具体分析，反正核心是不变的。

### 最后

有关 GC 的问题在面试中还是很常见的，其实来来回回就那么几样东西，记得我提到的抓住核心即可。

当然如果你有实际调优经历那更可，所以要抓住工作中的机会，如果发生异常情况请积极参与，然后勤加思考，这可都是实打实的实战经历。

当然如果你想知道更多的 GC 细节那就看源码吧，源码之中无秘密。



### 巨人的肩膀

*https://segmentfault.com/a/1190000021394215?utm_source=tag-newest*

*https://blogs.oracle.com/jonthecollector/our-collectors*

*https://www.iteye.com/blog/user/rednaxelafx R大的博客*

*https://www.jianshu.com/u/90ab66c248e6 占小狼的博客*

---

## JVM三大参数类型

**本实验的目的是**讲解 JVM 的三大参数类型。在**JVM调优**中用到的最多的 XX 参数，而如何去查看和设置 JVM 的 XX 参数也是调优的基本功，本节以实验的方式讲解 JVM 参数的查看和设置。希望大家能有所启发。

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZywrrmKRHeJKroKlzr2XuUocDbKOZW9gbLy3OJTia6fXJ39MfYVZ7wBw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

### 标配参数

#### 常见标配参数

- -version，获取JDK版本
- -help，获取帮助
- -showverision，获取JDK版本和帮助

#### 动手实验 1 - 查看标配参数

实验步骤：

- 查看Java JDK 版本

```cmd
java -version
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZKQ4OtEVouiaW3cwXw0ATyibYDSvgsiaUZqVkwaQrTVwIpTX1BciafzxsZg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)实验 1-1

可以看到Java JDK 版本为1.8.0_131

- 查看 Java 帮助文档

```cmd
java -help
```

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)实验 1-2

- 查看版本和帮助文档

```cmd
java -showversion
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZmT8P23FXJzO5UkvPYdHrjFUx8NrNW11rFx8ibZc3HibEHYBTnJqcXsKA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)实验 1-3

### X 参数

#### X 参数简介

我们常用的`javac`大家都知道是把java代码编译成 class 文 Java 文件，那么 class 文件怎么去执行呢？这里用到了三个X参数来说明 class 文件怎么在虚拟机里面跑起来的。

- -Xint：直接解释执行
- -Xcomp：先编译成本地代码再执行
- -XMixed：混合模式（既有编译执行也有解释执行）

#### 动手实验 2 - 查看和配置X参数

- 查看版本

```cmd
java -version
```

在WebIDE的控制台窗口执行Java -version 后，可以看到我的环境是混合模式执行java程序的。

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZKQ4OtEVouiaW3cwXw0ATyibYDSvgsiaUZqVkwaQrTVwIpTX1BciafzxsZg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)实验 2-1

- 修改编译模式为解释执行模式

```cmd
java -Xint -version
```

在WebIDE的控制台窗口执行命令

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)实验 2-2

- 修改编译模式为只编译模式

```cmd
java -Xcomp -version
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZEx34ibc3KZ06fA2VnhrSmk2kRDUoicX2Rsia1bKMmZniaSCu6slKibn9LSQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)实验 2-3

### XX 参数

#### XX 参数简介

XX 参数有两种类型，一种是 Boolean 类型，另外一种是键值对类型。

- Boolean 类型

- - 公式：`-XX:+某个属性` 或者，`-XX:-某个属性` +表示开启了这个属性，-表示关闭了这个属性。
  - 案例：`-XX:-PrintGCDetails`，表示关闭了GC详情输出

- key-value类型

- - 公式：`-XX:属性key=属性value`
  - 案例：`-XX:属性metaspace=2000000`，设置Java元空间的值为2000000。

#### 动手实验 3 - 查看参数是否开启

本实验主要讲解如下内容：查看运行的 Java 程序 PrintGCDetails 参数是否开启

- 编写一个一直运行的 Java 程序
- 查看该应用程序的进程 id
- 查看该进程的 GCDetail 参数是否开启

##### 在 WebIDE 上右键单击菜单，选择 New File 创建新文件

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZQ2YAbGkiaGcHiaRAqODOQiap4d26ZX0HEFFahPLXhf4Twc3ibH47KrCJGQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)New File

##### 创建文件名为 demoXXparam.java

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZA2c6X4VfFF6sdXMjYN3HY955SxKEBTRia2X8b7PqU7V8MFtW9m3HTDg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)demoXXparam.java

##### 在 WebIDE 上编写 demoXXparam.java

```java
public class demoXXparam {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("hello XX params");
        Thread.sleep(Integer.MAX_VALUE);
    }
}
```

##### 在 WebIDE 的控制台窗口编译 demoXXparam.java 代码

```cmd
javac demoXXparam.java 
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZdEXuvzYqicS0NL6oelKmNT1NX02u3wWib3v6xDtnPZZRrXeS0IRM5cog/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)编译代码

编译之后，会在当前文件夹产生我们所编写的  `demoXXparam` 类的 `demoXXparam.class`字节码文件

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZqwjZia0AulRLZxCVCO8d0UyyIvBUOraNHSAec6HXs7iamgeVLVbdHAmw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)生产Class文件

##### 在 WebIDE 上运行 demoXXparam 代码

```cmd
java demoXXparam
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZK8t6CVKWkuiaBj5ibP0mhIZzA7zErhROn7nLC019FpYuicD5XyfGubMWQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)运行Java程序

输出：

```cmd
hello XX params
```

#### 在 WebIDE 中新开一个控制台窗口

Terminal->New Terminal

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZUVibXkRicdcj5Sd7hibEMY5daGlFhFJA06oYWO82AutLgiaSaORa9AUyrw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)开启新控制台窗口

##### 查看所有的运行的java程序，-l 表示打印出class文件的包名

```cmd
jps -l
```

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)jps

发现`demoXXparam`进程的id为 518

##### 查看 demoXXparam 程序是否开启了PrintGCDetails这个参数

**PrintGCDetails：** 在发生垃圾回收时打印内存回收日志，并在进程退出时输出当前内存各区域分配情况

```cmd
jinfo -flag PrintGCDetails 518
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZHvDksxNC21e3rDwia9PQfFfwezumDT9abiarc8VjT2s8etwJjSMeK8Ww/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)jinfo

结果如下：

```cmd
-XX:-PrintGCDetails
```

上面提到  `-`号表示关闭，所以当前 demo 程序没有开启 `PrintGCDetails`参数。

#### 动手实验 4  - 开启参数

- 在 WebIDE 控制台强制退出demoXXparam程序

```cmd
ctrl + c
```

- 然后清理屏幕

```cmd
clear
```

- 然后以参数 `-XX:+PrintGCDetails` 运行 demoXXparam 程序

```cmd
java -XX:+PrintGCDetails demoXXparam
```

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)实验 4

- 输出：

```cmd
hello XX params
```

##### 查看demoXXparam进程的 id

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZo3QPgBenrKwxVGQicX7qQicGQCJga1cVM59SyFudDiakuUZZ8iajiaxSLCA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)进程 id

可以看到demoXXparam进程 id 为 1225

##### 查看 demoXXparam 的配置参数 PrintGCDetails

打开一个新的控制台窗口，执行以下命令来查看进程为 1225 的 `PrintGCDetails`参数是否开启

```cmd
jinfo -flag PrintGCDetails 1225
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZoKiciau8fAkdzibecl7Of5wvlXeGfSenRprXhUwFOyqXb9tbk1jicqwZfw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)PrintGCDetails 参数

可以看到PrintGCDetails是开启的，`+`号表示开启。

#### 动手实验 5 - Key-Value 类型参数值

##### 查看元空间的值

```cmd
jinfo -flag MetaspaceSize 526
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZz6Cb8Zc6KeqhHuguZ2NCkhPibSiblfmnTWkuOsrB6Bg5ga4lC2LpPeOQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)MetaspaceSize 大小

由此可以得出元空间的大小为 21 M。

##### 设置元空间的值为 128 M

```cmd
java -XX:MetaspaceSize=128m demoXXparam   
```

查看元空间的大小

```cmd
jinfo -flag MetaspaceSize 1062
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZhDhGW7yjAdYKK4hOx3qibey566KP1vibyLveAVzWialDsWPd5WbA71zfw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)调整元空间大小

### 最常见的 -Xms 和 -Xmx 属于哪种参数？

- -Xms参数代表-XX:InitialHeapSize ，初始化堆内存（默认只会用最大物理内存的64分1）
- -Xmx:参数代表-XX:MaxHeapSize ，大堆内存（默认只会用最大物理内存的4分1）

起了别名，但还是属于XX参数。

#### 动手实验 6 - 设置 -XX:InitialHeapSize 和 -XX:MaxHeapSize 的值。

```cmd
java -XX:InitialHeapSize=200m demoXXparam
或者
java -Xms200m demoXXparam
```

查看 InitialHeapSize 参数的值，大小为 200 M。

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZwG0NianRkwmDWQDjvib7b0s0BzXBrdhOaNDtiaZXpVQ7asLfBjWjAhaww/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)设置 InitialHeapSize

```cmd
java -XX:MaxHeapSize=200M demoXXparam
或者
java -Xmx200m demoXXparam
```

查看 MaxHeapSize 参数的值，大小为 200 M。

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZLCrRn7XmgRKZnmHlBAib226GQJ5GHdPF2JeyFOriax0cxKIoEzsTv6MA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)设置 MaxHeapSize

#### 扩展：查看 Java 程序已设置的所有参数值

```cmd
jinfo -flags <进程id>
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZH5eGmKNXINE0vxCqick59uibIw6NNia2GgRswk2nOTGA7jibnqyGkgH5lA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)mark

- Non-Defalut VM flags 代表参数类型是JVM自带的参数。
- Command line 代表是用户自定义的参数

### 如何查看出厂设置和自定义设置的XX配置项

#### 动手实验 7 - 查看出厂默认设置的所有XX配置项

```cmd
java -XX:+PrintFlagsInitial -version 
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZdKhtzgVnfYw9AnPC4do9wTFStLM7VqLe75oM3dHgXnKBG4uZtiarE8Q/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)PrintFlagsInitial

#### 动手实验 8 - 查看 JVM 当前所有XX配置项

```cmd
java -XX:+PrintFlagsFinal -version 
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZh2SYB0rVUkZUTxIWOf9EASKZKulDGDbjyFxHXC9BhG5vXPpVgib2M2w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)PrintFlagsFinal

我们可以看到几个关键信息：

- `[Global flags]`：全局参数，如果自定义修改了某个应用的参数，并不会修改全局参数

  比如之前我们修改了MetaspaceSize为128m，但列表里面还是21m。

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZ3r4UeluuXoMGGzdS1z0c89tGXet97r4jvrM6TqSMJCHCMWXg09gI7w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)Global flags

- `:=`：参数已被修改，如下图所示InitialHeapSize初始化堆内存参数已修改为264241152

  总结如下：

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZ67RQ8BennibiabFnAwdxPLmDsHibNCMJ3RQ2u3lhPUqYzrnugYxqib6tww/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)出厂设置和自定义参数设置

#### 动手实验 9 - 运行程序时打印XX配置选项

```cmd
java -XX:+PrintFlagsFinal -XX:+InitialHeapSize=150M demoXXparam
```

可以看到修改后的值为 157286400（150 M）

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZKtcFplFZzricpOl505sTnrt6XrXLTAOjI7JEDSJJZVx6Fbhicdz0BEkw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

运行程序时打印XX配置选项

#### 动手实验 10 - 查看 JVM 自动配置的或者用户手动设置的XX选项（非应用程序的）

```cmd
java -XX:+PrintCommandLineFlags -version
```

会打印出如下参数：

![图片](https://mmbiz.qpic.cn/mmbiz_png/SfAHMuUxqJ1ZZWTnvEcgvUOB9quHRjEZcUqI9bp1qwQWY4T9EId8yQ6Ak5ibsEOgo5lGMKiaiayeicYGNzs7sw23Ow/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

JVM 自动配置的XX选项

### 实验总结

本次用实验学习了如何查看基本参数、X参数、XX参数和设置XX参数。以及用好`jps`和`jinfo`工具来查看进程和设置参数。

---

## G1垃圾收集器

作为一款高效的垃圾收集器，G1在JDK7中加入JVM，在JDK9中取代CMS成为了默认的垃圾收集器。

### 1 垃圾收集器回顾

#### 1.1 新生代

新生代采用复制算法，主要的垃圾收集器有三个，Serial、Parallel New 和 Parallel Scavenge，特性如下：

- Serial:单线程收集器，串行方式运行，GC 进行时，其他线程都会停止工作。在单核 CPU 下，收集效率最高。
- Parallel New：Serial 的多线程版本，新生代默认收集器。在多核 CPU 下，效率更高，可以跟CMS收集器配合使用。
- Parallel Scavenge：多线程收集器，更加注重吞吐量，适合交互少的任务，不能跟 CMS 配合使用。

#### 1.1 老年代

- Serial Old：采用标记-整理(压缩)算法，单线程收集。
- Parallel Old：采用标记-整理(压缩)算法，可以跟 Parallel Scavenge 配合使用
- CMS：Concurrent Mark Sweep，采用标记-清除算法，收集线程可以跟用户线程一起工作。

> CMS缺点：吞吐量低、无法处理浮动垃圾、标记清除算法会产生大量内存碎片、并发模式失败后会切到Serial old。

- G1：把堆划分成多个大小相等的Region，新生代和老年代不再物理隔离，多核 CPU 和大内存的场景下有很好的性能。新生代使用复制算法，老年代使用标记-压缩(整理)算法。



### 2 G1介绍

#### 2.1 初识G1

G1垃圾收集器主要用于多处理器、大内存的场景，它有五个属性：分代、增量、并行(大多时候可以并发)、stop the word、标记整理。

- 分代：跟其他垃圾收集器一样，G1把堆分成了年轻代和老年代，垃圾收集主要在年轻代，并且年轻代回收效率最高。偶尔也会在老年代进行回收。
- 增量：为了让垃圾收集时STW时间更短，G1采用增量和分步进行回收。G1通过对应用之前的行为和停顿时间进行分析构建出可预测停顿时间模型，并且利用这个信息来预测停顿时间内的垃圾收集情况。比如：G1会首先回收那些收集效率高的内存区域(这些区别大部分空间是可回收垃圾，这也是为啥叫G1的原因）。
- 并行和并发：为了提高吞吐量，一些操作需要STW。一些需要花费很多时间的操作，比如整堆操作(像全局标记)**可以并发执行，同时可以并发跟应用并行执行**。
- 标记整理：G1主要使用标记整理算法来进行垃圾收集，标记阶段跟“标记清除”算法一样，但标记之后不会直接对可回收对象进⾏清理，⽽是让所有存活对象都移动到一端，然后直接回收掉移动之后边界以外的内存。如下图：

![图片](https://mmbiz.qpic.cn/mmbiz_png/a1gicTYmvicdicNg1QXA7Ribhb0pfzpGBLZGdZlBP3PMNu2ePDEAAVZuaSNnfSgCmibhOBoFTjRgJ03XeYV2OiaA2cGg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

   我们知道，垃圾收集器的一个目标就是STW(stop the word)越短越好。利用可预测停顿时间模型，G1为垃圾收集设定一个STW的目标时间(通过 -XX:MaxGCPauseMillis 参数设定，默认200ms)，G1尽可能地在这个时间内完成垃圾收集，并且在不需要额外配置的情况下实现高吞吐量。

G1致力于在下面的应用和环境下寻找延迟和吞吐量的最佳平衡：

- 堆大小达到10GB以上，并且一半以上的空间被存活的对象占用
- 随着系统长期运行，对象分配和升级速率变化很快
- 堆中存在大量内存碎片
- 垃圾收集时停顿时间不能超过几百毫秒，避免垃圾收集造成的长时间停顿。

如果在JDK8中使用G1，我们可以使用参数 -XX:+UseG1GC 来开启。

> G1并不是一款实时收集器，它尽最大努力以高性能完成 MaxGCPauseMillis 设置的停顿时间，但并不能绝对保证在这个时间内完成收集。

#### 2.2 堆布局

G1把整个堆分成了大小相等的region，每一个region都是连续的虚拟内存，region是内存分配和回收的基本单位。如下图：

![图片](https://mmbiz.qpic.cn/mmbiz_png/a1gicTYmvicdibngxYs4dUUdTnEJw56yPIJmV3TwviaoB7vicS8cGicZNJuK2C6OSZ0rru3PFMNt6CsaLfiaylVOH26Kw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

红色带"S"的region表示新生代的survivor，红色不带"S"的表示新生代eden，浅蓝色不带"H"的表示老年代，浅蓝色带"H"的表示老年代中的大对象。跟G1之前的内存分配策略不同的是，survivor、eden、老年代这些区域可能是不连续的。

G1在停顿的时候可以回收整个新生代的region，新生代region的对象要不复制到survivor区要不复制到老年代region。同时每次停顿都可以回收一部分老年代的内存，把老年代从一个region复制到另一个region。

#### 2.3 关于region

上一节我们看到，整个堆内存被G1分成了多个大小相等的region，每个堆大约可以有2048个region，每个region大小为 1~32 MB(必须是2的次方)。region的大小通过 -XX:G1HeapRegionSize 来设置，所以按照默认值来G1能管理的最大内存大约 32MB * 2048 = 64G。

#### 2.4 大对象

大对象是指大小超过了region一半的对象，大对象可以横跨多个region，给大对象分配内存的时候会直接分配在老年代，并不会分配在eden区。

如下图，一个大对象占据了两个半region，给大对象分配内存时，必须从一个region开始分配连续的region，在大对象被回收前，最后一个region不能被分配给其他对象。

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

**大对象什么时候回收**？通常，只有在mark结束以后的Cleanup停顿阶段或者FullGC的时候，死亡的大对象才会被回收掉。但是，基本类型(比如bool数组、所有的整形数组、浮点型数组等)的数组大对象有个例外,G1会在任何GC停顿的时候回收这些死亡大对象。这个默认是开启的，但是可以使用 -XX:G1EagerReclaimHumongousObjects 这个参数禁用掉。

分配大对象的时候，因为占用空间太大，可能会过早发生GC停顿。G1在每次分配大对象的时候都会去检查当前堆内存占用是否超过 初始堆占用阈值`IHOP`(The Initiating Heap Occupancy Percent)，如果当前的堆占用率超过了IHOP阈值，就会立刻触发 initial mark。**关于initial mark详见第4节**。

即使是在FullGC的时候，大对象也是永远不会被移动的。这可能导致过早发生FullGC或者是意外的OOM，因为此时虽然还有大量的空闲内存，但是这些内存都是region中的内存碎片。

### 3 内存分配

G1虽然把堆内存划分成了多个region，但是依然存在新生代和老年代的概念。G1新增了2个控制新生代内存大小的参数，-XX:G1NewSizePercent(默认等于5)，-XX:G1MaxNewSizePercent(默认等于60)。也就是说新生代大小默认占整个堆内存的 5% ~ 60%。

根据前面介绍，一个堆大概可以分配2048个region，每个region最大32M，这样G1管理的整个堆的大小最大可以是64G，新生代占用的大小范围是 3.2G ~ 38.4G。

对于 -XX:G1NewSizePercent 和 -XX:G1MaxNewSizePercent，下面几个问题需要注意：

1. **如果设置了-Xmn，那这两个参数是否生效？**

生效，比如堆大小是64G，设置 -Xmn3.2G，那么就等价于 -XX:G1NewSizePercent=5 并且 -XX:G1MaxNewSizePercent=5，因为3.2G/64G = 5%。

2. **如果设置了 -XX:NewRatio，这两个参数是否生效？**

生效，比如堆大小是64G，设置 -XX:NewRatio=3，那么就等价于 -XX:G1NewSizePercent=25 并且 -XX:G1MaxNewSizePercent=25。因为年轻代：老年代 = 1 ：3，说明年轻代占1/4 = 25%。

3. **如果 -XX:G1NewSizePercent 和 -XX:G1MaxNewSizePercent 只设置其中一个，那这两个参数还生效吗？**

设置的这个参数不生效，两个参数都用默认值。

4. **如果-XX:G1NewSizePercent 和 -XX:G1MaxNewSizePercent 这两个参数都生效了，什么时候动态扩容？**

跟 -XX:GCTimeRatio 这个参数相关。这个参数为0~100之间的整数(G1默认是9, 其它收集器默认是99)，值为 n 则系统将花费不超过 1/(1+n) 的时间用于垃圾收集。因此G1默认最多 10% 的时间用于垃圾收集，如果垃圾收集时间超过10%，则触发扩容。如果扩容失败，则发起Full GC。

### 4 垃圾回收

G1的垃圾收集是在 Young-Only 和 Space-Reclamation两个阶段交替执行的。如下图：

![图片](https://mmbiz.qpic.cn/mmbiz_png/a1gicTYmvicdibngxYs4dUUdTnEJw56yPIJon3pibl9cmbTCKcyRd63W9Hdz5yPfYEsVic03xxlYMkV3OOXbomH3oqg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

young-only阶段会用对象逐步把老年代区域填满，space-reclamation阶段除了会回收年轻代的内存以外，还会增量回收老年代的内存。完成后重新开始young-only阶段。

#### 4.1 Young-only

Young-only阶段流程如下图：

![图片](https://mmbiz.qpic.cn/mmbiz_png/a1gicTYmvicdibngxYs4dUUdTnEJw56yPIJa0oDAx2lYZ06cBPAFgpUnLfX0ib9Ig31U73SIdTWfnFAr6sqmLUIH2A/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这个阶段从普通的 young-only GC 开始，young-only GC把一些对象移动到老年代，当老年代的空间占用达到IHOP时，G1就停止普通的young-only GC，开始初始标记(Initial Mark)。

- 初始标记：这个过程除了普通的 young-only GC 外，还会开始并发标记过程，这个过程决定了被标记的老年代存活对象在下一次space-reclamation阶段会被保留。这个过程不会STW，有可能标记还没有结束普通的 young-only GC 就开始了。这个标记过程需要在重新标记(Remark)和清理(Cleanup)两个过程后才能结束。

- 重新标记: 这个过程会STW，这个过程做全局引用和类卸载。

  在重新标记和清理这两个阶段之间G1会并发计算对象存活信息，这个信息用于清理阶段更新内部数据结构。

- 清理阶段：

  这个节点回收所有的空闲区域，并且决定是否接着执行一次space-reclamation，如果是，则仅仅执行一次单独的young-only GC，young-only阶段就结束了。

> 关于IHOP，默认情况下，G1会观察标记周期内标记花了多少时间，老年代分配了多少内存，以此来自动确定一个最佳的IHOP，这叫做自适应IHOP。如果开启这个功能，因为初始时没有足够的观察数据来确定IHOP，G1会用参数 -XX:InitiatingHeapOccupancyPercent 来指定初始IHOP。可以用 -XX:-G1UseAdaptiveIHOP 参数关闭自适应IHOP，这样IHOP就参数 -XX:InitiatingHeapOccupancyPercent 指定的固定值。自适应IHOP这样设置老年代占有率,当老年代占有率=老年代最大占有率 - 参数 -XX:G1HeapReservePercent 值时，启动space-reclamation阶段的第一个Mixed GC。这里参数 -XX:G1HeapReservePercent 作为一个额外的缓存值。

> 关于标记，标记使用 SATB 算法，初始标记开始时，G1保存堆的一份虚拟镜像，这份镜像存活的对象在后续的标记过程中也被认为是存活的。这有一个问题，就是标记过程中如果部分对象死亡了，对于 space-reclamation 阶段来说它们仍然是存活的(也有少部分例外)。跟其他垃圾收集器相比，这会导致一部分死亡对象被错误保留，但是为标记阶段提供了更好的吞吐量，而且这些错误保留的对象会在下一次标记阶段被回收。

在young-only阶段，要回收新生代的region。每一次 young-only 结束的时候，G1总是会调整新生代大小。G1可以使用参数 -XX:MaxGCPauseTimeMillis和 -XX:PauseTimeIntervalMillis 来设置目标停顿时间，这两个参数是对实际停顿时间的长期观察得来的。他会根据在GC的时候要拷贝多少个对象，对象之间是如何相互关联的等信息计算出来回收相同大小的新生代内存需要花费多少时间，

如果没有其他的限定条件，G1会把young区的大小调整为 -XX:G1NewSizePercent和 -XX:G1MaxNewSizePercent 之间的值来满足停顿时间的要求。

#### 4.2 Space-reclamation

这个阶段由多个Mixed GC组成，不光回收年轻代垃圾，也回收老年代垃圾。当 G1 发现回收更多的老年代区域不能释放更多空闲空间时，这个阶段结束。之后，周期性地再次开启一个新的Young-only阶段。

当G1收集存活对象信息时内存不足，G1会做一个Full GC，并且会STW。

在 space-reclamation 阶段，G1会尽量在GC停顿时间内回收尽可能多的老年代内存。这个阶段新生代内存大小被调整为 -XX:G1NewSizePercent 设置的允许的最小值，只要存在可回收的老年代region就会被添加到回收集合中，直到再添加会超出目标停顿时间为止。在特定的某个GC停顿时间内，G1会按照这老年代region回收的效率(效率高的优先收集)和剩余可用时间来得到最终待回收region集合。

每一个GC停顿期间要回收的老年代region数量受限于候选region集合数量除以 -XX:G1MixedGCCountTarget 这个参数值，参数 -XX:G1MixedGCCountTarget 指定一个周期内触发Mixed GC最大次数，默认值8。比如 -XX:G1MixedGCCountTarget 采用默认值8，候选region集合有200个region，那每次停顿期间收集25个region。

> 候选region集合是老年代中所有占用率低于 -XX:G1MixedGCLiveThresholdPercent 的region。

当待回收region集合中可回收的空间占用率低于参数值 -XX:G1HeapWastePercent 的时候，Space-Reclamation结束。

#### 4.3 内存紧张情况

当应用存活对象占用了大量内存，以至于回收剩余对象没有足够的空间拷贝时，就会触发 evacuation failure。这时G1为了完成当前的垃圾收集，会保留已经位于新的位置上的存活对象不动，对于没有移动和拷贝的对象就不会进行拷贝了，仅仅调整对象间的引用。

evacuation failure会导致一些额外的开销，但是一般会跟其他 young GC 一样快。evacuation failure完成以后，G1会跟正常情况下一样继续恢复应用的执行。G1会假设 evacuation failure是发生在GC的后期，这时大部分对象已经移动过了，并且已经有足够的内存来继续执行应用程序一直到 mark 结束 space-reclamation 开始。如果这个假设不成立(也就是说没有足够的内存来执行应用程序)，G1最终只能发起Full GC，对整个堆做压缩，这个过程可能会非常慢。

### 5 跟其他收集器比较

#### 5.1 Parallel GC

Parallel GC 可以压缩和回收老年代的内存，但是也只能对老年代整体来操作。G1以增量的方式把整个GC工作增量的分散到多个更短的停顿时间中，当然这可能会牺牲一定吞吐量。

#### 5.2 CMS

跟CMS类似，G1并发回收老年代内存，但是，CMS采用标记-清除算法，不会处理老年代的内存碎片，最终就会导致长时间的FullGC。

#### 5.3 G1问题

因为采用并发收集，G1的性能开销会更大，这可能会影响吞吐量。

#### 5.4 G1优势

G1在任何的GC期间都可以回收老年代中全空或者占用大空间的内存。这可以避免一些不必要的GC，因而可以非常轻易地释放大量的内存空间。这个功能默认开启，可以采用 -XX:-G1EagerReclaimHumongousObjects 参数关闭。

G1可以选择对整个堆里面的String进行并行去重。这个功能默认关闭，可以使用参数 -XX:+G1EnableStringDeduplication 来开启。

### 6 总结

本文详细介绍了G1垃圾收集器，希望能够对你理解G1有所帮助。

参考:

1. https://docs.oracle.com/javase/10/gctuning/garbage-first-garbage-collector.htm#JSGCT-GUID-CE6F94B6-71AF-45D5-829E-DEADD9BA929D
2. https://mp.weixin.qq.com/s/KkA3c2_AX6feYPJRhnPOyQ

![图片](https://mmbiz.qpic.cn/mmbiz_gif/b96CibCt70iabwjyojLhA03PtxUnkNPREnt2F48ywfXLpDdDAjicOTPI8Q94tVLbJ58tbRs12iaXDKhUOW9gd4NlFA/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

---

# 其他

## Java源码

### 为什么要读JDK源码

当然不是为了装，毕竟谁没事找事虐自己 ...

![图片](https://mmbiz.qpic.cn/mmbiz_gif/xq9PqibkVAzo7kPxibSNTFvENnsVHicic8GLQOEQpe60trRHiatUugKR5qxB1YC7ItzBSVt97ZEXlxRwh8JW04hBf1Q/640?wx_fmt=gif&tp=webp&wxfrom=5&wx_lazy=1)

1、**面试跑不掉**。现在只要面试Java相关的岗位，肯定或多或少会会涉及JDK源码相关的问题。

2、**弄懂原理才不慌**。我们作为JDK的使用者，虽然说天天用得很开心，但是有时候遇到问题还是得跟到底层源码去看看，才能帮助我们更好的弄懂原理，

3、**学习优秀的代码、思想和模式**。JDK毕竟是一个优秀的代码库，我们天天用，源码也就在里面，作为一个有志向的程序员，读一读源码也能让我们吸取到更多优秀的思想和模式。

4、**睡前催眠**。额 …… 不过的确有效（滑稽）。

------

### 源码难吗？

像JDK这种源码，和我们平常练手写小例子、写业务代码不一样，人家毕竟是 **类库**，为了**性能**、**稳定性**、**通用性**，**扩展性**等因素考虑，加入了很多**辅助代码**、**泛型**、以及一些**设计模式**上的考量，所以看起来肯定没有那么轻松，**没办法一眼看穿它**。

所以这玩意儿肯定是一个长期的过程，我个人建议（包括我自己也是这样），有时候遇到一些问题，可以针对性地把某些组件或者某个部分的源码，跟到底层去看看，然后做点笔记，写点注释啥的，这样慢慢就能渗透到很多的内容了。

但是我们一定要有足够的信心，我坚信代码人家都写出来了，我就不信我看不懂！

------

### 源码该怎么看

1、**方法一：按需阅读**。如果对某个组件、语法或者特性感兴趣，或者遇到什么问题疑惑，可以有针对性地跟到底层源码按需查看，这也是一种比较高效，能快速树立信心的阅读方式。

2、**方法二：系统化阅读**。具体阅读内容和顺序建议下文详述。

3、**多调试**：如果仅仅靠眼睛看，然后脑补画面调试还是比较吃力的，最好还是借助IDE动手调试起来，走两步就知道了。

4、**别光读，记得读完留下点什么**。我觉得看了多少不重要，重要的是能输出多少，多总结、归纳，写注释，记笔记

> 所以下文准备搭建一个Java源码的阅读和调试环境，建议人手一个，每当**心血来潮时**、**遇到问题时**、**碰到疑惑时**、**闲得无聊**时都可以打开工程看一看源码，做做笔记和注释。

------

### 搭建源码阅读调试环境

我个人觉得看源码这个事情还是应该单独搞一个Java工程，源码放里面，测试代码也放里面，**集中调试**，**集中看代码**，**集中写注释**比较方便一些。

**1、创建源码阅读项目**

选择最普通的Java基础项目即可：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzo7kPxibSNTFvENnsVHicic8GLbIempkFwZJ5HyWjLICzqcFzHpUF2VJAib0u8hcLSfzUB7RrzuPIWTUQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**2、创建两个目录**

分别为：

- `source`：稍后放置JDK源码进去
- `test`：放置测试代码，里面还可以按需要建立层级子目录

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzo7kPxibSNTFvENnsVHicic8GLMNK24iclt4jQHPw5AHILdcncc3XJCaxNH2reOIOQ89HfZzNiaXWTRb4w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**3、导入JDK源码**

有很多小伙伴问**JDK的源码在哪里呢？**

远在天边，仅在眼前，其实在的**JDK安装目录**下就能找到。

JDK安装目录下有一个名为`src.zip`压缩包，这正是JDK源码！

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzo7kPxibSNTFvENnsVHicic8GLg9LdibiaqGbBiaNbTvACmicYKrhGq8oYqIa3pTmZZBNnibfEibnlt7CWwIcQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

将其解压后拷贝到上面项目的`source`目录下，这样JDK源码就导入好了。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzo7kPxibSNTFvENnsVHicic8GL3N9qDS4WmfBDe4yLicU9pGw7JxickhNmtWiaxFZwic7aIDB4Q8IRzGLf4g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

有些小伙伴会有疑问，**为什么要将JDK源码导一份放到这个项目里****？**其实主要原因还是方便我们在源码里阅读、调试、以及做笔记和注释。

至于这份JDK源码怎么用上，下文继续阐述。

**4、调试并运行**

我们可以在`test`目录里去随意编写一段测试代码。

比如我这里就以`HashMap`为例，在`test`目录下创建一个子目录`hashmap`，然后在里面创建一个测试主入口文件`Test.java`，随意放上一段测试代码：

```
public static void main( String[] args ) {


    Map<String,Double> hashMap = new HashMap<>();

    hashMap.put( "k1", 0.1 );
    hashMap.put( "k2", 0.2 );
    hashMap.put( "k3", 0.3 );
    hashMap.put( "k4", 0.4 );

    for ( Map.Entry<String,Double> entry : hashMap.entrySet() ) {
        System.out.println( entry.getKey() +"：" + entry.getValue());
    }

}
```

然后启动调试即可。

不过接下来会有几个问题需要一一去解决。

**问题一：启动调试时Build报错，提示系统资源不足**

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzo7kPxibSNTFvENnsVHicic8GL4PQnPYm6aSCc5WOiaN6SOuOkc4PibyUGVmfiaVELrkZNQdMY4oPBF82GQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**解决方法：** 加大`Build process heap size`。

设置方法：`Preferences --> Build,Execution,Deployment --> Compiler`，将默认`700`的数值加大，比如我这里设置为`1700`：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzo7kPxibSNTFvENnsVHicic8GLlz1uZyjCKJChpbh88w5W0esric66PcDBDja279XKGvPjkZpYAX6JKlw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**问题二：想从外层代码F7单步调试进入JDK源码内部，结果发现进不去**

这是因为调试时，JDK源码受保护，一般单步调试不让进，但是可以设置。

**解决方法：**：

```
Preferences --> Build,Execution,Deployment --> Debugger --> Stepping
```

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzo7kPxibSNTFvENnsVHicic8GLctOS0sOsdETgaQ1G1R5NUb5hgeK2akXsAmUib3MzRM315ONqnVJia1zg/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

**问题三：如何对JDK源码做注释？**

调试进入JDK源码以后，发现不能进行注释，每个文件上都有一个小锁的图标，这是因为现在关联的源码并不是我们项目里刚拷进去的源码，而是JDK安装目录下的`src.zip`只读压缩包。

**解决办法：** 重新关联JDK源码路径为本项目路径下的这一份JDK源码。

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzo7kPxibSNTFvENnsVHicic8GLqvFY7DeZhQhhicj1HnqiaPlNx4WIjWbZxAukTjPzvI8VbicUA2kN2Sx7g/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

这样一来，我们就可以愉快地对JDK源码调试以及做注释了。

------

### 源码结构和阅读顺序

JDK源码毕竟太庞大了，所有都看不太现实，我们还是愿意根据日常使用和面试考察的频繁度来挖取重要的内容先看一看。

如果自己没有特别的规划，可以按照如下所示的建议阅读顺序往下进行：

![图片](https://mmbiz.qpic.cn/mmbiz_png/xq9PqibkVAzo7kPxibSNTFvENnsVHicic8GL6cf9rhibvKXVAkvBGJEenticDfYfQKkyVh6W7nkw1bsu4kpxIAdkXMbw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

具体的内容简介如下：

**1、`java.lang`**

这里面其实就是Java的基本语法，比如各种基本包装类型（`Integer`、`Long`、`Double`等）、基本类（`Object`，`Class`，`Enum`，`Exception`，`Thread`）等等...

**2、`java.lang.annotation`**

包含Java注解基本元素相关的源码

**3、`java.lang.reflect`**

包含Java反射基本元素相关的代码

**4、`java.util`**

这里面放的都是Java的基本工具，最典型和常用的就是各种容器和集合（`List`、`Map`、`Set`）

**5、`java.util.concurrent`**

大名鼎鼎的JUC包，里面包含了Java并发和多线程编程相关的代码

**6、`java.util.function` +`java.util.stream`**

包含Java函数式编程的常见接口和代码

**7、`java.io`**

包含Java传统I/O相关的源码，主要是面向字节和流的I/O

**8、`java.nio`**

包含Java非阻塞I/O相关的源码，主要是面向缓冲、通道以及选择器的I/O

**9、`java.time`**

包含Java新日期和期间相关的代码，最典型的当属`LocalDateTime`、`DateTimeFormatter`等

**10、`java.math`**

主要包含一些高精度运算的支持数据类

**11、`java.math`**

主要包含一些高精度运算的支持数据类

**12、`java.net`**

主要包含Java网络通信（典型的如：`Socket`通信）相关的源代码。



---

## Try-Catch

### try-catch循环体内外差异

**很多人对 try-catch 有一定误解**，比如我们经常会把它（try-catch）和“低性能”直接画上等号，但对 try-catch 的本质（是什么）却缺少着最基础的了解，因此我们也**会在本篇中对 try-catch 的本质进行相关的探索**。

![图片](https://mmbiz.qpic.cn/mmbiz_png/HrWw6ZuXCsgdtF7EBiaj09OmRCQJ2Cr4l3ch0z6Ol5gx7dGTiaHPeQgzR8ibwy085wesExSvdiar3ofkiccdBBJ0O1w/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

> 小贴士：我会尽量用代码和评测结果来证明问题，但由于本身认知的局限，如有不当之处，请读者朋友们在评论区指出。

#### 性能评测

话不多说，我们直接来开始今天的测试，本文我们依旧使用 Oracle 官方提供的 JMH（Java Microbenchmark Harness，JAVA 微基准测试套件）来进行测试。

首先在 pom.xml 文件中添加 JMH 框架，配置如下：

```java
<!-- https://mvnrepository.com/artifact/org.openjdk.jmh/jmh-core -->
	  <dependency>
          <groupId>org.openjdk.jmh</groupId>
          <artifactId>jmh-core</artifactId>
          <version>1.23</version>
      </dependency>
      <dependency>
          <groupId>org.openjdk.jmh</groupId>
          <artifactId>jmh-generator-annprocess</artifactId>
          <version>1.20</version>
          <scope>provided</scope>
      </dependency>
```

完整测试代码如下：

```java
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * try - catch 性能测试
 */
@BenchmarkMode(Mode.AverageTime) // 测试完成时间
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS) // 预热 1 轮，每次 1s
@Measurement(iterations = 5, time = 5, timeUnit = TimeUnit.SECONDS) // 测试 5 轮，每次 3s
@Fork(1) // fork 1 个线程
@State(Scope.Benchmark)
@Threads(100)
public class TryCatchPerformanceTest {
    private static final int forSize = 1000; // 循环次数
    public static void main(String[] args) throws RunnerException {
        // 启动基准测试
        Options opt = new OptionsBuilder()
                .include(TryCatchPerformanceTest.class.getSimpleName()) // 要导入的测试类
                .build();
        new Runner(opt).run(); // 执行测试
    }

    @Benchmark
    public int innerForeach() {
        int count = 0;
        for (int i = 0; i < forSize; i++) {
            try {
                if (i == forSize) {
                    throw new Exception("new Exception");
                }
                count++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    @Benchmark
    public int outerForeach() {
        int count = 0;
        try {
            for (int i = 0; i < forSize; i++) {
                if (i == forSize) {
                    throw new Exception("new Exception");
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }
}
```

以上代码的测试结果为：

![图片](https://mmbiz.qpic.cn/mmbiz_png/HrWw6ZuXCsgdtF7EBiaj09OmRCQJ2Cr4lnwcgKf3T7VGHsSiczQDdWDpYDDFmLPFTu4TNwOpf3m1DialMGo8mUarQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

从以上结果可以看出，程序在循环 1000 次的情况下，单次平均执行时间为：

- 循环内包含 try-catch 的平均执行时间是 635 纳秒 ±75 纳秒，也就是 635 纳秒上下误差是 75 纳秒；
- 循环外包含 try-catch 的平均执行时间是 630 纳秒，上下误差 38 纳秒。

也就是说，在没有发生异常的情况下，除去误差值，我们得到的结论是：**try-catch 无论是在 `for` 循环内还是  `for` 循环外，它们的性能相同，几乎没有任何差别**。

![图片](https://mmbiz.qpic.cn/mmbiz_png/HrWw6ZuXCsgdtF7EBiaj09OmRCQJ2Cr4lfBrqZlbL9BbNeia6ZibCosPqicX56S2Lq7icDz1tTC1jwgN4TXAeqGSvCw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### try-catch的本质

要理解 try-catch 的性能问题，必须从它的字节码开始分析，只有这样我能才能知道 try-catch 的本质到底是什么，以及它是如何执行的。

此时我们写一个最简单的 try-catch 代码：

```java
public class AppTest {
    public static void main(String[] args) {
        try {
            int count = 0;
            throw new Exception("new Exception");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
```

然后使用 `javac` 生成字节码之后，再使用 `javap -c AppTest` 的命令来查看字节码文件：

```java
➜ javap -c AppTest 
警告: 二进制文件AppTest包含com.example.AppTest
Compiled from "AppTest.java"
public class com.example.AppTest {
  public com.example.AppTest();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: return

  public static void main(java.lang.String[]);
    Code:
       0: iconst_0
       1: istore_1
       2: new           #2                  // class java/lang/Exception
       5: dup
       6: ldc           #3                  // String new Exception
       8: invokespecial #4                  // Method java/lang/Exception."<init>":(Ljava/lang/String;)V
      11: athrow
      12: astore_1
      13: aload_1
      14: invokevirtual #5                  // Method java/lang/Exception.printStackTrace:()V
      17: return
    Exception table:
       from    to  target type
           0    12    12   Class java/lang/Exception
}
```

从以上字节码中可以看到有一个异常表：

```java
Exception table:
       from    to  target type
          0    12    12   Class java/lang/Exception
```

参数说明：

- from：表示 try-catch 的开始地址；
- to：表示 try-catch 的结束地址；
- target：表示异常的处理起始位；
- type：表示异常类名称。

从字节码指令可以看出，当代码运行时出错时，会先判断出错数据是否在 `from` 到 `to` 的范围内，如果是则从 `target` 标志位往下执行，如果没有出错，直接 `goto` 到 `return`。也就是说，如果代码不出错的话，性能几乎是不受影响的，和正常的代码的执行逻辑是一样的。

![图片](https://mmbiz.qpic.cn/mmbiz_png/HrWw6ZuXCsgdtF7EBiaj09OmRCQJ2Cr4lr8rib0MlEh22W6TuCrm1Ww0icgWelcnFkLcuKic9FE4Uv6TZm6uib2oTiaA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 业务情况分析

虽然 try-catch 在循环体内还是循环体外的性能是类似的，但是它们所代码的业务含义却完全不同，例如以下代码：

```java
public class AppTest {
    public static void main(String[] args) {
        System.out.println("循环内的执行结果：" + innerForeach());
        System.out.println("循环外的执行结果：" + outerForeach());
    }
    
    // 方法一
    public static int innerForeach() {
        int count = 0;
        for (int i = 0; i < 6; i++) {
            try {
                if (i == 3) {
                    throw new Exception("new Exception");
                }
                count++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return count;
    }

    // 方法二
    public static int outerForeach() {
        int count = 0;
        try {
            for (int i = 0; i < 6; i++) {
                if (i == 3) {
                    throw new Exception("new Exception");
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }
}
```

以上程序的执行结果为：

> java.lang.Exception: new Exception
>
> at com.example.AppTest.innerForeach(AppTest.java:15)
>
> at com.example.AppTest.main(AppTest.java:5)
>
> java.lang.Exception: new Exception
>
> at com.example.AppTest.outerForeach(AppTest.java:31)
>
> at com.example.AppTest.main(AppTest.java:6)
>
> 循环内的执行结果：5
>
> 循环外的执行结果：3

可以看出在循环体内的 try-catch 在发生异常之后，可以继续执行循环；而循环外的 try-catch 在发生异常之后会终止循环。==catch住异常之后系统不会停止，而是会继续执行catch/finally之后的代码知道程序结束==

因此我们**在决定 try-catch 究竟是应该放在循环内还是循环外，不取决于性能（因为性能几乎相同），而是应该取决于具体的业务场景**。

例如我们需要处理一批数据，而无论这组数据中有哪一个数据有问题，都不能影响其他组的正常执行，此时我们可以把 try-catch 放置在循环体内；而当我们需要计算一组数据的合计值时，只要有一组数据有误，我们就需要终止执行，并抛出异常，此时我们需要将 try-catch 放置在循环体外来执行。

![图片](https://mmbiz.qpic.cn/mmbiz_png/HrWw6ZuXCsgdtF7EBiaj09OmRCQJ2Cr4lsyZ7hvKic6qGVIpo78d8kCVFBKXYot3xYbhtM4PnaoUZ0AQbKia9hoMw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### 总结

本文我们测试了 try-catch 放在循环体内和循环体外的性能，发现**二者在循环很多次的情况下性能几乎是一致的**。然后我们通过字节码分析，发现只有当发生异常时，才会对比异常表进行异常处理，而正常情况下则可以忽略 try-catch 的执行。但在循环体内还是循环体外使用 try-catch，对于程序的执行结果来说是完全不同的，因此**我们应该从实际的业务出发，来决定到 try-catch 应该存放的位置，而非性能考虑**。



---

## if-switch对比

**条件判断语句是程序的重要组成部分**，也是系统业务逻辑的控制手段。重要程度和使用频率更是首屈一指，那我们要如何选择 if 还是 switch 呢？他们的性能差别有多大？switch 性能背后的秘密是什么？接下来让我们一起来寻找这些问题的答案。

### switch VS if

之前有听到过，要尽量使用 switch 因为他的性能比较高，但具体高多少？以及为什么高的原因将在本文为你揭晓。

我们依然借助 Oracle 官方提供的 JMH（Java Microbenchmark Harness，JAVA 微基准测试套件）框架来进行测试，首先引入 JMH 框架，在 pom.xml 文件中添加如下配置：

```xml
<!-- https://mvnrepository.com/artifact/org.openjdk.jmh/jmh-core -->
<dependency>
   <groupId>org.openjdk.jmh</groupId>
   <artifactId>jmh-core</artifactId>
   <version>1.23</version>
</dependency>
<dependency>
   <groupId>org.openjdk.jmh</groupId>
   <artifactId>jmh-generator-annprocess</artifactId>
   <version>1.20</version>
   <scope>provided</scope>
</dependency>
```

然后编写测试代码，我们这里添加 5 个条件判断分支，具体实现代码如下：

```java
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime) // 测试完成时间
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS) // 预热 2 轮，每次 1s
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS) // 测试 5 轮，每次 3s
@Fork(1) // fork 1 个线程
@State(Scope.Thread) // 每个测试线程一个实例
public class SwitchOptimizeTest {

    static Integer _NUM = 9;

    public static void main(String[] args) throws RunnerException {
        // 启动基准测试
        Options opt = new OptionsBuilder()
                .include(SwitchOptimizeTest.class.getSimpleName()) // 要导入的测试类
                .output("/Users/admin/Desktop/jmh-switch.log") // 输出测试结果的文件
                .build();
        new Runner(opt).run(); // 执行测试
    }

    @Benchmark
    public void switchTest() {
        int num1;
        switch (_NUM) {
            case 1:
                num1 = 1;
                break;
            case 3:
                num1 = 3;
                break;
            case 5:
                num1 = 5;
                break;
            case 7:
                num1 = 7;
                break;
            case 9:
                num1 = 9;
                break;
            default:
                num1 = -1;
                break;
        }
    }

    @Benchmark
    public void ifTest() {
        int num1;
        if (_NUM == 1) {
            num1 = 1;
        } else if (_NUM == 3) {
            num1 = 3;
        } else if (_NUM == 5) {
            num1 = 5;
        } else if (_NUM == 7) {
            num1 = 7;
        } else if (_NUM == 9) {
            num1 = 9;
        } else {
            num1 = -1;
        }
    }
}
```

以上代码的测试结果如下：

![图片](data:image/gif;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVQImWNgYGBgAAAABQABh6FO1AAAAABJRU5ErkJggg==)

> 备注：本文的测试环境为：JDK 1.8 / Mac mini (2018) / Idea 2020.1

从以上结果可以看出（Score 列），**switch 的平均执行完成时间比 if 的平均执行完成时间快了约 2.33 倍**。

### 性能分析

为什么 switch 的性能会比 if 的性能高这么多？

这需要从他们字节码说起，我们把他们的代码使用 `javac` 生成字节码如下所示：

```java
public class com.example.optimize.SwitchOptimize {
  static java.lang.Integer _NUM;

  public com.example.optimize.SwitchOptimize();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: return

  public static void main(java.lang.String[]);
    Code:
       0: invokestatic  #7                  // Method switchTest:()V
       3: invokestatic  #12                 // Method ifTest:()V
       6: return

  public static void switchTest();
    Code:
       0: getstatic     #15                 // Field _NUM:Ljava/lang/Integer;
       3: invokevirtual #19                 // Method java/lang/Integer.intValue:()I
       6: tableswitch   { // 1 to 9
                     1: 56
                     2: 83
                     3: 61
                     4: 83
                     5: 66
                     6: 83
                     7: 71
                     8: 83
                     9: 77
               default: 83
          }
      56: iconst_1
      57: istore_0
      58: goto          85
      61: iconst_3
      62: istore_0
      63: goto          85
      66: iconst_5
      67: istore_0
      68: goto          85
      71: bipush        7
      73: istore_0
      74: goto          85
      77: bipush        9
      79: istore_0
      80: goto          85
      83: iconst_m1
      84: istore_0
      85: return

  public static void ifTest();
    Code:
       0: getstatic     #15                 // Field _NUM:Ljava/lang/Integer;
       3: invokevirtual #19                 // Method java/lang/Integer.intValue:()I
       6: iconst_1
       7: if_icmpne     15
      10: iconst_1
      11: istore_0
      12: goto          81
      15: getstatic     #15                 // Field _NUM:Ljava/lang/Integer;
      18: invokevirtual #19                 // Method java/lang/Integer.intValue:()I
      21: iconst_3
      22: if_icmpne     30
      25: iconst_3
      26: istore_0
      27: goto          81
      30: getstatic     #15                 // Field _NUM:Ljava/lang/Integer;
      33: invokevirtual #19                 // Method java/lang/Integer.intValue:()I
      36: iconst_5
      37: if_icmpne     45
      40: iconst_5
      41: istore_0
      42: goto          81
      45: getstatic     #15                 // Field _NUM:Ljava/lang/Integer;
      48: invokevirtual #19                 // Method java/lang/Integer.intValue:()I
      51: bipush        7
      53: if_icmpne     62
      56: bipush        7
      58: istore_0
      59: goto          81
      62: getstatic     #15                 // Field _NUM:Ljava/lang/Integer;
      65: invokevirtual #19                 // Method java/lang/Integer.intValue:()I
      68: bipush        9
      70: if_icmpne     79
      73: bipush        9
      75: istore_0
      76: goto          81
      79: iconst_m1
      80: istore_0
      81: return

  static {};
    Code:
       0: iconst_1
       1: invokestatic  #25                 // Method java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
       4: putstatic     #15                 // Field _NUM:Ljava/lang/Integer;
       7: return
}
```

这些字节码中最重要的信息是“getstatic   #15”，这段代码表示取出“_NUM”变量和条件进行判断。

从上面的字节码可以看出，**在 switch 中只取出了一次变量和条件进行比较，而 if 中每次都会取出变量和条件进行比较，因此 if 的效率就会比 switch 慢很多**。

### 提升测试量

前面的测试代码我们使用了 5 个分支条件来测试了 if 和 switch 的性能，那如果把分支的判断条件增加 3 倍（15 个）时，测试的结果又会怎么呢？

增加至 15 个分支判断的实现代码如下：

```java
package com.example.optimize;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime) // 测试完成时间
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS) // 预热 2 轮，每次 1s
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS) // 测试 5 轮，每次 3s
@Fork(1) // fork 1 个线程
@State(Scope.Thread) // 每个测试线程一个实例
public class SwitchOptimizeTest {

    static Integer _NUM = 1;

    public static void main(String[] args) throws RunnerException {
        // 启动基准测试
        Options opt = new OptionsBuilder()
                .include(SwitchOptimizeTest.class.getSimpleName()) // 要导入的测试类
                .output("/Users/admin/Desktop/jmh-switch.log") // 输出测试结果的文件
                .build();
        new Runner(opt).run(); // 执行测试
    }

    @Benchmark
    public void switchTest() {
        int num1;
        switch (_NUM) {
            case 1:
                num1 = 1;
                break;
            case 2:
                num1 = 2;
                break;
            case 3:
                num1 = 3;
                break;
            case 4:
                num1 = 4;
                break;
            case 5:
                num1 = 5;
                break;
            case 6:
                num1 = 6;
                break;
            case 7:
                num1 = 7;
                break;
            case 8:
                num1 = 8;
                break;
            case 9:
                num1 = 9;
                break;
            case 10:
                num1 = 10;
                break;
            case 11:
                num1 = 11;
                break;
            case 12:
                num1 = 12;
                break;
            case 13:
                num1 = 13;
                break;
            case 14:
                num1 = 14;
                break;
            case 15:
                num1 = 15;
                break;
            default:
                num1 = -1;
                break;
        }
    }

    @Benchmark
    public void ifTest() {
        int num1;
        if (_NUM == 1) {
            num1 = 1;
        } else if (_NUM == 2) {
            num1 = 2;
        } else if (_NUM == 3) {
            num1 = 3;
        } else if (_NUM == 4) {
            num1 = 4;
        } else if (_NUM == 5) {
            num1 = 5;
        } else if (_NUM == 6) {
            num1 = 6;
        } else if (_NUM == 7) {
            num1 = 7;
        } else if (_NUM == 8) {
            num1 = 8;
        } else if (_NUM == 9) {
            num1 = 9;
        } else if (_NUM == 10) {
            num1 = 10;
        } else if (_NUM == 11) {
            num1 = 11;
        } else if (_NUM == 12) {
            num1 = 12;
        } else if (_NUM == 13) {
            num1 = 13;
        } else if (_NUM == 14) {
            num1 = 14;
        } else if (_NUM == 15) {
            num1 = 15;
        } else {
            num1 = -1;
        }
    }
}
```

以上代码的测试结果如下：

![图片](https://mmbiz.qpic.cn/mmbiz_png/HrWw6ZuXCsh4YQ10wiaChib6guDK6OonWMbmdqiaTMO3wz7m86tc9q93CFVzbqPDicARWRvAiaqU8aOnuvZUHthibEtw/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

从 Score 的值可以看出，当分支判断增加至 15 个，switch 的性能比 if 的性能高出了约 3.7 倍，而之前有 5 个分支判断时的测试结果为，switch 的性能比 if 的性能高出了约 2.3 倍，也就是说**分支的判断条件越多，switch 性能高的特性体现的就越明显**。

### switch 的秘密

对于 switch 来说，他最终生成的字节码有两种形态，一种是 tableswitch，另一种是 lookupswitch，决定最终生成的代码使用那种形态取决于 switch 的判断添加是否紧凑，例如到 case 是 1...2...3...4 这种依次递增的判断条件时，使用的是 tableswitch，而像 case 是 1...33...55...22 这种非紧凑型的判断条件时则会使用 lookupswitch，测试代码如下：

```java
public class SwitchOptimize {
    static Integer _NUM = 1;
    public static void main(String[] args) {
        tableSwitchTest();
        lookupSwitchTest();
    }
    public static void tableSwitchTest() {
        int num1;
        switch (_NUM) {
            case 1:
                num1 = 1;
                break;
            case 2:
                num1 = 2;
                break;
            case 3:
                num1 = 3;
                break;
            case 4:
                num1 = 4;
                break;
            case 5:
                num1 = 5;
                break;
            case 6:
                num1 = 6;
                break;
            case 7:
                num1 = 7;
                break;
            case 8:
                num1 = 8;
                break;
            case 9:
                num1 = 9;
                break;
            default:
                num1 = -1;
                break;
        }
    }
    public static void lookupSwitchTest() {
        int num1;
        switch (_NUM) {
            case 1:
                num1 = 1;
                break;
            case 11:
                num1 = 2;
                break;
            case 3:
                num1 = 3;
                break;
            case 4:
                num1 = 4;
                break;
            case 19:
                num1 = 5;
                break;
            case 6:
                num1 = 6;
                break;
            case 33:
                num1 = 7;
                break;
            case 8:
                num1 = 8;
                break;
            case 999:
                num1 = 9;
                break;
            default:
                num1 = -1;
                break;
        }
    }
}
```

对应的字节码如下：

```java
public class com.example.optimize.SwitchOptimize {
  static java.lang.Integer _NUM;

  public com.example.optimize.SwitchOptimize();
    Code:
       0: aload_0
       1: invokespecial #1                  // Method java/lang/Object."<init>":()V
       4: return

  public static void main(java.lang.String[]);
    Code:
       0: invokestatic  #7                  // Method tableSwitchTest:()V
       3: invokestatic  #12                 // Method lookupSwitchTest:()V
       6: return

  public static void tableSwitchTest();
    Code:
       0: getstatic     #15                 // Field _NUM:Ljava/lang/Integer;
       3: invokevirtual #19                 // Method java/lang/Integer.intValue:()I
       6: tableswitch   { // 1 to 9
                     1: 56
                     2: 61
                     3: 66
                     4: 71
                     5: 76
                     6: 81
                     7: 87
                     8: 93
                     9: 99
               default: 105
          }
      56: iconst_1
      57: istore_0
      58: goto          107
      61: iconst_2
      62: istore_0
      63: goto          107
      66: iconst_3
      67: istore_0
      68: goto          107
      71: iconst_4
      72: istore_0
      73: goto          107
      76: iconst_5
      77: istore_0
      78: goto          107
      81: bipush        6
      83: istore_0
      84: goto          107
      87: bipush        7
      89: istore_0
      90: goto          107
      93: bipush        8
      95: istore_0
      96: goto          107
      99: bipush        9
     101: istore_0
     102: goto          107
     105: iconst_m1
     106: istore_0
     107: return

  public static void lookupSwitchTest();
    Code:
       0: getstatic     #15                 // Field _NUM:Ljava/lang/Integer;
       3: invokevirtual #19                 // Method java/lang/Integer.intValue:()I
       6: lookupswitch  { // 9
                     1: 88
                     3: 98
                     4: 103
                     6: 113
                     8: 125
                    11: 93
                    19: 108
                    33: 119
                   999: 131
               default: 137
          }
      88: iconst_1
      89: istore_0
      90: goto          139
      93: iconst_2
      94: istore_0
      95: goto          139
      98: iconst_3
      99: istore_0
     100: goto          139
     103: iconst_4
     104: istore_0
     105: goto          139
     108: iconst_5
     109: istore_0
     110: goto          139
     113: bipush        6
     115: istore_0
     116: goto          139
     119: bipush        7
     121: istore_0
     122: goto          139
     125: bipush        8
     127: istore_0
     128: goto          139
     131: bipush        9
     133: istore_0
     134: goto          139
     137: iconst_m1
     138: istore_0
     139: return

  static {};
    Code:
       0: iconst_1
       1: invokestatic  #25                 // Method java/lang/Integer.valueOf:(I)Ljava/lang/Integer;
       4: putstatic     #15                 // Field _NUM:Ljava/lang/Integer;
       7: return
}
```

从上面字节码可以看出 tableSwitchTest 使用的 tableswitch，而 lookupSwitchTest 则是使用的 lookupswitch。

#### tableswitch VS lookupSwitchTest

当执行一次 tableswitch 时，堆栈顶部的 int 值直接用作表中的索引，以便抓取跳转目标并立即执行跳转。也就是说 tableswitch 的存储结构类似于数组，是直接用索引获取元素的，所以整个查询的时间复杂度是 O(1)，这也意味着它的搜索速度非常快。

而执行 lookupswitch 时，会逐个进行分支比较或者使用二分法进行查询，因此查询时间复杂度是 O(log n)，**所以使用 lookupswitch 会比 tableswitch 慢**。

接下来我们使用实际的代码测试一下，他们两个之间的性能，测试代码如下：

```java
package com.example.optimize;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime) // 测试完成时间
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS) // 预热 2 轮，每次 1s
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS) // 测试 5 轮，每次 3s
@Fork(1) // fork 1 个线程
@State(Scope.Thread) // 每个测试线程一个实例
public class SwitchOptimizeTest {

    static Integer _NUM = -1;

    public static void main(String[] args) throws RunnerException {
        // 启动基准测试
        Options opt = new OptionsBuilder()
                .include(SwitchOptimizeTest.class.getSimpleName()) // 要导入的测试类
                .build();
        new Runner(opt).run(); // 执行测试
    }

    @Benchmark
    public void tableSwitchTest() {
        int num1;
        switch (_NUM) {
            case 1:
                num1 = 1;
                break;
            case 2:
                num1 = 2;
                break;
            case 3:
                num1 = 3;
                break;
            case 4:
                num1 = 4;
                break;
            case 5:
                num1 = 5;
                break;
            case 6:
                num1 = 6;
                break;
            case 7:
                num1 = 7;
                break;
            case 8:
                num1 = 8;
                break;
            case 9:
                num1 = 9;
                break;
            default:
                num1 = -1;
                break;
        }
    }

    @Benchmark
    public void lookupSwitchTest() {
        int num1;
        switch (_NUM) {
            case 1:
                num1 = 1;
                break;
            case 11:
                num1 = 2;
                break;
            case 3:
                num1 = 3;
                break;
            case 4:
                num1 = 4;
                break;
            case 19:
                num1 = 5;
                break;
            case 6:
                num1 = 6;
                break;
            case 33:
                num1 = 7;
                break;
            case 8:
                num1 = 8;
                break;
            case 999:
                num1 = 9;
                break;
            default:
                num1 = -1;
                break;
        }
    }
}
```

以上代码的测试结果如下：

![图片](https://mmbiz.qpic.cn/mmbiz_png/HrWw6ZuXCsh4YQ10wiaChib6guDK6OonWMSubwY5HJflA741zWriaIhbXU24ThgqHamb9vzQBibwo1GKRPcicI3CbeQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

可以看出在分支判断为 9 个时，**tableswitch 的性能比 lookupwitch 的性能快了约 1.3 倍。但即使这样 lookupwitch 依然比 if 查询性能要高很多**。

### 总 结

switch 的判断条件是 5 个时，性能比 if 高出了约 2.3 倍，**而当判断条件的数量越多时，他们的性能相差就越大**。而 switch 在编译为字节码时，会根据 switch 的判断条件是否紧凑生成两种代码：tableswitch（紧凑时生成）和 lookupswitch（非紧凑时生成），其中 tableswitch 是采用类似于数组的存储结构，直接根据索引查询元素；而 lookupswitch 则需要逐个查询或者使用二分法查询，**因此 tableswitch 的性能会比 lookupswitch 的性能高，但无论如何 switch 的性能都比 if 的性能要高**。



# Java面试题

[链接](https://blog.csdn.net/ThinkWon/article/details/104390612)

本文知识点目录

- Java概述

- - 何为编程
  - 什么是Java
  - jdk1.5之后的三大版本
  - JVM、JRE和JDK的关系
  - 什么是跨平台性？原理是什么
  - Java语言有哪些特点？
  - 什么是字节码？采用字节码的最大好处是什么
  - 什么是Java程序的主类？应用程序和小程序的主类有何不同？
  - Java应用程序与小程序之间有那些差别？
  - Java和C++的区别
  - Oracle JDK 和 OpenJDK 的对比

- 基础语法

- - 数据类型

  - - Java有哪些数据类型
    - switch 是否能作用在 byte 上，是否能作用在 long 上，是否能作用在 String 上
    - 用最有效率的方法计算 2 乘以 8
    - Math.round(11.5) 等于多少？Math.round(-11.5)等于多少
    - float f=3.4;是否正确
    - short s1 = 1; s1 = s1 + 1;有错吗?short s1 = 1; s1 += 1;有错吗

  - 编码

  - - Java语言采用何种编码方案？有何特点？

  - 注释

  - - 什么是Java注释

  - 访问修饰符

  - - 访问修饰符 public,private,protected,以及不写（默认）时的区别

  - 运算符

  - - &和&&的区别

  - 关键字

  - - Java 有没有 goto
    - final 有什么用？
    - final finally finalize区别
    - this关键字的用法
    - super关键字的用法
    - this与super的区别
    - static存在的主要意义
    - static的独特之处
    - static应用场景
    - static注意事项

  - 流程控制语句

  - - break ,continue ,return 的区别及作用
    - 在 Java 中，如何跳出当前的多重嵌套循环

- 面向对象

- - 面向对象概述

  - - 面向对象和面向过程的区别

  - 面向对象三大特性

  - - 面向对象的特征有哪些方面
    - 什么是多态机制？Java语言是如何实现多态的？
    - 面向对象五大基本原则是什么（可选）

  - 类与接口

  - - 抽象类和接口的对比
    - 普通类和抽象类有哪些区别？
    - 抽象类能使用 final 修饰吗？
    - 创建一个对象用什么关键字？对象实例与对象引用有何不同？

  - 变量与方法

  - - 成员变量与局部变量的区别有哪些
    - 在Java中定义一个不做事且没有参数的构造方法的作用
    - 在调用子类构造方法之前会先调用父类没有参数的构造方法，其目的是？
    - 一个类的构造方法的作用是什么？若一个类没有声明构造方法，改程序能正确执行吗？为什么？
    - 构造方法有哪些特性？
    - 静态变量和实例变量区别
    - 静态变量与普通变量区别
    - 静态方法和实例方法有何不同？
    - 在一个静态方法内调用一个非静态成员为什么是非法的？
    - 什么是方法的返回值？返回值的作用是什么？

  - 内部类

  - - 什么是内部类？

    - 内部类的分类有哪些

    - - 静态内部类
      - 成员内部类
      - 局部内部类
      - 匿名内部类

    - 内部类的优点

    - 内部类有哪些应用场景

    - 局部内部类和匿名内部类访问局部变量的时候，为什么变量必须要加上final？

    - 内部类相关，看程序说出运行结果

  - 重写与重载

  - - 构造器（constructor）是否可被重写（override）
    - 重载（Overload）和重写（Override）的区别。重载的方法能否根据返回类型进行区分？

  - 对象相等判断

  - - == 和 equals 的区别是什么
    - hashCode 与 equals (重要)
    - 对象的相等与指向他们的引用相等，两者有什么不同？

  - 值传递

  - - 当一个对象被当作参数传递到一个方法后，此方法可改变这个对象的属性，并可返回变化后的结果，那么这里到底是值传递还是引用传递
    - 为什么 Java 中只有值传递
    - 值传递和引用传递有什么区别

  - Java包

  - - JDK 中常用的包有哪些
    - import java和javax有什么区别

- IO流

- - java 中 IO 流分为几种?
  - BIO,NIO,AIO 有什么区别?
  - Files的常用方法都有哪些？

- 反射

- - 什么是反射机制？
  - 反射机制优缺点
  - 反射机制的应用场景有哪些？
  - Java获取反射的三种方法

- 网络编程

- 常用API

- - String相关

  - - 字符型常量和字符串常量的区别
    - 什么是字符串常量池？
    - String 是最基本的数据类型吗
    - String有哪些特性
    - String为什么是不可变的吗？
    - String真的是不可变的吗？
    - 是否可以继承 String 类
    - String str="i"与 String str=new String("i")一样吗？
    - String s = new String("xyz");创建了几个字符串对象
    - 如何将字符串反转？
    - 数组有没有 length()方法？String 有没有 length()方法
    - String 类的常用方法都有那些？
    - 在使用 HashMap 的时候，用 String 做 key 有什么好处？
    - String和StringBuffer、StringBuilder的区别是什么？String为什么是不可变的

  - Date相关

  - 包装类相关

  - - 自动装箱与拆箱
    - int 和 Integer 有什么区别
    - Integer a= 127 与 Integer b = 127相等吗
    - 

## Java概述

### **何为编程**

编程就是让计算机为解决某个问题而使用某种程序设计语言编写程序代码，并最终得到结果的过程。

为了使计算机能够理解人的意图，人类就必须要将需解决的问题的思路、方法、和手段通过计算机能够理解的形式告诉计算机，使得计算机能够根据人的指令一步一步去工作，完成某种特定的任务。这种人和计算机之间交流的过程就是编程。

### **什么是Java**

Java是一门面向对象编程语言，不仅吸收了C++语言的各种优点，还摒弃了C++里难以理解的多继承、指针等概念，因此Java语言具有功能强大和简单易用两个特征。Java语言作为静态面向对象编程语言的代表，极好地实现了面向对象理论，允许程序员以优雅的思维方式进行复杂的编程 。

### **jdk1.5之后的三大版本**

- Java SE（J2SE，Java 2 Platform Standard Edition，标准版）
  Java SE 以前称为 J2SE。它允许开发和部署在桌面、服务器、嵌入式环境和实时环境中使用的 Java 应用程序。Java SE 包含了支持 Java Web 服务开发的类，并为Java EE和Java ME提供基础。
- Java EE（J2EE，Java 2 Platform Enterprise Edition，企业版）
  Java EE 以前称为 J2EE。企业版本帮助开发和部署可移植、健壮、可伸缩且安全的服务器端Java 应用程序。Java EE 是在 Java SE 的基础上构建的，它提供 Web 服务、组件模型、管理和通信 API，可以用来实现企业级的面向服务体系结构（service-oriented architecture，SOA）和 Web2.0应用程序。2018年2月，Eclipse 宣布正式将 JavaEE 更名为 JakartaEE
- Java ME（J2ME，Java 2 Platform Micro Edition，微型版）
  Java ME 以前称为 J2ME。Java ME 为在移动设备和嵌入式设备（比如手机、PDA、电视机顶盒和打印机）上运行的应用程序提供一个健壮且灵活的环境。Java ME 包括灵活的用户界面、健壮的安全模型、许多内置的网络协议以及对可以动态下载的连网和离线应用程序的丰富支持。基于 Java ME 规范的应用程序只需编写一次，就可以用于许多设备，而且可以利用每个设备的本机功能。

### JVM、JRE和JDK的关系

**JVM**
Java Virtual Machine是Java虚拟机，Java程序需要运行在虚拟机上，不同的平台有自己的虚拟机，因此Java语言可以实现跨平台。

**JRE**
Java Runtime Environment包括Java虚拟机和Java程序所需的核心类库等。核心类库主要是java.lang包：包含了运行Java程序必不可少的系统类，如基本数据类型、基本数学函数、字符串处理、线程、异常处理类等，系统缺省加载这个包

如果想要运行一个开发好的Java程序，计算机中只需要安装JRE即可。

**JDK**
Java Development Kit是提供给Java开发人员使用的，其中包含了Java的开发工具，也包括了JRE。所以安装了JDK，就无需再单独安装JRE了。其中的开发工具：编译工具(javac.exe)，打包工具(jar.exe)等

**JVM&JRE&JDK关系图**

![图片](https://mmbiz.qpic.cn/mmbiz_png/rAMaszgAyWpblibxHNficQAaicBURw5uxaYRxOOVacKtycFKG8VliaA5u2PferEA1iaDTic7etblzDklp8jHWDkEDdlA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

### **什么是跨平台性？原理是什么**

所谓跨平台性，是指java语言编写的程序，一次编译后，可以在多个系统平台上运行。

实现原理：Java程序是通过java虚拟机在系统平台上运行的，只要该系统可以安装相应的java虚拟机，该系统就可以运行java程序。

### **Java语言有哪些特点**

简单易学（Java语言的语法与C语言和C++语言很接近）

面向对象（封装，继承，多态）

平台无关性（Java虚拟机实现平台无关性）

支持网络编程并且很方便（Java语言诞生本身就是为简化网络编程设计的）

支持多线程（多线程机制使应用程序在同一时间并行执行多项任）

健壮性（Java语言的强类型机制、异常处理、垃圾的自动收集等）

安全性

### **什么是字节码？采用字节码的最大好处是什么**

**字节码**：Java源代码经过虚拟机编译器编译后产生的文件（即扩展为.class的文件），它不面向任何特定的处理器，只面向虚拟机。

**采用字节码的好处：**

Java语言通过字节码的方式，在一定程度上解决了传统解释型语言执行效率低的问题，同时又保留了解释型语言可移植的特点。所以Java程序运行时比较高效，而且，由于字节码并不专对一种特定的机器，因此，Java程序无须重新编译便可在多种不同的计算机上运行。

**先看下java中的编译器和解释器**：

Java中引入了虚拟机的概念，即在机器和编译程序之间加入了一层抽象的虚拟机器。这台虚拟的机器在任何平台上都提供给编译程序一个的共同的接口。编译程序只需要面向虚拟机，生成虚拟机能够理解的代码，然后由解释器来将虚拟机代码转换为特定系统的机器码执行。在Java中，这种供虚拟机理解的代码叫做字节码（即扩展为.class的文件），它不面向任何特定的处理器，只面向虚拟机。每一种平台的解释器是不同的，但是实现的虚拟机是相同的。Java源程序经过编译器编译后变成字节码，字节码由虚拟机解释执行，虚拟机将每一条要执行的字节码送给解释器，解释器将其翻译成特定机器上的机器码，然后在特定的机器上运行，这就是上面提到的Java的特点的编译与解释并存的解释。

```
Java源代码---->编译器---->jvm可执行的Java字节码(即虚拟指令)---->jvm---->jvm中解释器----->机器可执行的二进制机器码---->程序运行。
```

### **什么是Java程序的主类？应用程序和小程序的主类有何不同？**

一个程序中可以有多个类，但只能有一个类是主类。在Java应用程序中，这个主类是指包含main()方法的类。而在Java小程序中，这个主类是一个继承自系统类JApplet或Applet的子类。应用程序的主类不一定要求是public类，但小程序的主类要求必须是public类。主类是Java程序执行的入口点。

### **Java应用程序与小程序之间有那些差别？**

简单说应用程序是从主线程启动(也就是main()方法)。applet小程序没有main方法，主要是嵌在浏览器页面上运行(调用init()线程或者run()来启动)，嵌入浏览器这点跟flash的小游戏类似。

### **Java和C++的区别**

我知道很多人没学过C++，但是面试官就是没事喜欢拿咱们Java和C++比呀！没办法！！！就算没学过C++，也要记下来！

- 都是面向对象的语言，都支持封装、继承和多态
- Java不提供指针来直接访问内存，程序内存更加安全
- Java的类是单继承的，C++支持多重继承；虽然Java的类不可以多继承，但是接口可以多继承。
- Java有自动内存管理机制，不需要程序员手动释放无用内存

### **Oracle JDK 和 OpenJDK 的对比**

- Oracle JDK版本将每三年发布一次，而OpenJDK版本每三个月发布一次；
- OpenJDK 是一个参考模型并且是完全开源的，而Oracle JDK是OpenJDK的一个实现，并不是完全开源的；
- Oracle JDK 比 OpenJDK 更稳定。OpenJDK和Oracle JDK的代码几乎相同，但Oracle JDK有更多的类和一些错误修复。因此，如果您想开发企业/商业软件，我建议您选择Oracle JDK，因为它经过了彻底的测试和稳定。某些情况下，有些人提到在使用OpenJDK 可能会遇到了许多应用程序崩溃的问题，但是，只需切换到Oracle JDK就可以解决问题；
- 在响应性和JVM性能方面，Oracle JDK与OpenJDK相比提供了更好的性能；
- Oracle JDK不会为即将发布的版本提供长期支持，用户每次都必须通过更新到最新版本获得支持来获取最新版本；
- Oracle JDK根据二进制代码许可协议获得许可，而OpenJDK根据GPL v2许可获得许可。

## 基础语法

### 数据类型

#### **Java有哪些数据类型**

**定义**：Java语言是强类型语言，对于每一种数据都定义了明确的具体的数据类型，在内存中分配了不同大小的内存空间。

**分类**

- 基本数据类型

- - 整数类型(byte,short,int,long)
  - 浮点类型(float,double)
  - 数值型
  - 字符型(char)
  - 布尔型(boolean)

- 引用数据类型

- - 类(class)
  - 接口(interface)
  - 数组([])

**Java基本数据类型图**

![图片](https://mmbiz.qpic.cn/mmbiz_png/rAMaszgAyWpblibxHNficQAaicBURw5uxaYiaeFD6IjtjE65ia6gLV6UzwtGMibia0qvtxzNnC1cPGR5AbcNYCXT64aEA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

#### **switch 是否能作用在 byte 上，是否能作用在 long 上，是否能作用在 String 上**

在 Java 5 以前，switch(expr)中，expr 只能是 byte、short、char、int。从 Java5 开始，Java 中引入了枚举类型，expr 也可以是 enum 类型，从 Java 7 开始，expr 还可以是字符串（String），但是长整型（long）在目前所有的版本中都是不可以的。

#### **用最有效率的方法计算 2 乘以 8**

2 << 3（左移 3 位相当于乘以 2 的 3 次方，右移 3 位相当于除以 2 的 3 次方）。

#### **Math.round(11.5) 等于多少？Math.round(-11.5)等于多少**

Math.round(11.5)的返回值是 12，Math.round(-11.5)的返回值是-11。四舍五入的原理是在参数上加 0.5 然后进行下取整。

#### **float f=3.4;是否正确**

不正确。3.4 是双精度数，将双精度型（double）赋值给浮点型（float）属于下转型（down-casting，也称为窄化）会造成精度损失，因此需要强制类型转换float f =(float)3.4; 或者写成 float f =3.4F;。

#### **short s1 = 1; s1 = s1 + 1;有错吗?short s1 = 1; s1 += 1;有错吗**

对于 short s1 = 1; s1 = s1 + 1;由于 1 是 int 类型，因此 s1+1 运算结果也是 int型，需要强制转换类型才能赋值给 short 型。

而 short s1 = 1; s1 += 1;可以正确编译，因为 s1+= 1;相当于 s1 = (short(s1 + 1);其中有隐含的强制类型转换。

### **编码**

#### **Java语言采用何种编码方案？有何特点？**

Java语言采用Unicode编码标准，Unicode（标准码），它为每个字符制订了一个唯一的数值，因此在任何的语言，平台，程序都可以放心的使用。

### 注释

#### **什么Java注释**

**定义**：用于解释说明程序的文字

**分类**

- 单行注释
  格式：// 注释文字
- 多行注释
  格式：/* 注释文字 */
- 文档注释
  格式：/** 注释文字 */

**作用**

在程序中，尤其是复杂的程序中，适当地加入注释可以增加程序的可读性，有利于程序的修改、调试和交流。注释的内容在程序编译的时候会被忽视，不会产生目标代码，注释的部分不会对程序的执行结果产生任何影响。

注意事项：多行和文档注释都不能嵌套使用。

### **访问修饰符**

#### 访问修饰符 public,private,protected,以及不写（默认）时的区别

**定义**：Java中，可以使用访问修饰符来保护对类、变量、方法和构造方法的访问。Java 支持 4 种不同的访问权限。

**分类**

private : 在同一类内可见。使用对象：变量、方法。注意：不能修饰类（外部类）
default (即缺省，什么也不写，不使用任何关键字）: 在同一包内可见，不使用任何修饰符。使用对象：类、接口、变量、方法。
protected : 对同一包内的类和所有子类可见。使用对象：变量、方法。注意：不能修饰类（外部类）。
public : 对所有类可见。使用对象：类、接口、变量、方法

**访问修饰符图**

![图片](https://mmbiz.qpic.cn/mmbiz_png/rAMaszgAyWpblibxHNficQAaicBURw5uxaYHH3PwsT3uv5eYw341sEcibLqQMdVtchPRnmmSGAuXKWZyGTlzhvibozA/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

### 运算符

#### **&和&&的区别**

&运算符有两种用法：(1)按位与；(2)逻辑与。

&&运算符是短路与运算。逻辑与跟短路与的差别是非常巨大的，虽然二者都要求运算符左右两端的布尔值都是true 整个表达式的值才是 true。&&之所以称为短路运算，是因为如果&&左边的表达式的值是 false，右边的表达式会被直接短路掉，不会进行运算。

注意：逻辑或运算符（|）和短路或运算符（||）的差别也是如此。

### 关键字

#### **Java 有没有 goto**

goto 是 Java 中的保留字，在目前版本的 Java 中没有使用。

#### **final 有什么用？**

用于修饰类、属性和方法；

- 被final修饰的类不可以被继承
- 被final修饰的方法不可以被重写
- 被final修饰的变量不可以被改变，被final修饰不可变的是变量的引用，而不是引用指向的内容，引用指向的内容是可以改变的

#### **final finally finalize区别**

- final可以修饰类、变量、方法，修饰类表示该类不能被继承、修饰方法表示该方法不能被重写、修饰变量表
  示该变量是一个常量不能被重新赋值。
- finally一般作用在try-catch代码块中，在处理异常的时候，通常我们将一定要执行的代码方法finally代码块
  中，表示不管是否出现异常，该代码块都会执行，一般用来存放一些关闭资源的代码。
- finalize是一个方法，属于Object类的一个方法，而Object类是所有类的父类，该方法一般由垃圾回收器来调
  用，当我们调用System.gc() 方法的时候，由垃圾回收器调用finalize()，回收垃圾，一个对象是否可回收的
  最后判断。

#### **this关键字的用法**

this是自身的一个对象，代表对象本身，可以理解为：指向对象本身的一个指针。

this的用法在java中大体可以分为3种：

1.普通的直接引用，this相当于是指向当前对象本身。

2.形参与成员名字重名，用this来区分：

```
public Person(String name, int age) {
    this.name = name;
    this.age = age;
}
```



3.引用本类的构造函数

```
class Person{
    private String name;
    private int age;
    
    public Person() {
    }
 
    public Person(String name) {
        this.name = name;
    }
    public Person(String name, int age) {
        this(name);
        this.age = age;
    }
}
```

#### **super关键字的用法**

super可以理解为是指向自己超（父）类对象的一个指针，而这个超类指的是离自己最近的一个父类。

super也有三种用法：

1.普通的直接引用

与this类似，super相当于是指向当前对象的父类的引用，这样就可以用super.xxx来引用父类的成员。

2.子类中的成员变量或方法与父类中的成员变量或方法同名时，用super进行区分

```
class Person{
    protected String name;
 
    public Person(String name) {
        this.name = name;
    }
 
}
 
class Student extends Person{
    private String name;
 
    public Student(String name, String name1) {
        super(name);
        this.name = name1;
    }
 
    public void getInfo(){
        System.out.println(this.name); //Child
        System.out.println(super.name); //Father
    }
 
}

public class Test {
    public static void main(String[] args) {
       Student s1 = new Student("Father","Child");
       s1.getInfo();
    }
}
```



3.引用父类构造函数

3、引用父类构造函数

- super（参数）：调用父类中的某一个构造函数（应该为构造函数中的第一条语句）。
- this（参数）：调用本类中另一种形式的构造函数（应该为构造函数中的第一条语句）。

#### **this与super的区别**

- super:　它引用当前对象的直接父类中的成员（用来访问直接父类中被隐藏的父类中成员数据或函数，基类与派生类中有相同成员定义时如：super.变量名 super.成员函数据名（实参）
- this：它代表当前对象名（在程序中易产生二义性之处，应使用this来指明当前对象；如果函数的形参与类中的成员数据同名，这时需用this来指明成员变量名）
- super()和this()类似,区别是，super()在子类中调用父类的构造方法，this()在本类内调用本类的其它构造方法。
- super()和this()均需放在构造方法内第一行。
- 尽管可以用this调用一个构造器，但却不能调用两个。
- this和super不能同时出现在一个构造函数里面，因为this必然会调用其它的构造函数，其它的构造函数必然也会有super语句的存在，所以在同一个构造函数里面有相同的语句，就失去了语句的意义，编译器也不会通过。
- this()和super()都指的是对象，所以，均不可以在static环境中使用。包括：static变量,static方法，static语句块。
- 从本质上讲，this是一个指向本对象的指针, 然而super是一个Java关键字。

#### **static存在的主要意义**

static的主要意义是在于创建独立于具体对象的域变量或者方法。**以致于即使没有创建对象，也能使用属性和调用方法**！

static关键字还有一个比较关键的作用就是 **用来形成静态代码块以优化程序性能**。static块可以置于类中的任何地方，类中可以有多个static块。在类初次被加载的时候，会按照static块的顺序来执行每个static块，并且只会执行一次。

为什么说static块可以用来优化程序性能，是因为它的特性:只会在类加载的时候执行一次。因此，很多时候会将一些只需要进行一次的初始化操作都放在static代码块中进行。

#### **static的独特之处**

1、被static修饰的变量或者方法是独立于该类的任何对象，也就是说，这些变量和方法**不属于任何一个实例对象，而是被类的实例对象所共享**。

> 怎么理解 “被类的实例对象所共享” 这句话呢？就是说，一个类的静态成员，它是属于大伙的【大伙指的是这个类的多个对象实例，我们都知道一个类可以创建多个实例！】，所有的类对象共享的，不像成员变量是自个的【自个指的是这个类的单个实例对象】…我觉得我已经讲的很通俗了，你明白了咩？

2、在该类被第一次加载的时候，就会去加载被static修饰的部分，而且只在类第一次使用时加载并进行初始化，注意这是第一次用就要初始化，后面根据需要是可以再次赋值的。

3、static变量值在类加载的时候分配空间，以后创建类对象的时候不会重新分配。赋值的话，是可以任意赋值的！

4、被static修饰的变量或者方法是优先于对象存在的，也就是说当一个类加载完毕之后，即便没有创建对象，也可以去访问。

#### **static应用场景**

因为static是被类的实例对象所共享，因此如果**某个成员变量是被所有对象所共享的，那么这个成员变量就应该定义为静态变量**。

因此比较常见的static应用场景有：

> 1、修饰成员变量 2、修饰成员方法 3、静态代码块 4、修饰类【只能修饰内部类也就是静态内部类】 5、静态导包

#### **static注意事项**

1、静态只能访问静态。2、非静态既可以访问非静态的，也可以访问静态的。

### 流程控制语句

#### **break ,continue ,return 的区别及作用**

break 跳出总上一层循环，不再执行循环(结束当前的循环体)

continue 跳出本次循环，继续执行下次循环(结束正在执行的循环 进入下一个循环条件)

return 程序返回，不再执行下面的代码(结束当前的方法 直接返回)

#### **在 Java 中，如何跳出当前的多重嵌套循环**

在Java中，要想跳出多重循环，可以在外面的循环语句前定义一个标号，然后在里层循环体的代码中使用带有标号的break 语句，即可跳出外层循环。例如：

```
public static void main(String[] args) {
    ok:
    for (int i = 0; i < 10; i++) {
        for (int j = 0; j < 10; j++) {
            System.out.println("i=" + i + ",j=" + j);
            if (j == 5) {
                break ok;
            }

        }
    }
}
```

**面向过程**：

优点：性能比面向对象高，因为类调用时需要实例化，开销比较大，比较消耗资源;比如单片机、嵌入式开发、Linux/Unix等一般采用面向过程开发，性能是最重要的因素。

缺点：没有面向对象易维护、易复用、易扩展

**面向对象**：

优点：易维护、易复用、易扩展，由于面向对象有封装、继承、多态性的特性，可以设计出低耦合的系统，使系统更加灵活、更加易于维护

缺点：性能比面向过程低

面向过程是具体化的，流程化的，解决一个问题，你需要一步一步的分析，一步一步的实现。

面向对象是模型化的，你只需抽象出一个类，这是一个封闭的盒子，在这里你拥有数据也拥有解决问题的方法。需要什么功能直接使用就可以了，不必去一步一步的实现，至于这个功能是如何实现的，管我们什么事？我们会用就可以了。

面向对象的底层其实还是面向过程，把面向过程抽象成类，然后封装，方便我们使用的就是面向对象了。

**面向对象的特征主要有以下几个方面：**

**抽象**：抽象是将一类对象的共同特征总结出来构造类的过程，包括数据抽象和行为抽象两方面。抽象只关注对象有哪些属性和行为，并不关注这些行为的细节是什么。

**封装：**封装把一个对象的属性私有化，同时提供一些可以被外界访问的属性的方法，如果属性不想被外界访问，我们大可不必提供方法给外界访问。但是如果一个类没有提供给外界访问的方法，那么这个类也没有什么意义了。

**继承：**继承是使用已存在的类的定义作为基础建立新类的技术，新类的定义可以增加新的数据或新的功能，也可以用父类的功能，但不能选择性地继承父类。通过使用继承我们能够非常方便地复用以前的代码。

**关于继承如下 3 点请记住：**

- 子类拥有父类非 private 的属性和方法。
- 子类可以拥有自己属性和方法，即子类可以对父类进行扩展。
- 子类可以用自己的方式实现父类的方法。（以后介绍）。

**多态**

所谓多态就是指程序中定义的引用变量所指向的具体类型和通过该引用变量发出的方法调用在编程时并不确定，而是在程序运行期间才确定，即一个引用变量到底会指向哪个类的实例对象，该引用变量发出的方法调用到底是哪个类中实现的方法，必须在由程序运行期间才能决定。

**多态性**：父类或接口定义的引用变量可以指向子类或具体实现类的实例对象。提高了程序的拓展性。

在Java中有两种形式可以实现多态：继承（多个子类对同一方法的重写）和接口（实现接口并覆盖接口中同一方法）。

方法重载（overload）实现的是编译时的多态性（也称为前绑定），而方法重写（override）实现的是运行时的多态性（也称为后绑定）。

一个引用变量到底会指向哪个类的实例对象，该引用变量发出的方法调用到底是哪个类中实现的方法，必须在由程序运行期间才能决定。运行时的多态是面向对象最精髓的东西，要实现多态需要做两件事：

- 方法重写（子类继承父类并重写父类中已有的或抽象的方法）；
- 对象造型（用父类型引用子类型对象，这样同样的引用调用同样的方法就会根据子类对象的不同而表现出不同的行为）。

#### **什么是多态机制？Java语言是如何实现多态的？**

所谓多态就是指程序中定义的引用变量所指向的具体类型和通过该引用变量发出的方法调用在编程时并不确定，而是在程序运行期间才确定，即一个引用变量倒底会指向哪个类的实例对象，该引用变量发出的方法调用到底是哪个类中实现的方法，必须在由程序运行期间才能决定。因为在程序运行时才确定具体的类，这样，不用修改源程序代码，就可以让引用变量绑定到各种不同的类实现上，从而导致该引用调用的具体方法随之改变，即不修改程序代码就可以改变程序运行时所绑定的具体代码，让程序可以选择多个运行状态，这就是多态性。

多态分为编译时多态和运行时多态。其中编辑时多态是静态的，主要是指方法的重载，它是根据参数列表的不同来区分不同的函数，通过编辑之后会变成两个不同的函数，在运行时谈不上多态。而运行时多态是动态的，它是通过动态绑定来实现的，也就是我们所说的多态性。

**多态的实现**

Java实现多态有三个必要条件：继承、重写、向上转型。

继承：在多态中必须存在有继承关系的子类和父类。

重写：子类对父类中某些方法进行重新定义，在调用这些方法时就会调用子类的方法。

向上转型：在多态中需要将子类的引用赋给父类对象，只有这样该引用才能够具备技能调用父类的方法和子类的方法。

只有满足了上述三个条件，我们才能够在同一个继承结构中使用统一的逻辑实现代码处理不同的对象，从而达到执行不同的行为。

对于Java而言，它多态的实现机制遵循一个原则：当超类对象引用变量引用子类对象时，被引用对象的类型而不是引用变量的类型决定了调用谁的成员方法，但是这个被调用的方法必须是在超类中定义过的，也就是说被子类覆盖的方法。

#### **面向对象五大基本原则是什么（可选）**

- 单一职责原则SRP(Single Responsibility Principle)
  类的功能要单一，不能包罗万象，跟杂货铺似的。
- 开放封闭原则OCP(Open－Close Principle)
  一个模块对于拓展是开放的，对于修改是封闭的，想要增加功能热烈欢迎，想要修改，哼，一万个不乐意。
- 里式替换原则LSP(the Liskov Substitution Principle LSP)
  子类可以替换父类出现在父类能够出现的任何地方。比如你能代表你爸去你姥姥家干活。哈哈~~
- 依赖倒置原则DIP(the Dependency Inversion Principle DIP)
  高层次的模块不应该依赖于低层次的模块，他们都应该依赖于抽象。抽象不应该依赖于具体实现，具体实现应该依赖于抽象。就是你出国要说你是中国人，而不能说你是哪个村子的。比如说中国人是抽象的，下面有具体的xx省，xx市，xx县。你要依赖的抽象是中国人，而不是你是xx村的。
- 接口分离原则ISP(the Interface Segregation Principle ISP)
  设计时采用多个与特定客户类有关的接口比采用一个通用的接口要好。就比如一个手机拥有打电话，看视频，玩游戏等功能，把这几个功能拆分成不同的接口，比在一个接口里要好的多。

### 类与接口

#### **抽象类和接口的对比**

抽象类是用来捕捉子类的通用特性的。接口是抽象方法的集合。

从设计层面来说，抽象类是对类的抽象，是一种模板设计，接口是行为的抽象，是一种行为的规范。

**相同点**

- 接口和抽象类都不能实例化
- 都位于继承的顶端，用于被其他实现或继承
- 都包含抽象方法，其子类都必须覆写这些抽象方法

**不同点**

| 参数       | 抽象类                                                       | 接口                                                         |
| :--------- | :----------------------------------------------------------- | :----------------------------------------------------------- |
| 声明       | 抽象类使用abstract关键字声明                                 | 接口使用interface关键字声明                                  |
| 实现       | 子类使用extends关键字来继承抽象类。如果子类不是抽象类的话，它需要提供抽象类中所有声明的方法的实现 | 子类使用implements关键字来实现接口。它需要提供接口中所有声明的方法的实现 |
| 构造器     | 抽象类可以有构造器                                           | 接口不能有构造器                                             |
| 访问修饰符 | 抽象类中的方法可以是任意访问修饰符                           | 接口方法默认修饰符是public。并且不允许定义为 private 或者 protected |
| 多继承     | 一个类最多只能继承一个抽象类                                 | 一个类可以实现多个接口                                       |
| 字段声明   | 抽象类的字段声明可以是任意的                                 | 接口的字段默认都是 static 和 final 的                        |

**备注**：Java8中接口中引入默认方法和静态方法，以此来减少抽象类和接口之间的差异。

现在，我们可以为接口提供默认实现的方法了，并且不用强制子类来实现它。

接口和抽象类各有优缺点，在接口和抽象类的选择上，必须遵守这样一个原则：

- 行为模型应该总是通过接口而不是抽象类定义，所以通常是优先选用接口，尽量少用抽象类。
- 选择抽象类的时候通常是如下情况：需要定义子类的行为，又要为子类提供通用的功能。

#### **普通类和抽象类有哪些区别？**

- 普通类不能包含抽象方法，抽象类可以包含抽象方法。
- 抽象类不能直接实例化，普通类可以直接实例化。

#### **抽象类能使用 final 修饰吗？**

不能，定义抽象类就是让其他类继承的，如果定义为 final 该类就不能被继承，这样彼此就会产生矛盾，所以 final 不能修饰抽象类

#### **创建一个对象用什么关键字？对象实例与对象引用有何不同？**

new关键字，new创建对象实例（对象实例在堆内存中），对象引用指向对象实例（对象引用存放在栈内存中）。一个对象引用可以指向0个或1个对象（一根绳子可以不系气球，也可以系一个气球）;一个对象可以有n个引用指向它（可以用n条绳子系住一个气球）

### 变量与方法

#### **成员变量与局部变量的区别有哪些**

变量：在程序执行的过程中，在某个范围内其值可以发生改变的量。从本质上讲，变量其实是内存中的一小块区域

成员变量：方法外部，类内部定义的变量

局部变量：类的方法中的变量。

成员变量和局部变量的区别

**作用域**

成员变量：针对整个类有效。
局部变量：只在某个范围内有效。(一般指的就是方法,语句体内)

**存储位置**

成员变量：随着对象的创建而存在，随着对象的消失而消失，存储在堆内存中。
局部变量：在方法被调用，或者语句被执行的时候存在，存储在栈内存中。当方法调用完，或者语句结束后，就自动释放。

**生命周期**

成员变量：随着对象的创建而存在，随着对象的消失而消失
局部变量：当方法调用完，或者语句结束后，就自动释放。

**初始值**

成员变量：有默认初始值。

局部变量：没有默认初始值，使用前必须赋值。

使用原则

在使用变量时需要遵循的原则为：就近原则
首先在局部范围找，有就使用；接着在成员位置找。

#### **在Java中定义一个不做事且没有参数的构造方法的作用**

Java程序在执行子类的构造方法之前，如果没有用super()来调用父类特定的构造方法，则会调用父类中“没有参数的构造方法”。因此，如果父类中只定义了有参数的构造方法，而在子类的构造方法中又没有用super()来调用父类中特定的构造方法，则编译时将发生错误，因为Java程序在父类中找不到没有参数的构造方法可供执行。解决办法是在父类里加上一个不做事且没有参数的构造方法。

#### **在调用子类构造方法之前会先调用父类没有参数的构造方法，其目的是？**

帮助子类做初始化工作。

#### **一个类的构造方法的作用是什么？若一个类没有声明构造方法，改程序能正确执行吗？为什么？**

主要作用是完成对类对象的初始化工作。可以执行。因为一个类即使没有声明构造方法也会有默认的不带参数的构造方法。

#### **构造方法有哪些特性？**

名字与类名相同；

没有返回值，但不能用void声明构造函数；

生成类的对象时自动执行，无需调用。

#### **静态变量和实例变量区别**

静态变量：静态变量由于不属于任何实例对象，属于类的，所以在内存中只会有一份，在类的加载过程中，JVM只为静态变量分配一次内存空间。

实例变量：每次创建对象，都会为每个对象分配成员变量内存空间，实例变量是属于实例对象的，在内存中，创建几次对象，就有几份成员变量。

#### **静态变量与普通变量区别**

static变量也称作静态变量，静态变量和非静态变量的区别是：静态变量被所有的对象所共享，在内存中只有一个副本，它当且仅当在类初次加载时会被初始化。而非静态变量是对象所拥有的，在创建对象的时候被初始化，存在多个副本，各个对象拥有的副本互不影响。

还有一点就是static成员变量的初始化顺序按照定义的顺序进行初始化。

#### **静态方法和实例方法有何不同？**

静态方法和实例方法的区别主要体现在两个方面：

- 在外部调用静态方法时，可以使用"类名.方法名"的方式，也可以使用"对象名.方法名"的方式。而实例方法只有后面这种方式。也就是说，调用静态方法可以无需创建对象。
- 静态方法在访问本类的成员时，只允许访问静态成员（即静态成员变量和静态方法），而不允许访问实例成员变量和实例方法；实例方法则无此限制

#### **在一个静态方法内调用一个非静态成员为什么是非法的？**

由于静态方法可以不通过对象进行调用，因此在静态方法里，不能调用其他非静态变量，也不可以访问非静态变量成员。

#### **什么是方法的返回值？返回值的作用是什么？**

方法的返回值是指我们获取到的某个方法体中的代码执行后产生的结果！（前提是该方法可能产生结果）。返回值的作用:接收出结果，使得它可以用于其他的操作！

### 内部类

#### **什么是内部类？**

在Java中，可以将一个类的定义放在另外一个类的定义内部，这就是**内部类**。内部类本身就是类的一个属性，与其他属性定义方式一致。

#### **内部类的分类有哪些**

内部类可以分为四种：**成员内部类、局部内部类、匿名内部类和静态内部类**。

##### **静态内部类**

定义在类内部的静态类，就是静态内部类。

```
public class Outer {

    private static int radius = 1;

    static class StaticInner {
        public void visit() {
            System.out.println("visit outer static variable:" + radius);
        }
    }
}
```



静态内部类可以访问外部类所有的静态变量，而不可访问外部类的非静态变量；静态内部类的创建方式，`new 外部类.静态内部类()`，如下：

```
Outer.StaticInner inner = new Outer.StaticInner();
inner.visit();
```



##### **成员内部类** 

定义在类内部，成员位置上的非静态类，就是成员内部类。

```
public class Outer {

    private static  int radius = 1;
    private int count =2;
    
     class Inner {
        public void visit() {
            System.out.println("visit outer static variable:" + radius);
            System.out.println("visit outer variable:" + count);
        }
    }
}
```



成员内部类可以访问外部类所有的变量和方法，包括静态和非静态，私有和公有。成员内部类依赖于外部类的实例，它的创建方式`外部类实例.new 内部类()`，如下：

```
Outer outer = new Outer();
Outer.Inner inner = outer.new Inner();
inner.visit();
```

##### **局部内部类** 

定义在方法中的内部类，就是局部内部类。

```
public class Outer {

    private  int out_a = 1;
    private static int STATIC_b = 2;

    public void testFunctionClass(){
        int inner_c =3;
        class Inner {
            private void fun(){
                System.out.println(out_a);
                System.out.println(STATIC_b);
                System.out.println(inner_c);
            }
        }
        Inner inner = new Inner();
        inner.fun();
    }
    public static void testStaticFunctionClass(){
        int d =3;
        class Inner {
            private void fun(){
                // System.out.println(out_a); 编译错误，定义在静态方法中的局部类不可以访问外部类的实例变量
                System.out.println(STATIC_b);
                System.out.println(d);
            }
        }
        Inner inner = new Inner();
        inner.fun();
    }
}
```



定义在实例方法中的局部类可以访问外部类的所有变量和方法，定义在静态方法中的局部类只能访问外部类的静态变量和方法。局部内部类的创建方式，在对应方法内，`new 内部类()`，如下：

```
public static void testStaticFunctionClass(){
    class Inner {
    }
    Inner inner = new Inner();
}
```



##### **匿名内部类** 

匿名内部类就是没有名字的内部类，日常开发中使用的比较多。

```
public class Outer {

    private void test(final int i) {
        new Service() {
            public void method() {
                for (int j = 0; j < i; j++) {
                    System.out.println("匿名内部类" );
                }
            }
        }.method();
    }
 }
 //匿名内部类必须继承或实现一个已有的接口
 interface Service{
    void method();
}
```



除了没有名字，匿名内部类还有以下特点：

- 匿名内部类必须继承一个抽象类或者实现一个接口。
- 匿名内部类不能定义任何静态成员和静态方法。
- 当所在的方法的形参需要被匿名内部类使用时，必须声明为 final。
- 匿名内部类不能是抽象的，它必须要实现继承的类或者实现的接口的所有抽象方法。

**匿名内部类创建方式：**

```
new 类/接口{
  //匿名内部类实现部分
}
```



#### 内部类的优点 

**我们为什么要使用内部类呢？因为它有以下优点：**

- 一个内部类对象可以访问创建它的外部类对象的内容，包括私有数据！
- 内部类不为同一包的其他类所见，具有很好的封装性；
- 内部类有效实现了“多重继承”，优化 java 单继承的缺陷。
- 匿名内部类可以很方便的定义回调。

#### **内部类有哪些应用场景**

- 一些多算法场合
- 解决一些非面向对象的语句块。
- 适当使用内部类，使得代码更加灵活和富有扩展性。
- 当某个类除了它的外部类，不再被其他的类使用时。

#### 局部内部类和匿名内部类访问局部变量的时候，为什么变量必须要加上final？

局部内部类和匿名内部类访问局部变量的时候，为什么变量必须要加上final呢？它内部原理是什么呢？

先看这段代码：

```
public class Outer {

    void outMethod(){
        final int a =10;
        class Inner {
            void innerMethod(){
                System.out.println(a);
            }

        }
    }
}
```



以上例子，为什么要加final呢？是因为**生命周期不一致**， 局部变量直接存储在栈中，当方法执行结束后，非final的局部变量就被销毁。而局部内部类对局部变量的引用依然存在，如果局部内部类要调用局部变量时，就会出错。加了final，可以确保局部内部类使用的变量与外层的局部变量区分开，解决了这个问题。

#### 内部类相关，看程序说出运行结果 

```
public class Outer {
    private int age = 12;

    class Inner {
        private int age = 13;
        public void print() {
            int age = 14;
            System.out.println("局部变量：" + age);
            System.out.println("内部类变量：" + this.age);
            System.out.println("外部类变量：" + Outer.this.age);
        }
    }

    public static void main(String[] args) {
        Outer.Inner in = new Outer().new Inner();
        in.print();
    }

}
```



运行结果：

```
局部变量：14
内部类变量：13
外部类变量：12
```



### **重写与重载** 

#### 构造器（constructor）是否可被重写（override）

构造器不能被继承，因此不能被重写，但可以被重载。

#### **重载（Overload）和重写（Override）的区别。重载的方法能否根据返回类型进行区分？**

方法的重载和重写都是实现多态的方式，区别在于前者实现的是编译时的多态性，而后者实现的是运行时的多态性。

重载：发生在同一个类中，方法名相同参数列表不同（参数类型不同、个数不同、顺序不同），与方法返回值和访问修饰符无关，即重载的方法不能根据返回类型进行区分

重写：发生在父子类中，方法名、参数列表必须相同，返回值小于等于父类，抛出的异常小于等于父类，访问修饰符大于等于父类（里氏代换原则）；如果父类方法访问修饰符为private则子类中就不是重写。

### 对象相等判断

#### **== 和 equals 的区别是什么**

**==** : 它的作用是判断两个对象的地址是不是相等。即，判断两个对象是不是同一个对象。(基本数据类型 == 比较的是值，引用数据类型 == 比较的是内存地址)

**equals()** : 它的作用也是判断两个对象是否相等。但它一般有两种使用情况：

情况1：类没有覆盖 equals() 方法。则通过 equals() 比较该类的两个对象时，等价于通过“==”比较这两个对象。

情况2：类覆盖了 equals() 方法。一般，我们都覆盖 equals() 方法来两个对象的内容相等；若它们的内容相等，则返回 true (即，认为这两个对象相等)。

**举个例子：
**

```
public class test1 {
    public static void main(String[] args) {
        String a = new String("ab"); // a 为一个引用
        String b = new String("ab"); // b为另一个引用,对象的内容一样
        String aa = "ab"; // 放在常量池中
        String bb = "ab"; // 从常量池中查找
        if (aa == bb) // true
            System.out.println("aa==bb");
        if (a == b) // false，非同一对象
            System.out.println("a==b");
        if (a.equals(b)) // true
            System.out.println("aEQb");
        if (42 == 42.0) { // true
            System.out.println("true");
        }
    }
}
```



**说明：**

- String中的equals方法是被重写过的，因为object的equals方法是比较的对象的内存地址，而String的equals方法比较的是对象的值。
- 当创建String类型的对象时，虚拟机会在常量池中查找有没有已经存在的值和要创建的值相同的对象，如果有就把它赋给当前引用。如果没有就在常量池中重新创建一个String对象。

#### hashCode 与 equals (重要)

HashSet如何检查重复

**两个对象的 hashCode() 相同，则 equals() 也一定为 true，对吗？**

hashCode和equals方法的关系

面试官可能会问你：“你重写过 hashcode 和 equals 么，为什么重写equals时必须重写hashCode方法？”

**hashCode()介绍**

hashCode() 的作用是获取哈希码，也称为散列码；它实际上是返回一个int整数。这个哈希码的作用是确定该对象在哈希表中的索引位置。hashCode() 定义在JDK的Object.java中，这就意味着Java中的任何类都包含有hashCode()函数。

散列表存储的是键值对(key-value)，它的特点是：能根据“键”快速的检索出对应的“值”。这其中就利用到了散列码！（可以快速找到所需要的对象）

**为什么要有 hashCode**

**我们以“HashSet 如何检查重复”为例子来说明为什么要有 hashCode**：

当你把对象加入 HashSet 时，HashSet 会先计算对象的 hashcode 值来判断对象加入的位置，同时也会与其他已经加入的对象的 hashcode 值作比较，如果没有相符的hashcode，HashSet会假设对象没有重复出现。但是如果发现有相同 hashcode 值的对象，这时会调用 equals()方法来检查 hashcode 相等的对象是否真的相同。如果两者相同，HashSet 就不会让其加入操作成功。如果不同的话，就会重新散列到其他位置。（摘自我的Java启蒙书《Head first java》第二版）。这样我们就大大减少了 equals 的次数，相应就大大提高了执行速度。

**hashCode()与equals()的相关规定**

如果两个对象相等，则hashcode一定也是相同的

两个对象相等，对两个对象分别调用equals方法都返回true

两个对象有相同的hashcode值，它们也不一定是相等的

**因此，equals 方法被覆盖过，则 hashCode 方法也必须被覆盖**

hashCode() 的默认行为是对堆上的对象产生独特值。如果没有重写 hashCode()，则该 class 的两个对象无论如何都不会相等（即使这两个对象指向相同的数据）

#### **对象的相等与指向他们的引用相等，两者有什么不同？**

对象的相等 比的是内存中存放的内容是否相等而 引用相等 比较的是他们指向的内存地址是否相等。

### **值传递**

#### 当一个对象被当作参数传递到一个方法后，此方法可改变这个对象的属性，并可返回变化后的结果，那么这里到底是值传递还是引用传递

是值传递。Java 语言的方法调用只支持参数的值传递。当一个对象实例作为一个参数被传递到方法中时，参数的值就是对该对象的引用。对象的属性可以在被调用过程中被改变，但对对象引用的改变是不会影响到调用者的

#### **为什么 Java 中只有值传递**

首先回顾一下在程序设计语言中有关将参数传递给方法（或函数）的一些专业术语。**按值调用(call by value)表示方法接收的是调用者提供的值，而按引用调用（call by reference)表示方法接收的是调用者提供的变量地址。一个方法可以修改传递引用所对应的变量值，而不能修改传递值调用所对应的变量值。** 它用来描述各种程序设计语言（不只是Java)中方法参数传递方式。

**Java程序设计语言总是采用按值调用。也就是说，方法得到的是所有参数值的一个拷贝，也就是说，方法不能修改传递给它的任何参数变量的内容。**

**下面通过 3 个例子来给大家说明**

example 1

```
public static void main(String[] args) {
    int num1 = 10;
    int num2 = 20;

    swap(num1, num2);

    System.out.println("num1 = " + num1);
    System.out.println("num2 = " + num2);
}

public static void swap(int a, int b) {
    int temp = a;
    a = b;
    b = temp;

    System.out.println("a = " + a);
    System.out.println("b = " + b);
}
```



**结果**：

```
a = 20
b = 10
num1 = 10
num2 = 20
```



在swap方法中，a、b的值进行交换，并不会影响到 num1、num2。因为，a、b中的值，只是从 num1、num2 的复制过来的。也就是说，a、b相当于num1、num2 的副本，副本的内容无论怎么修改，都不会影响到原件本身。

**通过上面例子，我们已经知道了一个方法不能修改一个基本数据类型的参数，而对象引用作为参数就不一样，请看 example2.**

example 2

```
public static void main(String[] args) {
        int[] arr = { 1, 2, 3, 4, 5 };
        System.out.println(arr[0]);
        change(arr);
        System.out.println(arr[0]);
    }

    public static void change(int[] array) {
        // 将数组的第一个元素变为0
        array[0] = 0;
    }
```



**结果**：

```
1
0
```



**解析**：

array 被初始化 arr 的拷贝也就是一个对象的引用，也就是说 array 和 arr 指向的时同一个数组对象。因此，外部对引用对象的改变会反映到所对应的对象上。

**通过 example2 我们已经看到，实现一个改变对象参数状态的方法并不是一件难事。理由很简单，方法得到的是对象引用的拷贝，对象引用及其他的拷贝同时引用同一个对象。**

**很多程序设计语言（特别是，C++和Pascal)提供了两种参数传递的方式：值调用和引用调用。有些程序员（甚至本书的作者）认为Java程序设计语言对对象采用的是引用调用，实际上，这种理解是不对的。由于这种误解具有一定的普遍性，所以下面给出一个反例来详细地阐述一下这个问题。**

example 3

```
public class Test {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Student s1 = new Student("小张");
        Student s2 = new Student("小李");
        Test.swap(s1, s2);
        System.out.println("s1:" + s1.getName());
        System.out.println("s2:" + s2.getName());
    }

    public static void swap(Student x, Student y) {
        Student temp = x;
        x = y;
        y = temp;
        System.out.println("x:" + x.getName());
        System.out.println("y:" + y.getName());
    }
}
```



**结果**：

```
x:小李
y:小张
s1:小张
s2:小李
```



**解析**：

交换之前：

![图片](https://mmbiz.qpic.cn/mmbiz_png/rAMaszgAyWpblibxHNficQAaicBURw5uxaYWryWicYmUV9SATxicgiaJSYgicqA7DrZSyLmvMEBGXIm3U2zeucgb8yaYQ/640?wx_fmt=png&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

通过上面两张图可以很清晰的看出： **方法并没有改变存储在变量 s1 和 s2 中的对象引用。swap方法的参数x和y被初始化为两个对象引用的拷贝，这个方法交换的是这两个拷贝**

**总结**

Java程序设计语言对对象采用的不是引用调用，实际上，对象引用是按值传递的。

下面再总结一下Java中方法参数的使用情况：

- 一个方法不能修改一个基本数据类型的参数（即数值型或布尔型》
- 一个方法可以改变一个对象参数的状态。
- 一个方法不能让对象参数引用一个新的对象。

#### **值传递和引用传递有什么区别**

值传递：指的是在方法调用时，传递的参数是按值的拷贝传递，传递的是值的拷贝，也就是说传递后就互不相关了。

引用传递：指的是在方法调用时，传递的参数是按引用进行传递，其实传递的引用的地址，也就是变量所对应的内存空间的地址。传递的是值的引用，也就是说传递前和传递后都指向同一个引用（也就是同一个内存空间）。

### Java包

#### **JDK 中常用的包有哪些**

- java.lang：这个是系统的基础类；
- java.io：这里面是所有输入输出有关的类，比如文件操作等；
- java.nio：为了完善 io 包中的功能，提高 io 包中性能而写的一个新包；
- java.net：这里面是与网络有关的类；
- java.util：这个是系统辅助类，特别是集合类；
- java.sql：这个是数据库操作的类。

#### **import java和javax有什么区别**

刚开始的时候 JavaAPI 所必需的包是 java 开头的包，javax 当时只是扩展 API 包来说使用。然而随着时间的推移，javax 逐渐的扩展成为 Java API 的组成部分。但是，将扩展从 javax 包移动到 java 包将是太麻烦了，最终会破坏一堆现有的代码。因此，最终决定 javax 包将成为标准API的一部分。

所以，实际上java和javax没有区别。这都是一个名字。

## IO流

### **java 中 IO 流分为几种?**

- 按照流的流向分，可以分为输入流和输出流；
- 按照操作单元划分，可以划分为字节流和字符流；
- 按照流的角色划分为节点流和处理流。

Java Io流共涉及40多个类，这些类看上去很杂乱，但实际上很有规则，而且彼此之间存在非常紧密的联系， Java I0流的40多个类都是从如下4个抽象类基类中派生出来的。

- InputStream/Reader: 所有的输入流的基类，前者是字节输入流，后者是字符输入流。
- OutputStream/Writer: 所有输出流的基类，前者是字节输出流，后者是字符输出流。

按操作方式分类结构图：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/rAMaszgAyWpblibxHNficQAaicBURw5uxaY0c1KTiaia1oCJn3CMSict7ZOCET2GwvxkMl8WnH9eCEobicoDkuEAOTsmw/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

按操作对象分类结构图：

![图片](https://mmbiz.qpic.cn/mmbiz_jpg/rAMaszgAyWpblibxHNficQAaicBURw5uxaYBiaj1tlmtZGABnibCqCDgMJGI4HkeibyFfiaowGcuL1jdQEL9mwza3bIag/640?wx_fmt=jpeg&tp=webp&wxfrom=5&wx_lazy=1&wx_co=1)

### **BIO,NIO,AIO 有什么区别?**

简答

- BIO：Block IO 同步阻塞式 IO，就是我们平常使用的传统 IO，它的特点是模式简单使用方便，并发处理能力低。
- NIO：Non IO 同步非阻塞 IO，是传统 IO 的升级，客户端和服务器端通过 Channel（通道）通讯，实现了多路复用。
- AIO：Asynchronous IO 是 NIO 的升级，也叫 NIO2，实现了异步非堵塞 IO ，异步 IO 的操作基于事件和回调机制。

详细回答

- **BIO (Blocking I/O):** 同步阻塞I/O模式，数据的读取写入必须阻塞在一个线程内等待其完成。在活动连接数不是特别高（小于单机1000）的情况下，这种模型是比较不错的，可以让每一个连接专注于自己的 I/O 并且编程模型简单，也不用过多考虑系统的过载、限流等问题。线程池本身就是一个天然的漏斗，可以缓冲一些系统处理不了的连接或请求。但是，当面对十万甚至百万级连接的时候，传统的 BIO 模型是无能为力的。因此，我们需要一种更高效的 I/O 处理模型来应对更高的并发量。
- **NIO (New I/O):** NIO是一种同步非阻塞的I/O模型，在Java 1.4 中引入了NIO框架，对应 java.nio 包，提供了 Channel , Selector，Buffer等抽象。NIO中的N可以理解为Non-blocking，不单纯是New。它支持面向缓冲的，基于通道的I/O操作方法。NIO提供了与传统BIO模型中的 `Socket` 和 `ServerSocket` 相对应的 `SocketChannel` 和 `ServerSocketChannel` 两种不同的套接字通道实现,两种通道都支持阻塞和非阻塞两种模式。阻塞模式使用就像传统中的支持一样，比较简单，但是性能和可靠性都不好；非阻塞模式正好与之相反。对于低负载、低并发的应用程序，可以使用同步阻塞I/O来提升开发速率和更好的维护性；对于高负载、高并发的（网络）应用，应使用 NIO 的非阻塞模式来开发
- **AIO (Asynchronous I/O):** AIO 也就是 NIO 2。在 Java 7 中引入了 NIO 的改进版 NIO 2,它是异步非阻塞的IO模型。异步 IO 是基于事件和回调机制实现的，也就是应用操作之后会直接返回，不会堵塞在那里，当后台处理完成，操作系统会通知相应的线程进行后续的操作。AIO 是异步IO的缩写，虽然 NIO 在网络操作中，提供了非阻塞的方法，但是 NIO 的 IO 行为还是同步的。对于 NIO 来说，我们的业务线程是在 IO 操作准备好时，得到通知，接着就由这个线程自行进行 IO 操作，IO操作本身是同步的。查阅网上相关资料，我发现就目前来说 AIO 的应用还不是很广泛，Netty 之前也尝试使用过 AIO，不过又放弃了。

### **Files的常用方法都有哪些？**

- Files. exists()：检测文件路径是否存在。
- Files. createFile()：创建文件。
- Files. createDirectory()：创建文件夹。
- Files. delete()：删除一个文件或目录。
- Files. copy()：复制文件。
- Files. move()：移动文件。
- Files. size()：查看文件个数。
- Files. read()：读取文件。
- Files. write()：写入文件。

## 反射

### **什么是反射机制？**

JAVA反射机制是在运行状态中，对于任意一个类，都能够知道这个类的所有属性和方法；对于任意一个对象，都能够调用它的任意一个方法和属性；这种动态获取的信息以及动态调用对象的方法的功能称为java语言的反射机制。

静态编译和动态编译

- **静态编译：**在编译时确定类型，绑定对象
- **动态编译：**运行时确定类型，绑定对象

### 反射机制优缺点

- **优点：** 运行期类型的判断，动态加载类，提高代码灵活度。
- **缺点：** 性能瓶颈：反射相当于一系列解释操作，通知 JVM 要做的事情，性能比直接的java代码要慢很多。

### **反射机制的应用场景有哪些？**

反射是框架设计的灵魂。

在我们平时的项目开发过程中，基本上很少会直接使用到反射机制，但这不能说明反射机制没有用，实际上有很多设计、开发都与反射机制有关，例如模块化的开发，通过反射去调用对应的字节码；动态代理设计模式也采用了反射机制，还有我们日常使用的 Spring／Hibernate 等框架也大量使用到了反射机制。

举例：①我们在使用JDBC连接数据库时使用Class.forName()通过反射加载数据库的驱动程序；②Spring框架也用到很多反射机制，最经典的就是xml的配置模式。Spring 通过 XML 配置模式装载 Bean 的过程：1) 将程序内所有 XML 或 Properties 配置文件加载入内存中; 2)Java类里面解析xml或properties里面的内容，得到对应实体类的字节码字符串以及相关的属性信息; 3)使用反射机制，根据这个字符串获得某个类的Class实例; 4)动态配置实例的属性

### Java获取反射的三种方法

1.通过new对象实现反射机制 2.通过路径实现反射机制 3.通过类名实现反射机制

```
public class Student {
    private int id;
    String name;
    protected boolean sex;
    public float score;
}
public class Get {
    //获取反射机制三种方式
    public static void main(String[] args) throws ClassNotFoundException {
        //方式一(通过建立对象)
        Student stu = new Student();
        Class classobj1 = stu.getClass();
        System.out.println(classobj1.getName());
        //方式二（所在通过路径-相对路径）
        Class classobj2 = Class.forName("fanshe.Student");
        System.out.println(classobj2.getName());
        //方式三（通过类名）
        Class classobj3 = Student.class;
        System.out.println(classobj3.getName());
    }
}
```

## 网络编程

网络编程的面试题可以查看我的这篇文章重学TCP/IP协议和三次握手四次挥手，内容不仅包括TCP/IP协议和三次握手四次挥手的知识，还包括计算机网络体系结构，HTTP协议，get请求和post请求区别，session和cookie的区别等，欢迎大家阅读。

## 常用API

### String相关

#### **字符型常量和字符串常量的区别**

- 形式上: 字符常量是单引号引起的一个字符 字符串常量是双引号引起的若干个字符
- 含义上: 字符常量相当于一个整形值(ASCII值),可以参加表达式运算 字符串常量代表一个地址值(该字符串在内存中存放位置)
- 占内存大小 字符常量只占一个字节 字符串常量占若干个字节(至少一个字符结束标志)

#### **什么是字符串常量池？**

字符串常量池位于堆内存中，专门用来存储字符串常量，可以提高内存的使用率，避免开辟多块空间存储相同的字符串，在创建字符串时 JVM 会首先检查字符串常量池，如果该字符串已经存在池中，则返回它的引用，如果不存在，则实例化一个字符串放到池中，并返回其引用。

#### **String 是最基本的数据类型吗**

不是。Java 中的基本数据类型只有 8 个 ：byte、short、int、long、float、double、char、boolean；除了基本类型（primitive type），剩下的都是引用类型（referencetype），Java 5 以后引入的枚举类型也算是一种比较特殊的引用类型。

这是很基础的东西，但是很多初学者却容易忽视，Java 的 8 种基本数据类型中不包括 String，基本数据类型中用来描述文本数据的是 char，但是它只能表示单个字符，比如 ‘a’,‘好’ 之类的，如果要描述一段文本，就需要用多个 char 类型的变量，也就是一个 char 类型数组，比如“你好” 就是长度为2的数组 char[] chars = {‘你’,‘好’};

但是使用数组过于麻烦，所以就有了 String，String 底层就是一个 char 类型的数组，只是使用的时候开发者不需要直接操作底层数组，用更加简便的方式即可完成对字符串的使用。

#### **String有哪些特性**

- 不变性：String 是只读字符串，是一个典型的 immutable 对象，对它进行任何操作，其实都是创建一个新的对象，再把引用指向该对象。不变模式的主要作用在于当一个对象需要被多线程共享并频繁访问时，可以保证数据的一致性。
- 常量池优化：String 对象创建之后，会在字符串常量池中进行缓存，如果下次创建同样的对象时，会直接返回缓存的引用。
- final：使用 final 来定义 String 类，表示 String 类不能被继承，提高了系统的安全性。

#### **String为什么是不可变的吗？**

简单来说就是String类利用了final修饰的char类型数组存储字符，源码如下图所以：

```
/** The value is used for character storage. */
private final char value[];
```



**String真的是不可变的吗？**

我觉得如果别人问这个问题的话，回答不可变就可以了。下面只是给大家看两个有代表性的例子：

**1) String不可变但不代表引用不可以变**

```
String str = "Hello";
str = str + " World";
System.out.println("str=" + str);
结果：
str=Hello World
```



解析：

实际上，原来String的内容是不变的，只是str由原来指向"Hello"的内存地址转为指向"Hello World"的内存地址而已，也就是说多开辟了一块内存区域给"Hello World"字符串。

**2) 通过反射是可以修改所谓的“不可变”对象
**

```
// 创建字符串"Hello World"， 并赋给引用s
String s = "Hello World";

System.out.println("s = " + s); // Hello World

// 获取String类中的value字段
Field valueFieldOfString = String.class.getDeclaredField("value");

// 改变value属性的访问权限
valueFieldOfString.setAccessible(true);

// 获取s对象上的value属性的值
char[] value = (char[]) valueFieldOfString.get(s);

// 改变value所引用的数组中的第5个字符
value[5] = '_';

System.out.println("s = " + s); // Hello_World
结果：

s = Hello World
s = Hello_World
```



解析：

用反射可以访问私有成员， 然后反射出String对象中的value属性， 进而改变通过获得的value引用改变数组的结构。但是一般我们不会这么做，这里只是简单提一下有这个东西。

#### **是否可以继承 String 类**

String 类是 final 类，不可以被继承。

#### **String str="i"与 String str=new String(“i”)一样吗？**

不一样，因为内存的分配方式不一样。String str="i"的方式，java 虚拟机会将其分配到常量池中；而 String str=new String(“i”) 则会被分到堆内存中。

#### **String s = new String(“xyz”);创建了几个字符串对象**

两个对象，一个是静态区的"xyz"，一个是用new创建在堆上的对象。

```
String str1 = "hello"; //str1指向静态区
String str2 = new String("hello"); //str2指向堆上的对象
String str3 = "hello";
String str4 = new String("hello");
System.out.println(str1.equals(str2)); //true
System.out.println(str2.equals(str4)); //true
System.out.println(str1 == str3); //true
System.out.println(str1 == str2); //false
System.out.println(str2 == str4); //false
System.out.println(str2 == "hello"); //false
str2 = str1;
System.out.println(str2 == "hello"); //true
```



**如何将字符串反转？**

使用 StringBuilder 或者 stringBuffer 的 reverse() 方法。

示例代码：

```
// StringBuffer reverse
StringBuffer stringBuffer = new StringBuffer();
stringBuffer. append("abcdefg");
System. out. println(stringBuffer. reverse()); // gfedcba
// StringBuilder reverse
StringBuilder stringBuilder = new StringBuilder();
stringBuilder. append("abcdefg");
System. out. println(stringBuilder. reverse()); // gfedcba
```



**数组有没有 length()方法？String 有没有 length()方法**

数组没有 length()方法 ，有 length 的属性。String 有 length()方法。JavaScript中，获得字符串的长度是通过 length 属性得到的，这一点容易和 Java 混淆。

#### **String 类的常用方法都有那些？**

- indexOf()：返回指定字符的索引。
- charAt()：返回指定索引处的字符。
- replace()：字符串替换。
- trim()：去除字符串两端空白。
- split()：分割字符串，返回一个分割后的字符串数组。
- getBytes()：返回字符串的 byte 类型数组。
- length()：返回字符串长度。
- toLowerCase()：将字符串转成小写字母。
- toUpperCase()：将字符串转成大写字符。
- substring()：截取字符串。
- equals()：字符串比较。

#### **在使用 HashMap 的时候，用 String 做 key 有什么好处？**

HashMap 内部实现是通过 key 的 hashcode 来确定 value 的存储位置，因为字符串是不可变的，所以当创建字符串时，它的 hashcode 被缓存下来，不需要再次计算，所以相比于其他对象更快。

#### String和StringBuffer、StringBuilder的区别是什么？String为什么是不可变的

**可变性**

String类中使用字符数组保存字符串，private　final　char　value[]，所以string对象是不可变的。StringBuilder与StringBuffer都继承自AbstractStringBuilder类，在AbstractStringBuilder中也是使用字符数组保存字符串，char[] value，这两种对象都是可变的。

**线程安全性**

String中的对象是不可变的，也就可以理解为常量，线程安全。AbstractStringBuilder是StringBuilder与StringBuffer的公共父类，定义了一些字符串的基本操作，如expandCapacity、append、insert、indexOf等公共方法。StringBuffer对方法加了同步锁或者对调用的方法加了同步锁，所以是线程安全的。StringBuilder并没有对方法进行加同步锁，所以是非线程安全的。

**性能**

每次对String 类型进行改变的时候，都会生成一个新的String对象，然后将指针指向新的String 对象。StringBuffer每次都会对StringBuffer对象本身进行操作，而不是生成新的对象并改变对象引用。相同情况下使用StirngBuilder 相比使用StringBuffer 仅能获得10%~15% 左右的性能提升，但却要冒多线程不安全的风险。

**对于三者使用的总结**

如果要操作少量的数据用 = String

单线程操作字符串缓冲区 下操作大量数据 = StringBuilder

多线程操作字符串缓冲区 下操作大量数据 = StringBuffer

### Date相关

### 包装类相关

#### **自动装箱与拆箱**

**装箱**：将基本类型用它们对应的引用类型包装起来；

**拆箱**：将包装类型转换为基本数据类型；

#### **int 和 Integer 有什么区别**

Java 是一个近乎纯洁的面向对象编程语言，但是为了编程的方便还是引入了基本数据类型，但是为了能够将这些基本数据类型当成对象操作，Java 为每一个基本数据类型都引入了对应的包装类型（wrapper class），int 的包装类就是 Integer，从 Java 5 开始引入了自动装箱/拆箱机制，使得二者可以相互转换。

**Java 为每个原始类型提供了包装类型：**

原始类型: boolean，char，byte，short，int，long，float，double

包装类型：Boolean，Character，Byte，Short，Integer，Long，Float，Double

#### **Integer a= 127 与 Integer b = 127相等吗**

对于对象引用类型：==比较的是对象的内存地址。
对于基本数据类型：==比较的是值。

如果整型字面量的值在-128到127之间，那么自动装箱时不会new新的Integer对象，而是直接引用常量池中的Integer对象，超过范围 a1==b1的结果是false

```
public static void main(String[] args) {
    Integer a = new Integer(3);
    Integer b = 3; // 将3自动装箱成Integer类型
    int c = 3;
    System.out.println(a == b); // false 两个引用没有引用同一对象
    System.out.println(a == c); // true a自动拆箱成int类型再和c比较
    System.out.println(b == c); // true

    Integer a1 = 128;
    Integer b1 = 128;
    System.out.println(a1 == b1); // false

    Integer a2 = 127;
    Integer b2 = 127;
    System.out.println(a2 == b2); // true
}
```
