package hs.algorithmplatform.entity.DTO.dmc.request;

import org.hibernate.validator.constraints.Length;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/8 14:27
 */
@Component
public class DmcOutproperty {

    private String outputpinname;

    public String getOutputpinname() {
        return outputpinname;
    }

    public void setOutputpinname(String outputpinname) {
        this.outputpinname = outputpinname;
    }
}
