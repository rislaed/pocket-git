package com.aor.pocketgit.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import com.afollestad.materialdialogs.MaterialDialog;
import com.aor.pocketgit.R;
import com.aor.pocketgit.adapters.ProjectAdapter;
import com.aor.pocketgit.data.Project;
import com.aor.pocketgit.database.ProjectsDataSource;
import com.aor.pocketgit.services.GitClone;
import com.aor.pocketgit.utils.CredentialStorage;
import com.aor.pocketgit.utils.FontUtils;
import com.aor.pocketgit.utils.GitUtils;
import com.aor.pocketgit.widgets.FloatingActionButton;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jgit.lib.ConfigConstants;

public class MainActivity extends UpdatableActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private ListView mProjectList;

    enum TYPE {
        UNKNOWN,
        GITHUB,
        BITBUCKET
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.action_bar));
        setupFAB();
        this.mProjectList = (ListView) findViewById(R.id.list_projects);
        this.mProjectList.setChoiceMode(3);
        this.mProjectList.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                refreshMode(mode);
            }

            public void refreshMode(ActionMode mode) {
                if (MainActivity.this.mProjectList.getCheckedItemCount() == 0) {
                    mode.finish();
                } else {
                    mode.invalidate();
                }
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                long[] ids = MainActivity.this.mProjectList.getCheckedItemIds();
                switch (item.getItemId()) {
                    case R.id.action_delete_project:
                        List<Integer> toDelete = new ArrayList<>();
                        ProjectAdapter adapter = (ProjectAdapter) MainActivity.this.mProjectList.getAdapter();
                        for (long id : ids) {
                            toDelete.add(Integer.valueOf(adapter.getItem((int) id).getId()));
                        }
                        MainActivity.this.deleteProjects(toDelete);
                        mode.finish();
                        return true;
                    case R.id.action_edit_project:
                        if (ids.length != 1) {
                            return true;
                        }
                        Project project = (Project) MainActivity.this.mProjectList.getItemAtPosition((int) ids[0]);
                        Intent intent = new Intent(MainActivity.this, ProjectActivity.class);
                        intent.putExtra("id", Integer.toString(project.getId()));
                        intent.putExtra(ConfigConstants.CONFIG_KEY_NAME, project.getName());
                        intent.putExtra("url", project.getUrl());
                        intent.putExtra("local_path", project.getLocalPath());
                        intent.putExtra("authentication", project.getAuthentication());
                        intent.putExtra("username", project.getUsername());
                        intent.putExtra("password", project.getPassword());
                        intent.putExtra("privatekey", project.getPrivateKey());
                        MainActivity.this.startActivityForResult(intent, 2);
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.cab_project, menu);
                return true;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                if (MainActivity.this.mProjectList.getCheckedItemCount() > 1) {
                    menu.findItem(R.id.action_edit_project).setVisible(false);
                } else {
                    menu.findItem(R.id.action_edit_project).setVisible(true);
                }
                return true;
            }
        });
        this.mProjectList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                MainActivity.this.openProject((Project) MainActivity.this.mProjectList.getItemAtPosition(position));
            }
        });
        if (getIntent() != null && ("android.intent.action.VIEW".equals(getIntent().getAction()) || "android.intent.action.SEND".equals(getIntent().getAction()))) {
            openFromIntent(getIntent());
        }
        checkPermissions();
    }

    private void setupFAB() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        new FloatingActionButton.Builder(this).withColor(typedValue.data).withDrawable(getResources().getDrawable(R.drawable.ic_action_add)).withMargins(0, 0, 16, 16).create().setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                MainActivity.this.startActivityForResult(new Intent(MainActivity.this, ProjectActivity.class), 1);
            }
        });
    }

    private void openProject(Project project) {
        if (project.getState().equals(Project.STATE_CLONING)) {
            if (GitClone.isCloning(project.getId())) {
                Toast.makeText(this, "Project is cloning", 0).show();
            } else {
                prepareCloneRepository(project);
            }
        } else if (GitUtils.repositoryCloned(project) && !project.getState().equals(Project.STATE_FAILED)) {
            Intent intent = new Intent(this, FilesActivity.class);
            intent.putExtra("id", Integer.toString(project.getId()));
            startActivity(intent);
        } else if (project.getUrl().trim().equals("")) {
            Toast.makeText(this, "Project does not have an URL", 0).show();
        } else {
            prepareCloneRepository(project);
        }
    }

    private void prepareCloneRepository(final Project project) {
        FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this).title((int) R.string.message_clone_repository).iconRes(R.drawable.ic_action_clone).content((int) R.string.message_clone_now).positiveText((int) R.string.button_clone).negativeText((int) R.string.button_cancel).callback(new MaterialDialog.ButtonCallback() {
            public void onPositive(MaterialDialog dialog) {
                final File path = new File(project.getLocalPath());
                if (path.exists() && !path.isDirectory()) {
                    Toast.makeText(MainActivity.this, "Invalid local path", 0).show();
                } else if (!path.exists() || !path.isDirectory() || path.list() == null || path.list().length == 0) {
                    MainActivity.this.cloneRepository(project);
                } else {
                    FontUtils.setRobotoFont(MainActivity.this, new MaterialDialog.Builder(MainActivity.this).title((CharSequence) "Directory not Empty").content((CharSequence) "Delete all files to continue?").positiveText((int) R.string.button_delete).negativeText((int) R.string.button_cancel).callback(new MaterialDialog.ButtonCallback() {
                        public void onPositive(MaterialDialog dialog) {
                            MainActivity.this.deleteAllFilesIn(path);
                            MainActivity.this.cloneRepository(project);
                        }
                    }).show().getWindow().getDecorView());
                }
            }
        }).show().getWindow().getDecorView());
    }

    private void deleteAllFilesIn(File path) {
        File[] files;
        if (path.isDirectory() && (files = path.listFiles()) != null) {
            for (File file : files) {
                deleteAllFilesIn(file);
            }
        }
        path.delete();
    }

    private void cloneRepository(final Project project) {
        CredentialStorage.checkCredentials(this, project, new CredentialStorage.CredentialListener() {
            public void credentialsSet() {
                Intent intent = new Intent(MainActivity.this, GitClone.class);
                intent.putExtra("id", Integer.toString(project.getId()));
                MainActivity.this.startService(intent);
            }

            public void invalidPrivateKey() {
                Toast.makeText(MainActivity.this, "Invalid Private Key", 1).show();
            }
        });
    }

    private void openFromIntent(Intent intent) {
        final String urlSSH;
        String dataString = null;
        if ("android.intent.action.SEND".equals(intent.getAction())) {
            dataString = intent.getStringExtra("android.intent.extra.TEXT");
        }
        if (dataString != null) {
            TYPE type = TYPE.UNKNOWN;
            if (dataString.startsWith("https://github.com/")) {
                dataString = dataString.substring("https://github.com/".length());
                type = TYPE.GITHUB;
            }
            if (dataString.startsWith("https://bitbucket.org/")) {
                dataString = dataString.substring("https://bitbucket.org/".length());
                type = TYPE.BITBUCKET;
            }
            if (type != TYPE.UNKNOWN && dataString.indexOf(47) != -1) {
                String username = dataString.substring(0, dataString.indexOf(47)).trim();
                String project = dataString.substring(dataString.indexOf(47) + 1).trim();
                if (!username.equals("") && !username.contains("/") && !project.equals("")) {
                    if (project.contains("/")) {
                        project = project.substring(0, project.indexOf(47));
                    }
                    final String urlHTTP = type == TYPE.GITHUB ? "https://github.com/" + username + "/" + project + ".git" : "https://bitbucket.org/" + username + "/" + project + ".git";
                    if (type == TYPE.GITHUB) {
                        urlSSH = "git@github.com:" + username + "/" + project + ".git";
                    } else {
                        urlSSH = "git@bitbucket.org:" + username + "/" + project + ".git";
                    }
                    ProjectsDataSource datasource = new ProjectsDataSource(this);
                    datasource.open();
                    Project existing = datasource.getProjectFromURL(urlHTTP);
                    if (existing == null) {
                        existing = datasource.getProjectFromURL(urlSSH);
                    }
                    datasource.close();
                    if (existing == null) {
                        File directory = new File(PreferenceManager.getDefaultSharedPreferences(this).getString("pref_default_directory", Environment.getExternalStorageDirectory().getAbsolutePath() + "/Git"), project);
                        final Intent createIntent = new Intent(this, ProjectActivity.class);
                        createIntent.putExtra(ConfigConstants.CONFIG_KEY_NAME, project);
                        createIntent.putExtra("local_path", directory.getAbsolutePath());
                        FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this).title((CharSequence) "Select protocol").items("HTTP", "SSH").itemsCallback(new MaterialDialog.ListCallback() {
                            public void onSelection(MaterialDialog materialDialog, View view, int which, CharSequence charSequence) {
                                if (which == 0) {
                                    createIntent.putExtra("url", urlHTTP);
                                } else {
                                    createIntent.putExtra("url", urlSSH);
                                    createIntent.putExtra("authentication", 2);
                                }
                                MainActivity.this.startActivityForResult(createIntent, 1);
                            }
                        }).show().getWindow().getDecorView());
                        return;
                    }
                    openProject(existing);
                }
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.git_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_preferences) {
            startActivity(new Intent(this, PreferencesActivity.class));
        }
        if (item.getItemId() == R.id.action_help) {
            startActivity(new Intent(this, HelpActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onStart() {
        super.onStart();
        refreshProjects();
        checkPermissions();
    }

    private void checkPermissions() {
        int canRead = ContextCompat.checkSelfPermission(this, "android.permission.READ_EXTERNAL_STORAGE");
        int canWrite = ContextCompat.checkSelfPermission(this, "android.permission.WRITE_EXTERNAL_STORAGE");
        if (canRead == -1 || canWrite == -1) {
            ActivityCompat.requestPermissions(this, new String[]{"android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != 1) {
            return;
        }
        if (grantResults.length <= 0 || grantResults[0] != 0) {
            showSnackBar(findViewById(R.id.list_projects), "Permission denied. Unable to write to storage!", -2, "Retry", new View.OnClickListener() {
                public void onClick(View v) {
                    MainActivity.this.checkPermissions();
                }
            });
            return;
        }
        showSnackBar(findViewById(R.id.list_projects), "Permission granted. Thanks!", -1);
    }

    private void refreshProjects() {
        ProjectsDataSource datasource = new ProjectsDataSource(this);
        datasource.open();
        this.mProjectList.setAdapter(new ProjectAdapter(this, datasource.getAllProjects()));
        datasource.close();
    }

    @SuppressLint({"InflateParams"})
    private void deleteProjects(final List<Integer> ids) {
        final View viewDeleteFiles = getLayoutInflater().inflate(R.layout.dialog_delete_files, (ViewGroup) null);
        viewDeleteFiles.findViewById(R.id.text_delete_files).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((CheckBox) viewDeleteFiles.findViewById(R.id.check_delete_files)).toggle();
            }
        });
        FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this).title((CharSequence) "Delete Project").iconRes(R.drawable.ic_delete).customView(viewDeleteFiles, false).positiveText((int) R.string.button_delete).negativeText((int) R.string.button_cancel).callback(new MaterialDialog.ButtonCallback() {
            public void onPositive(MaterialDialog dialog) {
                MainActivity.this.deleteProjects(ids, ((CheckBox) viewDeleteFiles.findViewById(R.id.check_delete_files)).isChecked(), ((CheckBox) viewDeleteFiles.findViewById(R.id.check_delete_project)).isChecked());
            }
        }).show().getWindow().getDecorView());
    }

    private void deleteProjects(List<Integer> ids, boolean deleteFiles, boolean deleteProject) {
        ProjectsDataSource dataSource = new ProjectsDataSource(this);
        dataSource.open();
        for (Integer id : ids) {
            Project project = dataSource.getProject(Integer.toString(id.intValue()));
            if (deleteProject) {
                dataSource.deleteProject(id);
            }
            if (deleteFiles) {
                deleteAllFilesIn(new File(project.getLocalPath()));
                new File(project.getLocalPath()).delete();
            }
        }
        dataSource.close();
        refreshProjects();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == 1) {
            ProjectsDataSource dataSource = new ProjectsDataSource(this);
            dataSource.open();
            dataSource.createProject(data.getStringExtra(ConfigConstants.CONFIG_KEY_NAME), data.getStringExtra("url"), data.getStringExtra("local_path"), data.getIntExtra("authentication", 0), data.getStringExtra("username"), data.getStringExtra("password"), data.getStringExtra("privatekey"));
            dataSource.close();
            refreshProjects();
        }
        if (requestCode == 2 && resultCode == 1) {
            ProjectsDataSource dataSource2 = new ProjectsDataSource(this);
            dataSource2.open();
            dataSource2.updateProject(data.getStringExtra("id"), data.getStringExtra(ConfigConstants.CONFIG_KEY_NAME), data.getStringExtra("url"), data.getStringExtra("local_path"), data.getIntExtra("authentication", 0), data.getStringExtra("username"), data.getStringExtra("password"), data.getStringExtra("privatekey"));
            dataSource2.close();
            refreshProjects();
        }
    }

    protected void onUpdateData(Intent intent) {
        super.onUpdateData(intent);
        if (intent.getStringExtra("type").equals(UpdatableActivity.CLONE_UPDATE_BROADCAST)) {
            refreshProjects();
        }
    }
}
