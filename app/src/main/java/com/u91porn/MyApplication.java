package com.u91porn;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import android.app.Application;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.danikula.videocache.HttpProxyCacheServer;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.helper.loadviewhelper.load.LoadViewHelper;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.squareup.leakcanary.LeakCanary;
import com.u91porn.data.NoLimit91PornServiceApi;
import com.u91porn.data.cache.CacheProviders;
import com.u91porn.data.model.MyObjectBox;
import com.u91porn.data.model.UnLimit91PornItem;
import com.u91porn.data.model.UnLimit91PornItem_;
import com.u91porn.utils.Constants;
import com.u91porn.utils.Keys;
import com.u91porn.utils.MyFileNameGenerator;
import com.u91porn.utils.SPUtils;

import io.objectbox.Box;
import io.objectbox.BoxStore;
import io.objectbox.android.AndroidObjectBrowser;
import io.reactivex.annotations.Nullable;
import io.rx_cache2.internal.RxCache;
import io.victoralbertos.jolyglot.GsonSpeaker;

/**
 * 应用入口
 *
 * @author flymegoc
 * @date 2017/11/14
 * @describe
 */

public class MyApplication extends Application {

  private NoLimit91PornServiceApi mNoLimit91PornServiceApi;
  private static MyApplication mMyApplication;
  private PersistentCookieJar cookieJar;
  private volatile String host;
  /**
   * 视频缓存
   */
  private HttpProxyCacheServer proxy;
  private CacheProviders cacheProviders;
  private BoxStore boxStore;

  @Override
  public void onCreate() {
    super.onCreate();
    mMyApplication = this;
    host = (String) SPUtils.get(this, Keys.KEY_SP_NOW_ADDRESS, "");
    initBoxStore();
    initLeakCanry();
    initRetrofit();
    initCache();
    // 每次启动清除cookies
    cleanCookies();
    Fresco.initialize(this);
    initLogger();
    initLoadingHelper();
    initFileDownload();
  }

  private void initFileDownload() {
    FileDownloader.setup(this);
    // 将上次因为程序以外终止而处于下载状态的任务恢复成暂停状态
    Box<UnLimit91PornItem> box = boxStore.boxFor(UnLimit91PornItem.class);
    List<UnLimit91PornItem> list = box.query()
        .equal(UnLimit91PornItem_.status, FileDownloadStatus.progress).build().find();
    for (UnLimit91PornItem unLimit91PornItem : list) {
      unLimit91PornItem.setStatus(FileDownloadStatus.paused);
      box.put(unLimit91PornItem);
    }
  }

  /**
   * 初始化加载界面，空界面等
   */
  private void initLoadingHelper() {
    LoadViewHelper.getBuilder().setLoadEmpty(R.layout.empty_view).setLoadError(R.layout.error_view)
        .setLoadIng(R.layout.loading_view);
  }

  /**
   * 初始化boxStore库
   */
  private void initBoxStore() {
    boxStore = MyObjectBox.builder().androidContext(MyApplication.this).build();
    if (BuildConfig.DEBUG) {
      new AndroidObjectBrowser(boxStore).start(this);
    }
  }

  /**
   * 获取boxtore
   *
   * @return box
   */
  public BoxStore getBoxStore() {
    return boxStore;
  }

  /**
   * 设置地址
   *
   * @param host
   *          当前可访问地址
   */
  public void setHost(@Nullable String host) {
    this.host = host;
    SPUtils.put(this, Keys.KEY_SP_NOW_ADDRESS, host);
  }

  /**
   * 获取视频缓存代理
   *
   * @return proxy
   */
  public HttpProxyCacheServer getProxy() {
    synchronized (MyApplication.class) {
      if (proxy == null) {
        proxy = newProxy();
      }
    }
    return proxy;
  }

  /**
   * 初始化视频缓存代理
   *
   * @return proxy
   */
  private HttpProxyCacheServer newProxy() {
    return new HttpProxyCacheServer.Builder(this)
    // 1 Gb for cache
        .maxCacheSize(1024 * 1024 * 1024).fileNameGenerator(new MyFileNameGenerator()).build();
  }

  /**
   * 初始化内存分析工具
   */
  private void initLeakCanry() {
    if (LeakCanary.isInAnalyzerProcess(this)) {
      // This process is dedicated to LeakCanary for heap analysis.
      // You should not init your app in this process.
      return;
    }
    LeakCanary.install(this);
    // Normal app init code...
  }

  /**
   * 初始化Retrifit网络请求
   */
  private void initRetrofit() {

    cookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(this));

    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    builder.addInterceptor(new Interceptor() {
      @Override
      public okhttp3.Response intercept(@NonNull Chain chain) throws IOException {
        // 统一设置请求头
        Request original = chain.request();

        Request.Builder requestBuilder = original.newBuilder();
        requestBuilder
            .header(
                "User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/62.0.3202.89 Safari/537.36");
        requestBuilder.header("Accept-Language", "zh-CN,zh;q=0.9,zh-TW;q=0.8");
        // requestBuilder.header("X-Forwarded-For","114.114.114.117")
        requestBuilder.method(original.method(), original.body());
        String host = MyApplication.this.host;
        // 切换服务器地址
        if (!TextUtils.isEmpty(host)) {
          host = host.substring(host.indexOf("//") + 2, host.lastIndexOf("/"));
          Logger.d("host:" + host);
          if (!TextUtils.isEmpty(host)) {
            HttpUrl newUrl = original.url().newBuilder().host(host).build();
            requestBuilder.url(newUrl);
          }
        }

        Request request = requestBuilder.build();
        return chain.proceed(request);
      }
    });
    builder.cookieJar(cookieJar);
    // 设置代理
    // builder.proxy(new Proxy(Proxy.Type.HTTP, new
    // InetSocketAddress("222.66.22.82", 8090)));

    HttpLoggingInterceptor logging = new HttpLoggingInterceptor(
        new HttpLoggingInterceptor.Logger() {
          @Override
          public void log(String message) {
            Logger.d(message);
          }
        });
    logging.setLevel(HttpLoggingInterceptor.Level.HEADERS);
    builder.addInterceptor(logging);
    builder.readTimeout(5, TimeUnit.SECONDS);
    builder.writeTimeout(5, TimeUnit.SECONDS);
    Retrofit retrofit = new Retrofit.Builder().client(builder.build()).baseUrl(Constants.BASE_URL)
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .addConverterFactory(ScalarsConverterFactory.create()).build();

    mNoLimit91PornServiceApi = retrofit.create(NoLimit91PornServiceApi.class);
  }

  /**
   * 初始化缓存
   */
  private void initCache() {
    File cacheDir = getExternalCacheDir();
    cacheProviders = new RxCache.Builder().useExpiredDataIfLoaderNotAvailable(true)
        .persistence(cacheDir, new GsonSpeaker()).using(CacheProviders.class);
  }

  public CacheProviders getCacheProviders() {
    return cacheProviders;
  }

  /**
   * 清除cookies
   */
  public void cleanCookies() {
    if (cookieJar == null) {
      return;
    }
    cookieJar.clear();
    cookieJar.clearSession();
  }

  public static MyApplication getInstace() {
    return mMyApplication;
  }

  public NoLimit91PornServiceApi getNoLimit91PornService() {
    return mNoLimit91PornServiceApi;
  }

  /**
   * 初始化日志工具
   */
  private void initLogger() {
    FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
    // (Optional) Whether to show thread info or not. Default true
        .showThreadInfo(false)
        // (Optional) How many method line to show. Default 2
        .methodCount(0)
        // (Optional) Hides internal method calls up to offset. Default 5
        .methodOffset(7)
        // .logStrategy(customLog) // (Optional) Changes the log strategy to
        // print out. Default LogCat
        // .tag("My custom tag") // (Optional) Global tag for every log. Default
        // PRETTY_LOGGER
        .build();

    Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
      @Override
      public boolean isLoggable(int priority, String tag) {
        return BuildConfig.DEBUG;
      }
    });
  }
}
