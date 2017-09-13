package ru.tpgeovk.back.ru.tpgeovk.back.model.request;

public class PredictRequest {

    private Integer userId;
    private Float latitude;
    private Float longitude;
    private String text;

    public PredictRequest() { }

    public PredictRequest(Integer userId, Float latitude, Float longitude, String text) {
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.text = text;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
