package leonardomarcus.com.br.pathwheel.fragment;


import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import leonardomarcus.com.br.pathwheel.R;
import leonardomarcus.com.br.pathwheel.api.endpoint.MappingEndpoint;
import leonardomarcus.com.br.pathwheel.api.endpoint.RouteMapListener;
import leonardomarcus.com.br.pathwheel.api.space.GeographicCoordinate;
import leonardomarcus.com.br.pathwheel.api.request.RouteGoogleRequest;
import leonardomarcus.com.br.pathwheel.api.request.RouteMapRequest;
import leonardomarcus.com.br.pathwheel.api.response.RouteMapResponse;
import leonardomarcus.com.br.pathwheel.service.PathwheelPreferences;
import leonardomarcus.com.br.pathwheel.view.MainActivity;

public class RouteByGoogleFragment extends Fragment {

    private View rootView;
    private LatLng origin;
    private LatLng destination;
    private ProgressDialog progress = null;

    public RouteByGoogleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_route_by_google, container, false);

        MainActivity.getInstance().setPathwheelTitle("Rota Pelo Google");

        LatLng lastKnownLocation = PathwheelPreferences.getLastKnownLocation(rootView.getContext());
        LatLngBounds bounds = null;
        if (lastKnownLocation != null) {
           //bounds = new LatLngBounds(lastKnownLocation, lastKnownLocation);
            bounds = new LatLngBounds(new LatLng(-15.148512, -36.401483),new LatLng(-11.432390, -40.343066));
        }


        FragmentManager fm = getChildFragmentManager();
        PlaceAutocompleteFragment placeAutoCompleteOrigin = (PlaceAutocompleteFragment) fm.findFragmentById(R.id.place_autocomplete_google_origin);
        placeAutoCompleteOrigin.setHint("Origem");
        ((EditText)placeAutoCompleteOrigin.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(12.0f);

        if (bounds != null)
            placeAutoCompleteOrigin.setBoundsBias(bounds);
        placeAutoCompleteOrigin.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.d("Maps", "Place selected: " + place.getName());
                origin = place.getLatLng();
            }

            @Override
            public void onError(Status status) {
                Log.d("Maps", "An error occurred: " + status);
                AlertDialog alertDialog = new AlertDialog.Builder(rootView.getContext()).create();
                alertDialog.setTitle("Ooops!");
                alertDialog.setMessage(status.getStatusMessage());
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
        });

        PlaceAutocompleteFragment placeAutoCompleteDestination = (PlaceAutocompleteFragment) fm.findFragmentById(R.id.place_autocomplete_google_destination);
        placeAutoCompleteDestination.setHint("Destino");
        ((EditText)placeAutoCompleteDestination.getView().findViewById(R.id.place_autocomplete_search_input)).setTextSize(12.0f);

        if (bounds != null)
            placeAutoCompleteDestination.setBoundsBias(bounds);
        placeAutoCompleteDestination.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                destination = place.getLatLng();
            }

            @Override
            public void onError(Status status) {
                AlertDialog alertDialog = new AlertDialog.Builder(rootView.getContext()).create();
                alertDialog.setTitle("Ooops!");
                alertDialog.setMessage(status.getStatusMessage());
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();

            }
        });

        Button buttonOk = (Button) rootView.findViewById(R.id.button_ok_google_pesquisa);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (origin != null && destination != null) {
                    if (progress != null)
                        progress.dismiss();
                    else
                        progress = new ProgressDialog(rootView.getContext());
                    progress.setTitle("Aguarde");
                    progress.setMessage("Buscando rota...");
                    progress.setCancelable(false);
                    progress.show();

                    final RouteGoogleRequest request = new RouteGoogleRequest();
                    request.setOrigin(new GeographicCoordinate(origin.latitude, origin.longitude));
                    request.setDestination(new GeographicCoordinate(destination.latitude, destination.longitude));

                    MappingEndpoint endpoint = new MappingEndpoint();
                    endpoint.routeGoogle(request, new RouteMapListener() {
                        @Override
                        public void onRouteMapPathwheel(RouteMapResponse response) {
                            progress.dismiss();
                            progress = null;

                            if(response.getCode() == 200) {
                                RouteMapRequest routeMapRequest = new RouteMapRequest();
                                routeMapRequest.setCoordinates(response.getCoordinateSamples());
                                PathwheelPreferences.setRouteMapRequest(rootView.getContext(), routeMapRequest);

                                MainActivity.getInstance().changeFragment(new OsmMappedRouteFragment(), MainActivity.ROUTE);
                            }
                            else {
                                AlertDialog alertDialog = new AlertDialog.Builder(rootView.getContext()).create();
                                alertDialog.setTitle("Oooops!");
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
                else {
                    AlertDialog alertDialog = new AlertDialog.Builder(rootView.getContext()).create();
                    alertDialog.setTitle("Atenção");
                    alertDialog.setMessage("Digite uma Origem e Destino.");
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


        // Inflate the layout for this fragment
        return rootView;
    }

}
