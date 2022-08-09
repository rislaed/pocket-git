package com.aor.pocketgit.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.aor.pocketgit.R;
import com.aor.pocketgit.adapters.FolderAdapter;
import com.aor.pocketgit.utils.FontUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class PickerActivity extends UpdatableActivity {
    protected static final int RESULT_CANCELED = 2;
    protected static final int RESULT_SELECTED = 1;
    public static final int SELECT_FILE = 2;
    public static final int SELECT_FOLDER = 1;
    private File mCurrentFolder;
    private int mRequestCode;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);
        setSupportActionBar((Toolbar) findViewById(R.id.action_bar));
        this.mRequestCode = getIntent().getIntExtra("request_code", 1);
        if (this.mRequestCode == 2) {
            getSupportActionBar().setTitle(R.string.title_activity_file_picker);
        }
        String localPath = getIntent().getStringExtra("local_path");
        if (localPath != null && new File(localPath).isDirectory()) {
            this.mCurrentFolder = new File(localPath);
        }
        if (this.mCurrentFolder == null) {
            this.mCurrentFolder = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
        }
        final ListView listFolders = (ListView) findViewById(R.id.list_folders);
        refreshFiles(listFolders);
        listFolders.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                File selected = (File) listFolders.getItemAtPosition(position);
                if (selected.isDirectory()) {
                    File unused = PickerActivity.this.mCurrentFolder = selected;
                    PickerActivity.this.refreshFiles(listFolders);
                } else if (PickerActivity.this.mRequestCode == 2) {
                    Intent intent = new Intent();
                    intent.putExtra("selected_file", selected.getAbsolutePath());
                    PickerActivity.this.setResult(1, intent);
                    PickerActivity.this.finish();
                }
            }
        });
        ((Button) findViewById(R.id.button_cancel)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                PickerActivity.this.setResult(2);
                PickerActivity.this.finish();
            }
        });
        Button buttonSelect = (Button) findViewById(R.id.button_select);
        if (this.mRequestCode == 1) {
            buttonSelect.setOnClickListener(new View.OnClickListener() {
                public void onClick(View arg0) {
                    Intent intent = new Intent();
                    intent.putExtra("selected_folder", PickerActivity.this.mCurrentFolder.getAbsolutePath());
                    PickerActivity.this.setResult(1, intent);
                    PickerActivity.this.finish();
                }
            });
        } else {
            buttonSelect.setVisibility(8);
        }
        ((LinearLayout) findViewById(R.id.parent_folder)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                File unused = PickerActivity.this.mCurrentFolder = PickerActivity.this.mCurrentFolder.getParentFile();
                PickerActivity.this.refreshFiles(listFolders);
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void refreshFiles(ListView listFolders) {
        listFolders.setAdapter(new FolderAdapter(this, getFolders(this.mCurrentFolder)));
        File parentFolder = this.mCurrentFolder.getParentFile();
        if (parentFolder == null) {
            findViewById(R.id.parent_folder).setVisibility(8);
            return;
        }
        findViewById(R.id.parent_folder).setVisibility(0);
        ((TextView) findViewById(R.id.parent_name)).setText(parentFolder.getName().equals("") ? "/" : parentFolder.getName());
    }

    private List<File> getFolders(File folder) {
        File[] files = folder.listFiles();
        List<File> folders = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() || this.mRequestCode == 2) {
                    folders.add(file);
                }
            }
        }
        Collections.sort(folders, new Comparator<File>() {
            public int compare(File lhs, File rhs) {
                if (lhs.isDirectory() && !rhs.isDirectory()) {
                    return -1;
                }
                if (lhs.isDirectory() || !rhs.isDirectory()) {
                    return lhs.compareTo(rhs);
                }
                return 1;
            }
        });
        return folders;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.folder_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint({"InflateParams"})
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        if (item.getItemId() == R.id.action_new_folder) {
            final View dialogCreateFolder = LayoutInflater.from(this).inflate(R.layout.dialog_single_input, (ViewGroup) null);
            ((TextView) dialogCreateFolder.findViewById(R.id.text_content)).setText("Enter folder name");
            FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this)
					.title((CharSequence) "New Folder")
					.iconRes(R.drawable.ic_action_add_folder)
					.customView(dialogCreateFolder, false)
					.positiveText((CharSequence) "Create")
					.negativeText(R.string.button_cancel)
					.onPositive(new MaterialDialog.SingleButtonCallback() {
				public void onClick(MaterialDialog dialog, DialogAction which) {
                    ListView listFolders = (ListView) PickerActivity.this.findViewById(R.id.list_folders);
                    File newFolder = new File(PickerActivity.this.mCurrentFolder, ((EditText) dialogCreateFolder.findViewById(R.id.text_input)).getText().toString().trim());
                    newFolder.mkdir();
                    if (newFolder.exists()) {
                        File unused = PickerActivity.this.mCurrentFolder = newFolder;
                        PickerActivity.this.refreshFiles(listFolders);
                    }
                }
            }).show().getWindow().getDecorView());
        }
        return super.onOptionsItemSelected(item);
    }
}
