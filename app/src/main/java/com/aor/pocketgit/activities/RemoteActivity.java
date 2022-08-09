package com.aor.pocketgit.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.aor.pocketgit.R;
import com.aor.pocketgit.adapters.RefSpecAdapter;
import com.aor.pocketgit.data.Project;
import com.aor.pocketgit.data.TypedRefSpec;
import com.aor.pocketgit.database.ProjectsDataSource;
import com.aor.pocketgit.utils.FontUtils;
import com.aor.pocketgit.utils.GitUtils;
import com.aor.pocketgit.widgets.FloatingActionButton;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.RemoteConfig;
import org.eclipse.jgit.transport.URIish;

public class RemoteActivity extends UpdatableActivity implements AbsListView.MultiChoiceModeListener {
    private StoredConfig mConfig;
    private ListView mListRefSpecs;
    private RemoteConfig mRemoteConfig;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote);
        setSupportActionBar((Toolbar) findViewById(R.id.action_bar));
        setupFAB();
        String id = getIntent().getStringExtra("id");
        String remote = getIntent().getStringExtra("remote");
        ProjectsDataSource datasource = new ProjectsDataSource(this);
        datasource.open();
        Project project = datasource.getProject(id);
        datasource.close();
        try {
            this.mConfig = GitUtils.getRepository(project).getConfig();
            this.mRemoteConfig = new RemoteConfig(this.mConfig, remote);
            getSupportActionBar().setSubtitle((CharSequence) this.mRemoteConfig.getName());
        } catch (IOException | URISyntaxException e) {
            Log.e("Pocket Git", "", e);
            Toast.makeText(this, "Failed to read git config", 0).show();
            finish();
        }
        this.mListRefSpecs = (ListView) findViewById(R.id.list_refspec);
        this.mListRefSpecs.setChoiceMode(3);
        this.mListRefSpecs.setMultiChoiceModeListener(this);
        refreshRemote();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void refreshRemote() {
        ((EditText) findViewById(R.id.edit_url)).setText(this.mRemoteConfig.getURIs().get(0).toASCIIString());
        List<TypedRefSpec> refSpecs = new ArrayList<>();
        for (RefSpec refSpec : this.mRemoteConfig.getFetchRefSpecs()) {
            refSpecs.add(new TypedRefSpec(refSpec, TypedRefSpec.TYPE.FETCH));
        }
        for (RefSpec refSpec2 : this.mRemoteConfig.getPushRefSpecs()) {
            refSpecs.add(new TypedRefSpec(refSpec2, TypedRefSpec.TYPE.PUSH));
        }
        this.mListRefSpecs.setAdapter(new RefSpecAdapter(this, refSpecs));
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
            case R.id.action_cancel:
                cancel();
                break;
            case R.id.action_save:
                try {
                    this.mRemoteConfig.removeURI(this.mRemoteConfig.getURIs().get(0));
                    this.mRemoteConfig.addURI(new URIish(((EditText) findViewById(R.id.edit_url)).getText().toString().trim()));
                    this.mRemoteConfig.update(this.mConfig);
                    this.mConfig.save();
                    finish();
                    break;
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to save git config", 0).show();
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
                RemoteActivity.this.finish();
            }
        }).show().getWindow().getDecorView());
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.save_cancel_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint({"InflateParams"})
    private void setupFAB() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(R.attr.colorPrimary, typedValue, true);
        new FloatingActionButton.Builder(this).withColor(typedValue.data).withDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_add)).withMargins(0, 0, 16, 16).create().setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                final View viewCreateRefSpec = RemoteActivity.this.getLayoutInflater().inflate(R.layout.dialog_create_refspec, (ViewGroup) null);
                FontUtils.setRobotoFont(RemoteActivity.this, new MaterialDialog.Builder(RemoteActivity.this)
						.title((CharSequence) "Add RefSpec")
						.iconRes(R.drawable.ic_add)
						.positiveText(R.string.button_create)
						.negativeText(R.string.button_cancel)
						.customView(viewCreateRefSpec, false)
						.onPositive(new MaterialDialog.SingleButtonCallback() {
					public void onClick(MaterialDialog dialog, DialogAction which) {
                        String refspec = ((EditText) viewCreateRefSpec.findViewById(R.id.edit_refspec)).getText().toString().trim().toLowerCase();
                        boolean fetch = ((RadioButton) viewCreateRefSpec.findViewById(R.id.radio_fetch)).isChecked();
                        if (refspec.trim().equals("")) {
                            dialog.dismiss();
                            return;
                        }
                        if (fetch) {
                            RemoteActivity.this.mRemoteConfig.addFetchRefSpec(new RefSpec(refspec));
                        } else {
                            RemoteActivity.this.mRemoteConfig.addPushRefSpec(new RefSpec(refspec));
                        }
                        RemoteActivity.this.mRemoteConfig.update(RemoteActivity.this.mConfig);
                        RemoteActivity.this.refreshRemote();
                        dialog.dismiss();
                    }
                }).show().getWindow().getDecorView());
            }
        });
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
                    List<TypedRefSpec> toDelete = new ArrayList<>();
                    for (int i = 0; i < this.mListRefSpecs.getCheckedItemCount(); i++) {
                        toDelete.add((TypedRefSpec) this.mListRefSpecs.getItemAtPosition((int) this.mListRefSpecs.getCheckedItemIds()[i]));
                    }
                    for (TypedRefSpec refspec : toDelete) {
                        if (refspec.getType() == TypedRefSpec.TYPE.FETCH) {
                            this.mRemoteConfig.removeFetchRefSpec(refspec.getRefSpec());
                        } else {
                            this.mRemoteConfig.removePushRefSpec(refspec.getRefSpec());
                        }
                    }
                    mode.finish();
                    refreshRemote();
                } catch (Exception e) {
                    Toast.makeText(this, "Failed to delete refspec", 0).show();
                }
                return true;
            default:
                return false;
        }
    }

    public void onDestroyActionMode(ActionMode mode) {
    }

    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        if (this.mListRefSpecs.getCheckedItemCount() == 0) {
            mode.finish();
        } else {
            mode.invalidate();
        }
    }
}
