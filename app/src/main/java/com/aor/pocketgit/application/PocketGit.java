package com.aor.pocketgit.application;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.os.StrictMode;
import com.aor.pocketgit.services.GitService;

public class PocketGit extends Application {
    public void onCreate() {
        super.onCreate();
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder().build());
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
            NotificationChannel channelStart = new NotificationChannel(GitService.CHANNEL_START_ID, GitService.CHANNEL_START_NAME, 2);
            channelStart.setDescription(GitService.CHANNEL_START_DESCRIPTION);
            notificationManager.createNotificationChannel(channelStart);
            NotificationChannel channelFinished = new NotificationChannel(GitService.CHANNEL_FINISHED_ID, GitService.CHANNEL_FINISHED_NAME, 2);
            channelFinished.setDescription(GitService.CHANNEL_FINISHED_DESCRIPTION);
            notificationManager.createNotificationChannel(channelFinished);
            NotificationChannel channelError = new NotificationChannel(GitService.CHANNEL_ERROR_ID, GitService.CHANNEL_ERROR_NAME, 4);
            channelError.setDescription(GitService.CHANNEL_ERROR_DESCRIPTION);
            notificationManager.createNotificationChannel(channelError);
        }
    }
}
