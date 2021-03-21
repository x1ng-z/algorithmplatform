package hs.algorithmplatform.entity.DTO.pid.request;

import com.alibaba.fastjson.JSONObject;
import hs.algorithmplatform.entity.model.BaseModleImp;
import hs.algorithmplatform.entity.model.BaseModlePropertyImp;
import hs.algorithmplatform.entity.model.Modle;
import hs.algorithmplatform.entity.model.ModleProperty;
import hs.algorithmplatform.entity.model.controlmodle.PIDModle;
import hs.algorithmplatform.entity.model.modlerproerty.MPCModleProperty;
import hs.algorithmplatform.utils.help.Tool;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/9 9:22
 */
public class PidModleAdapter {

    @NotNull
    private PidBaseModleParam basemodelparam;
    @NotNull
    private PidInputproperty inputparam;
    @NotNull
    private List<PidOutPutproperty> outputparam;


    public PIDModle covertormodle() {
        PIDModle pidModle = new PIDModle();
        pidModle.setModleEnable(1);
        pidModle.setModleName(getBasemodelparam().getModelname());
        pidModle.setModletype(Modle.MODLETYPE_PID);
        pidModle.setRefprojectid(-1);
        pidModle.setModleId(getBasemodelparam().getModelid());
        pidModle.setPropertyImpList(new ArrayList<>());

        BaseModlePropertyImp kpbasemodleproperty = initpidproperty("kp", getBasemodelparam().getModelid(), getInputparam().getKp());//new BaseModlePropertyImp();
        pidModle.getPropertyImpList().add(kpbasemodleproperty);

        BaseModlePropertyImp kibasemodleproperty = initpidproperty("ki", getBasemodelparam().getModelid(), getInputparam().getKi());
        pidModle.getPropertyImpList().add(kibasemodleproperty);


        BaseModlePropertyImp kdbasemodleproperty = initpidproperty("kd", getBasemodelparam().getModelid(), getInputparam().getKd());
        pidModle.getPropertyImpList().add(kdbasemodleproperty);


        BaseModlePropertyImp deadZonebasemodleproperty = initpidproperty("deadZone", getBasemodelparam().getModelid(), getInputparam().getDeadZone());//new BaseModlePropertyImp();
        pidModle.getPropertyImpList().add(deadZonebasemodleproperty);


        BaseModlePropertyImp pvbasemodleproperty = initpidproperty(MPCModleProperty.TYPE_PIN_PV, getBasemodelparam().getModelid(), getInputparam().getPv());//new BaseModlePropertyImp();
        pidModle.getPropertyImpList().add(pvbasemodleproperty);


        BaseModlePropertyImp spbasemodleproperty = initpidproperty(MPCModleProperty.TYPE_PIN_SP, getBasemodelparam().getModelid(), getInputparam().getSp());//new BaseModlePropertyImp();
        pidModle.getPropertyImpList().add(spbasemodleproperty);


        BaseModlePropertyImp initpidmvproperty = initpidplusproperty(MPCModleProperty.TYPE_PIN_MV, getBasemodelparam().getModelid(), getInputparam().getMv(), getInputparam().getDmvHigh(), getInputparam().getDmvLow());//new BaseModlePropertyImp();
        pidModle.getPropertyImpList().add(initpidmvproperty);


        BaseModlePropertyImp initpidmvupproperty = initpidproperty(MPCModleProperty.TYPE_PIN_MVUP, getBasemodelparam().getModelid(), getInputparam().getMvuppinvalue());
        pidModle.getPropertyImpList().add(initpidmvupproperty);

        BaseModlePropertyImp initpidmvdownproperty = initpidproperty(MPCModleProperty.TYPE_PIN_MVDOWN, getBasemodelparam().getModelid(), getInputparam().getMvdownpinvalue());
        pidModle.getPropertyImpList().add(initpidmvdownproperty);


        if ( getInputparam().getFf() != null) {
            BaseModlePropertyImp ffbasemodleproperty = initpidproperty(MPCModleProperty.TYPE_PIN_FF, getBasemodelparam().getModelid(), getInputparam().getFf());//new BaseModlePropertyImp();
            pidModle.getPropertyImpList().add(ffbasemodleproperty);

            BaseModlePropertyImp kfbasemodleproperty = initpidproperty("kf", getBasemodelparam().getModelid(), getInputparam().getKf());//new BaseModlePropertyImp();
            pidModle.getPropertyImpList().add(kfbasemodleproperty);

        }


        BaseModlePropertyImp initpidautoproperty = initpidproperty(MPCModleProperty.TYPE_PIN_MODLE_AUTO, getBasemodelparam().getModelid(), getInputparam().getAuto());
        pidModle.getPropertyImpList().add(initpidautoproperty);


        for(PidOutPutproperty pidOutPutproperty:getOutputparam()){
            BaseModlePropertyImp outpropertyImp = new BaseModlePropertyImp();
            outpropertyImp.setRefmodleId( getBasemodelparam().getModelid());
            outpropertyImp.setModleOpcTag("");
            outpropertyImp.setModlePinName( pidOutPutproperty.getOutputpinname());
            outpropertyImp.setOpcTagName("");

            JSONObject resource = new JSONObject();
            resource.put("resource", ModleProperty.SOURCE_TYPE_MEMORY);
            outpropertyImp.setResource(resource);
            outpropertyImp.setPindir(ModleProperty.PINDIROUTPUT);
            outpropertyImp.setModlepropertyclazz(ModleProperty.MODLEPROPERTYCLAZZ_BASE);
            pidModle.getPropertyImpList().add(outpropertyImp);
        }

        return pidModle;
    }


    public void updatemodlevalue(PIDModle pidModle){

        BaseModlePropertyImp kpbasemodleproperty = Tool.selectmodleProperyByPinname("kp",pidModle.getPropertyImpList(),BaseModleImp.MODLETYPE_INPUT);
        if(kpbasemodleproperty!=null){
            kpbasemodleproperty.getResource().put("value", getInputparam().getKp());
        }

        BaseModlePropertyImp kibasemodleproperty = Tool.selectmodleProperyByPinname("ki",pidModle.getPropertyImpList(),BaseModleImp.MODLETYPE_INPUT);
        if(kibasemodleproperty!=null){
            kibasemodleproperty.getResource().put("value", getInputparam().getKi());
        }


        BaseModlePropertyImp kdbasemodleproperty = Tool.selectmodleProperyByPinname("kd",pidModle.getPropertyImpList(),BaseModleImp.MODLETYPE_INPUT);
        if(kdbasemodleproperty!=null){
            kdbasemodleproperty.getResource().put("value", getInputparam().getKd());
        }


        BaseModlePropertyImp deadZonebasemodleproperty = Tool.selectmodleProperyByPinname("deadZone",pidModle.getPropertyImpList(),BaseModleImp.MODLETYPE_INPUT);
        if(deadZonebasemodleproperty!=null){
            deadZonebasemodleproperty.getResource().put("value", getInputparam().getDeadZone());
        }


        BaseModlePropertyImp pvbasemodleproperty = Tool.selectmodleProperyByPinname(MPCModleProperty.TYPE_PIN_PV,pidModle.getPropertyImpList(),BaseModleImp.MODLETYPE_INPUT);
        if(pvbasemodleproperty!=null){
            pvbasemodleproperty.getResource().put("value", getInputparam().getPv());
        }


        BaseModlePropertyImp spbasemodleproperty = Tool.selectmodleProperyByPinname(MPCModleProperty.TYPE_PIN_SP,pidModle.getPropertyImpList(),BaseModleImp.MODLETYPE_INPUT);
        if(spbasemodleproperty!=null){
            spbasemodleproperty.getResource().put("value", getInputparam().getSp());
        }


        MPCModleProperty initpidmvproperty =
                (MPCModleProperty) Tool.selectmodleProperyByPinname(MPCModleProperty.TYPE_PIN_MV,pidModle.getPropertyImpList(),BaseModleImp.MODLETYPE_INPUT);
        if(initpidmvproperty!=null){
            initpidmvproperty.getResource().put("value", getInputparam().getMv());
            initpidmvproperty.setDmvHigh( getInputparam().getDmvHigh());
            initpidmvproperty.setDmvLow( getInputparam().getDmvLow());
        }


        BaseModlePropertyImp initpidmvupproperty =
                Tool.selectmodleProperyByPinname(MPCModleProperty.TYPE_PIN_MVUP,pidModle.getPropertyImpList(),BaseModleImp.MODLETYPE_INPUT);
        if(initpidmvupproperty!=null){
            initpidmvupproperty.getResource().put("value", getInputparam().getMvuppinvalue());
        }



        BaseModlePropertyImp initpidmvdownproperty =
                Tool.selectmodleProperyByPinname(MPCModleProperty.TYPE_PIN_MVDOWN,pidModle.getPropertyImpList(),BaseModleImp.MODLETYPE_INPUT);
        if(initpidmvdownproperty!=null){
            initpidmvdownproperty.getResource().put("value", getInputparam().getMvdownpinvalue());
        }


        if ( getInputparam().getFf() != null) {
            BaseModlePropertyImp ffbasemodleproperty =
                    Tool.selectmodleProperyByPinname(MPCModleProperty.TYPE_PIN_FF,pidModle.getPropertyImpList(),BaseModleImp.MODLETYPE_INPUT);
            if(ffbasemodleproperty!=null){
                ffbasemodleproperty.getResource().put("value", getInputparam().getFf());
            }
        }


        BaseModlePropertyImp initpidautoproperty =
                Tool.selectmodleProperyByPinname(MPCModleProperty.TYPE_PIN_MODLE_AUTO,pidModle.getPropertyImpList(),BaseModleImp.MODLETYPE_INPUT);
        if(initpidautoproperty!=null){
            initpidautoproperty.getResource().put("value", getInputparam().getAuto());
        }
    }


    private BaseModlePropertyImp initpidproperty(String pinname, long modleId, double properyconstant) {
        BaseModlePropertyImp kpbasemodleproperty = new BaseModlePropertyImp();
        kpbasemodleproperty.setModlePinName(pinname);
        kpbasemodleproperty.setModlepropertyclazz(ModleProperty.MODLEPROPERTYCLAZZ_BASE);
        kpbasemodleproperty.setPindir(ModleProperty.PINDIRINPUT);
        kpbasemodleproperty.setRefmodleId(modleId);
        kpbasemodleproperty.setPinEnable(1);
        JSONObject resource = new JSONObject();
        resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
        resource.put("value", properyconstant);
        kpbasemodleproperty.setResource(resource);
        kpbasemodleproperty.setModleOpcTag("");
        kpbasemodleproperty.setOpcTagName("");
        return kpbasemodleproperty;
    }


    private MPCModleProperty initpidplusproperty(String pinname, long modleId, double properyconstant, double dmvhight, double dmvlow) {
        MPCModleProperty kpbasemodleproperty = new MPCModleProperty();
        kpbasemodleproperty.setModlePinName(pinname);
        kpbasemodleproperty.setOpcTagName("");
        kpbasemodleproperty.setModlepropertyclazz(ModleProperty.MODLEPROPERTYCLAZZ_MPC);
        kpbasemodleproperty.setPindir(ModleProperty.PINDIRINPUT);
        kpbasemodleproperty.setRefmodleId(modleId);
        kpbasemodleproperty.setPinEnable(1);
        kpbasemodleproperty.setDmvLow(dmvlow);
        kpbasemodleproperty.setDmvHigh(dmvhight);
//        kpbasemodleproperty.setDeadZone(deadZone);


        JSONObject resource = new JSONObject();
        resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
        resource.put("value", properyconstant);
        kpbasemodleproperty.setResource(resource);
        kpbasemodleproperty.setModleOpcTag("");
        return kpbasemodleproperty;
    }


    public PidBaseModleParam getBasemodelparam() {
        return basemodelparam;
    }

    public void setBasemodelparam(PidBaseModleParam basemodelparam) {
        this.basemodelparam = basemodelparam;
    }

    public PidInputproperty getInputparam() {
        return inputparam;
    }

    public void setInputparam(PidInputproperty inputparam) {
        this.inputparam = inputparam;
    }


    public List<PidOutPutproperty> getOutputparam() {
        return outputparam;
    }

    public void setOutputparam(List<PidOutPutproperty> outputparam) {
        this.outputparam = outputparam;
    }
}
