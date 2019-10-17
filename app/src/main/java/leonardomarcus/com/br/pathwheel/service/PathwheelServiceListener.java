package leonardomarcus.com.br.pathwheel.service;

import android.location.Location;

import leonardomarcus.com.br.pathwheel.api.response.Response;

/**
 * Created by leonardo on 10/10/18.
 */

public interface PathwheelServiceListener {
    //void onPavementSampled(Location locationInit, Location locationEnd, double distance, double speed, double elapsedTime, double verticalAcceleration);
    void onRefreshInfoPanel(Location location, double speed, double verticalAcceleration);
    void onUploadPavementSamples(Response response);
}
