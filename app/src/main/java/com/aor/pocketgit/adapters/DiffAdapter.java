package com.aor.pocketgit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.aor.pocketgit.R;
import com.aor.pocketgit.utils.FontUtils;
import java.util.List;
import org.eclipse.jgit.diff.DiffEntry;

public class DiffAdapter extends ArrayAdapter<DiffEntry> {
    public DiffAdapter(Context context, List<DiffEntry> diff) {
        super(context, 0, diff);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_diff, parent, false);
            FontUtils.setRobotoFont(getContext(), convertView);
        }
        DiffEntry diff = (DiffEntry) getItem(position);
        ImageView iconType = (ImageView) convertView.findViewById(R.id.icon_type);
        if (diff.getChangeType() == DiffEntry.ChangeType.ADD) {
            iconType.setImageResource(R.drawable.ic_added);
        }
        if (diff.getChangeType() == DiffEntry.ChangeType.COPY) {
            iconType.setImageResource(R.drawable.ic_added);
        }
        if (diff.getChangeType() == DiffEntry.ChangeType.DELETE) {
            iconType.setImageResource(R.drawable.ic_removed);
        }
        if (diff.getChangeType() == DiffEntry.ChangeType.MODIFY) {
            iconType.setImageResource(R.drawable.ic_modified);
        }
        if (diff.getChangeType() == DiffEntry.ChangeType.RENAME) {
            iconType.setImageResource(R.drawable.ic_added);
        }
        String text = diff.getNewPath();
        if (diff.getChangeType() == DiffEntry.ChangeType.DELETE) {
            text = diff.getOldPath();
        }
        ((TextView) convertView.findViewById(R.id.text_path)).setText(text);
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }
}
