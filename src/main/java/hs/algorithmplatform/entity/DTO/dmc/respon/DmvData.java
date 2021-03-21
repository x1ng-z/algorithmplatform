package hs.algorithmplatform.entity.DTO.dmc.respon;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/9 11:55
 */
public class DmvData {
    /**
     * dmv:{"inputpinname":mvi其中i为mv引脚序号,
     *  * outputpinname:pvi其中i为pv引脚序号,value:0.1}}
     * */
    private String inputpinname;

    private double value;

    public String getInputpinname() {
        return inputpinname;
    }

    public void setInputpinname(String inputpinname) {
        this.inputpinname = inputpinname;
    }



    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}
