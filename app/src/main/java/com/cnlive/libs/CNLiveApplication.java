package com.cnlive.libs;

import android.app.Application;

import com.cnlive.libs.util.Config;
import com.facebook.drawee.backends.pipeline.Fresco;


/**
 * Created by Mr.hou on 2017/2/17.
 */
public class CNLiveApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        /**配置项目对应 appid 和 appkey */
        Config.init(this, "60_irg1rhs308", "4e7a37b27a717a9e9b0a7aea4620ee55ea61fbf923bf46");
        /**
         *fresco初始化
         */
        Fresco.initialize(this);
    }
}
