package net.lab1024.sa.base.signature;

/**
 * 签名算法接口。
 * <p>
 * &#064;Author  廖涛
 * &#064;Date  2026/08/11
 * &#064;Copyright  1024创新实验室
 */
public interface SignatureAlgorithm {

    /** 算法标识，对应 clientId 中 signType 字段，如 "sha256"、"sm3" */
    String getType();

    /** 使用密钥对数据进行签名 */
    String sign(String data, String secret);
}
