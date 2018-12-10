package org.dreamwork.network;

import org.dreamwork.concurrent.ICancelable;

import java.io.IOException;

/**
 * Created by game on 2016/3/26.
 */
public interface IServer extends ICancelable {
    void bind (int port) throws IOException;
}