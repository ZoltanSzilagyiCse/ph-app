package hu.sherad.hos.utils;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import hu.sherad.hos.ui.activities.ActivityComments;
import hu.sherad.hos.ui.fragments.FragmentComments;

public class TabManager extends PagerAdapter {

    private final SparseArray<FragmentComments> tabs;
    private final ActivityComments activity;
    private final int size;

    public TabManager(ActivityComments activity, SparseArray<FragmentComments> tabs, int size) {
        this.activity = activity;
        this.size = size;
        this.tabs = tabs;
    }

    @Override
    public int getCount() {
        return size;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        FragmentComments fragmentComments;
        if (tabs.get(position) == null) {
            fragmentComments = new FragmentComments(activity, container);
            tabs.put(position, fragmentComments);
            container.addView(fragmentComments.getParent());
            return fragmentComments.getParent();
        }
        fragmentComments = tabs.get(position);
        container.addView(fragmentComments.getParent());
        return fragmentComments.getParent();
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup parent, int position, @NonNull Object object) {
        super.setPrimaryItem(parent, position, object);
        tabs.get(position).setupAndLoad(position);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }


    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
