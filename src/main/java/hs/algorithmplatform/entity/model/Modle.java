package hs.algorithmplatform.entity.model;


import hs.algorithmplatform.entity.Connector;

/**
 * @author zzx
 * @version 1.0
 * @date 2021/1/8 16:08
 */
public interface Modle extends Process, Connector {
    String MODLETYPE_INPUT = "input";
    String MODLETYPE_OUTPUT = "output";
    String MODLETYPE_FILTER = "filter";
    String MODLETYPE_CUSTOMIZE = "customize";
    String MODLETYPE_MPC = "mpc";
    String MODLETYPE_PID = "pid";

}
