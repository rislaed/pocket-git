package com.aor.pocketgit.dialogs;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import com.afollestad.materialdialogs.MaterialDialog;
import com.aor.pocketgit.R;
import com.aor.pocketgit.adapters.BranchArrayAdapter;
import com.aor.pocketgit.utils.FontUtils;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.MergeResult;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

public class MergeDialog {
    private final Context mContext;
    private MergeListener mListener;
    private int mSortDirection;
    private int mSortType;

    public interface MergeListener {
        void mergeFailed(Exception exc);

        void mergeResult(MergeResult mergeResult);
    }

    public MergeDialog(Context context) {
        this.mContext = context;
    }

    public MergeDialog showDialog(final Repository repository) {
        try {
            String currentBranch = repository.getFullBranch();
            ListBranchCommand branchList = new Git(repository).branchList();
            branchList.setListMode(ListBranchCommand.ListMode.ALL);
            List<Ref> branches = branchList.call();
            final List<Ref> selectableBranches = new ArrayList<>();
            for (Ref ref : branches) {
                if (!ref.getName().equals("HEAD") && !ref.getName().equals(currentBranch)) {
                    selectableBranches.add(ref);
                }
            }
			List<String> namesOfBranches = new ArrayList<>();
			for (Ref ref : selectableBranches) {
				String name = ref.getName();
				if (name.startsWith(Constants.R_HEADS)) {
					name = name.substring(11);
				}
				namesOfBranches.add(name);
			}
            // final ArrayAdapter<Ref> adapter = new BranchArrayAdapter(this.mContext, selectableBranches);
            final MaterialDialog dialog = new MaterialDialog.Builder(this.mContext)
				.title((CharSequence) "Select Branch to Merge")
				.iconRes(R.drawable.ic_action_merge)
				.items(namesOfBranches)
				.itemsCallback(new MaterialDialog.ListCallback() {
					@Override
					public void onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
						Ref ref = selectableBranches.get(which);
						try {
							MergeCommand merge = new Git(repository).merge();
							merge.include(ref);
							MergeDialog.this.mListener.mergeResult(merge.call());
						} catch (Exception e) {
							Log.e("Pocket Git", "", e);
							MergeDialog.this.mListener.mergeFailed(e);
						}
					}
				})
				.negativeText(R.string.button_cancel).show();
            FontUtils.setRobotoFont(this.mContext, dialog.getWindow().getDecorView());
        } catch (Exception e) {
            Log.e("Pocket Git", "", e);
        }
        return this;
    }

    public void setMergeListener(MergeListener listener) {
        this.mListener = listener;
    }
}
