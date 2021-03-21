package hs.algorithmplatform.entity.model.modlerproerty;


import hs.algorithmplatform.entity.model.BaseModlePropertyImp;

import java.time.Instant;
import java.util.regex.Pattern;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/8 16:54
 */
public class MPCModleProperty extends BaseModlePropertyImp {
    Pattern pvenablepattern = Pattern.compile("(^pvenable\\d+$)");


    /***memory**/
    private MPCModleProperty dcsEnabePin;
    private MPCModleProperty upLmt;//高限
    private MPCModleProperty downLmt;//低限
    private MPCModleProperty feedBack;//反馈

    /**运行时钟，用于判断引脚是否进行run*/
    private Instant runClock;
    /**由于计算环境的变化，有些不在置信区间内的引脚需要移除控制，
     * 改标识符是用于查看现在引脚是否参与控制
     * */
    private volatile boolean thisTimeParticipate=true;


    /**
     * 判断是否超过的上下界限
     * @return true 突破了;false 没有突破
     * */
    public boolean isBreakLimit(){
        //判断是否超过置信区间
        boolean breaklow=((downLmt!=null)&&(getValue()<downLmt.getValue()));
        boolean breakup=((upLmt!=null)&&(getValue()>upLmt.getValue()));
        return (breaklow||breakup);
    }

    /**
     * 检测引脚闹铃时间到了，可以就可以运行引脚了
     * */
    public boolean clockAlarm(){
        return Instant.now().isAfter(runClock);
    }

    public void clearRunClock() {
        this.runClock = null;
    }



    /******db*****/
    private double Q;
    private double dmvHigh;
    private double deadZone;
    private double funelinitValue;
    private double R;
    private double dmvLow;
    private double referTrajectoryCoef;
    private String funneltype;
    private String pintype;
    private String tracoefmethod;
    /*************/





    public double getQ() {
        return Q;
    }

    public void setQ(double q) {
        Q = q;
    }

    public double getDmvHigh() {
        return dmvHigh;
    }

    public void setDmvHigh(double dmvHigh) {
        this.dmvHigh = dmvHigh;
    }

    public double getDeadZone() {
        return deadZone;
    }

    public void setDeadZone(double deadZone) {
        this.deadZone = deadZone;
    }

    public double getFunelinitValue() {
        return funelinitValue;
    }

    public void setFunelinitValue(double funelinitValue) {
        this.funelinitValue = funelinitValue;
    }

    public double getR() {
        return R;
    }

    public void setR(double r) {
        R = r;
    }

    public double getDmvLow() {
        return dmvLow;
    }

    public void setDmvLow(double dmvLow) {
        this.dmvLow = dmvLow;
    }

    public double getReferTrajectoryCoef() {
        return referTrajectoryCoef;
    }

    public void setReferTrajectoryCoef(double referTrajectoryCoef) {
        this.referTrajectoryCoef = referTrajectoryCoef;
    }

    public String getFunneltype() {
        return funneltype;
    }

    public void setFunneltype(String funneltype) {
        this.funneltype = funneltype;
    }

    public String getPintype() {
        return pintype;
    }

    public void setPintype(String pintype) {
        this.pintype = pintype;
    }

    public String getTracoefmethod() {
        return tracoefmethod;
    }

    public void setTracoefmethod(String tracoefmethod) {
        this.tracoefmethod = tracoefmethod;
    }

    public MPCModleProperty getDcsEnabePin() {
        return dcsEnabePin;
    }

    public void setDcsEnabePin(MPCModleProperty dcsEnabePin) {
        this.dcsEnabePin = dcsEnabePin;
    }

    public MPCModleProperty getUpLmt() {
        return upLmt;
    }

    public void setUpLmt(MPCModleProperty upLmt) {
        this.upLmt = upLmt;
    }

    public MPCModleProperty getDownLmt() {
        return downLmt;
    }

    public void setDownLmt(MPCModleProperty downLmt) {
        this.downLmt = downLmt;
    }

    public MPCModleProperty getFeedBack() {
        return feedBack;
    }

    public void setFeedBack(MPCModleProperty feedBack) {
        this.feedBack = feedBack;
    }

    public Instant getRunClock() {
        return runClock;
    }

    public void setRunClock(Instant runClock) {
        this.runClock = runClock;
    }

    public boolean isThisTimeParticipate() {
        return thisTimeParticipate;
    }

    public void setThisTimeParticipate(boolean thisTimeParticipate) {
        this.thisTimeParticipate = thisTimeParticipate;
    }

    /*************/

}
