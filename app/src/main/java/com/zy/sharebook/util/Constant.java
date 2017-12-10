package com.zy.sharebook.util;

/**
 * Created by ZY on 2017/11/7.
 */

public class Constant {
    /*图书查询接口*/
    public static final String BOOK_URL= "http://api.douban.com/book/subject/isbn/";
    public static final String ACCOUNT_VERIFY = "http://39.108.82.12:8080/ShareBook/login?";
    public static final String SELECT_ACCOUNT = "http://39.108.82.12:8080/ShareBook/selectByPhoneNumber?PhoneNumber=";
    public static final String REGISTER = "http://39.108.82.12:8080/ShareBook/register";
    public static final String SELECT_ALL_BOOK = "http://39.108.82.12:8080/ShareBook/selectAllBook";
    public static final String SELECT_BOOK = "http://39.108.82.12:8080/ShareBook/selectByIsbn?IsbnNumber=";
    public static final String IMAGE_URL = "http://39.108.82.12:8080/ShareBook/downloadImage?image=";
    public static final String UPLOAD_IMAGE = "http://39.108.82.12:8080/ShareBook/uploadImage?image=";
    public static final String SELECT_BOOKSTORE = "http://39.108.82.12:8080/ShareBook/selectBookStore?phoneNumber=";
    public static final String SELECT_HBOOKSTORE = "http://39.108.82.12:8080/ShareBook/selectHBookStore?phoneNumber=";
    public static final String CHOOSE_ROLE = "http://39.108.82.12:8080/ShareBook/chooseRole?";
    public static final String SELECT_OWNERS = "http://39.108.82.12:8080/ShareBook/selectOwners?isbnNumber=";
    public static final String UNDERCARRIAGE_BOOK = "http://39.108.82.12:8080/ShareBook/undercarriageBook?";
    public static final String GROUND_BOOK = "http://39.108.82.12:8080/ShareBook/groundBook?";
    public static final String SELECT_NAME = "http://39.108.82.12:8080/ShareBook/selectName?phoneNumber=";
    public static final String SELECT_OWNER = "http://39.108.82.12:8080/ShareBook/selectOwner?owner=";
    public static final String SELECT_BORROWER = "http://39.108.82.12:8080/ShareBook/selectBorrower?borrower=";
    public static final String APPLY = "http://39.108.82.12:8080/ShareBook/apply";
    public static final String COMPLETE = "http://39.108.82.12:8080/ShareBook/complete";
    public static final String UPDATE = "http://39.108.82.12:8080/ShareBook/update";
    public static final String UPDATE_ACCOUNT = "http://39.108.82.12:8080/ShareBook/updateAccount";

    /*创建数据库*/
    public static final String CREATE_BOOK = "create table book(" +
            "isbnNumber text primary key," +
            "title text," +
            "author text," +
            "pages text," +
            "prices text," +
            "pubDate text," +
            "publisher text," +
            "image text," +
            "summary text," +
            "tag text)";
}
