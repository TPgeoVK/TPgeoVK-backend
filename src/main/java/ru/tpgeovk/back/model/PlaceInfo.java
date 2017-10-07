package ru.tpgeovk.back.model;

import com.vk.api.sdk.objects.places.PlaceFull;

public class PlaceInfo {

    private static final Integer MAX_DISTANCE = 300;

    private Integer id;
    private String title;
    private Float latitude;
    private Float longitude;
    private Integer distance;
    private Integer groupId;
    private Integer checkinsCount;
    private Integer userCheckinsCount;
    private Integer friendsCheckinsCount;
    private Integer textRating;

    public PlaceInfo(Integer id, String title, Float latitude, Float longitude, Integer distance,
                     Integer checkinsCount, Integer groupId, Integer userCheckinsCount, Integer friendsCheckinsCount,
                     Integer textRating) {
        this.id = id;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.distance = distance;
        this.checkinsCount = checkinsCount;
        this.groupId = groupId;
        this.userCheckinsCount = userCheckinsCount;
        this.friendsCheckinsCount = friendsCheckinsCount;
        this.textRating = textRating;
    }

    public static PlaceInfo fromPlaceFull(PlaceFull placeFull) {
        return new PlaceInfo(placeFull.getId(), placeFull.getTitle(), placeFull.getLatitude(),
                placeFull.getLongitude(), placeFull.getDistance(), placeFull.getCheckins(), placeFull.getGroupId(),
                0,0, 0);
    }

    public Integer calculateRating() {
        /** TODO: обучить коэффициенты */
        return 5*(MAX_DISTANCE - distance) + 5*userCheckinsCount + 4*friendsCheckinsCount + 4*checkinsCount + 3*textRating;
    }

    public void updateUserCheckinsCount() {
        userCheckinsCount = userCheckinsCount + 1;
    }

    public void updateFriendsCheckinsCount() {
        friendsCheckinsCount = friendsCheckinsCount + 1;
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

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Integer getCheckinsCount() {
        return checkinsCount;
    }

    public void setCheckinsCount(Integer checkinsCount) {
        this.checkinsCount = checkinsCount;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getUserCheckinsCount() {
        return userCheckinsCount;
    }

    public void setUserCheckinsCount(Integer userCheckinsCount) {
        this.userCheckinsCount = userCheckinsCount;
    }

    public Integer getFriendsCheckinsCount() {
        return friendsCheckinsCount;
    }

    public void setFriendsCheckinsCount(Integer friendsCheckinsCount) {
        this.friendsCheckinsCount = friendsCheckinsCount;
    }

    public Integer getTextRating() {
        return textRating;
    }

    public void setTextRating(Integer textRating) {
        this.textRating = textRating;
    }
}
