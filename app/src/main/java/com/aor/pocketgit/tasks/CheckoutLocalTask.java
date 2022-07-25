package com.aor.pocketgit.tasks;

import android.os.AsyncTask;
import android.util.Log;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;

public class CheckoutLocalTask extends AsyncTask<Object, Void, Exception> {
    private String mDisplayName;
    private CheckoutTaskListener mListener;

    public interface CheckoutTaskListener {
        void checkedOut(String str);

        void conflict(String str);

        void error(String str);
    }

    protected Exception doInBackground(Object... params) {
        Repository repository = (Repository) params[0];
        String name = (String) params[1];
        this.mDisplayName = name.substring(0, 8);
        if (name.startsWith(Constants.R_HEADS)) {
            this.mDisplayName = name.substring(11);
        }
        if (name.startsWith(Constants.R_TAGS)) {
            this.mDisplayName = name.substring(10);
        }
        try {
            new Git(repository).checkout().setName(name).call();
            repository.close();
            return null;
        } catch (Exception e) {
            Log.e("Pocket Git", "", e);
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
            this.mListener.checkedOut(this.mDisplayName);
        } else if (e instanceof CheckoutConflictException) {
            this.mListener.conflict(this.mDisplayName);
        } else {
            this.mListener.error(this.mDisplayName);
        }
    }

    public CheckoutLocalTask setListener(CheckoutTaskListener listener) {
        this.mListener = listener;
        return this;
    }
}
