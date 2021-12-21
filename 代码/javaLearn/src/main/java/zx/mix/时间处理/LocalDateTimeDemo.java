package zx.mix.时间处理;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

/**
 * @Description: zx.mix.时间处理
 * @version: 1.0
 */
public class LocalDateTimeDemo {
    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.now();    // 当前时间
        Consumer consumer = System.out::println;
        consumer.accept("时间："+now);
        consumer.accept("年："+now.getYear());
        consumer.accept("月："+now.getMonth());
        consumer.accept("月："+now.getMonthValue());
        consumer.accept("日："+now.getDayOfMonth());
        consumer.accept("时："+now.getHour());
        consumer.accept("分："+now.getMinute());
        consumer.accept("秒："+now.getSecond());

        // 构造指定年月日的时间
        LocalDateTime time = LocalDateTime.of(2022, Month.JANUARY, 01, 23, 59, 59);
        consumer.accept("构造时间："+time);

        /** 修改日期
         * 主要方法：
         * minusYears() 减去指定年数
         * plusYears()  增加指定年数
         * withYear()   直接修改为指定年份
         * 其它类推
         */
        LocalDateTime localTime = LocalDateTime.now();
        System.out.println("当前时间:"+localTime);
        System.out.println("两年前:"+localTime.minusYears(2));
        System.out.println("两年后:"+localTime.plusYears(2));
        System.out.println("2080年的今天:"+localTime.withYear(2080));  //直接修改到指定年份
        System.out.println("两月前:"+localTime.minusMonths(2));
        System.out.println("两天前:"+localTime.minusDays(2));
        System.out.println("两小时前:"+localTime.minusHours(2));
        System.out.println("两分钟前:"+localTime.minusMinutes(2));
        System.out.println("两秒钟前:"+localTime.minusSeconds(2));

        /** 格式化日期
         *
         */
        consumer.accept("==========格式化日期==========");
        LocalDateTime rightNow = LocalDateTime.now();
        System.out.println(rightNow.format(DateTimeFormatter.ISO_DATE));
        System.out.println(rightNow.format(DateTimeFormatter.BASIC_ISO_DATE));
        System.out.println(rightNow.format(DateTimeFormatter.ISO_DATE_TIME));
        System.out.println(rightNow.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")));

        /** 时间反解析
         *
         */
        consumer.accept("==========时间反解析==========");
        LocalDateTime parseTime = LocalDateTime.parse("2021--10--1 11:21",DateTimeFormatter.ofPattern("yyyy--MM--d HH:mm"));
        consumer.accept("反解析时间："+parseTime);
    }
}
