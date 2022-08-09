package com.aor.pocketgit.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.aor.pocketgit.R;
import com.aor.pocketgit.adapters.TagAdapter;
import com.aor.pocketgit.utils.FontUtils;
import com.aor.pocketgit.utils.GitUtils;
import java.util.List;
import java.util.Set;
import org.eclipse.jgit.lib.Repository;

public class TagListDialog {
    private final Context mContext;
    private TagListListener mListener;

    public interface TagListListener {
        void checkout(String str);

        void deleteBranch(Set<String> set);
    }

    public TagListDialog(Context context) {
        this.mContext = context;
    }

    public TagListDialog showDialog(Repository repository) {
        View dialogList = LayoutInflater.from(this.mContext).inflate(R.layout.dialog_tag_list, (ViewGroup) null);
        try {
            List<GitUtils.Tag> tags = new GitUtils().getTags(repository);
            if (tags.size() == 0) {
                Toast.makeText(this.mContext, "No tags found", 0).show();
            } else {
                final TagAdapter adapter = new TagAdapter(this.mContext, tags);
                final ListView listTags = (ListView) dialogList.findViewById(R.id.list_tags);
                final MaterialDialog dialog = new MaterialDialog.Builder(this.mContext)
					.title((CharSequence) "Tag List")
					.iconRes(R.drawable.ic_action_tag_list)
					.customView(dialogList, false)
					.positiveText(R.string.button_close)
					.negativeText(R.string.button_checkout)
					.neutralText(R.string.button_delete)
					.onNegative(new MaterialDialog.SingleButtonCallback() {
                    	public void onClick(MaterialDialog dialog, DialogAction which) {
                        	TagListDialog.this.mListener.checkout(adapter.getSelectedTags().iterator().next());
                    	}
					})
					.onNeutral(new MaterialDialog.SingleButtonCallback() {
						public void onClick(MaterialDialog dialog, DialogAction which) {
							GitUtils.Tag tag = (GitUtils.Tag) listTags.getItemAtPosition(listTags.getCheckedItemPosition());
                        	TagListDialog.this.mListener.deleteBranch(adapter.getSelectedTags());
                    	}
                	}).show();
				dialog.getActionButton(DialogAction.NEGATIVE).setEnabled(false);
				dialog.getActionButton(DialogAction.NEUTRAL).setEnabled(false);
                listTags.setAdapter(adapter);
                adapter.setOnItemSelectionChangedListener(new TagAdapter.ItemSelectionChangedListener() {
                    public void itemSelectionChanged(Set<String> selected) {
                    	dialog.getActionButton(DialogAction.NEGATIVE).setEnabled(selected.size() == 1);
                        dialog.getActionButton(DialogAction.NEUTRAL).setEnabled(selected.size() >= 1);
                    }
                });
                FontUtils.setRobotoFont(this.mContext, dialog.getWindow().getDecorView());
            }
        } catch (Exception e) {
            Toast.makeText(this.mContext, "Failed to get tag list", 0).show();
        }
        return this;
    }

    public void setTagListListener(TagListListener listener) {
        this.mListener = listener;
    }
}
