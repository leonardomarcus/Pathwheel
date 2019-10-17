package leonardomarcus.com.br.pathwheel.api.response;

import leonardomarcus.com.br.pathwheel.api.model.Spot;

public class FetchSpotResponse extends Response {
    private Spot spot;

    public Spot getSpot() {
        return spot;
    }

    public void setSpot(Spot spot) {
        this.spot = spot;
    }

    @Override
    public String toString() {
        return "FetchSpotResponse{" +
                "code=" + super.code +
                " ,description=" + super.description +
                ", spot=" + spot +
                '}';
    }
}