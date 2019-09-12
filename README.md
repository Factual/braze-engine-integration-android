# Factual / Braze SDK for Android ![Build Status](https://app.bitrise.io/app/f001791884e47358/status.svg?token=zill-aMMVVaFzOKXBor3Ow&branch=master)

This repository contains the code for an integration between [Factual's Engine SDK](https://www.factual.com/products/engine/) and [Braze's Mobile SDK](https://www.braze.com/). Using this library you can configure Factual's Location Engine SDK to send custom events to Braze to better understand users in the physical world and build personalized experiences to drive user engagement and revenue.

### Integration with Braze UI

see: [engine-braze-integration](https://github.com/Factual/engine-braze-integration)

# Installation

The project artifacts are available from Factual's Bintray Maven repository.

```
// repository for the Factual artifacts
repositories {
  maven {
    url "https://factual.bintray.com/maven"
  }
}

...

dependencies {
  compile 'com.factual.engine:braze-engine:2.1.0'
}
```

# Usage

### Requirements

* Configured and started `Engine` client. [see here](http://developer.factual.com/engine/android/)
* Configured `Braze` client. [see here](https://www.braze.com/documentation/iOS/#initial-sdk-setup)

### Tracking Factual Engine Circumstances

Start tracking Factual Engine circumstances by calling ` BrazeEngineIntegration.pushToBraze()` in the `onCircumstancesMet()` callback of `FactualClientReceiver`.

```java
public class ExampleFactualClientReceiver extends FactualClientReceiver {

    @Override
    public void onCircumstancesMet(List<CircumstanceResponse> responses) {
      /*
      Max number of "engine_at_" events that should be sent per "engine_" + CIRCUMSTANCE_NAME.
      Default is set to 10.
      */
      int maxAtPlaceEvents = 3;

      /*
      Max number of "engine_near_" events that should be sent per "engine_" + CIRCUMSTANCE_NAME.
      Default is set to 20.
      */
      int maxNearPlaceEvents = 5;
      for (CircumstanceResponse response : responses) {
        /* Send circumstance event to braze */
        BrazeEngineIntegration.pushToBraze(getContext().getApplicationContext(),
              response,
              maxAtPlaceEvents,
              maxNearPlaceEvents);
    }
  }

  ...

}
```

### Tracking Factual Engine User Journey Spans

Start tracking User Journey Spans by first setting User Journey Receiver to `BrazeEngineUserJourneyReceiver` before starting FactualEngine.
```java
public void initializeEngine() throws FactualException {
    FactualEngine.initialize(getApplicationContext(),
        Configuration.ENGINE_API_KEY,
        ExampleFactualClientReceiver.class);
    FactualEngine.setUserJourneyReceiver(BrazeEngineUserJourneyReceiver.class);
    FactualEngine.start();
}
```

Then call `BrazeEngineIntegration.trackUserJourneySpans()` in the `onStarted()` method of `FactualClientReceiver`.

```java
public class ExampleFactualClientReceiver extends FactualClientReceiver {
  @Override
  public void onStarted() {
    Log.i("engine", "Engine has started.");
    /*
    Max number of "engine_span_attached_place" events that should be sent per
    "engine_span_occurred". Default is set to 20.
    */
    int maxAttachedPlaceEventsPerSpan = 10;

    /* Start tracking spans */
    BrazeEngineIntegration.trackUserJourneySpans(getContext().getApplicationContext(),
        maxAttachedPlaceEventsPerSpan);
}

    ...
}
```

Please refer to the [Factual Developer Docs](http://developer.factual.com) for more information about Engine.

## Example App

An example app is included in this repository to demonstrate the usage of this library, see [./example](./example) for documentation and usage instructions.
