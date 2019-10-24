package com.example.packageviewer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.packageviewer.Utils.App;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

public class DeleteFragment extends Fragment {
    private RecyclerView list;
    private List<App> apps;
    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delete,null);
        apps = DataSupport.findAll(App.class);
        list = (RecyclerView) view.findViewById(R.id.delete_list);
        PackageManager pm =getActivity().getPackageManager();
        List<PackageInfo> packageInfoList = new ArrayList<>();
        for (App app:apps) {
            try {
                packageInfoList.add(pm.getPackageInfo(app.getPackageName(),0));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
        }
        PackageAdapter packageAdapter = new PackageAdapter(packageInfoList,2,false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        list.setLayoutManager(linearLayoutManager);
        list.setAdapter(packageAdapter);
        localBroadcastManager =  LocalBroadcastManager.getInstance(getContext());
        intentFilter = new IntentFilter("com.example.packageviewer.LOCALBROADCAST");
        localReceiver = new LocalReceiver();
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);

       // EventBus.getDefault().register(getContext());
        return view;
    }
    class LocalReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            apps = DataSupport.findAll(App.class);
            list = (RecyclerView) getView().findViewById(R.id.delete_list);
            PackageManager pm =getActivity().getPackageManager();
            List<PackageInfo> packageInfoList = new ArrayList<>();
            for (App app:apps) {
                try {
                    packageInfoList.add(pm.getPackageInfo(app.getPackageName(),0));
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
            PackageAdapter packageAdapter = new PackageAdapter(packageInfoList,2,false);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
            list.setLayoutManager(linearLayoutManager);
            list.setAdapter(packageAdapter);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null!=localBroadcastManager)
        {
            localBroadcastManager.unregisterReceiver(localReceiver);
        }
    }
}
