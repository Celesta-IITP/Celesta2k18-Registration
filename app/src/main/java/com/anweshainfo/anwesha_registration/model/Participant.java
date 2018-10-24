package com.anweshainfo.anwesha_registration.model;

/**
 * Created by mayank on 22/1/18.
 */

public class Participant {
    private String name;
    private String clstid;
    private  String phoneNumber;

    public Participant(String name, String anwid, String phoneNumber) {
        this.name = name;
        this.clstid = anwid;
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAnwid() {
        return clstid;
    }

    public void setAnwid(String anwid) {
        this.clstid = anwid;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
