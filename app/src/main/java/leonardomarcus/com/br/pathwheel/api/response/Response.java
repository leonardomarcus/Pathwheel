package leonardomarcus.com.br.pathwheel.api.response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by leonardo on 12/07/18.
 */

public class Response {
    protected int code;
    protected String description;
    public Response() {}
    public Response(int code, String description) {
        this.code = code;
        this.description = description;
    }
    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    @Override
    public String toString() {
        return "Response [code=" + code + ", description=" + description + "]";
    }

    public static Response parse(String jsonString) {
        Response response = new Response();
        try {
            JSONObject json = new JSONObject(jsonString);
            response.setCode(json.getInt("code"));
            response.setDescription(json.getString("description"));
        } catch (JSONException e) {
            e.printStackTrace();
            response.setCode(-99);
            response.setDescription(e.getMessage());
        }
        return response;
    }
}
