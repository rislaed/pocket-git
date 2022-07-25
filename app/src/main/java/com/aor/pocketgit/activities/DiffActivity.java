package com.aor.pocketgit.activities;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ListView;
import androidx.appcompat.widget.Toolbar;
import com.aor.pocketgit.R;
import com.aor.pocketgit.adapters.DiffLineAdapter;
import com.aor.pocketgit.data.DiffLine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.eclipse.jgit.lib.ConfigConstants;

public class DiffActivity extends UpdatableActivity {
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diff);
        setSupportActionBar((Toolbar) findViewById(R.id.action_bar));
        String diff = getIntent().getStringExtra(ConfigConstants.CONFIG_DIFF_SECTION);
        ListView listView = (ListView) findViewById(R.id.list_diff);
        List<DiffLine> lineList = new ArrayList<>();
        boolean started = false;
        int max = 0;
        for (String text : diff.split("\n")) {
            max = Math.max(max, text.length());
            DiffLine line = new DiffLine(text);
            if (!started && line.isHeader()) {
                started = true;
            }
            if (started) {
                lineList.add(line);
            }
        }
        char[] charArray = new char[max];
        Arrays.fill(charArray, ' ');
        lineList.add(0, new DiffLine(new String(charArray)));
        listView.setAdapter(new DiffLineAdapter(this, lineList));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == 16908332) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
