package com.example.summer.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.summer.R;
import com.example.summer.MainActivity;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";

    public static void showCrowdAlertNotification(Context context, String spotName, int count) {
        Log.d(TAG, "显示人流量预警通知: " + spotName);
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("target_fragment", "crowd");
        intent.putExtra("spot_name", spotName);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                NotificationChannelManager.CHANNEL_ID_ALERT)
                .setContentTitle("⚠️ 人流量预警")
                .setContentText(String.format("%s 当前人流量较大，建议错峰游览", spotName))
                .setSmallIcon(R.drawable.ic_notifications)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setDefaults(Notification.DEFAULT_ALL)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVibrate(new long[]{0, 500, 200, 500, 200, 500})
                .setLights(Color.RED, 1000, 1000)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOngoing(false)
                .setOnlyAlertOnce(false);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            builder.setSound(alarmSound);
        }

        Notification notification = builder.build();

        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        int notificationId = getNotificationId("crowd_" + spotName);
        manager.notify(notificationId, notification);
        Log.d(TAG, "人流量预警通知已发送, ID: " + notificationId);
    }

    public static void showPromotionNotification(Context context, String title, String content) {
        Log.d(TAG, "显示优惠活动通知: " + title);
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("target_fragment", "ticket");
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 1, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                NotificationChannelManager.CHANNEL_ID_PROMOTION)
                .setContentTitle("🎉 " + title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_notifications)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        Notification notification = builder.build();

        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        int notificationId = getNotificationId("promotion");
        manager.notify(notificationId, notification);
        Log.d(TAG, "优惠活动通知已发送, ID: " + notificationId);
    }

    public static void showSystemNotification(Context context, String title, String content) {
        Log.d(TAG, "显示系统公告通知: " + title);
        
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("target_fragment", "notice");
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 2, intent,
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context,
                NotificationChannelManager.CHANNEL_ID_SYSTEM)
                .setContentTitle("📢 " + title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_notifications)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        Notification notification = builder.build();

        NotificationManager manager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        int notificationId = getNotificationId("system");
        manager.notify(notificationId, notification);
        Log.d(TAG, "系统公告通知已发送, ID: " + notificationId);
    }

    private static int getNotificationId(String key) {
        return Math.abs(key.hashCode());
    }
}
