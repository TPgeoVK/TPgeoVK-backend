package ru.tpgeovk.back.model;

import com.vk.api.sdk.objects.places.PlaceFull;

public class FullPlaceInfo {

    private static final Integer MAX_DISTANCE = 300;

    private Integer id;
    private String title;
    private Float latitude;
    private Float longitude;
    private String placeIcon;
    private Integer distance;
    private Integer groupId;
    private String groupPhoto;
    private Integer checkinsCount;
    private Integer userCheckinsCount;
    private Integer friendsCheckinsCount;
    private Integer textRating;
    private Integer similarityRating;

    public static FullPlaceInfo fromPlaceFull(PlaceFull placeFull) {
        FullPlaceInfo result = new FullPlaceInfo();
        result.setId(placeFull.getId());
        result.setTitle(placeFull.getTitle());
        result.setLatitude(placeFull.getLatitude());
        result.setLongitude(placeFull.getLongitude());
        result.setPlaceIcon(placeFull.getIcon());
        result.setDistance(placeFull.getDistance());
        result.setCheckinsCount(placeFull.getCheckins());
        result.setGroupId(placeFull.getGroupId());
        result.setGroupPhoto(placeFull.getGroupPhoto());
        result.setUserCheckinsCount(0);
        result.setFriendsCheckinsCount(0);
        result.setTextRating(0);
        result.setSimilarityRating(0);

        return result;
    }

    public static FullPlaceInfo fromPlaceFull(PlaceFull placeFull, Integer similarityRating) {
        FullPlaceInfo result = new FullPlaceInfo();
        result.setId(placeFull.getId());
        result.setTitle(placeFull.getTitle());
        result.setLatitude(placeFull.getLatitude());
        result.setLongitude(placeFull.getLongitude());
        result.setPlaceIcon(placeFull.getIcon());
        result.setDistance(placeFull.getDistance());
        result.setCheckinsCount(placeFull.getCheckins());
        result.setGroupId(placeFull.getGroupId());
        result.setGroupPhoto(placeFull.getGroupPhoto());
        result.setUserCheckinsCount(0);
        result.setFriendsCheckinsCount(0);
        result.setTextRating(0);
        result.setSimilarityRating(similarityRating);

        return result;
    }

    public FullPlaceInfo() { }

    public Integer calculateRating() {
        /** TODO: обучить коэффициенты */
        return 5*(MAX_DISTANCE - distance) + 4*similarityRating + 3*textRating;
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

    public String getPlaceIcon() { return placeIcon; }

    public void setPlaceIcon(String placeIcon) { this.placeIcon = placeIcon; }

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

    public String getGroupPhoto() { return groupPhoto; }

    public void setGroupPhoto(String groupPhoto) { this.groupPhoto = groupPhoto; }

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

    public Integer getSimilarityRating() { return similarityRating; }

    public void setSimilarityRating(Integer similarityRating) { this.similarityRating = similarityRating; }
}
