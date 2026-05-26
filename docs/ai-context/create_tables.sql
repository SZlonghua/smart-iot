-- ==============================================
-- 智能物联网平台 - MySQL建表脚本
-- ==============================================

-- ----------------------------
-- 1. 产品分类表（product_category）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `product_category` (
    `id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(64) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID',
    `sort_order` INT DEFAULT 0 COMMENT '排序值',
    `description` VARCHAR(255) COMMENT '分类描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品分类表';

-- ----------------------------
-- 2. 产品表（product）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `product` (
    `id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT COMMENT '产品ID',
    `product_key` VARCHAR(64) NOT NULL UNIQUE COMMENT '产品Key',
    `product_secret` VARCHAR(128) NOT NULL COMMENT '产品密钥',
    `name` VARCHAR(128) NOT NULL COMMENT '产品名称',
    `category_id` BIGINT COMMENT '分类ID',
    `category_name` VARCHAR(64) COMMENT '分类名称',
    `device_type` VARCHAR(32) NOT NULL DEFAULT 'direct' COMMENT '设备类型(direct:直连设备, gateway-child:网关子设备, gateway:网关设备)',
    `description` TEXT COMMENT '产品描述',
    `model_json` LONGTEXT COMMENT '物模型JSON',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0禁用/1启用)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`category_id`) REFERENCES `product_category`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='产品表';

-- ----------------------------
-- 3. 设备表（device）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `device` (
    `id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    `name` VARCHAR(128) NOT NULL COMMENT '设备名称',
    `device_key` VARCHAR(64) NOT NULL UNIQUE COMMENT '设备Key',
    `device_secret` VARCHAR(128) NOT NULL COMMENT '设备密钥',
    `parent_device_id` BIGINT COMMENT '父级设备ID',
    `product_id` BIGINT(20) COMMENT '产品ID',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态(0离线/1在线)',
    `last_online_time` DATETIME COMMENT '最后在线时间',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_parent_device_id` (`parent_device_id`),
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_status` (`status`),
    FOREIGN KEY (`parent_device_id`) REFERENCES `device`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- ----------------------------
-- 4. 网络组件表（network_component）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `network_component` (
    `id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT COMMENT '组件ID',
    `name` VARCHAR(64) NOT NULL COMMENT '组件名称',
    `type` VARCHAR(32) NOT NULL COMMENT '类型(tcp:TCP服务, http:HTTP服务, mqtt-server:MQTT服务, mqtt-client:MQTT客户端)',
    `configuration` LONGTEXT COMMENT '配置信息(JSON格式)',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(0停止/1运行)',
    `description` VARCHAR(255) COMMENT '组件描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_type` (`type`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网络组件表';

-- ----------------------------
-- 5. 协议表（protocol）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `protocol` (
    `id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT COMMENT '协议ID',
    `name` VARCHAR(64) NOT NULL COMMENT '协议名称',
    `version` VARCHAR(16) NOT NULL COMMENT '版本号',
    `jar_path` VARCHAR(255) COMMENT 'JAR包路径',
    `description` VARCHAR(255) COMMENT '协议描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_name_version` (`name`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='协议表';

-- ----------------------------
-- 6. 网关表（gateway）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `gateway` (
    `id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT COMMENT '网关ID',
    `name` VARCHAR(64) NOT NULL COMMENT '网关名称',
    `type` VARCHAR(32) NOT NULL COMMENT '接入类型(mqtt-direct:MQTT直连接入, gateway-child:网关子设备接入, http-push:HTTP推送接入, mqtt-broker:MQTT Broker接入)',
    `component_id` BIGINT COMMENT '网络组件ID',
    `protocol_id` BIGINT COMMENT '协议ID',
    `transport` VARCHAR(32) NOT NULL DEFAULT 'tcp' COMMENT '传输协议(tcp:TCP, udp:UDP, ws:WebSocket)',
    `description` VARCHAR(255) COMMENT '网关描述',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_component_id` (`component_id`),
    INDEX `idx_protocol_id` (`protocol_id`),
    INDEX `idx_type` (`type`),
    FOREIGN KEY (`component_id`) REFERENCES `network_component`(`id`) ON DELETE SET NULL,
    FOREIGN KEY (`protocol_id`) REFERENCES `protocol`(`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网关表';

-- ----------------------------
-- 7. 告警日志表（alarm_log）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `alarm_log` (
    `id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT COMMENT '告警ID',
    `device_key` VARCHAR(64) NOT NULL COMMENT '设备Key',
    `level` TINYINT NOT NULL COMMENT '级别(1紧急/2重要/3次要/4提示)',
    `description` VARCHAR(512) NOT NULL COMMENT '告警描述',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态(1未处理/2处理中/3已处理/4已忽略)',
    `trigger_time` DATETIME NOT NULL COMMENT '触发时间',
    `handle_time` DATETIME COMMENT '处理时间',
    `handler` VARCHAR(64) COMMENT '处理人',
    `handle_note` TEXT COMMENT '处理说明',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_device_key` (`device_key`),
    INDEX `idx_level` (`level`),
    INDEX `idx_status` (`status`),
    INDEX `idx_trigger_time` (`trigger_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='告警日志表';

-- ----------------------------
-- 8. 运行日志表（device_log）
-- ----------------------------
CREATE TABLE IF NOT EXISTS `device_log` (
    `id` BIGINT(20) PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    `device_id` BIGINT(20) NOT NULL COMMENT '设备ID',
    `device_name` VARCHAR(128) NOT NULL COMMENT '设备名称',
    `type` VARCHAR(32) NOT NULL COMMENT '类型(online:上线, offline:离线, properties_report:属性上报, properties_get:读取属性, event:事件, command:命令)',
    `content` LONGTEXT COMMENT '消息数据(JSON格式)',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_device_id` (`device_id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='运行日志表';

-- ==============================================
-- 脚本执行完毕
-- ==============================================