package org.seckill.exception;

/**
 * 秒杀业务相关异常
 * Created by Siglam Lee on 2017/7/21.
 */
public class SeckillException extends RuntimeException{
    public SeckillException(String message) {
        super(message);
    }

    public SeckillException(String message, Throwable cause) {
        super(message, cause);
    }
}
