package leonardomarcus.com.br.pathwheel.api.request;

import java.util.ArrayList;
import java.util.List;

import leonardomarcus.com.br.pathwheel.api.space.GeographicCoordinate;

/**
 * Created by leonardo on 16/07/19.
 */

public class RouteMapRequest extends Request {
    private List<GeographicCoordinate> coordinates = new ArrayList<GeographicCoordinate>();
    private Integer travelModeId = new Integer(1);

    public List<GeographicCoordinate> getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(List<GeographicCoordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public Integer getTravelModeId() {
        return travelModeId;
    }

    public void setTravelModeId(Integer travelModeId) {
        this.travelModeId = travelModeId;
    }

    @Override
    public String toString() {
        return "RouteMapRequest [coordinates=" + coordinates + ", travelModeId="+travelModeId+"]";
    }


}