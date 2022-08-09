package com.aor.pocketgit.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatCheckBox;
import com.aor.pocketgit.R;
import com.aor.pocketgit.utils.FontUtils;
import com.aor.pocketgit.utils.GitUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jgit.lib.Constants;

public class TagAdapter extends ArrayAdapter<GitUtils.Tag> {
    private ItemSelectionChangedListener mListener;
    private Set<String> mSelectedTags = new HashSet<>();

    public interface ItemSelectionChangedListener {
        void itemSelectionChanged(Set<String> set);
    }

    public TagAdapter(Context context, List<GitUtils.Tag> tags) {
        super(context, 0, tags);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_revtag, parent, false);
            FontUtils.setRobotoFont(getContext(), convertView);
        }
        final GitUtils.Tag tag = getItem(position);
        ((TextView) convertView.findViewById(R.id.text_name)).setText(tag.getName());
        TextView tagMessage = (TextView) convertView.findViewById(R.id.text_message);
        tagMessage.setText(tag.getMessage());
        if (tag.getMessage() == null || tag.getMessage().trim().equals("")) {
            tagMessage.setVisibility(8);
        } else {
            tagMessage.setVisibility(0);
        }
        final AppCompatCheckBox checkTag = (AppCompatCheckBox) convertView.findViewById(R.id.check_tag);
        checkTag.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    TagAdapter.this.mSelectedTags.add(Constants.R_TAGS + tag.getName());
                } else {
                    TagAdapter.this.mSelectedTags.remove(Constants.R_TAGS + tag.getName());
                }
                if (TagAdapter.this.mListener != null) {
                    TagAdapter.this.mListener.itemSelectionChanged(TagAdapter.this.mSelectedTags);
                }
            }
        });
        checkTag.setChecked(this.mSelectedTags.contains(Constants.R_TAGS + tag.getName()));
        convertView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                checkTag.toggle();
            }
        });
        return convertView;
    }

    public boolean hasStableIds() {
        return true;
    }

    public Set<String> getSelectedTags() {
        return this.mSelectedTags;
    }

    public void setOnItemSelectionChangedListener(ItemSelectionChangedListener listener) {
        this.mListener = listener;
    }
}
