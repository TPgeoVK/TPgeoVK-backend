package ru.tpgeovk.back.model;

public class CityCountry {

    private String city;
    private String country;

    public CityCountry() { }

    public CityCountry(String city, String country) {
        this.city = city;
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
}
