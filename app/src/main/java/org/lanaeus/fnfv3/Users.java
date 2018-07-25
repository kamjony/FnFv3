package org.lanaeus.fnfv3;

/**
 * Created by KamrulHasan on 3/11/2018.
 */

class Users {
    public String name;
    public String image;
    public String email;
    public String status;
    public String thumb_image;
    public String name_lowercase;

    public Users() {
    }


    public Users(String name, String image, String status, String thumb_image, String email, String name_lowercase) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumb_image = thumb_image;
        this.email = email;
        this.name_lowercase = name_lowercase;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(){
        this.email = email;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumb_img() {
        return thumb_image;
    }

    public void setThumb_img(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getName_lowercase() {
        return name_lowercase;
    }

    public void setName_lowercase(String name_lowercase) {
        this.name_lowercase = name_lowercase;
    }
}
