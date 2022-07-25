package com.aor.pocketgit.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import com.aor.pocketgit.R;
import com.aor.pocketgit.adapters.BlameLineAdapter;
import com.aor.pocketgit.data.BlameLine;
import com.aor.pocketgit.data.Project;
import com.aor.pocketgit.database.ProjectsDataSource;
import com.aor.pocketgit.utils.GitUtils;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.jgit.api.BlameCommand;
import org.eclipse.jgit.blame.BlameResult;
import org.eclipse.jgit.lib.ConfigConstants;
import org.eclipse.jgit.lib.Repository;

public class BlameActivity extends UpdatableActivity {
    private String mPath;
    private Project mProject;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blame);
        setSupportActionBar((Toolbar) findViewById(R.id.action_bar));
        String id = getIntent().getStringExtra("id");
        ProjectsDataSource datasource = new ProjectsDataSource(this);
        datasource.open();
        this.mProject = datasource.getProject(id);
        datasource.close();
        this.mPath = getIntent().getStringExtra(ConfigConstants.CONFIG_KEY_PATH);
        new AsyncTask<Void, Void, List<BlameLine>>() {
            protected void onPreExecute() {
                BlameActivity.this.findViewById(R.id.progress).setVisibility(0);
            }

            protected List<BlameLine> doInBackground(Void... params) {
                return BlameActivity.this.update();
            }

            protected void onPostExecute(List<BlameLine> result) {
                BlameActivity.this.findViewById(R.id.progress).setVisibility(8);
                if (result == null) {
                    BlameActivity.this.finish();
                } else {
                    ((ListView) BlameActivity.this.findViewById(R.id.list_blame)).setAdapter(new BlameLineAdapter(BlameActivity.this, result));
                }
            }
        }.execute(new Void[0]);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private List<BlameLine> update() {
        try {
            Repository repository = GitUtils.getRepository(this.mProject);
            BlameResult result = new BlameCommand(repository).setFilePath(this.mPath).setStartCommit(repository.resolve("HEAD")).call();
            List<BlameLine> blameList = readLines(this.mProject.getLocalPath() + "/" + this.mPath);
            int max = 0;
            for (BlameLine line : blameList) {
                max = Math.max(max, line.getLineText().length());
            }
            for (int i = 0; i < result.getResultContents().size(); i++) {
                if (result.getSourceLine(i) < blameList.size()) {
                    String commit = result.getSourceCommit(i).name().substring(0, 8);
                    String author = result.getSourceAuthor(i).getName();
                    blameList.get(i).setCommit(commit);
                    blameList.get(i).setAuthor(author);
                }
            }
            char[] charArray = new char[max];
            Arrays.fill(charArray, ' ');
            blameList.add(0, new BlameLine(new String(charArray), "", ""));
            return blameList;
        } catch (IOException e) {
            Log.e("Pocket Git", "", e);
            Toast.makeText(this, "Failed to read file", 0).show();
        } catch (Exception e2) {
            Log.e("Pocket Git", "", e2);
            Toast.makeText(this, "Failed to read blame information", 0).show();
        }
        return null;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public List<BlameLine> readLines(String filename) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
        List<BlameLine> lines = new ArrayList<>();
        while (true) {
            String line = bufferedReader.readLine();
            if (line != null) {
                lines.add(new BlameLine(line, "", ""));
            } else {
                bufferedReader.close();
                return lines;
            }
        }
    }
}
