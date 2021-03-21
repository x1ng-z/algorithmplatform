package hs.algorithmplatform.entity.DTO.dmc.respon;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/9 11:27
 */
public class Pvpredict {
    /**
     * {jsonarray,[里面的内容为pvpinname:pvi其中i引脚名称后面的序号,
     *      *  *  * predictorder:[后续N步pv的预测值，用于绘制预测曲线]],[]..}
     * */
    private String pvpinname;
    private double[] predictorder;
    private double[] upfunnel;
    private double[] downfunnel;
    private double e;

    public String getPvpinname() {
        return pvpinname;
    }

    public void setPvpinname(String pvpinname) {
        this.pvpinname = pvpinname;
    }

    public double[] getPredictorder() {
        return predictorder;
    }

    public void setPredictorder(double[] predictorder) {
        this.predictorder = predictorder;
    }

    public double[] getUpfunnel() {
        return upfunnel;
    }

    public void setUpfunnel(double[] upfunnel) {
        this.upfunnel = upfunnel;
    }

    public double[] getDownfunnel() {
        return downfunnel;
    }

    public void setDownfunnel(double[] downfunnel) {
        this.downfunnel = downfunnel;
    }

    public double getE() {
        return e;
    }

    public void setE(double e) {
        this.e = e;
    }
}
