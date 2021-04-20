package hs.algorithmplatform.pydriver.session;

import io.netty.channel.ChannelHandlerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

    public synchronized void addSessionModule(long nodeid, String function, long timestamp, ChannelHandlerContext ctx) {
        //remove old connect
        List<ChannelHandlerContext> expriedconn = new ArrayList<>();
        for (Map.Entry<ChannelHandlerContext, PySession> entry : modulepool.entrySet()) {
            if (!entry.getKey().equals(ctx)) {
                if (entry.getValue().getModleid() == nodeid && entry.getValue().getScriptName().equals(function)) {
                    expriedconn.add(entry.getKey());
                }
            }
        }

        for (ChannelHandlerContext deletctx : expriedconn) {
            PySession pySession = modulepool.remove(deletctx);
            if (pySession.getCtx() != null) {
                pySession.getCtx().close();
            }
        }
        //put it
        if (!modulepool.containsKey(ctx)) {
            PySession newsession = new PySession();
            newsession.setCtx(ctx);
            newsession.setScriptName(function);
            newsession.setModleid(nodeid);
            newsession.setTimestamp(timestamp);
            modulepool.put(ctx, newsession);
        }

    }


    public synchronized PySession removeSessionModule(ChannelHandlerContext ctx) {
        if (ctx != null) {
            return modulepool.remove(ctx);
        }
        return null;
    }

    public synchronized PySession getSpecialSession(long modleid, String scriptname) {

        for (PySession session : modulepool.values()) {
            if (session.getModleid() == modleid && session.getScriptName().equals(scriptname)) {
                return session;
            }
        }
        return null;
    }

    public Map<ChannelHandlerContext, PySession> getModulepool() {
        return modulepool;
    }
}
