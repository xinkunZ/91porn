package com.u91porn.ui.download;

import java.util.List;

import com.u91porn.data.model.UnLimit91PornItem;
import com.u91porn.ui.BaseView;

/**
 * @author flymegoc
 * @date 2017/11/27
 * @describe
 */

public interface DownloadView extends BaseView {
  void setDownloadingData(List<UnLimit91PornItem> unLimit91PornItems);

  void setFinishedData(List<UnLimit91PornItem> unLimit91PornItems);
}
