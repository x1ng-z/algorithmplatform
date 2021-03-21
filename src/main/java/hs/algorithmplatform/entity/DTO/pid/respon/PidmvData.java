package hs.algorithmplatform.entity.DTO.pid.respon;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/10 11:46
 */
public class PidmvData {
    private String pinname;
    private double value;

    public String getPinname() {
        return pinname;
    }

    public void setPinname(String pinname) {
        this.pinname = pinname;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
