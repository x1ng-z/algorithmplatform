package hs.algorithmplatform.entity.model.filter;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/9 8:49
 */
public class FirstOrderLagFilter  implements Filter {
    /******db**/
    private int filterid;
    private int refmodleid;
    private String filtermethod;
    private double filteralphe;
    /************/
//    private List<ModleProperty> propertyImpList;




    public double getFilteralphe() {
        return filteralphe;
    }

    public void setFilteralphe(double filteralphe) {
        this.filteralphe = filteralphe;
    }


//    public List<ModleProperty> getPropertyImpList() {
//        return propertyImpList;
//    }
//
//    public void setPropertyImpList(List<ModleProperty> propertyImpList) {
//        this.propertyImpList = propertyImpList;
//    }

    public String getFiltermethod() {
        return filtermethod;
    }

    public void setFiltermethod(String filtermethod) {
        this.filtermethod = filtermethod;
    }

    public int getFilterid() {
        return filterid;
    }

    public void setFilterid(int filterid) {
        this.filterid = filterid;
    }

    public int getRefmodleid() {
        return refmodleid;
    }

    public void setRefmodleid(int refmodleid) {
        this.refmodleid = refmodleid;
    }
}
