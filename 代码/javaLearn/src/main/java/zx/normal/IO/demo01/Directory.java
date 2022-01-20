package zx.normal.IO.demo01;

import java.io.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.regex.Pattern;

/**
 * @Description: zx.normal.IO.demo01
 * @version: 1.0
 */
public class Directory {
    /**
     * 根据传入的规则，遍历得到目录中所有的文件构成File对象数组
     */
    public static File[] getLocalFiles(File dir,final String regex){
        return dir.listFiles(new FilenameFilter() {
            private Pattern pattern = Pattern.compile(regex);
            @Override
            public boolean accept(File dir, String name) {
                return pattern.matcher(new File(name).getName()).matches();
            }
        });
    }
    //重载方法
    public static File[] getLocalFiles(String path,final String regex){
        return getLocalFiles(new File(path),regex);
    }
    public static void main(String[] args) throws IOException {
        String dir = "d:";
        File[] files = Directory.getLocalFiles(dir,".*\\" +
                " .txt");
        for (File file : files) {
            System.out.println(file.getAbsolutePath());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String s;
        while ((s=reader.readLine())!=null && s.length()!=0){
            System.out.println(s);
        }
    }
}

