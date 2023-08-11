import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.http.sessions.ActionResult;
import burp.api.montoya.http.sessions.SessionHandlingAction;
import burp.api.montoya.http.sessions.SessionHandlingActionData;

public class ClientCredentialsOauth implements BurpExtension {
    static final String NAME = "Client Credentials OAuth";
    private static final String SESSION_CONFIG_KEY = "project_options.sessions";

    private static void initializeWithValidConfiguration(MontoyaApi api, ConfigurationProvider configurationProvider) {
        TokenRetriever tokenRetriever = new TokenRetriever(api, configurationProvider);

        api.http().registerSessionHandlingAction(new MySessionHandlingAction(tokenRetriever));
        api.extension().registerUnloadingHandler(tokenRetriever::shutdownAuthenticationRequests);
    }

    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName(NAME);

        String sessionConfig = api.burpSuite().exportProjectOptionsAsJson(SESSION_CONFIG_KEY);
        ConfigurationProvider configurationProvider = new ConfigurationProvider(api.logging(), sessionConfig);

        if (configurationProvider.isValid()) {
            initializeWithValidConfiguration(api, configurationProvider);
        } else {
            api.http().registerSessionHandlingAction(new InvalidCredentialsSessionHandlingAction());
        }

        api.logging().logToOutput(NAME + " - Loaded successfully.");
    }

    private static class InvalidCredentialsSessionHandlingAction implements SessionHandlingAction {
        @Override
        public String name() {
            return "Client credentials flow";
        }

        @Override
        public ActionResult performAction(SessionHandlingActionData actionData) {
            return ActionResult.actionResult(actionData.request());
        }
    }
}
