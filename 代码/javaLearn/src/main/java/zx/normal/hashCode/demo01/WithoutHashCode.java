package zx.normal.hashCode.demo01;

import java.util.HashMap;

/**
 * @Description: zx.normal.hashCode.ConcurrentHashMap键值不能为null
 * @version: 1.0
 */
public class WithoutHashCode {
    public static void main(String[] args) {
        Key key1 = new Key(1,2);
        Key key2 = new Key(1,2);
        HashMap<Key, String> hm = new HashMap<>();
        hm.put(key1,"Key with id is 1");
        System.out.println(hm.get(key2));
        System.out.println("自定义hashCode:"+key1.hashCode());//自定义hashCode
        System.out.println("原来的hashCode:"+System.identityHashCode(key1));//原来的hashCode
    }
}
class Key {
    private Integer id;
    private Integer age;

    public Key(Integer id) {
        this.id = id;
    }

    public Key(Integer id, Integer age) {
        this.id = id;
        this.age = age;
    }

    public Integer getId() {
        return id;
    }

    public Integer getAge() {
        return age;
    }

    /**
     * 没重写hashCode方法时
     * 默认调用的是Object.hashCode()方法，返回的是对象的内存地址
     * 所以不同的对象由于内存地址不同，所以hashCode也不一样
     *
     *重写之后相同id值的对象hashCode也相同
     * @return
     */
    public int hashCode(){
        return id.hashCode();
    }

    /**
     * 没重写equals方法时
     * 默认调用的是Object.equals()方法，根据两个对象的内存地址来判断是否相等
     * 所以不同的对象由于内存地址不同，所以equals返回false
     *
     * 重写之后只要对象都是Key类型并且id值都相等就返回true
     * @param o
     * @return
     */
    public boolean equals(Object o){
        if (o == null || !(o instanceof Key)){
            return false;
        }else {
            return this.getAge().hashCode()==(((Key)o).getAge().hashCode());
        }
    }
}
