package zx.normal.注解.自定义注解.demo01;

import java.lang.reflect.Field;

/**
 * @Description: zx.normal.注解.自定义注解.demo01
 * @version: 1.0
 */
//反射校验Length注解
public class ReflectionVali {
    public static String validate(Object object) throws IllegalAccessException {
        // 1、获取对象的所有字段
        Field[] declaredFields = object.getClass().getDeclaredFields();
        // 2、遍历所有字段
        for (Field declaredField : declaredFields) {
            // 3、判断属性上是否有Length注解
            if (declaredField.isAnnotationPresent(Length.class)){
                // 4、获取字段注解内容
                Length length = declaredField.getAnnotation(Length.class);
                /** 校验注解内容 */
                declaredField.setAccessible(true);
                String field = (String)declaredField.get(object);
                // 5、校验注解
                if (length.min() > field.length() || length.max() < field.length()){
                    return length.errorMsg();
                }
            }
        }
        return null;
    }
}
