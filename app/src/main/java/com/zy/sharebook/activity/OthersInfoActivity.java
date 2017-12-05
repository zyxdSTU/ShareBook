package com.zy.sharebook.activity;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zy.sharebook.R;
import com.zy.sharebook.activity.book.OthersBookInfoActivity;
import com.zy.sharebook.bean.Account;
import com.zy.sharebook.fragment.UserFragment;
import com.zy.sharebook.network.HttpHelper;
import com.zy.sharebook.util.PreferenceManager;
import com.zy.sharebook.util.imageloader.ImageLoader;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.zy.sharebook.util.Constant.IMAGE_URL;
import static com.zy.sharebook.util.Constant.SELECT_ACCOUNT;

public class OthersInfoActivity extends AppCompatActivity {
    private TextView nameTextView;
    private TextView sexTextView;
    private TextView idTextView;
    private TextView addressTextView;
    private CircleImageView imageCircleImageView;
    private TextView titleNameTextView;

    private Button borrowButton;
    private Button reportButton;

    private Account account;
    private final static int ACQUIRE_SUCCESS = 11;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case ACQUIRE_SUCCESS:{
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
        setContentView(R.layout.activity_others_info);
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        imageCircleImageView = (CircleImageView) findViewById(R.id.image);
        nameTextView = (TextView) findViewById(R.id.name_textView);
        sexTextView = (TextView) findViewById(R.id.sex_textView);
        idTextView = (TextView) findViewById(R.id.id_textView);
        addressTextView = (TextView) findViewById(R.id.address_textView);

        titleNameTextView = (TextView) findViewById(R.id.titleName_textView);
        titleNameTextView.setText("用户信息");

        borrowButton = (Button) findViewById(R.id.borrow_button);
        reportButton = (Button) findViewById(R.id.report_button);
        String type = getIntent().getStringExtra("type");
        if(type.equals("borrow")) {
            reportButton.setVisibility(View.GONE);
            borrowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    /*处理借书逻辑*/
                }
            });
        }else if(type.equals("report")) {
            borrowButton.setVisibility(View.GONE);
            reportButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view) {
                    /*处理举报逻辑*/
                }
            });
        }


        String phoneNumber = getIntent().getStringExtra("phoneNumber");
        if(phoneNumber == null) {
            String jsonAccount = getIntent().getStringExtra("jsonAccount");
            account = new Gson().fromJson(jsonAccount, Account.class);
            setInfo();
        }else {
            HttpHelper.sendOkHttpRequest(SELECT_ACCOUNT + phoneNumber, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(OthersInfoActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String accountJson = response.body().string();
                    account = new Gson().fromJson(accountJson, Account.class);

                    Message msg = new Message();
                    msg.what = ACQUIRE_SUCCESS;
                    handler.sendMessage(msg);
                }
            });
        }
    }

    public void setInfo() {
        nameTextView.setText(account.getName());
        sexTextView.setText(account.getSex());
        idTextView.setText(account.getId());
        addressTextView.setText(account.getAddress());

        if(account.getImage() != null) {
            ImageLoader.getImageLoader(this).bindBitmap(IMAGE_URL + account.getImage(), imageCircleImageView, 300, 300);
        }else{
            imageCircleImageView.setImageDrawable(getResources().getDrawable(R.drawable.test_image));
        }
    }
}
