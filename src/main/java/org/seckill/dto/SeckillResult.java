package org.seckill.dto;

/**
 * 所有ajax请求的返回类型。封装json结果
 *
 * @param <T>
 */
public class SeckillResult<T> {
    //请求是否成功
    private boolean success;
    //泛型类型的数据
    private T data;
    //错误信息
    private String errer;

    public SeckillResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public SeckillResult(boolean success, String errer) {

        this.success = success;
        this.errer = errer;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getErrer() {
        return errer;
    }

    public void setErrer(String errer) {
        this.errer = errer;
    }

    public SeckillResult() {
    }

    public SeckillResult(boolean success, T data, String errer) {

        this.success = success;
        this.data = data;
        this.errer = errer;
    }
}
