package zx.mix.map踩坑.demo01;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @Description: zx.mix.map踩坑.demo01
 * @version: 1.0
 */
public class demo01 {
    public static void main(String[] args) {
        /**
         * 返回 key 的 set 视图
         *      Set<K> keySet()；
         *
         * 返回所有 value   Collection 视图
         *      Collection<V> values();
         *
         * 返回 key-value 的 set 视图
         *      Set<Map.Entry<K, V>> entrySet();
         *
         * 注意：
         *      这三个方法创建返回新集合，底层其实都依赖的原有 Map 中数据，
         *      所以一旦 Map 中元素变动，就会同步影响返回的集合。
         *
         *      另外这三个方法返回新集合，是不支持的新增以及修改操作的，
         *      但是却支持 clear、remove 等操作。
         */

        Consumer c = System.out::println;
        Map<String,Integer> map = new HashMap<>();
        map.put("1",1);
        map.put("2",2);
        map.put("3",3);

        Collection<Integer> values = map.values();
        // 原集合新增元素，collection 也会被同步影响
//        map.put("4",4);
        // collection 支持 clear。并且会同步清空原Map中的所有元素
        values.clear();

        c.accept(map.toString());
        c.accept(values.toString());

    }
}
