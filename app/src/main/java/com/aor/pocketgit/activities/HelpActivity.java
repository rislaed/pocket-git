package com.aor.pocketgit.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.webkit.WebView;
import androidx.appcompat.widget.Toolbar;
import com.aor.pocketgit.R;

public class HelpActivity extends UpdatableActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_help);
        setSupportActionBar((Toolbar) findViewById(R.id.action_bar));
        getSupportActionBar().setTitle((int) R.string.title_activity_help);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((WebView) findViewById(R.id.webview_help)).loadUrl("file:///android_asset/help/index.html");
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
