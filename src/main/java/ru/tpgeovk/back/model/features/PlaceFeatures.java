package ru.tpgeovk.back.model.features;

public class PlaceFeatures {

    private Integer placeId;
    private Integer checkinsCount;
    private Integer distance;
    private Float sameAgePercent;
    private Float sameGenderPercent;
    private Float groupsSimilarity;
    private Float textSimilarity;

    public PlaceFeatures() { }

    public Integer getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Integer placeId) {
        this.placeId = placeId;
    }

    public Integer getCheckinsCount() {
        return checkinsCount;
    }

    public void setCheckinsCount(Integer checkinsCount) {
        this.checkinsCount = checkinsCount;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Float getSameAgePercent() {
        return sameAgePercent;
    }

    public void setSameAgePercent(Float sameAgePercent) {
        this.sameAgePercent = sameAgePercent;
    }

    public Float getSameGenderPercent() {
        return sameGenderPercent;
    }

    public void setSameGenderPercent(Float sameGenderPercent) {
        this.sameGenderPercent = sameGenderPercent;
    }

    public Float getGroupsSimilarity() {
        return groupsSimilarity;
    }

    public void setGroupsSimilarity(Float groupsSimilarity) {
        this.groupsSimilarity = groupsSimilarity;
    }

    public Float getTextSimilarity() { return textSimilarity; }

    public void setTextSimilarity(Float textSimilarity) { this.textSimilarity = textSimilarity; }
}
