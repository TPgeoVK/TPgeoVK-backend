package ru.tpgeovk.back.model;

public class PlaceInfo {

    private Integer id;
    private String title;
    private Float longitude;
    private Float latitude;
    private String placeIcon;
    private Integer groupId;
    private String groupPhoto;

    public PlaceInfo() { }

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

    public Integer getGroupId() { return groupId; }

    public void setGroupId(Integer groupId) { this.groupId = groupId; }

    public String getGroupPhoto() {
        return groupPhoto;
    }

    public void setGroupPhoto(String groupPhoto) {
        this.groupPhoto = groupPhoto;
    }
}
