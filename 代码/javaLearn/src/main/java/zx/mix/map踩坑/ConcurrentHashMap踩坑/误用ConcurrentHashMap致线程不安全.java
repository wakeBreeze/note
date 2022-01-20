package zx.mix.map踩坑.ConcurrentHashMap踩坑;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description: zx.mix.map踩坑.ConcurrentHashMap踩坑
 * @version: 1.0
 */
public class 误用ConcurrentHashMap致线程不安全 {
    public static void main(String[] args) throws InterruptedException {
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
         *      1、一二步代码加锁
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
    }
}
