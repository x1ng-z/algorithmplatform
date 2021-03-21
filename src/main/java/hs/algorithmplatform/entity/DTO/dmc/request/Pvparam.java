package hs.algorithmplatform.entity.DTO.dmc.request;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/8 14:08
 */
public class Pvparam {
    private String pvpinname;
    private double pvpinvalue;
    private double deadzone;
    private double funelinitvalue;
    private String funneltype;
    private double q;
    private double refertrajectorycoef;

    private Double pvuppinvalue;
    private Double pvdownpinvalue;
    private double sppinvalue;
    private String tracoefmethod;


    public String getPvpinname() {
        return pvpinname;
    }

    public void setPvpinname(String pvpinname) {
        this.pvpinname = pvpinname;
    }

    public double getPvpinvalue() {
        return pvpinvalue;
    }

    public void setPvpinvalue(double pvpinvalue) {
        this.pvpinvalue = pvpinvalue;
    }

    public double getDeadzone() {
        return deadzone;
    }

    public void setDeadzone(double deadzone) {
        this.deadzone = deadzone;
    }

    public double getFunelinitvalue() {
        return funelinitvalue;
    }

    public void setFunelinitvalue(double funelinitvalue) {
        this.funelinitvalue = funelinitvalue;
    }

    public String getFunneltype() {
        return funneltype;
    }

    public void setFunneltype(String funneltype) {
        this.funneltype = funneltype;
    }

    public double getQ() {
        return q;
    }

    public void setQ(double q) {
        this.q = q;
    }

    public double getRefertrajectorycoef() {
        return refertrajectorycoef;
    }

    public void setRefertrajectorycoef(double refertrajectorycoef) {
        this.refertrajectorycoef = refertrajectorycoef;
    }

    public Double getPvuppinvalue() {
        return pvuppinvalue;
    }

    public void setPvuppinvalue(Double pvuppinvalue) {
        this.pvuppinvalue = pvuppinvalue;
    }

    public Double getPvdownpinvalue() {
        return pvdownpinvalue;
    }

    public void setPvdownpinvalue(Double pvdownpinvalue) {
        this.pvdownpinvalue = pvdownpinvalue;
    }

    public double getSppinvalue() {
        return sppinvalue;
    }

    public void setSppinvalue(double sppinvalue) {
        this.sppinvalue = sppinvalue;
    }

    public String getTracoefmethod() {
        return tracoefmethod;
    }

    public void setTracoefmethod(String tracoefmethod) {
        this.tracoefmethod = tracoefmethod;
    }
}
