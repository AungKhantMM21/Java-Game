package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen implements Screen {
    private SpriteBatch batch;
    private ShapeRenderer shape;
    private BitmapFont gameFont;
    private BitmapFont hudFont;
    private Texture bgTexture;
    
    private Rectangle player;
    private Rectangle powerUpPrize;
    
    private float velocityY = 0;
    private boolean isLiquidState = false;
    private boolean prizeActive = true;
    private boolean isPaused = false;
    
    private int currentLevel = 1;
    private int score = 0;
    private int lives = 3;
    private GameCallback callback;
    
    private static final Color PLAYER_NORMAL = Color.RED;
    private static final Color PLAYER_LIQUID = Color.GREEN;
    private static final Color PRIZE_COLOR = Color.YELLOW;
    private static final Color FLOOR_COLOR = Color.DARK_GRAY;
    
    public interface GameCallback {
        void onPause();
        void onQuit();
        void onRestart();
    }
    
    public GameScreen(SpriteBatch batch, GameCallback callback) {
        this.batch = batch;
        this.callback = callback;
        this.shape = new ShapeRenderer();
        this.bgTexture = new Texture("background.png");
        
        this.gameFont = new BitmapFont();
        this.gameFont.getData().setScale(2.5f);
        
        this.hudFont = new BitmapFont();
        this.hudFont.getData().setScale(1.5f);
        
        this.player = new Rectangle(50, 50, 40, 80);
        this.powerUpPrize = new Rectangle(500, 50, 30, 30);
    }
    
    public void setLevel(int level) {
        this.currentLevel = level;
        this.score = 0;
        this.lives = 3;
        resetLevel();
    }
    
    private void resetLevel() {
        this.player = new Rectangle(50, 50, 40, 80);
        this.powerUpPrize = new Rectangle(500 - (currentLevel * 50), 50, 30, 30);
        this.velocityY = 0;
        this.isLiquidState = false;
        this.prizeActive = true;
    }
    
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.2f, 0.2f, 0.25f, 1);
        
        handleInput();
        
        if (!isPaused) {
            update(delta);
        }
        
        draw();
        drawHUD();
        
        if (isPaused) {
            drawPauseMenu();
        }
    }
    
    private void handleInput() {
        if (Gdx.input.isKeyJustPressed(Keys.ESCAPE)) {
            isPaused = !isPaused;
        }
        if (Gdx.input.isKeyJustPressed(Keys.R) && isPaused) {
            callback.onRestart();
        }
        if (Gdx.input.isKeyJustPressed(Keys.Q) && isPaused) {
            callback.onQuit();
        }
    }
    
    private void update(float delta) {
        float speed = isLiquidState ? 150f : 300f;
        
        if (Gdx.input.isKeyPressed(Keys.A)) player.x -= speed * delta;
        if (Gdx.input.isKeyPressed(Keys.D)) player.x += speed * delta;
        
        if (Gdx.input.isKeyJustPressed(Keys.SPACE) && player.y <= 50) {
            velocityY = 450f;
        }
        
        player.y += velocityY * delta;
        if (player.y > 50) {
            velocityY -= 1200f * delta;
        } else {
            player.y = 50;
            velocityY = 0;
        }
        
        if (prizeActive && player.overlaps(powerUpPrize)) {
            isLiquidState = true;
            prizeActive = false;
            score += currentLevel * 100;
            
            player.width = 80;
            player.height = 20;
        }
    }
    
    private void draw() {
        // Draw background
        batch.begin();
        batch.draw(bgTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.end();
        
        shape.begin(ShapeRenderer.ShapeType.Filled);
        
        // Draw floor
        shape.setColor(FLOOR_COLOR);
        shape.rect(0, 0, Gdx.graphics.getWidth(), 50);
        
        // Draw prize
        if (prizeActive) {
            shape.setColor(PRIZE_COLOR);
            shape.rect(powerUpPrize.x, powerUpPrize.y, powerUpPrize.width, powerUpPrize.height);
            
            // Prize glow
            shape.setColor(new Color(1f, 1f, 0f, 0.3f));
            shape.rect(powerUpPrize.x - 10, powerUpPrize.y - 10, 
                       powerUpPrize.width + 20, powerUpPrize.height + 20);
        }
        
        // Draw player
        shape.setColor(isLiquidState ? PLAYER_LIQUID : PLAYER_NORMAL);
        shape.rect(player.x, player.y, player.width, player.height);
        
        shape.end();
    }
    
    private void drawHUD() {
        batch.begin();
        
        // Level info
        hudFont.setColor(new Color(0.2f, 1f, 0.4f, 1f));
        hudFont.draw(batch, "LEVEL: " + currentLevel, 10, Gdx.graphics.getHeight() - 10);
        
        // Score
        hudFont.setColor(Color.YELLOW);
        hudFont.draw(batch, "SCORE: " + score, Gdx.graphics.getWidth() / 2 - 80, Gdx.graphics.getHeight() - 10);
        
        // Lives
        hudFont.setColor(Color.RED);
        hudFont.draw(batch, "LIVES: " + lives, Gdx.graphics.getWidth() - 150, Gdx.graphics.getHeight() - 10);
        
        // State indicator
        String stateText = isLiquidState ? "STATE: LIQUID" : "STATE: NORMAL";
        hudFont.setColor(isLiquidState ? new Color(0, 1, 0, 1) : Color.RED);
        hudFont.getData().setScale(1f);
        hudFont.draw(batch, stateText, 10, Gdx.graphics.getHeight() - 50);
        hudFont.getData().setScale(1.5f);
        
        // Controls hint
        hudFont.setColor(new Color(0.6f, 0.6f, 0.7f, 0.7f));
        hudFont.getData().setScale(1f);
        hudFont.draw(batch, "A/D - Move  SPACE - Jump  ESC - Pause", 10, 20);
        hudFont.getData().setScale(1.5f);
        
        batch.end();
    }
    
    private void drawPauseMenu() {
        shape.begin(ShapeRenderer.ShapeType.Filled);
        shape.setColor(0, 0, 0, 0.7f);
        shape.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shape.end();
        
        batch.begin();
        
        // Title
        gameFont.setColor(Color.YELLOW);
        gameFont.getData().setScale(3f);
        gameFont.draw(batch, "PAUSED", Gdx.graphics.getWidth() / 2 - 120, Gdx.graphics.getHeight() / 2 + 120);
        gameFont.getData().setScale(2.5f);
        
        // Menu options with better formatting
        hudFont.setColor(Color.WHITE);
        hudFont.getData().setScale(1.8f);
        
        float startY = Gdx.graphics.getHeight() / 2 + 20;
        float spacing = 60;
        
        // Resume option
        hudFont.setColor(Color.LIME);
        hudFont.draw(batch, "ESC - Resume Game", Gdx.graphics.getWidth() / 2 - 200, startY);
        
        // Restart option
        hudFont.setColor(Color.YELLOW);
        hudFont.draw(batch, "R   - Restart Level", Gdx.graphics.getWidth() / 2 - 200, startY - spacing);
        
        // Quit option
        hudFont.setColor(Color.RED);
        hudFont.draw(batch, "Q   - Quit to Menu", Gdx.graphics.getWidth() / 2 - 200, startY - spacing * 2);
        
        // Show lives and score info
        hudFont.setColor(Color.WHITE);
        hudFont.getData().setScale(1.5f);
        hudFont.draw(batch, "Lives: " + lives + "   |   Score: " + score + "   |   Level: " + currentLevel, 
                     Gdx.graphics.getWidth() / 2 - 250, startY - spacing * 3.5f);
        
        hudFont.getData().setScale(1.5f);
        batch.end();
    }
    
    @Override
    public void dispose() {
        if (gameFont != null) gameFont.dispose();
        if (hudFont != null) hudFont.dispose();
        if (bgTexture != null) bgTexture.dispose();
        if (shape != null) shape.dispose();
    }
    
    @Override
    public void resize(int width, int height) {}
    
    @Override
    public void pause() {}
    
    @Override
    public void resume() {}
    
    @Override
    public void hide() {}
    
    @Override
    public void show() {}
    
    public boolean isPaused() {
        return isPaused;
    }
    
    public int getCurrentLevel() {
        return currentLevel;
    }
    
    public int getLives() {
        return lives;
    }
}
