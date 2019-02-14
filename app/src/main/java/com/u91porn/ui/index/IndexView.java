package com.u91porn.ui.index;

import java.util.List;

import com.u91porn.data.model.UnLimit91PornItem;
import com.u91porn.ui.BaseView;

/**
 * @author flymegoc
 * @date 2017/11/15
 * @describe
 */

public interface IndexView extends BaseView {

  void loadData(boolean pullToRefresh);

  void setData(List<UnLimit91PornItem> data);
}
