package com.zy.sharebook.fragment.fragment_mybook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zy.sharebook.R;
import com.zy.sharebook.activity.book.BaseBookInfoActivity;
import com.zy.sharebook.activity.book.GroundBookInfoActivity;
import com.zy.sharebook.activity.book.UndercarriageBookInfoActivity;
import com.zy.sharebook.bean.Book;
import com.zy.sharebook.bean.BookStore;
import com.zy.sharebook.database.DatabaseHelper;
import com.zy.sharebook.network.HttpHelper;
import com.zy.sharebook.util.PreferenceManager;
import com.zy.sharebook.util.imageloader.ImageLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.zy.sharebook.util.Constant.SELECT_BOOK;
import static com.zy.sharebook.util.Constant.SELECT_BOOKSTORE;

/**
 * Created by ZY on 2017/12/2.
 */

public class GroundFragment extends Fragment implements AbsListView.OnScrollListener  {
    private final static int ACQUIRE_SUCCESS = 11;
    private boolean mIsGridViewIdle = true;
    private ArrayList<BookStore> list = new ArrayList<BookStore>();
    private GridView gridView;
    private GridViewAdapter adapter;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case ACQUIRE_SUCCESS:{
                    adapter.notifyDataSetChanged();
                    break;
                }
                default: break;
            }
        }
    };

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mybook_ground, container, false);
        gridView = (GridView) view.findViewById(R.id.grid_view);
        adapter = new GridViewAdapter(this.getContext(), list);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                BookStore bookStore = list.get(i);
                if(bookStore.getFlag() == 0) {
                    Intent intent = new Intent(getActivity(), BaseBookInfoActivity.class);
                    intent.putExtra("isbnNumber", bookStore.getIsbnNumber());
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getActivity(), UndercarriageBookInfoActivity.class);
                    intent.putExtra("isbnNumber", bookStore.getIsbnNumber());
                    startActivity(intent);
                }
            }
        });


        getInternetData();
        return view;
    }

    /**可见时调用**/
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            getInternetData();
        }
    }

    public void getInternetData() {
        String phoneNumber = PreferenceManager.getInstance().preferenceManagerGet("currentPhoneNumber");
        HttpHelper.sendOkHttpRequest(SELECT_BOOKSTORE + phoneNumber, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                GroundFragment.this.getActivity().
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GroundFragment.this.getActivity(), "网络错误", Toast.LENGTH_SHORT).show();
                            }
                        });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonBookStoreList = response.body().string();
                Log.d("GroundFragment", jsonBookStoreList);
                List<BookStore> tempList = new Gson().fromJson(jsonBookStoreList, new TypeToken<List<BookStore>>(){}.getType());
                list.clear();
                list.addAll(tempList);
                Message msg = new Message(); msg.what = ACQUIRE_SUCCESS;
                handler.sendMessage(msg);
            }
        });
    }

    private static class ViewHolder {
        public ImageView bookImageView;
        public TextView bookTextView;
    }

    private class GridViewAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<BookStore> mList;
        private LayoutInflater layoutInflater;

        public GridViewAdapter(Context context, ArrayList<BookStore> mList) {
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
            BookStore bookStore = mList.get(position);

            /*数据库中有图书的信息*/
            Book book = DatabaseHelper.getDatabaseHelper().selectByIsbn(bookStore.getIsbnNumber());
            if(book == null) {
                /*网络加载*/
                HttpHelper.sendOkHttpRequest(SELECT_BOOK + bookStore.getIsbnNumber(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        GroundFragment.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GroundFragment.this.getActivity(), "网络错误", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        String jsonBook = response.body().string();
                        final Book book = new Gson().fromJson(jsonBook, Book.class);
                        /*存储进数据库*/
                        DatabaseHelper.getDatabaseHelper().insertBook(book);
                        GroundFragment.this.getActivity().runOnUiThread(new Runnable() {
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


    @Override
    public void onScroll(AbsListView absListView, int i, int i1, int i2) {
    }

    @Override
    public void onScrollStateChanged(AbsListView absListView, int i) {
        if (i == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            mIsGridViewIdle = true;
            adapter.notifyDataSetChanged();
        } else {
            mIsGridViewIdle = false;
        }
    }
}

