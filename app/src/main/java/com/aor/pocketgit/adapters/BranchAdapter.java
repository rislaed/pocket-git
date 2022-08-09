package com.aor.pocketgit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.aor.pocketgit.R;
import com.aor.pocketgit.utils.FontUtils;
import java.util.List;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;

public class BranchAdapter extends ArrayAdapter<Ref> {
    private String mCurrentBranch;

    public BranchAdapter(Context context, List<Ref> branches, String currentBranch) {
        super(context, 0, branches);
        this.mCurrentBranch = currentBranch;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_branch, parent, false);
            FontUtils.setRobotoFont(getContext(), convertView);
        }
        Ref ref = getItem(position);
        String name = ref.getName();
        if (name.equals("HEAD")) {
            name = this.mCurrentBranch;
        }
        if (!name.startsWith(Constants.R_REFS)) {
            name = name.substring(0, 8);
        }
        if (name.startsWith(Constants.R_HEADS)) {
            name = name.substring(11);
        }
        if (name.startsWith(Constants.R_REMOTES)) {
            name = name.substring(13);
        }
        ((TextView) convertView.findViewById(R.id.branch_name)).setText(name);
        if (ref.getName().equals(this.mCurrentBranch) || ref.getName().equals("HEAD")) {
            convertView.findViewById(R.id.current_branch).setVisibility(0);
        } else {
            convertView.findViewById(R.id.current_branch).setVisibility(4);
        }
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void refill(List<Ref> branches, String currentBranch) {
        clear();
        addAll(branches);
        this.mCurrentBranch = currentBranch;
        notifyDataSetChanged();
    }
}
