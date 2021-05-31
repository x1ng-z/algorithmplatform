package hs.algorithmplatform.entity.model;

import com.alibaba.fastjson.JSONObject;
import hs.algorithmplatform.entity.bean.BridgeInfo;

import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/22 13:23
 */
public interface Process   {

    JSONObject inprocess();
    void docomputeprocess();
    JSONObject computresulteprocess(JSONObject computedata);

    void outprocess(JSONObject outdata);

    void init(Map<Long, BridgeInfo> bridgeCache);
}
