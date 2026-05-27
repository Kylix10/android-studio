package com.example.summer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.summer.utils.NotificationHelper;

public class NotificationReceiver extends BroadcastReceiver {

    public static final String ACTION_SHOW_ALERT = "com.example.summer.action.SHOW_ALERT";
    public static final String ACTION_SHOW_PROMOTION = "com.example.summer.action.SHOW_PROMOTION";
    public static final String ACTION_SHOW_SYSTEM = "com.example.summer.action.SHOW_SYSTEM";

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_CONTENT = "content";
    public static final String EXTRA_SPOT_NAME = "spot_name";
    public static final String EXTRA_COUNT = "count";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (ACTION_SHOW_ALERT.equals(action)) {
            String spotName = intent.getStringExtra(EXTRA_SPOT_NAME);
            int count = intent.getIntExtra(EXTRA_COUNT, 0);
            NotificationHelper.showCrowdAlertNotification(context, spotName, count);
        } else if (ACTION_SHOW_PROMOTION.equals(action)) {
            String title = intent.getStringExtra(EXTRA_TITLE);
            String content = intent.getStringExtra(EXTRA_CONTENT);
            NotificationHelper.showPromotionNotification(context, title, content);
        } else if (ACTION_SHOW_SYSTEM.equals(action)) {
            String title = intent.getStringExtra(EXTRA_TITLE);
            String content = intent.getStringExtra(EXTRA_CONTENT);
            NotificationHelper.showSystemNotification(context, title, content);
        }
    }
}
