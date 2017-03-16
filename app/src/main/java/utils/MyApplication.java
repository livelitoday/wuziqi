package utils;

import android.app.Application;
import android.content.Context;

/**
 * 项目名称：com.test.wuziqi
 * 创建人：Created by zhiyuan.
 * 创建时间：Created on 2016/8/31 19:05
 * 修改备注：让程序启动的时候初始化MyApplication，而不是默认的Application，借助此工具类可以在全局范围内获取Application级别的Context.
 */
public class MyApplication extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        //super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
