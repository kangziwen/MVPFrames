package me.mvp.frame.di.module;

import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import me.mvp.frame.http.converter.GsonConverterBodyFactory;
import me.mvp.frame.http.converter.JsonConverterFactory;
import me.mvp.frame.http.interceptor.NetworkInterceptor;
import me.mvp.frame.utils.FileUtils;

import java.io.File;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.rx_cache2.internal.RxCache;
import io.victoralbertos.jolyglot.GsonSpeaker;
import okhttp3.Cache;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * HTTP参数配置
 */
@Module
public class HttpModule {

    // 连接、读取、写入超时时间
    private static final int TIME_OUT = 60;
    // 缓存文件最大值100MB
    private static final int CACHE_MAX_SIZE = 1024 * 1024 * 10 * 100;

    @Singleton
    @Provides
    Retrofit provideRetrofit(Application application, HttpModule.RetrofitConfiguration retrofitConfiguration, Retrofit.Builder builder, OkHttpClient client
            , HttpUrl httpUrl, Gson gson) {
        builder
                .baseUrl(httpUrl)// 域名
                .client(client);// 设置OkHttpClient

        retrofitConfiguration.configRetrofit(application, builder);

        builder
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())// 支持 RxJava
                .addConverterFactory(GsonConverterBodyFactory.create(gson))// 支持 Gson
                .addConverterFactory(JsonConverterFactory.create());// 支持 Json
        return builder.build();
    }

    @Singleton
    @Provides
    OkHttpClient provideClient(@Nullable Application application, @Nullable HttpModule.OkHttpConfiguration okHttpConfiguration, @Nullable OkHttpClient.Builder okHttpClient, @Nullable Interceptor interceptor, List<Interceptor> interceptors, CookieJar cookie) {
        OkHttpClient.Builder builder = okHttpClient
                .connectTimeout(TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(TIME_OUT, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)// 设置出现错误进行重新连接
                .cache(new Cache(application.getCacheDir(), CACHE_MAX_SIZE))// 设置缓存路径和大小
                .addNetworkInterceptor(interceptor);// 网络拦截器，在 Request 和 Resposne 是分别被调用一次

        if (cookie != null) {
            builder.cookieJar(cookie);// Cookie
        }

        if (interceptors != null && interceptors.size() > 0) {// Interceptors，只在Response被调用一次
            for (Interceptor item : interceptors) {
                builder.addInterceptor(item);
            }
        }

        if (okHttpConfiguration != null) {
            okHttpConfiguration.configOkHttp(application, builder);
        }

        return builder.build();
    }

    @Singleton
    @Provides
    Retrofit.Builder provideRetrofitBuilder() {
        return new Retrofit.Builder();
    }


    @Singleton
    @Provides
    OkHttpClient.Builder provideOkHttpClientBuilder() {
        return new OkHttpClient.Builder();
    }

    /**
     * Http拦截器
     */
    @Singleton
    @Provides
    Interceptor provideNetworkInterceptor(NetworkInterceptor interceptor) {
        return interceptor;
    }

    /**
     * 提供RxCache
     *
     * @param cacheDirectory RxCache缓存路径
     * @return
     */
    @Singleton
    @Provides
    RxCache provideRxCache(Application application, @Nullable RxCacheConfiguration configuration, @Named("RxCacheDirectory") File cacheDirectory) {
        RxCache.Builder builder = new RxCache.Builder();
        RxCache rxCache = null;
        if (configuration != null) {
            rxCache = configuration.configRxCache(application, builder);
        }
        if (rxCache != null) return rxCache;
        return builder.persistence(cacheDirectory, new GsonSpeaker());
    }


    /**
     * 需要单独给RxCache提供缓存路径
     * 提供RxCache缓存地址
     */
    @Singleton
    @Provides
    @Named("RxCacheDirectory")
    File provideRxCacheDirectory(File cacheDir) {
        File cacheDirectory = new File(cacheDir, "RxCache");
        return FileUtils.makeDirs(cacheDirectory);
    }

    /**
     * 提供一个Retrofit配置接口，用于对Retrofit进行格外的参数配置
     */
    public interface RetrofitConfiguration {

        void configRetrofit(Context context, Retrofit.Builder builder);

        RetrofitConfiguration EMPTY = new RetrofitConfiguration() {

            @Override
            public void configRetrofit(Context context, Retrofit.Builder builder) {

            }
        };
    }

    /**
     * 提供一个OkHttp配置接口，用于对OkHttp进行格外的参数配置
     */
    public interface OkHttpConfiguration {

        void configOkHttp(Context context, OkHttpClient.Builder builder);

        OkHttpConfiguration EMPTY = new OkHttpConfiguration() {

            @Override
            public void configOkHttp(Context context, OkHttpClient.Builder builder) {

            }
        };
    }

    public interface RxCacheConfiguration {

        RxCache configRxCache(Context context, RxCache.Builder builder);

        RxCacheConfiguration EMPTY = new RxCacheConfiguration() {

            @Override
            public RxCache configRxCache(Context context, RxCache.Builder builder) {
                return null;
            }
        };
    }
}