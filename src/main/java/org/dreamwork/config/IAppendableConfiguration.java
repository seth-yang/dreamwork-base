package org.dreamwork.config;

public interface IAppendableConfiguration extends IConfiguration {
    void setRawProperty (String key, String value);
}