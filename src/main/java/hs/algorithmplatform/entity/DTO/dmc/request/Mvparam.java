package hs.algorithmplatform.entity.DTO.dmc.request;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/8 14:11
 */
public class Mvparam {
    private String mvpinname;
    private double mvpinvalue;
    private double r;
    private double dmvhigh;
    private double dmvlow;
    private double mvuppinvalue;
    private double mvdownpinvalue;
    private double mvfbpinvalue;

    public String getMvpinname() {
        return mvpinname;
    }

    public void setMvpinname(String mvpinname) {
        this.mvpinname = mvpinname;
    }

    public double getMvpinvalue() {
        return mvpinvalue;
    }

    public void setMvpinvalue(double mvpinvalue) {
        this.mvpinvalue = mvpinvalue;
    }

    public double getR() {
        return r;
    }

    public void setR(double r) {
        this.r = r;
    }

    public double getDmvhigh() {
        return dmvhigh;
    }

    public void setDmvhigh(double dmvhigh) {
        this.dmvhigh = dmvhigh;
    }

    public double getDmvlow() {
        return dmvlow;
    }

    public void setDmvlow(double dmvlow) {
        this.dmvlow = dmvlow;
    }

    public double getMvuppinvalue() {
        return mvuppinvalue;
    }

    public void setMvuppinvalue(double mvuppinvalue) {
        this.mvuppinvalue = mvuppinvalue;
    }

    public double getMvdownpinvalue() {
        return mvdownpinvalue;
    }

    public void setMvdownpinvalue(double mvdownpinvalue) {
        this.mvdownpinvalue = mvdownpinvalue;
    }

    public double getMvfbpinvalue() {
        return mvfbpinvalue;
    }

    public void setMvfbpinvalue(double mvfbpinvalue) {
        this.mvfbpinvalue = mvfbpinvalue;
    }
}
