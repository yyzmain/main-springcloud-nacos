package com.main.utils;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * <p>Title: 基于OKHttp的请求工具类</p>
 */
public class MainOkHttpUtil {

    private static final Logger logger = LoggerFactory.getLogger(MainOkHttpUtil.class);

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");
    private static final MediaType MEDIA_TYPE_XML = MediaType.parse("application/xml; charset=utf-8");

    private static final byte[] LOCKER = new byte[0];

    private static MainOkHttpUtil instance;

    private OkHttpClient mOkHttpClient;

    private MainOkHttpUtil(int readTimeout, int connectTimeout, int writeTimeout) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        //设置读取超时时间
        clientBuilder.readTimeout(readTimeout, TimeUnit.SECONDS);
        //设置超时连接时间
        clientBuilder.connectTimeout(connectTimeout, TimeUnit.SECONDS);
        //设置写入超时时间
        clientBuilder.writeTimeout(writeTimeout, TimeUnit.SECONDS);
        //支持HTTPS请求，跳过证书验证
        clientBuilder.sslSocketFactory(createSSLSocketFactory());
        clientBuilder.hostnameVerifier((hostname, session) -> Boolean.TRUE);

        mOkHttpClient = clientBuilder.build();
    }

    /**
     * 单例模式获取NetUtils
     */
    public static MainOkHttpUtil getInstance(int readTimeout, int connectTimeout, int writeTimeout) {
        if (instance == null) {
            synchronized (MainOkHttpUtil.class) {
                if (Objects.isNull(instance)) {
                    instance = new MainOkHttpUtil(readTimeout, connectTimeout, writeTimeout);
                }
            }
        }
        return instance;
    }

    /**
     * 针对json post处理
     */
    public String postJson(String url, String json, Map<String, String> headerMap) throws IOException {
        RequestBody body = RequestBody.create(MEDIA_TYPE_JSON, json);
        return getString(url, headerMap, body);
    }


    /**
     * 针对json post处理
     */
    public String postxML(String url, String xmlStr, Map<String, String> headerMap) throws IOException {
        RequestBody body = RequestBody.create(MEDIA_TYPE_XML, xmlStr);
        return getString(url, headerMap, body);
    }

    private String getString(String url, Map<String, String> headerMap, RequestBody body) throws IOException {
        Request.Builder requestBuilder = new Request.Builder().url(url).post(body);
        addHeaders(headerMap, requestBuilder);
        Request request = requestBuilder.build();
        Response response = mOkHttpClient.newCall(request).execute();
        if (response.isSuccessful()) {
            return Objects.requireNonNull(response.body()).string();
        } else {
            throw new IOException("Unexpected code " + response);
        }
    }


    /**
     * 生成安全套接字工厂，用于https请求的证书跳过
     */
    public SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;
        try {
            SSLContext sc = SSLContext.getInstance("TLSv1.2");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());
            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
            logger.error("", e);
        }
        return ssfFactory;
    }


    /**
     * 添加header信息
     */
    private static Request.Builder addHeaders(Map<String, String> headerMap, Request.Builder builder) {
        if (headerMap != null && !headerMap.isEmpty()) {
            headerMap.forEach(builder::addHeader);
        }
        return builder;
    }

    /**
     * 用于信任所有证书
     */
    class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
            //
        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
            //
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

}