import burp.api.montoya.logging.Logging;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigurationProvider {
    private static final String OAUTH_ENDPOINT = "oauth_endpoint";
    private static final String KEY_CLIENT_ID = "client_id";
    private static final String KEY_CLIENT_SECRET = "client_secret";
    private static final String KEY_AUDIENCE = "audience";
    private static final String DESCRIPTION_REGEX = "\"description\":\"(\\{.+\\})\"";
    private String oauthEndpoint;
    private String clientId;
    private String clientSecret;
    private String audience;

    public ConfigurationProvider(Logging logging, String sessionConfig) {
        try {
            String sessionHandlingActionDescription = retrieveSessionHandlingDescription(sessionConfig)
                    .orElseThrow(() -> new ConfigurationException("Failed to retrieve session handling description."));

            String unescapedDescription = sessionHandlingActionDescription.replaceAll("\\\\", "");
            logging.logToOutput(ClientCredentialsOauth.NAME + " - Session handling description found.");

            JSONObject jsonObject = new JSONObject(unescapedDescription);
            populateConfigurationFromJson(jsonObject);
        } catch (JSONException jsonException) {
            logging.logToError(ClientCredentialsOauth.NAME + " - Failed to retrieve configuration values.\r\n" + jsonException);
        } catch (ConfigurationException configurationException) {
            logging.logToError(ClientCredentialsOauth.NAME + " - %s.\r\n".formatted(configurationException));
        }
    }

    public boolean isValid() {
        return oauthEndpoint != null && clientId != null && clientSecret != null && audience != null;
    }

    public String getOauthEndpoint() {
        return oauthEndpoint;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getAudience() {
        return audience;
    }

    private Optional<String> retrieveSessionHandlingDescription(String sessionConfig) {
        Pattern pattern = Pattern.compile(DESCRIPTION_REGEX);
        Matcher matcher = pattern.matcher(sessionConfig);

        return matcher.find() ? Optional.ofNullable(matcher.group(1)) : Optional.empty();
    }

    private void populateConfigurationFromJson(JSONObject jsonContent) {
        oauthEndpoint = jsonContent.optString(OAUTH_ENDPOINT, null);
        clientId = jsonContent.optString(KEY_CLIENT_ID, null);
        clientSecret = jsonContent.optString(KEY_CLIENT_SECRET, null);
        audience = jsonContent.optString(KEY_AUDIENCE, null);
    }
}
