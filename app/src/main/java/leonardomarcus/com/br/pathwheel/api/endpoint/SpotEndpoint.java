package leonardomarcus.com.br.pathwheel.api.endpoint;

import android.os.AsyncTask;

import com.google.gson.Gson;

import leonardomarcus.com.br.pathwheel.api.request.RegisterSpotRequest;
import leonardomarcus.com.br.pathwheel.api.request.ReportSpotRequest;
import leonardomarcus.com.br.pathwheel.api.response.FetchSpotResponse;
import leonardomarcus.com.br.pathwheel.api.response.RegisterSpotResponse;
import leonardomarcus.com.br.pathwheel.api.response.Response;

public class SpotEndpoint extends PathwheelApiClient {

    private RegisterSpotListener registerSpotListener;
    private FetchSpotListener fetchSpotListener;
    private ReportSpotListener reportSpotListener;

    public void register(RegisterSpotRequest request, RegisterSpotListener registerSpotListener) {
        this.registerSpotListener = registerSpotListener;
        new RegisterAsyncTask().execute(new Gson().toJson(request));
    }

    public void fetch(Long id, FetchSpotListener fetchSpotListener) {
        this.fetchSpotListener = fetchSpotListener;
        new FetchAsyncTask().execute(String.valueOf(id));
    }

    public void report(ReportSpotRequest request, ReportSpotListener reportSpotListener) {
        this.reportSpotListener = reportSpotListener;
        new ReportSpotAsyncTask().execute(new Gson().toJson(request));
    }

    private class RegisterAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                return doPost("/v1/spot/register", params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return new Gson().toJson(new Response(500, e.getMessage()));
            }
        }
        @Override
        protected void onPostExecute(String s) {
            try {
                RegisterSpotResponse response = new Gson().fromJson(s, RegisterSpotResponse.class);
                registerSpotListener.onRegisterSpot(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class FetchAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                return doGet("/v1/spot/fetch/"+params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return new Gson().toJson(new Response(500, e.getMessage()));
            }
        }
        @Override
        protected void onPostExecute(String s) {
            try {
                FetchSpotResponse response = new Gson().fromJson(s, FetchSpotResponse.class);
                fetchSpotListener.onFetchSpot(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private class ReportSpotAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                return doPost("/v1/spot/report", params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return new Gson().toJson(new Response(500, e.getMessage()));
            }
        }
        @Override
        protected void onPostExecute(String s) {
            try {
                Response response = new Gson().fromJson(s, Response.class);
                reportSpotListener.onReportSpot(response);
            } catch (Exception e) {
                e.printStackTrace();
                reportSpotListener.onReportSpot(new Response(500,e.getMessage()));
            }
        }
    }
}
