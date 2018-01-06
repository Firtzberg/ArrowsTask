package com.hrca.arrowstask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;

import com.hrca.arrowstask.ArrowsView.ArrowsViewListener;

/**
 * Activity with arrows task.
 */
public class TaskActivity extends Activity implements ArrowsViewListener {
    /**
     * Total duration of task in seconds.
     */
    public static final int TASK_DURATION = 10;

    public static final String PARCELABLE_HITS_KEY = "hit";
    public static final String PARCELABLE_MISSES_KEY = "miss";
    public static final String PARCELABLE_TIME_KEY = "t";
    /**
     * Number of hit arrows.
     */
    private int hits = 0;
    /**
     * Number of miss clicks.
     */
    private int misses = 0;
    /**
     * Timer counting down the task time.
     */
    private Timer timer;
    /**
     * Remaining time for timer to finish.
     */
    private int remainingTime = TASK_DURATION * 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        ArrowsView arrows = (ArrowsView) findViewById(R.id.gridview);
        arrows.setListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // create timer with remaining time
        timer = new Timer(remainingTime);
        timer.start();
    }

    @Override
    public void onPause() {
        // Destroy timer and update remaining time.
        timer.cancel();
        remainingTime = timer.getRemainingTime();
        timer = null;

        super.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PARCELABLE_HITS_KEY, hits);
        outState.putInt(PARCELABLE_MISSES_KEY, misses);
        outState.putInt(PARCELABLE_TIME_KEY, remainingTime);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        hits = savedInstanceState.getInt(PARCELABLE_HITS_KEY, 0);
        misses = savedInstanceState.getInt(PARCELABLE_MISSES_KEY, 0);
        remainingTime = savedInstanceState.getInt(PARCELABLE_TIME_KEY, TASK_DURATION * 1000);
    }

    @Override
    public void onArrowClicked(boolean hit) {
        if (hit)
            hits++;
        else
            misses++;
    }

    /**
     * Timer ending activity on expiration.
     */
    private class Timer extends CountDownTimer {
        /**
         * Time at which the activity should finish.
         */
        long expirationTime;

        public Timer(int remainingMilliseconds) {
            super(remainingMilliseconds, 1000);
            expirationTime = System.currentTimeMillis() + remainingMilliseconds;
        }

        /**
         * Gets the remaining to finish activity.
         *
         * @return Remaining time in milliseconds.
         */
        public int getRemainingTime() {
            return (int) (expirationTime - System.currentTimeMillis());
        }

        @Override
        public void onTick(long l) {
            //update time
        }

        @Override
        public void onFinish() {
            // Set hits into result and finish activity.
            Intent resultData = new Intent();
            resultData.putExtra(PARCELABLE_HITS_KEY, hits);
            TaskActivity.this.setResult(RESULT_OK, resultData);
            TaskActivity.this.finish();
        }
    }
}
