package hs.algorithmplatform.entity.model.filter;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/9 8:50
 */
public class MoveAverageFilter  implements Filter {
    /******db**/

    private int filterid;
    private int refmodleid;
    private String filtermethod;
    private int filtercapacity;//队列容量移动平均滤波时间
    /************/

//    private List<ModleProperty> propertyImpList;



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

    public int getFiltercapacity() {
        return filtercapacity;
    }

    public void setFiltercapacity(int filtercapacity) {
        this.filtercapacity = filtercapacity;
    }
}
