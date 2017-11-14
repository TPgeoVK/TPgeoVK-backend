package ru.tpgeovk.back.model;

import java.util.ArrayList;
import java.util.List;

public class UserFeatures {

    private Integer userId;
    private Integer age;
    private Boolean gender; // 0 - male, 1 - female
    private String religion;
    private List<String> groups = new ArrayList<>();

    public UserFeatures() { }

    public Integer getUserId() { return userId; }

    public void setUserId(Integer userId) { this.userId = userId; }

    public Integer getAge() { return age; }

    public void setAge(Integer age) { this.age = age; }

    public Boolean getGender() { return gender; }

    public void setGender(Boolean gender) { this.gender = gender; }

    public String getReligion() { return religion; }

    public void setReligion(String religion) { this.religion = religion; }

    public List<String> getGroups() { return groups; }

    public void setGroups(List<String> groups) { this.groups = groups; }
}
