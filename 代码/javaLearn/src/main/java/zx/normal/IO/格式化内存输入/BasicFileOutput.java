package zx.normal.IO.格式化内存输入;

import zx.normal.IO.缓冲输入文件.BufferedInputFile;

import java.io.*;

/**
 * @Description: zx.normal.IO.格式化内存输入
 * @version: 1.0
 */

/**
 * 通过代码加上行号
 */
public class BasicFileOutput {
    public static void addLineCount(String sourcePath,String destPath) throws IOException {
        BufferedReader bf = new BufferedReader(new StringReader(BufferedInputFile.readFile(sourcePath)));
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(destPath)));

        int lineCount = 1;
        String s;
        while ((s = bf.readLine()) != null){
            pw.println(lineCount++ + ":" + s);
        }
        pw.flush();
        pw.close();
        bf.close();
    }

    public static void main(String[] args) throws IOException {
        addLineCount("D:\\haha.txt","F:\\BasicFileOutPut.out");
    }
}
