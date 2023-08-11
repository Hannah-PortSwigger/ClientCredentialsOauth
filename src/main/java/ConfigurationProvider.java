import burp.api.montoya.logging.Logging;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigurationProvider
{
    private String oauthEndpoint;
    private String clientId;
    private String clientSecret;
    private String audience;

    public ConfigurationProvider(Logging logging, String sessionConfig)
    {
        String sessionHandlingActionDescription = retrieveSessionHandlingDescription(sessionConfig);

        String unescapedDescription;
        if (sessionHandlingActionDescription == null)
        {
            logging.logToOutput(ClientCredentialsOauth.NAME + " - No session handling rule description set.");
            unescapedDescription = null;
        }
        else {
            unescapedDescription = sessionHandlingActionDescription.replaceAll("\\\\", "");
        }

        try
        {
            JSONObject jsonContent = createJsonObject(logging, unescapedDescription);

            extractJsonFields(jsonContent);
        }
        catch (Exception e)
        {
            logging.logToError(ClientCredentialsOauth.NAME + " - Issue retrieving configuration values.\r\n" + e);
        }
    }

    public String getOauthEndpoint()
    {
        return oauthEndpoint;
    }

    public String getClientId()
    {
        return clientId;
    }

    public String getClientSecret()
    {
        return clientSecret;
    }

    public String getAudience()
    {
        return audience;
    }

    private String retrieveSessionHandlingDescription(String sessionConfig)
    {
        Pattern pattern = Pattern.compile("\"description\":\"(\\{.+\\})\"");
        Matcher matcher = pattern.matcher(sessionConfig);

        if (matcher.find())
        {
            return matcher.group(1);
        }

        return null;
    }

    private JSONObject createJsonObject(Logging logging, String unescapedDescription)
    {
        assert unescapedDescription != null;
        JSONObject jsonContent = new JSONObject(unescapedDescription);
        logging.logToOutput(ClientCredentialsOauth.NAME + " - Session handling description found.");

        return jsonContent;
    }

    private void extractJsonFields(JSONObject jsonContent)
    {
        oauthEndpoint = jsonContent.getString("oauth_endpoint");
        clientId = jsonContent.getString("client_id");
        clientSecret = jsonContent.getString("client_secret");
        audience = jsonContent.getString("audience");
    }

    public boolean isValid()
    {
        return oauthEndpoint != null && clientId != null && clientSecret != null && audience != null;
    }
}
