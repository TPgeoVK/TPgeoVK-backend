package ru.tpgeovk.back.model.vk;

import com.google.gson.annotations.SerializedName;
import com.vk.api.sdk.objects.base.CommentsInfo;
import com.vk.api.sdk.objects.base.LikesInfo;
import com.vk.api.sdk.objects.base.RepostsInfo;
import com.vk.api.sdk.objects.wall.Wallpost;

import java.util.List;
import java.util.Objects;

public class VkWallpostFull extends VkWallpost {
    @SerializedName("copy_history")
    private List<Wallpost> copyHistory;
    @SerializedName("can_edit")
    private Integer canEdit;
    @SerializedName("created_by")
    private Integer createdBy;
    @SerializedName("can_delete")
    private Integer canDelete;
    @SerializedName("can_pin")
    private Integer canPin;
    @SerializedName("is_pinned")
    private Integer isPinned;
    @SerializedName("comments")
    private CommentsInfo comments;
    @SerializedName("likes")
    private LikesInfo likes;
    @SerializedName("reposts")
    private RepostsInfo reposts;

    public VkWallpostFull() {
    }

    public List<Wallpost> getCopyHistory() {
        return this.copyHistory;
    }

    public Integer getCanEdit() {
        return this.canEdit;
    }

    public Integer getCreatedBy() {
        return this.createdBy;
    }

    public Integer getCanDelete() {
        return this.canDelete;
    }

    public Integer getCanPin() {
        return this.canPin;
    }

    public Integer getIsPinned() {
        return this.isPinned;
    }

    public CommentsInfo getComments() {
        return this.comments;
    }

    public LikesInfo getLikes() {
        return this.likes;
    }

    public RepostsInfo getReposts() {
        return this.reposts;
    }

    public int hashCode() {
        return Objects.hash(new Object[]{super.hashCode(), this.comments, this.createdBy, this.isPinned, this.canEdit, this.canDelete, this.canPin, this.reposts, this.copyHistory, this.likes});
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            if (!super.equals(o)) {
                return false;
            } else {
                VkWallpostFull wallpostFull = (VkWallpostFull)o;
                return Objects.equals(this.copyHistory, wallpostFull.copyHistory) && Objects.equals(this.canEdit, wallpostFull.canEdit) && Objects.equals(this.createdBy, wallpostFull.createdBy) && Objects.equals(this.canDelete, wallpostFull.canDelete) && Objects.equals(this.canPin, wallpostFull.canPin) && Objects.equals(this.isPinned, wallpostFull.isPinned) && Objects.equals(this.comments, wallpostFull.comments) && Objects.equals(this.likes, wallpostFull.likes) && Objects.equals(this.reposts, wallpostFull.reposts);
            }
        } else {
            return false;
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("WallpostFull{");
        sb.append("copyHistory=").append(this.copyHistory);
        sb.append(", canEdit=").append(this.canEdit);
        sb.append(", createdBy=").append(this.createdBy);
        sb.append(", canDelete=").append(this.canDelete);
        sb.append(", canPin=").append(this.canPin);
        sb.append(", isPinned=").append(this.isPinned);
        sb.append(", comments=").append(this.comments);
        sb.append(", likes=").append(this.likes);
        sb.append(", reposts=").append(this.reposts);
        sb.append('}');
        return sb.toString();
    }
}
