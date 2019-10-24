package com.example.packageviewer.Utils;

import android.util.Log;

import static android.content.ContentValues.TAG;

public class CalUtil {
    private int piece = 1024;

    //换算输出
    public String Caltoshow(String str){
        if(!str.matches("\\d+")){
            return null;
        }

        int flag = 0;
        long num = Integer.parseInt(str) ;
        long temp =0;
        float result;
        while(num>piece){
            temp = (long)(num*1000/(float)piece);
            Log.d(TAG, "Caltoshow: "+temp);
            num = num/piece;
            flag++;
        }
        result = temp/(float)1000;
        switch (flag){
            case 0:
                return String.valueOf(result)+"KB";
            case 1:
                return String.valueOf(result)+"MB";
            case 2:
                return String.valueOf(result)+"GB";
            case 3:
                return String.valueOf(result)+"TB";
             default:
                 return null;
        }

    }
    //百分比计算
    public String CalPercent(String part,String total){
        if(!(part.matches("\\d+")&&(total.matches("\\d+" )))){
            return null;
        }

        long totalMem = Integer.parseInt(total);
        long availMem = Integer.parseInt(part);
        int result = (int)((totalMem-availMem)*1000/(float)totalMem);
        return String.valueOf(result/(float)10)+'%';
    }
}
