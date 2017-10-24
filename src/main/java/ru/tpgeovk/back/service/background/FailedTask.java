package ru.tpgeovk.back.service.background;

public class FailedTask extends TaskStatus {

    protected String error;

    public FailedTask(Boolean friendsCompleted, Boolean placesCompleted, String error) {
        super(friendsCompleted, placesCompleted);
        this.error = error;
    }

    public String getError() { return error; }
}
