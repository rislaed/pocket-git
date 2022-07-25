package com.aor.pocketgit.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;
import com.aor.pocketgit.R;
import com.aor.pocketgit.adapters.DiffAdapter;
import com.aor.pocketgit.data.Project;
import com.aor.pocketgit.database.ProjectsDataSource;
import com.aor.pocketgit.utils.GitUtils;
import com.aor.pocketgit.utils.MD5Util;
import java.io.IOException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

public class CommitActivity extends UpdatableActivity {
    private RevCommit mCommit;
    private Project mProject;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commit);
        setSupportActionBar((Toolbar) findViewById(R.id.action_bar));
        String id = getIntent().getStringExtra("id");
        String commit = getIntent().getStringExtra(Constants.TYPE_COMMIT);
        ProjectsDataSource datasource = new ProjectsDataSource(this);
        datasource.open();
        this.mProject = datasource.getProject(id);
        datasource.close();
        final ListView listDiff = (ListView) findViewById(R.id.list_differences);
        try {
            Repository repository = GitUtils.getRepository(this.mProject);
            RevWalk walk = new RevWalk(repository);
            this.mCommit = walk.parseCommit(repository.resolve(commit));
            listDiff.setAdapter(new DiffAdapter(this, GitUtils.getDiffList(repository, this.mCommit, walk)));
            walk.dispose();
            repository.close();
        } catch (IOException e) {
            Log.e("Pocket Git", "", e);
        }
        ((TextView) findViewById(R.id.text_commit)).setText(this.mCommit.getName().substring(0, 8));
        ((TextView) findViewById(R.id.text_message_short)).setText(this.mCommit.getShortMessage());
        ((TextView) findViewById(R.id.text_author)).setText(this.mCommit.getAuthorIdent().getName() + " <" + this.mCommit.getAuthorIdent().getEmailAddress() + ">");
        ((TextView) findViewById(R.id.text_message_complete)).setText(this.mCommit.getFullMessage());
        ((TextView) findViewById(R.id.text_date)).setText(GitUtils.formatDate(this.mCommit.getCommitTime()));
        listDiff.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                try {
                    Repository repository = GitUtils.getRepository(CommitActivity.this.mProject);
                    String diffMessage = GitUtils.getDiffText(repository, (DiffEntry) listDiff.getAdapter().getItem(position));
                    Intent intent = new Intent(CommitActivity.this, DiffActivity.class);
                    intent.putExtra(ConfigConstants.CONFIG_DIFF_SECTION, diffMessage);
                    CommitActivity.this.startActivity(intent);
                    repository.close();
                } catch (IOException e) {
                    Log.e("Pocket Git", "", e);
                }
            }
        });
        // if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("pref_gravatar", true)) {
            // Picasso.with(this).load("http://www.gravatar.com/avatar/" + MD5Util.md5Hex(this.mCommit.getAuthorIdent().getEmailAddress()) + "?s=204&d=404").into((ImageView) findViewById(R.id.image_photo));
        // }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.commit_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_checkout) {
            Intent intent = new Intent();
            intent.putExtra(Constants.TYPE_COMMIT, this.mCommit.getName());
            setResult(1, intent);
            finish();
        }
        if (item.getItemId() == 16908332) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
