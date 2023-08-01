import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.handler.*;
import burp.api.montoya.http.message.HttpRequestResponse;
import burp.api.montoya.http.message.requests.HttpRequest;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static burp.api.montoya.http.handler.RequestToBeSentAction.continueWith;
import static burp.api.montoya.http.handler.ResponseReceivedAction.continueWith;

public class MyHttpHandler implements HttpHandler
{
    private final MontoyaApi api;
    private final HttpRequest authorizationRequest;
    private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
    private final ScheduledFuture<?> schedule;
    private String tokenHeaderValue;
    private Duration authenticationRequestInterval;

    public MyHttpHandler(MontoyaApi api)
    {
        this.api = api;

        HttpRequest authorizationRequestWithoutBody = HttpRequest.httpRequestFromUrl(ClientCredentialsOauth.OAUTH_ENDPOINT).withMethod("POST").withHeader("Content-Type", "application/json");

        String requestBody = String.format(
                "{\"client_id\":\"%s\",\"client_secret\":\"%s\",\"audience\":\"%s\",\"grant_type\":\"client_credentials\"}",
                ClientCredentialsOauth.clientId,
                ClientCredentialsOauth.clientSecret,
                ClientCredentialsOauth.audience
        );

        authorizationRequest = authorizationRequestWithoutBody.withBody(requestBody);

        retrieveToken();

        scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
        schedule = scheduledThreadPoolExecutor.scheduleAtFixedRate(this::retrieveToken, authenticationRequestInterval.getSeconds(), authenticationRequestInterval.getSeconds(), TimeUnit.SECONDS);
    }

    @Override
    public RequestToBeSentAction handleHttpRequestToBeSent(HttpRequestToBeSent requestToBeSent)
    {
        if (!requestToBeSent.toolSource().isFromTool(ToolType.EXTENSIONS))
        {
            return continueWith(requestToBeSent.withHeader("Authorization", tokenHeaderValue));
        }

        return continueWith(requestToBeSent);
    }

    @Override
    public ResponseReceivedAction handleHttpResponseReceived(HttpResponseReceived responseReceived)
    {
        return continueWith(responseReceived);
    }

    public void retrieveToken()
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
            api.logging().logToError("Failed to parse JSON");
            tokenHeaderValue = null;
        }
    }

    public void shutdownAuthenticationRequests()
    {
        schedule.cancel(true);
        scheduledThreadPoolExecutor.shutdown();
    }
}
