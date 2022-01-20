package zx.mix.tryCatch.tryCatch性能测试;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @Description: zx.mix.tryCatch.tryCatch性能测试
 * @version: 1.0
 */
@BenchmarkMode(Mode.AverageTime)// 测试完成时间
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@Warmup(iterations = 1,time = 1,timeUnit = TimeUnit.SECONDS)// 预热1轮，每次1秒
@Measurement(iterations = 5,time = 5,timeUnit = TimeUnit.SECONDS)// 测试5轮，每次3秒
@Fork(1)
@State(Scope.Benchmark)
@Threads(100)
public class TryCatchPerformanceTest {
    private static final int forSize = 1000; // 循环次数

    public static void main(String[] args) throws RunnerException {
        // 启动基准测试
        Options opt = new OptionsBuilder()
                .include(TryCatchPerformanceTest.class.getSimpleName())
                .build();
                new Runner(opt).run();
    }

    @Benchmark
    public int innerForeach(){
        int count = 0;
        for (int i = 0; i < forSize; i++) {
            try {
                if (i == forSize){
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
    public int outterForeach(){
        int count = 0;
        try {
            for (int i = 0; i < forSize; i++) {
                if (i == forSize){
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
