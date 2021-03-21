package hs.algorithmplatform.pydriver.session;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/12/29 23:04
 */
@Component
public class PySessionManager {
    private Logger logger = LoggerFactory.getLogger(PySessionManager.class);

    private Map<ChannelHandlerContext, PySession> modulepool = new ConcurrentHashMap<>();

    public synchronized void addSessionModule(long nodeid, String function, ChannelHandlerContext ctx) {
        if (!modulepool.containsKey(ctx)) {
            PySession session = new PySession();
            session.setCtx(ctx);
            session.setScriptName(function);
            session.setModleid(nodeid);
            modulepool.put(ctx, session);
        }

    }


    public synchronized PySession removeSessionModule(ChannelHandlerContext ctx) {
        if (ctx != null) {
            return modulepool.remove(ctx);
        }
        return null;
    }

    public synchronized PySession getSpecialSession(long modleid, String scriptname){

        for(PySession session:modulepool.values()){
            if(session.getModleid()==modleid&&session.getScriptName().equals(scriptname)){
                return session;
            }
        }
        return null;
    }

    public Map<ChannelHandlerContext, PySession> getModulepool() {
        return modulepool;
    }
}
