package leonardomarcus.com.br.pathwheel.fragment;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.BoundingBox;
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
import leonardomarcus.com.br.pathwheel.api.endpoint.MappingEndpoint;
import leonardomarcus.com.br.pathwheel.api.endpoint.RouteMapListener;
import leonardomarcus.com.br.pathwheel.api.model.Spot;
import leonardomarcus.com.br.pathwheel.api.request.RouteMapRequest;
import leonardomarcus.com.br.pathwheel.api.response.RouteMapResponse;
import leonardomarcus.com.br.pathwheel.api.space.PavementSegment;
import leonardomarcus.com.br.pathwheel.service.PathwheelPreferences;
import leonardomarcus.com.br.pathwheel.view.MainActivity;

public class OsmMappedRouteFragment extends Fragment {

    private View rootView;
    private MapView map = null;
    private ProgressDialog progress = null;

    private double latMax = -999999;
    private double latMin =  999999;
    private double lngMax = -999999;
    private double lngMin =  999999;


    public OsmMappedRouteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_osm_mapped_route, container, false);
        MainActivity.getInstance().setPathwheelTitle("Rota Mapeada");


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
                progress.dismiss();
                progress = null;
                Log.d("routemap req", response.toString());

                if(response.getCode() == 200) {

                    for(PavementSegment segment : response.getPavementSegments()) {
                        if(segment.getLatitudeInit() > latMax)
                            latMax = segment.getLatitudeInit();
                        if(segment.getLatitudeInit() < latMin)
                            latMin = segment.getLatitudeInit();
                        if(segment.getLongitudeInit() > lngMax)
                            lngMax = segment.getLongitudeInit();
                        if(segment.getLongitudeInit() < lngMin)
                            lngMin = segment.getLongitudeInit();

                        if(segment.getLatitudeEnd() > latMax)
                            latMax = segment.getLatitudeEnd();
                        if(segment.getLatitudeEnd() < latMin)
                            latMin = segment.getLatitudeEnd();
                        if(segment.getLongitudeEnd() > lngMax)
                            lngMax = segment.getLongitudeEnd();
                        if(segment.getLongitudeEnd() < lngMin)
                            lngMin = segment.getLongitudeEnd();

                        int color = Color.parseColor("#0000ff");
                        if(segment.getVerticalAcceleration() > 0 && segment.getVerticalAcceleration() < 1.48)
                            color = Color.parseColor("#00FF00");
                        else if(segment.getVerticalAcceleration() >= 1.48 && segment.getVerticalAcceleration() < 2.69)
                            color = Color.parseColor("#FFA500");
                        else if(segment.getVerticalAcceleration() >= 2.69)
                            color = Color.parseColor("#FF0000");
                        List<GeoPoint> latLngs = new ArrayList<>();
                        latLngs.add(new GeoPoint(segment.getLatitudeInit(),segment.getLongitudeInit()));
                        latLngs.add(new GeoPoint(segment.getLatitudeEnd(),segment.getLongitudeEnd()));
                        Polyline line = new Polyline();   //see note below!
                        line.setPoints(latLngs);
                        line.setColor(color);
                        line.setWidth(15);
                        map.getOverlayManager().add(line);
                        if(segment.getSlopePercentage() > 8.33) {
                            Marker slope = new Marker(map);
                            slope.setPosition(new GeoPoint(segment.getLatitudeInit(),segment.getLongitudeInit()));
                            slope.setTitle("SUBIDA");
                            slope.setSnippet("Inclinação: " + String.format("%.2f", segment.getSlopePercentage()) + "%");
                            Drawable d = new BitmapDrawable(rootView.getResources(), resizeMapIcons(R.mipmap.ic_slope_up_yllw, 50, 50));
                            slope.setIcon(d);
                            slope.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker, MapView mapView) {
                                    if(marker.isInfoWindowShown())
                                        marker.closeInfoWindow();
                                    else
                                        marker.showInfoWindow();
                                    return false;
                                }
                            });
                            map.getOverlayManager().add(slope);
                        }
                        else if(segment.getSlopePercentage() < -8.33) {
                            Marker slope = new Marker(map);
                            slope.setPosition(new GeoPoint(segment.getLatitudeInit(),segment.getLongitudeInit()));
                            slope.setTitle("DESCIDA");
                            slope.setSnippet("Declive: " + String.format("%.2f", segment.getSlopePercentage()) + "%");
                            Drawable d = new BitmapDrawable(rootView.getResources(), resizeMapIcons(R.mipmap.ic_slope_down_yllw, 50, 50));
                            slope.setIcon(d);
                            slope.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                                @Override
                                public boolean onMarkerClick(Marker marker, MapView mapView) {
                                    if(marker.isInfoWindowShown())
                                        marker.closeInfoWindow();
                                    else
                                        marker.showInfoWindow();
                                    return false;
                                }
                            });
                            map.getOverlayManager().add(slope);
                        }
                    }

                    Spot.addOsmMarkers(map,response.getSpots(),rootView.getContext());

                    if(response.getCoordinateSamples().size() > 0) {
                        Marker origin = new Marker(map);
                        origin.setPosition(new GeoPoint(response.getCoordinateSamples().get(0).getLatitude(),response.getCoordinateSamples().get(0).getLongitude()));
                        origin.setTitle("ORIGEM");
                        origin.setIcon(rootView.getContext().getResources().getDrawable(R.mipmap.ic_marker_origin));
                        origin.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker, MapView mapView) {
                                if(marker.isInfoWindowShown())
                                    marker.closeInfoWindow();
                                else
                                    marker.showInfoWindow();
                                return false;
                            }
                        });
                        map.getOverlayManager().add(origin);

                        Marker dest = new Marker(map);
                        dest.setPosition(new GeoPoint(response.getCoordinateSamples().get(response.getCoordinateSamples().size()-1).getLatitude(),response.getCoordinateSamples().get(response.getCoordinateSamples().size()-1).getLongitude()));
                        dest.setTitle("DESTINO");
                        dest.setIcon(rootView.getContext().getResources().getDrawable(R.mipmap.ic_marker_destination));
                        dest.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker, MapView mapView) {
                                if(marker.isInfoWindowShown())
                                    marker.closeInfoWindow();
                                else
                                    marker.showInfoWindow();
                                return false;
                            }
                        });
                        map.getOverlayManager().add(dest);
                    }

                    map.invalidate();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            final double padding = 0.0008;
                            BoundingBox boundingBox = new BoundingBox(latMax+padding,lngMax+padding,latMin-padding,lngMin-padding);
                            map.zoomToBoundingBox(boundingBox,true);
                        }
                    }, 100);
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


        return rootView;
    }

    public Bitmap resizeMapIcons(int resourceId, int width, int height){
        Bitmap imageBitmap = BitmapFactory.decodeResource(rootView.getResources(),resourceId);
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, false);
        return resizedBitmap;
    }

}
