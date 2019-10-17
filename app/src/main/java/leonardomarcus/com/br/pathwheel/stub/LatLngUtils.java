package leonardomarcus.com.br.pathwheel.stub;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by leonardo on 05/09/18.
 */

public abstract class LatLngUtils {

    public static double getAngle(LatLng a, LatLng b) {
        if(b.longitude == a.longitude && b.latitude > a.latitude)
            return Math.toRadians(90);
        if(b.longitude == a.longitude && b.latitude < a.latitude)
            return Math.toRadians(270);
        if(b.longitude > a.longitude && b.latitude == a.latitude)
            return Math.toRadians(0);
        if(b.longitude < a.longitude && b.latitude == a.latitude)
            return Math.toRadians(180);
        if(b.longitude == a.longitude && b.latitude == a.latitude)
            return Math.toRadians(0);

        double angle = Math.atan(
                (Math.abs(b.latitude-a.latitude))
                        /
                        (Math.abs(b.longitude-a.longitude))
        );
        if(b.longitude < a.longitude && b.latitude > a.latitude)
            return Math.toRadians(180)-angle;
        else if(b.longitude < a.longitude && b.latitude < a.latitude)
            return Math.toRadians(180)+angle;
        else if(b.longitude > a.longitude && b.latitude < a.latitude)
            return Math.toRadians(360)-angle;
        else
            return angle;
    }

    public static LatLng byReference(LatLng reference, double distance, double angle) {

        //http://jsfiddle.net/dts67ran/268/

        //essa razao foi calculada experimentalmente pela distancia e rota pegas no google
        //double modulo = (double)(9.148333122843854E-6*(double)distancia);

        //cada grau de curvatura terrestre tem 111,12 quilômetros.
        double module = (double)(distance/111120d);

        double dLat = module*Math.sin(angle);
        double dLng = module*Math.cos(angle);
        LatLng newCoordinate = new LatLng(reference.latitude+dLat,reference.longitude+dLng);
        return newCoordinate;
    }
}
