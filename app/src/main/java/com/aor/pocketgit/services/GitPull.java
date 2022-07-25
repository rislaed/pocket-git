package com.aor.pocketgit.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import com.aor.pocketgit.activities.UpdatableActivity;
import com.aor.pocketgit.utils.CredentialStorage;
import com.aor.pocketgit.utils.GitUtils;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.BranchConfig;

public class GitPull extends GitService {
    public GitPull() {
        super("Git Pull Service");
    }

    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        startNotification("Pulling from Remote", GitService.CHANNEL_START_ID);
        String remote = intent.getStringExtra("remote");
        if (this.mProject == null) {
            finishNotificationBigMessage("Failed to pull.", "No such project!", UpdatableActivity.PULL_COMPLETED, (PendingIntent) null, GitService.CHANNEL_ERROR_ID);
            return;
        }
        try {
            GitUtils.pullRepository(this.mProject, this, remote);
            finishNotificationSmallMessage("Finished pulling " + this.mProject.getName() + BranchConfig.LOCAL_REPOSITORY, UpdatableActivity.PULL_COMPLETED, createProjectPendingIntent(this, this.mProject.getId()), GitService.CHANNEL_FINISHED_ID);
        } catch (TransportException e) {
            Log.e("Pocket Git", "", e);
            finishNotificationBigMessage("Failed to pull " + this.mProject.getName() + BranchConfig.LOCAL_REPOSITORY, e.getMessage(), UpdatableActivity.PULL_COMPLETED, createProjectPendingIntent(this, this.mProject.getId()), GitService.CHANNEL_ERROR_ID);
            if (e.getMessage().contains("not authorized")) {
                CredentialStorage.removePassword(this.mProject);
            }
        } catch (Exception e2) {
            String message = e2.getMessage();
            if (e2.getCause() != null) {
                message = e2.getCause().getMessage();
            }
            Log.e("Pocket Git", "", e2);
            finishNotificationBigMessage("Failed to pull " + this.mProject.getName() + BranchConfig.LOCAL_REPOSITORY, message, UpdatableActivity.PULL_COMPLETED, createProjectPendingIntent(this, this.mProject.getId()), GitService.CHANNEL_ERROR_ID);
        }
    }
}
