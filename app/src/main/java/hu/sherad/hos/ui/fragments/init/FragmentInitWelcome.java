package hu.sherad.hos.ui.fragments.init;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.navigation.Navigation;
import hu.sherad.hos.R;
import hu.sherad.hos.utils.keyboard.LinkTouchMovementMethod;

public class FragmentInitWelcome extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.flow_fragment_init_welcome, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_activity_login).setOnClickListener(v -> Navigation.findNavController(view).navigate(R.id.action_fragmentInitWelcome_to_fragmentLoginEntry));
        view.findViewById(R.id.button_activity_register).setOnClickListener(v -> Toast.makeText(getActivity(), "Még nem elérhető", Toast.LENGTH_SHORT).show());

        ((TextView) view.findViewById(R.id.text_view_login_finish_welcome_intro)).setMovementMethod(new LinkTouchMovementMethod());
    }
}
