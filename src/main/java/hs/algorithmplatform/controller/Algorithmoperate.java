package hs.algorithmplatform.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import hs.algorithmplatform.entity.DTO.dmc.request.DmcBasemodleparam;
import hs.algorithmplatform.entity.DTO.dmc.request.DmcModleAdapter;
import hs.algorithmplatform.entity.DTO.pid.request.PidModleAdapter;
import hs.algorithmplatform.entity.DTO.python.request.PythonAdapter;
import hs.algorithmplatform.entity.model.BaseModleImp;
import hs.algorithmplatform.entity.model.Modle;
import hs.algorithmplatform.entity.model.controlmodle.MPCModle;
import hs.algorithmplatform.entity.model.controlmodle.PIDModle;
import hs.algorithmplatform.entity.model.customizemodle.CUSTOMIZEModle;
import hs.algorithmplatform.manager.ModleManager;
import hs.algorithmplatform.pydriver.pyproxyserve.IOServer;
import hs.algorithmplatform.pydriver.session.PySessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import javax.validation.Valid;
import java.util.concurrent.ExecutorService;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/8 11:16
 */
@RestController
@RequestMapping("/algorithm")
public class Algorithmoperate {
    private Logger logger = LoggerFactory.getLogger(Algorithmoperate.class);

    private IOServer ioServer;
    private PySessionManager pySessionManager;
    private String pyproxyexecute;
    private int mpcpinnumber;
    private String filterscript;
    private String pidscript;
    private String simulatorscript;
    private String mpcscrip;
    private String oceandir;
    private String pydriverport;
    private ExecutorService executethreadpool;
    private ModleManager modleManager;

    public Algorithmoperate(IOServer ioServer,
                            PySessionManager pySessionManager,
                            ModleManager modleManager,
                            @Value("${pyproxyexecute}") String pyproxyexecute,
                            @Value("${mpcpinnumber}") int mpcpinnumber,
                            @Value("${filterscript}") String filterscript,
                            @Value("${pidscript}") String pidscript,
                            @Value("${simulatorscript}") String simulatorscript,
                            @Value("${mpcscrip}") String mpcscrip,
                            @Value("${oceandir}") String oceandir,
                            @Value("${pydriverport}") String pydriverport,
                            @Qualifier("executethreadpool")
                                    ExecutorService executethreadpool) {
        this.ioServer = ioServer;
        this.pySessionManager = pySessionManager;
        this.pyproxyexecute = pyproxyexecute;
        this.mpcpinnumber = mpcpinnumber;
        this.filterscript = filterscript;
        this.pidscript = pidscript;
        this.simulatorscript = simulatorscript;
        this.mpcscrip = mpcscrip;
        this.oceandir = oceandir;
        this.pydriverport = pydriverport;
        this.executethreadpool = executethreadpool;
        this.modleManager = modleManager;
    }


    /**
     * {"input_data":{"basemodleparam":{json,基本模型参数详细见上},"pv":{jsonarray,形如[pv设置参数],[]...},"mv":{jsonarray,[mv设置参数],[]..},"ff":{jsonarray,[ff设置参数],[]..},"modle":{jsonarray,[模型设置参数],[]..},“outputparam”{jsonarray,[输出设置],[],..}}}
     */

    @RequestMapping(path = "/dmc/buildrun", consumes = "application/json")
    public DeferredResult<String> dmcbuildrun(@RequestBody DmcModleAdapter dmcModleAdapter, BindingResult bindingresult) {
        logger.info("/dmc/buildrun" + JSON.toJSONString(dmcModleAdapter));
        DeferredResult<String> result = new DeferredResult<String>(60 * 1000L);

        long modleid = dmcModleAdapter.getBasemodelparam().getModelid();
        Modle modle = modleManager.getspecialModle(modleid);
        if (modle == null) {
            modle = dmcModleAdapter.covertormodle();
            modleManager.activeModle(modle);
            logger.info("first time call");
        } else {
            MPCModle mpcModle = (MPCModle) modle;
            if (mpcModle.getModlerunlevel() == BaseModleImp.RUNLEVEL_RUNCOMPLET) {
//                mpcModle.setModlerunlevel(BaseModleImp.RUNLEVEL_INITE);
                dmcModleAdapter.updatemodlevalue((MPCModle) modle);
                logger.info("some time1 call");
            } else if ((mpcModle.getModlerunlevel() == BaseModleImp.RUNLEVEL_RUNNING) && (null == pySessionManager.getSpecialSession(modleid, mpcModle.getMpcscript()))) {
//                mpcModle.setModlerunlevel(BaseModleImp.RUNLEVEL_INITE);
                dmcModleAdapter.updatemodlevalue((MPCModle) modle);
                logger.info("some time2 call");
            } else {
                JSONObject res = new JSONObject();
                res.put("message", "other connector call algorithm");
                res.put("status", 123456);
                result.setResult(res.toJSONString());
                return result;
            }

        }

        Modle finalModle = modle;
        result.onTimeout(() -> {
            logger.info("modleid=" + modleid + " 调用超时");
            ((BaseModleImp) finalModle).getSyneventLinkedBlockingQueue().poll();
        });

        result.onCompletion(() -> {
            logger.info("modleid=" + modleid + "调用完成");
        });

        Modle finalModle1 = modle;
        executethreadpool.execute(new Runnable() {
            @Override
            public void run() {
                ((MPCModle) finalModle1).otherApcPlanteRunonce(result);
            }
        });
        return result;
    }


    @RequestMapping(path = "/fpid/buildrun", consumes = "application/json")
    public DeferredResult<String> fpidrun(@Valid @RequestBody PidModleAdapter pidModleAdapter, BindingResult bindingresult) {
        logger.info("/fpid/buildrun" + JSON.toJSONString(pidModleAdapter));
        DeferredResult<String> result = new DeferredResult<String>(60 * 1000L);

        long modleid = pidModleAdapter.getBasemodelparam().getModelid();
        Modle modle = modleManager.getspecialModle(modleid);
        if (modle == null) {
            modle = pidModleAdapter.covertormodle();
            modleManager.activeModle(modle);
        } else {
            PIDModle pidmodle = (PIDModle) modle;
            if (pidmodle.getModlerunlevel() == BaseModleImp.RUNLEVEL_RUNCOMPLET) {
//                pidmodle.setModlerunlevel(BaseModleImp.RUNLEVEL_INITE);
                pidModleAdapter.updatemodlevalue((PIDModle) modle);
            } else if ((pidmodle.getModlerunlevel() == BaseModleImp.RUNLEVEL_RUNNING) && (null == pySessionManager.getSpecialSession(modleid, pidmodle.getPidscript()))) {
//                pidmodle.setModlerunlevel(BaseModleImp.RUNLEVEL_INITE);
                pidModleAdapter.updatemodlevalue((PIDModle) modle);
            } else {
                JSONObject res = new JSONObject();
                res.put("message", "other connector call algorithm");
                res.put("status", 123456);
                result.setResult(res.toJSONString());
                return result;
            }

        }

        Modle finalModle = modle;
        result.onTimeout(() -> {
            logger.info("modleid=" + modleid + " 调用超时");
            ((BaseModleImp) finalModle).getSyneventLinkedBlockingQueue().poll();
        });

        result.onCompletion(() -> {
            logger.info("modleid=" + modleid + "调用完成");
        });

        Modle finalModle1 = modle;
        executethreadpool.execute(new Runnable() {
            @Override
            public void run() {
                ((PIDModle) finalModle1).otherApcPlanteRunonce(result);
            }
        });

        return result;
    }


    @RequestMapping(path = "/cpython/buildrun", consumes = "application/json")
    public DeferredResult<String> cpython(@Valid @RequestBody PythonAdapter pythonAdapter, BindingResult bindingresult) {
        logger.info("/cpython/buildrun" + JSON.toJSONString(pythonAdapter));
        DeferredResult<String> result = new DeferredResult<String>(60 * 1000L);

        long modleid = pythonAdapter.getBasemodelparam().getModelid();
        Modle modle = modleManager.getspecialModle(modleid);
        if (modle == null) {
            modle = pythonAdapter.covertormodle();
            modleManager.activeModle(modle);
        } else {
            CUSTOMIZEModle customizeModle = (CUSTOMIZEModle) modle;
            if (customizeModle.getModlerunlevel() == BaseModleImp.RUNLEVEL_RUNCOMPLET) {
//                customizeModle.setModlerunlevel(BaseModleImp.RUNLEVEL_INITE);
                pythonAdapter.updatemodlevalue((CUSTOMIZEModle) modle);
            } else if ((customizeModle.getModlerunlevel() == BaseModleImp.RUNLEVEL_RUNNING) && (null == pySessionManager.getSpecialSession(modleid, customizeModle.noscripNametail()))) {
//                customizeModle.setModlerunlevel(BaseModleImp.RUNLEVEL_INITE);
                pythonAdapter.updatemodlevalue((CUSTOMIZEModle) modle);
            } else {
                JSONObject res = new JSONObject();
                res.put("message", "other connector call algorithm");
                res.put("status", 123456);
                result.setResult(res.toJSONString());
                return result;
            }

        }

        Modle finalModle = modle;
        result.onTimeout(() -> {
            logger.info("modleid=" + modleid + " 调用超时");
            ((BaseModleImp) finalModle).getSyneventLinkedBlockingQueue().poll();
        });

        result.onCompletion(() -> {
            logger.info("modleid=" + modleid + "调用完成");
        });


        Modle finalModle1 = modle;
        executethreadpool.execute(new Runnable() {
            @Override
            public void run() {
                ((CUSTOMIZEModle) finalModle1).otherApcPlanteRunonce(result);
            }
        });
        return result;
    }


    @RequestMapping(path = "/stop/{modleid}")
    public String modlestop(@PathVariable("modleid") long modleid) {
        logger.info("/stop/modleid=" + modleid);
        JSONObject res = new JSONObject();
        Modle modle = modleManager.getspecialModle(modleid);
        if (modle == null) {
            res.put("message", "algorithm isn't run");
            res.put("status", 200);
        } else {
            modleManager.stopAndRemoveModle(modleid);
            res.put("message", "algorithm stop right now");
            res.put("status", 200);
        }
        return res.toJSONString();
    }


    @RequestMapping("/test")
    public DeferredResult<DmcBasemodleparam> test() {
        DeferredResult<DmcBasemodleparam> result = new DeferredResult<DmcBasemodleparam>(60 * 1000L);
        result.setResult(new DmcBasemodleparam());
        return result;
    }
}
