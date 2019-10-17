package leonardomarcus.com.br.pathwheel.api.space;

import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by leonardo on 12/07/18.
 */

public class GeographicCoordinate {
    private double latitude;
    private double longitude;

    public GeographicCoordinate() {}

    public GeographicCoordinate(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
    public double getLongitude() {
        return longitude;
    }
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
    @Override
    public String toString() {
        return "GeographicCoordinate [latitude=" + latitude + ", longitude=" + longitude + "]";
    }

    public static double getDistanceInMeters(GeographicCoordinate a, GeographicCoordinate b) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(b.getLatitude()-a.getLatitude());
        double dLng = Math.toRadians(b.getLongitude()-a.getLongitude());
        double alpha = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(a.getLatitude())) * Math.cos(Math.toRadians(b.getLatitude())) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(alpha), Math.sqrt(1-alpha));
        double dist = (double) (earthRadius * c);
        return dist;
    }

    public static double getModule(GeographicCoordinate a, GeographicCoordinate b) {
        return Math.sqrt(
                Math.pow(b.getLongitude()-a.getLongitude(), 2)
                        +
                        Math.pow(b.getLatitude()-a.getLatitude(), 2)
        );
    }

    public static double getAngle(GeographicCoordinate a, GeographicCoordinate b) {
        if(b.getLongitude() == a.getLongitude() && b.getLatitude() > a.getLatitude())
            return Math.toRadians(90);
        if(b.getLongitude() == a.getLongitude() && b.getLatitude() < a.getLatitude())
            return Math.toRadians(270);
        if(b.getLongitude() > a.getLongitude() && b.getLatitude() == a.getLatitude())
            return Math.toRadians(0);
        if(b.getLongitude() < a.getLongitude() && b.getLatitude() == a.getLatitude())
            return Math.toRadians(180);
        if(b.getLongitude() == a.getLongitude() && b.getLatitude() == a.getLatitude())
            return Math.toRadians(0);

        double angle = Math.atan(
                (Math.abs(b.getLatitude()-a.getLatitude()))
                        /
                        (Math.abs(b.getLongitude()-a.getLongitude()))
        );
        if(b.getLongitude() < a.getLongitude() && b.getLatitude() > a.getLatitude())
            return Math.toRadians(180)-angle;
        else if(b.getLongitude() < a.getLongitude() && b.getLatitude() < a.getLatitude())
            return Math.toRadians(180)+angle;
        else if(b.getLongitude() > a.getLongitude() && b.getLatitude() < a.getLatitude())
            return Math.toRadians(360)-angle;
        else
            return angle;
    }

    public static GeographicCoordinate byReference(GeographicCoordinate reference, int distance, double angle) {

        //http://jsfiddle.net/dts67ran/268/

        //essa razao foi calculada experimentalmente pela distancia e rota pegas no google
        //double modulo = (double)(9.148333122843854E-6*(double)distancia);

        //cada grau de curvatura terrestre tem 111,12 quilômetros.
        double module = (double)(distance/111120d);

        double dLat = module*Math.sin(angle);
        double dLng = module*Math.cos(angle);
        GeographicCoordinate newCoordinate = new GeographicCoordinate(reference.getLatitude()+dLat,reference.getLongitude()+dLng);
        return newCoordinate;
    }

    public static List<GeographicCoordinate> listVerticesNearPolygon(GeographicCoordinate a, GeographicCoordinate b) {
        final int radius = 10;
        List<GeographicCoordinate> verticesCoordinates = new ArrayList<GeographicCoordinate>();
        double angle = GeographicCoordinate.getAngle(a, b); //Math.atan(Math.abs(b.getLatitude()-a.getLatitude())/Math.abs(b.getLongitude()-a.getLongitude()));

        verticesCoordinates.add(GeographicCoordinate.byReference(a, radius, angle+Math.toRadians(90)));
        verticesCoordinates.add(GeographicCoordinate.byReference(a, radius/2, angle+Math.toRadians(180)));
        verticesCoordinates.add(GeographicCoordinate.byReference(a, radius, angle-Math.toRadians(90)));

        verticesCoordinates.add(GeographicCoordinate.byReference(b, radius, angle-Math.toRadians(90)));
        verticesCoordinates.add(GeographicCoordinate.byReference(b, radius/2, angle));
        verticesCoordinates.add(GeographicCoordinate.byReference(b, radius, angle+Math.toRadians(90)));

        return verticesCoordinates;
    }

    public static List<GeoPoint> listVerticesNearPolygon(GeoPoint geoA, GeoPoint geoB, int radius) {
        GeographicCoordinate a = new GeographicCoordinate(geoA.getLatitude(),geoA.getLongitude());
        GeographicCoordinate b = new GeographicCoordinate(geoB.getLatitude(),geoB.getLongitude());
        //final int radius = 10;
        List<GeographicCoordinate> verticesCoordinates = new ArrayList<GeographicCoordinate>();
        double angle = GeographicCoordinate.getAngle(a, b); //Math.atan(Math.abs(b.getLatitude()-a.getLatitude())/Math.abs(b.getLongitude()-a.getLongitude()));

        verticesCoordinates.add(GeographicCoordinate.byReference(a, radius, angle+Math.toRadians(90)));
        //verticesCoordinates.add(GeographicCoordinate.byReference(a, radius/2, angle+Math.toRadians(180)));
        verticesCoordinates.add(GeographicCoordinate.byReference(a, radius, angle-Math.toRadians(90)));

        verticesCoordinates.add(GeographicCoordinate.byReference(b, radius, angle-Math.toRadians(90)));
        //verticesCoordinates.add(GeographicCoordinate.byReference(b, radius/2, angle));
        verticesCoordinates.add(GeographicCoordinate.byReference(b, radius, angle+Math.toRadians(90)));

        List<GeoPoint> pts = new ArrayList<>();
        for(GeographicCoordinate coordinate : verticesCoordinates)
            pts.add(new GeoPoint(coordinate.latitude,coordinate.longitude));

        return pts;
    }

    public static GeographicCoordinate parse(JSONObject jsonObject) throws Exception {
        GeographicCoordinate coordinate = new GeographicCoordinate();
        coordinate.setLatitude(jsonObject.getDouble("latitude"));
        coordinate.setLongitude(jsonObject.getDouble("longitude"));
        return coordinate;
    }
}
