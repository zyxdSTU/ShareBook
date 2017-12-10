package com.zy.sharebook.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zy.sharebook.R;
import com.zy.sharebook.bean.Account;
import com.zy.sharebook.network.HttpHelper;
import com.zy.sharebook.util.PreferenceManager;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.zy.sharebook.util.Constant.UPDATE_ACCOUNT;

public class UserUpdateActivity extends AppCompatActivity {
    private EditText nameEditText;
    private EditText idEditText;
    private EditText sexEditText;
    private EditText addressEditText;
    private EditText passwordEditText;

    private Button updateButton;

    private Account account;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if(Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_user_update);

        if(getSupportActionBar() != null) getSupportActionBar().hide();

        nameEditText = (EditText) findViewById(R.id.name_editTextView);
        idEditText = (EditText) findViewById(R.id.id_editTextView);
        sexEditText = (EditText) findViewById(R.id.sex_editTextView);
        addressEditText = (EditText) findViewById(R.id. address_editTextView);
        passwordEditText = (EditText) findViewById(R.id.password_editTextView);
        updateButton = (Button) findViewById(R.id.update_button);

        /*从缓存加载*/
        String currentAccountJson = PreferenceManager.getInstance().preferenceManagerGet("currentAccountJson");
        account = new Gson().fromJson(currentAccountJson, Account.class);

        nameEditText.setText(account.getName());
        idEditText.setText(account.getId());
        sexEditText.setText(account.getSex());
        addressEditText.setText(account.getAddress());
        passwordEditText.setText(account.getPassword());

        updateButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(checkItem()) {
                    HttpHelper.sendOKHttpPost(new Gson().toJson(account), UPDATE_ACCOUNT, new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(UserUpdateActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(UserUpdateActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                                    PreferenceManager.getInstance().preferenceManagerSave("currentAccountJson", new Gson().toJson(account));

                                    Intent intent = new Intent(UserUpdateActivity.this, MainActivity.class);
                                    startActivity(intent);
                                }
                            });
                        }
                    });
                }
            }
        });
    }

    private boolean checkItem() {
        String name = nameEditText.getText().toString();
        if(name == null || name.length() > 20) {
            Toast.makeText(UserUpdateActivity.this, "姓名格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        String id = idEditText.getText().toString();
        if(id == null ||id.length() != 11) {
            Toast.makeText(UserUpdateActivity.this, "学号格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        String sex = sexEditText.getText().toString();
        if(sex== null || (!sex.equals("男")&&!sex.equals("女"))) {
            Toast.makeText(UserUpdateActivity.this, "性别格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        String address = addressEditText.getText().toString();
        if(address== null || address.length() > 50) {
            Toast.makeText(UserUpdateActivity.this, "地址格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        String password = passwordEditText.getText().toString();
        if(password== null || password.length() > 50) {
            Toast.makeText(UserUpdateActivity.this, "密码格式错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        account.setPhoneNumber(PreferenceManager.getInstance().preferenceManagerGet("currentPhoneNumber"));
        account.setName(nameEditText.getText().toString());
        account.setId(idEditText.getText().toString());
        account.setSex(sexEditText.getText().toString());
        account.setAddress(addressEditText.getText().toString());
        account.setPassword(passwordEditText.getText().toString());

        return true;
    }
}
