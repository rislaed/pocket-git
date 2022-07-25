package com.aor.pocketgit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.aor.pocketgit.R;
import com.aor.pocketgit.data.BlameLine;
import com.aor.pocketgit.utils.FontUtils;
import java.util.List;

public class BlameLineAdapter extends ArrayAdapter<BlameLine> {
    public BlameLineAdapter(Context context, List<BlameLine> blameLines) {
        super(context, 0, blameLines);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_blame_line, parent, false);
        }
        if (position == 0) {
            convertView.setLayoutParams(new AbsListView.LayoutParams(-1, 1));
        } else {
            convertView.setLayoutParams(new AbsListView.LayoutParams(-1, -2));
        }
        BlameLine blame = (BlameLine) getItem(position);
        TextView lineText = (TextView) convertView.findViewById(R.id.text_line_text);
        lineText.setTypeface(FontUtils.getSourceCodeTypeface(getContext()));
        lineText.setText(blame.getLineText());
        TextView author = (TextView) convertView.findViewById(R.id.text_author);
        author.setTypeface(FontUtils.getSourceCodeTypeface(getContext()));
        author.setText(blame.getAuthor());
        TextView commit = (TextView) convertView.findViewById(R.id.text_commit);
        commit.setTypeface(FontUtils.getSourceCodeTypeface(getContext()));
        commit.setText(blame.getCommit());
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }
}
