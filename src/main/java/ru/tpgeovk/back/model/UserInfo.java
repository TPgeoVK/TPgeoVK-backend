package ru.tpgeovk.back.model;

import com.vk.api.sdk.objects.users.UserFull;

import java.util.Base64;

public class UserInfo {

    private Integer id;
    private String firstName;
    private String lastName;
    private String photo200;
    private String photo200Base64;

    public static UserInfo fromUserFull(UserFull userFull) {
        UserInfo result = new UserInfo();
        result.setId(userFull.getId());
        result.setFirstName(userFull.getFirstName());
        result.setLastName(userFull.getLastName());
        result.setPhoto200(userFull.getPhoto200());

        return result;
    }

    public UserInfo() { }

    public UserInfo(Integer id, String firstName, String lastName, String photo200) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photo200 = photo200;
        this.photo200Base64 = Base64.getEncoder().encodeToString(photo200.getBytes());
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

    public String getPhoto200Base64() { return photo200Base64; }

    public void setPhoto200(String photo200) {
        this.photo200 = photo200;
        if (photo200 != null) {
            this.photo200Base64 = Base64.getEncoder().encodeToString(photo200.getBytes());
        } else {
            this.photo200Base64 = null;
        }
    }
}
