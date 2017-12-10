package com.zy.sharebook.fragment.fragment_history;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zy.sharebook.R;
import com.zy.sharebook.activity.OthersInfoActivity;
import com.zy.sharebook.bean.Book;
import com.zy.sharebook.bean.Borrow;
import com.zy.sharebook.database.DatabaseHelper;
import com.zy.sharebook.fragment.fragment_mybook.GroundFragment;
import com.zy.sharebook.network.HttpHelper;
import com.zy.sharebook.util.PreferenceManager;
import com.zy.sharebook.util.imageloader.ImageLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.zy.sharebook.util.Constant.COMPLETE;
import static com.zy.sharebook.util.Constant.SELECT_BOOK;
import static com.zy.sharebook.util.Constant.SELECT_BORROWER;
import static com.zy.sharebook.util.Constant.SELECT_NAME;
import static com.zy.sharebook.util.Constant.UPDATE;

/**
 * Created by Administrator on 2017/12/9.
 */

public class BorrowFragment extends Fragment implements AbsListView.OnScrollListener{
    private ListView listView;
    private final static int ACQUIRE_SUCCESS = 11;

    /*按钮事件*/
    private final static int CHANGE_SUCCESS = 22;

    private boolean mIsGridViewIdle = true;
    private ArrayList<Borrow> list = new ArrayList<Borrow>();
    private ListViewAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case ACQUIRE_SUCCESS:{
                    if(swipeRefreshLayout.isRefreshing()) swipeRefreshLayout.setRefreshing(false);
                    adapter.notifyDataSetChanged();
                    break;
                }
                case CHANGE_SUCCESS:{
                    getInternetData();
                    break;
                }
                default: break;
            }
        }
    };

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lebo, container, false);
        listView = (ListView) view.findViewById(R.id.list_view);
        adapter = new ListViewAdapter(this.getContext(), list);
        listView.setAdapter(adapter);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getInternetData();
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
        HttpHelper.sendOkHttpRequest(SELECT_BORROWER + phoneNumber, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                BorrowFragment.this.getActivity().
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(BorrowFragment.this.getActivity(), "网络错误", Toast.LENGTH_SHORT).show();
                            }
                        });

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String jsonBorrowList = response.body().string();
                Log.d("BorrowFragment", jsonBorrowList);
                List<Borrow> tempList = new Gson().fromJson(jsonBorrowList, new TypeToken<List<Borrow>>(){}.getType());
                list.clear();
                list.addAll(tempList);
                Message msg = new Message(); msg.what = ACQUIRE_SUCCESS;
                handler.sendMessage(msg);
            }
        });
    }

    private static class ViewHolder {
        public ImageView bookImageView;
        public TextView titleTextView;
        public TextView statusTextView;
        public TextView roleTextView;
        public TextView nameTextView;

        public LinearLayout buttonLinearLayout;
        public Button agreeButton;
        public Button rejectButton;
        public Button cancelButton;
        public Button revertButton;
        public Button confirmButton;
    }

    private class ListViewAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<Borrow> mList;
        private LayoutInflater layoutInflater;

        public ListViewAdapter(Context context, ArrayList<Borrow> mList) {
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
        public View getView(final int position, View convertView, ViewGroup viewGroup) {
            ViewHolder holder = null;
            if(convertView == null) {
                convertView = layoutInflater.inflate(R.layout.item_borrow, viewGroup, false);
                holder = new ViewHolder();
                holder.bookImageView = (ImageView) convertView.findViewById(R.id.book_imageView);
                holder.titleTextView = (TextView) convertView.findViewById(R.id.title_textView);
                holder.statusTextView = (TextView) convertView.findViewById(R.id.status_textView);
                holder.roleTextView = (TextView) convertView.findViewById(R.id.role_textView);
                holder.nameTextView = (TextView) convertView.findViewById(R.id.name_textView);

                holder.buttonLinearLayout = (LinearLayout) convertView.findViewById(R.id.button_linearLayout);
                holder.agreeButton = (Button) convertView.findViewById(R.id.agree_button);
                holder.rejectButton = (Button) convertView.findViewById(R.id.reject_button);
                holder.cancelButton = (Button) convertView.findViewById(R.id.cancel_button);
                holder.revertButton = (Button) convertView.findViewById(R.id.revert_button);
                holder.confirmButton = (Button) convertView.findViewById(R.id.confirm_button);

                convertView.setTag(holder);
            }else {
                holder = (ViewHolder) convertView.getTag();
            }

            final ImageView bookImageView = holder.bookImageView;
            final TextView titleTextView = holder.titleTextView;
            TextView statusTextView = holder.statusTextView;
            TextView roleTextView = holder.roleTextView;
            final TextView nameTextView = holder.nameTextView;

            LinearLayout buttonLinearLayout = holder.buttonLinearLayout;
            Button agreeButton = holder.agreeButton;
            Button rejectButton = holder.rejectButton;
            Button cancelButton = holder.cancelButton;
            Button revertButton = holder.revertButton;
            Button confirmButton = holder.confirmButton;

            /*开始全部不可见*/
            buttonLinearLayout.setVisibility(View.GONE);
            cancelButton.setVisibility(View.GONE);
            revertButton.setVisibility(View.GONE);
            confirmButton.setVisibility(View.GONE);

            Borrow borrow = mList.get(position);

            roleTextView.setText("所有者: ");

            switch(borrow.getStatus()) {
                case 1:{
                    cancelButton.setVisibility(View.VISIBLE);
                    statusTextView.setText("申请中");
                    break;
                }
                case 4:{
                    revertButton.setVisibility(View.VISIBLE);
                    statusTextView.setText("借阅中");
                    break;
                }
                case 5:{
                    statusTextView.setText("归还交接中");
                    break;
                }
                default:break;
            }

            /*按钮监听事件*/

            /*取消申请*/
            cancelButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Borrow borrow = new Borrow(mList.get(position));
                    borrow.setStatus(2);
                    HttpHelper.sendOKHttpPost(new Gson().toJson(borrow), COMPLETE, new Callback(){
                        @Override
                        public void onFailure(Call call, IOException e) {
                            BorrowFragment.this.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(BorrowFragment.this.getActivity(), "网络错误", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            BorrowFragment.this.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(BorrowFragment.this.getActivity(), "取消申请成功", Toast.LENGTH_SHORT).show();

                                    sendChangeMessage();
                                }
                            });
                        }
                    });
                }
            });

            /*归还交接*/
            revertButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    Borrow borrow = new Borrow(mList.get(position));
                    borrow.setStatus(5);
                    HttpHelper.sendOKHttpPost(new Gson().toJson(borrow), UPDATE, new Callback(){
                        @Override
                        public void onFailure(Call call, IOException e) {
                            BorrowFragment.this.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(BorrowFragment.this.getActivity(), "网络错误", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            BorrowFragment.this.getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(BorrowFragment.this.getActivity(), "申请归还成功", Toast.LENGTH_SHORT).show();

                                    sendChangeMessage();
                                }
                            });
                        }
                    });
                }
            });

            /*加载个人信息*/
            String tag = (String) nameTextView.getTag();
            if(!borrow.getOwner().equals(tag)) {
                nameTextView.setText("周宇");
            }
            nameTextView.setTag(borrow.getOwner());

            HttpHelper.sendOkHttpRequest(SELECT_NAME + borrow.getOwner(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    BorrowFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(BorrowFragment.this.getActivity(), "网络错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call,  Response response) throws IOException {
                    final String name = response.body().string();
                    BorrowFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            nameTextView.setText(name);
                        }
                    });
                }
            });

            nameTextView.setOnClickListener(new View.OnClickListener() {
                /*跳转个人信息界面*/
                @Override
                public void onClick(View view) {
                    String tag = (String) view.getTag();
                    Intent intent = new Intent(BorrowFragment.this.getActivity(), OthersInfoActivity.class);
                    intent.putExtra("type", "report");
                    intent.putExtra("phoneNumber", tag);
                    BorrowFragment.this.getActivity().startActivity(intent);
                }
            });




            /*加载图书信息*/
            Book book = DatabaseHelper.getDatabaseHelper().selectByIsbn(borrow.getIsbnNumber());
            if(book == null) {
                /*网络加载*/
                HttpHelper.sendOkHttpRequest(SELECT_BOOK + borrow.getIsbnNumber(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        BorrowFragment.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(BorrowFragment.this.getActivity(), "网络错误", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        String jsonBook = response.body().string();
                        final Book book = new Gson().fromJson(jsonBook, Book.class);
                        /*存储进数据库*/
                        DatabaseHelper.getDatabaseHelper().insertBook(book);
                        BorrowFragment.this.getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                titleTextView.setText(book.getTitle());
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
                titleTextView.setText(book.getTitle());
                String tagBook = (String) bookImageView.getTag();
                if (!book.getImage().equals(tagBook)) {
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


    /*按钮事件*/
    public void sendChangeMessage() {
        Message msg = new Message();
        msg.what = CHANGE_SUCCESS;
        handler.sendMessage(msg);
    }
}
