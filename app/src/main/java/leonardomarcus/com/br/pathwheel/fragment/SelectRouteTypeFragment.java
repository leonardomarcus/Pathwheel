package leonardomarcus.com.br.pathwheel.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import leonardomarcus.com.br.pathwheel.R;
import leonardomarcus.com.br.pathwheel.view.MainActivity;


public class SelectRouteTypeFragment extends Fragment {

    public SelectRouteTypeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        MainActivity.getInstance().setPathwheelTitle("Selecionar Rota");

        View view = inflater.inflate(R.layout.fragment_select_route_type, container, false);


        Button buttonManual = (Button) view.findViewById(R.id.button_rota_manual);
        buttonManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().changeFragment(new OsmManualRouteFragment(),MainActivity.ROUTE);
            }
        });

        Button buttonGoogle = (Button) view.findViewById(R.id.button_rota_google);
        buttonGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.getInstance().changeFragment(new RouteByGoogleFragment(),MainActivity.ROUTE);
            }
        });

        return view;

    }

}
