package com.aor.pocketgit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.preference.Preference;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceManager;
import android.provider.Settings;
import android.widget.Toast;
import com.aor.pocketgit.R;

public class PreferencesFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        initSummary(getPreferenceScreen());
        findPreference("pref_default_directory").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                String localPath = PreferenceManager.getDefaultSharedPreferences(PreferencesFragment.this.getActivity()).getString("pref_default_directory", getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
                Intent intent = new Intent(PreferencesFragment.this.getActivity(), PickerActivity.class);
                intent.putExtra("local_path", localPath);
                intent.putExtra("request_code", 1);
                PreferencesFragment.this.startActivityForResult(intent, 1);
                return true;
            }
        });
        Preference manageExternalStoragePref = findPreference("pref_manage_external_storage");
        manageExternalStoragePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                try {
                    Uri uri = Uri.parse("package:" + getActivity().getPackageName());
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri);
                    intent.putExtra("request_code", 2);
                    PreferencesFragment.this.startActivityForResult(intent, 2);
                } catch (Exception e) {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    intent.putExtra("request_code", 2);
                    PreferencesFragment.this.startActivityForResult(intent, 2);
                }
                return true;
            }
        });
        manageExternalStoragePref.setEnabled(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePrefSummary(findPreference(key));
    }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
            return;
        }
        updatePrefSummary(p);
    }

    private void updatePrefSummary(Preference p) {
        if (p instanceof EditTextPreference) {
            p.setSummary(((EditTextPreference) p).getText());
        }
        if (p != null && p.getKey().equals("pref_default_directory")) {
			p.setSummary(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("pref_default_directory", getActivity().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()));
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {
        	if (requestCode == 1) {
        		PreferenceManager.getDefaultSharedPreferences(getActivity()).edit().putString("pref_default_directory", data.getStringExtra("selected_folder")).commit();
        		updatePrefSummary(findPreference("pref_default_directory"));
        	}
        	if (requestCode == 2) {
        		Toast.makeText(getActivity(), "Permission granted.", Toast.LENGTH_LONG).show();
        	}
        }
	}

	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String type) {
		
	}
}
