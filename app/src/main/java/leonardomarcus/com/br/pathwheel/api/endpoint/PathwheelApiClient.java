package leonardomarcus.com.br.pathwheel.api.endpoint;

import android.util.Log;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class PathwheelApiClient {

    protected final String WS_URL = "http://189.43.132.218:8067/PathwheelApi/api";
    private final String API_KEY = "1:3iwJo0iWV!VcR";

    private final long CONNECT_TIMEOUT = 30;
    private final long READ_TIMEOUT = 30;

    private Request.Builder getRequestBuilder(String endpoint) {
        String url = WS_URL+endpoint+ (endpoint.contains("?") ? "&" : "?")+"key="+API_KEY;
        Log.d("PathwheelApiClient", "URL: "+url);

        Request.Builder requestBuilder = new Request.Builder()
                .url(url);

        return requestBuilder;
    }

    protected String doGet(String endpoint) throws IOException {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS); //connect timeout
        client.setReadTimeout(READ_TIMEOUT, TimeUnit.SECONDS); //socket timeout
        Request request = getRequestBuilder(endpoint)
                .build();
        Response response = client.newCall(request).execute();
        String jsonResponse = response.body().string();
        return jsonResponse;
    }

    protected String doPost(String endpoint, String contentBody) throws IOException {

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, contentBody);

        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS); //connect timeout
        client.setReadTimeout(READ_TIMEOUT, TimeUnit.SECONDS); //socket timeout
        Request request = getRequestBuilder(endpoint)
                .post(body) //POST
                .build();

        Response response = client.newCall(request).execute();
        String jsonResponse = response.body().string();
        return jsonResponse;
    }

    protected String doPut(String endpoint, String contentBody) throws IOException {

        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, contentBody);

        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS); //connect timeout
        client.setReadTimeout(READ_TIMEOUT, TimeUnit.SECONDS); //socket timeout
        Request request = getRequestBuilder(endpoint)
                .put(body) //PUT
                .build();

        Response response = client.newCall(request).execute();
        String jsonResponse = response.body().string();
        return jsonResponse;
    }

    protected String doDelete(String endpoint) throws IOException {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS); //connect timeout
        client.setReadTimeout(READ_TIMEOUT, TimeUnit.SECONDS); //socket timeout
        Request request = getRequestBuilder(endpoint)
                .delete()
                .build();
        Response response = client.newCall(request).execute();
        String jsonResponse = response.body().string();
        return jsonResponse;
    }


    protected String doPostForm(String endpoint, FormEncodingBuilder formEncodingBuilder) throws Exception {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS); //connect timeout
        client.setReadTimeout(READ_TIMEOUT, TimeUnit.SECONDS); //socket timeout
        Request.Builder builder = new Request.Builder();
        builder.url(endpoint);
        RequestBody body = formEncodingBuilder.build();
        builder.post(body);
        Request request = builder.build();
        Response response = client.newCall(request).execute();
        String jsonDeResposta = response.body().string();
        Log.d("executarPost", jsonDeResposta);
        return jsonDeResposta;
    }

}
