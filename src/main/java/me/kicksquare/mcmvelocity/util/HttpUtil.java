package me.kicksquare.mcmvelocity.util;

import com.squareup.okhttp.*;
import me.kicksquare.mcmvelocity.MCMVelocity;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class HttpUtil {

    private static final OkHttpClient client = new OkHttpClient();
    private static MCMVelocity plugin = MCMVelocity.getPlugin();

    /**
     * @param url        The URL to send the request to
     * @param bodyString The body of the request
     * @param headers    2D array of Strings for the header names and values
     * @return The response body as a CompletableFuture
     */
    public static CompletableFuture<String> makeAsyncPostRequest(String url, String bodyString, String[][] headers) {
        if (bodyString == null) {
            bodyString = "";
        }
        CompletableFuture<String> postRequestFuture = new CompletableFuture<>();

        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(mediaType, bodyString);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        if (headers != null) {
            for (String[] header : headers) {
                request = request.newBuilder().addHeader(header[0], header[1]).build();
            }
        }

        callRequest(request).thenAccept(postRequestFuture::complete).exceptionally(e -> {
            postRequestFuture.completeExceptionally(e);
            throw new RuntimeException(e);
        });

        return postRequestFuture;
    }

    /**
     * @param url     The URL to send the request to
     * @param headers 2D array of Strings for the header names and values
     * @return The response body as a CompletableFuture
     */
    public static CompletableFuture<String> makeAsyncGetRequest(String url, String[][] headers) {
        System.out.println("Making async get request to " + url);

        CompletableFuture<String> getRequestFuture = new CompletableFuture<>();
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Content-Type", "application/json")
                .build();

        if (headers != null) {
            for (String[] header : headers) {
                request = request.newBuilder().addHeader(header[0], header[1]).build();
            }
        }

        callRequest(request).thenAccept(getRequestFuture::complete).exceptionally(e -> {
            getRequestFuture.completeExceptionally(e);
            throw new RuntimeException(e);
        });

        return getRequestFuture;
    }

    private static CompletableFuture<String> callRequest(Request request) {
        CompletableFuture<String> callRequestFuture = new CompletableFuture<>();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Request request, IOException e) {
                System.out.println("Request failed while making async request with request body " + request.body());
                callRequestFuture.completeExceptionally(e);
                throw new RuntimeException(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                String responseBody = response.body().string();
                System.out.println("Async request successful! Response body: " + responseBody);
                callRequestFuture.complete(responseBody);
            }
        });

        return callRequestFuture;
    }

    public static String[][] getAuthHeadersFromConfig() {
        return new String[][]{
                {"user_id", plugin.getMainConfig().getString("uid")},
                {"server_id", plugin.getMainConfig().getString("server_id")}
        };
    }
}
