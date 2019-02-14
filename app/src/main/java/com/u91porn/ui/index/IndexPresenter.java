package com.u91porn.ui.index;

import java.util.List;

import com.hannesdorfmann.mosby3.mvp.MvpBasePresenter;
import com.orhanobut.logger.Logger;
import com.u91porn.MyApplication;
import com.u91porn.data.NoLimit91PornServiceApi;
import com.u91porn.data.cache.CacheProviders;
import com.u91porn.data.model.UnLimit91PornItem;
import com.u91porn.ui.favorite.FavoritePresenter;
import com.u91porn.utils.CallBackWrapper;
import com.u91porn.utils.ParseUtils;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.rx_cache2.EvictProvider;

/**
 * @author flymegoc
 * @date 2017/11/15
 * @describe
 */

public class IndexPresenter extends MvpBasePresenter<IndexView> implements IIndex {
  private NoLimit91PornServiceApi mNoLimit91PornServiceApi = MyApplication.getInstace()
      .getNoLimit91PornService();
  private CacheProviders cacheProviders = MyApplication.getInstace().getCacheProviders();
  private FavoritePresenter favoritePresenter;

  /**
   * 加载首页视频数据
   *
   * @param pullToRefresh
   *          是否刷新
   */
  @Override
  public void loadIndexData(final boolean pullToRefresh) {
    Observable<String> indexPhpObservable = mNoLimit91PornServiceApi.indexPhp();
    cacheProviders.getIndexPhp(indexPhpObservable, new EvictProvider(pullToRefresh))
        .compose(getView().bindView()).map(responseBodyReply -> {
          switch (responseBodyReply.getSource()) {
            case CLOUD:
              Logger.d("数据来自：网络");
              break;
            case MEMORY:
              Logger.d("数据来自：内存");
              break;
            case PERSISTENCE:
              Logger.d("数据来自：磁盘缓存");
              break;
            default:
              break;
          }
          return responseBodyReply.getData();
        }).map(ParseUtils::parseIndex).subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new CallBackWrapper<List<UnLimit91PornItem>>() {
          @Override
          public void onBegin(Disposable d) {
            if (isViewAttached() && !pullToRefresh) {
              getView().showLoading(pullToRefresh);
            }
          }

          @Override
          public void onSuccess(List<UnLimit91PornItem> unLimit91PornItems) {
            if (isViewAttached()) {
              getView().setData(unLimit91PornItems);
              getView().showContent();
            }
          }

          @Override
          public void onError(String msg, int code) {
            if (isViewAttached()) {
              getView().showError(new Throwable(msg), false);
            }
          }
        });
  }

  // List<UnLimit91PornItem> datas = new ArrayList<>();
  // for (int i = 0; i < 50; i++) {
  // UnLimit91PornItem item = new UnLimit91PornItem();
  // item.setInfo("随机数据 info" + i);
  // item.setTitle("随机数据 title" + i);
  // item.setImgUrl("https://source.unsplash.com/user/erondu/100x100");
  // datas.add(item);
  // }
  // return datas;

  @Override
  public void favorite(UnLimit91PornItem unLimit91PornItem) {
    if (favoritePresenter == null) {
      favoritePresenter = new FavoritePresenter();
    }
    favoritePresenter.favorite(unLimit91PornItem, new FavoritePresenter.FavoriteListener() {
      @Override
      public void onSuccess(String message) {
        if (isViewAttached()) {
          getView().showMessage(message);
        }
      }

      @Override
      public void onError(String message) {
        if (isViewAttached()) {
          getView().showMessage(message);
        }
      }
    });
  }
}
