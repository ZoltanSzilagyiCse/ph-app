package hu.sherad.hos.ui.recyclerview.adapters.section;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.sherad.hos.BuildConfig;
import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.api.ph.PHPreferences;
import hu.sherad.hos.data.models.Topic;
import hu.sherad.hos.ui.activities.ActivityComments;
import hu.sherad.hos.ui.recyclerview.holders.HolderCardButton;
import hu.sherad.hos.ui.recyclerview.holders.HolderCardRateApp;
import hu.sherad.hos.utils.HtmlUtils;

public class AdapterAbout extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int POSITION_VERSION_CARD = 0;
    private static final int POSITION_CONTRIBUTORS_CARD = 1;
    private static final int POSITION_DEVELOP_CARD = 2;
    private static final int POSITION_RATE_CARD = 3;

    private boolean rate;

    public AdapterAbout() {
        rate = PHPreferences.getInstance().getDefaultPreferences().getBoolean(PH.Prefs.KEY_DEFAULT_RATE_APP, true);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case R.layout.list_item_rate_app:
                return new HolderCardRateApp(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_rate_app, parent, false));
            case R.layout.list_item_two_lines_button:
                return new HolderCardButton(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_two_lines_button, parent, false));
        }
        throw new IllegalArgumentException();
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case R.layout.list_item_two_lines_button:
                HolderCardButton holderCardButton = (HolderCardButton) holder;
                switch (holder.getAdapterPosition()) {
                    case POSITION_VERSION_CARD:
                        setupVersionCard(holderCardButton);
                        break;
                    case POSITION_CONTRIBUTORS_CARD:
                        setupContributorsCard(holderCardButton);
                        break;
                    case POSITION_DEVELOP_CARD:
                        setupHelpDevelopCard(holderCardButton);
                        break;
                }
                break;
            case R.layout.list_item_rate_app:
                setupRateCard((HolderCardRateApp) holder);
                break;
        }
    }

    private void setupRateCard(@NonNull HolderCardRateApp holder) {
        Context context = holder.itemView.getContext();
        holder.getStars().setOnClickListener(v -> {
            final Uri uri = Uri.parse("market://details?id=hu.sherad.hos");
            final Intent rateAppIntent = new Intent(Intent.ACTION_VIEW, uri);
            if (context.getPackageManager().queryIntentActivities(rateAppIntent, 0).size() > 0) {
                context.startActivity(rateAppIntent);
            }
            PHPreferences.getInstance().getDefaultPreferences().edit().putBoolean(PH.Prefs.KEY_DEFAULT_RATE_APP, false).apply();
            rate = false;
            notifyItemRemoved(POSITION_RATE_CARD);
        });
    }

    private void setupHelpDevelopCard(HolderCardButton holderCardButton) {
        Context context = holderCardButton.itemView.getContext();
        holderCardButton.getTvTitle().setText(R.string.help_to_develop_the_app);
        holderCardButton.getTvContent().setText(R.string.develop_the_app_content);
        holderCardButton.getTvButton().setText(R.string.go_to_topic);
        holderCardButton.getTvButton().setOnClickListener(v -> {
                    Topic topic = new Topic();
                    topic.addURL(Topic.UrlType.TOPIC,
                            HtmlUtils.changeDomainToPH("https://prohardver.hu/tema/prohardver_topik_figyelo_android_alkalmazas/friss.html"));
                    context.startActivity(ActivityComments.createIntent(topic));
                }
        );
        Linkify.addLinks(holderCardButton.getTvContent(), Linkify.EMAIL_ADDRESSES);
        holderCardButton.getTvContent().setLinksClickable(true);
    }

    private void setupContributorsCard(HolderCardButton holderCardButton) {
        holderCardButton.getTvTitle().setText(R.string.acknowledgements);
        holderCardButton.getTvContent().setText(R.string.contributors);
        holderCardButton.getTvButton().setVisibility(View.GONE);
    }

    private void setupVersionCard(HolderCardButton holderCardButton) {
        holderCardButton.getTvTitle().setText(R.string.default_informations);
        holderCardButton.getTvContent().setText(new StringBuilder()
                .append("Verzió név: ")
                .append(BuildConfig.VERSION_NAME)
                .append(System.lineSeparator())
                .append("Verzió kód: ")
                .append(String.valueOf(BuildConfig.VERSION_CODE))
                .append(System.lineSeparator()));
        holderCardButton.getTvButton().setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return rate ? 4 : 3;
    }

    @Override
    public int getItemViewType(int position) {
        switch (position) {
            case POSITION_VERSION_CARD:
            case POSITION_CONTRIBUTORS_CARD:
            case POSITION_DEVELOP_CARD:
                return R.layout.list_item_two_lines_button;
            case POSITION_RATE_CARD:
                return R.layout.list_item_rate_app;
            default:
                return RecyclerView.INVALID_TYPE;
        }
    }

}
