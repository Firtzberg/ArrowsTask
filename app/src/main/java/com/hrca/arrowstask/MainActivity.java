package com.hrca.arrowstask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {

    public static final int REQUEST_CODE_TASK = 524;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void startTask(View v) {
        Intent intent = new Intent(this, TaskActivity.class);
        startActivityForResult(intent, REQUEST_CODE_TASK);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_TASK) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user completed the task
                int hits = data.getIntExtra(TaskActivity.PARCELABLE_HITS_KEY, 0);
                Toast.makeText(this, String.valueOf(hits), Toast.LENGTH_LONG).show();
            }
        }
    }
}
