package org.seckill.entity;

import java.util.Date;

/**
 * Created by Siglam Lee on 2017/7/18.
 */
public class SuccessKilled {
    private long seckillId;
    private long userPhone;
    private short state;
    private Date createTime;
    //变通
    //多对一
    private Seckill seckill;

    @Override
    public String toString() {
        return "SuccessKilled{" +
                "seckillId=" + seckillId +
                ", userPhone=" + userPhone +
                ", state=" + state +
                ", createTime=" + createTime +
                '}';
    }

    public void setSeckillId(long seckillId) {
        this.seckillId = seckillId;
    }

    public void setUserPhone(long userPhone) {
        this.userPhone = userPhone;
    }

    public void setState(short state) {
        this.state = state;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public long getSeckillId() {

        return seckillId;
    }

    public long getUserPhone() {
        return userPhone;
    }

    public short getState() {
        return state;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setSeckill(Seckill seckill) {
        this.seckill = seckill;
    }

    public Seckill getSeckill() {

        return seckill;
    }
}
