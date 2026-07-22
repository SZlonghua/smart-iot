# EventBus 设计文档

## 一、需求背景

Spring 的 `@EventListener` 是静态订阅 — 编码时写死方法名和事件类型，无法运行时动态增删。需要一套动态事件总线，支持：

1. 运行时 `subscribe(eventType, handler)` 注册处理器
2. 运行时 `unsubscribe(eventType, handler)` 注销处理器
3. 同步和异步两种发布模式
4. 与 Spring 原生事件共存（publish 同时通知 @EventListener）

## 二、接口定义

### 2.1 ResolvableTypeUtil — 泛型类型解析工具

```java
/**
 * 从事件对象中提取 ResolvableType。
 * 优先使用 ResolvableTypeProvider（CrudEvent 已实现），
 * 否则回退为普通 Object 类型。
 */
public class ResolvableTypeUtil {

    public static ResolvableType resolve(Object event) {
        if (event instanceof ResolvableTypeProvider) {
            return ((ResolvableTypeProvider) event).getResolvableType();
        }
        return ResolvableType.forInstance(event);
    }
}
```

> CrudEvent 不变，自己实现 `ResolvableTypeProvider`。其他事件想参与泛型匹配就实现 `ResolvableTypeProvider`，不想就算了 — `publish` 仍接受 `Object`。

### 2.2 EventHandler — 事件处理器（自带泛型类型）

```java
/**
 * 事件处理器 — 通过匿名子类携带完整泛型事件类型。
 * 用法:
 *   new EventHandler<SaveAfterEvent<DeviceEntity>>() {
 *       public void handle(SaveAfterEvent<DeviceEntity> event) { ... }
 *   }
 *
 * 原理和 @EventListener 一样：从字节码的泛型签名中提取类型信息。
 * getGenericSuperclass() → SaveAfterEvent<DeviceEntity> → ResolvableType
 */
public abstract class EventHandler<T> {

    private final ResolvableType eventType;

    protected EventHandler() {
        // 从匿名子类提取泛型: new EventHandler<SaveAfterEvent<DeviceEntity>>() {}
        // → getGenericSuperclass() = EventHandler<SaveAfterEvent<DeviceEntity>>
        // → 泛型参数 = SaveAfterEvent<DeviceEntity>
        ResolvableType superType = ResolvableType.forClass(getClass()).getSuperType();
        this.eventType = superType.getGeneric(0);
    }

    public ResolvableType getEventType() {
        return eventType;
    }

    /** 处理事件 */
    public abstract void handle(T event);
}
```

### 2.2 IEventBus — 事件总线

```java
/**
 * 动态事件总线。
 */
public interface IEventBus {

    /**
     * 注册事件处理器（不限制泛型）。
     */
    <T> Subscription subscribe(Class<T> eventType, Consumer<T> handler);

    /**
     * 注册事件处理器 + 泛型约束。
     */
    <T> Subscription subscribe(EventHandler<T> eventHandler);

    void unsubscribe(Subscription subscription);

    /** 同步发布 */
    void publish(Object event);

    /** 异步发布 */
    void publishAsync(Object event);
}
```

### 2.3 Subscription — 注册凭证

```java
public interface Subscription {
    ResolvableType getEventType();
    boolean isActive();
    void cancel();
}
```

### 2.4 泛型匹配规则

```
订阅:
  eventBus.subscribe(new EventHandler<SaveAfterEvent<DeviceEntity>>() {
      public void handle(SaveAfterEvent<DeviceEntity> event) { ... }
  });
  → EventHandler 内部自动解析为 ResolvableType: SaveAfterEvent<DeviceEntity>

发布:
  事件 SaveAfterEvent<DeviceEntity> 实现 ResolvableTypeProvider

匹配:
  ResolvableType.isAssignableFrom(EventHandler.type, 发布事件.type)
  → SaveAfterEvent vs SaveAfterEvent ✓
  → DeviceEntity vs DeviceEntity ✓
  → 触发 handle()

无泛型订阅:
  subscribe(SaveAfterEvent.class, consumer)
  → ResolvableType.forClass(SaveAfterEvent.class)
  → hasGenerics() = false → 匹配所有 SaveAfterEvent<?>
```

## 三、实现

### 3.1 SpringEventBus

```java
public class SpringEventBus implements IEventBus {

    private final Map<Class<?>, List<SubscriptionEntry<?>>> subscribers = new ConcurrentHashMap<>();
    private final ApplicationEventPublisher springPublisher;
    private final AsyncTaskExecutor asyncExecutor;

    @Override
    public <T> Subscription subscribe(Class<T> eventType, Consumer<T> handler) {
        return addSubscription(ResolvableType.forClass(eventType), handler);
    }

    @Override
    public <T> Subscription subscribe(EventHandler<T> eventHandler) {
        return addSubscription(eventHandler.getEventType(), eventHandler::handle);
    }

    private <T> Subscription addSubscription(ResolvableType eventType, Consumer<T> handler) {
        Class<?> rawType = eventType.resolve();
        SubscriptionEntry<T> entry = new SubscriptionEntry<>(eventType, handler, this);
        subscribers.computeIfAbsent(rawType, k -> new CopyOnWriteArrayList<>()).add(entry);
        return entry;
    }

    @Override
    public void unsubscribe(Subscription subscription) {
        Class<?> rawType = subscription.getEventType().resolve();
        List<SubscriptionEntry<?>> list = subscribers.get(rawType);
        if (list != null) {
            list.removeIf(e -> e == subscription);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void publish(Object event) {
        ResolvableType publishedType = ResolvableTypeUtil.resolve(event);
        Class<?> rawType = publishedType.resolve();

        List<SubscriptionEntry<?>> list = subscribers.get(rawType);
        if (list != null) {
            for (SubscriptionEntry entry : list) {
                if (!entry.isActive()) continue;
                if (!entry.matches(publishedType)) continue;
                try {
                    ((Consumer) entry.getHandler()).accept(event);
                } catch (Exception e) {
                    log.error("EventBus handler error: event={}", rawType.getSimpleName(), e);
                }
            }
        }
        // 同时发布给 Spring @EventListener（接收 ApplicationEvent）
        if (springPublisher != null) {
            springPublisher.publishEvent(event);
        }
    }

    @Override
    public void publishAsync(Object event) {
        asyncExecutor.execute(() -> publish(event));
    }
}
```

### 3.2 SubscriptionEntry — 注册条目实现

```java
class SubscriptionEntry<T> implements Subscription {

    private final ResolvableType eventType;  // 完整泛型类型
    private final Consumer<T> handler;
    private final SpringEventBus bus;
    private volatile boolean active = true;

    @Override
    public ResolvableType getEventType() { return eventType; }

    @Override
    public boolean isActive() { return active; }

    @Override
    public void cancel() {
        this.active = false;
        this.bus.unsubscribe(this);
    }

    Consumer<T> getHandler() { return handler; }

    /**
     * 检查发布事件的 ResolvableType 是否匹配此订阅的 ResolvableType。
     * 使用 Spring ResolvableType.isAssignableFrom 做完整泛型比较。
     */
    boolean matches(ResolvableType publishedType) {
        // 订阅若没有泛型参数（如 subscribe(SaveAfterEvent.class, h)），全部匹配
        if (!eventType.hasGenerics()) {
            return true;
        }
        return eventType.isAssignableFrom(publishedType);
    }
}
```

## 四、使用示例

```java
@Component
public class DeviceSubscriberManager {

    @Resource
    private IEventBus eventBus;

    private Subscription deviceCreatedSub;
    private Subscription deviceDeletedSub;
    private Subscription allUpdatesSub;

    // ===== 定义 EventHandler — 类型 + 处理逻辑一体 =====
    private final EventHandler<SaveAfterEvent<DeviceEntity>> deviceCreatedHandler =
        new EventHandler<SaveAfterEvent<DeviceEntity>>() {
            @Override
            public void handle(SaveAfterEvent<DeviceEntity> event) {
                DeviceEntity device = event.getAfterData();
                log.info("设备新增: id={}, name={}", event.getEntityId(), device.getName());
            }
        };

    private final EventHandler<DeleteAfterEvent<DeviceEntity>> deviceDeletedHandler =
        new EventHandler<DeleteAfterEvent<DeviceEntity>>() {
            @Override
            public void handle(DeleteAfterEvent<DeviceEntity> event) {
                log.info("设备删除: id={}", event.getEntityId());
            }
        };

    public void activate() {
        // 直接订阅 EventHandler，类型信息内置，不需要额外传 Class
        deviceCreatedSub = eventBus.subscribe(deviceCreatedHandler);
        deviceDeletedSub = eventBus.subscribe(deviceDeletedHandler);

        // 不限泛型也可以用 Class + Consumer
        allUpdatesSub = eventBus.subscribe(WriteEvent.class, event ->
            log.info("任意写入: entity={}, id={}", event.getEntitySimpleName(), event.getEntityId())
        );
    }

    public void deactivate() {
        deviceCreatedSub.cancel();
        deviceDeletedSub.cancel();
        allUpdatesSub.cancel();
    }
}
```

## 五、与现有体系的关系

```
发布端:
  CrudEventInterceptor → eventPublisher.publishEvent(event)
     ↓ event 是 CrudEvent<T>，已实现 ResolvableTypeProvider
  Spring 事件分发:
    ├── @EventListener (静态)
    └── IEventBus (动态)
          ├── 通过 CrudEventInterceptor 发布的也能被动态 handler 捕获
          └── 反之 eventBus.publish() 也会触发 @EventListener

直接发布:
  eventBus.publish(myEvent)       ← 任意 Object，无需特殊接口
  eventBus.publishAsync(myEvent)  ← 异步线程池执行
```

> CrudEvent 已实现 `ResolvableTypeProvider`，EventBus 通过 `ResolvableTypeUtil.resolve()` 自动提取泛型。其他事件想参与泛型匹配，实现 `ResolvableTypeProvider` 即可。

## 六、文件清单

```
sa-base/src/main/java/net/lab1024/sa/base/module/support/eventbus/

  core/
    ├── IEventBus.java              (动态事件总线接口)
    ├── Subscription.java           (注册凭证接口)
    ├── EventHandler.java           (事件处理器，自带泛型类型)
    └── ResolvableTypeUtil.java     (从事件中提取 ResolvableType)

  impl/
    ├── SpringEventBus.java         (Spring 实现)
    └── SubscriptionEntry.java     (注册条目实现)

  config/
    └── EventBusAutoConfiguration.java  (自动装配)
```

## 七、验证方案

1. `subscribe` 注册 handler → `publish` 触发 handler 执行
2. `unsubscribe` 注销 → `publish` 不再触发
3. `cancel()` 后 handler 不再执行
4. `publishAsync` 在不同线程执行 handler
5. 多个 handler 注册同一事件类型 → 按注册顺序依次执行
6. handler 抛异常 → 不影响其他 handler，不影响 Spring @EventListener
7. CRUD 事件（拦截器发布）→ @EventListener 和动态 handler 都被触发
