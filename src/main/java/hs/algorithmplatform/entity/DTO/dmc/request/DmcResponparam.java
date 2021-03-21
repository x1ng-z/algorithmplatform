package hs.algorithmplatform.entity.DTO.dmc.request;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/8 14:13
 */
public class DmcResponparam {
    private String inputpinname;
    private String outputpinname;
    private Double k;
    private Double t;
    private Double tau;
    private Double ki;

    public String getInputpinname() {
        return inputpinname;
    }

    public void setInputpinname(String inputpinname) {
        this.inputpinname = inputpinname;
    }

    public String getOutputpinname() {
        return outputpinname;
    }

    public void setOutputpinname(String outputpinname) {
        this.outputpinname = outputpinname;
    }

    public double getK() {
        return k;
    }

    public void setK(double k) {
        this.k = k;
    }

    public double getT() {
        return t;
    }

    public void setT(double t) {
        this.t = t;
    }

    public double getTau() {
        return tau;
    }

    public void setTau(double tau) {
        this.tau = tau;
    }

    public double getKi() {
        return ki;
    }

    public void setKi(double ki) {
        this.ki = ki;
    }
}
