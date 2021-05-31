package hs.algorithmplatform.entity.model;

import com.alibaba.fastjson.JSONObject;
import hs.algorithmplatform.entity.ModleSight;
import hs.algorithmplatform.entity.bean.BridgeInfo;
import lombok.Data;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/8 16:46
 */
@Data
public abstract class BaseModleImp implements Modle {
    public static final int RUNLEVEL_RUNNING=1;//正在运行中
    public static final int RUNLEVEL_RUNCOMPLET=2;//运行完成
    public static final int RUNLEVEL_INITE=3;//初始状态
    public static final int RUNLEVEL_PYTHONFAILD=8;//python运行报错
    public static final int RUNLEVEL_DISCONNECT=9;//断线
    public static final int RUNLEVEL_JAVAMODLEBUILDCOMPLET=5;//java模型构建完成状态
    public static final int RUNLEVEL_PYTHONMODLEBUILDCOMPLET=4;//python模型构建状态


    /**memory*/
    private volatile int modlerunlevel=RUNLEVEL_INITE;
    private String errormsg="";
    private long errortimestamp;
    private int autovalue=1;
    private Instant beginruntime;//模型开始运行时间，用于重置模型运行状态
    private Instant activetime;//用于判断模型是否已经离线

    private LinkedBlockingQueue<DeferredResult<String>> syneventLinkedBlockingQueue = new LinkedBlockingQueue();
    private  AtomicBoolean cancelrun = new AtomicBoolean(false);

    /***db***/
    private long modleId;//模型id主键
    private String modleName;//模型名称
    private int modleEnable=1;//模块使能，用于设置算法是否运行，算法是否运行
    private String modletype;
    private int refprojectid;
    /*****/

    private ModleSight modleSight;//模型视图

    @Override
    public abstract void connect();
    @Override
    public abstract void reconnect();

    /**
     * 模型销毁
     * */
    @Override
    public abstract void destory() ;
    @Override
    public abstract void docomputeprocess() ;

    @Override
    public abstract JSONObject inprocess() ;

    @Override
    public abstract JSONObject computresulteprocess(JSONObject computedata) ;

    @Override
    public abstract  void outprocess(JSONObject outdata) ;

    @Override
    public abstract void init(Map<Long, BridgeInfo> bridgeCache);

    /**
     * 响应反馈
     * */
    public abstract void otherApcPlantRespon(int status);

}
