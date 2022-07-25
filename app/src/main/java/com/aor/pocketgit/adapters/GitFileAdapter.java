package com.aor.pocketgit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.aor.pocketgit.R;
import com.aor.pocketgit.data.GitFile;
import com.aor.pocketgit.utils.FontUtils;
import java.util.List;

public class GitFileAdapter extends ArrayAdapter<GitFile> {
    public GitFileAdapter(Context context, List<GitFile> folders) {
        super(context, 0, folders);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_git_file, parent, false);
            FontUtils.setRobotoFont(getContext(), convertView);
        }
        GitFile file = (GitFile) getItem(position);
        ((TextView) convertView.findViewById(R.id.file_name)).setText(file.getName());
        if (file.isDirectory()) {
            ((ImageView) convertView.findViewById(R.id.file_icon)).setImageResource(R.drawable.ic_folder);
        } else {
            ((ImageView) convertView.findViewById(R.id.file_icon)).setImageResource(R.drawable.ic_file);
        }
        ImageView state = (ImageView) convertView.findViewById(R.id.file_state);
        state.setImageResource(R.drawable.ic_commited);
        if (file.hasState(GitFile.UNTRACKED)) {
            state.setImageResource(R.drawable.ic_untracked);
        }
        if (file.hasState(GitFile.UNCOMMITED)) {
            state.setImageResource(R.drawable.ic_changed);
        }
        if (file.hasState(GitFile.IGNORED)) {
            state.setImageResource(R.drawable.ic_ignored);
        }
        if (file.hasState(GitFile.ADDED)) {
            state.setImageResource(R.drawable.ic_added);
        }
        if (file.hasState(GitFile.CHANGED)) {
            state.setImageResource(R.drawable.ic_changed);
        }
        if (file.hasState(GitFile.MODIFIED)) {
            state.setImageResource(R.drawable.ic_modified);
        }
        if (file.hasState(GitFile.CHANGED) && file.hasState(GitFile.MODIFIED)) {
            state.setImageResource(R.drawable.ic_changed_modified);
        }
        if (file.hasState(GitFile.REMOVED)) {
            state.setImageResource(R.drawable.ic_removed);
        }
        if (file.hasState(GitFile.CONFLICTING)) {
            state.setImageResource(R.drawable.ic_conflicting);
        }
        if (file.hasState(GitFile.MISSING)) {
            state.setImageResource(R.drawable.ic_missing);
        }
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void refill(List<GitFile> files) {
        clear();
        addAll(files);
        notifyDataSetChanged();
    }
}
