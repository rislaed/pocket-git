package com.aor.pocketgit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.aor.pocketgit.R;
import com.aor.pocketgit.utils.FontUtils;
import com.aor.pocketgit.utils.GitUtils;
import java.util.List;
import org.eclipse.jgit.revwalk.RevCommit;

public class StashAdapter extends ArrayAdapter<RevCommit> {
    public StashAdapter(Context context, List<RevCommit> revCommits) {
        super(context, 0, revCommits);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_revcommit, parent, false);
            FontUtils.setRobotoFont(getContext(), convertView);
        }
        RevCommit revCommit = (RevCommit) getItem(position);
        ((TextView) convertView.findViewById(R.id.text_message)).setText(revCommit.getShortMessage());
        ((TextView) convertView.findViewById(R.id.text_date)).setText(GitUtils.formatDate(revCommit.getCommitTime()));
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }
}
