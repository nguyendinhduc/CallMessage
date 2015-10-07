package com.modev.call;

/**
 * Created by ducnd on 29/09/2015.
 */
public class ContentContact {
    private String name, phoneNumber;
    public ContentContact ( String name, String phoneNumber ) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if ( !( o instanceof ContentContact) || !(((ContentContact)o).getPhoneNumber().equals(getPhoneNumber()))) return false;
        return true;
    }
}
