package com.u91porn.ui.main;

import java.io.File;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.design.widget.NavigationView.OnNavigationItemSelectedListener;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import butterknife.BindView;
import butterknife.ButterKnife;

import com.u91porn.MyApplication;
import com.u91porn.R;
import com.u91porn.ui.BaseAppCompatActivity;
import com.u91porn.ui.common.CommonFragment;
import com.u91porn.ui.download.DownloadActivity;
import com.u91porn.ui.favorite.FavoriteActivity;
import com.u91porn.ui.index.IndexFragment;
import com.u91porn.utils.Constants;
import com.u91porn.utils.Keys;
import com.u91porn.utils.SPUtils;
import com.yanzhenjie.permission.AndPermission;

/**
 * @author flymegoc
 */
public class MainActivity extends BaseAppCompatActivity implements OnNavigationItemSelectedListener {

  @BindView(R.id.toolbar)
  Toolbar toolbar;
  @BindView(R.id.drawer_layout)
  DrawerLayout drawerLayout;
  @BindView(R.id.nav_view)
  NavigationView navView;

  private Fragment mCurrentFragment;
  private IndexFragment indexFragment;
  private CommonFragment commonFragment;
  private CommonFragment rpFragment;
  private CommonFragment tenMinutesFragment;
  private CommonFragment thisMonthFragment;
  private CommonFragment thisMonthCollectFragment;
  private CommonFragment mostCollectFragment;
  private CommonFragment nearScoreFragment;
  private CommonFragment thisMonthHotFragment;
  private CommonFragment lastMonthHotFragment;
  private CommonFragment hdVideoFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    AppCompatDelegate.setDefaultNightMode((int) SPUtils.get(this, Keys.KEY_THEME_DARK,
        AppCompatDelegate.MODE_NIGHT_NO));
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);
    File file = new File(Constants.DOWNLOAD_PATH);
    if (!file.exists()) {
      file.mkdirs();
    }
    AndPermission
        .with(this)
        .requestCode(300)
        .permission(Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE).start();
    setSupportActionBar(toolbar);

    ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
        R.string.navigation_drawer_open, R.string.navigation_drawer_close);
    drawerLayout.addDrawerListener(toggle);
    toggle.syncState();

    navView.setNavigationItemSelectedListener(this);

    mCurrentFragment = new Fragment();
    indexFragment = IndexFragment.getInstance();
    getSupportFragmentManager().beginTransaction().add(R.id.content, indexFragment).commit();
    mCurrentFragment = indexFragment;
  }

  @Override
  public void onBackPressed() {
    if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
      drawerLayout.closeDrawer(GravityCompat.START);
    } else {
      super.onBackPressed();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();

    // noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      showSettingDialog();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void showSettingDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("访问地址设置");
    final View view = LayoutInflater.from(this).inflate(R.layout.setting_dialog_layout, null);
    final RadioGroup radioGroup = view.findViewById(R.id.rg_address);
    RadioButton naverRadioButton = view.findViewById(R.id.rb_never_go_address);
    RadioButton willGoRadioButton = view.findViewById(R.id.rb_will_go_someday);
    RadioButton customRadioButton = view.findViewById(R.id.rb_now_custom_adress);
    final Switch darkModeSwitch = view.findViewById(R.id.switch_dark);
    boolean checked = ((int) SPUtils
        .get(this, Keys.KEY_THEME_DARK, AppCompatDelegate.MODE_NIGHT_NO)) == AppCompatDelegate.MODE_NIGHT_YES;
    darkModeSwitch.setChecked(checked);
    final EditText editText = view.findViewById(R.id.et_custom_ip_address);
    final String customAddress = (String) SPUtils.get(MainActivity.this,
        Keys.KEY_SP_CUSTOM_ADDRESS, "");
    String nowAddress = (String) SPUtils.get(this, Keys.KEY_SP_NOW_ADDRESS, "");
    if (TextUtils.isEmpty(customAddress)) {
      customRadioButton.setVisibility(View.GONE);
    } else {
      customRadioButton.setText(customAddress + "(当前自定义地址)");
    }
    if (!TextUtils.isEmpty(nowAddress)) {
      if (nowAddress.equals(Constants.NEVER_GO_ADDRESS)) {
        naverRadioButton.setChecked(true);
      } else if (nowAddress.equals(Constants.BASE_URL)) {
        willGoRadioButton.setChecked(true);
      } else if (nowAddress.equals(customAddress)) {
        customRadioButton.setVisibility(View.VISIBLE);
        customRadioButton.setText(customAddress + "(当前自定义地址)");
        customRadioButton.setChecked(true);
      }
    }
    builder.setView(view);
    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
        String customAddress = editText.getText().toString().trim();
        // 优先填入的自定义地址
        if (!TextUtils.isEmpty(customAddress)) {
          // 简单验证地址是否合法
          if (customAddress.contains("http://") && customAddress.endsWith("/")) {
            MyApplication.getInstace().setHost(customAddress);
            SPUtils.put(MainActivity.this, Keys.KEY_SP_CUSTOM_ADDRESS, customAddress);
          } else {
            showMessage("设置失败，输入地址格式不正确");
          }

        } else {
          switch (radioGroup.getCheckedRadioButtonId()) {
            case R.id.rb_never_go_address:
              MyApplication.getInstace().setHost(Constants.NEVER_GO_ADDRESS);
              break;
            case R.id.rb_will_go_someday:
              MyApplication.getInstace().setHost(Constants.BASE_URL);
              break;
            case R.id.rb_now_custom_adress:
              MyApplication.getInstace().setHost(customAddress);
              break;
            default:
          }
        }
        if (darkModeSwitch.isChecked() != checked) {
          SPUtils.put(MainActivity.this, Keys.KEY_THEME_DARK,
              darkModeSwitch.isChecked() ? AppCompatDelegate.MODE_NIGHT_YES
                  : AppCompatDelegate.MODE_NIGHT_NO);
          recreate();
        }
      }
    });
    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        dialog.dismiss();
      }
    });
    builder.show();
  }

  @SuppressWarnings("StatementWithEmptyBody")
  @Override
  public boolean onNavigationItemSelected(MenuItem item) {
    // Handle navigation view item clicks here.
    int id = item.getItemId();
    boolean needCloseMenu = true;
    if (id == R.id.nav_index) {
      // Handle the camera action
      if (mCurrentFragment != indexFragment) {
        if (indexFragment == null) {
          indexFragment = IndexFragment.getInstance();
        }
        switchContent(mCurrentFragment, indexFragment);
        setTitle(R.string.index);
      }
    } else if (id == R.id.nav_hot) {
      if (mCurrentFragment != commonFragment) {
        if (commonFragment == null) {
          commonFragment = CommonFragment.getInstance("hot", null);
        }
        switchContent(mCurrentFragment, commonFragment);
        setTitle(R.string.hot);
      }
    } else if (id == R.id.nav_rp) {
      if (mCurrentFragment != rpFragment) {
        if (rpFragment == null) {
          rpFragment = CommonFragment.getInstance("rp", null);
        }
        switchContent(mCurrentFragment, rpFragment);
        setTitle(R.string.near_rp);
      }
    } else if (id == R.id.nav_ten_minutes) {
      if (mCurrentFragment != tenMinutesFragment) {
        if (tenMinutesFragment == null) {
          tenMinutesFragment = CommonFragment.getInstance("long", null);
        }
        switchContent(mCurrentFragment, tenMinutesFragment);
        setTitle(R.string.ten_minutes);
      }
    } else if (id == R.id.nav_this_months) {
      if (mCurrentFragment != thisMonthFragment) {
        if (thisMonthFragment == null) {
          thisMonthFragment = CommonFragment.getInstance("md", null);
        }
        switchContent(mCurrentFragment, thisMonthFragment);
        setTitle(R.string.this_month);
      }

    } else if (id == R.id.nav_this_months_collect) {
      if (mCurrentFragment != thisMonthCollectFragment) {
        if (thisMonthCollectFragment == null) {
          thisMonthCollectFragment = CommonFragment.getInstance("tf", null);
        }
        switchContent(mCurrentFragment, thisMonthCollectFragment);
        setTitle(R.string.this_month_collect);
      }
    } else if (id == R.id.nav_most_collect) {
      if (mCurrentFragment != mostCollectFragment) {
        if (mostCollectFragment == null) {
          mostCollectFragment = CommonFragment.getInstance("mf", null);
        }
        switchContent(mCurrentFragment, mostCollectFragment);
        setTitle(R.string.most_collect);
      }
    } else if (id == R.id.nav_near_score) {
      if (mCurrentFragment != nearScoreFragment) {
        if (nearScoreFragment == null) {
          nearScoreFragment = CommonFragment.getInstance("rf", null);
        }
        switchContent(mCurrentFragment, nearScoreFragment);
        setTitle(R.string.near_score);
      }
    } else if (id == R.id.nav_this_months_hot) {
      if (mCurrentFragment != thisMonthHotFragment) {
        if (thisMonthHotFragment == null) {
          thisMonthHotFragment = CommonFragment.getInstance("top", null);
        }
        switchContent(mCurrentFragment, thisMonthHotFragment);
        setTitle(R.string.this_month_hot);
      }
    } else if (id == R.id.nav_last_months_hot) {
      if (mCurrentFragment != lastMonthHotFragment) {
        if (lastMonthHotFragment == null) {
          lastMonthHotFragment = CommonFragment.getInstance("top", "-1");
        }
        switchContent(mCurrentFragment, lastMonthHotFragment);
        setTitle(R.string.last_month_hot);
      }
    } else if (id == R.id.nav_hd_video) {
      if (mCurrentFragment != hdVideoFragment) {
        if (hdVideoFragment == null) {
          hdVideoFragment = CommonFragment.getInstance("hd", null);
        }
        switchContent(mCurrentFragment, hdVideoFragment);
        setTitle(R.string.hd_video);
      }
    } else if (id == R.id.nav_my_download) {
      Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
      startActivityWithAnimotion(intent);
      needCloseMenu = false;
    } else if (id == R.id.nav_my_collect) {
      Intent intent = new Intent(MainActivity.this, FavoriteActivity.class);
      startActivityWithAnimotion(intent);
      needCloseMenu = false;
    } else if (id == R.id.nav_about) {
      showAboutMeDialog();
    }
    if (needCloseMenu) {
      drawerLayout.closeDrawer(GravityCompat.START);
    }
    return true;
  }

  private void showAboutMeDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.about_me);
    builder.setView(R.layout.about_me);
    builder.show();
  }

  // 切换类型
  public void switchContent(Fragment toHide, Fragment toShow) {
    if (mCurrentFragment != toShow) {
      mCurrentFragment = toShow;
      FragmentTransaction transaction = getSupportFragmentManager().beginTransaction()
          .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
      // 先判断是否被add过
      if (!toShow.isAdded()) {
        // 隐藏当前的fragment，add下一个到Activity中
        transaction.hide(toHide).add(R.id.content, toShow).commit();
      } else {
        // 隐藏当前的fragment，显示下一个
        transaction.hide(toHide).show(toShow).commit();
      }
    }
  }

}
