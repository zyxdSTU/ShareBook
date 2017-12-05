package com.zy.sharebook.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.zy.sharebook.R;
import com.zy.sharebook.activity.book.BaseBookInfoActivity;
import com.zy.sharebook.activity.book.OthersBookInfoActivity;
import com.zy.sharebook.activity.book.UndercarriageBookInfoActivity;
import com.zy.sharebook.bean.Book;
import com.zy.sharebook.bean.BookStore;
import com.zy.sharebook.database.DatabaseHelper;
import com.zy.sharebook.network.HttpHelper;
import com.zy.sharebook.util.PreferenceManager;
import com.zy.sharebook.util.imageloader.ImageLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import static com.zy.sharebook.util.Constant.CHOOSE_ROLE;
import static com.zy.sharebook.util.Constant.SELECT_ALL_BOOK;
import static com.zy.sharebook.util.Constant.SELECT_BOOK;


public class BookStoreFragment extends Fragment implements AbsListView.OnScrollListener {
    private View view;
    private GridView gridView;
    private boolean mIsGridViewIdle = true;
    private GridViewAdapter adapter;
    private static final int ACQUIRE_SUCCESS = 11;
    private ArrayList<String> dataList = new ArrayList<>();
    private TextView titleName;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case ACQUIRE_SUCCESS: {
                    adapter.notifyDataSetChanged();
                    break;
                }
                default: break;
            }
        }
    };
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_book_store, container, false);
        gridView = (GridView) view.findViewById(R.id.grid_view);
        titleName = (TextView) view.findViewById(R.id.titleName_textView);
        titleName.setText("共享书籍库");
        adapter = new GridViewAdapter(container.getContext(), dataList);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final String isbnNumber = dataList.get(i);
                String phoneNumber = PreferenceManager.getInstance().preferenceManagerGet("currentPhoneNumber");
                String url = CHOOSE_ROLE + "isbnNumber=" +isbnNumber + "&phoneNumber=" +phoneNumber;
                Log.d("BookStoreFragment", url);
                HttpHelper.sendOkHttpRequest(url, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        BookStoreFragment.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(BookStoreFragment.this.getActivity(), "网络错误", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String result = response.body().string();
                        if(result.equals("one")) {
                            Intent intent = new Intent(getActivity(), UndercarriageBookInfoActivity.class);
                            intent.putExtra("isbnNumber", isbnNumber);
                            startActivity(intent);
                        }else if(result.equals("two") || result.equals("four")) {
                            Intent intent = new Intent(getActivity(), BaseBookInfoActivity.class);
                            intent.putExtra("isbnNumber", isbnNumber);
                            startActivity(intent);
                        }else if(result.equals("three")) {
                            Intent intent = new Intent(getActivity(), OthersBookInfoActivity.class);
                            intent.putExtra("isbnNumber", isbnNumber);
                            startActivity(intent);
                        }
                    }
                });

            }
        });

        HttpHelper.sendOkHttpRequest(SELECT_ALL_BOOK, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                BookStoreFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BookStoreFragment.this.getActivity(), "网络错误", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonBookList = response.body().string();
                Gson gson = new Gson();
                List<String> bookList = gson.fromJson(jsonBookList, new TypeToken<List<String>>(){}.getType());

                dataList.clear();
                dataList.addAll(bookList);
                Message msg = new Message();
                msg.what = ACQUIRE_SUCCESS;
                handler.sendMessage(msg);
            }
        });
        return view;
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


    private static class ViewHolder {
        public ImageView bookImageView;
        public TextView bookTextView;
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
            if(convertView == null) {
                convertView = layoutInflater.inflate(R.layout.item_book, viewGroup, false);
                holder = new ViewHolder();
                holder.bookImageView = (ImageView) convertView.findViewById(R.id.book_imageView);
                holder.bookTextView = (TextView) convertView.findViewById(R.id.book_textView);
                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ImageView bookImageView = holder.bookImageView;
            final TextView bookTextView = holder.bookTextView;
            String isbnNumber = mList.get(position);

            /*数据库中有图书的信息*/
            Book book = DatabaseHelper.getDatabaseHelper().selectByIsbn(isbnNumber);
            if(book == null) {
                /*网络加载*/
                HttpHelper.sendOkHttpRequest(SELECT_BOOK + isbnNumber, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        BookStoreFragment.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(BookStoreFragment.this.getActivity(), "网络错误", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        String jsonBook = response.body().string();
                        final Book book = new Gson().fromJson(jsonBook, Book.class);
                        /*存储进数据库*/
                        DatabaseHelper.getDatabaseHelper().insertBook(book);
                        BookStoreFragment.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                bookTextView.setText(book.getTitle());
                                String tag = (String) bookImageView.getTag();
                                if (!book.getImage().equals(tag)) {
                                    bookImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.testbook));
                                }
                                if (mIsGridViewIdle) {
                                    bookImageView.setTag(book.getImage());
                                    ImageLoader.getImageLoader(mContext).bindBitmap(book.getImage(), bookImageView, 140, 180);
                                }
                            }
                        });
                    }
                });
            }else {
                bookTextView.setText(book.getTitle());
                String tag = (String) bookImageView.getTag();
                if (!book.getImage().equals(tag)) {
                    bookImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.testbook));
                }
                if (mIsGridViewIdle) {
                    bookImageView.setTag(book.getImage());
                    ImageLoader.getImageLoader(mContext).bindBitmap(book.getImage(), bookImageView, 140, 180);
                }
            }
            return convertView;
        }
        @Override
        public long getItemId(int i) {
            return i;
        }
    }
}
