# 设备注册中心设计文档

---

## 一、核心接口

### DeviceRegistry

> 实现类: **DefaultDeviceRegistry**

```java
public interface DeviceRegistry {
    Mono<DeviceOperator>        getDevice(String deviceId);
    Mono<DeviceOperator>        getDevice(String productKey, String deviceKey);
    Mono<DeviceProductOperator> getProduct(String productId);
    Mono<DeviceOperator>        register(DeviceInfo deviceInfo);
    Mono<DeviceProductOperator> register(ProductInfo productInfo);
    Mono<Void>                  unregisterDevice(String deviceId);
    Mono<Void>                  unregisterProduct(String productId);
}
```

### DeviceOperator

> 实现类: **DefaultDeviceOperator**

```java
public interface DeviceOperator {
    String                       getDeviceId();
    Mono<String>                 getConnectionServerId();
    Mono<String>                 getSessionId();
    Mono<Long>                   getOnlineTime();
    Mono<Long>                   getOfflineTime();
    Mono<Value>                  getSelfConfig(String key);
    Flux<Value>                  getSelfConfigs(Collection<String> keys);
    Mono<Boolean>                disconnect();
    Mono<AuthenticationResponse> authenticate(AuthenticationRequest request);
    /** 设备物模型 — 直接委托 product.getMetadata() */
    Mono<ThingsMetadata>         getMetadata();
    /** 所属产品 */
    Mono<DeviceProductOperator>  getProduct();
}
```

### DeviceProductOperator

> 实现类: **DefaultDeviceProductOperator**

```java
public interface DeviceProductOperator {
    String               getId();
    Mono<ThingsMetadata> getMetadata();
    Mono<Boolean>        updateMetadata(String metadata);
    Flux<DeviceOperator> getDevices();
}
```

---

## 二、Redis 存储结构

### 2.1 产品信息 — Hash

```
Key:    device-product:{productId}
Type:   Hash

字段（枚举定义）:
  productName      string    产品名称
  metadata         string    物模型 JSON
  productKey       string    产品密钥 (认证用)
  productSecret    string    产品密钥 (认证用)
  isGatewayDevice  boolean   是否网关设备
```

> 字段枚举: `net.lab1024.sa.base.device.support.ProductField`

```java
public enum ProductField {
    PRODUCT_NAME("productName"),
    METADATA("metadata"),
    PRODUCT_KEY("productKey"),
    PRODUCT_SECRET("productSecret"),
    IS_GATEWAY_DEVICE("isGatewayDevice");

    private final String value;
    ProductField(String value) { this.value = value; }
    public String getValue() { return value; }
}
```

### 2.2 设备信息 — Hash

```
Key:    device:{deviceId}
Type:   Hash

字段（枚举定义）:
  deviceName         string   设备名称
  deviceKey          string   设备密钥 (认证用)
  deviceSecret       string   设备密钥 (认证用)
  productId          string   所属产品ID
  productName        string   产品名称 (冗余, 产品变更时同步)
  productKey         string   产品密钥 (冗余, 产品变更时同步)
  productSecret      string   产品密钥 (冗余, 产品变更时同步)
  offlineTime        long     下线时间 (预留)
  onlineTime         long     上线时间 (预留)
  sessionId          string   会话ID (预留, 上线时写入)
  protocolId         string   协议ID (预留, 上线时写入)
  gatewayId          string   网关ID (预留, 上线时写入)
  connectionServerId string   集群节点ID (预留)
```

> 字段枚举: `net.lab1024.sa.base.device.support.DeviceField`

```java
public enum DeviceField {
    DEVICE_NAME("deviceName"),
    DEVICE_KEY("deviceKey"),
    DEVICE_SECRET("deviceSecret"),
    PRODUCT_ID("productId"),
    PRODUCT_NAME("productName"),
    PRODUCT_KEY("productKey"),
    PRODUCT_SECRET("productSecret"),
    OFFLINE_TIME("offlineTime"),
    ONLINE_TIME("onlineTime"),
    SESSION_ID("sessionId"),
    PROTOCOL_ID("protocolId"),
    GATEWAY_ID("gatewayId"),
    CONNECTION_SERVER_ID("connectionServerId");

    private final String value;
    DeviceField(String value) { this.value = value; }
    public String getValue() { return value; }
}
```

### 2.3 产品→设备绑定 — Set

```
Key:    device-product-bind:{productId}
Type:   Set

写入:  设备注册时 SADD
删除:  设备注销时 SREM

示例:
  device-product-bind:1932737300376702976
    1)  "deviceId1"
    2)  "deviceId2"
```

### 2.4 设备映射 — String

```
Key:    mapping:deviceId:{productKey-deviceKey}
Type:   String
Value:  deviceId

写入:  设备注册时 SET
删除:  设备注销时 DEL

用于 deviceKey 反查 deviceId, 支持 getDevice(productKey, deviceKey)
```

---

## 三、组件设计

### 3.1 ProductConfigsHandler — 产品 CRUD 事件处理

```
监听: EntityCreatedEvent / EntitySavedEvent / EntityModifyEvent / EntityDeletedEvent
       of DeviceProductEntity

流程:
  保存/修改事件:
    if (product.state == enabled):
      registry.register(productInfo)                        // 写入 Redis Hash
      eventBus.publish(DeviceConfigsProductChangeEvent)     // 通知设备同步
    else:
      registry.unregisterProduct(productId)                 // 删除 Redis Hash

  删除事件:
    registry.unregisterProduct(productId)                   // 删除 Redis Hash
```

### 3.2 DeviceConfigsHandler — 设备 CRUD 事件处理

```
监听: EntityCreatedEvent / EntitySavedEvent / EntityModifyEvent / EntityDeletedEvent
       of DeviceInstanceEntity

  保存/修改事件:
    if (device.state == enabled):
      registry.register(deviceInfo)                         // 写入 Redis Hash + Set + String
    else:
      registry.unregisterDevice(deviceId)                   // 删除 Redis Hash + Set + String

  删除事件:
    registry.unregisterDevice(deviceId)                     // 删除 Redis Hash + Set + String
```

### 3.3 ProductChangeSyncHandler — 产品变更同步

```
监听: DeviceConfigsProductChangeEvent

  产品信息变更时, 异步更新该产品下所有设备的冗余字段:

  product.getDevices()                                         // 从 Set 获取设备列表
    .flatMap(device -> device.setConfigs({
      productName:  productInfo.productName,                   // 同步产品名称
      productKey:   productInfo.productKey,                    // 同步产品密钥
      productSecret: productInfo.productSecret                 // 同步产品密钥
    }))
```

---

## 四、注册 / 注销流程

### 4.1 产品注册

```
ProductConfigsHandler
  ↓
registry.register(ProductInfo)
  ↓
IConfigStorageManager.getStorage("device-product:{productId}")
  ↓
.setConfigs({
    productName:     info.getProductName(),
    metadata:        info.getMetadata(),
    productKey:      info.getProductKey(),
    productSecret:   info.getProductSecret(),
    isGatewayDevice: info.getIsGatewayDevice()
})
  ↓
写入 Redis Hash: device-product:{productId}
```

### 4.2 设备注册

```
DeviceConfigsHandler
  ↓
registry.register(DeviceInfo)
  ↓
IConfigStorageManager.getStorage("device:{deviceId}")
  ↓
.setConfigs({
    deviceName:         info.getDeviceName(),
    deviceKey:          info.getDeviceKey(),
    deviceSecret:       info.getDeviceSecret(),
    productId:          info.getProductId(),
    productName:        info.getProductName(),
    productKey:         info.getProductKey(),
    productSecret:      info.getProductSecret(),
    offlineTime:        info.getOfflineTime(),
    onlineTime:         info.getOnlineTime(),
    sessionId:          info.getSessionId(),
    protocolId:         info.getProtocolId(),
    gatewayId:          info.getGatewayId(),
    connectionServerId: info.getConnectionServerId()
})
  ↓
写入 Redis Hash: device:{deviceId}

同时写入绑定关系:
  SADD device-product-bind:{productId}  {deviceId}
  SET  mapping:deviceId:{productKey-deviceKey}  {deviceId}
```

### 4.3 设备注销

```
DeviceConfigsHandler
  ↓
registry.unregisterDevice(deviceId)
  ↓
IConfigStorageManager.getStorage("device:{deviceId}")
  ↓
.clear()    // 删除 Redis Hash: device:{deviceId}

同时清理绑定关系:
  SREM device-product-bind:{productId}  {deviceId}
  DEL  mapping:deviceId:{productKey-deviceKey}
```

### 4.4 产品注销

```
ProductConfigsHandler
  ↓
registry.unregisterProduct(productId)
  ↓
IConfigStorageManager.getStorage("device-product:{productId}")
  ↓
.clear()    // 删除 Redis Hash: device-product:{productId}
```

---

## 五、查询流程

### 5.1 通过 deviceId 查设备

```
registry.getDevice(deviceId)
  ↓
getStorage("device:{deviceId}") → 读 Redis Hash → DeviceOperator
```

### 5.2 通过 productKey + deviceKey 查设备

```
registry.getDevice(productKey, deviceKey)
  ↓
GET mapping:deviceId:{productKey-deviceKey}  → 拿到 deviceId
  ↓
getDevice(deviceId)  → DeviceOperator
```

### 5.3 通过 productId 查产品

```
registry.getProduct(productId)
  ↓
getStorage("device-product:{productId}") → 读 Redis Hash → DeviceProductOperator
```

### 5.4 查产品下所有设备

```
product.getDevices()
  ↓
SMEMBERS device-product-bind:{productId}  → 拿到 [deviceId1, deviceId2, ...]
  ↓
逐个 getDevice(deviceId) → Flux<DeviceOperator>
```

---

## 六、事件流总览

```
产品 CRUD
  ↓
ProductConfigsHandler
  ├── register(productInfo) → Redis Hash: device-product:{productId}
  └── DeviceConfigsProductChangeEvent
        ↓
      DeviceConfigsHandler
        └── 异步: 产品下所有设备 setConfigs({productName, productKey, productSecret})

设备 CRUD
  ↓
DeviceConfigsHandler
  ├── register(deviceInfo) → Redis Hash: device:{deviceId}
  │                         + SADD device-product-bind:{productId} {deviceId}
  │                         + SET mapping:deviceId:{productKey-deviceKey} {deviceId}
  └── unregisterDevice(id) → DEL device:{deviceId}
                              + SREM device-product-bind:{productId} {deviceId}
                              + DEL mapping:deviceId:{productKey-deviceKey}
```

---

## 七、新建类清单

### 7.1 sa-base 模块 — 接口 & DTO

| 类 | 路径 | 说明 |
|---|------|------|
| `DeviceRegistry` | `net.lab1024.sa.base.device.DeviceRegistry` | 设备注册中心接口 |
| `DeviceOperator` | `net.lab1024.sa.base.device.DeviceOperator` | 设备操作器接口 |
| `DeviceProductOperator` | `net.lab1024.sa.base.device.DeviceProductOperator` | 产品操作器接口 |
| `DeviceInfo` | `net.lab1024.sa.base.device.DeviceInfo` | 设备注册信息 DTO |
| `ProductInfo` | `net.lab1024.sa.base.device.ProductInfo` | 产品注册信息 DTO |
| `AuthenticationRequest` | `net.lab1024.sa.base.device.AuthenticationRequest` | 设备认证请求 |
| `AuthenticationResponse` | `net.lab1024.sa.base.device.AuthenticationResponse` | 设备认证响应 |

### 7.2 sa-base 模块 — 默认实现 & 枚举

| 类 | 路径 | 说明 |
|---|------|------|
| `DefaultDeviceRegistry` | `net.lab1024.sa.base.device.support.DefaultDeviceRegistry` | DeviceRegistry 实现 |
| `DefaultDeviceOperator` | `net.lab1024.sa.base.device.support.DefaultDeviceOperator` | DeviceOperator 实现 |
| `DefaultDeviceProductOperator` | `net.lab1024.sa.base.device.support.DefaultDeviceProductOperator` | DeviceProductOperator 实现 |
| `DeviceField` | `net.lab1024.sa.base.device.support.DeviceField` | Redis Hash 设备字段枚举 |
| `ProductField` | `net.lab1024.sa.base.device.support.ProductField` | Redis Hash 产品字段枚举 |

### 7.3 sa-admin 模块 — 事件处理器

| 类 | 路径 | 说明 |
|---|------|------|
| `ProductConfigsHandler` | `net.lab1024.sa.admin.module.business.device.registry.handler.ProductConfigsHandler` | 监听产品 CRUD 事件，同步 Redis |
| `DeviceConfigsHandler` | `net.lab1024.sa.admin.module.business.device.registry.handler.DeviceConfigsHandler` | 监听设备 CRUD 事件，同步 Redis |
| `ProductChangeSyncHandler` | `net.lab1024.sa.admin.module.business.device.registry.handler.ProductChangeSyncHandler` | 监听产品变更事件，同步设备冗余字段 |

### 7.4 依赖的已有模块

| 模块 | 路径 | 用途 |
|------|------|------|
| `IConfigStorageManager` | `net.lab1024.sa.base.module.support.cache.core.IConfigStorageManager` | Hash 配置存储，承载 Redis Hash 读写 |
| `IConfigStorage` | `net.lab1024.sa.base.module.support.cache.core.IConfigStorage` | 单 key 配置存储操作 |
| `IEventBus` | `net.lab1024.sa.base.module.support.eventbus.core.IEventBus` | 事件总线，发布产品变更事件 |
| `IStringCache` | `net.lab1024.sa.base.module.support.cache.core.IStringCache` | String 缓存，承载映射表 SET/GET |
| `ISetCache` | `net.lab1024.sa.base.module.support.cache.core.ISetCache` | Set 缓存，承载产品-设备绑定关系 |
