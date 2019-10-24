package com.example.packageviewer;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.packageviewer.Utils.App;

import org.greenrobot.eventbus.EventBus;
import org.litepal.crud.DataSupport;

import java.util.List;

import static android.view.View.GONE;

public class PackageAdapter extends RecyclerView.Adapter<PackageAdapter.ViewHolder> {
    private List<PackageInfo> mpackageInfo;
    private int isSystem;
    private boolean isEdit = false;
    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView packageImage;
        TextView packageName;
        TextView appName;
        CardView packageView;
        PackageManager packageManager;
        Button add;
        Button delete;
        public ViewHolder(View view,PackageManager pm) {
            super(view);
            packageView = (CardView) view;
            packageImage = (ImageView) view.findViewById(R.id.package_image);
            packageName = (TextView) view.findViewById(R.id.package_name);
            appName = (TextView) view.findViewById(R.id.app_name);
            packageManager = pm;
            add = view.findViewById(R.id.add);
            delete = view.findViewById(R.id.delete);
        }
    }
    public PackageAdapter(List<PackageInfo> packageInfos,int isSystem,boolean isEdit){
        mpackageInfo = packageInfos;
        this.isSystem = isSystem;
        this.isEdit = isEdit;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_package,parent,false);
        final PackageManager packageManager = parent.getContext().getPackageManager();
        final ViewHolder holder = new ViewHolder(view,packageManager);

        if (isSystem == 2){//删除列表
           holder.add.setVisibility(GONE);
           holder.delete.setVisibility(View.VISIBLE);
           holder.delete.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View v) {
                   int position = holder.getAdapterPosition();
                   List<App> apps =  DataSupport.where("PackageName = ?",mpackageInfo.get(holder.getAdapterPosition()).packageName).find(App.class);

                   for (App app:apps) {
                       app.delete();
                   }
                   mpackageInfo.remove(position);
                   notifyItemRemoved(position);
                   notifyItemRangeChanged(position,getItemCount());
               }
           });

       }
        holder.packageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = holder.getAdapterPosition();
                final PackageInfo packageInfo = mpackageInfo.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle(packageInfo.applicationInfo.loadLabel(holder.packageManager));
                builder.setCancelable(true);
                builder.setMessage(packageInfo.packageName);
                builder.setIcon(packageInfo.applicationInfo.loadIcon(holder.packageManager));
                builder.setPositiveButton("打开", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = parent.getContext().getPackageManager().getLaunchIntentForPackage(packageInfo.packageName);
                        parent.getContext().startActivity(intent);

                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });
        return holder;
    }
    public void setEdit(boolean isEdit){
        this.isEdit = isEdit;
    }
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        PackageInfo packageInfo = mpackageInfo.get(position);
        holder.appName.setText(packageInfo.applicationInfo.loadLabel(holder.packageManager));
        holder.packageName.setText(packageInfo.packageName);
        holder.packageImage.setImageDrawable(packageInfo.applicationInfo.loadIcon(holder.packageManager));
        //在绑定部分刷新序列
        if(isSystem<2&&isEdit){

            //排除掉自身
            if("com.example.packageviewer".equals(holder.packageName.getText()))
            {
                holder.add.setVisibility(GONE);
            }
            else{
                //判断是否已经添加，改变布局
                final List<App> appCheck = DataSupport.where("PackageName = ?" ,holder.packageName.getText().toString()).find(App.class);
                if (!appCheck.isEmpty()) {
                    holder.add.setVisibility(GONE);
                    holder.delete.setVisibility(View.VISIBLE);
                    holder.delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            for (App app:appCheck) {
                                app.delete();
                            }
                            onBindViewHolder(holder,position);
                            EventBus.getDefault().post("delete");
                        }
                    });
                }
                else {
                    holder.add.setVisibility(View.VISIBLE);
                    holder.delete.setVisibility(GONE);
                }

            }
            holder.add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    List<App> apps = DataSupport.findAll(App.class);
                    if(apps.size()==5)
                    {
                        AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                        builder.setTitle("菜单已满!");
                        builder.setMessage("目前版本仅支持最多5个菜单应用");
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                        builder.show();
                    }
                    else{
                        int position = holder.getAdapterPosition();
                        List<App> appCheck = DataSupport.where("PackageName = ?" ,mpackageInfo.get(position).packageName).find(App.class);
                        if (appCheck.isEmpty()) {
                            App app = new App();
                            app.setAppName(mpackageInfo.get(position).applicationInfo.name);
                            app.setPackageName(mpackageInfo.get(position).packageName);
                            app.save();
                            onBindViewHolder(holder,position);
                            EventBus.getDefault().post("add");
                        }
                    }
                }
            });
        }else if(!isEdit&&isSystem<2){
            holder.add.setVisibility(View.INVISIBLE);
            holder.delete.setVisibility(GONE);
        }

}

    @Override
    public int getItemCount() {
        return mpackageInfo.size();
    }


}
