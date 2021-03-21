package hs.algorithmplatform.pydriver.command;

import com.alibaba.fastjson.JSONObject;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/9/28 13:28
 */
public interface Command {

    JSONObject analye(byte[] context);

    byte[] build(byte[] context, long nodeid);

    boolean valid(byte[] context);
}
