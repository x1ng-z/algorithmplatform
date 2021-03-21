package hs.algorithmplatform.entity.model.filtermodle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import hs.algorithmplatform.entity.Project;
import hs.algorithmplatform.entity.model.BaseModleImp;
import hs.algorithmplatform.entity.model.BaseModlePropertyImp;
import hs.algorithmplatform.entity.model.Modle;
import hs.algorithmplatform.entity.model.ModleProperty;
import hs.algorithmplatform.entity.model.controlmodle.MPCModle;
import hs.algorithmplatform.entity.model.controlmodle.PIDModle;
import hs.algorithmplatform.entity.model.customizemodle.CUSTOMIZEModle;
import hs.algorithmplatform.entity.model.filter.Filter;
import hs.algorithmplatform.entity.model.filter.FirstOrderLagFilter;
import hs.algorithmplatform.entity.model.filter.MoveAverageFilter;
import hs.algorithmplatform.entity.model.iomodle.INModle;
import hs.algorithmplatform.entity.model.iomodle.OUTModle;
import hs.algorithmplatform.pydriver.command.CommandImp;
import hs.algorithmplatform.pydriver.session.PySession;
import hs.algorithmplatform.pydriver.session.PySessionManager;
import hs.algorithmplatform.utils.bridge.ExecutePythonBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/11 12:48
 */
public class FilterModle extends BaseModleImp {
    private Logger logger = LoggerFactory.getLogger(FilterModle.class);

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
    private String filterscript;
    private String port;
    private String pyproxyexecute;


    public void toBeRealModle(PySessionManager pySessionManager, String filterscript,String nettyport, String pyproxyexecute){
        this. port=nettyport;
        this.pyproxyexecute=pyproxyexecute;
        this.pySessionManager=pySessionManager;
        this.filterscript=filterscript;
    }



    @Override
    public void connect() {
        executepythonbridge.execute();
        PySession filterPySession = pySessionManager.getSpecialSession(getModleId(), filterscript);
//        while (filterPySession == null) {
////            logger.info("try");
//            try {
//                TimeUnit.MILLISECONDS.sleep(500);
//            } catch (InterruptedException e) {
//                logger.error(e.getMessage(),e);
//            }
//
//
//        }

    }

    @Override
    public void reconnect() {
        executepythonbridge.stop();
        executepythonbridge.execute();
    }

    @Override
    public void destory() {
        PySession pySession = pySessionManager.getSpecialSession(getModleId(),filterscript);
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

    //发送计算的数据给本模块的python脚本
    @Override
    public void docomputeprocess() {
        PySession pySession = pySessionManager.getSpecialSession(getModleId(),filterscript);
        JSONObject scriptinputcontext = new JSONObject();

        if (pySession != null) {
            JSONObject filterinfo = new JSONObject();
            //设置过滤器的属性
            if (filter instanceof FirstOrderLagFilter) {
                FirstOrderLagFilter orderLagFilter = (FirstOrderLagFilter) filter;
                String filtermethod = orderLagFilter.getFiltermethod();
                double alphe = orderLagFilter.getFilteralphe();
                filterinfo.put("filtermethod", filtermethod);
                filterinfo.put("alphe", alphe);
                scriptinputcontext.put("filterinfo", filterinfo);
            } else if (filter instanceof MoveAverageFilter) {
                MoveAverageFilter moveAverageFilter = (MoveAverageFilter) filter;
                String filtermethod = moveAverageFilter.getFiltermethod();
                int capacity = moveAverageFilter.getFiltercapacity();
                filterinfo.put("filtermethod", filtermethod);
                filterinfo.put("capacity", capacity);
                scriptinputcontext.put("filterinfo", filterinfo);
            }

            //设置过滤器的计算属性
            JSONArray data = new JSONArray();
            for (ModleProperty modleProperty : propertyImpList) {
                BaseModlePropertyImp filteroutproperty = (BaseModlePropertyImp) modleProperty;
                if (filteroutproperty.getPindir().equals(ModleProperty.PINDIROUTPUT)) {
                    JSONObject value = new JSONObject();
                    BaseModlePropertyImp filtinputpin = indexproperties.get(filteroutproperty.getResource().getInteger("modlepinsId"));
                    if (filtinputpin != null) {
                        value.put("value", filtinputpin.getValue());//计算值
                        value.put("outputmodleid", filteroutproperty.getRefmodleId());//输出模型id
                        value.put("outputpropertyid", filteroutproperty.getModlepinsId());//输出模型的输出位号
                        data.add(value);
                    }
                }
            }
            scriptinputcontext.put("data", data);

            try {
                setModlerunlevel(BaseModleImp.RUNLEVEL_RUNNING);
                pySession.getCtx().writeAndFlush(CommandImp.PARAM.build(scriptinputcontext.toJSONString().getBytes("utf-8"), getModleId()));
            } catch (UnsupportedEncodingException e) {
                logger.error(e.getMessage(), e);
            }

        }


    }

    //将上几个模块的输出引脚数据赋值给本模块的输入引脚
    @Override
    public JSONObject inprocess(Project project) {

        for (ModleProperty modleProperty : propertyImpList) {
            BaseModlePropertyImp filterproperty = (BaseModlePropertyImp) modleProperty;
            if (filterproperty.getPindir().equals(ModleProperty.PINDIRINPUT)) {
                int modleId = filterproperty.getResource().getInteger("modleId");
                int modlepinsId = filterproperty.getResource().getInteger("modlepinsId");
                Modle modle = project.getIndexmodles().get(modleId);
                if (modle != null) {
                    if (modle instanceof MPCModle) {
                        MPCModle mpcModle = (MPCModle) modle;
                        BaseModlePropertyImp baseModlePropertyImp = mpcModle.getIndexproperties().get(modlepinsId);
                        filterproperty.setValue(baseModlePropertyImp.getValue());
                    } else if (modle instanceof PIDModle) {
                        PIDModle pidModle = (PIDModle) modle;
                        BaseModlePropertyImp baseModlePropertyImp = pidModle.getIndexproperties().get(modlepinsId);
                        filterproperty.setValue(baseModlePropertyImp.getValue());
                    } else if (modle instanceof CUSTOMIZEModle) {
                        CUSTOMIZEModle customizeModle = (CUSTOMIZEModle) modle;
                        BaseModlePropertyImp baseModlePropertyImp = customizeModle.getIndexproperties().get(modlepinsId);
                        filterproperty.setValue(baseModlePropertyImp.getValue());
                    } else if (modle instanceof FilterModle) {
                        FilterModle filterModle = (FilterModle) modle;
                        BaseModlePropertyImp baseModlePropertyImp = filterModle.getIndexproperties().get(modlepinsId);
                        filterproperty.setValue(baseModlePropertyImp.getValue());
                    } else if (modle instanceof INModle) {
                        INModle inModle = (INModle) modle;
                        BaseModlePropertyImp baseModlePropertyImp = inModle.getIndexproperties().get(modlepinsId);
                        filterproperty.setValue(baseModlePropertyImp.getValue());
                    } else if (modle instanceof OUTModle) {
                        OUTModle outModle = (OUTModle) modle;
                        BaseModlePropertyImp baseModlePropertyImp = outModle.getIndexproperties().get(modlepinsId);
                        filterproperty.setValue(baseModlePropertyImp.getValue());
                    }

                }
            }
        }
        return null;
    }

    /**
     * 处理python脚本处理后的数据，并且将数据赋值给输出引脚
     * OU={1：{
     * 'value':filtevalue ,
     * 'outputmodleid': subdata['outputmodleid'],
     * 'outputpropertyid': subdata['outputpropertyid']
     * }
     * }
     */
    @Override
    public JSONObject computresulteprocess(Project project, JSONObject computedata) {
        for (ModleProperty modleProperty : propertyImpList) {
            BaseModlePropertyImp baseModlePropertyImp = (BaseModlePropertyImp) modleProperty;
            if (baseModlePropertyImp.getPindir().equals(ModleProperty.PINDIROUTPUT)) {
                JSONObject conputeresult = computedata.getJSONObject("data").getJSONObject(baseModlePropertyImp.getModlepinsId() + "");
                if (conputeresult != null) {
                    baseModlePropertyImp.setValue(conputeresult.getDouble("value"));
                }
            }
        }

        return null;
    }

    //不用做如何事情了 computresulteprocess已经将数据赋值给输出引脚了
    @Override
    public void outprocess(Project project, JSONObject outdata) {
        setModlerunlevel(BaseModleImp.RUNLEVEL_RUNCOMPLET);
        setActivetime(Instant.now());
    }

    @Override
    public void init() {
        indexproperties = new HashMap<>();
        for (ModleProperty modleProperty : propertyImpList) {
            BaseModlePropertyImp baseModlePropertyImp = (BaseModlePropertyImp) modleProperty;
            indexproperties.put(baseModlePropertyImp.getModlepinsId(), baseModlePropertyImp);
        }
        String filterpath = System.getProperty("user.dir") + "\\" + pyproxyexecute;
        executepythonbridge = new ExecutePythonBridge(filterpath, "127.0.0.1", port, filterscript, getModleId() + "");
    }

    @Override
    public void otherApcPlantRespon(int status) {

    }


    /**
     * db
     */
    private Filter filter;
    private List<ModleProperty> propertyImpList;

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

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

    public String getFilterscript() {
        return filterscript;
    }

    public void setFilterscript(String filterscript) {
        this.filterscript = filterscript;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getPyproxyexecute() {
        return pyproxyexecute;
    }

    public void setPyproxyexecute(String pyproxyexecute) {
        this.pyproxyexecute = pyproxyexecute;
    }

//    @Override
//    public boolean isIscomputecomplete() {
//        return iscomputecomplete;
//    }
//
//    public void setIscomputecomplete(boolean iscomputecomplete) {
//        this.iscomputecomplete = iscomputecomplete;
//    }
}
