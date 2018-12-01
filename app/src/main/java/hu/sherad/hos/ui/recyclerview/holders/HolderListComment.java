package hu.sherad.hos.ui.recyclerview.holders;

import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.ui.widget.AuthorTextView;
import hu.sherad.hos.ui.widget.RichTextView;

public class HolderListComment extends RecyclerView.ViewHolder {

    private AuthorTextView tvHeaderCount;
    private AuthorTextView tvHeaderName;
    private AuthorTextView tvHeaderQuote;
    private AuthorTextView tvHeaderQuoteCount;
    private AuthorTextView tvHeaderQuoteName;
    private RichTextView tvComment;
    private TextView tvTime;
    private RichTextView textViewRank;
    private ImageView imgNewComment;
    private TextView tvSignature;
    private ImageView imageView;
    private ImageView imgOptions;
    private ImageView imgAlert;

    public HolderListComment(View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.img_list_comment);
        tvHeaderCount = itemView.findViewById(R.id.text_view_list_comment_header_count);
        tvHeaderName = itemView.findViewById(R.id.text_view_list_comment_header_name);
        tvHeaderQuote = itemView.findViewById(R.id.text_view_list_comment_header_quote);
        tvHeaderQuoteCount = itemView.findViewById(R.id.text_view_list_comment_header_quote_count);
        tvHeaderQuoteName = itemView.findViewById(R.id.text_view_list_comment_header_quote_name);
        tvComment = itemView.findViewById(R.id.text_view_list_comment);
        tvTime = itemView.findViewById(R.id.text_view_list_comment_time);
        imgOptions = itemView.findViewById(R.id.img_list_item_comment_options);
        imgAlert = itemView.findViewById(R.id.img_list_item_comment_alert);
        imgNewComment = itemView.findViewById(R.id.view_list_item_comment_new);
        tvSignature = itemView.findViewById(R.id.text_view_list_comment_signature);
        textViewRank = itemView.findViewById(R.id.text_view_list_comment_rank);
        tvComment.setTextSize(TypedValue.COMPLEX_UNIT_PX,
                itemView.getContext().getResources().getDimension(R.dimen.text_size_body) *
                        (PHPreferences.getInstance().getAppearancePreferences().getFloat(PH.Prefs.KEY_APPEARANCE_COMMENT_TEXT_SIZE, 100.0f) / 100));
    }

    public RichTextView getTextViewRank() {
        return textViewRank;
    }

    public TextView getTvSignature() {
        return tvSignature;
    }

    public AuthorTextView getTvHeaderName() {
        return tvHeaderName;
    }

    public AuthorTextView getTvHeaderQuote() {
        return tvHeaderQuote;
    }

    public AuthorTextView getTvHeaderQuoteName() {
        return tvHeaderQuoteName;
    }

    public ImageView getImgNewComment() {
        return imgNewComment;
    }

    public RichTextView getTvComment() {
        return tvComment;
    }

    public TextView getTvTime() {
        return tvTime;
    }

    public AuthorTextView getTvHeaderCount() {
        return tvHeaderCount;
    }

    public AuthorTextView getTvHeaderQuoteCount() {
        return tvHeaderQuoteCount;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public ImageView getImgOptions() {
        return imgOptions;
    }

    public ImageView getImgAlert() {
        return imgAlert;
    }
}
