package com.anweshainfo.anwesha_registration.model;

/**
 * Created by mayank on 22/1/18.
 */

public class Participant {
    private String name;
    private String clstid;

    public Participant(String name, String anwid) {
        this.name = name;
        this.clstid = anwid;
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
}
