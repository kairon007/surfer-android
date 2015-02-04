package com.trysurfer.surfer.model;

/**
 * Created by PRO on 10/9/2014.
 */
import java.io.Serializable;

public class UserDAO implements Serializable{

    private static final long serialVersionUID = 1L;

    private long id;
    private String fb_id;
    private String email;
    private String auth_token;
    private String gender;
    private String birthday;

    public UserDAO(){
    }

    public UserDAO(String fb_id){
        setFbId(fb_id);
    }

    public UserDAO(long id, String fb_id, String email, String auth_token, String birthday, String gender){
        setId(id);
        setFbId(fb_id);
        setEmail(email);
        setAuth_token(auth_token);
        setBirthday(birthday);
        setGender(gender);
    }

    public long getId(){
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFbId(){
        return fb_id;
    }

    public void setFbId(String fbId) {
        this.fb_id = fbId;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAuth_token() {
        return auth_token;
    }

    public void setAuth_token(String auth_token) {
        this.auth_token = auth_token;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBirthday() {
        return birthday;
    }

    public void setBirthday(String birthday) {
        this.birthday = birthday;
    }

    @Override
    public String toString(){
        return "UserDAO [id=" + id + ", fb_id=" + fb_id + ", email=" + email + "]";
    }
}
