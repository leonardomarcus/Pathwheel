package leonardomarcus.com.br.pathwheel.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

import leonardomarcus.com.br.pathwheel.api.endpoint.PavementSampleEndpoint;
import leonardomarcus.com.br.pathwheel.api.endpoint.RegisterPavementSampleListener;
import leonardomarcus.com.br.pathwheel.api.io.Logger;
import leonardomarcus.com.br.pathwheel.api.model.PavementSample;
import leonardomarcus.com.br.pathwheel.api.request.RegisterSampleRequest;
import leonardomarcus.com.br.pathwheel.api.response.Response;

public class PathwheelService extends Service implements LocationListener, SensorEventListener {

    private static PathwheelService instance = null;
    private static PathwheelServiceListener listener;

    //maximum GPS accuracy to register measurement (in meters)
    //accuracy is the precision radius of the location provided for the GPS
    private float locationAccuracyThresholdMax = 20;

    //minimum wheelchair speed to register measurement
    private float speedThresholdMin = 1;
    //maximum wheelchair speed to register measurement
    private float speedThresholdMax = 100;

    //near to ~200 samples per second
    private static final long BUFFER_VERTICAL_ACCELERATION_SIZE = 60000; //~5 minutes //12000; //~1 minutes
    //in milliseconds
    private static final long PANEL_REFRESH_INTERVAL = 500;

    /* The rate sensor events are delivered at.
    This is only a hint to the system.
    Events may be received faster or slower than the specified rate.
    Usually events are received faster.
    The value must be one of SENSOR_DELAY_NORMAL, SENSOR_DELAY_UI, SENSOR_DELAY_GAME,
    or SENSOR_DELAY_FASTEST or, the desired delay between events in microseconds.
    Specifying the delay in microseconds only works from Android 2.3 (API level 9) onwards.
    For earlier releases, you must use one of the SENSOR_DELAY_* constants. */
    private final int SENSOR_SAMPLING_PERIOD_US = SensorManager.SENSOR_DELAY_GAME; //~200 sample/s

    private SensorManager sensorManager;
    private Sensor linearAccelerationSensor;
    private Sensor gravitySensor;
    private Sensor stepCounterSensor;

    private boolean linearAccelerationSensorReady = false;
    private boolean gravitySensorReady = false;
    private float ax = 0;
    private float ay = 0;
    private float az = 0;

    private float gx = 0;
    private float gy = 0;
    private float gz = 0;

    private List<Double> verticalAccelerations = new ArrayList<>();

    private LocationManager locManager;

    private float speed = 0;
    private long lastLocationMillisTime = 0;

    private double lastVerticalAccelerationAvg = 0;

    private Location lastLocation = null;

    private long lastPanelRefreshMillisTime = 0;

    private int samplesCount = 0;
    private int locationUpdatesMinTime = 0;

    private PowerManager.WakeLock mWakeLock;

    private Handler handler;
    private boolean sendingData = false;

    private float stepCounterInit = -1;
    private float stepCounterNow = -1;

    private int travelModeId;

    public static PathwheelService getInstance() {
        return PathwheelService.instance;
    }

    public static void start(Activity activity, int locationUpdatesMinTime, int locationUpdatesMinDistance, float locationAccuracyThresholdMax, float speedThresholdMin, float speedThresholdMax, int travelModeId) {
        try {
            if (!PathwheelPreferences.isRunning(activity.getApplicationContext())) {

                Logger.debug(activity.getApplicationContext(), "service starting");

                PathwheelPreferences.setLocationUpdatesMinTime(activity.getApplicationContext(), locationUpdatesMinTime);
                PathwheelPreferences.setLocationUpdatesMinDistance(activity.getApplicationContext(), locationUpdatesMinDistance);
                PathwheelPreferences.setLocationAccuracyThresholdMax(activity.getApplicationContext(), locationAccuracyThresholdMax);
                PathwheelPreferences.setSpeedThresholdMin(activity.getApplicationContext(), speedThresholdMin);
                PathwheelPreferences.setSpeedThresholdMax(activity.getApplicationContext(), speedThresholdMax);
                PathwheelPreferences.setTravelModeId(activity.getApplicationContext(), travelModeId);

                //turn off vibration
                //AudioManager audioManager = (AudioManager) activity.getSystemService(AUDIO_SERVICE);
                //audioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);

                PathwheelPreferences.setRunning(activity.getApplicationContext(), true);
                Intent intent = new Intent(activity.getApplicationContext(), PathwheelService.class);
                activity.startService(intent);
                Logger.debug(activity.getApplicationContext(), "service started");
            }
        } catch(Exception e) {
            e.printStackTrace();
            Logger.debug(activity.getApplicationContext(), "erro: "+e.getMessage());
        }
    }

    public static boolean isRunning(Context context) {
        if(instance == null) {
            PathwheelPreferences.setRunning(context, false);
        }
        return PathwheelPreferences.isRunning(context);
    }

    public PathwheelService() {
        PathwheelService.instance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Logger.debug(getApplicationContext(), "onStartCommand");

            if (PathwheelPreferences.isRunning(getApplicationContext())) {

                handler = new Handler();

                acquireWakeLock();

                this.locationUpdatesMinTime = PathwheelPreferences.getLocationUpdatesMinTime(this);
                int locationUpdatesMinDistance = PathwheelPreferences.getLocationUpdatesMinDistance(this);
                this.locationAccuracyThresholdMax = PathwheelPreferences.getLocationAccuracyThresholdMax(this);
                this.speedThresholdMin = PathwheelPreferences.getSpeedThresholdMin(this);
                this.speedThresholdMax = PathwheelPreferences.getSpeedThresholdMax(this);

                this.travelModeId = PathwheelPreferences.getTravelModeId(this);


                this.lastLocationMillisTime = System.currentTimeMillis();
                this.locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
                LocationProvider locProvider = locManager.getProvider(LocationManager.GPS_PROVIDER);

                sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
                if ((linearAccelerationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)) == null) {
                    Logger.debug(getApplicationContext(), "Ooops! The device doesn´t have linear acceleration... ;(");
                    Toast.makeText(this, "Ooops! The device doesn´t have linear acceleration... ;(", Toast.LENGTH_SHORT).show();
                    stop();
                }
                if ((gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)) == null) {
                    Logger.debug(getApplicationContext(), "Ooops! The device doesn´t have gravity sensor... ;(");
                    Toast.makeText(this, "Ooops! The device doesn´t have gravity sensor... ;(", Toast.LENGTH_SHORT).show();
                    stop();
                }
                if ((stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)) == null) {
                    Logger.debug(getApplicationContext(), "Ooops! The device doesn´t have step counter sensor... ;(");
                    Toast.makeText(this, "Ooops! The device doesn´t have step counter sensor... ;(", Toast.LENGTH_SHORT).show();
                    stop();
                }

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return Service.START_NOT_STICKY;
                }
                this.locManager.requestLocationUpdates(locProvider.getName(), locationUpdatesMinTime, locationUpdatesMinDistance, this);

                samplesCount = 0;
                sensorManager.registerListener(this, linearAccelerationSensor, SENSOR_SAMPLING_PERIOD_US);
                sensorManager.registerListener(this, gravitySensor, SENSOR_SAMPLING_PERIOD_US);
                sensorManager.registerListener(this, stepCounterSensor, SENSOR_SAMPLING_PERIOD_US);

                Logger.debug(getApplicationContext(), "service is running");
                return Service.START_STICKY;
            } else {
                return Service.START_NOT_STICKY;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Logger.debug(getApplicationContext(), "erro: "+e.getMessage());
            return Service.START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.debug(getApplicationContext(),"onDestroy");
        locManager.removeUpdates(this);
        sensorManager.unregisterListener(this);
        releaseWakeLock();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void stop() {
        try {
            if (PathwheelPreferences.isRunning(getApplicationContext())) {
                Logger.debug(getApplicationContext(),"service stop");

                sensorManager.unregisterListener(this, linearAccelerationSensor);
                sensorManager.unregisterListener(this, gravitySensor);
                sensorManager.unregisterListener(this, stepCounterSensor);
                locManager.removeUpdates(this);
                PathwheelPreferences.setSampleRequests(getApplicationContext(),new ArrayList<RegisterSampleRequest>());
                PathwheelPreferences.setRunning(getApplicationContext(),false);
            }
        } catch(Throwable e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            Logger.debug(getApplicationContext(), "erro: "+e.getMessage());
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        try {
            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                if(stepCounterInit == -1)
                    stepCounterInit = event.values[0];
                stepCounterNow = event.values[0];
                if((stepCounterNow-stepCounterInit) < 0) {
                    stepCounterInit = stepCounterNow;
                }
                //Log.d("STEPS", "steps: "+((int)(stepCounterNow-stepCounterInit)));
            }
            if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
                ax = event.values[0];
                ay = event.values[1];
                az = event.values[2];
                linearAccelerationSensorReady = true;
            }
            if (event.sensor.getType() == Sensor.TYPE_GRAVITY) {
                gx = event.values[0];
                gy = event.values[1];
                gz = event.values[2];
                gravitySensorReady = true;
            }
            if(!linearAccelerationSensorReady && !gravitySensorReady) {
                Logger.debug(getApplicationContext(),"sensor not ready");
                return;
            }
            //discarting the firsts readings to wait the sensor to calibrate...
            if(samplesCount < 1000) {
                samplesCount++;
                return;
            }

            //projeção ortogonal do vetor "aceleração linear" na direção do vetor "gravidade":
            //https://www.youtube.com/watch?v=E2ULYsABr2Q
            //https://www.youtube.com/watch?v=sZHPSAKXij0
            double aceleracaoLinearEscalarGravidade = (ax*gx)+(ay*gy)+(az*gz);
            double normaAoQuadradoDaGravidade = Math.pow(gx,2)+Math.pow(gy,2)+Math.pow(gz,2);
            double projecaoX = (aceleracaoLinearEscalarGravidade/normaAoQuadradoDaGravidade)*gx;
            double projecaoY = (aceleracaoLinearEscalarGravidade/normaAoQuadradoDaGravidade)*gy;
            double projecaoZ = (aceleracaoLinearEscalarGravidade/normaAoQuadradoDaGravidade)*gz;

            final double verticalAcceleration =
                    Math.sqrt(Math.pow(projecaoX, 2) +
                            Math.pow(projecaoY, 2) +
                            Math.pow(projecaoZ, 2));

            if (Double.isNaN(verticalAcceleration) || Double.isInfinite(verticalAcceleration))
                return;

            this.verticalAccelerations.add(verticalAcceleration);
            if (this.verticalAccelerations.size() > BUFFER_VERTICAL_ACCELERATION_SIZE) {
                this.verticalAccelerations.clear();
                this.lastLocation = null;
                this.speed = 0f;
                this.lastLocationMillisTime = System.currentTimeMillis();
                Logger.debug(getApplicationContext(),"the av buffer is full: "+verticalAccelerations.size());
                return;
            }
            if (this.lastPanelRefreshMillisTime == 0)
                this.lastPanelRefreshMillisTime = System.currentTimeMillis();
            long now = System.currentTimeMillis();
            if (((now - this.lastPanelRefreshMillisTime)) >= PANEL_REFRESH_INTERVAL) {
                try {
                    if(lastLocation == null)
                        this.lastVerticalAccelerationAvg = verticalAcceleration;
                    else
                        this.lastVerticalAccelerationAvg = Statistics.meanRemovingOutliersUsingMedianAbsoluteDeviation(verticalAccelerations, 3);

                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (listener != null)
                                listener.onRefreshInfoPanel(lastLocation, speed, lastVerticalAccelerationAvg);
                        }
                    });

                    lastPanelRefreshMillisTime = System.currentTimeMillis();
                } catch(Exception e) {
                    Logger.debug(getApplicationContext(),"Problem on refresh panel: "+e.getMessage());
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.getMessage();
            Logger.debug(getApplicationContext(),"error onsensorchanged: "+e.getMessage());
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onLocationChanged(Location location) {
        try {
            //sensorManager.unregisterListener(this);
            sensorManager.unregisterListener(this, linearAccelerationSensor);
            sensorManager.unregisterListener(this, gravitySensor);
            long now = System.currentTimeMillis();

            Logger.debug(getApplicationContext(),"onLocationChanged: " + location.toString());
            PathwheelPreferences.setLastKnownLocation(getApplicationContext(),new LatLng(location.getLatitude(),location.getLongitude()));

            this.speed = 0;
            if (this.lastLocation != null && !verticalAccelerations.isEmpty()) {
                float elapsedTimeInSecs = ((float) (now - this.lastLocationMillisTime)) / 1000f;

                if((elapsedTimeInSecs*1000f) < (locationUpdatesMinTime-100)) {
                    Logger.debug(getApplicationContext(),"location update less then "+locationUpdatesMinTime+ "ms ("+elapsedTimeInSecs+")");
                    return;
                }

                this.speed = elapsedTimeInSecs > 0 ? (this.lastLocation.distanceTo(location) / elapsedTimeInSecs) * 3.6f : 0; //km/h

                try {
                    this.lastVerticalAccelerationAvg = Statistics.meanRemovingOutliersUsingMedianAbsoluteDeviation(verticalAccelerations, 3);
                } catch(Throwable e) {
                    Logger.debug(getApplicationContext(),"erro getting the mean: "+e.getMessage());
                    Log.d("verticalAccelerations", "erro getting the mean: "+e.getMessage());
                    this.lastVerticalAccelerationAvg = 0d;
                }

                //Log.d("service", "Speed: "+String.format("%.2f",speed)+"km/h accuracy: "+String.format("%.2f",location.getAccuracy())+"m elapsedTime: "+String.format("%.2f",elapsedTimeInSecs)+"s");
                Logger.debug(getApplicationContext(), "Speed: "+String.format("%.2f",speed)+"km/h accuracy: "+String.format("%.2f",location.getAccuracy())+"m elapsedTime: "+String.format("%.2f",elapsedTimeInSecs)+"s");
                if (this.speed >= speedThresholdMin
                        && this.speed <= speedThresholdMax
                        && location.getAccuracy() < locationAccuracyThresholdMax
                        ) {

                    PavementSample data = new PavementSample();
                    data.setElapsedTime((double) elapsedTimeInSecs);
                    data.setSpeed((double) speed);
                    data.setDistance((double) this.lastLocation.distanceTo(location));
                    data.setVerticalAcceleration(lastVerticalAccelerationAvg);
                    data.setAccuracy((double) location.getAccuracy());
                    data.setLatitudeInit(lastLocation.getLatitude());
                    data.setLongitudeInit(lastLocation.getLongitude());
                    data.setLatitudeEnd(location.getLatitude());
                    data.setLongitudeEnd(location.getLongitude());
                    data.setVerticalAccelerations(new ArrayList<Double>(verticalAccelerations));
                    data.setUser(PathwheelPreferences.getUser(getApplicationContext()));
                    data.setSteps((int)(stepCounterNow-stepCounterInit));
                    data.setTravelModeId(this.travelModeId);


                    Logger.debug(getApplicationContext(), "new data: " + data.toString());

                    RegisterSampleRequest request = new RegisterSampleRequest();
                    request.setSample(data);
                    request.setSmartDevice(android.os.Build.MANUFACTURER.toUpperCase() + " " + Build.MODEL);

                    List<RegisterSampleRequest> requests = PathwheelPreferences.getSampleRequests(getApplicationContext());
                    requests.add(request);
                    PathwheelPreferences.setSampleRequests(getApplicationContext(),requests);
                    sendData();

                }
                else {
                    Logger.debug(getApplicationContext(), "Location discarted! speed: "+String.format("%.2f",speed)+"km/h");
                }
            }


            this.lastLocationMillisTime = now;
            if (location.getAccuracy() < locationAccuracyThresholdMax) {
                this.lastLocation = location;
            }
            else {
                this.lastLocation = null;
                this.speed = 0;
                Logger.debug(getApplicationContext(), "Location ignored by accuracy");
            }

            stepCounterInit = stepCounterNow;
            this.verticalAccelerations.clear();

            //read vertical accelerations only if has an init location already
            if(lastLocation != null) {
                sensorManager.registerListener(this, linearAccelerationSensor, SENSOR_SAMPLING_PERIOD_US);
                sensorManager.registerListener(this, gravitySensor, SENSOR_SAMPLING_PERIOD_US);
            }

            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    if (listener != null)
                        listener.onRefreshInfoPanel(lastLocation, speed, lastVerticalAccelerationAvg);
                }
            });

        } catch(Throwable e) {
            e.printStackTrace();
            Logger.debug(getApplicationContext(), "erro onlocationchange: "+e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

    @Override
    public void onProviderEnabled(String provider) { }

    @Override
    public void onProviderDisabled(String provider) { }

    public static void setListener(PathwheelServiceListener listener) {
        PathwheelService.listener = listener;
    }

    public void cancel() {
        PathwheelPreferences.setRunning(getApplicationContext(), false);
        stopSelf();
    }

    public List<Double> getVerticalAccelerations() {
        return verticalAccelerations;
    }


    public float getElapsedTimeInSecs() {
        return ((float) (System.currentTimeMillis() - this.lastLocationMillisTime)) / 1000f;
    }

    @SuppressLint("InvalidWakeLockTag")
    public void acquireWakeLock() {
        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        releaseWakeLock();
        //Acquire new wake lock
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "PARTIAL_WAKE_LOCK");
        mWakeLock.acquire();
    }

    public void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            mWakeLock = null;
        }
    }

    private synchronized void sendData() {
        List<RegisterSampleRequest> requests = PathwheelPreferences.getSampleRequests(getApplicationContext());
        if(!sendingData && requests.size() > 0) {
            sendingData = true;
            Logger.debug(getApplicationContext(),"sending data...");
            handler.removeCallbacksAndMessages(null);
            final RegisterSampleRequest request = requests.get(0);
            PavementSampleEndpoint endpoit = new PavementSampleEndpoint();
            endpoit.register(request, new RegisterPavementSampleListener() {
                @Override
                public void onRegisterPavementSampleListener(Response response) {
                    Logger.debug(getApplicationContext(), "response register pavement sample: " + response.getDescription());
                    if (response.getCode() == 200) {
                        //Toast.makeText(getInstance(), "Obrigado! Você acaba de colaborar com o Pathwheel!", Toast.LENGTH_SHORT).show();
                        List<RegisterSampleRequest> requests = PathwheelPreferences.getSampleRequests(getApplicationContext());
                        requests.remove(0);
                        PathwheelPreferences.setSampleRequests(getApplicationContext(),requests);
                        sendingData = false;
                        if(requests.size() > 0)
                            sendData();
                    } else {
                        Logger.debug(getApplicationContext(), "retrying in 30s...");
                        sendingData = false;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sendData();
                            }
                        }, 30000);
                    }
                }
            });
        }
    }
}
