package com.sujeet.spaceshooter.ui;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.sujeet.spaceshooter.fighter.ui.FightView;

public class MainActivity extends AppCompatActivity {

    private GameSelectView gameSelectView;
    private GameView spaceView;
    private FightView fightView;

    private int activeMode = 0; // 0 = Select, 1 = Space, 2 = Fighter

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showGameSelect();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (activeMode == 1 && spaceView != null && spaceView.handleBackPressed()) {
                    return;
                }
                if (activeMode == 2 && fightView != null && fightView.handleBackPressed()) {
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
        if (spaceView != null) {
            spaceView.stopGame();
            spaceView = null;
        }
        if (fightView != null) {
            fightView.stopGame();
            fightView = null;
        }
        gameSelectView = new GameSelectView(this, new GameSelectView.OnGameSelectedListener() {
            @Override
            public void onSelectSpaceShooter() {
                launchSpaceShooter();
            }

            @Override
            public void onSelectStreetFighter() {
                launchStreetFighter();
            }
        });
        setContentView(gameSelectView);
    }

    private void launchSpaceShooter() {
        activeMode = 1;
        spaceView = new GameView(this);
        setContentView(spaceView);
    }

    private void launchStreetFighter() {
        activeMode = 2;
        fightView = new FightView(this);
        fightView.onExitListener = this::showGameSelect;
        setContentView(fightView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (spaceView != null) {
            spaceView.pauseGame();
        }
    }
}
