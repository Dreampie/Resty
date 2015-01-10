DROP TABLE IF EXISTS sec_role;
CREATE TABLE sec_role (
  id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name       VARCHAR(50) NOT NULL
  COMMENT '名称',
  value      VARCHAR(50) NOT NULL
  COMMENT '值',
  intro      VARCHAR(255) COMMENT '简介',
  pid        BIGINT               DEFAULT 0
  COMMENT '父级id',
  left_code  BIGINT               DEFAULT 0
  COMMENT '数据左边码',
  right_code BIGINT               DEFAULT 0
  COMMENT '数据右边码'
)
  ENGINE =InnoDB
  DEFAULT CHARSET =utf8
  COMMENT ='角色';