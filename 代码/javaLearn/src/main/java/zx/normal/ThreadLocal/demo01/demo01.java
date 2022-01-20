package zx.normal.ThreadLocal.demo01;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: zx.normal.ThreadLocal.demo01
 * @version: 1.0
 */
public class demo01 {
    public static void main(String[] args) {
        ThreadLocal<Map> threadLocal = new ThreadLocal<>();
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("1","test1");
        hashMap.put("2","test2");
        threadLocal.set(hashMap);
        System.out.println(threadLocal.get().get("1"));
        System.out.println(threadLocal.get().get("2"));
        threadLocal.remove();

        ThreadLocal threadLocal1 = new ThreadLocal();
        //如果第一次给线程赋值，此处类似
        // Map map = new HashMap(); map.put(threadLocal1,"变量第一次赋值")
        threadLocal1.set("变量第一次赋值");//类似map.put(threadLocal1,"变量第一次赋值")
        threadLocal1.set("变量第二次赋值");//类似map.put(threadLocal1,"变量第一次赋值")
        System.out.println(threadLocal1.get());//类似map.get(threadLocal1)
        threadLocal1.remove();

        ThreadLocal threadLocal2 = new ThreadLocal<String>(){
            @Override
            protected String initialValue(){
                return "init value";
            }
        };
        System.out.println(threadLocal2.get());
        threadLocal2.set("hello");
        System.out.println(threadLocal2.get());
        threadLocal2.remove();
    }
}
