package hs.algorithmplatform.entity.DTO.dmc.respon;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/9 11:19
 * {“data”:{"output_data":{"mv":{jsonarray,[按照输出设置中设置的输出引脚进行输出，形如mv1:1.2],[]},
 * "pvpredict":{jsonarray,[里面的内容为pvpinname:pvi其中i引脚名称后面的序号,
 * predictorder:[后续N步pv的预测值，用于绘制预测曲线]],[]..}},status:200,message:"..."},
 * dmv:{"inputpinname":mvi其中i为mv引脚序号,
 * outputpinname:pvi其中i为pv引脚序号,value:0.1}}
 */

public class DmcRespon {
    @JSONField(name = "data")
    private DmcData data;
    private int status;
    private String message;

    public DmcData getData() {
        return data;
    }

    public void setData(DmcData data) {
        this.data = data;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
