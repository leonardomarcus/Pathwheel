package leonardomarcus.com.br.pathwheel.api.endpoint;

import leonardomarcus.com.br.pathwheel.api.response.FetchSpotResponse;

/**
 * Created by leonardo on 24/07/19.
 */

public interface FetchSpotListener {
    void onFetchSpot(FetchSpotResponse response);
}
