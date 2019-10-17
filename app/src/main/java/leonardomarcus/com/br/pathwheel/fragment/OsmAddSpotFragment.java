package leonardomarcus.com.br.pathwheel.fragment;


import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.app.Fragment;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

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
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;

import leonardomarcus.com.br.pathwheel.R;
import leonardomarcus.com.br.pathwheel.api.endpoint.RegisterSpotListener;
import leonardomarcus.com.br.pathwheel.api.endpoint.SpotEndpoint;
import leonardomarcus.com.br.pathwheel.api.model.Spot;
import leonardomarcus.com.br.pathwheel.api.model.SpotType;
import leonardomarcus.com.br.pathwheel.api.request.RegisterSpotRequest;
import leonardomarcus.com.br.pathwheel.api.response.RegisterSpotResponse;
import leonardomarcus.com.br.pathwheel.service.PathwheelPreferences;
import leonardomarcus.com.br.pathwheel.view.MainActivity;

public class OsmAddSpotFragment extends Fragment {

    private View rootView;
    private MapView map = null;

    private final int PICK_IMAGE = 1;
    private Marker marker;
    private EditText editTextComment;
    private ImageView imageViewPicture;
    private ProgressDialog progress = null;

    public OsmAddSpotFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        rootView = inflater.inflate(R.layout.fragment_osm_add_spot, container, false);

        MainActivity.getInstance().setPathwheelTitle("Ponto Informativo");

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

        if(getArguments() != null) {
            String latitude = getArguments().getString("latitude");
            String longitude = getArguments().getString("longitude");
            Log.d("latitude", latitude);
            Log.d("longitude", longitude);
            if (latitude != null && longitude != null && !latitude.isEmpty() && !longitude.isEmpty())
                latLng = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        }

        GeoPoint startPoint = new GeoPoint(latLng.latitude, latLng.longitude);
        mapController.setCenter(startPoint);

        Spinner spinnerSpotType = (Spinner) rootView.findViewById(R.id.spinner_spots);
        String array_spinner[] = new String[3];
        array_spinner[0]="Alerta";
        array_spinner[1]="Perigo";
        array_spinner[2]="Barreira";

        ArrayAdapter adapter = new ArrayAdapter(rootView.getContext(), android.R.layout.simple_dropdown_item_1line, array_spinner);
        spinnerSpotType.setAdapter(adapter);
        spinnerSpotType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d("onItemSelected","id: "+id);
                if(id == 0) {
                    marker.setIcon(rootView.getResources().getDrawable(R.mipmap.ic_marker_alert));
                    marker.setTitle("ALERTA");
                    marker.setRelatedObject(SpotType.ALERT);
                }
                else if(id == 1) {
                    marker.setIcon(rootView.getResources().getDrawable(R.mipmap.ic_marker_danger));
                    marker.setTitle("PERIGO");
                    marker.setRelatedObject(SpotType.DANGER);
                }
                else if(id == 2) {
                    marker.setIcon(rootView.getResources().getDrawable(R.mipmap.ic_marker_blocked));
                    marker.setTitle("BARREIRA");
                    marker.setRelatedObject(SpotType.BARRIER);
                }
                marker.showInfoWindow();
                map.invalidate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        this.editTextComment = (EditText) rootView.findViewById(R.id.edittext_comment);

        this.imageViewPicture = (ImageView) rootView.findViewById(R.id.imageView_spot_picture);

        Button buttonAdd = (Button) rootView.findViewById(R.id.button_spot_add);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = editTextComment.getText().toString();
                if(comment.equals("")) {
                    //Toast.makeText(rootView.getContext(), "Escreva um comentário", Toast.LENGTH_SHORT).show();
                    AlertDialog alertDialog = new AlertDialog.Builder(rootView.getContext()).create();
                    alertDialog.setTitle("Atenção");
                    alertDialog.setMessage("Digite um comentário.");
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    return;
                }
                Spot spot = new Spot();
                spot.setLatitude(marker.getPosition().getLatitude());
                spot.setLongitude(marker.getPosition().getLongitude());
                spot.setComment(editTextComment.getText().toString());
                SpotType spotType = new SpotType();
                spotType.setId((int)marker.getRelatedObject());
                spot.setSpotType(spotType);
                spot.setUser(PathwheelPreferences.getUser(rootView.getContext()));
                if(imageViewPicture.getDrawable() != null) {
                    Bitmap bitmap = ((BitmapDrawable)imageViewPicture.getDrawable()).getBitmap();

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                    byte[] byteArray = byteArrayOutputStream .toByteArray();

                    spot.setPicture(Base64.encodeToString(byteArray, Base64.DEFAULT));
                }
                Log.d("spot", spot.toString());

                final RegisterSpotRequest request = new RegisterSpotRequest();
                request.setSpot(spot);
                SpotEndpoint endpoint = new SpotEndpoint();


                if (progress != null)
                    progress.dismiss();
                else
                    progress = new ProgressDialog(rootView.getContext());
                progress.setTitle("Aguarde");
                progress.setMessage("Registrando dados...");
                progress.setCancelable(false);
                progress.show();

                endpoint.register(request, new RegisterSpotListener() {
                    @Override
                    public void onRegisterSpot(RegisterSpotResponse response) {
                        Log.d("reg resp", response.toString());
                        if(response.getCode() == 200) {
                            Date date = new Date();
                            //response.getSpot().setRegistrationDate(new SimpleDateFormat("dd/mm/yyyy").format(date));

                            PathwheelPreferences.setSpot(rootView.getContext(),response.getSpot());

                            FragmentTransaction transaction = MainActivity.getInstance().getFragmentManager().beginTransaction();
                            //Fragment fragment = new ShowNewSpotFragment();
                            Fragment fragment = new OsmShowNewSpotFragment();
                            transaction.replace(R.id.content, fragment);
                            transaction.commit();

                        }
                        else {
                            Toast.makeText(rootView.getContext(), response.getDescription(), Toast.LENGTH_SHORT).show();
                        }

                        progress.dismiss();
                        progress = null;
                    }
                });
            }
        });


        Button buttonSelectPicture = (Button) rootView.findViewById(R.id.button_spot_cancel);
        buttonSelectPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
                getIntent.setType("image/*");

                Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                pickIntent.setType("image/*");

                Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] {pickIntent});

                startActivityForResult(chooserIntent, PICK_IMAGE);
            }
        });

        marker = new Marker(map);
        marker.setPosition(startPoint);
        marker.setIcon(rootView.getResources().getDrawable(R.mipmap.ic_marker_alert));
        marker.setDraggable(true);
        marker.setTitle("ALERTA");
        marker.setRelatedObject(SpotType.ALERT);
        marker.setSnippet("Clique ou arraste até o local desejado");
        marker.showInfoWindow();
        map.getOverlayManager().add(marker);

        Overlay overlay = new Overlay() {

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e, MapView mapView) {
                Projection proj = mapView.getProjection();
                GeoPoint loc = (GeoPoint) proj.fromPixels((int)e.getX(), (int)e.getY());
                Log.d("click", loc.toString());
                //map.getController().animateTo(loc);
                //map.getController().setCenter(loc);
                marker.setPosition(loc);
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
                    public void onMarkerDragEnd(Marker m) {
                        marker = m;
                        map.getController().setCenter(m.getPosition());
                        marker.showInfoWindow();
                        map.invalidate();
                    }

                    @Override
                    public void onMarkerDragStart(Marker marker) {

                    }
                });
                marker.showInfoWindow();
                map.invalidate();
                return super.onSingleTapUp(e, mapView);
            }
        };
        map.getOverlayManager().add(overlay);

        return rootView;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_IMAGE) {
            Log.d("result", "data: "+data);
            if(data != null) {
                try {
                    InputStream inputStream = rootView.getContext().getContentResolver().openInputStream(data.getData());
                    BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

                    Bitmap bmp = BitmapFactory.decodeStream(bufferedInputStream);

                    int width, height;

                    width = 640;
                    height = (bmp.getHeight()*(100*width/bmp.getWidth()))/100;

                    Bitmap resizedBmp = Bitmap.createScaledBitmap(bmp, width, height, false);

                    ImageView iv = (ImageView) rootView.findViewById(R.id.imageView_spot_picture);
                    iv.setImageBitmap(resizedBmp);

                } catch (Exception e) {
                    Toast.makeText(rootView.getContext(), "erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

}
