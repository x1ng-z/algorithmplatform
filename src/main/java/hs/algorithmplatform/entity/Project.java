package hs.algorithmplatform.entity;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import hs.algorithmplatform.entity.model.BaseModleImp;
import hs.algorithmplatform.entity.model.Modle;
import hs.algorithmplatform.entity.model.controlmodle.MPCModle;
import hs.algorithmplatform.entity.model.controlmodle.PIDModle;
import hs.algorithmplatform.entity.model.customizemodle.CUSTOMIZEModle;
import hs.algorithmplatform.entity.model.filtermodle.FilterModle;
import hs.algorithmplatform.entity.model.iomodle.INModle;
import hs.algorithmplatform.entity.model.iomodle.OUTModle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/8 17:09
 */
public class Project implements Runnable {
    private Logger logger = LoggerFactory.getLogger(Project.class);

    /**
     * memory
     */
    private Map<Long, Modle> indexmodles;
    private boolean projectrun = true;

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted() && projectrun) {

            try {
                for (Modle modle : modleList) {
                    if (modle instanceof MPCModle) {
                        MPCModle mpcModle = (MPCModle) modle;
                        if(mpcModle.ismpcmodleruncomplet()){
                            continue;
                        }
                        JSONArray parents = mpcModle.getModleSight().getParents();
                        boolean isneedrun = true;
                        for (int index = 0; index < parents.size(); index++) {
                            JSONObject parent = parents.getJSONObject(index);
                            int parentid = parent.getInteger("id");
                            BaseModleImp parentmodle = (BaseModleImp) indexmodles.get(parentid);
                            if (parentmodle instanceof MPCModle) {
                                isneedrun = isneedrun && ((MPCModle) parentmodle).ismpcmodleruncomplet();
                            } else {
                                isneedrun = isneedrun && (parentmodle.getModlerunlevel() == BaseModleImp.RUNLEVEL_RUNCOMPLET);
                            }
                            if (!isneedrun) {
                                break;
                            }
                        }
                        //父节点全部运行完成的条件下，如果mpc运行处于初始化状态下或者mpcModle不为空的时候，simultor也是运行状态处于初始状态下，就达到运算条件
                        if (isneedrun && ((mpcModle.getModlerunlevel() == BaseModleImp.RUNLEVEL_INITE) /**|| (mpcModle.getSimulatControlModle() != null ? (mpcModle.getSimulatControlModle().getModlerunlevel() == BaseModleImp.RUNLEVEL_INITE) : true)*/)) {
                            //根节点设置开始运行时间
                            if (mpcModle.getModleSight().getParents().size() == 0) {
                                mpcModle.setBeginruntime(Instant.now());
                            }

                            mpcModle.inprocess(this);
                            mpcModle.docomputeprocess();
                        }
                        continue;
                    } else if (modle instanceof PIDModle) {
                        PIDModle pidModle = (PIDModle) modle;
                        if(pidModle.getModlerunlevel()==BaseModleImp.RUNLEVEL_RUNCOMPLET){
                            continue;
                        }
                        JSONArray parents = pidModle.getModleSight().getParents();
                        boolean isneedrun = true;
                        for (int index = 0; index < parents.size(); index++) {
                            JSONObject parent = parents.getJSONObject(index);
                            int parentid = parent.getInteger("id");
                            BaseModleImp parentmodle = (BaseModleImp) indexmodles.get(parentid);
                            if (parentmodle instanceof MPCModle) {
                                isneedrun = isneedrun && ((MPCModle) parentmodle).ismpcmodleruncomplet();
                            } else {
                                isneedrun = isneedrun && (parentmodle.getModlerunlevel() == BaseModleImp.RUNLEVEL_RUNCOMPLET);
                            }
                            if (!isneedrun) {
                                break;
                            }

                        }
                        if (isneedrun && (pidModle.getModlerunlevel() == BaseModleImp.RUNLEVEL_INITE)) {
                            if (pidModle.getModleSight().getParents().size() == 0) {
                                pidModle.setBeginruntime(Instant.now());
                            }
                            pidModle.inprocess(this);
                            pidModle.docomputeprocess();
                        }
                        continue;
                    } else if (modle instanceof CUSTOMIZEModle) {
                        CUSTOMIZEModle customizeModle = (CUSTOMIZEModle) modle;
                        if(customizeModle.getModlerunlevel()==BaseModleImp.RUNLEVEL_RUNCOMPLET){
                            continue;
                        }
                        JSONArray parents = customizeModle.getModleSight().getParents();
                        boolean isneedrun = true;
                        for (int index = 0; index < parents.size(); index++) {
                            JSONObject parent = parents.getJSONObject(index);
                            int parentid = parent.getInteger("id");
                            BaseModleImp parentmodle = (BaseModleImp) indexmodles.get(parentid);
                            if (parentmodle instanceof MPCModle) {
                                isneedrun = isneedrun && ((MPCModle) parentmodle).ismpcmodleruncomplet();
                            } else {
                                isneedrun = isneedrun && (parentmodle.getModlerunlevel() == BaseModleImp.RUNLEVEL_RUNCOMPLET);
                            }
                            if (!isneedrun) {
                                break;
                            }
                        }
                        if (isneedrun && (customizeModle.getModlerunlevel() == BaseModleImp.RUNLEVEL_INITE)) {
                            if (customizeModle.getModleSight().getParents().size() == 0) {
                                customizeModle.setBeginruntime(Instant.now());
                            }
                            customizeModle.inprocess(this);
                            customizeModle.docomputeprocess();
                        }
                        continue;
                    } else if (modle instanceof FilterModle) {
                        FilterModle filterModle = (FilterModle) modle;
                        if(filterModle.getModlerunlevel()==BaseModleImp.RUNLEVEL_RUNCOMPLET){
                            continue;
                        }
                        JSONArray parents = filterModle.getModleSight().getParents();
                        boolean isneedrun = true;
                        for (int index = 0; index < parents.size(); index++) {
                            JSONObject parent = parents.getJSONObject(index);
                            int parentid = parent.getInteger("id");
                            BaseModleImp parentmodle = (BaseModleImp) indexmodles.get(parentid);
                            if (parentmodle instanceof MPCModle) {
                                isneedrun = isneedrun && ((MPCModle) parentmodle).ismpcmodleruncomplet();
                            } else {
                                isneedrun = isneedrun && (parentmodle.getModlerunlevel() == BaseModleImp.RUNLEVEL_RUNCOMPLET);
                            }
                            if (!isneedrun) {
                                break;
                            }
                        }
                        //                    System.out.println("projectid="+filterModle.getModleName()+"  isneedrun="+isneedrun+"  "+ filterModle.getModlerunlevel()+"");

                        if (isneedrun && (filterModle.getModlerunlevel() == BaseModleImp.RUNLEVEL_INITE)) {
                            if (filterModle.getModleSight().getParents().size() == 0) {
                                filterModle.setBeginruntime(Instant.now());
                            }
                            filterModle.inprocess(this);
                            filterModle.docomputeprocess();
                        }
                        continue;
                    } else if (modle instanceof INModle) {
                        INModle inModle = (INModle) modle;
                        if(inModle.getModlerunlevel()==BaseModleImp.RUNLEVEL_RUNCOMPLET){
                            continue;
                        }
                        JSONArray parents = inModle.getModleSight().getParents();
                        boolean isneedrun = true;
                        for (int index = 0; index < parents.size(); index++) {
                            JSONObject parent = parents.getJSONObject(index);
                            int parentid = parent.getInteger("id");
                            BaseModleImp parentmodle = (BaseModleImp) indexmodles.get(parentid);
                            if (parentmodle instanceof MPCModle) {
                                isneedrun = isneedrun && ((MPCModle) parentmodle).ismpcmodleruncomplet();
                            } else {
                                isneedrun = isneedrun && (parentmodle.getModlerunlevel() == BaseModleImp.RUNLEVEL_RUNCOMPLET);
                            }
                            if (!isneedrun) {
                                break;
                            }
                        }
                        //System.out.println("projectid=" + inModle.getModleName() + "  isneedrun=" + isneedrun + "  " + inModle.getModlerunlevel() + "");

                        if (isneedrun && (inModle.getModlerunlevel() == BaseModleImp.RUNLEVEL_INITE)) {
                            if (inModle.getModleSight().getParents().size() == 0) {
                                inModle.setBeginruntime(Instant.now());
                            }
                            JSONObject inputdata = inModle.inprocess(this);
                            //                        System.out.println("inModle "+inputdata.toJSONString());
                            inModle.docomputeprocess();
                            inModle.outprocess(this, inputdata);
                        }
                        continue;
                    } else if (modle instanceof OUTModle) {

                        OUTModle outModle = (OUTModle) modle;
                        if(outModle.getModlerunlevel()==BaseModleImp.RUNLEVEL_RUNCOMPLET){
                            continue;
                        }
                        JSONArray parents = outModle.getModleSight().getParents();
                        boolean isneedrun = true;
                        for (int index = 0; index < parents.size(); index++) {
                            JSONObject parent = parents.getJSONObject(index);
                            int parentid = parent.getInteger("id");
                            BaseModleImp parentmodle = (BaseModleImp) indexmodles.get(parentid);
                            if (parentmodle instanceof MPCModle) {
                                isneedrun = isneedrun && ((MPCModle) parentmodle).ismpcmodleruncomplet();
                            } else {
                                isneedrun = isneedrun && (parentmodle.getModlerunlevel() == BaseModleImp.RUNLEVEL_RUNCOMPLET);
                            }
                            if (!isneedrun) {
                                break;
                            }
                        }

                        if (isneedrun && (outModle.getModlerunlevel() == BaseModleImp.RUNLEVEL_INITE)) {
                            if (outModle.getModleSight().getParents().size() == 0) {
                                outModle.setBeginruntime(Instant.now());
                            }
                            outModle.inprocess(this);
                            //                        System.out.println("outModle ");
                            outModle.docomputeprocess();
                            outModle.outprocess(this, null);
                        }
                        continue;
                    }

                }


                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
//                if (allcomplete) {

                try {
                    for (Modle modle : modleList) {
                        BaseModleImp leafmodle = (BaseModleImp) modle;
                        //是否为叶节点
                        if (leafmodle.getModleSight().getChilds().size() == 0) {
                            if (leafmodle.getModlerunlevel() == BaseModleImp.RUNLEVEL_RUNCOMPLET) {
                                //已经完成状态的叶节点
                                List<BaseModleImp> rootmodles = new ArrayList<>();
                                //查找到所有的根节点
                                findRootModle(rootmodles, leafmodle.getModleId());
                                Instant earliest = Instant.now();
                                //找到运行完成最早的根节点
                                if (rootmodles.size() != 0) {
                                    for (BaseModleImp rootmodle : rootmodles) {
                                        if (rootmodle.getBeginruntime() != null) {
                                            if (rootmodle.getBeginruntime().isBefore(earliest)) {
                                                earliest = rootmodle.getBeginruntime();
                                            }
                                        }
                                    }
                                }
                                //最早的根节点开始运行时间是不是和现在相差一个调度时间了
                                if (earliest.plusMillis(Double.valueOf(getRunperiod() * 1000).longValue()).isBefore(Instant.now())) {
                                    //根据叶子模块是的运行完成，去重置输出模块父节点上的运行状态,采用深度优先遍历
                                    JSONArray outparents = leafmodle.getModleSight().getParents();
                                    for (int index = 0; index < outparents.size(); index++) {
                                        reSetParentsModleRunLevel(outparents.getJSONObject(index).getInteger("id"));
                                    }
                                    leafmodle.setModlerunlevel(BaseModleImp.RUNLEVEL_INITE);

                                }

                            }


                        }
                    }
//                        TimeUnit.MILLISECONDS.sleep(Double.valueOf(getRunperiod() * 1000).intValue());
                } catch (Exception e) {
                    return;
                }
//                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }

    }

    public void init() {
        indexmodles = new HashMap<>();
        for (Modle modle : modleList) {
            BaseModleImp baseModleImp = (BaseModleImp) modle;
            indexmodles.put(baseModleImp.getModleId(), baseModleImp);
        }
    }


    /**
     * db
     */
    private int projectid;
    private String name;
    private double runperiod;

    private List<Modle> modleList;

    public int getProjectid() {
        return projectid;
    }

    public void setProjectid(int projectid) {
        this.projectid = projectid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getRunperiod() {
        return runperiod;
    }

    public void setRunperiod(double runperiod) {
        this.runperiod = runperiod;
    }

    public List<Modle> getModleList() {
        return modleList;
    }

    public void setModleList(List<Modle> modleList) {
        this.modleList = modleList;
    }

    public Map<Long, Modle> getIndexmodles() {
        return indexmodles;
    }

    public void setIndexmodles(Map<Long, Modle> indexmodles) {
        this.indexmodles = indexmodles;
    }

    public boolean isProjectrun() {
        return projectrun;
    }

    public void setProjectrun(boolean projectrun) {
        this.projectrun = projectrun;
    }

    //深度优先遍历
    private void reSetParentsModleRunLevel(int parentmodleid) {
        BaseModleImp baseModleImp = (BaseModleImp) indexmodles.get(parentmodleid);
        if (baseModleImp == null) {
            return;
        }
        //根节点运行完成
        if (baseModleImp.getModlerunlevel() == BaseModleImp.RUNLEVEL_RUNCOMPLET) {
//            if (baseModleImp instanceof MPCModle) {
//                MPCModle mpcModle = (MPCModle) baseModleImp;
//                if(mpcModle.getSimulatControlModle()!=null){
//                    mpcModle.setModlerunlevel(BaseModleImp.RUNLEVEL_INITE);
//                }
//            }
            baseModleImp.setModlerunlevel(BaseModleImp.RUNLEVEL_INITE);
            JSONArray parents = baseModleImp.getModleSight().getParents();
            for (int index = 0; index < parents.size(); index++) {
                this.reSetParentsModleRunLevel(parents.getJSONObject(index).getInteger("id"));
            }

        }

    }

    //深度优先遍历根节点模型
    private void findRootModle(List<BaseModleImp> rootmodle, long parentmodleid) {
        BaseModleImp baseModleImp = (BaseModleImp) indexmodles.get(parentmodleid);
        if (baseModleImp == null) {
            return;
        }
        JSONArray parents = baseModleImp.getModleSight().getParents();
        if (parents.size() == 0) {
            rootmodle.add(baseModleImp);
            return;
        } else {
            for (int index = 0; index < parents.size(); index++) {
                this.findRootModle(rootmodle, parents.getJSONObject(index).getInteger("id"));
            }
        }

    }

}
