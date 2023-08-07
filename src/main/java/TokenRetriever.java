import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TokenRetriever
{
    private final HttpRequest authorizationRequest;
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private final ScheduledFuture<?> schedule;
    private final MontoyaApi api;
    private Duration authenticationRequestInterval;
    private String tokenHeaderValue;

    public TokenRetriever(MontoyaApi api, String oauthEndpoint, String clientId, String clientSecret, String audience)
    {
        this.api = api;
        HttpRequest authorizationRequestWithoutBody = HttpRequest.httpRequestFromUrl(oauthEndpoint).withMethod("POST").withHeader("Content-Type", "application/json");

        String requestBody = String.format(
                "{\"client_id\":\"%s\",\"client_secret\":\"%s\",\"audience\":\"%s\",\"grant_type\":\"client_credentials\"}",
                clientId,
                clientSecret,
                audience
        );

        authorizationRequest = authorizationRequestWithoutBody.withBody(requestBody);

        retrieveToken();

        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        schedule = scheduledThreadPoolExecutor.scheduleAtFixedRate(this::retrieveToken, authenticationRequestInterval.getSeconds(), authenticationRequestInterval.getSeconds(), TimeUnit.SECONDS);

        api.logging().logToOutput(ClientCredentialsOauth.NAME + " - Token retrieval polling started.");
    }

    private void retrieveToken()
    {
        HttpRequestResponse requestResponse = api.http().sendRequest(authorizationRequest);

        try {
            JSONObject jsonContent = new JSONObject(requestResponse.response().bodyToString());

            String accessToken = jsonContent.getString("access_token");
            String tokenType = jsonContent.getString("token_type");
            int expiresIn = jsonContent.getInt("expires_in");

            authenticationRequestInterval = Duration.ofSeconds(expiresIn);
            tokenHeaderValue = tokenType + " " + accessToken;
        } catch (JSONException e)
        {
            api.logging().logToError(ClientCredentialsOauth.NAME + " - Failed to parse JSON");
            tokenHeaderValue = null;
        }
    }

    public void shutdownAuthenticationRequests()
    {
        schedule.cancel(true);
        scheduledThreadPoolExecutor.shutdown();

        api.logging().logToOutput(ClientCredentialsOauth.NAME + " - Token retrieval polling stopped.");
    }

    public String getTokenHeaderValue()
    {
        return tokenHeaderValue;
    }
}
