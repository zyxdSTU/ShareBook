package com.zy.sharebook.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.zy.sharebook.R;
import com.zy.sharebook.adapter.FragmentAdapter;
import com.zy.sharebook.fragment.fragment_history.BorrowFragment;
import com.zy.sharebook.fragment.fragment_history.LendFragment;
import com.zy.sharebook.util.NoScrollViewPager;

import java.util.ArrayList;

/**
 * Created by ZY on 2017/11/16.
 */

public class HistoryFragment extends Fragment {
    private NoScrollViewPager viewPager;
    private RadioGroup radioGroup;
    private RadioButton borrowRadioButton;
    private RadioButton lendRadioButton;

    private View leftLine;
    private View rightLine;


    private ArrayList<Fragment> list = new ArrayList<Fragment>();
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        viewPager = (NoScrollViewPager) view.findViewById(R.id.view_pager);
        radioGroup = (RadioGroup) view.findViewById(R.id.history_radioGroup);
        borrowRadioButton = (RadioButton) view.findViewById(R.id.borrow_radioButton);
        lendRadioButton = (RadioButton) view.findViewById(R.id.lend_radioButton);

        leftLine = (View) view.findViewById(R.id.left_line);
        rightLine = (View) view.findViewById(R.id.right_line);


        list.add(new BorrowFragment());
        list.add(new LendFragment());

        final FragmentAdapter adapter = new FragmentAdapter(getChildFragmentManager(), list);

        viewPager.setAdapter(adapter);

        changeTab(0);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                switch (i) {
                    case R.id.borrow_radioButton: {
                        changeTab(0);
                    }
                    break;

                    case R.id.lend_radioButton:{
                        changeTab(1);
                    }
                    break;
                    default:break;
                }
            }
        });
        return view;
    }

    private void changeTab(int position) {
        borrowRadioButton.setTextColor(Color.parseColor("#8a8a8a"));
        lendRadioButton.setTextColor(Color.parseColor("#8a8a8a"));
        leftLine.setBackgroundColor(Color.parseColor("#8a8a8a"));
        rightLine.setBackgroundColor(Color.parseColor("#8a8a8a"));
        switch(position) {
            case 0:
                borrowRadioButton.setTextColor(Color.parseColor("#3D5AFE"));
                leftLine.setBackgroundColor(Color.parseColor("#3D5AFE"));
                break;
            case 1:
                lendRadioButton.setTextColor(Color.parseColor("#3D5AFE"));
                rightLine.setBackgroundColor(Color.parseColor("#3D5AFE"));
                break;
            default:
                break;
        }
        viewPager.setCurrentItem(position);
    }
}
