package leonardomarcus.com.br.pathwheel.api.endpoint;

import leonardomarcus.com.br.pathwheel.api.response.RouteMapResponse;

/**
 * Created by leonardo on 16/07/19.
 */

public interface RouteMapListener {
    void onRouteMapPathwheel(RouteMapResponse response);
}
