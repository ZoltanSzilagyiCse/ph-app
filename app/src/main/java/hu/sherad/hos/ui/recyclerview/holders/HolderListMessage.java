package hu.sherad.hos.ui.recyclerview.holders;

import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import hu.sherad.hos.R;
import hu.sherad.hos.ui.widget.RichTextView;

public class HolderListMessage extends RecyclerView.ViewHolder {

    private RichTextView textViewTitle;
    private RichTextView textViewBadge;
    private RichTextView textViewDate;
    private AppCompatImageButton imageViewNotification;
    private AppCompatImageButton imageViewUndo;
    private AppCompatImageButton imageViewBan;
    private AppCompatImageView imageAvatar;
    private boolean lastVisitedAbsolute;

    public HolderListMessage(View itemView) {
        super(itemView);
        textViewTitle = itemView.findViewById(R.id.tv_util_list_item_message_title);
        textViewBadge = itemView.findViewById(R.id.tv_list_item_message_badge);
        imageAvatar = itemView.findViewById(R.id.img_util_list_item_message);
        textViewDate = itemView.findViewById(R.id.text_view_list_item_message_date);
        imageViewBan = itemView.findViewById(R.id.image_view_list_item_message_ban);
        imageViewUndo = itemView.findViewById(R.id.image_view_list_item_message_undo);
        imageViewNotification = itemView.findViewById(R.id.image_view_list_item_message_notification);
    }

    public RichTextView getTextViewTitle() {
        return textViewTitle;
    }

    public RichTextView getTextViewBadge() {
        return textViewBadge;
    }

    public AppCompatImageView getImageAvatar() {
        return imageAvatar;
    }

    public RichTextView getTextViewDate() {
        return textViewDate;
    }

    public AppCompatImageButton getImageViewBan() {
        return imageViewBan;
    }

    public AppCompatImageButton getImageViewNotification() {
        return imageViewNotification;
    }

    public AppCompatImageButton getImageViewUndo() {
        return imageViewUndo;
    }

    public boolean isLastVisitedAbsolute() {
        return lastVisitedAbsolute;
    }

    public void setLastVisitedAbsolute(boolean lastVisitedAbsolute) {
        this.lastVisitedAbsolute = lastVisitedAbsolute;
    }

}
