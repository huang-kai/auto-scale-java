package com.hujiang.autoscale.utils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Kyne on 16/9/6.
 */
@Slf4j
public class OKHttpUtil {
    private static OKHttpUtil INSTANCE = new OKHttpUtil();

    private static OkHttpClient client;

    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    public static synchronized OKHttpUtil getInstance(){
        ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                .tlsVersions(TlsVersion.TLS_1_2)
                .cipherSuites(
                        CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256,
                        CipherSuite.TLS_DHE_RSA_WITH_AES_128_GCM_SHA256)
                .build();
        client = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
//                .connectionSpecs(Collections.singletonList(spec)).
                .addInterceptor(new RetryInterceptor())
                .build();
        return INSTANCE;
    }


    public ResponseBody doSyncRequest(String method, String strUrl, String body,  Map<String, String> parameters, Map<String, String> headers) throws IOException {
        HttpUrl url = HttpUrl.parse(strUrl);
        if(parameters!= null && !parameters.isEmpty() ){
            HttpUrl.Builder builder = url.newBuilder();
            parameters.forEach((key,value)->{
                builder.addQueryParameter(key,value);

            });
            url = builder.build();
        }
        Request.Builder builder = new Request.Builder().url(url);
        if(headers!= null && !headers.isEmpty() )
            builder.headers(Headers.of(headers));

        switch (method){
            case "PUT":
                builder.put(RequestBody.create(MEDIA_TYPE_JSON, body));
                break;
            case "POST":
                builder.post(RequestBody.create(MEDIA_TYPE_JSON, body));
                break;
            case "DELETE":
                builder.delete();
                break;
            default:
                builder.get();
        }

        Response response = null;
        try {
            response = client.newCall(builder.build()).execute();
            return response.body();
        } catch (IOException e) {
            if (response!=null){
               response.close();
            }
            throw e;
        }
    }

    public static void main(String[] args) {
        try {
            OKHttpUtil.getInstance().doSyncRequest("GET", "http://192.168.33.60:4000/", null, null, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String doGet(String url, Map<String, String> parameters, Map<String, String> headers) throws IOException {
        return doSyncRequest("GET",url,null,parameters,headers).string();
    }

    public String doPut(String url, String body, Map<String, String> parameters, Map<String, String> headers) throws IOException {
        return doSyncRequest("PUT",url,body,parameters,headers).string();
    }
}

@Slf4j
class RetryInterceptor implements Interceptor {
    @Override public Response intercept(Chain chain) throws IOException {
        int retryTime = 0;
        while (retryTime<=3){
            if (retryTime>0){
                log.debug("Waiting {} s ...", 3*retryTime);
                try {
                    Thread.sleep(3*retryTime*1000);
                } catch (InterruptedException e1) {
                    ;
                }
                log.debug("Retry {} time(s)", retryTime);
            }
            Request request = chain.request();
            Response response= null;
            try {
                response = chain.proceed(request);
                if (!response.isSuccessful()){
                    throw new IOException("Request is not successful");
                }else{
                    return response;
                }
            } catch (IOException e) {
                log.error("Send http request error ", e);
                if (response!=null){
                    response.close();
                }
            }
            retryTime++;
        }
        throw new IOException();

    }
}
