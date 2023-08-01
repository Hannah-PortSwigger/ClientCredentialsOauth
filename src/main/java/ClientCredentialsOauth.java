import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.extension.ExtensionUnloadingHandler;

public class ClientCredentialsOauth implements BurpExtension
{
    public static final String OAUTH_ENDPOINT = "https://portswigger-labs.net/oauth/token";

    public static final String clientId = "CLIENT_ID";
    public static final String clientSecret = "CLIENT_SECRET";
    public static final String audience = "AUDIENCE";

    @Override
    public void initialize(MontoyaApi api)
    {
        api.extension().setName("Client Credentials OAuth");

        MyHttpHandler httpHandler = new MyHttpHandler(api);
        api.http().registerHttpHandler(httpHandler);

        api.extension().registerUnloadingHandler(httpHandler::shutdownAuthenticationRequests);
    }
}
