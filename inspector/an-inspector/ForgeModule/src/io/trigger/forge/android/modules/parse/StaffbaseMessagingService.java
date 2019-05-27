package io.trigger.forge.android.modules.parse;

import android.os.Handler;
import android.os.Message;

import com.google.firebase.messaging.RemoteMessage;
import com.parse.fcm.ParseFirebaseMessagingService;

import io.trigger.forge.android.core.ForgeLog;

/**
 * Overridden messaging service, which is aware that forge/parse has been initialized successfully
 * before handling notifications (see: https://mitarbeiterapp.atlassian.net/browse/RE-492).
 */
public class StaffbaseMessagingService extends ParseFirebaseMessagingService {

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        if (EventListener.isInitialized()) {
            super.onMessageReceived(remoteMessage);
        } else {
            ForgeLog.i("com.parse.push onMessageReceived -- wait for initialization");
            EventListener.addOnInitializedListener(new Handler.Callback() {
                @Override
                public boolean handleMessage(Message msg) {
                    StaffbaseMessagingService.super.onMessageReceived(remoteMessage);
                    return true;
                }
            });
        }
    }
}
