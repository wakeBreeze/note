package zx.normal.IO.缓冲输入文件;

import java.io.*;

/**
 * @Description: zx.normal.IO.缓冲输入文件
 * @version: 1.0
 */
public class BufferedInputFile {
    /**
     * 缓冲输入文件
     * @param fileName
     * @return
     * @throws IOException
     */
    public static String readFile(String fileName) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        String s;
        StringBuilder sb = new StringBuilder();
        while ((s = br.readLine()) != null){
            sb.append(s + "\n");
        }
        br.close();
        return sb.toString();
    }

    public static void main(String[] args) throws IOException {
        System.out.println(readFile("D:\\haha.txt"));
    }
}
