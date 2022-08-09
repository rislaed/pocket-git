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

public class BranchArrayAdapter extends ArrayAdapter<Ref> {
    public BranchArrayAdapter(Context context, List<Ref> branches) {
        super(context, 0, branches);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_simple, parent, false);
            FontUtils.setRobotoFont(getContext(), convertView);
        }
        String name = getItem(position).getName();
        if (name.startsWith(Constants.R_HEADS)) {
            name = name.substring(11);
        }
        ((TextView) convertView.findViewById(16908308)).setText(name);
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }
}
