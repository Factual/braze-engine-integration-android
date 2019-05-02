package com.factual.engine.braze;

import android.content.Context;
import android.location.Location;
import com.appboy.Appboy;
import com.appboy.models.outgoing.AppboyProperties;
import com.factual.engine.api.FactualPlace;
import com.factual.engine.api.mobile_state.FactualPlaceVisit;
import com.factual.engine.api.mobile_state.Geographies;
import com.factual.engine.api.mobile_state.UserJourneyEvent;
import com.factual.engine.api.mobile_state.UserJourneyReceiver;
import com.factual.engine.api.mobile_state.UserJourneySpan;
import java.util.List;

public class BrazeEngineUserJourneyReceiver extends UserJourneyReceiver {

  // Key constants
  static final String BRAZE_ENGINE_SPAN_KEY = "engine_span_occurred";
  static final String SPAN_ID_KEY = "span_id";
  static final String EVENT_SOURCE_KEY = "event_source";

  static final String START_TIME_UNAVAILABLE_KEY = "start_time_unavailable";
  static final String END_TIME_UNAVAILABLE_KEY = "end_time_unavailable";
  static final String START_TIMESTAMP_KEY = "start_timestamp";
  static final String END_TIMESTAMP_KEY = "end_timestamp";
  static final String DURATION_KEY = "duration";

  static final String IS_HOME_KEY = "is_home";
  static final String IS_WORK_KEY = "is_work";
  static final String INGRESS_LATITUDE_KEY = "ingress_latitude";
  static final String INGRESS_LONGITUDE_KEY = "ingress_longitude";

  static final String COUNTRY_KEY = "country";
  static final String LOCALITIES_KEY = "localities";
  static final String POSTCODE_KEY = "postcode";
  static final String REGION_KEY = "region";

  static final String BRAZE_ENGINE_SPAN_ATTACHED_PLACE_KEY = "engine_span_attached_place";
  static final String NAME_KEY = "name";
  static final String FACTUAL_PLACE_ID_KEY = "factual_id";
  static final String LATITUDE_KEY = "latitude";
  static final String LONGITUDE_KEY = "longitude";
  static final String CATEGORIES_KEY = "category_labels";
  static final String CHAIN_KEY = "chain_name";
  static final String DISTANCE_KEY = "distance";
  static final String LOCALITY_KEY = "locality";

  private String sourceName = "factual";

  @Override
  public void onUserJourneyEvent(UserJourneyEvent userJourneyEvent) { /* Not supported */ }

  @Override
  public void onUserJourneySpan(UserJourneySpan userJourneySpan) {
    // Only send span data when did travel is false and tracking User Journey Spans is enabled
    if (BrazeEngineIntegration.isTrackingSpans() && !userJourneySpan.didTravel()) {
      Context context = getContext().getApplicationContext();
      handleUserJourneySpan(context, userJourneySpan);
    }
  }

  // Sends span data to Braze
  void handleUserJourneySpan(Context context, UserJourneySpan span) {
    Appboy appboy = Appboy.getInstance(context);
    AppboyProperties properties = new AppboyProperties();

    // Span information
    String spanId = span.getSpanId();
    boolean startTimestampUnavailable = span.isStartTimestampUnavailable();
    boolean endTimestampUnavailable = span.isEndTimestampUnavailable();
    long startTimestamp = span.getStartTimestamp();
    long endTimestamp = span.getEndTimestamp();

    // Initialize data
    FactualPlaceVisit currentPlace = span.getCurrentPlace();
    List<FactualPlace> places = null;
    boolean isHome = false;
    boolean isWork = false;
    double ingressLatitude = -1;
    double ingressLongitude = -1;
    Geographies geographies;
    String localities = null;
    String country = null;
    String postcode = null;
    String region = null;

    // Current place information
    if (currentPlace != null) {
      places = currentPlace.getAttachedPlaces();
      isHome = currentPlace.isHome();
      isWork = currentPlace.isWork();

      // Ingress information
      Location ingressLocation = currentPlace.getIngressLocation();
      if (ingressLocation != null) {
        ingressLatitude = ingressLocation.getLatitude();
        ingressLongitude = ingressLocation.getLongitude();
      }

      // Geographies information
      geographies = currentPlace.getGeographies();
      if (geographies != null) {
        country = geographies.getCountry();
        String localitiesString = geographies.getLocalities().toString();
        // Trim off open and closing bracket
        localities = localitiesString.substring(1, localitiesString.length() - 1);
        postcode = geographies.getPostcode();
        region = geographies.getRegion();
      }
    }

    // Get duration of span in seconds
    long duration = !startTimestampUnavailable && !endTimestampUnavailable ?
        (endTimestamp - startTimestamp) : 0;

    // Populate properties
    properties.addProperty(SPAN_ID_KEY, spanId);
    properties.addProperty(EVENT_SOURCE_KEY, sourceName);
    properties.addProperty(START_TIME_UNAVAILABLE_KEY, startTimestampUnavailable);
    properties.addProperty(END_TIME_UNAVAILABLE_KEY, endTimestampUnavailable);
    properties.addProperty(START_TIMESTAMP_KEY, startTimestamp);
    properties.addProperty(END_TIMESTAMP_KEY, endTimestamp);
    properties.addProperty(DURATION_KEY, duration);
    properties.addProperty(IS_HOME_KEY, isHome);
    properties.addProperty(IS_WORK_KEY, isWork);
    properties.addProperty(INGRESS_LATITUDE_KEY, ingressLatitude);
    properties.addProperty(INGRESS_LONGITUDE_KEY, ingressLongitude);
    properties.addProperty(COUNTRY_KEY, country);
    properties.addProperty(LOCALITIES_KEY, localities);
    properties.addProperty(POSTCODE_KEY, postcode);
    properties.addProperty(REGION_KEY, region);

    // Send data to Braze
    appboy.logCustomEvent(BRAZE_ENGINE_SPAN_KEY, properties);

    // Get max number of places to send
    int numberOfPlaces = places == null ? 0 : places.size();
    int maxPlaceEvents = getMaxAttachedPlaceEvents(context, numberOfPlaces);

    if (maxPlaceEvents > 0 && places != null) {
      // Send attached places data
      sendPlacesData(places, spanId, maxPlaceEvents, appboy);
    }
  }

  // Sends attached places data to Braze
  private void sendPlacesData(List<FactualPlace> places, String spanId, int maxPlaceEvents,
      Appboy appboy) {
    AppboyProperties properties = new AppboyProperties();
    properties.addProperty(EVENT_SOURCE_KEY, sourceName);
    properties.addProperty(SPAN_ID_KEY, spanId);

    // Loop through attached places
    for (int index = 0; index < maxPlaceEvents; index++) {
      FactualPlace place = places.get(index);

      // Add categories
      String categoriesString = PlaceCategoryMap.getPlaceCategories(place).toString();
      // Trim off open and closing bracket
      String categories = categoriesString.substring(1, categoriesString.length() - 1);

      // Add chain
      String chain = PlaceChainMap.getChain(place);

      String id = place.getFactualId();
      double latitude = place.getLatitude();
      double longitude = place.getLongitude();
      double distance = place.getDistance();
      String locality = place.getLocality();
      String region = place.getRegion();
      String country = place.getCountry();
      String postcode = place.getPostcode();

      properties.addProperty(NAME_KEY, place.getName());
      properties.addProperty(CATEGORIES_KEY, categories);
      properties.addProperty(CHAIN_KEY, chain);
      properties.addProperty(FACTUAL_PLACE_ID_KEY, id);
      properties.addProperty(LATITUDE_KEY, latitude);
      properties.addProperty(LONGITUDE_KEY, longitude);
      properties.addProperty(DISTANCE_KEY, distance);
      properties.addProperty(LOCALITY_KEY, locality);
      properties.addProperty(REGION_KEY, region);
      properties.addProperty(COUNTRY_KEY, country);
      properties.addProperty(POSTCODE_KEY, postcode);

      // Send data to Braze
      appboy.logCustomEvent(BRAZE_ENGINE_SPAN_ATTACHED_PLACE_KEY, properties);
    }
  }

  // Gets max attached place events allowed to be sent
  private static int getMaxAttachedPlaceEvents(Context context, Integer numAttachedPlaces) {
    int maxAtPlaceEventsPerCircumstance = context
        .getSharedPreferences(BrazeEngineIntegration.class.getName(),
            Context.MODE_PRIVATE)
        .getInt(
            BrazeEngineIntegration.NUM_MAX_ATTACHED_PLACE_EVENTS_PER_SPAN_KEY,
            BrazeEngineIntegration.NUM_MAX_ATTACHED_PLACE_EVENTS_PER_SPAN_DEFAULT
        );

    return Math.min(maxAtPlaceEventsPerCircumstance, numAttachedPlaces);
  }
}
