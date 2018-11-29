package io.trigger.forge.android.modules.parse;

import android.content.Intent;

import com.google.firebase.FirebaseOptions;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.SaveCallback;
import com.parse.VisibilityManager;

import io.trigger.forge.android.core.ForgeApp;
import io.trigger.forge.android.core.ForgeEventListener;
import io.trigger.forge.android.core.ForgeLog;

public class EventListener extends ForgeEventListener {
    @Override
    public void onApplicationCreate() {
        final JsonObject config = ForgeApp.configForPlugin(Constant.MODULE_NAME);

        String server = config.has("server")
            ? config.get("server").getAsString()
            : "https://api.parse.com/1/";

        // interim workaround for: https://github.com/ParsePlatform/Parse-SDK-Android/pull/436
        if (!server.endsWith("/")) {
            server += "/";
        }

        final String applicationId = config.has("applicationId")
            ? config.get("applicationId").getAsString()
            : "";

        final String clientKey = config.has("clientKey")
            ? ForgeApp.configForPlugin(Constant.MODULE_NAME).get("clientKey").getAsString()
            : null;

        final String GCMSenderId = (config.has("android") && config.get("android").getAsJsonObject().has("GCMsenderID"))
            ? config.get("android").getAsJsonObject().get("GCMsenderID").getAsString()
            : null;

        
        FirebaseOptions.Builder builder = new FirebaseOptions.Builder()
                //.setProjectId("")
                .setApplicationId("1:545578024019:android:b3b93ea3cfe98586")
                .setApiKey("AIzaSyCmBw72yy_2RhxPRHipqsq4hvpIy7VtnQM")
                //.setGcmSenderId(GCMSenderId)
                ;


        final Parse.Configuration configuration = new Parse.Configuration.Builder(ForgeApp.getApp())
            .server(server)
            .applicationId(applicationId)
            .clientKey(clientKey)
            .build();

        ForgeLog.d("com.parse.push initializing with server: " + server);
        Parse.initialize(configuration);

        if (GCMSenderId != null) {
            ParseInstallation.getCurrentInstallation().put("GCMSenderId", GCMSenderId);
        }

        ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    ForgeLog.e("com.parse.push failed to initialize: " + e.getLocalizedMessage());
                    e.printStackTrace();
                    return;
                }

                ForgeLog.d("com.parse.push initialized successfully");
                Object deviceToken = ParseInstallation.getCurrentInstallation().get("deviceToken");
                if (deviceToken == null) {
                    ForgeLog.e("deviceToken is null :-(");
                    return;
                }

                ParsePush.subscribeInBackground("", new SaveCallback() {
                    @Override
                    public void done(com.parse.ParseException e) {
                        if (e != null) {
                            ForgeLog.e("com.parse.push failed to subscribe for push: " + e.getLocalizedMessage());
                            e.printStackTrace();
                            return;
                        }
                        ForgeLog.d("com.parse.push successfully subscribed to the broadcast channel.");
                    }
                });
            }
        });

        ForgeLog.i("Initializing Parse and subscribing to default channel.");
    }


    @Override
    public void onNewIntent(Intent intent) {
        if (intent.getExtras() != null && intent.getExtras().get("com.parse.Data") != null) {
            ForgeApp.event("event.messagePushed", new JsonParser().parse((String) intent.getExtras().get("com.parse.Data")));
        }
    }

    @Override
    public void onStart() {
        ForgeLog.d("com.parse.push onStart");
        VisibilityManager.resumed();
    }

    @Override
    public void onStop() {
        ForgeLog.d("com.parse.push onStop");
        VisibilityManager.paused();
    }
}
