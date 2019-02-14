package com.u91porn.ui.favorite;

import java.util.List;

import com.u91porn.data.model.UnLimit91PornItem;
import com.u91porn.ui.BaseView;

/**
 * @author flymegoc
 * @date 2017/11/25
 * @describe
 */

public interface FavoriteView extends BaseView {
  void setFavoriteData(List<UnLimit91PornItem> unLimit91PornItemList);

  void deleteFavoriteSucc(int position);

  void noLoadMoreData();
}
