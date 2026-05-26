---
name: code-generator
description: Generate a complete CRUD module (backend Java + frontend Vue) from a database table DDL. Follows SmartAdmin code-generator-template conventions exactly.
user-invocable: true
allowed-tools:
  - Read
  - Write
  - Edit
  - Glob
  - Grep
  - Bash(ls *)
  - Bash(mkdir *)
---

# /code-generator — SmartAdmin CRUD Module Generator

从数据库表 DDL 生成完整的 CRUD 模块代码，严格遵循 `smart-admin-api-java8-springboot2/sa-base/src/main/resources/code-generator-template/` 下的 Velocity 模板风格和 `CLAUDE.md` 中的项目规范。

---

## Step 1: 收集输入信息

要求用户提供以下信息，**每次只问一个问题**：

### 1.1 DDL 或表名

```
请提供数据库表的 DDL 语句（CREATE TABLE），或者已有表名：
```

如果用户只给表名，从已有项目中搜索该表的结构（检查 Mapper XML、Entity 等）。

### 1.2 模块归属

```
该模块属于哪个业务域？
1. system   — 系统功能（员工、部门、角色、菜单等）
2. business — 业务功能（OA、ERP 等），默认
3. support  — 支撑功能（字典、配置、日志等）
```

### 1.3 Java 包名（可跳过）

根据表名自动推断，格式：`net.lab1024.sa.admin.module.{domain}.{module}`

例如 `t_goods` → `net.lab1024.sa.admin.module.business.goods`

### 1.4 作者署名

```
请提供作者名（中文或英文）和日期：
```

---

## Step 2: 解析 DDL 并自动推断配置

解析 DDL 时，自动推断以下映射：

### 2.1 命名转换

| 表名 → 变量 | 规则 | 示例 (`t_goods_spec`) |
|-------------|------|----------------------|
| `tableName` | 原始表名 | `t_goods_spec` |
| `name.upperCamel` | 去 `t_` 前缀，snake→UpperCamel | `GoodsSpec` |
| `name.lowerCamel` | UpperCamel 首字母小写 | `goodsSpec` |
| `name.lowerHyphenCamel` | lowerCamel 转 kebab-case | `goods-spec` |
| `primaryKeyFieldName` | 主键列 lowerCamel | `goodsSpecId` |
| `primaryKeyJavaType` | 主键 Java 类型 | `Long` |

### 2.2 SQL 类型 → Java/JS 类型映射

| SQL Type | Java Type | JS Type | FrontComponent |
|----------|-----------|---------|----------------|
| `varchar`, `char`, `text`, `longtext` | `String` | `String` | `Input` |
| `text` | `String` | `String` | `Textarea` |
| `int`, `tinyint`, `smallint` | `Integer` | `Number` | `InputNumber` |
| `bigint` | `Long` | `Number` | `InputNumber` |
| `decimal`, `double`, `float` | `BigDecimal` | `Number` | `InputNumber` |
| `datetime`, `timestamp` | `LocalDateTime` | `Date` | `DateTime` |
| `date` | `LocalDate` | `Date` | `Date` |
| `bool`, `bit` | `Boolean` | `Boolean` | `BooleanSelect` |
| `tinyint(1)` | `Boolean` | `Boolean` | `BooleanSelect` |

### 2.3 枚举检测

如果列注释包含 `[value:desc, ...]` 或枚举模式，自动提取：
- `enumName`：`{ModuleName}{FieldName}Enum`（如 `GoodsStatusEnum`）
- 枚举值列表
- `frontComponent` → `SmartEnumSelect`
- `queryType` → `Enum`

### 2.4 查询字段类型推断

| 列类型 | queryTypeEnum |
|--------|--------------|
| `String` / `varchar` / `text` | `Like` |
| `Integer` / `Long` / `BigDecimal` / 枚举字段 | `Equal` |
| `Date` / `LocalDate` | `DateRange` |
| `LocalDateTime` | `DateRange` |
| 字典字段 | `Dict` |
| 枚举字段 | `Enum` |

### 2.5 删除策略推断

- 如果表中存在 `deleted_flag` 列 → 软删除（`isPhysicallyDeleted=false`）
- 否则 → 硬删除（`isPhysicallyDeleted=true`）
- 默认 `deleteEnum = SingleAndBatch`（同时支持单个和批量删除）

### 2.6 主键推断

- 检测 `auto_increment` 或列名 `id` / `*_id` → 主键
- 如果主键自增 → `autoIncreaseFlag=true`, `@TableId(type = IdType.AUTO)`
- 否则 → `@TableId`

---

## Step 3: 确认配置后生成

将推断的配置汇总展示给用户确认，然后**按以下顺序逐个生成文件**。

### 输出目录结构

```
后端 (sa-admin/src/main/java/net/lab1024/sa/admin/module/{domain}/{module}/):
  ├── controller/{Name}Controller.java
  ├── service/{Name}Service.java
  ├── manager/{Name}Manager.java
  ├── dao/{Name}Dao.java
  ├── domain/entity/{Name}Entity.java
  ├── domain/form/{Name}AddForm.java
  ├── domain/form/{Name}UpdateForm.java
  ├── domain/form/{Name}QueryForm.java
  ├── domain/vo/{Name}VO.java
  └── constant/{Name}Enum.java (optional)

后端资源:
  └── resources/mapper/{domain}/{module}/{Name}Mapper.xml

SQL:
  └── （追加到数据库 SQL 脚本目录或直接输出）

前端 (src/):
  ├── api/{domain}/{module}/{name}-api.js
  ├── constants/{domain}/{module}/{name}-const.js
  └── views/{domain}/{module}/{name}-list.vue
  └── views/{domain}/{module}/{name}-form-modal.vue (或 drawer)
```

---

## Step 4: 后端代码生成 — 严格模板参照

### 4.1 Entity.java

**模板参照**: `code-generator-template/java/domain/entity/Entity.java.vm`

```java
package ${packageName}.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * ${description} 实体类
 *
 * @Author ${author}
 * @Date ${date}
 * @Copyright ${copyright}
 */

@Data
@TableName("${tableName}")
public class ${Name}Entity {
    #{foreach field}
    /** ${comment} */
    #{if primaryKey && autoIncrement}
    @TableId(type = IdType.AUTO)
    #{else if primaryKey}
    @TableId
    #{end}
    #{if columnName == "create_time"}
    @TableField(fill = FieldFill.INSERT)
    #{else if columnName == "update_time"}
    @TableField(fill = FieldFill.INSERT_UPDATE)
    #{end}
    private ${javaType} ${fieldName};
    #{end}
}
```

**约束**:
- 包名末尾必须是 `domain.entity`
- 必须用 `@Data`（Lombok）
- `@TableName` 值即原始表名
- `createTime` 用 `FieldFill.INSERT`，`updateTime` 用 `FieldFill.INSERT_UPDATE`
- 不需要生成 `deletedFlag` 字段（框架自动处理）
- **不写 @Schema 等 Swagger 注解**（Entity 不暴露给 API）
- **不要写 getter/setter**（@Data 已处理）

### 4.2 AddForm.java

**模板参照**: `code-generator-template/java/domain/form/AddForm.java.vm`

```java
package ${packageName}.domain.form;

import lombok.Data;
import javax.validation.constraints.*;

/**
 * ${description} 新建表单
 *
 * @Author ${author}
 * @Date ${date}
 * @Copyright ${copyright}
 */

@Data
public class ${Name}AddForm {
    #{foreach field: non-primary-key, non-createTime, non-updateTime}
    #{if enum}
    @SchemaEnum(${EnumClass}.class)
    @CheckEnum(${EnumClass}.class)
    #{end}
    #{if required}
    @NotBlank(message = "${label} 不能为空")  // String 类型
    @NotNull(message = "${label} 不能为空")    // 其他类型
    #{end}
    @Schema(description = "${label}")
    private ${javaType} ${fieldName};
    #{end}
}
```

**约束**:
- **不包含主键字段**（新增时无 ID）
- **不包含 createTime/updateTime**（自动填充）
- `String` 类型用 `@NotBlank`，非 String 用 `@NotNull`
- 枚举字段加 `@CheckEnum` + `@SchemaEnum`
- 所有字段加 `@Schema(description = "...")`
- **只包含前端表单需要填写的字段**

### 4.3 UpdateForm.java

**模板参照**: `code-generator-template/java/domain/form/UpdateForm.java.vm`

与 AddForm 相同，但**必须包含主键字段**且标记为 `@NotNull`。

```java
@Data
public class ${Name}UpdateForm {
    @NotNull(message = "${primaryKeyLabel} 不能为空")
    @Schema(description = "${primaryKeyLabel}")
    private ${primaryKeyType} ${primaryKeyField};
    // ... 其余字段同 AddForm
}
```

### 4.4 QueryForm.java

**模板参照**: `code-generator-template/java/domain/form/QueryForm.java.vm`

```java
package ${packageName}.domain.form;

import net.lab1024.sa.base.common.domain.PageParam;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ${Name}QueryForm extends PageParam {
    #{foreach queryField}
    #{if queryType == "DateRange"}
    @Schema(description = "${label} 开始")
    private ${type} ${fieldName}Begin;
    @Schema(description = "${label} 结束")
    private ${type} ${fieldName}End;
    #{else if enum}
    @CheckEnum(${EnumClass}.class)
    @Schema(description = "${label}")
    private ${type} ${fieldName};
    #{else}
    @Schema(description = "${label}")
    private ${type} ${fieldName};
    #{end}
    #{end}
}
```

**约束**:
- **必须继承 `PageParam`**
- **必须有 `@EqualsAndHashCode(callSuper = false)`**
- DateRange 字段拆分为 `Begin` + `End` 两个字段
- 不包含 `pageNum`/`pageSize`/`sortItemList`（PageParam 已定义）

### 4.5 VO.java

**模板参照**: `code-generator-template/java/domain/vo/VO.java.vm`

```java
package ${packageName}.domain.vo;

import lombok.Data;

@Data
public class ${Name}VO {
    #{foreach field: showFlag=true}
    @Schema(description = "${label}")
    private ${javaType} ${fieldName};
    #{end}
}
```

**约束**:
- 包含所有需要在列表/详情中展示的字段
- 每个字段加 `@Schema`
- **不需要 @CheckEnum**（VO 是只读的）

### 4.6 Controller.java

**模板参照**: `code-generator-template/java/controller/Controller.java.vm`

```java
package ${packageName}.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.PageResult;
import net.lab1024.sa.base.common.domain.ValidateList;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@Tag(name = "${description}")
public class ${Name}Controller {

    @Resource
    private ${Name}Service ${name}Service;

    @Operation(summary = "分页查询 @author ${author}")
    @PostMapping("/${name}/queryPage")
    @SaCheckPermission("${name}:query")
    public ResponseDTO<PageResult<${Name}VO>> queryPage(@RequestBody @Valid ${Name}QueryForm queryForm) {
        return ResponseDTO.ok(${name}Service.queryPage(queryForm));
    }

    @Operation(summary = "添加 @author ${author}")
    @PostMapping("/${name}/add")
    @SaCheckPermission("${name}:add")
    public ResponseDTO<String> add(@RequestBody @Valid ${Name}AddForm addForm) {
        return ${name}Service.add(addForm);
    }

    @Operation(summary = "更新 @author ${author}")
    @PostMapping("/${name}/update")
    @SaCheckPermission("${name}:update")
    public ResponseDTO<String> update(@RequestBody @Valid ${Name}UpdateForm updateForm) {
        return ${name}Service.update(updateForm);
    }

    @Operation(summary = "批量删除 @author ${author}")
    @PostMapping("/${name}/batchDelete")
    @SaCheckPermission("${name}:delete")
    public ResponseDTO<String> batchDelete(@RequestBody @Valid ValidateList<${pkType}> idList) {
        return ${name}Service.batchDelete(idList);
    }

    @Operation(summary = "单个删除 @author ${author}")
    @GetMapping("/${name}/delete/{${pkField}}")
    @SaCheckPermission("${name}:delete")
    public ResponseDTO<String> delete(@PathVariable ${pkType} ${pkField}) {
        return ${name}Service.delete(${pkField});
    }
}
```

**约束**:
- `@Resource` 注入 Service（不用 `@Autowired`）
- `@PostMapping` 用于增删改查 mutation
- `@GetMapping` 用于单个删除
- 删除 URL: `/{module}/delete/{id}` (GET)
- 批量删除 URL: `/{module}/batchDelete` (POST, body: `ValidateList<IdType>`)
- 每个方法加 `@SaCheckPermission`
- 权限格式: `{moduleName}:{action}` (如 `goods:query`)
- add/update 返回 `ResponseDTO<String>`
- queryPage 返回 `ResponseDTO<PageResult<${Name}VO>>`

### 4.7 Service.java

**模板参照**: `code-generator-template/java/service/Service.java.vm`

```java
package ${packageName}.service;

import net.lab1024.sa.base.common.util.SmartBeanUtil;
import net.lab1024.sa.base.common.util.SmartPageUtil;
import net.lab1024.sa.base.common.domain.ResponseDTO;
import net.lab1024.sa.base.common.domain.PageResult;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.util.List;

@Service
public class ${Name}Service {

    @Resource
    private ${Name}Dao ${name}Dao;

    public PageResult<${Name}VO> queryPage(${Name}QueryForm queryForm) {
        Page<?> page = SmartPageUtil.convert2PageQuery(queryForm);
        List<${Name}VO> list = ${name}Dao.queryPage(page, queryForm);
        return SmartPageUtil.convert2PageResult(page, list);
    }

    public ResponseDTO<String> add(${Name}AddForm addForm) {
        ${Name}Entity entity = SmartBeanUtil.copy(addForm, ${Name}Entity.class);
        ${name}Dao.insert(entity);
        return ResponseDTO.ok();
    }

    public ResponseDTO<String> update(${Name}UpdateForm updateForm) {
        ${Name}Entity entity = SmartBeanUtil.copy(updateForm, ${Name}Entity.class);
        ${name}Dao.updateById(entity);
        return ResponseDTO.ok();
    }

    #{if softDelete (~deleted_flag)}
    public ResponseDTO<String> batchDelete(List<${pkType}> idList) {
        if (CollectionUtils.isEmpty(idList)) return ResponseDTO.ok();
        ${name}Dao.batchUpdateDeleted(idList, true);
        return ResponseDTO.ok();
    }

    public ResponseDTO<String> delete(${pkType} ${pkField}) {
        if (null == ${pkField}) return ResponseDTO.ok();
        ${name}Dao.updateDeleted(${pkField}, true);
        return ResponseDTO.ok();
    }
    #{else hardDelete}
    public ResponseDTO<String> batchDelete(List<${pkType}> idList) {
        if (CollectionUtils.isEmpty(idList)) return ResponseDTO.ok();
        ${name}Dao.deleteBatchIds(idList);
        return ResponseDTO.ok();
    }

    public ResponseDTO<String> delete(${pkType} ${pkField}) {
        if (null == ${pkField}) return ResponseDTO.ok();
        ${name}Dao.deleteById(${pkField});
        return ResponseDTO.ok();
    }
    #{end}
}
```

**约束**:
- `@Resource` 注入 DAO
- 分页用 `SmartPageUtil.convert2PageQuery` + `SmartPageUtil.convert2PageResult`
- 新增/更新用 `SmartBeanUtil.copy(form, Entity.class)` 转换
- add 返回 `ResponseDTO.ok()` 无 data
- 软删除: 调用 `updateDeleted` / `batchUpdateDeleted`
- 硬删除: 调用 `deleteById` / `deleteBatchIds`（MyBatis-Plus 内置方法）
- 空集合检查: `CollectionUtils.isEmpty(idList)`

### 4.8 Manager.java

**模板参照**: `code-generator-template/java/manager/Manager.java.vm`

```java
package ${packageName}.manager;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ${Name}Manager extends ServiceImpl<${Name}Dao, ${Name}Entity> {
}
```

**约束**:
- **必须继承 `ServiceImpl<Dao, Entity>`**（MyBatis-Plus 提供 CRUD 实现）
- 使用 `@Service`（不用 `@Component`）
- 空类体即可，复杂业务逻辑可在此扩展

### 4.9 Dao.java

**模板参照**: `code-generator-template/java/dao/Dao.java.vm`

```java
package ${packageName}.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ${Name}Dao extends BaseMapper<${Name}Entity> {

    List<${Name}VO> queryPage(Page page, @Param("queryForm") ${Name}QueryForm queryForm);

    #{if softDelete}
    long updateDeleted(@Param("${pkField}") ${pkType} ${pkField}, @Param("deletedFlag") boolean deletedFlag);
    void batchUpdateDeleted(@Param("idList") List<${pkType}> idList, @Param("deletedFlag") boolean deletedFlag);
    #{end}
}
```

**约束**:
- **必须继承 `BaseMapper<Entity>`**
- `@Mapper` 注解
- `queryPage` 方法: 第一个参数是 `Page`（MyBatis-Plus 分页），第二个用 `@Param("queryForm")`
- 软删除方法: `updateDeleted`（单个）+ `batchUpdateDeleted`（批量）
- 硬删除直接用 `BaseMapper.deleteById` / `deleteBatchIds`，不需要声明

### 4.10 Mapper.xml

**模板参照**: `code-generator-template/java/mapper/Mapper.xml.vm`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${daoFullClassName}">

    <sql id="base_columns">
        ${tableName}.col1, ${tableName}.col2, ...
    </sql>

    <select id="queryPage" resultType="${voFullClassName}">
        SELECT <include refid="base_columns"/>
        FROM ${tableName}
        <where>
            #{foreach queryField}
            #{if type == "Like"}
            <if test="queryForm.${field} != null and queryForm.${field} != ''">
                AND INSTR(${tableName}.${column}, #{queryForm.${field}})
            </if>
            #{else if type == "Equal" or type == "Enum"}
            <if test="queryForm.${field} != null">
                AND ${tableName}.${column} = #{queryForm.${field}}
            </if>
            #{else if type == "DateRange"}
            <if test="queryForm.${field}Begin != null">
                AND ${tableName}.${column} &gt;= #{queryForm.${field}Begin}
            </if>
            <if test="queryForm.${field}End != null">
                AND ${tableName}.${column} &lt;= #{queryForm.${field}End}
            </if>
            #{end}
            #{end}
        </where>
    </select>

    #{if softDelete}
    <update id="batchUpdateDeleted">
        UPDATE ${tableName} SET deleted_flag = #{deletedFlag}
        WHERE ${pkColumn} IN
        <foreach collection="idList" open="(" close=")" separator="," item="item">
            #{item}
        </foreach>
    </update>

    <update id="updateDeleted">
        UPDATE ${tableName} SET deleted_flag = #{deletedFlag}
        WHERE ${pkColumn} = #{${pkField}}
    </update>
    #{end}
</mapper>
```

**约束**:
- namespace 用 DAO 的**全限定类名**
- `<sql id="base_columns">` 列出所有列，带表别名前缀
- **模糊搜索用 `INSTR()`**（不是 `LIKE`）
- DateRange 用 `Begin >=` + `End <=`
- 条件判断: String 用 `!= null and != ''`, 数字用 `!= null`
- 软删除 SQL: `UPDATE ... SET deleted_flag = ...`

### 4.11 Menu.sql

**模板参照**: `code-generator-template/java/sql/Menu.sql.vm`

```sql
INSERT INTO t_menu ( menu_name, menu_type, parent_id, path, component, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, create_user_id )
VALUES ( '${description}', 2, 0, '/${name}/list', '/business/${name}/${name}-list.vue', false, false, true, false, 1, 1 );

SET @parent_id = NULL;
SELECT t_menu.menu_id INTO @parent_id FROM t_menu WHERE t_menu.menu_name = '${description}';

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '查询', 3, @parent_id, false, false, true, false, 1, '${name}:query', '${name}:query', @parent_id, 1 );

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '添加', 3, @parent_id, false, false, true, false, 1, '${name}:add', '${name}:add', @parent_id, 1 );

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '更新', 3, @parent_id, false, false, true, false, 1, '${name}:update', '${name}:update', @parent_id, 1 );

INSERT INTO t_menu ( menu_name, menu_type, parent_id, frame_flag, cache_flag, visible_flag, disabled_flag, perms_type, api_perms, web_perms, context_menu_id, create_user_id )
VALUES ( '删除', 3, @parent_id, false, false, true, false, 1, '${name}:delete', '${name}:delete', @parent_id, 1 );
```

**约束**:
- menu_type: 2=菜单, 3=功能点（按钮权限）
- parent_id: 菜单为 0，按钮为 `@parent_id`
- path 格式: `/{module}/list`
- component 路径: `/business/{module}/{module}-list.vue`（根据 domain 调整）
- perms_type: 1
- api_perms / web_perms: `{module}:{action}`

### 4.12 Enum.java（可选）

**模板参照**: `code-generator-template/java/constant/enum.java.vm`

```java
package ${packageName}.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.lab1024.sa.base.common.enumeration.BaseEnum;

@AllArgsConstructor
@Getter
public enum ${EnumName} implements BaseEnum {
    #{foreach enumValue}
    ${key}(${value}, "${desc}"),
    #{end}
    ;
    private final ${valueType} value;
    private final String desc;
}
```

**约束**:
- **必须实现 `BaseEnum`** 接口
- 用 `@AllArgsConstructor` + `@Getter`
- value 类型通常是 `Integer`
- 枚举值用大写英文命名

---

## Step 5: 前端代码生成 — 严格模板参照

### 5.1 API 模块 (`{name}-api.js`)

**模板参照**: `code-generator-template/js/api.js.vm`

```javascript
import { postRequest, getRequest } from '/@/lib/axios';

export const ${name}Api = {
  queryPage: (param) => postRequest('/${name}/queryPage', param),
  add: (param) => postRequest('/${name}/add', param),
  update: (param) => postRequest('/${name}/update', param),
  delete: (id) => getRequest(`/${name}/delete/${id}`),
  batchDelete: (idList) => postRequest('/${name}/batchDelete', idList),
};
```

**约束**:
- 文件路径: `src/api/{domain}/{module}/{name}-api.js`
- 导出命名为 `export const ${name}Api = { ... }`
- 查询/新增/修改/批量删除 → `postRequest`
- 单个删除 → `getRequest`
- URL 前缀与后端 Controller 一致（`/{name}/...`）

### 5.2 常量文件 (`{name}-const.js`)

**模板参照**: `code-generator-template/js/const.js.vm`

```javascript
export const ${ENUM_NAME} = {
  // 从 DDL 注释提取枚举值
  KEY: { value: 0, desc: '描述' },
};

export default {
  ${ENUM_NAME},
};
```

**约束**:
- 文件路径: `src/constants/{domain}/{module}/{name}-const.js`
- 每个枚举对象有 `{ value, desc }` 结构
- default export 聚合所有枚举（供 `smart-enums-plugin` 注册）

### 5.3 列表页面 (`{name}-list.vue`)

**模板参照**: `code-generator-template/js/list.vue.vm`

```vue
<template>
  <!---------- 查询表单 begin ---------->
  <a-form class="smart-query-form">
    <a-row class="smart-query-form-row">
      #{foreach queryField}
      #{if type == "Like" or type == "Equal"}
      <a-form-item label="${label}" class="smart-query-form-item">
        <a-input style="width: ${width}" v-model:value="queryForm.${field}" placeholder="${label}" />
      </a-form-item>
      #{else if type == "Enum"}
      <a-form-item label="${label}" class="smart-query-form-item">
        <SmartEnumSelect width="${width}" v-model:value="queryForm.${field}" enum-name="${enumName}" placeholder="${label}"/>
      </a-form-item>
      #{else if type == "Dict"}
      <a-form-item label="${label}" class="smart-query-form-item">
        <DictSelect :dict-code="'${dictCode}'" placeholder="${label}" v-model:value="queryForm.${field}" width="${width}" />
      </a-form-item>
      #{else if type == "DateRange"}
      <a-form-item label="${label}" class="smart-query-form-item">
        <a-range-picker v-model:value="queryForm.${field}" :presets="defaultTimeRanges" style="width: ${width}" @change="on${Field}Change" />
      </a-form-item>
      #{end}
      #{end}
      <a-form-item class="smart-query-form-item">
        <a-button type="primary" @click="onSearch"><template #icon><SearchOutlined /></template>查询</a-button>
        <a-button @click="resetQuery" class="smart-margin-left10"><template #icon><ReloadOutlined /></template>重置</a-button>
      </a-form-item>
    </a-row>
  </a-form>

  <a-card size="small" :bordered="false" :hoverable="true">
    <a-row class="smart-table-btn-block">
      <div class="smart-table-operate-block">
        <a-button @click="showForm" type="primary" size="small"><template #icon><PlusOutlined /></template>新建</a-button>
        <a-button @click="confirmBatchDelete" type="primary" danger size="small" :disabled="selectedRowKeyList.length == 0"><template #icon><DeleteOutlined /></template>批量删除</a-button>
      </div>
      <div class="smart-table-setting-block">
        <TableOperator v-model="columns" :tableId="null" :refresh="queryData" />
      </div>
    </a-row>

    <a-table size="small" :scroll="{ y: 800 }" :dataSource="tableData" :columns="columns" rowKey="${pkField}" bordered :loading="tableLoading" :pagination="false" :row-selection="{ selectedRowKeys: selectedRowKeyList, onChange: onSelectChange }">
      <template #bodyCell="{ text, record, column }">
        #{foreach field: with enum/dict/file}
        <template v-if="column.dataIndex === '${field}'">
          <!-- enum → $smartEnumPlugin / dict → DictLabel / file → FilePreview -->
        </template>
        #{end}
        <template v-if="column.dataIndex === 'action'">
          <div class="smart-table-operate">
            <a-button @click="showForm(record)" type="link">编辑</a-button>
            <a-button @click="onDelete(record)" danger type="link">删除</a-button>
          </div>
        </template>
      </template>
    </a-table>

    <div class="smart-query-table-page">
      <a-pagination showSizeChanger showQuickJumper show-less-items :pageSizeOptions="PAGE_SIZE_OPTIONS" :defaultPageSize="queryForm.pageSize" v-model:current="queryForm.pageNum" v-model:pageSize="queryForm.pageSize" :total="total" @change="queryData" @showSizeChange="queryData" :show-total="(total) => `共${total}条`" />
    </div>

    <${Name}Form ref="formRef" @reloadList="queryData"/>
  </a-card>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue';
import { message, Modal } from 'ant-design-vue';
import { SmartLoading } from '/@/components/framework/smart-loading';
import { ${name}Api } from '/@/api/${domain}/${module}/${name}-api';
import { PAGE_SIZE_OPTIONS } from '/@/constants/common-const';
import { smartSentry } from '/@/lib/smart-sentry';
import TableOperator from '/@/components/support/table-operator/index.vue';
// ... enum/dict imports as needed
</script>
```

**约束**:
- **`v-model:value`** 双向绑定（不是 `v-model:modelValue`）
- 查询表单用 class `smart-query-form`, `smart-query-form-row`, `smart-query-form-item`
- 表格操作区用 `smart-table-btn-block`, `smart-table-operate-block`
- 分页区 class `smart-query-table-page`
- `PAGE_SIZE_OPTIONS` 从 `@/constants/common-const` 导入
- 错误处理: `smartSentry.captureError(e)`
- `onMounted(queryData)` 初始加载
- DateRange 需要 onChange 回调拆分 Begin/End

### 5.4 表单页面 (`{name}-form-modal.vue`)

**模板参照**: `code-generator-template/js/form.vue.vm`

```vue
<template>
  <a-modal title="..." :width="..." :open="visibleFlag" @cancel="onClose" :maskClosable="false" :destroyOnClose="true">
    <a-form ref="formRef" :model="form" :rules="rules" :label-col="{ span: 5 }">
      #{foreach formField}
      #{if component == "Input"}
      <a-form-item label="${label}" name="${field}">
        <a-input v-model:value="form.${field}" placeholder="${label}" />
      </a-form-item>
      #{else if component == "SmartEnumSelect"}
      <a-form-item label="${label}" name="${field}">
        <SmartEnumSelect v-model:value="form.${field}" enum-name="${enumName}" placeholder="${label}" />
      </a-form-item>
      #{... etc}
      #{end}
      #{end}
    </a-form>
    <template #footer>
      <a-space><a-button @click="onClose">取消</a-button><a-button type="primary" @click="onSubmit">保存</a-button></a-space>
    </template>
  </a-modal>
</template>
```

**约束**:
- 组件命名: `{Name}Form`（UpperCamel）
- 暴露 `show(rowData)` 方法: `defineExpose({ show })`
- `show()` 逻辑: 复制 rowData 到 form（编辑模式），或清空（新增模式）
- 保存: `form.${pk} ? update(form) : add(form)`
- 完成后 `emits('reloadList')` + `onClose()`
- 校验规则: required 字段配 `{ required: true, message: '...' }`
- **非 required 字段不加校验规则**

---

## Step 6: 生成完成后输出摘要

生成完所有文件后，输出一个清单：

```
## 生成完成 — ${Name} 模块

### 后端 (9-11 files)
- [x] ${Name}Entity.java
- [x] ${Name}AddForm.java
- [x] ${Name}UpdateForm.java
- [x] ${Name}QueryForm.java
- [x] ${Name}VO.java
- [x] ${Name}Controller.java
- [x] ${Name}Service.java
- [x] ${Name}Manager.java
- [x] ${Name}Dao.java
- [x] ${Name}Mapper.xml
- [x] menu.sql

### 前端 (4 files)
- [x] ${name}-api.js
- [x] ${name}-const.js
- [x] ${name}-list.vue
- [x] ${name}-form-modal.vue

### 后续步骤
1. 执行 menu.sql 注册菜单权限
2. 重启后端，菜单自动加载
3. 前端运行 npm run dev 验证
```

---

## 关键检查清单

生成每个文件后，自检以下约束:

- [ ] Java 文件: `@Resource` 注入，非 `@Autowired`
- [ ] Controller: `@SaCheckPermission` 格式 `module:action`
- [ ] Entity: `@TableName("t_xxx")`, `@Data`, `@TableId`
- [ ] QueryForm: 继承 `PageParam`, `@EqualsAndHashCode(callSuper = false)`
- [ ] Manager: 继承 `ServiceImpl<Dao, Entity>`
- [ ] DAO: 继承 `BaseMapper<Entity>`, `@Mapper`
- [ ] Mapper.xml: 模糊搜索用 `INSTR()`, namespace 用全限定类名
- [ ] Service: 分页用 `SmartPageUtil`, 转换用 `SmartBeanUtil.copy`
- [ ] Vue: `<script setup>`, `v-model:value`, 绝对路径 `/@/`
- [ ] API: 导出命名对象 `export const xxxApi = { ... }`
- [ ] 软删除表: 所有 Mapper 查询加上 `deleted_flag` 过滤条件（生成后提醒用户）

---

**Begin by asking for the DDL or table name. Only ask ONE question at a time.**
