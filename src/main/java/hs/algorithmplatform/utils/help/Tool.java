package hs.algorithmplatform.utils.help;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hs.algorithmplatform.entity.model.BaseModlePropertyImp;
import hs.algorithmplatform.entity.model.ModleProperty;
import hs.algorithmplatform.entity.model.modlerproerty.MPCModleProperty;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/6/16 13:21
 */
public class Tool {

    /**
     * 数据保留位数设置函数
     */
    public static double getSpecalScale(int scaledouble, double value) {
        BigDecimal a = new BigDecimal(value);
        return a.setScale(scaledouble, BigDecimal.ROUND_UP).doubleValue();
    }

    public static int getSpecalScale(double value) {
        BigDecimal a = new BigDecimal(value);
        return a.setScale(0, BigDecimal.ROUND_UP).intValue();
    }


    public static BaseModlePropertyImp selectmodleProperyByPinname(String pinname, List<ModleProperty> properties, String pindir) {
        for (ModleProperty baseproperty : properties) {
            BaseModlePropertyImp property=(BaseModlePropertyImp)baseproperty;
            if (property.getModlePinName().equals(pinname)) {
                if (pindir != null) {
                    if (property.getPindir().equals(pindir)) {
                        return property;
                    }
                } else {
                    return property;
                }
            }
        }

        return null;

    }


    /**
     * @param count 所有的opc服务器的数量
     * @param datas 本页opc服务器信息
     */
    public static JSONObject sendLayuiPage(int count, JSONArray datas) {
        JSONObject result = new JSONObject();
        result.put("code", 0);
        result.put("msg", "");
        result.put("count", count);
        result.put("data", datas);
        return result;
    }


    public static boolean isNoneString(String value) {
        if (value == null || value.equals("")) {
            return true;
        }
        return false;

    }


    public static List<Integer> getunUserPinScope(Pattern pvpattern, List<MPCModleProperty> usepinscope, int maxindex){

        List<Integer> usepvpinindex=new LinkedList<>();
        List<Integer> allpinindex=new LinkedList<>();


        for(MPCModleProperty usepin:usepinscope){
            Matcher matcher=pvpattern.matcher(usepin.getModlePinName());
            if(matcher.find()){
                usepvpinindex.add(Integer.parseInt(matcher.group(2)));
            }
        }

        for(int indexpv=1;indexpv<=maxindex;indexpv++){
            allpinindex.add(indexpv);
        }

        allpinindex.removeAll(usepvpinindex);
        return allpinindex;
    }

    public static List<MPCModleProperty> getspecialpintypeBympc(String pintype, List<MPCModleProperty> mpcModleProperties){
        List<MPCModleProperty> specialpintypeproperties=new ArrayList<>();
        for(MPCModleProperty modleProperty:mpcModleProperties){
            if(!isNoneString(modleProperty.getPintype())){
                if(modleProperty.getPintype().equals(pintype)){
                    specialpintypeproperties.add(modleProperty);
                }
            }

        }
        return specialpintypeproperties;
    }



//    public  static int getPinindex(Pattern pattern, ModlePin pin){
//        int pvpinscope = -1;
//        Matcher matcher = pattern.matcher(pin.getModlePinName());
//        if (matcher.find()) {
//            pvpinscope = Integer.parseInt(matcher.group(2));
//        }
//        return pvpinscope;
//    }

}
