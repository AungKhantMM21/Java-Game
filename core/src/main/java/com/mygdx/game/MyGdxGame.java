package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;

public class MyGdxGame extends ApplicationAdapter {
    enum ScreenState {
        MENU, GAME
    }
    
    private ScreenState currentScreen = ScreenState.MENU;
    private Screen screen;
    private MenuScreen menuScreen;
    private GameScreen gameScreen;
    private int currentLevel = 1;

    @Override
    public void create() {
        showMenu(false);
    }
    
    private void showMenu(boolean canResume) {
        currentScreen = ScreenState.MENU;
        menuScreen = new MenuScreen(getBatch(), new MenuScreen.MenuCallback() {
            @Override
            public void onPlay() {
                currentLevel = 1;
                startNewGame(1);
            }

            @Override
            public void onResume() {
                if (gameScreen != null) {
                    currentScreen = ScreenState.GAME;
                    screen = gameScreen;
                }
            }

            @Override
            public void onRestart() {
                startNewGame(currentLevel);
            }

            @Override
            public void onQuit() {
                Gdx.app.exit();
            }
        }, canResume);
        
        screen = menuScreen;
    }

    private void startNewGame(int level) {
        if (gameScreen != null) {
            gameScreen.dispose();
        }
        
        gameScreen = new GameScreen(getBatch(), new GameScreen.GameCallback() {
            @Override
            public void onPause() {
                currentScreen = ScreenState.MENU;
                showMenu(true);
            }

            @Override
            public void onQuit() {
                currentScreen = ScreenState.MENU;
                showMenu(true);
            }
            
            @Override
            public void onRestart() {
                startNewGame(currentLevel);
            }
        });
        
        currentLevel = level;
        gameScreen.setLevel(level);
        currentScreen = ScreenState.GAME;
        screen = gameScreen;
    }

    @Override
    public void render() {
        if (screen != null) {
            screen.render(Gdx.graphics.getDeltaTime());
        }
    }

    private com.badlogic.gdx.graphics.g2d.SpriteBatch getBatch() {
        if (batch == null) {
            batch = new com.badlogic.gdx.graphics.g2d.SpriteBatch();
        }
        return batch;
    }
    
    private com.badlogic.gdx.graphics.g2d.SpriteBatch batch;

    @Override
    public void dispose() {
        if (screen != null) {
            screen.dispose();
        }
        if (menuScreen != null) {
            menuScreen.dispose();
        }
        if (gameScreen != null) {
            gameScreen.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
    }
}
