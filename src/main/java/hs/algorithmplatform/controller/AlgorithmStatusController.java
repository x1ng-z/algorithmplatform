package hs.algorithmplatform.controller;

import com.alibaba.fastjson.JSONObject;
import hs.algorithmplatform.entity.VO.ModleStatus;
import hs.algorithmplatform.entity.model.BaseModleImp;
import hs.algorithmplatform.entity.model.Modle;
import hs.algorithmplatform.entity.model.controlmodle.MPCModle;
import hs.algorithmplatform.entity.model.controlmodle.PIDModle;
import hs.algorithmplatform.entity.model.customizemodle.CUSTOMIZEModle;
import hs.algorithmplatform.manager.ModleManager;
import hs.algorithmplatform.pydriver.session.PySession;
import hs.algorithmplatform.pydriver.session.PySessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/5/6 9:44
 */
@RestController
@RequestMapping("/algorithmstatus")
public class AlgorithmStatusController {
    private Logger logger = LoggerFactory.getLogger(AlgorithmStatusController.class);
    @Autowired
    private PySessionManager pySessionManager;
    @Autowired
    private ModleManager modleManager;

    @RequestMapping("/all")
    public String getAllAlgorithmSTatus() {
        List<ModleStatus> result= new ArrayList<>();
        Map<Long, Modle> modleMap = modleManager.getAllModel();
        Map<Integer,String>runstatusmapp=new HashMap<>();
        runstatusmapp.put(BaseModleImp.RUNLEVEL_RUNNING,"正在运行中");
        runstatusmapp.put(BaseModleImp.RUNLEVEL_RUNCOMPLET,"运行完成");
        runstatusmapp.put(BaseModleImp.RUNLEVEL_INITE,"初始状态");
        runstatusmapp.put(BaseModleImp.RUNLEVEL_PYTHONFAILD,"python运行报错");
        runstatusmapp.put(BaseModleImp.RUNLEVEL_DISCONNECT,"断线");
        runstatusmapp.put(BaseModleImp.RUNLEVEL_JAVAMODLEBUILDCOMPLET,"java模型构建完成状态");
        runstatusmapp.put(BaseModleImp.RUNLEVEL_PYTHONMODLEBUILDCOMPLET,"python模型构建完成状态");
        if (!CollectionUtils.isEmpty(modleMap)) {
            modleMap.forEach((k, v) -> {
                ModleStatus modleStatus = new ModleStatus();
                modleStatus.setAlgorithmId(k);
                modleStatus.setAlgorithmName(((BaseModleImp) v).getModleName());
                result.add(modleStatus);
                if (v instanceof MPCModle) {
                    MPCModle mpcModle = (MPCModle) v;
                    PySession session = pySessionManager.getSpecialSession(k, mpcModle.getMpcscript());
                    modleStatus.setRunStatus(runstatusmapp.get(mpcModle.getModlerunlevel()));
                    modleStatus.setIsOnline(session == null ? "离线" : "在线");
                } else if (v instanceof PIDModle) {
                    PIDModle pidModle = (PIDModle) v;
                    PySession session = pySessionManager.getSpecialSession(k, pidModle.getPidscript());
                    modleStatus.setRunStatus(runstatusmapp.get(pidModle.getModlerunlevel()));
                    modleStatus.setIsOnline(session == null ? "离线" : "在线");
                }else if (v instanceof CUSTOMIZEModle) {
                    CUSTOMIZEModle customizeModle = (CUSTOMIZEModle) v;
                    PySession session = pySessionManager.getSpecialSession(k, customizeModle.noscripNametail());
                    modleStatus.setRunStatus(runstatusmapp.get(customizeModle.getModlerunlevel()));
                    modleStatus.setIsOnline(session == null ? "离线" : "在线");
                }

            });
        }
        return JSONObject.toJSONString(result);
    }
}
