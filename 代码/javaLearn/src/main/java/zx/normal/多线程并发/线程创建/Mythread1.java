package zx.normal.多线程并发.线程创建;

/**
 * @Description: zx.normal.多线程并发.线程创建
 * @version: 1.0
 */
public class Mythread1 extends Thread{
    @Override
    public void run() {
        for (int i = 0; i < 30; i++) {
            System.out.println("thread#1===" + i);
            try {
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
