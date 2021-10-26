package com.obdx.meezan.THSCard;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public class NotificationUtil {


    public static Notification getNotification(final Context context,
                                               String contentMessage,
                                               String channelId) {

        Notification.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = showChannel(context, channelId);
        } else {
            builder = new Notification.Builder(context);
        }
        return builder.setAutoCancel(false)
                .setOngoing(true)
                .setAutoCancel(true)
                .setContentTitle(Constants.app_name)
                .setContentText(contentMessage)
                .setPriority(Notification.PRIORITY_MAX)
                .build();
    }

    @TargetApi(26)
    private static Notification.Builder showChannel(final Context context,
                                                    String channelId) {

        NotificationChannel channel = new NotificationChannel(channelId,
                channelId, NotificationManager.IMPORTANCE_LOW);
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .createNotificationChannel(channel);
        //For android 8 and above device
        return new Notification.Builder(context, channelId);
    }

}
