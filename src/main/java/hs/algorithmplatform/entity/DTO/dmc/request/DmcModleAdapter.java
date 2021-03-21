package hs.algorithmplatform.entity.DTO.dmc.request;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.annotation.JSONField;
import hs.algorithmplatform.entity.ResponTimeSerise;
import hs.algorithmplatform.entity.model.BaseModlePropertyImp;
import hs.algorithmplatform.entity.model.Modle;
import hs.algorithmplatform.entity.model.ModleProperty;
import hs.algorithmplatform.entity.model.controlmodle.MPCModle;
import hs.algorithmplatform.entity.model.modlerproerty.MPCModleProperty;
import hs.algorithmplatform.utils.help.Tool;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/8 12:02
 */
public class DmcModleAdapter {
    private static Pattern pvpattern = Pattern.compile("(^pv(\\d+)$)");
    private static Pattern ffpattern = Pattern.compile("(^ff(\\d+)$)");
    private static Pattern mvpattern = Pattern.compile("(^mv(\\d+)$)");

//    @JSONField(name = "basemodleparam")
    @NotNull
    private DmcBasemodleparam basemodelparam;

    @NotNull(message = "pv context is null")
    @JSONField(name = "pv")
    @Size(min = 1, message = "至少需要设置一个pv")
    private List<Pvparam> pv;

    @NotNull
    @JSONField(name = "mv")
    @Size(min = 1, message = "至少需要设置一个mv")
    private List<Mvparam> mv;


    @JSONField(name = "ff")
    private List<Ffparam> ff;

    @NotNull
    @JSONField(name = "model")
    @Size(min = 1, message = "至少需要设置一个模型")
    private List<DmcResponparam> model;


    @JSONField(name = "outputparam")
    private List<DmcOutproperty> outputparam;

    public static Pattern getPvpattern() {
        return pvpattern;
    }

    public static void setPvpattern(Pattern pvpattern) {
        DmcModleAdapter.pvpattern = pvpattern;
    }

    public static Pattern getFfpattern() {
        return ffpattern;
    }

    public static void setFfpattern(Pattern ffpattern) {
        DmcModleAdapter.ffpattern = ffpattern;
    }

    public static Pattern getMvpattern() {
        return mvpattern;
    }

    public static void setMvpattern(Pattern mvpattern) {
        DmcModleAdapter.mvpattern = mvpattern;
    }

    public DmcBasemodleparam getBasemodelparam() {
        return basemodelparam;
    }

    public void setBasemodelparam(DmcBasemodleparam basemodelparam) {
        this.basemodelparam = basemodelparam;
    }

    public List<Pvparam> getPv() {
        return pv;
    }

    public void setPv(List<Pvparam> pv) {
        this.pv = pv;
    }

    public List<Mvparam> getMv() {
        return mv;
    }

    public void setMv(List<Mvparam> mv) {
        this.mv = mv;
    }

    public List<Ffparam> getFf() {
        return ff;
    }

    public void setFf(List<Ffparam> ff) {
        this.ff = ff;
    }

    public List<DmcResponparam> getModel() {
        return model;
    }

    public void setModel(List<DmcResponparam> model) {
        this.model = model;
    }

    public List<DmcOutproperty> getOutputparam() {
        return outputparam;
    }

    public void setOutputparam(List<DmcOutproperty> outputparam) {
        this.outputparam = outputparam;
    }

    /***
     * 转换为mpc
     * */
    public MPCModle covertormodle() {

        DmcBasemodleparam dmcBasemodleparam = getBasemodelparam();
        MPCModle mpc = new MPCModle();
        mpc.setModleEnable(1);
        mpc.setModleName(dmcBasemodleparam.getModelname());
        mpc.setModletype(Modle.MODLETYPE_MPC);
        mpc.setRefprojectid(-1);
        mpc.setPredicttime_P(Integer.parseInt(dmcBasemodleparam.getPredicttime_P()));
        mpc.setTimeserise_N(Integer.parseInt(dmcBasemodleparam.getTimeserise_N()));
        mpc.setControltime_M(Integer.parseInt(dmcBasemodleparam.getControltime_M()));
        mpc.setRunstyle(0);//默认自动分配
        mpc.setModleId(dmcBasemodleparam.getModelid());
        mpc.setControlAPCOutCycle(Tool.getSpecalScale(dmcBasemodleparam.getControlapcoutcycle()));

        mpc.setPropertyImpList(new ArrayList<>());
        mpc.setResponTimeSeriseList(new ArrayList<>());
        /*auto*/
        MPCModleProperty auto = new MPCModleProperty();
        auto.setModlePinName(BaseModlePropertyImp.TYPE_PIN_MODLE_AUTO);
        auto.setOpcTagName(BaseModlePropertyImp.TYPE_PIN_MODLE_AUTO + dmcBasemodleparam.getModelid());
        auto.setModlepropertyclazz(ModleProperty.MODLEPROPERTYCLAZZ_MPC);
        auto.setPindir(ModleProperty.PINDIRINPUT);
        auto.setRefmodleId(dmcBasemodleparam.getModelid());
        auto.setPinEnable(1);
        JSONObject auto_resource = new JSONObject();
        auto_resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
        auto_resource.put("value", dmcBasemodleparam.getAuto());
        auto.setResource(auto_resource);
        mpc.getPropertyImpList().add(auto);


        /*pv*/
        for (Pvparam pvparam : getPv()) {

            MPCModleProperty pvpinmpcModleProperty = new MPCModleProperty();
            pvpinmpcModleProperty.setRefmodleId(dmcBasemodleparam.getModelid());
            pvpinmpcModleProperty.setModlePinName(pvparam.getPvpinname());
            pvpinmpcModleProperty.setPindir(ModleProperty.PINDIRINPUT);
            pvpinmpcModleProperty.setPintype(ModleProperty.TYPE_PIN_PV);
            pvpinmpcModleProperty.setModlepropertyclazz(ModleProperty.MODLEPROPERTYCLAZZ_MPC);
            pvpinmpcModleProperty.setOpcTagName(pvparam.getPvpinname() + dmcBasemodleparam.getModelid());//spmodleOpcTag
            pvpinmpcModleProperty.setModleOpcTag(pvparam.getPvpinname() + dmcBasemodleparam.getModelid());
            JSONObject pv_resource = new JSONObject();
            pv_resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
            pv_resource.put("value", pvparam.getPvpinvalue());
            pvpinmpcModleProperty.setResource(pv_resource);
            pvpinmpcModleProperty.setOpcTagName("");

            pvpinmpcModleProperty.setDeadZone(pvparam.getDeadzone());
            pvpinmpcModleProperty.setFunelinitValue(pvparam.getFunelinitvalue());

            pvpinmpcModleProperty.setFunneltype(pvparam.getFunneltype());
            pvpinmpcModleProperty.setQ(pvparam.getQ());
            pvpinmpcModleProperty.setReferTrajectoryCoef(pvparam.getRefertrajectorycoef());
            pvpinmpcModleProperty.setTracoefmethod(pvparam.getTracoefmethod());
            mpc.getPropertyImpList().add(pvpinmpcModleProperty);

            int pinorder = 0;
            Matcher pvmatch = pvpattern.matcher(pvparam.getPvpinname());
            if (pvmatch.find()) {
                pinorder = Integer.parseInt(pvmatch.group(2));
            } else {
                throw new RuntimeException("can't match pin order");
            }
            if (pvparam.getPvuppinvalue() != null) {
                MPCModleProperty pvuppinmpcModleProperty = new MPCModleProperty();
                pvuppinmpcModleProperty.setPindir(ModleProperty.PINDIRINPUT);
                pvuppinmpcModleProperty.setRefmodleId(dmcBasemodleparam.getModelid());
                pvuppinmpcModleProperty.setModlePinName(ModleProperty.TYPE_PIN_PVUP + pinorder);
                pvuppinmpcModleProperty.setPintype(ModleProperty.TYPE_PIN_PVUP);
                pvuppinmpcModleProperty.setModlepropertyclazz(ModleProperty.MODLEPROPERTYCLAZZ_MPC);
                pvuppinmpcModleProperty.setOpcTagName("");//spmodleOpcTag
                pvuppinmpcModleProperty.setModleOpcTag("");

                JSONObject pvup_resource = new JSONObject();
                pvup_resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
                pvup_resource.put("value", pvparam.getPvuppinvalue());
                pvuppinmpcModleProperty.setOpcTagName("");
                pvuppinmpcModleProperty.setResource(pvup_resource);
                mpc.getPropertyImpList().add(pvuppinmpcModleProperty);
            }


            if (pvparam.getPvdownpinvalue() != null) {
                MPCModleProperty pvdownpinmpcModleProperty = new MPCModleProperty();
                pvdownpinmpcModleProperty.setPindir(ModleProperty.PINDIRINPUT);
                pvdownpinmpcModleProperty.setRefmodleId(dmcBasemodleparam.getModelid());
                pvdownpinmpcModleProperty.setModlePinName(ModleProperty.TYPE_PIN_PVDOWN + pinorder);
                pvdownpinmpcModleProperty.setPintype(ModleProperty.TYPE_PIN_PVDOWN);
                pvdownpinmpcModleProperty.setModlepropertyclazz(ModleProperty.MODLEPROPERTYCLAZZ_MPC);
                pvdownpinmpcModleProperty.setOpcTagName("");//spmodleOpcTag
                pvdownpinmpcModleProperty.setModleOpcTag("");

                JSONObject pvdown_resource = new JSONObject();
                pvdown_resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
                pvdown_resource.put("value", pvparam.getPvdownpinvalue());
                pvdownpinmpcModleProperty.setResource(pvdown_resource);
                pvdownpinmpcModleProperty.setOpcTagName("");
                mpc.getPropertyImpList().add(pvdownpinmpcModleProperty);

            }


            MPCModleProperty sppinmpcModleProperty = new MPCModleProperty();
            sppinmpcModleProperty.setPindir(ModleProperty.PINDIRINPUT);
            sppinmpcModleProperty.setRefmodleId(dmcBasemodleparam.getModelid());
            sppinmpcModleProperty.setModlePinName(ModleProperty.TYPE_PIN_SP + pinorder);
            sppinmpcModleProperty.setPintype(ModleProperty.TYPE_PIN_SP);
            sppinmpcModleProperty.setModlepropertyclazz(ModleProperty.MODLEPROPERTYCLAZZ_MPC);
            sppinmpcModleProperty.setOpcTagName("");//spmodleOpcTag
            sppinmpcModleProperty.setModleOpcTag("");


            JSONObject sp_resource = new JSONObject();
            sp_resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
            sp_resource.put("value", pvparam.getSppinvalue());
            sppinmpcModleProperty.setResource(sp_resource);
            mpc.getPropertyImpList().add(sppinmpcModleProperty);

        }

//mv
        for (Mvparam mvparam : getMv()) {
            MPCModleProperty mvpinmpcModleProperty = new MPCModleProperty();
            mvpinmpcModleProperty.setRefmodleId(dmcBasemodleparam.getModelid());
            mvpinmpcModleProperty.setModlePinName(mvparam.getMvpinname());
            mvpinmpcModleProperty.setPindir(ModleProperty.PINDIRINPUT);
            mvpinmpcModleProperty.setPintype(ModleProperty.TYPE_PIN_MV);
            mvpinmpcModleProperty.setModlepropertyclazz(ModleProperty.MODLEPROPERTYCLAZZ_MPC);
            mvpinmpcModleProperty.setOpcTagName("");
            mvpinmpcModleProperty.setModleOpcTag("");
            JSONObject mv_resource = new JSONObject();
            mv_resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
            mv_resource.put("value", mvparam.getMvpinvalue());
            mvpinmpcModleProperty.setResource(mv_resource);
            mvpinmpcModleProperty.setR(mvparam.getR());
            mvpinmpcModleProperty.setDmvHigh(mvparam.getDmvhigh());
            mvpinmpcModleProperty.setDmvLow(mvparam.getDmvlow());
            mpc.getPropertyImpList().add(mvpinmpcModleProperty);


            int pinorder = 0;
            Matcher pvmatch = mvpattern.matcher(mvparam.getMvpinname());
            if (pvmatch.find()) {
                pinorder = Integer.parseInt(pvmatch.group(2));
            } else {
                throw new RuntimeException("can't match pin order");
            }


            MPCModleProperty mvuppinmpcModleProperty = new MPCModleProperty();
            mvuppinmpcModleProperty.setPindir(ModleProperty.PINDIRINPUT);
            mvuppinmpcModleProperty.setRefmodleId(dmcBasemodleparam.getModelid());
            mvuppinmpcModleProperty.setModlePinName(ModleProperty.TYPE_PIN_MVUP + pinorder);
            mvuppinmpcModleProperty.setPintype(ModleProperty.TYPE_PIN_MVUP);
            mvuppinmpcModleProperty.setModlepropertyclazz(ModleProperty.MODLEPROPERTYCLAZZ_MPC);

            mvuppinmpcModleProperty.setOpcTagName("");//spmodleOpcTag
            mvuppinmpcModleProperty.setModleOpcTag("");


            JSONObject mvup_resource = new JSONObject();
            mvup_resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
            mvup_resource.put("value", mvparam.getMvuppinvalue());
            mvuppinmpcModleProperty.setResource(mvup_resource);
            mpc.getPropertyImpList().add(mvuppinmpcModleProperty);


            MPCModleProperty mvdownpinmpcModleProperty = new MPCModleProperty();
            mvdownpinmpcModleProperty.setPindir(ModleProperty.PINDIRINPUT);
            mvdownpinmpcModleProperty.setRefmodleId(dmcBasemodleparam.getModelid());
            mvdownpinmpcModleProperty.setModlePinName(ModleProperty.TYPE_PIN_MVDOWN + pinorder);
            mvdownpinmpcModleProperty.setPintype(ModleProperty.TYPE_PIN_MVDOWN);
            mvdownpinmpcModleProperty.setModlepropertyclazz(ModleProperty.MODLEPROPERTYCLAZZ_MPC);
            mvdownpinmpcModleProperty.setOpcTagName("");//spmodleOpcTag
            mvdownpinmpcModleProperty.setModleOpcTag("");
            JSONObject mvdown_resource = new JSONObject();
            mvdown_resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
            mvdown_resource.put("value", mvparam.getMvdownpinvalue());
            mvdownpinmpcModleProperty.setResource(mvdown_resource);
            mpc.getPropertyImpList().add(mvdownpinmpcModleProperty);


            MPCModleProperty mvfbpinmpcModleProperty = new MPCModleProperty();
            mvfbpinmpcModleProperty.setPindir(ModleProperty.PINDIRINPUT);
            mvfbpinmpcModleProperty.setRefmodleId(dmcBasemodleparam.getModelid());
            mvfbpinmpcModleProperty.setModlePinName(ModleProperty.TYPE_PIN_MVFB + pinorder);
            mvfbpinmpcModleProperty.setPintype(ModleProperty.TYPE_PIN_MVFB);
            mvfbpinmpcModleProperty.setModlepropertyclazz(ModleProperty.MODLEPROPERTYCLAZZ_MPC);
            mvfbpinmpcModleProperty.setOpcTagName("");//mvfbmodleOpcTag
            mvfbpinmpcModleProperty.setModleOpcTag("");
            JSONObject mvfb_resource = new JSONObject();
            mvfb_resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
            mvfb_resource.put("value", mvparam.getMvfbpinvalue());
            mvfbpinmpcModleProperty.setResource(mvfb_resource);
            mpc.getPropertyImpList().add(mvfbpinmpcModleProperty);


        }
//ff
        for (Ffparam ffparam : getFf()) {
            MPCModleProperty ffpinmpcModleProperty = new MPCModleProperty();
            ffpinmpcModleProperty.setRefmodleId(dmcBasemodleparam.getModelid());
            ffpinmpcModleProperty.setModlePinName(ffparam.getFfpinname());
            ffpinmpcModleProperty.setPindir(ModleProperty.PINDIRINPUT);
            ffpinmpcModleProperty.setPintype(ModleProperty.TYPE_PIN_FF);
            ffpinmpcModleProperty.setModlepropertyclazz(ModleProperty.MODLEPROPERTYCLAZZ_MPC);
            ffpinmpcModleProperty.setOpcTagName("");
            ffpinmpcModleProperty.setModleOpcTag("");
            JSONObject ffresource = new JSONObject();
            ffresource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
            ffresource.put("value", ffparam.getFfpinvalue());
            ffpinmpcModleProperty.setResource(ffresource);
            ffpinmpcModleProperty.setOpcTagName("");
            mpc.getPropertyImpList().add(ffpinmpcModleProperty);

            int pinorder = 0;
            Matcher pvmatch = ffpattern.matcher(ffparam.getFfpinname());
            if (pvmatch.find()) {
                pinorder = Integer.parseInt(pvmatch.group(2));
            } else {
                throw new RuntimeException("can't match pin order");
            }


            if(ffparam.getFfuppinvalue()!=null){
                MPCModleProperty ffuppinmpcModleProperty = new MPCModleProperty();
                ffuppinmpcModleProperty.setPindir(ModleProperty.PINDIRINPUT);
                ffuppinmpcModleProperty.setRefmodleId(dmcBasemodleparam.getModelid());
                ffuppinmpcModleProperty.setModlePinName(ModleProperty.TYPE_PIN_FFUP + pinorder);
                ffuppinmpcModleProperty.setPintype(ModleProperty.TYPE_PIN_FFUP);
                ffuppinmpcModleProperty.setModlepropertyclazz(ModleProperty.MODLEPROPERTYCLAZZ_MPC);
                ffuppinmpcModleProperty.setOpcTagName("");//spmodleOpcTag
                ffuppinmpcModleProperty.setModleOpcTag("");
                JSONObject ffupresource = new JSONObject();
                ffupresource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
                ffupresource.put("value", ffparam.getFfuppinvalue());
                ffuppinmpcModleProperty.setResource(ffupresource);
                mpc.getPropertyImpList().add(ffuppinmpcModleProperty);

            }

            if(ffparam.getFfdownpinvalue()!=null){
                MPCModleProperty ffdownpinmpcModleProperty = new MPCModleProperty();
                ffdownpinmpcModleProperty.setPindir(ModleProperty.PINDIRINPUT);
                ffdownpinmpcModleProperty.setRefmodleId(dmcBasemodleparam.getModelid());
                ffdownpinmpcModleProperty.setModlePinName(ModleProperty.TYPE_PIN_FFDOWN + pinorder);
                ffdownpinmpcModleProperty.setPintype(ModleProperty.TYPE_PIN_FFDOWN);
                ffdownpinmpcModleProperty.setModlepropertyclazz(ModleProperty.MODLEPROPERTYCLAZZ_MPC);
                ffdownpinmpcModleProperty.setOpcTagName("");//spmodleOpcTag
                ffdownpinmpcModleProperty.setModleOpcTag("");
                JSONObject ffdownresource = new JSONObject();
                ffdownresource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
                ffdownresource.put("value", ffparam.getFfdownpinvalue());
                ffdownpinmpcModleProperty.setResource(ffdownresource);
                mpc.getPropertyImpList().add(ffdownpinmpcModleProperty);
            }



        }
        //respon

        for (DmcResponparam dmcResponparam : getModel()) {
            long modleid = dmcBasemodleparam.getModelid();
            int responid = -1;
            String inputpinName = dmcResponparam.getInputpinname();
            String outputpinName = dmcResponparam.getOutputpinname();
            double K = dmcResponparam.getK();
            double T = dmcResponparam.getT();
            double Tau = dmcResponparam.getTau();
            float effectRatio = 1.0f;
            double Ki = dmcResponparam.getKi();
            ResponTimeSerise respontimeserise;
            JSONObject jsonres;

            respontimeserise = new ResponTimeSerise();

            respontimeserise.setInputPins(inputpinName);
            respontimeserise.setOutputPins(outputpinName);
            respontimeserise.setRefrencemodleId(modleid);
//            respontimeserise.setModletagId(responid.equals("") ? -1 : Integer.valueOf(responid));
            jsonres = new JSONObject();
            jsonres.put("k", K);
            jsonres.put("t", T);
            jsonres.put("tao", Tau);
            jsonres.put("Ki", Ki);
            respontimeserise.setStepRespJson(jsonres);
            respontimeserise.setEffectRatio(effectRatio);
            mpc.getResponTimeSeriseList().add(respontimeserise);

        }

        /*output pin*/

        for (DmcOutproperty dmcOutparam : getOutputparam()) {
            MPCModleProperty propertyImp = new MPCModleProperty();
            propertyImp.setRefmodleId(dmcBasemodleparam.getModelid());
            propertyImp.setModleOpcTag("");
            propertyImp.setModlePinName(dmcOutparam.getOutputpinname());
            propertyImp.setOpcTagName("");

            //数据源
            JSONObject resource = new JSONObject();
            resource.put("resource", ModleProperty.SOURCE_TYPE_MEMORY);
            propertyImp.setResource(resource);
            propertyImp.setPindir(ModleProperty.PINDIROUTPUT);
            propertyImp.setModlepropertyclazz(ModleProperty.MODLEPROPERTYCLAZZ_MPC);
            mpc.getPropertyImpList().add(propertyImp);
        }


        return mpc;
    }


    /**
     * 更新mpc引脚数据
     */

    public void updatemodlevalue(MPCModle mpc) {

        DmcBasemodleparam dmcBasemodleparam = getBasemodelparam();


        BaseModlePropertyImp auto = Tool.selectmodleProperyByPinname(BaseModlePropertyImp.TYPE_PIN_MODLE_AUTO, mpc.getPropertyImpList(), BaseModlePropertyImp.PINDIRINPUT);
        JSONObject auto_resource = new JSONObject();
        auto_resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
        auto_resource.put("value", dmcBasemodleparam.getAuto());
        auto.setResource(auto_resource);

        /*pv*/
        for (Pvparam pvparam : getPv()) {

            MPCModleProperty pvpinmpcModleProperty = (MPCModleProperty) Tool.selectmodleProperyByPinname(pvparam.getPvpinname(), mpc.getPropertyImpList(), BaseModlePropertyImp.PINDIRINPUT);

            JSONObject pv_resource = new JSONObject();
            pv_resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
            pv_resource.put("value", pvparam.getPvpinvalue());
            pvpinmpcModleProperty.setResource(pv_resource);
            pvpinmpcModleProperty.setOpcTagName("");

            pvpinmpcModleProperty.setDeadZone(pvparam.getDeadzone());
            pvpinmpcModleProperty.setFunelinitValue(pvparam.getFunelinitvalue());

            pvpinmpcModleProperty.setFunneltype(pvparam.getFunneltype());
            pvpinmpcModleProperty.setQ(pvparam.getQ());
            pvpinmpcModleProperty.setReferTrajectoryCoef(pvparam.getRefertrajectorycoef());
            pvpinmpcModleProperty.setTracoefmethod(pvparam.getTracoefmethod());

            int pinorder = 0;
            Matcher pvmatch = pvpattern.matcher(pvparam.getPvpinname());
            if (pvmatch.find()) {
                pinorder = Integer.parseInt(pvmatch.group(2));
            } else {
                throw new RuntimeException("can't match pin order");
            }


            MPCModleProperty pvuppinmpcModleProperty = (MPCModleProperty) Tool.selectmodleProperyByPinname(ModleProperty.TYPE_PIN_PVUP + pinorder, mpc.getPropertyImpList(), BaseModlePropertyImp.PINDIRINPUT);
            if(pvuppinmpcModleProperty!=null){
                JSONObject pvup_resource = new JSONObject();
                pvup_resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
                pvup_resource.put("value", pvparam.getPvuppinvalue());
                pvuppinmpcModleProperty.setOpcTagName("");
                pvuppinmpcModleProperty.setResource(pvup_resource);
            }

            MPCModleProperty pvdownpinmpcModleProperty = (MPCModleProperty) Tool.selectmodleProperyByPinname(ModleProperty.TYPE_PIN_PVDOWN + pinorder, mpc.getPropertyImpList(), BaseModlePropertyImp.PINDIRINPUT);
            if(pvdownpinmpcModleProperty!=null){
                JSONObject pvdown_resource = new JSONObject();
                pvdown_resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
                pvdown_resource.put("value", pvparam.getPvdownpinvalue());
                pvdownpinmpcModleProperty.setResource(pvdown_resource);
                pvdownpinmpcModleProperty.setOpcTagName("");
            }


            MPCModleProperty sppinmpcModleProperty = (MPCModleProperty) Tool.selectmodleProperyByPinname(ModleProperty.TYPE_PIN_SP + pinorder, mpc.getPropertyImpList(), BaseModlePropertyImp.PINDIRINPUT);
            JSONObject sp_resource = new JSONObject();
            sp_resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
            sp_resource.put("value", pvparam.getSppinvalue());
            sppinmpcModleProperty.setResource(sp_resource);

        }

//mv
        for (Mvparam mvparam : getMv()) {
            MPCModleProperty mvpinmpcModleProperty = (MPCModleProperty) Tool.selectmodleProperyByPinname(mvparam.getMvpinname(), mpc.getPropertyImpList(), BaseModlePropertyImp.PINDIRINPUT);
            JSONObject mv_resource = new JSONObject();
            mv_resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
            mv_resource.put("value", mvparam.getMvpinvalue());
            mvpinmpcModleProperty.setResource(mv_resource);
            mvpinmpcModleProperty.setR(mvparam.getR());
            mvpinmpcModleProperty.setDmvHigh(mvparam.getDmvhigh());
            mvpinmpcModleProperty.setDmvLow(mvparam.getDmvlow());


            int pinorder = 0;
            Matcher pvmatch = mvpattern.matcher(mvparam.getMvpinname());
            if (pvmatch.find()) {
                pinorder = Integer.parseInt(pvmatch.group(2));
            } else {
                throw new RuntimeException("can't match pin order");
            }


            MPCModleProperty mvuppinmpcModleProperty = (MPCModleProperty) Tool.selectmodleProperyByPinname(ModleProperty.TYPE_PIN_MVUP + pinorder, mpc.getPropertyImpList(), BaseModlePropertyImp.PINDIRINPUT);
            JSONObject mvup_resource = new JSONObject();
            mvup_resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
            mvup_resource.put("value", mvparam.getMvuppinvalue());
            mvuppinmpcModleProperty.setResource(mvup_resource);


            MPCModleProperty mvdownpinmpcModleProperty = (MPCModleProperty) Tool.selectmodleProperyByPinname(ModleProperty.TYPE_PIN_MVDOWN + pinorder, mpc.getPropertyImpList(), BaseModlePropertyImp.PINDIRINPUT);
            JSONObject mvdown_resource = new JSONObject();
            mvdown_resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
            mvdown_resource.put("value", mvparam.getMvdownpinvalue());
            mvdownpinmpcModleProperty.setResource(mvdown_resource);


            MPCModleProperty mvfbpinmpcModleProperty = (MPCModleProperty) Tool.selectmodleProperyByPinname(ModleProperty.TYPE_PIN_MVFB + pinorder, mpc.getPropertyImpList(), BaseModlePropertyImp.PINDIRINPUT);
            JSONObject mvfb_resource = new JSONObject();
            mvfb_resource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
            mvfb_resource.put("value", mvparam.getMvfbpinvalue());
            mvfbpinmpcModleProperty.setResource(mvfb_resource);
        }
//ff
        for (Ffparam ffparam : getFf()) {

            MPCModleProperty ffpinmpcModleProperty = (MPCModleProperty) Tool.selectmodleProperyByPinname(ffparam.getFfpinname(), mpc.getPropertyImpList(), BaseModlePropertyImp.PINDIRINPUT);

            JSONObject ffresource = new JSONObject();
            ffresource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
            ffresource.put("value", ffparam.getFfpinvalue());
            ffpinmpcModleProperty.setResource(ffresource);
            ffpinmpcModleProperty.setOpcTagName("");

            int pinorder = 0;
            Matcher pvmatch = ffpattern.matcher(ffparam.getFfpinname());
            if (pvmatch.find()) {
                pinorder = Integer.parseInt(pvmatch.group(2));
            } else {
                throw new RuntimeException("can't match pin order");
            }


            MPCModleProperty ffuppinmpcModleProperty = (MPCModleProperty) Tool.selectmodleProperyByPinname(ModleProperty.TYPE_PIN_FFUP + pinorder, mpc.getPropertyImpList(), BaseModlePropertyImp.PINDIRINPUT);
            if(ffuppinmpcModleProperty!=null){
                JSONObject ffupresource = new JSONObject();
                ffupresource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
                ffupresource.put("value", ffparam.getFfuppinvalue());
                ffuppinmpcModleProperty.setResource(ffupresource);
            }



            MPCModleProperty ffdownpinmpcModleProperty = (MPCModleProperty) Tool.selectmodleProperyByPinname(ModleProperty.TYPE_PIN_FFDOWN + pinorder, mpc.getPropertyImpList(), BaseModlePropertyImp.PINDIRINPUT);
            if(ffdownpinmpcModleProperty!=null){
                JSONObject ffdownresource = new JSONObject();
                ffdownresource.put("resource", ModleProperty.SOURCE_TYPE_CONSTANT);
                ffdownresource.put("value", ffparam.getFfdownpinvalue());
                ffdownpinmpcModleProperty.setResource(ffdownresource);
            }

        }

    }

}
