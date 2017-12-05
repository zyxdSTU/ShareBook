package com.zy.sharebook.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zy.sharebook.R;
import com.zy.sharebook.bean.Account;
import com.zy.sharebook.network.HttpHelper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.zy.sharebook.util.Constant.REGISTER;

public class RegisterNextActivity extends AppCompatActivity {
    private String TAG = "RegisterNextActivity";

    private EditText nameEditText;
    private EditText idEditText;
    private EditText sexEditText;
    private EditText addressEditText;
    private EditText passwordEditText;

    private Button registerButton;
    private Account account = new Account();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_register_next);
        if(getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        TextView titleTextView = (TextView)findViewById(R.id.titleName_textView);
        titleTextView.setText("注册");
        account.setPhoneNumber(getIntent().getStringExtra("phoneNumber"));

        nameEditText = (EditText) findViewById(R.id.name_editTextView);
        idEditText = (EditText) findViewById(R.id.id_editTextView);
        sexEditText = (EditText) findViewById(R.id.sex_editTextView);
        addressEditText = (EditText) findViewById(R.id.address_editTextView);
        passwordEditText = (EditText) findViewById(R.id.password_editTextView);
        registerButton = (Button) findViewById(R.id.register_button);

        registerButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                //if(checkItem()) {
                    account.setName(nameEditText.getText().toString());
                    account.setId(idEditText.getText().toString());
                    account.setSex(sexEditText.getText().toString());
                    account.setAddress(addressEditText.getText().toString());
                    account.setPassword(passwordEditText.getText().toString());

                    Toast.makeText(RegisterNextActivity.this, new Gson().toJson(account), Toast.LENGTH_SHORT).show();

                    HttpHelper.sendOKHttpPost(new Gson().toJson(account), REGISTER, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RegisterNextActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(RegisterNextActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                            startActivity(new Intent(RegisterNextActivity.this, LoginActivity.class));
                        }
                    });
                //}
            }
        });
    }

    private boolean checkItem() {
        String name = nameEditText.getText().toString();
        if(name == null || name.length() > 20) {
            Toast.makeText(RegisterNextActivity.this, "姓名格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        String id = idEditText.getText().toString();
        if(id == null ||id.length() != 11) {
            Toast.makeText(RegisterNextActivity.this, "学号格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        String sex = sexEditText.getText().toString();
        if(sex== null || (!sex.equals("男")&&!sex.equals("女"))) {
            Toast.makeText(RegisterNextActivity.this, "性别格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        String address = addressEditText.getText().toString();
        if(address== null || address.length() > 50) {
            Toast.makeText(RegisterNextActivity.this, "地址格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        String password = passwordEditText.getText().toString();
        if(password== null || password.length() > 50) {
            Toast.makeText(RegisterNextActivity.this, "密码格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
