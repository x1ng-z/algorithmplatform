package hs.algorithmplatform.entity.model.customizemodle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import hs.algorithmplatform.entity.DTO.python.respon.DmvData;
import hs.algorithmplatform.entity.DTO.python.respon.PythonData;
import hs.algorithmplatform.entity.DTO.python.respon.PythonRespon;
import hs.algorithmplatform.entity.model.BaseModleImp;
import hs.algorithmplatform.entity.model.BaseModlePropertyImp;
import hs.algorithmplatform.entity.model.Modle;
import hs.algorithmplatform.entity.model.ModleProperty;
import hs.algorithmplatform.entity.model.controlmodle.MPCModle;
import hs.algorithmplatform.entity.model.controlmodle.PIDModle;
import hs.algorithmplatform.entity.model.filtermodle.FilterModle;
import hs.algorithmplatform.entity.model.iomodle.INModle;
import hs.algorithmplatform.entity.model.iomodle.OUTModle;
import hs.algorithmplatform.pydriver.command.CommandImp;
import hs.algorithmplatform.pydriver.session.PySession;
import hs.algorithmplatform.pydriver.session.PySessionManager;
import hs.algorithmplatform.utils.bridge.ExecutePythonBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/9 11:28
 */
public class CUSTOMIZEModle extends BaseModleImp {
    private Logger logger = LoggerFactory.getLogger(CUSTOMIZEModle.class);
    public static Pattern scriptpattern = Pattern.compile("^(.*).py$");
    /**
     * memery
     */
//    private boolean javabuildcomplet = false;//java控制模型是构建完成？
//    private boolean pythonbuildcomplet = false;//python的控制模型是否构建完成
//    private boolean iscomputecomplete = false;//运算是否完成
    private String datasource;
    private Map<Integer, BaseModlePropertyImp> indexproperties;//key=modleid
    private PySessionManager pySessionManager;
    private ExecutePythonBridge executepythonbridge;
    private String pyproxyexecute;
    private String port;


    public void toBeRealModle(PySessionManager pySessionManager, String nettyport, String pyproxyexecute) {
        this.port = nettyport;
        this.pyproxyexecute = pyproxyexecute;
        this.pySessionManager = pySessionManager;
    }


    @Override
    public void connect() {
        executepythonbridge.execute();

    }

    @Override
    public void reconnect() {
        PySession mpcpySession = pySessionManager.getSpecialSession(getModleId(), noscripNametail());
        if (mpcpySession != null) {
            JSONObject json = new JSONObject();
            json.put("msg", "stop");
            try {
                mpcpySession.getCtx().writeAndFlush(CommandImp.STOP.build(json.toJSONString().getBytes("utf-8"), getModleId()));
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
            pySessionManager.removeSessionModule(mpcpySession.getCtx()).getCtx().close();
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
        executepythonbridge.stop();
        executepythonbridge.execute();

        mpcpySession = pySessionManager.getSpecialSession(getModleId(), noscripNametail());
//        simulatepySession = pySessionManager.getSpecialSession(getModleId(), simulatorscript);
        int trycheckcount = 5;
        while ((trycheckcount-- > 0) && (mpcpySession == null /**|| simulatepySession == null*/)) {
            //等待连接上来
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            mpcpySession = pySessionManager.getSpecialSession(getModleId(), noscripNametail());
//            simulatepySession = pySessionManager.getSpecialSession(getModleId(), simulatorscript);
        }
    }

    @Override
    public void destory() {
        getCancelrun().set(true);
        PySession pySession = pySessionManager.getSpecialSession(getModleId(), noscripNametail());
        if (pySession != null) {
            JSONObject json = new JSONObject();
            json.put("msg", "stop");
            try {
                pySession.getCtx().writeAndFlush(CommandImp.STOP.build(json.toJSONString().getBytes("utf-8"), getModleId()));
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
        }
        executepythonbridge.stop();
    }

    @Override
    public void docomputeprocess() {
        PySession pySession = pySessionManager.getSpecialSession(getModleId(), noscripNametail());
        //是否断线
        if (pySession == null) {
            int retry = 3;
            while (retry-- > 0 && (null == pySession)) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
                pySession = pySessionManager.getSpecialSession(getModleId(), noscripNametail());
            }
            if (null == pySession) {
                reconnect();
            }
        }


        if (pySession != null) {
            JSONObject scriptinputcontext = new JSONObject();
            for (ModleProperty modleProperty : propertyImpList) {
                BaseModlePropertyImp baseModlePropertyImp = (BaseModlePropertyImp) modleProperty;

                if (baseModlePropertyImp.getPindir().equals(ModleProperty.PINDIRINPUT)) {
                    JSONObject invalue = new JSONObject();
                    invalue.put("value", baseModlePropertyImp.getValue());
                    scriptinputcontext.put(baseModlePropertyImp.getModlePinName(), invalue);
                }
            }
            try {
                setModlerunlevel(BaseModleImp.RUNLEVEL_RUNNING);
                pySession.getCtx().writeAndFlush(CommandImp.PARAM.build(scriptinputcontext.toJSONString().getBytes("utf-8"), getModleId()));
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
        }


    }


    @Override
    public JSONObject inprocess() {
        for (ModleProperty modleProperty : propertyImpList) {
            BaseModlePropertyImp customizeinproperty = (BaseModlePropertyImp) modleProperty;
            if (customizeinproperty.getPindir().equals(ModleProperty.PINDIRINPUT)) {
                if (customizeinproperty.getResource().getString("resource").equals(ModleProperty.SOURCE_TYPE_CONSTANT)) {
                    customizeinproperty.setValue(customizeinproperty.getResource().getDouble("value"));
                }

            }
        }
        return null;
    }

    /**
     * 解析计算后的数据，并且将数据设置到输出引脚上
     *
     * @param computedata 里面的key是引脚的名称，值是value比如{
     *                    'pintag':{'value':1.2}
     *                    }
     */
    @Override
    public JSONObject computresulteprocess(JSONObject computedata) {

        for (ModleProperty modleProperty : propertyImpList) {
            BaseModlePropertyImp baseModlePropertyImp = (BaseModlePropertyImp) modleProperty;
            if (baseModlePropertyImp.getPindir().equals(ModleProperty.PINDIROUTPUT)) {
                JSONObject jsonObject = computedata.getJSONObject("data").getJSONObject(baseModlePropertyImp.getModlePinName());
                if (jsonObject != null) {
                    baseModlePropertyImp.setValue(jsonObject.getDouble("value"));
                }
            }

        }

        return null;
    }

    @Override
    public void outprocess(JSONObject outdata) {
        setModlerunlevel(BaseModleImp.RUNLEVEL_RUNCOMPLET);
        setActivetime(Instant.now());

        otherApcPlantRespon(200);
    }

    private void handler() {
        do {
            docomputeprocess();
            try {
                if (getModlerunlevel() == BaseModleImp.RUNLEVEL_INITE) {
                    TimeUnit.MILLISECONDS.sleep(50);
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        } while (getModlerunlevel() == BaseModleImp.RUNLEVEL_INITE && (!getCancelrun().get()));

        /***
         * docomputeprocess返回的可能
         * //            *1运行完成 因为切手动/java构建失败，短路 --》》返回运行结果
         * //             * 2运行初始状态:python构建未返回(因为自旋可能不会存在)、未连接   -》》继续自旋运行
         * //             * 3python运行失败:python脚本调用报错--》》在python计算结果反馈的线程中返回返回运行结果
         * //             * 4进入进行计算状态--》》在python计算结果反馈的线程中返回
         * */
        if (getModlerunlevel() == BaseModleImp.RUNLEVEL_PYTHONFAILD) {
            //计算或者构建失败，反馈结果
            otherApcPlantRespon(123456);
        }

    }

    /**
     * 算法平台单独调用
     */

    public synchronized void otherApcPlanteRunonce(DeferredResult<String> deferredResult) {
        setModlerunlevel(BaseModleImp.RUNLEVEL_INITE);
        getSyneventLinkedBlockingQueue().offer(deferredResult);

        //父节点全部运行完成的条件下，如果mpc运行处于初始化状态下或者mpcModle不为空的时候，simultor也是运行状态处于初始状态下，就达到运算条件
//        if (((getModlerunlevel() == BaseModleImp.RUNLEVEL_INITE) /**|| (mpcModle.getSimulatControlModle() != null ? (mpcModle.getSimulatControlModle().getModlerunlevel() == BaseModleImp.RUNLEVEL_INITE) : true)*/)) {
        //根节点设置开始运行时间
        boolean fristtime = true;
        inprocess();
        //no cancle and no complete
        while ((!getCancelrun().get()) && (getModlerunlevel() != BaseModleImp.RUNLEVEL_RUNCOMPLET)) {

            //first  or running and disconnect
            if (fristtime || ((BaseModleImp.RUNLEVEL_RUNNING == getModlerunlevel()) && (getMySession() == null))) {
                if ((BaseModleImp.RUNLEVEL_RUNNING == getModlerunlevel()) && (getMySession() == null)) {
                    logger.info("modleid=" + getModleId() + " wait compupte result but it disconnect");
                }
                fristtime = false;
                handler();
            }

        }
    }

    public PySession getMySession() {
        return pySessionManager.getSpecialSession(getModleId(), noscripNametail());
    }


    /**
     * {"data":{mv:1.2,partkp:0.1,partki:0.2,partkd}}，里面的partkp,partki和partkd为方便调试模型使用，放置在前端PID调试模块进行展示，展示方式以列表方式显示即可
     * <p>
     * 200 ok
     * 123456 error
     *
     */
    @Override
    public void otherApcPlantRespon(int status) {
        PythonRespon pythonRespon = new PythonRespon();
        pythonRespon.setMessage(getErrormsg());
        pythonRespon.setStatus(status);


        PythonData pythonData = new PythonData();
        pythonRespon.setData(pythonData);
        List<DmvData> dmvDatalist = new ArrayList<>();
        pythonData.setMvData(dmvDatalist);
        try {

            for (ModleProperty modleProperty : propertyImpList) {
                BaseModlePropertyImp baseModlePropertyImp = (BaseModlePropertyImp) modleProperty;
                if (baseModlePropertyImp.getPindir().equals(ModleProperty.PINDIROUTPUT)) {
                    DmvData dmvData = new DmvData();
                    dmvData.setPinname(baseModlePropertyImp.getModlePinName());
                    dmvData.setValue(baseModlePropertyImp.getValue() == null ? 0 : baseModlePropertyImp.getValue());
                    dmvDatalist.add(dmvData);
                }

            }

            DeferredResult<String> deferredResult = null;
            deferredResult = getSyneventLinkedBlockingQueue().poll();
            if (deferredResult != null) {
                deferredResult.setResult(JSON.toJSONString(pythonRespon));
            }
        } finally {
            //重置
            setModlerunlevel(MPCModle.RUNLEVEL_RUNCOMPLET);
        }

    }


    @Override
    public void init() {
        indexproperties = new HashMap<>();
        for (ModleProperty modleProperty : propertyImpList) {
            BaseModlePropertyImp baseModlePropertyImp = (BaseModlePropertyImp) modleProperty;
            indexproperties.put(baseModlePropertyImp.getModlepinsId(), baseModlePropertyImp);
        }

        String filterpath = System.getProperty("user.dir") + "\\" + pyproxyexecute;

        executepythonbridge = new ExecutePythonBridge(filterpath, "127.0.0.1", port, noscripNametail(), getModleId() + "");


    }


    /****db****/
    private String customizepyname;
    /*********/
    private List<ModleProperty> propertyImpList;


    public List<ModleProperty> getPropertyImpList() {
        return propertyImpList;
    }

    public void setPropertyImpList(List<ModleProperty> propertyImpList) {
        this.propertyImpList = propertyImpList;
    }

    public String getCustomizepyname() {
        return customizepyname;
    }

    public void setCustomizepyname(String customizepyname) {
        this.customizepyname = customizepyname;
    }


    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    public Map<Integer, BaseModlePropertyImp> getIndexproperties() {
        return indexproperties;
    }

    public void setIndexproperties(Map<Integer, BaseModlePropertyImp> indexproperties) {
        this.indexproperties = indexproperties;
    }

    public PySessionManager getPySessionManager() {
        return pySessionManager;
    }

    public void setPySessionManager(PySessionManager pySessionManager) {
        this.pySessionManager = pySessionManager;
    }


    public String getPyproxyexecute() {
        return pyproxyexecute;
    }

    public void setPyproxyexecute(String pyproxyexecute) {
        this.pyproxyexecute = pyproxyexecute;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String noscripNametail() {
        Matcher matcher = scriptpattern.matcher(customizepyname);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

}
