package hu.sherad.hos.ui.recyclerview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.ColorInt;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * A decoration which draws a horizontal divider between {@link RecyclerView.ViewHolder}s of a given
 * type; with a left inset.
 */
public class InsetDividerDecoration extends RecyclerView.ItemDecoration {

    private final Paint paint;
    private final int inset;
    private final int height;
    private Class<?> dividedClass = null;

    public InsetDividerDecoration(int dividerHeight, int leftInset, @ColorInt int dividerColor) {
        inset = leftInset;
        height = dividerHeight;
        paint = new Paint();
        paint.setColor(dividerColor);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(dividerHeight);
    }

    public InsetDividerDecoration(Class<?> dividedViewHolderClass, int dividerHeight, int leftInset, @ColorInt int dividerColor) {
        this(dividerHeight, leftInset, dividerColor);
        dividedClass = dividedViewHolderClass;
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int childCount = parent.getChildCount();
        if (childCount < 2) {
            return;
        }

        RecyclerView.LayoutManager lm = parent.getLayoutManager();
        float[] lines = new float[childCount * 4];
        boolean hasDividers = false;

        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            if (dividedClass == null || parent.getChildViewHolder(child).getClass() == dividedClass) {
                if (child.isActivated() || (i + 1 < childCount && parent.getChildAt(i + 1).isActivated())) {
                    continue;
                }
                lines[i * 4] = inset + lm.getDecoratedLeft(child);
                lines[(i * 4) + 2] = lm.getDecoratedRight(child);
                int y = lm.getDecoratedBottom(child) + (int) child.getTranslationY() - height;
                lines[(i * 4) + 1] = y;
                lines[(i * 4) + 3] = y;
                hasDividers = true;
            }
        }
        if (hasDividers) {
            canvas.drawLines(lines, paint);
        }
    }
}
