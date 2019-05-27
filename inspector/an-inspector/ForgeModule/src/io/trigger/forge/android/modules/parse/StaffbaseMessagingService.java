package io.trigger.forge.android.modules.parse;

import android.os.Handler;
import android.os.Message;

import com.google.firebase.messaging.RemoteMessage;
import com.parse.fcm.ParseFirebaseMessagingService;

import io.trigger.forge.android.core.ForgeLog;

public class StaffbaseMessagingService extends ParseFirebaseMessagingService {

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {

        ForgeLog.i("com.parse.push onMessageReceived");

        if (EventListener.isInitialized()) {
            ForgeLog.i("com.parse.push process message");
            super.onMessageReceived(remoteMessage);
        } else {
            EventListener.addOnInitializedListener(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    ForgeLog.i("com.parse.push process message");
                    StaffbaseMessagingService.super.onMessageReceived(remoteMessage);
                    return true;
                }
            });
        }
    }
}
