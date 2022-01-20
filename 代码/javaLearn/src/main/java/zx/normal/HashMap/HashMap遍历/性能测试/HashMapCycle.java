package zx.normal.HashMap.HashMap遍历.性能测试;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description: zx.normal.HashMap.HashMap遍历.性能测试
 * @version: 1.0
 */
@BenchmarkMode(Mode.Throughput)// 测试类型：吞吐量
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 2, time = 1, timeUnit = TimeUnit.SECONDS)// 预热 2 轮,每次 1 秒
@Measurement(iterations = 5, time = 3, timeUnit = TimeUnit.SECONDS)//测试 5 轮,每次 3 秒
@Fork(1)// fork 1 个线程
@State(Scope.Thread)// 每个测试线程一个实例
public class HashMapCycle {
    static Map<Integer,String> map = new HashMap(){{
        //添加数据
        for (int i = 0; i < 10; i++) {
            put(i,"val"+i);
        }
    }};

    public static void main(String[] args) throws RunnerException {
//        System.out.println(map.toString());
        // 启动基准测试
        Options opt = new OptionsBuilder()
                .include(HashMapCycle.class.getSimpleName())// 要导入的测试类
                .output("F:\\jmh-map.log")                  // 输出测试结果的文件
                .build();
        new Runner(opt).run();
    }

    @Benchmark
    public void entrySet(){
        Iterator<Map.Entry<Integer, String>> iterator = map.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Integer, String> entry = iterator.next();
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
    }

    @Benchmark
    public void keySet(){
        Iterator<Integer> iterator = map.keySet().iterator();
        while (iterator.hasNext()){
            Integer key = iterator.next();
            System.out.println(key);
            System.out.println(map.get(key));
        }
    }

    @Benchmark
    public void forEachEntrySet(){
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        }
    }

    @Benchmark
    public void forEachKeySet(){
        for (Integer key : map.keySet()) {
            System.out.println(key);
            System.out.println(map.get(key));
        }
    }

    @Benchmark
    public void lambda(){
        map.forEach((key,value) -> {
            System.out.println(key);
            System.out.println(value);
        });
    }

    @Benchmark
    public void streamApi(){
        map.entrySet().stream().forEach(entry -> {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        });
    }

    @Benchmark
    public void parallelStreamApi(){
        map.entrySet().parallelStream().forEach(entry -> {
            System.out.println(entry.getKey());
            System.out.println(entry.getValue());
        });
    }
}
