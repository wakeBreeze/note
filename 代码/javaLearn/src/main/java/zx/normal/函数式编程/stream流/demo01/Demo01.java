package zx.normal.函数式编程.stream流.demo01;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 给定一个字符串元素列表:{"1", "2", "bilibili", "of", "codesheep", "5", "at", "BILIBILI", "codesheep", "23", "CHEERS", "6"}
 * 实现：找出所有 长度>=5的字符串，并且忽略大小写、去除重复字符串，然后按字母排序，最后用“爱心❤”连接成一个字符串输出！
 * @Description: zx.normal.函数式编程.stream流.demo01
 * @version: 1.0
 */
public class Demo01 {
    private static String[] strs = new String[]{"1", "2", "bilibili", "of", "codesheep", "5", "at", "BILIBILI", "codesheep", "23", "CHEERS", "6"};

    /**
     * 判断是否是数字类型
     *
     * @param str
     * @return
     */
    public static boolean isNum(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        /*char[] chars = str.toCharArray();
        for (char c : chars) {
            if (!Character.isDigit(c)){
                return false;
            }
        }*/
        return true;
    }

    /**
     * 传统方法
     *
     * @param strs
     */
    public static void traditionalMethod(String[] strs) {
        // 先定义一个具备按字母排序功能的Set容器，Set本身即可去重
        Set<String> strSet = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });

        // 以下for循环完成元素去重、大小写转换、长度判断等操作
        for (int i = 0; i < strs.length; i++) {
            String str = strs[i];
            if (!isNum(str) && str.length() > 5) {
                strSet.add(str.toLowerCase());
            }
        }

        // 以下for循环完成连词成句
        StringBuilder stringBuilder = new StringBuilder(strSet.size());
        for (String s : strSet) {
            stringBuilder.append(s);
            stringBuilder.append("❤");
        }
        //去除最后一个❤
        stringBuilder.replace(stringBuilder.lastIndexOf("❤"), stringBuilder.length(), "");
//        String s = stringBuilder.substring(0, stringBuilder.length() - 1).toString();
        System.out.println(stringBuilder);
    }

    public static void streamMethod(String[] strs) {
        List<String> list = Arrays.asList(strs);
        String s = list.stream()
                .filter(i -> !isNum(i))
                .filter(i -> i.length() >= 5)
                .map(i -> i.toLowerCase())
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.joining("❤"));
        System.out.println(s);
    }

    public static void main(String[] args) {
//        traditionalMethod(strs);
//        streamMethod(strs);
        String str = "ss";
        System.out.println(8 & str.hashCode());
        System.out.println(str.hashCode() % 8);

        /**Consumer*/
        Consumer c = System.out::println;
        c.accept("===========Consumer===========");
        c.accept("hello world!");
        c.andThen(c).andThen(c).accept("hi");

        /**Function*/
        c.accept("===========Function===========");
        Function<Integer,Integer> f1 = i -> i + i;
        Function<Integer,Integer> f2 = i -> i * i;
        c.accept(f1.andThen(f2).apply(3));

        /**Optional*/

    }
}
