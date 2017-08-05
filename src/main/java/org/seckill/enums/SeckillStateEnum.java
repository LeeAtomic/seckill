package org.seckill.enums;

/**
 * 建议使用枚举类来表述常量数据字典
 */
public enum SeckillStateEnum {
    SUCCESS(1, "秒杀成功"),
    END(0, "秒杀结束"),
    REPEAT_KILL(-1, "重复秒杀"),
    INNER_ERROR(-2, "系统异常"),
    DATA_REWRITE(-3, "数据篡改");

    private int state;
    private String stateInfo;

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public void setStateInfo(String stateInfo) {
        this.stateInfo = stateInfo;
    }

    SeckillStateEnum() {

    }

    SeckillStateEnum(int state, String stateInfo) {

        this.state = state;
        this.stateInfo = stateInfo;
    }

    public static SeckillStateEnum stateOf(int index) {
        for (SeckillStateEnum seckillStateEnum : values()) {
            if (seckillStateEnum.getState() == index) {
                return seckillStateEnum;
            }
        }
        return null;
    }
}
