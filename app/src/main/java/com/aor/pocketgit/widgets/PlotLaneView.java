package com.aor.pocketgit.widgets;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.eclipse.jgit.revplot.PlotLane;

public class PlotLaneView extends ImageView {
    private static final int[] COLORS = {
    	Color.parseColor("#0099CC"),
    	Color.parseColor("#9933CC"),
    	Color.parseColor("#669900"),
    	Color.parseColor("#FF8800"),
    	Color.parseColor("#CC0000")
    };
    private static final Paint PAINT = new Paint();
    public static int RADIUS = 3;
    private List<Integer> mChildren = new ArrayList<>();
    private int mLane;
    private List<Integer> mParent = new ArrayList<>();
    private Set<Integer> mPassing;

    static {
        PAINT.setStyle(Paint.Style.FILL);
    }

    public PlotLaneView(Context context) {
        super(context);
    }

    public PlotLaneView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlotLaneView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onDraw(Canvas canvas) {
        Resources r = getResources();
        float radius = TypedValue.applyDimension(1, (float) RADIUS, r.getDisplayMetrics());
        PAINT.setStrokeWidth(radius);
        float cx = TypedValue.applyDimension(1, (float) ((this.mLane + 1) * RADIUS * 2), r.getDisplayMetrics());
        float cy = (float) (getHeight() / 2);
        for (Integer childLane : this.mChildren) {
            if (this.mPassing.contains(childLane)) {
                childLane = Integer.valueOf(this.mLane);
            }
            PAINT.setColor(COLORS[childLane.intValue() % 5]);
            canvas.drawLine(cx, cy, TypedValue.applyDimension(1, (float) ((childLane.intValue() + 1) * RADIUS * 2), r.getDisplayMetrics()), 0.0f, PAINT);
        }
        for (Integer parentLane : this.mParent) {
            if (this.mPassing.contains(parentLane)) {
                parentLane = Integer.valueOf(this.mLane);
            }
            PAINT.setColor(COLORS[parentLane.intValue() % 5]);
            canvas.drawLine(cx, cy, TypedValue.applyDimension(1, (float) ((parentLane.intValue() + 1) * RADIUS * 2), r.getDisplayMetrics()), (float) getHeight(), PAINT);
        }
        if (this.mPassing != null) {
            for (Integer childLane2 : this.mPassing) {
                PAINT.setColor(COLORS[childLane2.intValue() % 5]);
                float chx = TypedValue.applyDimension(1, (float) ((childLane2.intValue() + 1) * RADIUS * 2), r.getDisplayMetrics());
                canvas.drawLine(chx, 0.0f, chx, (float) getHeight(), PAINT);
            }
        }
        PAINT.setColor(COLORS[this.mLane % 5]);
        canvas.drawCircle(cx, cy, radius, PAINT);
    }

    public void setLane(int lane) {
        this.mLane = lane;
    }

    public void clearLines() {
        this.mChildren.clear();
        this.mParent.clear();
    }

    public void addChildLane(int position) {
        this.mChildren.add(0, Integer.valueOf(position));
    }

    public void setPassing(ArrayList<PlotLane> passing) {
        if (this.mPassing == null) {
            this.mPassing = new HashSet<>();
        }
        this.mPassing.clear();
        Iterator<PlotLane> it = passing.iterator();
        while (it.hasNext()) {
            this.mPassing.add(Integer.valueOf(it.next().getPosition()));
        }
    }

    public void addParentLane(int position) {
        this.mParent.add(0, Integer.valueOf(position));
    }
}
