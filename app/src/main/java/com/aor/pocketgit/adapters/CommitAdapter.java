package com.aor.pocketgit.adapters;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.preference.PreferenceManager;
import com.aor.pocketgit.R;
import com.aor.pocketgit.utils.FontUtils;
import com.aor.pocketgit.utils.GitUtils;
import com.aor.pocketgit.utils.MD5Util;
import com.aor.pocketgit.widgets.FlowLayout;
import com.aor.pocketgit.widgets.PlotLaneView;
import com.bumptech.glide.Glide;
import java.util.ArrayList;
import java.util.Iterator;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotCommitList;
import org.eclipse.jgit.revplot.PlotLane;

public class CommitAdapter extends BaseAdapter {
    private Context mContext;
    private final boolean mGravatar;
    private PlotCommitList<PlotLane> mPlotCommitList;
    private float mWidth;

    public CommitAdapter(Context context, PlotCommitList<PlotLane> plotCommitList) {
        this.mContext = context;
        this.mPlotCommitList = plotCommitList;
        int maxLanes = 0;
        Iterator it = plotCommitList.iterator();
        while (it.hasNext()) {
            maxLanes = Math.max(maxLanes, ((PlotCommit) it.next()).getLane().getPosition());
        }
        this.mWidth = TypedValue.applyDimension(1, (float) (((maxLanes + 1) * PlotLaneView.RADIUS * 2) + 4), this.mContext.getResources().getDisplayMetrics());
        this.mGravatar = PreferenceManager.getDefaultSharedPreferences(context).getBoolean("pref_gravatar", true);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(this.mContext).inflate(R.layout.item_commit, parent, false);
            FontUtils.setRobotoFont(this.mContext, convertView);
        }
        PlotCommit<PlotLane> commit = (PlotCommit<PlotLane>) getItem(position);
        PlotLaneView plotLane = (PlotLaneView) convertView.findViewById(R.id.plot_lane);
        plotLane.getLayoutParams().width = (int) this.mWidth;
        plotLane.setLane(commit.getLane().getPosition());
        plotLane.clearLines();
        ArrayList<PlotLane> passing = new ArrayList<>();
        this.mPlotCommitList.findPassingThrough(commit, passing);
        plotLane.setPassing(passing);
        for (int c = 0; c < commit.getChildCount(); c++) {
            plotLane.addChildLane(commit.getChild(c).getLane().getPosition());
        }
        for (int p = 0; p < commit.getParentCount(); p++) {
            plotLane.addParentLane(((PlotCommit) commit.getParent(p)).getLane().getPosition());
        }
        ((TextView) convertView.findViewById(R.id.text_sha)).setText("SHA: " + commit.getName().substring(0, 8));
        ((TextView) convertView.findViewById(R.id.text_message)).setText(commit.getShortMessage());
        ((TextView) convertView.findViewById(R.id.text_author_and_date)).setText(commit.getAuthorIdent().getName() + " • " + GitUtils.formatDateShort(commit.getCommitTime()));
        FlowLayout layoutRef = (FlowLayout) convertView.findViewById(R.id.layout_ref);
        layoutRef.removeAllViews();
        for (int r = 0; r < commit.getRefCount(); r++) {
            String name = commit.getRef(r).getName();
            if (!name.equals("HEAD")) {
                TextView refView = (TextView) LayoutInflater.from(this.mContext).inflate(R.layout.item_ref, parent, false);
                if (name.startsWith(Constants.R_REFS)) {
                    name = name.substring(5);
                    refView.setBackgroundColor(Color.parseColor("#99CC00"));
                }
                if (name.startsWith("heads/")) {
                    name = name.substring(6);
                    refView.setBackgroundColor(Color.parseColor("#33B5E5"));
                }
                if (name.startsWith("remotes/")) {
                    name = name.substring(8);
                    refView.setBackgroundColor(Color.parseColor("#FFBB33"));
                }
                refView.setText(name);
                layoutRef.addView(refView);
            }
        }
        plotLane.invalidate();
        if (this.mGravatar) {
            Glide.with(this.mContext).load("https://www.gravatar.com/avatar/" + MD5Util.md5Hex(commit.getAuthorIdent().getEmailAddress()) + "?d=identicon").placeholder(R.drawable.img_default_photo).into((ImageView) convertView.findViewById(R.id.image_photo));
        } else {
            convertView.findViewById(R.id.image_photo).setVisibility(8);
        }
        return convertView;
		
    }

    public boolean hasStableIds() {
        return true;
    }

    public int getCount() {
        return this.mPlotCommitList.size();
    }

    public Object getItem(int position) {
        return this.mPlotCommitList.get(position);
    }

    public long getItemId(int position) {
        return (long) position;
    }
}
