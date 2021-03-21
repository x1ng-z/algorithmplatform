package hs.algorithmplatform.manager;

import hs.algorithmplatform.entity.model.BaseModleImp;
import hs.algorithmplatform.entity.model.Modle;
import hs.algorithmplatform.entity.model.controlmodle.MPCModle;
import hs.algorithmplatform.entity.model.controlmodle.PIDModle;
import hs.algorithmplatform.entity.model.customizemodle.CUSTOMIZEModle;
import hs.algorithmplatform.entity.model.filtermodle.FilterModle;
import hs.algorithmplatform.entity.model.iomodle.INModle;
import hs.algorithmplatform.entity.model.iomodle.OUTModle;
import hs.algorithmplatform.pydriver.pyproxyserve.IOServer;
import hs.algorithmplatform.pydriver.session.PySessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/26 8:24
 */
@Component
public class ModleManager {
    private Logger logger = LoggerFactory.getLogger(ModleManager.class);


    private PySessionManager pySessionManager;
    private String pyproxyexecute;

    private int mpcpinnumber;

    private String filterscript;


    private String pidscript;


    private String simulatorscript;


    private String mpcscrip;


    private String oceandir;


    private String pydriverport;


    /***memory**/
    private Map<Long, Modle> modlePool = new ConcurrentHashMap();

    private ExecutorService executethreadpool;
    private IOServer ioServer;


    @Autowired
    public ModleManager(
            IOServer ioServer,
            PySessionManager pySessionManager,
//            ProjectOperaterImp projectOperaterImp,
            @Value("${pyproxyexecute}") String pyproxyexecute,
            @Value("${mpcpinnumber}") int mpcpinnumber,
            @Value("${filterscript}") String filterscript,
            @Value("${pidscript}") String pidscript,
            @Value("${simulatorscript}") String simulatorscript,
            @Value("${mpcscrip}") String mpcscrip,
            @Value("${oceandir}") String oceandir,
            @Value("${pydriverport}") String pydriverport,
            @Qualifier("executethreadpool") ExecutorService executethreadpool
    ) {
        this.executethreadpool = executethreadpool;
        this.pySessionManager = pySessionManager;
//        this.projectOperaterImp = projectOperaterImp;
        this.pyproxyexecute = pyproxyexecute;
        this.mpcpinnumber = mpcpinnumber;
        this.filterscript = filterscript;
        this.pidscript = pidscript;
        this.simulatorscript = simulatorscript;
        this.mpcscrip = mpcscrip;
        this.oceandir = oceandir;
        this.pydriverport = pydriverport;
        this.ioServer = ioServer;
        ioServer.getNettyServerInitializer().msgDecoder_inbound.setModleManager(this);
    }


    @PreDestroy
    public void destory() {
        for (Modle modle : modlePool.values()) {
                BaseModleImp baseModleImp = (BaseModleImp) modle;
                if (baseModleImp != null) {
                    baseModleImp.destory();
                }
        }
    }

    public void activeModle(Modle modle) {
        if (modle != null) {
                if (modle instanceof MPCModle) {
                    MPCModle mpcmodle = (MPCModle) modle;
                    mpcmodle.toBeRealModle(
                            this.mpcscrip,
                            this.simulatorscript,
                            this.mpcpinnumber,
                            this.pySessionManager,
                            this.pydriverport,
                            this.pyproxyexecute);
                    mpcmodle.init();
                    mpcmodle.connect();

                } else if (modle instanceof PIDModle) {
                    PIDModle pidmodle = (PIDModle) modle;
                    pidmodle.toBeRealModle(this.pySessionManager, this.pidscript, this.pydriverport, this.pyproxyexecute);
                    pidmodle.init();
                    pidmodle.connect();

                } else if (modle instanceof CUSTOMIZEModle) {
                    CUSTOMIZEModle customizeModle = (CUSTOMIZEModle) modle;
                    customizeModle.toBeRealModle(this.pySessionManager, this.pydriverport, this.pyproxyexecute);
                    customizeModle.init();
                    customizeModle.connect();
                } else if (modle instanceof FilterModle) {
                    FilterModle filterModle = (FilterModle) modle;
                    filterModle.toBeRealModle(this.pySessionManager, this.filterscript, this.pydriverport, this.pyproxyexecute);
                    filterModle.init();
                    filterModle.connect();
                } else if (modle instanceof INModle) {
                    INModle inModle = (INModle) modle;
                    inModle.toBeRealModle(this.oceandir);
                    inModle.init();
                    inModle.connect();
                } else if (modle instanceof OUTModle) {
                    OUTModle outModle = (OUTModle) modle;
                    outModle.toBeRealModle(this.oceandir);
                    outModle.init();
                    outModle.connect();
                }
            modlePool.put(((BaseModleImp)modle).getModleId(),modle);
        }
    }

    public void stopAndRemoveModle(long modleid){
        Modle modle=modlePool.remove(modleid);
        ((BaseModleImp)modle).destory();

    }



    public Modle getspecialModle(long modleid) {
        return modlePool.get(modleid);
    }





}
