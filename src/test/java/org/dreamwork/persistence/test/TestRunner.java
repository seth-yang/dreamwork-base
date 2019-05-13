package org.dreamwork.persistence.test;

/**
 * Created by seth.yang on 2017/8/25
 */
public class TestRunner implements Runnable {
    private String name;

    TestRunner (String name) {
        this.name = name;
    }

    @Override
    public void run () {
        int count = 5;
        String threadName = Thread.currentThread ().getName ();

        do {
            System.out.printf ("[%s.%s] count = %d%n", threadName, name, count --);
            try {
                Thread.sleep (500);
            } catch (InterruptedException e) {
//                e.printStackTrace ();
                break;
            }
        } while (count > 0);

        System.out.printf ("--------------------------- [%s.%s]remain count = %d%n", threadName, name, count);
    }
}
