package hs.algorithmplatform.entity.model.controlmodle;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import hs.algorithmplatform.entity.DTO.pid.respon.PidData;
import hs.algorithmplatform.entity.DTO.pid.respon.PidRespon;
import hs.algorithmplatform.entity.DTO.pid.respon.PidmvData;
import hs.algorithmplatform.entity.Project;
import hs.algorithmplatform.entity.model.BaseModleImp;
import hs.algorithmplatform.entity.model.BaseModlePropertyImp;
import hs.algorithmplatform.entity.model.Modle;
import hs.algorithmplatform.entity.model.ModleProperty;
import hs.algorithmplatform.entity.model.customizemodle.CUSTOMIZEModle;
import hs.algorithmplatform.entity.model.filtermodle.FilterModle;
import hs.algorithmplatform.entity.model.iomodle.INModle;
import hs.algorithmplatform.entity.model.iomodle.OUTModle;
import hs.algorithmplatform.entity.model.modlerproerty.MPCModleProperty;
import hs.algorithmplatform.pydriver.command.CommandImp;
import hs.algorithmplatform.pydriver.session.PySession;
import hs.algorithmplatform.pydriver.session.PySessionManager;
import hs.algorithmplatform.utils.bridge.ExecutePythonBridge;
import hs.algorithmplatform.utils.help.Tool;
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

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/9 11:17
 */
public class PIDModle extends BaseModleImp {
    private Logger logger = LoggerFactory.getLogger(PIDModle.class);

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
    private String pidscript;

    private double backpartkp;
    private double backpartki;
    private double backpartkd;

    public void toBeRealModle(PySessionManager pySessionManager, String pidscript, String nettyport, String pyproxyexecute) {
        this.pidscript = pidscript;
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
        PySession mpcpySession = pySessionManager.getSpecialSession(getModleId(), pidscript);
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
        setModlerunlevel(BaseModleImp.RUNLEVEL_INITE);
        executepythonbridge.execute();


        mpcpySession = pySessionManager.getSpecialSession(getModleId(), pidscript);
//        simulatepySession = pySessionManager.getSpecialSession(getModleId(), simulatorscript);
        int trycheckcount = 5;
        while ((trycheckcount-- > 0) && (mpcpySession == null /**|| simulatepySession == null*/)) {
            //等待连接上来
            try {
                TimeUnit.MILLISECONDS.sleep(500);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
            mpcpySession = pySessionManager.getSpecialSession(getModleId(), pidscript);
//            simulatepySession = pySessionManager.getSpecialSession(getModleId(), simulatorscript);
        }
    }

    @Override
    public void destory() {
        PySession pySession = pySessionManager.getSpecialSession(getModleId(), pidscript);
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


    //模型短路
    private void modleshortcircuit() {
        BaseModlePropertyImp mvinputpin = Tool.selectmodleProperyByPinname(ModleProperty.TYPE_PIN_MV, propertyImpList, ModleProperty.PINDIRINPUT);
        BaseModlePropertyImp mvoutputpin = Tool.selectmodleProperyByPinname(ModleProperty.TYPE_PIN_MV, propertyImpList, ModleProperty.PINDIROUTPUT);
        if ((mvinputpin != null) && (mvoutputpin != null)) {
            JSONObject fakecomputedata = new JSONObject();
            JSONObject data = new JSONObject();
            data.put("value", mvinputpin.getValue());
            data.put("partkp", 0f);
            data.put("partki", 0f);
            data.put("partkd", 0f);
            backpartkp = 0;
            backpartki = 0;
            backpartkd = 0;
            JSONObject pindata = new JSONObject();
            pindata.put(mvoutputpin.getModlePinName(), data);
            fakecomputedata.put("data", pindata);
            computresulteprocess(null, fakecomputedata);
            outprocess(null, null);
            setAutovalue(0);
        }

    }

    @Override
    public void docomputeprocess() {

        BaseModlePropertyImp autopin = Tool.selectmodleProperyByPinname(ModleProperty.TYPE_PIN_MODLE_AUTO, propertyImpList, ModleProperty.PINDIRINPUT);

        if ((autopin != null)) {
            if (autopin.getValue() == 0) {
                //把输入的mv直接丢给输出mv
                modleshortcircuit();
                return;
            } else if ((autopin.getValue() != 0) && (getAutovalue() == 0)) {
                setAutovalue(1);
                reconnect();
            }

        }


        PySession pySession = pySessionManager.getSpecialSession(getModleId(), pidscript);

        //是否断线
        if (pySession == null) {
            int retry = 3;
            while (retry-- > 0 && (null == pySession)) {
                try {
                    TimeUnit.MILLISECONDS.sleep(1000);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
                pySession = pySessionManager.getSpecialSession(getModleId(), pidscript);
            }
            if (null == pySession) {
//                System.out.println("$$reconnect*********");
                reconnect();
//                System.out.println("###reconnect*********");

            }
        }


        if (pySession != null) {
            JSONObject scriptinputcontext = new JSONObject();

            JSONObject INjson = new JSONObject();
            scriptinputcontext.put("IN1", INjson);

            JSONObject OUTjson = new JSONObject();
            scriptinputcontext.put("OUT1", OUTjson);
            for (ModleProperty modleProperty : propertyImpList) {
                BaseModlePropertyImp baseModlePropertyImp = (BaseModlePropertyImp) modleProperty;

                if (baseModlePropertyImp.getPindir().equals(ModleProperty.PINDIRINPUT)) {
                    JSONObject invalue = new JSONObject();
                    invalue.put("value", baseModlePropertyImp.getValue());
                    INjson.put(baseModlePropertyImp.getModlePinName(), invalue);

                    if (baseModlePropertyImp.getModlePinName().equals(ModleProperty.TYPE_PIN_MV)) {
                        JSONObject inmvdmvHighnealue = new JSONObject();
                        inmvdmvHighnealue.put("value", ((MPCModleProperty) baseModlePropertyImp).getDmvHigh());
                        INjson.put("dmvHigh", inmvdmvHighnealue);

                        JSONObject inmvdmvLownealue = new JSONObject();
                        inmvdmvLownealue.put("value", ((MPCModleProperty) baseModlePropertyImp).getDmvLow());
                        INjson.put("dmvLow", inmvdmvLownealue);
                    }

                } else if (baseModlePropertyImp.getPindir().equals(ModleProperty.PINDIROUTPUT)) {
                    JSONObject outvalue = new JSONObject();
                    OUTjson.put(baseModlePropertyImp.getModlePinName(), outvalue);
                    outvalue.put("pinName", baseModlePropertyImp.getModlePinName());
                }
            }
            try {
                setModlerunlevel(BaseModleImp.RUNLEVEL_RUNNING);
                logger.info("scriptinputcontext:"+scriptinputcontext.toJSONString());
                pySession.getCtx().writeAndFlush(CommandImp.PARAM.build(scriptinputcontext.toJSONString().getBytes("utf-8"), getModleId()));
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }
        }


    }

    @Override
    public JSONObject inprocess(Project project) {
        for (ModleProperty modleProperty : propertyImpList) {
            BaseModlePropertyImp pidinproperty = (BaseModlePropertyImp) modleProperty;
            if (pidinproperty.getPindir().equals(ModleProperty.PINDIRINPUT)) {
                if (pidinproperty.getResource().getString("resource").equals(ModleProperty.SOURCE_TYPE_CONSTANT)) {
                    pidinproperty.setValue(pidinproperty.getResource().getDouble("value"));
                } else if (pidinproperty.getResource().getString("resource").equals(ModleProperty.SOURCE_TYPE_MODLE)) {
                    int modleId = pidinproperty.getResource().getInteger("modleId");
                    int modlepinsId = pidinproperty.getResource().getInteger("modlepinsId");

                    Modle modle = project.getIndexmodles().get(modleId);
                    if (modle != null) {
                        if (modle instanceof MPCModle) {
                            MPCModle mpcModle = (MPCModle) modle;
                            BaseModlePropertyImp baseModlePropertyImp = mpcModle.getIndexproperties().get(modlepinsId);
                            pidinproperty.setValue(baseModlePropertyImp.getValue());
                        } else if (modle instanceof PIDModle) {
                            PIDModle pidModle = (PIDModle) modle;
                            BaseModlePropertyImp baseModlePropertyImp = pidModle.getIndexproperties().get(modlepinsId);
                            pidinproperty.setValue(baseModlePropertyImp.getValue());
                        } else if (modle instanceof CUSTOMIZEModle) {
                            CUSTOMIZEModle customizeModle = (CUSTOMIZEModle) modle;
                            BaseModlePropertyImp baseModlePropertyImp = customizeModle.getIndexproperties().get(modlepinsId);
                            pidinproperty.setValue(baseModlePropertyImp.getValue());
                        } else if (modle instanceof FilterModle) {
                            FilterModle filterModle = (FilterModle) modle;
                            BaseModlePropertyImp baseModlePropertyImp = filterModle.getIndexproperties().get(modlepinsId);
                            pidinproperty.setValue(baseModlePropertyImp.getValue());
                        } else if (modle instanceof INModle) {
                            INModle inModle = (INModle) modle;
                            BaseModlePropertyImp baseModlePropertyImp = inModle.getIndexproperties().get(modlepinsId);
                            pidinproperty.setValue(baseModlePropertyImp.getValue());
                        } else if (modle instanceof OUTModle) {
                            OUTModle outModle = (OUTModle) modle;
                            BaseModlePropertyImp baseModlePropertyImp = outModle.getIndexproperties().get(modlepinsId);
                            pidinproperty.setValue(baseModlePropertyImp.getValue());
                        }

                    }

                }

            }
        }
        return null;
    }

    //解析计算输出结果，并且赋值给输出引脚
    @Override
    public JSONObject computresulteprocess(Project project, JSONObject computedata) {
        for (ModleProperty modleProperty : propertyImpList) {
            BaseModlePropertyImp pidinproperty = (BaseModlePropertyImp) modleProperty;
            if (pidinproperty.getPindir().equals(ModleProperty.PINDIROUTPUT)) {
                JSONObject outpinjsoncontext = computedata.getJSONObject("data").getJSONObject(pidinproperty.getModlePinName());
                if (outpinjsoncontext != null) {
                    pidinproperty.setValue(outpinjsoncontext.getDouble("value"));
                    if (outpinjsoncontext.containsKey("partkp") && outpinjsoncontext.containsKey("partki") && outpinjsoncontext.containsKey("partkd")) {
                        setErrormsg("partkp:" + Tool.getSpecalScale(4, outpinjsoncontext.getDouble("partkp")) + "\n"
                                + "partki:" + Tool.getSpecalScale(4, outpinjsoncontext.getDouble("partki")) + "\n"
                                + "partkd:" + Tool.getSpecalScale(4, outpinjsoncontext.getDouble("partkd"))
                        );
                        backpartkp = outpinjsoncontext.getDouble("partkp");
                        backpartki = outpinjsoncontext.getDouble("partki");
                        backpartkd = outpinjsoncontext.getDouble("partkd");
                    }

                }
            }
        }

        return null;
    }

    @Override
    public void outprocess(Project project, JSONObject outdata) {
        setModlerunlevel(BaseModleImp.RUNLEVEL_RUNCOMPLET);
        setActivetime(Instant.now());

        otherApcPlantRespon(200);
    }

    /**
     * 算法平台单独调用
     */

    public synchronized void otherApcPlanteRunonce(DeferredResult<String> deferredResult) {

        getSyneventLinkedBlockingQueue().offer(deferredResult);

//        if (ismpcmodleruncomplet()) {
//            return;
//        }
        //父节点全部运行完成的条件下，如果mpc运行处于初始化状态下或者mpcModle不为空的时候，simultor也是运行状态处于初始状态下，就达到运算条件
//        if (((getModlerunlevel() == BaseModleImp.RUNLEVEL_INITE) /**|| (mpcModle.getSimulatControlModle() != null ? (mpcModle.getSimulatControlModle().getModlerunlevel() == BaseModleImp.RUNLEVEL_INITE) : true)*/)) {
        //根节点设置开始运行时间
        inprocess(null);
        do {
            docomputeprocess();
            try {
                if (getModlerunlevel() == BaseModleImp.RUNLEVEL_INITE) {
                    TimeUnit.MILLISECONDS.sleep(50);
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        } while (getModlerunlevel() == BaseModleImp.RUNLEVEL_INITE);

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
     * {"data":{mv:1.2,partkp:0.1,partki:0.2,partkd}}，里面的partkp,partki和partkd为方便调试模型使用，放置在前端PID调试模块进行展示，展示方式以列表方式显示即可
     * <p>
     * 200 ok
     * 123456 error
     */
    @Override
    public void otherApcPlantRespon(int status) {

        try {
            PidRespon pidRespon = new PidRespon();
            PidData pidData = new PidData();
            pidRespon.setMessage(getErrormsg());
            pidRespon.setStatus(status);
            pidRespon.setData(pidData);

            pidData.setPartkp(backpartkp);
            pidData.setPartki(backpartki);
            pidData.setPartkd(backpartkd);

            List<PidmvData> dmvDataList = new ArrayList<>();
            pidData.setMvData(dmvDataList);
            for (ModleProperty modleProperty : propertyImpList) {
                BaseModlePropertyImp pidinproperty = (BaseModlePropertyImp) modleProperty;
                if (pidinproperty.getPindir().equals(ModleProperty.PINDIROUTPUT)) {
                    PidmvData dmvData = new PidmvData();
                    dmvData.setPinname(pidinproperty.getModlePinName());
                    dmvData.setValue(pidinproperty.getValue());
                    dmvDataList.add(dmvData);
                    break;
                }
            }

            DeferredResult<String> deferredResult = null;

            deferredResult = getSyneventLinkedBlockingQueue().poll();
            if (deferredResult != null) {
                deferredResult.setResult(JSON.toJSONString(pidRespon));
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
        executepythonbridge = new ExecutePythonBridge(filterpath, "127.0.0.1", port, pidscript, getModleId() + "");
    }


    /******db****/

    private List<ModleProperty> propertyImpList;


    public List<ModleProperty> getPropertyImpList() {
        return propertyImpList;
    }

    public void setPropertyImpList(List<ModleProperty> propertyImpList) {
        this.propertyImpList = propertyImpList;
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

    public String getPidscript() {
        return pidscript;
    }

    public void setPidscript(String pidscript) {
        this.pidscript = pidscript;
    }

//    public boolean isIscomputecomplete() {
//        return iscomputecomplete;
//    }
//
//    public void setIscomputecomplete(boolean iscomputecomplete) {
//        this.iscomputecomplete = iscomputecomplete;
//    }
}
