package com.example.summer.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

public class NotificationChannelManager {

    private static final String TAG = "NotificationChannelMgr";
    public static final String CHANNEL_ID_ALERT = "tourism_alert";
    public static final String CHANNEL_ID_PROMOTION = "tourism_promotion";
    public static final String CHANNEL_ID_SYSTEM = "tourism_system";

    public static void createNotificationChannels(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "开始创建通知渠道");
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            // 先删除旧渠道（如果存在），确保新配置生效
            deleteOldNotificationChannels(notificationManager);

            // 配置音频属性
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            Uri notificationSound = Settings.System.DEFAULT_NOTIFICATION_URI;

            // 人流量预警渠道 - 最高重要性，确保能弹出
            NotificationChannel alertChannel = new NotificationChannel(
                    CHANNEL_ID_ALERT,
                    "人流量预警",
                    NotificationManager.IMPORTANCE_HIGH
            );
            alertChannel.setDescription("景点人流量实时预警通知");
            alertChannel.enableVibration(true);
            alertChannel.setVibrationPattern(new long[]{0, 500, 200, 500, 200, 500});
            alertChannel.setSound(notificationSound, audioAttributes);
            alertChannel.setShowBadge(true);
            alertChannel.enableLights(true);
            alertChannel.setBypassDnd(true);

            // 优惠活动渠道 - 高重要性
            NotificationChannel promotionChannel = new NotificationChannel(
                    CHANNEL_ID_PROMOTION,
                    "优惠活动",
                    NotificationManager.IMPORTANCE_HIGH
            );
            promotionChannel.setDescription("门票优惠、促销活动通知");
            promotionChannel.enableVibration(true);
            promotionChannel.setSound(notificationSound, audioAttributes);
            promotionChannel.setShowBadge(true);

            // 系统公告渠道 - 低重要性
            NotificationChannel systemChannel = new NotificationChannel(
                    CHANNEL_ID_SYSTEM,
                    "系统公告",
                    NotificationManager.IMPORTANCE_LOW
            );
            systemChannel.setDescription("景区公告、维护通知");
            systemChannel.setShowBadge(false);

            // 创建渠道
            notificationManager.createNotificationChannels(
                    java.util.Arrays.asList(alertChannel, promotionChannel, systemChannel)
            );
            Log.d(TAG, "通知渠道创建完成");
        }
    }

    private static void deleteOldNotificationChannels(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(CHANNEL_ID_ALERT);
            notificationManager.deleteNotificationChannel(CHANNEL_ID_PROMOTION);
            notificationManager.deleteNotificationChannel(CHANNEL_ID_SYSTEM);
            Log.d(TAG, "旧通知渠道已删除");
        }
    }

    /**
     * 检查通知渠道是否存在
     * @param context 上下文
     * @param channelId 渠道 ID
     * @return 渠道是否存在
     */
    public static boolean hasNotificationChannel(Context context, String channelId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            
            if (notificationManager != null) {
                java.util.List<NotificationChannel> channels = notificationManager.getNotificationChannels();
                for (NotificationChannel channel : channels) {
                    if (channel.getId().equals(channelId)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
