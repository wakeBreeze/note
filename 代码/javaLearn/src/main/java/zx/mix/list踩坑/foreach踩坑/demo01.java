package zx.mix.list踩坑.foreach踩坑;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @Description: zx.mix.list踩坑.foreach踩坑
 * @version: 1.0
 */
public class demo01 {
    public static void main(String[] args) {
        String[] arrays = {"1","2","3"};
        List<String> list = new ArrayList<>(Arrays.asList(arrays));
//        list.remove("1");
        /**
         * 这段代码看似没问题,实则会产生ConcurrentModificationException
         * foreach方法其实是迭代器的语法糖。
         * remove()方法其实已经在内部进行过遍历，并且会修改迭代器的计数器（modCount）
         * 所以外层foreach遍历时取得的计数器数和预期不符，则会抛出并发修改异常！
         * 疑问：移除list倒数第二个数时不会抛出并发修改异常，这是为啥？
         *
         * 因此在使用foreach的时候不应该使用list#remove方法
         * 解决：
         * 1、使用CopyOnWriteArrayList。
         *      CopyOnWriteArrayList 并不会使用 modCount 计数。
         *      因此不会受到remove()方法的影响。
         * 2、使用Iterator#remove删除元素:
         *      Iterator<String> iterator = list.iterator();
         *         while (iterator.hasNext()){
         *             String next = iterator.next();
         *             if ("1".equals(next)){
         *                 iterator.remove();
         *             }
         *         }
         *
         * 3、使用JDK1.8 List#removeIf
         *      list.removeIf(str -> "1".equals(str));
         */
        for (String str : list) {
            if ("1".equals(str)){
                list.remove(str);
            }
            /**以下代码会在foreach迭代器第二次获取计数器之前退出遍历
             * 因此不会触发并发修改异常。
             */
            /*if (!list.contains("1")){
                break;
            }*/
        }
        list.removeIf(str -> "1".equals(str));
    }
}
