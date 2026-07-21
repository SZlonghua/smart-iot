-- 协议管理 菜单（父级：网络管理）
-- 执行方式：mysql -u root -p --default-character-set=utf8mb4 smart_admin_v3 < menu.sql

-- 创建父级目录：网络管理（如已存在可跳过）
INSERT INTO t_menu (menu_name, menu_type, parent_id, path, component, icon, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, create_user_id)
SELECT '网络管理', 1, 0, '/network', '', 'GlobalOutlined', false, false, true, false, 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_menu WHERE menu_name = '网络管理' AND menu_type = 1);

-- 如果网络管理是新创建的，用 LAST_INSERT_ID()；否则查出已有 ID
SET @network_id = (SELECT menu_id FROM t_menu WHERE menu_name = '网络管理' AND menu_type = 1 LIMIT 1);

INSERT INTO t_menu ( menu_name, menu_type, parent_id, path, component, icon, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, create_user_id )
VALUES ( '协议管理', 2, @network_id, '/protocol/list', '/business/protocol/protocol-list.vue', 'FileTextOutlined', false, false, true, false, 1, 1 );

SET @protocol_id = LAST_INSERT_ID();

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '查询', 3, @protocol_id, false, false, true, false, 1, 'protocol:query', 'protocol:query', @protocol_id, 1 );

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '添加', 3, @protocol_id, false, false, true, false, 1, 'protocol:add', 'protocol:add', @protocol_id, 1 );

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '更新', 3, @protocol_id, false, false, true, false, 1, 'protocol:update', 'protocol:update', @protocol_id, 1 );

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '删除', 3, @protocol_id, false, false, true, false, 1, 'protocol:delete', 'protocol:delete', @protocol_id, 1 );
