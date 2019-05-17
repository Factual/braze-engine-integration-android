package com.factual.engine.braze;

import android.content.Context;

import com.factual.engine.api.FactualCircumstance;
import com.factual.engine.FactualEngine;
import com.factual.engine.api.FactualCircumstanceException;

public class BrazeEngineIntegration {
  public static final String EXPECTED_ACTION_ID = "push-to-braze";
  public static final int NUM_MAX_EVENTS_PER_CIRCUMSTANCE_DEFAULT = 10;
  public static final String CIRCUMSTANCE_EVENT_NAME_PREFIX = "engine_circumstance_";
  public static final String CIRCUMSTANCE_PLACE_AT_EVENT_NAME_PREFIX = "engine_circumstance_place_at_";
  static final String NUM_MAX_EVENTS_PER_CIRCUMSTANCE_KEY = "max_events_per_circumstance_";
  static final String USER_JOURNEY_CIRCUMSTANCE_ID = "engine_user_journey";

  /**
   * WARNING: Both Engine and Braze need to be individually initialized prior to executing this method.
   * <p>
   * Configures Engine to push custom events to Braze when either of the following is triggered:
   * 1) Engine circumstances with actionId {@value #EXPECTED_ACTION_ID}
   * 2) Engine User Journey events
   * <p>
   * In the case of (1) the following custom events will be pushed:
   * a) Event named "{@value #CIRCUMSTANCE_EVENT_NAME_PREFIX}CIRCUMSTANCE_ID" to represent the triggered circumstance
   * b) At most {@value #NUM_MAX_EVENTS_PER_CIRCUMSTANCE_DEFAULT} events named "{@value #CIRCUMSTANCE_PLACE_AT_EVENT_NAME_PREFIX}CIRCUMSTANCE_ID"
   * for each place which triggered the event. Based on the specificity of the circumstance expression there may be multiple
   * places that concurrently trigger a circumstance.
   */
  public static void initializeBrazeEngineIntegration(Context context) {
    initializeBrazeEngineIntegration(context, true);
  }


  /**
   * @param enableUserJourney whether user journey events should trigger a Braze custom event
   * @see BrazeEngineIntegration#initializeBrazeEngineIntegration(Context)
   */
  public static void initializeBrazeEngineIntegration(Context context, boolean enableUserJourney) {
    initializeBrazeEngineIntegration(context, enableUserJourney, NUM_MAX_EVENTS_PER_CIRCUMSTANCE_DEFAULT);
  }

  /**
   * @param numMaxEventsPerCircumstance max number of custom braze events to push for each place that triggered a circumstance
   * @see BrazeEngineIntegration#initializeBrazeEngineIntegration(Context, boolean)
   */
  public static void initializeBrazeEngineIntegration(Context context, boolean enableUserJourney, int numMaxEventsPerCircumstance) {
    context.getApplicationContext()
        .getSharedPreferences(BrazeEngineIntegration.class.getName(), Context.MODE_PRIVATE)
        .edit()
        .putInt(NUM_MAX_EVENTS_PER_CIRCUMSTANCE_KEY, numMaxEventsPerCircumstance)
        .apply();

    if (enableUserJourney) {
      try {
        FactualCircumstance userJourneyCircumstance =
            new FactualCircumstance(USER_JOURNEY_CIRCUMSTANCE_ID, "(at any-factual-place)", EXPECTED_ACTION_ID);
        FactualEngine.registerCircumstance(userJourneyCircumstance);
      } catch (FactualCircumstanceException e) {
        throw new RuntimeException(e);
      }
    }

    FactualEngine.registerAction(EXPECTED_ACTION_ID, BrazeEngineBroadcastReceiver.class);
  }
}
