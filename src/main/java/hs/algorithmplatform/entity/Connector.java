package hs.algorithmplatform.entity;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/24 12:36
 */
public interface Connector {
    void connect();
    void reconnect();
    void destory();
}
