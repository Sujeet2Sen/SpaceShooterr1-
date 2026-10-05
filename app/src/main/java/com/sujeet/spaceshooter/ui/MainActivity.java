package com.sujeet.spaceshooter.ui;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private GameView spaceShooterView;
    private com.sujeet.spaceshooter.wrestling.GameView wrestlingGameView;

    private int activeMode = 0; // 0 = Mode Select, 1 = Space Shooter, 2 = Wrestling Game

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showGameSelect();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (activeMode == 1 && spaceShooterView != null && spaceShooterView.handleBackPressed()) {
                    return;
                }
                if (activeMode == 2 && wrestlingGameView != null && wrestlingGameView.handleBackPressed()) {
                    return;
                }
                if (activeMode != 0) {
                    showGameSelect();
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    private void showGameSelect() {
        activeMode = 0;
        cleanupActiveGames();

        GameSelectView gameSelectView = new GameSelectView(this, new GameSelectView.OnGameSelectedListener() {
            @Override
            public void onSelectSpaceShooter() {
                launchSpaceShooter();
            }

            @Override
            public void onSelectStreetFighter() {
                launchWrestlingGame();
            }
        });
        setContentView(gameSelectView);
    }

    private void launchSpaceShooter() {
        activeMode = 1;
        cleanupActiveGames();
        spaceShooterView = new GameView(this);
        setContentView(spaceShooterView);
    }

    private void launchWrestlingGame() {
        activeMode = 2;
        cleanupActiveGames();
        wrestlingGameView = new com.sujeet.spaceshooter.wrestling.GameView(this);
        setContentView(wrestlingGameView);
    }

    private void cleanupActiveGames() {
        if (spaceShooterView != null) {
            spaceShooterView.stopGame();
            spaceShooterView = null;
        }
        if (wrestlingGameView != null) {
            wrestlingGameView.stopGame();
            wrestlingGameView = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (spaceShooterView != null) {
            spaceShooterView.pauseGame();
        }
        if (wrestlingGameView != null) {
            wrestlingGameView.pauseGame();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (wrestlingGameView != null) {
            wrestlingGameView.resumeGame();
        }
    }
}
