package org.seckill.web;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Controller
@RequestMapping(value = "/seckill")//url:/模块/资源/细分（URL格式）
public class SeckillController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private SeckillService seckillService;

    /**
     * 获取秒杀商品列表页
     *
     * @param model
     * @return
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String list(Model model) {
        //获取列表页
        List<Seckill> list = seckillService.getSeckillList();
        model.addAttribute("list", list);
        //list.jsp+model = ModelAndView
        return "list";
    }

    /**
     * 获取指定商品详情
     *
     * @param seckillId
     * @param model
     * @return
     */
    @RequestMapping(value = "/{seckillId}/detail",
            method = RequestMethod.GET)
    public String detail(@PathVariable("seckillId") Long seckillId, Model model) {
        logger.info("秒杀商品ID为：========", seckillId);
        if (seckillId == null) {
            return "redirect:/seckill/list";
        }
        Seckill seckill = seckillService.getById(seckillId);
        if (seckill == null) {
            return "forward:/seckill/list";
        }
        logger.info("testJrebel");
        model.addAttribute("seckill", seckill);
        return "detail";
    }


    /**
     * 输出秒杀地址，ajax json 只接收post请求，地址栏输入地址无效
     *
     * @param seckillId
     * @return
     */
    @RequestMapping(value = "/{seckillId}/exposer",
            method = RequestMethod.POST,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody//告诉框架返回的是json类型，框架会将返回的数据类型封装成json。
    public SeckillResult<Exposer> exposer(@PathVariable Long seckillId) {
        //声明封装json结果的Dto实例
        SeckillResult<Exposer> result;
        try {
            //封装了输出秒杀地址DTO类实例，包含是否开启秒杀，md5加密地址等
            Exposer exposer = seckillService.exportSeckillUrl(seckillId);
            //初始化秒杀结果DTO类，封装了成功秒杀的标记以及秒杀信息
            result = new SeckillResult<>(true, exposer);
        } catch (Exception e) {
            //请求出现异常，则秒杀不成功，并输出异常信息，打印异常日志
            logger.error(e.getMessage(), e);
            //这样的处理方式使得所有的异常都不会暴露给用户，而是先输出日志，然后返回秒杀结果实例，包含错误信息
            result = new SeckillResult<>(false, e.getMessage());
        }
        return result;
    }

    /**
     * 执行秒杀
     *
     * @return
     */
    @RequestMapping(value = "/{seckillId}/{md5}/execution",
            method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<SeckillExecution> excute(@PathVariable("seckillId") Long seckillId,
                                                  @PathVariable("md5") String md5,
                                                  @CookieValue(value = "killPhone", required = false) Long phone) {
        if (phone == null) {
            //如果用户手机号（用户的唯一标识）为空，则直接返回秒杀失败以及错误信息
            return new SeckillResult<>(false, "未注册");
        }
        SeckillResult<SeckillExecution> result;
        try {
            SeckillExecution execution = seckillService.executeSeckillProcedure(seckillId, phone, md5);
            return new SeckillResult<>(true, execution);
            /**
             * 在Controller层捕获并处理所有底层可能抛出的异常
             */
            //系统允许的异常:重复秒杀异常
        } catch (RepeatKillException e) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.REPEAT_KILL);
            return new SeckillResult<>(true, execution);
            //系统允许的异常:秒杀关闭异常
        } catch (SeckillCloseException e) {
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.END);
            return new SeckillResult<>(true, execution);
            //除了以上两种系统允许的异常外，其他都属于系统异常
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            SeckillExecution execution = new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
            result = new SeckillResult<>(true, execution);
        }
        return result;
    }

    @RequestMapping(value = "/time/now", method = RequestMethod.GET,
            produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public SeckillResult<Long> time() {
        Date now = new Date();
        return new SeckillResult<>(true, now.getTime());
    }

}
