package com.zy.sharebook.bean;

/*
 * Created by ZY on 2017/11/20.
 */

public class HBookStore {
    private String phoneNumber;
    private String isbnNumber;
    private String btime;
    private int flag;

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public void setIsbnNumber(String isbnNumber) {
        this.isbnNumber = isbnNumber;
    }

    public void setBtime(String btime) {
        this.btime = btime;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getIsbnNumber() {
        return isbnNumber;
    }

    public String getBtime() {
        return btime;
    }
}