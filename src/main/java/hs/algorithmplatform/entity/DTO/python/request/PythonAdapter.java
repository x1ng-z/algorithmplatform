package hs.algorithmplatform.entity.DTO.python.request;

import com.alibaba.fastjson.JSONObject;
import hs.algorithmplatform.entity.model.BaseModlePropertyImp;
import hs.algorithmplatform.entity.model.Modle;
import hs.algorithmplatform.entity.model.ModleProperty;
import hs.algorithmplatform.entity.model.customizemodle.CUSTOMIZEModle;
import hs.algorithmplatform.utils.help.FileHelp;
import hs.algorithmplatform.utils.help.Tool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/10 0:44
 */
public class PythonAdapter {
    private static Logger logger = LoggerFactory.getLogger(PythonAdapter.class);

    private PythonBaseParam basemodelparam;
    private String pythoncontext;
    private Map<String,String> inputparam;
    private List<PythonOutParam> outputparam;



    public CUSTOMIZEModle covertormodle() {
        CUSTOMIZEModle customizeModle = new CUSTOMIZEModle();

        customizeModle.setModleEnable(1);
        customizeModle.setModleName(getBasemodelparam().getModelname());
        customizeModle.setModletype(Modle.MODLETYPE_CUSTOMIZE);
        customizeModle.setModleId( getBasemodelparam().getModelid());
        customizeModle.setPropertyImpList(new ArrayList<>());
        for (Map.Entry<String, String> pythonInputParam :  inputparam.entrySet()) {
            BaseModlePropertyImp propertyImp = new BaseModlePropertyImp();
            propertyImp.setRefmodleId( getBasemodelparam().getModelid());
            propertyImp.setModleOpcTag("");
            propertyImp.setModlePinName(pythonInputParam.getKey());
            propertyImp.setOpcTagName("");
            JSONObject resource = new JSONObject();
            resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
            resource.put("value", pythonInputParam.getValue());
            propertyImp.setResource(resource);
            propertyImp.setPindir(ModleProperty.PINDIRINPUT);
            propertyImp.setModlepropertyclazz(ModleProperty.MODLEPROPERTYCLAZZ_BASE);
            customizeModle.getPropertyImpList().add(propertyImp);
        }


        for (PythonOutParam pythonOutParam :  getOutputparam()) {
            BaseModlePropertyImp propertyImp = new BaseModlePropertyImp();
            propertyImp.setRefmodleId( getBasemodelparam().getModelid());
            propertyImp.setModleOpcTag("");
            propertyImp.setModlePinName(pythonOutParam.getOutputpinname());
            propertyImp.setOpcTagName("");
            JSONObject resource = new JSONObject();
            resource.put("resource", ModleProperty.SOURCE_TYPE_MEMORY);
            propertyImp.setResource(resource);
            propertyImp.setResource(resource);
            propertyImp.setPindir(ModleProperty.PINDIROUTPUT);
            propertyImp.setModlepropertyclazz(ModleProperty.MODLEPROPERTYCLAZZ_BASE);
            customizeModle.getPropertyImpList().add(propertyImp);

        }
        //sript
        customizeModle.setCustomizepyname("_" +  getBasemodelparam().getModelid()+".py");
        try {
            FileHelp.updateFile(customizeModle.getCustomizepyname(),  getPythoncontext());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException("python file operate failed");
        }

        return customizeModle;
    }


    /**
     * 更新mpc引脚数据
     */

    public void updatemodlevalue(CUSTOMIZEModle customizeModle) {
        for (Map.Entry<String, String> pythonInputParam :  inputparam.entrySet()) {
            BaseModlePropertyImp selectmodleProperyByPinname = Tool.selectmodleProperyByPinname(pythonInputParam.getKey(), customizeModle.getPropertyImpList(), BaseModlePropertyImp.PINDIRINPUT);
            if (selectmodleProperyByPinname != null) {
                JSONObject resource = new JSONObject();
                resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
                resource.put("value", pythonInputParam.getValue());
                selectmodleProperyByPinname.setResource(resource);
            }
        }
    }

    public PythonBaseParam getBasemodelparam() {
        return basemodelparam;
    }

    public void setBasemodelparam(PythonBaseParam basemodelparam) {
        this.basemodelparam = basemodelparam;
    }

    public String getPythoncontext() {
        return pythoncontext;
    }

    public void setPythoncontext(String pythoncontext) {
        this.pythoncontext = pythoncontext;
    }


    public List<PythonOutParam> getOutputparam() {
        return outputparam;
    }

    public void setOutputparam(List<PythonOutParam> outputparam) {
        this.outputparam = outputparam;
    }

    public Map<String, String> getInputparam() {
        return inputparam;
    }

    public void setInputparam(Map<String, String> inputparam) {
        this.inputparam = inputparam;
    }
}
