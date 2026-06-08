# 智能物联网平台 - 详细设计文档

---

## 1. 数据库设计

### 1.1 产品分类表（product_category）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT(20) | PRIMARY KEY, AUTO_INCREMENT | 分类ID |
| name | VARCHAR(64) | NOT NULL | 分类名称 |
| parent_id | BIGINT | FOREIGN KEY, DEFAULT 0 | 父分类ID |
| sort_order | INT | DEFAULT 0 | 排序值 |
| description | VARCHAR(255) | | 分类描述 |
| create_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 1.2 产品表（product）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT(20) | PRIMARY KEY, AUTO_INCREMENT | 产品ID |
| product_key | VARCHAR(64) | UNIQUE, NOT NULL | 产品Key |
| product_secret | VARCHAR(128) | NOT NULL | 产品密钥 |
| name | VARCHAR(128) | NOT NULL | 产品名称 |
| category_id | BIGINT | FOREIGN KEY | 分类ID |
| category_name | VARCHAR(64) | | 分类名称 |
| device_type | VARCHAR(32) | NOT NULL, DEFAULT 'direct' | 设备类型(direct:直连设备, gateway-child:网关子设备, gateway:网关设备) |
| description | TEXT | | 产品描述 |
| model_json | LONGTEXT | | 物模型JSON |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态(0禁用/1启用) |
| create_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 1.3 设备表（device）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT(20) | PRIMARY KEY, AUTO_INCREMENT | 设备ID |
| name | VARCHAR(128) | NOT NULL | 设备名称 |
| device_key | VARCHAR(64) | UNIQUE, NOT NULL | 设备Key |
| device_secret | VARCHAR(128) | NOT NULL | 设备密钥 |
| parent_device_id | BIGINT | | 父级设备ID |
| product_id | BIGINT(20) | | 产品ID |
| status | TINYINT | NOT NULL, DEFAULT 0 | 状态(0离线/1在线) |
| last_online_time | DATETIME | | 最后在线时间 |
| create_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 1.4 网络组件表（network_component）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT(20) | PRIMARY KEY, AUTO_INCREMENT | 组件ID |
| name | VARCHAR(64) | NOT NULL | 组件名称 |
| type | VARCHAR(32) | NOT NULL | 类型(tcp:TCP服务, http:HTTP服务, mqtt-server:MQTT服务, mqtt-client:MQTT客户端) |
| configuration | LONGTEXT | | 配置信息(JSON格式，不同协议有不同的配置字段) |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态(0停止/1运行) |
| description | VARCHAR(255) | | 组件描述 |
| create_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**configuration字段JSON格式说明：**

```json
{
  "listen_address": "0.0.0.0",
  "listen_port": 1883,
  "username": "admin",
  "password": "password",
  "qos": 1,
  "max_connections": 10000,
  "keepalive_interval": 60
}
```

**不同协议配置示例：**

| 协议类型 | 配置示例 |
|----------|----------|
| **MQTT** | `{"listen_address": "0.0.0.0", "listen_port": 1883, "username": "admin", "password": "xxx", "qos": 1}` |
| **HTTP** | `{"listen_port": 8080, "path": "/api", "auth_type": "token", "timeout": 30}` |
| **CoAP** | `{"listen_port": 5683, "resource_path": "/devices", "block_size": 1024}` |

### 1.5 协议表（protocol）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT(20) | PRIMARY KEY, AUTO_INCREMENT | 协议ID |
| name | VARCHAR(64) | NOT NULL | 协议名称 |
| version | VARCHAR(16) | NOT NULL | 版本号 |
| jar_path | VARCHAR(255) | | JAR包路径 |
| description | VARCHAR(255) | | 协议描述 |
| create_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 1.6 网关表（gateway）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT(20) | PRIMARY KEY, AUTO_INCREMENT | 网关ID |
| name | VARCHAR(64) | NOT NULL | 网关名称 |
| type | VARCHAR(32) | NOT NULL | 接入类型(mqtt-direct:MQTT直连接入, gateway-child:网关子设备接入, http-push:HTTP推送接入, mqtt-broker:MQTT Broker接入) |
| component_id | BIGINT | FOREIGN KEY | 网络组件ID |
| protocol_id | BIGINT | FOREIGN KEY | 协议ID |
| transport | VARCHAR(32) | NOT NULL, DEFAULT 'tcp' | 传输协议(tcp:TCP, udp:UDP, ws:WebSocket) |
| description | VARCHAR(255) | | 网关描述 |
| create_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 1.7 告警日志表（alarm_log）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT(20) | PRIMARY KEY, AUTO_INCREMENT | 告警ID |
| device_key | VARCHAR(64) | NOT NULL | 设备Key |
| level | TINYINT | NOT NULL | 级别(1紧急/2重要/3次要/4提示) |
| description | VARCHAR(512) | NOT NULL | 告警描述 |
| status | TINYINT | NOT NULL, DEFAULT 1 | 状态(1未处理/2处理中/3已处理/4已忽略) |
| trigger_time | DATETIME | NOT NULL | 触发时间 |
| handle_time | DATETIME | | 处理时间 |
| handler | VARCHAR(64) | | 处理人 |
| handle_note | TEXT | | 处理说明 |
| create_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 1.8 运行日志表（device_log）

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| id | BIGINT(20) | PRIMARY KEY, AUTO_INCREMENT | 日志ID |
| device_id | BIGINT(20) | NOT NULL | 设备ID |
| device_name | VARCHAR(128) | NOT NULL | 设备名称 |
| type | VARCHAR(32) | NOT NULL | 类型(online:上线, offline:离线, properties_report:属性上报, properties_get:读取属性, event:事件, command:命令) |
| content | LONGTEXT | | 消息数据(JSON格式) |
| create_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP | 创建时间 |
| update_time | DATETIME | NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

### 1.9 TDengine超级表结构

为支持海量设备属性数据的高效存储和查询，采用TDengine时序数据库存储设备属性历史数据。每个产品对应一个超级表（STable）。

**产品p1的超级表示例：**

```sql
CREATE STABLE IF NOT EXISTS device_properties_p1 (
    _ts timestamp,
    id VARCHAR(64),
    deviceId VARCHAR(64),
    messageId VARCHAR(64),
    createTime timestamp,
    timestamp timestamp,
    property VARCHAR(64),
    numberValue DOUBLE,
    geoValue GEOGRAPHY,
    arrayValue BINARY,
    objectValue JSON,
    timeValue timestamp,
    value VARCHAR(512)
) TAGS (
    deviceId VARCHAR(64),
    templateId VARCHAR(64),
    property VARCHAR(64)
);
```

---

## 2. 物模型JSON结构

### 2.1 完整物模型结构

```json
{
  "properties": [...属性],
  "functions": [...功能],
  "events": [...事件]
}
```

### 2.2 属性结构

```json
{
  "id": "temperature",
  "name": "温度",
  "valueType": {
    值类型
  },
  "accessMode": "rw",
  "description": "设备温度"
}
```

**属性字段说明：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | string | 属性标识符（唯一） |
| name | string | 属性名称 |
| valueType | json | 数值类型定义 |
| accessMode | string | 访问模式(r/rw/w) |
| description | string | 描述 |

### 2.2.1 数值类型说明

#### 2.2.1.1 数字类型

```json
{
     "type":"int",
     "max":100,//最大值
     "min":0,//最小值 
     "unit":"percent"//单位
}
```

**数字类型说明：**

type: 数字值类型（double/int/float/long）


#### 2.2.1.2 boolean布尔类型

```json
{
    "type":"boolean",
    "trueText":"开启",
    "falseText":"关闭",
    "trueValue":"1",
    "falseValue":"0"
}
```

**布尔类型说明：**

trueText 为true时的文本,默认为是
falseText 为false时的文本,默认为否
trueValue 为true时的值,默认为true
falseValue 为false时的值,默认为false

#### 2.2.1.3 string字符类型

```json
{
     "type":"string"
}
```

#### 2.2.1.4 enum类型

```json
  {
      "type":"enum",
      "elements":[
          {"value":"1","text":"正常","description":"说明"},
          {"value":"-1","text":"警告","description":"警告状态"},
          {"value":"0","text":"未知","description":"未知状态"}  
      ]
   }
```

**枚举类型说明：**

Element:
value 枚举值
text 枚举文本
description 说明

#### 2.2.1.5 date时间类型

```json
{
      "type":"date",
      "format":"yyyy-MM-dd",
      "tz": "Asia/Shanghai"
}
```

**数字类型说明：**

属性
format 格式,如: yyyy-MM-dd
tz 时区,如: Asia/Shanghai

#### 2.2.1.6 array数组类型

```json
 {
      "type":"array",
      "elementType":{ 
          "type":"string"
      }
}
```

**数组类型说明：**

type: 数组值类型（string/int/float/long）
elementType: 数组元素类型（string/int/float/long/double/enum/boolean/date）,相应类型对应相应的值类型

#### 2.2.1.7 object对象类型

```json
{
      "type":"object",
      "properties":[
          {
           "id": "location",
           "name": "地点",
           "valueType": {
             "type": "string"
           }
         },
         {
           "id": "lng",
           "name": "经度",
           "valueType": {
             "type": "double"
           }
         },
         {
           "id": "lat",
           "name": "纬度",
           "valueType": {
             "type": "double"
           }
         }
      ]
}
```

**数字类型说明：**

属性
format 格式,如: yyyy-MM-dd
tz 时区,如: Asia/Shanghai

#### 2.2.1.8 date时间类型

```json
{
      "type":"date",
      "format":"yyyy-MM-dd",
      "tz": "Asia/Shanghai"
}
```

**数字类型说明：**

属性
format 格式,如: yyyy-MM-dd
tz 时区,如: Asia/Shanghai

### 2.3 功能结构

```json
{
   "id": "playVoice", //功能标识
   "name": "播放声音", //名称
   "async": false, //是否异步
   "description": "播放声音", //描述
   "inputs": [  //输入参数
     {
       "id": "text",
       "name": "文字内容",
       "required": true,
       "valueType": { //参数类型
         "type": "string"
       }
     }
   ],
   "output": { //输出
     "type": "boolean", //输出类型
     "properties": [ //输出参数类型为Object时，可配置属性
        {
           "valueType": {
               "type": "string"
           },
           "name": "名称",
           "id": "id"
        }
     ]
   }
 }
```

**功能字段说明：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | string | 功能标识符（唯一） |
| name | string | 功能名称 |
| inputs | array | 输入参数列表 |
| output | array | 输出参数列表 |

### 2.4 事件结构

```json
{
    "id": "事件ID", 
    "name": "事件名称", //名称
    "description": "事件描述", //描述
    "type": "warning",
    "valueType": {
      "type": "object",  //对象(结构体)类型
      "properties": [    //对象属性(结构与属性定义相同)
        {
          "id": "属性ID",
          "name": "属性名称",
          "valueType": {
            "type": "string"
          }
        }
      ]
    }
 }
```

**事件字段说明：**

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | string | 事件标识符（唯一） |
| name | string | 事件名称 |
| type | string | 事件类型(warning/error/info) |

---

网络组件type字段说明：
组件类型：  tcp:TCP服务, http:HTTP服务, mqtt-server:MQTT服务, mqtt-client:MQTT客户端
JSON配置信息如下：


TCP服务：
-本地地址（必填）：固定为0.0.0.0
-本地端口（必填）：数字
-公网地址（必填）
-公网端口（必填）：数字
-开启TLS（必填）: 单选：是，否

MQTT客户端：
-远程地址（必填）
-远程端口（必填）：数字
-clientId（必填）
-用户名（必填）
-密码（必填）
-最大消息长度（必填）
-开启TLS（必填）: 单选：是，否

HTTP服务：
-本地地址（必填）：固定为0.0.0.0
-本地端口（必填）：数字
-公网地址（必填）
-公网端口（必填）：数字
-开启TLS（必填）: 单选：是，否

MQTT服务：
-本地地址（必填）：固定为0.0.0.0
-本地端口（必填）：数字
-公网地址（必填）
-公网端口（必填）：数字
-最大消息长度（必填）
-开启TLS（必填）: 单选：是，否
