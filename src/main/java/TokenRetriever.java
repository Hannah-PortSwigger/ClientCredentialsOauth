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
    private final MontoyaApi api;
    private final HttpRequest authorizationRequest;
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private final ScheduledFuture<?> schedule;
    private Duration authenticationRequestInterval;
    private String tokenHeaderValue;

    public TokenRetriever(MontoyaApi api, ConfigurationProvider configurationProvider)
    {
        this.api = api;

        authorizationRequest = getAuthorizationRequest(configurationProvider);

        retrieveToken();

        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        schedule = scheduledThreadPoolExecutor.scheduleAtFixedRate(this::retrieveToken, authenticationRequestInterval.getSeconds(), authenticationRequestInterval.getSeconds(), TimeUnit.SECONDS);

        api.logging().logToOutput(ClientCredentialsOauth.NAME + " - Token retrieval polling started.");
    }

    private HttpRequest getAuthorizationRequest(ConfigurationProvider configurationProvider)
    {
        final HttpRequest authorizationRequest;
        HttpRequest authorizationRequestWithoutBody = HttpRequest.httpRequestFromUrl(configurationProvider.getOauthEndpoint()).withMethod("POST").withHeader("Content-Type", "application/json");
        String requestBody = String.format(
                "{\"client_id\":\"%s\",\"client_secret\":\"%s\",\"audience\":\"%s\",\"grant_type\":\"client_credentials\"}",
                configurationProvider.getClientId(),
                configurationProvider.getClientSecret(),
                configurationProvider.getAudience()
        );
        authorizationRequest = authorizationRequestWithoutBody.withBody(requestBody);

        return authorizationRequest;
    }

    public String getTokenHeaderValue()
    {
        return tokenHeaderValue;
    }

    public void shutdownAuthenticationRequests()
    {
        schedule.cancel(true);
        scheduledThreadPoolExecutor.shutdown();

        api.logging().logToOutput(ClientCredentialsOauth.NAME + " - Token retrieval polling stopped.");
    }

    private void retrieveToken()
    {
        HttpRequestResponse requestResponse = api.http().sendRequest(authorizationRequest);

        try {
            JSONObject jsonContent = new JSONObject(requestResponse.response().bodyToString());

            if (authenticationRequestInterval == null)
            {
                int expiresIn = jsonContent.getInt("expires_in");
                authenticationRequestInterval = Duration.ofSeconds(expiresIn);
            }

            String accessToken = jsonContent.getString("access_token");
            String tokenType = jsonContent.getString("token_type");

            tokenHeaderValue = tokenType + " " + accessToken;
        } catch (JSONException e)
        {
            api.logging().logToError(ClientCredentialsOauth.NAME + " - Failed to parse JSON");
            tokenHeaderValue = null;
        }
    }
}
