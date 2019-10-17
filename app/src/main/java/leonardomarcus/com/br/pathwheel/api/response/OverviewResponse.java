package leonardomarcus.com.br.pathwheel.api.response;

import java.util.List;

import leonardomarcus.com.br.pathwheel.api.model.PavementSample;
import leonardomarcus.com.br.pathwheel.api.model.Spot;

/**
 * Created by leonardo on 15/07/19.
 */

public class OverviewResponse extends Response {
    List<PavementSample> samples;
    List<Spot> spots;

    public List<PavementSample> getSamples() {
        return samples;
    }

    public void setSamples(List<PavementSample> samples) {
        this.samples = samples;
    }

    public List<Spot> getSpots() {
        return spots;
    }

    public void setSpots(List<Spot> spots) {
        this.spots = spots;
    }

    @Override
    public String toString() {
        return "OverviewResponse{" +
                "samples=" + samples +
                ", spots=" + spots +
                '}';
    }
}
