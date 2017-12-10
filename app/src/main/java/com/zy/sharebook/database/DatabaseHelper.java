package com.zy.sharebook.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.zy.sharebook.bean.Book;

import static com.zy.sharebook.util.Constant.CREATE_BOOK;

/**
 * Created by ZY on 2017/11/25.
 */

public class DatabaseHelper extends SQLiteOpenHelper {
    private Context mContext;
    private static DatabaseHelper databaseHelper;

    public static DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public static void init(Context context) {
        if(databaseHelper == null) {
            databaseHelper = new DatabaseHelper(context, "bookstore.db", null, 1);
        }
    }

    public void insertBook(Book book) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("isbnNumber", book.getIsbnNumber());
        values.put("title", book.getTitle());
        values.put("author", book.getAuthor());
        values.put("pages", book.getPages());
        values.put("prices", book.getPrice());
        values.put("pubDate", book.getPubDate());
        values.put("publisher", book.getPublisher());
        values.put("image", book.getImage());
        values.put("summary", book.getSummary());
        values.put("tag", book.getTag());
        db.insert("book", null, values);
    }

    public Book selectByIsbn(String isbnNumber) {
        boolean flag = false;
        Book book = new Book();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query("book",
                new String[]{"isbnNumber","title", "author", "pages", "prices","pubDate", "publisher", "image", "summary", "tag"},
                "isbnNumber=?", new String[]{isbnNumber}, null, null, null);

        while(cursor.moveToNext()){
            flag = true;
            book.setIsbnNumber(isbnNumber);
            book.setTitle(cursor.getString(cursor.getColumnIndex("title")));
            book.setAuthor(cursor.getString(cursor.getColumnIndex("author")));
            book.setPages(cursor.getString(cursor.getColumnIndex("pages")));
            book.setPrice(cursor.getString(cursor.getColumnIndex("prices")));
            book.setPubDate(cursor.getString(cursor.getColumnIndex("pubDate")));
            book.setPublisher(cursor.getString(cursor.getColumnIndex("publisher")));
            book.setImage(cursor.getString(cursor.getColumnIndex("image")));
            book.setSummary(cursor.getString(cursor.getColumnIndex("summary")));
            book.setTag(cursor.getString(cursor.getColumnIndex("tag")));
        }

        if(!flag) return null;
        else return book;
    }

    public DatabaseHelper(Context context, String name,
                          SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        mContext = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_BOOK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
