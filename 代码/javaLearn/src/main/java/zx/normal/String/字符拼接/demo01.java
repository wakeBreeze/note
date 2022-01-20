package zx.normal.String.字符拼接;

/**
 * @Description: zx.normal.String.字符拼接
 * @version: 1.0
 */
public class demo01 {
    /**
     * 优化前拼接
     * @return
     */
    public static String doAdd(){
        String str = "";
        for (int i = 0; i < 10000; i++) {
            str += "i:" + i;
        }
        return str;
    }

    /**
     * 优化后拼接
     * @return
     */
    public static String doAddEfficient(){
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < 10000000; i++) {
            str.append("i:" + i);
        }
        return str.toString();
    }

    public static String doAddEfficient2(){
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < 10000000; i++) {
            str.append("i:");
            str.append(i);
        }
        return str.toString();
    }

    public static void main(String[] args) {
        for (int i = 0; i < 1; i++) {
            /*long start1 = System.currentTimeMillis();
            doAdd();
            long end1 = System.currentTimeMillis();
            System.out.println("doAdd耗时："+(end1 - start1));*/

            long start2 = System.currentTimeMillis();
            doAddEfficient();
            long end2 = System.currentTimeMillis();
            System.out.println("doAddEfficient耗时："+(end2 - start2));

            long start3 = System.currentTimeMillis();
            doAddEfficient2();
            long end3 = System.currentTimeMillis();
            System.out.println("doAddEfficient2耗时："+(end3 - start3));
            System.out.println("-----------------------");
        }

    }
}
