package hu.sherad.hos.utils.keyboard;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.MovementMethod;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * A movement method that only highlights any touched
 * {@link TouchableUrlSpan}s
 * <p>
 * See: http://stackoverflow.com/a/20905824
 */
public class LinkTouchMovementMethod extends LinkMovementMethod {

    private static LinkTouchMovementMethod instance;
    private URLSpan pressedSpan;

    public static MovementMethod getInstance() {
        if (instance == null) {
            instance = new LinkTouchMovementMethod();
        }
        return instance;
    }

    @Override
    public boolean onTouchEvent(TextView textView, Spannable spannable, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            pressedSpan = getPressedSpan(textView, spannable, event);
            if (pressedSpan != null) {
                // pressedSpan.setPressed(true);
                Selection.setSelection(spannable, spannable.getSpanStart(pressedSpan), spannable.getSpanEnd(pressedSpan));
            }
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            URLSpan touchedSpan = getPressedSpan(textView, spannable, event);
            if (pressedSpan != null && touchedSpan != pressedSpan) {
                // pressedSpan.setPressed(false);
                pressedSpan = null;
                Selection.removeSelection(spannable);
            }
        } else {
            if (pressedSpan != null) {
                // pressedSpan.setPressed(false);
                super.onTouchEvent(textView, spannable, event);
            }
            pressedSpan = null;
            Selection.removeSelection(spannable);
        }
        return true;
    }

    private URLSpan getPressedSpan(TextView textView, Spannable spannable, MotionEvent event) {

        int x = (int) event.getX();
        int y = (int) event.getY();

        x -= textView.getTotalPaddingLeft();
        y -= textView.getTotalPaddingTop();

        x += textView.getScrollX();
        y += textView.getScrollY();

        Layout layout = textView.getLayout();
        int line = layout.getLineForVertical(y);
        int off = layout.getOffsetForHorizontal(line, x);

        URLSpan[] link = spannable.getSpans(off, off, URLSpan.class);
        URLSpan touchedSpan = null;
        if (link.length > 0) {
            touchedSpan = link[0];
        }
        return touchedSpan;
    }

}