-- 告警日志 菜单
-- 执行方式：mysql -u root -p --default-character-set=utf8mb4 smart_admin_v3 < menu.sql

-- 创建顶一级菜单：告警管理
INSERT INTO t_menu ( menu_name, menu_type, parent_id, path, component, icon, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, create_user_id )
VALUES ( '告警管理', 1, 0, '', '', 'AlertOutlined', false, false, true, false, 1, 1 );

SET @alarm_mgr_id = LAST_INSERT_ID();

-- 二级菜单：告警日志
INSERT INTO t_menu ( menu_name, menu_type, parent_id, path, component, icon, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, create_user_id )
VALUES ( '告警日志', 2, @alarm_mgr_id, '/alarmLog/list', '/business/alarmlog/alarm-log-list.vue', 'WarningOutlined', false, false, true, false, 1, 1 );

SET @alarm_log_id = LAST_INSERT_ID();

-- 按钮：查询
INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '查询', 3, @alarm_log_id, false, false, true, false, 1, 'alarmLog:query', 'alarmLog:query', @alarm_log_id, 1 );

-- 按钮：处理
INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '处理', 3, @alarm_log_id, false, false, true, false, 1, 'alarmLog:handle', 'alarmLog:handle', @alarm_log_id, 1 );
