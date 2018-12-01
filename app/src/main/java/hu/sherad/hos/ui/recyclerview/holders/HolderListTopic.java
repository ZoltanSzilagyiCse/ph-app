package hu.sherad.hos.ui.recyclerview.holders;

import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import hu.sherad.hos.R;
import hu.sherad.hos.ui.widget.RichTextView;

public class HolderListTopic extends RecyclerView.ViewHolder {

    private RichTextView textViewTitle;
    private RichTextView textViewBadge;
    private RichTextView buttonTitle;
    private AppCompatImageButton imageButtonNotification;
    private AppCompatImageButton imageButtonDelete;
    private AppCompatImageButton imageButtonCopy;

    public HolderListTopic(View itemView) {
        super(itemView);
        textViewTitle = itemView.findViewById(R.id.tv_util_list_item_topic_title);
        textViewBadge = itemView.findViewById(R.id.tv_list_item_topic_badge);
        buttonTitle = itemView.findViewById(R.id.btn_util_list_item_topic);
        imageButtonNotification = itemView.findViewById(R.id.image_view_list_item_topic_notification);
        imageButtonDelete = itemView.findViewById(R.id.image_view_list_item_topic_delete);
        imageButtonCopy = itemView.findViewById(R.id.image_view_list_item_topic_copy);
    }

    public AppCompatImageButton getImageButtonNotification() {
        return imageButtonNotification;
    }

    public AppCompatImageButton getImageButtonCopy() {
        return imageButtonCopy;
    }

    public AppCompatImageButton getImageButtonDelete() {
        return imageButtonDelete;
    }

    public RichTextView getTextViewTitle() {
        return textViewTitle;
    }

    public RichTextView getTextViewBadge() {
        return textViewBadge;
    }

    public RichTextView getButtonTitle() {
        return buttonTitle;
    }

}
