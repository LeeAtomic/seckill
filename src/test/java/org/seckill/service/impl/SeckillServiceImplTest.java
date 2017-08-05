package org.seckill.service.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

/**
 * 使用集成测试service逻辑
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml", "classpath:spring/spring-service.xml"})
public class SeckillServiceImplTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private SeckillService seckillService;

    @Test
    public void getSeckillList() throws Exception {
        List<Seckill> list = seckillService.getSeckillList();
        //花括号为占位符，打印时会将list的内容放于占位符中。正常项目中禁用system.out.println().
        logger.info("list={}", list);
    }

    @Test
    public void getById() throws Exception {
        Seckill seckill = seckillService.getById(1000L);
        logger.info("seckill={}", seckill);
    }

    @Test
    public void exportSeckillUrl() throws Exception {
        long id = 1000;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        logger.info("exposer={}", exposer);
    }

    @Test
    /**
     * 此测试方法再次执行时就会出现重复秒杀异常，
     * 因此我们应该把测试方法体try{}catch(){}起来，捕获我们感兴趣的，
     * 或者业务允许的异常放在catch块中，不去向上抛给junit，
     * 当出现这两个异常时，junit则会认为单元测试通过了。
     */
    public void executeSeckill() throws Exception {
        long id = 1000;
        long phone = 13573453456L;
        String md5 = "d27fae239ac5b5fb49c0d4bbf50b7289";
        try {
            SeckillExecution seckillExecution = seckillService.executeSeckill(id, phone, md5);
            logger.info("seckillExecution={}", seckillExecution);
        } catch (RepeatKillException e) {
            logger.error(e.getMessage(), e);
        } catch (SeckillCloseException e) {
            logger.error(e.getMessage(), e);
        }

    }

    @Test
    /**
     * ①该测试方法用于测试整个秒杀流程逻辑---->输出秒杀地址，执行秒杀
     * ②集成测试业务的完整性：测试代码完整逻辑，注意可重复执行。
     */
    public void testSeckillLogic() throws Exception {
        long id = 1000;
        Exposer exposer = seckillService.exportSeckillUrl(id);
        //符合秒杀开启条件，开启秒杀
        if (exposer.isExposed()) {
            logger.info("exposer={ }", exposer);
            long phone = 13564678942L;
            String md5 = exposer.getMd5();
            try {
                SeckillExecution seckillExecution = seckillService.executeSeckill(id, phone, md5);
                logger.info("result = { }", seckillExecution);
            } catch (RepeatKillException e) {
                logger.error(e.getMessage(), e);
            } catch (SeckillCloseException e) {
                logger.error(e.getMessage());
            }
        } else {
            //输出警告，秒杀未开启
            logger.warn("exposer{ }", exposer);
        }
    }

}