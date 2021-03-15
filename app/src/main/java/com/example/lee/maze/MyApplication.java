package com.example.lee.maze;

import android.app.Activity;
import android.app.Application;

import java.util.ArrayList;
import java.util.List;


public class MyApplication extends Application {

    private List<Activity> activities = new ArrayList<>();

    private static MyApplication instance = null;

    private MyApplication() {

    }

    public synchronized static MyApplication getInstance() {
        if (instance == null) {
            instance = new MyApplication();
        }
        return instance;
    }

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

    }

    public void finish_activity(Activity activity) {
        for (Activity activity1 : activities) {
            if (activity1 != activity) {
                activity1.finish();
            }
        }
        activity.finish();
        System.exit(0);
    }

}

