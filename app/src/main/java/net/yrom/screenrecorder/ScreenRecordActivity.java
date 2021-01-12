package net.yrom.screenrecorder;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import android.widget.RadioGroup;


public class ScreenRecordActivity extends AppCompatActivity {
    RecordFragment recordFragment;
    UserFragment userFragment;
    FragmentManager manager;
    FragmentTransaction transaction;
    RadioGroup radioGroup;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_recorder);
        radioGroup=findViewById(R.id.radio);
         manager = getFragmentManager();
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
