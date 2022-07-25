package com.aor.pocketgit.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import com.aor.pocketgit.R;
import com.google.android.material.snackbar.Snackbar;
import com.aor.pocketgit.utils.FontUtils;

public abstract class UpdatableActivity extends AppCompatActivity {
    public static final String CLONE_UPDATE_BROADCAST = "CLONE_COMPLETED";
    public static final String FETCH_COMPLETED = "FECTH_COMPLETED";
    public static final String PULL_COMPLETED = "PULL_COMPLETED";
    public static final String PUSH_COMPLETED = "PUSH_COMPLETED";
    public static final String UPDATE_BROADCAST = "UPDATE_BROADCAST";
    private BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            UpdatableActivity.this.onUpdateData(intent);
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void onUpdateData(Intent intent) {
        Toast.makeText(this, intent.getStringExtra("key"), 0).show();
    }

    protected void onResume() {
        registerReceiver(this.mUpdateReceiver, new IntentFilter(UPDATE_BROADCAST));
        super.onResume();
    }

    protected void onPause() {
        unregisterReceiver(this.mUpdateReceiver);
        super.onPause();
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        FontUtils.setRobotoFont(this, getWindow().getDecorView());
    }

    protected void showErrorSnackBar(View view, String message) {
        final Snackbar snackbar = Snackbar.make(view, (CharSequence) message, -2);
        snackbar.setAction((CharSequence) "Ok", (View.OnClickListener) new View.OnClickListener() {
            public void onClick(View v) {
                snackbar.dismiss();
            }
        });
        snackbar.show();
    }

    protected void showSnackBar(View view, String message, int length) {
        Snackbar.make(view, (CharSequence) Html.fromHtml("<font color=\"#ffffff\">" + message + "</font>"), length).show();
    }

    protected void showSnackBar(View view, String message, int length, String actionMessage, View.OnClickListener action) {
        Snackbar snackbar = Snackbar.make(view, (CharSequence) Html.fromHtml("<font color=\"#ffffff\">" + message + "</font>"), length);
        snackbar.setAction((CharSequence) actionMessage, action);
        snackbar.show();
    }
}
