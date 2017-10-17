package ru.tpgeovk.back.model;

public class PlaceInfo {

    private Integer id;
    private String title;
    private Float longitude;
    private Float latitude;
    private String placeIcon;

    public PlaceInfo() { }

    public PlaceInfo(Integer id, String title, Float longitude, Float latitude, String placeIcon) {
        this.id = id;
        this.title = title;
        this.longitude = longitude;
        this.latitude = latitude;
        this.placeIcon = placeIcon;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public String getPlaceIcon() {
        return placeIcon;
    }

    public void setPlaceIcon(String placeIcon) {
        this.placeIcon = placeIcon;
    }
}
