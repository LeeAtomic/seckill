package org.seckill.exception;

/**
 * 重复秒杀异常（运行时异常，spring声明式事务只接收运行时异常事务回滚策略）
 * Created by Siglam Lee on 2017/7/20.
 */
public class RepeatKillException extends SeckillException {

    public RepeatKillException(String message) {
        super(message);
    }

    public RepeatKillException(String message, Throwable cause) {
        super(message, cause);
    }
}
