package com.example.summer.datas;

public class LocationConfig {
    private final String name;
    private final double latitude;
    private final double longitude;
    private final int[] bannerResIds;
    private final String cityCode;
    private final String servicePhone;
    private final String helpDetails;

    public LocationConfig(String name, double latitude, double longitude, int[] bannerResIds, String cityCode, String servicePhone, String helpDetails) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.bannerResIds = bannerResIds;
        this.cityCode = cityCode;
        this.servicePhone = servicePhone;
        this.helpDetails = helpDetails;
    }

    public String getName() {
        return name;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public int[] getBannerResIds() {
        return bannerResIds;
    }

    public String getCityCode() {
        return cityCode;
    }

    public String getServicePhone() {
        return servicePhone;
    }

    public String getHelpDetails() {
        return helpDetails;
    }
}
