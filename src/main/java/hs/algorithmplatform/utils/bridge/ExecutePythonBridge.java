package hs.algorithmplatform.utils.bridge;

import hs.algorithmplatform.entity.bean.BridgeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ExecutePythonBridge {
    private Logger logger = LoggerFactory.getLogger(ExecutePythonBridge.class);
    public Process p = null;
    Thread result = null;
    Thread error = null;
    private List<String> args = new ArrayList<>();
    private String exename;
    private Long id;
    private Map<Long, BridgeInfo> bridgeCache;


    /***
     * @param id 组件id
     * @param exename 执行脚本
     * @param args 附带参数
     * */
    public ExecutePythonBridge(Long id, Map<Long, BridgeInfo> bridgeCache, String exename, String... args) {
        this.id = id;
        this.bridgeCache = bridgeCache;
        this.exename = exename;
        this.args.add(exename);
        for (String arg : args) {
            this.args.add(arg);
        }
    }

    public synchronized boolean stop() {
        logger.info("try to stop manul!");
        if (p != null) {
            removeExperialBridgeInfo();
            /**
             *result.interrupt();
             * error.interrupt();
             * */
            p = null;
        }
        return true;
    }

    public synchronized boolean execute() {
        if (p != null) {
            //程序已经在执行了
            return true;
        }
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(args);
            processBuilder.directory(new File(System.getProperty("user.dir")));
            processBuilder.redirectErrorStream(true);//合并stdout和stderr这两个输出流。
            p = processBuilder.start();

            BridgeInfo newbriedgeinfo = BridgeInfo.builder().process(p).id(id).exeName(exename).build();
            bridgeCache.put(id, newbriedgeinfo);
            /**
             * result = new Thread(new InputStreamRunnable(p.getInputStream(), "Result", null));
             *             result.setDaemon(true);
             *             result.start();
             *                为"错误输出流"单独开一个线程读取之,否则会造成标准输出流的阻塞
             *              error = new Thread(new InputStreamRunnable(p.getErrorStream(), "ErrorStream", null));
             *             error.setDaemon(true);
             *             error.start();
             * */
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.error("exename " + exename);
            return false;
        }
        return true;
    }


    private void removeExperialBridgeInfo(){
        BridgeInfo bridgeInfo = bridgeCache.remove(id);
        if (bridgeInfo != null) {
            try {
                bridgeInfo.getProcess().getInputStream().close();
            } catch (IOException e) {
                logger.error(e.getMessage(),e);
            }

            try {
                bridgeInfo.getProcess().destroy();
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
        }
    }

}


