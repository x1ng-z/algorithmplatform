package hs.algorithmplatform.entity.model;

import com.alibaba.fastjson.JSONObject;

import java.time.Instant;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/8 16:54
 */
public class BaseModlePropertyImp implements ModleProperty {

    /**memory*/
    private Double value;




    /****db****/
    private int modlepinsId;
    private long refmodleId;
    private String modleOpcTag;
    private String modlePinName;
    private String opcTagName;//中文注释
    private JSONObject resource;
    private int pinEnable=1;
    private Instant updateTime;
    private String pindir;//引脚方向//in/out?
    private String modlepropertyclazz;//引脚类型 base/mpc?

    /**********/

    public int getModlepinsId() {
        return modlepinsId;
    }

    public void setModlepinsId(int modlepinsId) {
        this.modlepinsId = modlepinsId;
    }

    public long getRefmodleId() {
        return refmodleId;
    }

    public void setRefmodleId(long refmodleId) {
        this.refmodleId = refmodleId;
    }

    public String getModleOpcTag() {
        return modleOpcTag;
    }

    public void setModleOpcTag(String modleOpcTag) {
        this.modleOpcTag = modleOpcTag;
    }

    public String getModlePinName() {
        return modlePinName;
    }

    public void setModlePinName(String modlePinName) {
        this.modlePinName = modlePinName;
    }

    public String getOpcTagName() {
        return opcTagName;
    }

    public void setOpcTagName(String opcTagName) {
        this.opcTagName = opcTagName;
    }



    public int getPinEnable() {
        return pinEnable;
    }

    public void setPinEnable(int pinEnable) {
        this.pinEnable = pinEnable;
    }

    public Instant getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Instant updateTime) {
        this.updateTime = updateTime;
    }

    public String getPindir() {
        return pindir;
    }

    public void setPindir(String pindir) {
        this.pindir = pindir;
    }

    public String getModlepropertyclazz() {
        return modlepropertyclazz;
    }

    public void setModlepropertyclazz(String modlepropertyclazz) {
        this.modlepropertyclazz = modlepropertyclazz;
    }

    public JSONObject getResource() {
        return resource;
    }

    public void setResource(JSONObject resource) {
        this.resource = resource;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
    /**************/



}
