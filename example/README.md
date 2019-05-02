# Example

## Setup

### Add API Keys

**(1)** Locate your Factual Engine API Key from the [Factual Engine Dashboard](https://engine.factual.com/garage)

![Dashboard image](./images/dashboard.png)

**(2)** Add Factual Location Engine API Key in Configuration.java where it says, `"Your Factual Location Engine API Key Here"`

**(3)** Locate your Braze API Key for your app from the [Braze Dashboard](https://dashboard.braze.com) in **Developer Console** under the **APP SETTINGS** tab.  Go to **Identification** and use the API Key listed for your app.

**(4)** Add your Braze SDK API Key to `appboy.xml` where it says `"Your Braze SDK API Key here"`. [see here](src/main/res/values/appboy.xml)

**(5)** Determine your [Braze Endpoint](https://www.braze.com/docs/user_guide/administrative/access_braze/sdk_endpoints/) and add it to `appboy.xml` where it says `"Your Braze SDK Endpoint here"`

**(5)** In Configuration.java replace `"Your Braze User ID here"` and `"Your Braze User Email here"` to a test user id and user email which you can use to look up on Braze to ensure the data is being sent.

### Update google-services.json

The example is setup to send a push notification using [Firebase](https://firebase.google.com/). The file google-services.json should be updated with you credentials and permissions.

### Testing

If you'd like to test the integration, an example test is given. To run the test, fill out your information in `StubConfiguration`. Change the `TEST_LATITUDE` and `TEST_LONGITUDE` variables to coordinates for a place which would trigger your engine circumstance.

### Explore

From here you can setup a Braze Campaign to trigger actions based on Engine custom events. For example, see [here](https://github.com/Factual/engine-iterable-integration#example) for an example of sending a push notification for when a user visits a coffee shop
