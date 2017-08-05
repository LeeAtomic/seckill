package org.seckill.dao.cache;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisDao {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
    private final JedisPool jedisPool;
    //根据传入的类的字节码创建对象序列化模板，赋予相应的值（参数的Class必须是一个标准的POJO类）
    private RuntimeSchema<Seckill> schema = RuntimeSchema.createFrom(Seckill.class);

    public RedisDao(String ip, int port) {
        jedisPool = new JedisPool(ip, port);
    }

    /**
     * 根据ID从redis中获取秒杀对象
     *
     * @param seckillId
     * @return
     */
    public Seckill getSeckill(long seckillId) {
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "seckill:" + seckillId;
                /**
                 redis需要手动实现内部序列化操作
                 get-->byte[]-->反序列化-->Object(Seckill)
                 采用性能最好的序列化工具：protoStuff;POJO
                 **/
                //获取对象的字节数组
                byte[] bytes = jedis.get(key.getBytes());
                if (bytes != null) {
                    //创建一个序列化类的空对象
                    Seckill seckill = schema.newMessage();
                    //下列是使用ProtostuffIOUtil根据schema为seckill空对象赋值
                    ProtostuffIOUtil.mergeFrom(bytes, seckill, schema);
                    //seckill被反序列化
                    return seckill;
                }
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 将秒杀对象放入redis中
     *
     * @param seckill
     * @return
     */
    public String putSeckill(Seckill seckill) {
        /**
         * put Object(Seckill)-->序列化-->byte[]
         */
        try {
            Jedis jedis = jedisPool.getResource();
            try {
                String key = "seckill:" + seckill.getSeckillId();
                byte[] bytes = ProtostuffIOUtil.toByteArray(seckill, schema,
                        //缓存器
                        LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
                int timeout = 60 * 60;
                //超时缓存,一小时，单位秒，返回缓存结果（String）
                String result = jedis.setex(key.getBytes(), timeout, bytes);
                return result;
            } finally {
                jedis.close();
            }
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
        }
        return null;
    }

}
