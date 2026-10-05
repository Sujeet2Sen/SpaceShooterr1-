package com.sujeet.spaceshooter.ui;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.sujeet.spaceshooter.wrestling.GameView;

public class MainActivity extends AppCompatActivity {

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gameView = new GameView(this);
        setContentView(gameView);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (gameView != null && gameView.handleBackPressed()) {
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            gameView.pauseGame();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (gameView != null) {
            gameView.resumeGame();
        }
    }
}
