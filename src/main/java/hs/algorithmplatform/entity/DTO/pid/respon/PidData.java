package hs.algorithmplatform.entity.DTO.pid.respon;

import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/10 0:25
 */
public class PidData {
    private List<PidmvData> mvData;
    private double partkp;
    private double partki;
    private double partkd;


    public double getPartkp() {
        return partkp;
    }

    public void setPartkp(double partkp) {
        this.partkp = partkp;
    }

    public double getPartki() {
        return partki;
    }

    public void setPartki(double partki) {
        this.partki = partki;
    }

    public double getPartkd() {
        return partkd;
    }

    public void setPartkd(double partkd) {
        this.partkd = partkd;
    }

    public List<PidmvData> getMvData() {
        return mvData;
    }

    public void setMvData(List<PidmvData> mvData) {
        this.mvData = mvData;
    }
}
