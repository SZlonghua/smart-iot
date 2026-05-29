-- 设备管理 菜单（父级：物联管理）
-- 执行方式：mysql -u root -p --default-character-set=utf8mb4 smart_admin_v3 < menu.sql

SET @iot_id = (SELECT menu_id FROM t_menu WHERE menu_name = '物联管理' AND menu_type = 1 LIMIT 1);

INSERT INTO t_menu ( menu_name, menu_type, parent_id, path, component, icon, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, create_user_id )
VALUES ( '设备管理', 2, @iot_id, '/device/list', '/business/device/device-list.vue', 'DesktopOutlined', false, false, true, false, 1, 1 );

SET @device_id = LAST_INSERT_ID();

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '查询', 3, @device_id, false, false, true, false, 1, 'device:query', 'device:query', @device_id, 1 );

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '添加', 3, @device_id, false, false, true, false, 1, 'device:add', 'device:add', @device_id, 1 );

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '更新', 3, @device_id, false, false, true, false, 1, 'device:update', 'device:update', @device_id, 1 );

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '删除', 3, @device_id, false, false, true, false, 1, 'device:delete', 'device:delete', @device_id, 1 );
