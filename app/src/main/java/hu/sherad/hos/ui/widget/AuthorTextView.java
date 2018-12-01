package hu.sherad.hos.ui.widget;


import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;

import hu.sherad.hos.R;

public class AuthorTextView extends RichTextView {

    private boolean isTopicModerator = false;

    private ColorStateList colorStateListAccent;

    public AuthorTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        colorStateListAccent = ContextCompat.getColorStateList(getContext(), R.color.accent);
    }

    public boolean isTopicModerator() {
        return isTopicModerator;
    }

    public void setTopicModerator(boolean isTopicModerator) {
        this.isTopicModerator = isTopicModerator;
        if (isTopicModerator) {
            setTextColor(colorStateListAccent);
        } else {
            setOffComment(isOffComment());
        }
    }
}
