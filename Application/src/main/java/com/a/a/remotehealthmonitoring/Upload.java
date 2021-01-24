// For uploading basic info to the firebase Database
// This intermediate class is called from 'AfterSignupActivity' - to upload the Name, Age, BloodGroup, ImageUrl and Email
// to the firebase database

package com.a.a.remotehealthmonitoring;

public class Upload {
    private String Name;
    private String Age;
    private String BloodGroup;
    private String ImageUrl;
    private String Email;

    public Upload() {
        // empty constructor needed
    }

    public Upload(String name, String age, String bloodGroup, String imageUrl, String email) {
        if(name.trim().equals("")) {
            name="No Name";
        }
        if(age.trim().equals("")) {
            age="No Age";
        }
        if(bloodGroup.trim().equals("")) {
            bloodGroup="No BloodGroup";
        }
        Name=name;
        Age=age;
        BloodGroup=bloodGroup;
        ImageUrl=imageUrl;
        Email=email;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getAge() {
        return Age;
    }

    public void setAge(String age) {
        Age = age;
    }

    public String getBloodGroup() {
        return BloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        BloodGroup = bloodGroup;
    }

    public String getImageUrl() {
        return ImageUrl;
    }

    public void setImageUrl(String imageUrl) {
        ImageUrl = imageUrl;
    }

    public String getEmail() {
        return Email;
    }

    public void setEmail(String email) {
        Email = email;
    }
}
