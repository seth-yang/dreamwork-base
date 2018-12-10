package org.dreamwork.ansy.progress;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 14-7-29
 * Time: 下午12:24
 */
public interface IProgressWatcher<T> {
    void ready ();
    void setMax (int max);
    void setValue (int value);
    void setMessage (String message);
    void setPercentText (String percentText);
    void done (T data);
    void fail (int code, String reason);
    void updateProgress ();

    IProgressWatcher execute (IProgressWorker worker) throws Exception;
}
