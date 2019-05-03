package com.factual.engine.braze;

import android.content.Context;
import android.location.Location;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.appboy.Appboy;
import com.factual.engine.FactualEngine;
import com.factual.engine.api.FactualPlace;
import com.factual.engine.api.mobile_state.FactualActivityType;
import com.factual.engine.api.mobile_state.FactualPlaceVisit;
import com.factual.engine.api.mobile_state.UserJourneySpan;
import com.factual.engine.api.FactualException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Example tests for Braze Engine integration
 */
@RunWith(AndroidJUnit4.class)
public class BrazeEngineIntegrationTest {

  // Keys used in Braze API
  private static String API_KEY = "api_key";
  private static String EMAIL_KEY = "email_address";
  private static String ID_KEY = "external_id";
  private static String USERS_KEY = "users";
  private static String CUSTOM_EVENTS_KEY = "custom_events";
  private static String EVENT_NAME_KEY = "name";
  private static String EVENT_DATE_KEY = "last";

  /**
   * Setup Braze Engine
   */
  @Before
  public void setUp() {
    // Ensure Engine has not been initialized yet
    if (!FactualEngine.isEngineStarted()) {
      // Context of the app under test.
      Context appContext = InstrumentationRegistry.getTargetContext();

      // Setup Engine
      try {
        FactualEngine.initialize(appContext, StubConfiguration.ENGINE_API_KEY);
        FactualEngine.setReceiver(StubFactualClientReceiver.class);
        FactualEngine.start();
      } catch (FactualException e) {
        fail("Could not start Factual Engine because of exception: " + e.getMessage());
      }

      // Wait for Engine to start
      synchronized (StubFactualClientReceiver.lock) {
        try {
          StubFactualClientReceiver.lock.wait(30000);
        } catch (InterruptedException ex) {
          fail("Engine was initialized, but never started");
        }
      }

      // Configure Braze
      Appboy appboy = Appboy.getInstance(appContext);
      appboy.changeUser(StubConfiguration.BRAZE_TEST_USER_ID);
      appboy.getCurrentUser().setEmail(StubConfiguration.BRAZE_TEST_USER_EMAIL);

      // Start the integration
      BrazeEngineIntegration.trackCircumstances(appContext, 0, 0);
      BrazeEngineIntegration.trackUserJourneySpans(appContext, 1);
    }
  }

  /**
   * Tests that Braze Engine Integration is detecting circumstances
   */
  @Test
  public void testCircumstances() {

    // Give location to test
    Location location = new Location("test-location");
    location.setLatitude(StubConfiguration.TEST_LATITUDE);
    location.setLongitude(StubConfiguration.TEST_LONGITUDE);

    Date aboutToRun = new Date();
    delay(5);

    try {
      FactualEngine.runCircumstances(location);
    } catch (Exception e) {
      fail("Could not run circumstance because error: " + e.getMessage());
    }

    HashSet<String> events = new HashSet<>();
    events.add(String.format("engine_circumstance_%s", StubConfiguration.CIRCUMSTANCE_NAME));
    events.add(String.format("engine_circumstance_at_place_%s", StubConfiguration.CIRCUMSTANCE_NAME));

    verify(aboutToRun, events);
  }

  /**
   * Tests that Braze Engine Integration is detecting spans
   */
  @Test
  public void testSpans() {
    UserJourneySpan span = createSpan();
    Date aboutToRun = new Date();
    delay(5);

    // Push span
    BrazeEngineUserJourneyReceiver receiver = new BrazeEngineUserJourneyReceiver();
    receiver.handleUserJourneySpan(InstrumentationRegistry.getContext(), span);

    HashSet<String> events = new HashSet<>();
    events.add("engine_span_occurred");
    events.add("engine_span_attached_place");

    verify(aboutToRun, events);
  }

  /**
   * Verifies the data was sent to Braze
   *
   * @param preRun Date right before sending data to Braze
   * @param events Events to look for from braze
   */
  private void verify(Date preRun, HashSet<String> events) {
    // Wait for Braze to track event
    delay(60);

    // Ensure data was sent to Braze
    OkHttpClient client = new OkHttpClient();
    Request request = brazeApiRequest();

    try {
      // Get Braze API data
      Response responses = client.newCall(request).execute();
      JSONObject jsonData = new JSONObject(responses.body().string());
      // Get test user
      JSONArray users = jsonData.getJSONArray(USERS_KEY);
      JSONObject user = null;
      for (int i = 0; i < users.length(); i++) {
        JSONObject testUser = users.getJSONObject(i);
        if (testUser.getString(ID_KEY).equals(StubConfiguration.BRAZE_TEST_USER_ID)) {
          user = testUser;
          break;
        }
      }
      // Get custom events
      JSONArray customEvents = user.getJSONArray(CUSTOM_EVENTS_KEY);
      // Loop through each custom event
      for (int i = 0; i < customEvents.length(); i++) {
        JSONObject event = customEvents.getJSONObject(i);
        String eventName = event.getString(EVENT_NAME_KEY);
        // Ensure this event is the one we sent
        if (events.contains(event.getString(EVENT_NAME_KEY))) {
          // Get event date
          String dateString = event.getString(EVENT_DATE_KEY);
          SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
          Date date = dateFormat.parse(dateString);
          // Ensure this is the event we just sent and not an old event
          Assert.assertTrue(String.format("Braze did not receive event: %s", eventName),
              preRun.before(date));
          events.remove(eventName);
        }
      }

      Assert.assertEquals(String.format("Events missing from custom events: %s", events.toString()),
          0,
          events.size());

    } catch (Exception e) {
      fail("Failed to get valid response from Braze: " + e.getMessage());
    }
  }

  /**
   * Waits for given number of seconds
   *
   * @param seconds Number of seconds to delay
   */
  private void delay(int seconds) {
    try {
      Thread.sleep(seconds * 1000);
    } catch (Exception e) {
      fail("Could not run delay because error: " + e.getMessage());
    }
  }

  /**
   * Creates a unique span to be tested. The unique values are the startTimestamp and endTimestamp
   * (creating a unique duration), id, distance, latitude, and longitude.
   *
   * @return A unique UserJourneySpan object to be tested.
   */
  private UserJourneySpan createSpan() {
    // Create unique parameters for UserJourneySpan
    long startTimestamp = System.currentTimeMillis() - 100000;
    long endTimestamp = System.currentTimeMillis();
    Location location = new Location("test-location");
    location.setLatitude(33.8003);
    location.setLongitude(-117.8827);

    JSONArray categoryArray = new JSONArray();
    categoryArray.put(372).put(406);
    FactualPlace place;
    List<FactualPlace> places = new ArrayList<>();

    // Create objects for span
    JSONObject placeObject = new JSONObject();
    try {
      placeObject.put(BrazeEngineUserJourneyReceiver.NAME_KEY,
          "Angel Stadium of Anaheim")
          .put(BrazeEngineUserJourneyReceiver.FACTUAL_PLACE_ID_KEY, "test-id")
          .put(BrazeEngineUserJourneyReceiver.CATEGORIES_KEY, categoryArray)
          .put(BrazeEngineUserJourneyReceiver.DISTANCE_KEY, -1)
          .put(BrazeEngineUserJourneyReceiver.LATITUDE_KEY, location.getLatitude())
          .put(BrazeEngineUserJourneyReceiver.LONGITUDE_KEY, location.getLongitude())
          .put(BrazeEngineUserJourneyReceiver.COUNTRY_KEY, "us")
          .put(BrazeEngineUserJourneyReceiver.LOCALITY_KEY, "Anaheim")
          .put(BrazeEngineUserJourneyReceiver.REGION_KEY, "CA")
          .put(BrazeEngineUserJourneyReceiver.POSTCODE_KEY, "92806");
      place = new FactualPlace(placeObject);
      places.add(place);
    } catch (JSONException exception) {
      fail("Could not create placeObject because of exception: " + exception.getMessage());
    }

    FactualPlaceVisit placeVisit = new FactualPlaceVisit(location,
        places,
        null,
        false,
        false);

    return new UserJourneySpan("this-is-a-test",
        startTimestamp,
        false,
        endTimestamp,
        false,
        false,
        false,
        placeVisit,
        null,
        FactualActivityType.NO_ACTIVITY,
        null);
  }

  /**
   * @return A Request object for getting data from Braze
   */
  private Request brazeApiRequest() {
    String endpoint = StubConfiguration.BRAZE_REST_ENDPOINT;
    String url = String.format("https://%s/users/export/ids", endpoint);

    HashMap<String, String> map = new HashMap<>();
    map.put(API_KEY, StubConfiguration.BRAZE_REST_API_KEY);
    map.put(EMAIL_KEY, StubConfiguration.BRAZE_TEST_USER_EMAIL);
    JSONObject jsonObject = new JSONObject(map);
    String json = jsonObject.toString();

    MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    RequestBody requestBody = RequestBody.create(JSON, json);

    return new Request.Builder().url(url).post(requestBody).build();
  }
}
