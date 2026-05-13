-- Pounds-Dream 减肥减脂平台 数据库建表脚本

CREATE DATABASE IF NOT EXISTS pounds_dream DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE pounds_dream;

-- 用户表
CREATE TABLE IF NOT EXISTS t_user (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username        VARCHAR(50)  NOT NULL UNIQUE COMMENT '用户名',
    password        VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname        VARCHAR(50)  DEFAULT '' COMMENT '昵称',
    avatar          VARCHAR(500) DEFAULT '' COMMENT '头像URL',
    gender          TINYINT      DEFAULT 0 COMMENT '性别 0-未知 1-男 2-女',
    birthday        DATE         DEFAULT NULL COMMENT '生日',
    height          DECIMAL(5,1) DEFAULT NULL COMMENT '身高(cm)',
    target_weight   DECIMAL(5,1) DEFAULT NULL COMMENT '目标体重(kg)',
    target_calorie  INT          DEFAULT NULL COMMENT '每日目标热量(kcal)',
    activity_level  TINYINT      DEFAULT 2 COMMENT '活动量 1-久坐 2-轻度 3-中度 4-重度',
    phone           VARCHAR(20)  DEFAULT NULL UNIQUE COMMENT '手机号',
    email           VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
    status          TINYINT      DEFAULT 1 COMMENT '状态 0-禁用 1-正常',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_phone (phone)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 体重记录表
CREATE TABLE IF NOT EXISTS t_weight_record (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    weight      DECIMAL(5,1) NOT NULL COMMENT '体重(kg)',
    body_fat    DECIMAL(4,1) DEFAULT NULL COMMENT '体脂率(%)',
    bmi         DECIMAL(4,1) DEFAULT NULL COMMENT 'BMI值',
    record_date DATE         NOT NULL COMMENT '记录日期',
    note        VARCHAR(200) DEFAULT '' COMMENT '备注',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, record_date),
    INDEX idx_user_id (user_id),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='体重记录表';

-- 围度记录表
CREATE TABLE IF NOT EXISTS t_body_measurement (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    waist       DECIMAL(5,1) DEFAULT NULL COMMENT '腰围(cm)',
    hip         DECIMAL(5,1) DEFAULT NULL COMMENT '臀围(cm)',
    chest       DECIMAL(5,1) DEFAULT NULL COMMENT '胸围(cm)',
    upper_arm   DECIMAL(5,1) DEFAULT NULL COMMENT '上臂围(cm)',
    thigh       DECIMAL(5,1) DEFAULT NULL COMMENT '大腿围(cm)',
    record_date DATE         NOT NULL COMMENT '记录日期',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='围度记录表';

-- 食物热量表
CREATE TABLE IF NOT EXISTS t_food (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '食物ID',
    name            VARCHAR(100) NOT NULL COMMENT '食物名称',
    category        VARCHAR(50)  NOT NULL COMMENT '分类(主食/蔬菜/水果/肉类/饮品/零食等)',
    calorie         INT          NOT NULL COMMENT '每100g热量(kcal)',
    protein         DECIMAL(6,2) DEFAULT 0 COMMENT '蛋白质(g/100g)',
    fat             DECIMAL(6,2) DEFAULT 0 COMMENT '脂肪(g/100g)',
    carbohydrate    DECIMAL(6,2) DEFAULT 0 COMMENT '碳水化合物(g/100g)',
    fiber           DECIMAL(6,2) DEFAULT 0 COMMENT '膳食纤维(g/100g)',
    gi              DECIMAL(5,1) DEFAULT NULL COMMENT '升糖指数(GI)',
    unit            VARCHAR(20)  DEFAULT 'g' COMMENT '计量单位',
    unit_weight     DECIMAL(6,1) DEFAULT 100 COMMENT '一个单位的重量(g)',
    is_custom       TINYINT      DEFAULT 0 COMMENT '是否用户自定义 0-系统 1-自定义',
    creator_id      BIGINT       DEFAULT NULL COMMENT '创建者ID(自定义食物)',
    INDEX idx_name (name),
    INDEX idx_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='食物热量表';

-- 饮食记录表
CREATE TABLE IF NOT EXISTS t_diet_record (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    food_id     BIGINT       NOT NULL COMMENT '食物ID',
    meal_type   TINYINT      NOT NULL COMMENT '餐次 1-早餐 2-午餐 3-晚餐 4-加餐',
    amount      DECIMAL(6,1) NOT NULL COMMENT '食用量(克)',
    calorie     INT          NOT NULL COMMENT '该次摄入热量(kcal)',
    record_date DATE         NOT NULL COMMENT '记录日期',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, record_date),
    INDEX idx_meal_type (meal_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食记录表';

-- 运动类型表
CREATE TABLE IF NOT EXISTS t_exercise_type (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '运动类型ID',
    name            VARCHAR(100) NOT NULL COMMENT '运动名称',
    category        VARCHAR(30)  NOT NULL COMMENT '分类(有氧/无氧/柔韧/球类)',
    met_value       DECIMAL(4,1) NOT NULL COMMENT 'MET值(代谢当量)',
    calorie_per_min DECIMAL(5,1) DEFAULT NULL COMMENT '每分钟消耗热量(参考值70kg)',
    description     VARCHAR(500) DEFAULT '' COMMENT '说明'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动类型表';

-- 运动记录表
CREATE TABLE IF NOT EXISTS t_exercise_record (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id         BIGINT       NOT NULL COMMENT '用户ID',
    exercise_type_id BIGINT      NOT NULL COMMENT '运动类型ID',
    duration        INT          NOT NULL COMMENT '运动时长(分钟)',
    calorie_burned  INT          NOT NULL COMMENT '消耗热量(kcal)',
    heart_rate      INT          DEFAULT NULL COMMENT '平均心率',
    record_date     DATE         NOT NULL COMMENT '记录日期',
    note            VARCHAR(200) DEFAULT '' COMMENT '备注',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运动记录表';

-- 社区帖子表
CREATE TABLE IF NOT EXISTS t_post (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '帖子ID',
    user_id       BIGINT       NOT NULL COMMENT '发布者ID',
    title         VARCHAR(200) NOT NULL COMMENT '标题',
    content       TEXT         NOT NULL COMMENT '内容',
    images        VARCHAR(2000) DEFAULT '[]' COMMENT '图片URL列表(JSON数组)',
    category      VARCHAR(30)  DEFAULT '经验分享' COMMENT '分类(经验分享/打卡/求助/食谱)',
    view_count    INT          DEFAULT 0 COMMENT '浏览数',
    like_count    INT          DEFAULT 0 COMMENT '点赞数',
    comment_count INT          DEFAULT 0 COMMENT '评论数',
    is_top        TINYINT      DEFAULT 0 COMMENT '是否置顶',
    status        TINYINT      DEFAULT 1 COMMENT '状态 0-删除 1-正常',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_category (category),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='社区帖子表';

-- 评论表
CREATE TABLE IF NOT EXISTS t_comment (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论ID',
    post_id          BIGINT       NOT NULL COMMENT '帖子ID',
    user_id          BIGINT       NOT NULL COMMENT '评论者ID',
    parent_id        BIGINT       DEFAULT 0 COMMENT '父评论ID(0为一级评论)',
    reply_to_user_id BIGINT      DEFAULT NULL COMMENT '被回复用户ID',
    content          VARCHAR(500) NOT NULL COMMENT '评论内容',
    like_count       INT          DEFAULT 0 COMMENT '点赞数',
    status           TINYINT      DEFAULT 1 COMMENT '状态 0-删除 1-正常',
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_post_id (post_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论表';

-- 点赞表
CREATE TABLE IF NOT EXISTS t_like (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id     BIGINT      NOT NULL COMMENT '用户ID',
    target_id   BIGINT      NOT NULL COMMENT '目标ID',
    target_type TINYINT     NOT NULL COMMENT '目标类型 1-帖子 2-评论',
    created_at  DATETIME    DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_target (user_id, target_id, target_type),
    INDEX idx_target (target_id, target_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='点赞表';

-- 饮食模版主表
CREATE TABLE IF NOT EXISTS t_diet_template (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模版ID',
    user_id       BIGINT       NOT NULL COMMENT '用户ID',
    name          VARCHAR(100) NOT NULL COMMENT '模版名称',
    meal_type     TINYINT      NOT NULL COMMENT '餐次 1-早餐 2-午餐 3-晚餐 4-加餐',
    total_calorie INT          DEFAULT 0 COMMENT '总热量(kcal)',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食模版表';

-- 饮食模版明细表
CREATE TABLE IF NOT EXISTS t_diet_template_item (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    template_id BIGINT       NOT NULL COMMENT '模版ID',
    food_id     BIGINT       NOT NULL COMMENT '食物ID',
    amount      DECIMAL(6,1) NOT NULL COMMENT '食用量(g)',
    INDEX idx_template_id (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮食模版明细表';

-- 每日统计表
CREATE TABLE IF NOT EXISTS t_daily_stats (
    id             BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    user_id        BIGINT       NOT NULL COMMENT '用户ID',
    stat_date      DATE         NOT NULL COMMENT '统计日期',
    total_intake   INT          DEFAULT 0 COMMENT '总摄入热量(kcal)',
    total_burned   INT          DEFAULT 0 COMMENT '总消耗热量(kcal)',
    target_calorie INT          DEFAULT 0 COMMENT '目标热量(kcal)',
    protein        DECIMAL(6,1) DEFAULT 0 COMMENT '蛋白质(g)',
    fat            DECIMAL(6,1) DEFAULT 0 COMMENT '脂肪(g)',
    carbohydrate   DECIMAL(6,1) DEFAULT 0 COMMENT '碳水(g)',
    diet_count     INT          DEFAULT 0 COMMENT '饮食记录数',
    exercise_count INT          DEFAULT 0 COMMENT '运动记录数',
    weight         DECIMAL(5,1) DEFAULT NULL COMMENT '当日体重(kg)',
    is_goal_met    TINYINT      DEFAULT 0 COMMENT '是否达标 0-否 1-是',
    created_at     DATETIME     DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, stat_date),
    INDEX idx_stat_date (stat_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日统计表';

-- 餐次状态表
CREATE TABLE IF NOT EXISTS t_meal_status (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id    BIGINT      NOT NULL COMMENT '用户ID',
    meal_date  DATE        NOT NULL COMMENT '日期',
    meal_type  TINYINT     NOT NULL COMMENT '餐次 1-4',
    status     VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/RECORDED/SKIPPED/EXPIRED',
    updated_at DATETIME    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date_meal (user_id, meal_date, meal_type),
    INDEX idx_meal_date (meal_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='餐次状态表';

-- 通知表
CREATE TABLE IF NOT EXISTS t_notification (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知ID',
    user_id    BIGINT       NOT NULL COMMENT '用户ID',
    type       VARCHAR(30)  NOT NULL COMMENT '通知类型 MISSED_MEAL/SYSTEM',
    title      VARCHAR(200) NOT NULL COMMENT '标题',
    content    VARCHAR(500) NOT NULL COMMENT '内容',
    is_read    TINYINT      DEFAULT 0 COMMENT '是否已读 0-未读 1-已读',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_read (user_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 训练计划主表
CREATE TABLE IF NOT EXISTS t_training_plan (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '计划ID',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    name        VARCHAR(100) NOT NULL COMMENT '计划名称',
    description VARCHAR(500) DEFAULT '' COMMENT '计划描述',
    status      TINYINT      DEFAULT 1 COMMENT '状态 0-归档 1-进行中',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练计划表';

-- 训练计划明细表
CREATE TABLE IF NOT EXISTS t_training_plan_item (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    plan_id          BIGINT       NOT NULL COMMENT '计划ID',
    day_of_week      TINYINT      NOT NULL COMMENT '星期几 1-7',
    exercise_type_id BIGINT       NOT NULL COMMENT '运动类型ID',
    sets             INT          DEFAULT NULL COMMENT '组数',
    reps             INT          DEFAULT NULL COMMENT '每组次数',
    duration         INT          DEFAULT NULL COMMENT '时长(分钟)',
    note             VARCHAR(200) DEFAULT '' COMMENT '备注',
    INDEX idx_plan_id (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='训练计划明细表';

-- AI对话会话表
CREATE TABLE IF NOT EXISTS t_chat_conversation (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话ID',
    user_id    BIGINT       NOT NULL COMMENT '用户ID',
    title      VARCHAR(100) NOT NULL DEFAULT '新对话' COMMENT '会话标题',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_updated_at (updated_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话会话表';

-- AI对话消息表
CREATE TABLE IF NOT EXISTS t_chat_message (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    conversation_id BIGINT       NOT NULL COMMENT '会话ID',
    role            VARCHAR(10)  NOT NULL COMMENT '角色 user/assistant',
    content         TEXT         NOT NULL COMMENT '消息内容',
    thinking        TEXT         DEFAULT NULL COMMENT '思考过程(仅assistant)',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_conversation_id (conversation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话消息表';

-- 用户段位积分表
CREATE TABLE IF NOT EXISTS t_user_rank (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id         BIGINT       NOT NULL UNIQUE COMMENT '用户ID',
    points          INT          DEFAULT 0 COMMENT '当前积分',
    rank_level      TINYINT      DEFAULT 1 COMMENT '段位等级 1-7',
    rank_name       VARCHAR(20)  DEFAULT '觉醒学徒' COMMENT '段位名称',
    max_streak      INT          DEFAULT 0 COMMENT '历史最高连续天数',
    current_streak  INT          DEFAULT 0 COMMENT '当前连续天数',
    last_check_date DATE         DEFAULT NULL COMMENT '最后打卡日期',
    created_at      DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户段位积分表';

-- 数字孪生健身宠表
CREATE TABLE IF NOT EXISTS t_pet (
    id         BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id    BIGINT       NOT NULL UNIQUE COMMENT '用户ID',
    pet_name   VARCHAR(50)  DEFAULT '小梦' COMMENT '宠物昵称',
    created_at DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数字孪生健身宠表';

-- 月经周期记录表
CREATE TABLE IF NOT EXISTS t_menstrual_cycle (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id       BIGINT       NOT NULL COMMENT '用户ID',
    start_date    DATE         NOT NULL COMMENT '经期开始日期',
    end_date      DATE         DEFAULT NULL COMMENT '经期结束日期',
    cycle_length  INT          DEFAULT NULL COMMENT '周期长度(天)',
    period_length INT          DEFAULT NULL COMMENT '经期天数',
    symptoms      VARCHAR(500) DEFAULT '' COMMENT '症状(JSON数组)',
    mood          VARCHAR(20)  DEFAULT '' COMMENT '情绪状态',
    note          VARCHAR(500) DEFAULT '' COMMENT '备注',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_start_date (start_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='月经周期记录表';

-- 饮水记录表
CREATE TABLE IF NOT EXISTS t_water_record (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id     BIGINT       NOT NULL COMMENT '用户ID',
    amount      INT          NOT NULL COMMENT '饮水量(ml)',
    drink_type  VARCHAR(20)  DEFAULT 'water' COMMENT '饮水方式 water/tea/coffee/other',
    record_time DATETIME     NOT NULL COMMENT '记录时间',
    record_date DATE         NOT NULL COMMENT '记录日期',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮水记录表';

-- 饮水目标表
CREATE TABLE IF NOT EXISTS t_water_goal (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id     BIGINT       NOT NULL UNIQUE COMMENT '用户ID',
    daily_goal  INT          NOT NULL DEFAULT 2000 COMMENT '每日目标(ml)',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='饮水目标表';

-- 睡眠记录表
CREATE TABLE IF NOT EXISTS t_sleep_record (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id       BIGINT       NOT NULL COMMENT '用户ID',
    bed_time      DATETIME     NOT NULL COMMENT '上床时间',
    wake_time     DATETIME     NOT NULL COMMENT '起床时间',
    duration      INT          NOT NULL COMMENT '睡眠时长(分钟)',
    quality       TINYINT      DEFAULT 3 COMMENT '睡眠质量 1-5星',
    tags          VARCHAR(500) DEFAULT '[]' COMMENT '标签(JSON数组)',
    is_nap        TINYINT      DEFAULT 0 COMMENT '是否午睡 0-否 1-是',
    record_date   DATE         NOT NULL COMMENT '记录日期(起床日)',
    note          VARCHAR(500) DEFAULT '' COMMENT '备注',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_date (user_id, record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='睡眠记录表';

-- 睡眠目标表
CREATE TABLE IF NOT EXISTS t_sleep_goal (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id       BIGINT       NOT NULL UNIQUE COMMENT '用户ID',
    daily_goal    INT          NOT NULL DEFAULT 480 COMMENT '每日目标(分钟)',
    created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='睡眠目标表';
