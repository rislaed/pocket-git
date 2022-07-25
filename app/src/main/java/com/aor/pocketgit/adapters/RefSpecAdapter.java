package com.aor.pocketgit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.aor.pocketgit.R;
import com.aor.pocketgit.data.TypedRefSpec;
import com.aor.pocketgit.utils.FontUtils;
import java.util.List;

public class RefSpecAdapter extends ArrayAdapter<TypedRefSpec> {
    public RefSpecAdapter(Context context, List<TypedRefSpec> refSpecs) {
        super(context, 0, refSpecs);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_refspec, parent, false);
            FontUtils.setRobotoFont(getContext(), convertView);
        }
        TypedRefSpec refspec = (TypedRefSpec) getItem(position);
        ((TextView) convertView.findViewById(R.id.text_refspec)).setText(refspec.getRefSpec().toString());
        ((TextView) convertView.findViewById(R.id.text_type)).setText(refspec.getType().toString());
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }
}
