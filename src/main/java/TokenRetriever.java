import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TokenRetriever {
    private static final String CONTENT_TYPE = "application/json";
    private static final String KEY_EXPIRES_IN = "expires_in";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_TOKEN_TYPE = "token_type";
    private final MontoyaApi api;
    private final HttpRequest authorizationRequest;
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private final ScheduledFuture<?> schedule;
    private Duration authenticationRequestInterval;
    private String tokenHeaderValue;

    public TokenRetriever(MontoyaApi api, ConfigurationProvider configurationProvider) {
        this.api = api;
        this.authorizationRequest = getAuthorizationRequest(configurationProvider);
        this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);

        retrieveToken();
        this.schedule = scheduledThreadPoolExecutor.scheduleAtFixedRate(this::retrieveToken,
                authenticationRequestInterval.getSeconds(),
                authenticationRequestInterval.getSeconds(),
                TimeUnit.SECONDS);

        api.logging().logToOutput(ClientCredentialsOauth.NAME + " - Token retrieval polling started.");
    }

    private HttpRequest getAuthorizationRequest(ConfigurationProvider configurationProvider) {
        String requestBody = String.format(
                "{\"client_id\":\"%s\",\"client_secret\":\"%s\",\"audience\":\"%s\",\"grant_type\":\"client_credentials\"}",
                configurationProvider.getClientId(),
                configurationProvider.getClientSecret(),
                configurationProvider.getAudience()
        );

        return HttpRequest.httpRequestFromUrl(configurationProvider.getOauthEndpoint())
                .withMethod("POST")
                .withHeader("Content-Type", CONTENT_TYPE)
                .withBody(requestBody);
    }

    public String getTokenHeaderValue() {
        return tokenHeaderValue;
    }

    public void shutdownAuthenticationRequests() {
        schedule.cancel(true);
        scheduledThreadPoolExecutor.shutdown();
        api.logging().logToOutput(ClientCredentialsOauth.NAME + " - Token retrieval polling stopped.");
    }

    private void retrieveToken() {
        HttpRequestResponse requestResponse = api.http().sendRequest(authorizationRequest);

        try {
            JSONObject jsonContent = new JSONObject(requestResponse.response().bodyToString());
            updateToken(jsonContent);
        } catch (JSONException e) {
            api.logging().logToError(ClientCredentialsOauth.NAME + " - Failed to parse JSON");
            tokenHeaderValue = null;
        }
    }

    private void updateToken(JSONObject jsonContent) {
        if (authenticationRequestInterval == null) {
            int expiresIn = jsonContent.getInt(KEY_EXPIRES_IN);
            authenticationRequestInterval = Duration.ofSeconds(expiresIn);
        }

        String accessToken = jsonContent.getString(KEY_ACCESS_TOKEN);
        String tokenType = jsonContent.getString(KEY_TOKEN_TYPE);

        tokenHeaderValue = tokenType + " " + accessToken;
    }
}
