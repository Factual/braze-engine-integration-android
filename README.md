# Description

This repository contains the code for an integration between Factual's Engine SDK and Braze SDK. 
Using this integration library you can configure Factual's Engine SDK to send Braze custom events
when the user is at a known factual place or an engine circumstance with actionId ```push-to-braze```
is met. The following Braze custom events will be sent:

## User Journey Events

***Name***: engine_user_journey

***Descritpion***: User has visited a known factual palce

***Properties***: 
* name (place name)
* factual_id (factual unique identifier for place)
* latitude
* longitude
* user_latitude
* user_longitude 
* place_categories (comma seperated factual category ids)

## Circumstance Met Events

***Name***: engine_circumstance_[CIRCUMSTANCE_NAME]

***Description***: A circumstance with actionId ```push-to-braze``` has been met

***Properties***: 
* incidence_id
* user_latitude
* user_longitude

## Circumstance Met At Place

***Name***: engine_circumstance_place_at_[CIRCUMSTANCE_NAME]

***Properties***: 
* incidence_id
* name (place name)
* factual_id (factual unique identifier for place)
* latitude
* longitude
* user_latitude
* user_longitude 
* place_categories (comma seperated factual category ids)

***Description***: Additional place related information about place at which the circumstance was met. 
             Based on the specificity of the circumstance rule, it is possible that multiple places may
             simultaneously trigger circumstance. We choose to not include all of the places within the 
             event properties of a single event to simplify the usage within the Braze dashboard. 
             Instead, for each place that triggered the original circumstance, we send a slightly 
             different custom event.
             
***Note***: Given that we have multiple circumstance met events generated whenever an individual circumstance is triggered you
can use the incidence_id property to map these related events. 

# Installation

The project artifacts are avaliable from Factual's bintray Maven repository. 

```
// repository for the Factual artifacts
repositories {
  maven {
    url "https://factual.bintray.com/maven"
  }
}

...

dependencies {
  compile 'com.factual.engine:braze-engine:1.0.0'
}
```

# Usage Requirements

* Configured and started `Engine` client. [see here](http://developer.factual.com/engine/android/)
* Configured `Appboy` client. [see here](https://www.braze.com/documentation/Android/#step-2-configure-the-braze-sdk-in-appboyxml)

# Usage

To capture both user journey and circumstance met events add the following method call to the ```onStarted``` method of either your 
implementation of ```FactualClientListener``` or ```FactualClientReceiver```:

```
BrazeEngineIntegration.initializeBrazeEngineIntegration(androidApplicationContext);
```

To capture only circumstance met events use the following code:

```
boolean enableUserJourney = false;
BrazeEngineIntegration.initializeBrazeEngineIntegration(androidApplicationContext, enableUserJourney);
```

To limit the number of circumstance met at place events sent to Braze use the following method call:

```
boolean enableUserJourney = false;
numMaxEventsPerCircumstance = 3; //note default is set to 10
BrazeEngineIntegration.initializeBrazeEngineIntegration(androidApplicationContext, enableUserJourney, numMaxEventsPerCircumstance);
```

# Demo App

A demo app is included in this repository to demonstrate the usage of this library. (see here)[todo]

