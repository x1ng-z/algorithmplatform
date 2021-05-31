package hs.algorithmplatform.entity.bean;

import hs.algorithmplatform.utils.bridge.InputStreamRunnable;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/5/31 10:59
 */
@Data
@Builder
public class BridgeInfo {
    private Logger logger = LoggerFactory.getLogger(BridgeInfo.class);
    private Long id;
    private Process process;
    private String exeName;

    /**查询是否可以进行读
     *
     */
    public AvailableResult isNeedRead(){
        if(process.isAlive()){
            InputStream inputStream =process.getInputStream();
            if(inputStream==null){
                return AvailableResult.builder().isReady(false).build();
            }
            try {
                int noBlockReadSize=inputStream.available();
                if(noBlockReadSize>0){
                    return AvailableResult.builder().isReady(true).availableCount(noBlockReadSize).build();
                }
            } catch (IOException e) {
                logger.error(e.getMessage(),e);
                return AvailableResult.builder().isReady(false).build();
            }
        }
        return AvailableResult.builder().isReady(false).build();
    }


    /**
     * 读取数据
     * @param availableResult
     * */
    public void readStdOutOrErr(AvailableResult availableResult){
        InputStream inputStream =process.getInputStream();
        if(availableResult.getIsReady()){
            byte[] buf=new byte[availableResult.getAvailableCount()];
            try {
                int readcount=inputStream.read(buf);
                if(readcount>=0){
                    logger.info(new String(buf, StandardCharsets.UTF_8));
                }
            } catch (IOException e) {
                logger.error(e.getMessage(),e);
            }
        }
    }


}
