package ru.tpgeovk.back.model;

import com.vk.api.sdk.objects.base.Place;
import com.vk.api.sdk.objects.places.Checkin;
import com.vk.api.sdk.objects.users.UserFull;
import com.vk.api.sdk.objects.wall.WallpostFull;

public class CheckinInfo {

    private String checkinId;
    private Integer postId;
    private String date;
    private Float latitude;
    private Float longitude;
    private String text;

    private UserInfo user;
    private PlaceInfo place;

    public static CheckinInfo fromPostFull(WallpostFull postFull) {
        CheckinInfo result = new CheckinInfo();
        result.setCheckinId(postFull.getFromId().toString() + "_" + postFull.getId().toString());
        result.setPostId(postFull.getId());
        result.setDate(postFull.getDate().toString());
        result.setText(postFull.getText());
        String[] coordinates = postFull.getGeo().getCoordinates().split(" ");
        result.setLatitude(Float.parseFloat(coordinates[0]));
        result.setLongitude(Float.parseFloat(coordinates[1]));

        UserInfo userInfo = new UserInfo();
        userInfo.setId(postFull.getFromId());

        Place place = postFull.getGeo().getPlace();
        if ((place != null) && (!place.getId().equals(0))) {
            PlaceInfo placeInfo = new PlaceInfo(
                    place.getId(),
                    place.getTitle(),
                    place.getLongitude(),
                    place.getLatitude(),
                    place.getIcon());
            result.setPlace(placeInfo);
        }

        return result;
    }

    public static CheckinInfo fromPostAndUser(WallpostFull postFull, UserFull userFull) {
        CheckinInfo result = new CheckinInfo();
        result.setCheckinId(postFull.getFromId().toString() + "_" + postFull.getId().toString());
        result.setPostId(postFull.getId());
        result.setDate(postFull.getDate().toString());
        result.setText(postFull.getText());
        String[] coordinates = postFull.getGeo().getCoordinates().split(" ");
        result.setLatitude(Float.parseFloat(coordinates[0]));
        result.setLongitude(Float.parseFloat(coordinates[1]));

        UserInfo userInfo = new UserInfo(
                userFull.getId(),
                userFull.getFirstName(),
                userFull.getLastName(),
                userFull.getPhoto200()
        );
        result.setUser(userInfo);

        Place place = postFull.getGeo().getPlace();
        if ((place != null) && (!place.getId().equals(0))) {
            PlaceInfo placeInfo = new PlaceInfo(
                    place.getId(),
                    place.getTitle(),
                    place.getLongitude(),
                    place.getLatitude(),
                    place.getIcon());
            result.setPlace(placeInfo);
        }

        return result;
    }

    public CheckinInfo() {
    }

    public String getCheckinId() {
        return checkinId;
    }

    public void setCheckinId(String checkinId) {
        this.checkinId = checkinId;
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

    public UserInfo getUser() { return user; }

    public void setUser(UserInfo user) { this.user = user; }

    public PlaceInfo getPlace() { return place; }

    public void setPlace(PlaceInfo place) { this.place = place; }
}

