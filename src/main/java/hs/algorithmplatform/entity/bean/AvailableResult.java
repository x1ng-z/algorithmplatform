package hs.algorithmplatform.entity.bean;

import lombok.Builder;
import lombok.Data;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/5/31 1:52
 */
@Data
@Builder
public class AvailableResult {
    private Boolean isReady;
    private Integer availableCount;
}
