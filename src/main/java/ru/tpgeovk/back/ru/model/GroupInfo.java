package ru.tpgeovk.back.ru.model;

import com.vk.api.sdk.objects.groups.Group;
import com.vk.api.sdk.objects.groups.GroupFull;

public class GroupInfo {

    private String id;
    private String name;
    private String description;
    private Integer membersCount;
    private Long friendsCount;

    /** TODO: добавить id и координаты места */
    private String placeTitle;

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
            res.setPlaceTitle(groupFull.getPlace().getTitle());
        }
        return res;
    }

    public GroupInfo() { }

    public GroupInfo(String id, String name, String description, Integer membersCount, String placeTitle,
                     Long friendsCount) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.membersCount = membersCount;
        this.placeTitle = placeTitle;
        this.friendsCount = friendsCount;
    }

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

    public String getPlaceTitle() {
        return placeTitle;
    }

    public void setPlaceTitle(String placeTitle) {
        this.placeTitle = placeTitle;
    }

    public Long getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(Long friendsCount) {
        this.friendsCount = friendsCount;
    }
}
