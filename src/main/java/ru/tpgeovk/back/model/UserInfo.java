package ru.tpgeovk.back.model;

import com.vk.api.sdk.objects.users.UserFull;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import org.springframework.util.StringUtils;

import java.util.Base64;

public class UserInfo {

    private Integer id;
    private String firstName;
    private String lastName;
    private String occupation;
    private String photo200;
    private String photo200Base64;

    public static UserInfo fromUserFull(UserFull userFull) {
        UserInfo result = new UserInfo();
        result.setId(userFull.getId());
        result.setFirstName(userFull.getFirstName());
        result.setLastName(userFull.getLastName());

        if ((userFull.getCareer() != null) && (userFull.getCareer().size() != 0)) {
            result.setOccupation(userFull.getCareer().get(0).getCompany());
        } else if (StringUtils.isEmpty(userFull.getUniversityName())) {
            result.setOccupation(userFull.getUniversityName());
        } else if ((userFull.getSchools() != null) && (userFull.getSchools().size() != 0)) {
            result.setOccupation(userFull.getSchools().get(0).getName());
        }

        result.setPhoto200(userFull.getPhoto200());

        return result;
    }

    public static UserInfo fromXtrCounters(UserXtrCounters userFull) {
        UserInfo result = new UserInfo();
        result.setId(userFull.getId());
        result.setFirstName(userFull.getFirstName());
        result.setLastName(userFull.getLastName());

        if ((userFull.getCareer() != null) && (userFull.getCareer().size() != 0)) {
            result.setOccupation(userFull.getCareer().get(0).getCompany());
        } else if (StringUtils.isEmpty(userFull.getUniversityName())) {
            result.setOccupation(userFull.getUniversityName());
        } else if ((userFull.getSchools() != null) && (userFull.getSchools().size() != 0)) {
            result.setOccupation(userFull.getSchools().get(0).getName());
        }

        result.setPhoto200(userFull.getPhoto200());

        return result;
    }

    public UserInfo() { }

    public UserInfo(Integer id, String firstName, String lastName, String occupation, String photo200) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.occupation = occupation;
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

    public String getOccupation() { return occupation; }

    public void setOccupation(String occupation) { this.occupation = occupation; }

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
