package com.aor.pocketgit.tasks;

import android.os.AsyncTask;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.lib.Repository;

public class CheckoutRemoteTask extends AsyncTask<Object, Void, Exception> {
    private CheckoutRemoteListener mListener;
    private String mName;

    public interface CheckoutRemoteListener {
        void checkoutFailed(Exception exc);

        void checkoutStarted();

        void conflictDetected();

        void refAlreadyExists();

        void remoteCheckedOut(String str);
    }

    protected void onPreExecute() {
        this.mListener.checkoutStarted();
    }

    protected Exception doInBackground(Object... params) {
        Repository repository = (Repository) params[0];
        this.mName = (String) params[1];
        try {
            // TODO: String or RevCommit
			new Git(repository).checkout().setCreateBranch(true).setName(this.mName).setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK).setStartPoint((String) params[2]).call();
            repository.close();
            return null;
        } catch (Exception e) {
            repository.close();
            return e;
        } catch (Throwable th) {
            repository.close();
            throw th;
        }
    }

    protected void onPostExecute(Exception e) {
        if (this.mListener == null) {
            return;
        }
        if (e == null) {
            this.mListener.remoteCheckedOut(this.mName);
        } else if (e instanceof RefAlreadyExistsException) {
            this.mListener.refAlreadyExists();
        } else if (e instanceof CheckoutConflictException) {
            this.mListener.conflictDetected();
        } else {
            this.mListener.checkoutFailed(e);
        }
    }

    public CheckoutRemoteTask setListener(CheckoutRemoteListener listener) {
        this.mListener = listener;
        return this;
    }
}
