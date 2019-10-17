package leonardomarcus.com.br.pathwheel.fragment;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

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
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

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

//exemplo de heatmap
//https://github.com/osmdroid/osmdroid/blob/master/OpenStreetMapViewer/src/main/java/org/osmdroid/samplefragments/data/HeatMap.java
public class OsmOverviewFragment extends Fragment {

    private View rootView;
    private MapView map = null;
    private Handler cameraChangeHandler = new Handler();

    private LatLng northeast = null;
    private LatLng southwest = null;
    private double lastZoom = 0;

    private MyLocationNewOverlay locationOverlay;
    private List<Marker> markers = new ArrayList<>();
    private List<Polygon> polygons = new ArrayList<>();
    private List<Polyline> polylines = new ArrayList<>();


    //colors and hexAlpha settings
    private final String hexAlpha = "#55";//"#55";
    private final String hexGreen = "00ff00";
    private final String hexRed = "FF0000";
    private final String hexOrange = "FFA500";

    public OsmOverviewFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_osm_overview, container, false);
        MainActivity.getInstance().setPathwheelTitle("Visão Geral");

        //handle permissions first, before map is created. not depicted here
        //load/initialize the osmdroid configuration, this can be done
        Context ctx = rootView.getContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's tile servers will get you banned based on this string
        map = (MapView) rootView.findViewById(R.id.mapOsm);
        map.setTileSource(TileSourceFactory.MAPNIK);
        //map.setBuiltInZoomControls(false);
        map.getZoomController().setVisibility(CustomZoomButtonsController.Visibility.NEVER);
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(ctx, map);
        mRotationGestureOverlay.setEnabled(true);
        map.setMultiTouchControls(true);
        map.getOverlays().add(mRotationGestureOverlay);

        locationOverlay = new MyLocationNewOverlay(map);
        locationOverlay.enableMyLocation();
        //locationOverlay.enableFollowLocation();

        //Bitmap bmp = BitmapFactory.decodeResource(rootView.getResources(),R.mipmap.ic_navigation);
        //locationOverlay.setDirectionArrow(bmp,bmp);

        map.getOverlayManager().add(locationOverlay);

        map.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, final int left, final int top, final int right, final int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                cameraChangeHandler.removeCallbacksAndMessages(null);
                cameraChangeHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("postDelayed","CAMERA CHANGE! Zoom: "+map.getZoomLevelDouble());
                        Log.d("pixels","top: "+top+" right: "+right+" left: "+left+" bottom: "+bottom);
                        Log.d("info","markers: "+markers.size()+" polylines: "+polylines.size());
                        Projection proj = map.getProjection();
                        GeoPoint topRight = (GeoPoint) proj.fromPixels(bottom, left);
                        GeoPoint bottomLeft = (GeoPoint) proj.fromPixels(top, right);
                        LatLng newNortheast = new LatLng(topRight.getLatitude(), topRight.getLongitude());
                        LatLng newSouthwest = new LatLng(bottomLeft.getLatitude(), bottomLeft.getLongitude());
                        //Log.d("northeast",newNortheast.toString());
                        //Log.d("southwest",newSouthwest.toString());
                        final boolean refresh = needToRefresh(newNortheast, newSouthwest);
                        if(!refresh) {
                            map.invalidate();
                            return;
                        }
                        refreshOverview();
                    }
                }, 200);
            }
        });

        Overlay onSingleTapUpOverlay = new Overlay() {
            @Override
            public boolean onSingleTapUp(MotionEvent e, MapView mapView) {
                Projection proj = mapView.getProjection();
                GeoPoint loc = (GeoPoint) proj.fromPixels((int)e.getX(), (int)e.getY());
                //Log.d("click", loc.toString());
                map.getController().animateTo(loc);
                return super.onSingleTapUp(e, mapView);
            }
        };
        map.getOverlayManager().add(onSingleTapUpOverlay);

        Overlay onDoubleTapOverlay = new Overlay() {
            @Override
            public boolean onDoubleTap(MotionEvent e, MapView mapView) {
                Projection proj = mapView.getProjection();
                GeoPoint loc = (GeoPoint) proj.fromPixels((int)e.getX(), (int)e.getY());

                map.clearAnimation();
                map.getController().animateTo(loc,(map.getZoomLevelDouble()+0.5d),400l);
                return false;
                //return super.onDoubleTap(e, mapView);
            }
        };
        map.getOverlayManager().add(onDoubleTapOverlay);



        Overlay onLongPressOverlay = new Overlay() {
            @Override
            public boolean onLongPress(MotionEvent e, MapView mapView) {
                Projection proj = mapView.getProjection();
                GeoPoint loc = (GeoPoint) proj.fromPixels((int)e.getX(), (int)e.getY());

                OsmAddSpotFragment osmAddSpotFragment = new OsmAddSpotFragment();
                Log.d("onLongPress", loc.toString());
                Bundle bundle = new Bundle();
                bundle.putString("latitude", String.valueOf(loc.getLatitude()));
                bundle.putString("longitude", String.valueOf(loc.getLongitude()));
                osmAddSpotFragment.setArguments(bundle);

                MainActivity.getInstance().changeFragment(osmAddSpotFragment,MainActivity.COLLABORATE);
                return super.onLongPress(e, mapView);
            }
        };
        map.getOverlayManager().add(onLongPressOverlay);



        IMapController mapController = map.getController();
        mapController.setZoom(20d);
        map.setMaxZoomLevel(20.3d);
        map.setMinZoomLevel(5d);

        LatLng latLng = PathwheelPreferences.getLastKnownLocation(rootView.getContext());
        mapController.setCenter(new GeoPoint(latLng.latitude,latLng.longitude));

        final Handler handler = new Handler();
        locationOverlay.runOnFirstFix(new Runnable() {
            @Override
            public void run() {
                //Log.d("TESTE", "runOnFirstFix: "+locationOverlay.getMyLocation());
                PathwheelPreferences.setLastKnownLocation(rootView.getContext(),new LatLng(locationOverlay.getMyLocation().getLatitude(),locationOverlay.getMyLocation().getLongitude()));
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        map.getController().setCenter(locationOverlay.getMyLocation());
                        FloatingActionButton floatingActionButton = (FloatingActionButton) rootView.findViewById(R.id.floatingActionButtonMyLocation);
                        floatingActionButton.setVisibility(View.VISIBLE);
                        floatingActionButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                map.getController().animateTo(locationOverlay.getMyLocation());
                            }
                        });
                    }
                });

            }
        });

        return rootView;
    }

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
        if(PathwheelPreferences.isRefreshOverview(rootView.getContext())) {
            PathwheelPreferences.setRefreshOverview(rootView.getContext(),false);
            refreshOverview();
        }
    }


    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    private boolean needToRefresh(LatLng newNortheast, LatLng newSouthwest) {
        double marginCache = 0.01d;
        if(northeast == null || southwest == null) {
            northeast = new LatLng(newNortheast.latitude+marginCache,newNortheast.longitude+marginCache);
            southwest = new LatLng(newSouthwest.latitude-marginCache,newSouthwest.longitude-marginCache);
            return true;
        }
        boolean update = false;

        if(Math.abs(map.getZoomLevelDouble()-lastZoom) > 3d) {
            Log.d("ZOOM", "REFRESH BY ZOOM");
            update = true;
        }
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
            lastZoom = map.getZoomLevelDouble();
        }
        return update;
    }

    private void refreshOverview() {
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
                            for(Marker marker : markers)
                                map.getOverlayManager().remove(marker);
                            markers.clear();

                            for(Polygon polygon : polygons)
                                map.getOverlayManager().remove(polygon);
                            polygons.clear();

                            for(Polyline polyline : polylines)
                                map.getOverlayManager().remove(polyline);
                            polylines.clear();

                            map.getOverlayManager().remove(locationOverlay);

                            for(PavementSample sample : response.getSamples()) {
                                List<GeoPoint> geoPoints = new ArrayList<>();
                                geoPoints.add(new GeoPoint(sample.getLatitudeInit(),sample.getLongitudeInit()));
                                geoPoints.add(new GeoPoint(sample.getLatitudeEnd(), sample.getLongitudeEnd()));

                                //overview with polygons
                                /*Polygon polygon = new Polygon(map);
                                if (sample.getVerticalAcceleration() < 1.48) {
                                    polygon.setFillColor(Color.parseColor(hexAlpha + hexGreen));
                                } else if (sample.getVerticalAcceleration() >= 1.48 && sample.getVerticalAcceleration() < 2.69) {
                                    polygon.setFillColor(Color.parseColor(hexAlpha + hexOrange));
                                } else{
                                    polygon.setFillColor(Color.parseColor(hexAlpha + hexRed));
                                }

                                polygon.setStrokeColor(polygon.getFillColor());
                                polygon.setStrokeWidth(0f);
                                polygon.setPoints(GeographicCoordinate.listVerticesNearPolygon(geoPoints.get(0),geoPoints.get(1),3));
                                map.getOverlayManager().add(polygon);

                                polygons.add(polygon);*/

                                //overview with polylines
                                Polyline line = new Polyline(map);
                                if (sample.getVerticalAcceleration() < 1.48) {
                                    line.setColor(Color.parseColor(hexAlpha + hexGreen));
                                } else if (sample.getVerticalAcceleration() >= 1.48 && sample.getVerticalAcceleration() < 2.69) {
                                    line.setColor(Color.parseColor(hexAlpha + hexOrange));
                                } else {
                                    line.setColor(Color.parseColor(hexAlpha + hexRed));
                                }
                                line.setWidth(20);
                                line.addPoint(new GeoPoint(sample.getLatitudeInit(), sample.getLongitudeInit()));
                                line.addPoint(new GeoPoint(sample.getLatitudeEnd(), sample.getLongitudeEnd()));
                                line.setOnClickListener(new Polyline.OnClickListener() {
                                    @Override
                                    public boolean onClick(Polyline polyline, MapView mapView, GeoPoint eventPos) {
                                        return true;
                                    }
                                });
                                map.getOverlayManager().add(line);
                                polylines.add(line);
                            }
                            markers = Spot.addOsmMarkers(map, response.getSpots(), rootView.getContext());

                            map.getOverlayManager().add(locationOverlay);
                            map.invalidate();

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



}
