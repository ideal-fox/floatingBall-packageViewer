package com.example.packageviewer.Utils;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class PackageInfoFilter {
    private List<PackageInfo> packageInfos;
    private List<ResolveInfo> resolveInfos;
    private PackageManager pm;
    public PackageInfoFilter(PackageManager pm) throws PackageManager.NameNotFoundException {
        this.pm = pm;
        packageInfos = new ArrayList<>();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        resolveInfos = pm.queryIntentActivities(intent,PackageManager.MATCH_ALL);
        DataSupport.deleteAll(AppShow.class);

        for (ResolveInfo activityInfo:resolveInfos) {
            List<AppShow> appShows = DataSupport.where("packageName = ?",activityInfo.activityInfo.packageName).find(AppShow.class);
            if(appShows.isEmpty())
            {
                AppShow appShow = new AppShow();
                appShow.setPackageName(activityInfo.activityInfo.packageName);
                appShow.setAppName((String) activityInfo.activityInfo.applicationInfo.loadLabel(pm));
                appShow.save();
            }
        }
        List<AppShow> appShowList = DataSupport.order("packageName").find(AppShow.class);

        for (AppShow appShowItem:appShowList ){
            Log.d(TAG, "PackageInfoFilter: ========="+appShowItem.getPackageName());
            packageInfos.add(pm.getPackageInfo(appShowItem.getPackageName(),0));
        }
    }
    public List<PackageInfo> getPackageInfos(){
        return packageInfos;
    }
}
