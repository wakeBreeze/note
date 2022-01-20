package zx.normal.多线程并发.线程创建;

import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description: zx.normal.多线程并发.线程创建
 * @version: 1.0
 */
public class Test {
    public static void main(String[] args) throws InterruptedException {
        long start = System.currentTimeMillis();
        Mythread1 thread1 = new Mythread1();
        thread1.start();

        Thread thread2 = new Thread(new Mythread2());
        thread2.start();

        Mythread3 mythread3 = new Mythread3();
        FutureTask<Integer> futureTask = new FutureTask<>(mythread3);
        new Thread(futureTask).start();

        Thread.sleep(1000L);
        System.out.println("主线程结束！用时：" + (System.currentTimeMillis() - start));

    }
}
