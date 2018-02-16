# Introduction

This app demonstrates the usage of the Braze/Engine integration library. 

# Requirements

Configure ``appboy.xml`` configuration file with correct Braze Android SDK API key and endpoint. [see here](https://github.com/Factual/braze-engine-integration-android/blob/master/demo/src/main/res/values/appboy.xml) 

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string translatable="false" name="com_appboy_api_key">API-KEY</string>
    <!--<string translatable="false" name="com_appboy_custom_endpoint">ENDPOINT</string>-->
</resources>
```

Configure ```engine.xml``` configuration file with correct Engine SDK API Key. [see here](https://github.com/Factual/braze-engine-integration-android/blob/master/demo/src/main/res/values/engine.xml)


```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string translatable="false" name="com_factual_engine_api_key">API-KEY</string>
</resources>
```

# Usage

This demo app will send Braze both user journey and circumstance met events. To see circumstance events 
define your custom circumstances with actionId `push-to-braze` using Engine's Garage dashboard.
