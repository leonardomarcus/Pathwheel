package leonardomarcus.com.br.pathwheel.fragment;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import leonardomarcus.com.br.pathwheel.R;
import leonardomarcus.com.br.pathwheel.api.space.GeographicCoordinate;
import leonardomarcus.com.br.pathwheel.api.request.RouteMapRequest;
import leonardomarcus.com.br.pathwheel.service.PathwheelPreferences;
import leonardomarcus.com.br.pathwheel.view.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class ManualRouteFragment extends Fragment implements OnMapReadyCallback {

    private View rootView;
    private List<Marker> markers = new ArrayList<Marker>();
    private List<Polyline> polylines = new ArrayList<Polyline>();

    public ManualRouteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        MainActivity.getInstance().setPathwheelTitle("Rota Manual");

        this.rootView = inflater.inflate(R.layout.fragment_manual_route, container, false);

        Button button = (Button) rootView.findViewById(R.id.button_route_map_manual);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(markers.size() < 2) {
                    //Toast.makeText(rootView.getContext(), "Marque pelo menos 2 pontos", Toast.LENGTH_SHORT).show();
                    AlertDialog alertDialog = new AlertDialog.Builder(rootView.getContext()).create();
                    alertDialog.setTitle("Atenção");
                    alertDialog.setMessage("Marque pelo menos 2 pontos no mapa.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                else {
                    RouteMapRequest request = new RouteMapRequest();
                    for(Marker m : markers) {
                        GeographicCoordinate geo = new GeographicCoordinate();
                        geo.setLatitude(m.getPosition().latitude);
                        geo.setLongitude(m.getPosition().longitude);
                        request.getCoordinates().add(geo);
                    }
                    PathwheelPreferences.setRouteMapRequest(rootView.getContext(),request);

                    FragmentTransaction transaction = MainActivity.getInstance().getFragmentManager().beginTransaction();
                    Fragment fragment = new MappedRouteFragment();
                    transaction.replace(R.id.content, fragment);
                    transaction.commit();
                }
            }
        });


        MapView mapView = (MapView) rootView.findViewById(R.id.mapaViewRotaManual);
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
    public void onMapReady(final GoogleMap googleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PathwheelPreferences.getLastKnownLocation(rootView.getContext()), 18));
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng).draggable(true));

                markers.add(marker);

                refreshPolylines(googleMap);
            }
        });
        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                refreshPolylines(googleMap);
            }
        });

    }

    private void refreshPolylines(GoogleMap googleMap) {
        for(Polyline polyline : polylines) {
            polyline.remove();
        }
        List<LatLng> latLngs = new ArrayList<LatLng>();
        for (Marker m : markers) {
            latLngs.add(m.getPosition());
        }
        Polyline polyline = googleMap.addPolyline(new PolylineOptions().addAll(latLngs).width(20).color(Color.parseColor("#000000")));
        polylines.add(polyline);
    }
}
