package com.zy.sharebook.util;

import android.util.Log;

import com.zy.sharebook.bean.Book;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ZY on 2017/11/7.
 */

public class Util {
    public static Book parserBookXml(String bookXml) {
        //Log.d("MainActivity", bookXml);
        Book book = new Book();

        String stringOne = "<link href=\"(.*)\" rel=\"image\"/>";
        String stringTwo = "<db:attribute name=\"(.*)\">(.*)</db:attribute>";
        String stringThree = "<summary>(.*)";
        String stringFour = "(.*)</summary>";
        Pattern patternOne = Pattern.compile(stringOne);
        Pattern patternTwo = Pattern.compile(stringTwo);
        Pattern patternThree = Pattern.compile(stringThree);
        Pattern patternFour = Pattern.compile(stringFour);
        Scanner scanner = new Scanner(bookXml);
        boolean flag  = false;
        StringBuilder summary = new StringBuilder();
        while(scanner.hasNextLine()) {
            String line = scanner.nextLine().trim();
            Matcher matcherOne = patternOne.matcher(line);
            if(matcherOne.find()) {
                //Log.d(TAG, matcherOne.group(1));
                book.setImage(matcherOne.group(1));
                continue;
            }

            Matcher matcherTwo = patternTwo.matcher(line);
            if(matcherTwo.find()) {
                //Log.d("MainActivity", matcherTwo.group(1) + ": " + matcherTwo.group(2));
                if(matcherTwo.group(1).equals("isbn13")) {
                    book.setIsbnNumber(matcherTwo.group(2));
                }

                if(matcherTwo.group(1).equals("title")) {
                    book.setTitle(matcherTwo.group(2));
                }

                if(matcherTwo.group(1).equals("pages")) {
                    book.setPages(matcherTwo.group(2));
                }

                if(matcherTwo.group(1).equals("author")) {
                    if(book.getAuthor() != null) {
                        book.setAuthor(book.getAuthor() + "、" + matcherTwo.group(2));
                    }
                    book.setAuthor(matcherTwo.group(2));
                }

                if(matcherTwo.group(1).equals("price")) {
                    book.setPrice(matcherTwo.group(2));
                }

                if(matcherTwo.group(1).equals("publisher")) {
                    book.setPublisher(matcherTwo.group(2));
                }

                if(matcherTwo.group(1).equals("pubdate")) {
                    book.setPubDate(matcherTwo.group(2));
                }

                continue;
            }

            Matcher matcherThree = patternThree.matcher(line);
            if(matcherThree.find()) {
                flag = true;
                summary.append(matcherThree.group(1));
                continue;
            }

            Matcher matcherFour = patternFour.matcher(line);
            if(matcherFour.find()) {
                flag = false;
                summary.append(matcherFour.group(1));
                continue;
            }

            if(flag) {
                summary.append(line);
                continue;
            }
        }
        book.setSummary(summary.toString());
        return book;
    }

    /*保证11位数字的电话号码*/
    public static boolean isPhoneNumber(String phoneNumber) {
        if(phoneNumber.length() != 11) return false;
        for(int i = 0; i < phoneNumber.length(); i++) {
            if(phoneNumber.charAt(i) < '0' || phoneNumber.charAt(i) > '9') return false;
        }
        return true;
    }

    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentTime() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
        return df.format(new Date());
    }
}

