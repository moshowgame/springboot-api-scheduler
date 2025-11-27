package com.software.dev.util;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import com.alibaba.fastjson2.JSON;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
public class HttpUtil {

    private static final OkHttpClient httpClient = createHttpClient();

    private static OkHttpClient createHttpClient() {
        final TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        //nothing to verify
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                        //nothing to verify
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
        SSLContext sslContext = null;
        {
            try {
                sslContext = SSLContext.getInstance("SSL");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
        builder.hostnameVerifier((hostname, session) -> true);

        return builder
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    /**
     * 构建带参数的URL
     * @param baseUrl 基础URL
     * @param params 参数Map
     * @return 带参数的完整URL
     */
    private static String buildUrlWithParams(String baseUrl, Map<String, String> params) {
        if (params == null || params.isEmpty()) {
            return baseUrl;
        }

        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        boolean hasQuery = baseUrl.contains("?");
        for (Map.Entry<String, String> entry : params.entrySet()) {
            try {
                String key = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name());
                String value = URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.name());
                if (!hasQuery) {
                    urlBuilder.append("?");
                    hasQuery = true;
                } else {
                    urlBuilder.append("&");
                }
                urlBuilder.append(key).append("=").append(value);
            } catch (Exception e) {
                log.warn("Failed to encode URL parameter: {}={}", entry.getKey(), entry.getValue(), e);
            }
        }
        return urlBuilder.toString();
    }

    /**
     * 发送GET请求
     * @param url 请求URL
     * @param headers 请求头
     * @param params URL参数
     * @return 响应结果
     */
    public static String get(String url, Map<String, String> headers, Map<String, String> params) {
        try {
            String fullUrl = buildUrlWithParams(url, params);
            Request.Builder requestBuilder = new Request.Builder().url(fullUrl);

            // 添加请求头
            if (headers != null && !headers.isEmpty()) {
                headers.forEach(requestBuilder::addHeader);
            }

            Request request = requestBuilder.get().build();
            try (Response response = httpClient.newCall(request).execute()) {
                return response.body() != null ? response.body().string() : "";
            }
        } catch (Exception e) {
            log.error("GET request failed, url: {}", url, e);
            throw new RuntimeException("GET request failed", e);
        }
    }

    /**
     * 发送POST请求
     * @param url 请求URL
     * @param headers 请求头
     * @param body 请求体
     * @param contentType 内容类型
     * @return 响应结果
     */
    public static String post(String url, Map<String, String> headers, String body, String contentType) {
        try {
            Request.Builder requestBuilder = new Request.Builder().url(url);

            // 添加请求头
            if (headers != null && !headers.isEmpty()) {
                headers.forEach(requestBuilder::addHeader);
            }

            // 设置请求体
            MediaType mediaType = MediaType.parse(contentType != null ? contentType : "application/json; charset=utf-8");
            RequestBody requestBody = RequestBody.create(body != null ? body : "", mediaType);

            Request request = requestBuilder.post(requestBody).build();
            try (Response response = httpClient.newCall(request).execute()) {
                return response.body() != null ? response.body().string() : "";
            }
        } catch (Exception e) {
            log.error("POST request failed, url: {}", url, e);
            throw new RuntimeException("POST request failed", e);
        }
    }

    /**
     * 发送HTTP请求
     * @param url 请求URL
     * @param method 请求方法
     * @param headers 请求头
     * @param params URL参数
     * @param body 请求体
     * @param contentType 内容类型
     * @return 响应结果
     */
    public static String request(String url, String method, Map<String, String> headers, 
                                 Map<String, String> params, String body, String contentType) {
        if ("GET".equalsIgnoreCase(method)) {
            return get(url, headers, params);
        } else if ("POST".equalsIgnoreCase(method)) {
            return post(url, headers, body, contentType);
        } else {
            throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }
    }
}