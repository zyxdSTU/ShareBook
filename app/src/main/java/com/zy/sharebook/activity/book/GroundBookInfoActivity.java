package com.zy.sharebook.activity.book;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zy.sharebook.R;
import com.zy.sharebook.bean.Book;
import com.zy.sharebook.database.DatabaseHelper;
import com.zy.sharebook.network.HttpHelper;
import com.zy.sharebook.util.PreferenceManager;
import com.zy.sharebook.util.Util;
import com.zy.sharebook.util.imageloader.ImageLoader;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.zy.sharebook.util.Constant.GROUND_BOOK;
import static com.zy.sharebook.util.Constant.SELECT_BOOK;

public class GroundBookInfoActivity extends AppCompatActivity{
    private Book book;
    private TextView titleTextView;
    private TextView authorTextView;
    private TextView priceTextView;
    private TextView pubDateTextView;
    private TextView pagesTextView;
    private TextView isbnNumberTextView;
    private TextView summaryTextView;
    private TextView publisherTextView;

    private ImageView bookImageView;
    private Button groupButton;
    private Button undercarriageButton;
    private TextView borrowTextView;
    private GridView gridView;

    private static final int ACQUIRE_SUCCESS = 11;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case ACQUIRE_SUCCESS: {
                    setInfo();
                    break;
                }
                default: break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_gubook_info);
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        titleTextView = (TextView) findViewById(R.id.title_textView);
        authorTextView = (TextView) findViewById(R.id.author_textView);
        priceTextView = (TextView) findViewById(R.id.price_textView);
        pubDateTextView = (TextView) findViewById(R.id.pubDate_textView);
        pagesTextView = (TextView) findViewById(R.id.pages_textView);
        isbnNumberTextView = (TextView) findViewById(R.id.isbnNumber_textView);
        publisherTextView = (TextView) findViewById(R.id.publisher_textView);
        summaryTextView = (TextView) findViewById(R.id.summary_textView);
        bookImageView = (ImageView) findViewById(R.id.book_imageView);

        groupButton = (Button) findViewById(R.id.ground_button);

        undercarriageButton = (Button) findViewById(R.id.undercarriage_button);
        borrowTextView = (TextView) findViewById(R.id.borrow_textView);
        gridView = (GridView) findViewById(R.id.grid_view);

        undercarriageButton.setVisibility(View.GONE);
        borrowTextView.setVisibility(View.GONE);
        gridView.setVisibility(View.GONE);


        String isbnNumber = getIntent().getStringExtra("isbnNumber");
        book = DatabaseHelper.getDatabaseHelper().selectByIsbn(isbnNumber);
        if(book == null) {
            HttpHelper.sendOkHttpRequest(SELECT_BOOK + isbnNumber, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(GroundBookInfoActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String jsonBook = response.body().string();
                    book = new Gson().fromJson(jsonBook, Book.class);
                    Message msg = new Message();
                    msg.what = ACQUIRE_SUCCESS;
                    handler.sendMessage(msg);
                }
            });
        } else {
            setInfo();
        }

        groupButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                String phoneNumber = PreferenceManager.getInstance().preferenceManagerGet("currentPhoneNumber");
                String btime = Util.getCurrentTime();
                String url = GROUND_BOOK + "phoneNumber=" + phoneNumber + "&btime=" + btime;
                Log.d("GroundBookInfoActivity", url);
                HttpHelper.sendOKHttpPost(new Gson().toJson(book), url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        GroundBookInfoActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GroundBookInfoActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        GroundBookInfoActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(GroundBookInfoActivity.this, UndercarriageBookInfoActivity.class);
                                intent.putExtra("isbnNumber", book.getIsbnNumber());
                                startActivity(intent);
                                /*不能back*/
                                finish();
                            }
                        });
                    }
                });
            }
        });
    }

    public void setInfo() {
        titleTextView.setText("书名： " + dispose(book.getTitle()));
        authorTextView.setText("作者： " + dispose(book.getAuthor()));
        priceTextView.setText("价格： " + dispose(book.getPrice()));
        pubDateTextView.setText("出版日期：" + dispose(book.getPubDate()));
        pagesTextView.setText("页数： " + dispose(book.getPages()));
        isbnNumberTextView.setText("isbn码： "+ dispose(book.getIsbnNumber()));
        publisherTextView.setText("出版社： " + dispose(book.getPublisher()));
        summaryTextView.setText(book.getSummary());

        ImageLoader.getImageLoader(GroundBookInfoActivity.this).bindBitmap(book.getImage(), bookImageView, 105, 135);
    }

    public String dispose(String str) {
        StringBuilder builder = new StringBuilder();
        if(str.length() >= 22) {
            builder.append(str.substring(0, 20));
            builder.append("..");
            return builder.toString();
        }else return str;
    }
}
