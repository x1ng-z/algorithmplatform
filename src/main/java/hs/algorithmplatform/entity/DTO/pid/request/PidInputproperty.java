package hs.algorithmplatform.entity.DTO.pid.request;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/9 9:19
 */
public class PidInputproperty {
    private double auto;
    private double kp;
    private double ki;
    private double kd;
    private double pv;
    private double sp;
    private double mv;
    private Double ff;
    private double kf;
    private double deadZone;
    private double dmvHigh;
    private double dmvLow;
    private double mvuppinvalue;
    private double mvdownpinvalue;

    public double getKp() {
        return kp;
    }

    public void setKp(double kp) {
        this.kp = kp;
    }

    public double getKi() {
        return ki;
    }

    public void setKi(double ki) {
        this.ki = ki;
    }

    public double getKd() {
        return kd;
    }

    public void setKd(double kd) {
        this.kd = kd;
    }

    public double getPv() {
        return pv;
    }

    public void setPv(double pv) {
        this.pv = pv;
    }

    public double getSp() {
        return sp;
    }

    public void setSp(double sp) {
        this.sp = sp;
    }

    public double getMv() {
        return mv;
    }

    public void setMv(double mv) {
        this.mv = mv;
    }



    public double getKf() {
        return kf;
    }

    public void setKf(double kf) {
        this.kf = kf;
    }

    public double getDeadZone() {
        return deadZone;
    }

    public void setDeadZone(double deadZone) {
        this.deadZone = deadZone;
    }

    public double getDmvHigh() {
        return dmvHigh;
    }

    public void setDmvHigh(double dmvHigh) {
        this.dmvHigh = dmvHigh;
    }

    public double getDmvLow() {
        return dmvLow;
    }

    public void setDmvLow(double dmvLow) {
        this.dmvLow = dmvLow;
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

    public double getAuto() {
        return auto;
    }

    public void setAuto(double auto) {
        this.auto = auto;
    }

    public Double getFf() {
        return ff;
    }

    public void setFf(Double ff) {
        this.ff = ff;
    }
}
