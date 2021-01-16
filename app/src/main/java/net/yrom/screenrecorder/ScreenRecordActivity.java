package net.yrom.screenrecorder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioGroup;

import me.yokeyword.fragmentation.SupportActivity;


public class ScreenRecordActivity extends SupportActivity {
    Fragment recordFragment;
    Fragment userFragment;
    FragmentManager manager;
    FragmentTransaction transaction;
    RadioGroup radioGroup;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_recorder);
        radioGroup=findViewById(R.id.radio);
         manager = getSupportFragmentManager();
        transaction= manager.beginTransaction();
         recordFragment = new RecordFragment();
        userFragment=new UserFragment();
        transaction.add(R.id.container, recordFragment, null);
        transaction.add(R.id.container, userFragment, null);
        transaction.hide(userFragment);
        transaction.show(recordFragment);
        transaction.commitAllowingStateLoss();
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                transaction=manager.beginTransaction();
                if (checkedId==R.id.record){
                    transaction.hide(userFragment);
                    transaction.show(recordFragment);
                }else {
                    transaction.hide(recordFragment);
                    transaction.show(userFragment);
                }
                transaction.commitAllowingStateLoss();
            }
        });
    }
}
