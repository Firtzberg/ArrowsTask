package com.hrca.arrowstask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.view.View;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends Activity {

    public static final int REQUEST_CODE_TASK = 524;
    public static final int REQUEST_CODE_SIGN_IN_LEADERBOARD = 1478;
    public static final int REQUEST_CODE_SIGN_IN_SUBMIT = 1479;
    public static final int REQUEST_CODE_LEADERBOARD_UI = 9004;
    public static final String SHARED_PREFERENCES_SCORE_COUNT = "n";
    public static final String PARCELABLE_SCORE_TO_DISPLAY_KEY = "score";
    public GoogleSignInAccount signedInAccount = null;
    public int scoreToDisplay = Integer.MIN_VALUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        signedInAccount = GoogleSignIn.getLastSignedInAccount(this);
        submitScores();
        if (signedInAccount == null)
            signInSilently();
        displayScore();
    }

    /**
     * Returns configured sign in options.
     * @return Google sign in options.
     */
    private GoogleSignInOptions getSignInOptions() {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN)
                .requestIdToken(getString(R.string.request_token))
                .build();
    }

    /**
     * Tries to sign in without starting a new intent.
     */
    private void signInSilently() {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                getSignInOptions());
        signInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            // The signed in account is stored in the task's result.
                            signedInAccount = task.getResult();
                            submitScores();
                        } else {
                            // Player will need to sign-in explicitly using via UI
                        }
                    }
                });
    }

    /**
     * Starts an intent to sign in.
     *
     * @param requestCode Request code with which the sign in intent is started.
     */
    private void startSignInIntent(int requestCode) {
        GoogleSignInClient signInClient = GoogleSignIn.getClient(this,
                getSignInOptions());
        Intent intent = signInClient.getSignInIntent();
        startActivityForResult(intent, requestCode);
    }

    /**
     * Starts the Task Activity for result.
     *
     * @param v View which caused this method to b invoked.
     */
    public void startTask(View v) {
        Intent intent = new Intent(this, TaskActivity.class);
        startActivityForResult(intent, REQUEST_CODE_TASK);
    }

    /**
     * Displays leader board.
     *
     * @param v View which caused this method to b invoked.
     */
    public void displayLeaderBoard(View v) {
        if (signedInAccount == null) {
            startSignInIntent(REQUEST_CODE_SIGN_IN_LEADERBOARD);
        } else {
            Games.getLeaderboardsClient(this, signedInAccount)
                    .getLeaderboardIntent(getString(R.string.leaderboard_id))
                    .addOnSuccessListener(new OnSuccessListener<Intent>() {
                        @Override
                        public void onSuccess(Intent intent) {
                            startActivityForResult(intent, REQUEST_CODE_LEADERBOARD_UI);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            new AlertDialog.Builder(MainActivity.this).setMessage(e.getMessage())
                                    .setNeutralButton(android.R.string.ok, null).show();
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SIGN_IN_LEADERBOARD || requestCode == REQUEST_CODE_SIGN_IN_SUBMIT) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // The signed in account is stored in the result.
                signedInAccount = task.getResult(ApiException.class);
                if (requestCode == REQUEST_CODE_SIGN_IN_LEADERBOARD) {
                    displayLeaderBoard(null);
                }
            } catch (ApiException apiException) {
                String message = apiException.getMessage();
                if (message != null && !message.isEmpty()) {
                    new AlertDialog.Builder(this).setMessage(message)
                            .setNeutralButton(android.R.string.ok, null).show();
                }
            }
        }
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_TASK) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user completed the task
                int hits = data.getIntExtra(TaskActivity.PARCELABLE_HITS_KEY, 0);
                queueScore(hits);
            }
        }
    }

    /**
     * Queues a score for submission.
     *
     * @param score Score to queue.
     */
    @SuppressLint("CommitPrefEdits")
    public void queueScore(int score) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        // get number of stored scores
        int count = sp.getInt(SHARED_PREFERENCES_SCORE_COUNT, 0);

        SharedPreferences.Editor editor = sp.edit();
        // add score
        editor.putInt("score" + count, score);
        // increment number of stored scores.
        editor.putInt(SHARED_PREFERENCES_SCORE_COUNT, count + 1);

        editor.commit();

        scoreToDisplay = score;
    }

    /**
     * Submits queued scores and dequeues them if successful.
     */
    public void submitScores() {
        if (signedInAccount == null)
            return;
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        // get number of stored scores
        int count = sp.getInt(SHARED_PREFERENCES_SCORE_COUNT, 0);

        // submit scores
        SharedPreferences.Editor editor = sp.edit();
        for (int scoreIndex = 0; scoreIndex < count; scoreIndex++) {
            Games.getLeaderboardsClient(this, signedInAccount)
                    .submitScore(getString(R.string.leaderboard_id), sp.getInt("score" + scoreIndex, 0));
            editor.remove("score" + scoreIndex);
        }
        // reset number of stored scores.
        editor.putInt(SHARED_PREFERENCES_SCORE_COUNT, 0);

        editor.apply();
    }

    /**
     * Displays score to be displayed if any.
     *
     * @return True if score was displayed.
     */
    public boolean displayScore() {
        if (scoreToDisplay != Integer.MIN_VALUE) {
            new AlertDialog.Builder(this).setMessage(String.valueOf(scoreToDisplay))
                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (signedInAccount == null) {
                                startSignInIntent(REQUEST_CODE_SIGN_IN_SUBMIT);
                            }
                        }
                    })
                    .show();
            scoreToDisplay = Integer.MIN_VALUE;
            return true;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle savedInstanceState) {
        if (scoreToDisplay != Integer.MIN_VALUE)
            savedInstanceState.putInt(PARCELABLE_SCORE_TO_DISPLAY_KEY, scoreToDisplay);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        scoreToDisplay = savedInstanceState.getInt(PARCELABLE_SCORE_TO_DISPLAY_KEY, Integer.MIN_VALUE);
    }
}
