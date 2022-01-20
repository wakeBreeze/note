package zx.normal.多线程并发.线程创建;

import java.util.concurrent.Callable;

/**
 * @Description: zx.normal.多线程并发.线程创建
 * @version: 1.0
 */
public class Mythread3 implements Callable<Integer> {
    @Override
    public Integer call() throws Exception {
        int sum = 0;
        for (int i = 0; i < 30; i++) {
            System.out.println("thread#3===" + i);
            sum += i;
        }
        return sum;
    }
}
