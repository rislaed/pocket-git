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
import org.eclipse.jgit.transport.RemoteConfig;

public class RemoteAdapter extends ArrayAdapter<RemoteConfig> {
    public RemoteAdapter(Context context, List<RemoteConfig> remotes) {
        super(context, 0, remotes);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_remote, parent, false);
            FontUtils.setRobotoFont(getContext(), convertView);
        }
        RemoteConfig remote = (RemoteConfig) getItem(position);
        ((TextView) convertView.findViewById(R.id.remote_name)).setText(remote.getName());
        TextView remoteUrl = (TextView) convertView.findViewById(R.id.remote_url);
        if (remote.getURIs().size() == 0) {
            remoteUrl.setText("");
        } else {
            remoteUrl.setText(remote.getURIs().get(0).toASCIIString());
        }
        return convertView;
    }

    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(17367049, parent, false);
            FontUtils.setRobotoFont(getContext(), convertView);
        }
        ((TextView) convertView.findViewById(16908308)).setText(((RemoteConfig) getItem(position)).getName());
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }
}
