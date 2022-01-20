package zx.normal.注解.自定义注解.demo01;

/**
 * @Description: zx.normal.注解.自定义注解.ConcurrentHashMap键值不能为null
 * @version: 1.0
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**字段长度校验注解*/
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Length {
    int min();// 最小长度
    int max();// 最大长度
    String errorMsg();// 错误信息
}
