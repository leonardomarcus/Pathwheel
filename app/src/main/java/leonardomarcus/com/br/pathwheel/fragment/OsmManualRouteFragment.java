package leonardomarcus.com.br.pathwheel.fragment;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.ArrayList;
import java.util.List;

import leonardomarcus.com.br.pathwheel.R;
import leonardomarcus.com.br.pathwheel.api.request.RouteMapRequest;
import leonardomarcus.com.br.pathwheel.api.space.GeographicCoordinate;
import leonardomarcus.com.br.pathwheel.service.PathwheelPreferences;
import leonardomarcus.com.br.pathwheel.view.MainActivity;

public class OsmManualRouteFragment extends Fragment {


    private View rootView;
    private MapView map = null;
    private List<Marker> markers = new ArrayList<>();
    private List<Polyline> polylines = new ArrayList<>();

    public OsmManualRouteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        MainActivity.getInstance().setPathwheelTitle("Rota Manual");

        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_osm_manual_route, container, false);

        Context ctx = rootView.getContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        map = (MapView) rootView.findViewById(R.id.mapOsm);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(ctx, map);
        mRotationGestureOverlay.setEnabled(true);
        map.getOverlays().add(mRotationGestureOverlay);
        map.setMultiTouchControls(true);
        IMapController mapController = map.getController();
        mapController.setZoom(20d);
        map.setMaxZoomLevel(20.3d);
        map.setMinZoomLevel(5d);
        LatLng latLng = PathwheelPreferences.getLastKnownLocation(rootView.getContext());
        GeoPoint startPoint = new GeoPoint(latLng.latitude, latLng.longitude);
        mapController.setCenter(startPoint);

        Overlay dlbTap = new Overlay() {
            @Override
            public boolean onDoubleTap(MotionEvent e, MapView mapView) {
                Projection proj = mapView.getProjection();
                GeoPoint loc = (GeoPoint) proj.fromPixels((int)e.getX(), (int)e.getY());
                map.clearAnimation();
                map.getController().animateTo(loc,(map.getZoomLevelDouble()+0.5d),400l);
                return false;
            }
        };
        map.getOverlayManager().add(dlbTap);

        Overlay overlay = new Overlay() {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
                Projection proj = mapView.getProjection();
                GeoPoint loc = (GeoPoint) proj.fromPixels((int)e.getX(), (int)e.getY());
                Log.d("click", loc.toString());
                //map.getController().animateTo(loc);
                //map.getController().setCenter(loc);
                Marker marker = new Marker(map);
                marker.setPosition(loc);
                marker.setTitle("Ponto da rota");
                marker.setAnchor(org.osmdroid.views.overlay.Marker.ANCHOR_CENTER, org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM);
                marker.setDraggable(true);
                marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker, MapView mapView) {
                        return false;
                    }
                });
                marker.setOnMarkerDragListener(new Marker.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDrag(Marker marker) {

                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        Log.d("OPA", "DRAG ENDED!");
                        refreshPolylines();
                        map.invalidate();
                    }

                    @Override
                    public void onMarkerDragStart(Marker marker) {

                    }
                });
                markers.add(marker);
                refreshPolylines();

                map.invalidate();

                return super.onSingleTapUp(e, mapView);
            }
        };
        map.getOverlayManager().add(overlay);

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
                        geo.setLatitude(m.getPosition().getLatitude());
                        geo.setLongitude(m.getPosition().getLongitude());
                        request.getCoordinates().add(geo);
                    }
                    PathwheelPreferences.setRouteMapRequest(rootView.getContext(),request);

                    FragmentTransaction transaction = MainActivity.getInstance().getFragmentManager().beginTransaction();
                    Fragment fragment = new OsmMappedRouteFragment();
                    transaction.replace(R.id.content, fragment);
                    transaction.commit();
                }
            }
        });

        return rootView;
    }

    private void refreshPolylines() {
        for(Polyline polyline : polylines)
            map.getOverlayManager().remove(polyline);
        for(Marker marker : markers)
            map.getOverlayManager().remove(marker);

        List<GeoPoint> geoPoints = new ArrayList<>();

        for(Marker marker : markers)
            geoPoints.add(marker.getPosition());

        Polyline line = new Polyline();   //see note below!
        line.setPoints(geoPoints);
        polylines.add(line);
        map.getOverlayManager().add(line);

        for(Marker marker : markers)
            map.getOverlayManager().add(marker);
    }

}
