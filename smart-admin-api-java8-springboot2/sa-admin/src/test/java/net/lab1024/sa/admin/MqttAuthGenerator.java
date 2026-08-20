package net.lab1024.sa.admin;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.lab1024.sa.base.signature.SignatureManager;

/**
 * MQTT 认证参数生成器 — 生成 clientId/username/password，方便用 MQTT 客户端（MQTTX 等）手动测试连接。
 * <p>
 * 规则：
 * - clientId = "productKey:deviceKey:timestamp:mode:signType"
 * - username = "productKey:deviceKey"（冒号连接，productKey 含 "-" 不能用 "-" 分隔）
 * - password = HMAC-SHA256 签名（hex），data = "productKey:deviceKey:timestamp"
 *   mode=1 一机一密 → 用 deviceSecret；mode=2 一型一密 → 用 productSecret
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/17
 * &#064;Copyright  1024创新实验室
 */
public class MqttAuthGenerator {

    /** 生成一机一密（mode=1, sha256）认证参数 */
    public static Credentials generate(String productKey, String deviceKey, String deviceSecret) {
        return generate(productKey, deviceKey, deviceSecret, null, 1, "sha256");
    }

    /** 生成认证参数 — mode=1 用 deviceSecret，mode=2 用 productSecret */
    public static Credentials generate(String productKey, String deviceKey,
                                       String deviceSecret, String productSecret,
                                       int mode, String signType) {
        long timestamp = System.currentTimeMillis();
        String data = productKey + ":" + deviceKey + ":" + timestamp;
        String secret = mode == 1 ? deviceSecret : productSecret;
        String signature = SignatureManager.sign(signType, data, secret);
        return new Credentials(
                productKey + ":" + deviceKey + ":" + timestamp + ":" + mode + ":" + signType,
                productKey + ":" + deviceKey,
                signature);
    }

    /** 测试入口 — 直接运行打印认证参数 */
    public static void main(String[] args) {
        Credentials credentials = generate("PK-8CD4A2CA480E4DD0", "DK-8BE3FE31FECF4DC5", "0e8a54d92c434cd0ba876e17e8a3b968");
        System.out.println(credentials);
    }

    @Getter
    @AllArgsConstructor
    @ToString
    public static class Credentials {

        private final String clientId;
        private final String username;
        private final String password;
    }
}
