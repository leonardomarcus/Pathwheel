package leonardomarcus.com.br.pathwheel.fragment;


import android.Manifest;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.List;

import leonardomarcus.com.br.pathwheel.R;
import leonardomarcus.com.br.pathwheel.api.endpoint.MappingEndpoint;
import leonardomarcus.com.br.pathwheel.api.endpoint.OverviewPavementTestListener;
import leonardomarcus.com.br.pathwheel.api.model.PavementSample;
import leonardomarcus.com.br.pathwheel.api.model.Spot;
import leonardomarcus.com.br.pathwheel.api.model.TravelMode;
import leonardomarcus.com.br.pathwheel.api.response.OverviewResponse;
import leonardomarcus.com.br.pathwheel.service.PathwheelPreferences;
import leonardomarcus.com.br.pathwheel.view.MainActivity;

public class OverviewFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap map;
    private View rootView;
    private List<Marker> markers = new ArrayList<>();
    private TileOverlay tileOverlayGreen = null;
    private TileOverlay tileOverlayOrange = null;
    private TileOverlay tileOverlayRed = null;
    private LatLng northeast = null;
    private LatLng southwest = null;

    public OverviewFragment() { }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Log.d("MapFragment", "onCreateView");
        // Inflate the layout for this fragment
        this.rootView = inflater.inflate(R.layout.fragment_overview, container, false);

        MainActivity.getInstance().setPathwheelTitle("Overview");

        MapView mapView = (MapView) rootView.findViewById(R.id.mapaView);
        mapView.onCreate(savedInstanceState);

        mapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            AlertDialog alertDialog = new AlertDialog.Builder(rootView.getContext()).create();
            alertDialog.setTitle("Ooops!");
            alertDialog.setMessage(e.getMessage());
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }

        mapView.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            map = googleMap;
            if (ActivityCompat.checkSelfPermission(MainActivity.getInstance(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.getInstance(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            }
            map.setMyLocationEnabled(true);
            map.getUiSettings().setMyLocationButtonEnabled(true);

            map.setMaxZoomPreference(16.5f);
            //map.getUiSettings().setTiltGesturesEnabled(false);
            //map.getUiSettings().setRotateGesturesEnabled(false);

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(PathwheelPreferences.getLastKnownLocation(rootView.getContext()), 16.5f));

            map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    LatLngBounds curScreen = map.getProjection()
                            .getVisibleRegion().latLngBounds;

                    /*Log.d("northeast", curScreen.northeast.toString());
                    Log.d("southwest", curScreen.southwest.toString());
                    Log.d("zoom", String.valueOf(map.getCameraPosition().zoom));*/

                    boolean refresh = needToRefresh(curScreen.northeast, curScreen.southwest);
                    //Log.d("needrefresh", "Need to refresh: "+refresh);
                    if(!refresh) {

                        return;
                    }

                    MappingEndpoint endpoint = new MappingEndpoint();
                    endpoint.overview(northeast.latitude+","+northeast.longitude,
                            southwest.latitude+","+southwest.longitude,
                            TravelMode.WHEELCHAIR,
                            new OverviewPavementTestListener() {
                        @Override
                        public void onOverviewPavementTest(final OverviewResponse response) {
                            //Log.d("overview", response.toString());
                            if (response.getCode() == 200) {

                                //cleaning stuffs
                                for(Marker oldMarker : markers)
                                    oldMarker.remove();
                                if(tileOverlayGreen != null)
                                    tileOverlayGreen.remove();
                                if(tileOverlayOrange != null)
                                    tileOverlayOrange.remove();
                                if(tileOverlayRed != null)
                                    tileOverlayRed.remove();

                                // Create the gradient.
                                int[] greenGradient = {
                                        Color.rgb(0, 0, 0),
                                        Color.parseColor("#99ff99"),
                                        Color.parseColor("#7fff7f"),
                                        Color.parseColor("#66ff66"),
                                        Color.parseColor("#4cff4c"),
                                        Color.parseColor("#32ff32"),
                                        Color.parseColor("#19ff19"),
                                        Color.parseColor("#00ff00"),
                                };
                                int[] orangeGradient = {
                                        Color.rgb(0, 0, 0),
                                        Color.parseColor("#ffdb99"),
                                        Color.parseColor("#ffd27f"),
                                        Color.parseColor("#ffc966"),
                                        Color.parseColor("#ffc04c"),
                                        Color.parseColor("#ffb732"),
                                        Color.parseColor("#ffae19"),
                                        Color.parseColor("#ffa500"),
                                };
                                int[] redGradient = {
                                        Color.rgb(0, 0, 0),
                                        Color.parseColor("#ff9999"),
                                        Color.parseColor("#ff7f7f"),
                                        Color.parseColor("#ff6666"),
                                        Color.parseColor("#ff4c4c"),
                                        Color.parseColor("#ff3232"),
                                        Color.parseColor("#ff1919"),
                                        Color.parseColor("#ff0000"),
                                };

                                List<LatLng> heatmapGreenData = new ArrayList<LatLng>();
                                List<LatLng> heatmapOrangeData = new ArrayList<LatLng>();
                                List<LatLng> heatmapRedData = new ArrayList<LatLng>();

                                LatLngBounds.Builder builder = LatLngBounds.builder();
                                for (PavementSample sample : response.getSamples()) {
                                    builder.include(new LatLng(sample.getLatitudeInit(), sample.getLongitudeInit()));
                                    builder.include(new LatLng(sample.getLatitudeEnd(), sample.getLongitudeEnd()));

                                    if (sample.getVerticalAcceleration() < 1.48) {
                                        heatmapGreenData.add(new LatLng(sample.getLatitudeInit(), sample.getLongitudeInit()));
                                        heatmapGreenData.add(new LatLng(sample.getLatitudeEnd(), sample.getLongitudeEnd()));
                                    } else if (sample.getVerticalAcceleration() >= 1.48 && sample.getVerticalAcceleration() < 2.69) {
                                        heatmapOrangeData.add(new LatLng(sample.getLatitudeInit(), sample.getLongitudeInit()));
                                        heatmapOrangeData.add(new LatLng(sample.getLatitudeEnd(), sample.getLongitudeEnd()));
                                    } else {
                                        heatmapRedData.add(new LatLng(sample.getLatitudeInit(), sample.getLongitudeInit()));
                                        heatmapRedData.add(new LatLng(sample.getLatitudeEnd(), sample.getLongitudeEnd()));
                                    }
                                }

                                tileOverlayGreen = addHeatMap(heatmapGreenData, greenGradient);
                                tileOverlayOrange = addHeatMap(heatmapOrangeData, orangeGradient);
                                tileOverlayRed = addHeatMap(heatmapRedData, redGradient);

                                markers = Spot.addMarkers(map, response.getSpots(), rootView.getContext());

                                //final LatLngBounds bounds = builder.build();
                                //map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150));

                            } else {
                                AlertDialog alertDialog = new AlertDialog.Builder(rootView.getContext()).create();
                                alertDialog.setTitle("Ooops!");
                                alertDialog.setMessage(response.getDescription());
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }

                        }
                    });

                }
            });

        } catch(Exception e) {
            AlertDialog alertDialog = new AlertDialog.Builder(rootView.getContext()).create();
            alertDialog.setTitle("Ooops!");
            alertDialog.setMessage(e.getMessage());
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    private boolean needToRefresh(LatLng newNortheast, LatLng newSouthwest) {
        double marginCache = 0.01d;
        if(northeast == null || southwest == null) {
            northeast = new LatLng(newNortheast.latitude+marginCache,newNortheast.longitude+marginCache);
            southwest = new LatLng(newSouthwest.latitude-marginCache,newSouthwest.longitude-marginCache);
            return true;
        }
        boolean update = false;
        if(newNortheast.latitude > northeast.latitude || newSouthwest.latitude > northeast.latitude)
            update = true;
        if(newNortheast.latitude < southwest.latitude || newSouthwest.latitude < southwest.latitude)
            update = true;
        if(newNortheast.longitude > northeast.longitude || newSouthwest.longitude > northeast.longitude)
            update = true;
        if(newNortheast.longitude < southwest.longitude || newSouthwest.longitude < southwest.longitude)
            update = true;
        if(update) {
            northeast = new LatLng(newNortheast.latitude+marginCache,newNortheast.longitude+marginCache);
            southwest = new LatLng(newSouthwest.latitude-marginCache,newSouthwest.longitude-marginCache);
        }
        return update;
    }

    private TileOverlay addHeatMap(List<LatLng> list, int[] colors) {
        if(list != null && !list.isEmpty()) {
            float[] startPoints = {
                    0.2f, 0.4f, 0.6f, 0.8f, 1f, 1.2f, 1.4f, 1.6f
            };
            Gradient gradient = new Gradient(colors, startPoints);
            // Create a heat map tile provider, passing it the latlngs of the police stations.
            HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                    .data(list)
                    .gradient(gradient)
                    .radius(13)
                    .opacity(0.5)
                    .build();
            // Add a tile overlay to the map, using the heat map tile provider.
            return map.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
        }
        else
            return null;
    }

}