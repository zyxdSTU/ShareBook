package com.zy.sharebook.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.zy.sharebook.R;
import com.zy.sharebook.activity.LoginActivity;
import com.zy.sharebook.activity.UserUpdateActivity;
import com.zy.sharebook.bean.Account;
import com.zy.sharebook.bean.Book;
import com.zy.sharebook.database.DatabaseHelper;
import com.zy.sharebook.network.HttpHelper;
import com.zy.sharebook.util.PreferenceManager;
import com.zy.sharebook.util.imageloader.ImageLoader;

import java.io.File;
import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static android.app.Activity.RESULT_OK;
import static com.zy.sharebook.util.Constant.IMAGE_URL;
import static com.zy.sharebook.util.Constant.SELECT_ACCOUNT;
import static com.zy.sharebook.util.Constant.SELECT_BOOK;
import static com.zy.sharebook.util.Constant.UPLOAD_IMAGE;

/**
 * Created by ZY on 2017/11/16.
 */

public class UserFragment extends Fragment {
    private TextView nameTextView;
    private TextView phoneNumberText;
    private TextView sexTextView;
    private TextView idTextView;
    private TextView addressTextView;
    private Button logoutButton;
    private CircleImageView imageCircleImageView;
    private ImageButton editButton;
    private static final int CHOOSE_PHOTO = 2;
    private Account account = new Account();
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user, container, false);
        imageCircleImageView = (CircleImageView) view.findViewById(R.id.image);
        nameTextView = (TextView)view.findViewById(R.id.name_textView);
        phoneNumberText = (TextView) view.findViewById(R.id.phoneNumber_textView);
        sexTextView = (TextView) view.findViewById(R.id.sex_textView);
        idTextView = (TextView) view.findViewById(R.id.id_textView);
        addressTextView = (TextView) view.findViewById(R.id.address_textView);
        logoutButton = (Button) view.findViewById(R.id.logout_button);
        editButton = (ImageButton) view.findViewById(R.id.edit_button);
        String accountJson = PreferenceManager.getInstance().preferenceManagerGet("currentAccountJson");
        if(accountJson == "") {
            String currentPhoneNumber = PreferenceManager.getInstance().preferenceManagerGet("currentPhoneNumber");
            HttpHelper.sendOkHttpRequest(SELECT_ACCOUNT + currentPhoneNumber, new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    UserFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(UserFragment.this.getActivity(), "网络错误", Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String accountJson = response.body().string();
                    account = new Gson().fromJson(accountJson, Account.class);
                        /*存储进缓存*/
                    PreferenceManager.getInstance().preferenceManagerSave("currentAccountJson", accountJson);

                    Message msg = new Message();
                    msg.what = ACQUIRE_SUCCESS;
                    handler.sendMessage(msg);
                }
            });
        }else {
            account = new Gson().fromJson(accountJson, Account.class);
            setInfo();
        }

        imageCircleImageView.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                PreferenceManager.getInstance().preferenceManagerRemove("currentAccountJson");
                uploadImage();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener(){
            /*跳转界面login, 清除用户信息*/
            @Override
            public void onClick(View view) {
                PreferenceManager.getInstance().preferenceManagerRemove("currentPhoneNumber");
                PreferenceManager.getInstance().preferenceManagerRemove("currentAccountJson");

                Intent intent = new Intent(UserFragment.this.getActivity(), LoginActivity.class);

                /*bug*/
                UserFragment.this.getActivity().finish();

                UserFragment.this.getActivity().startActivity(intent);
            }
        });

        editButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserFragment.this.getActivity(), UserUpdateActivity.class);
                UserFragment.this.getActivity().startActivity(intent);
            }
        });

        return view;
    }

    public void setInfo() {
        nameTextView.setText(account.getName());
        phoneNumberText.setText(account.getPhoneNumber());
        sexTextView.setText(account.getSex());
        idTextView.setText(account.getId());
        addressTextView.setText(account.getAddress());

        if(account.getImage() != null) {
            ImageLoader.getImageLoader(getContext()).bindBitmap(IMAGE_URL + account.getImage(), imageCircleImageView, 300, 300);
        }else{
            imageCircleImageView.setImageDrawable(getContext().getResources().getDrawable(R.drawable.test_image));
        }
    }

    public void uploadImage() {
        /**判断权限**/
        if(ContextCompat.checkSelfPermission(UserFragment.this.getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(UserFragment.this.getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        } else {
            openAlbum();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openAlbum();
                } else{
                    Toast.makeText(this.getActivity(), "获取权限失败", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case CHOOSE_PHOTO:
                if(resultCode == RESULT_OK) {
                    if(Build.VERSION.SDK_INT >= 19) {
                        /**4.4以上系统**/
                        handleImageOnKitKat(data);
                    } else {
                        /**4.4以下系统**/
                        handImageBeforeKitKat(data);
                    }
                }
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) {
        String imagePath = null;
        Uri uri = data.getData();
        if(DocumentsContract.isDocumentUri(this.getActivity(), uri)) {
            String docId = DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())) {
            imagePath = getImagePath(uri, null);
        }else if("file".equalsIgnoreCase(uri.getScheme())) {
            imagePath = uri.getPath();
        }

        /**展示照片**/
        displayImage(imagePath);

        /**上传照片**/
        uploadToServer(imagePath);
    }

    private void handImageBeforeKitKat(Intent data) {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);
        uploadToServer(imagePath);
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        /**通过Uri和selection来获取真实的图片路径**/
        Cursor cursor = this.getActivity().getContentResolver().query(uri, null, selection, null ,null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
        }
        cursor.close();
        return path;
    }

    private void displayImage(String imagePath) {
        if(imagePath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            imageCircleImageView.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this.getActivity(), "加载图片失败", Toast.LENGTH_SHORT).show();
        }
    }


    public void uploadToServer(final String imagePath) {
        File file = new File(imagePath);
        String image = PreferenceManager.getInstance().preferenceManagerGet("currentPhoneNumber") + Long.toString(System.currentTimeMillis());
        /*更新image*/


        HttpHelper.uploadImage(UPLOAD_IMAGE + image, file, new okhttp3.Callback(){
            @Override
            public void onFailure(Call call, IOException e) {
                UserFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UserFragment.this.getActivity(),"上传图片失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                UserFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(UserFragment.this.getActivity(),"上传图片成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
