package hs.algorithmplatform.entity.model;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/9/2 13:12
 */
public interface ModleProperty {

    //mpc引脚
    public static final String TYPE_PIN_PV = "pv";
    public static final String TYPE_PIN_PVDOWN = "pvdown";
    public static final String TYPE_PIN_PVUP = "pvup";
    public static final String TYPE_PIN_SP = "sp";
    public static final String TYPE_PIN_MV = "mv";
    public static final String TYPE_PIN_MVFB = "mvfb";
    public static final String TYPE_PIN_FF = "ff";
    public static final String TYPE_PIN_MODLE_AUTO = "auto";//模型整体是否进行运行
    public static final String TYPE_PIN_FFDOWN = "ffdown";
    public static final String TYPE_PIN_FFUP = "ffup";
    public static final String TYPE_PIN_MVUP = "mvup";
    public static final String TYPE_PIN_MVDOWN = "mvdown";
    public static final String TYPE_PIN_PIN_PVENABLE = "pvenable";//pv引脚是否启用,控制方为dcs
    public static final String TYPE_PIN_PIN_FFENABLE = "ffenable";//ff引脚是否启用,控制方为dcs
    public static final String TYPE_PIN_PIN_MVENABLE = "mvenable";//mv引脚是否启用,控制方为dcs
    public static final String TYPE_FUNNEL_FULL = "fullfunnel";
    public static final String TYPE_FUNNEL_UP = "upfunnel";
    public static final String TYPE_FUNNEL_DOWN = "downfunnel";

    //pid引脚
    public static final String TYPE_PIN_KP = "kp";
    public static final String TYPE_PIN_KI = "ki";
    public static final String TYPE_PIN_KD = "kd";


    String SOURCE_TYPE_CONSTANT = "constant";
    String SOURCE_TYPE_MEMORY = "memory";//+node
    String SOURCE_TYPE_OPC = "opc";
    String SOURCE_TYPE_MODLE="modle";

    String PINDIRINPUT = "input";
    String PINDIROUTPUT = "output";

    String MODLEPROPERTYCLAZZ_BASE = "basepropery";
    String MODLEPROPERTYCLAZZ_MPC = "mpcproperty";




}
