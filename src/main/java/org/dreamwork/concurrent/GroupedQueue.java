package org.dreamwork.concurrent;

import org.dreamwork.util.CircleArrayQueue;
import org.dreamwork.util.IGroupedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 分组队列.
 * 已 {@code K} 分组的消息 {@code T} 队列.
 * 不同的分组将尽量并行处理；而相同的分组则按 {@code FIFO} 原则依次处理。相同分组的处理器将在同一个线程内处理，
 * 故处理器<strong>不应该阻塞当前线程</strong>
 * @param <K> 分组键值
 * @param <T> 消息
 */
@SuppressWarnings ("unused")
public class GroupedQueue<K, T> implements IGroupedQueue<K, T> {
    private final Logger logger = LoggerFactory.getLogger (GroupedQueue.class);
    private final ExecutorService executor = Executors.newCachedThreadPool ();
    private final Map<K, GroupProcessor<K, T>> clutch = Collections.synchronizedMap (new HashMap<> ());
    private final IGroupedProcessor<K, T> processor;
    private int capacity = 32;

    /**
     * 构造函数
     * @param processor 消息处理器. 处理器的第一个参数是队列内的处理次数，第二参数为消息对象
     */
    public GroupedQueue (IGroupedProcessor<K, T> processor) {
        Objects.requireNonNull (processor, "processor cannot be null");
        this.processor = processor;
    }

    /**
     * 构造函数
     * @param capacity  队列容量
     * @param processor 消息处理器. 处理器的第一个参数是队列内的处理次数，第二参数为消息对象
     */
    public GroupedQueue (int capacity, IGroupedProcessor<K, T> processor) {
        this (processor);
        this.capacity = capacity;
    }

    /**
     * 设置队列的容量.
     * <p>该容量值的是单一分组的容量</p>
     * 仅影响后续新建的分组队列
     * @param capacity 单一分组的队列容量
     */
    public void setCapacity (int capacity) {
        this.capacity = capacity;
    }

    @Override
    public synchronized void remove (K key) {
        clutch.remove (key);
    }

    public synchronized void offer (K key, T value) {
        if (clutch.containsKey (key)) {
            if (logger.isTraceEnabled ()) {
                logger.trace ("there's another processor associated to the key: {}, attach to it!", key);
            }
            clutch.get (key).add (value);
        } else {
            if (logger.isTraceEnabled ()) {
                logger.trace ("key {} not in clutch, create a new one, and start it!", key);
            }
            clutch.computeIfAbsent (key, index -> {
                GroupProcessor<K, T> p = new GroupProcessor<> (this, index, capacity);
                p.add (value);
                executor.execute (() -> {
                    Thread.currentThread ().setName (String.format ("processor-#%s", index));
                    p.process (processor);
                });
                return p;
            });
        }
    }

    /**
     * 销毁分组队列
     */
    @Override
    public synchronized void dispose () {
        clutch.clear ();
        executor.shutdownNow ();
    }

    private static class GroupProcessor<K, T> {
        private final Logger logger = LoggerFactory.getLogger (GroupProcessor.class);
        private final K key;
        private final GroupedQueue<K, T> container;
        private final CircleArrayQueue<T> queue;
        private int count;

        GroupProcessor (GroupedQueue<K, T> container, K key, int capacity) {
            this.key = key;
            this.container = container;
            this.queue = new CircleArrayQueue<> (capacity);
        }

        void add (T message) {
            this.queue.push (message);
        }

        void process (IGroupedProcessor<K, T> processor) {
            try {
                processor.setup (key);
                while (queue.size () > 0) {
                    try {
                        processor.process (queue.take ());
                    } catch (Exception ex) {
                        logger.warn (ex.getMessage (), ex);
                    }
                    if (logger.isTraceEnabled ()) {
                        count ++;
                    }
                }
                processor.cleanup (key);
            } catch (Exception ex) {
                logger.warn (ex.getMessage ());
            } finally {
                if (logger.isTraceEnabled ()) {
                    logger.trace ("{} jobs done in processor[{}]. remove the group from clutch", count, key);
                }
                container.remove (key);
            }
        }
    }
}