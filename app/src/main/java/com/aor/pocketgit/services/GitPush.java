package com.aor.pocketgit.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;
import com.aor.pocketgit.activities.UpdatableActivity;
import com.aor.pocketgit.utils.CredentialStorage;
import com.aor.pocketgit.utils.GitUtils;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.BranchConfig;

public class GitPush extends GitService {
    public GitPush() {
        super("Git Push Service");
    }

    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        startNotification("Pushing to Remote", GitService.CHANNEL_START_ID);
        String remote = intent.getStringExtra("remote");
        boolean pushTags = intent.getBooleanExtra("push_tags", false);
        if (this.mProject == null) {
            finishNotificationBigMessage("Failed to push.", "No such project!", UpdatableActivity.PUSH_COMPLETED, (PendingIntent) null, GitService.CHANNEL_ERROR_ID);
            return;
        }
        try {
            String messages = GitUtils.pushRepository(this.mProject, this, remote, pushTags);
            if (messages.trim().equals("")) {
                finishNotificationSmallMessage("Finished pushing " + this.mProject.getName() + BranchConfig.LOCAL_REPOSITORY, UpdatableActivity.PUSH_COMPLETED, createProjectPendingIntent(this, this.mProject.getId()), GitService.CHANNEL_FINISHED_ID);
            } else if (messages.contains("error:") || messages.contains("rejected")) {
                finishNotificationBigMessage("Failed to push " + this.mProject.getName() + BranchConfig.LOCAL_REPOSITORY, messages, UpdatableActivity.PUSH_COMPLETED, createProjectPendingIntent(this, this.mProject.getId()), GitService.CHANNEL_ERROR_ID);
            } else {
                finishNotificationBigMessage("Finished pushing " + this.mProject.getName() + BranchConfig.LOCAL_REPOSITORY, messages, UpdatableActivity.PUSH_COMPLETED, createProjectPendingIntent(this, this.mProject.getId()), GitService.CHANNEL_FINISHED_ID);
            }
        } catch (TransportException e) {
            Log.e("Pocket Git", "", e);
            finishNotificationBigMessage("Failed to push " + this.mProject.getName() + BranchConfig.LOCAL_REPOSITORY, e.getMessage(), UpdatableActivity.PUSH_COMPLETED, createProjectPendingIntent(this, this.mProject.getId()), GitService.CHANNEL_ERROR_ID);
            if (e.getMessage().contains("not authorized")) {
                CredentialStorage.removePassword(this.mProject);
            }
        } catch (Exception e2) {
            String message = e2.getMessage();
            if (e2.getCause() != null) {
                message = e2.getCause().getMessage();
            }
            Log.e("Pocket Git", "", e2);
            finishNotificationBigMessage("Failed to push " + this.mProject.getName() + BranchConfig.LOCAL_REPOSITORY, message, UpdatableActivity.PUSH_COMPLETED, createProjectPendingIntent(this, this.mProject.getId()), GitService.CHANNEL_ERROR_ID);
        }
    }
}
