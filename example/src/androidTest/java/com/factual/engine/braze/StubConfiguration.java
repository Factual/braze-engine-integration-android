package com.factual.engine.braze;

final class StubConfiguration {

  static final String ENGINE_API_KEY = "Your Factual Location Engine API Key here";
  static final String BRAZE_REST_API_KEY = "Your Braze Rest API Key here";
  static final String BRAZE_REST_ENDPOINT = "Your Braze Rest Endpoint here";
  static final String BRAZE_TEST_USER_EMAIL = "Your Braze Test User Email here";
  static final String BRAZE_TEST_USER_ID = "Your Braze Test User ID here";
  static final String CIRCUMSTANCE_NAME = "Your Engine Circumstance Name here";

  // Testing coordinates (LA Memorial Coliseum - Circumstance is set to trigger for sports related places)
  // Change these to coordinates for a place which will trigger your engine circumstance
  static final double TEST_LATITUDE = 34.0135;
  static final double TEST_LONGITUDE = -118.2875;
}