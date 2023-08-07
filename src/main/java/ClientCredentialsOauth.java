import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.sessions.ActionResult;
import burp.api.montoya.http.sessions.SessionHandlingAction;
import burp.api.montoya.http.sessions.SessionHandlingActionData;

public class ClientCredentialsOauth implements BurpExtension
{
    protected static final String NAME = "Client Credentials OAuth";
    @Override
    public void initialize(MontoyaApi api)
    {
        api.extension().setName(NAME);

        String sessionConfig = api.burpSuite().exportProjectOptionsAsJson("project_options.sessions");
        ConfigurationParser configurationParser = new ConfigurationParser(api, sessionConfig);

        String oauthEndpoint = configurationParser.getOauthEndpoint();
        String clientId = configurationParser.getClientId();
        String clientSecret = configurationParser.getClientSecret();
        String audience = configurationParser.getAudience();

        if (oauthEndpoint != null && clientId != null && clientSecret != null && audience != null)
        {
            TokenRetriever tokenRetriever = new TokenRetriever(api, oauthEndpoint, clientId, clientSecret, audience);

            api.http().registerSessionHandlingAction(new MySessionHandlingAction(tokenRetriever));

            api.extension().registerUnloadingHandler(tokenRetriever::shutdownAuthenticationRequests);
        }
        else {
            api.http().registerSessionHandlingAction(new SessionHandlingAction()
            {
                @Override
                public String name()
                {
                    return "Client credentials flow";
                }

                @Override
                public ActionResult performAction(SessionHandlingActionData actionData)
                {
                    return ActionResult.actionResult(actionData.request());
                }
            });
        }

        api.logging().logToOutput(NAME + " - Loaded successfully.");
    }
}
