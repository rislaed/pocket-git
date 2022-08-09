package com.aor.pocketgit.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.aor.pocketgit.R;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@SuppressLint({"DrawAllocation"})
public class FlowLayout extends ViewGroup {
    public static final int DEFAULT_HORIZONTAL_SPACING = 5;
    public static final int DEFAULT_VERTICAL_SPACING = 5;
    private List<RowMeasurement> currentRows = Collections.emptyList();
    private final int horizontalSpacing;
    private final int verticalSpacing;

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.FlowLayout);
        this.horizontalSpacing = styledAttributes.getDimensionPixelSize(0, 5);
        this.verticalSpacing = styledAttributes.getDimensionPixelSize(1, 5);
        styledAttributes.recycle();
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int horizontalPadding;
        int verticalPadding;
        int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = View.MeasureSpec.getMode(heightMeasureSpec);
        int maxInternalWidth = View.MeasureSpec.getSize(widthMeasureSpec) - getHorizontalPadding();
        int maxInternalHeight = View.MeasureSpec.getSize(heightMeasureSpec) - getVerticalPadding();
        ArrayList<RowMeasurement> arrayList = new ArrayList<>();
        RowMeasurement currentRow = new RowMeasurement(maxInternalWidth, widthMode);
        arrayList.add(currentRow);
        for (View child : getLayoutChildren()) {
            ViewGroup.LayoutParams childLayoutParams = child.getLayoutParams();
            child.measure(createChildMeasureSpec(childLayoutParams.width, maxInternalWidth, widthMode), createChildMeasureSpec(childLayoutParams.height, maxInternalHeight, heightMode));
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            if (currentRow.wouldExceedMax(childWidth)) {
                currentRow = new RowMeasurement(maxInternalWidth, widthMode);
                arrayList.add(currentRow);
            }
            currentRow.addChildDimensions(childWidth, childHeight);
        }
        int longestRowWidth = 0;
        int totalRowHeight = 0;
        for (int index = 0; index < arrayList.size(); index++) {
            RowMeasurement row = arrayList.get(index);
            totalRowHeight += row.getHeight();
            if (index < arrayList.size() - 1) {
                totalRowHeight += this.verticalSpacing;
            }
            longestRowWidth = Math.max(longestRowWidth, row.getWidth());
        }
        if (widthMode == 1073741824) {
            horizontalPadding = View.MeasureSpec.getSize(widthMeasureSpec);
        } else {
            horizontalPadding = getHorizontalPadding() + longestRowWidth;
        }
        if (heightMode == 1073741824) {
            verticalPadding = View.MeasureSpec.getSize(heightMeasureSpec);
        } else {
            verticalPadding = getVerticalPadding() + totalRowHeight;
        }
        setMeasuredDimension(horizontalPadding, verticalPadding);
        this.currentRows = Collections.unmodifiableList(arrayList);
    }

    private int createChildMeasureSpec(int childLayoutParam, int max, int parentMode) {
        if (childLayoutParam == -1) {
            return View.MeasureSpec.makeMeasureSpec(max, 1073741824);
        }
        if (childLayoutParam != -2) {
            return View.MeasureSpec.makeMeasureSpec(childLayoutParam, 1073741824);
        }
        return View.MeasureSpec.makeMeasureSpec(max, parentMode == 0 ? 0 : Integer.MIN_VALUE);
    }

    protected void onLayout(boolean changed, int leftPosition, int topPosition, int rightPosition, int bottomPosition) {
        int widthOffset = getMeasuredWidth() - getPaddingRight();
        int x = getPaddingLeft();
        int y = getPaddingTop();
        Iterator<RowMeasurement> rowIterator = this.currentRows.iterator();
        RowMeasurement currentRow = rowIterator.next();
        for (View child : getLayoutChildren()) {
            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();
            if (x + childWidth > widthOffset) {
                x = getPaddingLeft();
                y += currentRow.height + this.verticalSpacing;
                if (rowIterator.hasNext()) {
                    currentRow = rowIterator.next();
                }
            }
            child.layout(x, y, x + childWidth, y + childHeight);
            x += this.horizontalSpacing + childWidth;
        }
    }

    private List<View> getLayoutChildren() {
        List<View> children = new ArrayList<>();
        for (int index = 0; index < getChildCount(); index++) {
            View child = getChildAt(index);
            if (child.getVisibility() != 8) {
                children.add(child);
            }
        }
        return children;
    }

    protected int getVerticalPadding() {
        return getPaddingTop() + getPaddingBottom();
    }

    protected int getHorizontalPadding() {
        return getPaddingLeft() + getPaddingRight();
    }

    private final class RowMeasurement {
        private int height;
        private final int maxWidth;
        private int width;
        private final int widthMode;

        public RowMeasurement(int maxWidth2, int widthMode2) {
            this.maxWidth = maxWidth2;
            this.widthMode = widthMode2;
        }

        public int getHeight() {
            return this.height;
        }

        public int getWidth() {
            return this.width;
        }

        public boolean wouldExceedMax(int childWidth) {
            return this.widthMode != 0 && getNewWidth(childWidth) > this.maxWidth;
        }

        public void addChildDimensions(int childWidth, int childHeight) {
            this.width = getNewWidth(childWidth);
            this.height = Math.max(this.height, childHeight);
        }

        private int getNewWidth(int childWidth) {
            return this.width == 0 ? childWidth : childWidth + this.width + FlowLayout.this.horizontalSpacing;
        }
    }
}
