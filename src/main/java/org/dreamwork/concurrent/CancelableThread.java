package org.dreamwork.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * 可取消的线程.
 * <p>
 *
 * </p>
 * Created by seth on 16-1-14
 *
 * @since 2.1.0
 * @see org.dreamwork.concurrent.ICancelable
 */
public abstract class CancelableThread extends Thread implements ICancelable {
    volatile protected boolean running = false;
    private static final Logger logger = LoggerFactory.getLogger (CancelableThread.class);

    private static final Set<CancelableThread> references = new HashSet<CancelableThread> ();

    /**
     * 真正实现业务的方法.
     *
     * <p>
     *     <strong>重要</strong>
     *     <i>所有实现，若无特殊的原因，不应处理 InterruptedException 异常</i>
     * </p>
     */
    protected abstract void doWork () throws InterruptedException;

    /**
     * 取消线程前的处理代码，您可以在这里销毁该对象所持有的资源.
     *
     * 在 <code>{@link #cancel(boolean)}</code> 方法内部被调用
     *
     * <p>该方法并不能阻止线程被取消.</p>
     */
    protected void beforeCancel () {}

    /**
     * 静态的销毁实例方法.
     *
     * <p>在 <code>CancelableThread</code> 类内部，会持有所有该类的实例。
     * 若该静态方法被调用，将停止所有已经缓存的实例
     * </p>
     */
    public static void dispose () {
        if (!references.isEmpty ()) {
            for (CancelableThread ct : references) {
                if (!ct.isCanceled ())
                    ct.cancel (true);
            }
        }
    }

    /**
     * 所有实现都应该调用父类的无参 或 一个参数 版本的构造函数，以确保实例缓存.
     * 所有被缓存的实例，在调用 <code>{@link #dispose() static dispose ()}</code>方法后被取消，并销毁
     */
    public CancelableThread () {
        references.add (this);
    }

    /**
     * 所有实现都应该调用父类的构造函数，以确保实例缓存.
     * 所有被缓存的实例，在调用 <code>{@link #dispose() static dispose ()}</code>方法后被取消，并销毁
     *
     * @param name 指定线程名
     */
    public CancelableThread (String name) {
        super (name);
        references.add (this);
    }

    /**
     * 所有实现都应该调用父类的无参 或 一个参数 版本的构造函数，以确保实例缓存.
     * 所有被缓存的实例，在调用 <code>{@link #dispose() static dispose ()}</code>方法后被取消，并销毁
     *
     * @param name 指定线程名
     * @param running 是否自动启动线程
     */
    public CancelableThread (String name, boolean running) {
        super (name);
        references.add (this);
        this.running = running;
        if (running) {
            start ();
        }
    }

    /**
     * 取消线程.
     *
     * @param block 指示是否阻塞当前线程. 若为真，则阻塞当前线程，直至该实例所运行的线程停止.
     *              否则，该方法仅发出线程取消信号，线程真正停止取决于实例本身 ( <code>{@link #doWork()}</code> 方法的具体实现)
     */
    @Override
    public void cancel (boolean block) {
        if (logger.isTraceEnabled ()) {
            logger.trace ("trying to stop " + getName () + "...");
            logger.trace ("invoking the before cancel...");
        }
        try {
            beforeCancel ();
        } catch (Exception ex) {
            logger.warn (ex.getMessage (), ex);
        }
        running = false;
        if (block && (Thread.currentThread () != this))
            try {
                this.join ();
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        synchronized (references) {
            references.remove (this);
        }
        logger.info ("Server [" + getName () + "] stopped.");
    }

    /**
     * 返回线程是否已经被取消
     * @return 若线程已被取消，返回 <code>true</code>， 否则返回 <code>false</code>
     */
    @Override
    public boolean isCanceled () {
        return running;
    }

    /**
     * 线程实现.
     *
     * <p>
     *     无限调用 <code>{@link #doWork()}</code> 方法，直至线程被取消，<br/>
     *     -- 或 --<br/>
     *     捕获到 <code>InterruptedException</code> 异常
     * </p>
     * 换句话说，<code>{@link #doWork()}</code> 方法的实现，不应该处理 <code>InterruptedException</code>，将它直接抛出，留给
     * 父类处理。
     */
    @Override
    public void run () {
        if (logger.isTraceEnabled ())
            logger.trace ("Starting thread[" + getName () + "]");
        while (running) {
            try {
                doWork ();
            } catch (InterruptedException ex) {
                logger.warn ("the thread is interrupted.");
                break;
            } catch (Exception ex) {
                logger.warn (ex.getMessage (), ex);
            }
        }
        if (logger.isTraceEnabled ())
            logger.trace ("Thread[" + getName () + "] stopped.");
    }
}