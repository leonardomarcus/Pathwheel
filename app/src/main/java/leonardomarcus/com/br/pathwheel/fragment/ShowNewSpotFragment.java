package leonardomarcus.com.br.pathwheel.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import leonardomarcus.com.br.pathwheel.R;
import leonardomarcus.com.br.pathwheel.api.model.Spot;
import leonardomarcus.com.br.pathwheel.service.PathwheelPreferences;


public class ShowNewSpotFragment extends Fragment implements OnMapReadyCallback {

    private View rootView;


    public ShowNewSpotFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.rootView = inflater.inflate(R.layout.fragment_show_new_spot, container, false);

        MapView mapView = (MapView) rootView.findViewById(R.id.mapaViewNewSpot);
        mapView.onCreate(savedInstanceState);

        mapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getMapAsync(this);

        return this.rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Spot spot = PathwheelPreferences.getSpot(rootView.getContext());
        LatLng latLgn = new LatLng(spot.getLatitude(),spot.getLongitude());
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLgn, 18));
        List<Spot> spots = new ArrayList<Spot>();
        spots.add(spot);
        Spot.addMarkers(googleMap, spots, rootView.getContext());
    }
}
