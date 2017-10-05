package ru.tpgeovk.back.ru.model.request;

public class PredictRequest {

    private String token;
    private Float latitude;
    private Float longitude;
    private String text;

    public PredictRequest() { }

    public PredictRequest(String token, Float latitude, Float longitude, String text) {
        this.token = token;
        this.latitude = latitude;
        this.longitude = longitude;
        this.text = text;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) { this.token = token; }

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
