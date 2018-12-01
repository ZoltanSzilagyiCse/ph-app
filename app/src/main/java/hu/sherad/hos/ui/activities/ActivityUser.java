package hu.sherad.hos.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.format.DateUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Locale;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.models.TopicResult;
import hu.sherad.hos.data.models.User;
import hu.sherad.hos.utils.ViewUtils;

public class ActivityUser extends ActivityBase implements TopicResult.OnTopicResult {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy.MM.dd. HH:mm", Locale.getDefault());

    private PH.Data data = PH.Data.DATA_LOADING;
    private User user;
    private String userLink;

    private ImageView imageView;
    private Button buttonSendPrivate;
    private TextView textViewLastVisited;
    private TextView textViewRegistered;
    private TextView textViewName;
    private TextView textViewUserRank;
    private TextView textViewUserSignature;
    private TextView textViewCommentsProfessional;
    private TextView textViewCommentsSocial;
    private TextView textViewCommentsMarket;
    private TextView textViewBlogCount;
    private TextView textViewItems;

    private boolean lastVisitedAbsolute = false;
    private boolean registeredAbsolute = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getIntent().getExtras();

        handleArguments(arguments);

        setContentView(R.layout.activity_user);

        setStatusBarColor();
        assignLayoutElements();

        setupViews();

        loadUserInfo();
    }

    private void loadUserInfo() {
        PH.getPHService().getUserInfo(userLink, this);
    }

    private void setupViews() {
        textViewRegistered.setOnClickListener(view -> {
            registeredAbsolute = !registeredAbsolute;
            textViewRegistered.setText(getString(R.string.registeredV, user.getRegistered() == null ? "" :
                    (registeredAbsolute ? simpleDateFormat.format(user.getRegistered().getTime()) : DateUtils.getRelativeTimeSpanString(user.getRegistered().getTime(),
                            System.currentTimeMillis(),
                            DateUtils.SECOND_IN_MILLIS))));
        });
        textViewLastVisited.setOnClickListener(view -> {
            lastVisitedAbsolute = !lastVisitedAbsolute;
            textViewLastVisited.setText(getString(R.string.lastSeenV, user.getLastVisited() == null ? "" :
                    (lastVisitedAbsolute ? simpleDateFormat.format(user.getLastVisited().getTime()) : DateUtils.getRelativeTimeSpanString(user.getLastVisited().getTime(),
                            System.currentTimeMillis(),
                            DateUtils.SECOND_IN_MILLIS))));
        });
        buttonSendPrivate.setOnClickListener(view -> {
            Intent intent = new Intent(this, ActivityNewComment.class);
            // Send private message to the user
            intent.putExtra(ActivityNewComment.EXTRA_URL, user.getMessageLink());
            intent.putExtra(ActivityNewComment.EXTRA_ACTION, ActivityNewComment.Action.SEND_PRIVATE);
            startActivity(intent);
        });
    }

    private void assignLayoutElements() {
        imageView = findViewById(R.id.img_activity_user);
        textViewUserSignature = findViewById(R.id.text_view_activity_user_signature_detailed);
        textViewLastVisited = findViewById(R.id.text_view_activity_user_activity_last);
        textViewRegistered = findViewById(R.id.text_view_activity_user_activity_started);
        textViewName = findViewById(R.id.text_view_activity_user_name);
        buttonSendPrivate = findViewById(R.id.button_activity_user_send_private);
        textViewUserRank = findViewById(R.id.text_view_activity_user_rank);
        textViewCommentsProfessional = findViewById(R.id.text_view_activity_user_comments_professional);
        textViewCommentsSocial = findViewById(R.id.text_view_activity_user_comments_social);
        textViewCommentsMarket = findViewById(R.id.text_view_activity_user_comments_market);
        textViewBlogCount = findViewById(R.id.text_view_activity_user_blogs);
        textViewItems = findViewById(R.id.text_view_activity_user_items_detailed);
    }

    private void handleArguments(Bundle arguments) {
        if (arguments == null || arguments.getString(ActivityNewComment.EXTRA_URL) == null) {
            throw new RuntimeException("Arguments cannot be null! Need at least: " + ActivityNewComment.EXTRA_URL);
        }
        userLink = arguments.getString(ActivityNewComment.EXTRA_URL);
    }

    @Override
    public void onResult(@NonNull TopicResult result) {
        this.data = result.getData();
        if (!isDestroyed()) {
            if (data == PH.Data.OK) {
                this.user = result.getUser();
                bindUser();
            } else {
                Toast.makeText(this, PH.getErrorFromCode(data), Toast.LENGTH_SHORT).show();
                textViewName.setText(R.string.failed_to_load);
            }
        }
    }

    private void bindUser() {
        if (user == null || data != PH.Data.OK) {
            Toast.makeText(this, PH.getErrorFromCode(data), Toast.LENGTH_SHORT).show();
            return;
        }
        Glide.with(this)
                .load(user.getAvatarLink())
                .apply(ViewUtils.getDefaultGlideOptions(R.drawable.vd_account_circle))
                .into(imageView);
        textViewName.setText(user.getName());
        textViewUserSignature.setText(Html.fromHtml(user.getSignature()));
        textViewCommentsProfessional.setText(getString(R.string.professionalCommentV, String.valueOf(user.getCommentProfessional())));
        textViewCommentsSocial.setText(getString(R.string.socialCommentV, String.valueOf(user.getCommentSocial())));
        textViewCommentsMarket.setText(getString(R.string.marketCommentV, String.valueOf(user.getCommentMarket())));
        textViewBlogCount.setText(getString(R.string.blogsV, String.valueOf(user.getBlogCount())));
        textViewItems.setText(Html.fromHtml(user.getItems()));
        textViewUserRank.setText(user.getRank());

        textViewLastVisited.callOnClick();
        textViewRegistered.callOnClick();

        buttonSendPrivate.setEnabled(!user.getMessageLink().isEmpty());

    }
}
