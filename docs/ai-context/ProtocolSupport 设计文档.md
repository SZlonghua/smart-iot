# ProtocolSupport — 协议扩展 SPI 设计文档

## 一、设计动机

IoT 平台需要支持多种协议（MQTT、TCP、CoAP、HTTP、WebSocket 等），每种协议的编解码、认证方式各不相同。`ProtocolSupport` 是面向外部扩展的 SPI（Service Provider Interface），允许以 jar 包插件形式加载新的协议实现，**协议怎么编解码、怎么认证，全部由外部说了算**。

## 二、包结构


```
net.lab1024.sa.base
  ├── common/protocol/                    (SPI 接口层)
  │     ├── ProtocolSupport.java          (中心接口)
  │     ├── ProtocolSupportLoader.java    (加载策略)
  │     ├── ProtocolSupportLoaders.java   (复合加载器)
  │     ├── ProtocolSupportDefinition.java(DTO：协议元数据)
  │     ├── ProtocolSupportManager.java   (协议注册中心)
  │     └── ProtocolSupportDataService.java(数据访问层)
  │
  └── module/support/protocol/            (实现层)
        ├── RenameProtocolSupport.java    (装饰器：重命名包装)
        ├── DefaultProtocolSupportManager.java
        ├── DefaultProtocolSupportLoaders.java
        ├── JarProtocolSupportLoader.java
        ├── LocalProtocolSupportLoader.java
        │
        ├── mqtt/session/                 (MQTT 会话)
        ├── tcp/session/                  (TCP 会话)
        ├── websocket/session/            (WebSocket 会话)
        ├── http/session/                 (HTTP 会话)
        └── coap/session/                 (CoAP 会话)
```

## 三、核心接口

### 3.1 ProtocolSupport

中心接口，包含协议的身份标识、传输层支持、编解码发现、设备认证等全部能力。

```java
public interface ProtocolSupport extends Disposable, Ordered, Comparable<ProtocolSupport> {

    @Nonnull String getId();
    String getName();
    String getDescription();

    /** 支持的 Transport 列表 */
    Flux<? extends Transport> getSupportedTransport();

    /** 根据 Transport 获取对应的消息编解码器 */
    @Nonnull Mono<? extends DeviceMessageCodec> getMessageCodec(Transport transport);

    /** 注册认证器 */
    void addAuthenticator(Transport transport, Authenticator authenticator);

    /** 注册编解码器 */
    void addMessageCodec(Transport transport, DeviceMessageCodec codec);

    /** 获取默认的编解码器 */
    default void addMessageCodec(DeviceMessageCodec codec) { ... }

    /** 设备认证 */
    @Nonnull Mono<AuthenticationResponse> authenticate(
        AuthenticationRequest request, DeviceOperator deviceOperation);

    /** 生命周期 */
    @Override default void dispose() { }
    @Override default int getOrder() { return Integer.MAX_VALUE; }
    @Override default int compareTo(ProtocolSupport o) { return Integer.compare(getOrder(), o.getOrder()); }
}
```

### 3.2 ProtocolSupportDefinition — 协议元数据 DTO

```java
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor @ToString
public class ProtocolSupportDefinition implements Serializable {
    private String id;                          // 协议 ID
    private String name;                        // 协议名称
    private String description;                 // 描述
    private String loader;                      // 加载方式: "jar" / "local"
    private byte state;                         // 状态: 0=禁用, 1=启用
    private Map<String, Object> configuration;  // 配置
}
```

### 3.3 ProtocolSupportLoader / ProtocolSupportLoaders

```java
/** 加载策略 — 根据 ProtocolSupportDefinition 加载对应的 ProtocolSupport 实例 */
public interface ProtocolSupportLoader {

    /** 是否支持该定义 */
    boolean supports(ProtocolSupportDefinition definition);

    /** 加载协议实例 */
    Mono<? extends ProtocolSupport> load(ProtocolSupportDefinition definition);
}

/** 复合加载器 — 遍历内部 Loader 列表，找到第一个 supports=true 的 Loader 并委托加载 */
public interface ProtocolSupportLoaders extends ProtocolSupportLoader {
}
```

### 3.4 ProtocolSupportManager — 注册中心

```java
public interface ProtocolSupportManager {
    Mono<ProtocolSupport> getProtocol(String protocol);
    Flux<ProtocolSupport> getProtocols();
    Mono<Void> register(Mono<ProtocolSupport> protocolSupport);
}
```

### 3.5 ProtocolSupportDataService — 数据访问

```java
public interface ProtocolSupportDataService {
    Flux<ProtocolSupportDefinition> getAll();
    Mono<ProtocolSupportDefinition> getById(String id);
}
```

**实现类：`ProtocolSupportDataServiceImpl`** — 位于 `sa-admin` 模块的 `net.lab1024.sa.admin.module.business.protocol.service` 包下。从 `protocol` 表中读取配置，将 `ProtocolEntity` 转换为 `ProtocolSupportDefinition`：

| ProtocolEntity 字段 | → | ProtocolSupportDefinition 字段 |
|---------------------|---|-------------------------------|
| `id` | → | `id` (String.valueOf) |
| `name` | → | `name` |
| `description` | → | `description` |
| `loader` | → | `loader` (实体新增字段) |
| `jarPath` | → | `configuration.jarPath` |
| `jarName` | → | `configuration.jarName` |

**`ProtocolEntity` 新增字段 `loader`：** `varchar(16) NOT NULL DEFAULT 'jar' COMMENT '加载方式(jar/local)'`。

**`jarPath` 双语义：**

| loader | jarPath 含义 | 前端交互 |
|--------|-------------|---------|
| `jar` | 上传 jar 后的服务端存储路径 | 前端上传 jar 文件，后端保存路径 |
| `local` | 本地 classpath 目标目录（如 `C:\...\sa-admin\target`） | 前端输入文本路径，后端直接保存 |

## 四、加载流程

### 4.1 启动时全量加载

```
应用启动
    │
    ▼
DefaultProtocolSupportManager.init()
    │
    ├── [1] dataService.getAll() → 遍历每条 definition
    │       └── loaders.load(definition)
    │           └── Flux.fromIterable(loaders)
    │               .filter(loader -> loader.supports(definition))  ← 按 loader 字段路由
    │               .next()
    │               .flatMap(loader -> loader.load(definition))
    │       └── register(support) → 存入 Map
    │
    └── [2] eventBus.subscribe() → 监听 ProtocolEntity CRUD 事件
```

### 4.2 事件驱动动态管理

```
前端上传 jar / 配置 local 路径
    │
    ▼
protocol 表 INSERT/UPDATE/DELETE
    │
    ▼
CrudEventInterceptor 发布事件
    │
    ├── SaveAfterEvent   → load → register
    ├── UpdateAfterEvent  → unregister(旧) + dispose → load(新) → register
    └── DeleteAfterEvent  → unregister + dispose (关闭类加载器)
```

## 五、自动装配

### 5.1 ProtocolAutoConfiguration

```java
@Configuration
public class ProtocolAutoConfiguration {

    @Bean(initMethod = "init")
    public ProtocolSupportManager protocolSupportManager(ProtocolSupportLoaders loaders,
                                                          IEventBus eventBus,
                                                          ObjectProvider<ProtocolSupport> customSupport,
                                                          ProtocolSupportDataService dataService) {
        return new DefaultProtocolSupportManager(loaders, eventBus, customSupport, dataService);
    }

    @Bean
    public ProtocolSupportLoaders protocolSupportLoaders(List<ProtocolSupportLoader> loaders) {
        return new DefaultProtocolSupportLoaders(loaders);
    }

    @Bean
    public ProtocolSupportLoader jarProtocolSupportLoader() {
        return new JarProtocolSupportLoader();
    }

    @Bean
    @Profile("dev")
    public ProtocolSupportLoader localProtocolSupportLoader(ApplicationContext applicationContext) {
        return new LocalProtocolSupportLoader(applicationContext);
    }
}
```

## 六、实现类

### 6.1 DefaultProtocolSupportLoaders

组合模式，构造函数接收 `List<ProtocolSupportLoader>`，按 `supports()` 路由：

```java
public class DefaultProtocolSupportLoaders implements ProtocolSupportLoaders {

    private final List<ProtocolSupportLoader> loaders;

    public DefaultProtocolSupportLoaders(List<ProtocolSupportLoader> loaders) {
        this.loaders = loaders;
    }

    @Override
    public Mono<? extends ProtocolSupport> load(ProtocolSupportDefinition definition) {
        return Flux.fromIterable(loaders)
                .filter(loader -> loader.supports(definition))
                .next()
                .switchIfEmpty(Mono.error(() ->
                        new IllegalArgumentException("无匹配的加载器: loader=" + definition.getLoader())))
                .flatMap(loader -> loader.load(definition));
    }
}
```

**路由规则：** 遍历 `loaders` 列表，找到第一个 `supports(definition)` 返回 `true` 的加载器，委托其 `load()`。不再依赖 Map 或 name 匹配。

### 6.2 DefaultProtocolSupportManager

基于 `ConcurrentHashMap` 的协议注册中心。

```java
public class DefaultProtocolSupportManager implements ProtocolSupportManager {

    private final Map<String, ProtocolSupport> registry = new ConcurrentHashMap<>();

    private final ProtocolSupportLoaders loaders;
    private final IEventBus eventBus;
    private final ObjectProvider<ProtocolSupport> customProtocolSupport;
    private final ProtocolSupportDataService dataService;

    public DefaultProtocolSupportManager(ProtocolSupportLoaders loaders,
                                          IEventBus eventBus,
                                          ObjectProvider<ProtocolSupport> customProtocolSupport,
                                          ProtocolSupportDataService dataService) {
        this.loaders = loaders;
        this.eventBus = eventBus;
        this.customProtocolSupport = customProtocolSupport;
        this.dataService = dataService;
    }

    /** 由 Spring @Bean(initMethod = "init") 调用 */
    public void init() {
        // 1. 启动时加载所有已定义的协议
        dataService.getAll()
                .flatMap(def -> loaders.load(def))
                .flatMap(s -> registerInternal(s))
                .then()
                .subscribe();

        // 2. 监听 ProtocolEntity 的 CRUD 事件，动态管理协议
        eventBus.subscribe(SaveAfterEvent.class, this::onProtocolSaved);
        eventBus.subscribe(UpdateAfterEvent.class, this::onProtocolUpdated);
        eventBus.subscribe(DeleteAfterEvent.class, this::onProtocolDeleted);
    }

    /** 保存 → 加载并注册 */
    private void onProtocolSaved(SaveAfterEvent<?> event) {
        if (!isProtocolEntity(event)) return;
        ProtocolSupportDefinition def = toDefinition(event.getAfterData());
        loaders.load(def).flatMap(this::registerInternal).subscribe();
    }

    /** 修改 → 注销旧 → 加载新 → 注册 */
    private void onProtocolUpdated(UpdateAfterEvent<?> event) {
        if (!isProtocolEntity(event)) return;
        String protocolId = String.valueOf(event.getEntityId());
        // 注销旧协议，关闭类加载器
        ProtocolSupport old = registry.remove(protocolId);
        if (old != null) {
            old.dispose();
            log.info("[ProtocolManager] 协议已注销 — id={}", protocolId);
        }
        ProtocolSupportDefinition def = toDefinition(event.getAfterData());
        loaders.load(def).flatMap(this::registerInternal).subscribe();
    }

    /** 删除 → 注销并关闭类加载器 */
    private void onProtocolDeleted(DeleteAfterEvent<?> event) {
        if (!isProtocolEntity(event)) return;
        String protocolId = String.valueOf(event.getEntityId());
        ProtocolSupport removed = registry.remove(protocolId);
        if (removed != null) {
            removed.dispose();  // 关闭 URLClassLoader，释放 jar 文件锁
            log.info("[ProtocolManager] 协议已移除 — id={}", protocolId);
        }
    }
```

### 6.3 JarProtocolSupportLoader

从外部 jar 包加载协议实现。

```java
public class JarProtocolSupportLoader implements ProtocolSupportLoader {

    @Override
    public boolean supports(ProtocolSupportDefinition definition) {
        return "jar".equals(definition.getLoader());
    }

    @Override
    public Mono<? extends ProtocolSupport> load(ProtocolSupportDefinition definition) {
        log.info("[JarLoader] 加载外部 jar — id={}, config={}",
                definition.getId(), definition.getConfiguration());
        // TODO: 后续实现 — URLClassLoader 加载外部 jar，实例化 ProtocolSupport
        return Mono.empty();
    }
}
```

### 6.4 LocalProtocolSupportLoader

从本地 classpath 加载协议实现。

```java
public class LocalProtocolSupportLoader implements ProtocolSupportLoader {

    private final ApplicationContext applicationContext;

    public LocalProtocolSupportLoader(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean supports(ProtocolSupportDefinition definition) {
        return "local".equals(definition.getLoader());
    }

    @Override
    public Mono<? extends ProtocolSupport> load(ProtocolSupportDefinition definition) {
        log.info("[LocalLoader] 加载本地协议 — id={}, config={}",
                definition.getId(), definition.getConfiguration());
        // TODO: 后续实现 — Spring Bean 查找或反射创建
        return Mono.empty();
    }
}
```

### 6.5 RenameProtocolSupport

装饰器模式，包装另一个 `ProtocolSupport`，覆盖 `id/name/description`，其他方法全部委托给 `target`。用于租户定制化场景（同一个协议实现，不同的展示名称）。

## 七、协议管理模块

协议通过系统前端页面管理，无需直接操作数据库或手动放置 jar。

### 7.1 数据库变更

```sql
ALTER TABLE protocol ADD COLUMN loader VARCHAR(16) NOT NULL DEFAULT 'jar' COMMENT '加载方式(jar/local)';
```

### 7.2 `ProtocolEntity` 新增字段

```java
/** 加载方式：jar / local */
private String loader;
```

### 7.3 前端改动

**新增/修改页 — loader 单选项：**

```
<a-form-item label="加载方式">
  <a-radio-group v-model:value="form.loader">
    <a-radio value="jar">Jar 包</a-radio>
    <a-radio value="local">Local 本地</a-radio>
  </a-radio-group>
</a-form-item>
```

**根据 loader 切换表单项：**

| loader | 显示控件 | 说明 |
|--------|---------|------|
| `jar` | 文件上传 | 上传 jar 文件，后端保存路径到 `jarPath` |
| `local` | 文本输入框 | 输入本地 target 目录，如 `C:\...\sa-admin\target`，保存到 `jarPath` |

**列表/查看页 — loader 列：** 直接显示 `jar` 或 `local`。

## 八、外部扩展方式

外部开发者实现协议扩展：

```
1. 创建独立 Maven 项目                        ← 必须
2. 依赖 sa-base-{version}.jar                 ← 必须
3. 后续加载方式灵活，可能的实现路径：
   a) 扫描 jar 中的 ProtocolSupportProvider 来创建 ProtocolSupport
   b) JarProtocolSupportLoader 反射实例化
   c) Spring SPI (META-INF/spring.factories)
   d) ServiceLoader (META-INF/services)
4. 打包为 jar
5. 通过系统前端页面上传 jar 至平台
6. 系统自动在数据库插入一条 ProtocolSupportDefinition (loader="jar")
```

## 九、协议会话关联

`ProtocolSupport` 不直接管理会话。会话由 `DeviceSessionManager`（`device.session` 包）管理。协议实现（如 TCP Server）在建立连接后：
1. 调用 `ProtocolSupport.authenticate()` 认证设备
2. 认证成功后创建对应的协议会话（如 `TcpDeviceSession`）
3. 调用 `DeviceSessionManager.compute()` 注册会话

```
ProtocolSupport (common.protocol)        DeviceSessionManager (device.session)
     │                                          │
     ├── authenticate()  ──────► 认证通过        │
     │                              └──► compute(session)
     ├── getMessageCodec() ──────► 编解码        │
     │                                          │
```

## 十、验证清单

1. 本地加载：`LocalProtocolSupportLoader` 加载 classpath 中的 `ProtocolSupport` Bean
2. Jar 加载：`JarProtocolSupportLoader` 从外部 jar 加载协议实现
3. 注册中心：`DefaultProtocolSupportManager` 注册、查找、列举所有协议
4. 复合路由：`DefaultProtocolSupportLoaders` 根据 `loader` 字段路由到正确的 Loader
5. 装饰器：`RenameProtocolSupport` 覆盖 id/name/description，功能委托给 target
6. Transport 映射：`getMessageCodec(transport)` 返回正确的编解码器
