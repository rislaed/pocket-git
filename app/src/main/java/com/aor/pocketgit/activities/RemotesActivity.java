package com.aor.pocketgit.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.afollestad.materialdialogs.MaterialDialog;
import com.aor.pocketgit.R;
import com.aor.pocketgit.adapters.RemoteAdapter;
import com.aor.pocketgit.data.Project;
import com.aor.pocketgit.database.ProjectsDataSource;
import com.aor.pocketgit.utils.FontUtils;
import com.aor.pocketgit.utils.GitUtils;
import com.aor.pocketgit.widgets.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

public class RemotesActivity extends UpdatableActivity implements AdapterView.OnItemClickListener, AbsListView.MultiChoiceModeListener {
    private ListView mListRemotes;
    private Project mProject;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_remotes);
        setupFAB();
        setSupportActionBar((Toolbar) findViewById(R.id.action_bar));
        String id = getIntent().getStringExtra("id");
        ProjectsDataSource datasource = new ProjectsDataSource(this);
        datasource.open();
        this.mProject = datasource.getProject(id);
        datasource.close();
        this.mListRemotes = (ListView) findViewById(R.id.list_remotes);
        this.mListRemotes.setChoiceMode(3);
        this.mListRemotes.setMultiChoiceModeListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected void onStart() {
        refreshRemotes();
        super.onStart();
    }

    @SuppressLint({"InflateParams"})
    private void setupFAB() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        new FloatingActionButton.Builder(this).withColor(typedValue.data).withDrawable(getResources().getDrawable(R.drawable.ic_action_add)).withMargins(0, 0, 16, 16).create().setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final View viewCreateRemote = RemotesActivity.this.getLayoutInflater().inflate(R.layout.dialog_create_remote, (ViewGroup) null);
                FontUtils.setRobotoFont(RemotesActivity.this, new MaterialDialog.Builder(RemotesActivity.this).title((CharSequence) "Add Remote").iconRes(R.drawable.ic_add).positiveText((int) R.string.button_create).negativeText((int) R.string.button_cancel).customView(viewCreateRemote, false).callback(new MaterialDialog.ButtonCallback() {
                    public void onPositive(MaterialDialog dialog) {
                        try {
                            StoredConfig config = GitUtils.getRepository(RemotesActivity.this.mProject).getConfig();
                            String name = ((EditText) viewCreateRemote.findViewById(R.id.edit_name)).getText().toString().trim().toLowerCase();
                            String url = ((EditText) viewCreateRemote.findViewById(R.id.edit_url)).getText().toString().trim();
                            RemoteConfig remote = new RemoteConfig(config, name);
                            remote.addURI(new URIish(url));
                            remote.addFetchRefSpec(new RefSpec("+refs/heads/*:refs/remotes/" + name + RefSpec.WILDCARD_SUFFIX));
                            remote.update(config);
                            config.save();
                            RemotesActivity.this.refreshRemotes();
                            dialog.dismiss();
                        } catch (Exception e) {
                            Toast.makeText(RemotesActivity.this, "Failed to create remote", 0).show();
                            Log.e("Pocket Git", "", e);
                        }
                    }
                }).show().getWindow().getDecorView());
            }
        });
    }

    private void refreshRemotes() {
        try {
            this.mListRemotes.setAdapter(new RemoteAdapter(this, RemoteConfig.getAllRemoteConfigs(GitUtils.getRepository(this.mProject).getConfig())));
            this.mListRemotes.setOnItemClickListener(this);
        } catch (Exception e) {
            Log.e("Pocket Git", "", e);
            Toast.makeText(this, "Failed to read git config", 0).show();
            finish();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent intent = new Intent(this, RemoteActivity.class);
        intent.putExtra("id", Integer.toString(this.mProject.getId()));
        intent.putExtra("remote", ((RemoteConfig) this.mListRemotes.getItemAtPosition(position)).getName());
        startActivity(intent);
    }

    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.cab_delete, menu);
        return true;
    }

    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                try {
                    StoredConfig config = GitUtils.getRepository(this.mProject).getConfig();
                    List<RemoteConfig> toDelete = new ArrayList<>();
                    for (int i = 0; i < this.mListRemotes.getCheckedItemCount(); i++) {
                        toDelete.add((RemoteConfig) this.mListRemotes.getItemAtPosition((int) this.mListRemotes.getCheckedItemIds()[i]));
                    }
                    for (RemoteConfig remote : toDelete) {
                        config.unsetSection("remote", remote.getName());
                    }
                    config.save();
                    mode.finish();
                    refreshRemotes();
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to delete remote", 0).show();
                }
                return true;
            default:
                return false;
        }
    }

    public void onDestroyActionMode(ActionMode mode) {
    }

    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        if (this.mListRemotes.getCheckedItemCount() == 0) {
            mode.finish();
        } else {
            mode.invalidate();
        }
    }
}
