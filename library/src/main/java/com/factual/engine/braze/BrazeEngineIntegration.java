package com.factual.engine.braze;

import android.content.Context;

import com.factual.engine.FactualEngine;
import com.factual.engine.api.FactualCircumstanceException;

public class BrazeEngineIntegration {

  static final String EXPECTED_ACTION_ID = "send-custom-event";
  static final int NUM_MAX_AT_PLACE_EVENTS_PER_CIRCUMSTANCE_DEFAULT = 10;
  static final int NUM_MAX_NEAR_PLACE_EVENTS_PER_CIRCUMSTANCE_DEFAULT = 20;
  static final int NUM_MAX_ATTACHED_PLACE_EVENTS_PER_SPAN_DEFAULT = 20;
  static final String CIRCUMSTANCE_MET_EVENT = "engine_circumstance_";
  static final String CIRCUMSTANCE_PLACE_AT_EVENT = "engine_circumstance_at_place_";
  static final String CIRCUMSTANCE_PLACE_NEAR_EVENT = "engine_circumstance_near_place_";
  static final String SPAN_EVENT = "engine_span";
  static final String SPAN_ATTACHED_PLACE = "engine_span_attached_place";
  static final String NUM_MAX_AT_PLACE_EVENTS_PER_CIRCUMSTANCE_KEY = "max_at_place_events_per_circumstance";
  static final String NUM_MAX_NEAR_PLACE_EVENTS_PER_CIRCUMSTANCE_KEY = "max_near_place_events_per_circumstance";
  static final String NUM_MAX_ATTACHED_PLACE_EVENTS_PER_SPAN_KEY = "max_attached_place_events_per_span";

  private static Boolean trackingSpans = false;
  private static Boolean trackingCircumstances = false;

  /**
   * <b>WARNING: Both Braze and Engine must be individually initialized prior to executing
   * this method. </b>
   * <p>
   * Configures Engine to push custom events when an Engine circumstances with actionId {@value
   * #EXPECTED_ACTION_ID} occurs
   *
   * <p>
   * When a circumstance is met the following custom events will be pushed:
   * <br>
   * a) Event named {@value #CIRCUMSTANCE_MET_EVENT} <i>+ CIRCUMSTANCE NAME</i> to represent the
   * triggered circumstance
   * <br>
   * b) At most {@value #NUM_MAX_AT_PLACE_EVENTS_PER_CIRCUMSTANCE_DEFAULT} events named {@value
   * #CIRCUMSTANCE_PLACE_AT_EVENT} <i>+ CIRCUMSTANCE NAME</i> for each place the user is at which
   * triggered the event. Based on the specificity of the circumstance expression there may be
   * multiple places that concurrently trigger a circumstance.
   * <br>
   * c) At most {@value #NUM_MAX_NEAR_PLACE_EVENTS_PER_CIRCUMSTANCE_DEFAULT} events named {@value
   * #CIRCUMSTANCE_PLACE_NEAR_EVENT} <i>+ CIRCUMSTANCE NAME</i> for each place the user is near -
   * but not at - which triggered the event. Based on the specificity of the circumstance expression
   * there may be multiple places that concurrently trigger a circumstance.
   */

  public static void trackCircumstances(Context context) {
    trackCircumstances(context,
        NUM_MAX_AT_PLACE_EVENTS_PER_CIRCUMSTANCE_DEFAULT,
        NUM_MAX_NEAR_PLACE_EVENTS_PER_CIRCUMSTANCE_DEFAULT);
  }

  /**
   * @param numMaxAtPlaceEventsPerCircumstance max number of custom events to push for each place
   * the user was at that triggered a circumstance
   * @param numMaxNearPlaceEventsPerCircumstance max number of custom events to push for each place
   * the user was near that triggered a circumstance
   * @see BrazeEngineIntegration#trackCircumstances(Context, int, int)
   */
  public static void trackCircumstances(Context context,
      int numMaxAtPlaceEventsPerCircumstance,
      int numMaxNearPlaceEventsPerCircumstance) {
    context.getApplicationContext()
        .getSharedPreferences(BrazeEngineIntegration.class.getName(),
            Context.MODE_PRIVATE)
        .edit()
        .putInt(NUM_MAX_AT_PLACE_EVENTS_PER_CIRCUMSTANCE_KEY,
            numMaxAtPlaceEventsPerCircumstance)
        .putInt(NUM_MAX_NEAR_PLACE_EVENTS_PER_CIRCUMSTANCE_KEY,
            numMaxNearPlaceEventsPerCircumstance)
        .apply();

    FactualEngine.registerAction(EXPECTED_ACTION_ID, BrazeEngineActionReceiver.class);
    trackingCircumstances = true;
  }

  /**
   * Disables Braze Engine's circumstance tracking
   */
  public static void disableTrackingCircumstances() {
    try {
      FactualEngine.disableCircumstance(EXPECTED_ACTION_ID);
      trackingCircumstances = false;
    } catch (FactualCircumstanceException exception) {
      exception.printStackTrace();
    }
  }

  /**
   * <b>WARNING: Both Braze and Engine must be individually initialized and you must set
   * Engine's User Journey Receiver to BrazeEngineUserJourneyReceiver prior to executing this
   * method.  </b>
   * <p>
   * Configures Engine to push User Journey Spans to Braze
   *
   * <p>
   * When a span occurs the following custom events will be pushed:
   * <br>
   * a) Event named {@value #SPAN_EVENT} to represent the span
   * <br>
   * b) At most {@value #NUM_MAX_ATTACHED_PLACE_EVENTS_PER_SPAN_DEFAULT} events named {@value
   * #SPAN_ATTACHED_PLACE} for each attached place the user was at or near during the span Based on
   * the specificity of the attached places expression there may be multiple places that are listed
   * in the span.
   */
  public static void trackUserJourneySpans(Context context) {
    trackUserJourneySpans(context, NUM_MAX_ATTACHED_PLACE_EVENTS_PER_SPAN_DEFAULT);
  }

  /**
   * @param numMaxAttachedPlaceEventsPerSpan max number of custom events to push for each place the
   * user was at or near during the span
   * @see BrazeEngineIntegration#trackUserJourneySpans(Context, int)
   */
  public static void trackUserJourneySpans(Context context,
      int numMaxAttachedPlaceEventsPerSpan) {
    context.getApplicationContext()
        .getSharedPreferences(BrazeEngineIntegration.class.getName(),
            Context.MODE_PRIVATE)
        .edit()
        .putInt(NUM_MAX_ATTACHED_PLACE_EVENTS_PER_SPAN_KEY,
            numMaxAttachedPlaceEventsPerSpan)
        .apply();

    BrazeEngineIntegration.trackingSpans = true;
  }

  /**
   * Disables Braze Engine's User Journey Span tracking
   */
  public static void disableTrackingUserJourneySpans() {
    BrazeEngineIntegration.trackingSpans = false;
  }

  /**
   * @return true if tracking spans is enabled
   */
  public static Boolean isTrackingSpans() {
    return BrazeEngineIntegration.trackingSpans;
  }

  /**
   * @return true if tracking spans is enabled
   */
  public static Boolean isTrackingCircumstances() {
    return BrazeEngineIntegration.trackingCircumstances;
  }
}
