package org.dreamwork.persistence.test;

import org.dreamwork.concurrent.Looper;

import java.util.concurrent.TimeUnit;

/**
 * Created by seth.yang on 2017/8/25
 */
public class Main {
    public static void main (String[] args) throws InterruptedException {
        for (int i = 0; i < 3; i ++) {
/*
            String name = "test.loop." + i;
            Looper.create (name, 16);
            for (int j = 0; j < 5; j ++) {
                Looper.runInLoop (name, new TestRunner ("#" + j));
            }
            Looper.invokeLater (new TestRunner ("bbb"));
            Looper.schedule (new TestRunner ("aaa"), 3 * i, TimeUnit.SECONDS);
*/
            Looper.invokeLater (new TT ());
            Thread.sleep (2000);
        }


        Looper.waitForShutdown ();
        System.out.println ("all jobs done.");
    }

    private static final class TT implements Runnable {
        @Override
        public void run () {
            long start = System.currentTimeMillis ();
            try {
                Thread.sleep (1000);
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
            System.out.println (System.currentTimeMillis () - start);
        }
    }
}
