package leonardomarcus.com.br.pathwheel.fragment;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import leonardomarcus.com.br.pathwheel.R;
import leonardomarcus.com.br.pathwheel.api.endpoint.MappingEndpoint;
import leonardomarcus.com.br.pathwheel.api.endpoint.RouteMapListener;
import leonardomarcus.com.br.pathwheel.api.space.GeographicCoordinate;
import leonardomarcus.com.br.pathwheel.api.space.PavementSegment;
import leonardomarcus.com.br.pathwheel.api.model.Spot;
import leonardomarcus.com.br.pathwheel.api.request.RouteMapRequest;
import leonardomarcus.com.br.pathwheel.api.response.RouteMapResponse;
import leonardomarcus.com.br.pathwheel.service.PathwheelPreferences;
import leonardomarcus.com.br.pathwheel.view.MainActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class MappedRouteFragment extends Fragment implements OnMapReadyCallback {

    private View rootView;
    ProgressDialog progress = null;

    public MappedRouteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_mapped_route, container, false);

        MainActivity.getInstance().setPathwheelTitle("Rota Mapeada");

        MapView mapView = (MapView) rootView.findViewById(R.id.mapaViewRotaMapeada);
        mapView.onCreate(savedInstanceState);

        mapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mapView.getMapAsync(this);

        return rootView;
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(PathwheelPreferences.getLastKnownLocation(rootView.getContext()), 15));
        if (progress != null)
            this.progress.dismiss();
        else
            progress = new ProgressDialog(rootView.getContext());
        progress.setTitle("Aguarde");
        progress.setMessage("Mapeando rota...");
        progress.setCancelable(false);
        progress.show();

        final RouteMapRequest request = PathwheelPreferences.getRouteMapRequest(rootView.getContext());

        MappingEndpoint endpoint = new MappingEndpoint();
        endpoint.routeMap(request, new RouteMapListener() {
            @Override
            public void onRouteMapPathwheel(RouteMapResponse response) {
                Log.d("routemap req", response.toString());

                if(response.getCode() == 200) {

                    if(response.getCoordinateSamples().size() > 0) {
                        googleMap.addMarker(new MarkerOptions().position(new LatLng(response.getCoordinateSamples().get(0).getLatitude(),response.getCoordinateSamples().get(0).getLongitude()))
                                .title("ORIGEM")
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_origin))
                        );
                        googleMap.addMarker(new MarkerOptions().position(new LatLng(response.getCoordinateSamples().get(response.getCoordinateSamples().size()-1).getLatitude(),response.getCoordinateSamples().get(response.getCoordinateSamples().size()-1).getLongitude()))
                                .title("DESTINO")
                                .icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_marker_destination))
                        );
                    }
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (GeographicCoordinate geo : response.getCoordinateSamples()) {
                        builder.include(new LatLng(geo.getLatitude(), geo.getLongitude()));
                    }
                    LatLngBounds bounds = builder.build();
                    CameraUpdate camerUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                    for(PavementSegment segment : response.getPavementSegments()) {
                        int color = Color.parseColor("#0000ff");
                        if(segment.getVerticalAcceleration() > 0 && segment.getVerticalAcceleration() < 1.48)
                            color = Color.parseColor("#00FF00");
                        else if(segment.getVerticalAcceleration() >= 1.48 && segment.getVerticalAcceleration() < 5.89)
                            color = Color.parseColor("#FFA500");
                        else if(segment.getVerticalAcceleration() >= 5.89)
                        color = Color.parseColor("#FF0000");

                        List<LatLng> latLngs = new ArrayList<>();
                        latLngs.add(new LatLng(segment.getLatitudeInit(),segment.getLongitudeInit()));
                        latLngs.add(new LatLng(segment.getLatitudeEnd(),segment.getLongitudeEnd()));
                        googleMap.addPolyline(new PolylineOptions()
                                .addAll(latLngs)
                                .width(20)
                                .color(color)
                        );

                        if(segment.getSlopePercentage() > 8.33) {
                            googleMap.addMarker(new MarkerOptions().position(new LatLng(segment.getLatitudeInit(), segment.getLongitudeInit()))
                                    .title("SUBIDA")
                                    .snippet("Inclinação: " + String.format("%.2f", segment.getSlopePercentage()) + "%")
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.mipmap.ic_slope_up_yllw, 50, 50))));
                        }
                        else if(segment.getSlopePercentage() < -8.33) {
                            googleMap.addMarker(new MarkerOptions().position(new LatLng(segment.getLatitudeInit(), segment.getLongitudeInit()))
                                    .title("DESCIDA")
                                    .snippet("Declive: " + String.format("%.2f", segment.getSlopePercentage()) + "%")
                                    .icon(BitmapDescriptorFactory.fromBitmap(resizeMapIcons(R.mipmap.ic_slope_down_yllw, 50, 50))));
                        }
                    }

                    Spot.addMarkers(googleMap,response.getSpots(),rootView.getContext());

                    googleMap.moveCamera(camerUpdate);

                    progress.dismiss();
                    progress = null;
                }
                else {
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

    public Bitmap resizeMapIcons(int resourceId, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(rootView.getResources(),resourceId);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }
}