package com.factual.engine.braze;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import com.appboy.Appboy;
import com.appboy.models.outgoing.AppboyProperties;
import com.factual.engine.integrationutils.*;
import com.factual.engine.api.FactualPlace;
import com.factual.engine.api.mobile_state.UserJourneyEvent;
import com.factual.engine.api.mobile_state.UserJourneyReceiver;
import com.factual.engine.api.mobile_state.UserJourneySpan;
import java.util.List;

public class BrazeEngineUserJourneyReceiver extends UserJourneyReceiver {

  // Key constants
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

  static final String NAME_KEY = "name";
  static final String PLACE_ID_KEY = "factual_id";
  static final String LATITUDE_KEY = "latitude";
  static final String LONGITUDE_KEY = "longitude";
  static final String CATEGORIES_KEY = "category_labels";
  static final String CHAIN_KEY = "chain_name";
  static final String DISTANCE_KEY = "distance";
  static final String LOCALITY_KEY = "locality";

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
    double startTimestamp = span.getStartTimestamp();
    double endTimestamp = span.getEndTimestamp();

    // Get duration of span in seconds
    double duration = !startTimestampUnavailable && !endTimestampUnavailable ?
        (endTimestamp - startTimestamp) : 0;

    // Get current place data
    PlaceVisitData currentPlace = new PlaceVisitData(span.getCurrentPlace());

    // Populate properties
    properties.addProperty(SPAN_ID_KEY, spanId)
        .addProperty(EVENT_SOURCE_KEY, BrazeEngineIntegration.sourceName)
        .addProperty(START_TIME_UNAVAILABLE_KEY, startTimestampUnavailable)
        .addProperty(END_TIME_UNAVAILABLE_KEY, endTimestampUnavailable)
        .addProperty(START_TIMESTAMP_KEY, startTimestamp)
        .addProperty(END_TIMESTAMP_KEY, endTimestamp)
        .addProperty(DURATION_KEY, duration)
        .addProperty(COUNTRY_KEY, currentPlace.getCountry())
        .addProperty(LOCALITIES_KEY, currentPlace.getLocalities())
        .addProperty(POSTCODE_KEY, currentPlace.getPostcode())
        .addProperty(REGION_KEY, currentPlace.getRegion())
        .addProperty(INGRESS_LATITUDE_KEY, currentPlace.getIngressLatitude())
        .addProperty(INGRESS_LONGITUDE_KEY, currentPlace.getIngressLongitude())
        .addProperty(IS_HOME_KEY, currentPlace.isHome())
        .addProperty(IS_WORK_KEY, currentPlace.isWork());

    // Send data to Braze
    Log.i(BrazeEngineIntegration.TAG, "Sending user journey span event to Braze");
    appboy.logCustomEvent(BrazeEngineIntegration.ENGINE_SPAN_EVENT_KEY, properties);


    // Send attached places data if there are any to send
    int numPlaceEvents = getNumPlaceEvents(context, currentPlace.getNumPlaces());
    if (numPlaceEvents > 0) {
      Log.i(BrazeEngineIntegration.TAG,
          String.format("Sending %d attached place event(s) to Braze", numPlaceEvents));
      sendPlacesData(currentPlace.getPlaces(), spanId, appboy, numPlaceEvents);
    }
  }

  // Sends attached places data to Braze
  private void sendPlacesData(List<FactualPlace> places, String spanId, Appboy appboy, int numPlaceEvents) {
    AppboyProperties properties = new AppboyProperties();
    properties.addProperty(EVENT_SOURCE_KEY, BrazeEngineIntegration.sourceName);
    properties.addProperty(SPAN_ID_KEY, spanId);

    // Loop through attached places
    for (int index = 0; index < numPlaceEvents; index++) {
      FactualPlace place = places.get(index);

      // Get place data
      String categories = TextUtils.join(", ", PlaceCategoryMap.getPlaceCategories(place));
      String chain = PlaceChainMap.getChain(place);

      // Add properties
      properties.addProperty(NAME_KEY, place.getName())
          .addProperty(CATEGORIES_KEY, categories)
          .addProperty(CHAIN_KEY, chain)
          .addProperty(PLACE_ID_KEY, place.getFactualId())
          .addProperty(LATITUDE_KEY, place.getLatitude())
          .addProperty(LONGITUDE_KEY, place.getLongitude())
          .addProperty(DISTANCE_KEY, place.getDistance())
          .addProperty(LOCALITY_KEY, place.getLocality())
          .addProperty(REGION_KEY, place.getRegion())
          .addProperty(COUNTRY_KEY, place.getCountry())
          .addProperty(POSTCODE_KEY, place.getPostcode());

      // Push custom event to braze
      appboy.logCustomEvent(BrazeEngineIntegration.ENGINE_SPAN_ATTACHED_PLACE_EVENT_KEY, properties);
    }
  }

  // Gets number of attached place events to send
  private static int getNumPlaceEvents(Context context, Integer numAvailableAttachedPlaces) {
    int maxAtPlaceEventsPerCircumstance = context
        .getSharedPreferences(BrazeEngineIntegration.class.getName(),
            Context.MODE_PRIVATE)
        .getInt(
            BrazeEngineIntegration.NUM_MAX_ATTACHED_PLACE_EVENTS_PER_SPAN_KEY,
            BrazeEngineIntegration.NUM_MAX_ATTACHED_PLACE_EVENTS_PER_SPAN_DEFAULT
        );

    return Math.min(maxAtPlaceEventsPerCircumstance, numAvailableAttachedPlaces);
  }
}
