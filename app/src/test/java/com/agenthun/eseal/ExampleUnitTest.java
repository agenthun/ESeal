package com.agenthun.eseal;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    @Test
    public void doAsnyTask() {
        final Thread thread = new Thread(new MyThread());
        System.out.println("begin");

        thread.start();

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            System.out.println("error 2");
            e.printStackTrace();
        }

        Thread.currentThread().interrupt();
        System.out.println("end");
    }

    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
    }

    private class WebPage {
        int current;
        int total;

        public WebPage() {
        }

        public WebPage(int current, int total) {
            this.current = current;
            this.total = total;
        }

        public int getCurrent() {
            return current;
        }

        public void setCurrent(int current) {
            this.current = current;
        }

        public int getTotal() {
            return total;
        }

        public void setTotal(int total) {
            this.total = total;
        }
    }

    private class MyThread implements Runnable {

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    System.out.println("doing something");
                    Thread.sleep(100);
                } catch (Exception e) {
                    System.out.println("error 1");
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}