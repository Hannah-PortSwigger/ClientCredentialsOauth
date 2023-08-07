import burp.api.montoya.MontoyaApi;
import org.json.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigurationParser
{
    private String oauthEndpoint;
    private String clientId;
    private String clientSecret;
    private String audience;

    public ConfigurationParser(MontoyaApi api, String sessionConfig)
    {

        String sessionHandlingActionDescription = retrieveSessionHandlingDescription(sessionConfig);

        String unescapedDescription;
        if (sessionHandlingActionDescription == null)
        {
            api.logging().logToOutput(ClientCredentialsOauth.NAME + " - No session handling rule description set.");
            unescapedDescription = null;
        }
        else {
            unescapedDescription = sessionHandlingActionDescription.replaceAll("\\\\", "");
        }

        try
        {
            assert unescapedDescription != null;
            JSONObject jsonContent = new JSONObject(unescapedDescription);
            api.logging().logToOutput(ClientCredentialsOauth.NAME + " - Session handling description found.");

            oauthEndpoint = jsonContent.getString("oauth_endpoint");
            clientId = jsonContent.getString("client_id");
            clientSecret = jsonContent.getString("client_secret");
            audience = jsonContent.getString("audience");
        }
        catch (Exception e)
        {
            api.logging().logToError(ClientCredentialsOauth.NAME + " - Issue retrieving configuration values.\r\n" + e);
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
}
