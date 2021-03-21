package hs.algorithmplatform.entity.DTO.dmc.request;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/8 14:12
 */
public class Ffparam {
    private String ffpinname;
    private double ffpinvalue;
    private Double ffuppinvalue;
    private Double ffdownpinvalue;

    public String getFfpinname() {
        return ffpinname;
    }

    public void setFfpinname(String ffpinname) {
        this.ffpinname = ffpinname;
    }

    public double getFfpinvalue() {
        return ffpinvalue;
    }

    public void setFfpinvalue(double ffpinvalue) {
        this.ffpinvalue = ffpinvalue;
    }

    public Double getFfuppinvalue() {
        return ffuppinvalue;
    }

    public void setFfuppinvalue(Double ffuppinvalue) {
        this.ffuppinvalue = ffuppinvalue;
    }

    public Double getFfdownpinvalue() {
        return ffdownpinvalue;
    }

    public void setFfdownpinvalue(Double ffdownpinvalue) {
        this.ffdownpinvalue = ffdownpinvalue;
    }
}
