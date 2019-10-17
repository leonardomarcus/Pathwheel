package leonardomarcus.com.br.pathwheel.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.maps.model.LatLng;

import leonardomarcus.com.br.pathwheel.R;
import leonardomarcus.com.br.pathwheel.api.endpoint.AuthenticateUserListener;
import leonardomarcus.com.br.pathwheel.api.endpoint.UserEndpoint;
import leonardomarcus.com.br.pathwheel.api.request.AuthenticateRequest;
import leonardomarcus.com.br.pathwheel.api.response.AuthenticateResponse;
import leonardomarcus.com.br.pathwheel.service.PathwheelPreferences;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_MILIS = 500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //set content view AFTER ABOVE sequence (to avoid crash)
        this.setContentView(R.layout.activity_splash);

        setContentView(R.layout.activity_splash);

        
        if(PathwheelPreferences.getLastKnownLocation(getApplicationContext()) == null) {
            //PathwheelMemory.setLastKnownLocation(getApplicationContext(), new LatLng(-12.9016241,-38.4198075));
            PathwheelPreferences.setLastKnownLocation(getApplicationContext(), new LatLng(-13.002433, -38.451021));
        }

        AuthenticateRequest authenticateRequest = PathwheelPreferences.getAuthenticateRequest(getApplicationContext());

        if(authenticateRequest == null) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                }
            }, SPLASH_MILIS);
        }
        else {
            final UserEndpoint userEndpoint = new UserEndpoint();
            userEndpoint.authenticate(authenticateRequest, new AuthenticateUserListener() {
                @Override
                public void onAuthenticateUser(AuthenticateResponse response) {
                    if (response.getCode() == 200) {
                        PathwheelPreferences.setUser(getApplicationContext(),response.getUser());
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }
                    else
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                }
            });
        }

    }
}
