package com.aor.pocketgit.activities;

import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import com.aor.pocketgit.R;

public class PreferencesActivity extends UpdatableActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preferences);
        setSupportActionBar((Toolbar) findViewById(R.id.action_bar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener((PreferencesFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_preferences));
    }

    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener((PreferencesFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_preferences));
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
