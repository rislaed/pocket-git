package com.aor.pocketgit.services;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.aor.pocketgit.R;
import com.aor.pocketgit.activities.FilesActivity;
import com.aor.pocketgit.activities.MainActivity;
import com.aor.pocketgit.activities.UpdatableActivity;
import com.aor.pocketgit.data.Project;
import com.aor.pocketgit.database.ProjectsDataSource;

public abstract class GitService extends IntentService {
    public static final String CHANNEL_ERROR_DESCRIPTION = "A git service has failed (clone, fetch, pull our push)";
    public static final String CHANNEL_ERROR_ID = "ERROR";
    public static final CharSequence CHANNEL_ERROR_NAME = "Service Error";
    public static final String CHANNEL_FINISHED_DESCRIPTION = "A git service has finished (clone, fetch, pull our push)";
    public static final String CHANNEL_FINISHED_ID = "FINISHED";
    public static final CharSequence CHANNEL_FINISHED_NAME = "Service Completion";
    public static final String CHANNEL_START_DESCRIPTION = "A git service has started (clone, fetch, pull our push)";
    public static final String CHANNEL_START_ID = "START";
    public static final CharSequence CHANNEL_START_NAME = "Service Start";
    private long lastTime = 0;
    private NotificationCompat.Builder mBuilder;
    protected int mId;
    private NotificationManager mNotifyManager;
    protected Project mProject;

    public GitService(String name) {
        super(name);
    }

    protected void onHandleIntent(Intent intent) {
        this.mId = Integer.valueOf(intent.getStringExtra("id")).intValue();
        ProjectsDataSource datasource = new ProjectsDataSource(this);
        datasource.open();
        this.mProject = datasource.getProject(Integer.toString(this.mId));
        datasource.close();
        this.mNotifyManager = (NotificationManager) getSystemService("notification");
        this.mBuilder = new NotificationCompat.Builder(this, CHANNEL_START_ID);
    }

    protected void broadcastMessage(String message, String type) {
        Intent broadcast = new Intent(UpdatableActivity.UPDATE_BROADCAST);
        broadcast.putExtra("type", type);
        broadcast.putExtra("key", message);
        sendBroadcast(broadcast);
    }

    public void finishNotificationSmallMessage(String message, String type, PendingIntent pendingIntent, String channelId) {
        this.mBuilder.setChannelId(channelId);
        finishNotification(message, pendingIntent, channelId);
        broadcastMessage(message, type);
    }

    public void finishNotificationBigMessage(String message, String bigMessage, String broadcastType, PendingIntent pendingIntent, String channelId) {
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText(bigMessage);
        style.setSummaryText(message);
        this.mBuilder.setStyle(style);
        finishNotificationSmallMessage(message, broadcastType, pendingIntent, channelId);
    }

    public void finishNotificationRetry(String message, String bigMessage, String broadcastType, String action, PendingIntent actionIntent, PendingIntent pendingIntent, String channelId) {
        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.bigText(bigMessage);
        style.setSummaryText(message);
        this.mBuilder.setStyle(style);
        this.mBuilder.addAction(new NotificationCompat.Action(R.drawable.ic_action_refresh, action, actionIntent));
        finishNotificationSmallMessage(message, broadcastType, pendingIntent, channelId);
    }

    private void finishNotification(String message, PendingIntent pendingIntent, String channelId) {
        this.mBuilder.setOngoing(false);
        this.mBuilder.setChannelId(channelId);
        if (pendingIntent != null) {
            this.mBuilder.setAutoCancel(true);
            this.mBuilder.setContentIntent(pendingIntent);
        }
        this.mBuilder.setProgress(0, 0, false);
        this.mBuilder.setContentText(message);
        this.mNotifyManager.notify(0, this.mBuilder.build());
    }

    protected void startNotification(String title, String channelId) {
        this.mBuilder.setOngoing(true);
        this.mBuilder.setChannelId(channelId);
        this.mBuilder.setContentTitle(title).setContentText("Starting").setSmallIcon(R.drawable.ic_notification);
        Notification notification = this.mBuilder.build();
        startForeground(this.mId, notification);
        this.mNotifyManager.notify(this.mId, notification);
    }

    public void updateNotification(int total, int done, String channelId) {
        long currentTime = System.currentTimeMillis();
        if (currentTime > this.lastTime + 1000) {
            this.mBuilder.setProgress(total, done, false);
            this.mBuilder.setChannelId(channelId);
            this.mNotifyManager.notify(this.mId, this.mBuilder.build());
            this.lastTime = currentTime;
        }
    }

    public void updateNotificationMessage(int total, int done, String message, String channelId) {
        long currentTime = System.currentTimeMillis();
        if (currentTime > this.lastTime + 1000) {
            this.mBuilder.setProgress(total, done, false);
            this.mBuilder.setContentText(message);
            this.mBuilder.setChannelId(channelId);
            this.mNotifyManager.notify(this.mId, this.mBuilder.build());
            this.lastTime = currentTime;
        }
    }

    protected PendingIntent createProjectPendingIntent(Context context, int projectId) {
        Intent notificationIntent = new Intent(context, FilesActivity.class);
        notificationIntent.setFlags(603979776);
        notificationIntent.putExtra("id", String.valueOf(projectId));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        	return PendingIntent.getActivity(context, projectId, notificationIntent, PendingIntent.FLAG_IMMUTABLE | 134217728);
        }
        return PendingIntent.getActivity(context, projectId, notificationIntent, 134217728);
    }

    protected PendingIntent createMainPendingIntent(Context context) {
        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setFlags(603979776);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        	return PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE | 134217728);
        }
        return PendingIntent.getActivity(context, 0, notificationIntent, 134217728);
    }
}
