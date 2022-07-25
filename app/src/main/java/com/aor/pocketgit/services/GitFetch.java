package com.aor.pocketgit.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import com.aor.pocketgit.activities.UpdatableActivity;
import com.aor.pocketgit.utils.CredentialStorage;
import com.aor.pocketgit.utils.GitUtils;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.BranchConfig;

public class GitFetch extends GitService {
    public GitFetch() {
        super("Git Pull Service");
    }

    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        startNotification("Fetching from Remote", GitService.CHANNEL_START_ID);
        String remote = intent.getStringExtra("remote");
        if (this.mProject == null) {
            finishNotificationBigMessage("Failed to fetch.", "No such project!", UpdatableActivity.FETCH_COMPLETED, (PendingIntent) null, GitService.CHANNEL_ERROR_ID);
            return;
        }
        try {
            GitUtils.fetchRepository(this.mProject, this, remote);
            finishNotificationSmallMessage("Finished fetching " + this.mProject.getName() + BranchConfig.LOCAL_REPOSITORY, UpdatableActivity.FETCH_COMPLETED, createProjectPendingIntent(this, this.mProject.getId()), GitService.CHANNEL_FINISHED_ID);
        } catch (TransportException e) {
            Log.e("Pocket Git", "", e);
            finishNotificationBigMessage("Failed to fetch " + this.mProject.getName() + BranchConfig.LOCAL_REPOSITORY, e.getMessage(), UpdatableActivity.FETCH_COMPLETED, createProjectPendingIntent(this, this.mProject.getId()), GitService.CHANNEL_ERROR_ID);
            if (e.getMessage().contains("not authorized")) {
                CredentialStorage.removePassword(this.mProject);
            }
        } catch (Exception e2) {
            String message = e2.getMessage();
            if (e2.getCause() != null) {
                message = e2.getCause().getMessage();
            }
            Log.e("Pocket Git", "", e2);
            finishNotificationBigMessage("Failed to fetch " + this.mProject.getName() + BranchConfig.LOCAL_REPOSITORY, message, UpdatableActivity.FETCH_COMPLETED, createProjectPendingIntent(this, this.mProject.getId()), GitService.CHANNEL_ERROR_ID);
        }
    }
}
