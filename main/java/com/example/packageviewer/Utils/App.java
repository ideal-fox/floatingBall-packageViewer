package com.example.packageviewer.Utils;

import org.litepal.crud.DataSupport;

public class App extends DataSupport {
    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }


    private String appName;
    private String packageName;





}
