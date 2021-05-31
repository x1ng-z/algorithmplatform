package hs.algorithmplatform.entity.model.controlmodle;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import hs.algorithmplatform.entity.ResponTimeSerise;
import hs.algorithmplatform.entity.bean.BridgeInfo;
import hs.algorithmplatform.entity.model.BaseModleImp;
import hs.algorithmplatform.entity.model.ModleProperty;
import hs.algorithmplatform.entity.model.modlerproerty.MPCModleProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static hs.algorithmplatform.entity.model.controlmodle.MPCModle.*;


/**
 * @author zzx
 * @version 1.0
 * @date 2020/8/7 13:34
 */

/**
 * 拆分每个pv，并分配相应的mv、ff进行dmv的计算
 **/
public class SimulatControlModle extends BaseModleImp {

    private Logger logger = LoggerFactory.getLogger(SimulatControlModle.class);
    private String simulatorbuilddir;
    private Long modleid;
    private MPCModle controlModle;
    private boolean javabuildcomplet = false;//java控制模型是构建完成？
    private boolean pythonbuildcomplet = false;//python的控制模型是否构建完成
    private boolean iscomputecomplete = false;//运算是否完成


    /**模型真实运行状态*/


    /**
     * apc反馈的y0的预测值
     */
    private double[][] backsimulatorPVPrediction;//pv的预测曲线
    /**
     * apc反馈的pv的漏斗的上边界
     */
    private double[][] backsimulatorPVFunelUp;//PV的漏斗上限
    /**
     * apc反馈的pv的漏斗的下边界
     */
    private double[][] backsimulatorPVFunelDown;//PV的漏斗下限
    /**
     * apc反馈的dmv增量的预测值shape=(p,m)
     */
    private double[][] backsimulatorDmvWrite;
    private double[] backsimulatorrawDmv;
    private double[] backsimulatorrawDff;
    /**
     * apc反馈的y0与yreal的error预测值
     */
    private double[] backsimulatorPVPredictionError;//预测误差

    /**
     * 模型计算时候的前馈变换值dff shape=(p,num_ff)
     */
    private double[][] backDff;


    /**
     * 模型的标识token，用于apc仿真算法判别自己的算法是否已经过去，需要停止
     */
    private long simulatevalidkey = System.currentTimeMillis();
    /***********************仿真系统应用**********************/

    /**
     * 仿真标志位
     */
    private boolean issimulation = false;

    /**
     * 表示mv使用了哪些pv
     * mvusepv矩阵shape=(num_mv,num_pv)
     * 如mv1用了pv1,mv2用了pv2
     * 如[[1,0],
     * [0,1]]
     */
//    private int[][] matrixMvUsePv = null;


    /**
     * 表示FF使用了哪些pv
     * ffusepv矩阵shape=(num_ff,num_pv)
     * 如ff1用了pv1,ff2用了pv2
     * 如[[1,0],
     * [0,1]]
     */
//    private int[][] matrixFfUsePv = null;


    /**
     * 输入输出映射关系数量
     */
    private volatile int numOfIOMappingRelation = 0;


    /**
     * 前馈输出映射关系数量
     */
//    private int numOfFOMappingRelation = 0;

    /**
     * 输入响应 shape=[pv][mv][resp_N]
     */
    private double[][][] A_SimulatetimeseriseMatrix = null;
    /**
     * 前馈响应 shape=[pv][ff][resp_N]
     */
    private double[][][] B_SimulatetimeseriseMatrix = null;

    private int[][] matrixSimulatePvUseMv = null;


    private Double[] simulatQ;
    private Double[] simulatR;
    private Double[] simulateAlpheTrajectoryCoefficients;


    private String[] simulateAlpheTrajectoryCoefmethods;


    private Double[] simulatedeadZones;
    private Double[] simulatefunelinitvalues;
    private Double[][] simulatefunneltype;

    /**
     * 输出数量
     */
    private Integer simulateOutpoints_p = 0;
    /**
     * 前馈数量
     */
    private Integer simulateFeedforwardpoints_v = 0;
    /**
     * 输入量数量
     */
    private Integer simulateInputpoints_m = 0;


    /**
     * 预测时域
     */
    private Integer predicttime_P = 12;


    /**
     * 单一控制输入未来控制M步增量(控制域)
     */
    private Integer controltime_M = 6;


    /**
     * 响应序列长度
     */
    private Integer timeserise_N = 40;
    /**
     * 控制周期
     */
    private Integer controlAPCOutCycle = 0;


    /**
     * 模型仿真计算出的dmv数据
     * shape=(num_of_pv,num_of_mv)
     */
//    private double[][] backSimulateDmv;

    /**
     * 引脚
     */
    private List<MPCModleProperty> modlePins;


    public List<MPCModleProperty> getExtendModlePVPins() {
        return extendModlePVPins;
    }

    /**
     * 扩展引脚
     * 比如pv与mv的响应矩阵为:
     * 假设pv数量为p,mv数量为m
     * [ [pv1Tomv1 response shape=(N,1)],[..],...
     * [pv2Tomv1 response shape=(N,1)],[..],...
     * ....
     * ]
     * <p>
     * 扩展引脚是拆分把每行pv对应的每一个mv记入到扩extendModlePins
     * mv1    mv2
     * pv1  [pmr1],[pmr2],...
     * pv2  []     [pmr3],..
     * pv3 ....
     * 因此可以将这个pv1,pv1,pv2这样的顺序放入到extendModlePins中
     */
    private List<MPCModleProperty> extendModlePVPins = new ArrayList<>();


    public List<MPCModleProperty> getExtendModleSPPins() {
        return extendModleSPPins;
    }

    public void setExtendModleSPPins(List<MPCModleProperty> extendModleSPPins) {
        this.extendModleSPPins = extendModleSPPins;
    }

    private List<MPCModleProperty> extendModleSPPins = new ArrayList<>();


    public List<MPCModleProperty> getExtendModleMVPins() {
        return extendModleMVPins;
    }

    /**
     * 顺序与上述pv相对于
     */
    private List<MPCModleProperty> extendModleMVPins = new ArrayList<>();


//    private int[][] matrixPvUseMv = null;

    /**
     * 执行apc算法的仿真桥接器
     */
//    private ExecutePythonBridge executePythonBridgeSimulate;


    /*******************仿真属性结束**************************/


    public SimulatControlModle(int M, int P, int N, int controlAPCOutCycle, String simulatorbuilddir, Long modleid) {
        this.simulatorbuilddir = simulatorbuilddir;
        this.modleid = modleid;
        this.controltime_M = M;
        this.predicttime_P = P;
        this.timeserise_N = N;
        this.controlAPCOutCycle = controlAPCOutCycle;
    }

    /**
     * 获取输入输出响应
     */
    private double[] getSpecialIORespon(String pvpinname, String mvpinname) {
        for (ResponTimeSerise responTimeSerise : controlModle.getResponTimeSeriseList()) {
            if (responTimeSerise.getInputPins().equals(mvpinname) && responTimeSerise.getOutputPins().equals(pvpinname)) {
                return responTimeSerise.responOneTimeSeries(timeserise_N, controlAPCOutCycle);
            }
        }
        return null;
    }


    /**
     * 2020.11.12需求版本更新函数，保持原有的数据以外，
     * 将其归置到simulate上
     * 1、添加原始dmvlimit结构数据
     * 2、添加原始mv结构数据
     * 3\添加mvfb原始结构
     * 获取原始结构的mv相关数据，用于模型预测曲线计算、mv输出前的校验以及最终mv的叠加
     */
    public JSONObject getRealSimulateData() {

        try {
            /**
             * y0(pv)
             * limitU(mv)
             * limitY()
             * FF
             * Wi(sp)
             *
             * */
            JSONObject jsonObject = new JSONObject();
            //sp
            Double[] sp = new Double[simulateOutpoints_p];
            int loop = 0;
            for (MPCModleProperty sppin : extendModleSPPins) {
                sp[loop] = sppin.getValue();
                loop++;
            }
            jsonObject.put("wi", sp);

            //pv
            Double[] pv = new Double[simulateOutpoints_p];
            loop = 0;
            for (MPCModleProperty pvpin : extendModlePVPins) {
                /***
                 * 是否有滤波器，有则使用滤波器的值，不然就使用实时数据
                 * */
                pv[loop++] = pvpin.getValue();
            }
            jsonObject.put("y0", pv);


            controlModle.getPVRealData(jsonObject);

            //limitU输入限制
            Double[][] limitU = new Double[simulateInputpoints_m][2];
            Double[][] limitDU = new Double[simulateInputpoints_m][2];
            loop = 0;
            //U执行器当前给定
            Double[] U = new Double[simulateInputpoints_m];
            //U执行器当前反馈
            Double[] UFB = new Double[simulateInputpoints_m];

            for (MPCModleProperty mvpin : extendModleMVPins) {
                Double[] mvminmax = new Double[2];
                MPCModleProperty mvdown = mvpin.getDownLmt();
                MPCModleProperty mvup = mvpin.getUpLmt();

                mvminmax[0] = mvdown.getValue();

                mvminmax[1] = mvup.getValue();

                //执行器限制
                limitU[loop] = mvminmax;

                Double[] dmvminmax = new Double[2];
                dmvminmax[0] = mvpin.getDmvLow();
                dmvminmax[1] = mvpin.getDmvHigh();
                limitDU[loop] = dmvminmax;

                //执行器给定
                U[loop] = mvpin.getValue();
                UFB[loop] = mvpin.getFeedBack().getValue();
                loop++;

            }
            jsonObject.put("limitU", limitU);
            jsonObject.put("limitDU", limitDU);
            jsonObject.put("U", U);
            jsonObject.put("UFB", UFB);

            /**获取原始结构的mv相关数据，用于模型预测曲线计算、mv输出前的校验以及最终mv的叠加*/
            controlModle.getMVRelationData(jsonObject);

            //FF
            int indexEnableFF = 0;
            if (controlModle.getNumOfRunnableFFpins_vv() != 0) {
                Double[] ff = new Double[controlModle.getNumOfRunnableFFpins_vv()];
                Double[] fflmt = new Double[controlModle.getNumOfRunnableFFpins_vv()];
                for (int indexff = 0; indexff < controlModle.getCategoryFFmodletag().size(); indexff++) {
                    //是否FF是否可运行
                    if (controlModle.getMaskisRunnableFFMatrix()[indexff] == 0) {
                        continue;
                    }
                    ff[indexEnableFF] = controlModle.getCategoryFFmodletag().get(indexff).getValue();
                    MPCModleProperty ffuppin = controlModle.getCategoryFFmodletag().get(indexff).getUpLmt();
                    MPCModleProperty ffdownpin = controlModle.getCategoryFFmodletag().get(indexff).getDownLmt();

                    /**
                     *ff信号是否在置信区间内
                     * */
                    Double ffHigh = 0d;
                    Double ffLow = 0d;

                    ffHigh = ffuppin.getValue();
                    ffLow = ffdownpin.getValue();

                    if ((ffLow <= controlModle.getCategoryFFmodletag().get(indexff).getValue()) && (ffHigh >= controlModle.getCategoryFFmodletag().get(indexff).getValue())) {
                        fflmt[indexEnableFF] = 1d;
                    } else {
                        fflmt[indexEnableFF] = 0d;
                    }

                    indexEnableFF++;
                }

                jsonObject.put("FF", ff);
                jsonObject.put("FFLmt", fflmt);

            }

//            jsonObject.put("enable", isIssimulation() ? 1 : 0);

            /**
             *死区时间和漏斗初始值
             * */
            jsonObject.put("deadZones", getSimulatedeadZones());
            jsonObject.put("funelInitValues", getSimulatefunelinitvalues());
//            jsonObject.put("validekey", getSimulatevalidkey());

            jsonObject.put("msgtype", MSGTYPE_COMPUTE);
            return jsonObject;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }


    /**
     * 获取前馈输出响应
     */
    private double[] getSpecialFORespon(String pvpinname, String ffpinname) {
        for (ResponTimeSerise responTimeSerise : controlModle.getResponTimeSeriseList()) {
            if (responTimeSerise.getInputPins().equals(ffpinname) && responTimeSerise.getOutputPins().equals(pvpinname)) {
                return responTimeSerise.responOneTimeSeries(timeserise_N, controlAPCOutCycle);
            }
        }
        return null;
    }


    public JSONObject dataforpythonbuildsimulate() {
        JSONObject jsonObject = new JSONObject();

        /**
         *计算说明：
         * 1、根据mvUsePv矩阵、numOfMappingRelation映射关系数量，构建输入、输出、前馈引脚数目
         * 2、构建出输入输出映射关系矩阵
         *
         * 3、构建出前馈对输出的映射关系矩阵
         *
         * */


        /**
         * base
         * */
        SimulatControlModle simulatControlModle = controlModle.getSimulatControlModle();
        jsonObject.put("m", simulatControlModle.getSimulateInputpoints_m());//映射数量
        jsonObject.put("p", simulatControlModle.getSimulateOutpoints_p());//映射数量
        jsonObject.put("M", simulatControlModle.getControltime_M());
        jsonObject.put("P", simulatControlModle.getPredicttime_P());
        jsonObject.put("N", simulatControlModle.getTimeserise_N());
        jsonObject.put("fnum", simulatControlModle.getSimulateFeedforwardpoints_v());
        jsonObject.put("pvusemv", simulatControlModle.getMatrixSimulatePvUseMv());
        jsonObject.put("APCOutCycle", simulatControlModle.getControlAPCOutCycle());
//        jsonObject.put("enable", simulatControlModle.isIssimulation() ? 1 : 0);//是否仿真
//        jsonObject.put("validekey", simulatControlModle.getSimulatevalidkey());
        jsonObject.put("funneltype", simulatControlModle.getSimulatefunneltype());


        /**
         *mv
         * */
        if (controlModle.getNumOfRunnableMVpins_mm() != 0) {
            jsonObject.put("A", simulatControlModle.getA_SimulatetimeseriseMatrix());
            jsonObject.put("origionA", controlModle.getA_RunnabletimeseriseMatrix());
            /**
             * 添加映射矩阵
             * */
            jsonObject.put("pvmvmapping", controlModle.getMaskMatrixRunnablePVUseMV());
            jsonObject.put("pvmveffect", controlModle.getMaskMatrixRunnablePvEffectMv());//pv对mv输出占比影响权重

        }

        /**
         *ff
         */
        if (controlModle.getNumOfRunnableFFpins_vv() != 0) {
            jsonObject.put("B", simulatControlModle.getB_SimulatetimeseriseMatrix());
            jsonObject.put("origionB", controlModle.getB_RunnabletimeseriseMatrix());
            jsonObject.put("pvffmapping", controlModle.getMaskMatrixRunnablePVUseFF());
        }

        jsonObject.put("Q", simulatControlModle.getSimulatQ());
        jsonObject.put("R", simulatControlModle.getSimulatR());
        jsonObject.put("alphe", simulatControlModle.getSimulateAlpheTrajectoryCoefficients());//柔化系数
        jsonObject.put("alphemethod", simulatControlModle.getSimulateAlpheTrajectoryCoefmethods());//柔化系数
        jsonObject.put("msgtype", MSGTYPE_BUILD);
        return jsonObject;
    }


    @Override
    public void connect() {

    }

    @Override
    public void reconnect() {

    }

    @Override
    public void otherApcPlantRespon(int status){

    }

    @Override
    public void destory() {

    }

    @Override
    public void docomputeprocess() {

    }

    @Override
    public JSONObject inprocess() {
        return null;
    }

    @Override
    public JSONObject computresulteprocess(JSONObject computedata) {

        if (computedata.getJSONObject("data").getString("msgtype").equals(MSGTYPE_BUILD)) {
            pythonbuildcomplet = true;
        } else if (computedata.getJSONObject("data").getString("msgtype").equals(MSGTYPE_COMPUTE)) {
            try {

                JSONObject modlestatus = computedata.getJSONObject("data"); //JSONObject.parseObject(data);
                JSONArray predictpvJson = modlestatus.getJSONArray("predict");//
                JSONArray writemvJson = modlestatus.getJSONArray("writemv");//要进行写入
                JSONArray eJson = modlestatus.getJSONArray("e");//
                JSONArray funelupAnddownJson = modlestatus.getJSONArray("funelupAnddown");//
                JSONArray dmvJson = modlestatus.getJSONArray("dmv");//实时显示拆分以后的dmv,如mv-对pv1和pv2都有影响，则返回的是dmv[0]是pv1对mv1 dmv[2]是pv2对dmv1
                JSONArray dffJson = modlestatus.getJSONArray("dff");//
                JSONArray writedmvJson = modlestatus.getJSONArray("writedmv");


                int p = controlModle.getNumOfRunnablePVPins_pp();
                int m = controlModle.getNumOfRunnableMVpins_mm();
                int m_mapping = controlModle.getSimulatControlModle().getSimulateInputpoints_m();
                int v = controlModle.getNumOfRunnableFFpins_vv();
                int N = controlModle.getTimeserise_N();

                double[] predictpvArray = new double[p * N];
                double[][] funelupAnddownArray = new double[2][p * N];
                double[] eArray = new double[p];
                double[] dmvArray = new double[m_mapping];
                double[] writedmvArray = new double[m];

                double[] dffArray = null;
                if (v != 0) {
                    dffArray = new double[v];
                }
                for (int i = 0; i < p * N; i++) {
                    predictpvArray[i] = predictpvJson.getDouble(i);
                    funelupAnddownArray[0][i] = funelupAnddownJson.getJSONArray(0).getDouble(i);
                    funelupAnddownArray[1][i] = funelupAnddownJson.getJSONArray(1).getDouble(i);
                }

                for (int i = 0; i < p; i++) {
                    eArray[i] = eJson.getDouble(i);
                }

                for (int i = 0; i < m_mapping; i++) {
                    dmvArray[i] = dmvJson.getDouble(i);
                }

                for (int i = 0; i < m; i++) {
                    writedmvArray[i] = writedmvJson.getDouble(i);
                }


                for (int i = 0; i < v; ++i) {
                    dffArray[i] = dffJson.getDouble(i);
                }
                controlModle.getSimulatControlModle().updateModleComputeResult(predictpvArray, funelupAnddownArray, writedmvArray, dmvArray, eArray, dffArray);

//                writemvJson;//要进行写入
//                writedmvJson;

                if (controlModle.getRunstyle().equals(RUNSTYLEBYMANUL)) {
                    int index = 0;
                    List<MPCModleProperty> runablemv = controlModle.getRunablePins(controlModle.getCategoryMVmodletag(), controlModle.getMaskisRunnableMVMatrix());
                    for (MPCModleProperty mpcModleProperty : runablemv) {

                        String outputpinname = mpcModleProperty.getModlePinName();
                        double outmvvalue = writemvJson.getDouble(index);
//                        double outdmvvalue = writedmvJson.getDouble(index);
                        index++;
                        for (ModleProperty modleProperty : controlModle.getPropertyImpList()) {
                            MPCModleProperty outputpin = (MPCModleProperty) modleProperty;
                            //找到输出的引脚
                            if (outputpin.getPindir().equals(MPCModleProperty.PINDIROUTPUT)) {

                                //赋值mv
                                if (outputpinname.equals(outputpin.getModlePinName())) {
                                    outputpin.setValue(outmvvalue);
                                } else {
                                    //dmv
                                    for (int indexpv = 0; indexpv < extendModlePVPins.size(); indexpv++) {
                                        /**拆分计算的dPViMVi值*/
                                        if (("d" + extendModlePVPins.get(indexpv).getModlePinName() + extendModleMVPins.get(indexpv).getModlePinName()).equals(outputpin.getModlePinName())) {
                                            outputpin.setValue(dmvArray[indexpv]);
                                        }
                                    }
                                }


                            }

                        }
                    }
                }

                iscomputecomplete = true;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }


        return null;
    }



    @Override
    public void outprocess( JSONObject outdata) {
        setModlerunlevel(BaseModleImp.RUNLEVEL_RUNCOMPLET);
        setActivetime(Instant.now());
    }

    @Override
    public void init(Map<Long, BridgeInfo> bridgeCache) {
        //do nothing
    }

    /**
     * 计算说明：
     * 1、根据mvUsePv矩阵、numOfMappingRelation映射关系数量，构建输入、输出、前馈引脚数目
     * 2、构建出输入输出映射关系矩阵
     * 3、构建出前馈对输出的映射关系矩阵
     * 4、漏斗类型等
     */
    public void build() {

        /***
         * 输入输出响应对应矩阵
         * init A matrix
         * 数据填入方式是按照pv-mv映射矩阵的行顺序进行填入
         * */
        if (numOfIOMappingRelation == 0) {
            //映射数量为0，那就不需要构建了，构建也是报错的
            return;
        }
        A_SimulatetimeseriseMatrix = new double[numOfIOMappingRelation][numOfIOMappingRelation][timeserise_N];

        /**pv用了哪些mv,标记矩阵*/
        matrixSimulatePvUseMv = new int[numOfIOMappingRelation][numOfIOMappingRelation];

        /**仿真R
         * 这里仿真的pv的数量肯定要和mv数量相同的，因为仿真的时候是把pv与mv一条映射线作为一个仿真单元
         * */
        simulatR = new Double[numOfIOMappingRelation];

        /**仿真Q参数*/
        simulatQ = new Double[numOfIOMappingRelation];
        /**仿真轨迹柔化系数*/
        simulateAlpheTrajectoryCoefficients = new Double[numOfIOMappingRelation];
        simulateAlpheTrajectoryCoefmethods = new String[numOfIOMappingRelation];
        /**仿真死区*/
        simulatedeadZones = new Double[numOfIOMappingRelation];
        /**仿真漏斗初始值*/
        simulatefunelinitvalues = new Double[numOfIOMappingRelation];
        /**仿真漏斗类型*/
        simulatefunneltype = new Double[numOfIOMappingRelation][2];

        int index4IOMappingRelation = 0;
        for (int indexpv = 0; indexpv < controlModle.getCategoryPVmodletag().size(); ++indexpv) {

            /**没有可运行的pv直接跳过*/
            if (0 == controlModle.getMaskisRunnablePVMatrix()[indexpv]) {
                continue;
            }

            for (int indexmv = 0; indexmv < controlModle.getCategoryMVmodletag().size(); ++indexmv) {

                /**激活的pv没有用到的mv直接跳过*/
                if (controlModle.getMaskisRunnableMVMatrix()[indexmv] == 0) {
                    continue;
                }

                double[] ioRespon = getSpecialIORespon(controlModle.getCategoryPVmodletag().get(indexpv).getModlePinName(), controlModle.getCategoryMVmodletag().get(indexmv).getModlePinName());
                if ((ioRespon != null) && (index4IOMappingRelation < numOfIOMappingRelation)) {
                    /**重构的响应矩阵*/
                    A_SimulatetimeseriseMatrix[index4IOMappingRelation][index4IOMappingRelation] = ioRespon;
                    /**重构剥离的pv用了哪些mv*/
                    matrixSimulatePvUseMv[index4IOMappingRelation][index4IOMappingRelation] = 1;

                    /**控制域参数*/
                    simulatR[index4IOMappingRelation] = controlModle.getCategoryMVmodletag().get(indexmv).getR();

                    /**预测域系数*/
                    simulatQ[index4IOMappingRelation] = controlModle.getCategoryPVmodletag().get(indexpv).getQ();
                    /**柔化系数*/
                    simulateAlpheTrajectoryCoefficients[index4IOMappingRelation] = controlModle.getCategoryPVmodletag().get(indexpv).getReferTrajectoryCoef();
                    simulateAlpheTrajectoryCoefmethods[index4IOMappingRelation] = controlModle.getCategoryPVmodletag().get(indexpv).getTracoefmethod();
                    /**死区*/
                    simulatedeadZones[index4IOMappingRelation] = controlModle.getCategoryPVmodletag().get(indexpv).getDeadZone();
                    /**漏斗初始值*/
                    simulatefunelinitvalues[index4IOMappingRelation] = controlModle.getCategoryPVmodletag().get(indexpv).getFunelinitValue();
                    /**漏斗类型*/
                    Double[] fnl = new Double[2];
                    if (controlModle.getCategoryPVmodletag().get(indexpv).getFunneltype() != null) {
                        switch (controlModle.getCategoryPVmodletag().get(indexpv).getFunneltype()) {
                            case MPCModleProperty.TYPE_FUNNEL_FULL:
                                /**全漏斗*/
                                fnl[0] = 0d;
                                fnl[1] = 0d;
                                simulatefunneltype[index4IOMappingRelation] = fnl;
                                break;
                            case MPCModleProperty.TYPE_FUNNEL_UP:
                                /**上漏斗*/
                                fnl[0] = 0d;
                                //乘负无穷
                                fnl[1] = 1d;
                                simulatefunneltype[index4IOMappingRelation] = fnl;
                                break;
                            case MPCModleProperty.TYPE_FUNNEL_DOWN:
                                /**下漏斗*/
                                //乘正无穷
                                fnl[0] = 1d;
                                fnl[1] = 0d;
                                simulatefunneltype[index4IOMappingRelation] = fnl;
                                break;
                            default:
                                /**匹配不到就是全漏斗*/
                                fnl = new Double[2];
                                fnl[0] = 0d;
                                fnl[1] = 0d;
                                simulatefunneltype[index4IOMappingRelation] = fnl;
                        }
                    } else {
                        /**匹配不到就是全漏斗*/
                        fnl[0] = 0d;
                        fnl[1] = 0d;
                        simulatefunneltype[index4IOMappingRelation] = fnl;
                    }
                    extendModlePVPins.add(controlModle.getCategoryPVmodletag().get(indexpv));
                    extendModleMVPins.add(controlModle.getCategoryMVmodletag().get(indexmv));
                    extendModleSPPins.add(controlModle.getCategorySPmodletag().get(indexpv));
                    ++index4IOMappingRelation;
                }
            }
        }


        /***
         * 前馈输出对应矩阵
         * init B matrix
         * */
        if (controlModle.getNumOfRunnableFFpins_vv() > 0) {
            B_SimulatetimeseriseMatrix = new double[numOfIOMappingRelation][controlModle.getNumOfRunnableFFpins_vv()][timeserise_N];
            /**
             *fill respon into 前馈与输出 响应matrix
             *填入前馈输出响应矩阵扩展矩阵
             * */
            for (int indexExpv = 0; indexExpv < numOfIOMappingRelation; ++indexExpv) {
                /*扩展矩阵*/
                int indexEnbaleFF = 0;
                for (int indexff = 0; indexff < controlModle.getCategoryFFmodletag().size(); ++indexff) {

                    if (controlModle.getMaskisRunnableFFMatrix()[indexff] == 0) {
                        continue;
                    }

                    double[] foRespon = getSpecialFORespon(extendModlePVPins.get(indexExpv).getModlePinName(), controlModle.getCategoryFFmodletag().get(indexff).getModlePinName());
                    if (foRespon != null) {
                        B_SimulatetimeseriseMatrix[indexExpv][indexEnbaleFF] = foRespon;
                    }
                    ++indexEnbaleFF;
                }

            }

        }

        simulateOutpoints_p = numOfIOMappingRelation;
        simulateInputpoints_m = numOfIOMappingRelation;
        simulateFeedforwardpoints_v = controlModle.getNumOfRunnableFFpins_vv();


        backsimulatorPVPrediction = new double[controlModle.getNumOfRunnablePVPins_pp()][timeserise_N];//pv的预测曲线

        backsimulatorPVFunelUp = new double[controlModle.getNumOfRunnablePVPins_pp()][timeserise_N];//PV的漏斗上曲线

        backsimulatorPVFunelDown = new double[controlModle.getNumOfRunnablePVPins_pp()][timeserise_N];//漏斗下曲线

        backsimulatorDmvWrite = new double[controlModle.getNumOfRunnablePVPins_pp()][controlModle.getNumOfRunnableMVpins_mm()];//MV写入值
        backsimulatorrawDmv = new double[controlModle.getNumOfRunnableMVpins_mm()];
        backsimulatorrawDff = new double[controlModle.getNumOfRunnableFFpins_vv()];

        backsimulatorPVPredictionError = new double[controlModle.getNumOfRunnablePVPins_pp()];//预测误差

        backDff = new double[controlModle.getNumOfRunnablePVPins_pp()][controlModle.getNumOfRunnableFFpins_vv()];//前馈变换值
        javabuildcomplet = true;
//        backSimulateDmv = new double[controlModle.getNumOfRunnablePVPins_pp()][controlModle.getNumOfRunnableMVpins_mm()];
//        generateSimulatevalidkey();
        //executePythonBridgeSimulate= new ExecutePythonBridge(simulatorbuilddir, "http://localhost:8080/AILab/pythonsimulate/modlebuild/" + modleid + ".do", modleid + "");
    }

//    public void simulateModleRun(){
//        if(!isIssimulation()){
//            generateSimulatevalidkey();
//            executePythonBridgeSimulate.execute();
//            setIssimulation(true);
//        }
//    }
//
//    public void simulateModleStop(){
//        if(isIssimulation()){
//            generateSimulatevalidkey();
//            executePythonBridgeSimulate.stop();
//            setIssimulation(false);
//        }
//    }


    public synchronized boolean isIssimulation() {
        return issimulation;
    }

    public synchronized void setIssimulation(boolean issimulation) {
        this.issimulation = issimulation;
    }


    public synchronized int getNumOfIOMappingRelation() {
        return numOfIOMappingRelation;
    }

    /**
     * add num of pv and mv mapping ralationship
     */
    public synchronized void addNumOfIOMappingRelation() {
        ++numOfIOMappingRelation;
    }


    public int[][] getMatrixSimulatePvUseMv() {
        return matrixSimulatePvUseMv;
    }

    public long getSimulatevalidkey() {
        return simulatevalidkey;
    }

    public void generateSimulatevalidkey() {
        this.simulatevalidkey = System.currentTimeMillis();
    }


    public void setNumOfIOMappingRelation(int numOfIOMappingRelation) {
        this.numOfIOMappingRelation = numOfIOMappingRelation;
    }


    public double[][][] getA_SimulatetimeseriseMatrix() {
        return A_SimulatetimeseriseMatrix;
    }

    public void setA_SimulatetimeseriseMatrix(double[][][] a_SimulatetimeseriseMatrix) {
        A_SimulatetimeseriseMatrix = a_SimulatetimeseriseMatrix;
    }

    public double[][][] getB_SimulatetimeseriseMatrix() {
        return B_SimulatetimeseriseMatrix;
    }

    public void setB_SimulatetimeseriseMatrix(double[][][] b_SimulatetimeseriseMatrix) {
        B_SimulatetimeseriseMatrix = b_SimulatetimeseriseMatrix;
    }

    public void setMatrixSimulatePvUseMv(int[][] matrixSimulatePvUseMv) {
        this.matrixSimulatePvUseMv = matrixSimulatePvUseMv;
    }

    public Double[] getSimulatQ() {
        return simulatQ;
    }

    public void setSimulatQ(Double[] simulatQ) {
        this.simulatQ = simulatQ;
    }

    public Double[] getSimulatR() {
        return simulatR;
    }

    public void setSimulatR(Double[] simulatR) {
        this.simulatR = simulatR;
    }

    public Double[] getSimulateAlpheTrajectoryCoefficients() {
        return simulateAlpheTrajectoryCoefficients;
    }

    public void setSimulateAlpheTrajectoryCoefficients(Double[] simulateAlpheTrajectoryCoefficients) {
        this.simulateAlpheTrajectoryCoefficients = simulateAlpheTrajectoryCoefficients;
    }

    public Double[] getSimulatedeadZones() {
        return simulatedeadZones;
    }

    public void setSimulatedeadZones(Double[] simulatedeadZones) {
        this.simulatedeadZones = simulatedeadZones;
    }

    public Double[] getSimulatefunelinitvalues() {
        return simulatefunelinitvalues;
    }

    public void setSimulatefunelinitvalues(Double[] simulatefunelinitvalues) {
        this.simulatefunelinitvalues = simulatefunelinitvalues;
    }


    public Double[][] getSimulatefunneltype() {
        return simulatefunneltype;
    }

    public void setSimulatefunneltype(Double[][] simulatefunneltype) {
        this.simulatefunneltype = simulatefunneltype;
    }

    public Integer getSimulateOutpoints_p() {
        return simulateOutpoints_p;
    }

    public void setSimulateOutpoints_p(Integer simulateOutpoints_p) {
        this.simulateOutpoints_p = simulateOutpoints_p;
    }

    public Integer getSimulateFeedforwardpoints_v() {
        return simulateFeedforwardpoints_v;
    }

    public void setSimulateFeedforwardpoints_v(Integer simulateFeedforwardpoints_v) {
        this.simulateFeedforwardpoints_v = simulateFeedforwardpoints_v;
    }

    public Integer getSimulateInputpoints_m() {
        return simulateInputpoints_m;
    }

    public void setSimulateInputpoints_m(Integer simulateInputpoints_m) {
        this.simulateInputpoints_m = simulateInputpoints_m;
    }


    public List<MPCModleProperty> getModlePins() {
        return modlePins;
    }

    public void setModlePins(List<MPCModleProperty> modlePins) {
        this.modlePins = modlePins;
    }

    public String[] getSimulateAlpheTrajectoryCoefmethods() {
        return simulateAlpheTrajectoryCoefmethods;
    }

    public void setSimulateAlpheTrajectoryCoefmethods(String[] simulateAlpheTrajectoryCoefmethods) {
        this.simulateAlpheTrajectoryCoefmethods = simulateAlpheTrajectoryCoefmethods;
    }


    public Integer getTimeserise_N() {
        return timeserise_N;
    }

    public void setTimeserise_N(Integer timeserise_N) {
        this.timeserise_N = timeserise_N;
    }

    public Integer getPredicttime_P() {
        return predicttime_P;
    }

    public void setPredicttime_P(Integer predicttime_P) {
        this.predicttime_P = predicttime_P;
    }

    public Integer getControltime_M() {
        return controltime_M;
    }

    public void setControltime_M(Integer controltime_M) {
        this.controltime_M = controltime_M;
    }

    public Integer getControlAPCOutCycle() {
        return controlAPCOutCycle;
    }

    public void setControlAPCOutCycle(Integer controlAPCOutCycle) {
        this.controlAPCOutCycle = controlAPCOutCycle;
    }


//    public ExecutePythonBridge getExecutePythonBridgeSimulate() {
//        return executePythonBridgeSimulate;
//    }
//
//    public void setExecutePythonBridgeSimulate(ExecutePythonBridge executePythonBridgeSimulate) {
//        this.executePythonBridgeSimulate = executePythonBridgeSimulate;
//    }

//    public double[][] getBackSimulateDmv() {
//        return backSimulateDmv;
//    }

    /**
     * 更新仿真器dmv值
     */
//    public boolean updateBackSimulateComputeResult(double[] simulateDmv) {
//        int indexmappingration = 0;
//        try {
//            for (int indexpv = 0; indexpv < controlModle.getNumOfRunnablePVPins_pp(); indexpv++) {
//
//                for (int indexmv = 0; indexmv < controlModle.getNumOfRunnableMVpins_mm(); indexmv++) {
//                    if (controlModle.getMaskMatrixRunnablePVUseMV()[indexpv][indexmv] == 1) {
//                        this.backSimulateDmv[indexpv][indexmv] = simulateDmv[indexmappingration];
//                        ++indexmappingration;
//                    }
//                }
//            }
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            return false;
//        }
//        return true;
//    }


    /**
     * 更新模型计算后的数据
     *
     * @param funelupAnddown   尺寸：2XpN
     *                         第0行存漏斗的上限；[0~N-1]第一个pv的，[N~2N-1]为第二个pv的漏斗上限
     *                         第1行存漏斗的下限；
     * @param backPVPrediction
     */
    public boolean updateModleComputeResult(double[] backPVPrediction, double[][] funelupAnddown, double[] backWritedmv, double[] backdmv, double[] backPVPredictionError, double[] dff) {
        /**
         * 模型运算状态值
         * */
        try {
            for (int i = 0; i < controlModle.getNumOfRunnablePVPins_pp(); i++) {
                this.backsimulatorPVPrediction[i] = Arrays.copyOfRange(backPVPrediction, 0 + timeserise_N * i, timeserise_N + timeserise_N * i);//pv的预测曲线
                this.backsimulatorPVFunelUp[i] = Arrays.copyOfRange(funelupAnddown[0], 0 + timeserise_N * i, timeserise_N + timeserise_N * i);//PV的漏斗上限
                this.backsimulatorPVFunelDown[i] = Arrays.copyOfRange(funelupAnddown[1], 0 + timeserise_N * i, timeserise_N + timeserise_N * i);//PV的漏斗下限
            }

            /**预测误差*/
            this.backsimulatorPVPredictionError = backPVPredictionError;
            this.backsimulatorrawDmv = backWritedmv;
            this.backsimulatorrawDff = dff;
            int indexmappingration = 0;
            for (int indexpv = 0; indexpv < controlModle.getNumOfRunnablePVPins_pp(); indexpv++) {

                /**dMV写入值*/
                for (int indexmv = 0; indexmv < controlModle.getNumOfRunnableMVpins_mm(); indexmv++) {
                    if (controlModle.getMaskMatrixRunnablePVUseMV()[indexpv][indexmv] == 1) {
                        this.backsimulatorDmvWrite[indexpv][indexmv] = backdmv[indexmappingration];
                        indexmappingration++;
                    }
                }

                /**前馈增量*/
                for (int indexff = 0; indexff < controlModle.getNumOfRunnableFFpins_vv(); indexff++) {
                    if (controlModle.getMaskMatrixRunnablePVUseFF()[indexpv][indexff] == 1) {
                        this.backDff[indexpv][indexff] = dff[indexff];
                    }
                }

            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    public double[][] getBacksimulatorPVPrediction() {
        return backsimulatorPVPrediction;
    }

    public double[][] getBacksimulatorPVFunelUp() {
        return backsimulatorPVFunelUp;
    }

    public double[][] getBacksimulatorPVFunelDown() {
        return backsimulatorPVFunelDown;
    }

    public double[][] getBacksimulatorDmvWrite() {
        return backsimulatorDmvWrite;
    }

    public double[] getBacksimulatorrawDmv() {
        return backsimulatorrawDmv;
    }

    public double[] getBacksimulatorrawDff() {
        return backsimulatorrawDff;
    }

    public double[] getBacksimulatorPVPredictionError() {
        return backsimulatorPVPredictionError;
    }

    public double[][] getBackDff() {
        return backDff;
    }

    public void setControlModle(MPCModle controlModle) {
        this.controlModle = controlModle;
    }


    public boolean isPythonbuildcomplet() {
        return pythonbuildcomplet;
    }

    public void setPythonbuildcomplet(boolean pythonbuildcomplet) {
        this.pythonbuildcomplet = pythonbuildcomplet;
    }

    public boolean isJavabuildcomplet() {
        return javabuildcomplet;
    }

    public void setJavabuildcomplet(boolean javabuildcomplet) {
        this.javabuildcomplet = javabuildcomplet;
    }

    public boolean isIscomputecomplete() {
        return iscomputecomplete;
    }

    public void setIscomputecomplete(boolean iscomputecomplete) {
        this.iscomputecomplete = iscomputecomplete;
    }
}
