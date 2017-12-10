package com.zy.sharebook.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zy.sharebook.R;
import com.zy.sharebook.network.HttpHelper;
import com.zy.sharebook.util.PreferenceManager;
import com.zy.sharebook.util.Util;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.zy.sharebook.util.Constant.ACCOUNT_VERIFY;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG="LoginActivity";
    private EditText phoneNumberEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private TextView forgetTextView;
    private TextView registerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }


        setContentView(R.layout.activity_login);
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        phoneNumberEditText = (EditText) findViewById(R.id.phoneNumber_editText);
        passwordEditText = (EditText) findViewById(R.id.password_editText);
        loginButton = (Button) findViewById(R.id.login_button);
        forgetTextView = (TextView) findViewById(R.id.forget_textView);
        registerTextView = (TextView) findViewById(R.id.register_textView);

        loginButton.setOnClickListener(this);
        forgetTextView.setOnClickListener(this);
        registerTextView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id) {
            /*登录*/
            case R.id.login_button:{
                final String phoneNumber = phoneNumberEditText.getText().toString();
                if(!Util.isPhoneNumber(phoneNumber)) break;
                String password = passwordEditText.getText().toString();
                if(password.length() > 100 || password == null) break;

                String url = ACCOUNT_VERIFY + "phoneNumber=" + phoneNumber + "&password=" + password;
                Log.d(TAG, url);
                HttpHelper.sendOkHttpRequest(url, new Callback() {
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String result = response.body().string();
                        if(result.equals(false)) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "手机号或者密码出错", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }else {
                            PreferenceManager.getInstance().preferenceManagerSave("currentPhoneNumber", phoneNumber);
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);

                            /*can't back*/
                            finish();
                        }
                    }

                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, "网络出错", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }

            /*忘记密码*/
            case R.id.forget_textView:{
                break;
            }

            /*注册*/
            case R.id.register_textView:{
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;
            }
            default:break;
        }
    }
}
