package hs.algorithmplatform.pydriver.pyproxyserve;


import com.alibaba.fastjson.JSONObject;

import hs.algorithmplatform.manager.ModleManager;

import hs.algorithmplatform.entity.model.BaseModleImp;
import hs.algorithmplatform.entity.model.Modle;
import hs.algorithmplatform.entity.model.controlmodle.MPCModle;
import hs.algorithmplatform.entity.model.controlmodle.PIDModle;
import hs.algorithmplatform.entity.model.customizemodle.CUSTOMIZEModle;
import hs.algorithmplatform.entity.model.filtermodle.FilterModle;
import hs.algorithmplatform.pydriver.command.CommandImp;
import hs.algorithmplatform.pydriver.session.PySession;
import hs.algorithmplatform.pydriver.session.PySessionManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Arrays;


@ChannelHandler.Sharable
@Component
public class MsgDecoder_Inbound extends ChannelInboundHandlerAdapter {

    private Logger logger = LoggerFactory.getLogger(MsgDecoder_Inbound.class);

    @Autowired
    private PySessionManager sessionManager;


    private ModleManager modleManager;

    public MsgDecoder_Inbound() {
        super();
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        InetSocketAddress ipSocket = (InetSocketAddress) ctx.channel().remoteAddress();

        String clientIp = ipSocket.getAddress().getHostAddress();
        Integer port = ipSocket.getPort();
        logger.info("come in " + clientIp + ":" + port);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        InetSocketAddress ipSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        String clientIp = ipSocket.getAddress().getHostAddress();
        Integer port = ipSocket.getPort();
        PySession pySession = sessionManager.removeSessionModule(ctx);
        if (pySession != null) {
            logger.info("come out" + clientIp + ":" + port + " modleid=" + pySession.getModleid() + " scriptName=" + pySession.getScriptName());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
//            InetSocketAddress ipSocket = (InetSocketAddress) ctx.channel().remoteAddress();
//            String clientIp = ipSocket.getAddress().getHostAddress();
//            Integer port = ipSocket.getPort();
//            String ipAndPort = clientIp + ":" + port;
////            logger.info(ipAndPort);
            ByteBuf wait_for_read = (ByteBuf) msg;
            if (wait_for_read.isReadable()) {
                byte[] bytes = new byte[wait_for_read.readableBytes()];
                wait_for_read.readBytes(bytes);
                //提取命令
                byte[] opcserveidarray = Arrays.copyOfRange(bytes, 3, 11);//header
                long modleid = byteToLong(opcserveidarray,0,false);
                byte[] command = Arrays.copyOfRange(bytes, 2, 3);//cmd
                JSONObject paramjson = null;
                switch (command[0]) {
                    case 0x01: {
                        if (CommandImp.RESULT.valid(bytes)) {
                            JSONObject computeresult = CommandImp.RESULT.analye(bytes);
                            logger.info(computeresult.toJSONString());
                            Modle modle = modleManager.getspecialModle(modleid);
                            if (computeresult.getString("msg").equals("error")) {
                                BaseModleImp baseModleImp = (BaseModleImp) modle;
                                baseModleImp.setErrormsg(computeresult.getString("reason"));
                                baseModleImp.setErrortimestamp(computeresult.getLong("errortimestamp"));
                                baseModleImp.setActivetime(Instant.now());

                                if (modle instanceof MPCModle) {
                                    MPCModle mpcmodle = (MPCModle) modle;
                                    if (mpcmodle.getSimulatControlModle() != null) {
                                        mpcmodle.setActivetime(Instant.now());
                                    }
                                    if (mpcmodle.getModlerunlevel() == BaseModleImp.RUNLEVEL_RUNNING) {
                                        //在运行中控制权交由当前线程处理
                                        mpcmodle.otherApcPlantRespon(123456);
                                    } else {
                                        //python构建完成，但是计算的时候报错了
                                        baseModleImp.setModlerunlevel(BaseModleImp.RUNLEVEL_PYTHONFAILD);
                                    }

                                } else {
                                    baseModleImp.otherApcPlantRespon(123456);
                                }

                            } else {
                                BaseModleImp baseModleImp = (BaseModleImp) modle;
                                baseModleImp.setErrormsg("");//清除错误消息
                                if (modle instanceof MPCModle) {
                                    MPCModle mpcModle = (MPCModle) modle;
                                    if (computeresult.getString("scriptName").equals(mpcModle.getMpcscript())) {

                                        mpcModle.computresulteprocess(null, computeresult);
                                        if (!computeresult.getJSONObject("data").getString("msgtype").equals(MPCModle.MSGTYPE_BUILD)) {
                                            mpcModle.outprocess(null, null);
                                        }


                                    } else if (computeresult.getString("scriptName").equals(mpcModle.getSimulatorscript())) {
                                        mpcModle.getSimulatControlModle().computresulteprocess(null, computeresult);
                                        if (!computeresult.getJSONObject("data").getString("msgtype").equals(MPCModle.MSGTYPE_BUILD)) {
                                            mpcModle.getSimulatControlModle().outprocess(null, null);
                                        }

                                    }
                                    break;
                                } else if (modle instanceof PIDModle) {
                                    PIDModle pidModle = (PIDModle) modle;
                                    if (computeresult.getString("scriptName").equals(pidModle.getPidscript())) {
                                        pidModle.computresulteprocess(null, computeresult);
                                        pidModle.outprocess(null, null);
                                    }
                                    break;
                                } else if (modle instanceof CUSTOMIZEModle) {
                                    CUSTOMIZEModle customizeModle = (CUSTOMIZEModle) modle;
                                    if (computeresult.getString("scriptName").equals(customizeModle.noscripNametail())) {
                                        customizeModle.computresulteprocess(null, computeresult);
                                        customizeModle.outprocess(null, null);
                                    }
                                    break;
                                } else if (modle instanceof FilterModle) {
                                    FilterModle filterModle = (FilterModle) modle;
                                    if (computeresult.getString("scriptName").equals(filterModle.getFilterscript())) {
                                        filterModle.computresulteprocess(null, computeresult);
                                        filterModle.outprocess(null, null);
                                    }
                                    break;
                                }
                            }


                        }
                        break;
                    }

                    case 0x03: {
                        if (CommandImp.HEART.valid(bytes)) {
                            JSONObject heartmsg = CommandImp.HEART.analye(bytes);
                            logger.info(heartmsg.toJSONString());
                            sessionManager.addSessionModule(modleid, heartmsg.getString("scriptName"), ctx);
                            Modle modle = modleManager.getspecialModle(modleid);
                            if (modle != null) {
                                if (modle instanceof MPCModle) {
                                    MPCModle mpcModle = (MPCModle) modle;
                                    if (mpcModle.getSimulatControlModle() != null) {
                                        mpcModle.setActivetime(Instant.now());
                                    }
                                }
                                BaseModleImp baseModleImp = (BaseModleImp) modle;
                                baseModleImp.setActivetime(Instant.now());
                            }

                        }
                        break;
                    }

                    case 0x04: {
                        if (CommandImp.ACK.valid(bytes)) {
                            logger.info(CommandImp.ACK.analye(bytes).toJSONString());
                        }
                        break;
                    }

                    case 0x05: {
                        if (CommandImp.STOP.valid(bytes)) {
                            logger.info(CommandImp.STOP.analye(bytes).toJSONString());
                        }
                        break;
                    }
                    default:
                        logger.warn("no match any command");
                        break;
                }
            }


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            ReferenceCountUtil.release(msg);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        logger.error("have error in MsgDecoder_Inbound");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        if (IdleStateEvent.class.isAssignableFrom(evt.getClass())) {
            IdleStateEvent event = (IdleStateEvent) evt;

            InetSocketAddress ipSocket = (InetSocketAddress) ctx.channel().remoteAddress();
            String clientIp = ipSocket.getAddress().getHostAddress();
            IdleStateEvent stateEvent = (IdleStateEvent) evt;

            switch (stateEvent.state()) {
                case READER_IDLE:
                    logger.info(clientIp + "Read Idle");
                    break;
                case WRITER_IDLE:
                    logger.info(clientIp + "Read Idle");
                    break;
                case ALL_IDLE:
                    logger.info(clientIp + "Read Idle");
                    break;
                default:
                    break;
            }
        }
    }

    private long byteToLong(byte[] input, int offset, boolean littleEndian) {
        long value = 0;
        // 循环读取每个字节通过移位运算完成long的8个字节拼装
        for (int count = 0; count < 8; ++count) {
            int shift = (littleEndian ? count : (7 - count)) << 3;
            value |= ((long) 0xff << shift) & ((long) input[offset + count] << shift);
        }
        return value;
    }


    public void setModleManager(ModleManager modleManager) {
        this.modleManager = modleManager;
    }
}
