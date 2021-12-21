package zx.normal.注解.自定义注解.demo01;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: zx.normal.注解.自定义注解.demo01
 * @version: 1.0
 */
@Data
@NoArgsConstructor
//@AllArgsConstructor
public class Student {
    private String name;// 学号

    private int age;    // 姓名

    @Length(min = 11,max = 11,errorMsg = "电话号码长度应为11位")
    private String mobile;// 手机号码(11位)

    //set时校验注解(也可通过反射实现)
    public void setMobile(String mobile) throws IllegalAccessException {
        this.mobile = mobile;
        // 反射校验注解
        System.out.println(ReflectionVali.validate(this));
    }

    //构造时校验注解(也可通过反射实现)
    public Student(String name, int age, String mobile) throws IllegalAccessException {
        this.name = name;
        this.age = age;
        this.mobile = mobile;
        // 反射校验注解
        System.out.println(ReflectionVali.validate(this));
    }

    public static void main(String[] args) throws IllegalAccessException {
        Student s = new Student("小明", 22, "1234567890");
//        String validate = ReflectionVali.validate(s);
//        System.out.println(validate);
        s.setMobile("12345678901");
    }
}
