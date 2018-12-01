package hu.sherad.hos.ui.recyclerview.adapters;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import hu.sherad.hos.BuildConfig;
import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.data.models.TopicComment;
import hu.sherad.hos.ui.activities.ActivityComments;
import hu.sherad.hos.ui.activities.ActivityNewComment;
import hu.sherad.hos.ui.recyclerview.holders.HolderListComment;
import hu.sherad.hos.ui.recyclerview.holders.HolderListSimpleView;
import hu.sherad.hos.utils.DrawableCallback;
import hu.sherad.hos.utils.HtmlUtils;
import hu.sherad.hos.utils.ViewUtils;
import hu.sherad.hos.utils.keyboard.ClipboardUtils;

public class AdapterComments extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<TopicComment> comments = new ArrayList<>();
    private SimpleDateFormat simpleDateFormat;
    private PH.Data data = PH.Data.OK;
    private ActivityComments activity;
    private Actions actions;
    private boolean isTimeAbsolute;
    private boolean isListInc;
    private boolean isSignatureVisible;
    private boolean isRankVisible;
    private int newComments;
    private int[] activeComments = null;

    public AdapterComments() {
        setHasStableIds(true);
    }

    public void setup(ActivityComments activity, int newComments) {
        this.actions = activity;
        this.activity = activity;
        this.newComments = newComments;
        SharedPreferences sharedPreferencesAppearance = PHPreferences.getInstance().getAppearancePreferences();
        isListInc = sharedPreferencesAppearance.getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_LIST_INC, true);
        isTimeAbsolute = sharedPreferencesAppearance.getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_TIME, false);
        isSignatureVisible = sharedPreferencesAppearance.getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_SIGNATURE, false);
        isRankVisible = sharedPreferencesAppearance.getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_RANK, false);

        simpleDateFormat = new SimpleDateFormat(sharedPreferencesAppearance.getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_TIME_SECONDS, false) ?
                "yyyy.MM.dd. HH:mm:ss" : "yyyy.MM.dd. HH:mm", Locale.getDefault());

        setData(PH.Data.DATA_LOADING);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case R.layout.list_item_comment:
                return new HolderListComment(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
            case R.layout.list_item_loading:
            case R.layout.list_item_text_view:
                return new HolderListSimpleView(LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false));
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case R.layout.list_item_text_view:
                bindViewAsText((HolderListSimpleView) holder, position);
                break;
            case R.layout.list_item_comment:
                if (isListInc && data != PH.Data.OK) {
                    position--;
                }
                bindViewAsComment((HolderListComment) holder, position);
                break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        int dataPosition = RecyclerView.NO_POSITION;
        if (isListInc) {
            if (data != PH.Data.OK && position == 0) {
                dataPosition = position;
            }
        } else {
            if (position >= comments.size()) {
                dataPosition = position;
            }
        }
        if (position == dataPosition) {
            if (data == PH.Data.DATA_LOADING) {
                return R.layout.list_item_loading;
            }
            return R.layout.list_item_text_view;
        }
        return R.layout.list_item_comment;
    }

    @Override
    public int getItemCount() {
        return comments.size() + (data == PH.Data.OK ? 0 : 1);
    }

    @Override
    public long getItemId(int position) {
        if (isListInc) {
            // First item can be TextView or Comment depends the data. Otherwise it is a comment
            if (data == PH.Data.OK) {
                return comments.get(position).getDate().getTime();
            } else {
                return position == 0 ? PH.getIDFromCode(data) : comments.get(position - 1).getDate().getTime();
            }
        } else {
            // Last item is a TextView, the others are the comments
            return position >= comments.size() ? PH.getIDFromCode(data) : comments.get(position).getDate().getTime();
        }
    }

    @NonNull
    public List<TopicComment> getComments() {
        return comments;
    }

    public void setActiveComments(int[] fromTo) {
        this.activeComments = fromTo;
    }

    public void setData(PH.Data data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public int getNewComments() {
        return newComments;
    }

    public boolean isListInc() {
        return isListInc;
    }

    public void addComments(List<TopicComment> comments, @NonNull PH.Data data) {
        this.comments.clear();
        this.comments.addAll(comments);
        this.data = data;
        notifyDataSetChanged();
    }

    public void addComment(TopicComment comment, int index) {
        if (data == PH.Data.OK) {
            this.comments.add(index, comment);
            notifyItemInserted(index);
        }
    }

    public void modifyComment(TopicComment comment, int i) {
        comments.set(i, comment);
        notifyDataSetChanged();
    }

    private void bindViewAsComment(HolderListComment holder, int position) {
        TopicComment comment = comments.get(position);

        holder.getImgAlert().setVisibility(isCommentActivated(comment) ? View.VISIBLE : View.GONE);

        holder.getTvHeaderCount().setText(activity.getType() == ActivityComments.Type.TOPIC_MESSAGE ? "" : String.valueOf(comment.getID()));
        holder.getTvHeaderCount().setOnLongClickListener(v -> ClipboardUtils.copyToClipBoard(activity, comment.getID_URL(), comment.getID_URL(), "Link vágólapra másolva"));

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
            holder.getTvHeaderQuoteCount().setOnClickListener(v -> activity.getAnswersList(comment));
            holder.getTvHeaderQuoteCount().setOnLongClickListener(v -> ClipboardUtils.copyToClipBoard(activity, comment.getID_URL_quoted(), comment.getID_URL_quoted(), "Link vágólapra másolva"));
            holder.getTvHeaderQuoteName().setText(comment.getQuotedName());
            holder.getTvHeaderQuoteName().setOnClickListener(view -> actions.showUserProfile(comment.getQuotedURL(), view));
        }
        if (newComments > 0) {
            if (isListInc) {
                int difference = getComments().size() - position;
                if ((newComments) >= difference) {
                    holder.getImgNewComment().setVisibility(View.VISIBLE);
                } else {
                    holder.getImgNewComment().setVisibility(View.GONE);
                }
            } else {
                if ((newComments - 1) >= position) {
                    holder.getImgNewComment().setVisibility(View.VISIBLE);
                } else {
                    holder.getImgNewComment().setVisibility(View.GONE);
                }
            }
        } else {
            holder.getImgNewComment().setVisibility(View.GONE);
        }

        holder.getTvHeaderName().setText(comment.getAuthorName());
        holder.getTvHeaderName().setOnClickListener(view -> actions.showUserProfile(comments.get(position).getAuthorURL(), view));

        holder.getImageView().setOnClickListener(view -> actions.showUserProfile(comment.getAuthorURL(), view));
        Glide.with(activity).load(comment.getAuthorAvatarURL()).apply(ViewUtils.getDefaultGlideOptions(R.drawable.vd_account_circle)).into(holder.getImageView());

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

        holder.getTvHeaderCount().setTopicModerator(activity.getTopicDetailed().isModerator(comment.getAuthorName()));
        holder.getTvHeaderName().setTopicModerator(activity.getTopicDetailed().isModerator(comment.getAuthorName()));
        holder.getTvHeaderQuoteName().setTopicModerator(activity.getTopicDetailed().isModerator(comment.getQuotedName()));
        holder.getTvHeaderQuoteCount().setTopicModerator(activity.getTopicDetailed().isModerator(comment.getQuotedName()));

        holder.getImgOptions().setEnabled(true);
        holder.getImgOptions().setVisibility(View.VISIBLE);
        holder.getImgOptions().setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
            MenuItem.OnMenuItemClickListener clickListener = item -> {
                switch (item.getItemId()) {
                    case R.id.menu_comment_options_send_reply:
                        actions.startNewComment(comment, null, ActivityNewComment.Action.SEND_REPLY);
                        break;
                    case R.id.menu_comment_options_send_private:
                        actions.startNewComment(comment, comment.getPrivateURL(), ActivityNewComment.Action.SEND_PRIVATE);
                        break;
                    case R.id.menu_comment_options_edit:
                        actions.startNewComment(comment, null, ActivityNewComment.Action.EDIT);
                        break;
                    case R.id.menu_comment_options_copy:
                        MaterialDialog materialDialog = new MaterialDialog.Builder(activity)
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
            if (activity.getTopicDetailed().getStatus() != Topic.Status.CLOSED && activity.getType() != ActivityComments.Type.TOPIC_MESSAGE) {
                popupMenu.getMenu().add(0, R.id.menu_comment_options_send_reply, 0, R.string.send_reply).setOnMenuItemClickListener(clickListener);
            }
            if (!comment.getPrivateURL().isEmpty()) {
                popupMenu.getMenu().add(0, R.id.menu_comment_options_send_private, 0, R.string.send_private_message).setOnMenuItemClickListener(clickListener);
            }
            if (!comment.getEditURL().isEmpty()) {
                popupMenu.getMenu().add(0, R.id.menu_comment_options_edit, 0, R.string.edit).setOnMenuItemClickListener(clickListener);
            }
            if (BuildConfig.DEBUG) {
                popupMenu.getMenu().add(0, R.id.menu_comment_options_edit, 0, R.string.edit_test).setOnMenuItemClickListener(clickListener);
            }
            popupMenu.getMenu().add(0, R.id.menu_comment_options_copy, 0, R.string.copy).setOnMenuItemClickListener(clickListener);
            popupMenu.show();
        });
    }

    private boolean isCommentActivated(TopicComment comment) {
        return activeComments != null && activeComments[0] <= comment.getID() && activeComments[1] >= comment.getID();
    }

    private void bindViewAsText(HolderListSimpleView holder, int position) {
        TextView info = holder.itemView.findViewById(R.id.text_view_util);
        info.setText(PH.getErrorFromCode(data));

        switch (data) {
            case ERROR_LOAD:
            case ERROR_NO_INTERNET:
            case ERROR_INTERNET_FAILURE:
            case ERROR_TIMEOUT:
            case ERROR_SERVER:
                info.setOnClickListener(v -> {
                    data = PH.Data.DATA_LOADING;
                    notifyItemChanged(position);
                    actions.reload();
                });
                break;
            default:
                info.setOnClickListener(null);
                break;
        }
    }

    public interface Actions {

        void reload();

        void showUserProfile(String userLink, View view);

        void startNewComment(@Nullable TopicComment comment, @Nullable String URL, ActivityNewComment.Action action);

    }

}
