package leonardomarcus.com.br.pathwheel.api.request;

import leonardomarcus.com.br.pathwheel.api.space.GeographicCoordinate;

/**
 * Created by leonardo on 18/07/19.
 */

public class RouteGoogleRequest extends Request {
    private GeographicCoordinate origin;
    private GeographicCoordinate destination;
    public GeographicCoordinate getOrigin() {
        return origin;
    }
    public void setOrigin(GeographicCoordinate origin) {
        this.origin = origin;
    }
    public GeographicCoordinate getDestination() {
        return destination;
    }
    public void setDestination(GeographicCoordinate destination) {
        this.destination = destination;
    }
    @Override
    public String toString() {
        return "RouteGoogleRequest [origin=" + origin + ", destination=" + destination + "]";
    }

}
