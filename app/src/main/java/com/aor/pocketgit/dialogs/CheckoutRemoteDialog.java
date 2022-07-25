package com.aor.pocketgit.dialogs;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import com.afollestad.materialdialogs.MaterialDialog;
import com.aor.pocketgit.R;
import com.aor.pocketgit.tasks.CheckoutRemoteTask;
import com.aor.pocketgit.utils.FontUtils;
import org.eclipse.jgit.lib.Repository;

public class CheckoutRemoteDialog {
    private final Context mContext;
    private CheckoutRemoteTask.CheckoutRemoteListener mListener;
    private int mSortDirection;
    private int mSortType;

    public CheckoutRemoteDialog(Context context) {
        this.mContext = context;
    }

    public CheckoutRemoteDialog showDialog(final Repository repository, final String remote) {
        View dialogBranchName = LayoutInflater.from(this.mContext).inflate(R.layout.dialog_single_input, (ViewGroup) null);
        final EditText input = (EditText) dialogBranchName.findViewById(R.id.text_input);
        input.setText(remote.substring(remote.lastIndexOf(47) + 1));
        input.setInputType(145);
        ((TextView) dialogBranchName.findViewById(R.id.text_content)).setText("Enter the new branch name");
        FontUtils.setRobotoFont(this.mContext, new MaterialDialog.Builder(this.mContext).title((CharSequence) "Checkout remote").iconRes(R.drawable.ic_action_create_branch).customView(dialogBranchName, true).positiveText((int) R.string.button_ok).negativeText((int) R.string.button_cancel).callback(new MaterialDialog.ButtonCallback() {
            public void onPositive(MaterialDialog dialog) {
                String name = input.getText().toString().trim();
                new CheckoutRemoteTask().setListener(CheckoutRemoteDialog.this.mListener).execute(new Object[]{repository, name, remote});
            }
        }).show().getWindow().getDecorView());
        return this;
    }

    public void setCheckoutListener(CheckoutRemoteTask.CheckoutRemoteListener listener) {
        this.mListener = listener;
    }
}
