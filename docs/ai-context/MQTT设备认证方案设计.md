# MQTT 设备认证方案设计

## Context

当前 `MqttAuthenticator` 是空实现（return null），`DefaultDeviceOperator.authenticate()` 是桩（始终返回 success）。需要实现完整的 MQTT 设备认证链路，支持一机一密和一型一密两种模式。

## 1. MQTT 认证协议

### 1.1 CONNECT 报文约定

| 字段 | 格式                                               | 说明 |
|------|--------------------------------------------------|------|
| clientId | `{productKey}:{deviceKey}:{timestamp}:{mode}:{signType}` | `:` 分隔 |
| username | `{productKey}-{deviceKey}`                       | `-` 分隔 |
| password | HMAC-SHA256 签名（hex 字符串）                          | 见 1.2 |

### 1.2 签名算法

**一机一密（mode=1）：**
```
HMAC-SHA256(productKey:deviceKey:timestamp, deviceSecret)
```

**一型一密（mode=2）：**
```
HMAC-SHA256(productKey:deviceKey:timestamp, productSecret)
```

### 1.3 防重放

校验 `|now - timestamp| ≤ 5分钟`，超出则拒绝。

---

## 2. 类设计

### 2.1 DeviceAuthenticationRequest（新建）

`device/DeviceAuthenticationRequest.java` — 实现 `AuthenticationRequest`，提供 copy 和签名比对：

```java
@Data @Builder(toBuilder = true)
public class DeviceAuthenticationRequest implements AuthenticationRequest {
    private String transport;      // "MQTT"
    private String productKey;     // 产品 Key
    private String deviceKey;      // 设备 Key
    private long timestamp;        // 设备当前时间戳
    private int mode;              // 1=一机一密, 2=一型一密
    private String signType;       // 签名算法标识，如 "sha256"
    private String signature;      // 签名值

    @Override
    public Transport getTransport() {
        return DefaultTransport.MQTT;
    }

    /** 用服务端密钥构建新的请求（提前计算签名），用于签名比对 */
    public DeviceAuthenticationRequest copy(String deviceSecret, String productSecret) {
        String secret = (mode == 1) ? deviceSecret : productSecret;
        String data = productKey + ":" + deviceKey + ":" + timestamp;
        return this.toBuilder()
                .signature(SignatureManager.sign(signType, data, secret))
                .build();
    }

    /** 比较签名值是否一致 */
    public boolean isSignatureMatch(DeviceAuthenticationRequest other) {
        return this.signature != null && this.signature.equals(other.signature);
    }
}

```

### 2.2 MqttAuthenticationRequest 解析逻辑

在现有 `MqttAuthenticationRequest` 中增加 `toDeviceRequest()` 方法，带格式校验和空值检查：

```java
/**
 * 解析 MQTT CONNECT 报文为 DeviceAuthenticationRequest
 * clientId = "productKey:deviceKey:timestamp:mode:signType"
 * username = "productKey-deviceKey"
 * password = signature(hex)
 */
public DeviceAuthenticationRequest toDeviceRequest() {
    String clientId = getClientId();
    String username = getUsername();
    String password = getPassword();

    if (clientId == null || username == null || password == null) {
        throw new IllegalArgumentException("clientId/username/password 不能为空");
    }

    String[] parts = clientId.split(":");
    if (parts.length != 5) {
        throw new IllegalArgumentException("clientId 格式错误，期望 productKey:deviceKey:timestamp:mode:signType");
    }

    String[] userParts = username.split("-");
    if (userParts.length != 2) {
        throw new IllegalArgumentException("username 格式错误，期望 productKey-deviceKey");
    }

    if (!parts[0].equals(userParts[0]) || !parts[1].equals(userParts[1])) {
        throw new IllegalArgumentException("clientId 与 username 中 productKey/deviceKey 不一致");
    }

    return DeviceAuthenticationRequest.builder()
            .transport(DefaultTransport.MQTT.getId())
            .productKey(parts[0])
            .deviceKey(parts[1])
            .timestamp(Long.parseLong(parts[2]))
            .mode(Integer.parseInt(parts[3]))
            .signType(parts[4])
            .signature(password)
            .build();
}
```

### 2.3 SignatureAlgorithm 接口（新建）

`signature/SignatureAlgorithm.java` — 签名算法抽象：

```java
public interface SignatureAlgorithm {

    /** 算法标识，对应 clientId 中 signType 字段，如 "sha256"、"sm3" */
    String getType();

    /** 使用密钥对数据进行签名 */
    String sign(String data, String secret);
}
```

### 2.4 SignatureManager 管理类（新建）

`signature/SignatureManager.java` — static 注册所有算法，提供统一签名入口：

```java
public final class SignatureManager {

    private static final Map<String, SignatureAlgorithm> registry = new ConcurrentHashMap<>();

    static {
        register(new HmacSha256SignatureAlgorithm());
        // 后续扩展：register(new Sm3SignatureAlgorithm());
    }

    public static void register(SignatureAlgorithm algorithm) {
        registry.put(algorithm.getType(), algorithm);
    }

    public static String sign(String type, String data, String secret) {
        SignatureAlgorithm algorithm = registry.get(type);
        if (algorithm == null) {
            throw new IllegalArgumentException("不支持的签名算法: " + type);
        }
        return algorithm.sign(data, secret);
    }
}
```

### 2.5 HmacSha256SignatureAlgorithm 实现（新建）

`signature/HmacSha256SignatureAlgorithm.java`：

```java
public class HmacSha256SignatureAlgorithm implements SignatureAlgorithm {

    @Override
    public String getType() {
        return "sha256";
    }

    @Override
    public String sign(String data, String secret) {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return Hex.encodeHexString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
    }
}
```

### 2.5 DeviceOperator.authenticate 签名变更

`DeviceOperator.java` — 参数改为 `DeviceAuthenticationRequest`：

```java
Mono<AuthenticationResponse> authenticate(DeviceAuthenticationRequest request);
```

### 2.6 MqttAuthenticator 实现

`MqttAuthenticator` 直接解析 MQTT 请求并委托给 `DeviceOperator`，不需要注入算法 Map：

```java
public class MqttAuthenticator implements Authenticator {

    @Override
    public Mono<AuthenticationResponse> authenticate(@Nonnull AuthenticationRequest request,
                                                     @Nonnull DeviceOperator device) {
        return device.authenticate(((MqttAuthenticationRequest) request).toDeviceRequest());
    }

    @Override
    public Mono<AuthenticationResponse> authenticate(@Nonnull AuthenticationRequest request,
                                                     @Nonnull DeviceRegistry registry) {
        MqttAuthenticationRequest mqttReq = (MqttAuthenticationRequest) request;
        DeviceAuthenticationRequest deviceReq = mqttReq.toDeviceRequest();
        return registry.getDevice(deviceReq.getProductKey(), deviceReq.getDeviceKey())
                .flatMap(device -> authenticate(request, device));
    }
}
```

### 2.7 DefaultDeviceOperator.authenticate 实现

`DefaultDeviceOperator` 读取密钥 → copy 构建服务端请求 → 比对签名：

```java
@Override
public Mono<AuthenticationResponse> authenticate(DeviceAuthenticationRequest request) {
    return Mono.fromCallable(() -> {
        // 1. 防重放：|now - timestamp| ≤ 5分钟
        long now = System.currentTimeMillis();
        if (Math.abs(now - request.getTimestamp()) > 300_000) {
            return AuthenticationResponse.error(401, "时间戳超时");
        }

        // 2. 读取设备/产品密钥
        String deviceSecret = storage.getConfig(DeviceField.DEVICE_SECRET.getValue()).asString();
        String productSecret = getProduct()
                .flatMap(p -> p.getSelfConfig("productSecret"))
                .map(Value::asString)
                .block();

        // 3. 用服务端密钥构建期望签名 → 比对
        DeviceAuthenticationRequest expected = request.copy(deviceSecret, productSecret);
        if (!request.isSignatureMatch(expected)) {
            return AuthenticationResponse.error(403, "签名验证失败");
        }

        return AuthenticationResponse.success(deviceId);
    });
}
```

### 2.5 MqttAuthenticator 实现

`sa-protocol/.../MqttAuthenticator.java` — 解析 MQTT 认证请求后委托给 `DeviceOperator`：

```java
public class MqttAuthenticator implements Authenticator {

    @Override
    public Mono<AuthenticationResponse> authenticate(@Nonnull AuthenticationRequest request,
                                                     @Nonnull DeviceOperator device) {
        MqttAuthenticationRequest mqttReq = (MqttAuthenticationRequest) request;
        DeviceAuthenticationRequest deviceReq = mqttReq.toDeviceRequest();
        return device.authenticate(deviceReq);
    }

    @Override
    public Mono<AuthenticationResponse> authenticate(@Nonnull AuthenticationRequest request,
                                                     @Nonnull DeviceRegistry registry) {
        MqttAuthenticationRequest mqttReq = (MqttAuthenticationRequest) request;
        DeviceAuthenticationRequest deviceReq = mqttReq.toDeviceRequest();
        return registry.getDevice(deviceReq.getProductKey(), deviceReq.getDeviceKey())
                .flatMap(device -> authenticate(request, device));
    }
}
```

---

## 3. 调用链路

```
MQTT CONNECT 报文
  → VertxMqttConnection.getAuthenticationRequest(transport)
     构建 MqttAuthenticationRequest(clientId, username, password, transport)
  → MqttServerDeviceGateway.handleConnection
     protocolSupport.authenticate(authReq, registry)
  → Authenticator.authenticate(request, registry) [默认实现]
     registry.getDevice(productKey, deviceKey) → DeviceOperator
  → Authenticator.authenticate(request, deviceOperator)
  → MqttAuthenticator.authenticate(request, deviceOperator)
     mqttReq.toDeviceRequest() → DeviceAuthenticationRequest
  → DeviceOperator.authenticate(deviceRequest)
     DefaultDeviceOperator: 防重放 → 取密钥 → 签名验证 → success/error
```

---

## 4. 涉及文件

| 文件 | 操作 |
|------|------|
| `signature/SignatureAlgorithm.java` | **新建** — 签名算法接口（getType/sign） |
| `signature/SignatureManager.java` | **新建** — static 注册所有算法 + 统一签名入口 |
| `signature/HmacSha256SignatureAlgorithm.java` | **新建** — HMAC-SHA256 实现 |
| `device/DeviceAuthenticationRequest.java` | **新建** — 实现 AuthenticationRequest，copy(isSignatureMatch) |
| `mqtt/session/MqttAuthenticationRequest.java` | 增加 toDeviceRequest() — 带格式校验和空值检查 |
| `device/DeviceOperator.java` | authenticate 参数改为 DeviceAuthenticationRequest |
| `device/support/DefaultDeviceOperator.java` | 重写 authenticate：防重放 + resolveSecret + toCompareRequest |
| `sa-protocol/.../MqttAuthenticator.java` | 重写：toDeviceRequest → delegate to deviceOperator |

## 5. 验证方案

1. 构造测试 MQTT CONNECT 报文（mode=1/2），验证签名计算和比对
2. 防重放测试：使用过期时间戳，验证 401 返回
3. 错误签名测试：验证 403 返回
