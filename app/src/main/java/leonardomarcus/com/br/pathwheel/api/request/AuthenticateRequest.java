package leonardomarcus.com.br.pathwheel.api.request;

/**
 * Created by leonardo on 27/08/18.
 */

public class AuthenticateRequest extends Request {
    private String login;
    private String secret;
    private String fcmToken;

    public String getLogin() {
        return login;
    }
    public void setLogin(String login) {
        this.login = login;
    }
    public String getSecret() {
        return secret;
    }
    public void setSecret(String secret) {
        this.secret = secret;
    }
    public String getFcmToken() {
        return fcmToken;
    }
    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }
    @Override
    public String toString() {
        return "AuthenticateRequest [login=" + login + ", secret=" + secret + ", fcmToken=" + fcmToken + ", uuid="
                + uuid + "]";
    }
}
