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
        ConfigurationProvider configurationProvider = new ConfigurationProvider(api.logging(), sessionConfig);

        if (configurationProvider.isValid())
        {
            TokenRetriever tokenRetriever = new TokenRetriever(api, configurationProvider);

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
