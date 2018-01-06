package com.hrca.arrowstask;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.hrca.arrowstask.ArrowsView.ArrowsViewListener;

public class TaskActivity extends Activity implements ArrowsViewListener {

    public static final String PARCELABLE_HITS_KEY = "hit";
    public static final String PARCELABLE_MISSES_KEY = "miss";
    private int hits = 0;
    private int misses = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        ArrowsView arrows = (ArrowsView) findViewById(R.id.gridview);
        arrows.setListener(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PARCELABLE_HITS_KEY, hits);
        outState.putInt(PARCELABLE_MISSES_KEY, misses);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        hits = savedInstanceState.getInt(PARCELABLE_HITS_KEY, 0);
        misses = savedInstanceState.getInt(PARCELABLE_MISSES_KEY, 0);
    }

    @Override
    public void onArrowClicked(boolean hit) {
        if (hit)
            hits++;
        else
            misses++;
    }
}
