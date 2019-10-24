package com.example.packageviewer.Utils;

import org.litepal.crud.DataSupport;

public class AppShow extends DataSupport {
    private String packageName;
    private String appName;
    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }


}
