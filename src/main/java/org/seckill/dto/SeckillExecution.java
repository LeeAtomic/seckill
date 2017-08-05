package org.seckill.dto;

import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnum;

/**
 * 封装秒杀执行后的结果
 * Created by Siglam Lee on 2017/7/20.
 */
public class SeckillExecution {
    private long seckilId;
    //秒杀执行结果状态
    private int state;
    //状态标识
    private String stateInfo;
    //秒杀成功对象
    private SuccessKilled successKilled;

    public SeckillExecution() {
    }

    public SeckillExecution(long seckilId, SeckillStateEnum seckillStateEnum, SuccessKilled successKilled) {
        this.seckilId = seckilId;
        this.state = seckillStateEnum.getState();
        this.stateInfo = seckillStateEnum.getStateInfo();
        this.successKilled = successKilled;
    }

    public SeckillExecution(long seckilId, SeckillStateEnum seckillStateEnum) {
        this.seckilId = seckilId;
        this.state = seckillStateEnum.getState();
        this.stateInfo = seckillStateEnum.getStateInfo();
    }

    public void setSeckilId(long seckilId) {
        this.seckilId = seckilId;
    }

    public void setState(int state) {
        this.state = state;
    }

    public void setStateInfo(String stateInfo) {
        this.stateInfo = stateInfo;
    }

    public void setSuccessKilled(SuccessKilled successKilled) {
        this.successKilled = successKilled;
    }

    public long getSeckilId() {

        return seckilId;
    }

    public int getState() {
        return state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public SuccessKilled getSuccessKilled() {
        return successKilled;
    }

    @Override
    public String toString() {
        return "SeckillExecution{" +
                "state=" + state +
                ", stateInfo='" + stateInfo + '\'' +
                ", successKilled=" + successKilled +
                '}';
    }
}
