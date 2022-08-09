package com.aor.pocketgit.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.aor.pocketgit.R;
import com.aor.pocketgit.adapters.CommitAdapter;
import com.aor.pocketgit.data.Project;
import com.aor.pocketgit.database.ProjectsDataSource;
import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotCommitList;
import org.eclipse.jgit.revplot.PlotLane;
import org.eclipse.jgit.revplot.PlotWalk;

public class LogActivity extends UpdatableActivity {
    public static final int COMMIT_SELECTED = 1;
    private Project mProject;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);
        setSupportActionBar((Toolbar) findViewById(R.id.action_bar));
        String id = getIntent().getStringExtra("id");
        ProjectsDataSource datasource = new ProjectsDataSource(this);
        datasource.open();
        this.mProject = datasource.getProject(id);
        datasource.close();
        final ListView listLog = (ListView) findViewById(R.id.list_log);
        listLog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(LogActivity.this, CommitActivity.class);
                intent.putExtra("id", Integer.toString(LogActivity.this.mProject.getId()));
                intent.putExtra(Constants.TYPE_COMMIT, ((PlotCommit) listLog.getAdapter().getItem(position)).getName());
                LogActivity.this.startActivityForResult(intent, 0);
            }
        });
        new AsyncTask<Void, Void, PlotCommitList<PlotLane>>() {
            protected void onPreExecute() {
                LogActivity.this.findViewById(R.id.progress).setVisibility(0);
            }

            protected PlotCommitList<PlotLane> doInBackground(Void... params) {
                return LogActivity.this.initializeList();
            }

            protected void onPostExecute(PlotCommitList<PlotLane> result) {
                LogActivity.this.findViewById(R.id.progress).setVisibility(8);
                if (result == null) {
                    LogActivity.this.finish();
                } else {
                    ((ListView) LogActivity.this.findViewById(R.id.list_log)).setAdapter(new CommitAdapter(LogActivity.this, result));
                }
            }
        }.execute(new Void[0]);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private PlotCommitList<PlotLane> initializeList() {
        try {
            Repository repository = new RepositoryBuilder().setGitDir(new File(this.mProject.getLocalPath() + "/.git")).readEnvironment().build();
            PlotWalk revWalk = new PlotWalk(repository);
            revWalk.markStart(revWalk.parseCommit(repository.resolve(repository.getFullBranch())));
            PlotCommitList<PlotLane> plotCommitList = new PlotCommitList<>();
            plotCommitList.source(revWalk);
            plotCommitList.fillTo(Integer.MAX_VALUE);
            return plotCommitList;
        } catch (IOException e) {
            Log.e("Pocket Git", "", e);
            Toast.makeText(this, "Failed to generate log", 0).show();
            return null;
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {
            String commit = data.getStringExtra(Constants.TYPE_COMMIT);
            Intent intent = new Intent();
            intent.putExtra(Constants.TYPE_COMMIT, commit);
            setResult(1, intent);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
