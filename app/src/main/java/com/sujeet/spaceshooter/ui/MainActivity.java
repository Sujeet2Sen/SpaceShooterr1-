package com.sujeet.spaceshooter.ui;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.sujeet.spaceshooter.fighter.ui.FightView;
import com.sujeet.spaceshooter.fighter.ui.WweRosterView;

public class MainActivity extends AppCompatActivity {

    private GameView spaceView;
    private FightView fightView;
    private WweRosterView rosterView;

    private int activeMode = 0; // 0 = Select, 1 = Space, 2 = Roster, 3 = Fight

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
                if (activeMode == 3 && fightView != null && fightView.handleBackPressed()) {
                    showWweRoster();
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
        cleanupViews();
        GameSelectView gameSelectView = new GameSelectView(this, new GameSelectView.OnGameSelectedListener() {
            @Override
            public void onSelectSpaceShooter() {
                launchSpaceShooter();
            }

            @Override
            public void onSelectStreetFighter() {
                showWweRoster();
            }
        });
        setContentView(gameSelectView);
    }

    private void showWweRoster() {
        activeMode = 2;
        cleanupViews();
        rosterView = new WweRosterView(this, new WweRosterView.OnRosterActionListener() {
            @Override
            public void onStartFight() {
                launchStreetFighter();
            }

            @Override
            public void onBackToSelect() {
                showGameSelect();
            }
        });
        setContentView(rosterView);
    }

    private void launchSpaceShooter() {
        activeMode = 1;
        cleanupViews();
        spaceView = new GameView(this);
        setContentView(spaceView);
    }

    private void launchStreetFighter() {
        activeMode = 3;
        cleanupViews();
        fightView = new FightView(this);
        fightView.onExitListener = this::showWweRoster;
        setContentView(fightView);
    }

    private void cleanupViews() {
        if (spaceView != null) {
            spaceView.stopGame();
            spaceView = null;
        }
        if (fightView != null) {
            fightView.stopGame();
            fightView = null;
        }
        rosterView = null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (spaceView != null) {
            spaceView.pauseGame();
        }
    }
}
