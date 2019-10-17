package leonardomarcus.com.br.pathwheel.view;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import leonardomarcus.com.br.pathwheel.R;
import leonardomarcus.com.br.pathwheel.api.crypto.MD5;
import leonardomarcus.com.br.pathwheel.api.endpoint.AuthenticateUserListener;
import leonardomarcus.com.br.pathwheel.api.endpoint.UserEndpoint;
import leonardomarcus.com.br.pathwheel.api.request.AuthenticateRequest;
import leonardomarcus.com.br.pathwheel.api.response.AuthenticateResponse;
import leonardomarcus.com.br.pathwheel.service.PathwheelPreferences;


public class LoginActivity extends AppCompatActivity {

    private final int ACCESS_FINE_LOCATION = 1;
    private final int WRITE_EXTERNAL_STORAGE = 2;
    private final int ACCESS_NOTIFICATION_POLICY = 3;

    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    private NotificationManager mNotificationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(R.mipmap.ic_pathwheel_ac);
            getSupportActionBar().setTitle("  Pathwheel");
        } catch (Exception e) {
            Log.d("showHomeEnabled", e.getMessage());
        }

        //keep screen activated
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //disable rotation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        //allow to set do not disturbe to the device
        /*mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        changeInterruptionFiler(NotificationManager.INTERRUPTION_FILTER_ALARMS);*/

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        Button buttonAnonymous = (Button) findViewById(R.id.button_login_anonymous);
        buttonAnonymous.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        finish();
                    }
                });

            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        requestForPermissions();

    }

    protected void changeInterruptionFiler(int interruptionFilter) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // If api level minimum 23
            /*
                boolean isNotificationPolicyAccessGranted ()
                    Checks the ability to read/modify notification policy for the calling package.
                    Returns true if the calling package can read/modify notification policy.
                    Request policy access by sending the user to the activity that matches the
                    system intent action ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS.

                    Use ACTION_NOTIFICATION_POLICY_ACCESS_GRANTED_CHANGED to listen for
                    user grant or denial of this access.

                Returns
                    boolean

            */
            // If notification policy access granted for this package
            if (mNotificationManager.isNotificationPolicyAccessGranted()) {
                /*
                    void setInterruptionFilter (int interruptionFilter)
                        Sets the current notification interruption filter.

                        The interruption filter defines which notifications are allowed to interrupt
                        the user (e.g. via sound & vibration) and is applied globally.

                        Only available if policy access is granted to this package.

                    Parameters
                        interruptionFilter : int
                        Value is INTERRUPTION_FILTER_NONE, INTERRUPTION_FILTER_PRIORITY,
                        INTERRUPTION_FILTER_ALARMS, INTERRUPTION_FILTER_ALL
                        or INTERRUPTION_FILTER_UNKNOWN.
                */

                // Set the interruption filter
                mNotificationManager.setInterruptionFilter(interruptionFilter);
            } else {
                /*
                    String ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS
                        Activity Action : Show Do Not Disturb access settings.
                        Users can grant and deny access to Do Not Disturb configuration from here.

                    Input : Nothing.
                    Output : Nothing.
                    Constant Value : "android.settings.NOTIFICATION_POLICY_ACCESS_SETTINGS"
                */
                // If notification policy access not granted for this package
                Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
                startActivity(intent);
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }
        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);

            final AuthenticateRequest authenticateRequest = new AuthenticateRequest();
            authenticateRequest.setLogin(email.trim());
            authenticateRequest.setSecret(MD5.encode(password.trim()));

            final UserEndpoint userEndpoint = new UserEndpoint();
            userEndpoint.authenticate(authenticateRequest, new AuthenticateUserListener() {
                @Override
                public void onAuthenticateUser(final AuthenticateResponse response) {

                    if (response.getCode() == 200) {
                        Log.d("login: ", response.getUser().toString());
                        PathwheelPreferences.setUser(getApplicationContext(), response.getUser());
                        PathwheelPreferences.setAuthenticateRequest(getApplicationContext(), authenticateRequest);

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            }
                        }, 1);

                    } else {
                        showProgress(false);
                        mPasswordView.setError(response.getDescription());
                        mPasswordView.requestFocus();
                    }
                }
            });

        }
    }

    private boolean isPasswordValid(String password) {
        return password.length() >= 6;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    private void requestForPermissions() {

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    ACCESS_FINE_LOCATION);
        }
        else if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE);
        }
        else if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_NOTIFICATION_POLICY},
                    ACCESS_NOTIFICATION_POLICY);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == ACCESS_FINE_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Você precisa permitir o uso do GPS para utilizar este aplicativo.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        if (requestCode == WRITE_EXTERNAL_STORAGE) {
            if (grantResults.length == 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Você precisa permitir a escrita de dados no armazenamento para utilizar este aplicativo.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        if (requestCode == ACCESS_NOTIFICATION_POLICY) {
            if (grantResults.length == 1 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Você precisa permitir o uso do 'Não Perturbe' para utilizar este aplicativo.", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        requestForPermissions();
    }


}

