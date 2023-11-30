package ui;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;

public class ConfigPanel
{
    private final JPanel mainPanel;
    private JTextField oauthEndpointInput;
    private JTextField clientIdInput;
    private JTextField clientSecretInput;
    private JTextField audienceInput;
    private JTextArea generatedSessionRuleOutput;
    private JButton clipboardButton;

    public ConfigPanel()
    {
        mainPanel = new JPanel();
//        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        initComponents();
    }

    public JPanel getMainPanel()
    {
        return mainPanel;
    }

    private void initComponents()
    {
        mainPanel.add(generateConfigurationPanel());

        mainPanel.add(generateButtonPanel());

        mainPanel.add(generateSessionHandlingDescriptionPanel());
    }

    private JPanel generateConfigurationPanel()
    {
        GridLayout layout = new GridLayout(0,2);
        JPanel configurationPanel = new JPanel(layout);

        JLabel oauthEndpointLabel = new JLabel("OAuth Endpoint:");
        configurationPanel.add(oauthEndpointLabel);

        oauthEndpointInput = new JTextField("https://OAUTH_ENPOINT.COM/token");
        configurationPanel.add(oauthEndpointInput);

        JLabel clientIdLabel = new JLabel("Client ID:");
        configurationPanel.add(clientIdLabel);

        clientIdInput = new JTextField("CLIENT_ID");
        configurationPanel.add(clientIdInput);

        JLabel clientSecretLabel = new JLabel("Client secret:");
        configurationPanel.add(clientSecretLabel);

        clientSecretInput = new JTextField("CLIENT_SECRET");
        configurationPanel.add(clientSecretInput);

        JLabel audienceLabel = new JLabel("Audience:");
        configurationPanel.add(audienceLabel);

        audienceInput = new JTextField("AUDIENCE");
        configurationPanel.add(audienceInput);

        return configurationPanel;
    }

    private JPanel generateButtonPanel()
    {
        JPanel buttonPanel = new JPanel();

        JButton generateButton = new JButton("Generate");
        generateButton.addActionListener(l -> {
            generateSessionHandlingRuleDescription();
            clipboardButton.setEnabled(true);
        });
        buttonPanel.add(generateButton);

        clipboardButton = new JButton("Copy to clipboard");
        if (generatedSessionRuleOutput == null || generatedSessionRuleOutput.getText().isBlank())
        {
            clipboardButton.setEnabled(false);
        }
        clipboardButton.addActionListener(l ->
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
                        new StringSelection(generatedSessionRuleOutput.getText()),
                        null
                )
        );
        buttonPanel.add(clipboardButton);

        return buttonPanel;
    }

    private JPanel generateSessionHandlingDescriptionPanel()
    {
        GridLayout layout = new GridLayout(0,2);
        JPanel sessionHandlingDescriptionPanel = new JPanel(layout);

        generatedSessionRuleOutput = new JTextArea();
        generatedSessionRuleOutput.setEditable(false);
        sessionHandlingDescriptionPanel.add(generatedSessionRuleOutput);

        return sessionHandlingDescriptionPanel;
    }

    private void generateSessionHandlingRuleDescription()
    {
        String builtString = String.format(
                """
                {
                    "oauth_endpoint":"%s",
                    "client_id":"%s",
                    "client_secret":"%s",
                    "audience":"%s"
                }
                """,
                oauthEndpointInput.getText(),
                clientIdInput.getText(),
                clientSecretInput.getText(),
                audienceInput.getText()
        );

        generatedSessionRuleOutput.setText(builtString);
    }
}
