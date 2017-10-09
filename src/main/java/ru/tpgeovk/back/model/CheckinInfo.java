package ru.tpgeovk.back.model;

import com.vk.api.sdk.objects.base.Place;
import com.vk.api.sdk.objects.places.Checkin;
import com.vk.api.sdk.objects.wall.WallpostFull;

public class CheckinInfo {

    private String checkinId;
    private Integer userId;
    private Integer postId;
    private String date;
    private Float latitude;
    private Float longitude;
    private String text;
    private Integer placeId;
    private String placeTitle;

    public static CheckinInfo fromPostFull(WallpostFull postFull) {
        CheckinInfo result = new CheckinInfo();
        result.setCheckinId(postFull.getFromId().toString() + "_" + postFull.getId().toString());
        result.setUserId(postFull.getFromId());
        result.setPostId(postFull.getId());
        result.setDate(postFull.getDate().toString());
        result.setText(postFull.getText());
        String[] coordinates = postFull.getGeo().getCoordinates().split(" ");
        result.setLatitude(Float.parseFloat(coordinates[0]));
        result.setLongitude(Float.parseFloat(coordinates[1]));

        Place place = postFull.getGeo().getPlace();
        if ((place != null) && (!place.getId().equals(0))) {
            result.setPlaceId(place.getId());
            result.setPlaceTitle(place.getTitle());
        }

        return result;
    }

    public CheckinInfo() { }

    public String getCheckinId() {
        return checkinId;
    }

    public void setCheckinId(String checkinId) {
        this.checkinId = checkinId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Integer getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Integer placeId) {
        this.placeId = placeId;
    }

    public String getPlaceTitle() {
        return placeTitle;
    }

    public void setPlaceTitle(String placeTitle) {
        this.placeTitle = placeTitle;
    }
}
