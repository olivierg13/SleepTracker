package com.og.health.sleeptracker.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.og.health.sleeptracker.R;
import com.og.health.sleeptracker.application.ExampleApplication;
import com.og.health.sleeptracker.databinding.ActivityDimBinding;
import com.og.health.sleeptracker.lib.receivers.ScreenOnOffReceiver;
import com.og.health.sleeptracker.lib.services.SleepTrackerService;
import com.og.health.sleeptracker.lib.utilities.SharedPreferencesUtilities;
import com.og.health.sleeptracker.schema.Record;
import com.og.health.sleeptracker.schema.RecordDao;

import java.util.Date;

/**
 * Created by olivier.goutay on 2/25/16.
 */
public class DimActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    /**
     * This activity databinding
     */
    private ActivityDimBinding mBinding;

    /**
     * To recognize double tap
     */
    private GestureDetectorCompat mDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFullScreen();

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_dim);
        mDetector = new GestureDetectorCompat(this, this);

        showToast();

        startService();
    }

    @Override
    protected void onStop() {
        stopService();
        unDimScreen();

        super.onStop();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        this.mDetector.onTouchEvent(event);
        // Be sure to call the superclass implementation
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        finish();

        return false;
    }

    /**
     * Start the {@link SleepTrackerService} on {@link #onCreate(Bundle)}
     */
    public void startService() {
        //Create a record in db
        RecordDao recordDao = ExampleApplication.getDaoSession().getRecordDao();
        recordDao.insertOrReplace(new Record(null, new Date(), new Date()));

        //Launch service
        Intent intent = new Intent(this, SleepTrackerService.class);
        startService(intent);
    }

    /**
     * Stop the {@link SleepTrackerService} on {@link #onStop()}
     */
    public void stopService() {
        //Stop the service
        Intent intent = new Intent(this, SleepTrackerService.class);
        stopService(intent);

        //End the record in db
        RecordDao recordDao = ExampleApplication.getDaoSession().getRecordDao();
        Record record = recordDao.queryBuilder().limit(1).orderDesc(RecordDao.Properties.Beginning).unique();
        record.setEnding(new Date());
        recordDao.insertOrReplace(record);

        //May be a new wake up time
        ScreenOnOffReceiver.handleScreenWakeUp(ExampleApplication.getContext());
    }

    /**
     * Show an explanation and then calls {@link #dimScreen()} after 2sec
     */
    private void showToast() {
        Toast.makeText(this, "Swipe on screen to come back", Toast.LENGTH_LONG).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                dimScreen();
            }
        }, 2000);
    }

    /**
     * Dim the screen to 0
     */
    private void dimScreen() {
        if (getWindow() != null) {
            WindowManager.LayoutParams wm = getWindow().getAttributes();

            //Store the previous dim for future use
            SharedPreferencesUtilities.storeFloatForKey(this, SharedPreferencesUtilities.SCREEN_DIM_VALUE, wm.screenBrightness);

            //Apply new dim
            wm.screenBrightness = 0.0f;
            getWindow().setAttributes(wm);
        }
    }

    /**
     * Undim the screen
     */
    private void unDimScreen() {
        if (getWindow() != null) {
            WindowManager.LayoutParams wm = getWindow().getAttributes();

            //Store the previous dim for future use
            float previousDim = SharedPreferencesUtilities.getFloatForKey(this, SharedPreferencesUtilities.SCREEN_DIM_VALUE);
            previousDim = previousDim == 0f ? 1f : previousDim;

            //Apply new dim
            wm.screenBrightness = previousDim;
            getWindow().setAttributes(wm);
        }
    }

    /**
     * Set this activity full screen by hiding the {@link #getActionBar()}
     * and setting the flag {@link WindowManager.LayoutParams#FLAG_FULLSCREEN}
     */
    private void setFullScreen() {
        //Fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //No action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }
}
