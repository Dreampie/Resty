-- 第三方账号
CREATE TABLE `oau_client` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `key` varchar(50) DEFAULT '' COMMENT 'oauth2.0标准中的client_id',
  `secret` varchar(50) DEFAULT '' COMMENT 'oauth2.0标准中的client_secret',
  `uri` varchar(100) DEFAULT NULL COMMENT '网站uri',
  `redirect_uri` varchar(500) DEFAULT '' COMMENT '回调地址',
  `user_id` int(11) DEFAULT '0' COMMENT '用户id',
  `name` varchar(100) DEFAULT '' COMMENT '网站名',
  `provider` varchar(100) DEFAULT NULL COMMENT '提供者',
  `state` int(1) DEFAULT NULL COMMENT 'client状态  1: 测试  2: 待审核  3: 上线',
  `created_at` timestamp NULL DEFAULT NULL,
  `creater_id` int(11) DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `updater_id` int(11) DEFAULT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL,
  `deleter_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 授权范围
CREATE TABLE `oau_scope` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `key` varchar(100) DEFAULT '' COMMENT 'key',
  `name` varchar(100) DEFAULT '' COMMENT '名称',
  `method` varchar(10) DEFAULT '' COMMENT '请求类型',
  `uri` varchar(500) DEFAULT '' COMMENT '请求地址',
  `selected` int(1) DEFAULT '0' COMMENT '默认选择的授权',
  `created_at` timestamp NULL DEFAULT NULL,
  `creater_id` int(11) DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `updater_id` int(11) DEFAULT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL,
  `deleter_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 用户openid
CREATE TABLE `oau_openid` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `client_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `open_id` varchar(50) DEFAULT '' COMMENT '回调地址',
  `created_at` timestamp NULL DEFAULT NULL,
  `creater_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 授权范围
CREATE TABLE `oau_client_scope` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `client_id` int(11) DEFAULT NULL,
  `scope_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 授权方式
CREATE TABLE `oau_grant` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `key` varchar(100) DEFAULT '' COMMENT 'key',
  `name` varchar(100) DEFAULT '' COMMENT '名称',
  `intro` varchar(200) DEFAULT '' COMMENT '简介',
  `created_at` timestamp NULL DEFAULT NULL,
  `creater_id` int(11) DEFAULT NULL,
  `updated_at` timestamp NULL DEFAULT NULL,
  `updater_id` int(11) DEFAULT NULL,
  `deleted_at` timestamp NULL DEFAULT NULL,
  `deleter_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- 第三方 账号可以使用的授权方式
CREATE TABLE `oau_client_grant` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `client_id` int(11) DEFAULT NULL,
  `grant_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;