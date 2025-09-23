package org.dreamwork.persistence.test;

import org.dreamwork.concurrent.BatchProcessor;
import org.dreamwork.util.ThreadHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;


public class BatchProcessorTest {
    private static final Logger logger = LoggerFactory.getLogger (BatchProcessorTest.class);
    private static final int workers = 16, count = 32;
    private static int times = 0;

    public static void main (String[] args) throws Exception {
        int read = System.in.read (), capacity = 128;
        System.out.printf ("read = 0x%02x%n", read);
        final AtomicLong counter_send = new AtomicLong (0), counter_receive = new AtomicLong (0);
        final CountDownLatch latch = new CountDownLatch (1);
        final BatchProcessor<String> processor = new BatchProcessor<String> ("test", capacity, capacity, 600) {
            @Override
            protected void process (List<String> data) {
                long timeout = (long) (Math.random () * 40 + 600);
                ThreadHelper.delay (timeout);
                counter_receive.addAndGet (data.size ());
                logger.info ("[{}] {} messages processed.", times, counter_receive.get ());
                times ++;
                if (counter_receive.get () >= workers * count) {
                    latch.countDown ();
                }
            }
        };

        final ExecutorService executor = Executors.newFixedThreadPool (workers + 1);
        processor.start (executor);

        ThreadHelper.delay (500);
        logger.info ("ready to work.");
        for (int i = 0; i < workers; i ++) {
            final int index = i + 1;
            executor.execute (() -> {
                Thread.currentThread ().setName (String.format ("worker-%02d", index));
                for (int j = 0; j < count; j ++) {
                    String message = String.format ("[worker-%02d] message %06d", index, j);
                    processor.add (message);
                    counter_send.incrementAndGet ();
                }
            });
        }
        executor.shutdown ();
        latch.await ();
        logger.info ("send = {}, receive = {}, times = {}", counter_send.get (), counter_receive.get (), times);
        processor.dispose (true);
    }
}