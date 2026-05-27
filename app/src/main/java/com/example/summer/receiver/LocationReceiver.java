package com.example.summer.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.summer.utils.LocationManagerHelper;
import com.example.summer.utils.NotificationHelper;

public class LocationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (LocationManagerHelper.ACTION_SPOT_ARRIVAL.equals(action)) {
            String spotName = intent.getStringExtra(LocationManagerHelper.EXTRA_SPOT_NAME);
            NotificationHelper.showPromotionNotification(
                    context,
                    "到达景点",
                    String.format("欢迎来到%s，点击查看详情", spotName)
            );
        }
    }
}
