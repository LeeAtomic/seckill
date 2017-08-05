//存放主要交互逻辑js代码
//javascript 模块化
var seckill = {

    //封装秒杀相关ajax的URL
    URL: {
        /**
         *获取现在时间URL
         */
        now: function () {
            return '/seckill/time/now';
        },
        /**
         * 返回获取秒杀地址请求的URL
         */
        exposer: function (seckillId) {
            return '/seckill/' + seckillId + '/exposer';
        },

        /**
         *
         */
        execution: function (seckillId, md5) {
            return '/seckill/' + seckillId + '/' + md5 + '/execution';
        }
    },

    /**
     *此方法用户用户手机号验证
     */
    validatePhone: function (phone) {
        if (phone && phone.length == 11 && !isNaN(phone)) {
            return true;
        } else {
            return false;
        }
    },
    /**
     * 获取秒杀地址，控制显示逻辑，执行秒杀
     */
    handlerSeckill: function (seckillId, node) {
        node.hide().html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');
        $.post(seckill.URL.exposer(seckillId), {}, function (result) {
            //在回调函数中执行交互流程
            if (result && result['success']) {
                var exposer = result['data'];
                if (exposer['exposed']) {
                    //开启秒杀,
                    //获取秒杀地址
                    var md5 = exposer['md5'];
                    var killUrl = seckill.URL.execution(seckillId, md5);
                    console.log('killUrl:' + killUrl);
                    //绑定一次点击事件,秒杀时用户会多次重复点击，使用click为每次都向服务器发送请求
                    $('#killBtn').one('click', function () {
                        //执行秒杀请求
                        //1：先禁用按钮
                        $(this).addClass('disabled');
                        //2:发送秒杀请求
                        $.post(killUrl, {}, function (result) {
                            if (result && result['success']) {
                                var killResult = result['data'];
                                var state = killResult['state'];
                                var stateInfo = killResult['stateInfo'];
                                console.log(stateInfo);
                                //显示秒杀结果
                                node.html('<span class="label label-success">' + stateInfo + '</span>');
                            }
                        });
                    });
                    console.log("正在秒杀：result==" + result);
                    node.show();

                } else {
                    //未开启秒杀
                    console.log("未开启秒杀");
                    var now = exposer['now'];
                    var start = exposer['start'];
                    var end = exposer['end'];
                    //重新计算计时逻辑
                    seckill.countdown(seckillId, now, start, end);
                }
            } else {
                console.log('result:' + result)
            }
        });

    },

    /**
     *秒杀时间的逻辑判断
     */
    countDown: function (seckillId, nowTime, startTime, endTime) {
        var seckillBox = $('#seckill-box');
        if (nowTime > endTime) {
            //秒杀结束
            seckillBox.html("秒杀结束");
        } else if (nowTime < startTime) {
            //秒杀未开始,计时事件绑定
            var killTime = new Date(startTime + 1000);//加一秒防止用户计时过程中的时间偏移
            seckillBox.countdown(killTime, function (event) {
                var format = event.strftime('秒杀倒计时： %D天 %H时 %M分 %S秒');
                seckillBox.html(format);
                //时间完成后回调事件
            }).on('finish.countdown', function () {
                //获取秒杀地址，控制现实逻辑，执行秒杀
                seckill.handlerSeckill(seckillId, seckillBox);
            });
        } else {
            //秒杀开始
            seckill.handlerSeckill(seckillId, seckillBox);
        }
    },

    //详情页秒杀逻辑
    detail: {
        /**
         *详情页初始化方法
         */
        init: function (params) {
            //用户手机验证和登录，计时交互
            //规划我们的交互流程，在cookie中查找手机号
            var killPhone = $.cookie('killPhone');
            //验证手机号
            if (!seckill.validatePhone(killPhone)) {
                //绑定手机号
                //获取模态框对象
                var killPhoneModal = $('#killPhoneModal');
                killPhoneModal.modal({
                    //显示弹出层
                    show: true,
                    //禁止位置关闭
                    backdrop: 'static',
                    //关闭键盘事件
                    keyboard: false
                })
                ;
                $('#killPhoneBtn').click(function () {
                    var inputPhone = $('#killPhoneKey').val();
                    if (seckill.validatePhone(inputPhone)) {
                        //电话写入cookie中，生存期为7天，只与/seckill目录及其子目录下的网页关联
                        //指定cookie的生命周期，现在常用maxAge(),以秒为单位。
                        $.cookie('killPhone', inputPhone, {expires: 7, path: '/seckill'});
                        window.location.reload();
                    } else {
                        $('#killPhoneMessage').hide().html('<label class="label label-danger">手机号错误</label>').show(300);
                    }
                });
            }
            //已经登录
            //计时交互
            //JS访问json
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];
            $.get(seckill.URL.now(), {}, function (result) {
                /**
                 *  result必须存在，并且result的success必须为true
                 （data.key和data[’key’]各自有自己的应用场景，一般情况使用data.key即可，
                 也比较直观(它符合其它高级语言中访问对象中属性的方式)；当key为一个变量时，
                 并且使用在循环中，用data['key']这种方式。）
                 */
                if (result && result['success']) {
                    var nowTime = result['data'];
                    //时间判断
                    seckill.countDown(seckillId, nowTime, startTime, endTime);
                } else {
                    console.log('result:' + result);
                }
            })
        }
    }
}