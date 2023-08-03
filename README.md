# ClientCredentialsOauth
Extension to handle Client Credentials OAuth in Burp Suite Enterprise

## Usage
1. Modify hard-coded values in `src/main/java/ClientCredentialsOauth.java`
2. Build extension
3. Load extension into Enterprise. Bearer token will be added to all requests in the Scan.

*Note: This is an initial iteration. Further work will be done to improve usability.*

## Future improvements
Will be looking at implementing this as a session handling rule to be imported into Enterprise, so that scope can can be easily adjusted.
This should also make it easier to configure values in a way that makes this extension more reusable across sites.
