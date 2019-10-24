package com.example.packageviewer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
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

import java.util.List;

public class AppFragment extends Fragment {
    private List<PackageInfo> packageInfoList;
    private RecyclerView list;
    private boolean isEdit = false;
    private LocalBroadcastManager localBroadcastManager;
    private IntentFilter intentFilter;
    private EditReceiver editReceiver;
    private PackageAdapter packageAdapter;
    private int isSystem;
    private static final String TAG = "AppFragment";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_app,null);
        //floatingActionButton = view.findViewById(R.id.editMenu);
        list = (RecyclerView) view.findViewById(R.id.package_list);
        // floatingActionButton.setOnClickListener(new EditMenuListener());
        packageAdapter = new PackageAdapter(packageInfoList,0,isEdit);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        list.setLayoutManager(linearLayoutManager);
        list.setAdapter(packageAdapter);
        localBroadcastManager =  LocalBroadcastManager.getInstance(getContext());
        intentFilter = new IntentFilter("com.example.packageviewer.EDITMENU");
        editReceiver = new EditReceiver();
        localBroadcastManager.registerReceiver(editReceiver,intentFilter);
        return view;
    }
    public AppFragment(List<PackageInfo>packageInfos,int isSystem){ packageInfoList = packageInfos;this.isSystem = isSystem; }


    class EditReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            isEdit = !isEdit;
            packageAdapter.setEdit(isEdit);
            packageAdapter.notifyItemRangeChanged(0,packageInfoList.size());
        }

    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(null!=localBroadcastManager)
        {
            localBroadcastManager.unregisterReceiver(editReceiver);
        }

    }
}
