package ru.tpgeovk.back.model;

import com.vk.api.sdk.objects.base.Place;
import com.vk.api.sdk.objects.places.Checkin;
import com.vk.api.sdk.objects.places.PlaceFull;
import com.vk.api.sdk.objects.users.UserFull;
import com.vk.api.sdk.objects.wall.WallpostFull;
import ru.tpgeovk.back.model.vk.VkPlaceFull;
import ru.tpgeovk.back.model.vk.VkWallpostFull;

public class CheckinInfo {

    private String checkinId;
    private Integer postId;
    private Integer userId;
    private String date;
    private Integer likes;
    private Integer reposts;
    private String text;

    private UserInfo user;
    private PlaceInfo place;

    public static CheckinInfo fromPostFull(VkWallpostFull postFull) {
        CheckinInfo result = new CheckinInfo();
        result.setCheckinId(postFull.getFromId().toString() + "_" + postFull.getId().toString());
        result.setPostId(postFull.getId());
        result.setUserId(postFull.getFromId());
        result.setDate(postFull.getDate().toString());
        result.setText(postFull.getText());
        result.setLikes(postFull.getLikes().getCount());
        result.setReposts(postFull.getReposts().getCount());

        if (postFull.getGeo() != null) {
            VkPlaceFull place = postFull.getGeo().getPlace();
            if (place != null) {
                if (!place.getId().equals(0)) {
                    PlaceInfo placeInfo = new PlaceInfo();
                    placeInfo.setId(place.getId());
                    placeInfo.setTitle(place.getTitle());
                    placeInfo.setLongitude(place.getLongitude());
                    placeInfo.setLatitude(place.getLatitude());
                    placeInfo.setPlaceIcon(place.getIcon());
                    if ((place.getGroupId() != null) && (!place.getGroupId().equals(0))) {
                        placeInfo.setGroupId(place.getGroupId());
                        placeInfo.setGroupPhoto(place.getGroupPhoto());
                    }
                    result.setPlace(placeInfo);

                    return result;
                }
                else {
                    PlaceInfo placeInfo = new PlaceInfo();
                    placeInfo.setId(0);
                    placeInfo.setTitle(place.getTitle());
                    String[] coordinates = postFull.getGeo().getCoordinates().split(" ");
                    placeInfo.setLatitude(Float.parseFloat(coordinates[0]));
                    placeInfo.setLongitude(Float.parseFloat(coordinates[1]));
                    if ((place.getGroupId() != null) && (!place.getGroupId().equals(0))) {
                        placeInfo.setGroupId(place.getGroupId());
                        placeInfo.setGroupPhoto(place.getGroupPhoto());
                    }
                    result.setPlace(placeInfo);

                    return result;
                }
            }

            PlaceInfo placeInfo = new PlaceInfo();
            String[] coordinates = postFull.getGeo().getCoordinates().split(" ");
            placeInfo.setLatitude(Float.parseFloat(coordinates[0]));
            placeInfo.setLongitude(Float.parseFloat(coordinates[1]));
            if ((place.getGroupId() != null) && (!place.getGroupId().equals(0))) {
                placeInfo.setGroupId(place.getGroupId());
                placeInfo.setGroupPhoto(place.getGroupPhoto());
            }
            result.setPlace(placeInfo);
        }

        return result;
    }

    public static CheckinInfo fromPostAndUser(VkWallpostFull postFull, UserFull userFull) {
        CheckinInfo result = CheckinInfo.fromPostFull(postFull);

        UserInfo userInfo = new UserInfo(
                userFull.getId(),
                userFull.getFirstName(),
                userFull.getLastName(),
                userFull.getPhoto200()
        );
        result.setUser(userInfo);

        return result;
    }

    public CheckinInfo() { }

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

    public Integer getUserId() { return userId; }

    public void setUserId(Integer userId) { this.userId = userId; }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public Integer getLikes() { return likes; }

    public void setLikes(Integer likes) { this.likes = likes; }

    public Integer getReposts() { return reposts; }

    public void setReposts(Integer reposts) { this.reposts = reposts; }
}

