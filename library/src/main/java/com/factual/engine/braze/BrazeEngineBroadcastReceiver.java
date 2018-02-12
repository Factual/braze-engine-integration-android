package com.factual.engine.braze;

import android.content.Context;
import android.location.Location;
import android.text.TextUtils;

import com.appboy.Appboy;
import com.appboy.models.outgoing.AppboyProperties;
import com.factual.engine.api.CircumstanceResponse;
import com.factual.engine.api.FactualActionReceiver;
import com.factual.engine.api.FactualPlace;
import java.util.List;
import java.util.UUID;

public class BrazeEngineBroadcastReceiver extends FactualActionReceiver {

    @Override
    public void onCircumstancesMet(List<CircumstanceResponse> responses) {
        Context context = getContext().getApplicationContext();
        Appboy appboy = Appboy.getInstance(context);

        for (CircumstanceResponse response : responses) {
            String circumstanceId = response.getCircumstance().getCircumstanceId();
            if (BrazeEngineIntegration.USER_JOURNEY_CIRCUMSTANCE_ID.equals(circumstanceId)) {
                pushUserJourneyEvent(appboy, response);
            } else {
                pushGeneralCircumstanceEvents(appboy, context, response);
            }
        }
    }

    private void pushUserJourneyEvent(Appboy appboy, CircumstanceResponse response) {
        List<FactualPlace> places = response.getAtPlaces();
        AppboyProperties properties = createPlaceAppBoyProperties(places.get(0), response.getLocation());
        appboy.logCustomEvent(BrazeEngineIntegration.USER_JOURNEY_CIRCUMSTANCE_ID, properties);
    }

    private void pushGeneralCircumstanceEvents(Appboy appboy, Context context, CircumstanceResponse response) {
        String circumstanceId = response.getCircumstance().getCircumstanceId();
        String circumstanceEventName = BrazeEngineIntegration.CIRCUMSTANCE_EVENT_NAME_PREFIX+circumstanceId;
        String circumstancePlaceAtEventName = BrazeEngineIntegration.CIRCUMSTANCE_PLACE_AT_EVENT_NAME_PREFIX+circumstanceId;
        String incidenceId = UUID.randomUUID().toString();
        Location location = response.getLocation();
        List<FactualPlace> places = response.getAtPlaces();

        appboy.logCustomEvent(circumstanceEventName, createCircumstanceAppBoyProperties(incidenceId, location));

        int maxEventsPerCircumstance = context.getSharedPreferences(BrazeEngineIntegration.class.getName(), Context.MODE_PRIVATE)
                .getInt(BrazeEngineIntegration.NUM_MAX_EVENTS_PER_CIRCUMSTANCE_KEY, BrazeEngineIntegration.NUM_MAX_EVENTS_PER_CIRCUMSTANCE_DEFAULT);

        for (int i = 0; i < maxEventsPerCircumstance && i < places.size(); i++) {
            AppboyProperties properties = createPlaceAppBoyProperties(places.get(i), location, incidenceId);
            appboy.logCustomEvent(circumstancePlaceAtEventName, properties);
        }
    }

    private AppboyProperties createCircumstanceAppBoyProperties(String incidenceId, Location userLocation) {
        AppboyProperties properties = new AppboyProperties();
        properties.addProperty("incidence_id", incidenceId);
        properties.addProperty("user_latitude", userLocation.getLatitude());
        properties.addProperty("user_longitude", userLocation.getLongitude());
        return properties;
    }

    private AppboyProperties createPlaceAppBoyProperties(FactualPlace place, Location userLocation) {
        AppboyProperties properties = new AppboyProperties();
        properties.addProperty("name", place.getName());
        properties.addProperty("factual_id", place.getFactualId());
        properties.addProperty("latitude", place.getLatitude());
        properties.addProperty("longitude", place.getLongitude());
        properties.addProperty("user_latitude", userLocation.getLatitude());
        properties.addProperty("user_longitude", userLocation.getLongitude());
        properties.addProperty("place_categories", place.getCategoryIds() == null
                ? "" : TextUtils.join(",", place.getCategoryIds()));
        return properties;
    }

    private AppboyProperties createPlaceAppBoyProperties(FactualPlace place, Location userLocation, String incidenceId) {
        AppboyProperties properties = createPlaceAppBoyProperties(place, userLocation);
        properties.addProperty("incidence_id", incidenceId);
        return properties;
    }
}
