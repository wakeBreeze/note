package zx.normal.函数式编程.function;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @Description: zx.normal.函数式编程.function
 * @version: 1.0
 */
public class demo01 {
    public static void main(String[] args) {
        /**Function*/
        Consumer c = System.out::println;// 打印功能
        c.accept("===========Function===========");
        Function<Integer,Integer> f1 = i -> i + i;// 乘以2功能
        Function<Integer,Integer> f2 = i -> i * i;// 平方功能
        c.accept(f1.andThen(f2).apply(3));
    }
}
