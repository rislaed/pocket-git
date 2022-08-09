package com.aor.pocketgit.activities;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.webkit.MimeTypeMap;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.SearchView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.preference.PreferenceManager;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.aor.pocketgit.R;
import com.aor.pocketgit.adapters.BranchAdapter;
import com.aor.pocketgit.adapters.BranchArrayAdapter;
import com.aor.pocketgit.adapters.GitFileAdapter;
import com.aor.pocketgit.adapters.RemoteAdapter;
import com.aor.pocketgit.adapters.StashAdapter;
import com.aor.pocketgit.data.GitFile;
import com.aor.pocketgit.data.Project;
import com.aor.pocketgit.database.ProjectsDataSource;
import com.aor.pocketgit.dialogs.CheckoutRemoteDialog;
import com.aor.pocketgit.dialogs.MergeDialog;
import com.aor.pocketgit.dialogs.SortFilesDialog;
import com.aor.pocketgit.dialogs.TagListDialog;
import com.aor.pocketgit.services.GitFetch;
import com.aor.pocketgit.services.GitPull;
import com.aor.pocketgit.services.GitPush;
import com.aor.pocketgit.utils.FileUtils;
import com.aor.pocketgit.tasks.CheckoutLocalTask;
import com.aor.pocketgit.tasks.CheckoutRemoteTask;
import com.aor.pocketgit.utils.CredentialStorage;
import com.aor.pocketgit.utils.FileComparator;
import com.aor.pocketgit.utils.FontUtils;
import com.aor.pocketgit.utils.GitUtils;
import com.aor.pocketgit.widgets.FloatingActionButton;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.PatternSyntaxException;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.DeleteBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.api.StashApplyCommand;
import org.eclipse.jgit.api.StashCreateCommand;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.errors.NoWorkTreeException;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.util.HttpSupport;

public class FilesActivity extends UpdatableActivity {
    private FloatingActionButton mCommitFAB;
    private int mCountAhead = 0;
    private int mCountBehind = 0;
    private File mCurrentFolder;
    private ActionBarDrawerToggle mDrawerToggle;
    private ListView mListFiles;
    private Project mProject;
    private Status mStatus;

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        if (type == null) {
            return HttpSupport.TEXT_PLAIN;
        }
        return type;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_files);
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        setSupportActionBar(toolbar);
        setUpFAB();
        onNewIntent(getIntent());
        this.mListFiles = (ListView) findViewById(R.id.list_files);
        this.mListFiles.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                GitFile file = (GitFile) FilesActivity.this.mListFiles.getItemAtPosition(position);
                if (file.isDirectory()) {
                    File unused = FilesActivity.this.mCurrentFolder = ((GitFile) FilesActivity.this.mListFiles.getItemAtPosition(position)).getFile();
                    FilesActivity.this.refreshFiles(true);
                } else if (!file.hasState(GitFile.MISSING)) {
                    try {
                        Intent intent = new Intent("android.intent.action.VIEW");
                        Uri uri = Uri.fromFile(file.getFile());
                        intent.setDataAndType(uri, FilesActivity.getMimeType(uri.toString()));
                        FilesActivity.this.startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(FilesActivity.this, "Failed to open file", 0).show();
                        Log.e("PocketGit", "Failed to open file", e);
                    }
                }
            }
        });
        ((LinearLayout) findViewById(R.id.parent_folder)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                File unused = FilesActivity.this.mCurrentFolder = FilesActivity.this.mCurrentFolder.getParentFile();
                FilesActivity.this.refreshFiles(true);
            }
        });
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        this.mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.action_open_branches, R.string.action_close_branches) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                try {
                    String currentBranch = FilesActivity.this.getRepository(FilesActivity.this.mProject).getFullBranch();
                    if (currentBranch.startsWith(Constants.R_HEADS)) {
                        FilesActivity.this.getSupportActionBar().setTitle((CharSequence) FilesActivity.this.mProject.getName() + " (" + currentBranch.substring(11) + ")");
                    } else {
                        FilesActivity.this.getSupportActionBar().setTitle((CharSequence) FilesActivity.this.mProject.getName() + " (" + currentBranch.substring(0, 8) + ")");
                    }
                } catch (Exception e) {}
                FilesActivity.this.getSupportActionBar().setTitle((CharSequence) FilesActivity.this.mProject.getName());
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                FilesActivity.this.getSupportActionBar().setTitle(R.string.label_branches);
            }
        };
        drawerLayout.addDrawerListener(this.mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.mListFiles.setChoiceMode(3);
        this.mListFiles.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                refreshMode(mode);
            }

            public void refreshMode(ActionMode mode) {
                if (FilesActivity.this.mListFiles.getCheckedItemCount() == 0) {
                    mode.finish();
                } else {
                    mode.invalidate();
                }
            }

            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_blame_file:
                        FilesActivity.this.actionBlame();
                        mode.finish();
                        return true;
                    case R.id.action_delete_file:
                        FilesActivity.this.actionDeleteFiles();
                        mode.finish();
                        return true;
                    case R.id.action_diff_file:
                        FilesActivity.this.actionDiff();
                        mode.finish();
                        return true;
                    case R.id.action_remove_file:
                        FilesActivity.this.actionRemoveFiles();
                        mode.finish();
                        return true;
                    case R.id.action_revert_file:
                        FilesActivity.this.actionRevertFiles();
                        mode.finish();
                        return true;
                    case R.id.action_select_all:
                        FilesActivity.this.actionSelectAll();
                        return true;
                    case R.id.action_stage_file:
                        FilesActivity.this.actionStage();
                        mode.finish();
                        return true;
                    case R.id.action_unstage_file:
                        FilesActivity.this.actionUnstage();
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.cab_files, menu);
                return true;
            }

            public void onDestroyActionMode(ActionMode mode) {
            }

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                Set<String> states = new HashSet<>();
                for (int i = 0; i < FilesActivity.this.mListFiles.getCheckedItemCount(); i++) {
                    states.addAll(((GitFile) FilesActivity.this.mListFiles.getItemAtPosition((int) FilesActivity.this.mListFiles.getCheckedItemIds()[i])).getStates());
                }
                if (GitFile.isStageable(states)) {
                    menu.findItem(R.id.action_stage_file).setVisible(true);
                } else {
                    menu.findItem(R.id.action_stage_file).setVisible(false);
                }
                if (GitFile.isUnstageable(states)) {
                    menu.findItem(R.id.action_unstage_file).setVisible(true);
                } else {
                    menu.findItem(R.id.action_unstage_file).setVisible(false);
                }
                if (GitFile.isRemovable(states)) {
                    menu.findItem(R.id.action_remove_file).setVisible(true);
                } else {
                    menu.findItem(R.id.action_remove_file).setVisible(false);
                }
                if (GitFile.isDeletable(states)) {
                    menu.findItem(R.id.action_delete_file).setVisible(true);
                } else {
                    menu.findItem(R.id.action_delete_file).setVisible(false);
                }
                if (GitFile.isRevertable(states)) {
                    menu.findItem(R.id.action_revert_file).setVisible(true);
                } else {
                    menu.findItem(R.id.action_revert_file).setVisible(false);
                }
                if (FilesActivity.this.mListFiles.getCheckedItemCount() == 1) {
                    GitFile file = (GitFile) FilesActivity.this.mListFiles.getItemAtPosition((int) FilesActivity.this.mListFiles.getCheckedItemIds()[0]);
                    if (file.isDirectory() || !GitFile.isDiffable(states)) {
                        menu.findItem(R.id.action_diff_file).setVisible(false);
                    } else {
                        menu.findItem(R.id.action_diff_file).setVisible(true);
                    }
                    if (file.isDirectory() || !GitFile.isBlameable(states)) {
                        menu.findItem(R.id.action_blame_file).setVisible(false);
                    } else {
                        menu.findItem(R.id.action_blame_file).setVisible(true);
                    }
                } else {
                    menu.findItem(R.id.action_blame_file).setVisible(false);
                    menu.findItem(R.id.action_diff_file).setVisible(false);
                }
                return true;
            }
        });
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String id = intent.getStringExtra("id");
        if (id != null) {
            ProjectsDataSource datasource = new ProjectsDataSource(this);
            datasource.open();
            this.mProject = datasource.getProject(id);
            datasource.close();
            getSupportActionBar().setTitle((CharSequence) this.mProject.getName());
            this.mCurrentFolder = new File(this.mProject.getLocalPath());
            initializeBranches();
            refreshFiles(false);
        }
    }

    protected void onResume() {
        super.onResume();
        initializeBranches();
        refreshFiles(false);
    }

    private void actionSelectAll() {
        for (int i = 0; i < this.mListFiles.getAdapter().getCount(); i++) {
            this.mListFiles.setItemChecked(i, true);
        }
    }

    private void actionDiff() {
        if (this.mListFiles.getCheckedItemCount() == 1) {
            GitFile diffFile = (GitFile) this.mListFiles.getItemAtPosition((int) this.mListFiles.getCheckedItemIds()[0]);
            if (diffFile.isDiffable()) {
                actionDiffFile(diffFile);
            }
        }
    }

    private void actionBlame() {
        if (this.mListFiles.getCheckedItemCount() == 1) {
            GitFile blameFile = (GitFile) this.mListFiles.getItemAtPosition((int) this.mListFiles.getCheckedItemIds()[0]);
            if (blameFile.isBlameable()) {
                actionBlameFile(blameFile);
            }
        }
    }

    private void actionDiffFile(GitFile file) {
        try {
            if (file.getFile().getCanonicalPath().startsWith(this.mProject.getLocalPath())) {
                String path = file.getFile().getCanonicalPath().substring(this.mProject.getLocalPath().length());
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                Repository repository = GitUtils.getRepository(this.mProject);
                AbstractTreeIterator commitTreeIterator = GitUtils.prepareTreeParser(repository, "HEAD");
                FileTreeIterator workTreeIterator = new FileTreeIterator(repository);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DiffFormatter formatter = new DiffFormatter(baos);
                formatter.setRepository(repository);
                formatter.setPathFilter(PathFilter.create(path));
                for (DiffEntry entry : formatter.scan(commitTreeIterator, (AbstractTreeIterator) workTreeIterator)) {
                    formatter.format(entry);
                }
                Intent intent = new Intent(this, DiffActivity.class);
                intent.putExtra(ConfigConstants.CONFIG_DIFF_SECTION, baos.toString());
                startActivity(intent);
                repository.close();
            }
        } catch (Exception e) {
            Log.e("Pocket Git", "", e);
        }
    }

    private void actionBlameFile(GitFile file) {
        try {
            if (file.getFile().getCanonicalPath().startsWith(this.mProject.getLocalPath())) {
                String path = file.getFile().getCanonicalPath().substring(this.mProject.getLocalPath().length());
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                Intent intent = new Intent(this, BlameActivity.class);
                intent.putExtra("id", Integer.toString(this.mProject.getId()));
                intent.putExtra(ConfigConstants.CONFIG_KEY_PATH, path);
                startActivity(intent);
            }
        } catch (Exception e) {
            Log.e("Pocket Git", "", e);
        }
    }

    private void setUpFAB() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        this.mCommitFAB = new FloatingActionButton.Builder(this).withColor(typedValue.data).withDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_commit)).withMargins(0, 0, 16, 16).create();
        this.mCommitFAB.setVisibility(8);
        this.mCommitFAB.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FilesActivity.this.commit();
            }
        });
    }

    private void initializeBranches() {
        try {
            Repository repository = getRepository(this.mProject);
            final String currentBranch = repository.getFullBranch();
            getSupportActionBar().setTitle((CharSequence) this.mProject.getName());
            ((SwitchCompat) findViewById(R.id.toggle_remote)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    FilesActivity.this.initializeBranches();
                }
            });
            if (currentBranch == null) {
                optionCreateBranch();
                return;
            }
            final ListView listBranch = (ListView) findViewById(R.id.list_branches);
            if (currentBranch.startsWith(Constants.R_HEADS)) {
                getSupportActionBar().setSubtitle((CharSequence) currentBranch.substring(11));
            } else {
                getSupportActionBar().setSubtitle((CharSequence) currentBranch.substring(0, 8));
            }
            try {
                ListBranchCommand branchList = new Git(repository).branchList();
                if (((SwitchCompat) findViewById(R.id.toggle_remote)).isChecked()) {
                    ((TextView) findViewById(R.id.text_local_remote)).setText("Remote");
                    branchList.setListMode(ListBranchCommand.ListMode.REMOTE);
                } else {
                    ((TextView) findViewById(R.id.text_local_remote)).setText("Local");
                }
                List<Ref> branches = branchList.call();
                if (listBranch.getAdapter() == null) {
                    listBranch.setAdapter(new BranchAdapter(this, branches, currentBranch));
                } else {
                    ((BranchAdapter) listBranch.getAdapter()).refill(branches, currentBranch);
                }
            } catch (GitAPIException e) {
                Log.e("Pocket Git", "", e);
            }
            listBranch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Ref ref = (Ref) listBranch.getItemAtPosition(position);
                    if (!ref.getName().equals(currentBranch) && !ref.getName().equals("HEAD")) {
                        FilesActivity.this.actionCheckoutLocal(ref.getName());
                    }
                }
            });
            repository.close();
        } catch (IOException e2) {
            Log.e("Pocket Git", "", e2);
        }
    }

    @SuppressLint({"InflateParams"})
    protected void optionCreateBranch() {
        View dialogBranchName = LayoutInflater.from(this).inflate(R.layout.dialog_single_input, (ViewGroup) null);
        final EditText input = (EditText) dialogBranchName.findViewById(R.id.text_input);
        ((TextView) dialogBranchName.findViewById(R.id.text_content)).setText("Enter the new branch name");
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
				.title((CharSequence) "Create Branch")
				.iconRes(R.drawable.ic_action_create_branch)
				.customView(dialogBranchName, true)
				.positiveText((CharSequence) "Create")
				.onPositive(new MaterialDialog.SingleButtonCallback() {
            public void onClick(MaterialDialog dialog, DialogAction which) {
                String name = input.getText().toString().trim();
                try {
                    Repository repo = FilesActivity.this.getRepository(FilesActivity.this.mProject);
                    CreateBranchCommand createBranch = new Git(repo).branchCreate();
                    createBranch.setName(name);
                    Ref ref = createBranch.call();
                    if (ref != null) {
                        FilesActivity.this.actionCheckoutLocal(ref.getName());
                    }
                    FilesActivity.this.initializeBranches();
                    repo.close();
                    Toast.makeText(FilesActivity.this, "Branch created", 0).show();
                } catch (RefAlreadyExistsException e) {
                    Toast.makeText(FilesActivity.this, "Branch alredy exists", 0).show();
                } catch (IOException | GitAPIException e2) {
                    Toast.makeText(FilesActivity.this, "Failed to create branch", 0).show();
                    Log.e("Pocket Git", "", e2);
                }
            }
        }).negativeText(R.string.button_cancel).show();
        input.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            public void afterTextChanged(Editable s) {
                dialog.getActionButton(DialogAction.POSITIVE).setEnabled(!s.toString().trim().equals(""));
            }
        });
        dialog.getActionButton(DialogAction.POSITIVE).setEnabled(false);
        FontUtils.setRobotoFont(this, dialog.getWindow().getDecorView());
    }

    @SuppressLint({"InflateParams"})
    protected void actionCheckoutRemote(Repository repository, String remote) {
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
        ((SwitchCompat) findViewById(R.id.toggle_remote)).setChecked(false);
        new CheckoutRemoteDialog(this).showDialog(repository, remote).setCheckoutListener(new CheckoutRemoteTask.CheckoutRemoteListener() {
            public MaterialDialog mDialog;

            public void checkoutStarted() {
                this.mDialog = FilesActivity.this.showCheckoutInProgressDialog();
            }

            public void remoteCheckedOut(String name) {
                this.mDialog.dismiss();
                Toast.makeText(FilesActivity.this, "Checked out " + name, 0).show();
                FilesActivity.this.refreshFiles(false);
                FilesActivity.this.initializeBranches();
            }

            public void refAlreadyExists() {
                this.mDialog.dismiss();
                Toast.makeText(FilesActivity.this, "Failed to create branch: ref already exists", 0).show();
            }

            public void conflictDetected() {
                this.mDialog.dismiss();
                Toast.makeText(FilesActivity.this, "Failed to checkout: conflict detected", 0).show();
            }

            public void checkoutFailed(Exception e) {
                this.mDialog.dismiss();
                Toast.makeText(FilesActivity.this, "Failed to checkout branch", 0).show();
                Log.e("PocketGit", "", e);
            }
        });
    }

    protected void actionCheckoutLocal(String name) {
        if (!name.equals("HEAD")) {
            try {
                Repository repository = getRepository(this.mProject);
                if (name.startsWith("refs/remotes")) {
                    actionCheckoutRemote(repository, name);
                    return;
                }
                ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
                final MaterialDialog dialog = showCheckoutInProgressDialog();
                new CheckoutLocalTask().setListener(new CheckoutLocalTask.CheckoutTaskListener() {
                    public void checkedOut(String name) {
                        Toast.makeText(FilesActivity.this, "Checked out " + name, 0).show();
                        FilesActivity.this.refreshFiles(false);
                        FilesActivity.this.initializeBranches();
                        dialog.dismiss();
                    }

                    public void conflict(String name) {
                        Toast.makeText(FilesActivity.this, "Failed to checkout: conflict detected", 0).show();
                        dialog.dismiss();
                    }

                    public void error(String name) {
                        Toast.makeText(FilesActivity.this, "Failed to checkout", 0).show();
                        dialog.dismiss();
                    }
                }).execute(new Object[] { repository, name });
            } catch (Exception e) {
                Toast.makeText(this, "Failed to checkout", 0).show();
            }
        }
    }

    private MaterialDialog showCheckoutInProgressDialog() {
        MaterialDialog dialog = new MaterialDialog.Builder(this).customView(R.layout.dialog_checkout, false).cancelable(false).show();
        RotateAnimation rotateAnimation = new RotateAnimation(0.0f, 360.0f, 1, 0.5f, 1, 0.5f);
        rotateAnimation.setRepeatCount(-1);
        rotateAnimation.setDuration(3000);
        ((ImageView) dialog.getCustomView().findViewById(R.id.icon_checkout)).startAnimation(rotateAnimation);
        return dialog;
    }

    private Repository getRepository(Project project) throws IOException {
        Repository repository = new RepositoryBuilder().setGitDir(new File(project.getLocalPath() + "/.git")).readEnvironment().build();
        loadConfig(repository);
        return repository;
    }

    protected void loadConfig(Repository repository) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        StoredConfig config = repository.getConfig();
        String author = config.getString(ConfigConstants.CONFIG_USER_SECTION, (String) null, ConfigConstants.CONFIG_KEY_NAME);
        String email = config.getString(ConfigConstants.CONFIG_USER_SECTION, (String) null, "email");
        if (author == null || author.trim().equals("")) {
            author = preferences.getString("pref_default_author", "");
        }
        if (email == null || email.trim().equals("")) {
            email = preferences.getString("pref_default_email", "");
        }
        config.setString(ConfigConstants.CONFIG_USER_SECTION, (String) null, ConfigConstants.CONFIG_KEY_NAME, author);
        config.setString(ConfigConstants.CONFIG_USER_SECTION, (String) null, "email", email);
    }

    private void refreshFiles(boolean useCache) {
        if (!useCache || this.mStatus == null) {
            refreshStatus();
            return;
        }
        this.mListFiles.setChoiceMode(0);
        this.mListFiles.setChoiceMode(3);
        if (this.mListFiles.getAdapter() == null) {
            this.mListFiles.setAdapter(new GitFileAdapter(this, getFiles(this.mCurrentFolder)));
        } else {
            ((GitFileAdapter) this.mListFiles.getAdapter()).refill(getFiles(this.mCurrentFolder));
        }
        File parentFolder = this.mCurrentFolder.getParentFile();
        if (parentFolder == null || this.mCurrentFolder.equals(new File(this.mProject.getLocalPath()))) {
            findViewById(R.id.parent_folder).setVisibility(8);
        } else {
            findViewById(R.id.parent_folder).setVisibility(0);
            ((TextView) findViewById(R.id.parent_name)).setText(parentFolder.getName().equals("") ? "/" : parentFolder.getName());
        }
        try {
            Repository repository = getRepository(this.mProject);
            BranchTrackingStatus trackingStatus = BranchTrackingStatus.of(repository, repository.getBranch());
            if (trackingStatus != null) {
                this.mCountAhead = trackingStatus.getAheadCount();
                this.mCountBehind = trackingStatus.getBehindCount();
            } else {
                this.mCountAhead = 0;
                this.mCountBehind = 0;
            }
        } catch (Exception e) {
            Log.e("PocketGit", "", e);
        }
        invalidateOptionsMenu();
    }

    private void refreshStatus() {
        new AsyncTask<Void, Void, Boolean>() {
            boolean patternError = false;

            protected void onPreExecute() {
                try {
                    FilesActivity.this.findViewById(R.id.progress).setVisibility(0);
                } catch (Exception e) {
                }
            }

            protected Boolean doInBackground(Void... params) {
                try {
                    Repository repository = FilesActivity.this.getRepository(FilesActivity.this.mProject);
                    FilesActivity.this.mStatus = new Git(repository).status().call();
                    repository.close();
                    return true;
                } catch (PatternSyntaxException e) {
                    this.patternError = true;
                    return false;
                } catch (Exception e2) {
                    Log.e("PocketGit", "", e2);
                    return false;
                }
            }

            protected void onPostExecute(Boolean result) {
                try {
                    FilesActivity.this.findViewById(R.id.progress).setVisibility(8);
                } catch (Exception e) {
                }
                if (result.booleanValue()) {
                    FilesActivity.this.refreshFiles(true);
                    return;
                }
                View.OnClickListener retryAction = new View.OnClickListener() {
                    public void onClick(View v) {
                        FilesActivity.this.refreshStatus();
                    }
                };
                if (this.patternError) {
                    FilesActivity.this.showSnackBar(FilesActivity.this.findViewById(R.id.list_files), "Failed to parse .gitignore", -1, "Retry", retryAction);
                } else {
                    FilesActivity.this.showSnackBar(FilesActivity.this.findViewById(R.id.list_files), "Error calculating status", -1, "Retry", retryAction);
                }
            }
        }.execute(new Void[0]);
    }

    private List<GitFile> getFiles(File folder) {
        List<GitFile> folders = new ArrayList<>();
        try {
            File gitFile = new File(this.mProject.getLocalPath() + "/.git");
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (!file.equals(gitFile)) {
                        Set<String> states = calculateFileStates(file);
                        if (states.size() == 0) {
                            states.add(GitFile.COMMITED);
                        }
                        folders.add(new GitFile(file, states));
                    }
                }
            }
            HashSet<String> missingSet = new HashSet<>();
            missingSet.add(GitFile.MISSING);
            for (String missing : this.mStatus.getMissing()) {
                if (new File(this.mProject.getLocalPath(), missing).getParentFile().equals(this.mCurrentFolder)) {
                    folders.add(new GitFile(new File(this.mProject.getLocalPath(), missing), (Set<String>) missingSet));
                }
            }
            Collections.sort(folders, FileComparator.getPreferedComparator(this));
        } catch (IOException e) {
            Log.e("Pocket Git", "", e);
        } catch (NoWorkTreeException e) {
            Log.e("Pocket Git", "", e);
        }
        return folders;
    }

    @NonNull
    private Set<String> calculateFileStates(File file) throws IOException {
        Set<String> states = new HashSet<>();
        if (file.isFile()) {
            if (contains(this.mStatus.getAdded(), file)) {
                states.add(GitFile.ADDED);
            }
            if (contains(this.mStatus.getModified(), file)) {
                states.add(GitFile.MODIFIED);
            }
            if (contains(this.mStatus.getChanged(), file)) {
                states.add(GitFile.CHANGED);
            }
            if (contains(this.mStatus.getRemoved(), file)) {
                states.add(GitFile.REMOVED);
            }
            if (contains(this.mStatus.getConflicting(), file)) {
                states.add(GitFile.CONFLICTING);
            }
            if (contains(this.mStatus.getIgnoredNotInIndex(), file)) {
                states.add(GitFile.IGNORED);
            }
            if (containsParent(this.mStatus.getIgnoredNotInIndex(), file)) {
                states.add(GitFile.IGNORED);
            }
            if (contains(this.mStatus.getUntracked(), file)) {
                states.add(GitFile.UNTRACKED);
            }
            if (contains(this.mStatus.getUntracked(), file) && !contains(this.mStatus.getRemoved(), file)) {
                states.add(GitFile.UNTRACKED_NOT_REMOVED);
            }
            if (contains(this.mStatus.getUncommittedChanges(), file)) {
                states.add(GitFile.UNCOMMITED);
            }
        }
        if (file.isDirectory()) {
            if (containsChild(this.mStatus.getAdded(), file)) {
                states.add(GitFile.CHANGED);
            }
            if (containsChild(this.mStatus.getModified(), file)) {
                states.add(GitFile.MODIFIED);
            }
            if (containsChild(this.mStatus.getChanged(), file)) {
                states.add(GitFile.CHANGED);
            }
            if (containsChild(this.mStatus.getRemoved(), file)) {
                states.add(GitFile.CHANGED);
            }
            if (containsChild(this.mStatus.getConflicting(), file)) {
                states.add(GitFile.CONFLICTING);
            }
            if (containsParent(this.mStatus.getIgnoredNotInIndex(), file)) {
                states.add(GitFile.IGNORED);
            }
            if (containsChild(this.mStatus.getUntracked(), file)) {
                states.add(GitFile.UNTRACKED);
            }
            if (containsChild(this.mStatus.getUncommittedChanges(), file)) {
                states.add(GitFile.UNCOMMITED);
            }
        }
        return states;
    }

    private boolean containsChild(Set<String> files, File file) throws IOException {
        String dName = file.getCanonicalPath() + "/";
        for (String name : files) {
            if (new File(this.mProject.getLocalPath(), name).getCanonicalPath().startsWith(dName)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsParent(Set<String> files, File file) throws IOException {
        for (String name : files) {
            if (file.getCanonicalPath().startsWith(new File(this.mProject.getLocalPath(), name).getCanonicalPath())) {
                return true;
            }
        }
        return false;
    }

    private boolean contains(Set<String> files, File file) {
        for (String name : files) {
            if (new File(this.mProject.getLocalPath(), name).equals(file)) {
                return true;
            }
        }
        return false;
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.mDrawerToggle.syncState();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mDrawerToggle.onConfigurationChanged(newConfig);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        switch (item.getItemId()) {
            case R.id.action_author:
                optionSetAuthor();
                break;
            case R.id.action_create_branch:
                optionCreateBranch();
                break;
            case R.id.action_delete_branch:
                optionDeleteBranch();
                break;
            case R.id.action_fetch:
                optionFetch();
                break;
            case R.id.action_help:
                optionHelp();
                break;
            case R.id.action_log:
                optionLog();
                break;
            case R.id.action_manage_remotes:
                Intent intent = new Intent(this, RemotesActivity.class);
                intent.putExtra("id", Integer.toString(this.mProject.getId()));
                startActivity(intent);
                break;
            case R.id.action_merge:
                optionMerge();
                break;
            case R.id.action_new_file:
                optionNewFile();
                break;
            case R.id.action_new_folder:
                optionNewFolder();
                break;
            case R.id.action_pull:
                optionPull();
                break;
            case R.id.action_push:
                optionPush();
                break;
            case R.id.action_revert:
                optionRevert();
                break;
            case R.id.action_sort_files:
                new SortFilesDialog(this).showDialog().setSortListener(new SortFilesDialog.SortListener() {
                    public void sortChanged() {
                        FilesActivity.this.refreshFiles(true);
                    }
                });
                break;
            case R.id.action_stash_apply:
                optionStashApply(0);
                break;
            case R.id.action_stash_create:
                optionStashCreate();
                break;
            case R.id.action_stash_list:
                optionStashList();
                break;
            case R.id.action_tag_create:
                optionTagCreate();
                break;
            case R.id.action_tag_list:
                optionTagList();
                break;
            case R.id.action_tag_push:
                optionTagPush();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void optionLog() {
        Intent intentLog = new Intent(this, LogActivity.class);
        intentLog.putExtra("id", Integer.toString(this.mProject.getId()));
        startActivityForResult(intentLog, 0);
    }

    private void optionHelp() {
        startActivity(new Intent(this, HelpActivity.class));
    }

    private void optionTagList() {
        try {
            new TagListDialog(this).showDialog(getRepository(this.mProject)).setTagListListener(new TagListDialog.TagListListener() {
                public void checkout(String tag) {
                    FilesActivity.this.actionCheckoutLocal(tag);
                }

                public void deleteBranch(Set<String> tags) {
                    FilesActivity.this.actionDeleteBranch(tags, "Tag", "Tags");
                }
            });
        } catch (Exception e) {
            Log.e("PocketGit", "", e);
        }
    }

    private void optionTagCreate() {
        final View dialogTag = LayoutInflater.from(this).inflate(R.layout.dialog_tag, (ViewGroup) null);
        FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this)
				.title(R.string.title_dialog_tag)
				.iconRes(R.drawable.ic_action_tag_create)
				.customView(dialogTag, false)
				.negativeText(R.string.button_cancel)
				.positiveText(R.string.button_create)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
            public void onClick(MaterialDialog dialog, DialogAction which) {
                try {
                    Repository repository = FilesActivity.this.getRepository(FilesActivity.this.mProject);
                    String name = ((EditText) dialogTag.findViewById(R.id.text_name)).getText().toString().trim();
                    String message = ((EditText) dialogTag.findViewById(R.id.text_message)).getText().toString().trim();
                    TagCommand tag = new Git(repository).tag().setName(name);
                    if (!message.trim().equals("")) {
                        tag.setMessage(message);
                    }
                    tag.call();
                } catch (IOException | GitAPIException e) {
                    Toast.makeText(FilesActivity.this, "Failed to tag: " + e.getMessage(), 0).show();
                    Log.e("Pocket Git", "", e);
                }
            }
        }).show().getWindow().getDecorView());
    }

    @SuppressLint({"InflateParams"})
    private void optionStashList() {
        View dialogListStash = LayoutInflater.from(this).inflate(R.layout.dialog_stash_list, (ViewGroup) null);
        try {
            Repository repository = getRepository(this.mProject);
            Collection<RevCommit> collection = new Git(repository).stashList().call();
            repository.close();
            if (collection.size() == 0) {
                Toast.makeText(this, "No stashes found", 0).show();
                return;
            }
            List<RevCommit> revCommits = new ArrayList<>();
            revCommits.addAll(collection);
            StashAdapter adapter = new StashAdapter(this, revCommits);
            final ListView listStashed = (ListView) dialogListStash.findViewById(R.id.list_stashed);
            final MaterialDialog dialog = new MaterialDialog.Builder(this)
				.title((CharSequence) "Stash List")
				.iconRes(R.drawable.ic_action_stash_list)
				.customView(dialogListStash, false)
				.positiveText(R.string.button_close)
				.negativeText(R.string.button_apply)
				.neutralText(R.string.button_drop)
				.onNegative(new MaterialDialog.SingleButtonCallback() {
                	public void onClick(MaterialDialog dialog, DialogAction which) {
                    	FilesActivity.this.optionStashApply(listStashed.getCheckedItemPosition());
                	}
				})
				.onNeutral(new MaterialDialog.SingleButtonCallback() {
					public void onClick(MaterialDialog dialog, DialogAction which) {
                    	FilesActivity.this.actionStashDelete(listStashed.getCheckedItemPosition());
            		}
            	}).show();
            dialog.getActionButton(DialogAction.NEGATIVE).setEnabled(false);
            dialog.getActionButton(DialogAction.NEUTRAL).setEnabled(false);
            listStashed.setAdapter(adapter);
            listStashed.setChoiceMode(1);
            listStashed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    dialog.getActionButton(DialogAction.NEUTRAL).setEnabled(true);
                    dialog.getActionButton(DialogAction.NEGATIVE).setEnabled(true);
                }
            });
            FontUtils.setRobotoFont(this, dialog.getWindow().getDecorView());
        } catch (Exception e) {
            Toast.makeText(this, "Failed to get stash list", 0).show();
        }
    }

    @SuppressLint({"InflateParams"})
    public void optionStashApply(final int stashRef) {
        final List<RevCommit> list = new ArrayList<>();
        try {
            Repository repository = getRepository(this.mProject);
            Collection<RevCommit> collection = new Git(repository).stashList().call();
            repository.close();
            list.addAll(collection);
            if (list.size() <= stashRef) {
                Toast.makeText(this, "No stash to apply", 0).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to get stash list", 0).show();
        }
        final View dialogDeleteStash = LayoutInflater.from(this).inflate(R.layout.dialog_delete_stash, (ViewGroup) null);
        ((TextView) dialogDeleteStash.findViewById(R.id.text_message)).setText(list.get(stashRef).getShortMessage());
        FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this)
				.title((CharSequence) "Apply Stash")
				.iconRes(R.drawable.ic_action_stash_apply)
				.customView(dialogDeleteStash, false)
				.positiveText(R.string.button_apply)
				.negativeText(R.string.button_cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
            public void onClick(MaterialDialog dialog, DialogAction which) {
                try {
                    Repository repository = FilesActivity.this.getRepository(FilesActivity.this.mProject);
                    StashApplyCommand stashCommand = new Git(repository).stashApply().setStashRef(list.get(stashRef).getName());
                    stashCommand.setApplyIndex(true);
                    stashCommand.call();
                    if (((CheckBox) dialogDeleteStash.findViewById(R.id.check_delete_stash)).isChecked()) {
                        new Git(repository).stashDrop().setStashRef(stashRef).call();
                    }
                    repository.close();
                    FilesActivity.this.refreshFiles(false);
                } catch (Exception e) {
                    Log.e("Pocket Git", "", e);
                    Toast.makeText(FilesActivity.this, "Failed to apply stash", 0).show();
                }
            }
        }).show().getWindow().getDecorView());
    }

    @SuppressLint({"InflateParams"})
    public void actionStashDelete(int stashRef) {
        List<RevCommit> list = new ArrayList<>();
        try {
            Repository repository = getRepository(this.mProject);
            Collection<RevCommit> collection = new Git(repository).stashList().call();
            repository.close();
            list.addAll(collection);
            if (list.size() < stashRef) {
                Toast.makeText(this, "No stash to delete", 0).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to get stash list", 0).show();
        }
        try {
            Repository repository2 = getRepository(this.mProject);
            new Git(repository2).stashDrop().setStashRef(stashRef).call();
            repository2.close();
            refreshFiles(false);
        } catch (Exception e2) {
            Log.e("Pocket Git", "", e2);
            Toast.makeText(this, "Failed to delete stash", 0).show();
        }
    }

    @SuppressLint({"InflateParams"})
    private void optionStashCreate() {
        final View dialogCreateStash = LayoutInflater.from(this).inflate(R.layout.dialog_create_stash, (ViewGroup) null);
        FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this)
				.title((CharSequence) "Save Stash")
				.iconRes(R.drawable.ic_action_stash)
				.customView(dialogCreateStash, false)
				.positiveText(R.string.button_create)
				.negativeText(R.string.button_cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
            public void onClick(MaterialDialog dialog, DialogAction which) {
                try {
                    Repository repository = FilesActivity.this.getRepository(FilesActivity.this.mProject);
                    StashCreateCommand stashCommand = new Git(repository).stashCreate().setIncludeUntracked(((CheckBox) dialogCreateStash.findViewById(R.id.check_include_untracked)).isChecked());
                    String message = ((EditText) dialogCreateStash.findViewById(R.id.text_message)).getText().toString().trim();
                    if (!message.equals("")) {
                        stashCommand.setWorkingDirectoryMessage(message);
                    }
                    if (stashCommand.call() == null) {
                        Toast.makeText(FilesActivity.this, "Nothing to stash", 0).show();
                    }
                    repository.close();
                    FilesActivity.this.refreshFiles(false);
                } catch (Exception e) {
                    Toast.makeText(FilesActivity.this, "Failed to stash", 0).show();
                }
            }
        }).show().getWindow().getDecorView());
    }

    @SuppressLint({"InflateParams"})
    private void optionSetAuthor() {
        String author = null;
        String email = null;
        try {
            Repository repository = getRepository(this.mProject);
            StoredConfig config = repository.getConfig();
            author = config.getString(ConfigConstants.CONFIG_USER_SECTION, (String) null, ConfigConstants.CONFIG_KEY_NAME);
            email = config.getString(ConfigConstants.CONFIG_USER_SECTION, (String) null, "email");
            repository.close();
        } catch (IOException e) {
            Log.e("Pocket Git", "", e);
        }
        View dialogAuthor = LayoutInflater.from(this).inflate(R.layout.dialog_author, (ViewGroup) null);
        final EditText editAuthor = (EditText) dialogAuthor.findViewById(R.id.text_author);
        editAuthor.setText(author);
        final EditText editEmail = (EditText) dialogAuthor.findViewById(R.id.text_email);
        editEmail.setText(email);
        FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this)
				.title(R.string.title_author_data)
				.iconRes(R.drawable.ic_author)
				.customView(dialogAuthor, false)
				.positiveText(R.string.button_save)
				.negativeText(R.string.button_cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
            public void onClick(MaterialDialog dialog, DialogAction which) {
                try {
                    Repository repository = FilesActivity.this.getRepository(FilesActivity.this.mProject);
                    StoredConfig config = repository.getConfig();
                    if (editAuthor.getText().toString().trim().equals("")) {
                        config.unset(ConfigConstants.CONFIG_USER_SECTION, (String) null, ConfigConstants.CONFIG_KEY_NAME);
                    } else {
                        config.setString(ConfigConstants.CONFIG_USER_SECTION, (String) null, ConfigConstants.CONFIG_KEY_NAME, editAuthor.getText().toString().trim());
                    }
                    if (editEmail.getText().toString().trim().equals("")) {
                        config.unset(ConfigConstants.CONFIG_USER_SECTION, (String) null, "email");
                    } else {
                        config.setString(ConfigConstants.CONFIG_USER_SECTION, (String) null, "email", editEmail.getText().toString().trim());
                    }
                    config.save();
                    repository.close();
                } catch (IOException e) {
                    Log.e("Pocket Git", "", e);
                }
            }
        }).show().getWindow().getDecorView());
    }

    @SuppressLint({"InflateParams"})
    private void optionPull() {
        try {
            Repository repository = GitUtils.getRepository(this.mProject);
            StoredConfig config = repository.getConfig();
            List<RemoteConfig> remotes = RemoteConfig.getAllRemoteConfigs(config);
            View viewRemotes = LayoutInflater.from(this).inflate(R.layout.dialog_select_remote, (ViewGroup) null);
            final Spinner spinnerRemotes = (Spinner) viewRemotes.findViewById(R.id.spinner_remotes);
            spinnerRemotes.setAdapter(new RemoteAdapter(this, remotes));
            if (spinnerRemotes.getCount() == 0) {
                Toast.makeText(this, "No remotes configured", 0).show();
            } else if (spinnerRemotes.getCount() == 1) {
                executePull(remotes.get(0).getName());
            } else {
                String remote = config.getString(ConfigConstants.CONFIG_BRANCH_SECTION, repository.getBranch(), "remote");
                if (remote == null) {
                    remote = Constants.DEFAULT_REMOTE_NAME;
                }
                for (int i = 0; i < remotes.size(); i++) {
                    if (remotes.get(i).getName().equals(remote)) {
                        spinnerRemotes.setSelection(i);
                    }
                }
                FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this)
						.title(R.string.title_dialog_pull)
						.iconRes(R.drawable.ic_action_pull)
						.positiveText(R.string.button_pull)
						.negativeText(R.string.button_cancel)
						.customView(viewRemotes, false)
						.onPositive(new MaterialDialog.SingleButtonCallback() {
					public void onClick(MaterialDialog dialog, DialogAction which) {
                        FilesActivity.this.executePull(((RemoteConfig) spinnerRemotes.getSelectedItem()).getName());
                    }
                }).show().getWindow().getDecorView());
            }
        } catch (URISyntaxException e) {
            Toast.makeText(this, "Failed to read git config", 0).show();
            Log.e("Pocket Git", "", e);
        } catch (IOException e2) {
            Toast.makeText(this, "Failed to get repository information", 0).show();
            Log.e("Pocket Git", "", e2);
        }
    }

    private void executePull(final String remote) {
        CredentialStorage.checkCredentials(this, this.mProject, new CredentialStorage.CredentialListener() {
            public void credentialsSet() {
                Toast.makeText(FilesActivity.this, "Pulling from " + remote, 1).show();
                Intent intentPull = new Intent(FilesActivity.this, GitPull.class);
                intentPull.putExtra("id", Integer.toString(FilesActivity.this.mProject.getId()));
                intentPull.putExtra("remote", remote);
                FilesActivity.this.startService(intentPull);
            }

            public void invalidPrivateKey() {
                Toast.makeText(FilesActivity.this, "Invalid Private Key", 1).show();
            }
        });
    }

    @SuppressLint({"InflateParams"})
    private void optionFetch() {
        try {
            Repository repository = GitUtils.getRepository(this.mProject);
            StoredConfig config = repository.getConfig();
            List<RemoteConfig> remotes = RemoteConfig.getAllRemoteConfigs(config);
            View viewRemotes = LayoutInflater.from(this).inflate(R.layout.dialog_select_remote, (ViewGroup) null);
            final Spinner spinnerRemotes = (Spinner) viewRemotes.findViewById(R.id.spinner_remotes);
            spinnerRemotes.setAdapter(new RemoteAdapter(this, remotes));
            if (spinnerRemotes.getCount() == 0) {
                Toast.makeText(this, "No remotes configured", 0).show();
            } else if (spinnerRemotes.getCount() == 1) {
                executeFetch(remotes.get(0).getName());
            } else {
                String remote = config.getString(ConfigConstants.CONFIG_BRANCH_SECTION, repository.getBranch(), "remote");
                if (remote == null) {
                    remote = Constants.DEFAULT_REMOTE_NAME;
                }
                for (int i = 0; i < remotes.size(); i++) {
                    if (remotes.get(i).getName().equals(remote)) {
                        spinnerRemotes.setSelection(i);
                    }
                }
                FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this)
						.title(R.string.title_dialog_fetch)
						.iconRes(R.drawable.ic_action_fetch)
						.positiveText(R.string.button_fetch)
						.negativeText(R.string.button_cancel)
						.customView(viewRemotes, false)
						.onPositive(new MaterialDialog.SingleButtonCallback() {
					public void onClick(MaterialDialog dialog, DialogAction which) {
                        FilesActivity.this.executeFetch(((RemoteConfig) spinnerRemotes.getSelectedItem()).getName());
                    }
                }).show().getWindow().getDecorView());
            }
        } catch (URISyntaxException e) {
            Toast.makeText(this, "Failed to read git config", 0).show();
            Log.e("Pocket Git", "", e);
        } catch (IOException e2) {
            Toast.makeText(this, "Failed to get repository information", 0).show();
            Log.e("Pocket Git", "", e2);
        }
    }

    private void executeFetch(final String remote) {
        CredentialStorage.checkCredentials(this, this.mProject, new CredentialStorage.CredentialListener() {
            public void credentialsSet() {
                Toast.makeText(FilesActivity.this, "Fetching from " + remote, 1).show();
                Intent intentFetch = new Intent(FilesActivity.this, GitFetch.class);
                intentFetch.putExtra("id", Integer.toString(FilesActivity.this.mProject.getId()));
                intentFetch.putExtra("remote", remote);
                FilesActivity.this.startService(intentFetch);
            }

            public void invalidPrivateKey() {
                Toast.makeText(FilesActivity.this, "Invalid Private Key", 1).show();
            }
        });
    }

    @SuppressLint({"InflateParams"})
    private void optionPush() {
        try {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            Repository repository = GitUtils.getRepository(this.mProject);
            StoredConfig config = repository.getConfig();
            List<RemoteConfig> remotes = RemoteConfig.getAllRemoteConfigs(config);
            View viewRemotes = LayoutInflater.from(this).inflate(R.layout.dialog_select_remote, (ViewGroup) null);
            final Spinner spinnerRemotes = (Spinner) viewRemotes.findViewById(R.id.spinner_remotes);
            spinnerRemotes.setAdapter(new RemoteAdapter(this, remotes));
            if (spinnerRemotes.getCount() == 0) {
                Toast.makeText(this, "No remotes configured", 0).show();
            } else if (spinnerRemotes.getCount() == 1) {
                executePush(remotes.get(0).getName());
            } else {
                String remote = config.getString(ConfigConstants.CONFIG_BRANCH_SECTION, repository.getBranch(), "remote");
                if (remote == null) {
                    remote = Constants.DEFAULT_REMOTE_NAME;
                }
                for (int i = 0; i < remotes.size(); i++) {
                    if (remotes.get(i).getName().equals(remote)) {
                        spinnerRemotes.setSelection(i);
                    }
                }
                FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this)
						.title(R.string.title_dialog_push)
						.iconRes(R.drawable.ic_action_push)
						.positiveText(R.string.button_push)
						.negativeText(R.string.button_cancel)
						.onPositive(new MaterialDialog.SingleButtonCallback() {
					public void onClick(MaterialDialog dialog, DialogAction which) {
                        FilesActivity.this.executePush(((RemoteConfig) spinnerRemotes.getSelectedItem()).getName());
                    }
                }).customView(viewRemotes, false).show().getWindow().getDecorView());
            }
        } catch (URISyntaxException e) {
            Toast.makeText(this, "Failed to read git config", 0).show();
            Log.e("Pocket Git", "", e);
        } catch (IOException e2) {
            Toast.makeText(this, "Failed to get repository information", 0).show();
            Log.e("Pocket Git", "", e2);
        }
    }

    private void executePush(final String remote) {
        CredentialStorage.checkCredentials(this, this.mProject, new CredentialStorage.CredentialListener() {
            public void credentialsSet() {
                Toast.makeText(FilesActivity.this, "Pushing to " + remote, 1).show();
                Intent intentPush = new Intent(FilesActivity.this, GitPush.class);
                intentPush.putExtra("id", Integer.toString(FilesActivity.this.mProject.getId()));
                intentPush.putExtra("remote", remote);
                intentPush.putExtra("push_tags", false);
                FilesActivity.this.startService(intentPush);
            }

            public void invalidPrivateKey() {
                Toast.makeText(FilesActivity.this, "Invalid Private Key", 1).show();
            }
        });
    }

    @SuppressLint({"InflateParams"})
    private void optionTagPush() {
        try {
            SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            Repository repository = GitUtils.getRepository(this.mProject);
            StoredConfig config = repository.getConfig();
            List<RemoteConfig> remotes = RemoteConfig.getAllRemoteConfigs(config);
            View viewRemotes = LayoutInflater.from(this).inflate(R.layout.dialog_select_remote, (ViewGroup) null);
            final Spinner spinnerRemotes = (Spinner) viewRemotes.findViewById(R.id.spinner_remotes);
            spinnerRemotes.setAdapter(new RemoteAdapter(this, remotes));
            if (spinnerRemotes.getCount() == 0) {
                Toast.makeText(this, "No remotes configured", 0).show();
            } else if (spinnerRemotes.getCount() == 1) {
                executePushTags(remotes.get(0).getName());
            } else {
                String remote = config.getString(ConfigConstants.CONFIG_BRANCH_SECTION, repository.getBranch(), "remote");
                if (remote == null) {
                    remote = Constants.DEFAULT_REMOTE_NAME;
                }
                for (int i = 0; i < remotes.size(); i++) {
                    if (remotes.get(i).getName().equals(remote)) {
                        spinnerRemotes.setSelection(i);
                    }
                }
                FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this)
						.title(R.string.title_dialog_push)
						.iconRes(R.drawable.ic_action_push)
						.positiveText(R.string.button_push_tags)
						.negativeText(R.string.button_cancel)
						.onPositive(new MaterialDialog.SingleButtonCallback() {
					public void onClick(MaterialDialog dialog, DialogAction which) {
                        FilesActivity.this.executePushTags(((RemoteConfig) spinnerRemotes.getSelectedItem()).getName());
                    }
                }).customView(viewRemotes, false).show().getWindow().getDecorView());
            }
        } catch (URISyntaxException e) {
            Toast.makeText(this, "Failed to read git config", 0).show();
            Log.e("Pocket Git", "", e);
        } catch (IOException e2) {
            Toast.makeText(this, "Failed to get repository information", 0).show();
            Log.e("Pocket Git", "", e2);
        }
    }

    private void executePushTags(final String remote) {
        CredentialStorage.checkCredentials(this, this.mProject, new CredentialStorage.CredentialListener() {
            public void credentialsSet() {
                Toast.makeText(FilesActivity.this, "Pushing tags to " + remote, 1).show();
                Intent intentPush = new Intent(FilesActivity.this, GitPush.class);
                intentPush.putExtra("id", Integer.toString(FilesActivity.this.mProject.getId()));
                intentPush.putExtra("remote", remote);
                intentPush.putExtra("push_tags", true);
                FilesActivity.this.startService(intentPush);
            }

            public void invalidPrivateKey() {
                Toast.makeText(FilesActivity.this, "Invalid Private Key", 1).show();
            }
        });
    }

    private void optionRevert() {
        FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this)
				.title((CharSequence) "Revert")
				.iconRes(R.drawable.ic_action_reset)
				.content((CharSequence) "Discard all changes from the index and working tree?")
				.positiveText(R.string.button_revert)
				.negativeText(R.string.button_cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
            public void onClick(MaterialDialog dialog, DialogAction which) {
                try {
                    GitUtils.revert(FilesActivity.this.mProject);
                } catch (Exception e) {
                    Log.e("Pocket Git", "", e);
                }
                FilesActivity.this.refreshFiles(false);
            }
        }).show().getWindow().getDecorView());
    }

    @SuppressLint({"InflateParams"})
    private void optionNewFile() {
        final View dialogFileName = LayoutInflater.from(this).inflate(R.layout.dialog_single_input, (ViewGroup) null);
        ((TextView) dialogFileName.findViewById(R.id.text_content)).setText("Enter file name");
        FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this)
				.title((CharSequence) "New File")
				.iconRes(R.drawable.ic_action_add_file)
				.customView(dialogFileName, false)
				.positiveText((CharSequence) "Create")
				.negativeText(R.string.button_cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
            public void onClick(MaterialDialog dialog, DialogAction which) {
                try {
                    if (new File(FilesActivity.this.mCurrentFolder, ((EditText) dialogFileName.findViewById(R.id.text_input)).getText().toString().trim()).createNewFile()) {
                        Toast.makeText(FilesActivity.this, "File created", 0).show();
                    } else {
                        Toast.makeText(FilesActivity.this, "Failed to create file", 0).show();
                    }
                } catch (IOException e) {
                    Toast.makeText(FilesActivity.this, "Failed to create file", 0).show();
                }
                FilesActivity.this.refreshFiles(false);
            }
        }).show().getWindow().getDecorView());
    }

    @SuppressLint({"InflateParams"})
    private void optionNewFolder() {
        final View dialogFolderName = LayoutInflater.from(this).inflate(R.layout.dialog_single_input, (ViewGroup) null);
        ((TextView) dialogFolderName.findViewById(R.id.text_content)).setText("Enter folder name");
        FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this)
				.title((CharSequence) "New Folder")
				.iconRes(R.drawable.ic_action_add_folder)
				.customView(dialogFolderName, false)
				.positiveText((CharSequence) "Create")
				.negativeText(R.string.button_cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
            public void onClick(MaterialDialog dialog, DialogAction which) {
                EditText input = (EditText) dialogFolderName.findViewById(R.id.text_input);
                if (new File(FilesActivity.this.mCurrentFolder, input.getText().toString().trim()).mkdir()) {
                    Toast.makeText(FilesActivity.this, "Folder created", 0).show();
                    File unused = FilesActivity.this.mCurrentFolder = new File(FilesActivity.this.mCurrentFolder, input.getText().toString());
                } else {
                    Toast.makeText(FilesActivity.this, "Failed to create folder", 0).show();
                }
                FilesActivity.this.refreshFiles(false);
            }
        }).show().getWindow().getDecorView());
    }

    private void optionDeleteBranch() {
        try {
            Repository repository = getRepository(this.mProject);
            String currentBranch = repository.getFullBranch();
            ListBranchCommand branchList = new Git(repository).branchList();
            if (((SwitchCompat) findViewById(R.id.toggle_remote)).isChecked()) {
                branchList.setListMode(ListBranchCommand.ListMode.REMOTE);
            }
            List<Ref> branches = branchList.call();
            final List<Ref> selectableBranches = new ArrayList<>();
            for (Ref ref : branches) {
                if (!ref.getName().equals("HEAD") && !ref.getName().equals(currentBranch)) {
                    selectableBranches.add(ref);
                }
            }
            repository.close();
            if (selectableBranches.size() == 0) {
                Toast.makeText(this, "No other branches to delete!", 0).show();
                return;
            }
            // final ArrayAdapter<Ref> adapter = new BranchArrayAdapter(this, selectableBranches);
			List<String> namesOfBranches = new ArrayList<>();
			for (Ref ref : selectableBranches) {
				String name = ref.getName();
				if (name.startsWith(Constants.R_HEADS)) {
					name = name.substring(11);
				}
				namesOfBranches.add(name);
			}
            final MaterialDialog dialog = new MaterialDialog.Builder(this)
				.title((CharSequence) "Select Branch to Delete")
				.iconRes(R.drawable.ic_action_delete_branch)
				.items(namesOfBranches)
				.itemsCallback(new MaterialDialog.ListCallback() {
					@Override
					public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
						Set<String> set = new HashSet<>();
						set.add(selectableBranches.get(which).getName());
						FilesActivity.this.actionDeleteBranch(set, "Branch", "Branches");
					}
				})
				.negativeText(R.string.button_cancel).show();
            FontUtils.setRobotoFont(this, dialog.getWindow().getDecorView());
        } catch (Exception e) {
            Log.e("Pocket Git", "", e);
        }
    }

    public void actionDeleteBranch(final Set<String> names, String typeSingular, String typePlural) {
        final String type;
        if (names.size() > 1) {
            type = typePlural;
        } else {
            type = typeSingular;
        }
        FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this)
				.title((CharSequence) "Delete " + type)
				.iconRes(R.drawable.ic_action_delete_branch)
				.content((CharSequence) "Delete " + (names.size() == 1 ? names.iterator().next() + " " + type + "?" : String.valueOf(names.size()) + " " + type + "?"))
				.positiveText(R.string.button_delete)
				.negativeText(R.string.button_cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
			public void onClick(MaterialDialog dialog, DialogAction which) {
                try {
                    Repository repository = FilesActivity.this.getRepository(FilesActivity.this.mProject);
                    DeleteBranchCommand delete = new Git(repository).branchDelete();
                    delete.setBranchNames(names.toArray(new String[0]));
                    delete.setForce(true);
                    delete.call();
                    repository.close();
                    Toast.makeText(FilesActivity.this, type + " deleted!", 0).show();
                    FilesActivity.this.initializeBranches();
                } catch (Exception e) {
                    Toast.makeText(FilesActivity.this, "Failed to delete: " + e.getMessage(), 0).show();
                    Log.e("Pocket Git", "", e);
                }
            }
        }).show().getWindow().getDecorView());
    }

    private void optionMerge() {
        try {
            new MergeDialog(this).showDialog(getRepository(this.mProject)).setMergeListener(new MergeDialog.MergeListener() {
                public void mergeResult(MergeResult result) {
                    boolean z;
                    FilesActivity filesActivity = FilesActivity.this;
                    String replace = result.getMergeStatus().toString().replace('-', ' ');
                    if (!result.getMergeStatus().isSuccessful()) {
                        z = true;
                    } else {
                        z = false;
                    }
                    filesActivity.showMessage(replace, z);
                    FilesActivity.this.refreshFiles(false);
                }

                public void mergeFailed(Exception e) {
                    Toast.makeText(FilesActivity.this, "Failed to merge: " + e.getMessage(), 0).show();
                }
            });
        } catch (Exception e) {
            Log.e("PocketGit", "", e);
        }
    }

    @SuppressLint({"InflateParams"})
    private void commit() {
        String author = "";
        String email = "";
        try {
            Repository repository = getRepository(this.mProject);
            StoredConfig config = repository.getConfig();
            author = config.getString(ConfigConstants.CONFIG_USER_SECTION, (String) null, ConfigConstants.CONFIG_KEY_NAME);
            email = config.getString(ConfigConstants.CONFIG_USER_SECTION, (String) null, "email");
            repository.close();
        } catch (IOException e) {
            Log.e("Pocket Git", "", e);
        }
        if (author.trim().equals("") || email.trim().equals("")) {
            View dialogAuthor = LayoutInflater.from(this).inflate(R.layout.dialog_author, (ViewGroup) null);
            final EditText editAuthor = (EditText) dialogAuthor.findViewById(R.id.text_author);
            editAuthor.setText(author);
            final EditText editEmail = (EditText) dialogAuthor.findViewById(R.id.text_email);
            editEmail.setText(email);
            FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this)
					.title(R.string.title_author_data)
					.iconRes(R.drawable.ic_author)
					.customView(dialogAuthor, false)
					.positiveText(R.string.button_save)
					.negativeText(R.string.button_cancel)
					.onPositive(new MaterialDialog.SingleButtonCallback() {
				public void onClick(MaterialDialog dialog, DialogAction which) {
                    SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(FilesActivity.this).edit();
                    edit.putString("pref_default_author", editAuthor.getText().toString().trim());
                    edit.putString("pref_default_email", editEmail.getText().toString().trim());
                    edit.apply();
                    FilesActivity.this.commit();
                }
            }).show().getWindow().getDecorView());
            return;
        }
        final View dialogCommit = LayoutInflater.from(this).inflate(R.layout.dialog_commit, (ViewGroup) null);
        final ListView listStaged = (ListView) dialogCommit.findViewById(R.id.list_staged);
        listStaged.setAdapter(new GitFileAdapter(this, getStagedChanges()));
        MaterialDialog dialog = new MaterialDialog.Builder(this)
				.title(R.string.title_commit)
				.customView(dialogCommit, false)
				.positiveText(R.string.button_ok)
				.negativeText(R.string.button_cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
			public void onClick(MaterialDialog dialog, DialogAction which) {
                try {
                    Repository repo = FilesActivity.this.getRepository(FilesActivity.this.mProject);
                    new Git(repo).commit().setMessage(((TextView) dialogCommit.findViewById(R.id.text_message)).getText().toString()).call();
                    repo.close();
                    FilesActivity.this.refreshFiles(false);
                } catch (IOException | GitAPIException e) {
                    Toast.makeText(FilesActivity.this, "Failed to commit: " + e.getMessage(), 0).show();
                    Log.e("Pocket Git", "", e);
                }
            }
        }).show();
        listStaged.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                GitFile file = (GitFile) listStaged.getItemAtPosition(position);
                if (file.isDiffable()) {
                    FilesActivity.this.actionDiffFile(file);
                }
            }
        });
        FontUtils.setRobotoFont(this, dialog.getWindow().getDecorView());
    }

    private List<GitFile> getStagedChanges() {
        ArrayList<GitFile> result = new ArrayList<>();
        if (this.mStatus != null) {
            for (String file : this.mStatus.getAdded()) {
                result.add(new GitFile(new File(this.mProject.getLocalPath(), file), GitFile.ADDED));
            }
            for (String file2 : this.mStatus.getRemoved()) {
                result.add(new GitFile(new File(this.mProject.getLocalPath(), file2), GitFile.REMOVED));
            }
            for (String file3 : this.mStatus.getChanged()) {
                result.add(new GitFile(new File(this.mProject.getLocalPath(), file3), GitFile.CHANGED));
            }
        }
        return result;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_activity_actions, menu);
        SearchView search = (SearchView) menu.findItem(R.id.search_view).getActionView();
        search.setInputType(145);
        FontUtils.setRobotoFont(this, search);
        search.setSearchableInfo(((SearchManager) getSystemService("search")).getSearchableInfo(getComponentName()));
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            boolean isShowingSearch = false;
            public AsyncTask<String, Void, List<GitFile>> mTask;

            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            public boolean onQueryTextChange(String query) {
                performSearch(query);
                return true;
            }

            private void performSearch(String query) {
                if (FilesActivity.this.mListFiles.getAdapter() != null) {
                    if (this.mTask != null) {
                        this.mTask.cancel(true);
                    }
                    FilesActivity.this.findViewById(R.id.progress).setVisibility(8);
                    if (query.trim().equals("") && this.isShowingSearch) {
                        this.isShowingSearch = false;
                        ((GitFileAdapter) FilesActivity.this.mListFiles.getAdapter()).refill(new LinkedList<>());
                        FilesActivity.this.refreshFiles(true);
                    }
                    if (!query.trim().equals("")) {
                        if (!this.isShowingSearch) {
                            ((GitFileAdapter) FilesActivity.this.mListFiles.getAdapter()).refill(new LinkedList<>());
                        }
                        this.isShowingSearch = true;
                        this.mTask = new SearchTask(FilesActivity.this.mCurrentFolder).execute(new String[] { query });
                    }
                }
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    private void actionUnstage() {
        List<GitFile> filesToUnstage = new ArrayList<>();
        for (int i = 0; i < this.mListFiles.getCheckedItemCount(); i++) {
            GitFile file = (GitFile) this.mListFiles.getItemAtPosition((int) this.mListFiles.getCheckedItemIds()[i]);
            if (file.isUnstageable()) {
                filesToUnstage.add(file);
            }
        }
        try {
            GitUtils.unstageFiles(this.mProject, filesToUnstage);
        } catch (Exception e) {
            Log.e("Pocket Git", "", e);
        }
        refreshFiles(false);
    }

    private void actionStage() {
        List<GitFile> filesToStage = new ArrayList<>();
        for (int i = 0; i < this.mListFiles.getCheckedItemCount(); i++) {
            GitFile file = (GitFile) this.mListFiles.getItemAtPosition((int) this.mListFiles.getCheckedItemIds()[i]);
            if (file.isStageable()) {
                filesToStage.add(file);
            }
        }
        try {
            GitUtils.stageFiles(this.mProject, filesToStage);
        } catch (Exception e) {
            Log.e("Pocket Git", "", e);
        }
        refreshFiles(false);
    }

    protected void actionRevertFiles() {
        List<GitFile> toRevert = new ArrayList<>();
        for (int i = 0; i < this.mListFiles.getCheckedItemCount(); i++) {
            GitFile file = (GitFile) this.mListFiles.getItemAtPosition((int) this.mListFiles.getCheckedItemIds()[i]);
            if (file.isRevertable()) {
                toRevert.add(file);
            }
        }
        try {
            GitUtils.revertFiles(this.mProject, toRevert);
        } catch (Exception e) {
            Log.e("Pocket Git", "", e);
        }
        refreshFiles(false);
    }

    protected void actionDeleteFiles() {
        final List<File> toDelete = new ArrayList<>();
        for (int i = 0; i < this.mListFiles.getCheckedItemCount(); i++) {
            toDelete.add(((GitFile) this.mListFiles.getItemAtPosition((int) this.mListFiles.getCheckedItemIds()[i])).getFile());
        }
        FontUtils.setRobotoFont(this, new MaterialDialog.Builder(this)
				.title((CharSequence) "Delete files")
				.content((CharSequence) "Are you sure?")
				.positiveText(R.string.button_delete)
				.negativeText(R.string.button_cancel)
				.onPositive(new MaterialDialog.SingleButtonCallback() {
            public void onClick(MaterialDialog dialog, DialogAction which) {
                for (File file : toDelete) {
                    try {
                        deleteFile(file);
                    } catch (Exception e) {
                        Log.e("Pocket Git", "", e);
                    }
                }
                FilesActivity.this.refreshFiles(false);
                dialog.dismiss();
            }

            private void deleteFile(File file) {
                if (file.isFile()) {
                    file.delete();
                    return;
                }
                for (File child : file.listFiles()) {
                    deleteFile(child);
                }
                file.delete();
            }
        }).show().getWindow().getDecorView());
    }

    protected void actionRemoveFiles() {
        List<GitFile> filesToDelete = new ArrayList<>();
        for (int i = 0; i < this.mListFiles.getCheckedItemCount(); i++) {
            filesToDelete.add((GitFile) this.mListFiles.getItemAtPosition((int) this.mListFiles.getCheckedItemIds()[i]));
        }
        try {
            GitUtils.removeFiles(this.mProject, filesToDelete);
        } catch (Exception e) {
            Log.e("Pocket Git", "", e);
        }
        refreshFiles(false);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {
            actionCheckoutLocal(data.getStringExtra(Constants.TYPE_COMMIT));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showMessage(String message, boolean error) {
        if (error) {
            showSnackBar(findViewById(R.id.list_files), message, -1);
        } else {
            showErrorSnackBar(findViewById(R.id.list_files), message);
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (this.mStatus != null) {
            if (!this.mStatus.getAdded().isEmpty() || !this.mStatus.getChanged().isEmpty() || !this.mStatus.getRemoved().isEmpty()) {
                if (this.mCommitFAB.getVisibility() == 8) {
                    this.mCommitFAB.setAnimation(AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_bottom));
                }
                this.mCommitFAB.setVisibility(0);
            } else {
                if (this.mCommitFAB.getVisibility() == 0) {
                    this.mCommitFAB.setAnimation(AnimationUtils.loadAnimation(this, R.anim.abc_slide_out_bottom));
                }
                this.mCommitFAB.setVisibility(8);
            }
            if (!this.mStatus.getModified().isEmpty() || !this.mStatus.getAdded().isEmpty() || !this.mStatus.getChanged().isEmpty() || !this.mStatus.getRemoved().isEmpty()) {
                menu.findItem(R.id.action_revert).setVisible(true);
            } else {
                menu.findItem(R.id.action_revert).setVisible(false);
            }
        }
        menu.findItem(R.id.action_push).setTitle(getString(R.string.action_push) + (this.mCountAhead > 0 ? " (" + this.mCountAhead + ")" : ""));
        menu.findItem(R.id.action_pull).setTitle(getString(R.string.action_pull) + (this.mCountBehind > 0 ? " (" + this.mCountBehind + ")" : ""));
        return super.onPrepareOptionsMenu(menu);
    }

    protected void onUpdateData(Intent intent) {
        super.onUpdateData(intent);
        if (intent.getStringExtra("type").equals(UpdatableActivity.PULL_COMPLETED)) {
            initializeBranches();
            refreshFiles(false);
        }
        if (intent.getStringExtra("type").equals(UpdatableActivity.PUSH_COMPLETED)) {
            refreshFiles(true);
        }
        if (intent.getStringExtra("type").equals(UpdatableActivity.FETCH_COMPLETED)) {
            refreshFiles(true);
        }
    }

    class SearchTask extends AsyncTask<String, Void, List<GitFile>> {
        private final File mFolder;

        public SearchTask(File folder) {
            this.mFolder = folder;
        }

        protected void onPreExecute() {
            FilesActivity.this.findViewById(R.id.progress).setVisibility(0);
        }

        protected List<GitFile> doInBackground(String... strArr) {
            List<GitFile> result = new ArrayList<>();
            try {
                File[] searchFiles = FileUtils.searchFiles(this.mFolder, strArr[0], new File(FilesActivity.this.mProject.getLocalPath() + "/.git"));
                if (!isCancelled()) {
                    if (searchFiles != null) {
                        for (File file : searchFiles) {
                            if (isCancelled()) {
                                break;
                            }
                            Set<String> states = FilesActivity.this.calculateFileStates(file);
                            if (states.size() == 0) {
                                states.add("commited");
                            }
                            result.add(new GitFile(file, states));
                        }
                    }
                    HashSet<String> hashSet = new HashSet<>();
                    hashSet.add("missing");
                    for (String str : FilesActivity.this.mStatus.getMissing()) {
                        if (isCancelled()) {
                            break;
                        } else if (new File(FilesActivity.this.mProject.getLocalPath(), str).getParentFile().equals(FilesActivity.this.mCurrentFolder)) {
                            result.add(new GitFile(new File(FilesActivity.this.mProject.getLocalPath(), str), hashSet));
                        }
                    }
                    if (!isCancelled()) {
                        Collections.sort(result, FileComparator.getPreferedComparator(FilesActivity.this));
                    }
                }
            } catch (IOException e) {
                Log.e("Pocket Git", "", e);
            } catch (NoWorkTreeException e) {
                Log.e("Pocket Git", "", e);
            }
            return result;
        }

        protected void onPostExecute(List<GitFile> result) {
            FilesActivity.this.findViewById(R.id.progress).setVisibility(8);
            ((GitFileAdapter) FilesActivity.this.mListFiles.getAdapter()).refill(result);
        }
    }
}
