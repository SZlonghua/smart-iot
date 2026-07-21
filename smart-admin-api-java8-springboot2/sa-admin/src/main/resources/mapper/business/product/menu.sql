-- 产品管理 菜单（父级：物联管理 #312）
-- 执行方式：mysql -u root -p --default-character-set=utf8mb4 smart_admin_v3 < menu.sql

SET @iot_id = (SELECT menu_id FROM t_menu WHERE menu_name = '物联管理' AND menu_type = 1 LIMIT 1);

INSERT INTO t_menu ( menu_name, menu_type, parent_id, path, component, icon, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, create_user_id )
VALUES ( '产品管理', 2, @iot_id, '/product/list', '/business/product/product-list.vue', 'AppstoreOutlined', false, false, true, false, 1, 1 );

SET @product_id = LAST_INSERT_ID();

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '查询', 3, @product_id, false, false, true, false, 1, 'product:query', 'product:query', @product_id, 1 );

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '添加', 3, @product_id, false, false, true, false, 1, 'product:add', 'product:add', @product_id, 1 );

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '更新', 3, @product_id, false, false, true, false, 1, 'product:update', 'product:update', @product_id, 1 );

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '删除', 3, @product_id, false, false, true, false, 1, 'product:delete', 'product:delete', @product_id, 1 );
