package zx.normal.IO.数据存储与恢复;

import java.io.*;

/**
 * @Description: zx.normal.IO.数据存储与恢复
 * @version: 1.0
 */
public class StoringAndRecoveringData {
    public static void main(String[] args) throws IOException {
        /**
         * 数据存储
         */
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("F:\\store.txt")));
        dos.writeDouble(3.14159);
        dos.writeUTF("三连走起");
        dos.writeInt(123);
        dos.writeUTF("点赞加关注");
        dos.flush();
        dos.close();

        /**
         * 数据恢复
         */
        DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream("F:\\store.txt")));
        System.out.println(dis.readDouble());
        System.out.println(dis.readUTF());
        System.out.println(dis.readInt());
        System.out.println(dis.readUTF());
        dis.close();
    }
}
