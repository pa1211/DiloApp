package com.jjoseba.pecsmobile.app;

import android.app.Application;

import io.fabric.sdk.android.Fabric;
import com.crashlytics.android.Crashlytics;

import com.jjoseba.pecsmobile.R;
import com.jjoseba.pecsmobile.util.FileUtils;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class PECSMobile extends Application {

    public static final int DISPLAY_MODE_CARDS = 1;
    public static final int DISPLAY_MODE_TEXT = 2;
    public static final int DISPLAY_MODE_BASIC = 3;

    public static final boolean DEFAULT_SHOW_NEWCARD_BUTTON = true;
    public static final boolean DEFAULT_SHOW_TEMPTEXT_BUTTON = true;
    public static int DEFAULT_DISPLAY_MODE = DISPLAY_MODE_CARDS;

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/billy.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build()
        );

        //We set the StoragePath
        FileUtils.initialize(getApplicationContext());
    }

}
