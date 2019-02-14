package com.u91porn.ui.common;

import java.util.List;

import com.u91porn.data.model.UnLimit91PornItem;
import com.u91porn.ui.BaseView;

/**
 * @author flymegoc
 * @date 2017/11/16
 * @describe
 */

public interface CommonView extends BaseView {
  void loadMoreDataComplete();

  void loadMoreFailed();

  void noMoreData();

  void setMoreData(List<UnLimit91PornItem> unLimit91PornItemList);

  void loadData(boolean pullToRefresh);

  void setData(List<UnLimit91PornItem> data);

}
