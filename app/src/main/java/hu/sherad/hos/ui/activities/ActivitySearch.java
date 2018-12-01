package hu.sherad.hos.ui.activities;

import android.app.SearchManager;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.TransitionRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import hu.sherad.hos.BuildConfig;
import hu.sherad.hos.R;
import hu.sherad.hos.data.api.ph.PH;
import hu.sherad.hos.data.models.TopicResult;
import hu.sherad.hos.ui.recyclerview.SlideInItemAnimator;
import hu.sherad.hos.ui.recyclerview.WrapContentLinearLayoutManager;
import hu.sherad.hos.ui.recyclerview.adapters.AdapterSearch;
import hu.sherad.hos.utils.HtmlUtils;
import hu.sherad.hos.utils.io.Logger;
import hu.sherad.hos.utils.keyboard.ImeUtils;
import io.fabric.sdk.android.Fabric;

public class ActivitySearch extends ActivityBase implements TopicResult.OnTopicResult, AdapterSearch.ReachedEnd {

    private ViewGroup container;
    private RecyclerView recyclerView;
    private AdapterSearch adapter;
    private Toolbar toolbar;
    private EditText editText;
    private EditText editTextAll;
    private EditText editTextExac;
    private EditText editTextSome;
    private EditText editTextNone;
    private EditText editTextUser;
    private ViewSwitcher viewSwitcher;
    private Spinner spinner;
    private View viewSearchFor;
    private CardView cardViewDefault;
    private CardView cardViewFilter;

    private SparseArray<Transition> transitions = new SparseArray<>();

    private boolean focusQuery = true;

    private int currentOffset = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fabric.with(this, new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build()).build());
        setContentView(R.layout.activity_search);
        setStatusBarColor();

        container = findViewById(R.id.coordinator_layout_activity_search_container);
        recyclerView = findViewById(R.id.recycler_view_activity_search);
        toolbar = findViewById(R.id.toolbar_activity_search);
        editText = findViewById(R.id.edit_text_activity_search);
        editTextAll = findViewById(R.id.edit_text_activity_search_all);
        editTextExac = findViewById(R.id.edit_text_activity_search_exac);
        editTextSome = findViewById(R.id.edit_text_activity_search_some);
        editTextNone = findViewById(R.id.edit_text_activity_search_none);
        editTextUser = findViewById(R.id.edit_text_activity_search_user);
        viewSwitcher = findViewById(R.id.view_switcher_activity_search);
        viewSearchFor = findViewById(R.id.button_activity_search_for);
        spinner = findViewById(R.id.spinner_activity_search);
        cardViewFilter = findViewById(R.id.card_view_activity_search_filter);
        cardViewDefault = findViewById(R.id.card_view_activity_search_default);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setTitle("Keresés");

        container.setOnClickListener(v -> onBackPressed());

        adapter = new AdapterSearch(this, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setItemAnimator(new SlideInItemAnimator());
        recyclerView.setLayoutManager(new WrapContentLinearLayoutManager(this));
        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.HORIZONTAL));

        onNewIntent(getIntent());

        viewSearchFor.setOnClickListener(view -> searchFor());

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        if (cardViewFilter.getVisibility() == View.VISIBLE) {
                            TransitionManager.beginDelayedTransition(container, getTransition(R.transition.auto));
                            cardViewFilter.setVisibility(View.GONE);
                            cardViewDefault.setVisibility(View.VISIBLE);
                        }
                        break;
                    case 1:
                        if (cardViewDefault.getVisibility() == View.VISIBLE) {
                            TransitionManager.beginDelayedTransition(container, getTransition(R.transition.auto));
                            cardViewDefault.setVisibility(View.GONE);
                            cardViewFilter.setVisibility(View.VISIBLE);
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        MenuItem menuItemFilter = menu.add(0, R.id.menu_search_filter, 0, isSearchFilter() ? "Találatok" : "Szűrő");
        menuItemFilter.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        // Filter
        Drawable drawableFilter = ContextCompat.getDrawable(this, isSearchFilter() ? R.drawable.vd_filter : R.drawable.vd_filter_outline);
        if (drawableFilter != null) {
            drawableFilter.setTint(ContextCompat.getColor(this, R.color.icons));
        }
        menuItemFilter.setIcon(drawableFilter);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_search_filter:
                switchSearch();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.hasExtra(SearchManager.QUERY)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (!TextUtils.isEmpty(query)) {
                editText.setText(query);
                currentOffset = 0;
                searchFor();
            }
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onEnterAnimationComplete() {
        if (focusQuery) {
            editText.requestFocus();
            ImeUtils.showIme(editText);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case AdapterSearch.REQUEST_CODE_VIEW_TOPIC:
                focusQuery = false;
                break;
        }
    }

    @Override
    public void onResult(@NonNull TopicResult topicResult) {
        if (topicResult.getTopics().size() > 0) {
            adapter.addTopics(topicResult.getTopics(), topicResult.getData());
        } else {
            Toast.makeText(this, getString(R.string.no_search_results), Toast.LENGTH_SHORT).show();
            adapter.setData(PH.Data.OK);
        }
    }

    @Override
    public void loadMore() {
        currentOffset += 50;
        reload();
    }

    @Override
    public void reload() {
        String url = createURL(currentOffset);
        if (url != null) {
            PH.getPHService().getSubTopicsSearch(url, currentOffset, this);
        }
    }

    private void switchSearch() {
        switch (viewSwitcher.getCurrentView().getId()) {
            case R.id.recycler_view_activity_search:
                viewSwitcher.showPrevious();
                invalidateOptionsMenu();
                break;
            case R.id.scroll_view_activity_search:
                viewSwitcher.showNext();
                ImeUtils.hideIme(container);
                invalidateOptionsMenu();
                break;
        }
    }

    private boolean isSearchFilter() {
        return viewSwitcher.getCurrentView().getId() == R.id.scroll_view_activity_search;
    }

    private void searchFor() {
        String url = createURL(0);
        if (url != null) {
            currentOffset = 0;
            PH.getPHService().getSubTopicsSearch(url, currentOffset, this);
            ImeUtils.hideIme(container);
            adapter.setLoading();
            viewSwitcher.showPrevious();
            invalidateOptionsMenu();
        }
    }

    @Nullable
    private String createURL(int currentOffset) {
        try {
            if (spinner.getSelectedItemPosition() == 0) {
                String text = editText.getText().toString();
                if (text.isEmpty()) {
                    Toast.makeText(this, "Üres mező", Toast.LENGTH_SHORT).show();
                    return null;
                }
                return HtmlUtils.createSearchURL(text, currentOffset);
            } else {
                String textAll = editTextAll.getText().toString();
                String textExac = editTextExac.getText().toString();
                String textSome = editTextSome.getText().toString();
                String textNone = editTextNone.getText().toString();
                String textUser = editTextUser.getText().toString();
                if (!textAll.isEmpty() || !textExac.isEmpty() || !textSome.isEmpty() || !textNone.isEmpty() || !textUser.isEmpty()) {
                    return HtmlUtils.createSearchURL(
                            textAll,
                            textExac,
                            textSome,
                            textNone,
                            textUser,
                            currentOffset);
                } else {
                    Toast.makeText(this, "Üres mező", Toast.LENGTH_SHORT).show();
                    return null;
                }
            }
        } catch (Exception e) {
            Logger.getLogger().e(e);
            Toast.makeText(this, "Sikertelen EXTRA_URL készítés", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private Transition getTransition(@TransitionRes int transitionId) {
        Transition transition = transitions.get(transitionId);
        if (transition == null) {
            transition = TransitionInflater.from(this).inflateTransition(transitionId);
            transitions.put(transitionId, transition);
        }
        return transition;
    }

}
