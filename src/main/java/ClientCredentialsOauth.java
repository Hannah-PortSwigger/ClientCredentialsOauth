import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.core.BurpSuiteEdition;
import burp.api.montoya.http.sessions.ActionResult;
import burp.api.montoya.http.sessions.SessionHandlingAction;
import burp.api.montoya.http.sessions.SessionHandlingActionData;
import ui.ConfigPanel;

import javax.swing.*;

public class ClientCredentialsOauth implements BurpExtension
{
    static final String NAME = "Client Credentials OAuth";
    private static final String SESSION_CONFIG_KEY = "project_options.sessions";

    @Override
    public void initialize(MontoyaApi api)
    {
        api.extension().setName(NAME);

        String sessionConfig = api.burpSuite().exportProjectOptionsAsJson(SESSION_CONFIG_KEY);
        ConfigurationProvider configurationProvider = new ConfigurationProvider(api.logging(), sessionConfig);


        if (configurationProvider.isValid())
        {
            initializeWithValidConfiguration(api, configurationProvider);
        }
        else
        {
            api.http().registerSessionHandlingAction(new InvalidCredentialsSessionHandlingAction());
        }

        if (api.burpSuite().version().edition() != BurpSuiteEdition.ENTERPRISE_EDITION)
        {
            ConfigPanel configPanel = new ConfigPanel();
            JPanel panel = configPanel.getMainPanel();

            api.userInterface().registerSuiteTab("OAuth Config", panel);
        }

        api.logging().logToOutput(NAME + " - Loaded successfully.");
    }

    private static void initializeWithValidConfiguration(MontoyaApi api, ConfigurationProvider configurationProvider)
    {
        TokenRetriever tokenRetriever = new TokenRetriever(api, configurationProvider);

        api.http().registerSessionHandlingAction(new MySessionHandlingAction(tokenRetriever));
        api.extension().registerUnloadingHandler(tokenRetriever::shutdownAuthenticationRequests);
    }

    private static class InvalidCredentialsSessionHandlingAction implements SessionHandlingAction
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
    }
}
