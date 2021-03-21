package hs.algorithmplatform.entity.DTO.pid.request;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/9 9:17
 */
public class PidBaseModleParam {
    private String modelname;
    private String modeltype;
    private long modelid;

    public String getModelname() {
        return modelname;
    }

    public void setModelname(String modelname) {
        this.modelname = modelname;
    }

    public String getModeltype() {
        return modeltype;
    }

    public void setModeltype(String modeltype) {
        this.modeltype = modeltype;
    }

    public long getModelid() {
        return modelid;
    }

    public void setModelid(long modelid) {
        this.modelid = modelid;
    }

}
