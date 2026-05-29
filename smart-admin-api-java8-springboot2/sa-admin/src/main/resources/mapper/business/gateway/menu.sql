-- 设备网关管理 菜单（父级：网络管理）
-- 执行方式：mysql -u root -p --default-character-set=utf8mb4 smart_admin_v3 < menu.sql

SET @network_id = (SELECT menu_id FROM t_menu WHERE menu_name = '网络管理' AND menu_type = 1 LIMIT 1);

INSERT INTO t_menu ( menu_name, menu_type, parent_id, path, component, icon, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, create_user_id )
VALUES ( '设备网关', 2, @network_id, '/gateway/list', '/business/gateway/gateway-list.vue', 'GatewayOutlined', false, false, true, false, 1, 1 );

SET @gateway_id = LAST_INSERT_ID();

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '查询', 3, @gateway_id, false, false, true, false, 1, 'gateway:query', 'gateway:query', @gateway_id, 1 );

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '添加', 3, @gateway_id, false, false, true, false, 1, 'gateway:add', 'gateway:add', @gateway_id, 1 );

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '更新', 3, @gateway_id, false, false, true, false, 1, 'gateway:update', 'gateway:update', @gateway_id, 1 );

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '删除', 3, @gateway_id, false, false, true, false, 1, 'gateway:delete', 'gateway:delete', @gateway_id, 1 );
