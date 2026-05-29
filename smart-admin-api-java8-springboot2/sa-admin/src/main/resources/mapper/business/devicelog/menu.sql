-- 设备日志 菜单（父级：物联管理 #312）
-- 执行方式：mysql -u root -p --default-character-set=utf8mb4 smart_admin_v3 < menu.sql

SET @iot_id = (SELECT menu_id FROM t_menu WHERE menu_name = '物联管理' AND menu_type = 1 LIMIT 1);

INSERT INTO t_menu ( menu_name, menu_type, parent_id, path, component, icon, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, create_user_id )
VALUES ( '设备日志', 2, @iot_id, '/deviceLog/list', '/business/devicelog/device-log-list.vue', 'FileTextOutlined', false, false, true, false, 1, 1 );

SET @dev_log_id = LAST_INSERT_ID();

-- 按钮：查询
INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '查询', 3, @dev_log_id, false, false, true, false, 1, 'deviceLog:query', 'deviceLog:query', @dev_log_id, 1 );
