package zx.mix.map踩坑.HashMap踩坑;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Objects;

/**
 * @Description: zx.mix.map踩坑.HashMap踩坑
 * @version: 1.0
 */
public class 自定义对象作为Map的key {
    public static void main(String[] args) {
        HashMap<Goods, Integer> goodsMap = new HashMap<>();
        goodsMap.put(new Goods(1, "豆腐", 666),1);
        //重新创建一个相同的商品
        goodsMap.put(new Goods(1,"豆腐",666),2);
        Goods goods = new Goods(1, "豆腐", 666);

        /**
         * 上面代码中，第二次我们加入一个相同的商品，
         * 原本我们期望新加入的值将会替换原来旧值。
         * 但是实际上这里并没有替换成功，反而又加入一对键值。
         *
         * 解决：重写hashCode和equals方法
         *
         */
        System.out.println(goodsMap.get(goods));

        /**重写hashCode和equals方法后
         * 如果修改了原来的对象（重写后hashCode值也会产生更改）,则会丢失链接
         * 原因：get 方法是根据对象 的 hashcode 计算产生的 hash 值取定位内部存储位置。
         *
         * 解决：保证对象是不可变对象
         * 将类和属性设置为final
         */
        goods.setMount(999);
        System.out.println(goodsMap.get(goods));
    }
}

@AllArgsConstructor
@Getter
@Setter
class Goods {
    private long id;
    private String name;
    private long mount;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Goods goods = (Goods) o;
        return id == goods.id &&
                mount == goods.mount &&
                name.equals(goods.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, mount);
    }
}
