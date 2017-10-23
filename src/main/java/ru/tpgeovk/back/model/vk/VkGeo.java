package ru.tpgeovk.back.model.vk;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.base.Geo;
import com.vk.api.sdk.objects.base.Place;
import com.vk.api.sdk.objects.places.PlaceFull;

import java.util.Objects;

public class VkGeo {
    @SerializedName("type")
    private String type;
    @SerializedName("coordinates")
    private String coordinates;
    @SerializedName("place")
    private PlaceFull place;
    @SerializedName("showmap")
    private Integer showmap;

    public VkGeo() {
    }

    public String getType() {
        return this.type;
    }

    public String getCoordinates() {
        return this.coordinates;
    }

    public PlaceFull getPlace() {
        return this.place;
    }

    public Integer getShowmap() {
        return this.showmap;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.showmap, this.coordinates, this.place, this.type});
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            VkGeo geo = (VkGeo)o;
            return Objects.equals(this.type, geo.type) && Objects.equals(this.coordinates, geo.coordinates) && Objects.equals(this.place, geo.place) && Objects.equals(this.showmap, geo.showmap);
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Geo{");
        sb.append("type='").append(this.type).append("'");
        sb.append(", coordinates='").append(this.coordinates).append("'");
        sb.append(", place=").append(this.place);
        sb.append(", showmap=").append(this.showmap);
        sb.append('}');
        return sb.toString();
    }
}