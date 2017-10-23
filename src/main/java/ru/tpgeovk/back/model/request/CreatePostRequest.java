package ru.tpgeovk.back.model.request;

public class CreatePostRequest {

    private String token;
    private Integer placeId;
    private String text;

    public CreatePostRequest() { }

    public CreatePostRequest(String token, Integer placeId, String text) {
        this.token = token;
        this.placeId = placeId;
        this.text = text;
    }

    public String getToken() { return token; }

    public void setToken(String token) { this.token = token; }

    public Integer getPlaceId() { return placeId; }

    public void setPlaceId(Integer placeId) { this.placeId = placeId; }

    public String getText() { return text; }

    public void setText(String text) { this.text = text; }
}
