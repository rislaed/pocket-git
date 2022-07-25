package com.aor.pocketgit.services;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import com.aor.pocketgit.activities.UpdatableActivity;
import com.aor.pocketgit.data.Project;
import com.aor.pocketgit.database.ProjectsDataSource;
import com.aor.pocketgit.utils.CredentialStorage;
import com.aor.pocketgit.utils.GitUtils;
import java.security.cert.CertPathValidatorException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.BranchConfig;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;

public class GitClone extends GitService {
    protected static int sCurrent = 0;
    protected static boolean sRunning = false;

    public GitClone() {
        super("Git Clone Service");
    }

    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
        startNotification("Cloning Repository", GitService.CHANNEL_START_ID);
        boolean usePull = intent.getBooleanExtra("use_pull", false);
        if (this.mProject == null) {
            finishNotificationBigMessage("Clone failed.", "No such project!", UpdatableActivity.CLONE_UPDATE_BROADCAST, (PendingIntent) null, GitService.CHANNEL_ERROR_ID);
            return;
        }
        sRunning = true;
        sCurrent = this.mProject.getId();
        updateProjectState(Project.STATE_CLONING);
        broadcastMessage("Started cloning " + this.mProject.getName(), UpdatableActivity.CLONE_UPDATE_BROADCAST);
        if (usePull) {
            try {
                Repository repository = GitUtils.getRepository(this.mProject);
                repository.getConfig().setString("http", (String) null, "sslVerify", ConfigConstants.CONFIG_KEY_FALSE);
                repository.getConfig().save();
                GitUtils.pullRepository(this.mProject, this, repository.getRemoteNames().iterator().next());
            } catch (TransportException e) {
                Log.e("Pocket Git", "", e);
                updateProjectState(Project.STATE_FAILED);
                if (containsException(e, CertPathValidatorException.class)) {
                    try {
                        Intent intent2 = new Intent(getApplicationContext(), GitClone.class);
                        intent2.putExtra("id", Integer.toString(this.mProject.getId()));
                        intent2.putExtra("use_pull", true);
                        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent2, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_IMMUTABLE : 0);
                        finishNotificationRetry("Failed to clone " + this.mProject.getName() + BranchConfig.LOCAL_REPOSITORY, "Invalid Certificate", UpdatableActivity.CLONE_UPDATE_BROADCAST, "Ignore", pendingIntent, (PendingIntent) null, GitService.CHANNEL_ERROR_ID);
                    } catch (Exception e2) {
                        finishNotificationBigMessage("Failed to clone " + this.mProject.getName() + BranchConfig.LOCAL_REPOSITORY, e.getMessage(), UpdatableActivity.CLONE_UPDATE_BROADCAST, createMainPendingIntent(this), GitService.CHANNEL_ERROR_ID);
                    }
                } else {
                    finishNotificationBigMessage("Failed to clone " + this.mProject.getName() + BranchConfig.LOCAL_REPOSITORY, e.getMessage(), UpdatableActivity.CLONE_UPDATE_BROADCAST, createMainPendingIntent(this), GitService.CHANNEL_ERROR_ID);
                }
                if (e.getMessage().contains("not authorized")) {
                    CredentialStorage.removePassword(this.mProject);
                }
            } catch (Exception e3) {
                String message = e3.getMessage();
                if (e3.getCause() != null) {
                    message = e3.getCause().getMessage();
                }
                Log.e("Pocket Git", "", e3);
                updateProjectState(Project.STATE_FAILED);
                finishNotificationBigMessage("Failed to clone " + this.mProject.getName() + BranchConfig.LOCAL_REPOSITORY, message, UpdatableActivity.CLONE_UPDATE_BROADCAST, createMainPendingIntent(this), GitService.CHANNEL_ERROR_ID);
            }
        } else {
			try {
				GitUtils.cloneRepository(this.mProject, this);
			} catch (Exception e) {
				String message = e.getMessage();
				if (e.getCause() != null) {
					message = e.getCause().getMessage();
				}
				Log.e("Pocket Git", "", e);
				updateProjectState(Project.STATE_FAILED);
				finishNotificationBigMessage("Failed to clone " + this.mProject.getName() + BranchConfig.LOCAL_REPOSITORY, message, UpdatableActivity.CLONE_UPDATE_BROADCAST, createMainPendingIntent(this), GitService.CHANNEL_ERROR_ID);
			}
        }
        updateProjectState(Project.STATE_CLONED);
        finishNotificationSmallMessage("Finished cloning " + this.mProject.getName() + BranchConfig.LOCAL_REPOSITORY, UpdatableActivity.CLONE_UPDATE_BROADCAST, createProjectPendingIntent(this, this.mProject.getId()), GitService.CHANNEL_FINISHED_ID);
        sRunning = false;
    }

    private boolean containsException(Throwable e, Class<?> needle) {
        if (needle.isInstance(e)) {
            return true;
        }
        if (e.getCause() == null) {
            return false;
        }
        return containsException(e.getCause(), needle);
    }

    protected void updateProjectState(String state) {
        ProjectsDataSource datasource = new ProjectsDataSource(this);
        datasource.open();
        datasource.updateState(Integer.toString(this.mId), state);
        datasource.close();
    }

    public static boolean isCloning(int id) {
        return sRunning && sCurrent == id;
    }
}
