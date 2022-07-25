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
import java.io.File;
import java.util.List;

public class FolderAdapter extends ArrayAdapter<File> {
    public FolderAdapter(Context context, List<File> folders) {
        super(context, 0, folders);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_folder, parent, false);
            FontUtils.setRobotoFont(getContext(), convertView);
        }
        File file = (File) getItem(position);
        ((TextView) convertView.findViewById(R.id.file_name)).setText(file.getName());
        if (file.isDirectory()) {
            ((ImageView) convertView.findViewById(R.id.file_icon)).setImageResource(R.drawable.ic_folder);
        } else {
            ((ImageView) convertView.findViewById(R.id.file_icon)).setImageResource(R.drawable.ic_file);
        }
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }
}
