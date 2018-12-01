package hu.sherad.hos.ui.recyclerview.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHApplication;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.models.TopicComment;
import hu.sherad.hos.data.models.TopicDetailed;
import hu.sherad.hos.ui.recyclerview.holders.HolderListComment;
import hu.sherad.hos.utils.DrawableCallback;
import hu.sherad.hos.utils.HtmlUtils;
import hu.sherad.hos.utils.ViewUtils;
import hu.sherad.hos.utils.keyboard.ClipboardUtils;

public class AdapterCommentsRead extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<TopicComment> comments = new ArrayList<>();
    private TopicDetailed topicDetailed;
    private SimpleDateFormat simpleDateFormat;
    private boolean isTimeAbsolute;
    private boolean isSignatureVisible;
    private boolean isRankVisible;
    private AdapterComments.Actions actions;

    public AdapterCommentsRead(TopicDetailed topicDetailed, AdapterComments.Actions actions) {
        this.topicDetailed = topicDetailed;
        this.actions = actions;

        isTimeAbsolute = PHPreferences.getInstance().getAppearancePreferences().getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_TIME, false);
        isSignatureVisible = PHPreferences.getInstance().getAppearancePreferences().getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_SIGNATURE, false);
        isRankVisible = PHPreferences.getInstance().getAppearancePreferences().getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_RANK, false);
        simpleDateFormat = new SimpleDateFormat(
                PHPreferences.getInstance().getAppearancePreferences().getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_TIME_SECONDS, false) ?
                        "yyyy.MM.dd. HH:mm:ss" : "yyyy.MM.dd. HH:mm", Locale.getDefault());

        setHasStableIds(true);
    }

    public void addComments(List<TopicComment> comments) {
        this.comments.addAll(comments);
        notifyDataSetChanged();
    }

    public void addComment(TopicComment comment) {
        comments.clear();
        comments.add(comment);
        notifyDataSetChanged();
    }

    public void clearComments() {
        comments.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new HolderListComment(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        TopicComment comment = comments.get(position);
        HolderListComment holder = (HolderListComment) viewHolder;

        if (comments.size() > 1) {
            holder.getTvHeaderCount().setText(String.valueOf(comment.getID()));
            holder.getTvHeaderCount().setOnLongClickListener(v -> ClipboardUtils.copyToClipBoard(PHApplication.getInstance(), comment.getID_URL(), comment.getID_URL(), "Link vágólapra másolva"));
        }
        holder.getTvTime().setText(isTimeAbsolute ? simpleDateFormat.format(comment.getDate()) :
                DateUtils.getRelativeTimeSpanString(comment.getDate().getTime(),
                        System.currentTimeMillis(),
                        DateUtils.SECOND_IN_MILLIS)
                        .toString().toLowerCase());

        if (comment.getQuotedName().isEmpty() || comment.getID_quoted() == 0) {
            holder.getTvHeaderQuoteCount().setVisibility(View.GONE);
            holder.getTvHeaderQuoteName().setVisibility(View.GONE);
            holder.getTvHeaderQuote().setVisibility(View.GONE);
        } else {
            holder.getTvHeaderQuoteCount().setVisibility(View.VISIBLE);
            holder.getTvHeaderQuoteName().setVisibility(View.VISIBLE);
            holder.getTvHeaderQuote().setVisibility(View.VISIBLE);
            holder.getTvHeaderQuoteCount().setText(String.valueOf(comment.getID_quoted()));
            holder.getTvHeaderQuoteCount().setOnLongClickListener(v -> ClipboardUtils.copyToClipBoard(PHApplication.getInstance(), comment.getID_URL_quoted(), comment.getID_URL_quoted(), "Link vágólapra másolva"));
            holder.getTvHeaderQuoteName().setText(comment.getQuotedName());
            holder.getTvHeaderQuoteName().setOnClickListener(view -> actions.showUserProfile(comment.getQuotedURL(), view));
        }
        holder.getImgNewComment().setVisibility(View.GONE);

        holder.getTvHeaderName().setText(comment.getAuthorName());
        if (!comment.getAuthorURL().isEmpty()) {
            holder.getTvHeaderName().setOnClickListener(view -> actions.showUserProfile(comments.get(position).getAuthorURL(), view));
            holder.getImageView().setOnClickListener(view -> actions.showUserProfile(comment.getAuthorURL(), view));
        }
        Glide.with(holder.getImageView()).load(comment.getAuthorAvatarURL()).apply(ViewUtils.getDefaultGlideOptions(R.drawable.vd_account_circle)).into(holder.getImageView());

        // Set the content text
        HtmlUtils.setTextWithNiceLinks(holder.getTvComment(), HtmlUtils.parseHtml(
                TopicComment.Utils.getContentToDisplay(comment), holder.getTvComment().getLinkTextColors(),
                holder.getTvComment().getHighlightColor(),
                holder.getTvComment().getContext(),
                new DrawableCallback(holder.getTvComment())));
        // Set the signature text
        if (!comment.getAuthorSignature().isEmpty()) {
            HtmlUtils.setTextWithNiceLinks(holder.getTvSignature(), HtmlUtils.parseHtml(
                    comment.getAuthorSignature(),
                    holder.getTvSignature().getLinkTextColors(),
                    holder.getTvSignature().getHighlightColor(),
                    holder.getTvSignature().getContext(),
                    new DrawableCallback(holder.getTvSignature())));
        }
        holder.getTvSignature().setVisibility((isSignatureVisible && !comment.getAuthorSignature().isEmpty()) ? View.VISIBLE : View.GONE);
        // Rank
        if (isRankVisible && !comment.getAuthorRank().isEmpty()) {
            holder.getTextViewRank().setVisibility(View.VISIBLE);
            holder.getTextViewRank().setText(comment.getAuthorRank());
            holder.getTextViewRank().setOffComment(comment.isOff());
        } else {
            holder.getTextViewRank().setVisibility(View.GONE);
        }
        holder.getTvHeaderCount().setOffComment(comment.isOff());
        holder.getTvHeaderName().setOffComment(comment.isOff());
        holder.getTvHeaderQuote().setOffComment(comment.isOff());
        holder.getTvHeaderQuoteName().setOffComment(comment.isOff());
        holder.getTvHeaderQuoteCount().setOffComment(comment.isOff());
        holder.getTvComment().setOffComment(comment.isOff());

        holder.getTvHeaderCount().setTopicModerator(topicDetailed.isModerator(comment.getAuthorName()));
        holder.getTvHeaderName().setTopicModerator(topicDetailed.isModerator(comment.getAuthorName()));
        holder.getTvHeaderQuoteName().setTopicModerator(topicDetailed.isModerator(comment.getQuotedName()));
        holder.getTvHeaderQuoteCount().setTopicModerator(topicDetailed.isModerator(comment.getQuotedName()));

        holder.getImgOptions().setEnabled(true);
        holder.getImgOptions().setVisibility(View.VISIBLE);
        holder.getImgOptions().setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            MenuItem.OnMenuItemClickListener clickListener = item -> {
                switch (item.getItemId()) {
                    case R.id.menu_comment_options_copy:
                        MaterialDialog materialDialog = new MaterialDialog.Builder(v.getContext())
                                .title("Szöveg kijelölése másoláshoz")
                                .content(TopicComment.Utils.getContentToEdit(comment.getContent()))
                                .positiveText("Kész")
                                .build();
                        TextView textView = materialDialog.getContentView();
                        if (textView != null) {
                            textView.setTextIsSelectable(true);
                        }
                        materialDialog.show();
                        break;
                }
                return true;
            };
            popupMenu.getMenu().add(0, R.id.menu_comment_options_copy, 0, R.string.copy).setOnMenuItemClickListener(clickListener);
            popupMenu.show();
        });
    }

    @Override
    public long getItemId(int position) {
        return comments.get(position).getDate().getTime();
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

}
