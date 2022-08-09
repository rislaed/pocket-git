package com.aor.pocketgit.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import androidx.appcompat.widget.Toolbar;
import com.aor.pocketgit.R;

public class HelpActivity extends UpdatableActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        setSupportActionBar((Toolbar) findViewById(R.id.action_bar));
        getSupportActionBar().setTitle(R.string.title_activity_help);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((WebView) findViewById(R.id.webview_help)).loadUrl("file:///android_asset/help/index.html");
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
