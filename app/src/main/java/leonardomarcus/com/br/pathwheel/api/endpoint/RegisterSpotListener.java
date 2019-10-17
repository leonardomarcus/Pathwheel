package leonardomarcus.com.br.pathwheel.api.endpoint;

import leonardomarcus.com.br.pathwheel.api.response.RegisterSpotResponse;

public interface RegisterSpotListener {
    void onRegisterSpot(RegisterSpotResponse response);
}
