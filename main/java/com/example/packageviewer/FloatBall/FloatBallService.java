package com.example.packageviewer.FloatBall;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.packageviewer.MainActivity;
import com.example.packageviewer.R;
import com.example.packageviewer.Utils.App;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static java.lang.Math.abs;


public class FloatBallService extends Service {
    private WindowManager mWindowManager;
    private WindowManager.LayoutParams wmParams;
    private LayoutInflater inflater;
    private int ballSize = 45;
    public static boolean isStarted = false;
    private boolean isMid = false;
    private boolean isExpanded = false;
    private boolean isBottom = false;
    private static final String TAG = "FloatBallService";
    private PackageManager pm;
    //view
    private View mFloatingLayout;    //布局View
    //菜单
    private View menu;
    //菜单坐标定位
    private List<int[]> menuLocation;
    //菜单尺寸
    private int menuSize = 220;

    //菜单中心点击按钮
    private ImageView centerImage;
    //设置偏移量
    private int offset;
    //数据库
    List<App> apps;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onStartCommand: CreatService=====");
        isStarted = true;
        initWindow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: StartService");
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mWindowManager != null) {
            mWindowManager.removeView(mFloatingLayout);
            mWindowManager.removeView(menu);
            isStarted = false;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * 设置悬浮框基本参数,位置和size
     */
    private void initWindow() {
        mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        wmParams = getParams();
        inflater = LayoutInflater.from(getApplicationContext());
        mFloatingLayout = inflater.inflate(R.layout.layout_window, null);
        menu = inflater.inflate(R.layout.layout_menu,null);
        pm = getPackageManager();

        RelativeLayout menu_area = menu.findViewById(R.id.menu_area);
        for (int i =0;i<5;i++) {
            ImageView imageView = new ImageView(this);
            imageView.setImageResource(R.drawable.ic_floatball);
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            imageView.setId(i);

            RelativeLayout.LayoutParams wh = new RelativeLayout.LayoutParams(getPixelsFromDp(ballSize-10),getPixelsFromDp(ballSize-10));
            imageView.setLayoutParams(wh);
            imageView.setVisibility(View.INVISIBLE);
            menu_area.addView(imageView);
        }
        menu.setVisibility(View.INVISIBLE);

    }
    //params换算
    private int getPixelsFromDp(int size){

        DisplayMetrics metrics =new DisplayMetrics();

        mWindowManager.getDefaultDisplay().getMetrics(metrics);

        return(size * metrics.densityDpi) / DisplayMetrics.DENSITY_DEFAULT;

    }
    private WindowManager.LayoutParams getParams() {
        wmParams = new WindowManager.LayoutParams();
        wmParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        wmParams.format = PixelFormat.RGBA_8888;
        //设置可以显示在状态栏上
        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wmParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wmParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        //设置起始点
        wmParams.gravity = Gravity.LEFT | Gravity.TOP;
        wmParams.x = 1000;
        wmParams.y = 500;
        return wmParams;
    }


    private void showFloatingWindow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//判断系统版本
            if (Settings.canDrawOverlays(this)) {
                mFloatingLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            showMenu();
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                });
                menu.setOnTouchListener(new FloatingListener());
                mFloatingLayout.setOnTouchListener(new FloatingListener());
                mWindowManager.addView(mFloatingLayout, wmParams);
                mWindowManager.addView(menu, wmParams);
            }
        } else {
            mFloatingLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
            });
            mFloatingLayout.setOnTouchListener(new FloatingListener());
            mWindowManager.addView(mFloatingLayout, wmParams);
        }
    }

    private void hideMenu() {
        isExpanded = false;
            int itemCounter = 0;
            int left = centerImage.getLeft();
            int top = centerImage.getTop();
            for (; itemCounter <  apps.size(); itemCounter++) {
                final ImageView imageView = menu.findViewById(itemCounter);
                hideMenutool(imageView,left,top);
            }
            if(apps.size()<5){
                final ImageView imageView = menu.findViewById(R.id.addmenu);
                final int finalItemCounter = itemCounter;
                hideMenutool(imageView,left,top);
            }

        Log.d(TAG, "hideMenu: ------------------------------------------------");



    }
    //菜单收起动画部分暂时不修改
    public void hideMenutool(final ImageView imageView,int left,int top){
        TranslateAnimation animation = new TranslateAnimation(Animation.ABSOLUTE,imageView.getLeft()-left,
                Animation.ABSOLUTE,left-imageView.getLeft(),
                Animation.ABSOLUTE, imageView.getTop()-top,
                Animation.ABSOLUTE, top-imageView.getTop());
        animation.setDuration(200);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                imageView.clearAnimation();
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                layoutParams.setMargins(0,0,0,0);
                imageView.setVisibility(View.INVISIBLE);
                imageView.setLayoutParams(layoutParams);
                //恢复原来位置
                if(isMid){
                    RelativeLayout.LayoutParams midParams = (RelativeLayout.LayoutParams) centerImage.getLayoutParams();
                    midParams.setMargins(0,centerImage.getTop() - offset,centerImage.getRight(),centerImage.getBottom());
                    centerImage.setLayoutParams(midParams);
                    isMid = false;
                }
                else if (isBottom){
                    RelativeLayout.LayoutParams bottomParams = (RelativeLayout.LayoutParams) centerImage.getLayoutParams();
                    bottomParams.setMargins(0,0,centerImage.getRight(),centerImage.getBottom());
                    centerImage.setLayoutParams(bottomParams);
                    isBottom = false;
                }
                else{
                    RelativeLayout.LayoutParams topParams = (RelativeLayout.LayoutParams) centerImage.getLayoutParams();
                    topParams.setMargins(0,0,0,0);
                    centerImage.setLayoutParams(topParams);
                }
                menu.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        animation.setRepeatMode(Animation.RESTART);
        imageView.startAnimation(animation);
    }
    public void showMenu() throws PackageManager.NameNotFoundException {
        //获取屏幕信息
        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getMetrics(dm);
        //动态设置菜单大小
//        int maxWidth = getPixelsFromDp(menuSize-45);
//        int maxHeigth = getPixelsFromDp(menuSize-45);
        //再次读取数据库
        apps = DataSupport.findAll(App.class);
        //根据定位设置菜单显示方式
        centerImage = menu.findViewById(R.id.center);
        centerImage.setOnClickListener(new MenuListener());
        offset = getPixelsFromDp(menuSize/2-45);
        //计算各个位置，这里第一轨道设为5个菜单，先计算半径
        menu.setVisibility(View.VISIBLE);
        isExpanded = true;
        if (wmParams.x < dm.widthPixels/2 && wmParams.y < offset) {//左上
            final float r = getPixelsFromDp(ballSize) / (float) (Math.sin(Math.PI / 16) * 2);
            Log.d(TAG, "showMenu: ---------IN-----------" + r);
            int itemCounter = 0;
            for (; itemCounter <  apps.size(); itemCounter++) {

                final int finalItemCounter = itemCounter;
                final ImageView imageView = menu.findViewById(itemCounter);
                imageView.setImageDrawable(pm.getApplicationIcon(apps.get(itemCounter).getPackageName()));
                expandMenu(imageView,(int) (r * Math.cos(Math.PI * finalItemCounter / 8)),(int) (r * Math.sin(Math.PI * finalItemCounter / 8)),0,0,0);
                imageView.setOnClickListener(new MenuListener());
            }
            if(apps.size()<5){
                final ImageView imageView = menu.findViewById(R.id.addmenu);
                imageView.setVisibility(View.VISIBLE);
                final int finalItemCounter = itemCounter;
                expandMenu(imageView,(int) (r * Math.cos(Math.PI * finalItemCounter / 8)),(int) (r * Math.sin(Math.PI * finalItemCounter / 8)),0,0,0);
                imageView.setOnClickListener(new MenuListener());
            }

        }
        else if(wmParams.x<dm.widthPixels/2 &&wmParams.y<dm.heightPixels-getPixelsFromDp(menuSize)){//左中

            int y = wmParams.y;
            wmParams.y -=offset;
            mWindowManager.updateViewLayout(menu,wmParams);
            final float r = getPixelsFromDp(ballSize) / (float) (Math.sin(Math.PI / 8) * 2);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) centerImage.getLayoutParams();
            layoutParams.setMargins(0,centerImage.getTop() + offset,0,0);
            centerImage.setLayoutParams(layoutParams);
            isMid = true;
            Log.d(TAG, "showMenu: ---------IN-----------" + r);
            int itemCounter = 0;
            for (; itemCounter <  apps.size(); itemCounter++) {

                final int finalItemCounter = itemCounter;
                final ImageView imageView = menu.findViewById(itemCounter);
                imageView.setImageDrawable(pm.getApplicationIcon(apps.get(itemCounter).getPackageName()));
                expandMenu(imageView,(int) (r * Math.sin(Math.PI * finalItemCounter / 4)),(int) (r * Math.cos(Math.PI * finalItemCounter / 4)),0,0,offset);
                imageView.setOnClickListener(new MenuListener());
            }
            if(apps.size()<5){
                final ImageView imageView = menu.findViewById(R.id.addmenu);
                imageView.setVisibility(View.VISIBLE);
                final int finalItemCounter = itemCounter;
                expandMenu(imageView,(int) (r * Math.sin(Math.PI * finalItemCounter / 4)),(int) (r * Math.cos(Math.PI * finalItemCounter / 4)),0,0,offset);
                imageView.setOnClickListener(new MenuListener());
            }
            wmParams.y = y;
            mWindowManager.updateViewLayout(mFloatingLayout,wmParams);
        }
        else if(wmParams.x<dm.widthPixels/2 ) {//左下
            int y = wmParams.y;
            wmParams.y -=getPixelsFromDp(menuSize-ballSize);
            mWindowManager.updateViewLayout(menu,wmParams);
            final float r = getPixelsFromDp(ballSize) / (float) (Math.sin(Math.PI / 16) * 2);
            Log.d(TAG, "showMenu: ---------IN-----------" + r);

            RelativeLayout.LayoutParams centerParams = (RelativeLayout.LayoutParams) centerImage.getLayoutParams();
            centerParams.setMargins(centerImage.getLeft(),getPixelsFromDp(menuSize-ballSize),0,0);
            centerImage.setLayoutParams(centerParams);
            isBottom = true;
            int itemCounter = 0;
            for (; itemCounter <  apps.size(); itemCounter++) {

                final int finalItemCounter = itemCounter;
                final ImageView imageView = menu.findViewById(itemCounter);
                imageView.setImageDrawable(pm.getApplicationIcon(apps.get(itemCounter).getPackageName()));
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                layoutParams.setMargins(imageView.getLeft(),getPixelsFromDp(menuSize-ballSize),0,0);
                imageView.setLayoutParams(layoutParams);
                expandMenu(imageView,(int) (r * Math.sin(Math.PI * finalItemCounter / 8)),0,0,(int) (r * Math.cos(Math.PI * finalItemCounter / 8)),0);
                imageView.setOnClickListener(new MenuListener());
            }
            if(apps.size()<5){
                final ImageView imageView = menu.findViewById(R.id.addmenu);
                imageView.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                layoutParams.setMargins(imageView.getLeft(),getPixelsFromDp(menuSize-ballSize),0,0);
                imageView.setLayoutParams(layoutParams);
                final int finalItemCounter = itemCounter;
                expandMenu(imageView,(int) (r * Math.sin(Math.PI * finalItemCounter / 8)),0,0,(int) (r * Math.cos(Math.PI * finalItemCounter / 8)),0);
                imageView.setOnClickListener(new MenuListener());
            }
            wmParams.y = y;
            mWindowManager.updateViewLayout(mFloatingLayout,wmParams);
        }
        else if(wmParams.x > dm.widthPixels/2 && wmParams.y < offset){//右上
            final float r = getPixelsFromDp(ballSize) / (float) (Math.sin(Math.PI / 16) * 2);
            Log.d(TAG, "showMenu: ---------IN-----------" + r);
            RelativeLayout.LayoutParams centerParams = (RelativeLayout.LayoutParams) centerImage.getLayoutParams();
            centerParams.setMargins(getPixelsFromDp(menuSize-ballSize),0,0,0);
            centerImage.setLayoutParams(centerParams);
            int itemCounter = 0;
            for (; itemCounter <  apps.size(); itemCounter++) {

                final int finalItemCounter = itemCounter;
                final ImageView imageView = menu.findViewById(itemCounter);
                imageView.setImageDrawable(pm.getApplicationIcon(apps.get(itemCounter).getPackageName()));
                expandMenu(imageView,0,(int) (r * Math.sin(Math.PI * finalItemCounter / 8)),(int) (r * Math.cos(Math.PI * finalItemCounter / 8)),0,0);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                layoutParams.setMargins(imageView.getLeft()+getPixelsFromDp(menuSize-ballSize),0,0,0);
                imageView.setLayoutParams(layoutParams);
                imageView.setOnClickListener(new MenuListener());
            }
            if(apps.size()<5){
                final ImageView imageView = menu.findViewById(R.id.addmenu);
                imageView.setVisibility(View.VISIBLE);
                final int finalItemCounter = itemCounter;
                expandMenu(imageView,0,(int) (r * Math.sin(Math.PI * finalItemCounter / 8)),(int) (r * Math.cos(Math.PI * finalItemCounter / 8)),0,0);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                layoutParams.setMargins(imageView.getLeft()+getPixelsFromDp(menuSize-ballSize),0,0,0);
                imageView.setLayoutParams(layoutParams);
                imageView.setOnClickListener(new MenuListener());
            }
        }
        else if (wmParams.x>dm.widthPixels/2 &&wmParams.y<dm.heightPixels-getPixelsFromDp(menuSize)){//右中

            int y = wmParams.y;
            wmParams.y -=offset;
            mWindowManager.updateViewLayout(menu,wmParams);
            final float r = getPixelsFromDp(ballSize) / (float) (Math.sin(Math.PI / 8) * 2);
            RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) centerImage.getLayoutParams();
            layoutParams.setMargins(getPixelsFromDp(menuSize-ballSize),centerImage.getTop() + offset,0,0);
            centerImage.setLayoutParams(layoutParams);
            isMid = true;
            Log.d(TAG, "showMenu: ---------IN-----------" + r);
            int itemCounter = 0;
            for (; itemCounter <  apps.size(); itemCounter++) {

                final int finalItemCounter = itemCounter;
                final ImageView imageView = menu.findViewById(itemCounter);
                imageView.setImageDrawable(pm.getApplicationIcon(apps.get(itemCounter).getPackageName()));
                RelativeLayout.LayoutParams imageViewLayoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                imageViewLayoutParams.setMargins(imageView.getLeft()+getPixelsFromDp(menuSize-ballSize),centerImage.getTop(),0,0);
                imageView.setLayoutParams(imageViewLayoutParams);
                expandMenu(imageView,0,(int) (r * Math.cos(Math.PI * finalItemCounter / 4)),(int) (r * Math.sin(Math.PI * finalItemCounter / 4)),0,offset);
                imageView.setOnClickListener(new MenuListener());
            }
            if(apps.size()<5){
                final ImageView imageView = menu.findViewById(R.id.addmenu);
                imageView.setVisibility(View.VISIBLE);
                final int finalItemCounter = itemCounter;
                RelativeLayout.LayoutParams imageViewLayoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                imageViewLayoutParams.setMargins(imageView.getLeft()+getPixelsFromDp(menuSize-ballSize),centerImage.getTop(),0,0);
                imageView.setLayoutParams(imageViewLayoutParams);
                imageView.setOnClickListener(new MenuListener());
                expandMenu(imageView,0,(int) (r * Math.cos(Math.PI * finalItemCounter / 4)),(int) (r * Math.sin(Math.PI * finalItemCounter / 4)),0,offset);
            }
            wmParams.y = y;
            mWindowManager.updateViewLayout(mFloatingLayout,wmParams);
        }
        else{//右下
            int y = wmParams.y;
            wmParams.y -=getPixelsFromDp(menuSize-ballSize);
            mWindowManager.updateViewLayout(menu,wmParams);
            final float r = getPixelsFromDp(ballSize) / (float) (Math.sin(Math.PI / 16) * 2);
            Log.d(TAG, "showMenu: ---------IN-----------" + r);

            RelativeLayout.LayoutParams centerParams = (RelativeLayout.LayoutParams) centerImage.getLayoutParams();
            centerParams.setMargins(getPixelsFromDp(menuSize-ballSize),getPixelsFromDp(menuSize-ballSize),0,0);
            centerImage.setLayoutParams(centerParams);
            isBottom = true;
            int itemCounter = 0;
            for (; itemCounter <  apps.size(); itemCounter++) {

                final int finalItemCounter = itemCounter;
                final ImageView imageView = menu.findViewById(itemCounter);
                imageView.setImageDrawable(pm.getApplicationIcon(apps.get(itemCounter).getPackageName()));
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                layoutParams.setMargins(imageView.getLeft()+getPixelsFromDp(menuSize-ballSize),getPixelsFromDp(menuSize-ballSize),0,0);
                imageView.setLayoutParams(layoutParams);
                expandMenu(imageView,0,0,(int) (r * Math.sin(Math.PI * finalItemCounter / 8)),(int) (r * Math.cos(Math.PI * finalItemCounter / 8)),0);
                imageView.setOnClickListener(new MenuListener());
            }
            if(apps.size()<5){
                final ImageView imageView = menu.findViewById(R.id.addmenu);
                imageView.setVisibility(View.VISIBLE);
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                layoutParams.setMargins(imageView.getLeft()+getPixelsFromDp(menuSize-ballSize),getPixelsFromDp(menuSize-ballSize),0,0);
                imageView.setLayoutParams(layoutParams);
                final int finalItemCounter = itemCounter;
                expandMenu(imageView,0,0,(int) (r * Math.sin(Math.PI * finalItemCounter / 8)),(int) (r * Math.cos(Math.PI * finalItemCounter / 8)),0);
                imageView.setOnClickListener(new MenuListener());
            }
            wmParams.y = y;
            mWindowManager.updateViewLayout(mFloatingLayout,wmParams);

        }

    }
    //展开菜单动画
    public void expandMenu(final ImageView imageView, final int left, final int top, final int right, final int bottom,final int offset){
        TranslateAnimation animation = new TranslateAnimation(Animation.ABSOLUTE,imageView.getLeft(),
                Animation.ABSOLUTE,left-right+imageView.getLeft(),
                Animation.ABSOLUTE,imageView.getTop()+offset,
                Animation.ABSOLUTE, top-bottom+imageView.getTop()+offset);
        menuLocation= new ArrayList<>();
        int[] menuItem={imageView.getLeft(),imageView.getTop()+offset};
        menuLocation.add(menuItem);
        animation.setDuration(500);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }
            @Override
            public void onAnimationEnd(Animation animation) {
                imageView.clearAnimation();
                RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
                layoutParams.setMargins(imageView.getLeft()+ left-right,imageView.getTop() + top+offset-bottom,imageView.getRight(),imageView.getBottom());
                imageView.setVisibility(View.VISIBLE);
                imageView.setLayoutParams(layoutParams);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        animation.setRepeatMode(Animation.RESTART);
        imageView.startAnimation(animation);
    }
    //菜单点击事件
    class MenuListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            if (v.getId()==R.id.center&&isExpanded){
                Log.d(TAG, "onClick: ============");
                hideMenu();
            }else if(v.getId()==R.id.addmenu){
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
            }
            else{
                Toast.makeText(getApplicationContext(),"click the num "+v.getId(),Toast.LENGTH_SHORT);
                try {
                    Intent intent = getApplication().getPackageManager().getLaunchIntentForPackage(apps.get(v.getId()).getPackageName());
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),"Cannot Open!",Toast.LENGTH_SHORT);
                }
                Log.d(TAG, "onClick: +++++++++"+v.getId());
            }

        }
    }
    private int mTouchStartX, mTouchStartY, mTouchCurrentX, mTouchCurrentY;
    private int mStartX, mStartY, mStopX, mStopY;
    private boolean isMove;

    private class FloatingListener implements View.OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            int action = event.getAction();
            if (!isExpanded) {
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        isMove = false;
                        mTouchStartX = (int) event.getRawX();
                        mTouchStartY = (int) event.getRawY();
                        mStartX = (int) event.getX();
                        mStartY = (int) event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mTouchCurrentX = (int) event.getRawX();
                        mTouchCurrentY = (int) event.getRawY();
                        wmParams.x += mTouchCurrentX - mTouchStartX;
                        wmParams.y += mTouchCurrentY - mTouchStartY;
                        mWindowManager.updateViewLayout(menu, wmParams);
                        mWindowManager.updateViewLayout(mFloatingLayout, wmParams);
                        mTouchStartX = mTouchCurrentX;
                        mTouchStartY = mTouchCurrentY;
                        break;
                    case MotionEvent.ACTION_UP:
                        mStopX = (int) event.getX();
                        mStopY = (int) event.getY();
                        Log.d(TAG, "initWindow: ////////////////" + wmParams.x + "////" + wmParams.y);
                        if (abs(mStartX - mStopX) >= 1 || abs(mStartY - mStopY) >= 1) {
                            isMove = true;
                        }
                        attachSide();
                        break;
                }
                return isMove;
            }
            return false;
        }


    }
    public void attachSide(){
        if(wmParams.x<550){
            wmParams.x = 0;
        }
        else{
            wmParams.x = 1000;
        }
        mWindowManager.updateViewLayout(menu,wmParams);
        mWindowManager.updateViewLayout(mFloatingLayout,wmParams);

    }
}

