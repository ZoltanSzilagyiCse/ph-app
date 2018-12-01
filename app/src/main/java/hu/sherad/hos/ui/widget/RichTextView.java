package hu.sherad.hos.ui.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import hu.sherad.hos.R;
import hu.sherad.hos.utils.ColorUtils;

public class RichTextView extends AppCompatTextView {

    private boolean isOffComment;

    private ColorStateList colorStateListDefault;
    private ColorStateList colorStateListOff;

    public RichTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        colorStateListDefault = getTextColors();
        colorStateListOff = ContextCompat.getColorStateList(getContext(), ColorUtils.isLightTheme() ? R.color.disabled_text_light : R.color.disabled_text_dark);
    }

    public boolean isOffComment() {
        return isOffComment;
    }

    public void setOffComment(boolean isOffComment) {
        this.isOffComment = isOffComment;
        if (isOffComment) {
            setTextColor(colorStateListOff);
        } else {
            setTextColor(colorStateListDefault);
        }
    }
}