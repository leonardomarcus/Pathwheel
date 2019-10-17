package leonardomarcus.com.br.pathwheel.service;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import leonardomarcus.com.br.pathwheel.api.model.Spot;
import leonardomarcus.com.br.pathwheel.api.model.User;
import leonardomarcus.com.br.pathwheel.api.request.AuthenticateRequest;
import leonardomarcus.com.br.pathwheel.api.request.RegisterSampleRequest;
import leonardomarcus.com.br.pathwheel.api.request.RouteMapRequest;

/**
 * Created by leonardo on 10/10/18.
 */

public class PathwheelPreferences {

    //file name
    private static final String CONFIGURACOES = "PATHWHEELPREFERENCES";

    private static final String LAST_KNOWN_LOCATION_LATITUDE = "LAST_KNOWN_LOCATION_LATITUDE";
    private static final String LAST_KNOWN_LOCATION_LONGITUDE = "LAST_KNOWN_LOCATION_LONGITUDE";

    private static final String LOCATION_UPDATES_MIN_TIME = "LOCATION_UPDATES_MIN_TIME";
    private static final String LOCATION_UPDATES_MIN_DISTANCE = "LOCATION_UPDATES_MIN_DISTANCE";
    private static final String LOCATION_ACCURACY_THRESHOLD_MAX = "LOCATION_ACCURACY_THRESHOLD_MAX";
    private static final String SPEED_THRESHOLD_MIN = "SPEED_THRESHOLD_MIN";
    private static final String SPEED_THRESHOLD_MAX = "SPEED_THRESHOLD_MAX";

    private static final String IS_RUNNING = "IS_RUNNING";
    private static final String DESCRIPTION = "DESCRIPTION";

    private static final String USER = "USER";
    private static final String AUTHENTICATION_REQUEST = "AUTHENTICATION_REQUEST";

    private static final String ROUTE_MAP_REQUEST = "ROUTE_MAP_REQUEST";

    private static final String SPOT = "SPOT";

    private static final String REFRESH_OVERVIEW = "REFRESH_OVERVIEW";

    private static final String SAMPLE_REQUESTS = "SAMPLE_REQUESTS";

    private static final String TRAVEL_MODE_ID = "TRAVEL_MODE_ID";

    private static void save(Context context, String chave, String valor) {
        SharedPreferences sharedPreference = context.getSharedPreferences(CONFIGURACOES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putString(chave,valor);
        editor.apply();
    }

    private static String load(Context context, String chave) {
        SharedPreferences sp = context.getSharedPreferences(CONFIGURACOES, Context.MODE_PRIVATE);
        return sp.getString(chave,null);
    }

    public static void setLastKnownLocation(Context context, LatLng lastKnownLocation) {
        save(context, LAST_KNOWN_LOCATION_LATITUDE, lastKnownLocation == null ? "" : String.valueOf(lastKnownLocation.latitude));
        save(context, LAST_KNOWN_LOCATION_LONGITUDE, lastKnownLocation == null ? "" : String.valueOf(lastKnownLocation.longitude));
    }

    public static LatLng getLastKnownLocation(Context context) {
        try {
            String latitude = load(context, LAST_KNOWN_LOCATION_LATITUDE);
            String longitude = load(context, LAST_KNOWN_LOCATION_LONGITUDE);
            return new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));
        } catch(Exception e) {
            return null;
        }
    }

    public static void setLocationUpdatesMinTime(Context context, int value) {
        save(context, LOCATION_UPDATES_MIN_TIME, String.valueOf(value));
    }

    public static int getLocationUpdatesMinTime(Context context) {
        try {
            String value = load(context, LOCATION_UPDATES_MIN_TIME);
            return Integer.parseInt(value);
        } catch(Exception e) {
            return 5000;
        }
    }

    public static void setLocationUpdatesMinDistance(Context context, int value) {
        save(context, LOCATION_UPDATES_MIN_DISTANCE, String.valueOf(value));
    }

    public static int getLocationUpdatesMinDistance(Context context) {
        try {
            String value = load(context, LOCATION_UPDATES_MIN_DISTANCE);
            return Integer.parseInt(value);
        } catch(Exception e) {
            return 10;
        }
    }

    public static void setLocationAccuracyThresholdMax(Context context, float value) {
        save(context, LOCATION_ACCURACY_THRESHOLD_MAX, String.valueOf(value));
    }

    public static float getLocationAccuracyThresholdMax(Context context) {
        try {
            String value = load(context, LOCATION_ACCURACY_THRESHOLD_MAX);
            return Float.parseFloat(value);
        } catch(Exception e) {
            return 15;
        }
    }

    public static void setSpeedThresholdMin(Context context, float value) {
        save(context, SPEED_THRESHOLD_MIN, String.valueOf(value));
    }

    public static float getSpeedThresholdMin(Context context) {
        try {
            String value = load(context, SPEED_THRESHOLD_MIN);
            return Float.parseFloat(value);
        } catch(Exception e) {
            return 2;
        }
    }

    public static void setSpeedThresholdMax(Context context, float value) {
        save(context, SPEED_THRESHOLD_MAX, String.valueOf(value));
    }

    public static float getSpeedThresholdMax(Context context) {
        try {
            String value = load(context, SPEED_THRESHOLD_MAX);
            return Float.parseFloat(value);
        } catch(Exception e) {
            return 15;
        }
    }

    public static void setRunning(Context context, boolean value) {
        save(context, IS_RUNNING, String.valueOf(value));
    }

    public static boolean isRunning(Context context) {
        try {
            String value = load(context, IS_RUNNING);
            return value != null && value.isEmpty() ? false : Boolean.parseBoolean(value);
        } catch(Exception e) {
            return false;
        }
    }

    public static void setDescription(Context context, String value) {
        save(context, DESCRIPTION, String.valueOf(value));
    }

    public static String getDescription(Context context) {
        try {
            String value = load(context, DESCRIPTION);
            return value != null && value.isEmpty() ? "" : value;
        } catch(Exception e) {
            return "";
        }
    }


    public static void setUser(Context context, User user) {
        save(context, USER, user == null ? "" : new Gson().toJson(user));
    }

    public static AuthenticateRequest getAuthenticateRequest(Context context) {
        try {
            String value = load(context, AUTHENTICATION_REQUEST);
            return value != null && value.isEmpty() ? null : new Gson().fromJson(value, AuthenticateRequest.class);
        } catch(Exception e) {
            return null;
        }
    }

    public static void setAuthenticateRequest(Context context, AuthenticateRequest request) {
        save(context, AUTHENTICATION_REQUEST, request == null ? "" : new Gson().toJson(request));
    }

    public static User getUser(Context context) {
        try {
            String value = load(context, USER);
            return value != null && value.isEmpty() ? null : new Gson().fromJson(value, User.class);
        } catch(Exception e) {
            return null;
        }
    }


    public static void setRouteMapRequest(Context context, RouteMapRequest request) {
        save(context, ROUTE_MAP_REQUEST, request == null ? "" : new Gson().toJson(request));
    }

    public static RouteMapRequest getRouteMapRequest(Context context) {
        try {
            String value = load(context, ROUTE_MAP_REQUEST);
            return value != null && value.isEmpty() ? null : new Gson().fromJson(value, RouteMapRequest.class);
        } catch(Exception e) {
            return null;
        }
    }



    public static void setSpot(Context context, Spot spot) {
        save(context, SPOT, spot == null ? "" : new Gson().toJson(spot));
    }

    public static Spot getSpot(Context context) {
        try {
            String value = load(context, SPOT);
            return value != null && value.isEmpty() ? null : new Gson().fromJson(value, Spot.class);
        } catch(Exception e) {
            return null;
        }
    }

    public static void setRefreshOverview(Context context, boolean value) {
        save(context, REFRESH_OVERVIEW, String.valueOf(value));
    }

    public static boolean isRefreshOverview(Context context) {
        try {
            String value = load(context, REFRESH_OVERVIEW);
            return value == null || value.isEmpty() ? false : Boolean.parseBoolean(value);
        } catch(Exception e) {
            return false;
        }
    }

    public static void setSampleRequests(Context context, List<RegisterSampleRequest> requests) {
        save(context, SAMPLE_REQUESTS, new Gson().toJson(requests));
    }

    public static List<RegisterSampleRequest> getSampleRequests(Context context) {
        try {
            String value = load(context, SAMPLE_REQUESTS);
            if(value == null || value.isEmpty())
                return new ArrayList<RegisterSampleRequest>();
            else {
                Type listType = new TypeToken<ArrayList<RegisterSampleRequest>>(){}.getType();
                List<RegisterSampleRequest> yourClassList = new Gson().fromJson(value, listType);
                return yourClassList;
            }
        } catch(Exception e) {
            return new ArrayList<RegisterSampleRequest>();
        }
    }


    public static void setTravelModeId(Context context, int value) {
        save(context, TRAVEL_MODE_ID, String.valueOf(value));
    }

    public static int getTravelModeId(Context context) {
        try {
            String value = load(context, TRAVEL_MODE_ID);
            return value == null || value.isEmpty() ? 0 : Integer.parseInt(value);
        } catch(Exception e) {
            return 0;
        }
    }

}
