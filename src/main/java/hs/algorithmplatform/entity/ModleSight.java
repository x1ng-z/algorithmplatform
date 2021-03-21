package hs.algorithmplatform.entity;

import com.alibaba.fastjson.JSONArray;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/9 10:10
 */
public class ModleSight {
    private int modlesightid;
    private double positiontop;//前端位置
    private double positionleft;
    private JSONArray childs;//连接子模块
    private JSONArray parents;
    private int refmodleid;

    public int getModlesightid() {
        return modlesightid;
    }

    public void setModlesightid(int modlesightid) {
        this.modlesightid = modlesightid;
    }

    public double getPositiontop() {
        return positiontop;
    }

    public void setPositiontop(double positiontop) {
        this.positiontop = positiontop;
    }

    public double getPositionleft() {
        return positionleft;
    }

    public void setPositionleft(double positionleft) {
        this.positionleft = positionleft;
    }

    public JSONArray getChilds() {
        return childs;
    }

    public void setChilds(JSONArray childs) {
        this.childs = childs;
    }

    public int getRefmodleid() {
        return refmodleid;
    }

    public void setRefmodleid(int refmodleid) {
        this.refmodleid = refmodleid;
    }

    public JSONArray getParents() {
        return parents;
    }

    public void setParents(JSONArray parents) {
        this.parents = parents;
    }
}
