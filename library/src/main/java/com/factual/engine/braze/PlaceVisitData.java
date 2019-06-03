package com.factual.engine.braze;

import android.location.Location;
import android.text.TextUtils;
import com.factual.engine.api.FactualPlace;
import com.factual.engine.api.mobile_state.FactualPlaceVisit;
import com.factual.engine.api.mobile_state.Geographies;
import java.util.List;

class PlaceVisitData {

  private List<FactualPlace> places = null;
  private String localities = null;
  private String country = null;
  private String postcode = null;
  private String region = null;
  private double ingressLatitude = -1;
  private double ingressLongitude = -1;
  private boolean isHome = false;
  private boolean isWork = false;

  public List<FactualPlace> getPlaces() {
    return places;
  }

  public String getLocalities() {
    return localities;
  }

  public String getCountry() {
    return country;
  }

  public String getPostcode() {
    return postcode;
  }

  public String getRegion() {
    return region;
  }

  public double getIngressLatitude() {
    return ingressLatitude;
  }

  public double getIngressLongitude() {
    return ingressLongitude;
  }

  public boolean isHome() {
    return isHome;
  }

  public boolean isWork() {
    return isWork;
  }

  public PlaceVisitData(FactualPlaceVisit visit) {
    // Current place information
    if (visit != null) {
      places = visit.getAttachedPlaces();
      isHome = visit.isHome();
      isWork = visit.isWork();

      // Ingress information
      Location ingressLocation = visit.getIngressLocation();
      if (ingressLocation != null) {
        ingressLatitude = ingressLocation.getLatitude();
        ingressLongitude = ingressLocation.getLongitude();
      }

      // Geographies information
      Geographies geographies = visit.getGeographies();
      if (geographies != null) {
        country = geographies.getCountry();
        localities = TextUtils.join(", ", geographies.getLocalities());
        postcode = geographies.getPostcode();
        region = geographies.getRegion();
      }
    }
  }
}
