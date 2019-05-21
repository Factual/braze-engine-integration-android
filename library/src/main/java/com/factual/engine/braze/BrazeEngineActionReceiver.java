package com.factual.engine.braze;

import android.content.Context;

import com.appboy.Appboy;
import com.appboy.models.outgoing.AppboyProperties;
import com.factual.engine.api.CircumstanceResponse;
import com.factual.engine.api.FactualActionReceiver;
import com.factual.engine.api.FactualPlace;

import java.util.List;
import java.util.UUID;

public class BrazeEngineActionReceiver extends FactualActionReceiver {

  // Key constants
  static final String BRAZE_ENGINE_EVENT_KEY = "engine_";
  static final String INCIDENT_ID_KEY = "incidence_id";
  static final String USER_LATITUDE_KEY = "user_latitude";
  static final String USER_LONGITUDE_KEY = "user_longitude";
  static final String EVENT_SOURCE_KEY = "event_source";

  static final String AT_PLACE_EVENT_KEY = "engine_at_";
  static final String NEAR_PLACE_EVENT_KEY = "engine_near_";
  static final String PLACE_NAME_KEY = "name";
  static final String FACTUAL_PLACE_ID_KEY = "factual_id";
  static final String PLACE_LATITUDE_KEY = "latitude";
  static final String PLACE_LONGITUDE_KEY = "longitude";
  static final String PLACE_CATEGORIES_KEY = "category_labels";
  static final String PLACE_CHAIN_KEY = "chain_name";

  private final String sourceName = "factual";

  @Override
  public void onCircumstancesMet(List<CircumstanceResponse> responses) {
    if (BrazeEngineIntegration.isTrackingCircumstances()) {
      for (CircumstanceResponse response : responses) {
        pushCircumstanceEvents(getContext().getApplicationContext(), response);
      }
    }
  }

  private void pushCircumstanceEvents(Context context, CircumstanceResponse response) {
    Appboy appboy = Appboy.getInstance(context);
    AppboyProperties properties = new AppboyProperties();

    String circumstanceName = response.getCircumstance().getName();
    double userLatitude = response.getLocation().getLatitude();
    double userLongitude = response.getLocation().getLongitude();
    String incidentId = UUID.randomUUID().toString();

    properties.addProperty(INCIDENT_ID_KEY, incidentId);
    properties.addProperty(USER_LATITUDE_KEY, userLatitude);
    properties.addProperty(USER_LONGITUDE_KEY, userLongitude);
    properties.addProperty(EVENT_SOURCE_KEY, sourceName);

    String circumstanceEventName = BRAZE_ENGINE_EVENT_KEY + circumstanceName;
    appboy.logCustomEvent(circumstanceEventName, properties);

    int maxAtPlaceEvents = getMaxAtPlaceEvents(context, response.getAtPlaces().size());
    if (maxAtPlaceEvents > 0) {
      String atPlaceEventName = AT_PLACE_EVENT_KEY + circumstanceName;
      sendPlacesData(response.getAtPlaces(), atPlaceEventName, maxAtPlaceEvents, appboy,
          properties);
    }

    int maxNearPlaceEvents = getMaxNearPlaceEvents(context, response.getNearPlaces().size());
    if (maxNearPlaceEvents > 0) {
      String nearPlaceEventName = NEAR_PLACE_EVENT_KEY + circumstanceName;
      sendPlacesData(response.getNearPlaces(), nearPlaceEventName, maxNearPlaceEvents, appboy,
          properties);
    }
  }

  private void sendPlacesData(List<FactualPlace> places,
      String eventName,
      int maxPlaceEvents,
      Appboy appboy,
      AppboyProperties properties) {
    for (int index = 0; index < maxPlaceEvents; index++) {
      FactualPlace place = places.get(index);

      // Add categories
      String categoriesString = PlaceCategoryMap.getPlaceCategories(place).toString();
      // Trim off open and closing bracket
      String categories = categoriesString.substring(1, categoriesString.length() - 1);

      // Add chain
      String chain = PlaceChainMap.getChain(place);

      properties.addProperty(PLACE_NAME_KEY, place.getName());
      properties.addProperty(FACTUAL_PLACE_ID_KEY, place.getFactualId());
      properties.addProperty(PLACE_LATITUDE_KEY, place.getLatitude());
      properties.addProperty(PLACE_LONGITUDE_KEY, place.getLongitude());
      properties.addProperty(PLACE_CATEGORIES_KEY, categories);
      properties.addProperty(PLACE_CHAIN_KEY, chain);

      appboy.logCustomEvent(eventName, properties);
    }
  }

  private int getMaxAtPlaceEvents(Context context, Integer numAtPlaces) {
    int maxAtPlaceEventsPerCircumstance = context
        .getSharedPreferences(BrazeEngineIntegration.class.getName(),
            Context.MODE_PRIVATE)
        .getInt(
            BrazeEngineIntegration.NUM_MAX_AT_PLACE_EVENTS_PER_CIRCUMSTANCE_KEY,
            BrazeEngineIntegration.NUM_MAX_AT_PLACE_EVENTS_PER_CIRCUMSTANCE_DEFAULT
        );

    return Math.min(maxAtPlaceEventsPerCircumstance, numAtPlaces);
  }

  private int getMaxNearPlaceEvents(Context context, Integer numNearPlaces) {
    int maxNearPlaceEventsPerCircumstance = context
        .getSharedPreferences(BrazeEngineIntegration.class.getName(),
            Context.MODE_PRIVATE)
        .getInt(
            BrazeEngineIntegration.NUM_MAX_NEAR_PLACE_EVENTS_PER_CIRCUMSTANCE_KEY,
            BrazeEngineIntegration.NUM_MAX_NEAR_PLACE_EVENTS_PER_CIRCUMSTANCE_DEFAULT
        );

    return Math.min(maxNearPlaceEventsPerCircumstance, numNearPlaces);
  }
}
