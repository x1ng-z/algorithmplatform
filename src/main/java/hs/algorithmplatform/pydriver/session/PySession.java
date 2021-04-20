package hs.algorithmplatform.pydriver.session;

import io.netty.channel.ChannelHandlerContext;
import lombok.Data;

/**
 * @author zzx
 * @version 1.0
 * @date 2020/12/29 23:05
 */
@Data
public class PySession {
   // private Module object;//apc module;
    private ChannelHandlerContext ctx;
    private long modleid;
    private String scriptName;//不含.py
    private long timestamp;//connected timestamp;

}
