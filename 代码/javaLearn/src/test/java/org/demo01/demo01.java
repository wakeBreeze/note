package org.demo01;

import java.util.concurrent.TimeUnit;

/**
 * @Description: org.demo01
 * @version: 1.0
 */
public class demo01{
    public static void main(String[] args) {
        new Thread(()->{
            synchronized (Integer.class){
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                synchronized (String.class){
                }
            }
        }).start();

        new Thread(()->{
            synchronized (String.class){
                synchronized (Integer.class){
                }
            }
        }).start();
    }
    public static synchronized void method1(){
        System.out.println("method1 called");
    }
    public static synchronized void method2(){
        System.out.println("method2 called");
    }
}

class Th1{
}
class Th2{
}
