package com.zy.sharebook.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.zy.sharebook.R;
import com.zy.sharebook.activity.book.BaseBookInfoActivity;
import com.zy.sharebook.activity.book.GroundBookInfoActivity;
import com.zy.sharebook.activity.book.UndercarriageBookInfoActivity;
import com.zy.sharebook.adapter.FragmentAdapter;
import com.zy.sharebook.bean.Book;
import com.zy.sharebook.database.DatabaseHelper;
import com.zy.sharebook.fragment.BookStoreFragment;
import com.zy.sharebook.fragment.HistoryFragment;
import com.zy.sharebook.fragment.MyBookFragment;
import com.zy.sharebook.fragment.UserFragment;
import com.zy.sharebook.network.HttpHelper;
import com.zy.sharebook.util.PreferenceManager;
import com.zy.sharebook.util.Util;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.zy.sharebook.util.Constant.BOOK_URL;
import static com.zy.sharebook.util.Constant.CHOOSE_ROLE;

public class MainActivity extends AppCompatActivity implements  View.OnClickListener {
    private ImageButton bookstoreImageButton;
    private TextView bookstoreTextView;
    private ImageButton myBookImageButton;
    private TextView myBookTextView;
    private ImageButton historyImageButton;
    private TextView historyTextView;
    private ImageButton userImageButton;
    private TextView userTextView;


    private ViewPager viewPager;
    private ArrayList<Fragment> list = new ArrayList<>();

    private static final int GET_XML_SUCCESS = 11;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int id = msg.what;
            switch(id) {
                case GET_XML_SUCCESS:{
                    gotoActivity(Util.parserBookXml((String)msg.obj));
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
        setContentView(R.layout.activity_main);

        if(getSupportActionBar() != null) getSupportActionBar().hide();

        bookstoreImageButton = (ImageButton) findViewById(R.id.bookstore_ImageButton);
        bookstoreTextView = (TextView) findViewById(R.id.bookstore_textView);
        myBookImageButton = (ImageButton) findViewById(R.id.myBook_ImageButton);
        myBookTextView = (TextView) findViewById(R.id.myBook_textView);
        historyImageButton = (ImageButton) findViewById(R.id.history_ImageButton);
        historyTextView = (TextView) findViewById(R.id.history_textView);
        userImageButton = (ImageButton) findViewById(R.id.user_ImageButton);
        userTextView = (TextView) findViewById(R.id.user_textView);

        viewPager = (ViewPager) findViewById(R.id.view_pager);

        list.add(new BookStoreFragment());
        list.add(new MyBookFragment());
        list.add(new HistoryFragment());
        list.add(new UserFragment());

        bookstoreImageButton.setOnClickListener(this);
        myBookImageButton.setOnClickListener(this);
        historyImageButton.setOnClickListener(this);
        userImageButton.setOnClickListener(this);

        bookstoreTextView.setOnClickListener(this);
        myBookTextView.setOnClickListener(this);
        historyTextView.setOnClickListener(this);
        userTextView.setOnClickListener(this);

        FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), list);
        viewPager.setAdapter(fragmentAdapter);

        bookstoreImageButton.setSelected(true);
        bookstoreTextView.setTextColor(Color.parseColor("#3D5AFE"));

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener(){
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                changeTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id) {
            case R.id.bookstore_ImageButton:
            case R.id.bookstore_textView:
                changeTab(0);
                break;
            case R.id.myBook_ImageButton:
            case R.id.myBook_textView:
                changeTab(1);
                break;
            case R.id.history_ImageButton:
            case R.id.history_textView:
                changeTab(2);
                break;
            case R.id.user_textView:
            case R.id.user_ImageButton:
                changeTab(3);
                break;
            default:
                break;
        }
    }

    private void changeTab(int position) {
        bookstoreImageButton.setSelected(false);
        bookstoreTextView.setTextColor(Color.parseColor("#8a8a8a"));
        myBookImageButton.setSelected(false);
        myBookTextView.setTextColor(Color.parseColor("#8a8a8a"));
        historyImageButton.setSelected(false);
        historyTextView.setTextColor(Color.parseColor("#8a8a8a"));
        userImageButton.setSelected(false);
        userTextView.setTextColor(Color.parseColor("#8a8a8a"));

        switch(position) {
            case 0:
                bookstoreImageButton.setSelected(true);
                bookstoreTextView.setTextColor(Color.parseColor("#3D5AFE"));
                break;
            case 1:
                myBookImageButton.setSelected(true);
                myBookTextView.setTextColor(Color.parseColor("#3D5AFE"));
                break;
            case 2:
                historyImageButton.setSelected(true);
                historyTextView.setTextColor(Color.parseColor("#3D5AFE"));
                break;
            case 3:
                userImageButton.setSelected(true);
                userTextView.setTextColor(Color.parseColor("#3D5AFE"));
                break;
            default:
                break;
        }
        viewPager.setCurrentItem(position);
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "扫码取消！", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "扫描成功，条码值: " + result.getContents(), Toast.LENGTH_LONG).show();

                HttpHelper.sendOkHttpRequest(BOOK_URL + result.getContents(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String bookXml = response.body().string();
                        if(bookXml == null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "抱歉,数据库没有此图书信息", Toast.LENGTH_LONG).show();
                                }
                            });
                        }else {
                            Message msg = new Message();
                            msg.obj = bookXml;
                            msg.what = GET_XML_SUCCESS;
                            handler.sendMessage(msg);
                        }
                    }
                });
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void gotoActivity(final Book book) {
        String phoneNumber = PreferenceManager.getInstance().preferenceManagerGet("currentPhoneNumber");
        String url = CHOOSE_ROLE + "isbnNumber=" + book.getIsbnNumber() + "&phoneNumber=" +phoneNumber;

        HttpHelper.sendOkHttpRequest(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
               runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                        /*one 图书所有者  two 图书所有者，但书被借出 three 第三者 four 借书者*/
                String result = response.body().string();
                if(result.equals("one")) {
                    Intent intent = new Intent(MainActivity.this, UndercarriageBookInfoActivity.class);
                    intent.putExtra("isbnNumber", book.getIsbnNumber());
                    startActivity(intent);
                }else if(result.equals("two") || result.equals("four")) {
                    Intent intent = new Intent(MainActivity.this, BaseBookInfoActivity.class);
                    intent.putExtra("isbnNumber", book.getIsbnNumber());
                    startActivity(intent);
                }else if(result.equals("three")) {
                    Intent intent = new Intent(MainActivity.this, GroundBookInfoActivity.class);
                    intent.putExtra("isbnNumber", book.getIsbnNumber());
                    startActivity(intent);
                }
            }
        });
    }
}
