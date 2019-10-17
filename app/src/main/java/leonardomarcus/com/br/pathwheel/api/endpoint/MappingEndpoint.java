package leonardomarcus.com.br.pathwheel.api.endpoint;

import android.os.AsyncTask;
import android.util.StringBuilderPrinter;

import com.google.gson.Gson;

import leonardomarcus.com.br.pathwheel.api.request.RouteGoogleRequest;
import leonardomarcus.com.br.pathwheel.api.request.RouteMapRequest;
import leonardomarcus.com.br.pathwheel.api.response.OverviewResponse;
import leonardomarcus.com.br.pathwheel.api.response.Response;
import leonardomarcus.com.br.pathwheel.api.response.RouteMapResponse;

public class MappingEndpoint extends PathwheelApiClient {

    private OverviewPavementTestListener overviewPavementTestListener;
    private RouteMapListener routeMapListener;

    public void overview(String northeast, String southwest, int travelModeId, OverviewPavementTestListener overviewPavementTestListener) {
        this.overviewPavementTestListener = overviewPavementTestListener;
        new OverviewAsyncTask().execute(northeast, southwest, String.valueOf(travelModeId));
    }

    public void routeMap(RouteMapRequest request, RouteMapListener routeMapListener) {
        this.routeMapListener = routeMapListener;
        new RouteMapAsyncTask().execute(new Gson().toJson(request));
    }

    public void routeGoogle(RouteGoogleRequest request, RouteMapListener routeMapListener) {
        this.routeMapListener = routeMapListener;
        new RouteGoogleAsyncTask().execute(new Gson().toJson(request));
    }

    private class OverviewAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                return doGet("/v1/mapping/overview?northeast="+params[0]+"&southwest="+params[1]+"&travelModeId="+params[2]);
            } catch (Exception e) {
                e.printStackTrace();
                return new Gson().toJson(new Response(500, e.getMessage()));
            }
        }
        @Override
        protected void onPostExecute(String s) {
            try {
                OverviewResponse response = new Gson().fromJson(s, OverviewResponse.class);
                overviewPavementTestListener.onOverviewPavementTest(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class RouteMapAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                return doPost("/v1/mapping/route/map", params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return new Gson().toJson(new Response(500, e.getMessage()));
            }
        }
        @Override
        protected void onPostExecute(String s) {
            try {
                RouteMapResponse response = new Gson().fromJson(s, RouteMapResponse.class);
                routeMapListener.onRouteMapPathwheel(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class RouteGoogleAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                return doPost("/v1/mapping/route/google", params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return new Gson().toJson(new Response(500, e.getMessage()));
            }
        }
        @Override
        protected void onPostExecute(String s) {
            try {
                RouteMapResponse response = new Gson().fromJson(s, RouteMapResponse.class);
                routeMapListener.onRouteMapPathwheel(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
