package hs.algorithmplatform.entity.DTO.dmc.respon;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/9 11:22
 *
 * "output_data":{"mv":{jsonarray,[按照输出设置中设置的输出引脚进行输出，形如mv1:1.2],[]},
 *  * "pvpredict":{jsonarray,[里面的内容为pvpinname:pvi其中i引脚名称后面的序号,
 *  * predictorder:[后续N步pv的预测值，用于绘制预测曲线]],[]..}},status:200,message:"..."
 */
public class DmcData {

    private List<MvData> mvData;
    private List<Pvpredict> pvpredict;
    private List<DmvData> dmv;


    public List<MvData> getMvData() {
        return mvData;
    }

    public void setMvData(List<MvData> mvData) {
        this.mvData = mvData;
    }

    public List<Pvpredict> getPvpredict() {
        return pvpredict;
    }

    public void setPvpredict(List<Pvpredict> pvpredict) {
        this.pvpredict = pvpredict;
    }

    public List<DmvData> getDmv() {
        return dmv;
    }

    public void setDmv(List<DmvData> dmv) {
        this.dmv = dmv;
    }
}
