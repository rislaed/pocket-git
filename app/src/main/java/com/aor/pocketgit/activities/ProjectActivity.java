package com.aor.pocketgit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceManager;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.aor.pocketgit.R;
import com.aor.pocketgit.utils.FontUtils;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import java.io.File;
import org.eclipse.jgit.lib.ConfigConstants;

public class ProjectActivity extends UpdatableActivity {
    public static final int ACTION_CREATE = 1;
    public static final int ACTION_EDIT = 2;
    public static final int RESULT_CANCEL = 2;
    public static final int RESULT_SAVE = 1;
    private String mOriginalId;
    private TextWatcher mTextWatcher;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_project);
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        final String defaultPath = PreferenceManager.getDefaultSharedPreferences(this).getString("pref_default_directory", getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath());
        final EditText localPath = (EditText) findViewById(R.id.edit_local_path);
        localPath.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent(ProjectActivity.this, PickerActivity.class);
                String initialPath = localPath.getText().toString();
                if (initialPath.trim().equals("")) {
                    initialPath = defaultPath;
                }
                intent.putExtra("local_path", initialPath);
                intent.putExtra("request_code", 1);
                ProjectActivity.this.startActivityForResult(intent, 1);
            }
        });
        ((EditText) findViewById(R.id.edit_private_key)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                Intent intent = new Intent(ProjectActivity.this, PickerActivity.class);
                intent.putExtra("request_code", 2);
                ProjectActivity.this.startActivityForResult(intent, 2);
            }
        });
        Spinner spinnerAuthentication = (Spinner) findViewById(R.id.spinner_authentication);
        spinnerAuthentication.setAdapter(ArrayAdapter.createFromResource(this, R.array.values_authentication, 17367049));
        spinnerAuthentication.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> adapterView, View arg1, int arg2, long arg3) {
                ProjectActivity.this.updateAuthentication();
            }

            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        ((EditText) findViewById(R.id.edit_url)).addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                ProjectActivity.this.updateAuthentication();
            }
        });
        updateAuthentication();
        this.mOriginalId = getIntent().getStringExtra("id");
        if (this.mOriginalId != null) {
            getSupportActionBar().setSubtitle((CharSequence) "Edit");
        } else {
            getSupportActionBar().setSubtitle((CharSequence) "Create");
        }
        if (getIntent().getStringExtra(ConfigConstants.CONFIG_KEY_NAME) != null) {
            ((EditText) findViewById(R.id.edit_name)).setText(getIntent().getStringExtra(ConfigConstants.CONFIG_KEY_NAME));
        }
        if (getIntent().getStringExtra("url") != null) {
            ((EditText) findViewById(R.id.edit_url)).setText(getIntent().getStringExtra("url"));
        }
        if (getIntent().getStringExtra("local_path") != null) {
            ((EditText) findViewById(R.id.edit_local_path)).setText(getIntent().getStringExtra("local_path"));
        }
        ((Spinner) findViewById(R.id.spinner_authentication)).setSelection(getIntent().getIntExtra("authentication", 0));
        if (getIntent().getStringExtra("username") != null) {
            ((EditText) findViewById(R.id.edit_username)).setText(getIntent().getStringExtra("username"));
        }
        if (getIntent().getStringExtra("password") != null) {
            ((EditText) findViewById(R.id.edit_password)).setText(getIntent().getStringExtra("password"));
        }
        if (getIntent().getStringExtra("privatekey") != null) {
            ((EditText) findViewById(R.id.edit_private_key)).setText(getIntent().getStringExtra("privatekey"));
        }
        ((EditText) findViewById(R.id.edit_name)).addTextChangedListener(new TextWatcher() {
            boolean matches = false;

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                this.matches = localPath.getText().toString().trim().equals("") || localPath.getText().toString().trim().equals(new File(new StringBuilder().append(defaultPath.trim()).append("/").append(s).toString()).getAbsolutePath());
            }

            public void afterTextChanged(Editable s) {
                if (this.matches) {
                    localPath.setText(new File(defaultPath + "/" + s.toString().trim()).getAbsolutePath());
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected void onResume() {
        this.mTextWatcher = new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ProjectActivity.this.calculateErrors();
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        };
        ((EditText) findViewById(R.id.edit_name)).addTextChangedListener(this.mTextWatcher);
        ((EditText) findViewById(R.id.edit_username)).addTextChangedListener(this.mTextWatcher);
        super.onResume();
    }

    protected void onPause() {
        ((EditText) findViewById(R.id.edit_name)).removeTextChangedListener(this.mTextWatcher);
        ((EditText) findViewById(R.id.edit_username)).removeTextChangedListener(this.mTextWatcher);
        super.onPause();
    }

    private void calculateErrors() {
        EditText editName = (EditText) findViewById(R.id.edit_name);
        EditText editUsername = (EditText) findViewById(R.id.edit_username);
        EditText editUrl = (EditText) findViewById(R.id.edit_url);
        EditText localPath = (EditText) findViewById(R.id.edit_local_path);
        EditText privateKey = (EditText) findViewById(R.id.edit_private_key);
        if (editName.getText().toString().trim().equals("")) {
            editName.setError("Project name cannot be empty");
        } else {
            editName.setError((CharSequence) null);
        }
        if (editUrl.getText().toString().trim().equals("")) {
            editUrl.setError("URL cannot be empty");
        } else {
            editUrl.setError((CharSequence) null);
        }
        editUsername.setError((CharSequence) null);
        if (editUsername.getText().toString().trim().equals("")) {
            editUsername.setError("Username cannot be empty");
        } else {
            editUsername.setError((CharSequence) null);
        }
        localPath.setError(getLocalPathError(localPath.getText().toString().trim()));
        privateKey.setError(getPrivateKeyError(privateKey.getText().toString().trim()));
    }

    private String getLocalPathError(String localPath) {
        if (localPath.equals("")) {
            return "Local path not selected";
        }
        File localPathFile = new File(localPath);
        if (!localPathFile.exists() || localPathFile.isDirectory()) {
            return null;
        }
        return "Local path must be a directory";
    }

    private String getPrivateKeyError(String privateKey) {
        if (privateKey.equals("")) {
            return "Private key not selected";
        }
        File localPrivateKeyFile = new File(privateKey);
        if (!localPrivateKeyFile.exists()) {
            return "Private key does not exist";
        }
        if (localPrivateKeyFile.isDirectory()) {
            return "Private key is not a file";
        }
        try {
            KeyPair.load(new JSch(), localPrivateKeyFile.getAbsolutePath());
            return null;
        } catch (JSchException e) {
            return "Invalid private key";
        }
    }

    protected void updateAuthentication() {
        int i;
        int i2;
        int i3;
        int i4;
        int i5 = 0;
        EditText editUrl = (EditText) findViewById(R.id.edit_url);
        int selected = (int) ((Spinner) findViewById(R.id.spinner_authentication)).getSelectedItemId();
        findViewById(R.id.label_username).setVisibility(selected == 1 ? 0 : 8);
        View findViewById = findViewById(R.id.label_password);
        if (selected == 1) {
            i = 0;
        } else {
            i = 8;
        }
        findViewById.setVisibility(i);
        View findViewById2 = findViewById(R.id.edit_username);
        if (selected == 1) {
            i2 = 0;
        } else {
            i2 = 8;
        }
        findViewById2.setVisibility(i2);
        View findViewById3 = findViewById(R.id.edit_password);
        if (selected == 1) {
            i3 = 0;
        } else {
            i3 = 8;
        }
        findViewById3.setVisibility(i3);
        if (editUrl.getText().toString().contains("@")) {
            findViewById(R.id.label_username).setVisibility(8);
            findViewById(R.id.edit_username).setVisibility(8);
        }
        View findViewById4 = findViewById(R.id.label_private_key);
        if (selected == 2) {
            i4 = 0;
        } else {
            i4 = 8;
        }
        findViewById4.setVisibility(i4);
        View findViewById5 = findViewById(R.id.edit_private_key);
        if (selected != 2) {
            i5 = 8;
        }
        findViewById5.setVisibility(i5);
    }

    private void createResult() {
        Intent intent = new Intent();
        intent.putExtra("id", this.mOriginalId);
        intent.putExtra(ConfigConstants.CONFIG_KEY_NAME, ((EditText) findViewById(R.id.edit_name)).getText().toString().trim());
        intent.putExtra("url", ((EditText) findViewById(R.id.edit_url)).getText().toString());
        intent.putExtra("local_path", ((EditText) findViewById(R.id.edit_local_path)).getText().toString().trim());
        intent.putExtra("authentication", ((Spinner) findViewById(R.id.spinner_authentication)).getSelectedItemPosition());
        intent.putExtra("username", ((EditText) findViewById(R.id.edit_username)).getText().toString());
        intent.putExtra("password", ((EditText) findViewById(R.id.edit_password)).getText().toString());
        intent.putExtra("privatekey", ((EditText) findViewById(R.id.edit_private_key)).getText().toString());
        setResult(1, intent);
    }

    private String getErrorMessage() {
        LinearLayout layout = (LinearLayout) findViewById(R.id.layout_project);
        for (int index = 0; index < layout.getChildCount(); index++) {
            if (layout.getChildAt(index) instanceof EditText) {
                EditText edit = (EditText) layout.getChildAt(index);
                if (edit.getError() != null && edit.getVisibility() == 0) {
                    return edit.getError().toString();
                }
            }
        }
        return null;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == 1) {
            ((EditText) findViewById(R.id.edit_local_path)).setText(data.getStringExtra("selected_folder"));
            calculateErrors();
        }
        if (requestCode == 2 && resultCode == 1) {
            ((EditText) findViewById(R.id.edit_private_key)).setText(data.getStringExtra("selected_file"));
            calculateErrors();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_cancel_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
            case R.id.action_cancel:
                cancel();
                break;
            case R.id.action_save:
                calculateErrors();
                String error = getErrorMessage();
                if (error != null) {
                    Toast.makeText(this, error, 0).show();
                    break;
                } else {
                    createResult();
                    finish();
                    break;
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void cancel() {
        FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this)
				.title((CharSequence) "Close")
				.iconRes(R.drawable.ic_close)
				.content((CharSequence) "Close without saving?")
				.positiveText(R.string.button_close)
				.negativeText(R.string.button_cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
            public void onClick(MaterialDialog dialog, DialogAction which) {
                ProjectActivity.this.setResult(2);
                ProjectActivity.this.finish();
            }
        }).show().getWindow().getDecorView());
    }
}
