package net.lab1024.sa.base.signature;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 签名算法管理器 — static 注册所有算法，提供统一签名入口。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/11
 * &#064;Copyright  1024创新实验室
 */
public final class SignatureManager {

    private static final Map<String, SignatureAlgorithm> registry = new ConcurrentHashMap<>();

    static {
        register(new HmacSha256SignatureAlgorithm());
    }

    private SignatureManager() {
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
