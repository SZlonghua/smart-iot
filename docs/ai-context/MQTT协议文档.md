# MQTT官方协议文档

## 应用场景

### 说明

| 名词 | 解释 |
| --- | --- |
| 上行Topic | 设备端向平台发送 |
| 下行Topic | 平台向设备端发送 |

## 官方协议topic主题说明

### 注意
此Topic用于和MQTT服务通信广播消息，与平台业务Topic无任何关系。

## Topic列表

以下是官方协议包中，设备上报和平台下发数据的Topic。

### 下行Topic

| 设备类型 | 名称 | topic |
| --- | --- | --- |
| 直连 | 读取设备属性 | `/{productId}/{deviceId}/properties/read` |
| 直连 | 修改设备属性 | `/{productId}/{deviceId}/properties/write` |
| 直连 | 调用设备功能 | `/{productId}/{deviceId}/function/invoke` |
| 网关 | 读取子设备属性 | `/{productId}/{deviceId}/child/{childDeviceId}/properties/read` |
| 网关 | 修改子设备属性 | `/{productId}/{deviceId}/child/{childDeviceId}/properties/write` |
| 网关 | 调用子设备功能 | `/{productId}/{deviceId}/child/{childDeviceId}/function/invoke` |

### 上行Topic

| 设备类型 | 名称 | topic |
| --- | --- | --- |
| 直连 | 读取属性回复 | `/{productId}/{deviceId}/properties/read/reply` |
| 直连 | 修改属性回复 | `/{productId}/{deviceId}/properties/write/reply` |
| 直连 | 调用设备功能回复 | `/{productId}/{deviceId}/function/invoke/reply` |
| 直连 | 上报设备事件 | `/{productId}/{deviceId}/event/{eventId}` |
| 直连 | 上报设备属性 | `/{productId}/{deviceId}/properties/report` |
| 网关 | 子设备属性上报 | `/{GateWayProductId}/{GatewayDeviceID}/child/{GatewaySubDeviceId}/properties/report` |
| 网关 | 子设备上线消息 | `/{productId}/{deviceId}/child/{childDeviceId}/online` |
| 网关 | 子设备下线消息 | `/{productId}/{deviceId}/child/{childDeviceId}/offline` |
| 网关 | 读取子设备属性回复 | `/{productId}/{deviceId}/child-reply/{childDeviceId}/properties/read/reply` |
| 网关 | 修改子设备属性回复 | `/{productId}/{deviceId}/child-reply/{childDeviceId}/properties/write/reply` |
| 网关 | 调用子设备功能回复 | `/{productId}/{deviceId}/child-reply/{childDeviceId}/function/invoke/reply` |
| 网关 | 上报子设备事件 | `/{productId}/{deviceId}/child/{childDeviceId}/event/{eventId}` |

## MQTT接入

### 认证

#### 注意
secureId以及secureKey可在创建产品数据和设备实例数据时进行配置。

timestamp为当前系统时间戳(毫秒),与系统时间不能相差5分钟

### 读取设备属性

#### 注意
平台发起读取设备属性指令时，设备侧需进行响应，否则页面在超过10s后提示超时。

设备侧响应消息时需要注意：若设备侧复写了timestamp参数则以复写的时间值为准，默认平台会获取服务器的时间作为timestamp的值，若timestamp值和下发时间值相差超过10s会引起超时。

**下行指令**
- Topic: `/{productId}/{deviceId}/properties/read`
- 消息格式: 
```json
{
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"消息ID",
  "deviceId":"设备ID",
  "properties":["sn","model"] //要读取的属性列表
}
```

**上行回复**
- Topic: `/{productId}/{deviceId}/properties/read/reply`
- 消息格式: 
  - 成功
```json
{
	"timestamp":1601196762389, //毫秒时间戳
	"messageId":"与下行消息中的messageId相同",
	"properties":{"sn":"test","model":"test"}, //key与设备模型中定义的属性id一致
	"deviceId":"设备ID",
	"success":true
}
```
  - 失败
```json
{
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"与下行消息中的messageId相同",
  "deviceId":"设备ID",
  "success":false,
  "code":"error_code",
  "message":"失败原因"
}
```

### 修改设备属性

#### 注意
平台发起修改设备属性指令时，设备侧需响应指令，否则页面在超过10s后提示超时。

**下行指令**
- Topic: `/{productId}/{deviceId}/properties/write`
- 消息格式: 
```json
  {
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"消息ID",
  "deviceId":"设备ID",
  "properties":{"color":"red"} //要设置的属性
}
```

**上行回复**
- Topic: `/{productId}/{deviceId}/properties/write/reply`
- 消息格式: 
```json
  {
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"与下行消息中的messageId相同",
  "properties":{"color":"red"}, //设置成功后的属性,可不返回
  "success":true
}
```

### 设备属性上报

**上行上报**
- Topic: `/{productId}/{deviceId}/properties/report`
- 消息格式: 
```json
  {
  "deviceId":"设备id", //官方协议可以不设置，取的Topic上的设备id
  "messageId":"消息ID",//可以不要设置
  "properties":{"temp":36.8} //上报数据,必填,temp为物模型内定义的属性标识
  "timestamp":1601196762389, //毫秒时间戳,可以不设置，系统默认设置
}
```

### 设备离线消息

**上行上报**
- Topic: `/{productId}/{deviceId}/offline`
- 消息格式: 
```json
  {
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"随机消息ID",
  "deviceId":"设备ID"
}
```

### 调用设备功能

**下行指令**
- Topic: `/{productId}/{deviceId}/function/invoke`
- 消息格式: 
```json
  {
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"消息ID",
  "deviceId":"设备ID",
  "functionId":"playVoice",//功能ID
  "inputs":[{"name":"text","value":"播放声音"}] //参数
}
```

**上行回复**
- Topic: `/{productId}/{deviceId}/function/invoke/reply`
- 消息格式: 
```json
  {
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"与下行消息中的messageId相同",
  "deviceId":"设备ID",
  "output":"success", //返回执行结果,具体类型与物模型中功能输出类型一致
  "success":true
}
```

### 设备事件上报

**上行上报**
- Topic: `/{productId}/{deviceId}/event/{eventId}`
- 消息格式: 
```json
  {
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"随机消息ID",
  "deviceId":"设备ID",
  "data":{
    "test1":1,
    "test2":2
  } 
}
```

#### 说明
设备事件报文解析时，需注意eventId参数应当与系统内物模型配置的事件标识一致。

data内参数名与输出参数的参数名应一致。

### 子设备属性上报

**上行上报**
- Topic: `/{gateWayProductId}/{gatewayDeviceId}/child/{gatewaySubDeviceId}/properties/report`

| 参数 | 说明 |
| --- | --- |
| gateWayProductId | 网关产品ID |
| gatewayDeviceId | 子设备所属父设备ID |
| gatewaySubDeviceId | 网关子设备ID |

- 消息格式: 
```json
  {
    "timestamp":1601196762389, //毫秒时间戳
    "messageId":"随机消息ID",
    "deviceId":"1646353792696614912",
    "properties":{
      "temp":36.8
    }
}
```

### 直连设备注册

实现设备自动绑定.

**上行上报**
- Topic: `/{productId}/{deviceId}/register`
- 消息格式: 
```json
  {
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"随机消息ID",
  "deviceId":"设备ID",
  "productId":"设备在平台的产品ID",
  "deviceName":"设备名称"
}
```
### 直连设备注销

直连设备进行自动解绑.

**上行上报**
- Topic: `/{productId}/{deviceId}/unregister`
- 消息格式: 
```json
{
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"随机消息ID",
  "deviceId":"子设备ID"
}
```

### 直连设备上线

直连设备上线

**上行上报**
- Topic: `/{productId}/{deviceId}/online`
- 消息格式: 
```json
  {
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"随机消息ID",
  "deviceId":"设备ID"
}
```

### 直连设备离线

直连设备离线

**上行上报**
- Topic: `/{productId}/{deviceId}/offline`
- 消息格式: 
```json
  {
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"随机消息ID",
  "deviceId":"设备ID"
}
``` 

### 子设备注册

与子设备消息配合使用,实现设备与网关设备进行自动绑定.

**上行上报**
- Topic: `/{productId}/{deviceId}/child/{childDeviceId}/register`
- 消息格式: 
```json
  {
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"随机消息ID",
  "deviceId":"设备ID",
  "childDeviceId":"子设备ID",
  "childDeviceMessage":{
    "timestamp":1601196762389, //毫秒时间戳
    "messageId":"随机消息ID",
    "deviceId":"子设备ID",
    "productId":"子设备在平台的产品ID",
    "deviceName":"子设备名称"
  }
}
```

### 子设备注销

与子设备消息配合使用,实现设备与网关设备进行自动解绑.

**上行上报**
- Topic: `/{productId}/{deviceId}/child/{childDeviceId}/unregister`
- 消息格式: 
```json
{
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"随机消息ID",
  "deviceId":"设备ID",
  "childDeviceId":"子设备ID",
  "childDeviceMessage":{
    "timestamp":1601196762389, //毫秒时间戳
    "messageId":"随机消息ID",
    "deviceId":"子设备ID"
  }
}
```

### 子设备上线

与子设备消息配合使用,实现关联到网关的子设备上线.

父设备需要携带子设备报文，并且协议包内需将子设备报文解析为任意子设备消息ChildDeviceMessage。

**上行上报**
- Topic: `/{productId}/{deviceId}/child/{childDeviceId}/online`
- 消息格式: 
```json
  {
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"随机消息ID",
  "deviceId":"设备ID",
  "childDeviceId":"子设备ID",
  "childDeviceMessage":{
    "timestamp":1601196762389, //毫秒时间戳
    "messageId":"随机消息ID",
    "deviceId":"子设备ID"
  }
}
```

### 子设备离线

与子设备消息配合使用,实现关联到网关的子设备离线。子设备离线逻辑参考[子设备状态管理说明](https://hanta.yuque.com/px7kg1/yfac2l/mqk91kd7r9hor85a#dVRGq)

**上行上报**
- Topic: `/{productId}/{deviceId}/child/{childDeviceId}/offline`
- 消息格式: 
```json
  {
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"随机消息ID",
  "deviceId":"设备ID",
  "childDeviceId":"子设备ID",
  "childDeviceMessage":{
    "timestamp":1601196762389, //毫秒时间戳
    "messageId":"随机消息ID",
    "deviceId":"子设备ID"
  }
}
``` 

### 修改子设备属性

#### 注意
平台发起修改设备属性指令时，设备侧需响应指令，否则页面在超过10s后提示超时。

**下行指令**
- Topic: `/{productId}/{deviceId}/child/{childDeviceId}/properties/write`
- 消息格式: 
```json
 {  
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"随机消息ID",
  "deviceId":"设备ID",
  "childDeviceId":"子设备ID",
  "childDeviceMessage":{
    "timestamp":1601196762389, //毫秒时间戳
    "messageId":"随机消息ID",
    "deviceId":"子设备ID",
    "properties":{"color":"red"} //要设置的属性
  }
}
``` 

**上行回复**
- Topic: `/{productId}/{deviceId}/child/{childDeviceId}/properties/write/reply`
- 消息格式: 
```json
{  
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"随机消息ID",
  "deviceId":"设备ID",
  "childDeviceId":"子设备ID",
  "childDeviceMessage":{
    "timestamp":1601196762389, //毫秒时间戳
    "messageId":"随机消息ID",
    "deviceId":"子设备ID",
    "properties":{"color":"red"}, //设置成功后的属性,可不返回
    "success":true //是否成功设置
  }
}
```   

### 断开子设备连接

平台主动断开设备连接时，会发送此指令到网关

**下行指令**
- Topic: `/{productId}/{deviceId}/child/{childDeviceId}/disconnect`
- 消息格式: 
```json
{  
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"随机消息ID",
  "deviceId":"设备ID",
  "childDeviceId":"子设备ID",
  "childDeviceMessage":{
    "timestamp":1601196762389, //毫秒时间戳
    "messageId":"随机消息ID",
    "deviceId":"子设备ID"
  }
}
```     

**上行回复**
- Topic: `/{productId}/{deviceId}/child-reply/{childDeviceId}/disconnect/reply`
- 消息格式: 
```json
 {  
  "timestamp":1601196762389, //毫秒时间戳
  "messageId":"随机消息ID",
  "deviceId":"设备ID",
  "childDeviceId":"子设备ID",
  "childDeviceMessage":{
    "timestamp":1601196762389, //毫秒时间戳
    "messageId":"随机消息ID",
    "deviceId":"子设备ID",
    "success":true //是否成功断开连接
  }
} 
```       