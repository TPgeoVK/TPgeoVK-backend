package ru.tpgeovk.back.model;

public class UserInfo {

    private Integer id;
    private String firstName;
    private String lastName;
    private String photo200;

    public UserInfo() { }

    public UserInfo(Integer id, String firstName, String lastName, String photo200) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photo200 = photo200;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoto200() {
        return photo200;
    }

    public void setPhoto200(String photo200) {
        this.photo200 = photo200;
    }
}
