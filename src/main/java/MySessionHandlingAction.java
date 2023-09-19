import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.http.sessions.ActionResult;
import burp.api.montoya.http.sessions.SessionHandlingAction;
import burp.api.montoya.http.sessions.SessionHandlingActionData;

public class MySessionHandlingAction implements SessionHandlingAction
{
    private final TokenRetriever tokenRetriever;

    public MySessionHandlingAction(TokenRetriever tokenRetriever)
    {
        this.tokenRetriever = tokenRetriever;
    }

    @Override
    public String name()
    {
        return "Client credentials flow";
    }

    @Override
    public ActionResult performAction(SessionHandlingActionData actionData)
    {
        HttpRequest requestToBeSent;

        if (actionData.request().hasHeader("authorization"))
        {
            requestToBeSent = actionData.request().withHeader("authorization", tokenRetriever.getTokenHeaderValue());
        }
        else
        {
            requestToBeSent = actionData.request().withHeader("Authorization", tokenRetriever.getTokenHeaderValue());
        }

        return ActionResult.actionResult(requestToBeSent);
    }
}
