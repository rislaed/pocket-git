package com.aor.pocketgit.dialogs;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.preference.PreferenceManager;
import com.afollestad.materialdialogs.MaterialDialog;
import com.aor.pocketgit.R;
import com.aor.pocketgit.utils.FileComparator;
import com.aor.pocketgit.utils.FontUtils;

public class SortFilesDialog {
    private final Context mContext;
    private SortListener mListener;
    private int mSortDirection;
    private int mSortType;

    public interface SortListener {
        void sortChanged();
    }

    public SortFilesDialog(Context context) {
        this.mContext = context;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        this.mSortType = prefs.getInt("pref_sort_type", FileComparator.SORT_NAME);
        this.mSortDirection = prefs.getInt("pref_sort_dir", FileComparator.SORT_DOWN);
    }

    public SortFilesDialog showDialog() {
        final MaterialDialog dialog = new MaterialDialog.Builder(this.mContext).title((CharSequence) "Sort Files").iconRes(R.drawable.ic_action_sort_files).customView(LayoutInflater.from(this.mContext).inflate(R.layout.dialog_sort_files, (ViewGroup) null), false).positiveText(R.string.button_close).show();
        dialog.findViewById(R.id.action_sort_name).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (SortFilesDialog.this.mSortType == FileComparator.SORT_NAME) {
                    SortFilesDialog.this.setSort(dialog, FileComparator.SORT_NAME, 1 - SortFilesDialog.this.mSortDirection);
                } else {
                    SortFilesDialog.this.setSort(dialog, FileComparator.SORT_NAME, FileComparator.SORT_DOWN);
                }
            }
        });
        dialog.findViewById(R.id.action_sort_date).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (SortFilesDialog.this.mSortType == FileComparator.SORT_DATE) {
                    SortFilesDialog.this.setSort(dialog, FileComparator.SORT_DATE, 1 - SortFilesDialog.this.mSortDirection);
                } else {
                    SortFilesDialog.this.setSort(dialog, FileComparator.SORT_DATE, FileComparator.SORT_DOWN);
                }
            }
        });
        dialog.findViewById(R.id.action_sort_size).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (SortFilesDialog.this.mSortType == FileComparator.SORT_SIZE) {
                    SortFilesDialog.this.setSort(dialog, FileComparator.SORT_SIZE, 1 - SortFilesDialog.this.mSortDirection);
                } else {
                    SortFilesDialog.this.setSort(dialog, FileComparator.SORT_SIZE, FileComparator.SORT_DOWN);
                }
            }
        });
        dialog.findViewById(R.id.action_sort_type).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (SortFilesDialog.this.mSortType == FileComparator.SORT_TYPE) {
                    SortFilesDialog.this.setSort(dialog, FileComparator.SORT_TYPE, 1 - SortFilesDialog.this.mSortDirection);
                } else {
                    SortFilesDialog.this.setSort(dialog, FileComparator.SORT_TYPE, FileComparator.SORT_DOWN);
                }
            }
        });
        FontUtils.setRobotoFont(this.mContext, dialog.getWindow().getDecorView());
        updateDialog(dialog);
        return this;
    }

    private void setSort(MaterialDialog dialog, int type, int direction) {
        this.mSortType = type;
        this.mSortDirection = direction;
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this.mContext);
        prefs.edit().putInt("pref_sort_type", this.mSortType).apply();
        prefs.edit().putInt("pref_sort_dir", this.mSortDirection).apply();
        updateDialog(dialog);
        if (this.mListener != null) {
            this.mListener.sortChanged();
        }
    }

    private void updateDialog(MaterialDialog dialog) {
        int i = R.drawable.ic_action_sort_up;
        if (this.mSortType != FileComparator.SORT_NAME) {
            dialog.findViewById(R.id.image_sort_name).setVisibility(4);
        } else {
            dialog.findViewById(R.id.image_sort_name).setVisibility(0);
            ((ImageView) dialog.findViewById(R.id.image_sort_name)).setImageResource(this.mSortDirection == FileComparator.SORT_UP ? R.drawable.ic_action_sort_up : R.drawable.ic_action_sort_down);
        }
        if (this.mSortType != FileComparator.SORT_DATE) {
            dialog.findViewById(R.id.image_sort_date).setVisibility(4);
        } else {
            dialog.findViewById(R.id.image_sort_date).setVisibility(0);
            ((ImageView) dialog.findViewById(R.id.image_sort_date)).setImageResource(this.mSortDirection == FileComparator.SORT_UP ? R.drawable.ic_action_sort_up : R.drawable.ic_action_sort_down);
        }
        if (this.mSortType != FileComparator.SORT_SIZE) {
            dialog.findViewById(R.id.image_sort_size).setVisibility(4);
        } else {
            dialog.findViewById(R.id.image_sort_size).setVisibility(0);
            ((ImageView) dialog.findViewById(R.id.image_sort_size)).setImageResource(this.mSortDirection == FileComparator.SORT_UP ? R.drawable.ic_action_sort_up : R.drawable.ic_action_sort_down);
        }
        if (this.mSortType != FileComparator.SORT_TYPE) {
            dialog.findViewById(R.id.image_sort_type).setVisibility(4);
            return;
        }
        dialog.findViewById(R.id.image_sort_type).setVisibility(0);
        ImageView imageView = (ImageView) dialog.findViewById(R.id.image_sort_type);
        if (this.mSortDirection != FileComparator.SORT_UP) {
            i = R.drawable.ic_action_sort_down;
        }
        imageView.setImageResource(i);
    }

    public void setSortListener(SortListener listener) {
        this.mListener = listener;
    }
}
