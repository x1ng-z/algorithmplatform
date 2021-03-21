package hs.algorithmplatform.utils.bridge;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ExecutePythonBridge {
    private Logger logger = LoggerFactory.getLogger(ExecutePythonBridge.class);
    public Process p = null;
    Thread result = null;
    Thread error = null;
    private List<String> args = new ArrayList<>();
    private String exename;


    public ExecutePythonBridge(String exename, String... args) {
        this.exename = exename;
        this.args.add(exename);
        for (String arg : args) {
            this.args.add(arg);
        }
    }

    public boolean stop() {
        if (p != null) {
            p.destroy();
//            result.interrupt();
//            error.interrupt();
            p = null;
        }

        return true;
    }

    public boolean execute() {
//        logger.info("*****exename 1" + exename);
        if (p != null) {
            return true;
        }
        //LinkedBlockingQueue<String> linkedBlockingQueue=new LinkedBlockingQueue();
//        BufferedReader bReader = null;
//        InputStreamReader sReader = null;
//        logger.info("****exename 2" + exename);
        try {
            ProcessBuilder processBuilder=new ProcessBuilder(args);
            processBuilder.directory(new File(System.getProperty("user.dir")));
            p=processBuilder.start();
//            p = Runtime.getRuntime().exec(new String[]{exename, ip,
//                    port,
//                    opcsevename,
//                    opcseveip,
//                    opcsevid});
            result = new Thread(new InputStreamRunnable(p.getInputStream(), "Result", null));
            result.setDaemon(true);
            result.start();

            /* 为"错误输出流"单独开一个线程读取之,否则会造成标准输出流的阻塞 */
            error = new Thread(new InputStreamRunnable(p.getErrorStream(), "ErrorStream", null));
            error.setDaemon(true);
            error.start();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logger.error("exename " + exename);
            return false;
        }
        return true;
    }

}


