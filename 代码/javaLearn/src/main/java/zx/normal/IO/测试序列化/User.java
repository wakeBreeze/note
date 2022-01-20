package zx.normal.IO.测试序列化;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.*;

/**
 * @Description: zx.normal.IO.demo03
 * @version: 1.0
 */
@Data
@AllArgsConstructor
public class User implements Serializable{
    private String name;
    private int age;

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        User lili = new User("Lili", 21);
        // 写出对象
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("F:/user.out"));
        oos.writeObject(lili);
        oos.close();

        // 读入对象
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream("F:/user.out"));
        User user = (User)ois.readObject();
        System.out.println(user.toString());
        ois.close();
    }
}
