package zx.mix.面试题.tryCatch;

/**
 * @Description: zx.mix.面试题.tryCatch
 * @version: 1.0
 */
public class Test {
    public static void main(String[] args) {
//        returnTryExec();
//        returnCatchExec();
//        exitTryExec();
//        exitCatchExec();
        System.out.println(Integer.MIN_VALUE - 1);
    }

    public static int returnTryExec() {
        try {
            return 0;
        } catch (Exception e) {
        } finally {
            System.out.println("finally returnTryExec");
            return -1;
        }
    }

    public static int returnCatchExec() {
        try { } catch (Exception e) {
            return 0;
        } finally {
            System.out.println("finally returnCatchExec");
            return -1;
        }
    }

    public static void exitTryExec() {
        try {
            System.exit(0);
        } catch (Exception e) {
        } finally {
            System.out.println("finally exitTryExec");
        }
    }

    public static void exitCatchExec() {
        try { } catch (Exception e) {
            System.exit(0);
        } finally {
            System.out.println("finally exitCatchExec");
        }
    }
}
