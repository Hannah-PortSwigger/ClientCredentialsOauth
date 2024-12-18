import burp.api.montoya.logging.Logging;
import burp.api.montoya.utilities.json.JsonException;
import burp.api.montoya.utilities.json.JsonNode;
import burp.api.montoya.utilities.json.JsonObjectNode;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigurationProvider
{
    private static final String OAUTH_ENDPOINT = "oauth_endpoint";
    private static final String KEY_CLIENT_ID = "client_id";
    private static final String KEY_CLIENT_SECRET = "client_secret";
    private static final String KEY_AUDIENCE = "audience";
    private static final String DESCRIPTION_REGEX = "\"description\":\"(\\{.+\\})\"";

    private String oauthEndpoint;
    private String clientId;
    private String clientSecret;
    private String audience;

    public ConfigurationProvider(Logging logging, String sessionConfig)
    {
        try
        {
            String sessionHandlingActionDescription = retrieveSessionHandlingDescription(sessionConfig)
                    .orElseThrow(() -> new ConfigurationException("Failed to retrieve session handling description."));

            String unescapedDescription = sessionHandlingActionDescription.replaceAll("\\\\", "");
            logging.logToOutput(ClientCredentialsOauth.NAME + " - Session handling description found.");

            JsonObjectNode jsonNode = (JsonObjectNode) JsonNode.jsonNode(unescapedDescription);
            populateConfigurationFromJson(jsonNode);
        }
        catch (JsonException jsonException)
        {
            logging.logToError(ClientCredentialsOauth.NAME + " - Failed to retrieve configuration values.\r\n" + jsonException);
        }
        catch (ConfigurationException configurationException)
        {
            logging.logToError(ClientCredentialsOauth.NAME + " - %s.\r\n".formatted(configurationException));
        }
    }

    public boolean isValid()
    {
        return oauthEndpoint != null && clientId != null && clientSecret != null && audience != null;
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

    private Optional<String> retrieveSessionHandlingDescription(String sessionConfig)
    {
        Pattern pattern = Pattern.compile(DESCRIPTION_REGEX);
        Matcher matcher = pattern.matcher(sessionConfig);

        return matcher.find() ? Optional.ofNullable(matcher.group(1)) : Optional.empty();
    }

    private void populateConfigurationFromJson(JsonObjectNode jsonContent)
    {
        oauthEndpoint = jsonContent.getString(OAUTH_ENDPOINT);
        clientId = jsonContent.getString(KEY_CLIENT_ID);
        clientSecret = jsonContent.getString(KEY_CLIENT_SECRET);
        audience = jsonContent.getString(KEY_AUDIENCE);
    }
}
