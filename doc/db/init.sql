USE seckill;
# 建立秒杀库存表
CREATE TABLE seckill (
  seckill_id  BIGINT       NOT NULL AUTO_INCREMENT
  COMMENT '商品库存id',
  name        VARCHAR(120) NOT NULL
  COMMENT '商品名称',
  number      INT          NOT NULL
  COMMENT '库存数量',
  start_time  TIMESTAMP    NOT NULL
  COMMENT '秒杀开启时间',
  end_time    TIMESTAMP    NOT NULL
  COMMENT '秒杀结束时间',
  create_time TIMESTAMP    NOT NULL DEFAULT current_timestamp
  COMMENT '创建时间',
  PRIMARY KEY (seckill_id),
  KEY idx_start_time(start_time),
  KEY idx_end_time(end_time),
  KEY idx_create_time(create_time)
)
  ENGINE = InnoDB
  AUTO_INCREMENT = 1000
  DEFAULT CHARSET = utf8
  COMMENT = '秒杀库存表';

INSERT INTO seckill (name, number, start_time, end_time)
VALUES
  ('1000元秒杀iPhone6', 100, '2015-11-01 00:00:00', '2015-11-02 00:00:00'),
  ('500元秒杀iPhone5', 500, '2015-11-01 00:00:00', '2015-11-02 00:00:00'),
  ('3000元秒杀mi6', 700, '2015-11-01 00:00:00', '2015-11-02 00:00:00'),
  ('3000元秒杀iPhone7', 50, '2015-11-01 00:00:00', '2015-11-02 00:00:00');

# 创建秒杀成功明细表
# 用户登录认证相关信息
CREATE TABLE success_killed (
  seckill_id  BIGINT    NOT NULL
  COMMENT '秒杀商品id',
  user_phone  BIGINT    NOT NULL
  COMMENT '用户手机号',
  state       TINYINT   NOT NULL DEFAULT -1
  COMMENT '状态标识：-1,无效；0，成功；1，已付款 ',
  create_time TIMESTAMP NOT NULL
  COMMENT '创建时间',
  # 联合主键构成唯一性，同一个用户同一时间不能对同一件商品进行多次秒杀操作。
  PRIMARY KEY (seckill_id, user_phone),
  KEY idx_create_time(create_time)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8
  COMMENT = '秒杀成功表';
