package ru.tpgeovk.back.model.vk;

import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class VkPlaceFull {
    @SerializedName("id")
    private Integer id;
    @SerializedName("title")
    private String title;
    @SerializedName("latitude")
    private Float latitude;
    @SerializedName("longitude")
    private Float longitude;
    @SerializedName("created")
    private Integer created;
    @SerializedName("icon")
    private String icon;
    @SerializedName("checkins")
    private Integer checkins;
    @SerializedName("type")
    private String type;
    @SerializedName("country")
    private String country;
    @SerializedName("city")
    private String city;
    @SerializedName("address")
    private String address;
    @SerializedName("distance")
    private Integer distance;
    @SerializedName("group_id")
    private Integer groupId;
    @SerializedName("group_photo")
    private String groupPhoto;

    public VkPlaceFull() {
    }

    public Integer getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public Float getLatitude() {
        return this.latitude;
    }

    public Float getLongitude() {
        return this.longitude;
    }

    public Integer getCreated() {
        return this.created;
    }

    public String getIcon() {
        return this.icon;
    }

    public Integer getCheckins() {
        return this.checkins;
    }

    public String getType() {
        return this.type;
    }

    public String getCountry() {
        return this.country;
    }

    public String getCity() {
        return this.city;
    }

    public String getAddress() {
        return this.address;
    }

    public Integer getDistance() {
        return this.distance;
    }

    public Integer getGroupId() {
        return this.groupId;
    }

    public String getGroupPhoto() {
        return this.groupPhoto;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.country, this.address, this.distance, this.city, this.created, this.latitude, this.groupId, this.icon, this.groupPhoto, this.title, this.type, this.id, this.checkins, this.longitude});
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            VkPlaceFull placeFull = (VkPlaceFull) o;
            return Objects.equals(this.id, placeFull.id) && Objects.equals(this.title, placeFull.title) && Objects.equals(this.latitude, placeFull.latitude) && Objects.equals(this.longitude, placeFull.longitude) && Objects.equals(this.created, placeFull.created) && Objects.equals(this.icon, placeFull.icon) && Objects.equals(this.checkins, placeFull.checkins) && Objects.equals(this.type, placeFull.type) && Objects.equals(this.country, placeFull.country) && Objects.equals(this.city, placeFull.city) && Objects.equals(this.address, placeFull.address) && Objects.equals(this.distance, placeFull.distance) && Objects.equals(this.groupId, placeFull.groupId) && Objects.equals(this.groupPhoto, placeFull.groupPhoto);
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("PlaceFull{");
        sb.append("id=").append(this.id);
        sb.append(", title='").append(this.title).append("'");
        sb.append(", latitude=").append(this.latitude);
        sb.append(", longitude=").append(this.longitude);
        sb.append(", created=").append(this.created);
        sb.append(", icon='").append(this.icon).append("'");
        sb.append(", checkins=").append(this.checkins);
        sb.append(", type='").append(this.type).append("'");
        sb.append(", country=").append(this.country);
        sb.append(", city=").append(this.city);
        sb.append(", address='").append(this.address).append("'");
        sb.append(", distance=").append(this.distance);
        sb.append(", groupId=").append(this.groupId);
        sb.append(", groupPhoto='").append(this.groupPhoto).append("'");
        sb.append('}');
        return sb.toString();
    }
}

