-- create role--

INSERT INTO sec_role(id,name, value, intro, pid,created_at)
VALUES (1,'超级管理员','R_ADMIN','',0, current_timestamp),
       (2,'系统管理员','R_MANAGER','',1,current_timestamp),
       (3,'总部','R_MEMBER','',2,current_timestamp),
       (4,'分部','R_USER','',2,current_timestamp);

-- create permission--
INSERT INTO sec_permission(id, name,httpMethod, value, url, intro,pid, created_at)
VALUES (1,'管理员目录','*','P_D_ADMIN','/admin/**','',0,current_timestamp),
       (2,'角色权限管理','*','P_ROLE','/admin/role/**','',1,current_timestamp),
       (3,'用户管理','*','P_USER','/admin/user/**','',1,current_timestamp),
       (4,'总部目录','*','P_D_MEMBER','/member/**','',0,current_timestamp),
       (5,'分部目录','*','P_D_USER','/user/**','',0,current_timestamp),
       (6,'用户处理','*','P_USER_CONTROL','/user/branch**','',5,current_timestamp),
       (7,'订单','*','P_ORDER','/order/**','',0,current_timestamp),
       (8,'订单处理','*','P_ORDER_CONTROL','/order/deliver**','',7,current_timestamp),
       (9,'订单更新','*','P_ORDER_UPDATE','/order/update**','',7,current_timestamp),
       (10,'支部订单','*','P_ORDER_BRANCH','/order/branch**','',7,current_timestamp),
       (11,'区域支行处理','*','P_REGION_CONTROL','/order/region**','',7,current_timestamp),
       (12,'收货地址','*','P_Address','/address/**','',0,current_timestamp);

INSERT INTO sec_role_permission(id,role_id, permission_id)
VALUES (1,1,1),(2,1,2),(3,1,3),(4,1,4),(5,1,5),(6,1,6),(7,1,7),(8,1,8),(9,1,9),(10,1,10),(11,1,11),(12,1,12),
       (13,2,1),(14,2,3),(15,2,4),(16,2,5),(17,2,6),(18,2,7),(19,2,8),(20,2,9),(21,2,10),(22,2,11),(23,2,12),
       (24,3,4),(25,3,5),(26,3,6),(27,3,11),
       (28,4,5),(29,4,7),(30,4,9),(31,4,12);

-- user data--
-- create  admin--
INSERT INTO sec_user(id,sid,username, providername, email, phone, password, avatar_url, first_name, last_name, full_name, created_at)
VALUES (1,2,'admin','shengmu','wangrenhui1990@gmail.com','18611434500','$shiro1$SHA-256$500000$iLqsOFPx5bjMGlB0JiNjQQ==$1cPTj9gyPGmYcKGQ8aw3shybrNF1ixdMCm/akFkn71o=','','管理员','圣牧','圣牧.管理员',current_timestamp);

-- create user_info--
INSERT INTO sec_user_info(id,user_id, gender,province_id,city_id,county_id,street)
VALUES (1,1,0,1,2,3,'人民大学');

-- create user_role--
INSERT INTO sec_user_role(id, user_id, role_id)
VALUES (1,1,1);