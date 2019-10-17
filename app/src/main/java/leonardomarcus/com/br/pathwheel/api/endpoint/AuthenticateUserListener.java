package leonardomarcus.com.br.pathwheel.api.endpoint;

import leonardomarcus.com.br.pathwheel.api.response.AuthenticateResponse;

/**
 * Created by leonardo on 11/04/19.
 */

public interface AuthenticateUserListener {
    void onAuthenticateUser(AuthenticateResponse response);
}
