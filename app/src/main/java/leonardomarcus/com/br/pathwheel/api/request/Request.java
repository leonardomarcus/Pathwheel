package leonardomarcus.com.br.pathwheel.api.request;

import java.util.UUID;

/**
 * Created by leonardo on 27/08/18.
 */

public class Request {

    protected String uuid;

    public Request() {
        this.uuid = UUID.randomUUID().toString();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        return "Request{" +
                "uuid='" + uuid + '\'' +
                '}';
    }
}
