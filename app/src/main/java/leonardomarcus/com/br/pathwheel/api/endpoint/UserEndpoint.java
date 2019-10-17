package leonardomarcus.com.br.pathwheel.api.endpoint;

import android.os.AsyncTask;

import com.google.gson.Gson;

import leonardomarcus.com.br.pathwheel.api.request.AuthenticateRequest;
import leonardomarcus.com.br.pathwheel.api.response.AuthenticateResponse;
import leonardomarcus.com.br.pathwheel.api.response.Response;

/**
 * Created by leonardo on 11/04/19.
 */

public class UserEndpoint extends PathwheelApiClient {

    private AuthenticateUserListener authenticateUserListener;

    public void authenticate(AuthenticateRequest authenticateRequest, AuthenticateUserListener authenticateUserListener) {
        this.authenticateUserListener = authenticateUserListener;
        new AuthenticateAsyncTask().execute(new Gson().toJson(authenticateRequest));
    }

    private class AuthenticateAsyncTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                return doPost("/v1/user/authenticate", params[0]);
            } catch (Exception e) {
                e.printStackTrace();
                return new Gson().toJson(new Response(500, e.getMessage()));
            }
        }
        @Override
        protected void onPostExecute(String s) {
            try {
                AuthenticateResponse response = new Gson().fromJson(s, AuthenticateResponse.class);
                authenticateUserListener.onAuthenticateUser(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
