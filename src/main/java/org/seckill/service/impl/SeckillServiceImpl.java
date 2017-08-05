package org.seckill.service.impl;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.seckill.dao.SeckillDao;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Siglam Lee on 2017/7/21.
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    //创建slf4j日志对象
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private SeckillDao seckillDao;

    @Resource
    private SuccessKilledDao successKilledDao;

    @Autowired
    private RedisDao redisDao;
    //加入md5盐值字符串(程序员自定义，越复杂越好)，用于混淆MD5.
    private final String slat = "%$&^%*(*))_^%^$%*(&**_#$%^&*&^%*(";

    @Override
    public List<Seckill> getSeckillList() {
        return seckillDao.queryAll(0, 4);
    }

    @Override
    public Seckill getById(long seckillId) {
        return seckillDao.queryById(seckillId);
    }

    @Override
    public Exposer exportSeckillUrl(long seckillId) {
        /**
         * 所有的秒杀操作都需要调用此方法，因此将此方法用于缓存中，降低数据库访问压力
         * getFromCache
         * if null
         *  get db
         *  else
         *      put cache
         *  logic
         *  优化点：缓存优化,超时的基础上维护一致性，秒杀对象一般是
         *  不会变化的，如果需要修改，一般则直接废弃，然后重新创建。
         *  1.redis-->2.访问数据库-->return.
         */
        Seckill seckill = redisDao.getSeckill(seckillId);
        //此时秒杀商品表中没有此项商品的秒杀计划
        if (seckill == null) {
            seckill = seckillDao.queryById(seckillId);
            if (seckill == null) {
                //Exposer对象表示该商品尚不能开启秒杀
                return new Exposer(false, seckillId);
            } else {
                redisDao.putSeckill(seckill);
            }
        }
        //如果存在此商品的秒杀计划
        //秒杀开始时间
        Date startTime = seckill.getStartTime();
        //秒杀结束时间
        Date endTime = seckill.getEndTime();
        //系统当前时间
        Date nowTime = new Date();
        //未到秒杀开启时间或秒杀活动已经过期
        if (nowTime.getTime() < startTime.getTime() || nowTime.getTime() > endTime.getTime()) {
            return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
        }
        //转换特定字符串的过程，不可逆
        String md5 = getMD5(seckillId);
        //符合条件，开启秒杀
        return new Exposer(true, md5, seckillId);
    }

    @Override
    @Transactional
    /**
     * 使用注解控制事务方法的优点
     * 1：开发团队达成一致约定，明确标注事务方法的编程风格。代表此方法是一个事务方法。
     * 2:保证事务方法尽可能短，不要穿插其他网络操作RPC/HTTP请求，或者剥离到事务方法之外。
     * 3：不是所有的方法都需要事务。如只有一条修改操作，只读操作不需要事务控制。
     */
    public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5) throws RepeatKillException, SeckillCloseException, SeckillException {
        if (md5 == null || !getMD5(seckillId).equals(md5)) {
            throw new SeckillException("SECKILL DATE IS REWIRTED");
        }
        //执行秒杀逻辑，减库存+记录购买行为
        Date killTime = new Date();
        try {
            //记录购买行为
            int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
            //唯一：seckillId,userPhone
            if (insertCount <= 0) {
                throw new RepeatKillException("重复秒杀！");
            } else {
                //减库存，热点商品竞争，调换update与insert语句执行顺序，减少update时行级锁持有时间。
                int updateCount = seckillDao.reduceNumber(seckillId, killTime);
                if (updateCount <= 0) {
                    //没有更新到记录，秒杀结束
                    throw new SeckillCloseException("秒杀关闭");
                } else {
                    //秒杀成功
                    SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                    return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, successKilled);
                }
            }
        } catch (SeckillCloseException e1) {
            throw e1;
        } catch (RepeatKillException e2) {
            throw e2;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //编译期异常转换为运行时异常:SeckillException extends RuntimeException
            throw new SeckillException("Seckill inner Exception");
        }


    }

    /**
     * 通过存储过程执行秒杀
     *
     * @param seckillId
     * @param userPhone
     * @param md5
     * @return
     */
    @Override
    public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5) {
        if (md5 == null || !getMD5(seckillId).equals(md5)) {
            throw new SeckillException("SECKILL DATE IS REWIRTED");
        }
        Date killTime = new Date();
        Map<String, Object> map = new HashMap<>();
        map.put("seckillId", seckillId);
        map.put("phone", userPhone);
        map.put("killTime", killTime);
        map.put("result", null);
        try {
            //调用存储过程，result被赋值
            seckillDao.killByProcedure(map);
            //获取result
            int result = MapUtils.getInteger(map, "result", -2);
            if (result == 1) {
                //成功，返回秒杀成功信息，商品id以及商品对象
                SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
                return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS, sk);
            } else {
                //失败，返回失败信息以及失败商品ID
                return new SeckillExecution(seckillId, SeckillStateEnum.stateOf(result));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
        }
    }

    //生成MD5字符串,拼接规则加上盐值，不可逆向推算
    private String getMD5(long seckillId) {
        String base = seckillId + "/" + slat;
        String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
        return md5;
    }
}
