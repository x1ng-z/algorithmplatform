package hs.algorithmplatform.entity.DTO.pid.respon;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/3/10 0:27
 */
public class PidRespon {
    private PidData data;
    private String message;
    private int status;

    public PidData getData() {
        return data;
    }

    public void setData(PidData data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
