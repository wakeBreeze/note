package zx.mix.tryCatch.内外循环差异;

/**
 * @Description: zx.mix.tryCatch.内外循环差异
 * @version: 1.0
 */
public class AppTest {
    public static void main(String[] args) {
//        System.out.println("循环内的执行结果：" + innerForeach());
//        System.out.println("循环外的执行结果：" + outerForeach());
        String s = new String("1");//此步骤将"1"加入常量池中然后在堆中的创建对象指向常量池，返回对象地址
        s.intern();//此步骤将"1"加入常量池中（由于常量池中已经存在，则直接返回常量池的地址）
        String s2 = "1";//指向常量池中的地址
        System.out.println(s == s2);

        System.out.println("------------------");

        /** 此步骤:
         * 第一个new String("1")：将"1"加入常量池中然后在堆中的创建对象指向常量池
         * 第二个new String("1")：由于常量池中已经存在"1",所以常量池中不再创建"1".直接创建对象。
         * new String("1") + new String("1")：会创建一个新对象，值为"11",返回新对象的地址
         */
        String s3 = new String("1") + new String("1");
        /**由于常量池中没有"11",所以此步骤会在常量池中创建"11"（常量池中存的s3对象"11"的引用)
         * 会将堆中的字符串对象的引用保存到字符串常量池
         */
        String s5 = s3.intern();
        String s4 = "11";//常量池的"11"已经存在（s3的引用)，直接返引用
        System.out.println(s3 == s4);//
        System.out.println(s3 == s5);//
        System.out.println(s5 == s4);//

        System.out.println("===============");

        String str1 = new StringBuilder("计算机").append("软件").toString();

        String str2 = new StringBuilder("Ja").append("va").toString();
        String str5 = "计算机软件";

        String str3 = "java";
        String str4 = new String("java");

        System.out.println(str1.intern() == str1);//因为之前没有所以创建的引用和intern()返回的引用相同
        System.out.println(str1.intern() == str5);
        System.out.println(str2.intern() == str2);//"java在StringBuilder()之前已经出现过",所以intern()返回的引用与新创建的引用不是同一个

        System.out.println(str3 == str4);
    }
    public static int innerForeach(){
        int count = 0;
        for (int i = 0; i < 6; i++) {
            try {
                if (i == 3){
                    throw new Exception("new Exception");
                }
                count++;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return count;
    }
    public static int outerForeach(){
        int count = 0;
        try {
            for (int i = 0; i < 6; i++) {
                if (i == 3){
                    throw new Exception("new Exception");
                }
                count++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return count;
    }
}
