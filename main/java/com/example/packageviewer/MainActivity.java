package com.example.packageviewer;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager.widget.ViewPager;

import com.example.packageviewer.FloatBall.FloatBallService;
import com.example.packageviewer.Utils.CalUtil;
import com.example.packageviewer.Utils.MemInfoUtil;
import com.example.packageviewer.Utils.PackageInfoFilter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {


    private ViewPager viewPager;
    private TabLayout tabLayout;
    private List<Fragment> fragmentList;
    private List<String> title;
    private static final String TAG = "MainActivity";
    //内存计算部分
    private CalUtil calUtil;
    private MemInfoUtil memInfoUtil;
    private Intent serviceIntent;
    private static HomeWatcherReceiver mHomeKeyReceiver = null;
    private TextView memPercent;
    private TextView mem_av;
    private TextView mem_total;
    private boolean isEdit=false;
    private LinearLayout animationBar;
    private FloatingActionButton floatingActionButton;
    private boolean isChanged = false;
    private LocalBroadcastManager localBroadcastManager ;
    private Handler mhandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    //更新主界面
                    MemInfoUtil memInfoUtil = new MemInfoUtil();
                    CalUtil calUtil = new CalUtil();
                    memPercent.setText( calUtil.CalPercent(memInfoUtil.getMemAvailable(),memInfoUtil.getMemTotal()));
                    mem_av.setText("可用内存:"+calUtil.Caltoshow(memInfoUtil.getMemAvailable()));
                    mem_total.setText("总内存:"+calUtil.Caltoshow(memInfoUtil.getMemTotal()));
                    break;
                default:
                    break;
            }

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initData();
        try {
            initView();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }



    public void initData(){
        title = new ArrayList<>();
        title.add("用户应用");
        title.add("系统应用");
        title.add("菜单");
    }
    public void initView() throws PackageManager.NameNotFoundException {
        animationBar = findViewById(R.id.animationBar);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        memPercent = findViewById(R.id.memPercent);
        mem_av = findViewById(R.id.mem_av);
        mem_total = findViewById(R.id.mem_total);
        floatingActionButton = findViewById(R.id.editMenu);
        floatingActionButton.setOnClickListener(new EditMenuListener());
        memInfoUtil = new MemInfoUtil();
        calUtil = new CalUtil();
        memPercent.setText( calUtil.CalPercent(memInfoUtil.getMemAvailable(),memInfoUtil.getMemTotal()));
        mem_av.setText("可用内存:"+calUtil.Caltoshow(memInfoUtil.getMemAvailable()));
        mem_total.setText("总内存:"+calUtil.Caltoshow(memInfoUtil.getMemTotal()));
        memPercent.setOnClickListener(new HeadClickListener());
        LitePal.getDatabase();
        PackageManager packageManager = getPackageManager();
        //获取桌面应用并进行分类
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<PackageInfo> packageInfos = new PackageInfoFilter(packageManager).getPackageInfos();
        List<PackageInfo> userPackage = new ArrayList<>();
        List<PackageInfo> sysPackage = new ArrayList<>();
        for (PackageInfo packageinfo: packageInfos){

                if((packageinfo.applicationInfo.flags&packageinfo.applicationInfo.FLAG_SYSTEM)==1)
                    sysPackage.add(packageinfo);
                else
                    userPackage.add(packageinfo);
        }
        fragmentList = new ArrayList<>();
        fragmentList.add(new AppFragment(userPackage,0));
        fragmentList.add(new AppFragment(sysPackage,1));
        fragmentList.add(new DeleteFragment());
        viewPager.setOffscreenPageLimit(fragmentList.size());
        FragmentAdapter fragmentAdapter = new FragmentAdapter(fragmentList,getSupportFragmentManager(),title);
        viewPager.setAdapter(fragmentAdapter);
        tabLayout.setupWithViewPager(viewPager);
        //获取广播实例
        localBroadcastManager = LocalBroadcastManager.getInstance(this);

    }
    class EditMenuListener implements  View.OnClickListener{
        @Override
        public void onClick(View v) {
            Intent intent = new Intent("com.example.packageviewer.EDITMENU");
            localBroadcastManager.sendBroadcast(intent);
            isEdit = !isEdit;
            if(isEdit)
            {
                ColorStateList colorStateList = ContextCompat.getColorStateList(getApplicationContext(),R.color.colorAccent);
                floatingActionButton.setBackgroundTintList(colorStateList);
            }
            else{
                ColorStateList colorStateList = ContextCompat.getColorStateList(getApplicationContext(),R.color.colorPrimary);
                floatingActionButton.setBackgroundTintList(colorStateList);
            }

        }
    }
    class HeadClickListener implements View.OnClickListener{


        @Override
        public void onClick(View v) {
                if (!isChanged){//点击收缩内存页面
                    final String temp = memPercent.getText().toString();

                    ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f,1.0f,1.0f,0.1f, Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,0f);
                    scaleAnimation.setDuration(500);
                    scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            memPercent.setText("");
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            memPercent.clearAnimation();

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    memPercent.clearAnimation();
                    memPercent.setAnimation(scaleAnimation);
                    TranslateAnimation translateAnimation = new TranslateAnimation(animationBar.getLeft(),animationBar.getLeft(),memPercent.getTop(),memPercent.getTop()-getPixelsFromDp(300-30));
                    translateAnimation.setDuration(500);
                    translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            animationBar.clearAnimation();
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) memPercent.getLayoutParams();
                            layoutParams.height = getPixelsFromDp(30);
                            memPercent.setLayoutParams(layoutParams);
                            memPercent.setTextSize(getPixelsFromDp(5));
                            memPercent.setText("当前内存占比: "+temp);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    animationBar.clearAnimation();
                    animationBar.setAnimation(translateAnimation);
                    isChanged = true;
                }
                else{//点击弹出内存页面
                    ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f,1.0f,10.0f,10.0f, Animation.RELATIVE_TO_SELF,0f,Animation.RELATIVE_TO_SELF,0f);
                    scaleAnimation.setDuration(400);
                    scaleAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                            memPercent.setText("");
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            memPercent.clearAnimation();

                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    memPercent.clearAnimation();
                    memPercent.setAnimation(scaleAnimation);
                    TranslateAnimation translateAnimation = new TranslateAnimation(animationBar.getLeft(),animationBar.getLeft(),memPercent.getTop(),memPercent.getTop()+getPixelsFromDp(300-30));
                    translateAnimation.setDuration(300);
                    translateAnimation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            animationBar.clearAnimation();
                            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) memPercent.getLayoutParams();
                            layoutParams.height = getPixelsFromDp(300);
                            memPercent.setLayoutParams(layoutParams);
                            memPercent.setTextSize(getPixelsFromDp(25));
                            memPercent.setText(calUtil.CalPercent(memInfoUtil.getMemAvailable(),memInfoUtil.getMemTotal()));
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    animationBar.clearAnimation();
                    animationBar.setAnimation(translateAnimation);
                    isChanged = false;
                }
            }

    }
    //params换算
    private int getPixelsFromDp(int size){

        DisplayMetrics metrics =new DisplayMetrics();

        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        return(size * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;

    }
    public void openMinWindow() {
        if (!FloatBallService.isStarted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//判断系统版本
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "当前无权限，请授权", Toast.LENGTH_SHORT);
                    Log.d(TAG, "openMinWindow: 无授权");
                    startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 0);
                } else {

                    serviceIntent = new Intent(MainActivity.this, FloatBallService.class);

                    startService(serviceIntent);
                    Log.d(TAG, "openMinWindow: 已授权");
                    moveTaskToBack(true);
                }
            } else {
                serviceIntent = new Intent(MainActivity.this, FloatBallService.class);
                startService(serviceIntent);
                moveTaskToBack(true);
            }
        }
        else{
            Toast.makeText(this, "FloatBallisStarted" +
                    "", Toast.LENGTH_SHORT);
            Log.d(TAG, "openMinWindow: --------isStarted");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "授权成功", Toast.LENGTH_SHORT).show();
                    serviceIntent = new Intent(MainActivity.this, FloatBallService.class);
                    startService(serviceIntent);
                    moveTaskToBack(true);
                }
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (serviceIntent != null) {
            stopService(serviceIntent);
        }
    }

    //以下为Home键监听，最小化到桌面时也让悬浮窗启动

    @Subscribe
    public void onEvent(String event) {
        Log.d(TAG, "onEvent: getEvent-------"+event);
        if (event.equals("startService")) {
            openMinWindow();
        }
        else if(event.equals("add")||event.equals("delete")){
            Intent intent = new Intent("com.example.packageviewer.LOCALBROADCAST");
            localBroadcastManager.sendBroadcast(intent);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerHomeKeyReceiver(this);
        Message msg = new Message();
        msg.what = 0;
        mhandler.sendMessage(msg);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterHomeKeyReceiver(this);
    }

    private static void registerHomeKeyReceiver(Context context) {
        mHomeKeyReceiver = new HomeWatcherReceiver();
        final IntentFilter homeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        context.registerReceiver(mHomeKeyReceiver, homeFilter);
        EventBus.getDefault().register(context);
    }

    private static void unregisterHomeKeyReceiver(Context context) {
        if (null != mHomeKeyReceiver) {
            context.unregisterReceiver(mHomeKeyReceiver);
            EventBus.getDefault().unregister(context);
        }
    }

}
