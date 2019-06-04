package com.sxt.chat.activity;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.common.GoogleApiAvailabilityLight;
import com.sxt.chat.App;
import com.sxt.chat.R;
import com.sxt.chat.ar.HelloArActivity;
import com.sxt.chat.base.BaseFragment;
import com.sxt.chat.base.TabActivity;
import com.sxt.chat.db.User;
import com.sxt.chat.dialog.AlertDialogBuilder;
import com.sxt.chat.fragment.ChartFragment;
import com.sxt.chat.fragment.GalleryFragment;
import com.sxt.chat.fragment.HomePageFragment;
import com.sxt.chat.fragment.NewsFragment;
import com.sxt.chat.json.ResponseInfo;
import com.sxt.chat.task.MainService;
import com.sxt.chat.utils.AnimationUtil;
import com.sxt.chat.utils.ArithTool;
import com.sxt.chat.utils.Constants;
import com.sxt.chat.utils.Prefs;
import com.sxt.chat.utils.ScreenCaptureUtil;
import com.sxt.chat.utils.glide.GlideCircleTransformer;
import com.sxt.chat.view.FloatButton;
import com.sxt.chat.view.searchview.MaterialSearchView;
import com.sxt.chat.ws.BmobRequest;

import java.util.LinkedHashMap;
import java.util.Map;

import cn.bmob.v3.BmobUser;

public class MainActivity extends TabActivity implements View.OnClickListener {

    private boolean isFirst = true;
    private ImageView userIcon;
    private TextView userInfo, userName;
    private DrawerLayout drawerLayout;
    private BottomNavigationView bottomNavigationView;
    private ActionBarDrawerToggle drawerToggle;
    private View bottomBarLayout;
    private MaterialSearchView searchView;
    private Menu menu;
    private FloatButton floatButton;
    private Handler handler = new Handler();
    private final long millis = /*5 * 60*/ 30 * 1000L;
    private InterstitialAd interstitialAd;
    private RewardedVideoAd rewardedVideoAd;

    private final int REQUEST_CODE_LOCATION_MAP = 201;
    private final int REQUEST_CODE_LOCATION_AD = 202;
    public static String KEY_IS_AUTO_LOGIN = "KEY_IS_AUTO_LOGIN";
    public static final String KEY_IS_WILL_GO_LOGIN_ACTIVITY = "KEY_IS_WILL_GO_LOGIN_ACTIVITY";
    public final String CMD_UPDATE_USER_INFO = this.getClass().getName() + "CMD_UPDATE_USER_INFO";
    final String READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE;
    final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setNavigationBarColor(Color.WHITE);
        }
        if (BmobUser.getCurrentUser(User.class) == null) {
            startActivity(new Intent(App.getCtx(), LoginActivity.class));
            finish();
        } else {
            setContentView(R.layout.activity_main);
            initView();
            initDrawer();
            initFragment();
            startForgroundService();
            initGoogleAds();
        }
    }

    private void startForgroundService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(App.getCtx(), MainService.class));
        } else {
            startService(new Intent(App.getCtx(), MainService.class));
        }
    }

    private void initView() {
        drawerLayout = findViewById(R.id.drawerLayout);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomBarLayout = findViewById(R.id.bottom_bar_layout);
        findViewById(R.id.basic_info).setOnClickListener(this);
        findViewById(R.id.normal_settings).setOnClickListener(this);
        findViewById(R.id.ocr_scan_id_card).setOnClickListener(this);
        findViewById(R.id.ocr_scan).setOnClickListener(this);
        findViewById(R.id.exoplayer).setOnClickListener(this);
        findViewById(R.id.pdf_parse).setOnClickListener(this);
        findViewById(R.id.wifi).setOnClickListener(this);
        findViewById(R.id.notifycation).setOnClickListener(this);
        findViewById(R.id.ar).setOnClickListener(this);
        findViewById(R.id.vr).setOnClickListener(this);
        findViewById(R.id.map).setOnClickListener(this);
        findViewById(R.id.change_login).setOnClickListener(this);
        userIcon = findViewById(R.id.user_icon);
        userInfo = findViewById(R.id.user_info);
        userName = findViewById(R.id.user_name);
        initFloatButton();
    }

    /**
     * 初始化浮动按钮
     */
    private void initFloatButton() {
        if (floatButton == null) {
            floatButton = new FloatButton(this).setOnClickListener(view -> {
                Intent intent = new Intent(this, BasicInfoActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(intent,
                            ActivityOptions.makeSceneTransitionAnimation
                                    (MainActivity.this, floatButton.getItemFloatView(), "shareView").toBundle());
                } else {
                    startActivity(intent);
                }
            });
        }
    }

    private void initFragment() {
        Map<Integer, BaseFragment> fragmentMap = new LinkedHashMap<>();
        fragmentMap.put(R.id.home, new HomePageFragment());
        fragmentMap.put(R.id.gallery, new GalleryFragment());
        fragmentMap.put(R.id.chart, new ChartFragment());
        fragmentMap.put(R.id.ad, new NewsFragment());
        initFragment(fragmentMap, bottomNavigationView, R.id.container, 0);
    }

    private void initDrawer() {
        View searchContainer = getLayoutInflater().inflate(R.layout.toolbar_search, null);
        Toolbar toolbar = searchContainer.findViewById(R.id.toolbar);
        searchView = searchContainer.findViewById(R.id.search_view);
        replaceToolbarView(searchContainer, toolbar);
        drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
//                View content = drawerLayout.getChildAt(0); // 得到contentView
//                int offset = (int) (drawerView.getWidth() * slideOffset);
//                content.setTranslationX(offset);
            }
        };
        drawerToggle.syncState();//实现箭头和三条杠图案切换和侧滑菜单开关的同步
        drawerLayout.addDrawerListener(drawerToggle);

        searchView.setVoiceSearch(false);
        searchView.setEllipsize(false);
        searchView.setBackground(ContextCompat.getDrawable(this, R.drawable.white_solid_round_4));
        searchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //Do some magic
                Snackbar.make(findViewById(R.id.container), "Query: " + query, Snackbar.LENGTH_LONG).show();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });
    }

    /**
     * google ads 初始化
     */
    private void initGoogleAds() {
        MobileAds.initialize(this, getString(R.string.adsense_app_key));
        initGoogleAlertAds();
        initGoogleRewardedAds();
    }

    private void prepareGoogleAds() {
        restartAlertAds();
        restartRewardedVideo();
    }

    /**
     * 初始化google插屏ad
     */
    private void initGoogleAlertAds() {
        if (interstitialAd == null) {
            interstitialAd = new InterstitialAd(this);
            interstitialAd.setAdUnitId(getString(R.string.adsense_app_ad_chaping));
            interstitialAd.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    Log.e(TAG, "插屏广告加载成功");
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    Log.e(TAG, "插屏广告加载失败 error code: " + errorCode);
                    restartAlertAds();
                }

                @Override
                public void onAdClosed() {
                    Log.e(TAG, "插屏广告关闭");
                    restartAlertAds();
                }
            });
        }
    }

    /**
     * 预加载插屏广告
     */
    private void restartAlertAds() {
        initGoogleAlertAds();
        if (!interstitialAd.isLoading() && !interstitialAd.isLoaded()) {
            AdRequest adRequest = new AdRequest.Builder().build();
            interstitialAd.loadAd(adRequest);
            Log.e(TAG, "加载下一个插屏广告");
        }
    }

    /**
     * 广告加载完成后，显示出来,然后预加载下一条广告
     */
    private void showAlertAds() {
        if (interstitialAd != null && interstitialAd.isLoaded()) {
            interstitialAd.show();
            Log.e(TAG, "显示插屏广告");
        }
    }

    /**
     * 初始化google激励广告
     */
    private void initGoogleRewardedAds() {
        if (rewardedVideoAd == null) {
            rewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
            rewardedVideoAd.setRewardedVideoAdListener(new RewardedVideoAdListener() {
                @Override
                public void onRewardedVideoAdLoaded() {
                    Log.e(TAG, "onRewardedVideoAdLoaded : 激励广告加载完成");
                }

                @Override
                public void onRewardedVideoAdOpened() {
                    Log.e(TAG, "onRewardedVideoAdOpened : 激励广告被点开");
                }

                @Override
                public void onRewardedVideoStarted() {
                    Log.e(TAG, "onRewardedVideoAdLeftApplication : 激励广告加载开始");
                }

                @Override
                public void onRewardedVideoAdClosed() {
                    Log.e(TAG, "onRewardedVideoAdClosed : 激励广告被关闭");
                    restartRewardedVideo();
                }

                @Override
                public void onRewarded(RewardItem rewardItem) {
                    Log.e(TAG, "onRewarded : 激励广告获得奖励");
                }

                @Override
                public void onRewardedVideoAdLeftApplication() {
                    Log.e(TAG, "onRewardedVideoAdLeftApplication");
                }

                @Override
                public void onRewardedVideoAdFailedToLoad(int i) {
                    Log.e(TAG, "onRewardedVideoAdFailedToLoad  : 激励广告加载失败 i = " + i);
                }

                @Override
                public void onRewardedVideoCompleted() {
                    Log.e(TAG, "onRewardedVideoCompleted : 激励广告加载播放完成");
                    restartRewardedVideo();
                }
            });
        }
    }

    /**
     * 视频广告加载完成后，显示出来，然后预加载下一个激励广告
     */
    private void showRewardedAds() {
        if (rewardedVideoAd != null && rewardedVideoAd.isLoaded()) {
            rewardedVideoAd.show();
            Log.e(TAG, "显示激励广告");
        } else {
            showAlertAds();
        }
        restartRewardedVideo();
    }

    /**
     * 准备加载激励广告
     */
    private void restartRewardedVideo() {
        initGoogleRewardedAds();
        if (!rewardedVideoAd.isLoaded()) {
            rewardedVideoAd.loadAd(getString(R.string.adsense_app_ad_video),
                    new AdRequest.Builder().build());
            Log.e(TAG, "加载下一个激励广告");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUser();
        if (rewardedVideoAd != null) {
            rewardedVideoAd.resume(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (rewardedVideoAd != null) {
            rewardedVideoAd.pause(this);
        }
    }

    private void checkUser() {
        BmobRequest.getInstance(this).updateUserInfo(CMD_UPDATE_USER_INFO);
    }

    @Override
    public void onMessage(ResponseInfo resp) {
        if (ResponseInfo.OK == resp.getCode()) {
            if (CMD_UPDATE_USER_INFO.equals(resp.getCmd())) {
                updateUserInfo(BmobUser.getCurrentUser(User.class));
                loadAD();
            }
        } else {
            if (CMD_UPDATE_USER_INFO.equals(resp.getCmd())) {
                startActivity(new Intent(App.getCtx(), LoginActivity.class));
            } else {
                Toast(resp.getError());
            }
        }
    }

    /**
     * 加载广告
     * 因为腾讯的广告需要读取手机状态的权限，真坑，如果用户选择了拒绝并且不再提醒的话，
     * 就加载google的广告，google的广告不需要任何用户权限
     * <p>
     * 但是google的激励广告需要定位权限而且需要手机安装google play store
     */
    private void loadAD() {
        if (isFirst) {
            isFirst = false;
            prepareGoogleAds();
            return;
        }
        long lastMillis = Prefs.getInstance(this).getLong(Prefs.KEY_LAST_RESUME_MILLIS, 0);
        if (System.currentTimeMillis() - lastMillis > millis) {
            Prefs.getInstance(this).putLong(Prefs.KEY_LAST_RESUME_MILLIS, System.currentTimeMillis());
            //如果上次可视的时间距离现在短于2分钟,就去赚取广告费 嘎嘎嘎嘎
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                boolean readPhoneState = shouldShowRequestPermissionRationale(READ_PHONE_STATE);
                boolean writeStorage = shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE);
                boolean has0 = ActivityCompat.checkSelfPermission(this, READ_PHONE_STATE) ==
                        PackageManager.PERMISSION_GRANTED;
                boolean has1 = ActivityCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED;

                Log.e(TAG, String.format("has0 %s , has01 %s", has0, has1));

                if ((!has0 && !readPhoneState) || (!has1 && !writeStorage)) {
                    //验证手机的google play service是否可用
                    int playServicesAvailable = GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(this);
                    Log.e(TAG, String.format("playServicesAvailable - > %s", playServicesAvailable));
                    if (playServicesAvailable == 0) {
                        //只有在获取到位置权限后才能加载激励广告
                        boolean permission = requestLocationPermission(REQUEST_CODE_LOCATION_AD);
                        if (permission) {
                            showRewardedAds();
                        }
                    } else {
                        showAlertAds();
                    }
                } else {
                    openSplashActivity();
                }
            } else {
                openSplashActivity();
            }
        }
    }

    private void openSplashActivity() {
        Intent intent = new Intent(App.getCtx(), SplashActivity.class);
        intent.putExtra(MainActivity.KEY_IS_WILL_GO_LOGIN_ACTIVITY, false);
        startActivity(intent);
    }

    /**
     * 更新用户信息
     */
    private void updateUserInfo(User user) {
        if (user != null) {
            userName.setText(user.getName());
            String info;
            if (0 != (user.getAge() == null ? 0 : user.getAge())) {
                if ("F".equals(user.getGender())) {
                    info = user.getAge() + "岁" + "  女  ";
                } else {
                    info = user.getAge() + "岁" + "  男  ";
                }
            } else {
                if ("F".equals(user.getGender())) {
                    info = "女  ";
                } else {
                    info = "男  ";
                }
            }
            double pow = Math.pow((user.getHeight() == null ? 0 : user.getHeight()), 2);
            if (pow > 0) {
                info = info + " BMI:" + (ArithTool.div((user.getWeight() == null ? 0 : user.getWeight()) * 10000, pow, 1));
            }
            userInfo.setText(info);
            loadHeader(user.getImgUri(), user.getGender());
            Prefs.getInstance(this).putString(Prefs.KEY_USER_GENDER, user.getGender());
        }
    }

    /**
     * 显示头像
     */
    private void loadHeader(String url, String gender) {
        Glide.with(this).load(url)
                .error("M".equals(gender) ? R.mipmap.men : R.mipmap.female)
                .bitmapTransform(new GlideCircleTransformer(this))
                .signature(new StringSignature(Prefs.getInstance(App.getCtx()).getString(Prefs.KEY_USER_HEADER_IMAGE_FLAG, "")))
                .into(userIcon);
    }

    /**
     * 截屏
     */
    private void screenCapture() {
        if (menu == null) {
            return;
        }
        menu.findItem(R.id.action_more).setEnabled(false);
        ScreenCaptureUtil.getInstance(this)
                .capture(this.getWindow().getDecorView())
                .setOnScreenCaptureListener(new ScreenCaptureUtil.OnScreenCaptureListener() {
                    @Override
                    public void onCaptureSuccessed(final String path) {
                        final FrameLayout decorView = (FrameLayout) MainActivity.this.getWindow().getDecorView();
                        int[] decorViewLocation = new int[2];
                        decorView.getLocationOnScreen(decorViewLocation);
                        final ImageView imageView = new ImageView(MainActivity.this);
                        Bitmap bitmap = ScreenCaptureUtil.getInstance(MainActivity.this).getBitmapFromMemory(path);
                        if (bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                            imageView.setX(decorViewLocation[0]);
                            imageView.setY(decorViewLocation[1]);
                            decorView.addView(imageView);
                            AnimationUtil.fadeInScaleView(MainActivity.this, imageView, 400, new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                }
                            });
                            imageView.setOnClickListener(v -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    imageView.setTransitionName("shareView");
                                }
                                Intent intent = new Intent(MainActivity.this, ShareCaptureActivity.class);
                                intent.putExtra(Prefs.KEY_BITMAP, path);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    startActivity(intent,
                                            ActivityOptions.makeSceneTransitionAnimation
                                                    (MainActivity.this, imageView, "shareView").toBundle());
                                } else {
                                    startActivity(intent);
                                }
                                handler.postDelayed(() -> {
                                    decorView.removeView(imageView);
                                    menu.findItem(R.id.action_more).setEnabled(true);
                                }, 500);
                            });
                            Log.e("capture.mp3", String.format("截屏成功 path : %s", path));
                        }
                    }

                    @Override
                    public void onCaptureFailed() {
                        Log.e("capture.mp3", "截屏失败");
                    }
                });
    }

    /**
     * 分享
     */
    private void share() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, Constants.SHARE_CONTENT);
        intent.setType("text/plain");
        startActivity(Intent.createChooser(intent, getString(R.string.share_with)));
    }

    private boolean isBottom2Top;//临时记录上次的滑动状态
    private ObjectAnimator translationAnimator;

    public void setBottomBarTranslateY(float scrollY, boolean isBottom2Top) {
        if (this.isBottom2Top == isBottom2Top) return;//防止不停的动画
        this.isBottom2Top = isBottom2Top;
        int measuredHeight = bottomBarLayout.getMeasuredHeight();
        translationYAnimator(bottomBarLayout, (int) bottomBarLayout.getTranslationY(), isBottom2Top ? measuredHeight : 0);
    }

    private void translationYAnimator(View target, int startTranslationY, int endTranslationY) {
        if (translationAnimator != null) {
            translationAnimator.cancel();
        }
        translationAnimator = ObjectAnimator.ofFloat(target, "translationY", startTranslationY, endTranslationY).setDuration(300);
        translationAnimator.setInterpolator(new LinearInterpolator());
        translationAnimator.start();
    }

    @Override
    protected void onTabCheckedChange(MenuItem menuItem) {
        super.onTabCheckedChange(menuItem);
        if (floatButton != null) {
            floatButton.setVisibility(menuItem.getItemId() == R.id.home ? View.VISIBLE : View.GONE);
        }
        setToolbarTitle(menuItem.getTitle());
        if (menu != null) {
            if (menuItem.getItemId() == R.id.home) {
                menu.findItem(R.id.action_search).setVisible(true);
            } else {
                menu.findItem(R.id.action_search).setVisible(false);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.item_search_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        if (searchView != null) {
            searchView.setMenuItem(item);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                share();
                break;
            case R.id.action_more:
                screenCapture();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onGoBack(View view) {
        moveTaskToBack(true);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (searchView != null && searchView.isSearchOpen()) {
                searchView.closeSearch();
                return false;
            }
            if (drawerLayout != null && drawerLayout.isDrawerOpen(Gravity.START)) {
                drawerLayout.closeDrawers();
                return false;
            }
            long preMillis = Prefs.getInstance(App.getCtx()).getLong(Prefs.KEY_EXIT_ACTIVITY, 0);
            if (System.currentTimeMillis() - preMillis > 2000) {
                Prefs.getInstance(App.getCtx()).putLong(Prefs.KEY_EXIT_ACTIVITY, System.currentTimeMillis());
                Toast(getString(R.string.exit_press_again));
            } else {
                Prefs.getInstance(App.getCtx()).putLong(Prefs.KEY_EXIT_ACTIVITY, 0);
                onBackPressed();
            }
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        onGoBack(null);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.basic_info:
                startActivity(new Intent(this, BasicInfoActivity.class));
                break;
            case R.id.normal_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.change_login:
                startActivity(new Intent(this, ChangeLoginActivity.class));
                break;
            case R.id.ocr_scan:
                startActivity(new Intent(this, YouTuActivity.class));
                break;
            case R.id.ocr_scan_id_card:
                startActivity(new Intent(this, YouTuIdCardActivity.class));
                break;
            case R.id.wifi:
                startActivity(new Intent(this, WiFiSettingsActivity.class));
                break;
            case R.id.exoplayer:
                startActivity(new Intent(this, VideoExoPlayerActivity.class));
                break;
            case R.id.pdf_parse:
                startActivity(new Intent(this, PdfActivity.class));
                break;
            case R.id.notifycation:
                startActivity(new Intent(this, NotificationActivity.class));
                break;
            case R.id.ar:
                startActivity(new Intent(this, HelloArActivity.class));
                break;
            case R.id.vr:
                startActivity(new Intent(this, VR360Activity.class));
                break;
            case R.id.map:
                if (requestLocationPermission(REQUEST_CODE_LOCATION_MAP)) {
                    openMapActivity();
                }
                break;
        }
    }

    /**
     * 请求位置权限
     *
     * @param requestCode
     */
    private boolean requestLocationPermission(int requestCode) {
        return checkPermission(requestCode, Manifest.permission_group.LOCATION, new String[]{
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,});
    }

    @Override
    public void onPermissionsAllowed(int requestCode, String[] permissions, int[] grantResults) {
        super.onPermissionsAllowed(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_MAP) {
            openMapActivity();
        } else if (requestCode == REQUEST_CODE_LOCATION_AD) {
            showRewardedAds();//显示激励广告
        }
    }

    @Override
    public void onPermissionsRefused(int requestCode, String[] permissions, int[] grantResults) {
        super.onPermissionsRefused(requestCode, permissions, grantResults);
        //当位置权限被拒绝后，加载插屏广告
        if (requestCode == REQUEST_CODE_LOCATION_AD) {
            showAlertAds();
        }
    }

    @Override
    public void onPermissionsRefusedNever(int requestCode, String[] permissions, int[] grantResults) {
        super.onPermissionsRefusedNever(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_LOCATION_MAP) {
            String appName = getString(R.string.app_name);
            String message = String.format(getString(R.string.permission_request_LOCATION), appName);
            SpannableString span = new SpannableString(message);
            span.setSpan(new TextAppearanceSpan(this, R.style.text_15_color_2_style), 0, message.indexOf(appName), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            int start = message.indexOf(appName) + appName.length();
            span.setSpan(new TextAppearanceSpan(this, R.style.text_15_color_black_bold_style), message.indexOf(appName), start, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            span.setSpan(new TextAppearanceSpan(this, R.style.text_15_color_2_style), start, message.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            showPermissionRefusedNeverDialog(span);
        } else if (requestCode == REQUEST_CODE_LOCATION_AD) {
            showAlertAds();//当位置权限被拒绝后，加载插屏广告
        }
    }

    private void openMapActivity() {
        startActivity(new Intent(this, MapActivity.class));
    }

    /**
     * 权限被彻底禁止后 , 弹框提醒用户去开启
     */
    private void showPermissionRefusedNeverDialog(CharSequence message) {
        new AlertDialogBuilder(this)
                .setTitle(R.string.message_alert, true)
                .setMessage(message)
                .setLeftButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setRightButton(R.string.confirm, (dialog, which) -> {
                    dialog.dismiss();
                    goToAppSettingsPage();
                })
                .setShowLine(true)
                .setCanceledOnTouchOutside(false)
                .show();
    }

    @Override
    protected void onDestroy() {
        if (floatButton != null) {
            floatButton.onDestroy();
        }
        if (rewardedVideoAd != null) {
            rewardedVideoAd.destroy(this);
        }
        super.onDestroy();
    }
}
