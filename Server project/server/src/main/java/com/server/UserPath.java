package com.server;

class UserPath {
    private final String tour_name;
    private final String tourDescription;

    public UserPath(String tour_name, String tourDescription) {
        this.tour_name = tour_name;
        this.tourDescription = tourDescription;
    }

    public String getTourName() {
        return tour_name;
    }

    public String getTourDescription() {
        return tourDescription;
    }
}
