# CRUD 事件系统 — 设计文档

## 一、需求背景

后端任何模块执行「新增」「修改」「删除」操作时，自动发布「操作前事件」和「操作后事件」，方便后续通过事件监听器扩展功能（如缓存刷新、操作审计、数据同步、通知推送等），且**不修改任何现有 Service/Controller 代码**。

### 核心要求

1. **批量操作逐条发事件** — `insertBatch`、`updateBatchById`、`deleteBatchIds` 等批处理方法，每条记录单独发一个事件
2. **事件携带完整数据** — 包含时间戳、操作前数据（beforeData）、操作后数据（afterData）
3. **区分物理删除与软删除** — 事件明确标识删除类型
4. **零侵入** — 现有业务代码无需任何改动
5. **事件类拆分** — 每种事件独立成类，监听器直接按参数类型订阅，无需 `if type == xxx` 判断

---

## 二、技术方案

### 2.1 总体架构

```
                    ┌──────────────────────────┐
                    │      MyBatis Interceptor  │
                    │  (拦截所有 INSERT/UPDATE/  │
                    │   DELETE SQL 执行)          │
                    └──────────┬───────────────┘
                               │
                    ┌──────────▼───────────────┐
                    │   IEventBus                │
                    │   发布事件（同时通知动态订阅  │
                    │   和 Spring @EventListener）│
                    └──────────┬───────────────┘
                               │
          ┌────────────────────┼────────────────────┐
          ▼                    ▼                    ▼
   ┌──────────────┐   ┌──────────────┐   ┌──────────────┐
   │ @EventListener│   │ @TxEventListener│  │ @Async +     │
   │ (同步,事务内) │   │ (事务提交后)    │   │ @TxEventListener│
   └──────────────┘   └──────────────┘   └──────────────┘
```

**选型理由：MyBatis Interceptor 而非 AOP 注解**
- 所有持久化操作最终都经过 MyBatis `Executor.update()`，是唯一的拦截点
- `MappedStatement.getSqlCommandType()` 可准确区分 INSERT / UPDATE / DELETE
- 完全自动化，新增模块无需手动加注解
- 比 Service 层 AOP 更底层，能捕获 Manager、DAO 直接调用的场景

### 2.2 拦截机制

实现 `org.apache.ibatis.plugin.Interceptor`，拦截 `Executor.update(MappedStatement, Object)`：

```
intercept(Invocation):
  1. ThreadLocal 防重入检查 → 如果已在拦截中，直接 proceed
  2. 从 MappedStatement 获取 SqlCommandType (INSERT/UPDATE/DELETE)
  3. 软删除检测 → UPDATE + deletedFlag=true → 归类为 DELETE
  4. 根据 SqlCommandType 和参数类型，逐条发布 BEFORE 事件
  5. invocation.proceed() 执行 SQL
  6. 成功 → 逐条发布 AFTER 事件
  7. 异常 → 不发布 AFTER，直接 rethrow
  8. finally → 清除 ThreadLocal 标记
```

**关键设计：获取 beforeData（操作前数据）**

| 操作 | beforeData 来源 | 获取方式 |
|------|----------------|---------|
| INSERT | 无 | `null`（新增没有"旧数据"） |
| UPDATE | 数据库中的旧记录 | `SqlSessionFactory.openSession()` 新会话查旧值 |
| DELETE | 数据库中的记录 | `SqlSessionFactory.openSession()` 新会话查旧值 |

> **为什么用独立 SqlSession？** 拦截器运行在事务内部，在 `invocation.proceed()` 之前，当前事务还未提交。用 `openSession()` 打开新连接，读到的是已提交的快照，正好是"操作前"的持久化状态。避免了直接访问 MyBatis 内部 Executor/Session 的复杂性和脆弱性。

### 2.3 批量操作逐条发事件

| MyBatis-Plus 方法 | 实际执行方式 | 拦截器行为 |
|---|---|---|
| `insert(entity)` | 单条 SQL | 自然逐条：发 1 个 BEFORE + 1 个 AFTER |
| `saveBatch(list)` | 遍历调用 `insert` | 自然逐条：每条是独立 `Executor.update()` 调用 |
| `updateById(entity)` | 单条 SQL | 自然逐条 |
| `updateBatchById(list)` | 遍历调用 `updateById` | 自然逐条 |
| `deleteById(id)` | 单条 SQL | 自然逐条 |
| `deleteBatchIds(idList)` | **单条** `DELETE ... WHERE id IN (...)` | **需特殊处理**：遍历 idList，逐条查旧值、发事件 |

`deleteBatchIds` 是唯一一次调用只触发一次拦截器但涉及多条记录的批处理场景，拦截器需要：
1. 判断参数是 `Collection` 类型
2. 遍历每个 ID：查旧值 → 发 BEFORE_DELETE
3. 执行 `invocation.proceed()`（批量删除）
4. 遍历每个 ID：发 AFTER_DELETE

---

## 三、事件类层次结构

### 3.1 类图

```
ApplicationEvent (Spring)
  └── CrudEvent (abstract, 公共基类，所有事件公共字段)
        └── DataChangeEvent (abstract, "变更事件" = 保存 + 修改 + 删除)
              ├── WriteEvent (abstract, "写入事件" = 保存 + 修改，不含删除)
              │     ├── SaveBeforeEvent
              │     ├── SaveAfterEvent
              │     ├── UpdateBeforeEvent
              │     └── UpdateAfterEvent
              ├── DeleteBeforeEvent   (直接继承 DataChangeEvent)
              └── DeleteAfterEvent    (直接继承 DataChangeEvent)
```

**设计原则：**
- `CrudEvent` 是顶层抽象类，定义所有公共字段，**不直接实例化**
- `DataChangeEvent` 是中间抽象类，表示"所有数据变更"，供想监听一切变更的监听器使用
- `WriteEvent` 是中间抽象类，表示"写操作"（保存 + 修改），**不含删除**，供只想关注数据写入的监听器使用
- 6 个叶子类各自独立，监听器按参数类型精准匹配
- 不再需要 `CrudEventType` 枚举，事件类型和分组语义全部由 Java 类层次表达

**监听器匹配规则（Spring 自动按类型适配）：**

| 监听器参数类型 | 能匹配到的事件 |
|---|---|
| `CrudEvent` | 全部 6 种事件（保存前/后、修改前/后、删除前/后） |
| `DataChangeEvent` | 全部 6 种事件（= CrudEvent，语义更明确） |
| `WriteEvent` | 4 种：`SaveBeforeEvent`、`SaveAfterEvent`、`UpdateBeforeEvent`、`UpdateAfterEvent`（不含删除） |
| `SaveAfterEvent` | 仅新增后事件 |
| `DeleteAfterEvent` | 仅删除后事件 |
| ... | ... |

### 3.2 包结构

```
net.lab1024.sa.base.common.event
  ├── CrudEvent.java              (顶层抽象基类)
  ├── DataChangeEvent.java        (抽象中间层 — 所有变更)
  ├── WriteEvent.java             (抽象中间层 — 写入操作，不含删除)
  ├── SaveBeforeEvent.java
  ├── SaveAfterEvent.java
  ├── UpdateBeforeEvent.java
  ├── UpdateAfterEvent.java
  ├── DeleteBeforeEvent.java
  └── DeleteAfterEvent.java
```

### 3.3 `CrudEvent` — 抽象基类

```java
package net.lab1024.sa.base.common.event;

import org.springframework.context.ApplicationEvent;
import java.time.LocalDateTime;

/**
 * CRUD 事件基类（抽象类，不直接实例化）
 */
public abstract class CrudEvent extends ApplicationEvent {

    /** 实体全限定类名，如 "net.lab1024.sa.admin.module.business.device.domain.entity.DeviceEntity" */
    private final String entityClassName;

    /** 实体简单类名，如 "DeviceEntity" */
    private final String entitySimpleName;

    /** 数据库表名（从 @TableName 注解获取），如 "device" */
    private final String tableName;

    /** 主键值 */
    private final Long entityId;

    /** 操作前数据：INSERT→null，UPDATE→旧实体，DELETE→删除前实体 */
    private final Object beforeData;

    /** 操作后数据：INSERT→入库后实体（含自增ID），UPDATE→新实体，DELETE→null */
    private final Object afterData;

    /** 是否为物理删除（true = DELETE SQL，false = 软删除 update deletedFlag=true） */
    private final boolean physicalDelete;

    /** 事件发生时间 */
    private final LocalDateTime eventTime;

    /**
     * 子类通过此构造器初始化公共字段
     * @param source 事件源（传入 MappedStatement 或 entity）
     */
    protected CrudEvent(Object source, String entityClassName, String entitySimpleName,
                        String tableName, Long entityId, Object beforeData,
                        Object afterData, boolean physicalDelete) {
        super(source);
        this.entityClassName = entityClassName;
        this.entitySimpleName = entitySimpleName;
        this.tableName = tableName;
        this.entityId = entityId;
        this.beforeData = beforeData;
        this.afterData = afterData;
        this.physicalDelete = physicalDelete;
        this.eventTime = LocalDateTime.now();
    }

    // ========== getter ==========
    public String getEntityClassName()  { return entityClassName; }
    public String getEntitySimpleName() { return entitySimpleName; }
    public String getTableName()        { return tableName; }
    public Long   getEntityId()         { return entityId; }
    public Object getBeforeData()       { return beforeData; }
    public Object getAfterData()        { return afterData; }
    public boolean isPhysicalDelete()   { return physicalDelete; }
    public LocalDateTime getEventTime() { return eventTime; }

    /**
     * 判断事件源是否为指定实体类型
     */
    public boolean isEntityType(Class<?> entityClass) {
        return entityClass.getName().equals(this.entityClassName);
    }
}
```

### 3.4 中间抽象类 — `DataChangeEvent` 和 `WriteEvent`

```java
/**
 * "数据变更事件" 抽象中间层 — 涵盖所有 6 种事件（保存 + 修改 + 删除）
 * 监听器声明此类型即可监听一切数据变更
 */
public abstract class DataChangeEvent extends CrudEvent {
    protected DataChangeEvent(Object source, String entityClassName, String entitySimpleName,
                              String tableName, Long entityId, Object beforeData,
                              Object afterData, boolean physicalDelete) {
        super(source, entityClassName, entitySimpleName, tableName, entityId,
              beforeData, afterData, physicalDelete);
    }
}

/**
 * "写入事件" 抽象中间层 — 仅涵盖保存 + 修改（共 4 种事件），不包含删除
 * 监听器声明此类型即可监听所有写入操作，自动排除删除
 */
public abstract class WriteEvent extends DataChangeEvent {
    protected WriteEvent(Object source, String entityClassName, String entitySimpleName,
                         String tableName, Long entityId, Object beforeData,
                         Object afterData) {
        super(source, entityClassName, entitySimpleName, tableName, entityId,
              beforeData, afterData, false);
    }
}
```

### 3.5 6 个叶子类

每个子类极其简洁，仅提供构造器，行为完全由继承的类名表达：

```java
/** 新增前事件 */
public class SaveBeforeEvent extends WriteEvent {
    public SaveBeforeEvent(Object source, String entityClassName, String entitySimpleName,
                           String tableName, Long entityId, Object beforeData,
                           Object afterData) {
        super(source, entityClassName, entitySimpleName, tableName, entityId,
              beforeData, afterData);
    }
}

/** 新增后事件 */
public class SaveAfterEvent extends WriteEvent {
    public SaveAfterEvent(Object source, String entityClassName, String entitySimpleName,
                          String tableName, Long entityId, Object beforeData,
                          Object afterData) {
        super(source, entityClassName, entitySimpleName, tableName, entityId,
              beforeData, afterData);
    }
}

/** 修改前事件 */
public class UpdateBeforeEvent extends WriteEvent {
    public UpdateBeforeEvent(Object source, String entityClassName, String entitySimpleName,
                             String tableName, Long entityId, Object beforeData,
                             Object afterData) {
        super(source, entityClassName, entitySimpleName, tableName, entityId,
              beforeData, afterData);
    }
}

/** 修改后事件 */
public class UpdateAfterEvent extends WriteEvent {
    public UpdateAfterEvent(Object source, String entityClassName, String entitySimpleName,
                            String tableName, Long entityId, Object beforeData,
                            Object afterData) {
        super(source, entityClassName, entitySimpleName, tableName, entityId,
              beforeData, afterData);
    }
}

/** 删除前事件（含物理删除和软删除） */
public class DeleteBeforeEvent extends DataChangeEvent {
    public DeleteBeforeEvent(Object source, String entityClassName, String entitySimpleName,
                             String tableName, Long entityId, Object beforeData,
                             Object afterData, boolean physicalDelete) {
        super(source, entityClassName, entitySimpleName, tableName, entityId,
              beforeData, afterData, physicalDelete);
    }
}

/** 删除后事件（含物理删除和软删除） */
public class DeleteAfterEvent extends DataChangeEvent {
    public DeleteAfterEvent(Object source, String entityClassName, String entitySimpleName,
                            String tableName, Long entityId, Object beforeData,
                            Object afterData, boolean physicalDelete) {
        super(source, entityClassName, entitySimpleName, tableName, entityId,
              beforeData, afterData, physicalDelete);
    }
}
```

> **注意：** Save/Update 四个类 extends `WriteEvent` → `DataChangeEvent` → `CrudEvent`
> Delete 两个类 extends `DataChangeEvent` → `CrudEvent`（不继承 WriteEvent，因为删除不是写操作）

### 3.5 各事件的数据填充规则

| 事件类 | `beforeData` | `afterData` | `entityId` | `physicalDelete` |
|--------|-------------|------------|-----------|-----------------|
| `SaveBeforeEvent` | `null` | 传入的 entity | `null`（自增 ID 尚未生成） | `false` |
| `SaveAfterEvent` | `null` | 入库后的 entity（含自增 ID） | 入库后的 ID | `false` |
| `UpdateBeforeEvent` | 从 DB 查出的旧 entity | 传入的新 entity | entity ID | `false` |
| `UpdateAfterEvent` | 从 DB 查出的旧 entity | **`invocation.proceed()` 后重新查 DB**，完整的最新数据 | entity ID | `false` |
| `DeleteBeforeEvent`（物理） | 从 DB 查出的 entity | `null` | entity ID | **`true`** |
| `DeleteAfterEvent`（物理） | 从 DB 查出的 entity | `null` | entity ID | **`true`** |
| `DeleteBeforeEvent`（软删） | 从 DB 查出的 entity（deletedFlag=false） | entity（deletedFlag=true） | entity ID | **`false`** |
| `DeleteAfterEvent`（软删） | 从 DB 查出的 entity（deletedFlag=false） | **`invocation.proceed()` 后重新查 DB**，完整数据（含 deletedFlag=true） | entity ID | **`false`** |

> **关键设计：** `UpdateAfterEvent` 和 `DeleteAfterEvent`（软删除）的 `afterData` 是在 `invocation.proceed()` 执行 SQL 后，**重新通过独立 SqlSession 查询数据库**获得的完整数据。因为 MyBatis-Plus `updateById` 只更新非 null 字段，前端传入的 `entity` 可能只包含部分字段（如仅 `{name: "新名称"}`），直接使用会导致 `afterData` 残缺。重新查库确保了监听器拿到的是更新后的完整实体。

### 3.6 软删除检测逻辑

```
detectSoftDelete(SqlCommandType, parameter):
  if SqlCommandType != UPDATE → return false
  if parameter is not an entity → return false
  反射查找 parameter 类中是否有 "deletedFlag" 字段（类型 Boolean）
  if 存在 && Boolean.TRUE.equals(deletedFlag 的值) → return true
  else → return false
```

**覆盖范围：** CategoryEntity、GoodsEntity、EmployeeEntity、MenuEntity、PositionEntity、NoticeEntity、EnterpriseEntity、BankEntity、InvoiceEntity 等所有带 `Boolean deletedFlag` 字段的实体。

DeviceEntity、ProductEntity、GatewayEntity、ProtocolEntity 等没有 `deletedFlag` 字段的实体，走物理删除，不会被误判。

---

## 四、实现细节

### 4.1 获取 beforeData — 通过独立 SqlSession 查询

```
queryBeforeData(mappedStatement, parameter, entityClass, entityId):
  // 仅 UPDATE 和 DELETE 需要查 beforeData
  if SqlCommandType == INSERT → return null

  // 从 MappedStatement ID 推导 selectById 的 Statement ID
  // 例：xxx.DeviceDao.updateById → xxx.DeviceDao.selectById
  String msId = mappedStatement.getId()
  String selectMsId = msId.substring(0, msId.lastIndexOf('.')) + ".selectById"

  try (SqlSession session = sqlSessionFactory.openSession()):
    return session.selectOne(selectMsId, entityId)
```

### 4.2 反射缓存

所有反射操作（找 `@TableId`、`@TableName`、`deletedFlag` 字段）使用 `ConcurrentHashMap<Class<?>, EntityMetadata>` 缓存：

```java
class EntityMetadata {
    Field idField;          // 标注 @TableId 的字段（已 setAccessible(true)）
    String tableName;       // @TableName.value()
    Field deletedFlagField; // Boolean deletedFlag 字段，没有则为 null
}
```

首次遇到新实体类时通过反射解析并缓存，后续直接从 Map 读取，保证热路径性能。

### 4.3 ThreadLocal 防重入

```java
private static final ThreadLocal<Boolean> INTERCEPTING = ThreadLocal.withInitial(() -> false);

// 在 intercept() 开头：
if (Boolean.TRUE.equals(INTERCEPTING.get())) {
    return invocation.proceed();  // 直接放行，不发事件
}
INTERCEPTING.set(true);
try { ... } finally { INTERCEPTING.set(false); }
```

**场景：** 事件监听器内部触发了数据库写操作（如在 AFTER_INSERT 中写操作日志），如果没有防重入，会死循环。

### 4.4 异常处理

- `invocation.proceed()` 抛异常 → 不发布 AFTER 事件，异常直接 rethrow，事务正常回滚
- BEFORE 事件监听器抛异常 → 事务回滚，SQL 未执行
- AFTER 事件监听器抛异常 → 取决于监听方式：
    - `@EventListener` 同步执行 → 异常会导致事务回滚
    - `@TransactionalEventListener(phase = AFTER_COMMIT)` → 事务已提交，异常无法回滚（需监听器自行处理）

### 4.5 事件过滤器 — 排除不需要发事件的表

拦截器在发布事件前，先检查是否需要跳过。过滤通过 `CrudEventConfig` 配置实现。

#### 过滤器配置

支持三种过滤维度，自由组合：

| 过滤维度 | 配置项 | 说明 |
|----------|-------|------|
| 表名排除 | `excludeTableNames` | `Set<String>`，直接写表名如 `"t_employee"`, `"t_menu"`, `"t_role"` |
| 实体类排除 | `excludeEntityClasses` | `Set<Class<?>>`，如 `EmployeeEntity.class`, `MenuEntity.class` |
| 表名前缀排除 | `excludeTablePrefixes` | `Set<String>`，如 `"t_sys_"` 排除所有系统表 |

> **配置方式：** 在 `application.yaml` 中编写，或通过 `@Bean` 注入 `CrudEventConfig`

#### application.yaml 配置示例

```yaml
# CRUD 事件配置
crud-event:
  enabled: true                                # 总开关，默认 true

  # 按表名排除（不发送事件的表）
  exclude-table-names:
    # ====== 用户/权限模块 ======
    - t_employee             # 员工管理
    - t_menu                 # 菜单管理
    - t_role                 # 角色管理
    - t_role_menu            # 角色-菜单关联
    - t_role_employee        # 角色-员工关联
    - t_department           # 部门管理
    - t_position             # 岗位管理
    # ====== 系统支撑表 ======
    - t_operate_log          # 操作日志
    - t_data_tracer          # 数据追踪

  # 按表名前缀排除（可选）
  # exclude-table-prefixes:
  #   - t_sys_

  # 按实体类全限定名排除（可选）
  # exclude-entity-classes:
  #   - net.lab1024.sa.admin.module.system.employee.domain.entity.EmployeeEntity
```

#### 拦截器中的过滤判断

```java
// 在 intercept() 中，提取到 tableName 后立即判断：
if (crudEventConfig != null && !crudEventConfig.isEnabled()) {
    return invocation.proceed();  // 总开关关闭，不发事件
}
if (crudEventConfig != null && crudEventConfig.shouldExclude(tableName, entityClass)) {
    return invocation.proceed();  // 该表被排除，不发事件
}
// ... 继续正常的事件发布流程
```

### 4.6 拦截器中根据 SqlCommandType 选择事件类

```java
// 伪代码：在 intercept() 中根据 SqlCommandType 构建不同的事件实例
switch (sqlCommandType) {
    case INSERT:
        // 逐条发 SaveBeforeEvent
        publishEvent(new SaveBeforeEvent(source, entityClassName, ...));
        result = invocation.proceed();
        // 逐条发 SaveAfterEvent
        publishEvent(new SaveAfterEvent(source, entityClassName, ...));
        break;
    case UPDATE:
        boolean isSoftDel = detectSoftDelete(parameter);
        if (isSoftDel) {
            // 归类为删除
            publishEvent(new DeleteBeforeEvent(source, ..., /*physicalDelete=*/false));
            result = invocation.proceed();
            Object afterData = queryBeforeData(selectMsId, entityId);  // 重新查库拿完整数据
            publishEvent(new DeleteAfterEvent(source, ..., afterData, /*physicalDelete=*/false));
        } else {
            publishEvent(new UpdateBeforeEvent(source, ...));
            result = invocation.proceed();
            Object afterData = queryBeforeData(selectMsId, entityId);  // 重新查库拿完整数据
            publishEvent(new UpdateAfterEvent(source, ..., beforeData, afterData));
        }
        break;
    case DELETE:
        // 物理删除
        publishEvent(new DeleteBeforeEvent(source, ..., /*physicalDelete=*/true));
        result = invocation.proceed();
        publishEvent(new DeleteAfterEvent(source, ..., /*physicalDelete=*/true));
        break;
}
```

---

## 五、文件清单

### 5.1 新建文件（`sa-base` 模块）

| # | 文件路径 | 说明 |
|---|---------|------|
| 1 | `sa-base/src/main/java/net/lab1024/sa/base/common/event/CrudEvent.java` | 顶层抽象基类，定义公共字段 |
| 2 | `sa-base/src/main/java/net/lab1024/sa/base/common/event/DataChangeEvent.java` | 抽象中间层 — 所有变更（保存+修改+删除） |
| 3 | `sa-base/src/main/java/net/lab1024/sa/base/common/event/WriteEvent.java` | 抽象中间层 — 写入操作（保存+修改，不含删除） |
| 4 | `sa-base/src/main/java/net/lab1024/sa/base/common/event/SaveBeforeEvent.java` | 新增前事件 |
| 5 | `sa-base/src/main/java/net/lab1024/sa/base/common/event/SaveAfterEvent.java` | 新增后事件 |
| 6 | `sa-base/src/main/java/net/lab1024/sa/base/common/event/UpdateBeforeEvent.java` | 修改前事件 |
| 7 | `sa-base/src/main/java/net/lab1024/sa/base/common/event/UpdateAfterEvent.java` | 修改后事件 |
| 8 | `sa-base/src/main/java/net/lab1024/sa/base/common/event/DeleteBeforeEvent.java` | 删除前事件（物理删除 + 软删除） |
| 9 | `sa-base/src/main/java/net/lab1024/sa/base/common/event/DeleteAfterEvent.java` | 删除后事件（物理删除 + 软删除） |
| 10 | `sa-base/src/main/java/net/lab1024/sa/base/module/support/crudevent/core/CrudEventInterceptor.java` | MyBatis 拦截器（核心） |
| 11 | `sa-base/src/main/java/net/lab1024/sa/base/module/support/crudevent/core/EntityMetadata.java` | 实体反射元数据缓存 |
| 12 | `sa-base/src/main/java/net/lab1024/sa/base/module/support/crudevent/core/CrudEventConfig.java` | 配置（总开关 + 过滤器：表名排除/前缀排除/实体类排除） |

### 5.2 修改文件

| # | 文件路径 | 改动 |
|---|---------|------|
| 1 | `sa-base/src/main/java/net/lab1024/sa/base/config/AsyncConfig.java` | 添加 `@EnableAsync` 注解 |

**现有业务代码：零改动。**

---

## 六、使用示例（监听器按类名精准匹配）

### 6.1 监听某一实体的所有事件（泛型自动匹配，无需 isEntityType）

```java
@Component
@Slf4j
public class DeviceEventListener {

    /** 直接声明泛型参数，Spring 自动按 SaveAfterEvent<DeviceEntity> 匹配 */
    @EventListener
    public void onSave(SaveAfterEvent<DeviceEntity> event) {
        DeviceEntity device = event.getAfterData();
        log.info("设备新增成功: id={}, name={}", event.getEntityId(), device.getName());
        // 更新缓存...
    }

    @EventListener
    public void onUpdate(UpdateAfterEvent<DeviceEntity> event) {
        DeviceEntity before = event.getBeforeData();
        DeviceEntity after = event.getAfterData();
        log.info("设备修改: id={}, before={}, after={}", event.getEntityId(),
                before.getName(), after.getName());
    }

    @EventListener
    public void onDelete(DeleteAfterEvent<DeviceEntity> event) {
        log.info("设备删除: id={}, physicalDelete={}", event.getEntityId(), event.isPhysicalDelete());
    }
}
```

> **关键设计：** `CrudEvent<T>` 实现 `ResolvableTypeProvider`，监听器声明 `SaveAfterEvent<DeviceEntity>` 即可精准匹配该实体类型的保存事件，无需 `isEntityType()` 守卫或强转。

### 6.2 监听新增后事件

```java
@Component
@Slf4j
public class DeviceCacheListener {

    /** 直接监听 SaveAfterEvent，无需 if 判断事件类型 */
    @EventListener
    public void onDeviceCreated(SaveAfterEvent event) {
        if (!"DeviceEntity".equals(event.getEntitySimpleName())) return;

        DeviceEntity device = (DeviceEntity) event.getAfterData();
        log.info("设备新增成功: id={}, name={}", event.getEntityId(), device.getName());
        // 更新缓存...
    }
}
```

### 6.2 监听修改后事件（事务提交后异步，对比新旧数据）

```java
@Component
@Slf4j
public class ChangeLogListener {

    /** 只监听 UpdateAfterEvent，按参数类型自动匹配 */
    @Async("smart-async-executor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEntityUpdated(UpdateAfterEvent event) {
        Object before = event.getBeforeData();  // 旧数据
        Object after  = event.getAfterData();   // 新数据
        // 对比 before/after，记录变更日志...
        log.info("实体修改: entity={}, id={}, old={}, new={}",
            event.getEntitySimpleName(), event.getEntityId(), before, after);
    }
}
```

### 6.3 监听删除前事件做校验（可阻止删除）

```java
@Component
public class DeleteValidationListener {

    /** 直接监听 DeleteBeforeEvent */
    @EventListener
    public void onBeforeDelete(DeleteBeforeEvent event) {
        // 物理删除才做额外校验
        if (event.isPhysicalDelete() && "DeviceEntity".equals(event.getEntitySimpleName())) {
            DeviceEntity device = (DeviceEntity) event.getBeforeData();
            if ("ONLINE".equals(device.getStatus())) {
                throw new BusinessException("在线设备不允许删除");
            }
        }
    }
}
```

### 6.4 监听所有变更事件（用 DataChangeEvent 统一处理保存/修改/删除）

```java
@Component
@Slf4j
public class CacheEvictListener {

    /** 任何数据变更（新增/修改/删除）都刷新缓存 */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDataChanged(DataChangeEvent event) {
        log.info("数据变更: entity={}, id={}, event={}",
            event.getEntitySimpleName(), event.getEntityId(), event.getClass().getSimpleName());
        // 按表名刷新对应缓存...
        cacheManager.evict(event.getTableName());
    }
}
```

### 6.5 监听所有写入事件（用 WriteEvent，排除删除操作）

```java
@Component
@Slf4j
public class SearchIndexSyncListener {

    /** 只关注保存/修改，删除走单独的监听器 */
    @Async("smart-async-executor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDataWritten(WriteEvent event) {
        // WriteEvent 只会匹配 SaveAfterEvent 和 UpdateAfterEvent
        // DeleteAfterEvent 不会触发此方法
        Object data = event.getAfterData();
        searchEngine.index(event.getTableName(), String.valueOf(event.getEntityId()), data);
        log.info("同步搜索索引: entity={}, id={}", event.getEntitySimpleName(), event.getEntityId());
    }
}
```

### 6.6 监听所有删除后事件（统一处理）

```java
@Component
@Slf4j
public class DeleteAuditListener {

    /** 监听 DeleteAfterEvent，不论是物理删除还是软删除 */
    @EventListener
    public void onAfterDelete(DeleteAfterEvent event) {
        boolean physical = event.isPhysicalDelete();
        log.info("数据已删除: entity={}, id={}, 方式={}, 数据={}",
            event.getEntitySimpleName(), event.getEntityId(),
            physical ? "物理删除" : "软删除",
            event.getBeforeData());
    }
}
```

### 6.7 监听某一实体的所有事件（泛型匹配方式）

```java
@Component
@Slf4j
public class DeviceEventListener {

    @EventListener
    public void onSave(SaveAfterEvent<DeviceEntity> event) {
        DeviceEntity device = event.getAfterData();
        // 处理设备新增，无需 isEntityType 守卫
    }

    @EventListener
    public void onUpdate(UpdateAfterEvent<DeviceEntity> event) {
        // 处理设备修改
    }

    @EventListener
    public void onDelete(DeleteAfterEvent<DeviceEntity> event) {
        // 处理设备删除
    }
}
```

> **对比旧方案：** 旧方案需要 `if (event.getType() != CrudEventType.AFTER_INSERT) return` 或 `if (event.isEntityType(DeviceEntity.class))`，新方案通过 `ResolvableTypeProvider` 泛型直接声明参数类型为 `SaveAfterEvent<DeviceEntity>`，Spring 自动精准匹配，无需任何 if 守卫。

---

## 七、验证方案

1. **编译验证**：`mvn clean compile -Pdev`，确保 12 个新文件 + 1 个修改文件编译通过
2. **单条新增**：通过 API 新增设备 → 日志中确认 `SaveBeforeEvent` + `SaveAfterEvent`
3. **单条修改**：修改设备名称 → 确认 `UpdateBeforeEvent` (beforeData=旧值) + `UpdateAfterEvent` (afterData=新值)
4. **物理删除**：删除设备 → 确认 `DeleteBeforeEvent` + `DeleteAfterEvent`，`physicalDelete=true`
5. **软删除**：删除类目 → 确认 `DeleteBeforeEvent` + `DeleteAfterEvent`，`physicalDelete=false`
6. **批量新增**：`saveBatch` 插入 3 条 → 每一条都应有独立的 `SaveBeforeEvent` + `SaveAfterEvent`，共 6 个事件
7. **批量删除**：`deleteBatchIds` 删除 5 个设备 → 每一条都应有独立的 `DeleteBeforeEvent` + `DeleteAfterEvent`，共 10 个事件
8. **事务回滚**：BEFORE 监听器抛出异常 → 确认 SQL 未执行
9. **防重入**：AFTER 监听器中执行 DAO 写入 → 确认不触发新事件（无死循环）
10. **异步监听**：`@Async` + `@TransactionalEventListener(AFTER_COMMIT)` → 确认在不同线程执行且事务已提交
11. **类型精准匹配**：创建只监听 `SaveAfterEvent` 的监听器 → 确认只有新增操作触发，修改/删除不触发
12. **WriteEvent 匹配**：创建监听 `WriteEvent` 的监听器 → 确认新增和修改触发，删除不触发
13. **DataChangeEvent 匹配**：创建监听 `DataChangeEvent` 的监听器 → 确认新增、修改、删除都触发
14. **过滤器排除**：在 `application.yaml` 中配置 `exclude-table-names: [device]` → 新增/修改/删除设备不触发事件
15. **总开关关闭**：`crud-event.enabled: false` → 所有表都不触发事件
