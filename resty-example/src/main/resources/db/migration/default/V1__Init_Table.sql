
DROP TABLE IF EXISTS sec_user;
CREATE TABLE sec_user (
  id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  username      VARCHAR(50)  NOT NULL COMMENT '登录名',
  providername  VARCHAR(50)  NOT NULL COMMENT '提供者',
  email         VARCHAR(200) COMMENT '邮箱',
  phone        VARCHAR(50) COMMENT '联系电话',
  password      VARCHAR(200) NOT NULL COMMENT '密码',
  hasher        VARCHAR(200) NOT NULL COMMENT '加密类型',
  salt          VARCHAR(200) NOT NULL COMMENT '加密盐',
  avatar_url    VARCHAR(255) COMMENT '头像',
  first_name    VARCHAR(10) COMMENT '名字',
  last_name     VARCHAR(10) COMMENT '姓氏',
  full_name     VARCHAR(20) COMMENT '全名',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP   NOT NULL,
  updated_at TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP NULL
) ENGINE =InnoDB DEFAULT CHARSET =utf8 COMMENT ='用户';