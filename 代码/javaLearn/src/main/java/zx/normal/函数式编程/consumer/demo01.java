package zx.normal.函数式编程.consumer;

import java.util.function.Consumer;

/**
 * @Description: zx.normal.函数式编程.consumer
 * @version: 1.0
 */
public class demo01 {
    public static void main(String[] args) {
        /**測試打印*/
        Consumer c = System.out::println;
        c.accept("hello");
        c.accept("world");
    }
}
