package hs.algorithmplatform.entity.model.iomodle;

import com.alibaba.fastjson.JSONObject;

import hs.algorithmplatform.entity.Project;
import hs.algorithmplatform.entity.model.BaseModleImp;
import hs.algorithmplatform.entity.model.BaseModlePropertyImp;
import hs.algorithmplatform.entity.model.Modle;
import hs.algorithmplatform.entity.model.ModleProperty;
import hs.algorithmplatform.entity.model.controlmodle.MPCModle;
import hs.algorithmplatform.entity.model.controlmodle.PIDModle;
import hs.algorithmplatform.entity.model.customizemodle.CUSTOMIZEModle;
import hs.algorithmplatform.entity.model.filtermodle.FilterModle;
import hs.algorithmplatform.utils.httpclient.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/9 11:20
 */
public class OUTModle extends BaseModleImp {
    private Logger logger = LoggerFactory.getLogger(OUTModle.class);

    /**
     * memory
     */
//    private boolean javabuildcomplet = false;//java控制模型是构建完成？
//    private boolean pythonbuildcomplet = false;//python的控制模型是否构建完成
//    private boolean iscomputecomplete = false;//运算是否完成
    private String datasource;
    private Map<Integer, BaseModlePropertyImp> indexproperties;//key=modleid



    public void toBeRealModle(String datasource){
        this.datasource=datasource;
    }

    @Override
    public void connect() {

    }

    @Override
    public void reconnect() {

    }

    @Override
    public void destory() {

    }

    @Override
    public void docomputeprocess() {
        setModlerunlevel(BaseModleImp.RUNLEVEL_RUNNING);
    }



    /***
     * 从上一个模块引脚输出的数据赋值给模块的输入引脚
     * */
    @Override
    public JSONObject inprocess(Project project) {

        for (ModleProperty property : propertyImpList) {
            BaseModlePropertyImp outmodlepin = (BaseModlePropertyImp) property;
            if (outmodlepin.getPindir().equals(ModleProperty.PINDIRINPUT)) {

                int modleId = outmodlepin.getResource().getInteger("modleId");
                int modlepinsId = outmodlepin.getResource().getInteger("modlepinsId");

                Modle modle = project.getIndexmodles().get(modleId);
                if (modle != null) {
                    if (modle instanceof MPCModle) {
                        MPCModle mpcModle = (MPCModle) modle;
                        BaseModlePropertyImp baseModlePropertyImp = mpcModle.getIndexproperties().get(modlepinsId);
                        outmodlepin.setValue(baseModlePropertyImp.getValue());
                    } else if (modle instanceof PIDModle) {
                        PIDModle pidModle = (PIDModle) modle;
                        BaseModlePropertyImp baseModlePropertyImp = pidModle.getIndexproperties().get(modlepinsId);
                        outmodlepin.setValue(baseModlePropertyImp.getValue());
                    } else if (modle instanceof CUSTOMIZEModle) {
                        CUSTOMIZEModle customizeModle = (CUSTOMIZEModle) modle;
                        BaseModlePropertyImp baseModlePropertyImp = customizeModle.getIndexproperties().get(modlepinsId);
                        outmodlepin.setValue(baseModlePropertyImp.getValue());
                    } else if (modle instanceof FilterModle) {
                        FilterModle filterModle = (FilterModle) modle;
                        BaseModlePropertyImp baseModlePropertyImp = filterModle.getIndexproperties().get(modlepinsId);
                        outmodlepin.setValue(baseModlePropertyImp.getValue());
                    } else if (modle instanceof INModle) {
                        INModle inModle = (INModle) modle;
                        BaseModlePropertyImp baseModlePropertyImp = inModle.getIndexproperties().get(modlepinsId);
                        outmodlepin.setValue(baseModlePropertyImp.getValue());
                    } else if (modle instanceof OUTModle) {
                        OUTModle outModle = (OUTModle) modle;
                        BaseModlePropertyImp baseModlePropertyImp = outModle.getIndexproperties().get(modlepinsId);
                        outmodlepin.setValue(baseModlePropertyImp.getValue());
                    }

                }


            }

        }

        return null;
    }

    @Override
    public JSONObject computresulteprocess(Project project,JSONObject computedata) {
        return null;
    }



    /**将本模块的输入引脚输出给本模块的输出引脚，并且将输出数据提交给ocean*/
    @Override
    public void outprocess(Project project, JSONObject outdata) {

        JSONObject writecontext=new JSONObject();
        for (ModleProperty property : propertyImpList) {
            BaseModlePropertyImp outmodlepin = (BaseModlePropertyImp) property;
            if (outmodlepin.getPindir().equals(ModleProperty.PINDIROUTPUT)) {
                int modlepinsId=outmodlepin.getResource().getInteger("modlepinsId");
                String outmappingtag=outmodlepin.getResource().getString("outmappingtag");
                writecontext.put(outmappingtag,indexproperties.get(modlepinsId).getValue());
                outmodlepin.setValue(indexproperties.get(modlepinsId).getValue());
            }
        }

        Map<String,String> postdata=new HashMap<>();
        postdata.put("tagvalue",writecontext.toJSONString());
        String inputdata = HttpUtils.PostData(datasource + "/opc/write", postdata);
        logger.info("modleid="+getModleId()+" write info"+inputdata);
        setModlerunlevel(BaseModleImp.RUNLEVEL_RUNCOMPLET);
        setActivetime(Instant.now());
        return;
    }

    @Override
    public void init() {
        indexproperties = new HashMap<>();
        for (ModleProperty modleProperty : propertyImpList) {
            BaseModlePropertyImp baseModlePropertyImp = (BaseModlePropertyImp) modleProperty;
            indexproperties.put(baseModlePropertyImp.getModlepinsId(), baseModlePropertyImp);
        }
    }

    @Override
    public void otherApcPlantRespon(int status) {

    }

    /**
     * db
     */
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
//
//    public boolean isJavabuildcomplet() {
//        return javabuildcomplet;
//    }
//
//    public void setJavabuildcomplet(boolean javabuildcomplet) {
//        this.javabuildcomplet = javabuildcomplet;
//    }
//
//    public boolean isPythonbuildcomplet() {
//        return pythonbuildcomplet;
//    }
//
//    public void setPythonbuildcomplet(boolean pythonbuildcomplet) {
//        this.pythonbuildcomplet = pythonbuildcomplet;
//    }
//
//    public boolean isIscomputecomplete() {
//        return iscomputecomplete;
//    }
//
//    public void setIscomputecomplete(boolean iscomputecomplete) {
//        this.iscomputecomplete = iscomputecomplete;
//    }
}
