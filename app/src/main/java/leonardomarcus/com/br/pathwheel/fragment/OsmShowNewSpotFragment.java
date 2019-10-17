package leonardomarcus.com.br.pathwheel.fragment;


import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.CustomZoomButtonsController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.ArrayList;
import java.util.List;

import leonardomarcus.com.br.pathwheel.R;
import leonardomarcus.com.br.pathwheel.api.model.Spot;
import leonardomarcus.com.br.pathwheel.service.PathwheelPreferences;


public class OsmShowNewSpotFragment extends Fragment {

    private View rootView;
    private MapView map;

    public OsmShowNewSpotFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_osm_show_new_spot, container, false);


        Spot spot = PathwheelPreferences.getSpot(rootView.getContext());

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

        GeoPoint startPoint = new GeoPoint(spot.getLatitude(), spot.getLongitude());
        mapController.setCenter(startPoint);

        List<Spot> spots = new ArrayList<Spot>();
        spots.add(spot);

        List<Marker> markers = Spot.addOsmMarkers(map,spots,rootView.getContext());
        if(markers.size() > 0) {
            markers.get(0).setSnippet("Novo ponto informativo!");
            markers.get(0).showInfoWindow();
            map.invalidate();
        }

        return rootView;
    }

}
