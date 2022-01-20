package zx.normal.IO.格式化内存输入;

/**
 * @Description: zx.normal.IO.格式化内存输入
 * @version: 1.0
 */

import zx.normal.IO.缓冲输入文件.BufferedInputFile;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * 格式化内存输入
 *
 * 要读取格式化的数据，可以使用DataInputStream。
 */
public class FormattedMemoryInput {
    public static void main(String[] args) {
        try {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(BufferedInputFile.readFile("D:\\haha.txt").getBytes()));
            while (true){
                if (dis.available() == 0){
                    break;
                }
                System.out.print((char)dis.readByte());

            }
        } catch (IOException e) {
            System.out.println("End of stream");
        }
    }
}
