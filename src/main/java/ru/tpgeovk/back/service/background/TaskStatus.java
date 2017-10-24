package ru.tpgeovk.back.service.background;

public class TaskStatus {

    protected Boolean friendsCompleted = false;
    protected Boolean placesCompleted = false;

    public TaskStatus() { }

    public TaskStatus(Boolean friendsCompleted, Boolean placesCompleted) {
        this.friendsCompleted = friendsCompleted;
        this.placesCompleted = placesCompleted;
    }

    public Boolean getFriendsCompleted() { return friendsCompleted; }

    public void setFriendsCompleted(Boolean friendsCompleted) { this.friendsCompleted = friendsCompleted; }

    public Boolean getPlacesCompleted() { return placesCompleted; }

    public void setPlacesCompleted(Boolean placesCompleted) { this.placesCompleted = placesCompleted; }
}
