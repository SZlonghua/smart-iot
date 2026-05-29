-- 产品分类 菜单（父级：物联管理）
-- 执行方式：mysql -u root -p --default-character-set=utf8mb4 smart_admin_v3 < menu.sql

INSERT INTO t_menu (menu_name, menu_type, parent_id, path, component, icon, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, create_user_id)
SELECT '物联管理', 1, 0, '/iot', '', 'AppstoreOutlined', false, false, true, false, 1, 1
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM t_menu WHERE menu_name = '物联管理' AND menu_type = 1);

SET @iot_id = (SELECT menu_id FROM t_menu WHERE menu_name = '物联管理' AND menu_type = 1 LIMIT 1);

INSERT INTO t_menu ( menu_name, menu_type, parent_id, path, component, icon, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, create_user_id )
VALUES ( '产品分类', 2, @iot_id, '/productCategory/list', '/business/productcategory/product-category-list.vue', 'ApartmentOutlined', false, false, true, false, 1, 1 );

SET @category_id = LAST_INSERT_ID();

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '查询', 3, @category_id, false, false, true, false, 1, 'productCategory:query', 'productCategory:query', @category_id, 1 );

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '添加', 3, @category_id, false, false, true, false, 1, 'productCategory:add', 'productCategory:add', @category_id, 1 );

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '更新', 3, @category_id, false, false, true, false, 1, 'productCategory:update', 'productCategory:update', @category_id, 1 );

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '删除', 3, @category_id, false, false, true, false, 1, 'productCategory:delete', 'productCategory:delete', @category_id, 1 );
