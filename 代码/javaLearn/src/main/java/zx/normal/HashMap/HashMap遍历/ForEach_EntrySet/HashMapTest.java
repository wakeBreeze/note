package zx.normal.HashMap.HashMap遍历.ForEach_EntrySet;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: zx.normal.HashMapTest.HashMap遍历.ForEach_EntrySet
 * @version: 1.0
 */
public class HashMapTest {
    public static void main(String[] args) {
        // 创建并赋值 HashMapTest
        Map<Integer, String> map = new HashMap();
        map.put(1, "Java");
        map.put(2, "JDK");
        map.put(3, "Spring Framework");
        map.put(4, "MyBatis framework");
        map.put(5, "Java中文社群");
        // 遍历
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            if (entry.getKey() == 1){
                // 删除
                System.out.println("del:" + entry.getKey());
                //for循环中移除元素会触发并发修改异常
                map.remove(entry.getKey());
            }else{
                System.out.print(entry.getKey());
                System.out.print(entry.getValue());
            }
        }
    }
}
