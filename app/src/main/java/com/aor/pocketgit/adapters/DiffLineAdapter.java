package com.aor.pocketgit.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.aor.pocketgit.R;
import com.aor.pocketgit.data.DiffLine;
import com.aor.pocketgit.utils.FontUtils;
import java.util.List;

public class DiffLineAdapter extends ArrayAdapter<DiffLine> {
    public DiffLineAdapter(Context context, List<DiffLine> diffLines) {
        super(context, 0, diffLines);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_diff_line, parent, false);
        }
        if (position == 0) {
            convertView.setLayoutParams(new AbsListView.LayoutParams(-1, 1));
        } else {
            convertView.setLayoutParams(new AbsListView.LayoutParams(-1, -2));
        }
        DiffLine diff = getItem(position);
        TextView lineText = (TextView) convertView.findViewById(R.id.text_line_text);
        lineText.setTypeface(FontUtils.getSourceCodeTypeface(getContext()));
        if (diff.isHeader()) {
            lineText.setTextColor(Color.parseColor("#8c9eff"));
        } else if (diff.isAddition()) {
            lineText.setTextColor(Color.parseColor("#99CC00"));
        } else if (diff.isDeletion()) {
            lineText.setTextColor(Color.parseColor("#FF4444"));
        } else {
            lineText.setTextColor(-1);
        }
        lineText.setText(diff.getLineText());
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }
}
