package hs.algorithmplatform.entity.DTO.python.respon;

import java.util.List;
import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/10 0:25
 */
public class PythonData {

    private List<DmvData> mvData;

    public List<DmvData> getMvData() {
        return mvData;
    }

    public void setMvData(List<DmvData> mvData) {
        this.mvData = mvData;
    }
}
