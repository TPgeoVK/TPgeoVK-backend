package ru.tpgeovk.back.model;

import com.vk.api.sdk.objects.groups.Group;
import com.vk.api.sdk.objects.groups.GroupFull;

public class GroupInfo {

    private String id;
    private String name;
    private String description;
    private Integer membersCount;
    private Long friendsCount;
    private Integer placeId;
    private String placeTitle;
    private Float longitude;
    private Float latitude;

    public static GroupInfo fromGroup(Group group) {
        GroupInfo res = new GroupInfo();
        res.setId(group.getId());
        res.setName(group.getName());
        return res;
    }

    public static GroupInfo fromGroupFull(GroupFull groupFull) {
        GroupInfo res = new GroupInfo();
        res.setId(groupFull.getId());
        res.setName(groupFull.getName());
        res.setDescription(groupFull.getDescription());
        res.setMembersCount(groupFull.getMembersCount());
        if (groupFull.getPlace() != null) {
            res.setPlaceId(groupFull.getPlace().getId());
            res.setLongitude(groupFull.getPlace().getLongitude());
            res.setLatitude(groupFull.getPlace().getLatitude());
            res.setPlaceTitle(groupFull.getPlace().getTitle());
        }
        return res;
    }

    public GroupInfo() { }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMembersCount() {
        return membersCount;
    }

    public void setMembersCount(Integer membersCount) {
        this.membersCount = membersCount;
    }

    public Long getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(Long friendsCount) { this.friendsCount = friendsCount; }

    public Integer getPlaceId() { return placeId; }

    public void setPlaceId(Integer placeId) { this.placeId = placeId; }

    public String getPlaceTitle() { return placeTitle; }

    public void setPlaceTitle(String placeTitle) { this.placeTitle = placeTitle; }

    public Float getLongitude() { return longitude; }

    public void setLongitude(Float longitude) { this.longitude = longitude; }

    public Float getLatitude() { return latitude; }

    public void setLatitude(Float latitude) { this.latitude = latitude; }
}
