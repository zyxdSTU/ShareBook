package com.zy.sharebook.bean;

/**
 * Created by Administrator on 2017/12/9.
 */

public class Borrow {
    private String isbnNumber;
    private String owner;
    private String borrower;
    private int status;
    private String btime;

    public Borrow() {}
    public Borrow(Borrow borrow) {
        this.isbnNumber = borrow.isbnNumber;
        this.owner = borrow.owner;
        this.borrower = borrow.borrower;
        this.status = borrow.status;
        this.btime = borrow.btime;
    }

    public void setIsbnNumber(String isbnNumber) {this.isbnNumber = isbnNumber;}
    public void setOwner(String owner) {this.owner = owner;}
    public void setBorrower(String borrower) {this.borrower = borrower;}
    public void setStatus(int status) {this.status = status;}
    public void setBtime(String btime) {this.btime = btime;}

    public String getIsbnNumber() {return isbnNumber;}
    public String getOwner() {return owner;}
    public String getBorrower() {return borrower;}
    public int getStatus() {return status;}
    public String getBtime() {return btime;}
}
