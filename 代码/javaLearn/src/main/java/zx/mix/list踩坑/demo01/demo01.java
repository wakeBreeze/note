package zx.mix.list踩坑.demo01;

import com.google.common.collect.ImmutableList;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @Description: zx.mix.list踩坑.ConcurrentHashMap键值不能为null
 * @version: 1.0
 */

public class demo01 {
    public static void main(String[] args) {

        /**Arrays.asList()方法踩坑*/

        Consumer c = System.out::println;
        c.accept("=================Arrays.asList()踩坑=================");
        String[] arrays = {"1","2","3"};
        List<String> list = Arrays.asList(arrays);
        c.accept("arrays:"+Arrays.toString(arrays));
        c.accept("list:"+list.toString());
        /**
         * Arrays.asList()方法
         * 复制数组生成的list使用了原始数组
         * 改动的是同一个数组
         */
        list.set(0,"100");
        arrays[1] = "200";
        c.accept("arrays:"+Arrays.toString(arrays));
        c.accept("list:"+list.toString());
        /** 会抛UnsupportedOperationException
         * 因为Arrays.asList()生成的是java.util.Arrays$ArrayList类型
         * 没有重写其父类的方法，没有增删方法
         */
//        list.add("400");

        /** 解决
         * 套一层java.util.ArrayList
         */
        List<String> arrayList = new ArrayList<>(Arrays.asList(arrays));

        c.accept("=================List#subList()踩坑=================");
        List<Integer> il = new ArrayList<>();
        il.add(1);
        il.add(2);
        il.add(3);
        List<Integer> subList = il.subList(0, 2);
        c.accept("---------改动前---------");
        c.accept("al:"+il.toString());
        c.accept("subList:"+subList.toString());
        /**
         * List#subList()方法
         * 复制数组生成的list使用了原始数组
         * 改动的是同一个数组
         */
        c.accept("--------改动后--------");
        il.set(0,10);
        subList.set(1,20);
        c.accept("al:"+il.toString());
        c.accept("subList:"+subList.toString());

        c.accept("--------测试OOM--------");
        /**
         * data 看起来最终保存的只是 1000 个具有 1 个元素的 List，不会占用很大空间。但是程序很快就会 OOM。
         * OOM 的原因正是因为每个 SubList 都强引用个一个 100 万个元素的原始 List，导致 GC 无法回收。
         * 这里修复的办法也很简单，跟上面一样，也来个套娃呗，加一层 ArrayList 。
         */
        List<List<Integer>> data = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
//            List<Integer> rawList = IntStream.rangeClosed(1, 1000000).boxed().collect(Collectors.toList());
//            data.add(rawList.subList(0,1));
        }

        c.accept("=================Collections.unmodifiableList踩坑=================");
        /** Collections.unmodifiableList 会生成一个不可变集合
         * 生成的集合调用增加，删除，修改会抛出UnsupportedOperationException
         * 但是修改原集合，生成的不可变集合也会被修改
         * 说明不可变集合使用了原list
         *
         * 修复：也可以在生成不可变集合的时候套一层ArrayList
         * 即: List<String> unmodifiableList = Collections.unmodifiableList(new ArrayList<>(sl));
         */
        List<String> sl = new ArrayList<>(Arrays.asList("one", "two", "three"));
        List<String> unmodifiableList = Collections.unmodifiableList(sl);
        // 以下三个修改方法会抛出UnsupportedOperationException
//        unmodifiableList.add("four");
//        unmodifiableList.remove(0);
//        unmodifiableList.set(0,"test");

        c.accept("--------测试修改原集合是否会影响不可变集合--------");
        sl.set(0,"test");
        sl.add("four");
        c.accept("sl:"+sl.toString());
        c.accept("unmodifiableList:"+unmodifiableList.toString());

        //解决
        // 1、使用 JDK9 List#of 方法。
//        List<String> list1 = new ArrayList<>(Arrays.asList("one", "two", "three"));
//        List<String> unmodifiableList1 = List.of(list1.toArray(new String[]{}));

        // 2、使用 Guava immutable list
        List<String> list2 = new ArrayList<>(Arrays.asList("one", "two", "three"));
        ImmutableList<String> unmodifiableList2 = ImmutableList.copyOf(list2);
        list2.set(0,"test");
        c.accept("list2:"+list2.toString());
        c.accept("unmodifiableList2:"+unmodifiableList2.toString());

    }
}
