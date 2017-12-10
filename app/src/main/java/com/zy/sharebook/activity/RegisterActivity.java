package com.zy.sharebook.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import cn.smssdk.SMSSDK;

import com.mob.MobSDK;
import com.zy.sharebook.R;
import com.zy.sharebook.activity.book.*;
import com.zy.sharebook.util.Util;

import cn.smssdk.EventHandler;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "RegisterActivity";
    private static final String APP_KEY = "223adfc6a24ad";
    private static final String APP_SECRET = "70caefe9589732feed8808e7ffec63e0";

    /*线程退出*/
    private boolean flag = false;

    private EditText phoneNumberEditText;
    private EditText identifyCodeEditText;
    private Button acquireCodeButton;
    private Button nextStepButton;

    private String phoneNumber;
    private String identifyCode;

    private Handler handle = new Handler() {
        @Override
        public void handleMessage(Message message) {
            int num = message.arg1;
            if(num == 60){
                acquireCodeButton.setText("等待" + num + "秒");
                acquireCodeButton.setClickable(false);
            }
            if(num == 0) {
                acquireCodeButton.setText("获取验证码");
                acquireCodeButton.setClickable(true);
                return;
            }
            acquireCodeButton.setText("等待" + num + "秒");
        }
    };

    private EventHandler eventHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_register);
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        phoneNumberEditText = (EditText) findViewById(R.id.phoneNumber_editText);
        identifyCodeEditText = (EditText) findViewById(R.id.identifyCode_editText);
        acquireCodeButton = (Button) findViewById(R.id.acquireCode_button);
        nextStepButton = (Button) findViewById(R.id.nextStep_button);
        acquireCodeButton.setOnClickListener(this);
        nextStepButton.setOnClickListener(this);


        initSMSSDK();
    }

    public void initSMSSDK() {
        MobSDK.init(this, APP_KEY, APP_SECRET);
        eventHandler = new EventHandler() {
            @Override
            //消息回调接口
            public void afterEvent(int event, int result, Object data) {
                if(result == SMSSDK.RESULT_COMPLETE) {
                    if(event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        /*验证成功*/
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this, "验证成功", Toast.LENGTH_SHORT);
                                flag = true;
                                /*跳转接口*/
                                Intent intent = new Intent(RegisterActivity.this, RegisterNextActivity.class);
                                intent.putExtra("phoneNumber", phoneNumber);
                                startActivity(intent);
                            }
                        });
                    }
                    else if(event == SMSSDK.EVENT_GET_VERIFICATION_CODE) {
                        /*获取验证码成功*/
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RegisterActivity.this, "获取验证码成功", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                    else if(event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES) {
                        //返回支持发送验证码的国家列表
                    }
                }
            }
        };
        SMSSDK.registerEventHandler(eventHandler);
    }

    protected void onDestroy() {
        // 销毁回调监听接口
        SMSSDK.unregisterAllEventHandler();
        super.onDestroy();
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch(id) {
            case R.id.acquireCode_button:{
                Log.d(TAG, "onClick: ");
                String text = phoneNumberEditText.getText().toString().trim();
                /*保证11位数字的电话号码*/
                if(!Util.isPhoneNumber(text)) {
                    Toast.makeText(RegisterActivity.this, "手机号码格式错误", Toast.LENGTH_LONG).show();
                    return;
                }
                phoneNumber = text;
                SMSSDK.getVerificationCode("86", phoneNumber);//获取短信
                /*启动获取验证码倒计时*/
                countDown();
                break;
            }
            case R.id.nextStep_button: {
                String text = identifyCodeEditText.getText().toString().trim();
                /*保证4位数字的验证码*/
                if(!isIdentifyCode(text)) {
                    Toast.makeText(RegisterActivity.this, "验证码格式错误", Toast.LENGTH_LONG).show();
                    return;
                }

                identifyCode = text;
                Log.d(TAG,  identifyCode);
                Log.d(TAG,  phoneNumber);
                SMSSDK.submitVerificationCode("86", phoneNumber, identifyCode); //验证验证码
                Log.d(TAG, "nextStep: ");
                break;
            }
            default:
                break;
        }
    }

    /*验证号*/
    public boolean isIdentifyCode(String text) {
        if(text.length() != 4) return false;
        for(int i = 0; i < text.length(); i++) {
            if(text.charAt(i) < '0' || text.charAt(i) > '9') return false;
        }
        return true;
    }

    public void countDown() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int timeOut = 60;
                while(timeOut >= 0 && !flag) {
                     /*等待一秒*/
                    try {
                        Thread.sleep(1000);
                    }catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                    Message msg = new Message();
                    msg.arg1 = timeOut;
                    handle.sendMessage(msg);
                    timeOut--;
                }
            }
        }).start();
    }
}
