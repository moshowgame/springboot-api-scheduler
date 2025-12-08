package com.software.dev.util;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

public class EncryptPassword {
    public static void main(String[] args) {
        String rawPwd = "root123";
        String secretKey = "world-of-moshow";
        
        // 创建加密器
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword(secretKey);
        config.setAlgorithm("PBEWithMD5AndDES");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setStringOutputType("base64");
        encryptor.setConfig(config);
        
        // 加密
        String encrypted = encryptor.encrypt(rawPwd);
        System.out.println("原文: " + rawPwd);
        System.out.println("密钥: " + secretKey);
        System.out.println("算法: PBEWithMD5AndDES");
        System.out.println("加密后: ENC(" + encrypted + ")");
        
        // 验证解密
        String decrypted = encryptor.decrypt(encrypted);
        System.out.println("解密后: " + decrypted);
        System.out.println("匹配结果: " + rawPwd.equals(decrypted));
    }
}