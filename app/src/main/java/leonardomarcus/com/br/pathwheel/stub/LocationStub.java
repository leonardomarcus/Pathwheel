package leonardomarcus.com.br.pathwheel.stub;

import android.app.Activity;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class LocationStub {

    private Activity activity;
    private List<Location> locations = new ArrayList<>();
    private LocationStubListener listener;
    private int index = 0;
    private Timer timer;
    private Random rand = new Random();

    public LocationStub(Activity activity, List<LatLng> points) {
        this.activity = activity;
        for(int i=0; i<points.size();i++) {
            Location location = new Location("");
            location.setLatitude(points.get(i).latitude);
            location.setLongitude(points.get(i).longitude);
            location.setAccuracy(15f);
            if(i==0)
                location.setBearing(0f);
            else {
                float[] results = new float[1];
                location.setBearing((float)Math.toDegrees(LatLngUtils.getAngle(points.get(i-1), points.get(i))));
                Location.distanceBetween(points.get(i-1).latitude,points.get(i-1).longitude,points.get(i).latitude,points.get(i).longitude,results);
                double distance = results[0];
                if(distance > 5d) {
                    double sample = distance / 5d;
                    double sumDistance = 0;
                    while (sumDistance < (distance - sample)) {
                        LatLng latLng = LatLngUtils.byReference(
                                new LatLng(locations.get(locations.size() - 1).getLatitude(), locations.get(locations.size() - 1).getLongitude()),
                                sample,
                                LatLngUtils.getAngle(
                                        new LatLng(locations.get(locations.size() - 1).getLatitude(), locations.get(locations.size() - 1).getLongitude()),
                                        new LatLng(location.getLatitude(), location.getLongitude())
                                )
                        );
                        Location locationSample = new Location("");
                        locationSample.setLatitude(latLng.latitude);
                        locationSample.setLongitude(latLng.longitude);
                        locationSample.setAccuracy(15f);
                        locationSample.setBearing(location.getBearing());
                        locations.add(locationSample);
                        sumDistance += sample;
                    }
                }
            }
            locations.add(location);
        }
    }

    public Location getStartPosition() {
        return locations.get(0);
    }

    public void run(final LocationStubListener listener) {
        this.listener = listener;
        timer = new Timer();
        timer.schedule(new StubTimerTask(),1000);
    }

    private class StubTimerTask extends TimerTask {

        @Override
        public void run() {
           activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    listener.onLocationChanged(locations.get(index));
                    index++;
                    if(index < locations.size()) {
                        timer = new Timer();
                        timer.schedule(new StubTimerTask(), rand.nextInt(101)+950);
                    }
                }
            });
        }
    }
}
