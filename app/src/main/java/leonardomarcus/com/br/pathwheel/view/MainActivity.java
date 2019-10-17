package leonardomarcus.com.br.pathwheel.view;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import leonardomarcus.com.br.pathwheel.R;
import leonardomarcus.com.br.pathwheel.api.model.User;
import leonardomarcus.com.br.pathwheel.fragment.AboutFragment;
import leonardomarcus.com.br.pathwheel.fragment.CollaborateFragment;
import leonardomarcus.com.br.pathwheel.fragment.OsmAddSpotFragment;
import leonardomarcus.com.br.pathwheel.fragment.OsmManualRouteFragment;
import leonardomarcus.com.br.pathwheel.fragment.OsmOverviewFragment;
import leonardomarcus.com.br.pathwheel.fragment.OsmShowNewSpotFragment;
import leonardomarcus.com.br.pathwheel.fragment.RouteByGoogleFragment;
import leonardomarcus.com.br.pathwheel.fragment.SelectRouteTypeFragment;
import leonardomarcus.com.br.pathwheel.service.PathwheelPreferences;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    public static final int OVERVIEW = 0;
    public static final int ROUTE = 1;
    public static final int COLLABORATE = 2;
    public static final int ABOUT = 3;
    public static final int LOGOFF = 4;

    private Fragment currentFragment;

    private static MainActivity instance;

    public static MainActivity getInstance() {
        return instance;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        instance = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_pathwheel_ac);
        getSupportActionBar().setTitle("  Overview");

        /*FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //keep screen activated
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //disable rotation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) == null) {
            Toast.makeText(this, "Ooops! The device doesn´t have linear acceleration... ;(", Toast.LENGTH_SHORT).show();
            finish();
        }
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY) == null) {
            Toast.makeText(this, "Ooops! The device doesn´t have gravity sensor... ;(", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.content);
        if (frameLayout.getChildAt(0) == null) {
            changeFragment(new OsmOverviewFragment(), OVERVIEW);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else if(currentFragment instanceof OsmAddSpotFragment)
            changeFragment(new OsmOverviewFragment(), OVERVIEW);
        else if(currentFragment instanceof OsmShowNewSpotFragment)
            changeFragment(new OsmOverviewFragment(), OVERVIEW);
        else if(currentFragment instanceof SelectRouteTypeFragment)
            changeFragment(new OsmOverviewFragment(), OVERVIEW);
        else if(currentFragment instanceof OsmManualRouteFragment)
            changeFragment(new SelectRouteTypeFragment(), ROUTE);
        else if(currentFragment instanceof RouteByGoogleFragment)
            changeFragment(new SelectRouteTypeFragment(), ROUTE);
        else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        User user = PathwheelPreferences.getUser(getApplicationContext());
        if(user != null) {
            Log.d("main user", user.toString());
            TextView nome = (TextView)findViewById(R.id.nav_textView_nome);
            if(nome != null)
                nome.setText(user.getFullName());
            TextView email = (TextView)findViewById(R.id.nav_textView_email);
            if(email != null)
                email.setText(user.getEmail());
        }
        TextView textViewVersion = (TextView) findViewById(R.id.navheader_textView_version);
        try {
            PackageInfo pInfo = getApplicationContext().getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), 0);
            String version = pInfo.versionName;
            //int verCode = pInfo.versionCode;

            textViewVersion.setText("v"+version);
        } catch(Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(),"get version failed: "+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentTransaction transaction = getFragmentManager().beginTransaction();

        if (id == R.id.nav_overview) {
            //currentFragment = new OverviewFragment();
            currentFragment = new OsmOverviewFragment();
            transaction.replace(R.id.content, currentFragment);
            transaction.commit();
        }
        else if (id == R.id.nav_route) {
            currentFragment = new SelectRouteTypeFragment();
            transaction.replace(R.id.content, currentFragment);
            transaction.commit();
        }
        else if (id == R.id.nav_crowdsensing) {
            currentFragment = new CollaborateFragment();
            transaction.replace(R.id.content, currentFragment);
            transaction.commit();
        }
        else if (id == R.id.nav_about) {
            currentFragment = new AboutFragment();
            transaction.replace(R.id.content, currentFragment);
            transaction.commit();
        }

        else if (id == R.id.nav_logoff) {
            PathwheelPreferences.setUser(getApplicationContext(),null);
            PathwheelPreferences.setAuthenticateRequest(getApplicationContext(),null);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                }
            }, 100);
            return true;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*@Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            currentFragment.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }*/

    public void setPathwheelTitle(String title) {
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_pathwheel_ac);
        getSupportActionBar().setTitle("  "+title);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        try {
            switch (requestCode) {
                case 1: {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(getApplicationContext(), "GNSS access granted! :)", Toast.LENGTH_SHORT).show();
                    } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                        Toast.makeText(getApplicationContext(), "Ooops! GNSS access is needed... ;(", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    return;
                }
            }
        } catch(Throwable e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "onRequestPermissionsResult: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            currentFragment.onRequestPermissionsResult(requestCode,permissions,grantResults);
        }
    }

    public void changeFragment(Fragment fragment) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        currentFragment = fragment;
        transaction.replace(R.id.content, currentFragment);
        transaction.commit();
    }

    public void changeFragment(Fragment fragment, int menu) {
        changeFragment(fragment);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(menu).setChecked(true);
    }
}
