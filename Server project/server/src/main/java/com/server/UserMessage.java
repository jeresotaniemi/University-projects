package com.server;

import java.time.ZonedDateTime;

class UserMessage {
    private final String locationName;
    private final String locationDescription;
    private final String locationCity;
    private final ZonedDateTime originalPostingTime;
    private final String locationCountry;
    private final String locationStreetAddress;
    private final double latitude;
    private final double longitude;
    private final int weather;

    public UserMessage(String locationName, String locationDesc, String locationCity, ZonedDateTime originalPostTime,
            String locationCountry, String streetAddress, double latitude, double longitude, int weather) {
        this.locationName = locationName;
        this.locationDescription = locationDesc;
        this.locationCity = locationCity;
        this.originalPostingTime = originalPostTime;
        this.locationCountry = locationCountry;
        this.locationStreetAddress = streetAddress;
        this.latitude = latitude;
        this.longitude = longitude;
        this.weather = weather;
    }

    public String getLocationName() {
        return locationName;
    }

    public String getLocationDescription() {
        return locationDescription;
    }

    public String getLocationCity() {
        return locationCity;
    }

    public ZonedDateTime getOriginalPostingTime() {
        return originalPostingTime;
    }

    public String getLocationCountry() {
        return locationCountry;
    }

    public String getStreetAddress() {
        return locationStreetAddress;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getLatitude() {
        return latitude;
    }

    public int getWeather() {
        return weather;
    }
}
