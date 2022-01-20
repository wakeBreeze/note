package zx.normal.IO.demo02;

import zx.normal.IO.demo01.Directory;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * @Description: zx.normal.IO.demo02
 * @version: 1.0
 */

/**压缩工具
 * 将多个文件压缩成zip包
 */
public class ZipFileUtils {
    public static void compressFiles(File[] files,String zipPath) throws IOException {
        // 定义文件输出流，表名是要压缩成zip文件的
        FileOutputStream f = new FileOutputStream(zipPath);

        // 给输出流增加校验功能
        CheckedOutputStream checkedOs = new CheckedOutputStream(f,new Adler32());

        // 定义zip格式的输出流，这里要明白一直在使用装饰期末是在给流添加功能
        // ZipOutputStream 也是从FilterOutputStream 继承下来的
        ZipOutputStream zipOut = new ZipOutputStream(checkedOs);

        // 增加缓冲功能，提高性能
        BufferedOutputStream buffOut = new BufferedOutputStream(zipOut);

        // 对于压缩输出流我们可以设置个注释
        zipOut.setComment("zip test");

        // 下面就是从Files[]数组中读取一批文件,然后写入zip包的过程
        for (File file : files) {
            // 建立读取文件的缓冲流，同样是装饰器模式使用BufferedReader
            // 包装了FileReader
            BufferedReader bfReader = new BufferedReader(new FileReader(file));

            // 一个文件对象在zip流中用一个ZipEntry表示，使用putNextEntry添加到zip流中
            zipOut.putNextEntry(new ZipEntry(file.getName()));

            int c;
            while ((c = bfReader.read()) != -1){
                buffOut.write(c);
            }

            // 注意这里要关闭
            bfReader.close();
            buffOut.flush();;
        }
        buffOut.close();
    }

    /**
     * 解压缩zip包到目标文件夹
     * @param zipPath   zip包完整路径
     * @param destPath  解压到哪个文件夹
     */
    public static void unCompressZip(String zipPath,String destPath) throws IOException {
        if (!destPath.endsWith(File.separator)){
            destPath = destPath + File.separator;
            File file = new File(destPath);
            if (!file.exists()){
                file.mkdirs();
            }
        }
        // 新建文件输入流类
        FileInputStream fis = new FileInputStream(zipPath);

        // 给输入流增加检验功能
        CheckedInputStream checkedIns = new CheckedInputStream(fis,new Adler32());

        // 新建zip输出流，因为读取的zip格式的文件嘛
        ZipInputStream zipIn = new ZipInputStream(checkedIns);

        // 增加缓冲流功能，提高性能
        BufferedInputStream buffIn = new BufferedInputStream(zipIn);

        // 从zip输入流中读入每个ZipEntry对象
        ZipEntry zipEntry;
        while ((zipEntry = zipIn.getNextEntry()) != null){
            System.out.println("解压中" + zipEntry);

            // 将解压的文件写入到目标文件夹下
            int size;
            byte[] buffer = new byte[1024];
            FileOutputStream fos = new FileOutputStream(destPath + zipEntry.getName());
            BufferedOutputStream bos = new BufferedOutputStream(fos,buffer.length);
            while ((size = buffIn.read(buffer,0,buffer.length)) != -1){
                bos.write(buffer,0,size);
            }
            bos.flush();
            bos.close();
        }
        buffIn.close();

        // 输出校验和
        System.out.println("校验和" + checkedIns.getChecksum().getValue());
    }

    public static void unCompressZipSimple(String zipPath,String destPath) throws Exception {
        // 判断源文件是否存在
        File sourceFile = new File(zipPath);
        if (!sourceFile.exists()){
            throw new Exception(sourceFile.getPath() + "所指文件不存在！");
        }
        // 创建目的文件夹
        if (!destPath.endsWith(File.separator)){
            destPath += File.separator;
            File file = new File(destPath);
            if (!file.exists()){
                file.mkdirs();
            }
        }

        ZipFile zipFile = new ZipFile(zipPath);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()){
            ZipEntry zipEntry = entries.nextElement();
            System.out.println("解压中:" + zipEntry);

            InputStream inputStream = zipFile.getInputStream(zipEntry);//用输入流读取压缩文件中制定目录中的文件
            BufferedInputStream bis = new BufferedInputStream(inputStream);// 包装输入缓冲流

            // 将解压文件写入到目标文件夹下
            int size;
            byte[] buffer = new byte[1024];
            FileOutputStream fos = new FileOutputStream(destPath + zipEntry.getName());// 获取输出流
            BufferedOutputStream bos = new BufferedOutputStream(fos,buffer.length);// 包装输出缓冲流
            while ((size = bis.read(buffer,0,buffer.length)) != -1){
                bos.write(buffer,0,size);
            }
            bos.flush();
            bos.close();
            bis.close();
        }
    }

    public static void main(String[] args) throws Exception {
        String dir = "d:";
        String zipPath = "d:/test.zip";
        //压缩
//        File[] files = Directory.getLocalFiles(dir,".*\\.txt");
//        ZipFileUtils.compressFiles(files,zipPath);
        //解压缩
//        ZipFileUtils.unCompressZip(zipPath,"F:/ziptest");

        //解压缩simple
//        ZipFileUtils.unCompressZipSimple(zipPath,"F:\\ziptest");


        System.out.println("======================");

        /**
         * 根据手术组合找出
         * 所有可入组中最合适的组
         */
        List<String> maxList = new ArrayList<>();

        maxList.add("A+D+B");
//        maxList.add("B+C+E+D");
        maxList.add("A+C+B");
        maxList.add("A+B+C");
        maxList.add("C+B+A");
        maxList.add("C+B+D");
        String bkf172 = "A+B+C+D";
        String[] split = bkf172.split("\\+");
        Map<String,Integer> bkf172Map = new HashMap<>(maxList.size());// k:存储可入组,v:该组得分
        int max = 0; //存所有组目前最高分
        String result = "";//存最高分的组
        int i = split.length;// 初始化手术组中第一项得分，其他项分数递减
        for (String bkf172_tar : maxList) {
            bkf172Map.put(bkf172_tar,0);// 初始化可入组得分
        }
        for (String s : split) {
            for (String bkf172_tar : bkf172Map.keySet()) {
                if (bkf172_tar.contains(s)){// 可入组包含手术组中对应手术，即获得对应分数。
                    bkf172Map.put(bkf172_tar,bkf172Map.get(bkf172_tar) + i);
                }
                // 更新得分最高的组合以及最高分
                if (bkf172Map.get(bkf172_tar)>max){
                    max = bkf172Map.get(bkf172_tar);
                    result = bkf172_tar;
                }
            }
            i--;
        }
        System.out.println(result);
    }
}
