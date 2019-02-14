package com.u91porn.ui.common;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import com.aitsuki.swipe.SwipeItemLayout;
import com.aitsuki.swipe.SwipeMenuRecyclerView;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.helper.loadviewhelper.help.OnLoadViewListener;
import com.helper.loadviewhelper.load.LoadViewHelper;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.u91porn.R;
import com.u91porn.adapter.UnLimit91Adapter;
import com.u91porn.data.model.UnLimit91PornItem;
import com.u91porn.ui.MvpFragment;
import com.u91porn.ui.main.MainActivity;
import com.u91porn.ui.play.PlayVideoActivity;
import com.u91porn.utils.Keys;

import io.rx_cache2.Reply;

/**
 * 通用 A simple {@link Fragment} subclass.
 */
public class CommonFragment extends MvpFragment<CommonView, CommonPresenter> implements CommonView,
    SwipeRefreshLayout.OnRefreshListener {

  @BindView(R.id.recyclerView_common)
  SwipeMenuRecyclerView recyclerView;
  Unbinder unbinder;
  @BindView(R.id.contentView)
  SwipeRefreshLayout contentView;

  private UnLimit91Adapter mUnLimit91Adapter;
  private String category;
  private String m;
  /**
   * 上次是否强制刷新，则加载更多也强制一起刷新
   */
  private boolean pullToRefresh = false;
  private LoadViewHelper helper;

  public CommonFragment() {
    // Required empty public constructor
  }

  public static CommonFragment getInstance(String category, String m) {
    CommonFragment commonFragment = new CommonFragment();
    Bundle args = new Bundle();
    args.putString("category", category);
    args.putString("m", m);
    commonFragment.setArguments(args);
    return commonFragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    category = getArguments().getString("category");
    m = getArguments().getString("m");
  }

  @NonNull
  @Override
  public CommonPresenter createPresenter() {
    return new CommonPresenter(category);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    // Inflate the layout for this fragment
    return inflater.inflate(R.layout.fragment_common, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    unbinder = ButterKnife.bind(this, view);

    // Setup contentView == SwipeRefreshView
    contentView.setOnRefreshListener(this);

    ArrayList<UnLimit91PornItem> mUnLimit91PornItemList = new ArrayList<>();
    mUnLimit91Adapter = new UnLimit91Adapter(R.layout.item_right_menu_favorite,
        mUnLimit91PornItemList);
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerView.setAdapter(mUnLimit91Adapter);
    mUnLimit91Adapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
      @Override
      public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        String hd = "hd";
        if (hd.equals(category)) {
          showMessage("非会员，无法观看高清视频！");
          return;
        }
        UnLimit91PornItem unLimit91PornItems = (UnLimit91PornItem) adapter.getData().get(position);
        goToPlayVideo(unLimit91PornItems);
      }
    });
    mUnLimit91Adapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
      @Override
      public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        SwipeItemLayout swipeItemLayout = (SwipeItemLayout) view.getParent();
        swipeItemLayout.close();
        if (view.getId() == R.id.right_menu_favorite) {
          presenter.favorite((UnLimit91PornItem) adapter.getItem(position));
        }

      }
    });
    mUnLimit91Adapter.setOnLoadMoreListener(new BaseQuickAdapter.RequestLoadMoreListener() {
      @Override
      public void onLoadMoreRequested() {
        presenter.loadHotData(pullToRefresh, m);
      }
    }, recyclerView);
    helper = new LoadViewHelper(recyclerView);
    helper.setListener(new OnLoadViewListener() {
      @Override
      public void onRetryClick() {
        loadData(false);
      }
    });
    loadData(false);
  }

  private void goToPlayVideo(UnLimit91PornItem unLimit91PornItem) {
    Intent intent = new Intent(getContext(), PlayVideoActivity.class);
    intent.putExtra(Keys.KEY_INTENT_UNLIMIT91PORNITEM, unLimit91PornItem);
    ((MainActivity) getActivity()).startActivityWithAnimotion(intent);
  }

  @Override
  public String getErrorMessage(Throwable e, boolean pullToRefresh) {
    return getString(R.string.load_failed);
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
    unbinder.unbind();
  }

  @Override
  public void setData(List<UnLimit91PornItem> data) {
    mUnLimit91Adapter.setNewData(data);
  }

  @Override
  public void showLoading(boolean pullToRefresh) {
    helper.showLoading();
  }

  @Override
  public void loadData(boolean pullToRefresh) {
    presenter.loadHotData(pullToRefresh, m);
  }

  @Override
  public void onRefresh() {
    pullToRefresh = true;
    loadData(true);
  }

  @Override
  public void showContent() {
    helper.showContent();
    contentView.setRefreshing(false);
  }

  @Override
  public void showMessage(String msg) {
    super.showMessage(msg);
  }

  @Override
  public LifecycleTransformer<Reply<String>> bindView() {
    return bindToLifecycle();
  }

  @Override
  public void showError(Throwable e, boolean pullToRefresh) {
    contentView.setRefreshing(false);
    helper.showError();
    showMessage(e.getMessage());
    e.printStackTrace();
  }

  @Override
  public void loadMoreDataComplete() {
    mUnLimit91Adapter.loadMoreComplete();
  }

  @Override
  public void loadMoreFailed() {
    showMessage("加载更多失败");
    mUnLimit91Adapter.loadMoreFail();
  }

  @Override
  public void noMoreData() {
    mUnLimit91Adapter.loadMoreEnd(true);
    showMessage("没有更多数据了");
  }

  @Override
  public void setMoreData(List<UnLimit91PornItem> unLimit91PornItemList) {
    mUnLimit91Adapter.addData(unLimit91PornItemList);
  }
}
