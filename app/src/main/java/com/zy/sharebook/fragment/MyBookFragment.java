package com.zy.sharebook.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.zy.sharebook.R;
import com.zy.sharebook.activity.ScanActivity;
import com.zy.sharebook.activity.book.GroundBookInfoActivity;
import com.zy.sharebook.adapter.FragmentAdapter;
import com.zy.sharebook.bean.Book;
import com.zy.sharebook.database.DatabaseHelper;
import com.zy.sharebook.fragment.fragment_mybook.GroundFragment;
import com.zy.sharebook.fragment.fragment_mybook.UndercarriageFragment;
import com.zy.sharebook.network.HttpHelper;
import com.zy.sharebook.util.NoScrollViewPager;
import com.zy.sharebook.util.Util;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import static com.zy.sharebook.util.Constant.BOOK_URL;

/**
 * Created by ZY on 2017/11/26.
 */

public class MyBookFragment extends Fragment {
    private NoScrollViewPager viewPager;
    private RadioGroup radioGroup;
    private RadioButton groundRadioButton;
    private RadioButton undercarriageRadioButton;

    private View leftLine;
    private View rightLine;

    private ImageButton barCodeButton;

    private ArrayList<Fragment> list = new ArrayList<Fragment>();
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mybook, container, false);

        viewPager = (NoScrollViewPager) view.findViewById(R.id.view_pager);
        radioGroup = (RadioGroup) view.findViewById(R.id.myBook_radioGroup);
        groundRadioButton = (RadioButton) view.findViewById(R.id.ground_radioButton);
        undercarriageRadioButton = (RadioButton) view.findViewById(R.id.undercarriage_radioButton);

        barCodeButton = (ImageButton) view.findViewById(R.id.barCode_button);

        leftLine = (View) view.findViewById(R.id.left_line);
        rightLine = (View) view.findViewById(R.id.right_line);


        list.add(new GroundFragment());
        list.add(new UndercarriageFragment());

        final FragmentAdapter adapter = new FragmentAdapter(getChildFragmentManager(), list);

        viewPager.setAdapter(adapter);

        changeTab(0);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.ground_radioButton: {
                           changeTab(0);
                        }
                        break;

                    case R.id.undercarriage_radioButton:{
                           changeTab(1);
                        }
                        break;
                    default:break;
                }
            }
        });

        barCodeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                int id = view.getId();
                switch(id) {
                    case R.id.barCode_button:
                /*动态申请照相机权限*/
                        scanBarCode();
                    default:
                        break;
                }
            }
        });
        return view;
    }

    private void changeTab(int position) {
        groundRadioButton.setTextColor(Color.parseColor("#8a8a8a"));
        undercarriageRadioButton.setTextColor(Color.parseColor("#8a8a8a"));
        leftLine.setBackgroundColor(Color.parseColor("#8a8a8a"));
        rightLine.setBackgroundColor(Color.parseColor("#8a8a8a"));
        switch(position) {
            case 0:
                groundRadioButton.setTextColor(Color.parseColor("#3D5AFE"));
                leftLine.setBackgroundColor(Color.parseColor("#3D5AFE"));
                break;
            case 1:
                undercarriageRadioButton.setTextColor(Color.parseColor("#3D5AFE"));
                rightLine.setBackgroundColor(Color.parseColor("#3D5AFE"));
                break;
            default:
                break;
        }
        viewPager.setCurrentItem(position);
    }

    /*扫描条形码*/
    public void onScanBarcode(){
        IntentIntegrator integrator = new IntentIntegrator(this.getActivity());
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setPrompt("扫描条形码");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(true);
        integrator.setCaptureActivity(ScanActivity.class);
        integrator.initiateScan();
    }

    private void scanBarCode() {
        if(ContextCompat.checkSelfPermission(MyBookFragment.this.getActivity(), Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MyBookFragment.this.getActivity(), new String[]{Manifest.permission.CAMERA}, 1);
        } else onScanBarcode();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onScanBarcode();
                } else {
                    Toast.makeText(MyBookFragment.this.getActivity(), "开启相机失败", Toast.LENGTH_SHORT).show();
                }
                break;
            default:break;
        }
    }

}
