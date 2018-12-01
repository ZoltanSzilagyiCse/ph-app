package hu.sherad.hos.ui.activities;

import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Layout;
import android.text.TextUtils;
import android.transition.TransitionManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHApplication;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.api.ph.PHService;
import hu.sherad.hos.data.models.TopicComment;
import hu.sherad.hos.ui.recyclerview.adapters.AdapterSmiles;
import hu.sherad.hos.utils.HtmlUtils;
import hu.sherad.hos.utils.keyboard.ImeUtils;


public class ActivityNewComment extends ActivityBase implements AdapterSmiles.OnSelect {

    // Comment / Message
    public static final String EXTRA_TYPE = "EXTRA_TYPE";
    // EditText inserted/typed text before
    public static final String EXTRA_INPUT_TEXT = "EXTRA_TEXT";
    // New / Edit / Private
    public static final String EXTRA_ACTION = "EXTRA_ACTION";
    // Quoted comment
    public static final String EXTRA_COMMENT = "EXTRA_COMMENT";
    // EXTRA_URL that we will call
    public static final String EXTRA_URL = "EXTRA_URL";

    private AdapterSmiles adapterSmiles;
    private MaterialDialog progressDialog;
    private ActivityComments.Type type;
    private Action action;
    private TopicComment comment;
    private String url;
    private String inputText;

    private boolean optionsAutoHide;
    private boolean isOffTopic;

    private EditText editText;
    private ViewGroup parent;
    private LinearLayout linearLayoutOptions;
    private Toolbar toolbar;
    private RecyclerView recyclerViewSmiles;
    // Bunch of TextView-s. Replace them with list or something
    private TextView textViewTitle;
    private TextView textViewThrough;
    private TextView textViewUnderLine;
    private TextView textViewBold;
    private TextView textViewItalic;
    private TextView textViewOff;
    private TextView textViewCode;
    private TextView textViewMonospace;
    private TextView textViewLink;
    private TextView textViewImg;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Bundle arguments = getIntent().getExtras();

        handleArguments(arguments);
        assignVariables();

        setContentView(R.layout.activity_new_comment);

        setStatusBarColor();
        assignLayoutElements();
        initTitle();
        initActionBar();

        setupViews();

        invalidateOptionsMenu();
        createInfoIfNeeded();

    }

    private void setupViews() {
        recyclerViewSmiles.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerViewSmiles.setAdapter(adapterSmiles);

        textViewBold.setOnClickListener(view -> insertBB("[B]", 3, "[/B]"));
        textViewItalic.setOnClickListener(view -> insertBB("[I]", 3, "[/I]"));
        textViewOff.setOnClickListener(view -> insertBB("[OFF]", 5, "[/OFF]"));
        textViewCode.setOnClickListener(view -> insertBB("[CODE]", 6, "[/CODE]"));
        textViewMonospace.setOnClickListener(view -> insertBB("[M]", 3, "[/M]"));
        textViewThrough.setOnClickListener(view -> insertBB("[S]", 3, "[/S]"));
        textViewUnderLine.setOnClickListener(view -> insertBB("[U]", 3, "[/U]"));
        textViewLink.setOnClickListener(view -> insertLinkDialog());
        textViewImg.setOnClickListener(view -> insertImageDialog());

        textViewMonospace.setText(Html.fromHtml("<tt>Monospace</tt>"));
        textViewUnderLine.setPaintFlags(textViewUnderLine.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        textViewThrough.setPaintFlags(textViewThrough.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        editText.setText(inputText);
        editText.setSelection(editText.getText().length());
    }

    private void createInfoIfNeeded() {
        if (PHPreferences.getInstance().getDefaultPreferences().getBoolean(PH.Prefs.KEY_DEFAULT_WARNING_NEW_COMMENT, true)) {
            showWarning();
        } else {
            ImeUtils.showIme(editText);
        }
    }

    private MaterialDialog insertImageDialog() {
        return new MaterialDialog.Builder(this)
                .title("Kép link beillesztése")
                .input(null, null, false, (dialog, input) -> {
                    int start = Math.max(editText.getSelectionStart(), 0);
                    editText.getText().insert(start, "[IMG:\"" + input + "\"/]");
                    if (optionsAutoHide) {
                        hideOptions();
                    }
                })
                .negativeText(R.string.cancel)
                .dismissListener(dialogInterface -> ImeUtils.showIme(editText))
                .show();
    }

    private MaterialDialog insertLinkDialog() {
        return new MaterialDialog.Builder(this)
                .title("Link beillesztése")
                .input(null, null, false, (dialog, input) -> {
                    int start = Math.max(editText.getSelectionStart(), 0);
                    int end = Math.max(editText.getSelectionEnd(), 0);

                    // start = start selection (text)
                    // end = end selection (text)
                    // input.length() = link length
                    // +9 = <link:"">
                    editText.getText().insert(start, "[LINK:\"" + input + "\"]");
                    editText.getText().insert(start + input.length() + 9 + (end - start), "[/LINK]");
                    if (start == end) {
                        // No selection, so insert a 'link' text and select it
                        // + 9 + 4 = <link:""> + link
                        editText.getText().insert(start + input.length() + 9, "link");
                        editText.setSelection(start + input.length() + 9, start + input.length() + 9 + 4);
                    } else {
                        // Select text
                        editText.setSelection(start + input.length() + 9, start + input.length() + 9 + (end - start));
                    }
                    if (optionsAutoHide) {
                        hideOptions();
                    }
                })
                .negativeText(R.string.cancel)
                .dismissListener(dialogInterface -> ImeUtils.showIme(editText))
                .show();
    }

    private void insertBB(String s, int i, String s2) {
        int start = Math.max(editText.getSelectionStart(), 0);
        int end = Math.max(editText.getSelectionEnd(), 0);
        editText.getText().insert(start, s);
        editText.getText().insert(end + i, s2);
        editText.setSelection(start + i, end + i);
        if (optionsAutoHide) {
            hideOptions();
        }
    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle("");

        textViewTitle.setText(initTitle());
        textViewTitle.setMaxLines(1);
        textViewTitle.setEllipsize(TextUtils.TruncateAt.END);
        textViewTitle.setOnClickListener(v -> toggleTitle());
    }

    private void assignLayoutElements() {
        toolbar = findViewById(R.id.toolbar_activity_new_comment);
        editText = findViewById(R.id.edit_text_dialog_send_comment);
        parent = findViewById(R.id.coordinator_layout_activity_new_comment);
        linearLayoutOptions = findViewById(R.id.linear_layout_activity_send_comment_options);
        textViewTitle = findViewById(R.id.text_view_activity_new_comment_title);
        recyclerViewSmiles = findViewById(R.id.recycler_view_activity_new_comment_smiles);
        textViewUnderLine = findViewById(R.id.text_view_dialog_send_comment_bb_underline);
        textViewThrough = findViewById(R.id.text_view_dialog_send_comment_bb_through);
        textViewBold = findViewById(R.id.text_view_dialog_send_comment_bb_bold);
        textViewItalic = findViewById(R.id.text_view_dialog_send_comment_bb_italic);
        textViewOff = findViewById(R.id.text_view_dialog_send_comment_bb_off);
        textViewCode = findViewById(R.id.text_view_dialog_send_comment_bb_code);
        textViewMonospace = findViewById(R.id.text_view_dialog_send_comment_bb_monospace);
        textViewLink = findViewById(R.id.text_view_dialog_send_comment_bb_link);
        textViewImg = findViewById(R.id.text_view_dialog_send_comment_bb_img);
    }

    private void assignVariables() {
        progressDialog = new MaterialDialog.Builder(this)
                .title(R.string.loading)
                .progress(true, 0)
                .cancelable(false)
                .build();
        optionsAutoHide = PHPreferences.getInstance().getNotificationPreferences().getBoolean(PH.Prefs.KEY_APPEARANCE_SWITCH_NEW_COMMENT_PANEL_HIDE, false);
        adapterSmiles = new AdapterSmiles(this);
        isOffTopic = comment != null && comment.isOff();
    }

    private void handleArguments(Bundle arguments) {
        if (arguments == null) {
            throw new IllegalArgumentException("Arguments cannot be null!");
        }
        inputText = arguments.getString(ActivityNewComment.EXTRA_INPUT_TEXT);
        type = (ActivityComments.Type) arguments.getSerializable(ActivityNewComment.EXTRA_TYPE);
        comment = (TopicComment) arguments.get(ActivityNewComment.EXTRA_COMMENT);
        url = arguments.getString(ActivityNewComment.EXTRA_URL);
        action = (Action) arguments.getSerializable(ActivityNewComment.EXTRA_ACTION);
    }

    private String initTitle() {
        switch (action) {
            case EDIT:
                return getString(R.string.edit);
            case SEND_NEW:
                return getString(R.string.new_comment);
            case SEND_REPLY:
                return getString(R.string.send_reply);
            case SEND_PRIVATE:
                return getString(R.string.send_private_message);
            default:
                return null;
        }
    }

    private void showWarning() {
        new MaterialDialog.Builder(this)
                .title("Figyelem")
                .content("Formázásnál kérlek nagyon figyelj, ugyanis amennyiben rosszul formázod a szöveged (lemarad egy idézőjel, vagy valahol elírtad) az üzenetet/hozzászólást nem fogja tudni elküldeni az alkalmazás! (belső hibát fog írni)\n\nA lenyíló menüben található formázásokon kívül ne használj más HTML kódot, ugyanis ez hibát okozhat, valamint ezeket a formázásokat csak a megfelelő rendeltetésüknek megfelelően használd fel! (pl. ne nyiss egy [LINK: formátumot anélkül, hogy bezárnád)\n\nHa jól formáztál mindent mégsem sikerül elküldeni az üzenetet/hozzászólást akkor írj az alkalmazás topikjába és csatold a debug információkat (topik összefoglalóban le van írva hogyan nyerheted ki őket)")
                .positiveText("Értettem")
                .dismissListener(dialogInterface -> PHPreferences.getInstance().getDefaultPreferences().edit().putBoolean(PH.Prefs.KEY_DEFAULT_WARNING_NEW_COMMENT, false).apply())
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        if (!(type == ActivityComments.Type.TOPIC_MESSAGE || action == Action.SEND_PRIVATE)) {
            MenuItem menuItemOffTopic = menu.add(0, R.id.menu_new_comment_off_topic, 0, R.string.off_topic);
            menuItemOffTopic.setCheckable(true);
            menuItemOffTopic.setChecked(isOffTopic);
        }
        // Quoted comment
        if (comment != null) {
            menu.add(0, R.id.menu_new_comment_show_quote, 0, R.string.quoted_comment);
        }
        // Send
        Drawable drawableSend = ContextCompat.getDrawable(this, R.drawable.vd_send);
        if (drawableSend != null) {
            drawableSend.setTint(ContextCompat.getColor(this, R.color.icons));
        }
        // Formatting
        Drawable drawableOptions = ContextCompat.getDrawable(this, R.drawable.vd_chevron_down);
        if (drawableOptions != null) {
            drawableOptions.setTint(ContextCompat.getColor(this, R.color.icons));
        }
        menu.add(0, R.id.menu_new_comment_send, 0, R.string.send)
                .setIcon(drawableSend).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(0, R.id.menu_new_comment_options, 0, R.string.other_options)
                .setIcon(drawableOptions).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        // Help
        menu.add(0, R.id.menu_new_comment_help, 0, "Segítség");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finishNewComment(null);
                return true;
            case R.id.menu_new_comment_options:
                if (linearLayoutOptions.getVisibility() == View.VISIBLE) {
                    hideOptions();
                } else {
                    showOptions();
                }
                return true;
            case R.id.menu_new_comment_send:
                if (editText.getText().toString().isEmpty()) {
                    Toast.makeText(this, "A begépelt szöveg üres!", Toast.LENGTH_SHORT).show();
                } else {
                    if (action == Action.EDIT) {
                        editComment();
                    } else if (action == Action.SEND_NEW) {
                        sendNewComment();
                    } else if (action == Action.SEND_REPLY) {
                        sendReply();
                    } else if (action == Action.SEND_PRIVATE) {
                        sendPrivate();
                    }
                }
                return true;
            case R.id.menu_new_comment_off_topic:
                isOffTopic = !item.isChecked();
                invalidateOptionsMenu();
                return true;
            case R.id.menu_new_comment_show_quote:
                MaterialDialog materialDialog = new MaterialDialog.Builder(this)
                        .title("Szöveg kijelölése másoláshoz")
                        .content(TopicComment.Utils.getContentToEdit(comment.getContent()))
                        .positiveText("Kész")
                        .build();
                TextView textView = materialDialog.getContentView();
                if (textView != null) {
                    textView.setTextIsSelectable(true);
                }
                materialDialog.show();
                return true;
            case R.id.menu_new_comment_help:
                showWarning();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onBackPressed() {
        finishNewComment(null);
    }

    private void finishNewComment(@Nullable TopicComment comment) {
        Intent intent = new Intent();
        intent.putExtra(ActivityNewComment.EXTRA_ACTION, action);
        intent.putExtra(ActivityNewComment.EXTRA_INPUT_TEXT, comment == null ? editText.getText().toString() : "");
        if (comment == null) {
            setResult(RESULT_CANCELED, intent);
        } else {
            intent.putExtra(ActivityNewComment.EXTRA_COMMENT, comment);
            setResult(RESULT_OK, intent);
        }
        finish();
    }

    private void sendPrivate() {
        progressDialog.show();
        PH.getPHService().sendMessagePrivate(url,
                editText.getText().toString(),
                (commentNew, error) -> {
                    progressDialog.cancel();
                    if (error.isEmpty()) {
                        Toast.makeText(PHApplication.getInstance(), R.string.successfully_post, Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(PHApplication.getInstance(), error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void sendReply() {
        progressDialog.show();
        PH.getPHService().sendComment(comment.getReplyURL(),
                editText.getText().toString(),
                !isOffTopic,
                (commentNew, error) -> {
                    progressDialog.cancel();
                    if (error.isEmpty()) {
                        Toast.makeText(PHApplication.getInstance(), R.string.successfully_post, Toast.LENGTH_SHORT).show();
                        finishNewComment(commentNew);
                    } else {
                        Toast.makeText(PHApplication.getInstance(), error, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void editComment() {
        progressDialog.show();
        PHService.SingleComment singleComment = (editedComment, error) -> {
            progressDialog.cancel();
            if (error.isEmpty()) {
                Toast.makeText(this, R.string.successfully_edit, Toast.LENGTH_SHORT).show();
                finishNewComment(editedComment);
            } else {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        };
        if (type == ActivityComments.Type.TOPIC_MESSAGE) {
            PH.getPHService().editMessage(comment.getEditURL(),
                    editText.getText().toString(),
                    singleComment);
        } else {
            PH.getPHService().editComment(
                    HtmlUtils.changeExplicitToNew(comment.getID_URL()),
                    comment.getEditURL(),
                    editText.getText().toString(),
                    !isOffTopic,
                    singleComment);
        }
    }

    public void sendNewComment() {
        progressDialog.show();
        PHService.SingleComment singleComment = (commentNew, error) -> {
            progressDialog.cancel();
            if (error.isEmpty()) {
                Toast.makeText(this, R.string.successfully_post, Toast.LENGTH_SHORT).show();
                finishNewComment(commentNew);
            } else {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        };
        if (type == ActivityComments.Type.TOPIC_MESSAGE) {
            PH.getPHService().sendMessage(url,
                    editText.getText().toString(),
                    singleComment);
        } else {
            PH.getPHService().sendComment(url,
                    editText.getText().toString(),
                    !isOffTopic,
                    singleComment);
        }
    }

    private void showOptions() {
        TransitionManager.beginDelayedTransition(parent);
        linearLayoutOptions.setVisibility(View.VISIBLE);
        textViewTitle.setMaxLines(Integer.MAX_VALUE);
        textViewTitle.setEllipsize(null);
    }

    public void hideOptions() {
        if (linearLayoutOptions.getVisibility() == View.VISIBLE) {
            TransitionManager.beginDelayedTransition(parent);
            linearLayoutOptions.setVisibility(View.GONE);
            textViewTitle.setMaxLines(1);
            textViewTitle.setEllipsize(TextUtils.TruncateAt.END);
        }
    }

    private void toggleTitle() {
        Layout layout = textViewTitle.getLayout();
        int lines = layout.getLineCount();
        if (lines > 0 && layout.getEllipsisCount(lines - 1) > 0) {
            TransitionManager.beginDelayedTransition(parent);
            textViewTitle.setMaxLines(Integer.MAX_VALUE);
            textViewTitle.setEllipsize(null);
        } else {
            TransitionManager.beginDelayedTransition(parent);
            textViewTitle.setMaxLines(1);
            textViewTitle.setEllipsize(TextUtils.TruncateAt.END);
        }
    }

    @Override
    public void onSmileSelect(String key) {
        if (editText.getSelectionStart() != editText.getSelectionEnd()) {
            editText.getText().replace(editText.getSelectionStart(), editText.getSelectionEnd(), HtmlUtils.getAllSmileCharacterFromURL().get(key));
        } else {
            editText.getText().insert(editText.getSelectionStart(), HtmlUtils.getAllSmileCharacterFromURL().get(key));
        }
        if (optionsAutoHide) {
            hideOptions();
        }
    }

    public enum Action {
        SEND_PRIVATE,
        SEND_REPLY,
        SEND_NEW,
        EDIT
    }

}
