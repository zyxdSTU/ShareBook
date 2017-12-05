package com.zy.sharebook.activity.book;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zy.sharebook.R;
import com.zy.sharebook.activity.OthersInfoActivity;
import com.zy.sharebook.bean.Account;
import com.zy.sharebook.bean.Book;
import com.zy.sharebook.database.DatabaseHelper;
import com.zy.sharebook.fragment.BookStoreFragment;
import com.zy.sharebook.network.HttpHelper;
import com.zy.sharebook.util.imageloader.ImageLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.zy.sharebook.util.Constant.IMAGE_URL;
import static com.zy.sharebook.util.Constant.SELECT_ACCOUNT;
import static com.zy.sharebook.util.Constant.SELECT_BOOK;
import static com.zy.sharebook.util.Constant.SELECT_OWNERS;

public class OthersBookInfoActivity extends AppCompatActivity implements  AbsListView.OnScrollListener{
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

    private Button groundButton;
    private Button undercarriageButton;
    private GridView gridView;
    private GridViewAdapter adapter;

    private ArrayList<String> mList = new ArrayList<>();
    private static final int ACQUIRE_SUCCESS_PHONENUMBER = 22;
    private static final int ACCOUNT_INFO = 33;
    private String isbnNumber;
    private static final int  ACQUIRE_SUCCESS = 11;
    private boolean mIsGridViewIdle = true;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case ACQUIRE_SUCCESS: {
                    setInfo();
                    break;
                }
                case ACQUIRE_SUCCESS_PHONENUMBER: {
                    adapter.notifyDataSetChanged();
                    break;
                }
                default:break;
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

        gridView = (GridView) findViewById(R.id.grid_view);
        groundButton = (Button) findViewById(R.id.ground_button);
        undercarriageButton = (Button) findViewById(R.id.undercarriage_button);
        groundButton.setVisibility(View.GONE); undercarriageButton.setVisibility(View.GONE);
        isbnNumber = getIntent().getStringExtra("isbnNumber");

        book = DatabaseHelper.getDatabaseHelper().selectByIsbn(isbnNumber);
        if(book == null) {
              /*网络加载*/
            HttpHelper.sendOkHttpRequest(SELECT_BOOK + isbnNumber, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OthersBookInfoActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String jsonBook = response.body().string();
                    book = new Gson().fromJson(jsonBook, Book.class);
                        /*存储进数据库*/
                    DatabaseHelper.getDatabaseHelper().insertBook(book);
                    Message msg = new Message();
                    msg.what = ACQUIRE_SUCCESS;
                    handler.sendMessage(msg);
                }
            });
        } else setInfo();


        /*设置被借书者信息*/
        adapter = new GridViewAdapter(this, mList);
        gridView.setAdapter(adapter);
        getIntentData();

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent(OthersBookInfoActivity.this, OthersInfoActivity.class);
                intent.putExtra("phoneNumber", mList.get(i));
                intent.putExtra("type", "borrow");
                startActivity(intent);
            }
        });
    }

    public void getIntentData() {
        HttpHelper.sendOkHttpRequest(SELECT_OWNERS + isbnNumber, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                OthersBookInfoActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(OthersBookInfoActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonPhoneNumberList = response.body().string();
                Log.d("OthersBookInfoActivity", jsonPhoneNumberList);
                List<String> phoneNumberList = new Gson().fromJson(jsonPhoneNumberList, new TypeToken<List<String>>(){}.getType());
                Log.d("OthersBookInfoActivity", String.valueOf(phoneNumberList.size()));
                mList.clear();
                mList.addAll(phoneNumberList);
                Message msg = new Message(); msg.what = ACQUIRE_SUCCESS_PHONENUMBER;
                handler.sendMessage(msg);
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
        ImageLoader.getImageLoader(OthersBookInfoActivity.this).bindBitmap(book.getImage(), bookImageView, 140, 180);
    }

    public String dispose(String str) {
        StringBuilder builder = new StringBuilder();
        if(str.length() >= 25) {
            builder.append(str.substring(0, 22));
            builder.append("...");
            return builder.toString();
        }else return str;
    }

    private static class ViewHolder {
        public ImageView image;
        public TextView nameTextView;
    }

    private class GridViewAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<String> mList;
        private LayoutInflater layoutInflater;

        public GridViewAdapter(Context context, ArrayList<String> mList) {
            this.mContext = context;
            layoutInflater = LayoutInflater.from(mContext);
            this.mList = mList;
        }

        @Override
        public Object getItem(int i) {
            return mList.get(i);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.item_user, viewGroup, false);
                holder = new ViewHolder();
                holder.image = (ImageView) convertView.findViewById(R.id.image);
                holder.nameTextView = (TextView) convertView.findViewById(R.id.name_textView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ImageView image = holder.image;
            final TextView nameTextView = holder.nameTextView;
            final String phoneNumber = mList.get(position);

            /*网络加载*/
            HttpHelper.sendOkHttpRequest(SELECT_ACCOUNT + phoneNumber, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    OthersBookInfoActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OthersBookInfoActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, final Response response) throws IOException {
                    String jsonAccount = response.body().string();
                    final Account account = new Gson().fromJson(jsonAccount, Account.class);
                    OthersBookInfoActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            nameTextView.setText(account.getName());
                            String tag = (String) image.getTag();
                            if (account.getImage() == null) {
                                image.setImageDrawable(mContext.getResources().getDrawable(R.drawable.test_image));
                            } else {
                                if (!(IMAGE_URL + account.getImage()).equals(tag)) {
                                    image.setImageDrawable(mContext.getResources().getDrawable(R.drawable.test_image));
                                }
                                if (mIsGridViewIdle) {
                                    image.setTag(IMAGE_URL  + account.getImage());
                                    ImageLoader.getImageLoader(mContext).bindBitmap(IMAGE_URL + account.getImage(), image, 100, 100);
                                }
                            }
                        }
                    });
                }
            });
            return convertView;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            mIsGridViewIdle = true;
            adapter.notifyDataSetChanged();
        } else {
            mIsGridViewIdle = false;
        }
    }
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {}
}
