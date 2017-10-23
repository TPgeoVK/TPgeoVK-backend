package ru.tpgeovk.back.model.vk;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.base.Geo;
import com.vk.api.sdk.objects.wall.PostSource;
import com.vk.api.sdk.objects.wall.PostType;
import com.vk.api.sdk.objects.wall.Views;
import com.vk.api.sdk.objects.wall.WallpostAttachment;

import java.util.List;
import java.util.Objects;

public class VkWallpost {
    @SerializedName("id")
    private Integer id;
    @SerializedName("from_id")
    private Integer fromId;
    @SerializedName("owner_id")
    private Integer ownerId;
    @SerializedName("date")
    private Integer date;
    @SerializedName("views")
    private Views views;
    @SerializedName("post_type")
    private PostType postType;
    @SerializedName("text")
    private String text;
    @SerializedName("signer_id")
    private Integer signerId;
    @SerializedName("attachments")
    private List<WallpostAttachment> attachments;
    @SerializedName("geo")
    private VkGeo geo;
    @SerializedName("post_source")
    private PostSource postSource;

    public VkWallpost() {
    }

    public Integer getId() {
        return this.id;
    }

    public Integer getFromId() {
        return this.fromId;
    }

    public Integer getOwnerId() {
        return this.ownerId;
    }

    public Integer getDate() {
        return this.date;
    }

    public Views getViews() {
        return this.views;
    }

    public PostType getPostType() {
        return this.postType;
    }

    public String getText() {
        return this.text;
    }

    public Integer getSignerId() {
        return this.signerId;
    }

    public List<WallpostAttachment> getAttachments() {
        return this.attachments;
    }

    public VkGeo getGeo() {
        return this.geo;
    }

    public PostSource getPostSource() {
        return this.postSource;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.date, this.geo, this.signerId, this.attachments, this.postType, this.postSource, this.id, this.text, this.ownerId, this.fromId});
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            VkWallpost wallpost = (VkWallpost) o;
            return Objects.equals(this.id, wallpost.id) && Objects.equals(this.fromId, wallpost.fromId) && Objects.equals(this.ownerId, wallpost.ownerId) && Objects.equals(this.date, wallpost.date) && Objects.equals(this.views, wallpost.views) && Objects.equals(this.postType, wallpost.postType) && Objects.equals(this.text, wallpost.text) && Objects.equals(this.signerId, wallpost.signerId) && Objects.equals(this.attachments, wallpost.attachments) && Objects.equals(this.geo, wallpost.geo) && Objects.equals(this.postSource, wallpost.postSource);
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("Wallpost{");
        sb.append("id=").append(this.id);
        sb.append(", fromId=").append(this.fromId);
        sb.append(", ownerId=").append(this.ownerId);
        sb.append(", date=").append(this.date);
        sb.append(", views=").append(this.views);
        sb.append(", postType=").append(this.postType);
        sb.append(", text='").append(this.text).append("'");
        sb.append(", signerId=").append(this.signerId);
        sb.append(", attachments=").append(this.attachments);
        sb.append(", geo=").append(this.geo);
        sb.append(", postSource=").append(this.postSource);
        sb.append('}');
        return sb.toString();
    }
}
