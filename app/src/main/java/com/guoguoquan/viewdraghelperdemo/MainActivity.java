package com.guoguoquan.viewdraghelperdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import com.guoguoquan.viewdraghelperdemo.View.MainLockViewGroup;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener{

    private FrameLayout bt_open_lock;
    private Button bt_bottom_menu;
    private MainLockViewGroup mainLockViewGroup;
    float x1 = 0;
    float x2 = 0;
    float y1 = 0;
    float y2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        bt_bottom_menu= (Button) findViewById(R.id.bt_bottom_menu);
        bt_open_lock = (FrameLayout) findViewById(R.id.rl_main_lockly);
        mainLockViewGroup=(MainLockViewGroup)findViewById(R.id.main);
        bt_open_lock.setOnTouchListener(this);
        bt_bottom_menu.setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
       switch (v.getId())
       {
           case R.id.rl_main_lockly:
               if (event.getAction() == MotionEvent.ACTION_DOWN) {
                   x1 = event.getX();
                   y1 = event.getY();
               }
               if (event.getAction() == MotionEvent.ACTION_MOVE) {
                   x2 = event.getX();
                   y2 = event.getY();
                   if (x1 - x2 > 0 && Math.abs(x1 - x2) > Math.abs(y1 - y2)) {
                       mainLockViewGroup.jumpRightMenu();
                   } else if (Math.abs(x1 - x2) > Math.abs(y1 - y2)) {
                       mainLockViewGroup.openLock(MainLockViewGroup.sliddirection.Right, 0, R.id.rl_main_lockly);
                   }
                   if (y1 - y2 > 5) {
                       mainLockViewGroup.openLock(MainLockViewGroup.sliddirection.UP, 0, R.id.rl_main_lockly);
                   }
               }
               break;
           case R.id.bt_bottom_menu:
               mainLockViewGroup.upmenu(0);
               break;

       }
        return true;
    }
}
