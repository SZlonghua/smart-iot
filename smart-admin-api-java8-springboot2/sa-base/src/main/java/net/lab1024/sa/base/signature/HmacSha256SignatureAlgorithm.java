package net.lab1024.sa.base.signature;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * HMAC-SHA256 签名算法实现。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/11
 * &#064;Copyright  1024创新实验室
 */
public class HmacSha256SignatureAlgorithm implements SignatureAlgorithm {

    @Override
    public String getType() {
        return "sha256";
    }

    @Override
    public String sign(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Hex.encodeHexString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 签名失败", e);
        }
    }
}
