# 物模型 TSL 后端解析组件设计文档

## 一、包结构

```
sa-base/src/main/java/net/lab1024/sa/base/

  ═══════════════════════════════════════════
  metadata/                        元数据接口
  ═══════════════════════════════════════════
  ├── Metadata.java
  ├── PropertyMetadata.java
  ├── FunctionMetadata.java
  ├── FunctionParamMetadata.java
  ├── EventMetadata.java
  ├── ThingsMetadata.java
  ├── ThingsMetadataCodec.java
  │
  └── type/                        数据类型接口 + POJO
      ├── DataType.java
      ├── DataTypeCodec.java
      ├── NumberDataType.java       (A)
      ├── IntDataType.java
      ├── FloatDataType.java
      ├── DoubleDataType.java
      ├── LongDataType.java
      ├── StringDataType.java
      ├── BooleanDataType.java
      ├── EnumDataType.java
      ├── EnumElement.java
      ├── DateDataType.java
      ├── ArrayDataType.java
      ├── ObjectDataType.java
      └── ObjectProperty.java

  ═══════════════════════════════════════════
  module/support/json/              JSON 工具层
  ═══════════════════════════════════════════
  ├── JsonNode.java                 自定义接口
  ├── IotJsonNode.java              包私有实现（内部持有 Jackson JsonNode，外部不可见）
  └── JsonUtil.java                 静态工具：parse() / toJson() / createNode()

  ═══════════════════════════════════════════
  module/support/thingsmodel/       Iot 实现
  ═══════════════════════════════════════════
  ├── IotThingsMetadata.java
  ├── IotPropertyMetadata.java
  ├── IotFunctionMetadata.java
  ├── IotFunctionParamMetadata.java
  ├── IotEventMetadata.java
  ├── IotThingsMetadataCodec.java   单例，全手动解析
  ├── DataTypeCodecRegistry.java    typeName → DataTypeCodec
  │
  └── codec/                        DataTypeCodec 实现
      ├── NumberDataTypeCodec.java  (A)
      ├── IntDataTypeCodec.java
      ├── FloatDataTypeCodec.java
      ├── DoubleDataTypeCodec.java
      ├── LongDataTypeCodec.java
      ├── StringDataTypeCodec.java
      ├── BooleanDataTypeCodec.java
      ├── EnumDataTypeCodec.java
      ├── DateDataTypeCodec.java
      ├── ArrayDataTypeCodec.java
      └── ObjectDataTypeCodec.java
```

---

## 二、物模型体系

### 2.1 逻辑层级

```
ThingsMetadata（物模型 — 聚合根）
  │
  ├── PropertyMetadata（属性）               1:N
  │     └── valueType: DataType              必填
  │
  ├── FunctionMetadata（功能）               1:N
  │     ├── inputs: FunctionParamMetadata[]   可选
  │     │     └── valueType: DataType         必填
  │     └── output: DataType                  可选
  │
  └── EventMetadata（事件）                  1:N
        └── valueType: DataType               必填
```

**说明：**
- `ThingsMetadata` 是聚合根，持有三组实体列表
- `PropertyMetadata` / `FunctionMetadata` / `EventMetadata` 是聚合内的实体，各自独立，之间无直接引用
- `FunctionParamMetadata` 由 `FunctionMetadata` 持有，不独立存在
- 所有 `DataType` 均为值对象，内嵌于实体中

### 2.2 接口层级

```
Metadata（标记接口）
  │
  ├── PropertyMetadata
  ├── FunctionMetadata
  ├── FunctionParamMetadata
  ├── EventMetadata
  └── ThingsMetadata（聚合根）
```

### 2.2 PropertyMetadata

```java
public interface PropertyMetadata extends Metadata {
    String getId();
    String getName();
    DataType getValueType();
    String getAccessMode();    // r / rw / w
    String getDescription();
}
```

### 2.3 FunctionMetadata

```java
public interface FunctionMetadata extends Metadata {
    String getId();
    String getName();
    boolean isAsync();
    List<? extends FunctionParamMetadata> getInputs();
    DataType getOutput();
    String getDescription();
}
```

### 2.4 FunctionParamMetadata

```java
public interface FunctionParamMetadata extends Metadata {
    String getId();
    String getName();
    DataType getValueType();
    boolean isRequired();
}
```

### 2.5 EventMetadata

```java
public interface EventMetadata extends Metadata {
    String getId();
    String getName();
    String getEventType();     // info / warning / error
    DataType getValueType();
    String getDescription();
}
```

### 2.6 ThingsMetadata

```java
public interface ThingsMetadata extends Metadata {
    List<? extends PropertyMetadata> getProperties();
    List<? extends FunctionMetadata> getFunctions();
    List<? extends EventMetadata> getEvents();
}
```

### 2.7 Iot 实现

| 接口 | 实现类 |
|------|--------|
| `PropertyMetadata` | `IotPropertyMetadata` |
| `FunctionMetadata` | `IotFunctionMetadata` |
| `FunctionParamMetadata` | `IotFunctionParamMetadata` |
| `EventMetadata` | `IotEventMetadata` |
| `ThingsMetadata` | `IotThingsMetadata` |

均为纯 POJO（`@Data`），不含任何 Jackson 注解。

---

## 三、数据类型体系

### 3.1 DataType 接口

```java
public interface DataType {
    String getType();  // 类型标识，如 "int"、"object"
}
```

### 3.2 类型层级（7 种）

```
DataType
  │
  ├── NumberDataType (A)        type, min, max, unit
  │     ├── IntDataType
  │     ├── FloatDataType
  │     ├── DoubleDataType
  │     └── LongDataType
  │
  ├── StringDataType            type, maxLength
  ├── BooleanDataType           type, trueText, falseText, trueValue, falseValue
  ├── EnumDataType              type, elements: List<EnumElement>
  │     └── EnumElement         value, text, description
  ├── DateDataType              type, format, tz
  ├── ArrayDataType             type, elementType: DataType  （递归）
  └── ObjectDataType            type, properties: List<ObjectProperty>
        └── ObjectProperty      id, name, valueType: DataType
```

### 3.3 极值定义

| 类型 | min | max |
|------|-----|-----|
| int | -2147483648 | 2147483647 |
| long | Long.MIN_VALUE | Long.MAX_VALUE |
| float | -3.4028235e38 | 3.4028235e38 |
| double | -Double.MAX_VALUE | Double.MAX_VALUE |

---

## 四、物模型编解码

### 4.1 ThingsMetadataCodec 接口

```java
public interface ThingsMetadataCodec {

    /** JSON → ThingsMetadata */
    ThingsMetadata decode(String json);

    /** ThingsMetadata → JSON */
    String encode(ThingsMetadata metadata);

    /** 全量校验 */
    List<String> validate(ThingsMetadata metadata);

    /** 解析 + 校验 */
    ParseResult parseAndValidate(String json);
}
```

### 4.2 ParseResult

```java
public class ParseResult {
    private ThingsMetadata metadata;   // 解析结果（失败时为 null）
    private List<String> errors;       // 校验错误（空 = 合法）

    public boolean isValid() {
        return metadata != null && (errors == null || errors.isEmpty());
    }
}
```

### 4.3 IotThingsMetadataCodec

- **模式**：单例（DCL），`IotThingsMetadataCodec.getInstance()`
- **解析方式**：全手动。逐字段从 `JsonNode` 读取 → 设值到 `Iot*Metadata`
- **依赖**：`JsonUtil`（JSON 操作）+ `DataTypeCodecRegistry`（值类型解析）
- **不依赖**：ObjectMapper 注入、Jackson 注解、自定义反序列化器

```java
public class IotThingsMetadataCodec implements ThingsMetadataCodec {

    private static volatile IotThingsMetadataCodec INSTANCE;
    private final DataTypeCodecRegistry registry;

    private IotThingsMetadataCodec() {
        this.registry = new DataTypeCodecRegistry();
    }

    public static IotThingsMetadataCodec getInstance() { ... }

    // decode：JsonUtil.parse(json) → 遍历 JsonNode 树 → 手动设值
    // encode：手动构建 JsonNode 树 → JsonUtil.toJson(root)
    // validate：遍历 properties/functions/events，校验必填 + 唯一性 + 调 codec.validate()
}
```

### 4.4 调用方式

```java
// ProductService.saveModelJson()
ParseResult r = IotThingsMetadataCodec.getInstance().parseAndValidate(modelJson);
if (!r.isValid()) return ResponseDTO.userErrorParam(r.getErrors());
String canonical = IotThingsMetadataCodec.getInstance().encode(r.getMetadata());
// 入库 canonical
```

---

## 五、数据类型编解码体系

### 5.1 DataTypeCodec 接口

```java
public interface DataTypeCodec<T extends DataType> {

    /** 类型标识，如 "int" */
    String getTypeName();

    /** JsonNode → DataType */
    T decode(JsonNode node);

    /** DataType → JsonNode */
    JsonNode encode(T dataType);

    /** 校验：空列表 = 合法 */
    List<String> validate(T dataType, String path);

    /** 归一化：填默认值、清理无效字段 */
    T normalize(T dataType);
}
```

### 5.2 编解码器层级（7 种）

```
DataTypeCodec<T>
  │
  ├── NumberDataTypeCodec<T> (A)    共享 decode/encode/validate/normalize
  │     ├── IntDataTypeCodec        覆写 getTypeName() / newInstance() / getExtremes()
  │     ├── FloatDataTypeCodec
  │     ├── DoubleDataTypeCodec
  │     └── LongDataTypeCodec
  │
  ├── StringDataTypeCodec
  ├── BooleanDataTypeCodec
  ├── EnumDataTypeCodec
  ├── DateDataTypeCodec
  ├── ArrayDataTypeCodec            持有 DataTypeCodecRegistry，递归处理 elementType
  └── ObjectDataTypeCodec           持有 DataTypeCodecRegistry，递归处理子属性
```

### 5.3 DataTypeCodecRegistry

```java
public class DataTypeCodecRegistry {

    private final Map<String, DataTypeCodec<?>> map = new HashMap<>();

    public DataTypeCodecRegistry() {
        register(new IntDataTypeCodec());
        register(new FloatDataTypeCodec());
        register(new DoubleDataTypeCodec());
        register(new LongDataTypeCodec());
        register(new StringDataTypeCodec());
        register(new BooleanDataTypeCodec());
        register(new EnumDataTypeCodec());
        register(new DateDataTypeCodec());
        register(new ArrayDataTypeCodec(this));   // 递归
        register(new ObjectDataTypeCodec(this));  // 递归
    }

    public DataTypeCodec<?> get(String typeName) { ... }
}
```

### 5.4 扩展方式

新增类型仅 3 步，不动已有代码：

```
1. XxxDataType      → metadata/type/
2. XxxDataTypeCodec  → module/support/thingsmodel/codec/
3. DataTypeCodecRegistry 加 register(new XxxDataTypeCodec())
```

---

## 六、JSON 解析设计

### 6.1 分层

```
业务代码
   ↕
JsonNode 接口 + JsonUtil 工具  ← 外部唯一接触面
   ↕
IotJsonNode（包私有）          ← 持有 Jackson JsonNode
   ↕
Jackson ObjectMapper            ← 外部不可见
```

### 6.2 JsonNode 接口

```java
public interface JsonNode {

    // ===== 读取 =====
    String  getString(String field);   // node.getString("id")
    Integer getInt(String field);
    Double  getDouble(String field);
    Boolean getBoolean(String field);
    Long    getLong(String field);
    JsonNode get(String field);       // node.get("valueType")

    // ===== 数组 =====
    boolean isArray();
    int size();
    JsonNode get(int index);         // arr.get(0)

    // ===== 写入 =====
    void put(String field, String value);
    void put(String field, int value);
    void put(String field, double value);
    void put(String field, boolean value);
    void put(String field, long value);
    void put(String field, JsonNode value);
    void add(JsonNode value);        // arr.add(item)

    // ===== 基本 =====
    String asText();
    boolean isNull();
    boolean has(String field);
}
```

### 6.3 JsonUtil 工具类

```java
public class JsonUtil {

    /** JSON 字符串 → JsonNode */
    public static JsonNode parse(String json) { ... }

    /** JsonNode → JSON 字符串 */
    public static String toJson(JsonNode node) { ... }

    /** 创建空对象 {} */
    public static JsonNode createObjectNode() { ... }

    /** 创建空数组 [] */
    public static JsonNode createArrayNode() { ... }
}
```

### 6.4 使用示例

```java
// 解析
JsonNode root = JsonUtil.parse(json);
String id = root.getString("id");
JsonNode vt = root.get("valueType");
String type = vt.getString("type");

// 构建
JsonNode obj = JsonUtil.createObjectNode();
obj.put("id", "x01");
obj.put("min", 0);
obj.put("unit", "celsius");

JsonNode arr = JsonUtil.createArrayNode();
arr.add(obj);

String json = JsonUtil.toJson(arr);
```
