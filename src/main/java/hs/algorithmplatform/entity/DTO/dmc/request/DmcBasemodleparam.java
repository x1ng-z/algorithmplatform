package hs.algorithmplatform.entity.DTO.dmc.request;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/8 12:41
 */

public class DmcBasemodleparam {
    private String modelname;
    private String modeltype;
    private long modelid;
    private String predicttime_P;
    private String timeserise_N;
    private String controltime_M;
    private int runstyle;
    private double auto;
    private double controlapcoutcycle;

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

    public String getPredicttime_P() {
        return predicttime_P;
    }

    public void setPredicttime_P(String predicttime_P) {
        this.predicttime_P = predicttime_P;
    }

    public String getTimeserise_N() {
        return timeserise_N;
    }

    public void setTimeserise_N(String timeserise_N) {
        this.timeserise_N = timeserise_N;
    }

    public String getControltime_M() {
        return controltime_M;
    }

    public void setControltime_M(String controltime_M) {
        this.controltime_M = controltime_M;
    }

    public int getRunstyle() {
        return runstyle;
    }

    public void setRunstyle(int runstyle) {
        this.runstyle = runstyle;
    }

    public double getAuto() {
        return auto;
    }

    public void setAuto(double auto) {
        this.auto = auto;
    }

    public double getControlapcoutcycle() {
        return controlapcoutcycle;
    }

    public void setControlapcoutcycle(double controlapcoutcycle) {
        this.controlapcoutcycle = controlapcoutcycle;
    }
}
