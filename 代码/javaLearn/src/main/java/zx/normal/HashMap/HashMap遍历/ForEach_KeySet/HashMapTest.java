package zx.normal.HashMap.HashMap遍历.ForEach_KeySet;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: zx.normal.HashMapTest.HashMap遍历.ForEach_KeySet
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
        for (Integer key : map.keySet()) {
            if (key == 1){
                // 删除
                System.out.println("del:" + key);
                //ForEach中移除元素会触发并发修改异常
                map.remove(key);
            }else{
                System.out.print(key);
                System.out.print(map.get(key));
            }
        }
    }
}
