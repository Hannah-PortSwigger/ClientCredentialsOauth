# ClientCredentialsOauth
Extension to handle Client Credentials OAuth in Burp Suite Enterprise

## Usage
1. Add a session handling rule to Burp Pro/Community that invokes the Burp extension.
    - Build extension JAR
    - Load extension into Burp Suite Professional or Community Edition (Extensions > Installed > Add) *Ignore any errors on load*
    - Go to "Settings > Sessions > Session handling rules > Add > Rule actions > Add > Invoke a Burp extension > Extension action handler: Client credentials flow"
3. Provide the description for the session handling rule in the following format:
```
{"oauth_endpoint":"https://ENDPOINT_URL","client_id":"CLIENT_ID","client_secret":"CLIENT_SECRET","audience":"AUDIENCE"}
```
3. Set scope for session handling rule appropriately.
4. Reload extension in Burp and test it is working as expected. You can quickly reload an extension by going to "Extensions > Installed" and using Control/Command + Click on the "Loaded" checkbox.
5. Export session handling rule from Burp and import to Enterprise
   - Export from Burp: "Settings > Sessions > Session handling rules > Cog button > Save settings"
   - [Import to Enterprise](https://portswigger.net/burp/documentation/enterprise/working-with-scans/scan-configurations#importing-scan-configurations)

*Note: This is an initial iteration. Further work will be done to improve usability.*

## Example JSON configuration file for Enterprise upload
*Note: This session handling rule configuration is in-scope for all URLs*
```json
{
    "project_options":{
        "sessions":{
            "session_handling_rules":{
                "rules":[
                    {
                        "actions":[
                            {
                                "action_name":"Client credentials flow",
                                "enabled":true,
                                "type":"invoke_extension"
                            }
                        ],
                        "description":"{\"oauth_endpoint\":\"https://ENDPOINT_URL\",\"client_id\":\"CLIENT_ID\",\"client_secret\":\"CLIENT_SECRET\",\"audience\":\"AUDIENCE\"}",
                        "enabled":true,
                        "exclude_from_scope":[],
                        "include_in_scope":[],
                        "named_params":[],
                        "restrict_scope_to_named_params":false,
                        "tools_scope":[
                            "Target",
                            "Scanner",
                            "Intruder",
                            "Repeater",
                            "Sequencer"
                        ],
                        "url_scope":"all",
                        "url_scope_advanced_mode":false
                    }
                ]
            }
        }
    }
}
```

## Future improvements
- [X] Migrate to session handling rule for easier configuration
- [X] Add check to see if "authorization" header is present, as opposed to "Authorization"
- [ ] Provide UI in Desktop for easier description configuration
