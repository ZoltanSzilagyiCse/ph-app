package hu.sherad.hos.ui.fragments.register;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.sherad.hos.R;

public class FragmentRegistrationEntry extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View parent = inflater.inflate(R.layout.flow_fragment_register_entry, container, false);
        return parent;
    }
}
