package hs.algorithmplatform.entity;

import com.alibaba.fastjson.JSONObject;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/3/31 11:00
 */
public class ResponTimeSerise {
    private int modleresponId;//模型
    private long refrencemodleId;//模型id
    private JSONObject stepRespJson;//响应的json
    private String inputPins;//引脚名称ff mv
    private String outputPins;//引脚名称pv
    private float effectRatio;//作用比例
    /**
     *  * timeserise_N = 40;//响应序列长度
     *  * controlAPCOutCycle = 0;//控制输出间隔周期
     * 二阶时序*/
    public Double[] responTwoTimeSeries(Integer timeserise_N , Integer controlAPCOutCycle) {
        JSONObject json = stepRespJson;
        if (json == null || json.toJSONString().trim().equals("")) {
            return null;
        }

        JSONObject json_test = stepRespJson;//JSON.parseObject(json);

        Double[] respon = new Double[timeserise_N];
        //float delta=1f;
        Double Wdi = Math.sqrt(1 - Math.pow(json_test.getFloat("zata"), 2)) * json_test.getFloat("wn");
        for (int i = 0; i < timeserise_N; i++) {
            if (i*controlAPCOutCycle < json_test.getFloat("tao")) {
                    respon[i] = 0d;
                continue;
            }

            double temp_e = (Math.exp(-1 * json_test.getFloat("wn") * json_test.getFloat("zata") * ((i *controlAPCOutCycle) - json_test.getFloat("tao")))) / Math.sqrt(1 - Math.pow(json_test.getFloat("zata"), 2));
            double temp_sin = Math.sin(Wdi * ((i *controlAPCOutCycle) - json_test.getFloat("tao")) + Math.atan(Math.sqrt(1 - Math.pow(json_test.getFloat("zata"), 2)) / json_test.getFloat("zata")));
            respon[i] = json_test.getFloat("k") * (1 - temp_e * temp_sin);

        }

        return respon;
    }



    /**
     * 一阶
     *
     * timeserise_N = 40;//响应序列长度
     * controlAPCOutCycle = 0;//控制输出间隔周期
     * */
    public double[] responOneTimeSeries(int timeserise_N, int controlAPCOutCycle) {
        JSONObject json = stepRespJson;
        if (json == null || json.toJSONString().trim().equals("")) {
            return null;
        }
        JSONObject jsonmodlerespon = stepRespJson;//JSON.parseObject(json);
        double[] respon = new double[timeserise_N];
        /**
         * 原先是从0开始的到timeserise_N，但是我们要的事从k+1到 N+1
         * */
        for (int i = 0; i < (timeserise_N); i++) {
            if ((i+1)*controlAPCOutCycle < jsonmodlerespon.getFloat("tao")) {
                respon[i] = 0d;
                continue;
            }
            respon[i] = jsonmodlerespon.getFloat("k") * (1 - Math.exp(-(((i+1) * controlAPCOutCycle) - jsonmodlerespon.getFloat("tao")) / (jsonmodlerespon.getFloat("t")+0.0000001)));
        }
        return respon;
    }


    public int getModleresponId() {
        return modleresponId;
    }

    public void setModleresponId(int modleresponId) {
        this.modleresponId = modleresponId;
    }

    public long getRefrencemodleId() {
        return refrencemodleId;
    }

    public void setRefrencemodleId(long refrencemodleId) {
        this.refrencemodleId = refrencemodleId;
    }

    public JSONObject getStepRespJson() {
        return stepRespJson;
    }

    public void setStepRespJson(JSONObject stepRespJson) {
        this.stepRespJson = stepRespJson;
    }

    public String getInputPins() {
        return inputPins;
    }

    public void setInputPins(String inputPins) {
        this.inputPins = inputPins;
    }

    public String getOutputPins() {
        return outputPins;
    }

    public void setOutputPins(String outputPins) {
        this.outputPins = outputPins;
    }

    public float getEffectRatio() {
        return effectRatio;
    }

    public void setEffectRatio(float effectRatio) {
        this.effectRatio = effectRatio;
    }
}
